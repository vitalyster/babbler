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

package org.xmpp.stanza.client;

import org.xmpp.stanza.AbstractPresence;
import org.xmpp.stanza.StanzaError;
import org.xmpp.stream.ClientStreamElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The implementation of the {@code <presence/>} element for the client namespace ('jabber:client').
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "presence")
@XmlType(propOrder = {"from", "id", "to", "type", "status", "show", "priority", "extensions", "error"})
public final class Presence extends AbstractPresence implements ClientStreamElement {
    /**
     * Constructs an empty presence.
     */
    public Presence() {
    }

    /**
     * Constructs a presence of a specific type.
     *
     * @param type The type.
     */
    public Presence(Type type) {
        super(type);
    }

    /**
     * Constructs a presence with a specific 'show' attribute.
     *
     * @param show The 'show' attribute.
     */
    public Presence(Show show) {
        super(show);
    }


    @Override
    public Presence createError(StanzaError error) {
        Presence presence = new Presence(Type.ERROR);
        createError(presence, error);
        return presence;
    }
}
