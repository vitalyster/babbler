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

import org.jivesoftware.smack.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.Jid;
import org.xmpp.XmlTest;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Christian Schudt
 */
public class Socks5ByteStreamsTest extends XmlTest {
    protected Socks5ByteStreamsTest() throws JAXBException, XMLStreamException {
        super(Socks5ByteStream.class);
    }

    @Test
    public void unmarshalSocks5() throws XMLStreamException, JAXBException {
        String xml = "<query xmlns='http://jabber.org/protocol/bytestreams'>\n" +
                "    <streamhost\n" +
                "        host='24.24.24.1'\n" +
                "        jid='streamer.example.com'\n" +
                "        port='7625'/>\n" +
                "  </query>\n";

        Socks5ByteStream data = unmarshal(xml, Socks5ByteStream.class);
        Assert.assertNotNull(data);
        Assert.assertEquals(data.getStreamHosts().size(), 1);
        Assert.assertEquals(data.getStreamHosts().get(0).getHost(), "24.24.24.1");
        Assert.assertEquals(data.getStreamHosts().get(0).getJid(), Jid.valueOf("streamer.example.com"));
        Assert.assertEquals(data.getStreamHosts().get(0).getPort(), 7625);
    }
}
