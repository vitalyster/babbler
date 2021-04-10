/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2020 Christian Schudt
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

package rocks.xmpp.extensions.caps.client;

import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.stanza.OutboundPresenceHandler;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.extensions.caps.AbstractEntityCapabilities1Protocol;
import rocks.xmpp.extensions.caps.EntityCapabilitiesCache;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;

/**
 * @author Christian Schudt
 */
public class ClientEntityCapabilities1Protocol extends AbstractEntityCapabilities1Protocol
        implements OutboundPresenceHandler {

    private final ClientEntityCapabilitiesSupport capsSupport;

    public ClientEntityCapabilities1Protocol(XmppSession xmppSession) {
        super(xmppSession.getManager(ServiceDiscoveryManager.class),
                xmppSession.getManager(EntityCapabilitiesCache.class));
        this.capsSupport = new ClientEntityCapabilitiesSupport(xmppSession, this);
    }

    @Override
    public final void handleOutboundPresence(PresenceEvent e) {
        capsSupport.handleOutboundPresence(e);
    }
}
