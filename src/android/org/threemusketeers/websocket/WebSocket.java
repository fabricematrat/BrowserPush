/** Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *
 * @author <a href='mailto:th33musk3t33rs@gmail.com'>3.musket33rs</a>
 *
 * @since 0.1
 */
package org.threemusketeers.websocket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.net.URI;

public class WebSocket {
    private EventLoopGroup group;
    private URI uri;
    private WebSocketNotification notification;
    private WebSocketClientHandler handler;
    private Bootstrap bootstrap;
    private Channel channel;

    public WebSocket(String url, EventLoopGroup group, WebSocketNotification notification) {
        this.group = group;
        uri = URI.create(url);
        this.notification = notification;
        String protocol = uri.getScheme();
        if (!"ws".equals(protocol)) {
            notification.onerror("Unsupported protocol: " + protocol);
        }

        handler = new WebSocketClientHandler(uri, notification, this);
        try {
            createBootstrap();
        } catch (InterruptedException e) {
            channel.close();
            notification.onerror("Unable to create WebSocket");
        }
    }

    void createBootstrap() throws InterruptedException {
        bootstrap = new Bootstrap();
        bootstrap.group(group)
           .channel(NioSocketChannel.class)
           .handler(new ChannelInitializer<SocketChannel>() {
               @Override
               public void initChannel(SocketChannel ch) throws Exception {
                   ChannelPipeline pipeline = ch.pipeline();
                   pipeline.addLast("http-codec", new HttpClientCodec());
                   pipeline.addLast("aggregator", new HttpObjectAggregator(8192));
                   pipeline.addLast("ws-handler", handler);
               }
           });

        channel = bootstrap.connect(uri.getHost(), uri.getPort()).sync().channel();
    }

    public void close() {
        channel.write(new CloseWebSocketFrame());
        handler.close();

        // WebSocketClientHandler will close the connection when the server
        // responds to the CloseWebSocketFrame.
        try {
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            notification.onerror("Unable to close WebSocket");
        }
    }

    public boolean send(String data) {
        channel.write(new TextWebSocketFrame(data));
        return true;
    }

}
