/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2019 Christian Schudt
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

package rocks.xmpp.core.stanza.model.errors;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The implementation of the {@code <bad-request/>} stanza error.
 * <blockquote>
 * <p><cite><a href="https://xmpp.org/rfcs/rfc6120.html#stanzas-error-conditions-bad-request">8.3.3.1.
 * bad-request</a></cite></p>
 * <p>The sender has sent a stanza containing XML that does not conform to the appropriate schema or that cannot be
 * processed (e.g., an IQ stanza that includes an unrecognized value of the 'type' attribute, or an element that is
 * qualified by a recognized namespace but that violates the defined syntax for the element); the associated error type
 * SHOULD be "modify".</p>
 * </blockquote>
 * This class is a singleton.
 *
 * @see #BAD_REQUEST
 */
@XmlRootElement(name = "bad-request")
@XmlType(factoryMethod = "create")
final class BadRequest extends Condition {

    BadRequest() {
    }

    @SuppressWarnings("unused")
    private static BadRequest create() {
        return (BadRequest) BAD_REQUEST;
    }
}