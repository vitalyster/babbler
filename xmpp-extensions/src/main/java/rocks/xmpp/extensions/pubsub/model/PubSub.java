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

package rocks.xmpp.extensions.pubsub.model;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.util.adapters.InstantAdapter;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.pubsub.model.errors.ClosedNode;
import rocks.xmpp.extensions.pubsub.model.errors.ConfigurationRequired;
import rocks.xmpp.extensions.pubsub.model.errors.InvalidJid;
import rocks.xmpp.extensions.pubsub.model.errors.InvalidOptions;
import rocks.xmpp.extensions.pubsub.model.errors.InvalidPayload;
import rocks.xmpp.extensions.pubsub.model.errors.InvalidSubId;
import rocks.xmpp.extensions.pubsub.model.errors.ItemForbidden;
import rocks.xmpp.extensions.pubsub.model.errors.ItemRequired;
import rocks.xmpp.extensions.pubsub.model.errors.JidRequired;
import rocks.xmpp.extensions.pubsub.model.errors.MaxItemsExceeded;
import rocks.xmpp.extensions.pubsub.model.errors.MaxNodesExceeded;
import rocks.xmpp.extensions.pubsub.model.errors.NodeIdRequired;
import rocks.xmpp.extensions.pubsub.model.errors.NotInRosterGroup;
import rocks.xmpp.extensions.pubsub.model.errors.NotSubscribed;
import rocks.xmpp.extensions.pubsub.model.errors.PayloadRequired;
import rocks.xmpp.extensions.pubsub.model.errors.PayloadTooBig;
import rocks.xmpp.extensions.pubsub.model.errors.PendingSubscription;
import rocks.xmpp.extensions.pubsub.model.errors.PresenceSubscriptionRequired;
import rocks.xmpp.extensions.pubsub.model.errors.SubIdRequired;
import rocks.xmpp.extensions.pubsub.model.errors.TooManySubscriptions;
import rocks.xmpp.extensions.pubsub.model.errors.Unsupported;
import rocks.xmpp.extensions.pubsub.model.event.Event;
import rocks.xmpp.extensions.pubsub.model.owner.PubSubOwner;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of the {@code <pubsub/>} element in the {@code http://jabber.org/protocol/pubsub} namespace.
 * <p>
 * Child elements are created with a bunch of static factory methods.
 * </p>
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0060.html">XEP-0060: Publish-Subscribe</a>
 * @see <a href="http://xmpp.org/extensions/xep-0060.html#schemas-pubsub">XML Schema</a>
 */
@XmlRootElement(name = "pubsub")
@XmlSeeAlso({Unsupported.class, InvalidJid.class, PresenceSubscriptionRequired.class, NotInRosterGroup.class, ClosedNode.class, PendingSubscription.class, TooManySubscriptions.class, ConfigurationRequired.class, SubIdRequired.class, NotSubscribed.class, NotSubscribed.class, InvalidSubId.class, JidRequired.class, InvalidOptions.class, PayloadTooBig.class, InvalidPayload.class, ItemRequired.class, PayloadRequired.class, ItemForbidden.class, NodeIdRequired.class, MaxItemsExceeded.class, MaxNodesExceeded.class,
        Event.class,
        PubSubOwner.class
})
public final class PubSub {

    /**
     * http://jabber.org/protocol/pubsub
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";

    private Create create;

    private Configure configure;

    private Subscribe subscribe;

    private Options options;

    @XmlElements({
            @XmlElement(name = "affiliations", type = AffiliationsElement.class),
            @XmlElement(name = "default", type = Default.class),
            @XmlElement(name = "items", type = Items.class),
            @XmlElement(name = "publish", type = Publish.class),
            @XmlElement(name = "retract", type = Retract.class),
            @XmlElement(name = "subscription", type = SubscriptionInfo.class),
            @XmlElement(name = "subscriptions", type = Subscriptions.class),
            @XmlElement(name = "unsubscribe", type = Unsubscribe.class),
    })
    private PubSubChildElement type;

    @XmlElement(name = "publish-options")
    private PublishOptions publishOptions;

    private PubSub() {
    }

    private PubSub(Create create, Configure configure) {
        this.create = create;
        this.configure = configure;
    }

    private PubSub(Subscribe subscribe, Options options) {
        this.subscribe = subscribe;
        this.options = options;
    }

    private PubSub(Options options) {
        this.options = options;
    }

    private PubSub(PubSubChildElement pubSubChildElement) {
        this.type = pubSubChildElement;
    }

    private PubSub(Publish publish, PublishOptions publishOptions) {
        this.type = publish;
        this.publishOptions = publishOptions;
    }

    private PubSub(Configure configure) {
        this.configure = configure;
    }

    private PubSub(Subscribe subscribe) {
        this.subscribe = subscribe;
    }

    /**
     * Creates a pubsub element with an {@code <affiliations/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <affiliations/>
     * </pubsub>
     * }
     * </pre>
     *
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-affiliations">5.7 Retrieve Affiliations</a>
     */
    public static PubSub withAffiliations() {
        return withAffiliations(null);
    }

    /**
     * Creates a pubsub element with an {@code <affiliations/>} child element and a 'node' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     * <affiliations node='node6'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-affiliations">5.7 Retrieve Affiliations</a>
     */
    public static PubSub withAffiliations(String node) {
        return new PubSub(new AffiliationsElement(node));
    }

    /**
     * Creates a pubsub element with an {@code <configure/>} child element and a 'node' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <configure node='princely_musings'>
     *         <x xmlns='jabber:x:data' type='submit'>
     *             <field var='FORM_TYPE' type='hidden'>
     *                  <value>http://jabber.org/protocol/pubsub#node_config</value>
     *             </field>
     *             <field var='pubsub#tempsub'><value>true</value></field>
     *         </x>
     *     </configure>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node              The node.
     * @param configurationForm The configuration form.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#impl-tempsub">12.4 Temporary Subscriptions</a>
     */
    public static PubSub withConfigure(String node, DataForm configurationForm) {
        return new PubSub(new Configure(node, configurationForm));
    }

    /**
     * Creates a pubsub element with an {@code <create/>} child element and a 'node' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <create node='princely_musings'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create">8.1 Create a Node</a>
     */
    public static PubSub withCreate(String node) {
        return new PubSub(new Create(node), null);
    }

    /**
     * Creates a pubsub element with an {@code <create/>} and {@code <configure/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <create node='princely_musings'/>
     *     <configure>
     *         <x xmlns='jabber:x:data' type='submit'>
     *         </x>
     *     </configure>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node              The node.
     * @param configurationForm The configuration form.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#owner-create-and-configure">8.1.3 Create and Configure a Node</a>
     */
    public static PubSub withCreate(String node, DataForm configurationForm) {
        return new PubSub(new Create(node), new Configure(configurationForm));
    }

    /**
     * Creates a pubsub element with a {@code <subscriptions/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <subscriptions/>
     * </pubsub>
     * }
     * </pre>
     *
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-subscriptions">5.6 Retrieve Subscriptions</a>
     */
    public static PubSub withSubscriptions() {
        return withSubscriptions(null);
    }

    /**
     * Creates a pubsub element with a {@code <subscriptions/>} child element with a 'node' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <subscriptions node='princely_musings'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#entity-subscriptions">5.6 Retrieve Subscriptions</a>
     */
    public static PubSub withSubscriptions(String node) {
        return new PubSub(new Subscriptions(node));
    }

    /**
     * Creates a pubsub element with a {@code <subscribe/>} child element with a 'node' and 'jid' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <subscribe node='princely_musings' jid='francisco@denmark.lit'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @param jid  The JID.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-subscribe">6.1 Subscribe to a Node</a>
     */
    public static PubSub withSubscribe(String node, Jid jid) {
        return new PubSub(new Subscribe(node, jid));
    }

    /**
     * Creates a pubsub element with a {@code <subscribe/>} and {@code <options/>} child element with a 'node' and 'jid' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <subscribe node='princely_musings' jid='francisco@denmark.lit'/>
     *     <options>
     *         <x xmlns='jabber:x:data' type='submit'>
     *         </x>
     *     </options>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node     The node.
     * @param jid      The JID.
     * @param dataForm The dataForm.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-subandconfig">6.3.7 Subscribe and Configure</a>
     */
    public static PubSub withSubscribe(String node, Jid jid, DataForm dataForm) {
        return new PubSub(new Subscribe(node, jid), new Options(dataForm));
    }

    /**
     * Creates a pubsub element with an {@code <options/>} child element with a 'node' and 'jid' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <options>
     *         <x xmlns='jabber:x:data' type='submit'>
     *         </x>
     *     </options>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node     The node.
     * @param jid      The JID.
     * @param subid    The subscription id.
     * @param dataForm The data form.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-configure-submit">6.3.5 Form Submission</a>
     */
    public static PubSub withOptions(String node, Jid jid, String subid, DataForm dataForm) {
        return new PubSub(new Options(node, jid, subid, dataForm));
    }

    /**
     * Creates a pubsub element with an {@code <unsubscribe/>} child element with a 'node' and 'jid' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <unsubscribe node='princely_musings' jid='francisco@denmark.lit'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node           The node.
     * @param jid            The JID.
     * @param subscriptionId The subscription id.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-unsubscribe">6.2 Unsubscribe from a Node</a>
     */
    public static PubSub withUnsubscribe(String node, Jid jid, String subscriptionId) {
        return new PubSub(new Unsubscribe(node, jid, subscriptionId));
    }

    /**
     * Creates a pubsub element with a {@code <default/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <default/>
     * </pubsub>
     * }
     * </pre>
     *
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscribe-default">6.4 Request Default Subscription Configuration Options</a>
     */
    public static PubSub withDefault() {
        return withDefault(null);
    }

    /**
     * Creates a pubsub element with a {@code <default/>} child element with a 'node' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <default node='princely_musings'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscribe-default">6.4 Request Default Subscription Configuration Options</a>
     */
    public static PubSub withDefault(String node) {
        return new PubSub(new Default(node));
    }

    /**
     * Creates a pubsub element with an {@code <items/>} child element with a 'node' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <items node='princely_musings'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestall">6.5.2 Requesting All Items</a>
     */
    public static PubSub withItems(String node) {
        return new PubSub(new Items(node));
    }

    /**
     * Creates a pubsub element with an {@code <items/>} child element, containing multiple item elements with an 'id' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <items node='princely_musings'>
     *         <item id='368866411b877c30064a5f62b917cffe'/>
     *         <item id='4e30f35051b7b8b42abe083742187228'/>
     *     </items>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node The node.
     * @param ids  The ids.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-returnnotify">6.5.6 Returning Notifications Only</a>
     */
    public static PubSub withItems(String node, String... ids) {
        List<ItemElement> items = new ArrayList<>();
        for (String id : ids) {
            items.add(new ItemElement(id));
        }
        return new PubSub(new Items(node, items));
    }

    /**
     * Creates a pubsub element with an {@code <items/>} child element with a 'node' and a 'max_items' attribute.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <items node='princely_musings' max_items='2'/>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node     The node.
     * @param maxItems The max items.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#subscriber-retrieve-requestrecent">6.5.7 Requesting the Most Recent Items</a>
     */
    public static PubSub withItems(String node, int maxItems) {
        return new PubSub(new Items(node, maxItems));
    }

    /**
     * Creates a pubsub element with a {@code <publish/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <publish node='princely_musings'>
     *         <item id='bnd81g37d61f49fgn581'>
     *             <entry xmlns='http://www.w3.org/2005/Atom'>
     *     ...
     * </pubsub>
     * }
     * </pre>
     *
     * @param node    The node.
     * @param id      The id.
     * @param item    The item to publish.
     * @param options The publish options.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-publish">7.1 Publish an Item to a Node</a>
     */
    public static PubSub withPublish(String node, String id, Object item, DataForm options) {
        return new PubSub(new Publish(node, new ItemElement(id, item)), options != null ? new PublishOptions(options) : null);
    }

    /**
     * Creates a pubsub element with a {@code <retract/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <pubsub xmlns='http://jabber.org/protocol/pubsub'>
     *     <retract node='princely_musings' notify='true'>
     *         <item id='ae890ac52d0df67ed7cfdf51b644e901'/>
     *     </retract>
     * </pubsub>
     * }
     * </pre>
     *
     * @param node   The node.
     * @param id     The id.
     * @param notify The notify attribute.
     * @return The pubsub instance.
     * @see <a href="http://xmpp.org/extensions/xep-0060.html#publisher-delete">7.2 Delete an Item from a Node</a>
     */
    public static PubSub withRetract(String node, String id, boolean notify) {
        return new PubSub(new Retract(node, new ItemElement(id), notify));
    }

    public Default getDefault() {
        if (type instanceof Default) {
            return (Default) type;
        }
        return null;
    }

    public Subscription getSubscription() {
        if (type instanceof SubscriptionInfo) {
            return (SubscriptionInfo) type;
        }
        return null;
    }

    public Options getOptions() {
        return options;
    }

    public List<Item> getItems() {
        if (type instanceof Items) {
            return Collections.unmodifiableList(new ArrayList<>(((Items) type).getItems()));
        }
        return null;
    }

    public Publish getPublish() {
        if (type instanceof Publish) {
            return (Publish) type;
        }
        return null;
    }

    public DataForm getConfigurationForm() {
        if (configure != null) {
            return configure.getConfigurationForm();
        }
        return null;
    }

    public List<Subscription> getSubscriptions() {
        if (type instanceof Subscriptions) {
            return Collections.unmodifiableList(new ArrayList<>(((Subscriptions) type).getSubscriptions()));
        }
        return null;
    }

    public List<Affiliation> getAffiliations() {
        if (type instanceof AffiliationsElement) {
            return Collections.unmodifiableList(new ArrayList<>(((AffiliationsElement) type).getAffiliations()));
        }
        return null;
    }

    public String getNode() {
        if (type != null) {
            return type.getNode();
        } else if (create != null) {
            return create.getNode();
        } else if (subscribe != null) {
            return subscribe.getNode();
        }
        return null;
    }

    public DataForm getPublishOptions() {
        return publishOptions != null ? publishOptions.getDataForm() : null;
    }

    private static final class Create extends PubSubChildElement {

        private Create() {
        }

        private Create(String node) {
            super(node);
        }
    }

    private static final class Subscribe extends PubSubChildElement {

        @XmlAttribute
        private Jid jid;

        private Subscribe() {
        }

        private Subscribe(String node, Jid jid) {
            super(node);
            this.jid = jid;
        }
    }

    /**
     * The (subscribe) {@code <options/>} element.
     *
     * @see #withOptions(String, Jid, String, rocks.xmpp.extensions.data.model.DataForm)
     */
    public static final class Options {
        @XmlAttribute
        private String node;

        @XmlAttribute
        private Jid jid;

        @XmlAttribute
        private String subid;

        @XmlElementRef
        private DataForm dataForm;

        private Options() {
        }

        private Options(String node, Jid jid, String subid, DataForm dataForm) {
            this.node = node;
            this.jid = jid;
            this.subid = subid;
            this.dataForm = dataForm;
        }

        private Options(DataForm dataForm) {
            this.dataForm = dataForm;
        }

        /**
         * Gets the data form.
         *
         * @return The data form.
         */
        public DataForm getDataForm() {
            return dataForm;
        }
    }

    private static final class AffiliationsElement extends PubSubChildElement {

        private final List<AffiliationInfo> affiliation = new ArrayList<>();

        private AffiliationsElement() {
        }

        private AffiliationsElement(String node) {
            super(node);
        }

        private List<? extends Affiliation> getAffiliations() {
            return affiliation;
        }

        private static final class AffiliationInfo implements Affiliation {

            @XmlAttribute
            private String node;

            @XmlAttribute
            private AffiliationState affiliation;

            @XmlAttribute
            private Jid jid;

            @Override
            public Jid getJid() {
                return jid;
            }

            @Override
            public AffiliationState getAffiliationState() {
                return affiliation;
            }

            @Override
            public String getNode() {
                return node;
            }
        }
    }

    /**
     * The {@code <default/>} element.
     *
     * @see #withDefault()
     */
    public static final class Default extends PubSubChildElement {

        @XmlAttribute
        private Type type;

        @XmlElementRef
        private DataForm dataForm;

        public Default() {

        }

        private Default(String node) {
            super(node);
        }

        /**
         * Gets the default subscription options.
         *
         * @return The default subscription options.
         */
        public DataForm getDataForm() {
            return dataForm;
        }

        private enum Type {
            @XmlEnumValue("collection")
            COLLECTION,
            @XmlEnumValue("leaf")
            LEAF
        }
    }

    private static final class Items extends PubSubChildElement {

        private final List<ItemElement> item = new ArrayList<>();

        @XmlAttribute(name = "max_items")
        private Integer maxItems;

        @XmlAttribute
        private String subid;

        private Retract retract;

        private Items() {
        }

        private Items(String node) {
            super(node);
        }

        private Items(String node, int maxItems) {
            super(node);
            this.maxItems = maxItems;
        }

        private Items(String node, List<ItemElement> items) {
            super(node);
            this.item.addAll(items);
        }

        private List<? extends Item> getItems() {
            return item;
        }
    }

    /**
     * The {@code <publish/>} element.
     *
     * @see #withPublish(String, String, Object, rocks.xmpp.extensions.data.model.DataForm)
     */
    public static final class Publish extends PubSubChildElement {

        private ItemElement item;

        private Publish() {
        }

        private Publish(String node, ItemElement item) {
            super(node);
            this.item = item;
        }

        /**
         * Gets the published item.
         *
         * @return The item.
         */
        public Item getItem() {
            return item;
        }
    }

    private static final class PublishOptions extends PubSubChildElement {

        @XmlElementRef
        private DataForm dataForm;

        private PublishOptions() {
        }

        public PublishOptions(DataForm dataForm) {
            this.dataForm = dataForm;
        }

        public DataForm getDataForm() {
            return dataForm;
        }
    }


    private static final class Retract extends PubSubChildElement {

        @XmlAttribute
        private Boolean notify;

        private ItemElement item;

        @XmlAttribute
        private String id;

        private Retract() {

        }

        private Retract(String node, ItemElement item, Boolean notify) {
            super(node);
            this.item = item;
            this.notify = notify;
        }

        private String getId() {
            return id;
        }
    }

    private static final class SubscriptionInfo extends PubSubChildElement implements Subscription {

        @XmlAttribute
        private Jid jid;

        @XmlAttribute
        private String subid;

        @XmlAttribute
        private SubscriptionState subscription;

        @XmlAttribute
        @XmlJavaTypeAdapter(InstantAdapter.class)
        private Instant expiry;

        @XmlElement(name = "subscribe-options")
        private SubscribeOptions options;

        @Override
        public String getSubId() {
            return subid;
        }

        @Override
        public SubscriptionState getSubscriptionState() {
            return subscription;
        }

        @Override
        public Jid getJid() {
            return jid;
        }

        @Override
        public Instant getExpiry() {
            return expiry;
        }

        @Override
        public boolean isConfigurationRequired() {
            return options != null && options.isRequired();
        }

        @Override
        public boolean isConfigurationSupported() {
            return options != null;
        }

        private static final class SubscribeOptions {

            private String required;

            private boolean isRequired() {
                return required != null;
            }
        }
    }

    private static final class Subscriptions extends PubSubChildElement {

        private final List<SubscriptionInfo> subscription = new ArrayList<>();

        private Subscriptions() {
        }

        private Subscriptions(String node) {
            super(node);
        }

        private List<? extends Subscription> getSubscriptions() {
            return subscription;
        }
    }

    private static final class Unsubscribe extends PubSubChildElement {

        @XmlAttribute
        private String node;

        @XmlAttribute
        private Jid jid;

        @XmlAttribute
        private String subid;

        private Unsubscribe() {
        }

        private Unsubscribe(String node, Jid jid, String subid) {
            super(node);
            this.jid = jid;
            this.subid = subid;
        }
    }

    private static final class Configure {
        @XmlAttribute
        private String node;

        @XmlElementRef
        private DataForm dataForm;

        private Configure() {
        }

        private Configure(String node, DataForm dataForm) {
            this.node = node;
            this.dataForm = dataForm;
        }

        private Configure(DataForm dataForm) {
            this.dataForm = dataForm;
        }

        private DataForm getConfigurationForm() {
            return dataForm;
        }
    }

    private static final class ItemElement implements Item {

        @XmlAnyElement(lax = true)
        private Object object;

        @XmlAttribute
        private String id;

        private ItemElement() {
        }

        private ItemElement(String id) {
            this.id = id;
        }

        private ItemElement(String id, Object object) {
            this.id = id;
            this.object = object;
        }

        @Override
        public Object getPayload() {
            return object;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getNode() {
            return null;
        }

        @Override
        public String getPublisher() {
            return null;
        }
    }

    private abstract static class PubSubChildElement {

        @XmlAttribute
        private String node;

        private PubSubChildElement() {
        }

        private PubSubChildElement(String node) {
            this.node = node;
        }

        public String getNode() {
            return node;
        }
    }
}
