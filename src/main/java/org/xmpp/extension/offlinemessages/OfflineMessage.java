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

package org.xmpp.extension.offlinemessages;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "offline")
public final class OfflineMessage {

    @XmlElement(name = "item")
    private List<Item> items = new ArrayList<>();

    @XmlElement(name = "fetch")
    private String fetch;

    @XmlElement(name = "purge")
    private String purge;

    public OfflineMessage() {
    }

    public OfflineMessage(boolean fetch, boolean purge) {
        this.fetch = fetch ? "" : null;
        this.purge = purge ? "" : null;
    }

    public List<Item> getItems() {
        return items;
    }

    public static final class Item {
        @XmlAttribute(name = "node")
        private String id;

        //@XmlAttribute(name = "jid")
        //private Jid jid;

        @XmlAttribute(name = "action")
        private Action action;

        private Item() {
        }

        public Item(String id, Action action) {
            this.id = id;
            this.action = action;
        }

        @XmlEnum
        public enum Action {
            @XmlEnumValue("remove")
            REMOVE,
            @XmlEnumValue("view")
            VIEW
        }
    }
}
