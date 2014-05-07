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

package org.xmpp.sasl;

import org.xmpp.stream.ServerStreamElement;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The implementation of the {@code <challenge/>} element, which is sent during SASL negotiation.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#sasl-process-neg-challengeresponse">6.4.3.  Challenge-Response Sequence</a></cite></p>
 * <p>If necessary, the receiving entity challenges the initiating entity by sending a {@code <challenge/>} element qualified by the 'urn:ietf:params:xml:ns:xmpp-sasl' namespace; this element MAY contain XML character data (which MUST be generated in accordance with the definition of the SASL mechanism chosen by the initiating entity).</p>
 * </blockquote>
 *
 * @author Christian Schudt
 * @see Response
 */
@XmlRootElement
final class Challenge implements ServerStreamElement {

    @XmlValue
    private byte[] value;

    /**
     * Private default constructor, needed for unmarshalling.
     */
    private Challenge() {
    }

    /**
     * Gets the value, i.e. the challenge.
     *
     * @return The challenge.
     */
    public byte[] getValue() {
        return value;
    }
}
