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
 * The implementation of the {@code <item-required/>} pubsub error. This class is a singleton.
 *
 * @author Christian Schudt
 * @see #ITEM_REQUIRED
 * @see <a href="https://xmpp.org/extensions/xep-0060.html#publisher-publish-error-badrequest">7.1.3.6 Request Does Not
 * Match Configuration</a>
 * @see <a href="https://xmpp.org/extensions/xep-0060.html#publisher-delete-error-itemid">7.2.3.4 Item or ItemID
 * Required</a>
 */
@XmlRootElement(name = "item-required")
@XmlType(factoryMethod = "create")
public final class ItemRequired extends PubSubError {

    ItemRequired() {
    }

    @SuppressWarnings("unused")
    private static ItemRequired create() {
        return ITEM_REQUIRED;
    }
}