/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Christian Schudt
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

package rocks.xmpp.core.stanza.model;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.nullsLast;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.Text;
import rocks.xmpp.core.stanza.model.errors.Condition;

/**
 * The implementation of the {@code <presence/>} element.
 * <blockquote>
 * <p><cite><a href="https://xmpp.org/rfcs/rfc6121.html#presence-fundamentals">4.1.  Presence
 * Fundamentals</a></cite></p>
 * <p>The concept of presence refers to an entity's availability for communication over a network. At the most basic
 * level, presence is a boolean "on/off" variable that signals whether an entity is available or unavailable for
 * communication (the terms "online" and "offline" are also used). In XMPP, an entity's availability is signaled when
 * its client generates a {@code <presence/>} stanza with no 'type' attribute, and an entity's lack of availability is
 * signaled when its client generates a {@code <presence/>} stanza whose 'type' attribute has a value of
 * "unavailable".</p>
 * <p>XMPP presence typically follows a "publish-subscribe" or "observer" pattern, wherein an entity sends presence to
 * its server, and its server then broadcasts that information to all of the entity's contacts who have a subscription
 * to the entity's presence (in the terminology of [IMP-MODEL], an entity that generates presence is a "presentity" and
 * the entities that receive presence are "subscribers"). A client generates presence for broadcast to all subscribed
 * entities by sending a presence stanza to its server with no 'to' address, where the presence stanza has either no
 * 'type' attribute or a 'type' attribute whose value is "unavailable". This kind of presence is called "broadcast
 * presence".</p>
 * <p>After a client completes the preconditions specified in [XMPP-CORE], it can establish a "presence session" at its
 * server by sending initial presence, where the presence session is terminated by sending unavailable presence. For the
 * duration of its presence session, a connected resource (in the terminology of [XMPP-CORE]) is said to be an
 * "available resource".</p>
 * <p>In XMPP, applications that combine messaging and presence functionality, the default type of communication for
 * which presence signals availability is messaging; however, it is not necessary for XMPP applications to combine
 * messaging and presence functionality, and they can provide standalone presence features without messaging (in
 * addition, XMPP servers do not require information about network availability in order to successfully route message
 * and IQ stanzas).</p>
 * </blockquote>
 * This class is thread-safe.
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/rfcs/rfc6121.html#presence-syntax">4.7.  Presence Syntax</a>
 */
@XmlTransient
public class Presence extends ExtensibleStanza implements Comparable<Presence> {

    private static final Set<Type> SUBSCRIPTION_TYPES =
            EnumSet.of(Type.SUBSCRIBE, Type.SUBSCRIBED, Type.UNSUBSCRIBE, Type.UNSUBSCRIBED);

    private static final Comparator<Presence> DEFAULT_COMPARATOR = nullsLast(
            comparing(Presence::getPriority, (p1, p2) -> Byte.compare(p2 == null ? 0 : p2, p1 == null ? 0 : p1))
                    .thenComparing(Presence::getType, nullsFirst(naturalOrder()))
                    .thenComparing(Presence::getShow, nullsFirst(naturalOrder()))
                    .thenComparing(Presence::getStatuses, nullsFirst((o1, o2) -> {
                        int diff = o1.size() - o2.size();
                        if (diff == 0) {
                            if (Objects.equals(o1, o2)) {
                                return 0;
                            } else {
                                for (Text text1 : o1) {
                                    for (Text text2 : o2) {
                                        int result = nullsFirst(Text::compareTo).compare(text1, text2);
                                        if (result != 0) {
                                            return result;
                                        }
                                    }
                                }
                            }
                        }
                        return diff;
                    }))
                    .thenComparing(Presence::getFrom, nullsFirst(naturalOrder()))
                    .thenComparing(Presence::getTo, nullsFirst(naturalOrder()))
                    .thenComparing(Presence::getId, nullsFirst(naturalOrder()))
                    .thenComparing(Presence::getError, nullsFirst((o1, o2) -> {
                        if (Objects.equals(o1, o2)) {
                            return 0;
                        }
                        return -1;
                    }))
                    .thenComparing(Presence::getLanguage, nullsFirst(comparing(Locale::toLanguageTag)))
                    .thenComparing(Presence::getExtensions, nullsFirst((o1, o2) -> {
                        int diff = o1.size() - o2.size();
                        if (diff == 0) {
                            if (Objects.equals(o1, o2)) {
                                return 0;
                            } else {
                                for (Object ext1 : o1) {
                                    for (Object ext2 : o2) {
                                        if (!Objects.equals(ext1, ext2)) {
                                            return -1;
                                        }
                                    }
                                }
                            }
                        }
                        return diff;
                    })));

