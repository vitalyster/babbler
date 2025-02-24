/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Christian Schudt
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

package rocks.xmpp.extensions.httpbind;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.bind.DatatypeConverter;

import rocks.xmpp.core.net.AbstractConnection;
import rocks.xmpp.core.net.ChannelEncryption;
import rocks.xmpp.core.net.ReaderInterceptor;
import rocks.xmpp.core.net.ReaderInterceptorChain;
import rocks.xmpp.core.net.WriterInterceptor;
import rocks.xmpp.core.net.WriterInterceptorChain;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamError;
import rocks.xmpp.core.stream.model.StreamErrorException;
import rocks.xmpp.core.stream.model.StreamFeatures;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.dns.DnsResolver;
import rocks.xmpp.dns.TxtRecord;
import rocks.xmpp.extensions.compress.CompressionMethod;
import rocks.xmpp.extensions.httpbind.model.Body;
import rocks.xmpp.util.XmppStreamDecoder;
import rocks.xmpp.util.XmppStreamEncoder;
import rocks.xmpp.util.XmppUtils;
import rocks.xmpp.util.concurrent.QueuedScheduledExecutorService;

/**
 * The abstract base class for BOSH connections.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0124.html">XEP-0124: Bidirectional-streams Over
 * Synchronous HTTP (BOSH)</a>
 * @see <a href="https://xmpp.org/extensions/xep-0206.html">XEP-0206: XMPP Over BOSH</a>
 */
public abstract class BoshConnection extends AbstractConnection {

    /**
     * The executor, which will execute HTTP requests.
     */
    static final ExecutorService HTTP_BIND_EXECUTOR =
            Executors.newCachedThreadPool(XmppUtils.createNamedThreadFactory("BOSH Request Thread"));

    protected static final System.Logger logger = System.getLogger(BoshConnection.class.getName());

    final ScheduledExecutorService inOrderRequestExecutor = new QueuedScheduledExecutorService(HTTP_BIND_EXECUTOR);

    protected final XmppSession xmppSession;

    /**
     * Guarded by "this".
     */
    protected final URL url;

    final BoshConnectionConfiguration boshConnectionConfiguration;

    /**
     * Our supported compression methods.
     */
    final Map<String, CompressionMethod> compressionMethods;

    /**
     * The encoding we can support. This is added as "Accept-Encoding" header in a request.
     */
    final String clientAcceptEncoding;

    /**
     * Use ConcurrentSkipListMap to maintain insertion order.
     */
    final Map<Long, Body.Builder> unacknowledgedRequests = new ConcurrentSkipListMap<>();

    /**
     * The request id. A large number which will get incremented with every request.
     */
    private final AtomicLong rid = new AtomicLong();

    private final Deque<String> keySequence = new ArrayDeque<>();

    /**
     * The current request count, i.e. the current number of simultaneous requests.
     */
    private final AtomicInteger requestCount = new AtomicInteger();

    /**
     * Maps the stream element which is sent to a future associated with it. The future is done, when the element has
     * been sent.
     */
    private final Map<StreamElement, CompletableFuture<Void>> sendFutures = new ConcurrentHashMap<>();

    /**
     * When sending, elements are put ("collected") into this collection first. Later, when the HTTP request is sent,
     * they are all put to the request. This allows to send multiple elements with one request.
     */
    private final Collection<Object> elementsToSend = new ArrayDeque<>();

    private final CompletableFuture<Void> closeFuture = new CompletableFuture<>();

    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    private final XmppStreamEncoder streamEncoder;

    private final XmppStreamDecoder streamDecoder;

    /**
     * The compression method which is used to compress requests. Guarded by "this".
     */
    CompressionMethod requestCompressionMethod;

    /**
     * Guarded by "elementsToSend".
     */
    private long highestReceivedRid;

    /**
     * The SID MUST be unique within the context of the connection manager application. Guarded by "this".
     */
    private String sessionId;

