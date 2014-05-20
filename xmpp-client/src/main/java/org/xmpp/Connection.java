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

import org.xmpp.stream.ClientStreamElement;

import java.io.Closeable;
import java.io.IOException;
import java.net.Proxy;

/**
 * @author Christian Schudt
 */
public abstract class Connection implements Closeable {

    /**
     * The proxy, which is used while connecting to a host.
     */
    private final Proxy proxy;

    private final String hostname;

    private final int port;

    protected XmppSession xmppSession;

    /**
     * Any exception that occurred during stream negotiation ({@link #connect()}).
     */
    private volatile Exception exception;

    /**
     * Creates a connection to the specified host and port through a proxy.
     *
     * @param hostname The host, which is used to establish the connection.
     * @param port     The port, which is used to establish the connection.
     * @param proxy    The proxy.
     */
    protected Connection(XmppSession xmppSession, String hostname, int port, Proxy proxy) {
        this.xmppSession = xmppSession;
        this.hostname = hostname;
        this.port = port;
        this.proxy = proxy;
    }

    public void setXmppSession(XmppSession xmppSession) {
        this.xmppSession = xmppSession;
    }

    /**
     * Gets the hostname, which is used for the connection.
     *
     * @return The hostname.
     */
    public final String getHostname() {
        return hostname;
    }

    /**
     * Gets the port, which is used for the connection.
     *
     * @return The port.
     */
    public final int getPort() {
        return port;
    }

    /**
     * Gets the proxy.
     *
     * @return The proxy.
     */
    public final Proxy getProxy() {
        return proxy;
    }

    /**
     * Restarts the stream.
     */
    protected abstract void restartStream();

    public abstract void send(ClientStreamElement clientStreamElement);

    /**
     * Compresses the stream.
     */
    protected void compressStream() {
    }

    public abstract void connect() throws IOException;

    /**
     * Secures the connection, i.e. negotiates TLS.
     *
     * @throws IOException If an error occurs during TLS negotiation.
     */
    protected void secureConnection() throws IOException {
    }
}