    private final List<Text> status = new CopyOnWriteArrayList<>();

    private Byte priority;

    private Show show;

    @XmlAttribute
    private Type type;

    /**
     * Constructs an empty presence to indicate availability.
     */
    public Presence() {
        super(null, null, null, null, null, null);
        this.priority = null;
        this.show = null;
        this.type = null;
    }

    /**
     * Constructs a presence with a priority.
     *
     * @param priority The priority.
     */
    public Presence(byte priority) {
        this(null, null, null, null, priority, null, null, null, null, null);
    }

    /**
     * Constructs a presence with a specific 'show' value.
     *
     * @param show The 'show' value.
     */
    public Presence(Show show) {
        this(show, (byte) 0);
    }

    /**
     * Constructs a presence with a specific 'show' value and priority.
     *
     * @param show     The 'show' value.
     * @param priority The priority.
     */
    public Presence(Show show, byte priority) {
        this(null, null, show, null, priority, null, null, null, null, null);
    }

    /**
     * Constructs a presence of a specific type.
     *
     * @param type The type.
     */
    public Presence(Type type) {
        this(type, (byte) 0);
    }

    /**
     * Constructs a presence of a specific type.
     *
     * @param type     The type.
     * @param priority The priority.
     */
    public Presence(Type type, byte priority) {
        this(null, type, null, null, priority, null, null, null, null, null);
    }

    /**
     * Constructs a directed presence.
     *
     * @param to The recipient.
     */
    public Presence(Jid to) {
        this(to, null, null, null);
    }

    /**
     * Constructs a directed presence with a specific 'show' attribute and status.
     *
     * @param to     The recipient.
     * @param show   The 'show' value.
     * @param status The status.
     */
    public Presence(Jid to, Show show, String status) {
        this(to, null, show, status != null ? Collections.singleton(new Text(status)) : null, (byte) 0, null, null,
                null, null, null);
    }

    /**
     * Constructs a directed presence, which is useful for requesting subscription or for exiting a multi-user chat.
     *
     * @param to     The recipient.
     * @param type   The type.
     * @param status The status.
     */
    public Presence(Jid to, Type type, String status) {
        this(to, type, status, null);
    }

    /**
     * Constructs a directed presence, which is useful for requesting subscription or for exiting a multi-user chat.
     *
     * @param to     The recipient.
     * @param type   The type.
     * @param status The status.
     * @param id     The id.
     */
    public Presence(Jid to, Type type, String status, String id) {
        this(to, type, null, status != null ? Collections.singleton(new Text(status)) : null, (byte) 0, id, null, null,
                null, null);
    }

    /**
     * Constructs a presence with all possible values.
     *
     * @param to         The recipient.
     * @param type       The type.
     * @param show       The 'show' value.
     * @param status     The status.
     * @param priority   The priority.
     * @param id         The id.
     * @param from       The 'from' attribute.
     * @param language   The language.
     * @param extensions The extensions.
     * @param error      The stanza error.
     */
    public Presence(Jid to, Type type, Show show, Collection<Text> status, byte priority, String id, Jid from,
                    Locale language, Collection<?> extensions, StanzaError error) {
        super(to, from, id, language, extensions, error);
        this.show = show;
        this.type = type;
        if (status != null) {
            this.status.addAll(status);
        }
        this.priority = priority == 0 ? null : priority;
    }

    /**
     * Indicates, whether an entity is available.
     * <blockquote>
     * <p><cite><a href="https://xmpp.org/rfcs/rfc6121.html#presence-fundamentals">4.1.  Presence
     * Fundamentals</a></cite></p>
     * <p>In XMPP, an entity's availability is signaled when its client generates a {@code <presence/>} stanza with no
     * 'type' attribute, and an entity's lack of availability is signaled when its client generates a {@code
     * <presence/>} stanza whose 'type' attribute has a value of "unavailable".</p>
     * </blockquote>
     *
     * @return True, if the presence is available.
     */
    public final synchronized boolean isAvailable() {
        return type == null;
    }

    /**
     * Gets the {@code <show/>} element.
     *
     * @return The {@code <show/>} element.
     * @see Show
     */
    public final synchronized Show getShow() {
        return show;
    }

