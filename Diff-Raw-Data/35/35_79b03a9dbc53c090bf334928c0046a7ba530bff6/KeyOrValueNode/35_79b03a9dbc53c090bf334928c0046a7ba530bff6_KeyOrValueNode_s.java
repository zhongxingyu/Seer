 /**
  * Copyright 2013 Huining (Thomas) Feng (tfeng@berkeley.edu)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.bacoder.parser.javaproperties.api;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.bacoder.parser.core.DumpTextWithToString;
 import com.bacoder.parser.core.TextNode;
 
 @DumpTextWithToString
 public abstract class KeyOrValueNode extends TextNode {
 
   private static final String[][] SPECIAL_CHAR_MAP = {
     {"\\ ", " "},
     {"\\\t", "\t"},
     {"\\\f", "\f"},
     {"\\\r", "\r"},
     {"\\\n", "\n"},
     {"\\!", "!"},
     {"\\#", "#"}
   };
 
   private static final Pattern NEW_LINE_PATTERN = Pattern.compile("(\\n|\\r\\n|\\r)\\s*");
 
   private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9]{4})");
 
   public String getSanitizedText() {
     String result = getText();
     for (String[] part : SPECIAL_CHAR_MAP) {
       result = result.replace(part[0], part[1]);
     }
 
     Matcher matcher = NEW_LINE_PATTERN.matcher(result);
     if (matcher.find()) {
       result = matcher.replaceAll("");
     }
 
     matcher = UNICODE_PATTERN.matcher(result);
     if (matcher.find()) {
       StringBuffer buffer = new StringBuffer();
       do {
         String unicode = Character.toString((char) Integer.parseInt(matcher.group(1), 16));
         matcher.appendReplacement(buffer, unicode);
       } while (matcher.find());
       matcher.appendTail(buffer);
       result = buffer.toString();
     }
 
     return result;
   }
 
   @Override
   public String toString() {
     return getSanitizedText();
   }
 }
