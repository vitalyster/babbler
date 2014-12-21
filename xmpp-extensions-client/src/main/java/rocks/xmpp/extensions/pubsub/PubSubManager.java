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

package rocks.xmpp.extensions.pubsub;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ExtensionManager;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.extensions.disco.ServiceDiscoveryManager;
import rocks.xmpp.extensions.disco.model.items.Item;
import rocks.xmpp.extensions.pubsub.model.PubSub;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class is the entry point to work with pubsub.
 * <p>
 * You should first {@linkplain #createPubSubService(rocks.xmpp.core.Jid) create a pubsub service}, which allows you to work with that service.
 * If you don't know the service address, you can {@linkplain #getPubSubServices() discover} the pubsub services hosted at your server.
 * <p>
 * It also allows you to {@linkplain #createPersonalEventingService() create a Personal Eventing Service}, which is a virtual pubsub service, bound to your account.
 *
 * @author Christian Schudt
 * @see rocks.xmpp.extensions.pubsub.PubSubService
 * @see <a href="http://xmpp.org/extensions/xep-0060.html">XEP-0060: Publish-Subscribe</a>
 * @see <a href="http://xmpp.org/extensions/xep-0163.html">XEP-0163: Personal Eventing Protocol</a>
 */
public final class PubSubManager extends ExtensionManager {

    private final ServiceDiscoveryManager serviceDiscoveryManager;

    private PubSubManager(XmppSession xmppSession) {
        super(xmppSession);
        serviceDiscoveryManager = xmppSession.getExtensionManager(ServiceDiscoveryManager.class);
    }

    /**
     * Discovers the publish-subscribe services for the current connection.
     *
     * @return The list of publish-subscribe services.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the server returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the server did not respond.
     * @deprecated Use {@link #discoverPubSubServices()}
     */
    @Deprecated
    public Collection<PubSubService> getPubSubServices() throws XmppException {
        return discoverPubSubServices();
    }

    /**
     * Discovers the publish-subscribe services for the current connection.
     *
     * @return The list of publish-subscribe services.
     * @throws rocks.xmpp.core.stanza.model.StanzaException If the server returned a stanza error.
     * @throws rocks.xmpp.core.session.NoResponseException  If the server did not respond.
     */
    public Collection<PubSubService> discoverPubSubServices() throws XmppException {
        Collection<Item> services = serviceDiscoveryManager.discoverServices(PubSub.NAMESPACE);
        Collection<PubSubService> pubSubServices = new ArrayList<>();
        for (Item service : services) {
            pubSubServices.add(new PubSubService(service.getJid(), service.getName(), xmppSession, serviceDiscoveryManager));
        }
        return pubSubServices;
    }

    /**
     * Creates a pubsub service.
     *
     * @param service The pubsub service address, e.g. {@code Jid.valueOf("pubsub.mydomain")}
     * @return The pubsub service.
     */
    public PubSubService createPubSubService(Jid service) {
        return new PubSubService(service, null, xmppSession, serviceDiscoveryManager);
    }

    /**
     * Creates a personal eventing service.
     *
     * @return The personal eventing service.
     * @see <a href="http://xmpp.org/extensions/xep-0163.html">XEP-0163: Personal Eventing Protocol</a>
     */
    public PubSubService createPersonalEventingService() {
        return new PubSubService(xmppSession.getConnectedResource().asBareJid(), "Personal Eventing Service", xmppSession, serviceDiscoveryManager);
    }
}
