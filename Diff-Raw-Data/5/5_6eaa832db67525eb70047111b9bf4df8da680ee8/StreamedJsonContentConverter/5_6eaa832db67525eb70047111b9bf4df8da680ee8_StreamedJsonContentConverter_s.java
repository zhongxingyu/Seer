 /**
  * Copyright (C) 2012 Ness Computing, Inc.
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
 package com.nesscomputing.httpclient.response;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.Map;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonToken;
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.common.base.Objects;
 import com.google.common.base.Throwables;
 
 import com.nesscomputing.callback.Callback;
 import com.nesscomputing.callback.CallbackRefusedException;
 import com.nesscomputing.httpclient.HttpClientResponse;
 import com.nesscomputing.httpclient.HttpClientResponseHandler;
 import com.nesscomputing.logging.Log;
 
 /**
  * Accepts an incoming JSON data stream and converts it into objects on the fly. The JSON data stream must be structured in a special format:
  * <pre>
  * { "results": [ .... stream of data ],
  *   "success": true
  * }
  * </pre>
  *
  * No other fields must be present in the JSON object besides <tt>results</tt> and <tt>success</tt> and the <tt>success</tt> field must immediately follow
  * the <tt>results</tt> field to mark its end.
  */
 public class StreamedJsonContentConverter<T> extends AbstractErrorHandlingContentConverter<Void>
 {
     private static final Log LOG = Log.findLog();
 
     private static final TypeReference<Map<String, ? extends Object>> JSON_MAP_TYPE_REF = new TypeReference<Map<String, ? extends Object>>() {};
 
     public static StreamedJsonContentConverter<Map<String, ? extends Object>> of(final ObjectMapper mapper, final Callback<Map<String, ? extends Object>> callback)
     {
         return new StreamedJsonContentConverter<Map<String, ? extends Object>>(mapper, callback, JSON_MAP_TYPE_REF);
     }
 
     public static HttpClientResponseHandler<Void> handle(final ObjectMapper mapper, final Callback<Map<String, ? extends Object>> callback)
     {
         return ContentResponseHandler.forConverter(new StreamedJsonContentConverter<Map<String, ? extends Object>>(mapper, callback, JSON_MAP_TYPE_REF));
     }
 
     public static <T> HttpClientResponseHandler<Void> handle(final ObjectMapper mapper, final Callback<? super T> callback, final TypeReference<T> typeReference)
     {
         return ContentResponseHandler.forConverter(new StreamedJsonContentConverter<T>(mapper, callback, typeReference));
     }
 
     private final ObjectMapper mapper;
     private final TypeReference<T> typeRef;
     private final Callback<? super T> callback;
 
     StreamedJsonContentConverter(final ObjectMapper mapper, final Callback<? super T> callback, final TypeReference<T> typeRef)
     {
         this.mapper = mapper;
         this.typeRef = typeRef;
         this.callback = callback;
     }
 
     @Override
     public Void convert(final HttpClientResponse response, final InputStream inputStream)
         throws IOException
     {
         switch(response.getStatusCode())
         {
         case 201:
         case 204:
             LOG.debug("Return code is %d, finishing.", response.getStatusCode());
             return null;
 
         case 200:
             final JsonParser jp = mapper.getFactory().createJsonParser(inputStream);
             try {
                 expect(jp, jp.nextToken(), JsonToken.START_OBJECT);
                 expect(jp, jp.nextToken(), JsonToken.FIELD_NAME);
                 if (!"results".equals(jp.getCurrentName())) {
                     throw new JsonParseException("expecting results field", jp.getCurrentLocation());
                 }
                 expect(jp, jp.nextToken(), JsonToken.START_ARRAY);
                jp.nextToken(); // readValuesAs must be positioned after the START_ARRAY token, per javadoc.
 
                 Iterator<T> iter = jp.readValuesAs(typeRef);
 
                 while (iter.hasNext()) {
                     try {
                         callback.call(iter.next());
                     }
                     catch (CallbackRefusedException e) {
                         LOG.debug(e, "callback refused execution, finishing.");
                         return null;
                     }
                     catch (InterruptedException e) {
                         Thread.currentThread().interrupt();
                         throw new IOException("Callback interrupted", e);
                     }
                     catch (Exception e) {
                         Throwables.propagateIfPossible(e, IOException.class);
                         throw new IOException("Callback failure", e);
                     }
                 }
                 if (jp.nextValue() != JsonToken.VALUE_TRUE || !jp.getCurrentName().equals("success")) {
                     throw new IOException("Streamed receive did not terminate normally; inspect server logs for cause.");
                 }
                 return null;
             }
             finally {
                 jp.close();
             }
 
         default:
             throw throwHttpResponseException(response);
         }
     }
 
     private void expect(final JsonParser jp, final JsonToken token, final JsonToken expected) throws JsonParseException
     {
         if (!Objects.equal(token, expected)) {
             throw new JsonParseException(String.format("Expected %s, found %s", expected, token), jp.getCurrentLocation());
         }
     }
 }
