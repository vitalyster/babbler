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

package org.xmpp.extension.rosterx;

import org.xmpp.Jid;

import java.util.Date;
import java.util.EventObject;
import java.util.List;

/**
 * This event notifies listeners, when an entity suggests to add, delete or modify a contact.
 *
 * @author Christian Schudt
 * @see org.xmpp.extension.rosterx.RosterExchangeListener
 */
public final class RosterExchangeEvent extends EventObject {

    private final List<RosterExchange.Item> items;

    private final String message;

    private final Jid from;

    private final Date date;

    RosterExchangeEvent(Object source, List<RosterExchange.Item> items, Jid from, String message, Date date) {
        super(source);
        this.items = items;
        this.message = message;
        this.from = from;
        this.date = date;
    }

    /**
     * Gets the optional message, which has been sent together with the roster item exchange.
     *
     * @return The message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the roster exchange items.
     *
     * @return The roster exchange items.
     */
    public List<RosterExchange.Item> getItems() {
        return items;
    }

    /**
     * Gets the sender of the roster item exchange.
     *
     * @return The sender.
     */
    public Jid getFrom() {
        return from;
    }

    /**
     * Gets the send date.
     *
     * @return The send date.
     */
    public Date getDate() {
        return date;
    }
}
