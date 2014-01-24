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

package org.xmpp.extension.privatedata.rosterdelimiter;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;
import org.xmpp.extension.privatedata.PrivateData;
import org.xmpp.stanza.IQ;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class RosterDelimiterTest extends BaseTest {

    @Test
    public void unmarshalAnnotations() throws XMLStreamException, JAXBException {
        String xml = "<iq type='result'\n" +
                "    id='1'\n" +
                "    from='bill@shakespeare.lit/Globe'\n" +
                "    to='bill@shakespeare.lit/Globe'>\n" +
                "  <query xmlns='jabber:iq:private'>\n" +
                "    <roster xmlns='roster:delimiter'>::</roster>\n" +
                "  </query>\n" +
                "</iq>\n";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        IQ iq = (IQ) unmarshaller.unmarshal(xmlEventReader);
        PrivateData privateData = iq.getExtension(PrivateData.class);
        Assert.assertNotNull(privateData);
        Assert.assertEquals(privateData.getItems().size(), 1);
        Assert.assertTrue(privateData.getItems().get(0) instanceof RosterDelimiter);
        RosterDelimiter rosterDelimiter = (RosterDelimiter) privateData.getItems().get(0);
        Assert.assertEquals(rosterDelimiter.getRosterDelimiter(), "::");
    }

    @Test
    public void marshalRosterDelimiterQuery() throws JAXBException, XMLStreamException, IOException {
        IQ iq = new IQ("1", IQ.Type.GET, new PrivateData(new RosterDelimiter()));
        String xml = marshall(iq);
        Assert.assertEquals(xml, "<iq id=\"1\" type=\"get\"><query xmlns=\"jabber:iq:private\"><roster xmlns=\"roster:delimiter\"></roster></query></iq>");
    }
}
