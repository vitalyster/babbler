/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2018 Christian Schudt
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

package rocks.xmpp.nio.netty.net;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLOutputFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import rocks.xmpp.core.Session;
import rocks.xmpp.core.net.ConnectionConfiguration;
import rocks.xmpp.core.net.ReaderInterceptor;
import rocks.xmpp.core.net.TcpConnection;
import rocks.xmpp.core.net.WriterInterceptor;
import rocks.xmpp.core.session.model.SessionOpen;
import rocks.xmpp.core.stream.StreamHandler;
import rocks.xmpp.core.stream.model.StreamElement;
import rocks.xmpp.core.stream.model.StreamHeader;
import rocks.xmpp.util.XmppStreamEncoder;

/**
 * A NIO connection based on Netty.
 *
 * @author Christian Schudt
 */
public class NettyChannelConnection extends TcpConnection {

    protected final Channel channel;

    private final NettyXmppDecoder decoder;

    protected SessionOpen sessionOpen;

    public NettyChannelConnection(final Channel channel,
                                  final StreamHandler streamHandler,
                                  final Session session,
                                  final List<ReaderInterceptor> readerInterceptors,
                                  final Function<Locale, Unmarshaller> unmarshallerSupplier,
                                  final List<WriterInterceptor> writerInterceptors,
                                  final Supplier<Marshaller> marshallerSupplier,
                                  final Consumer<Throwable> onException,
                                  final ConnectionConfiguration connectionConfiguration) {
        super(connectionConfiguration, streamHandler, onException);
        this.channel = channel;
        this.decoder = new NettyXmppDecoder(this::handleElement, readerInterceptors, unmarshallerSupplier, onException,
                session, this);
        List<WriterInterceptor> interceptors = new ArrayList<>(writerInterceptors);
        interceptors.add(new XmppStreamEncoder(XMLOutputFactory.newFactory(), marshallerSupplier, s -> false));
        channel.pipeline().addLast(decoder, new NettyXmppEncoder(interceptors, onException, session, this));
    }

    /**
     * Converts a {@link Future} to a {@link CompletableFuture}.
     *
     * @param future The Netty Future.
     * @param <T>    The type.
     * @return The {@link CompletableFuture}.
     */
    public static <T> CompletableFuture<T> completableFutureFromNettyFuture(final Future<T> future) {
        final CompletableFuture<T> completableFuture = new CompletableFuture<>();
        future.addListener(f -> {
            if (f.isSuccess()) {
                completableFuture.complete(future.getNow());
            } else {
                completableFuture.completeExceptionally(future.cause());
            }
        });
        return completableFuture;
    }

    @Override
    public final InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    @Override
    public final CompletionStage<Void> open(final SessionOpen sessionOpen) {
        this.sessionOpen = sessionOpen;
        return send(sessionOpen);
    }

    @Override
    public final CompletionStage<Void> send(final StreamElement streamElement) {
        return write(streamElement, channel::writeAndFlush);
    }

    @Override
    public final CompletionStage<Void> write(final StreamElement streamElement) {
        return write(streamElement, channel::write);
    }

    private CompletionStage<Void> write(final StreamElement streamElement,
                                        final Function<StreamElement, ChannelFuture> writeFunction) {
        if (!isClosed() || streamElement == StreamHeader.CLOSING_STREAM_TAG) {
            return completableFutureFromNettyFuture(writeFunction.apply(streamElement));
        } else {
            final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            completableFuture.completeExceptionally(new IllegalStateException("Connection closed"));
            return completableFuture;
        }
    }

    @Override
    public final void flush() {
        channel.flush();
    }

    @Override
    public void secureConnection() throws Exception {
        final SSLContext sslContext = getConfiguration().getSSLContext();
        SslContext sslCtx = new JdkSslContext(sslContext, false, ClientAuth.NONE);
        final SslHandler handler = new SslHandler(sslCtx.newEngine(channel.alloc()), true);
        channel.pipeline().addFirst("SSL", handler);
    }

    /**
     * Compresses the connection.
     *
     * @param method    The compression method. Supported methods are: "zlib", "deflate" and "gzip".
     * @param onSuccess Invoked after the compression method has been chosen, but before compression is applied.
     * @throws IllegalArgumentException If the compression method is unknown.
     */
    @Override
    public final void compressConnection(final String method, final Runnable onSuccess) {
        final ZlibWrapper zlibWrapper;
        switch (method) {
            case "zlib":
                zlibWrapper = ZlibWrapper.ZLIB;
                break;
            case "deflate":
                zlibWrapper = ZlibWrapper.NONE;
                break;
            case "gzip":
                zlibWrapper = ZlibWrapper.GZIP;
                break;
            default:
                throw new IllegalArgumentException("Compression method '" + method + "' not supported");
        }
        if (onSuccess != null) {
            onSuccess.run();
        }
        final ChannelHandler channelHandler = channel.pipeline().get("SSL");
        if (channelHandler != null) {
            channel.pipeline().addAfter("SSL", "decompressor", new JdkZlibDecoder(zlibWrapper));
            channel.pipeline().addAfter("SSL", "compressor", new JdkZlibEncoder(zlibWrapper));
        } else {
            channel.pipeline().addFirst("decompressor", new JdkZlibDecoder(zlibWrapper));
            channel.pipeline().addFirst("compressor", new JdkZlibEncoder(zlibWrapper));
        }
    }

    @Override
    public final boolean isSecure() {
        return channel.pipeline().toMap().containsKey("SSL");
    }

    @Override
    protected void restartStream() {
        decoder.restart();
    }

    @Override
    public final CompletionStage<Void> closeFuture() {
        return completableFutureFromNettyFuture(channel.closeFuture());
    }

    @Override
    protected CompletionStage<Void> closeStream() {
        return send(StreamHeader.CLOSING_STREAM_TAG);
    }

    @Override
    protected CompletionStage<Void> closeConnection() {
        return completableFutureFromNettyFuture(channel.close());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TCP connection to ").append(channel.remoteAddress());
        final String streamId = getStreamId();
        if (streamId != null) {
            sb.append(" (").append(streamId).append(')');
        }
        sb.append(" using io.netty.channel.Channel");
        return sb.toString();
    }
}
