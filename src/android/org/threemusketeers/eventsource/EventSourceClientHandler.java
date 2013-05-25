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
package org.threemusketeers.eventsource;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class EventSourceClientHandler extends ChannelInboundMessageHandlerAdapter<Object> {

    URI uri;
    EventSourceNotification notification;
    EventSource eventSource;
    Channel channel;
    EventSourceHandshaker handshaker = new EventSourceHandshaker();
    Message message;
    String lastEventId;
    boolean closed = false;
    long reconnectDelay = 3000;
    String messageType;

    EventSourceClientHandler(URI uri, EventSourceNotification notification, EventSource eventSource) {
        super();
        this.uri = uri;
        this.notification = notification;
        this.eventSource = eventSource;
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        if (!closed) {
            notification.onerror("RECONNECTING");
            final EventLoop loop = ctx.channel().eventLoop();

            // Wait a delay equal to the reconnection time of the event source.            
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    handshaker = new EventSourceHandshaker();
                    // Reconnect
                    eventSource.createBootstrap();
                }
            }, reconnectDelay, TimeUnit.MILLISECONDS);
        }
    }
    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
        FullHttpRequest request =  getConnectHttpRequest();
        channel.write(request);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        String content = (String)msg;
        // First have the hand shake done
        if (!handshaker.isHandshakeComplete()) {
            if (handshaker.continueHandshake(content)) {
                if (handshaker.error != null) {
                    notification.onerror(handshaker.error);
                    message = null;
                } else {
                    if (Constants.EVENT_STREAM.equals(handshaker.contentType)) {
                        messageType = Constants.EVENT_STREAM;
                        message = new EventStreamMessage();
                    } else {
                        messageType = Constants.JSON;
                        message = new JSONMessage();
                    }
                    notification.onopen();
                }
            }
            return;
        }

        // Then when handshake active can parse the content
        if(message.parse(content)) {
            // Set the last event ID string of the event source to value of the last event ID buffer. 
            // The buffer does not get reset, so the last event ID string of the event source remains 
            // set to this value until the next time it is set by the server.            
            if (message.id != null) {
                lastEventId = message.id;
            }
            if (message.retry > 0) {
                reconnectDelay = message.retry;
            }
            notification.onmessage(message);
            if (Constants.EVENT_STREAM.equals(messageType)) {
                message = new EventStreamMessage();
            } else {
                message = new JSONMessage();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        notification.onerror(cause.getMessage());
        ctx.close();
    }

    public void close() {
        this.closed = true;
        if (this.channel == null) {
            return;
        }

        if (this.channel.isOpen()) {
            this.channel.close();
        }
    }

    FullHttpRequest getConnectHttpRequest() {
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toString());
        //the Accept header may be included but this is not mandatory
        request.headers().add(HttpHeaders.Names.ACCEPT, Constants.EVENT_STREAM);
        request.headers().add(HttpHeaders.Names.HOST, uri.getHost());

        int port = uri.getPort();
        if (uri.getScheme().equals("http") && port == 80) {
            port = -1;
        } else if (uri.getScheme().equals("https") && port == 443) {
            port = -1;
        }
        StringBuilder origin = new StringBuilder();
        origin.append(uri.getScheme()).append("://").append(uri.getHost());

        if (port != -1) {
            origin.append(':').append(port);
        }

        request.headers().add(HttpHeaders.Names.ORIGIN, origin);
        //User agents should use the Cache-Control: no-cache header in requests to bypass any caches for requests of event sources
        request.headers().add(HttpHeaders.Names.CACHE_CONTROL, "no-cache");
        //If the event source's last event ID string is not the empty string, 
        //then a Last-Event-ID HTTP header must be included with the request, 
        //whose value is the value of the event source's last event ID string, encoded as UTF-8.
        if (lastEventId != null && !lastEventId.isEmpty()) {
            request.headers().add("Last-Event-ID", lastEventId);
        }
        return request;
    }

    // Check if this is useful in case the person do not close the eventsource 
    // but do not use it anymore
    protected void finalize() throws Throwable {
        this.close();
    }
}