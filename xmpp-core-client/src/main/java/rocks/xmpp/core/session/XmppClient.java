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

package rocks.xmpp.core.session;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.bind.model.Bind;
import rocks.xmpp.core.sasl.AuthenticationException;
import rocks.xmpp.core.sasl.model.Mechanisms;
import rocks.xmpp.core.session.model.Session;
import rocks.xmpp.core.stanza.StanzaException;
import rocks.xmpp.core.stanza.model.IQ;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stanza.model.client.ClientIQ;
import rocks.xmpp.core.stanza.model.client.ClientMessage;
import rocks.xmpp.core.stanza.model.client.ClientPresence;
import rocks.xmpp.core.stream.StreamErrorException;
import rocks.xmpp.core.stream.StreamFeatureNegotiator;
import rocks.xmpp.core.stream.StreamNegotiationException;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.extensions.caps.EntityCapabilitiesManager;
import rocks.xmpp.extensions.sm.StreamManager;
import rocks.xmpp.im.roster.RosterManager;
import rocks.xmpp.im.subscription.PresenceManager;
import rocks.xmpp.util.concurrent.AsyncResult;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.sasl.RealmCallback;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The base class for establishing an XMPP session with a server, i.e. client-to-server sessions.
 * <h3>Establishing an XMPP Session</h3>
 * The following example shows the most simple way to establish a session:
 * <pre>
 * {@code
 * XmppClient xmppClient = XmppClient.create("domain");
 * xmppClient.connect();
 * xmppClient.login("username", "password");
 * }
 * </pre>
 * By default, the session will try to establish a TCP connection over port 5222 and will try BOSH as fallback.
 * You can configure a session and its connection methods by passing appropriate configurations in its constructor.
 * <h3>Sending Messages</h3>
 * Once connected, you can send messages:
 * <pre>
 * {@code
 * xmppClient.sendMessage(new Message(Jid.of("juliet@example.net"), Message.Type.CHAT));
 * }
 * </pre>
 * <h3>Closing the Session</h3>
 * <pre>
 * {@code
 * xmppClient.close();
 * }
 * </pre>
 * <h3>Listening for Messages and Presence</h3>
 * <b>Note:</b> Adding the following listeners should be added before logging in, otherwise they might not trigger.
 * <pre>
 * {@code
 * // Listen for messages
 * xmppClient.addInboundMessageListener(e ->
 *     // Handle inbound message.
 * );
 *
 * // Listen for presence changes
 * xmppClient.addInboundPresenceListener(e ->
 *     // Handle inbound presence.
 * );
 * }
 * </pre>
 * This class is thread-safe, which means you can safely add listeners or call <code>send()</code>, <code>close()</code> (and other methods) from different threads.
 *
 * @author Christian Schudt
 * @see XmppSessionConfiguration
 * @see TcpConnectionConfiguration
 * @see rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration
 */
public final class XmppClient extends XmppSession {

    private static final Logger logger = Logger.getLogger(XmppClient.class.getName());

    private final AuthenticationManager authenticationManager;

    /**
     * The user, which is assigned by the server after resource binding.
     */
    private volatile Jid connectedResource;

    /**
     * The resource, which the user requested during resource binding. This value is stored, so that it can be reused during reconnection.
     */
    private volatile String resource;

    private volatile String lastAuthorizationId;

    private volatile Collection<String> lastMechanisms;

    private volatile CallbackHandler lastCallbackHandler;

    private volatile boolean anonymous;

    /**
     * Creates a session with the specified service domain by using a configuration.
     *
     * @param xmppServiceDomain        The service domain.
     * @param configuration            The configuration.
     * @param connectionConfigurations The connection configurations.
     */
    private XmppClient(String xmppServiceDomain, XmppSessionConfiguration configuration, ConnectionConfiguration... connectionConfigurations) {
        super(xmppServiceDomain, configuration, connectionConfigurations);

        authenticationManager = new AuthenticationManager(this);

        streamFeaturesManager.addFeatureNegotiator(authenticationManager);
        streamFeaturesManager.addFeatureNegotiator(new StreamFeatureNegotiator(this, Bind.class) {
            @Override
            public Status processNegotiation(Object element) throws StreamNegotiationException {
                // Resource binding will be negotiated manually
                return Status.INCOMPLETE;
            }

            @Override
            public boolean canProcess(Object element) {
                return false;
            }
        });
    }

    /**
     * Creates a new XMPP client instance. Any registered {@link #addCreationListener(Consumer) creation listeners} are triggered.
     *
     * @param xmppServiceDomain        The XMPP service domain.
     * @param connectionConfigurations The connection methods, which are used to connect.
     * @return The XMPP client.
     */
    public static XmppClient create(String xmppServiceDomain, ConnectionConfiguration... connectionConfigurations) {
        return create(xmppServiceDomain, XmppSessionConfiguration.getDefault(), connectionConfigurations);
    }

