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

import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class WebSocketClientHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    private WebSocketClientHandshaker handshaker;
    private WebSocket webSocket;
    boolean closed = false;
    long reconnectDelay = 3000;
    private Channel channel;
    WebSocketNotification notification;
    URI uri;
    HttpHeaders customHeaders;


    public WebSocketClientHandler(URI uri, WebSocketNotification notification, WebSocket webSocket) {
        customHeaders = new DefaultHttpHeaders();
        customHeaders.add("MyHeader", "MyValue");
        this.uri = uri;

        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        this.handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, false, customHeaders);
        this.webSocket = webSocket;
        this.notification = notification;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
        handshaker.handshake(channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("WebSocket Client disconnected!");
        if (!closed) {
            final EventLoop loop = ctx.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Reconnecting");
                    try {
                        handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, false, customHeaders);
                        webSocket.createBootstrap();
                    } catch (InterruptedException e) {
                        notification.onerror("Unable to reconnect WebSocket");
                    }
                    System.out.println("After Reconnecting");
                }
            }, reconnectDelay, TimeUnit.MILLISECONDS);
        }

    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!handshaker.isHandshakeComplete()) {
            // try catch for on error
            handshaker.finishHandshake(channel, (FullHttpResponse) msg);
            notification.onopen();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            notification.onerror("Unexpected FullHttpResponse (getStatus=" + response.getStatus() + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            notification.onmessage(textFrame.text());
        } else if (frame instanceof CloseWebSocketFrame) {
            channel.close();
            notification.onclose();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        notification.onerror(cause.getMessage());
        ctx.close();
    }

    public void close() {
        this.closed = true;
    }

    protected void finalize() throws Throwable {
        channel.close();
    }
}