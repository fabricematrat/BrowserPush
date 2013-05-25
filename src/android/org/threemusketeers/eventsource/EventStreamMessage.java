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

public class EventStreamMessage extends Message {

    StringBuilder builder = null;

    public boolean parse(String content) {
        if (Constants.EMPTY_LINE.equals(content)) {
            if (builder != null) {
                // If the data buffer's last character is a U+000A LINE FEED (LF) character, 
                // then remove the last character from the data buffer.
                if (builder.length() > 0 && Constants.NEW_LINE == builder.charAt(builder.length()-1)) {
                    builder.deleteCharAt(builder.length()-1);
                }
                data = builder.toString();
                return true;
            } else {
                // Do nothing if this is just an empty line and nothing has been sent
                return false;
            }
        }

        // If the line starts with a U+003A COLON character (:)
        // Ignore the line.
        if (content.startsWith(Constants.COLON)) {
            return false;
        }

        // If the line contains a U+003A COLON character (:)
        // Collect the characters on the line before the first U+003A COLON character (:), and let field be that string.
        // Collect the characters on the line after the first U+003A COLON character (:), and let value be that string. 
        String[] values = content.split(Constants.COLON, 2);
        if (values.length == 2) {
            //If value starts with a U+0020 SPACE character, remove it from value.
                buildMessage(values[0], values[1].replaceFirst("^ ", ""));
        } else {
            // Use the whole line as the field name, and the empty string as the field value
            buildMessage(content, "");
        }

        return false;
    }

    void buildMessage(String key, String value) {

        if (Constants.DATA.equals(key)) {
            // If the field name is "data"
            // Append the field value to the data buffer, then append a single U+000A LINE FEED (LF) character to the data buffer.            
            if (builder == null) {
                builder = new StringBuilder();
            }
            builder.append(value).append(Constants.NEW_LINE);
        } else if (Constants.RETRY.equals(key)) {
            // If the field name is "retry"
            // If the field value consists of only ASCII digits, then interpret the field value as an integer 
            // in base ten, and set the event stream's reconnection time to that integer. Otherwise, ignore the field.
            try {
                retry = Integer.parseInt(value);
            } catch(NumberFormatException e){
            }
        } else if (Constants.ID.equals(key)) {
            // If the field name is "id"
            // Set the last event ID buffer to the field value.            
            id = value;
        } else if (Constants.EVENT.equals(key)) {
            // If the field name is "event"
            // Set the event type buffer to field value.
            event = value;
        }
        // Otherwise
        // The field is ignored.
    }
}
