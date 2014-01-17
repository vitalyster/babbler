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

package org.xmpp.extension.nickname;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.MockServer;
import org.xmpp.TestConnection;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.ping.Ping;
import org.xmpp.extension.ping.PingManager;
import org.xmpp.extension.servicediscovery.ServiceDiscoveryManager;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.Presence;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public class NicknameTest extends BaseTest {

    @Test
    public void unmarshalNickname() throws XMLStreamException, JAXBException {
        String xml = "<presence from='narrator@moby-dick.lit' to='starbuck@moby-dick.lit' type='subscribe'>\n" +
                "  <nick xmlns='http://jabber.org/protocol/nick'>Ishmael</nick>\n" +
                "</presence>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Presence presence = (Presence) unmarshaller.unmarshal(xmlEventReader);
        Nickname nickname = presence.getExtension(Nickname.class);
        Assert.assertNotNull(nickname);
        Assert.assertEquals(nickname.getValue(), "Ishmael");
    }

    @Test
    public void marshalNickname() throws JAXBException, XMLStreamException, IOException {
        String xml = marshall(new Nickname("Ishmael"));
        Assert.assertEquals("<nick xmlns=\"http://jabber.org/protocol/nick\">Ishmael</nick>", xml);
    }

}
