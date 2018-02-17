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

import rocks.xmpp.core.session.debug.XmppDebugger;
import rocks.xmpp.core.stanza.model.Stanza;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.extensions.sm.StreamManager;
import rocks.xmpp.util.XmppUtils;

import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is responsible for opening and closing the XMPP stream as well as writing any XML elements to the stream.
 * <p>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 */
final class XmppStreamWriter {

    private final XmppSession xmppSession;

    private final ScheduledExecutorService executor;

    private final Marshaller marshaller;

    private final XmppDebugger debugger;

    private final String namespace;

    private final StreamManager streamManager;

    /**
     * Will be accessed only by the writer thread.
     */
    private XMLStreamWriter xmlStreamWriter;

    /**
     * Will be accessed only by the writer thread.
     */
    private ByteArrayOutputStream byteArrayOutputStream;

    /**
     * Indicates whether the stream has been opened. Will be accessed only by the writer thread.
     */
    private boolean streamOpened;

    XmppStreamWriter(String namespace, StreamManager streamManager, final XmppSession xmppSession) {
        this.namespace = namespace;
        this.xmppSession = xmppSession;
        this.marshaller = xmppSession.createMarshaller();
        this.debugger = xmppSession.getDebugger();
        this.executor = Executors.newSingleThreadScheduledExecutor(xmppSession.getConfiguration().getThreadFactory("XMPP Writer Thread"));
        this.streamManager = streamManager;
    }

    void initialize(int keepAliveInterval) {
        if (keepAliveInterval > 0) {
            executor.scheduleAtFixedRate(() -> {
                if (EnumSet.of(XmppSession.Status.CONNECTED, XmppSession.Status.AUTHENTICATED).contains(xmppSession.getStatus())) {
                    try {
                        xmlStreamWriter.writeCharacters(" ");
                        xmlStreamWriter.flush();
                    } catch (Exception e) {
                        notifyException(e);
                    }
                }
            }, 0, keepAliveInterval, TimeUnit.SECONDS);
        }
    }

    CompletableFuture<Void> send(final StreamElement clientStreamElement) {
        Objects.requireNonNull(clientStreamElement);
        return CompletableFuture.runAsync(() -> {
            try {
                // When about to send a stanza, first put the stanza (paired with the current value of X) in an "unacknowledged" queue.
                if (clientStreamElement instanceof Stanza) {
                    streamManager.markUnacknowledged((Stanza) clientStreamElement);
                }

                marshaller.marshal(clientStreamElement, xmlStreamWriter);
                xmlStreamWriter.flush();

                if (clientStreamElement instanceof Stanza) {
                    // Workaround: Simulate keep-alive packet to convince client to process the already transmitted packet.
                    xmlStreamWriter.writeCharacters(" ");
                    xmlStreamWriter.flush();
                }

                if (debugger != null) {
                    debugger.writeStanza(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8).trim(), clientStreamElement);
                    byteArrayOutputStream.reset();
                }
            } catch (Exception e) {
                notifyException(e);
                throw new CompletionException(e);
            }
        }, executor);
    }

    void openStream(final OutputStream outputStream, final StreamHeader streamHeader) {
        executor.execute(() -> {
            try {

                OutputStream xmppOutputStream = null;
                if (debugger != null) {
                    byteArrayOutputStream = new ByteArrayOutputStream();
                    xmppOutputStream = XmppUtils.createBranchedOutputStream(outputStream, byteArrayOutputStream);
                    OutputStream debuggerOutputStream = debugger.createOutputStream(xmppOutputStream);
                    if (debuggerOutputStream != null) {
                        xmppOutputStream = debuggerOutputStream;
                    }
                }
                if (xmppOutputStream == null) {
                    xmppOutputStream = outputStream;
                }
                XMLStreamWriter writer = xmppSession.getConfiguration().getXmlOutputFactory().createXMLStreamWriter(xmppOutputStream, "UTF-8");

                xmlStreamWriter = XmppUtils.createXmppStreamWriter(writer, namespace);
                streamOpened = false;

                streamHeader.writeTo(writer);

                if (debugger != null) {
                    debugger.writeStanza(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8).trim(), null);
                    byteArrayOutputStream.reset();
                }
                streamOpened = true;
            } catch (Exception e) {
                notifyException(e);
            }
        });
    }

    private CompletableFuture<Void> closeStream() {
        return CompletableFuture.runAsync(() -> {
            if (streamOpened) {
                // Close the stream.
                try {
                    xmlStreamWriter.writeEndDocument();
                    xmlStreamWriter.flush();
                    if (debugger != null) {
                        debugger.writeStanza(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8).trim(), null);
                        byteArrayOutputStream.reset();
                    }
                    xmlStreamWriter.close();
                    streamOpened = false;
                } catch (Exception e) {
                    notifyException(e);
                }
            }
        }, executor);
    }

    /**
     * Shuts down the executors, cleans up resources and notifies the session about the exception.
     *
     * @param exception The exception which occurred during writing.
     */
    private void notifyException(Exception exception) {

        // Shutdown the executors.
        synchronized (this) {
            executor.shutdown();

            if (xmlStreamWriter != null) {
                try {
                    xmlStreamWriter.close();
                    xmlStreamWriter = null;
                } catch (Exception e) {
                    exception.addSuppressed(e);
                }
            }
            byteArrayOutputStream = null;
        }

        xmppSession.notifyException(exception);
    }

    /**
     * Closes the stream by sending a closing {@code </stream:stream>} to the server.
     * This method waits until this task is completed, but not more than 0.25 seconds.
     */
    CompletableFuture<Void> shutdown() {
        return closeStream().whenComplete((aVoid, throwable) -> {
            executor.shutdown();
            try {
                // Wait for the closing stream element to be sent before we can close the socket.
                if (!executor.awaitTermination(50, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                // (Re-)Cancel if current thread also interrupted
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
    }
}
