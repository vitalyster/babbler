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

package org.xmpp.extension.hashes;

import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Christian Schudt
 */
public final class HashManager extends ExtensionManager {

    private final static String[] REGISTERED_HASH_ALGORITHMS = new String[]{"md5", "sha-1", "sha-224", "sha-256", "sha-384", "sha-512"};

    private HashManager(XmppSession xmppSession) {
        super(xmppSession, "urn:xmpp:hashes:1");
        for (String algorithm : REGISTERED_HASH_ALGORITHMS) {
            try {
                MessageDigest.getInstance(algorithm);
                features.add("urn:xmpp:hash-function-text-names:" + algorithm);
            } catch (NoSuchAlgorithmException e) {
                // ignore
            }
        }

        setEnabled(true);
    }
}
