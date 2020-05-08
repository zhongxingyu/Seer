 /*
     Copyright (C) Fang Yidong <fangyidong@yahoo.com.cn>
 
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
 
         http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
 */
 package com.crashnote.external.json;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * A JSON array. JSONObject supports java.util.List interface.
  *
  * @author FangYidong<fangyidong@yahoo.com.cn>
  */
@SuppressWarnings("unchecked")
 public class JSONArray
         extends ArrayList<Object> implements List<Object>, JSONAware, JSONStreamAware {
 
     private static final long serialVersionUID = 3957988303675231981L;
 
     /**
      * Encode a list into JSON text and write it to out.
      * If this list is also a JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific behaviours will be ignored at this top level.
      *
      * @param list
      * @param out
      * @see JSONValue#writeJSONString(Object, Writer)
      */
     public static void writeJSONString(final List list, final Writer out) throws IOException {
         if (list == null) {
             out.write("null");
             return;
         }
 
         boolean first = true;
         final Iterator iter = list.iterator();
 
         out.write('[');
         while (iter.hasNext()) {
             if (first)
                 first = false;
             else
                 out.write(',');
 
             final Object value = iter.next();
             if (value == null) {
                 out.write("null");
                 continue;
             }
 
             JSONValue.writeJSONString(value, out);
         }
         out.write(']');
     }
 
     @Override
     public void writeJSONString(final Writer out) throws IOException {
         writeJSONString(this, out);
     }
 
     /**
      * Convert a list to JSON text. The result is a JSON array.
      * If this list is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
      *
      * @param list
      * @return JSON text, or "null" if list is null.
      * @see JSONValue#toJSONString(Object)
      */
     public static String toJSONString(final List list) {
         if (list == null)
             return "null";
 
         boolean first = true;
         final StringBuffer sb = new StringBuffer();
         final Iterator iter = list.iterator();
 
         sb.append('[');
         while (iter.hasNext()) {
             if (first)
                 first = false;
             else
                 sb.append(',');
 
             final Object value = iter.next();
             if (value == null) {
                 sb.append("null");
                 continue;
             }
             sb.append(JSONValue.toJSONString(value));
         }
         sb.append(']');
         return sb.toString();
     }
 
     @Override
     public String toJSONString() {
         return toJSONString(this);
     }
 
     public String toString() {
         return toJSONString();
     }
 
 
 }
