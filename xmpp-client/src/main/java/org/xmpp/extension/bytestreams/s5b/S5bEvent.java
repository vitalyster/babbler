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

package org.xmpp.extension.bytestreams.s5b;

import org.xmpp.XmppSession;
import org.xmpp.extension.bytestreams.ByteStreamEvent;
import org.xmpp.extension.bytestreams.ByteStreamSession;
import org.xmpp.stanza.StanzaError;
import org.xmpp.stanza.client.IQ;
import org.xmpp.stanza.errors.ItemNotFound;
import org.xmpp.stanza.errors.NotAcceptable;

import java.io.IOException;
import java.util.List;

/**
 * @author Christian Schudt
 */
final class S5bEvent extends ByteStreamEvent {

    private final XmppSession xmppSession;

    private final IQ iq;

    private final List<StreamHost> streamHosts;

    public S5bEvent(Object source, String sessionId, XmppSession xmppSession, IQ iq, List<StreamHost> streamHosts) {
        super(source, sessionId);
        this.xmppSession = xmppSession;
        this.iq = iq;
        this.streamHosts = streamHosts;
    }

    @Override
    public ByteStreamSession accept() throws IOException {
        try {
            // 5.3.2 Target Establishes SOCKS5 Connection with StreamHost/Requester
            // 6.3.2 Target Establishes SOCKS5 Connection with Proxy
            S5bSession s5bSession = Socks5ByteStreamManager.createS5bSession(iq.getFrom(), iq.getTo(), getSessionId(), streamHosts);
            // 5.3.3 Target Acknowledges Bytestream
            // 6.3.3 Target Acknowledges Bytestream
            IQ result = iq.createResult();
            result.setExtension(Socks5ByteStream.streamHostUsed(s5bSession.getStreamHost()));
            xmppSession.send(iq);
            return s5bSession;
        } catch (IOException e) {
            // If the Target tries but is unable to connect to any of the StreamHosts and it does not wish to attempt a connection from its side, it MUST return an <item-not-found/> error to the Requester.
            xmppSession.send(iq.createError(new StanzaError(new ItemNotFound())));
            throw e;
        }
    }

    @Override
    public void reject() {
        // Else if the Target is unwilling to accept the bytestream, it MUST return an error of <not-acceptable/> to the Requester.
        xmppSession.send(iq.createError(new StanzaError(new NotAcceptable())));
    }
}
