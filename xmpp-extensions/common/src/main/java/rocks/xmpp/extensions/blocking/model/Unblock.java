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

package rocks.xmpp.extensions.blocking.model;

import java.util.Collection;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.xmpp.addr.Jid;

/**
 * The implementation of the {@code <unblock/>} element in the {@code urn:xmpp:blocking} namespace.
 *
 * <p>This class is immutable.</p>
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0191.html">XEP-0191: Blocking Command</a>
 * @see <a href="https://xmpp.org/extensions/xep-0191.html#schema-blocking">XML Schema</a>
 */
@XmlRootElement
public final class Unblock extends Blockable {

    public Unblock() {
    }

    /**
     * @param unblockedItems The unblocked items.
     */
    public Unblock(Collection<Jid> unblockedItems) {
        super(unblockedItems);
    }

    @Override
    public final String toString() {
        return "Unblocked: " + getItems();
    }
}
