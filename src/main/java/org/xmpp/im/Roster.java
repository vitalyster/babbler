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

package org.xmpp.im;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the roster.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#roster-syntax">2.1.  Syntax and Semantics</a></cite></p>
 * <p>Rosters are managed using {@code <iq/>} stanzas (see Section 8.2.3 of [XMPP-CORE]), specifically by means of a {@code <query/>} child element qualified by the 'jabber:iq:roster' namespace. The detailed syntax and semantics are defined in the following sections.</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "query")
public final class Roster {

    @XmlAttribute
    @SuppressWarnings("unused") // Only set by server.
    private String ver;

    @XmlElement
    private List<Contact> item = new ArrayList<>();

    /**
     * Gets the roster version.
     *
     * @return The roster version.
     */
    public String getVersion() {
        return ver;
    }

    /**
     * Gets the contacts.
     *
     * @return The contacts.
     */
    public List<Contact> getContacts() {
        return item;
    }
}
