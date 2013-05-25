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
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaInterface;
import org.apache.cordova.api.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.threemusketeers.websocket.WebSocketNotification;

public class CordovaWebSocketNotification implements WebSocketNotification{
    private final CordovaInterface cordova;
    private final CallbackContext callbackContext;

    public CordovaWebSocketNotification(CordovaInterface cordova, CallbackContext callbackContext) {
        this.cordova = cordova;
        this.callbackContext = callbackContext;
    }

    @Override
    public void onopen() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONObject json = new JSONObject();
                    json.put("event", "open");
                    json.put("value", "");
                    PluginResult result = new PluginResult(PluginResult.Status.OK, json);
                    result.setKeepCallback(true);
                    Log.e("BrowserPush", "Sending on open");
                    callbackContext.sendPluginResult(result);
                } catch (JSONException exception) {
                    callbackContext.error(exception.getMessage());
                }
            }
        });
    }

    @Override
    public void onmessage(final String message) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONObject json = new JSONObject();
                    json.put("value", message);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, json);
                    result.setKeepCallback(true);
                    Log.e("BrowserPush", "Sending on message " + message);
                    callbackContext.sendPluginResult(result);
                } catch (JSONException exception) {
                    callbackContext.error(exception.getMessage());
                }
            }
        });
    }

    @Override
    public void onclose() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONObject json = new JSONObject();
                    json.put("value", "");
                    PluginResult result = new PluginResult(PluginResult.Status.OK, json);
                    result.setKeepCallback(true);
                    Log.e("BrowserPush", "Sending on close");
                    callbackContext.sendPluginResult(result);
                } catch (JSONException exception) {
                    callbackContext.error(exception.getMessage());
                }
            }
        });
    }

    @Override
    public void onerror(final String error) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONObject json = new JSONObject();
                    json.put("event", "error");
                    json.put("value", error);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, json);
                    result.setKeepCallback(true);
                    Log.e("BrowserPush", "Sending on error");
                    callbackContext.sendPluginResult(result);
                } catch (JSONException exception) {
                    callbackContext.error(exception.getMessage());
                }
            }
        });
    }


}
