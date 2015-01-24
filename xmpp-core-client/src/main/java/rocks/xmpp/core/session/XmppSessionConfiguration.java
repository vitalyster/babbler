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

package rocks.xmpp.core.session;

import rocks.xmpp.core.session.context.CoreContext;
import rocks.xmpp.core.session.debug.XmppDebugger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A configuration for an {@link XmppSession}.
 * <p>
 * Most importantly it allows you to introduce custom extensions to your {@link XmppSession}, simply by passing your JAXB annotated classes to the constructor of this class
 * and then {@linkplain XmppSession#XmppSession(String, XmppSessionConfiguration, ConnectionConfiguration...) use this configuration for the session}.
 * </p>
 * Since creating the JAXB context is quite expensive, this class allows you to create the context once and reuse it by multiple sessions.
 * You can also {@linkplain #setDefault(XmppSessionConfiguration) set} an application-wide default configuration (used by all XMPP sessions).
 * <p>
 * Use the {@link #builder()} to create instances of this class.
 * <p>
 * This class is immutable.
 *
 * @author Christian Schudt
 * @see XmppSession#XmppSession(String, XmppSessionConfiguration, ConnectionConfiguration...)
 */
public final class XmppSessionConfiguration {

    private static volatile XmppSessionConfiguration defaultConfiguration;

    private final Collection<Class<? extends ExtensionManager>> initialExtensionManagers = new HashSet<>();

    private final JAXBContext jaxbContext;

    private final Class<? extends XmppDebugger> xmppDebugger;

    private final int defaultResponseTimeout;

    private final List<String> authenticationMechanisms;

    private final File cacheDirectory;

    /**
     * Creates a configuration for an {@link XmppSession}. If you want to add custom classes to the {@link JAXBContext}, you can pass them as parameters.
     *
     * @param builder The builder.
     */
    private XmppSessionConfiguration(Builder builder) {
        this.xmppDebugger = builder.xmppDebugger;
        this.defaultResponseTimeout = builder.defaultResponseTimeout;
        this.authenticationMechanisms = builder.authenticationMechanisms;
        this.cacheDirectory = builder.cacheDirectory;

        CoreContext context = builder.context;

        if (context == null) {
            try {
                Class<?> extensionContext = Class.forName(CoreContext.class.getPackage().getName() + ".extensions.ExtensionContext");
                context = (CoreContext) extensionContext.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                context = new CoreContext();
            }
        }

        // These are the manager classes which are loaded immediately, when the XmppSession is initialized,
        // Typically the add listeners to the session, e.g. to automatically reply.
        initialExtensionManagers.addAll(context.getExtensionManagers());

        Class<?>[] classesToBeBound = new Class<?>[context.getExtensions().size()];
        context.getExtensions().toArray(classesToBeBound);

        try {
            jaxbContext = JAXBContext.newInstance(classesToBeBound);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the default configuration.
     *
     * @return The default configuration.
     */
    public static XmppSessionConfiguration getDefault() {
        // Use double-checked locking idiom
        if (defaultConfiguration == null) {
            synchronized (XmppSessionConfiguration.class) {
                if (defaultConfiguration == null) {
                    defaultConfiguration = XmppSessionConfiguration.builder().build();
                }
            }
        }
        return defaultConfiguration;
    }

    /**
     * Sets the default configuration.
     *
     * @param configuration The default configuration.
     */
    public static void setDefault(XmppSessionConfiguration configuration) {
        synchronized (XmppSessionConfiguration.class) {
            defaultConfiguration = configuration;
        }
    }

    /**
     * Creates a builder for this class.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Gets the JAXB context.
     *
     * @return The JAXB context.
     */
    JAXBContext getJAXBContext() {
        return jaxbContext;
    }

    /**
     * Gets the initial extension managers. Theses managers are initialized when the session is initialized, thus allowing them to immediately add listeners to the session e.g. to react to incoming stanzas.
     *
     * @return The initial extension managers.
     */
    Collection<Class<? extends ExtensionManager>> getInitialExtensionManagers() {
        return initialExtensionManagers;
    }

    /**
     * Gets the current debugger for this session. If no debugger was set, the default debugger is the {@link rocks.xmpp.core.session.debug.ConsoleDebugger}.
     *
     * @return The debugger.
     */
    public final Class<? extends XmppDebugger> getDebugger() {
        return xmppDebugger;
    }

    /**
     * Gets the response timeout.
     *
     * @return The response timeout.
     */
    public final int getDefaultResponseTimeout() {
        return defaultResponseTimeout;
    }

    /**
     * Gets the preferred authentication (SASL) mechanisms.
     *
     * @return The mechanisms.
     * @see Builder#authenticationMechanisms(String...)
     */
    public final List<String> getAuthenticationMechanisms() {
        return Collections.unmodifiableList(authenticationMechanisms);
    }

    /**
     * Sets the caching directory for directory-based caches used for:
     * <ul>
     * <li><a href="http://xmpp.org/extensions/xep-0084.html">XEP-0084: User Avatar</a></li>
     * <li><a href="http://xmpp.org/extensions/xep-0115.html">XEP-0115: Entity Capabilities</a></li>
     * <li><a href="http://xmpp.org/extensions/xep-0153.html">XEP-0153: vCard-Based Avatars</a></li>
     * </ul>
     * By default this directory is called "cache" in the application's executing directory.
     *
     * @return The directory.
     */
    public final File getCacheDirectory() {
        return cacheDirectory;
    }

    /**
     * A builder to create an {@link XmppSessionConfiguration} instance.
     */
    public static final class Builder {

        private Class<? extends XmppDebugger> xmppDebugger;

        private CoreContext context;

        private int defaultResponseTimeout;

        private File cacheDirectory;

        /**
         * The default preferred SASL mechanisms.
         */
        private List<String> authenticationMechanisms = Arrays.asList("SCRAM-SHA-1",
                "DIGEST-MD5",
                "GSSAPI",
                "CRAM-MD5",
                "PLAIN",
                "ANONYMOUS");

        private Builder() {
            defaultResponseTimeout(5000).cacheDirectory(new File("cache"));
        }

        /**
         * Sets the debugger.
         *
         * @param xmppDebugger The debugger or null, if you don't want to use a debugger.
         * @return The debugger.
         */
        public final Builder debugger(Class<? extends XmppDebugger> xmppDebugger) {
            this.xmppDebugger = xmppDebugger;
            return this;
        }

        /**
         * Sets XMPP context which is used to setup the JAXBContext and which defines the initial extension managers.
         *
         * @param context The context.
         * @return The builder.
         */
        public final Builder context(CoreContext context) {
            this.context = context;
            return this;
        }

        /**
         * Sets the default response timeout for synchronous calls, usually IQ calls.
         *
         * @param defaultResponseTimeout The default response timeout.
         * @return The builder.
         */
        public final Builder defaultResponseTimeout(int defaultResponseTimeout) {
            this.defaultResponseTimeout = defaultResponseTimeout;
            return this;
        }

        /**
         * Sets the preferred mechanisms used for this XMPP session.
         * <blockquote>
         * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#sasl-rules-preferences">6.3.3.  Mechanism Preferences</a></cite></p>
         * <p>Any entity that will act as a SASL client or a SASL server MUST maintain an ordered list
         * of its preferred SASL mechanisms according to the client or server,
         * where the list is ordered according to local policy or user configuration
         * (which SHOULD be in order of perceived strength to enable the strongest authentication possible).
         * The initiating entity MUST maintain its own preference order independent of the preference order of the receiving entity.
         * A client MUST try SASL mechanisms in its preference order.
         * For example, if the server offers the ordered list "PLAIN SCRAM-SHA-1 GSSAPI" or "SCRAM-SHA-1 GSSAPI PLAIN"
         * but the client's ordered list is "GSSAPI SCRAM-SHA-1",
         * the client MUST try GSSAPI first and then SCRAM-SHA-1 but MUST NOT try PLAIN (since PLAIN is not on its list).</p>
         * </blockquote>
         *
         * @param authenticationMechanisms The preferred mechanisms.
         * @return The builder.
         */
        public final Builder authenticationMechanisms(String... authenticationMechanisms) {
            this.authenticationMechanisms = Arrays.asList(authenticationMechanisms);
            return this;
        }

        /**
         * Sets the caching directory for directory-based caches used for:
         * <ul>
         * <li><a href="http://xmpp.org/extensions/xep-0084.html">XEP-0084: User Avatar</a></li>
         * <li><a href="http://xmpp.org/extensions/xep-0115.html">XEP-0115: Entity Capabilities</a></li>
         * <li><a href="http://xmpp.org/extensions/xep-0153.html">XEP-0153: vCard-Based Avatars</a></li>
         * </ul>.
         *
         * @param file The directory.
         * @return The builder.
         */
        public final Builder cacheDirectory(File file) {
            this.cacheDirectory = file;
            return this;
        }

        /**
         * Builds the configuration.
         *
         * @return The configuration.
         */
        public final XmppSessionConfiguration build() {
            return new XmppSessionConfiguration(this);
        }
    }
}
