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

package org.xmpp.extension.reach;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.*;
import org.xmpp.extension.attention.Attention;
import org.xmpp.extension.attention.AttentionManager;
import org.xmpp.extension.disco.ServiceDiscoveryManager;
import org.xmpp.extension.disco.info.Feature;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.net.URI;
import java.util.Arrays;

/**
 * @author Christian Schudt
 */
public class ReachabilityTest extends BaseTest {

    @Test
    public void unmarshalReachability() throws XMLStreamException, JAXBException {
        String xml = "<reach xmlns='urn:xmpp:reach:0'>\n" +
                "          <addr uri='tel:+1-303-555-1212'>\n" +
                "            <desc xml:lang='en'>Conference room phone</desc>\n" +
                "          </addr>\n" +
                "          <addr uri='sip:room123@example.com'>\n" +
                "            <desc xml:lang='en'>In-room video system</desc>\n" +
                "          </addr>\n" +
                "        </reach>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Reachability reachability = (Reachability) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertNotNull(reachability);
        Assert.assertEquals(reachability.getAddresses().size(), 2);
        Assert.assertEquals(reachability.getAddresses().get(0).getUri(), URI.create("tel:+1-303-555-1212"));
        Assert.assertEquals(reachability.getAddresses().get(0).getDescriptions().size(), 1);
        Assert.assertEquals(reachability.getAddresses().get(0).getDescriptions().get(0).getValue(), "Conference room phone");
        Assert.assertEquals(reachability.getAddresses().get(0).getDescriptions().get(0).getLanguage(), "en");
        Assert.assertEquals(reachability.getAddresses().get(1).getUri(), URI.create("sip:room123@example.com"));
        Assert.assertEquals(reachability.getAddresses().get(1).getDescriptions().get(0).getValue(), "In-room video system");
        Assert.assertEquals(reachability.getAddresses().get(1).getDescriptions().get(0).getLanguage(), "en");
    }

    @Test
    public void testReachabilityEquality() {

        Reachability reachability1 = new Reachability(Arrays.asList(new Address(URI.create("sip:room123@example.com"), new Address.Description("In-room video system", "en"))));
        Reachability reachability2 = new Reachability(Arrays.asList(new Address(URI.create("sip:room123@example.com"), new Address.Description("In-room video system", "en"))));

        Assert.assertEquals(reachability1, reachability2);
    }

    @Test
    public void testReachabilityManager() {

        MockServer mockServer = new MockServer();

        Connection connection1 = new TestConnection(ROMEO, mockServer);
        Connection connection2 = new TestConnection(JULIET, mockServer);

        final boolean[] attentionReceived = {false};
        connection2.addMessageListener(new MessageListener() {
            @Override
            public void handle(MessageEvent e) {
                if (e.isIncoming() && e.getMessage().getExtension(Attention.class) != null && e.getMessage().getType() == Message.Type.HEADLINE) {
                    attentionReceived[0] = true;
                    Assert.assertEquals(e.getMessage().getType(), Message.Type.HEADLINE);
                }
            }
        });

        AttentionManager attentionManager = connection1.getExtensionManager(AttentionManager.class);
        attentionManager.captureAttention(JULIET);

        Assert.assertTrue(attentionReceived[0]);
    }

    @Test
    public void testServiceDiscoveryEntry() {

        TestConnection connection1 = new TestConnection();
        ReachabilityManager reachabilityManager = connection1.getExtensionManager(ReachabilityManager.class);
        Assert.assertFalse(reachabilityManager.isEnabled());
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        Feature feature = new Feature("urn:xmpp:reach:0");
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(feature));
        reachabilityManager.setEnabled(true);
        Assert.assertTrue(reachabilityManager.isEnabled());
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(feature));
    }
}
