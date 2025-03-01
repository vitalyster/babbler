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

package rocks.xmpp.core.bind.model;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.testng.Assert;
import org.testng.annotations.Test;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmlTest;
import rocks.xmpp.core.stanza.model.IQ;

/**
 * Tests for the {@link Bind} class.
 *
 * @author Christian Schudt
 */
public class BindTest extends XmlTest {

    @Test
    public void testUnmarshal() throws XMLStreamException, JAXBException {
        String xml = "<iq id='wy2xa82b4' type='set'>\n" +
                "     <bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'>\n" +
                "       <resource>balcony</resource>\n" +
                "     </bind>\n" +
                "   </iq>";
        IQ iq = unmarshal(xml, IQ.class);
        Bind bind = iq.getExtension(Bind.class);
        Assert.assertEquals(bind.getResource(), "balcony");
    }

    @Test
    public void testMarshalResource() throws XMLStreamException, JAXBException {
        Bind bind = new Bind("balcony");
        String xml = marshal(bind);
        Assert.assertEquals(xml,
                "<bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"><resource>balcony</resource></bind>");
    }

    @Test
    public void testMarshalJid() throws XMLStreamException, JAXBException {
        Bind bind = new Bind(Jid.ofDomain("domain"));
        String xml = marshal(bind);
        Assert.assertEquals(xml, "<bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\"><jid>domain</jid></bind>");
    }
}
