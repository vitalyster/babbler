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

package rocks.xmpp.extensions.pubsub.model.errors;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The implementation of the {@code <invalid-subid/>} pubsub error. This class is a singleton.
 *
 * @author Christian Schudt
 * @see #INVALID_SUB_ID
 * @see <a href="https://xmpp.org/extensions/xep-0060.html#subscriber-unsubscribe-error-badsubid">6.2.3.5 Bad
 * Subscription ID</a>
 * @see <a href="https://xmpp.org/extensions/xep-0060.html#subscriber-configure-error-badsubid">6.3.4.5 Invalid
 * Subscription ID</a>
 * @see <a href="https://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-error-subid">6.5.9.2 Invalid Subscription
 * ID</a>
 */
@XmlRootElement(name = "invalid-subid")
@XmlType(factoryMethod = "create")
public final class InvalidSubId extends PubSubError {

    InvalidSubId() {
    }

    @SuppressWarnings("unused")
    private static InvalidSubId create() {
        return INVALID_SUB_ID;
    }
}