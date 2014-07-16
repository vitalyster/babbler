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

package org.xmpp.extension.bytestreams.ibb;

import org.xmpp.Jid;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.bytestreams.ByteStreamListener;
import org.xmpp.extension.bytestreams.ByteStreamManager;
import org.xmpp.extension.bytestreams.ByteStreamSession;
import org.xmpp.stanza.IQEvent;
import org.xmpp.stanza.IQListener;
import org.xmpp.stanza.StanzaError;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.errors.ItemNotFound;
import org.xmpp.stanza.errors.UnexpectedRequest;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Schudt
 */
public class InBandByteStreamManager extends ByteStreamManager {

    public static final String NAMESPACE = "http://jabber.org/protocol/ibb";

    private static final Logger logger = Logger.getLogger(InBandByteStreamManager.class.getName());

    final Set<ByteStreamListener> byteStreamListeners = new CopyOnWriteArraySet<>();

    private final Map<String, IbbSession> ibbSessionMap = new ConcurrentHashMap<>();

    private InBandByteStreamManager(final XmppSession xmppSession) {
        super(xmppSession, NAMESPACE);

        xmppSession.addIQListener(new IQListener() {
            @Override
            public void handle(IQEvent e) {
                IQ iq = e.getIQ();
                if (e.isIncoming() && isEnabled() && !e.isConsumed() && iq.getType() == IQ.Type.SET) {
                    // Check, if the IQ carries some IBB related payload.
                    Data data = iq.getExtension(Data.class);
                    if (data != null) {
                        IbbSession ibbSession = getIbbSession(iq, data.getSessionId());
                        if (ibbSession != null) {
                            if (ibbSession.dataReceived(data)) {
                                xmppSession.send(iq.createResult());
                            } else {
                                // 2. Because the sequence number has already been used, the recipient returns an <unexpected-request/> error with a type of 'cancel'.
                                xmppSession.send(iq.createError(new StanzaError(StanzaError.Type.CANCEL, new UnexpectedRequest())));
                            }
                        }
                        e.consume();
                    } else {
                        Open open = iq.getExtension(Open.class);
                        if (open != null) {
                            // Somebody wants to create a IBB session with me.
                            // Notify the listeners.
                            notifyByteStreamEvent(new IbbEvent(InBandByteStreamManager.this, open.getSessionId(), xmppSession, iq, open.getBlockSize()));
                            e.consume();
                        } else {
                            Close close = iq.getExtension(Close.class);
                            if (close != null) {
                                IbbSession ibbSession = getIbbSession(iq, close.getSessionId());
                                if (ibbSession != null) {
                                    try {
                                        ibbSessionMap.remove(close.getSessionId());
                                        ibbSession.closedByPeer();
                                    } catch (IOException e1) {
                                        logger.log(Level.WARNING, e1.getMessage(), e1);
                                    } finally {
                                        xmppSession.send(iq.createResult());
                                    }
                                }
                                e.consume();
                            }
                        }
                    }
                }
            }
        });
        setEnabled(true);
    }

    private IbbSession getIbbSession(IQ iq, String sessionId) {
        // Check, if the session id is known.
        IbbSession ibbSession = ibbSessionMap.get(sessionId);
        if (ibbSession == null) {
            // 1. Because the session ID is unknown, the recipient returns an <item-not-found/> error with a type of 'cancel'.
            xmppSession.send(iq.createError(new StanzaError(new ItemNotFound())));
        }
        return ibbSession;
    }

    /**
     * Creates an in-band byte stream session.
     *
     * @param receiver  The receiver.
     * @param sessionId The session id.
     * @param blockSize The block size.
     * @return The in-band byte stream session.
     */
    public IbbSession createSession(Jid receiver, final String sessionId, int blockSize) {
        IbbSession ibbSession = new IbbSession(sessionId, xmppSession, receiver, blockSize);
        ibbSessionMap.put(ibbSession.getSessionId(), ibbSession);
        return ibbSession;
    }

    /**
     * Initiates a in-band byte stream session.
     *
     * @param receiver  The receiver.
     * @param sessionId The session id.
     * @param blockSize The block size.
     * @return The in-band byte stream session.
     * @throws XmppException
     */
    public ByteStreamSession initiateSession(Jid receiver, final String sessionId, int blockSize) throws XmppException {
        IbbSession ibbSession = createSession(receiver, sessionId, blockSize);
        ibbSession.open();
        return ibbSession;
    }
}
