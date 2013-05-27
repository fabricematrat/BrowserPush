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

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.CharsetUtil;

import java.net.URI;

/**
 * Implementation of the EventSource java client compliant with
 * http://www.w3.org/TR/2012/WD-eventsource-20121023/
 */
public class EventSource {

    private EventLoopGroup group;
    private Bootstrap bootstrap;
    private URI uri;
    private EventSourceClientHandler handler;

    public EventSource(String url, EventLoopGroup group, EventSourceNotification notification) {
        this.group = group;
        this.uri = URI.create(url);
        String protocol = uri.getScheme();
        if (!"http".equals(protocol) && !"https".equals(protocol)) {
            notification.onerror("Unsupported protocol: " + protocol + " for URL " + url);
            return;
        }

        handler = new EventSourceClientHandler(uri, notification, this);
        createBootstrap();
    }

    public void close() {
        if (handler != null) {
            handler.close();
        }
    }

    void createBootstrap() {
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();

                        //Lines must be separated by either a U+000D CARRIAGE RETURN U+000A LINE FEED (CRLF) character pair, 
                        //a single U+000A LINE FEED (LF) character, 
                        //or a single U+000D CARRIAGE RETURN (CR) character.
                        p.addLast(new HttpRequestEncoder(),
                                  new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, new ByteBuf[] {
                                          Unpooled.wrappedBuffer(new byte[]{'\r', '\n'}),
                                          Unpooled.wrappedBuffer(new byte[] { '\n' }),
                                          Unpooled.wrappedBuffer(new byte[] { '\r' })}),
                                  new StringDecoder(CharsetUtil.UTF_8),
                                  handler);
                    }
                });

        int port = uri.getPort();
        if(port <= 0) {
            String protocol = uri.getScheme();
            if ("http".equals(protocol)) {
                port = 80;
            } else {
                port = 443;
            }
        }
        bootstrap.connect(uri.getHost(), port);
    }

    protected void finalize() throws Throwable {
        this.close();
    }
}
