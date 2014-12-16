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

package rocks.xmpp.extensions.disco;

import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.rsm.ResultSetProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A default item provider for Service Discovery. When items are requested via Service Discovery, this class provides the items
 * and if requested with Result Set Management, it also takes care of providing the correct number of items or the total count of items.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0030.html#items-nodes">XEP-0030: Service Discovery 4.2 Items Nodes</a>
 * @see <a href="http://xmpp.org/extensions/xep-0059.html#examples">XEP-0059: Result Set Management 3. Examples</a>
 */
public final class DefaultItemProvider implements ResultSetProvider<Item> {

    private final List<Item> items = Collections.synchronizedList(new ArrayList<Item>());

    public DefaultItemProvider() {
    }

    public DefaultItemProvider(Collection<Item> items) {
        this.items.addAll(items);
    }

    @Override
    public List<Item> getItems() {
        return items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public List<Item> getItems(int index, int maxSize) {
        return items.subList(index, index + maxSize);
    }

    @Override
    public List<Item> getItemsAfter(String itemId, int maxSize) {
        synchronized (items) {
            int fromIndex = indexOf(itemId) + 1;
            int toIndex = fromIndex + maxSize;
            if (fromIndex < 0 || toIndex > items.size() || fromIndex > toIndex) {
                return Collections.emptyList();
            }
            return items.subList(fromIndex, fromIndex + maxSize);
        }
    }

    @Override
    public List<Item> getItemsBefore(String itemId, int maxSize) {
        synchronized (items) {
            int fromIndex = indexOf(itemId) - maxSize;
            int toIndex = fromIndex - 1 + maxSize;
            if (fromIndex < 0 || toIndex > items.size() || fromIndex > toIndex) {
                return Collections.emptyList();
            }
            return items.subList(fromIndex, fromIndex + maxSize);
        }
    }

    @Override
    public int indexOf(String itemId) {
        synchronized (items) {
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                if (item.getId() != null && item.getId().equals(itemId)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
