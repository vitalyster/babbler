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

package rocks.xmpp.extensions.rosterx.model;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.JidAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of the {@code <x/>} element in the {@code http://jabber.org/protocol/rosterx} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0144.html">XEP-0144: Roster Item Exchange</a>
 * @see <a href="http://xmpp.org/extensions/xep-0144.html#schema">11. XML Schema</a>
 */
@XmlRootElement(name = "x")
public final class ContactExchange {

    /**
     * http://jabber.org/protocol/rosterx
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/rosterx";

    @XmlElement(name = "item")
    private final List<Item> items = new ArrayList<>();

    /**
     * Gets the contact exchange items.
     *
     * @return The items.
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * The roster exchange item.
     */
    public static final class Item {

        @XmlAttribute
        private Action action;

        @XmlJavaTypeAdapter(JidAdapter.class)
        @XmlAttribute
        private Jid jid;

        @XmlAttribute
        private String name;

        @XmlElement(name = "group")
        private List<String> groups = new ArrayList<>();

        private Item() {
        }

        public Item(Jid jid, String name, List<String> groups, Action action) {
            this.jid = jid;
            this.name = name;
            this.groups.addAll(groups);
            this.action = action;
        }

        /**
         * Gets the JID.
         *
         * @return The JID.
         */
        public Jid getJid() {
            return jid;
        }

        /**
         * Gets the action indicating adding, deleting or modifying the roster item.
         *
         * @return The action.
         */
        public Action getAction() {
            return action;
        }

        /**
         * Gets the suggested name.
         *
         * @return The name.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the suggested roster groups
         *
         * @return The roster groups.
         */
        public List<String> getGroups() {
            return Collections.unmodifiableList(groups);
        }

        /**
         * The action for a roster item exchange.
         */
        public enum Action {
            /**
             * Suggests roster item addition.
             *
             * @see <a href="http://xmpp.org/extensions/xep-0144.html#add">3.1 Suggesting Roster Item Addition</a>
             */
            @XmlEnumValue("add")
            ADD,
            /**
             * Suggests roster item deletion.
             *
             * @see <a href="http://xmpp.org/extensions/xep-0144.html#delete">3.2 Suggesting Roster Item Deletion</a>
             */
            @XmlEnumValue("delete")
            DELETE,
            /**
             * Suggests roster item modification.
             *
             * @see <a href="http://xmpp.org/extensions/xep-0144.html#modify">3.3 Suggesting Roster Item Modification</a>
             */
            @XmlEnumValue("modify")
            MODIFY
        }
    }
}
