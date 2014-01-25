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

package org.xmpp.extension.featurenegotiation;

import org.xmpp.extension.dataforms.DataForm;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The implementation of <a href="http://xmpp.org/extensions/xep-0020.html">XEP-0020: Feature Negotiation</a>
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/extensions/xep-0020.html#intro">1. Introduction</a></cite></p>
 * <p>The protocol defined herein enables Jabber entities to negotiate options for specific features. These features could be negotiated between any two endpoints on the Jabber network, such as two clients, a client and a component, two components, a client and a server, or two servers. The protocol is generic enough that it can be used whenever options need to be negotiated between two Jabber entities. For examples, Stream Initiation (XEP-0095) [2], SI File Transfer (XEP-0096) [3] or Stanza Session Negotiation (XEP-0155) [4].</p>
 * </blockquote>
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "feature")
public final class FeatureNegotiation {

    @XmlElementRef
    private DataForm dataForm;

    private FeatureNegotiation() {
    }

    /**
     * Creates a feature negotiation with structured data.
     *
     * @param dataForm The structured data form, which contains feature negotiation details.
     */
    public FeatureNegotiation(DataForm dataForm) {
        this.dataForm = dataForm;
    }

    /**
     * Gets the data form, which contains feature negotiation details.
     *
     * @return The data form.
     */
    public DataForm getDataForm() {
        return dataForm;
    }
}
