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

package org.xmpp.extension.pubsub.event;

import org.xmpp.Jid;
import org.xmpp.extension.dataforms.DataForm;

import javax.xml.bind.annotation.*;
import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * @author Christian Schudt
 */
@XmlRootElement(name = "event")
public final class Event {

    private Collection collection;

    private Configuration configuration;

    private Delete delete;

    private Items items;

    private Purge purge;

    private Subscription subscription;

    public static final class Collection {
        @XmlElementRef(name = "associate")
        private Associate associate;

        @XmlElementRef(name = "disassociate")
        private Disassociate disassociate;

        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Associate {
        @XmlAttribute(name = "node")
        private String node;

        private Associate() {

        }

        public Associate(String node) {
            this.node = node;
        }
    }

    public static final class Disassociate {
        @XmlAttribute(name = "node")
        private String node;

        private Disassociate() {

        }

        public Disassociate(String node) {
            this.node = node;
        }
    }

    public static final class Configuration {

        @XmlAttribute(name = "node")
        private String node;

        @XmlElementRef
        private DataForm dataForm;
    }

    public static final class Delete {
        @XmlAttribute(name = "node")
        private String node;

        @XmlElementRef(name = "redirect")
        private Redirect redirect;

    }

    public static final class Items {

        @XmlElementRef(name = "item")
        private List<Item> items;

        @XmlElementRef(name = "retract")
        private List<Retract> retracts;

        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Item {
        @XmlAnyElement(lax = true)
        private List<Object> objects;

        @XmlAttribute(name = "id")
        private String id;

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "publisher")
        private String publisher;
    }

    public static final class Purge {
        @XmlAttribute(name = "node")
        private String node;
    }

    public static final class Redirect {
        @XmlAttribute(name = "uri")
        private URI uri;
    }

    public static final class Retract {
        @XmlAttribute(name = "id")
        private String id;
    }

    public static final class Subscription {
        @XmlAttribute(name = "expiry")
        private Date expiry;

        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlAttribute(name = "node")
        private String node;

        @XmlAttribute(name = "subid")
        private String subid;

        @XmlAttribute(name = "subscription")
        private Type type;

        @XmlEnum
        public enum Type {
            @XmlEnumValue("none")
            NONE,
            @XmlEnumValue("pending")
            PENDING,
            @XmlEnumValue("subscribed")
            SUBSCRIBED,
            @XmlEnumValue("unconfigured")
            UNCONFIGURED
        }
    }
}
