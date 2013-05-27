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

import java.util.regex.Pattern;

public interface Constants {
    Pattern HTTP_STATUS = Pattern.compile("HTTP/1.1 (\\w+)");
    Pattern CONTENT_TYPE = Pattern.compile("Content-Type: (\\S+)");
    String EMPTY_LINE = "";
    String EVENT_STREAM = "text/event-stream";
    String CHARSET = "charset";
    String UTF8 = "utf-8";
    String JSON = "application/json";

    String EVENT = "event";
    String DATA = "data";
    String RETRY = "retry";
    String ID = "id";

    String COLON = ":";
    char NEW_LINE= '\n';
    String BRACKET = "{";
}
