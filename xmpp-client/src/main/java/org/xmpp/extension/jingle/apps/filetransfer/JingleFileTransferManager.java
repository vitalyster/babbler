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

package org.xmpp.extension.jingle.apps.filetransfer;

import org.xmpp.Jid;
import org.xmpp.NoResponseException;
import org.xmpp.XmppException;
import org.xmpp.XmppSession;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.bytestreams.ByteStreamSession;
import org.xmpp.extension.bytestreams.ibb.InBandByteStreamManager;
import org.xmpp.extension.filetransfer.FileTransferRejectedException;
import org.xmpp.extension.jingle.*;
import org.xmpp.extension.jingle.transports.TransportMethod;
import org.xmpp.extension.jingle.transports.ibb.InBandBytestreamsTransportMethod;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Christian Schudt
 */
public final class JingleFileTransferManager extends ExtensionManager {

    private final JingleManager jingleManager;

    private JingleFileTransferManager(XmppSession xmppSession) {
        super(xmppSession, "urn:xmpp:jingle:apps:file-transfer:3");
        jingleManager = xmppSession.getExtensionManager(JingleManager.class);
    }

    public JingleFileTransferSession initiateFileTransferSession(final Jid responder, File file, String description, long timeout) throws XmppException, IOException {
        JingleFileTransfer.File jingleFile = new JingleFileTransfer.File(file.getName(), file.length(), new Date(file.lastModified()), null, description);
        JingleFileTransfer jingleFileTransfer = new JingleFileTransfer(jingleFile);


        String ibbSessionId = UUID.randomUUID().toString();
        InBandBytestreamsTransportMethod ibbTransportMethod = new InBandBytestreamsTransportMethod(ibbSessionId, 4096);

        Jingle.Content content = new Jingle.Content("a-file-offer", Jingle.Content.Creator.INITIATOR, jingleFileTransfer, ibbTransportMethod);
        final JingleSession jingleSession = jingleManager.createSession(responder, content);

        final Lock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        final Jingle[] response = new Jingle[1];

        jingleSession.addJingleListener(new JingleListener() {
            @Override
            public void jingleReceived(JingleEvent e) {
                if (e.getJingle().getAction() == Jingle.Action.SESSION_ACCEPT || e.getJingle().getAction() == Jingle.Action.SESSION_TERMINATE) {
                    lock.lock();
                    try {
                        response[0] = e.getJingle();
                        condition.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            }
        });
        jingleSession.initiate();

        // Wait until the session is either accepted or declined (terminated).
        lock.lock();
        try {
            if (!condition.await(timeout, TimeUnit.MILLISECONDS)) {
                throw new NoResponseException("The receiver did not respond in time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
        Jingle jingle = response[0];
        if (jingle.getAction() == Jingle.Action.SESSION_TERMINATE) {
            throw new FileTransferRejectedException();
        }
        if (jingle.getAction() == Jingle.Action.SESSION_ACCEPT) {
            // TODO respect responders transport method.
            InBandByteStreamManager inBandByteStreamManager = xmppSession.getExtensionManager(InBandByteStreamManager.class);
            inBandByteStreamManager.initiateSession(responder, ibbSessionId, 4096);
        }
        return new JingleFileTransferSession(jingleSession);
    }
}
