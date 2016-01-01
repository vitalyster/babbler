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

package rocks.xmpp.core.session;

import rocks.xmpp.core.stream.StreamErrorException;
import rocks.xmpp.core.stream.model.errors.Condition;

import java.util.concurrent.TimeUnit;

/**
 * A strategy for reconnection logic, i.e. when and in which interval reconnection attempts will happen. You can provide your own strategy by implementing this interface.
 * <p>
 * Alternatively you can use some of the predefined strategies which you can retrieve by one of the static methods.
 * </p>
 * E.g. {@link #after(long, TimeUnit)} always tries to reconnect after a fix amount of time.
 *
 * @author Christian Schudt
 * @see ReconnectionManager#setReconnectionStrategy(ReconnectionStrategy)
 */
@FunctionalInterface
public interface ReconnectionStrategy {

    default boolean mayReconnect(Throwable e) {
        // By default allow reconnection only if it's not caused by a <conflict/> stream error.
        return !(e instanceof StreamErrorException) || ((StreamErrorException) e).getCondition() != Condition.CONFLICT;
    }

    /**
     * Gets the time (in seconds) until the next reconnection is attempted.
     *
     * @param attempt The current reconnection attempt. The first attempt is 0, the second attempt is 1, etc...
     * @param cause   The cause for the disconnection.
     * @return The number of seconds before the next reconnection is attempted.
     */
    long getNextReconnectionAttempt(int attempt, Throwable cause);

    /**
     * This is the default reconnection strategy used by the {@link rocks.xmpp.core.session.ReconnectionManager}.
     * <p>
     * It exponentially increases the time span from which a random value for the next reconnection attempt is chosen.
     * The formula for doing this, is: <code>(2<sup>n</sup> - 1) * s</code>, where <code>n</code> is the number of reconnection attempt and <code>s</code> is the slot time, which is 60 seconds by default.
     * </p>
     * <p>
     * In practice this means, the first reconnection attempt occurs after a random period of time between 0 and 60 seconds.<br>
     * The second attempt chooses a random number &gt;= 0 and &lt; 180 seconds.<br>
     * The third attempt chooses a random number &gt;= 0 and &lt; 420 seconds.<br>
     * The fourth attempt chooses a random number &gt;= 0 and &lt; 900 seconds.<br>
     * The fifth attempt chooses a random number &gt;= 0 and &lt; 1860 seconds (= 31 minutes)<br>
     * </p>
     * <p>
     * The strategy is called "truncated", because it won't increase the time span after the nth iteration, which means in the example above, the sixth and any further attempt
     * behaves equally to the fifth attempt.
     * </p>
     * This "truncated binary exponential backoff" is the <a href="http://xmpp.org/rfcs/rfc6120.html#tcp-reconnect">recommended reconnection strategy by the XMPP specification</a>.
     *
     * @param slotTime The slot time (in seconds), usually 60.
     * @param ceiling  The ceiling, i.e. when the time is truncated. E.g. if the ceiling is 4, the back off is truncated at the 5th reconnection attempt (it starts at zero).
     * @return The truncated binary exponential backoff strategy.
     */
    static ReconnectionStrategy truncatedBinaryExponentialBackoffStrategy(int slotTime, int ceiling) {
        return new TruncatedBinaryExponentialBackoffStrategy(slotTime, ceiling);
    }

    /**
     * Reconnects always after a fix amount of time, e.g. after 10 seconds.
     *
     * @param duration The fix duration after which a reconnection is attempted.
     * @param timeUnit The time unit.
     * @return The reconnection strategy.
     */
    static ReconnectionStrategy after(long duration, TimeUnit timeUnit) {
        return (attempt, cause) -> timeUnit.toSeconds(duration);
    }
}
