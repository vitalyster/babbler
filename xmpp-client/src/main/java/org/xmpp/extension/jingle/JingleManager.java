/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.xmpp.extension.jingle;

import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.jingle.apps.ApplicationFormat;
import org.xmpp.extension.jingle.apps.filetransfer.JingleFileTransfer;
import org.xmpp.extension.jingle.errors.UnknownSession;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaError;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.errors.BadRequest;
import org.xmpp.stanza.errors.ItemNotFound;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Christian Schudt
 */
public final class JingleManager extends ExtensionManager {

    private final Set<Class<? extends ApplicationFormat>> supportedApplicationFormats = new HashSet<>();

    private Map<String, JingleSession> jingleSessionMap = new ConcurrentHashMap<>();

    private JingleManager(final XmppSession xmppSession) {
        super(xmppSession);

        supportedApplicationFormats.add(JingleFileTransfer.class);

        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.SET) {
                    Jingle jingle = iq.getExtension(Jingle.class);
                    if (jingle != null) {

                        // The value of the 'action' attribute MUST be one of the following.
                        // If an entity receives a value not defined here, it MUST ignore the attribute and MUST return a <bad-request/> error to the sender.
                        // There is no default value for the 'action' attribute.
                        if (jingle.getAction() == null) {
                            xmppSession.send(iq.createError(new StanzaError(new BadRequest(), "No valid action attribute set.")));
                        } else if (jingle.getSessionId() == null) {
                            xmppSession.send(iq.createError(new StanzaError(new BadRequest(), "No session id set.")));
                        } else if (jingle.getAction() == Jingle.Action.SESSION_INITIATE) {

                            // Check if the Jingle request is not mal-formed, otherwise return a bad-request error.
                            // See 6.3.2 Errors
                            if (jingle.getContents().isEmpty()) {
                                xmppSession.send(iq.createError(new StanzaError(new BadRequest(), "No contents found.")));
                            } else {
                                boolean hasContentWithDispositionSession = false;
                                boolean hasSupportedApplications = false;
                                boolean hasSupportedTransports = false;
                                // Check if we support the application format and transport method and at least one content element has a disposition of "session".
                                for (Jingle.Content content : jingle.getContents()) {
                                    // Check if the content disposition is "session" (default value is "session").
                                    if (!hasContentWithDispositionSession && ("session".equals(content.getDisposition()) || content.getDisposition() == null)) {
                                        hasContentWithDispositionSession = true;
                                    }

                                    if (!hasSupportedApplications && content.getApplicationFormat() != null) {
                                        hasSupportedApplications = true;
                                    }

                                    if (!hasSupportedTransports && content.getTransportMethod() != null) {
                                        hasSupportedTransports = true;
                                    }
                                }

                                if (!hasContentWithDispositionSession) {
                                    // When sending a session-initiate with one <content/> element,
                                    // the value of the <content/> element's 'disposition' attribute MUST be "session"
                                    // (if there are multiple <content/> elements then at least one MUST have a disposition of "session");
                                    // if this rule is violated, the responder MUST return a <bad-request/> error to the initiator.
                                    xmppSession.send(iq.createError(new StanzaError(new BadRequest(), "No content with disposition 'session' found.")));
                                } else {
                                    // If the request was ok, immediately acknowledge the initiation request.
                                    // See 6.3.1 Acknowledgement
                                    xmppSession.send(iq.createResult());

                                    // However, after acknowledging the session initiation request,
                                    // the responder might subsequently determine that it cannot proceed with negotiation of the session
                                    // (e.g., because it does not support any of the offered application formats or transport methods,
                                    // because a human user is busy or unable to accept the session, because a human user wishes to formally decline
                                    // the session, etc.). In these cases, the responder SHOULD immediately acknowledge the session initiation request
                                    // but then terminate the session with an appropriate reason as described in the Termination section of this document.

                                    if (!hasSupportedApplications) {
                                        // Terminate the session with <unsupported-applications/>.
                                        xmppSession.send(new IQ(iq.getFrom(), IQ.Type.SET, new Jingle(jingle.getSessionId(), Jingle.Action.SESSION_TERMINATE, new Jingle.Reason(new Jingle.Reason.UnsupportedApplications()))));
                                    } else if (!hasSupportedTransports) {
                                        // Terminate the session with <unsupported-transports/>.
                                        xmppSession.send(new IQ(iq.getFrom(), IQ.Type.SET, new Jingle(jingle.getSessionId(), Jingle.Action.SESSION_TERMINATE, new Jingle.Reason(new Jingle.Reason.UnsupportedTransports()))));
                                    } else {
                                        // Everything is fine, notify the listeners.
                                        //notifyJingleListeners(new JingleEvent(JingleManager.this, xmppSession, iq, jingle));
                                    }
                                }
                            }
                        } else {

                            // Another action (!= session-initiate) has been sent.
                            // Check if we know the session.
                            JingleSession jingleSession = jingleSessionMap.get(jingle.getSessionId());

                            if (jingleSession == null) {
                                // If we receive a non-session-initiate Jingle action with an unknown session id,
                                // return <item-not-found/> and <unknown-session/>
                                StanzaError stanzaError = new StanzaError(new ItemNotFound());
                                stanzaError.setExtension(new UnknownSession());
                                xmppSession.send(iq.createError(stanzaError));
                            } else {
                                jingleSession.notifyJingleListeners(new JingleEvent(JingleManager.this, xmppSession, iq, jingle));
                                xmppSession.send(iq.createResult());
                            }
                        }
                        e.consume();
                    }
                }
            }
        });
    }

    public JingleSession createSession(Jid responder, Jingle.Content... contents) throws XmppException {
        if (responder == null) {
            throw new IllegalArgumentException("responder must not be null.");
        }
        if (contents.length == 0) {
            throw new IllegalArgumentException("no content provided.");
        }
        String sessionId = UUID.randomUUID().toString();
        JingleSession jingleSession = new JingleSession(sessionId, responder, true, xmppSession, this, contents);

        // Register the session. It is now in pending state until it has been accepted or terminated.
        jingleSessionMap.put(sessionId, jingleSession);

        return jingleSession;
    }

    void removeSession(String sessionId) {
        jingleSessionMap.remove(sessionId);
    }
}
