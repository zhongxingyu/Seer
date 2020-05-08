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
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
  *
  * @author FangYidong<fangyidong@yahoo.com.cn>
  */
 
@SuppressWarnings("unchecked")
 public class JSONObject
         extends HashMap<String, Object> implements Map<String, Object>, JSONAware, JSONStreamAware {
 
     private static final long serialVersionUID = -503443796854799292L;
 
     public JSONObject() {
         super();
     }
 
     /**
      * Encode a map into JSON text and write it to out.
      * If this map is also a JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific behaviours will be ignored at this top level.
      *
      * @param map
      * @param out
      * @see JSONValue#writeJSONString(Object, Writer)
      */
     public static void writeJSONString(final Map map, final Writer out) throws IOException {
         if (map == null) {
             out.write("null");
             return;
         }
 
         boolean first = true;
         final Iterator iter = map.entrySet().iterator();
 
         out.write('{');
         while (iter.hasNext()) {
             if (first)
                 first = false;
             else
                 out.write(',');
             final Map.Entry entry = (Map.Entry) iter.next();
             out.write('\"');
             out.write(escape(String.valueOf(entry.getKey())));
             out.write('\"');
             out.write(':');
             JSONValue.writeJSONString(entry.getValue(), out);
         }
         out.write('}');
     }
 
     @Override
     public void writeJSONString(final Writer out) throws IOException {
         writeJSONString(this, out);
     }
 
     /**
      * Convert a map to JSON text. The result is a JSON object.
      * If this map is also a JSONAware, JSONAware specific behaviours will be omitted at this top level.
      *
      * @param map
      * @return JSON text, or "null" if map is null.
      * @see JSONValue#toJSONString(Object)
      */
     public static String toJSONString(final Map map) {
         if (map == null)
             return "null";
 
         final StringBuffer sb = new StringBuffer();
         boolean first = true;
         final Iterator iter = map.entrySet().iterator();
 
         sb.append('{');
         while (iter.hasNext()) {
             if (first)
                 first = false;
             else
                 sb.append(',');
 
             final Map.Entry entry = (Map.Entry) iter.next();
             toJSONString(String.valueOf(entry.getKey()), entry.getValue(), sb);
         }
         sb.append('}');
         return sb.toString();
     }
 
     @Override
     public String toJSONString() {
         return toJSONString(this);
     }
 
     private static String toJSONString(final String key, final Object value, final StringBuffer sb) {
         sb.append('\"');
         if (key == null)
             sb.append("null");
         else
             JSONValue.escape(key, sb);
         sb.append('\"').append(':');
 
         sb.append(JSONValue.toJSONString(value));
 
         return sb.toString();
     }
 
     public String toString() {
         return toJSONString();
     }
 
     public static String toString(final String key, final Object value) {
         final StringBuffer sb = new StringBuffer();
         toJSONString(key, value, sb);
         return sb.toString();
     }
 
     /**
      * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
      * It's the same as JSONValue.escape() only for compatibility here.
      *
      * @param s
      * @return
      * @see JSONValue#escape(String)
      */
     public static String escape(final String s) {
         return JSONValue.escape(s);
     }
 }
