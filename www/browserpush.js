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
var cordova = window.cordova || window.Cordova || window.PhoneGap;
if (cordova) {
    window.BrowserPush = function() {
        var that = {};

        var supportedEvents = {error: [], open: [], close: [], message: []};

        that.onEvent = function(event, target) {
            if (event.type in supportedEvents) {
                for (var i = 0; i < supportedEvents[event.type].length; i++) {
                    supportedEvents[event.type][i](event);
                }
                var fullName = 'on' + event.type;
                if (target[fullName]) {
                    target[fullName](event);
                }
            }
        };

        that.addEventListener =  function (eventName, f) {
            if (eventName in supportedEvents) {
                supportedEvents[eventName].push(f);
            } else {
                supportedEvents[eventName] = [];
                supportedEvents[eventName].push(f)
            }
        };

        that.removeEventListener = function(eventName, f) {
            if (eventName in supportedEvents) {
                var index = supportedEvents[eventName].indexOf(f);
                supportedEvents[eventName].splice(index, 1);
            }
        };

        that.generateId = function () {
            var uuid = '', i, random;
            for (i = 0; i < 32; i++) {
                random = Math.random() * 16 | 0;

                if (i === 8 || i === 12 || i === 16 || i === 20) {
                    uuid += '-';
                }
                uuid += (i === 12 ? 4 : (i === 16 ? (random & 3 | 8) : random)).toString(16);
            }
            return uuid;
        };

        that.qualifyURL = function(url) {
            var a = document.createElement('a');
            if (url === null) {
                a.href = "";
                a.href += "/null";
            } else {
                a.href = url;
            }
            return a.href;
        };

        return that;
    };

    if (!window.EventSource) {
        window.EventSource = function (url) {
            var that = this;
            var lastSeenId = '';

            var bp = new window.BrowserPush();
            var uuid = bp.generateId();

            Object.defineProperty(that, 'url', {
                enumerable: false,
                configurable: false,
                writable: false,
                value: bp.qualifyURL(url)
            });

            var readyStateValue = EventSource.CONNECTING;
            Object.defineProperty(that, 'readyState', {get : function(){ return readyStateValue; }});

            var onEvent = bp.onEvent;

            var propagateEvent = function(data) {
                var event = document.createEvent('Event');
                event.initEvent(data.event, false, false);
                event.currentTarget = this;
                event.target = this;
                event.srcElement = this;
                event.eventPhase = 2;
                if (data.event == 'error') {
                    if (data.value == 'RECONNECTING') {
                        readyStateValue = EventSource.CONNECTING;
                    } else {
                        readyStateValue = EventSource.CLOSED;
                    }
                } else if (data.event == 'open') {
                    readyStateValue = EventSource.OPEN;
                } else {
                    event.data = data.value;
                    if (data.id) {
                        lastSeenId = data.id;
                    }
                    event.lastEventId = lastSeenId;
                }
                onEvent(event, that);
            };

            cordova.exec(propagateEvent, propagateEvent, 'BrowserPush', 'EventSource.constructor', [that.url, uuid]);

            that.addEventListener = bp.addEventListener;

            that.removeEventListener = bp.removeEventListener;

            that.close = function() {
                readyStateValue = EventSource.CLOSED;
                cordova.exec(null, null, 'BrowserPush', 'EventSource.close', [uuid]);
            };

            return that;
        };

        window.EventSource.CONNECTING = 0;
        window.EventSource.OPEN = 1;
        window.EventSource.CLOSED = 2;

    }


    if (!window.WebSocket) {
        window.WebSocket = function (url) {
            var that = this;

            var bp = new window.BrowserPush();
            var uuid = bp.generateId();

            Object.defineProperty(that, 'url', {
                enumerable: false,
                configurable: false,
                writable: false,
                value: bp.qualifyURL(url)
            });

            var onEvent = bp.onEvent;
            cordova.exec(onEvent, onEvent, 'BrowserPush', 'WebSocket.constructor', [url, uuid]);

            that.addEventListener = bp.addEventListener;

            that.removeEventListener = bp.removeEventListener;

            that.close = function() {
                cordova.exec(onEvent, onEvent, 'BrowserPush', 'WebSocket.close', [uuid]);
            };

            that.send = function(data) {
                cordova.exec(onEvent, onEvent, 'BrowserPush', 'WebSocket.send', [data, uuid]);
            };

            return that;
        };

        window.WebSocket.CONNECTING = 0;
        window.WebSocket.OPEN = 1;
        window.WebSocket.CLOSING = 2;
        window.WebSocket.CLOSED = 3;

    }
}