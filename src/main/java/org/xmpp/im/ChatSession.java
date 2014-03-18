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

package org.xmpp.im;

import org.xmpp.Connection;
import org.xmpp.Jid;
import org.xmpp.stanza.Message;
import org.xmpp.stanza.MessageEvent;
import org.xmpp.stanza.MessageListener;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a one-to-one chat session. They are described in <a href="http://xmpp.org/rfcs/rfc6121.html#message-chat">5.1.  One-to-One Chat Sessions</a> and <a href="http://xmpp.org/extensions/xep-0201.html">XEP-0201: Best Practices for Message Threads</a>.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-chat">5.1.  One-to-One Chat Sessions</a></cite></p>
 * <p>In practice, instant messaging activity between human users tends to occur in the form of a conversational burst that we call a "chat session": the exchange of multiple messages between two parties in relatively rapid succession within a relatively brief period of time.</p>
 * </blockquote>
 * <p>
 * In order to create a new chat session, use the {@linkplain ChatManager#newChatSession(org.xmpp.Jid) chat manager}.
 * </p>
 */
public final class ChatSession {
    private static final Logger logger = Logger.getLogger(ChatSession.class.getName());

    private final String thread;

    private final Connection connection;

    private final Set<MessageListener> messageListeners = new CopyOnWriteArraySet<>();

    volatile Jid chatPartner;

    ChatSession(Jid chatPartner, String thread, Connection connection) {
        if (chatPartner == null) {
            throw new IllegalArgumentException("chatPartner must not be null.");
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null.");
        }
        // The user's client SHOULD address the initial message in a chat session to the bare JID <contact@domainpart> of the contact (rather than attempting to guess an appropriate full JID <contact@domainpart/resourcepart> based on the <show/>, <status/>, or <priority/> value of any presence notifications it might have received from the contact).
        this.chatPartner = chatPartner.asBareJid();
        this.thread = thread;
        this.connection = connection;
    }

    /**
     * Adds a message listener, which will get notified, whenever a chat message has been received for this chat session.
     *
     * @param stanzaListener The listener.
     * @see #removeMessageListener(org.xmpp.stanza.MessageListener)
     */
    public void addMessageListener(MessageListener stanzaListener) {
        messageListeners.add(stanzaListener);
    }

    /**
     * Removes a previously added message listener.
     *
     * @param stanzaListener The listener.
     * @see #addMessageListener(org.xmpp.stanza.MessageListener)
     */
    public void removeMessageListener(MessageListener stanzaListener) {
        messageListeners.remove(stanzaListener);
    }

    void notifyMessageListeners(Message message, boolean incoming) {
        for (MessageListener messageListener : messageListeners) {
            try {
                messageListener.handle(new MessageEvent(this, message, incoming));
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Sends a chat message to the chat partner.
     *
     * @param message The message.
     */
    public void send(Message message) {
        // the message type generated by the user's client SHOULD be "chat" and the contact's client SHOULD preserve that message type in subsequent replies.
        message.setType(Message.Type.CHAT);
        // The user's client also SHOULD include a <thread/> element with its initial message, which the contact's client SHOULD also preserve during the life of the chat session (see Section 5.2.5).
        message.setThread(thread);
        message.setTo(chatPartner);
        connection.send(message);
    }

    /**
     * Gets the chat partner of this chat session.
     *
     * @return The chat partner.
     */
    public Jid getChatPartner() {
        return chatPartner;
    }

    /**
     * Gets the thread id which is used for this chat session.
     *
     * @return The thread id.
     */
    public String getThread() {
        return thread;
    }
}
