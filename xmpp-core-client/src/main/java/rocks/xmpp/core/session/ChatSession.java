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

package rocks.xmpp.core.session;

import rocks.xmpp.core.Jid;
import rocks.xmpp.core.stanza.model.AbstractMessage;
import rocks.xmpp.core.stanza.model.client.Message;

/**
 * Implements a one-to-one chat session. They are described in <a href="http://xmpp.org/rfcs/rfc6121.html#message-chat">5.1.  One-to-One Chat Sessions</a> and <a href="http://xmpp.org/extensions/xep-0201.html">XEP-0201: Best Practices for Message Threads</a>.
 * <blockquote>
 * <p><cite><a href="http://xmpp.org/rfcs/rfc6121.html#message-chat">5.1.  One-to-One Chat Sessions</a></cite></p>
 * <p>In practice, instant messaging activity between human users tends to occur in the form of a conversational burst that we call a "chat session": the exchange of multiple messages between two parties in relatively rapid succession within a relatively brief period of time.</p>
 * </blockquote>
 * <p>
 * In order to create a new chat session, use the {@linkplain ChatManager#createChatSession(rocks.xmpp.core.Jid) chat manager}.
 * </p>
 */
public final class ChatSession extends Chat {

    private final String thread;

    private final XmppSession xmppSession;

    volatile Jid chatPartner;

    ChatSession(Jid chatPartner, String thread, XmppSession xmppSession) {
        if (chatPartner == null) {
            throw new IllegalArgumentException("chatPartner must not be null.");
        }
        if (xmppSession == null) {
            throw new IllegalArgumentException("connection must not be null.");
        }
        // The user's client SHOULD address the initial message in a chat session to the bare JID <contact@domainpart> of the contact (rather than attempting to guess an appropriate full JID <contact@domainpart/resourcepart> based on the <show/>, <status/>, or <priority/> value of any presence notifications it might have received from the contact).
        this.chatPartner = chatPartner.asBareJid();
        this.thread = thread;
        this.xmppSession = xmppSession;
    }

    /**
     * @param message The message.
     * @deprecated Use {@link #sendMessage(String)}
     */
    @Deprecated
    public void send(String message) {
        sendMessage(message);
    }

    /**
     * @param message The message.
     * @deprecated Use {@link #sendMessage(Message)}
     */
    @Deprecated
    public void send(Message message) {
        sendMessage(message);
    }

    /**
     * Sends a chat message to the chat partner.
     *
     * @param message The message.
     */
    @Override
    public void sendMessage(String message) {
        sendMessage(new Message(chatPartner, AbstractMessage.Type.CHAT, message));
    }

    /**
     * Sends a chat message to the chat partner.
     *
     * @param message The message.
     */
    @Override
    public void sendMessage(Message message) {
        // the message type generated by the user's client SHOULD be "chat" and the contact's client SHOULD preserve that message type in subsequent replies.
        // The user's client also SHOULD include a <thread/> element with its initial message, which the contact's client SHOULD also preserve during the life of the chat session (see Section 5.2.5).
        xmppSession.send(new Message(chatPartner, Message.Type.CHAT, message.getBodies(), message.getSubjects(), thread, message.getParentThread(), message.getId(), message.getFrom(), message.getLanguage(), message.getError()));
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
