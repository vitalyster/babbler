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

package org.xmpp.stream;

import org.xmpp.Connection;
import org.xmpp.ConnectionEvent;
import org.xmpp.ConnectionListener;
import org.xmpp.tls.StartTls;

import java.util.*;

/**
 * Manages the various features, which are advertised during stream negotiation.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6120.html#streams-negotiation">4.3.  Stream Negotiation</a></cite></p>
 * <p>Because the receiving entity for a stream acts as a gatekeeper to the domains it services, it imposes certain conditions for connecting as a client or as a peer server. At a minimum, the initiating entity needs to authenticate with the receiving entity before it is allowed to send stanzas to the receiving entity (for client-to-server streams this means using SASL as described under Section 6). However, the receiving entity can consider conditions other than authentication to be mandatory-to-negotiate, such as encryption using TLS as described under Section 5. The receiving entity informs the initiating entity about such conditions by communicating "stream features": the set of particular protocol interactions that the initiating entity needs to complete before the receiving entity will accept XML stanzas from the initiating entity, as well as any protocol interactions that are voluntary-to-negotiate but that might improve the handling of an XML stream (e.g., establishment of application-layer compression as described in [XEP-0138]).</p>
 * </blockquote>
 * <p>Each feature is associated with a {@linkplain FeatureNegotiator feature negotiator}, which negotiates the particular feature.</p>
 * <p>This class manages these negotiators, receives XML elements and delegates them to the responsible feature negotiator for further processing.</p>
 * <p>It negotiates the stream by sequentially negotiating each stream feature.</p>
 *
 * @author Christian Schudt
 */
public final class FeaturesManager {
    private final Connection connection;

    /**
     * The features which have been advertised by the server.
     */
    private final Map<Class<? extends Feature>, Feature> features = new HashMap<>();

    /**
     * The list of features, which the server advertised and have not yet been negotiated.
     */
    private final List<Feature> featuresToNegotiate = new ArrayList<>();

    /**
     * The feature negotiators, which are responsible to negotiate each individual feature.
     */
    private final List<FeatureNegotiator> featureNegotiators = new ArrayList<>();

    /**
     * Creates a feature manager.
     *
     * @param connection The connection, features will be negotiated for.
     */
    public FeaturesManager(Connection connection) {
        this.connection = connection;

        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                switch (e.getStatus()) {
                    // If we're (re)connecting, make sure any previous features are forgotten.
                    case CONNECTING:
                        features.clear();
                        featuresToNegotiate.clear();
                        break;
                    // If the connection is closed, clear everything.
                    case CLOSED:
                        featureNegotiators.clear();
                        featuresToNegotiate.clear();
                        features.clear();
                        break;
                }
            }
        });
    }

    /**
     * Gets the available features, which the server has advertised.
     *
     * @return The features.
     */
    public Map<Class<? extends Feature>, Feature> getFeatures() {
        return features;
    }

    /**
     * Adds a new feature negotiator, which is responsible for negotiating an individual feature.
     *
     * @param featureNegotiator The feature negotiator, which is responsible for the feature.
     */
    public void addFeatureNegotiator(FeatureNegotiator featureNegotiator) {
        featureNegotiators.add(featureNegotiator);
    }

    /**
     * Processes the {@code <stream:features/>} element and immediately starts negotiating the first feature.
     *
     * @param featuresElement The {@code <stream:features/>} element.
     * @throws Exception If an exception occurred during feature negotiation.
     */
    public void processFeatures(Features featuresElement) throws Exception {
        List<Object> featureList = featuresElement.getFeatures();
        List<Feature> sortedFeatureList = new ArrayList<>();

        featuresToNegotiate.clear();

        // Check if a feature is known, that means it must implement Feature and be added to the context.
        for (Object feature : featureList) {
            if (feature instanceof Feature) {
                Feature f = (Feature) feature;
                features.put(f.getClass(), f);
                sortedFeatureList.add(f);
            }
        }
        // If the receiving entity advertises only the STARTTLS feature [...] the parties MUST consider TLS as mandatory-to-negotiate.
        if (featureList.size() == 1 && featureList.get(0) instanceof StartTls) {
            ((StartTls) featureList.get(0)).setMandatory(true);
        }

        // Sort features, so that voluntary-to-negotiate features are negotiated first. The spec says:
        // A <features/> element that contains both mandatory-to-negotiate and voluntary-to-negotiate features
        // indicates that the negotiation is not complete but that the initiating entity MAY complete
        // the voluntary-to-negotiate feature(s) before it attempts to negotiate the mandatory-to-negotiate feature(s).
        Collections.sort(sortedFeatureList, new Comparator<Feature>() {
            @Override
            public int compare(Feature o1, Feature o2) {
                int result = 0;
                if (o1 != null && o2 != null) {
                    result = Boolean.compare(o1.isMandatory(), o2.isMandatory());
                }
                return result;
            }
        });

        // Store the list of features. Each feature will be negotiated sequentially, if there is a corresponding feature negotiator.
        featuresToNegotiate.addAll(sortedFeatureList);

        // Immediately start negotiating the first feature.
        negotiateNextFeature();
    }

    /**
     * Tries to process an element, which is a feature or may belong to a feature protocol, e.g. the {@code <proceed/>} element from TLS negotiation.
     *
     * @param element The element.
     * @return True, if the stream needs restarted, after a feature has been negotiated.
     * @throws Exception If an exception occurred during feature negotiation.
     */
    public boolean processElement(Object element) throws Exception {
        // Check if the element is known to any feature negotiator.
        for (FeatureNegotiator featureNegotiator : featureNegotiators) {
            if (featureNegotiator.getFeatureClass() == element || featureNegotiator.canProcess(element)) {
                FeatureNegotiator.Status status = featureNegotiator.processNegotiation(element);
                // If the feature has been successfully negotiated.
                if (status == FeatureNegotiator.Status.SUCCESS) {
                    // Check if the feature expects a restart now.
                    if (featureNegotiator.needsRestart()) {
                        return true;
                    } else {
                        // If no restart is required, negotiate the next feature.
                        negotiateNextFeature();
                    }
                } else if (status == FeatureNegotiator.Status.FAILURE) {
                    // Ignore the failure and negotiate the next feature.
                    negotiateNextFeature();
                }
            }
        }
        return false;
    }

    /**
     * Negotiates the next feature. If the feature has been successfully negotiated, the next feature is automatically negotiated.
     *
     * @throws Exception If an exception occurred during feature negotiation.
     */
    private void negotiateNextFeature() throws Exception {
        if (featuresToNegotiate.size() > 0 && connection.getStatus() == Connection.Status.CONNECTING) {
            Feature advertisedFeature = featuresToNegotiate.remove(0);

            // See if there's a feature negotiator associated with the feature.
            for (FeatureNegotiator featureNegotiator : featureNegotiators) {
                if (featureNegotiator.getFeatureClass() == advertisedFeature.getClass()) {
                    // If feature negotiation is incomplete, return and wait until it is completed.
                    if (featureNegotiator.processNegotiation(advertisedFeature) == FeatureNegotiator.Status.INCOMPLETE) {
                        return;
                    }
                }
            }

            // If no feature negotiator was found or if the feature has been successfully negotiated, immediately go on with the next feature.
            negotiateNextFeature();
        }
    }
}