    /**
     * Creates a new XMPP client instance. Any registered {@link #addCreationListener(Consumer) creation listeners} are triggered.
     *
     * @param xmppServiceDomain        The XMPP service domain.
     * @param configuration            The configuration.
     * @param connectionConfigurations The connection methods, which are used to connect.
     * @return The XMPP client.
     */
    public static XmppClient create(String xmppServiceDomain, XmppSessionConfiguration configuration, ConnectionConfiguration... connectionConfigurations) {
        XmppClient xmppClient = new XmppClient(xmppServiceDomain, configuration, connectionConfigurations);
        notifyCreationListeners(xmppClient);
        return xmppClient;
    }

    /**
     * Connects to the XMPP server.
     *
     * @param from The 'from' attribute.
     * @throws ConnectionException        If a connection error occurred on the transport layer, e.g. the socket could not connect.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws XmppException              If any other XMPP exception occurs.
     * @throws IllegalStateException      If the session is in a wrong state, e.g. closed or already connected.
     */
    @Override
    public final void connect(Jid from) throws XmppException {

        Status previousStatus = preConnect();

        if (checkConnected()) {
            // Silently return, when we are already connected or connecting.
            return;
        }
        try {
            // Don't call listeners from within synchronized blocks to avoid possible deadlocks.
            updateStatus(Status.CONNECTING);

            synchronized (this) {
                // Double-checked locking: Recheck connected status. In a multi-threaded environment multiple threads could have passed the first check.
                if (checkConnected()) {
                    return;
                }
                // Reset
                exception = null;

                tryConnect(from, "jabber:client", this::setXmppServiceDomain);

                logger.fine("Negotiating stream, waiting until SASL is ready to be negotiated.");

                // Wait until the reader thread signals, that we are connected. That is after TLS negotiation and before SASL negotiation.
                try {
                    streamFeaturesManager.awaitNegotiation(Mechanisms.class, Duration.ofSeconds(10));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw e;
                }

                // Check if stream negotiation threw any exception.
                throwAsXmppExceptionIfNotNull(exception);

                logger.fine("Stream negotiated until SASL, now ready to login.");
            }
            // Don't call listeners from within synchronized blocks to avoid possible deadlocks.
            updateStatus(Status.CONNECTING, Status.CONNECTED);

            // This is for reconnection.
            if (wasLoggedIn) {
                logger.fine("Was already logged in. Re-login automatically with known credentials.");
                login(lastMechanisms, lastAuthorizationId, lastCallbackHandler, resource);
            }

        } catch (Throwable e) {
            onConnectionFailed(previousStatus, e);
        }
    }

    /**
     * Authenticates against the server and binds a random resource (assigned by the server).
     *
     * @param user     The user name. Usually this is the local part of the user's JID. Must not be null.
     * @param password The password. Must not be null.
     * @return The additional data with success, i.e. the data returned upon successful authentication.
     * @throws AuthenticationException    If the login failed, due to a SASL error reported by the server.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws StanzaException            If the server returned a stanza error during resource binding or roster retrieval.
     * @throws XmppException              If the login failed, due to another error.
     */
    public final byte[] login(String user, String password) throws XmppException {
        return login(user, password, null);
    }

    /**
     * Authenticates against the server with username/password credential and binds a resource.
     *
     * @param user     The user name. Usually this is the local part of the user's JID. Must not be null.
     * @param password The password. Must not be null.
     * @param resource The resource. If null or empty, the resource is randomly assigned by the server.
     * @return The additional data with success, i.e. the data returned upon successful authentication.
     * @throws AuthenticationException    If the login failed, due to a SASL error reported by the server.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws StanzaException            If the server returned a stanza error during resource binding or roster retrieval.
     * @throws XmppException              If the login failed, due to another error.
     */
    public final byte[] login(String user, String password, String resource) throws XmppException {
        return login(null, user, password, resource);
    }

