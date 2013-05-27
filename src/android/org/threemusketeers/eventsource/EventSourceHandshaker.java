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

import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.*;
import java.util.regex.Matcher;

public class EventSourceHandshaker {
    boolean handshakeStarted;
    String error;
    String contentType = Constants.EVENT_STREAM;
    boolean complete = false;

    public boolean continueHandshake(String content) {
        if (!Constants.EMPTY_LINE.equals(content)) {
            if(!handshakeStarted) {
                // Find the http status OK

                Matcher m = Constants.HTTP_STATUS.matcher(content);
                if (m.find()) {
                    int httpStatus = Integer.parseInt(m.group(1));
                    // HTTP 305 Use Proxy, 401 Unauthorized, and 407 Proxy Authentication Required should be treated
                    // transparently as for any other subresource.
                    // HTTP 301 Moved Permanently, 302 Found, 303 See Other, and 307 Temporary Redirect responses are
                    // handled by the fetching and CORS algorithms. In the case of 301 redirects, 
                    // the user agent must also remember the new URL so that subsequent requests for this resource 
                    // for this EventSource object start with the URL given for the last 301 seen for requests for this object.
                    // Any other HTTP response code not listed here must cause the user agent to fail the connection.
                    // TOBE IMPLEMENTED
                    if (httpStatus == HttpResponseStatus.OK.code()){
                        handshakeStarted = true;
                    } else {
                        error = "HTTP Status is incorrect " + httpStatus;
                        complete = true;
                        return true;
                    }
                }
            } else {
                Matcher m = Constants.CONTENT_TYPE.matcher(content);
                if (m.find()) {
                    // Find the MIME Type javax.activation.MimeType does not exist in android
                    String contentTypeFound = m.group(1);
                    String basetype = null;
                    String subtype = null;
                    String[] mimeTypeSplitted = contentTypeFound.split(";");
                    if (mimeTypeSplitted.length > 0) {
                        String mime = mimeTypeSplitted[0].toLowerCase(Locale.ENGLISH);
                        mime = mime.replace("\\", "");
                        if (!mime.contains("/")) {
                            error =  "Unsupported Content Type " + contentTypeFound;
                            complete = true;
                            return true;
                        } else {
                            String[] split = mime.split("/");
                            basetype = split[0];
                            subtype = split[1];
                        }
                    }

                    ArrayList<String> parameters = new ArrayList<String>(Arrays.asList(mimeTypeSplitted));
                    HashMap<String, String> parametersFormatted = new HashMap<String, String>();
                    parameters.remove(0);
                    for (Iterator<String> iterator = parameters.iterator(); iterator.hasNext();) {
                        String param = iterator.next();
                        String[] splitParam = param.split("=");
                        if (splitParam.length == 2) {
                            String name = splitParam[0];
                            name = name.toLowerCase(Locale.ENGLISH);
                            String value = splitParam[1];
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length());
                                value = value.substring(0, value.length() - 1);
                            }
                            value = value.toLowerCase(Locale.ENGLISH);
                            parametersFormatted.put(name, value);
                        }
                    }
                    
                    if (Constants.EVENT_STREAM.equals(basetype + "/" + subtype)) {
                        contentType = Constants.EVENT_STREAM;
                        String charSetValue = parametersFormatted.get(Constants.CHARSET);
                        if (charSetValue != null) {
                            if (!charSetValue.equals(Constants.UTF8)) {
                                error =  "Unsupported Content Type " + contentTypeFound;
                                complete = true;
                                return true;                                
                            }
                        }
                    } else if (Constants.JSON.equals(contentTypeFound)) {
                        contentType = Constants.JSON;
                    } else {
                        //error
                        error = "Unsupported Content Type " + contentTypeFound;
                        complete = true;
                        return true;
                    }
                }
            }
            return false;
        } else {
            // Check an empty line which means the end of the header
            if (!handshakeStarted) {
                error = "Unable to get an HTTP Status for the connection";
            }
            complete = true;
            return true;
        }
    }

    public boolean isHandshakeComplete() {
        return complete;
    }
}