    /**
     * True, if the connection manager sends acknowledgments. Guarded by "this".
     */
    private boolean usingAcknowledgments;

    private SessionOpen sessionOpen;

    BoshConnection(final URL url, final XmppSession xmppSession, final BoshConnectionConfiguration configuration) {
        super(configuration, xmppSession, xmppSession::notifyException);
        this.url = url;
        this.xmppSession = xmppSession;
        this.boshConnectionConfiguration = configuration;

        compressionMethods = new LinkedHashMap<>();
        for (CompressionMethod compressionMethod : this.boshConnectionConfiguration.getCompressionMethods()) {
            compressionMethods.put(compressionMethod.getName(), compressionMethod);
        }
        if (!compressionMethods.isEmpty()) {
            clientAcceptEncoding = String.join(",", compressionMethods.keySet());
        } else {
            clientAcceptEncoding = null;
        }
        streamEncoder = new XmppStreamEncoder(xmppSession.getConfiguration().getXmlOutputFactory(),
                xmppSession::createMarshaller, s -> {
            if (s instanceof Body) {
                return ((Body) s).getWrappedObjects().stream().map(Object::getClass)
                        .anyMatch(clazz -> clazz == StreamFeatures.class || clazz == StreamError.class);
            }
            return false;
        });
        streamDecoder = new XmppStreamDecoder(xmppSession.getConfiguration().getXmlInputFactory(),
                xmppSession::createUnmarshaller, "");

        // Set the initial request id with a large random number.
        // The largest possible number for a RID is (2^53)-1
        // So initialize it with a random number with max value of 2^52.
        // This will still allow for at least 4503599627370495 requests (2^53-1-2^52), which should be sufficient.
        rid.set(new BigInteger(52, new Random()).longValue());
    }

    /**
     * <blockquote>
     * <p>All HTTP codes except 200 have been superseded by Terminal Binding Conditions to allow clients to determine
     * whether the source of errors is the connection manager application or an HTTP intermediary.</p>
     * <p>A legacy client (or connection manager) is a client (or connection manager) that did not include a 'ver'
     * attribute in its session creation request (or response). A legacy client (or connection manager) will interpret
     * (or respond with) HTTP error codes according to the table below.</p>
     * </blockquote>
     *
     * @param httpCode The HTTP response code.
     * @throws BoshException If the HTTP code was not 200.
     */
    private static void handleCode(int httpCode) throws BoshException {
        if (httpCode != HttpURLConnection.HTTP_OK) {
            switch (httpCode) {
                case HttpURLConnection.HTTP_BAD_REQUEST:
                    // Superseded by bad-request
                    throw new BoshException(Body.Condition.BAD_REQUEST, httpCode);
                case HttpURLConnection.HTTP_FORBIDDEN:
                    // Superseded by policy-violation
                    throw new BoshException(Body.Condition.POLICY_VIOLATION, httpCode);
                case HttpURLConnection.HTTP_NOT_FOUND:
                    // Superseded by item-not-found
                    throw new BoshException(Body.Condition.ITEM_NOT_FOUND, httpCode);
                default:
                    throw new BoshException(Body.Condition.UNDEFINED_CONDITION, httpCode);
            }
        }
    }

