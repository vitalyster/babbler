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

package org.xmpp.extension.servicediscovery;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.*;
import org.xmpp.extension.dataforms.DataForm;
import org.xmpp.extension.servicediscovery.info.Feature;
import org.xmpp.extension.servicediscovery.info.Identity;
import org.xmpp.extension.servicediscovery.info.InfoDiscovery;
import org.xmpp.extension.servicediscovery.info.InfoNode;
import org.xmpp.extension.servicediscovery.items.ItemDiscovery;
import org.xmpp.extension.servicediscovery.items.ItemNode;
import org.xmpp.stanza.IQ;
import org.xmpp.stanza.StanzaException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Schudt
 */
public class ServiceDiscoveryTest extends BaseTest {

    @Test
    public void unmarshalServiceDiscoveryResponse() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='plays.shakespeare.lit'\n" +
                "    to='romeo@montague.net/orchard'\n" +
                "    id='info1'>\n" +
                "  <query xmlns='http://jabber.org/protocol/disco#info'>\n" +
                "    <identity\n" +
                "        category='conference'\n" +
                "        type='text'\n" +
                "        name='Play-Specific Chatrooms'/>\n" +
                "    <identity\n" +
                "        category='directory'\n" +
                "        type='chatroom'\n" +
                "        name='Play-Specific Chatrooms'/>\n" +
                "    <feature var='http://jabber.org/protocol/disco#info'/>\n" +
                "    <feature var='http://jabber.org/protocol/disco#items'/>\n" +
                "    <feature var='http://jabber.org/protocol/muc'/>\n" +
                "    <feature var='jabber:iq:register'/>\n" +
                "    <feature var='jabber:iq:search'/>\n" +
                "    <feature var='jabber:iq:time'/>\n" +
                "    <feature var='jabber:iq:version'/>\n" +
                "  </query>\n" +
                "</iq>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        InfoDiscovery serviceDiscovery = iq.getExtension(InfoDiscovery.class);
        Assert.assertNotNull(serviceDiscovery);
        Assert.assertEquals(serviceDiscovery.getIdentities().size(), 2);
        Assert.assertEquals(serviceDiscovery.getFeatures().size(), 7);

        Identity identity1 = new Identity("conference", "text", "Play-Specific Chatrooms");
        Identity identity2 = new Identity("directory", "chatroom", "Play-Specific Chatrooms");

