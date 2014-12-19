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

package rocks.xmpp.sample;

import rocks.xmpp.core.session.*;
import rocks.xmpp.core.session.context.CoreContext;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.debug.gui.VisualDebugger;
import rocks.xmpp.extensions.compress.model.CompressionMethod;
import rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration;

import javax.net.ssl.*;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * @author Christian Schudt
 */
public class SampleApplication {

    public static void main(String[] args) throws IOException, LoginException {

        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {
                try {

                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, new TrustManager[]{
                            new X509TrustManager() {
                                @Override
                                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                                }

                                @Override
                                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                                }

                                @Override
                                public X509Certificate[] getAcceptedIssuers() {
                                    return new X509Certificate[0];
                                }
                            }
                    }, new SecureRandom());

                    TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                            .port(5222)
                            .sslContext(sslContext)
                            .secure(false)
                            .build();


                    BoshConnectionConfiguration boshConnectionConfiguration = BoshConnectionConfiguration.builder()
                            .hostname("localhost")
                            .port(5280)
                            //.secure(true)
                            //.sslContext(sslContext)
                            .hostnameVerifier(new HostnameVerifier() {
                                @Override
                                public boolean verify(String s, SSLSession sslSession) {
                                    return true;
                                }
                            })
                            .file("/http-bind/")
                            .build();

                    Class<?>[] extensions = new Class<?>[0];
                    Arrays.asList(extensions, XmppSession.class);
                    XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                            .debugger(VisualDebugger.class)
                            .defaultResponseTimeout(5000)
                            .build();

                    XmppSession xmppSession = new XmppSession("localhost", configuration, tcpConfiguration);

                    // Listen for incoming messages.
                    xmppSession.addMessageListener(new MessageListener() {
                        @Override
                        public void handle(MessageEvent e) {
                            if (e.isIncoming()) {
                                System.out.println(e.getMessage());
                            }
                        }
                    });
                    // Connect
                    xmppSession.connect();
                    // Login
                    xmppSession.login("admin", "admin", "xmpp");
                    // Send initial presence
                    xmppSession.send(new Presence());
                } catch (IOException | LoginException | NoSuchAlgorithmException | KeyManagementException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