    /**
     * Gets the URL from the configuration.
     *
     * @param xmppSession   The session.
     * @param configuration The configuration.
     * @return The URL.
     * @throws MalformedURLException If the URL is malformed. This can only occur, if the URL has been discovered via
     *                               DNS TXT.
     */
    static URL getUrl(XmppSession xmppSession, BoshConnectionConfiguration configuration)
            throws MalformedURLException {
        URL url;
        String protocol = configuration.getChannelEncryption() == ChannelEncryption.DIRECT ? "https" : "http";
        // If no port has been configured, use the default ports.
        int targetPort = configuration.getPort() > 0 ? configuration.getPort()
                : (configuration.getChannelEncryption() == ChannelEncryption.DIRECT ? 5281 : 5280);
        // If a hostname has been configured, use it to connect.
        if (configuration.getHostname() != null) {
            url = new URL(protocol, configuration.getHostname(), targetPort, configuration.getPath());
        } else if (xmppSession.getDomain() != null) {
            // If a URL has not been set, try to find the URL by the domain via a DNS-TXT lookup
            // as described in XEP-0156.
            String resolvedUrl =
                    findBoshUrl(xmppSession.getDomain().toString(), xmppSession.getConfiguration().getNameServer(),
                            configuration.getConnectTimeout());
            if (resolvedUrl != null) {
                url = new URL(resolvedUrl);
            } else {
                // Fallback mechanism:
                // If the URL could not be resolved, use the domain name and port 5280 as default.
                url = new URL(protocol, xmppSession.getDomain().toString(), targetPort, configuration.getPath());
            }
        } else {
            throw new IllegalStateException("Neither an URL nor a domain given for a BOSH connection.");
        }
        return url;
    }

