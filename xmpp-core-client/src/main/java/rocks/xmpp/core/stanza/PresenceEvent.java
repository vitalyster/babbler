/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.core.stanza;

import rocks.xmpp.core.stanza.model.Presence;

import java.util.function.Consumer;

/**
 * A presence event is fired whenever a presence stanza is received or sent.
 * <p>
 * This class is immutable.
 *
 * @author Christian Schudt
 * @see rocks.xmpp.core.session.XmppSession#addInboundPresenceListener(Consumer)
 * @see rocks.xmpp.core.session.XmppSession#addOutboundPresenceListener(Consumer)
 */
public final class PresenceEvent extends StanzaEvent<Presence> {
    /**
     * Constructs a presence event.
     *
     * @param source   The object on which the event initially occurred.
     * @param presence The presence stanza.
     * @param inbound  True, if the stanza is inbound.
     * @throws IllegalArgumentException if source is null.
     */
    public PresenceEvent(Object source, Presence presence, boolean inbound) {
        super(source, presence, inbound);
    }

    /**
     * Gets the presence.
     *
     * @return The presence.
     */
    public final Presence getPresence() {
        return stanza;
    }
}
