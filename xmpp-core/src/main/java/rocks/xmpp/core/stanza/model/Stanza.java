/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Christian Schudt
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

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.stanza.model.errors.Condition;
import rocks.xmpp.core.stream.model.StreamElement;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The abstract base class for a XML stanza.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas">8.  XML Stanzas</a></cite>
 * <p>After a client and a server (or two servers) have completed stream negotiation, either party can send XML stanzas. Three kinds of XML stanza are defined for the 'jabber:client' and 'jabber:server' namespaces: {@code <message/>}, {@code <presence/>}, and {@code <iq/>}.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlTransient
public abstract class Stanza implements StreamElement {

    @XmlAttribute
    private final Jid from;

    @XmlAttribute
    private final String id;

    @XmlAttribute
    private final Jid to;

    @XmlAttribute(namespace = XMLConstants.XML_NS_URI)
    private final String lang;

    @XmlAnyElement(lax = true)
    private final List<Object> extensions = new CopyOnWriteArrayList<>();

    private final StanzaError error;

    protected Stanza() {
        this.to = null;
        this.from = null;
        this.id = null;
        this.lang = null;
        this.error = null;
    }

    protected Stanza(Jid to, Jid from, String id, String language, Collection<?> extensions, StanzaError error) {
        this.to = to;
        this.from = from;
        this.id = id;
        this.lang = language;
        if (extensions != null) {
            this.extensions.addAll(extensions);
        }
        this.error = error;
    }

    /**
     * Gets the stanza's 'to' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-to">8.1.1.  to</a></cite></p>
     * <p>The 'to' attribute specifies the JID of the intended recipient for the stanza.</p>
     * </blockquote>
     *
     * @return The JID.
     */
    public final Jid getTo() {
        return to;
    }

    /**
     * Gets the stanza's 'id' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-id">8.1.3.  id</a></cite></p>
     * <p>The 'id' attribute is used by the originating entity to track any response or error stanza that it might receive in relation to the generated stanza from another entity (such as an intermediate server or the intended recipient).</p>
     * <p>It is up to the originating entity whether the value of the 'id' attribute is unique only within its current stream or unique globally.</p>
     * <p>For {@code <message/>} and {@code <presence/>} stanzas, it is RECOMMENDED for the originating entity to include an 'id' attribute; for {@code <iq/>} stanzas, it is REQUIRED.</p>
     * <p>If the generated stanza includes an 'id' attribute then it is REQUIRED for the response or error stanza to also include an 'id' attribute, where the value of the 'id' attribute MUST match that of the generated stanza.</p>
     * </blockquote>
     *
     * @return The id.
     */
    public final String getId() {
        return id;
    }

    /**
     * Gets the stanza's 'from' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-from">8.1.2.  from</a></cite></p>
     * <p>The 'from' attribute specifies the JID of the sender.</p>
     * </blockquote>
     *
     * @return The JID.
     */
    public final Jid getFrom() {
        return from;
    }

    /**
     * Creates a copy of this stanza and adds a from attribute to it.
     *
     * @param from The sender.
     * @return A new stanza with a from attribute.
     */
    public abstract Stanza withFrom(Jid from);

    /**
     * Gets the stanza's 'xml:lang' attribute.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-attributes-lang">8.1.5.  xml:lang</a></cite></p>
     * <p>A stanza SHOULD possess an 'xml:lang' attribute (as defined in Section 2.12 of [XML]) if the stanza contains XML character data that is intended to be presented to a human user (as explained in [CHARSETS], "internationalization is for humans"). The value of the 'xml:lang' attribute specifies the default language of any such human-readable XML character data.</p>
     * </blockquote>
     *
     * @return The language.
     */
    public final String getLanguage() {
        return lang;
    }

    /**
     * Gets all extensions.
     *
     * @return The extensions.
     */
    public final List<Object> getExtensions() {
        return extensions;
    }

    /**
     * Gets all extensions.
     *
     * @return The extensions.
     */
    @SuppressWarnings("unchecked")
    public final <T> T getExtension(Class<T> type) {
        for (Object extension : extensions) {
            if (type.isAssignableFrom(extension.getClass())) {
                return (T) extension;
            }
        }
        return null;
    }

    /**
     * Gets the stanza's 'error' element.
     * <blockquote>
     * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#stanzas-error">8.3.  Stanza Errors</a></cite></p>
     * <div>
     * Stanza-related errors are handled in a manner similar to stream errors. Unlike stream errors, stanza errors are recoverable; therefore, they do not result in termination of the XML stream and underlying TCP connection. Instead, the entity that discovers the error condition returns an error stanza, which is a stanza that:
     * <ul>
     * <li>is of the same kind (message, presence, or IQ) as the generated stanza that triggered the error</li>
     * <li>has a 'type' attribute set to a value of "error"</li>
     * <li>typically swaps the 'from' and 'to' addresses of the generated stanza</li>
     * <li>mirrors the 'id' attribute (if any) of the generated stanza that triggered the error</li>
     * <li>contains an {@code <error/>} child element that specifies the error condition and therefore provides a hint regarding actions that the sender might be able to take in an effort to remedy the error (however, it is not always possible to remedy the error)</li>
     * </ul>
     * </div>
     * </blockquote>
     *
     * @return The stanza error.
     */
    public final StanzaError getError() {
        return error;
    }

    /**
     * Creates an error response for this stanza.
     *
     * @param error The error which is appended to the stanza.
     * @return The error response.
     * @see #getError()
     */
    public abstract Stanza createError(StanzaError error);

    /**
     * Creates an error response for this stanza.
     *
     * @param condition The error condition which is appended to the stanza.
     * @return The error response.
     * @see #getError()
     */
    public abstract Stanza createError(Condition condition);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (id != null) {
            sb.append(" (").append(id).append(')');
        }
        if (from != null) {
            sb.append(" from '").append(from).append('\'');
        }
        if (to != null) {
            sb.append(" to '").append(to).append('\'');
        }
        return sb.toString();
    }
}
