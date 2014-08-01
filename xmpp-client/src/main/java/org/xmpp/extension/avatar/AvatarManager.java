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

package org.xmpp.extension.avatar;

import org.xmpp.*;
import org.xmpp.extension.ExtensionManager;
import org.xmpp.extension.avatar.vcard.AvatarUpdate;
import org.xmpp.extension.muc.user.MucUser;
import org.xmpp.extension.vcard.VCard;
import org.xmpp.extension.vcard.VCardManager;
import org.xmpp.stanza.PresenceEvent;
import org.xmpp.stanza.PresenceListener;
import org.xmpp.stanza.StanzaException;
import org.xmpp.stanza.client.Presence;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages avatar updates as described in <a href="http://xmpp.org/extensions/xep-0153.html">XEP-0153: vCard-Based Avatars</a>.
 * <p>
 * A future implementation will also manage <a href="http://xmpp.org/extensions/xep-0084.html">XEP-0084: User Avatar</a>
 * </p>
 *
 * @author Christian Schudt
 */
public final class AvatarManager extends ExtensionManager {

    private static final Logger logger = Logger.getLogger(VCardManager.class.getName());

    private final Map<byte[], Avatar> avatars = new ConcurrentHashMap<>();

    private final Map<Jid, byte[]> userAvatars = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Jid, Lock> requestingAvatarLocks = new ConcurrentHashMap<>();

    private final Set<AvatarChangeListener> avatarChangeListeners = new CopyOnWriteArraySet<>();

    private final Executor avatarRequester;

