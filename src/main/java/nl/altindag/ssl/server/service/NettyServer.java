/*
 * Copyright 2023 Thunderberry.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.altindag.ssl.server.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.server.exception.ServerException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.X509ExtendedKeyManager;

import static io.netty.channel.ChannelFutureListener.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Hakan Altindag
 */
class NettyServer implements Server {

    private final ChannelFuture httpChannel;
    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;

    NettyServer(SSLFactory sslFactory, int port, String responseBody) {
        X509ExtendedKeyManager keyManager = sslFactory.getKeyManager()
                .orElseThrow(NullPointerException::new);

        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(keyManager)
                .ciphers(sslFactory.getCiphers(), SupportedCipherSuiteFilter.INSTANCE)
                .protocols(sslFactory.getProtocols())
                .clientAuth(getClientAuth(sslFactory.getSslParameters()));

        sslFactory.getTrustManager().ifPresent(sslContextBuilder::trustManager);

        try {
            SslContext sslContext = sslContextBuilder.build();

            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            serverBootstrap.group(bossGroup, workerGroup)
                    .channelFactory(NioServerSocketChannel::new)
                    .childHandler(new ServerInitializer(sslContext, responseBody));

            httpChannel = serverBootstrap.bind(port).sync();
        } catch (SSLException | InterruptedException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public void stop() {
        httpChannel.channel().close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    private static class ServerInitializer extends ChannelInitializer<Channel> {

        private final SslContext sslContext;
        private final String responseBody;

        public ServerInitializer(SslContext sslContext, String responseBody) {
            this.sslContext = sslContext;
            this.responseBody = responseBody;
        }

        @Override
        protected void initChannel(Channel channel) {
            channel.pipeline()
                    .addFirst("ssl", new SslHandler(sslContext.newEngine(channel.alloc())))
                    .addLast(new HttpServerCodec())
                    .addLast(new ServerHandler(responseBody));
        }

    }

    private static class ServerHandler extends ChannelInboundHandlerAdapter {

        private final String responseBody;

        public ServerHandler(String responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext channelHandlerContext) {
            channelHandlerContext.flush();
        }

        @Override
        public void channelRead(ChannelHandlerContext channelHandlerContext, Object message) {
            if (message instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) message;

                if (HttpUtil.is100ContinueExpected(request)) {
                    channelHandlerContext.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                }
                boolean keepAlive = HttpUtil.isKeepAlive(request);
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(responseBody.getBytes(UTF_8)));
                response.headers().set(CONTENT_TYPE, "text/plain");
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

                if (!keepAlive) {
                    channelHandlerContext.write(response).addListener(CLOSE);
                } else {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                    channelHandlerContext.write(response);
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
            throwable.printStackTrace();
            channelHandlerContext.close();
        }

    }

    static ClientAuth getClientAuth(SSLParameters sslParameters) {
        if (sslParameters.getNeedClientAuth()) {
            return ClientAuth.REQUIRE;
        } else if (sslParameters.getWantClientAuth()) {
            return ClientAuth.OPTIONAL;
        } else {
            return ClientAuth.NONE;
        }
    }

}
