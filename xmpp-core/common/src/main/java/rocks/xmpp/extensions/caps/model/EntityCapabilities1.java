/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Christian Schudt
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

package rocks.xmpp.extensions.caps.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import rocks.xmpp.core.stream.model.StreamFeature;
import rocks.xmpp.extensions.data.model.DataForm;
import rocks.xmpp.extensions.disco.model.info.DiscoverableInfo;
import rocks.xmpp.extensions.disco.model.info.Identity;
import rocks.xmpp.extensions.hashes.model.Hashed;
import rocks.xmpp.util.Strings;

/**
 * The implementation of the {@code <c/>} element in the {@code http://jabber.org/protocol/caps}.
 *
 * <p>This class is immutable.</p>
 *
 * @author Christian Schudt
 * @see <a href="https://xmpp.org/extensions/xep-0115.html">XEP-0115: Entity Capabilities</a>
 * @see <a href="https://xmpp.org/extensions/xep-0115.html#schema">XML Schema</a>
 */
@XmlRootElement(name = "c")
public final class EntityCapabilities1 extends StreamFeature implements EntityCapabilities, Hashed {

    /**
     * {@value}
     */
    public static final String NAMESPACE = "http://jabber.org/protocol/caps";

    /**
     * The hashing algorithm used to generate the verification string.
     */
    @XmlAttribute
    private final String hash;

    @XmlAttribute
    private final String node;

    /**
     * The 'ver' attribute is a specially-constructed string (called a "verification string") that represents the
     * entity's service discovery identity.
     */
    @XmlAttribute
    private final byte[] ver;

    private EntityCapabilities1() {
        this.node = null;
        this.hash = null;
        this.ver = null;
    }

    /**
     * Creates an entity caps from discoverable info and hash function.
     *
     * @param node             The node.
     * @param discoverableInfo The discoverable info.
     * @param messageDigest    The hash function.
     */
    public EntityCapabilities1(final String node, final DiscoverableInfo discoverableInfo,
                               final MessageDigest messageDigest) {
        this.node = Objects.requireNonNull(node);
        this.hash = messageDigest.getAlgorithm();
        this.ver = messageDigest.digest(createVerificationString(discoverableInfo));
    }

    @Override
    public final String getHashAlgorithm() {
        return hash;
    }

    @Override
    public final byte[] getHashValue() {
        return ver != null ? ver.clone() : null;
    }

    /**
     * Gets the node.
     *
     * <blockquote>
     * <p>A URI that uniquely identifies a software application, typically a URL at the website of the project or
     * company that produces the software.</p>
     * <p>It is RECOMMENDED for the value of the 'node' attribute to be an HTTP URL at which a user could find further
     * information about the software product, such as "http://psi-im.org" for the Psi client; this enables a processing
     * application to also determine a unique string for the generating application, which it could maintain in a list
     * of known software implementations (e.g., associating the name received via the disco#info reply with the URL
     * found in the caps data).</p>
     * </blockquote>
     *
     * @return The node.
     */
    public final String getNode() {
        return node;
    }

    /**
     * Gets the verification string that is used to verify the identity and supported features of the entity.
     *
     * @return The verification string.
     */
    public final String getVerificationString() {
        return ver != null ? Base64.getEncoder().encodeToString(ver) : null;
    }

    @Override
    public final Set<Hashed> getCapabilityHashSet() {
        return Collections.singleton(this);
    }

