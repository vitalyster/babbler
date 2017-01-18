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

package rocks.xmpp.addr;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;

/**
 * Represents the JID as described in <a href="https://tools.ietf.org/html/rfc7622">Extensible Messaging and Presence Protocol (XMPP): Address Format</a>.
 * <p>
 * A JID consists of three parts:
 * <p>
 * [ localpart "@" ] domainpart [ "/" resourcepart ]
 * </p>
 * The easiest way to create a JID is to use the {@link #of(CharSequence)} method:
 * <pre><code>
 * Jid jid = Jid.of("juliet@capulet.lit/balcony");
 * </code></pre>
 * You can then get the parts from it via the respective methods:
 * <pre><code>
 * String local = jid.getLocal(); // juliet
 * String domain = jid.getDomain(); // capulet.lit
 * String resource = jid.getResource(); // balcony
 * </code></pre>
 * Implementations of this interface should override <code>equals()</code> and <code>hashCode()</code>, so that different instances with the same value are equal:
 * <pre><code>
 * Jid.of("romeo@capulet.lit/balcony").equals(Jid.of("romeo@capulet.lit/balcony")); // true
 * </code></pre>
 * The default implementation of this class also supports <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>, i.e.
 * <pre><code>
 * Jid.of("d'artagnan@musketeers.lit")
 * </code></pre>
 * is escaped as <code>d\\27artagnan@musketeers.lit</code>.
 * <p>
 * Implementations of this interface should be thread-safe and immutable.
 *
 * @author Christian Schudt
 * @see <a href="https://tools.ietf.org/html/rfc7622">RFC 7622 - Extensible Messaging and Presence Protocol (XMPP): Address Format</a>
 */
@XmlJavaTypeAdapter(JidAdapter.class)
public interface Jid extends Comparable<Jid>, Serializable, CharSequence {

    /**
     * The service discovery feature used for determining support of JID escaping (<code>jid\20escaping</code>).
     */
    String ESCAPING_FEATURE = "jid\\20escaping";

    /**
     * Returns a full JID with a domain and resource part, e.g. <code>capulet.com/balcony</code>
     *
     * @param local    The local part.
     * @param domain   The domain.
     * @param resource The resource part.
     * @return The JID.
     * @throws NullPointerException     If the domain is null.
     * @throws IllegalArgumentException If the domain, local or resource part are not valid.
     */
    static Jid of(CharSequence local, CharSequence domain, CharSequence resource) {
        return new FullJid(local, domain, resource);
    }

    /**
     * Creates a bare JID with only the domain part, e.g. <code>capulet.com</code>
     *
     * @param domain The domain.
     * @return The JID.
     * @throws NullPointerException     If the domain is null.
     * @throws IllegalArgumentException If the domain or local part are not valid.
     */
    static Jid ofDomain(CharSequence domain) {
        return new FullJid(null, domain, null);
    }

    /**
     * Creates a bare JID with a local and domain part, e.g. <code>juliet@capulet.com</code>
     *
     * @param local  The local part.
     * @param domain The domain.
     * @return The JID.
     * @throws NullPointerException     If the domain is null.
     * @throws IllegalArgumentException If the domain or local part are not valid.
     */
    static Jid ofLocalAndDomain(CharSequence local, CharSequence domain) {
        return new FullJid(local, domain, null);
    }

    /**
     * Creates a full JID with a domain and resource part, e.g. <code>capulet.com/balcony</code>
     *
     * @param domain   The domain.
     * @param resource The resource part.
     * @return The JID.
     * @throws NullPointerException     If the domain is null.
     * @throws IllegalArgumentException If the domain or resource are not valid.
     */
    static Jid ofDomainAndResource(CharSequence domain, CharSequence resource) {
        return new FullJid(null, domain, resource);
    }

    /**
     * Creates a JID from an unescaped string. The format must be
     * <blockquote><p>[ localpart "@" ] domainpart [ "/" resourcepart ]</p></blockquote>
     * The input string will be escaped.
     *
     * @param jid The JID.
     * @return The JID.
     * @throws NullPointerException     If the jid is null.
     * @throws IllegalArgumentException If the jid could not be parsed or is not valid.
     * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>
     */
    static Jid of(CharSequence jid) {
        return FullJid.of(jid.toString(), false, true);
    }

    /**
     * Creates a JID from a escaped JID string. The format must be
     * <blockquote><p>[ localpart "@" ] domainpart [ "/" resourcepart ]</p></blockquote>
     * This method should be used, when parsing JIDs from the XMPP stream.
     * <p>
     * Note, that validation and enforcement is skipped when using this method, because it's expected,
     * that JID strings passed to this method are already enforced.
     *
     * @param jid The JID.
     * @return The JID.
     * @throws NullPointerException     If the jid is null.
     * @throws IllegalArgumentException If the jid could not be parsed or is not valid.
     * @see <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>
     */
    static Jid ofEscaped(CharSequence jid) {
        return FullJid.of(jid.toString(), true, false);
    }

