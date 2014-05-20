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

package org.xmpp.extension.caps;

import org.xmpp.extension.data.DataForm;
import org.xmpp.extension.disco.info.Identity;
import org.xmpp.extension.disco.info.InfoNode;
import org.xmpp.stream.Feature;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of the {@code <c/>} element.
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "c")
public final class EntityCapabilities extends Feature {

    static final String NAMESPACE = "http://jabber.org/protocol/caps";

    /**
     * The hashing algorithm used to generate the verification string.
     */
    @XmlAttribute
    private String hash;

    @XmlAttribute
    private String node;

    /**
     * The 'ver' attribute is a specially-constructed string (called a "verification string") that represents the entity's service discovery identity.
     */
    @XmlAttribute
    private String ver;

    private EntityCapabilities() {
    }

    public EntityCapabilities(String node, String hash, String ver) {
        this.node = node;
        this.hash = hash;
        this.ver = ver;
    }

    public static String getVerificationString(InfoNode infoNode, MessageDigest messageDigest) {

        List<Identity> identities = new ArrayList<>(infoNode.getIdentities());
        List<org.xmpp.extension.disco.info.Feature> features = new ArrayList<>(infoNode.getFeatures());
        List<DataForm> dataForms = new ArrayList<>(infoNode.getExtensions());

        // 1. Initialize an empty string S.
        StringBuilder sb = new StringBuilder();

        // 2. Sort the service discovery identities [15] by category and then by type and then by xml:lang (if it exists), formatted as CATEGORY '/' [TYPE] '/' [LANG] '/' [NAME]. [16] Note that each slash is included even if the LANG or NAME is not included (in accordance with XEP-0030, the category and type MUST be included.
        Collections.sort(identities);

        // 3. For each identity, append the 'category/type/lang/name' to S, followed by the '<' character.
        for (Identity identity : identities) {
            if (identity.getCategory() != null) {
                sb.append(identity.getCategory());
            }
            sb.append("/");
            if (identity.getType() != null) {
                sb.append(identity.getType());
            }
            sb.append("/");
            if (identity.getLanguage() != null) {
                sb.append(identity.getLanguage());
            }
            sb.append("/");
            if (identity.getName() != null) {
                sb.append(identity.getName());
            }
            sb.append("<");
        }

        // 4. Sort the supported service discovery features.
        Collections.sort(features);

        // 5. For each feature, append the feature to S, followed by the '<' character.
        for (org.xmpp.extension.disco.info.Feature feature : features) {
            if (feature.getVar() != null) {
                sb.append(feature.getVar());
            }
            sb.append("<");
        }

        // 6. If the service discovery information response includes XEP-0128 data forms, sort the forms by the FORM_TYPE (i.e., by the XML character data of the <value/> element).
        Collections.sort(dataForms);

        // 7. For each extended service discovery information form:
        for (DataForm dataForm : dataForms) {

            // 7.2. Sort the fields by the value of the "var" attribute.
            // This makes sure, that FORM_TYPE fields are always on zero position.
            Collections.sort(dataForm.getFields());

            if (!dataForm.getFields().isEmpty()) {

                // Also make sure, that we don't send an ill-formed verification string.
                // 3.6 If the response includes an extended service discovery information form where the FORM_TYPE field is not of type "hidden" or the form does not include a FORM_TYPE field, ignore the form but continue processing.
                if (!"FORM_TYPE".equals(dataForm.getFields().get(0).getVar()) || dataForm.getFields().get(0).getType() != DataForm.Field.Type.HIDDEN) {
                    // => Don't include this form in the verification string.
                    continue;
                }

                for (DataForm.Field field : dataForm.getFields()) {
                    // 7.3. For each field other than FORM_TYPE:
                    if (!"FORM_TYPE".equals(field.getVar())) {
                        // 7.3.1. Append the value of the "var" attribute, followed by the '<' character.
                        sb.append(field.getVar());
                        sb.append("<");

                        // 7.3.2. Sort values by the XML character data of the <value/> element.
                        Collections.sort(field.getValues());
                    }
                    // 7.1. Append the XML character data of the FORM_TYPE field's <value/> element, followed by the '<' character.
                    // 7.3.3. For each <value/> element, append the XML character data, followed by the '<' character.
                    for (String value : field.getValues()) {
                        sb.append(value);
                        sb.append("<");
                    }
                }

            }
        }

        // 8. Ensure that S is encoded according to the UTF-8 encoding
        String plainString = sb.toString();

        // 9. Compute the verification string by hashing S using the algorithm specified in the 'hash' attribute.
        messageDigest.reset();
        return DatatypeConverter.printBase64Binary(messageDigest.digest(plainString.getBytes()));
    }

    /**
     * Gets the hashing algorithm used to generate the verification string.
     *
     * @return The verification string.
     * @see #setHashingAlgorithm(String)
     */
    public String getHashingAlgorithm() {
        return hash;
    }

    /**
     * Sets the hashing algorithm used to generate the verification string.
     *
     * @param hash The verification string.
     * @see #getHashingAlgorithm()
     */
    public void setHashingAlgorithm(String hash) {
        this.hash = hash;
    }

    /**
     * Gets the node.
     * <blockquote>
     * <p>A URI that uniquely identifies a software application, typically a URL at the website of the project or company that produces the software.</p>
     * <p>It is RECOMMENDED for the value of the 'node' attribute to be an HTTP URL at which a user could find further information about the software product, such as "http://psi-im.org" for the Psi client; this enables a processing application to also determine a unique string for the generating application, which it could maintain in a list of known software implementations (e.g., associating the name received via the disco#info reply with the URL found in the caps data).</p>
     * </blockquote>
     *
     * @return The node.
     * @see #setNode(String)
     */
    public String getNode() {
        return node;
    }

    /**
     * Sets the node.
     *
     * @param node The node.
     * @see #getNode()
     */
    public void setNode(String node) {
        this.node = node;
    }

    /**
     * Gets the verification string that is used to verify the identity and supported features of the entity.
     *
     * @return The verification string.
     * @see #setVerificationString(String)
     */
    public String getVerificationString() {
        return ver;
    }

    /**
     * Sets the verification string.
     *
     * @param verificationString The verification string.
     * @see #getVerificationString()
     */
    public void setVerificationString(String verificationString) {
        this.ver = verificationString;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