    /**
     * Sets the {@code <show/>} element.
     *
     * @param show The {@code <show/>} element.
     * @see #getShow()
     */
    public final synchronized void setShow(Show show) {
        this.show = show;
    }

    /**
     * Gets the priority.
     * <blockquote>
     * <p><cite><a href="https://xmpp.org/rfcs/rfc6121.html#presence-syntax-children-priority">4.7.2.3.  Priority
     * Element</a></cite></p>
     * <p>The OPTIONAL {@code <priority/>} element contains non-human-readable XML character data that specifies the
     * priority level of the resource. The value MUST be an integer between -128 and +127.</p>
     * </blockquote>
     *
     * @return The priority.
     */
    public final synchronized byte getPriority() {
        return priority != null ? priority : 0;
    }

    /**
     * Sets the priority.
     *
     * @param priority The priority.
     * @see #getPriority()
     */
    public final synchronized void setPriority(byte priority) {
        this.priority = priority;
    }

    /**
     * Gets the type of the presence.
     *
     * @return The type.
     * @see Type
     */
    public final synchronized Type getType() {
        return type;
    }

    /**
     * Sets the type of the presence.
     *
     * @param type The type.
     * @see #getType()
     */
    public final synchronized void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets the statuses.
     * <blockquote>
     * <p><cite><a href="https://xmpp.org/rfcs/rfc6121.html#presence-syntax-children-status">4.7.2.2.  Status
     * Element</a></cite></p>
     * <p>The OPTIONAL {@code <status/>} element contains human-readable XML character data specifying a
     * natural-language description of an entity's availability. It is normally used in conjunction with the show
     * element to provide a detailed description of an availability state (e.g., "In a meeting") when the presence
     * stanza has no 'type' attribute.</p>
     * <p>Multiple instances of the {@code <status/>} element MAY be included, but only if each instance possesses an
     * 'xml:lang' attribute with a distinct language value.</p>
     * </blockquote>
     *
     * @return The statuses.
     * @see #getStatus()
     */
    public final List<Text> getStatuses() {
        return status;
    }

    /**
     * Gets the default body (which has no language attribute).
     * <blockquote>
     * <p><cite><a href="https://xmpp.org/rfcs/rfc6121.html#presence-syntax-children-status">4.7.2.2.  Status
     * Element</a></cite></p>
     * <p>The OPTIONAL {@code <status/>} element contains human-readable XML character data specifying a
     * natural-language description of an entity's availability. It is normally used in conjunction with the show
     * element to provide a detailed description of an availability state (e.g., "In a meeting") when the presence
     * stanza has no 'type' attribute.</p>
     * </blockquote>
     * <p>If there's no default status, but only statuses with a language attribute, the first status is returned.</p>
     * <p>If there are no statuses at all, null is returned.</p>
     *
     * @return The status or null.
     * @see #getStatuses()
     */
    public final String getStatus() {
        for (Text status : this.status) {
            if (status != null && status.getLanguage() == null) {
                return status.getText();
            }
        }
        synchronized (this) {
            if (!status.isEmpty()) {
                return status.get(0) != null ? status.get(0).getText() : null;
            }
        }
        return null;
    }

    /**
     * Sets the default status element.
     *
     * @param text The status text.
     * @see #getStatus() ()
     */
    public final void setStatus(String text) {
        if (text != null) {
            synchronized (this) {
                for (Text s : status) {
                    if (s.getLanguage() == null) {
                        status.remove(s);
                        break;
                    }
                }
                this.status.add(0, new Text(text));
            }
        } else {
            this.status.clear();
        }
    }

    /**
     * Indicates whether this presence is a subscription presence. This is the case if it has as type of {@link
     * Type#SUBSCRIBE}, {@link Type#SUBSCRIBED}, {@link Type#UNSUBSCRIBE} or {@link Type#UNSUBSCRIBED}.
     *
     * @return True, if this presence is a subscription presence, otherwise false.
     */
    public final boolean isSubscription() {
        return SUBSCRIPTION_TYPES.contains(getType());
    }

    @Override
    public final Presence createError(StanzaError error) {
        return new Presence(getFrom(), Presence.Type.ERROR, getShow(), getStatuses(), getPriority(), getId(), getTo(),
                getLanguage(), getExtensions(), error);
    }

