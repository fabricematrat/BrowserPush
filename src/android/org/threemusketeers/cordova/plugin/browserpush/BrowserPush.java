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
package org.threemusketeers.cordova.plugin.browserpush;

import android.util.Log;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.threemusketeers.eventsource.EventSource;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.threemusketeers.websocket.WebSocket;

import java.util.HashMap;
/**
 * Implementation of the EventSource for Cordova Android as it is missing today
 * http://www.w3.org/TR/2012/WD-eventsource-20121023/
 * Implementation of web socket RFC 6455 aka HyBi-17
 */
public class BrowserPush extends CordovaPlugin {

    public static EventLoopGroup group = new NioEventLoopGroup();
    public static HashMap<String, EventSource> sources = new HashMap<String, EventSource>();
    public static HashMap<String, WebSocket> webSockets = new HashMap<String, WebSocket>();

    @Override
    public boolean execute(String action, JSONArray args,
                           final CallbackContext callbackContext) throws JSONException {
        if (action.equals("EventSource.constructor")) {
            EventSource es = new EventSource(args.getString(0), group, new CordovaEventSourceNotification(cordova, callbackContext));
            sources.put(args.getString(1), es);
            return true;
        } else if (action.equals("EventSource.close")) {
            EventSource es = sources.get(args.getString(0));
            if (es != null) {
                es.close();
            } else {
                callbackContext.error("Unable to find EventSource associated");
                Log.d("BrowserPush", "Error finding open source ");
            }
            sources.remove(args.getString(0));
            return true;
        } else if (action.equals("WebSocket.constructor")) {
            WebSocket webSocket = new WebSocket(args.getString(0), group, new CordovaWebSocketNotification(cordova, callbackContext));
            webSockets.put(args.getString(1), webSocket);
            return true;
        } else if (action.equals("WebSocket.close")) {
            WebSocket webSocket = webSockets.get(args.getString(0));
            if (webSocket != null) {
                webSocket.close();
            } else {
                callbackContext.error("Unable to find WebSocket associated");
                Log.d("BrowserPush", "Error finding websocket ");
            }
            webSockets.remove(args.getString(0));
            return true;
        } else if (action.equals("WebSocket.send")) {
            WebSocket webSocket = webSockets.get(args.getString(1));
            if (webSocket != null) {
                webSocket.send(args.getString(0));
            } else {
                callbackContext.error("Unable to find WebSocket associated");
                Log.d("BrowserPush", "Error finding websocket ");
            }
            return true;
        }

        return false;
    }
}