    private AvatarManager(final XmppSession xmppSession) {
        super(xmppSession);

        avatarRequester = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "Avatar Request Thread");
                thread.setDaemon(true);
                return thread;
            }
        });
        xmppSession.addConnectionListener(new ConnectionListener() {
            @Override
            public void statusChanged(ConnectionEvent e) {
                if (e.getStatus() == XmppSession.Status.CLOSED) {
                    avatarChangeListeners.clear();
                    avatars.clear();
                    userAvatars.clear();
                }
            }
        });
        xmppSession.addPresenceListener(new PresenceListener() {
            @Override
            public void handle(PresenceEvent e) {
                final VCardManager vCardManager = xmppSession.getExtensionManager(VCardManager.class);
                // If vCard base avatars are enabled.
                if (vCardManager != null && vCardManager.isEnabled()) {
                    final Presence presence = e.getPresence();
                    final AvatarUpdate avatarUpdate = presence.getExtension(AvatarUpdate.class);
                    // If the presence has a avatar update information.
                    if (e.isIncoming() && presence.getExtension(MucUser.class) == null && avatarUpdate != null && avatarUpdate.getHash() != null) {
                        avatarRequester.execute(new Runnable() {
                            @Override
                            public void run() {

                                // Check, if we already know this avatar.
                                Avatar avatar = avatars.get(avatarUpdate.getHash());
                                // If the hash is unknown.
                                Jid contact = presence.getFrom().asBareJid();
                                if (avatar == null) {
                                    try {
                                        // Get the avatar for this user.
                                        avatar = getAvatar(contact);
                                    } catch (XmppException e1) {
                                        logger.log(Level.WARNING, e1.getMessage(), e1);
                                    }
                                }
                                // If the avatar was either known before or could be successfully retrieved from the vCard.
                                if (avatar != null) {
                                    byte[] hash = userAvatars.get(contact);
                                    // Compare the old hash and the new hash. If both are different notify listeners.
                                    if (!Arrays.equals(hash, avatarUpdate.getHash())) {
                                        for (AvatarChangeListener avatarChangeListener : avatarChangeListeners) {
                                            try {
                                                avatarChangeListener.avatarChanged(new AvatarChangeEvent(AvatarManager.this, contact, avatar));
                                            } catch (Exception e1) {
                                                logger.log(Level.WARNING, e1.getMessage(), e1);
                                            }
                                        }
                                        // Then store the new hash for that user.
                                        userAvatars.put(contact, avatarUpdate.getHash());
                                    }
                                }
                            }
                        });
                    } else if (!e.isIncoming() && vCardManager.isEnabled() && presence.isAvailable() && presence.getTo() == null) {
                        // 1. If a client supports the protocol defined herein, it MUST include the update child element in every presence broadcast it sends and SHOULD also include the update child in directed presence stanzas.

                        // 2. If a client is not yet ready to advertise an image, it MUST send an empty update child element, i.e.:
                        final Jid me = xmppSession.getConnectedResource().asBareJid();
                        byte[] myHash = userAvatars.get(me);
                        if (myHash == null) {
                            // Load my own avatar in order to advertise an image.
                            avatarRequester.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        getAvatar(null);
                                        // If the client subsequently obtains an avatar image (e.g., by updating or retrieving the vCard), it SHOULD then publish a new <presence/> stanza with character data in the <photo/> element.
                                        // As soon as the vCard has been loaded, broadcast presence, in order to update the avatar.
                                        Presence presence = xmppSession.getPresenceManager().getLastSentPresence();
                                        if (presence == null) {
                                            presence = new Presence();
                                        }
                                        presence.getExtensions().clear();
                                        xmppSession.send(presence);
                                    } catch (XmppException e1) {
                                        logger.log(Level.WARNING, e1.getMessage(), e1);
                                    }
                                }
                            });
                            // Append an empty element, to indicate, we are not yet ready (vCard is being loaded).
                            presence.getExtensions().add(new AvatarUpdate());
                        } else {
                            try {
                                VCard vCard = vCardManager.getVCard();
                                // 3. If there is no avatar image to be advertised, the photo element MUST be empty
                                byte[] currentHash = new byte[0];
                                // If we have a avatar, include its hash.
                                if (vCard != null && vCard.getPhoto() != null && vCard.getPhoto().getValue() != null) {
                                    currentHash = getHash(vCard.getPhoto().getValue());
                                }
                                presence.getExtensions().add(new AvatarUpdate(currentHash));
                            } catch (XmppException e1) {
                                logger.log(Level.WARNING, e1.getMessage(), e1);
                            }
                        }
                    }
                }
            }
        });
    }

    private byte[] getHash(byte[] photo) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("sha-1");
            messageDigest.reset();
            messageDigest.update(photo);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return new byte[0];
    }

    /**
     * Gets the user avatar.
     *
     * @param user The user.
     * @return The user's avatar or null, if it has no avatar.
     * @throws StanzaException     If the entity returned a stanza error.
     * @throws NoResponseException If the entity did not respond.
     */
    public Avatar getAvatar(Jid user) throws XmppException {
        Avatar avatar = new Avatar(null, null);
        if (user != null) {
            user = user.asBareJid();
        } else {
            user = xmppSession.getConnectedResource().asBareJid();
        }

        Lock lock = new ReentrantLock();
        Lock existingLock = requestingAvatarLocks.putIfAbsent(user, lock);
        if (existingLock != null) {
            lock = existingLock;
        }

        lock.lock();
        try {
            // Let's see, if there's a stored image already.
            byte[] hash = userAvatars.get(user);
            if (hash != null) {
                return avatars.get(hash);
            } else {
                // If there's no avatar for that user, load it.
                VCardManager vCardManager = xmppSession.getExtensionManager(VCardManager.class);
                hash = new byte[0];

                // Load the vCard for that user
                VCard vCard;
                if (user.equals(xmppSession.getConnectedResource().asBareJid())) {
                    vCard = vCardManager.getVCard();
                } else {
                    vCard = vCardManager.getVCard(user);
                }

                if (vCard != null) {
                    // And check if it has a photo.
                    VCard.Image image = vCard.getPhoto();
                    if (image != null && image.getValue() != null) {
                        hash = getHash(image.getValue());
                        if (hash != null) {
                            avatar = new Avatar(image.getType(), image.getValue());
                        }
                    }
                }
                userAvatars.put(user, hash);
                avatars.put(hash, avatar);
            }
            return avatar;
        } finally {
            lock.unlock();
            requestingAvatarLocks.remove(user);
        }
    }

    /**
     * Adds an avatar listener, to listen for avatar updates.
     *
     * @param avatarChangeListener The avatar listener.
     */
    public void addAvatarChangeListener(AvatarChangeListener avatarChangeListener) {
        avatarChangeListeners.add(avatarChangeListener);
    }

    /**
     * Removes a previously added avatar listener.
     *
     * @param avatarChangeListener The avatar listener.
     */
    public void removeAvatarChangeListener(AvatarChangeListener avatarChangeListener) {
        avatarChangeListeners.remove(avatarChangeListener);
    }
}
