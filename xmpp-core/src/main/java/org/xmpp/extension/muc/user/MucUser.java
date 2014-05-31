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

package org.xmpp.extension.muc.user;

import org.xmpp.Jid;
import org.xmpp.extension.muc.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The implementation of the {@code <x/>} element in the {@code http://jabber.org/protocol/muc#user} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0045.html">XEP-0045: Multi-User Chat</a>
 * @see <a href="http://xmpp.org/extensions/xep-0045.html#schemas-user">XML Schema</a>
 */
@XmlRootElement(name = "x")
public final class MucUser {

    @XmlElement(name = "item")
    private MucUserItem item;

    @XmlElement(name = "status")
    private List<Status> statusCodes = new ArrayList<>();

    @XmlElement(name = "invite")
    private List<Invite> invites = new ArrayList<>();

    @XmlElement(name = "decline")
    private Decline decline;

    @XmlElement(name = "destroy")
    private MucUserDestroy destroy;

    @XmlElement(name = "password")
    private String password;

    private MucUser() {
    }

    private MucUser(String password, Invite... invite) {
        this.invites.addAll(Arrays.asList(invite));
        this.password = password;
    }

    private MucUser(Decline decline) {
        this.decline = decline;
    }

    private MucUser(MucUserItem item, Status... status) {
        this.item = item;
        this.statusCodes.addAll(Arrays.asList(status));
    }

    private MucUser(MucUserItem item, MucUserDestroy destroy) {
        this.item = item;
        this.destroy = destroy;
    }

    /**
     * Creates a {@code <x/>} element with an {@code <item/>} and a {@code <status/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <item affiliation='owner' role='moderator'/>
     *     <status code='110'/>
     * </x>
     * }
     * </pre>
     *
     * @param affiliation The affiliation.
     * @param role        The role.
     * @param status      The status.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withItem(Affiliation affiliation, Role role, Status... status) {
        return new MucUser(new MucUserItem(affiliation, role, null, null, null, null), status);
    }

    /**
     * Creates a {@code <x/>} element with an {@code <item/>} and a {@code <status/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <item affiliation='none'
     *           jid='hag66@shakespeare.lit/pda'
     *           role='participant'/>
     * </x>
     * }
     * </pre>
     *
     * @param affiliation The affiliation.
     * @param role        The role.
     * @param jid         The JID.
     * @param status      The status.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withItem(Affiliation affiliation, Role role, Jid jid, Status... status) {
        return new MucUser(new MucUserItem(affiliation, role, jid, null, null, null), status);
    }

    /**
     * Creates a {@code <x/>} element with an {@code <item/>} and a {@code <status/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <item affiliation='member'
     *           jid='hag66@shakespeare.lit/pda'
     *           nick='oldhag'
     *           role='participant'/>
     *     <status code='303'/>
     * </x>
     * }
     * </pre>
     *
     * @param affiliation The affiliation.
     * @param role        The role.
     * @param jid         The JID.
     * @param nick        The nick.
     * @param status      The status.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withItem(Affiliation affiliation, Role role, Jid jid, String nick, Status... status) {
        return new MucUser(new MucUserItem(affiliation, role, jid, nick, null, null), status);
    }

    /**
     * Creates a {@code <x/>} element with an {@code <item/>} and a {@code <status/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <item affiliation='member'
     *           jid='hag66@shakespeare.lit/pda'
     *           nick='oldhag'
     *           role='participant'/>
     *     <status code='303'/>
     * </x>
     * }
     * </pre>
     *
     * @param role   The role.
     * @param nick   The nick.
     * @param reason The reason.
     * @param status The status.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withItem(Role role, String nick, String reason, Status... status) {
        return new MucUser(new MucUserItem(null, role, null, nick, null, reason), status);
    }

    /**
     * Creates a {@code <x/>} element with an {@code <item/>} and a {@code <status/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <item affiliation='none' role='none'>
     *         <actor nick='Fluellen'/>
     *         <reason>Avaunt, you cullion!</reason>
     *     </item>
     *     <status code='307'/>
     * </x>
     * }
     * </pre>
     *
     * @param affiliation The affiliation.
     * @param role        The role.
     * @param actor       The actor.
     * @param reason      The reason.
     * @param status      The status.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withItem(Affiliation affiliation, Role role, Actor actor, String reason, Status... status) {
        return new MucUser(new MucUserItem(affiliation, role, null, null, actor, reason), status);
    }

    /**
     * Creates a {@code <x/>} element with an {@code <item/>} and a {@code <status/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <item affiliation='member'
     *           nick='thirdwitch'
     *           role='participant'>
     *         <reason>A worthy witch indeed!</reason>
     *     </item>
     * </x>
     * }
     * </pre>
     *
     * @param affiliation The affiliation.
     * @param role        The role.
     * @param nick        The nick.
     * @param reason      The reason.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withItem(Affiliation affiliation, Role role, String nick, String reason) {
        return new MucUser(new MucUserItem(affiliation, role, null, nick, null, reason));
    }

    /**
     * Creates a {@code <x/>} element with an {@code <item/>} and a {@code <status/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <item affiliation='none' role='none' jid='hag66@shakespeare.lit/pda' nick='oldhag'>
     *         <actor nick='Fluellen'/>
     *         <reason>Avaunt, you cullion!</reason>
     *     </item>
     *     <status code='307'/>
     * </x>
     * }
     * </pre>
     *
     * @param affiliation The affiliation.
     * @param role        The role.
     * @param jid         The JID.
     * @param nick        The nick.
     * @param actor       The actor.
     * @param reason      The reason.
     * @param status      The status.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withItem(Affiliation affiliation, Role role, Jid jid, String nick, Actor actor, String reason, Status... status) {
        return new MucUser(new MucUserItem(affiliation, role, jid, nick, actor, reason), status);
    }

    /**
     * Creates a {@code <x/>} element with an {@code <item/>} and a {@code <destroy/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <item affiliation='none' role='none'/>
     *     <destroy jid='coven@chat.shakespeare.lit'>
     *         <reason>Macbeth doth come.</reason>
     *     </destroy>
     * </x>
     * }
     * </pre>
     *
     * @param affiliation The affiliation.
     * @param role        The role.
     * @param jid         The JID.
     * @param reason      The reason.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withDestroy(Affiliation affiliation, Role role, Jid jid, String reason) {
        return new MucUser(new MucUserItem(affiliation, role, null, null, null, null), new MucUserDestroy(jid, reason));
    }

    /**
     * Creates a {@code <x/>} element with an {@code <invite/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <invite to='hecate@shakespeare.lit'/>
     * </x>
     * }
     * </pre>
     *
     * @param invite The invites.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withInvites(Invite... invite) {
        return new MucUser(null, invite);
    }

    /**
     * Creates a {@code <x/>} element with an {@code <invite/>} and a {@code <password/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <invite to='hecate@shakespeare.lit'/>
     *     <password>cauldronburn</password>
     * </x>
     * }
     * </pre>
     *
     * @param password The password.
     * @param invite   The invites.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withInvite(String password, Invite... invite) {
        return new MucUser(password, invite);
    }

    /**
     * Creates a {@code <x/>} element with a {@code <decline/>} child element.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <decline to='crone1@shakespeare.lit'>
     *         <reason>
     *             Sorry, I'm too busy right now.
     *         </reason>
     *     </decline>
     * </x>
     * }
     * </pre>
     *
     * @param to     The to attribute.
     * @param reason The reason.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withDecline(Jid to, String reason) {
        return new MucUser(new Decline(to, reason));
    }

    /**
     * Creates a {@code <x/>} element with one or more {@code <status/>} child elements.
     * <p><b>Sample:</b></p>
     * <pre>
     * {@code
     * <x xmlns='http://jabber.org/protocol/muc#user'>
     *     <status code='170'/>
     * </x>
     * }
     * </pre>
     *
     * @param status The status.
     * @return The {@link MucUser} instance.
     */
    public static MucUser withStatus(Status... status) {
        return new MucUser(null, status);
    }

    /**
     * Gets the status codes.
     *
     * @return The status codes.
     */
    public List<Status> getStatusCodes() {
        return statusCodes;
    }

    /**
     * Gets the item.
     *
     * @return The item.
     */
    public Item getItem() {
        return item;
    }

    /**
     * Gets the invites
     *
     * @return The invites.
     */
    public List<Invite> getInvites() {
        return invites;
    }

    /**
     * Gets the decline.
     *
     * @return The decline.
     */
    public Decline getDecline() {
        return decline;
    }

    /**
     * Gets the destroy element.
     *
     * @return The destroy element.
     */
    public Destroy getDestroy() {
        return destroy;
    }

    /**
     * Gets the password for the room.
     *
     * @return The password for the room.
     */
    public String getPassword() {
        return password;
    }

    private static class MucUserItem implements Item {

        @XmlElement(name = "actor")
        private MucUserActor actor;

        @XmlElement(name = "reason")
        private String reason;

        @XmlAttribute(name = "affiliation")
        private Affiliation affiliation;

        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlAttribute(name = "nick")
        private String nick;

        @XmlAttribute(name = "role")
        private Role role;

        private MucUserItem() {
        }

        private MucUserItem(Affiliation affiliation, Role role, Jid jid, String nick, Actor actor, String reason) {
            this.affiliation = affiliation;
            this.role = role;
            this.jid = jid;
            this.nick = nick;
            this.actor = actor != null ? new MucUserActor(actor) : null;
            this.reason = reason;
        }

        @Override
        public String getNick() {
            return nick;
        }

        @Override
        public Role getRole() {
            return role;
        }

        @Override
        public Jid getJid() {
            return jid;
        }

        @Override
        public Affiliation getAffiliation() {
            return affiliation;
        }

        @Override
        public String getReason() {
            return reason;
        }

        @Override
        public Actor getActor() {
            return actor;
        }
    }

    private static final class MucUserDestroy implements Destroy {

        @XmlElement(name = "reason")
        private String reason;

        @XmlAttribute(name = "jid")
        private Jid jid;

        private MucUserDestroy() {
        }

        private MucUserDestroy(Jid jid, String reason) {
            this.jid = jid;
            this.reason = reason;
        }

        @Override
        public Jid getJid() {
            return jid;
        }

        @Override
        public String getReason() {
            return reason;
        }
    }

    private static final class MucUserActor implements Actor {
        @XmlAttribute(name = "jid")
        private Jid jid;

        @XmlAttribute(name = "nick")
        private String nick;

        private MucUserActor() {
        }

        private MucUserActor(Actor actor) {
            this.jid = actor.getJid();
            this.nick = actor.getNick();
        }

        @Override
        public String getNick() {
            return nick;
        }

        @Override
        public Jid getJid() {
            return jid;
        }
    }
}
