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

package rocks.xmpp.extensions.sm.model;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stream.model.StreamError;

/**
 * Tests for the {@link StreamManagement} class.
 *
 * @author Christian Schudt
 */
public class StreamManagementTest extends XmlTest {

    @Test
    public void unmarshalFeature() throws XMLStreamException, JAXBException {
        String xml = "<sm xmlns='urn:xmpp:sm:3'/>";
        StreamManagement feature = unmarshal(xml, StreamManagement.class);
        Assert.assertNotNull(feature);
    }

    @Test
    public void marshalEnable() throws JAXBException, XMLStreamException {
        StreamManagement.Enable enable = new StreamManagement.Enable();
        String xml = marshal(enable);
        Assert.assertEquals(xml, "<enable xmlns=\"urn:xmpp:sm:3\"></enable>");
    }

    @Test
    public void marshalEnableWithResume() throws JAXBException, XMLStreamException {
        StreamManagement.Enable enable = new StreamManagement.Enable(true, 23);
        String xml = marshal(enable);
        Assert.assertEquals(xml, "<enable xmlns=\"urn:xmpp:sm:3\" resume=\"true\" max=\"23\"></enable>");
    }

    @Test
    public void unmarshalEnabled() throws XMLStreamException, JAXBException {
        String xml = "<enabled xmlns='urn:xmpp:sm:3' id='some-long-sm-id' resume='true' max='23' location='domain'/>";
        StreamManagement.Enabled enabled = unmarshal(xml, StreamManagement.Enabled.class);
        Assert.assertNotNull(enabled);
        Assert.assertEquals(enabled.getId(), "some-long-sm-id");
        Assert.assertTrue(enabled.isResume());
        Assert.assertEquals(enabled.getLocation(), "domain");
        Assert.assertEquals(enabled.getMax(), (Integer) 23);
    }

    @Test
    public void unmarshalFailed() throws XMLStreamException, JAXBException {
        String xml = "<failed xmlns='urn:xmpp:sm:3' h='3'>\n" +
                "     <unexpected-request xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>\n" +
                "   </failed>";
        StreamManagement.Failed failed = unmarshal(xml, StreamManagement.Failed.class);
        Assert.assertNotNull(failed);
        Assert.assertSame(failed.getError(), Condition.UNEXPECTED_REQUEST);
        Assert.assertEquals(failed.getLastHandledStanza(), Long.valueOf(3));
    }

    @Test
    public void marshalAnswer() throws JAXBException, XMLStreamException {
        StreamManagement.Answer answer = new StreamManagement.Answer(1);
        String xml = marshal(answer);
        Assert.assertEquals(xml, "<a xmlns=\"urn:xmpp:sm:3\" h=\"1\"></a>");
    }

    @Test
    public void marshalRequest() throws JAXBException, XMLStreamException {
        String xml = marshal(StreamManagement.REQUEST);
        Assert.assertEquals(xml, "<r xmlns=\"urn:xmpp:sm:3\"></r>");
    }

    @Test
    public void unmarshalResume() throws XMLStreamException, JAXBException {
        String xml = "<resume xmlns='urn:xmpp:sm:3' \n" +
                "            h='2'\n" +
                "            previd='some-long-sm-id'/>\n";

        StreamManagement.Resume resume = unmarshal(xml, StreamManagement.Resume.class);
        Assert.assertNotNull(resume);
        Assert.assertEquals(resume.getPreviousId(), "some-long-sm-id");
        Assert.assertEquals(resume.getLastHandledStanza(), Long.valueOf(2));
    }

    @Test
    public void unmarshalResumed() throws XMLStreamException, JAXBException {
        String xml = "<resumed xmlns='urn:xmpp:sm:3' \n" +
                "            h='2'\n" +
                "            previd='some-long-sm-id'/>\n";

        StreamManagement.Resumed resumed = unmarshal(xml, StreamManagement.Resumed.class);
        Assert.assertNotNull(resumed);
        Assert.assertEquals(resumed.getPreviousId(), "some-long-sm-id");
        Assert.assertEquals(resumed.getLastHandledStanza(), Long.valueOf(2));
    }

    @Test
    public void unmarshalHandledCountTooHigh() throws XMLStreamException, JAXBException {
        String xml = "<error xmlns='http://etherx.jabber.org/streams'>\n" +
                "  <undefined-condition xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "  <handled-count-too-high xmlns='urn:xmpp:sm:3' h='10' send-count='8'/>\n" +
                "  <text xml:lang='en' xmlns='urn:ietf:params:xml:ns:xmpp-streams'>\n" +
                "    You acknowledged 10 stanzas, but I only sent you 8 so far.\n" +
                "  </text>\n" +
                "</error>\n";

        StreamError error = unmarshal(xml, StreamError.class);
        Assert.assertNotNull(error);
        StreamManagement.HandledCountTooHigh handledCountTooHigh =
                (StreamManagement.HandledCountTooHigh) error.getExtension();
        Assert.assertNotNull(handledCountTooHigh);
        Assert.assertEquals(handledCountTooHigh.getLastHandledStanza(), Long.valueOf(10));
        Assert.assertEquals(handledCountTooHigh.getSendCount(), Long.valueOf(8));
    }
}
