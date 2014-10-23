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

package rocks.xmpp.core.sample.geolocation;

import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.MessageListener;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.debug.gui.VisualDebugger;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * @author Christian Schudt
 */
public class GeolocationPublisher {

    public static void main(String[] args) throws IOException, LoginException {

        Executors.newFixedThreadPool(1).execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                            .port(5222)
                            .secure(false)
                            .build();

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
                    xmppSession.login("111", "111", "geolocation");
                    // Send initial presence
                    xmppSession.send(new Presence());

//                    GeoLocationManager geoLocationManager = xmppSession.getExtensionManager(GeoLocationManager.class);
//                    geoLocationManager.publish(new GeoLocation(123, 321));

                } catch (IOException | LoginException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
