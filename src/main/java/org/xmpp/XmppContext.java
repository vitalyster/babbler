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

package org.xmpp;

import org.xmpp.bind.Bind;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.bosh.Body;
import org.xmpp.extension.chatstate.*;
import org.xmpp.extension.compression.Compression;
import org.xmpp.extension.dataforms.DataForm;
import org.xmpp.extension.delayeddelivery.DelayedDelivery;
import org.xmpp.extension.lastactivity.LastActivity;
import org.xmpp.extension.lastactivity.LastActivityManager;
import org.xmpp.extension.servicediscovery.ItemDiscovery;
import org.xmpp.extension.servicediscovery.ServiceDiscovery;
import org.xmpp.im.Roster;
import org.xmpp.im.session.Session;
import org.xmpp.sasl.Mechanisms;
import org.xmpp.stanza.Stanza;
import org.xmpp.stream.Features;
import org.xmpp.stream.StreamError;
import org.xmpp.tls.StartTls;

import java.util.*;

/**
 * The XMPP context provides XMPP extensions and manager classes to manage extensions for a connection.
 *
 * @author Christian Schudt
 */
public abstract class XmppContext {

    private static volatile XmppContext defaultContext;

    private final Set<Class<?>> extensions = new HashSet<>();

    private final Set<Class<?>> core = new HashSet<>();

    private final Set<Class<? extends ExtensionManager>> managers = new HashSet<>();

    protected XmppContext() {
        core.addAll(Arrays.asList(Features.class, StreamError.class, Stanza.class, Session.class, Roster.class, Bind.class, Mechanisms.class, StartTls.class));
    }

    public static XmppContext getDefault() {
        // Use double-checked locking idiom
        if (defaultContext == null) {
            synchronized (XmppContext.class) {
                if (defaultContext == null) {
                    defaultContext = new DefaultXmppContext();
                }
            }
        }
        return defaultContext;
    }

    public static void setDefault(XmppContext xmppContext) {
        synchronized (XmppContext.class) {
            defaultContext = xmppContext;
        }
    }

    public final void registerExtension(Class<?>... extensions) {
        this.extensions.addAll(Arrays.asList(extensions));
    }

    public final void registerManager(Class<? extends ExtensionManager> manager) {
        managers.add(manager);
    }

    public final Collection<Class<?>> getExtensions() {
        List<Class<?>> result = new ArrayList<>(core);
        result.addAll(extensions);
        return result;
    }

    public final Collection<Class<? extends ExtensionManager>> getExtensionManagers() {
        return managers;
    }

    private static class DefaultXmppContext extends XmppContext {
        private DefaultXmppContext() {
            // Chat State Notifications
            registerExtension(Active.class);
            registerExtension(Composing.class);
            registerExtension(Gone.class);
            registerExtension(Inactive.class);
            registerExtension(Paused.class);

            // Delayed Delivery
            registerExtension(DelayedDelivery.class);

            // Last Activity
            registerExtension(LastActivity.class);
            registerManager(LastActivityManager.class);

            // BOSH
            registerExtension(Body.class);

            // Delayed Delivery
            registerExtension(DelayedDelivery.class);

            // Service Discovery
            registerExtension(ServiceDiscovery.class, ItemDiscovery.class);

            registerExtension(DataForm.class);

            // Compression
            registerExtension(Compression.class);
        }
    }
}
