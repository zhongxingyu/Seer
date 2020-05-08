 /**
  * Copyright 2010 CosmoCode GmbH
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
 
 package de.cosmocode.palava.bridge.content;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.extension.JSONConstructor;
 import org.json.extension.JSONEncoder;
 
 import de.cosmocode.json.JSON;
 import de.cosmocode.json.JSONMapable;
 import de.cosmocode.json.JSONRenderer;
 import de.cosmocode.palava.bridge.MimeType;
 import de.cosmocode.patterns.Immutable;
 
 /**
  * use the JSONConverter to produce JSON output of java objects.
  * 
  * @author Detlef Hüttemann
  * @author Willi Schoenborn
  */
 @Immutable
 public class JsonContent extends AbstractContent {
 
    public static final JsonContent EMPTY;
     
     private static final byte[] NULL = "null".getBytes(CHARSET);
     
    static {
        EMPTY = new JsonContent(new JSONObject());
    }
    
     private final byte[] bytes;
     
     public JsonContent(JSONRenderer renderer) {
         super(MimeType.JSON);
         bytes = renderer == null ? NULL : renderer.toString().getBytes(CHARSET);
     }
     
     public JsonContent(JSONObject object) {
         super(MimeType.JSON);
         bytes = object == null ? NULL : object.toString().getBytes(CHARSET);
     }
     
     public JsonContent(JSONArray array) {
         super(MimeType.JSON);
         bytes = array == null ? NULL : array.toString().getBytes(CHARSET);
     }
     
     public JsonContent(JSONConstructor constructor) {
         super(MimeType.JSON);
         bytes = constructor == null ? NULL : constructor.toString().getBytes(CHARSET);
     }
     
     public JsonContent(JSONMapable mapable) {
         super(MimeType.JSON);
         bytes = mapable == null ? NULL : JSON.createJSONRenderer().object(mapable).toString().getBytes(CHARSET);
     }
     
     public JsonContent(JSONEncoder encoder) {
         super(MimeType.JSON);
         bytes = encoder == null ? NULL : JSON.createJSONRenderer().object(encoder).toString().getBytes(CHARSET);
     }
     
     public <E> JsonContent(Iterable<E> iterable) {
         super(MimeType.JSON);
         bytes = iterable == null ? NULL : JSON.createJSONRenderer().array(iterable).toString().getBytes(CHARSET);
     }
     
     public <K, V> JsonContent(Map<K, V> map) {
         super(MimeType.JSON);
         bytes = map == null ? NULL : JSON.createJSONRenderer().object(map).toString().getBytes(CHARSET);
     }
     
     @Override
     public long getLength() {
         return bytes.length;
     }
     
     @Override
     public void write(OutputStream out) throws IOException {
         out.write(bytes, 0, bytes.length);
     }
     
     @Override
     public String toString() {
         return new String(bytes, CHARSET);
     }
     
 }
