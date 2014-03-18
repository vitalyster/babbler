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

package org.xmpp.extension.muc.conference;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.UnmarshalHelper;
import org.xmpp.stanza.Message;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author Christian Schudt
 */
public class DirectInvitationTest extends BaseTest {

    @Test
    public void unmarshalDirectInvitation() throws JAXBException, XMLStreamException, IOException {
        String xml = "<message\n" +
                "    from='crone1@shakespeare.lit/desktop'\n" +
                "    to='hecate@shakespeare.lit'>\n" +
                "  <x xmlns='jabber:x:conference'\n" +
                "     continue='true'\n" +
                "     jid='darkcave@macbeth.shakespeare.lit'\n" +
                "     password='cauldronburn'\n" +
                "     reason='Hey Hecate, this is the place for all good witches!'\n" +
                "     thread='e0ffe42b28561960c6b12b944a092794b9683a38'/>\n" +
                "</message>";
        XMLEventReader xmlEventReader = UnmarshalHelper.getStream(xml);
        Message message = (Message) unmarshaller.unmarshal(xmlEventReader);
        DirectInvitation directInvitation = message.getExtension(DirectInvitation.class);
        Assert.assertNotNull(directInvitation);
        Assert.assertTrue(directInvitation.isContinue());
        Assert.assertEquals(directInvitation.getRoomAddress(), Jid.valueOf("darkcave@macbeth.shakespeare.lit"));
        Assert.assertEquals(directInvitation.getPassword(), "cauldronburn");
        Assert.assertEquals(directInvitation.getReason(), "Hey Hecate, this is the place for all good witches!");
        Assert.assertEquals(directInvitation.getThread(), "e0ffe42b28561960c6b12b944a092794b9683a38");
    }
}