    /**
     * Checks if the JID is a full JID.
     * <blockquote>
     * <p>The term "full JID" refers to an XMPP address of the form &lt;localpart@domainpart/resourcepart&gt; (for a particular authorized client or device associated with an account) or of the form &lt;domainpart/resourcepart&gt; (for a particular resource or script associated with a server).</p>
     * </blockquote>
     *
     * @return True, if the JID is a full JID; otherwise false.
     */
    boolean isFullJid();

    /**
     * Checks if the JID is a bare JID.
     * <blockquote>
     * <p>The term "bare JID" refers to an XMPP address of the form &lt;localpart@domainpart&gt; (for an account at a server) or of the form &lt;domainpart&gt; (for a server).</p>
     * </blockquote>
     *
     * @return True, if the JID is a bare JID; otherwise false.
     */
    boolean isBareJid();

    /**
     * Gets the bare JID representation of this JID, i.e. removes the resource part.
     * <blockquote>
     * <p>The term "bare JID" refers to an XMPP address of the form &lt;localpart@domainpart&gt; (for an account at a server) or of the form &lt;domainpart&gt; (for a server).</p>
     * </blockquote>
     *
     * @return The bare JID.
     * @see #withResource(CharSequence)
     */
    Jid asBareJid();

    /**
     * Creates a new JID with a new local part and the same domain and resource part of the current JID.
     *
     * @param local The local part.
     * @return The JID with a new local part.
     * @throws IllegalArgumentException If the local is not a valid local part.
     * @see #withResource(CharSequence)
     */
    Jid withLocal(CharSequence local);

    /**
     * Creates a new full JID with a resource and the same local and domain part of the current JID.
     *
     * @param resource The resource.
     * @return The full JID with a resource.
     * @throws IllegalArgumentException If the resource is not a valid resource part.
     * @see #asBareJid()
     * @see #withLocal(CharSequence)
     */
    Jid withResource(CharSequence resource);

    /**
     * Creates a new JID at a subdomain and at the same domain as this JID.
     *
     * @param subdomain The subdomain.
     * @return The JID at a subdomain.
     * @throws NullPointerException     If subdomain is null.
     * @throws IllegalArgumentException If subdomain is not a valid subdomain name.
     */
    Jid atSubdomain(CharSequence subdomain);

    /**
     * Gets the local part of the JID, also known as the name or node.
     * <blockquote>
     * <p><cite><a href="https://tools.ietf.org/html/rfc7622#section-3.3">3.3.  Localpart</a></cite></p>
     * <p>The localpart of a JID is an optional identifier placed before the
     * domainpart and separated from the latter by the '@' character.
     * Typically, a localpart uniquely identifies the entity requesting and
     * using network access provided by a server (i.e., a local account),
     * although it can also represent other kinds of entities (e.g., a
     * chatroom associated with a multi-user chat service [XEP-0045]).  The
     * entity represented by an XMPP localpart is addressed within the
     * context of a specific domain (i.e., &lt;localpart@domainpart&gt;).</p>
     * </blockquote>
     *
     * @return The local part or null.
     */
    String getLocal();

    /**
     * Gets the domain part.
     * <blockquote>
     * <p><cite><a href="https://tools.ietf.org/html/rfc7622#section-3.2">3.2.  Domainpart</a></cite></p>
     * <p>The domainpart is the primary identifier and is the only REQUIRED
     * element of a JID (a mere domainpart is a valid JID).  Typically,
     * a domainpart identifies the "home" server to which clients connect
     * for XML routing and data management functionality.</p>
     * </blockquote>
     *
     * @return The domain part.
     */
    String getDomain();

    /**
     * Gets the resource part.
     * <blockquote>
     * <p><cite><a href="https://tools.ietf.org/html/rfc7622#section-3.4">3.4.  Resourcepart</a></cite></p>
     * <p>The resourcepart of a JID is an optional identifier placed after the
     * domainpart and separated from the latter by the '/' character.  A
     * resourcepart can modify either a &lt;localpart@domainpart&gt; address or a
     * mere &lt;domainpart&gt; address.  Typically, a resourcepart uniquely
     * identifies a specific connection (e.g., a device or location) or
     * object (e.g., an occupant in a multi-user chatroom [XEP-0045])
     * belonging to the entity associated with an XMPP localpart at a domain
     * (i.e., &lt;localpart@domainpart/resourcepart&gt;).</p>
     * </blockquote>
     *
     * @return The resource part or null.
     */
    String getResource();

    /**
     * Returns the JID in escaped form as described in <a href="http://xmpp.org/extensions/xep-0106.html">XEP-0106: JID Escaping</a>.
     *
     * @return The escaped JID.
     * @see #toString()
     */
    String toEscapedString();
}
