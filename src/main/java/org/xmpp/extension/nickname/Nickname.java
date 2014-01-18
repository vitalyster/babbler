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

package org.xmpp.extension.nickname;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * The implementation of <a href="http://xmpp.org/extensions/xep-0172.html">XEP-0172: User Nickname</a>.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0172.html#intro">1. Introduction</a></cite></p>
 * <p>A nickname is a global, memorable (but not necessarily unique) friendly or informal name chosen by the owner of a bare JID <localpart@domain.tld> for the purpose of associating a distinctive mapping between the person's unique JID and non-unique nickname.</p>
 * </blockquote>
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0172.html#usecases">4. Use Cases</a></cite></p>
 * <p>In general, a user SHOULD include his or her nickname when establishing initial communication with a contact or group of contacts (i.e., the user has never been in communication with and does not have a prior relationship with the contact or group of contacts). Appropriate use cases therefore include:</p>
 * <ul>
 * <li>Presence subscription requests</li>
 * <li>Message exchange</li>
 * </ul>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "nick")
public final class Nickname {

    @XmlValue
    private String value;

    private Nickname() {
    }

    /**
     * Creates a nick name.
     *
     * @param value The actual nick name.
     */
    public Nickname(String value) {
        this.value = value;
    }

    /**
     * Gets the nick name.
     *
     * @return The nick name.
     */
    public String getValue() {
        return value;
    }
}
