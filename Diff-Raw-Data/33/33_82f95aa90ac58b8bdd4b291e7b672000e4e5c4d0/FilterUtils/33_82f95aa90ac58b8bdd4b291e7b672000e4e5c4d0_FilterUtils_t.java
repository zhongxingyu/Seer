 /*
  * The MIT License
  *
  * Copyright 2013 Gravidence.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.gravidence.gravifon.filter;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import java.io.IOException;
 import javax.ws.rs.core.MediaType;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 
 /**
  * Service and client filter utility methods.
  * 
  * @author Maksim Liauchuk <maksim_liauchuk@fastmail.fm>
  */
 public class FilterUtils {
 
     /**
      * No entity placeholder.
      */
     public static final String NO_ENTITY = "<no entity>";
     
     /**
      * Corrupted entity placeholder. Used when serialization error occurs.
      */
     public static final String CORRUPTED_ENTITY = "<corrupted entity>";
     
     /**
      * Unsupported entity placeholder. Used when media type specified in request/response context
      * is not equal to <code>application/json</code>.
      * Actual media type name is mentioned in resulting placeholder.
      */
    private static final String UNSUPPORTED_ENTITY = "<%s/%s entity>";
 
     /**
      * Preventing class instantiation.
      */
     private FilterUtils() {
         // Nothing to do
     }
     
     /**
      * Serializes JSON entity (object) to {@link String}.<p>
      * Logs serialization error if such one happens.
      * 
      * @param objectMapper JSON data binding instance
      * @param jsonEntity JSON entity
      * @param logger logger instance
      * @return <ul>
      *           <li>serialized entity in successful case</li>
      *           <li>{@link #NO_ENTITY} in case the supplied <code>jsonEntity</code> argument is <code>null</code></li>
      *           <li>{@link #CORRUPTED_ENTITY} in case of serialization error</li>
      *         </ul>
      * @throws NullPointerException in case the supplied <code>objectMapper</code> or <code>logger</code>
      * argument is <code>null</code>
      */
     public static String jsonEntityToString(ObjectMapper objectMapper, Object jsonEntity, Logger logger) {
         if (objectMapper == null) {
             throw new NullPointerException("objectMapper");
         }
         if (logger == null) {
             throw new NullPointerException("logger");
         }
 
         String result;
 
         if (jsonEntity == null) {
             result = NO_ENTITY;
         } else {
             try {
                 result = objectMapper.writeValueAsString(jsonEntity);
             }
             catch (JsonProcessingException ex) {
                 logger.error("Failed to serialize entity", ex);
 
                 result = CORRUPTED_ENTITY;
             }
         }
 
         return result;
     }
 
     /**
      * Serializes JSON entity (byte array) to {@link String}.<p>
      * See {@link #jsonEntityToString(com.fasterxml.jackson.databind.ObjectMapper, java.lang.Object, org.slf4j.Logger)}
      * method version for details.
      */
     public static String jsonEntityToString(ObjectMapper objectMapper, byte[] jsonEntity, Logger logger) {
         if (objectMapper == null) {
             throw new NullPointerException("objectMapper");
         }
         if (logger == null) {
             throw new NullPointerException("logger");
         }
 
         String result;
 
         if (jsonEntity == null) {
             result = NO_ENTITY;
         } else {
             try {
                 JsonNode node = objectMapper.readTree(jsonEntity);
                 result = node.toString();
             }
             catch (IOException ex) {
                 logger.error("Failed to serialize entity", ex);
                 
                 try {
                     result = IOUtils.toString(jsonEntity, "UTF-8");
                 }
                 catch (IOException ex1) {
                     // it actually never occurs as declared by IOUtils.toString method
                     logger.warn("Failed to convert entity to plain string", ex1);
                     
                     result = CORRUPTED_ENTITY;
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Produces unsupported entity placeholder.
      * 
      * @param mediaType actual entity media type
      * @return unsupported entity placeholder
      * @throws NullPointerException in case the supplied argument is <code>null</code>
      * 
      * @see #UNSUPPORTED_ENTITY
      */
     public static String unsupportedEntityToString(MediaType mediaType) {
         if (mediaType == null) {
             throw new NullPointerException("mediaType");
         }
 
        return String.format(UNSUPPORTED_ENTITY, mediaType.getType(), mediaType.getSubtype());
     }
     
 }
