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

package org.xmpp.tls;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the TLS failure case.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#tls-process-initiate-failure">5.4.2.2.  Failure Case</a></cite></p>
 * <p>If the failure case occurs, the receiving entity MUST return a {@code <failure/>} element qualified by the 'urn:ietf:params:xml:ns:xmpp-tls' namespace, close the XML stream, and terminate the underlying TCP connection.</p>
 * </blockquote>
 */
@XmlRootElement(name = "failure")
public final class Failure {
    private Failure() {
    }
}