    @Override
    public final byte[] createVerificationString(final DiscoverableInfo discoverableInfo) {

        final Set<Identity> identities = discoverableInfo.getIdentities();
        final Set<String> features = discoverableInfo.getFeatures();
        final List<DataForm> dataForms = new ArrayList<>(discoverableInfo.getExtensions());

        // 1. Initialize an empty string S.
        final StringBuilder sb = new StringBuilder();

        // 2. Sort the service discovery identities [15] by category and then by type and then by xml:lang
        // (if it exists), formatted as CATEGORY '/' [TYPE] '/' [LANG] '/' [NAME]. [16] Note that each slash is
        // included even if the LANG or NAME is not included (in accordance with XEP-0030, the category and
        // type MUST be included.
        // This is done by TreeSet.

        // 3. For each identity, append the 'category/type/lang/name' to S, followed by the '<' character.
        for (Identity identity : identities) {
            if (identity.getCategory() != null) {
                sb.append(identity.getCategory());
            }
            sb.append('/');
            if (identity.getType() != null) {
                sb.append(identity.getType());
            }
            sb.append('/');
            if (identity.getLanguage() != null) {
                sb.append(identity.getLanguage());
            }
            sb.append('/');
            if (identity.getName() != null) {
                sb.append(identity.getName());
            }
            sb.append('<');
        }

        // 4. Sort the supported service discovery features.
        // This is done by TreeSet.

        // 5. For each feature, append the feature to S, followed by the '<' character.
        for (String feature : features) {
            if (feature != null) {
                sb.append(feature);
            }
            sb.append('<');
        }

        // 6. If the service discovery information response includes XEP-0128 data forms, sort the forms by the
        // FORM_TYPE (i.e., by the XML character data of the <value/> element).
        dataForms.sort(null);

        // 7. For each extended service discovery information form:
        for (DataForm dataForm : dataForms) {
            final List<DataForm.Field> fields = new ArrayList<>(dataForm.getFields());
            // 7.2. Sort the fields by the value of the "var" attribute.
            // This makes sure, that FORM_TYPE fields are always on zero position.
            fields.sort(null);

            if (!fields.isEmpty()) {

                // Also make sure, that we don't send an ill-formed verification string.
                // 3.6 If the response includes an extended service discovery information form where the FORM_TYPE
                // field is not of type "hidden" or the form does not include a FORM_TYPE field, ignore the form
                // but continue processing.
                if (!DataForm.FORM_TYPE.equals(fields.get(0).getName())
                        || fields.get(0).getType() != DataForm.Field.Type.HIDDEN) {
                    // => Don't include this form in the verification string.
                    continue;
                }

                for (DataForm.Field field : fields) {
                    List<String> values = new ArrayList<>(field.getValues());
                    // 7.3. For each field other than FORM_TYPE:
                    if (!DataForm.FORM_TYPE.equals(field.getName())) {
                        // 7.3.1. Append the value of the "var" attribute, followed by the '<' character.
                        sb.append(field.getName()).append('<');

                        // 7.3.2. Sort values by the XML character data of the <value/> element.
                        values.sort((s, t1) -> Strings.compareUnsignedBytes(s, t1, StandardCharsets.UTF_8));
                    }
                    // 7.1. Append the XML character data of the FORM_TYPE field's <value/> element, followed by the
                    // '<' character.
                    // 7.3.3. For each <value/> element, append the XML character data, followed by the '<' character.
                    for (String value : values) {
                        sb.append(value).append('<');
                    }
                }
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public final String createCapabilityHashNode(final Hashed hashed) {
        // The value of the 'node' attribute SHOULD be generated by concatenating the value of the caps 'node' attribute
        // (e.g., "http://code.google.com/p/exodus") as provided by the generating entity, the "#" character, and the
        // value of the caps 'ver' attribute (e.g., "QgayPKawpkPSDYmwT/WM94uAlu0=") as provided by the generating entity
        return node + '#' + Base64.getEncoder().encodeToString(hashed.getHashValue());
    }

    @Override
    public final boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EntityCapabilities1)) {
            return false;
        }
        EntityCapabilities1 other = (EntityCapabilities1) o;

        return Objects.equals(node, other.node)
                && Objects.equals(hash, other.hash)
                && Arrays.equals(ver, other.ver);

    }

    @Override
    public final int hashCode() {
        return Objects.hash(node, hash, Arrays.hashCode(ver));
    }

    @Override
    public final String toString() {
        return "Caps: " + getVerificationString();
    }
}
