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

package org.xmpp.im;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.BaseTest;
import org.xmpp.Jid;
import org.xmpp.TestConnection;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

/**
 * @author Christian Schudt
 */
public class RosterManagerTest extends BaseTest {

    @Test
    public void testRosterListener() throws XMLStreamException, JAXBException {
        final int[] rosterPushCount = new int[1];

        RosterManager rosterManager = new RosterManager(new TestConnection());
        rosterManager.addRosterListener(new RosterListener() {
            @Override
            public void rosterChanged(RosterEvent e) {
                if (rosterPushCount[0] == 0) {
                    Assert.assertEquals(e.getAddedContacts().size(), 3);
                    Assert.assertEquals(e.getUpdatedContacts().size(), 0);
                    Assert.assertEquals(e.getRemovedContacts().size(), 0);
                } else if (rosterPushCount[0] == 1) {
                    Assert.assertEquals(e.getAddedContacts().size(), 1);
                    Assert.assertEquals(e.getUpdatedContacts().size(), 0);
                    Assert.assertEquals(e.getRemovedContacts().size(), 0);
                } else if (rosterPushCount[0] == 2) {
                    Assert.assertEquals(e.getAddedContacts().size(), 0);
                    Assert.assertEquals(e.getUpdatedContacts().size(), 0);
                    Assert.assertEquals(e.getRemovedContacts().size(), 1);
                    Assert.assertEquals(e.getRemovedContacts().get(0).getJid(), Jid.fromString("contact2@domain"));
                } else if (rosterPushCount[0] == 3) {
                    Assert.assertEquals(e.getAddedContacts().size(), 1);
                    Assert.assertEquals(e.getUpdatedContacts().size(), 1);
                    Assert.assertEquals(e.getRemovedContacts().size(), 0);
                    Assert.assertEquals(e.getAddedContacts().get(0).getJid(), Jid.fromString("contact5@domain"));
                    Assert.assertEquals(e.getUpdatedContacts().get(0).getJid(), Jid.fromString("contact1@domain"));
                    Assert.assertEquals(e.getUpdatedContacts().get(0).getName(), "Name");
                }
            }
        });

        Roster roster1 = new Roster();
        roster1.getContacts().add(new Roster.Contact(Jid.fromString("contact1@domain")));
        roster1.getContacts().add(new Roster.Contact(Jid.fromString("contact2@domain")));
        roster1.getContacts().add(new Roster.Contact(Jid.fromString("contact3@domain")));
        rosterManager.updateRoster(roster1);
        rosterPushCount[0]++;

        Roster roster2 = new Roster();
        roster2.getContacts().add(new Roster.Contact(Jid.fromString("contact1@domain")));
        roster2.getContacts().add(new Roster.Contact(Jid.fromString("contact2@domain")));
        roster2.getContacts().add(new Roster.Contact(Jid.fromString("contact3@domain")));
        roster2.getContacts().add(new Roster.Contact(Jid.fromString("contact4@domain")));
        rosterManager.updateRoster(roster2);

        rosterPushCount[0]++;
        Roster roster3 = new Roster();
        roster3.getContacts().add(new Roster.Contact(Jid.fromString("contact1@domain")));
        Roster.Contact contact = new Roster.Contact(Jid.fromString("contact2@domain"));
        contact.setSubscription(Roster.Contact.Subscription.REMOVE);
        roster3.getContacts().add(contact);
        roster3.getContacts().add(new Roster.Contact(Jid.fromString("contact3@domain")));
        roster3.getContacts().add(new Roster.Contact(Jid.fromString("contact4@domain")));
        rosterManager.updateRoster(roster3);

        rosterPushCount[0]++;
        Roster roster4 = new Roster();
        Roster.Contact contact2 = new Roster.Contact(Jid.fromString("contact1@domain"));
        contact2.setName("Name");
        roster4.getContacts().add(contact2);
        roster4.getContacts().add(new Roster.Contact(Jid.fromString("contact3@domain")));
        roster4.getContacts().add(new Roster.Contact(Jid.fromString("contact4@domain")));
        roster4.getContacts().add(new Roster.Contact(Jid.fromString("contact5@domain")));
        rosterManager.updateRoster(roster4);
    }
}
