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
 
 package de.cosmocode.palava.bridge.simple;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Maps;
 
 import de.cosmocode.json.JSON;
 import de.cosmocode.palava.bridge.ConnectionLostException;
 import de.cosmocode.palava.bridge.Content;
 import de.cosmocode.palava.bridge.Header;
 import de.cosmocode.palava.bridge.call.CallType;
 import de.cosmocode.palava.bridge.call.JsonCall;
 import de.cosmocode.palava.bridge.content.JsonContent;
 
 /**
  * Default protocol algorithm.
  * 
  * @author Oliver Lorenz
  * @author Willi Schoenborn
  */
 final class DefaultProtocolAlgorithm implements ProtocolAlgorithm {
     
     private static final Logger LOG = LoggerFactory.getLogger(DefaultProtocolAlgorithm.class);
 
     /**
      * Identifies the different parts of the protocol structure.
      *
      * @author Willi Schoenborn
      */
     private static enum Part {
      
         TYPE {
           
             @Override
             public Part writeTo(char c, StringBuilder target) {
                 if (c == ':') {
                     return Part.COLON;
                 } else {
                     target.append(c);
                 }
                 return this;
             }
             
         },
         
         COLON {
             
             @Override
             public Part writeTo(char c, StringBuilder target) {
                 if (c == '/') {
                     return Part.FIRST_DOUBLE_SLASH;
                 } else {
                     throw new ProtocolException('/', c);
                 }
             }
             
         },
         
         FIRST_DOUBLE_SLASH {
             
             @Override
             public Part writeTo(char c, StringBuilder target) {
                 if (c == '/') {
                     return Part.NAME;
                 } else {
                     throw new ProtocolException('/', c);
                 }
             }
             
         },
         
         NAME {
             
             @Override
             public Part writeTo(char c, StringBuilder target) {
                 if (c == '/') {
                     return Part.SESSION_ID;
                 } else {
                     target.append(c);
                 }
                 return this;
             }
             
         },
         
         SESSION_ID {
             
             @Override
             public Part writeTo(char c, StringBuilder target) {
                 if (c == '/') {
                     return Part.LEFT_PARENTHESIS;
                 } else {
                     target.append(c);
                 }
                 return this;
             }
             
         },
         
         LEFT_PARENTHESIS {
             
             @Override
             public Part writeTo(char c, StringBuilder target) {
                 if (c == '(') {
                     return Part.CONTENT_LENGTH;
                 } else {
                     throw new ProtocolException('(', c);
                 }
             }
             
         },
         
         CONTENT_LENGTH {
 
             @Override
             public Part writeTo(char c, StringBuilder target) {
                 if (c == ')') {
                     return QUESTION_MARK;
                 } else {
                     target.append(c);
                 }
                 return this;
             }
             
         },
         
         QUESTION_MARK {
          
             @Override
             public Part writeTo(char c, StringBuilder target) {
                 return this;
             }
             
         };
         
         public abstract Part writeTo(char c, StringBuilder target);
         
     }
 
     @Override
     public Map<String, String> open(Header header, InputStream input, OutputStream output) {
         
         final JsonCall jsonCall = new SimpleJsonCall(null, header, input);
         
         final JSONObject object;
         
         try {
             object = jsonCall.getJSONObject();
             jsonCall.discard();
             sendTo(JsonContent.EMPTY, output);
         } catch (JSONException e) {
             throw new ProtocolException(e);
         } catch (IOException e) {
             throw new ProtocolException(e);
         }
         
         return Maps.transformValues(JSON.asMap(object), new Function<Object, String>() {
             
             @Override
             public String apply(Object from) {
                 return from == null ? null : from.toString();
             }
             
         });
     }
     
     @Override
     public Header read(InputStream input) {
         final StringBuilder type = new StringBuilder(6);
         final StringBuilder aliasedName = new StringBuilder(50);
         final StringBuilder sessionId = new StringBuilder(64);
         final StringBuilder contentLength = new StringBuilder(6);
         
         Part part = Part.TYPE;
         
         while (true) {
             final char c = readFrom(input);
             
             switch (part) {
                 case TYPE: {
                     part = part.writeTo(c, type);
                     break;
                 }
                 case COLON: {
                     part = part.writeTo(c, null);
                     break;
                 }
                 case FIRST_DOUBLE_SLASH: {
                     part = part.writeTo(c, null);
                     break;
                 }
                 case NAME: {
                     part = part.writeTo(c, aliasedName);
                     break;
                 }
                 case SESSION_ID: {
                     part = part.writeTo(c, sessionId);
                     break;
                 }
                 case LEFT_PARENTHESIS: {
                     part = part.writeTo(c, null);
                     break;
                 }
                 case CONTENT_LENGTH: {
                     part = part.writeTo(c, contentLength);
                     break;
                 }
                 default: {
                     if (c == '?') {
                         LOG.debug("Session ID in header: {}", sessionId);
                         return new SimpleHeader(
                             CallType.valueOf(type.toString().toUpperCase()),
                             aliasedName.toString(),
                             sessionId.toString(),
                             Integer.parseInt(contentLength.toString())
                         );
                     } else {
                         throw new ProtocolException('?', c);
                     }
                 }
             }
             
         }
         
     }
     
     private char readFrom(InputStream input) {
         try {
            final int b = input.read();
            if (b == -1) {
                 throw new ConnectionLostException("Reached end of stream");
             }
            return (char) b;
         } catch (IOException e) {
             throw new ConnectionLostException(e);
         }
     }
 
     @Override
     public void sendTo(Content content, OutputStream output) throws IOException {
         Preconditions.checkNotNull(content, "Content");
         Preconditions.checkNotNull(output, "Output");
         final BufferedOutputStream buffered = new BufferedOutputStream(output);
 
         // write header
         final String header = String.format("%s://(%s)?", content.getMimeType(), content.getLength());
         LOG.debug("Writing header: {}", header);
         buffered.write(header.getBytes());
 
         // write body
         LOG.debug("Writing content: {}", content);
         content.write(buffered);
         buffered.flush();
     }
 
 }