        Assert.assertTrue(serviceDiscovery.getIdentities().contains(identity1));
        Assert.assertTrue(serviceDiscovery.getIdentities().contains(identity2));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("http://jabber.org/protocol/disco#info")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("http://jabber.org/protocol/disco#items")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("http://jabber.org/protocol/muc")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("jabber:iq:register")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("jabber:iq:search")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("jabber:iq:time")));
        Assert.assertTrue(serviceDiscovery.getFeatures().contains(new Feature("jabber:iq:version")));
    }

    @Test
    public void unmarshalServiceDiscoveryItemResponse() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    from='catalog.shakespeare.lit'\n" +
                "    to='romeo@montague.net/orchard'\n" +
                "    id='items2'>\n" +
                "  <query xmlns='http://jabber.org/protocol/disco#items'>\n" +
                "    <item jid='catalog.shakespeare.lit'\n" +
                "          node='books'\n" +
                "          name='Books by and about Shakespeare'/>\n" +
                "    <item jid='catalog.shakespeare.lit'\n" +
                "          node='clothing'\n" +
                "          name='Wear your literary taste with pride'/>\n" +
                "    <item jid='catalog.shakespeare.lit'\n" +
                "          node='music'\n" +
                "          name='Music from the time of Shakespeare'/>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        ItemNode itemNode = iq.getExtension(ItemDiscovery.class);
        Assert.assertNotNull(itemNode);
        Assert.assertEquals(itemNode.getItems().size(), 3);
        Assert.assertEquals(itemNode.getItems().get(0).getJid(), Jid.fromString("catalog.shakespeare.lit"));
        Assert.assertEquals(itemNode.getItems().get(0).getNode(), "books");
        Assert.assertEquals(itemNode.getItems().get(0).getName(), "Books by and about Shakespeare");
        Assert.assertEquals(itemNode.getItems().get(1).getJid(), Jid.fromString("catalog.shakespeare.lit"));
        Assert.assertEquals(itemNode.getItems().get(1).getNode(), "clothing");
        Assert.assertEquals(itemNode.getItems().get(1).getName(), "Wear your literary taste with pride");
        Assert.assertEquals(itemNode.getItems().get(2).getJid(), Jid.fromString("catalog.shakespeare.lit"));
        Assert.assertEquals(itemNode.getItems().get(2).getNode(), "music");
        Assert.assertEquals(itemNode.getItems().get(2).getName(), "Music from the time of Shakespeare");
    }

    @Test
    public void testFeatureEquals() {
        ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addFeature(new Feature("http://jabber.org/protocol/muc"));
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(new Feature("http://jabber.org/protocol/muc")));
    }

    @Test
    public void testItemsEquals() {
        ServiceDiscoveryManager serviceDiscoveryManager = connection.getExtensionManager(ServiceDiscoveryManager.class);
        serviceDiscoveryManager.addIdentity(new Identity("conference", "text", "name1", "en"));
        Assert.assertTrue(serviceDiscoveryManager.getIdentities().contains(new Identity("conference", "text", "name2", "en")));
    }

    @Test
    public void testInfoDiscovery() throws IOException, TimeoutException, StanzaException {
        MockServer mockServer = new MockServer();
        TestConnection connection1 = new TestConnection(ROMEO, mockServer);
        new TestConnection(JULIET, mockServer);
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        InfoNode result = serviceDiscoveryManager.discoverInformation(JULIET);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getFeatures().size() > 1);
        //  Every entity MUST have at least one identity
        Assert.assertTrue(result.getIdentities().size() > 0);
    }

    @Test
    public void testServiceDiscoveryEntry() {
        TestConnection connection1 = new TestConnection();
        ServiceDiscoveryManager serviceDiscoveryManager = connection1.getExtensionManager(ServiceDiscoveryManager.class);
        // By default, the manager should be enabled.
        Assert.assertTrue(serviceDiscoveryManager.isEnabled());
        Feature featureInfo = new Feature("http://jabber.org/protocol/disco#info");
        Feature featureItems = new Feature("http://jabber.org/protocol/disco#items");
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(featureInfo));
        Assert.assertTrue(serviceDiscoveryManager.getFeatures().contains(featureItems));
        serviceDiscoveryManager.setEnabled(false);
        Assert.assertFalse(serviceDiscoveryManager.isEnabled());
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(featureInfo));
        Assert.assertFalse(serviceDiscoveryManager.getFeatures().contains(featureItems));
    }

    @Test
    public void unmarshalServiceDiscoveryExtension() throws JAXBException, XMLStreamException {
        String xml = "<iq type='result'\n" +
                "    from='shakespeare.lit'\n" +
                "    to='capulet.com'\n" +
                "    id='disco1'>\n" +
                "  <query xmlns='http://jabber.org/protocol/disco#info'>\n" +
                "    <identity\n" +
                "        category='server'\n" +
                "        type='im'\n" +
                "        name='shakespeare.lit jabber server'/>\n" +
                "    <feature var='jabber:iq:register'/>\n" +
                "    <x xmlns='jabber:x:data' type='result'>\n" +
                "      <field var='FORM_TYPE' type='hidden'>\n" +
                "        <value>http://jabber.org/network/serverinfo</value>\n" +
                "      </field>\n" +
                "      <field var='c2s_port'>\n" +
                "        <value>5222</value>\n" +
                "      </field>\n" +
                "      <field var='c2s_port_ssl'>\n" +
                "        <value>5223</value>\n" +
                "      </field>\n" +
                "      <field var='http_access'>\n" +
                "        <value>http://shakespeare.lit/jabber</value>\n" +
                "      </field>\n" +
                "      <field var='ip_version'>\n" +
                "        <value>ipv4</value>\n" +
                "        <value>ipv6</value>\n" +
                "      </field>\n" +
                "      <field var='info_url'>\n" +
                "        <value>http://shakespeare.lit/support.php</value>\n" +
                "      </field>\n" +
                "    </x>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        InfoNode infoDiscovery = iq.getExtension(InfoDiscovery.class);
        Assert.assertNotNull(infoDiscovery);
        Assert.assertEquals(infoDiscovery.getExtensions().size(), 1);
        Assert.assertEquals(infoDiscovery.getExtensions().get(0).getType(), DataForm.Type.RESULT);
    }
}
