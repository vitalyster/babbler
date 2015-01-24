/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

package rocks.xmpp.extensions.disco.model.items;

import rocks.xmpp.core.Jid;
import rocks.xmpp.extensions.rsm.model.ResultSetItem;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.UUID;

/**
 * The implementation of the {@code <item/>} element in the {@code http://jabber.org/protocol/disco#item} namespace, used for item discovery.
 *
 * @author Christian Schudt
 */
public final class Item implements ResultSetItem {

    @XmlTransient
    private String id;

    @XmlAttribute
    private Jid jid;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String node;

    private Item() {
    }

    public Item(Jid jid, String node, String name, String id) {
        this.jid = jid;
        this.node = node;
        this.name = name;
        this.id = id;
    }

    public Item(Jid jid, String node) {
        this(jid, node, null, UUID.randomUUID().toString());
    }

    public Item(Jid jid, String node, String name) {
        this(jid, node, name, UUID.randomUUID().toString());
    }

    /**
     * Gets the id of this item.
     *
     * @return The id.
     */
    public String getId() {
        return id;
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
     * Gets the name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the node.
     *
     * @return The node.
     */
    public String getNode() {
        return node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(jid.toString());
        if (node != null) {
            sb.append(" / ");
            sb.append(node);
        }
        if (name != null) {
            sb.append(" (");
            sb.append(name);
            sb.append(")");
        }
        return sb.toString();
    }
}
