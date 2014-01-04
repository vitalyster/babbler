/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Christian Schudt
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

package org.xmpp.stream;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.UnmarshalHelper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class StreamErrorTest extends BaseTest {

    @Test
    public void unmarshalBadFormat() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <bad-format\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        StreamError streamError = (StreamError) unmarshaller.unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.BadFormat);
    }

    @Test
    public void unmarshalBadNamespacePrefix() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <bad-namespace-prefix\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.BadNamespacePrefix);
    }

    @Test
    public void unmarshalConflict() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <conflict\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.Conflict);
    }

    @Test
    public void unmarshalConnectionTimeout() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <connection-timeout\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.ConnectionTimeout);
    }

    @Test
    public void unmarshalHostGone() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <host-gone\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.HostGone);
    }

    @Test
    public void unmarshalHostUnknown() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <host-unknown\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.HostUnknown);
    }

    @Test
    public void unmarshalImproperAddressing() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <improper-addressing\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.ImproperAddressing);
    }

    @Test
    public void unmarshalInternalServerError() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <internal-server-error\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.InternalServerError);
    }

    @Test
    public void unmarshalInvalidFrom() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <invalid-from\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.InvalidFrom);
    }

    @Test
    public void unmarshalInvalidNamespace() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <invalid-namespace\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.InvalidNamespace);
    }

    @Test
    public void unmarshalInvalidXml() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <invalid-xml\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.InvalidXml);
    }

    @Test
    public void unmarshalNotAuthorized() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <not-authorized\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.NotAuthorized);
    }

    @Test
    public void unmarshalNotWellFormed() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <not-well-formed\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.NotWellFormed);
    }

    @Test
    public void unmarshalPolicyViolation() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <policy-violation\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.PolicyViolation);
    }

    @Test
    public void unmarshalRemoteConnectionFailed() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <remote-connection-failed\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.RemoteConnectionFailed);
    }

    @Test
    public void unmarshalReset() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <reset\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.Reset);
    }

    @Test
    public void unmarshalResourceConstraint() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <resource-constraint\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.ResourceConstraint);
    }

    @Test
    public void unmarshalRestrictedXml() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <restricted-xml\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.RestrictedXml);
    }

    @Test
    public void unmarshalSeeOtherHost() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <see-other-host\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'>[2001:41D0:1:A49b::1]:9222</see-other-host>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.SeeOtherHost);
        Assert.assertEquals(((StreamError.SeeOtherHost) streamError.getCondition()).getOtherHost(), "[2001:41D0:1:A49b::1]:9222");
    }

    @Test
    public void unmarshalSystemShutdown() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <system-shutdown\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.SystemShutdown);
    }

    @Test
    public void unmarshalUndefinedCondition() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <undefined-condition\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.UndefinedCondition);
    }

    @Test
    public void unmarshalUnsupportedEncoding() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <unsupported-encoding\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.UnsupportedEncoding);
    }

    @Test
    public void unmarshalUnsupportedFeature() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <unsupported-feature\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.UnsupportedFeature);
    }

    @Test
    public void unmarshalUnsupportedStanzaType() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <unsupported-stanza-type\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.UnsupportedStanzaType);
    }

    @Test
    public void shouldUnmarshalUnsupportedVersion() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <unsupported-version\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.UnsupportedVersion);
    }

    @Test
    public void shouldUnmarshalErrorWithText() throws JAXBException, XMLStreamException {
        String xml = "<stream:error>\n" +
                "     <host-unknown\n" +
                "         xmlns='urn:ietf:params:xml:ns:xmpp-streams'/>\n" +
                "     <text xmlns='urn:ietf:params:xml:ns:xmpp-streams'\n" +
                "           xml:lang='en'>OPTIONAL descriptive text</text>" +
                "   </stream:error>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(StreamError.class);
        StreamError streamError = (StreamError) jaxbContext.createUnmarshaller().unmarshal(xmlEventReader);
        Assert.assertEquals(streamError.getText(), "OPTIONAL descriptive text");
        Assert.assertEquals(streamError.getLanguage(), "en");
        Assert.assertTrue(streamError.getCondition() instanceof StreamError.HostUnknown);
    }
}
