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

package org.xmpp.stanza;

/**
 * An IQ event is fired whenever an IQ stanza is received or sent.
 *
 * @author Christian Schudt
 * @see IQListener
 */
public final class IQEvent extends StanzaEvent<IQ> {
    /**
     * Constructs an IQ event.
     *
     * @param source   The object on which the event initially occurred.
     * @param iq       The IQ stanza.
     * @param incoming True, if the stanza is incoming.
     * @throws IllegalArgumentException if source is null.
     */
    public IQEvent(Object source, IQ iq, boolean incoming) {
        super(source, iq, incoming);
    }

    /**
     * Gets the IQ stanza.
     *
     * @return The IQ stanza.
     */
    public IQ getIQ() {
        return stanza;
    }
}
