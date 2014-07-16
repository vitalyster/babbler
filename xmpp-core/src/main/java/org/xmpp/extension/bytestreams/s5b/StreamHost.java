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

package org.xmpp.extension.bytestreams.s5b;

import org.xmpp.Jid;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * The {@code <streamhost/>} element.
 *
 * @author Christian Schudt
 */
public final class StreamHost {

    @XmlAttribute(name = "jid")
    private Jid jid;

    @XmlAttribute(name = "host")
    private String host;

    @XmlAttribute(name = "port")
    private int port = 1080;

    private StreamHost() {
    }

    /**
     * Creates a {@code <streamhost/>} element.
     *
     * @param jid  The JID.
     * @param host The host address.
     * @param port The port.
     */
    public StreamHost(Jid jid, String host, int port) {
        this.jid = jid;
        this.host = host;
        this.port = port;
    }

    /**
     * Gets the IP address or DNS domain name of the StreamHost for SOCKS5 communication over TCP.
     *
     * @return The host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the JabberID of the StreamHost for communication over XMPP.
     *
     * @return The JID.
     */
    public Jid getJid() {
        return jid;
    }

    /**
     * Get the port on which to connect for SOCKS5 communication over TCP.
     *
     * @return The port.
     */
    public int getPort() {
        return port;
    }
}