    /**
     * Authenticates against the server with an authorization id and username/password credential and binds a resource.
     *
     * @param authorizationId The authorization id.
     * @param user            The user name. Usually this is the local part of the user's JID. Must not be null.
     * @param password        The password. Must not be null.
     * @param resource        The resource. If null or empty, the resource is randomly assigned by the server.
     * @return The additional data with success, i.e. the data returned upon successful authentication.
     * @throws AuthenticationException    If the login failed, due to a SASL error reported by the server.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws StanzaException            If the server returned a stanza error during resource binding or roster retrieval.
     * @throws XmppException              If the login failed, due to another error.
     */
    public final byte[] login(String authorizationId, final String user, final String password, String resource) throws XmppException {
        Objects.requireNonNull(user, "user must not be null.");
        Objects.requireNonNull(password, "password must not be null.");

        // A default callback handler for username/password retrieval:
        return login(authorizationId, callbacks -> Arrays.stream(callbacks).forEach(callback -> {
            if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName(user);
            }
            if (callback instanceof PasswordCallback) {
                ((PasswordCallback) callback).setPassword(password.toCharArray());
            }
            if (callback instanceof RealmCallback) {
                ((RealmCallback) callback).setText(((RealmCallback) callback).getDefaultText());
            }
        }), resource);
    }

    /**
     * Authenticates against the server with a custom callback handler and binds a resource.
     *
     * @param authorizationId The authorization id.
     * @param callbackHandler The callback handler.
     * @param resource        The resource. If null or empty, the resource is randomly assigned by the server.
     * @return The additional data with success, i.e. the data returned upon successful authentication.
     * @throws AuthenticationException    If the login failed, due to a SASL error reported by the server.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws StanzaException            If the server returned a stanza error during resource binding or roster retrieval.
     * @throws XmppException              If the login failed, due to another error.
     */
    public final byte[] login(String authorizationId, CallbackHandler callbackHandler, String resource) throws XmppException {
        return login(configuration.getAuthenticationMechanisms(), authorizationId, callbackHandler, resource);
    }

    /**
     * Logs in anonymously and binds a resource.
     *
     * @return The additional data with success, i.e. the data returned upon successful authentication.
     * @throws AuthenticationException    If the login failed, due to a SASL error reported by the server.
     * @throws StreamErrorException       If the server returned a stream error.
     * @throws StreamNegotiationException If any exception occurred during stream feature negotiation.
     * @throws NoResponseException        If the server didn't return a response during stream establishment.
     * @throws StanzaException            If the server returned a stanza error during resource binding.
     * @throws XmppException              If the login failed, due to another error.
     */
    public final byte[] loginAnonymously() throws XmppException {
        byte[] successData = login(Collections.singleton("ANONYMOUS"), null, null, null);
        anonymous = true;
        return successData;
    }

    private byte[] login(Collection<String> mechanisms, String authorizationId, CallbackHandler callbackHandler, String resource) throws XmppException {

        if (checkAuthenticated()) {
            // Silently return, when we are already authenticated.
            return authenticationManager.getSuccessData();
        }
        Status previousStatus = preLogin();
        updateStatus(Status.AUTHENTICATING);

        synchronized (this) {
            if (checkAuthenticated()) {
                // Silently return, when we are already authenticated.
                return authenticationManager.getSuccessData();
            }
            lastMechanisms = mechanisms;
            lastAuthorizationId = authorizationId;
            lastCallbackHandler = callbackHandler;
            try {

                logger.fine("Starting SASL negotiation (authentication).");
                if (callbackHandler == null) {
                    authenticationManager.startAuthentication(mechanisms, null, null);
                } else {
                    authenticationManager.startAuthentication(mechanisms, authorizationId, callbackHandler);
                }

                // Negotiate all pending features until <bind/> would be negotiated.
                streamFeaturesManager.awaitNegotiation(Bind.class, configuration.getDefaultResponseTimeout());

                // Check if stream feature negotiation failed with an exception.
                throwAsXmppExceptionIfNotNull(exception);

                // Stream resumption.
                try {
                    StreamManager streamManager = getManager(StreamManager.class);
                    if (streamManager.resume().getResult(configuration.getDefaultResponseTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                        logger.fine("Stream resumed.");
                        updateStatus(Status.AUTHENTICATED);
                        // Copy the unacknowledged stanzas.
                        Queue<Stanza> toBeResent = new ArrayDeque<>(getUnacknowledgedStanzas());
                        // Then clear the queue.
                        getUnacknowledgedStanzas().clear();

                        // Then resend everything, which the server didn't acknowledge.
                        toBeResent.forEach(this::send);
                        return authenticationManager.getSuccessData();
                    }
                } catch (TimeoutException e) {
                    logger.warning("Could not resume stream due to timeout.");
                }

                // Then negotiate resource binding manually.
                bindResource(resource);

                // Proceed with any outstanding stream features which are negotiated after resource binding, e.g. XEP-0198
                // and wait until all features have been negotiated.
                streamFeaturesManager.completeNegotiation(configuration.getDefaultResponseTimeout());

                // Check again, if stream feature negotiation failed with an exception.
                throwAsXmppExceptionIfNotNull(exception);

                logger.fine("Stream negotiation completed successfully.");

                // Retrieve roster.
                RosterManager rosterManager = getManager(RosterManager.class);
                if (callbackHandler != null && rosterManager.isEnabled() && rosterManager.isRetrieveRosterOnLogin()) {
                    logger.fine("Retrieving roster on login (as per configuration).");
                    rosterManager.requestRoster().getResult();
                }
                PresenceManager presenceManager = getManager(PresenceManager.class);
                if (presenceManager.getLastSentPresence() != null) {
                    // After retrieving the roster, resend the last presence, if any (in reconnection case).
                    presenceManager.getLastSentPresences().forEach(presence -> {
                        presence.getExtensions().clear();
                        send(presence);
                    });
                } else if (configuration.getInitialPresence() != null) {
                    // Or send initial presence
                    Presence initialPresence = configuration.getInitialPresence().get();
                    if (initialPresence != null) {
                        send(initialPresence);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // Revert status
                updateStatus(previousStatus, e);
                throwAsXmppExceptionIfNotNull(e);
            } catch (Throwable e) {
                // Revert status
                updateStatus(previousStatus, e);
                throwAsXmppExceptionIfNotNull(e);
            }
            logger.fine("Login successful.");
            afterLogin();
            return authenticationManager.getSuccessData();
        }
    }

    /**
     * Binds a resource to the session.
     *
     * @param resource The resource to bind. If the resource is null and random resource is bound by the server.
     */
    private void bindResource(String resource) throws XmppException {
        this.resource = resource;

        logger.log(Level.FINE, "Negotiating resource binding, resource: {0}.", resource);

        // Bind the resource
        IQ result = query(IQ.set(new Bind(this.resource))).getResult();

        Bind bindResult = result.getExtension(Bind.class);
        this.connectedResource = bindResult.getJid();

        logger.log(Level.FINE, "Resource binding completed, connected resource: {0}.", connectedResource);

        // At this point the entity is free to send stanzas:
        // "If, before completing the resource binding step, the client attempts to send an XML stanza to an entity other
        // than the server itself or the client's account, the server MUST NOT process the stanza
        // and MUST close the stream with a <not-authorized/> stream error."

        // Deprecated method of session binding, according to the <a href="http://xmpp.org/rfcs/rfc3921.html#session">old specification</a>
        // This is no longer used, according to the <a href="http://xmpp.org/rfcs/rfc6120.html">updated specification</a>.
        // But some old server implementation still require it.
        Session session = (Session) streamFeaturesManager.getFeatures().get(Session.class);
        if (session != null && session.isMandatory()) {
            logger.fine("Establishing session.");
            query(IQ.set(new Session()));
        }

        // Set this status after session establishment. It's used to auto-send service discovery to a server and some servers won't response,
        // if it's send before.
        updateStatus(Status.AUTHENTICATED);
    }

    /**
     * Gets the connected resource, which is assigned by the server after resource binding.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#bind-fundamentals">7.1.  Fundamentals</a></cite></p>
     * <p>After a client has bound a resource to the stream, it is referred to as a "connected resource".</p>
     * </blockquote>
     *
     * @return The connected resource.
     */
    public final Jid getConnectedResource() {
        return connectedResource;
    }

    /**
     * Indicates whether the session has been logged in anonymously. If never logged in at all, returns false.
     *
     * @return True, if the session is anonymous.
     */
    public final boolean isAnonymous() {
        return anonymous;
    }

    @Override
    public final Future<Void> send(StreamElement element) {
        StreamElement e;
        if (element instanceof Message) {
            e = ClientMessage.from((Message) element);
        } else if (element instanceof Presence) {
            e = ClientPresence.from((Presence) element);
        } else if (element instanceof IQ) {
            e = ClientIQ.from((IQ) element);
        } else {
            e = element;
        }
        return super.send(e);
    }

    @Override
    public final SendTask<IQ> sendIQ(final IQ iq) {
        return trackAndSend(ClientIQ.from(iq));
    }

    @Override
    public final SendTask<Message> sendMessage(final Message message) {
        return trackAndSend(ClientMessage.from(message));
    }

    @Override
    public final SendTask<Presence> sendPresence(final Presence presence) {
        return trackAndSend(ClientPresence.from(presence));
    }

    /**
     * Determines support of another XMPP entity for a given feature.
     * <p>
     * Note that if you want to determine support of another client, you have to provide that client's full JID (user@domain/resource).
     * If you want to determine the server's capabilities provide only the domain JID of the server.
     * </p>
     * This method uses cached information and the presence based entity capabilities (XEP-0115) to determine support. Only if no information is available an explicit service discovery request is made.
     *
     * @param feature The feature, usually defined by an XMPP Extension Protocol, e.g. "urn:xmpp:ping".
     * @param jid     The XMPP entity.
     * @return True, if the XMPP entity supports the given feature; otherwise false.
     */
    public final AsyncResult<Boolean> isSupported(String feature, Jid jid) {
        return getManager(EntityCapabilitiesManager.class).isSupported(feature, jid);
    }
}
