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

package rocks.xmpp.extensions.si.model;

import rocks.xmpp.extensions.featureneg.model.FeatureNegotiation;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * The implementation of the {@code <open/>} element in the {@code http://jabber.org/protocol/ibb} namespace.
 *
 * @author Christian Schudt
 * @see <a href="http://xmpp.org/extensions/xep-0095.html">XEP-0095: Stream Initiation</a>
 * @see <a href="http://xmpp.org/extensions/xep-0095.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "si")
@XmlSeeAlso({BadProfile.class, NoValidStreams.class})
public final class StreamInitiation {

    /**
     * http://jabber.org/protocol/si
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/si";

    /**
     * The implementation of the {@code <si:bad-profile/>} error condition.
     * <p>
     * The profile is not understood or invalid. The profile MAY supply a profile-specific error condition.
     * </p>
     */
    public static final Object BAD_PROFILE = new BadProfile();

    /**
     * The implementation of the {@code <si:no-valid-streams/>} error condition.
     * <p>
     * None of the available streams are acceptable.
     * </p>
     */
    public static final Object NO_VALID_STREAMS = new NoValidStreams();

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "mime-type")
    private String mimeType;

    @XmlAttribute(name = "profile")
    private String profile;

    @XmlAnyElement(lax = true)
    private Object profileElement;

    @XmlElementRef
    private FeatureNegotiation featureNegotiation;

    private StreamInitiation() {
    }

    public StreamInitiation(FeatureNegotiation featureNegotiation) {
        this.featureNegotiation = featureNegotiation;
    }

    public StreamInitiation(String id, String profile, String mimeType, Object profileElement, FeatureNegotiation featureNegotiation) {
        this.id = id;
        this.profile = profile;
        this.mimeType = mimeType;
        this.profileElement = profileElement;
        this.featureNegotiation = featureNegotiation;
    }

    /**
     * The "id" attribute is an opaque identifier. This attribute MUST be present on type='set', and MUST be a valid string.
     *
     * @return The id.
     */
    public String getId() {
        return id;
    }

    /**
     * The "mime-type" attribute identifies the MIME-type for the data across the stream.
     *
     * @return The MIME type.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * The "profile" attribute defines the SI profile in use. This value MUST be present during negotiation, and is the namespace of the profile to use.
     *
     * @return The profile.
     */
    public String getProfile() {
        return profile;
    }

    /**
     * Gets the profile element, e.g. {@link rocks.xmpp.extensions.si.profile.filetransfer.model.SIFileTransferOffer}.
     *
     * @return The profile element.
     */
    public Object getProfileElement() {
        return profileElement;
    }

    /**
     * Gets the feature negotiation element.
     *
     * @return The feature negotiation.
     */
    public FeatureNegotiation getFeatureNegotiation() {
        return featureNegotiation;
    }
}