    @Override
    public final Presence createError(Condition condition) {
        return createError(new StanzaError(condition));
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Presence)) {
            return false;
        }
        Presence other = (Presence) o;
        return Objects.equals(getType(), other.getType())
                && getPriority() == other.getPriority()
                && Objects.equals(getShow(), other.getShow())
                && Objects.equals(getTo(), other.getTo())
                && Objects.equals(getFrom(), other.getFrom())
                && Objects.equals(getStatuses(), other.getStatuses())
                && Objects.equals(getError(), other.getError())
                && Objects.equals(getId(), other.getId())
                && Objects.equals(getLanguage(), other.getLanguage())
                && Objects.equals(getExtensions(), other.getExtensions());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getType(), getPriority(), getShow(), getTo(), getFrom(), getStatuses(), getError(), getId(),
                getLanguage(), getExtensions());
    }

    @Override
    public final synchronized int compareTo(Presence o) {
        return DEFAULT_COMPARATOR.compare(this, o);
    }

    @Override
    public final synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        if (type != null) {
            String sType = type.name();
            sb.append(sType, 0, 1).append(sType.substring(1).toLowerCase()).append(' ');
        }
        sb.append("Presence");
        if (show != null) {
            sb.append(' ').append(show.name());
        }
        sb.append(super.toString());

        String status = getStatus();
        if (status != null) {
            sb.append(": ").append(status);
        }
        return sb.toString();
    }

    /**
     * Represents a {@code <presence/>} {@code <show/>} element.
     * <blockquote>
     * <p><cite><a href="https://xmpp.org/rfcs/rfc6121.html#presence-syntax-children-show">4.7.2.1.  Show
     * Element</a></cite></p>
     * <p>The OPTIONAL {@code <show/>} element specifies the particular availability sub-state of an entity or a
     * specific resource thereof.</p>
     * <p>If no {@code <show/>} element is provided, the entity is assumed to be online and available.</p>
     * </blockquote>
     */
    public enum Show {
        /**
         * The entity or resource is actively interested in chatting.
         */
        @XmlEnumValue(value = "chat")
        CHAT,
        /**
         * The entity or resource is temporarily away.
         */
        @XmlEnumValue(value = "away")
        AWAY,
        /**
         * The entity or resource is away for an extended period (xa = "eXtended Away").
         */
        @XmlEnumValue(value = "xa")
        XA,
        /**
         * The entity or resource is busy (dnd = "Do Not Disturb").
         */
        @XmlEnumValue(value = "dnd")
        DND
    }

    /**
     * Represents a {@code <presence/>} 'type' attribute.
     * <blockquote>
     * <p><cite><a href="https://xmpp.org/rfcs/rfc6121.html#presence-syntax-type">4.7.1.  Type Attribute</a></cite></p>
     * <p>The absence of a 'type' attribute signals that the relevant entity is available for communication.</p>
     * <p>A 'type' attribute with a value of "unavailable" signals that the relevant entity is not available for
     * communication.</p>
     * <p>The XMPP presence stanza is also used to negotiate and manage subscriptions to the presence of other
     * entities. These tasks are completed via presence stanzas of type "subscribe", "unsubscribe", "subscribed", and
     * "unsubscribed".</p>
     * <p>If a user and contact are associated with different XMPP servers, those servers also use a special presence
     * stanza of type "probe" in order to determine the availability of the entity on the peer server.</p>
     * </blockquote>
     */
    @XmlType(name = "presenceType")
    public enum Type {
        /**
         * An error has occurred regarding processing of a previously sent presence stanza; if the presence stanza is of
         * type "error", it MUST include an {@code <error/>} child element.
         */
        @XmlEnumValue(value = "error")
        ERROR,
        /**
         * A request for an entity's current presence; SHOULD be generated only by a server on behalf of a user.
         */
        @XmlEnumValue(value = "probe")
        PROBE,
        /**
         * The sender wishes to subscribe to the recipient's presence.
         */
        @XmlEnumValue(value = "subscribe")
        SUBSCRIBE,
        /**
         * The sender has allowed the recipient to receive their presence.
         */
        @XmlEnumValue(value = "subscribed")
        SUBSCRIBED,
        /**
         * The sender is no longer available for communication.
         */
        @XmlEnumValue(value = "unavailable")
        UNAVAILABLE,
        /**
         * The sender is unsubscribing from the receiver's presence.
         */
        @XmlEnumValue(value = "unsubscribe")
        UNSUBSCRIBE,
        /**
         * The subscription request has been denied or a previously granted subscription has been canceled.
         */
        @XmlEnumValue(value = "unsubscribed")
        UNSUBSCRIBED
    }
}