    /**
     * Tries to find the BOSH URL by a DNS TXT lookup as described in
     * <a href="https://xmpp.org/extensions/xep-0156.html">XEP-0156</a>.
     *
     * @param xmppServiceDomain The fully qualified domain name.
     * @param nameServer        The name server.
     * @param timeout           The lookup timeout.
     * @return The BOSH URL, if it could be found or null.
     */
    private static String findBoshUrl(String xmppServiceDomain, String nameServer, long timeout) {

        try {
            List<TxtRecord> txtRecords = DnsResolver.resolveTXT(xmppServiceDomain, nameServer, timeout);
            for (TxtRecord txtRecord : txtRecords) {
                Map<String, String> attributes = txtRecord.asAttributes();
                String url = attributes.get("_xmpp-client-xbosh");
                if (url != null) {
                    return url;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    WriterInterceptorChain newWriterChain() {
        List<WriterInterceptor> writerInterceptors = new ArrayList<>(xmppSession.getWriterInterceptors());
        writerInterceptors.add(streamEncoder);
        return new WriterInterceptorChain(writerInterceptors, xmppSession, this);
    }

    private ReaderInterceptorChain newReaderChain() {
        List<ReaderInterceptor> readerInterceptors = new ArrayList<>(xmppSession.getReaderInterceptors());
        readerInterceptors.add(streamDecoder);
        return new ReaderInterceptorChain(readerInterceptors, xmppSession, this);
    }

    /**
     * Generates a key sequence.
     *
     * @see <a href="https://xmpp.org/extensions/xep-0124.html#keys-generate">15.3 Generating the Key Sequence</a>
     */
    private void generateKeySequence() {
        keySequence.clear();
        try {
            // K(1) = hex(SHA-1(seed))
            // K(2) = hex(SHA-1(K(1)))
            // ...
            // K(n) = hex(SHA-1(K(n-1)))

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            Random random = new SecureRandom();
            // Generate a high random value "n"
            int n = 256 + random.nextInt(32768 - 256);
            // Generate a random seed value.
            byte[] seed = new byte[1024];
            random.nextBytes(seed);
            String kn = DatatypeConverter.printHexBinary(seed).toLowerCase();
            for (int i = 0; i < n; i++) {
                kn = DatatypeConverter.printHexBinary(digest.digest(kn.getBytes(StandardCharsets.UTF_8))).toLowerCase();
                keySequence.add(kn);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final CompletionStage<Void> open(final SessionOpen sessionOpen) {
        synchronized (this) {
            this.sessionOpen = sessionOpen;
        }
        // Create initial request.
        Body.Builder body = Body.builder()
                .language(xmppSession.getConfiguration().getLanguage())
                .version("1.11")
                .wait(boshConnectionConfiguration.getWait())
                .hold((byte) 1)
                .route(boshConnectionConfiguration.getRoute())
                .ack(1L)
                .from(sessionOpen.getFrom())
                .xmppVersion("1.0");

        if (xmppSession.getDomain() != null) {
            body.to(xmppSession.getDomain());
        }
        // Send the initial request.
        return sendNewRequest(body, false);
    }

    @Override
    public final boolean isSecure() {
        return boshConnectionConfiguration.getChannelEncryption() == ChannelEncryption.DIRECT;
    }

    /**
     * Gets the body element from the response and unpacks its content.
     *
     * <p>If it is the session creation response it contains additional attributes like the session id. These
     * attributes are set to this connection.</p>
     *
     * <p>The contents are delegated to the {@link rocks.xmpp.core.session.XmppSession#handleElement(Object)} method,
     * where they are treated as normal XMPP elements, i.e. the same way as in a normal TCP connection.</p>
     *
     * @param responseBody The body.
     * @throws Exception If any exception occurred during handling the inner XMPP elements.
     */
    private void unpackBody(Body responseBody) throws Exception {
        // It's the session creation response.
        if (responseBody.getSid() != null) {
            handleElement(responseBody);
            synchronized (this) {
                sessionId = responseBody.getSid();
                if (responseBody.getAck() != null) {
                    usingAcknowledgments = true;
                }
                // The connection manager MAY include an 'accept' attribute in the session creation response element,
                // to specify a comma-separated list of the content encodings it can decompress.
                if (responseBody.getAccept() != null) {
                    // After receiving a session creation response with an 'accept' attribute,
                    // clients MAY include an HTTP Content-Encoding header in subsequent requests
                    // (indicating one of the encodings specified in the 'accept' attribute)
                    // and compress the bodies of the requests accordingly.
                    String[] serverAcceptedEncodings = responseBody.getAccept().split(",", 16);
                    // Let's see if we can compress the contents for the server by choosing a known compression method.
                    for (String serverAcceptedEncoding : serverAcceptedEncodings) {
                        requestCompressionMethod = compressionMethods.get(serverAcceptedEncoding.trim().toLowerCase());
                        if (requestCompressionMethod != null) {
                            break;
                        }
                    }
                }
            }
        }

        if (responseBody.getAck() != null) {
            // The response has acknowledged another request.
            ackReceived(responseBody.getAck());
        }

        for (Object wrappedObject : responseBody.getWrappedObjects()) {
            handleElement(wrappedObject);
        }

        // If the body contains an error condition, which is not a stream error,
        // terminate the connection by throwing an exception.
        if (responseBody.getType() == Body.Type.TERMINATE) {
            handleElement(StreamHeader.CLOSING_STREAM_TAG);
            if (responseBody.getCondition() != null
                    && responseBody.getCondition() != Body.Condition.REMOTE_STREAM_ERROR) {
                // Shutdown the connection, we don't want to send further requests from now on.
                shutdown();
                closeFuture
                        .completeExceptionally(new BoshException(responseBody.getCondition(), responseBody.getUri()));
                throw new BoshException(responseBody.getCondition(), responseBody.getUri());
            }
        } else if (responseBody.getType() == Body.Type.ERROR) {
            // In any response it sends to the client, the connection manager MAY return a recoverable error by setting
            // a 'type' attribute of the <body/> element to "error".
            // These errors do not imply that the HTTP session is terminated.
            // If it decides to recover from the error, then the client MUST repeat the HTTP request that resulted in
            // the error, as well as all the preceding HTTP requests that have not received responses.
            // The content of these requests MUST be identical to the <body/> elements of the original requests.
            // This enables the connection manager to recover a session after the previous request was
            // lost due to a communication failure.
            unacknowledgedRequests.forEach((key, value) -> sendNewRequest(value, true));
        }
    }

    /**
     * Restarts the stream.
     *
     * <blockquote>
     * <p><cite><a href="https://xmpp.org/extensions/xep-0206.html#preconditions-sasl">Authentication and Resource
     * Binding</a></cite></p>
     * <p>Upon receiving the {@code <success/>} element, the client MUST then ask the connection manager to restart the
     * stream by sending a "restart request" that is structured as follows</p>
     * <ul>
     * <li>The BOSH {@code <body/>} element MUST include a boolean 'restart' attribute
     * (qualified by the 'urn:xmpp:xbosh' namespace) whose value is set to "true".</li>
     * <li>The BOSH {@code <body/>} element SHOULD include the 'to' attribute.</li>
     * <li>The BOSH {@code <body/>} element SHOULD include the 'xml:lang' attribute.</li>
     * <li>The BOSH {@code <body/>} element SHOULD be empty (i.e., not contain an XML stanza).
     * However, if the client includes an XML stanza in the body, the connection manager SHOULD ignore it.</li>
     * </ul>
     * </blockquote>
     */
    @Override
    protected final void restartStream() {
        Body.Builder bodyBuilder;
        synchronized (this) {
            bodyBuilder = Body.builder()
                    .sessionId(sessionId)
                    .restart(true)
                    .to(xmppSession.getDomain())
                    .language(xmppSession.getConfiguration().getLanguage())
                    .from(sessionOpen.getFrom());
        }
        sendNewRequest(bodyBuilder, false);
    }

    @Override
    protected CompletionStage<Void> closeStream() {
        final CompletableFuture<Void> future;
        if (!shutdown.get()) {
            final String sid = getSessionId();
            if (sid != null) {
                // Terminate the BOSH session.
                Body.Builder bodyBuilder = Body.builder()
                        .sessionId(sid)
                        .type(Body.Type.TERMINATE);

                future = sendNewRequest(bodyBuilder, false);
            } else {
                future = CompletableFuture.completedFuture(null);
            }
            shutdown();
        } else {
            future = CompletableFuture.completedFuture(null);
        }
        return future;
    }

    @Override
    protected CompletionStage<Void> closeConnection() {

        try {
            synchronized (this) {
                sessionId = null;
                requestCompressionMethod = null;
                keySequence.clear();
            }
        } finally {
            closeFuture.complete(null);
            shutdown();
        }
        return closeFuture;
    }

    @Override
    public final CompletionStage<Void> closeFuture() {
        return closeFuture;
    }

    private void shutdown() {
        shutdown.set(true);
    }

    /**
     * Detaches this BOSH session without closing (aka terminating) it. This way the BOSH session is still alive on the
     * server and can be ported over to a web page, but new BOSH requests are no longer sent by this connection.
     *
     * @return The current request ID (RID) which was used for the last BOSH request.
     * @see <a href="https://conversejs.org/docs/html/#prebinding-and-single-session-support">https://conversejs.org/docs/html/#prebinding-and-single-session-support</a>
     */
    public final long detach() {
        shutdown();
        // Return the latest and greatest rid.
        return rid.get();
    }

    @Override
    public final CompletableFuture<Void> send(StreamElement element) {
        CompletableFuture<Void> future = write(element);
        flush();
        return future;
    }

    @Override
    public final CompletableFuture<Void> write(StreamElement streamElement) {
        synchronized (elementsToSend) {
            elementsToSend.add(streamElement);
        }
        CompletableFuture<Void> sendFuture = new CompletableFuture<>();
        sendFutures.put(streamElement, sendFuture);
        return sendFuture;
    }

    @Override
    public final void flush() {
        sendNewRequest(Body.builder().sessionId(getSessionId()), false);
    }

    /**
     * Appends a key attribute to the body and generates a new key sequence if the old one is empty.
     *
     * @param bodyBuilder The builder.
     * @see <a href="https://xmpp.org/extensions/xep-0124.html#keys-use">15.4 Use of Keys</a>
     * @see <a href="https://xmpp.org/extensions/xep-0124.html#keys-switch">15.5 Switching to Another Key Sequence</a>
     */
    private void appendKey(Body.Builder bodyBuilder) {
        if (boshConnectionConfiguration.isUseKeySequence()) {
            synchronized (keySequence) {
                // For the initial request generate the sequence and set the new key.
                if (keySequence.isEmpty()) {
                    generateKeySequence();
                    bodyBuilder.newKey(keySequence.removeLast());
                } else {
                    // For every other request, set the key
                    bodyBuilder.key(keySequence.removeLast());
                    // and switch to a new sequence, if the sequence is empty.
                    if (keySequence.isEmpty()) {
                        generateKeySequence();
                        bodyBuilder.newKey(keySequence.removeLast());
                    }
                }
            }
        }
    }

    /**
     * Gets the session id of this BOSH connection.
     *
     * @return The session id.
     */
    public final synchronized String getSessionId() {
        return sessionId;
    }

//    @Override
//    public final synchronized String getStreamId() {
//        // The same procedure applies to the obsolete XMPP-specific 'authid' attribute of the BOSH <body/> element,
//        which contains the value of the XMPP stream ID generated by the XMPP server.
//        return authId;
//    }

    @Override
    public final InetSocketAddress getRemoteAddress() {
        return InetSocketAddress.createUnresolved(url.getHost(), url.getPort());
    }

    @Override
    public final synchronized boolean isUsingAcknowledgements() {
        return usingAcknowledgments;
    }

    /**
     * Sends all elements waiting in the queue to the server.
     *
     * <p>If there are currently more requests than allowed by the server, the waiting elements will be send as soon
     * one of the requests return.</p>
     *
     * @param bodyBuilder      The body builder.
     * @param resendAfterError If the body is resent after an error has occurred. In this case the RID is not
     *                         incremented.
     */
    private CompletableFuture<Void> sendNewRequest(final Body.Builder bodyBuilder, boolean resendAfterError) {

        if (!shutdown.get()) {

            final Body body;
            if (!resendAfterError) {
                synchronized (elementsToSend) {
                    // Prevent that the session is terminated with policy-violation due to this:
                    //
                    // If during any period the client sends a sequence of new requests equal in length to
                    // the number specified by the 'requests' attribute,
                    // and if the connection manager has not yet responded to any of the requests,
                    // and if the last request was empty
                    // and did not include either a 'pause' attribute or a 'type' attribute set to
                    // "terminate", and if the last two requests arrived within a period shorter than the
                    // number of seconds specified by the 'polling' attribute in the session creation
                    // response, then the connection manager SHOULD consider that the client is making
                    // requests more frequently than it was permitted and terminate the HTTP session and
                    // return a 'policy-violation' terminal binding error to the client.
                    //
                    // In short: If we would send a second empty request, don't do that!
                    // Also don't send a new request, if the connection is shutdown.
                    Body b = bodyBuilder.build();
                    if (b.getType() != Body.Type.TERMINATE
                            && (shutdown.get() || (requestCount.get() > 0
                            && b.getPause() == null
                            && !b.isRestart()
                            && getSessionId() != null
                            && elementsToSend.isEmpty()))) {
                        return CompletableFuture.completedFuture(null);
                    }

                    appendKey(bodyBuilder);

                    // Acknowledge the highest received rid.
                    // The only exception is that, after its session creation request,
                    // the client SHOULD NOT include an 'ack' attribute in any request if it has received
                    // responses to all its previous requests.
                    if (!unacknowledgedRequests.isEmpty()) {
                        bodyBuilder.ack(highestReceivedRid);
                    }
                    bodyBuilder.wrappedObjects(elementsToSend);
                    // Clear everything after the elements have been sent.
                    elementsToSend.clear();
                }
            }

            requestCount.getAndIncrement();

            // Create the writer for this connection.
            body = bodyBuilder.requestId(rid.getAndIncrement()).build();

            if (isUsingAcknowledgements()) {
                unacknowledgedRequests.put(body.getRid(), bodyBuilder);
            }

            return sendBody(body).whenComplete((aVoid, exc) -> {
                body.getWrappedObjects().stream()
                        .filter(wrappedObject -> wrappedObject instanceof StreamElement)
                        .forEach(wrappedObject -> {
                            StreamElement streamElement = (StreamElement) wrappedObject;
                            CompletableFuture<Void> future = sendFutures.remove(streamElement);
                            if (future != null) {
                                if (exc != null) {
                                    future.completeExceptionally(exc);
                                } else {
                                    future.complete(null);
                                }
                            }
                        });
                if (exc != null) {
                    rid.getAndDecrement();
                    xmppSession.notifyException(exc);
                    throw exc instanceof CompletionException ? (CompletionException) exc : new CompletionException(exc);
                }
            });
        } else {
            throw new IllegalStateException("Connection already shutdown via close() or detach()");
        }
    }

    final void handleSuccessfulResponse(Reader reader, Body requestBody) throws Exception {
        try {
            // The response itself acknowledges the request, so we can remove the request.
            ackReceived(requestBody.getRid());

            // We received a response for the request. Store the RID, so that we can inform the
            // connection manager with our next request, that we received a response.
            synchronized (elementsToSend) {
                highestReceivedRid = requestBody.getRid() != null ? requestBody.getRid() : 0;
            }

            ReaderInterceptorChain readerInterceptorChain = newReaderChain();
            List<StreamElement> streamElements = new ArrayList<>();
            readerInterceptorChain.proceed(reader, streamElements::add);
            for (StreamElement element : streamElements) {
                if (element instanceof Body) {
                    this.unpackBody((Body) element);
                }
            }
        } catch (StreamErrorException e) {
            logger.log(System.Logger.Level.WARNING, "Server responded with malformed XML.",
                    e);
        } finally {
            // As soon as the client receives a response from the connection manager it sends another
            // request, thereby ensuring that the connection manager is (almost) always holding a
            // request that it can use to "push" data to the client.
            if (requestCount.decrementAndGet() == 0) {
                // Wait shortly before sending the next long polling request.
                // This allows the send method to chime in and send a <body/> with actual payload
                // instead of an empty body just to "hold the line".
                inOrderRequestExecutor.schedule(() -> {
                    sendNewRequest(Body.builder().sessionId(sessionId), false);
                }, 100, TimeUnit.MILLISECONDS);
            }
        }
    }

    final void handleErrorHttpResponse(int httpResponseCode) throws BoshException {
        // Shutdown the connection, we don't want to send further requests from now on.
        shutdown();
        handleCode(httpResponseCode);
    }

    /**
     * Sends a body using a specific transport.
     *
     * @param body The body.
     * @return The future which is complete, when the body has been sent.
     */
    protected abstract CompletableFuture<Void> sendBody(Body body);

    private void ackReceived(Long rid) {
        if (rid != null) {
            Body.Builder body = unacknowledgedRequests.remove(rid);
            if (body != null) {
                body.build().getWrappedObjects().stream().filter(object -> object instanceof Stanza).forEach(object -> {
                    Stanza stanza = (Stanza) object;
                    xmppSession.markAcknowledged(stanza);
                });
            }
        }
    }

    /**
     * Gets the route.
     *
     * <blockquote>
     * <p>A connection manager MAY be configured to enable sessions with more than one server in different domains.
     * When requesting a session with such a "proxy" connection manager, a client SHOULD include a 'route' attribute
     * that specifies the protocol, hostname, and port of the server with which it wants to communicate, formatted as
     * "proto:host:port" (e.g., "xmpp:example.com:9999").</p>
     * </blockquote>
     *
     * @return The route.
     */
    public final String getRoute() {
        return boshConnectionConfiguration.getRoute();
    }
}
