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

package rocks.xmpp.im.subscription.preapproval.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import rocks.xmpp.core.stream.model.StreamFeature;

/**
 * The implementation of the subscription pre-approval feature.
 *
 * <p>This class is immutable.</p>
 *
 * @author Christian Schudt
 */
@XmlRootElement(name = "sub")
@XmlType(factoryMethod = "create")
public final class SubscriptionPreApproval extends StreamFeature {

    /**
     * The {@code <sub/>} element.
     */
    public static final SubscriptionPreApproval INSTANCE = new SubscriptionPreApproval();

    private SubscriptionPreApproval() {
    }

    @SuppressWarnings("unused")
    private static SubscriptionPreApproval create() {
        return INSTANCE;
    }
}
