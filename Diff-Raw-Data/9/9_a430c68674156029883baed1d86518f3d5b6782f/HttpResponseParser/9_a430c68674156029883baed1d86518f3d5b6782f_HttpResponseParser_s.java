 /*
  * Copyright 2011 Mark Renouf
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
 package com.bitgrind.websocket;
 
 import java.io.IOException;
 import java.io.PushbackInputStream;
 import java.nio.charset.Charset;
 
 /**
  * A *very* minimalistic HTTP response parser. It's only job is to retrieve
  * basic headers and not touch the response body in any way, since the first
  * 16 bytes are needed for the WebSocket handshake process. This code is
  * reasonably robust but should only be used for WebSocket connections where
  * you expect to start immediately into websocket mode. For anything else
  * seek another solution.
  */
 class HttpResponseParser {
 
   private static final Charset UTF8 = Charset.forName("UTF-8");
 
   private HttpResponse response;
   private boolean done;
 
   HttpResponse parse(PushbackInputStream input) throws IOException {
     final byte CR = 0x0d;
     final byte LF = 0x0a;
     boolean foundCR = false;
 
     byte[] buffer = new byte[2000];
     int previous = 0;
     int position = 0;
     int capacity = buffer.length;
     int limit = capacity;
     int read = 0;
 
     while (!done) {
       if ((read = input.read(buffer, position, capacity - position)) != -1) {
         position += read;
       } else {
         // end of stream
         break;
       }
 
       // flip
       limit = position;
       position = previous;
 
       while (position < limit) {
         byte b = buffer[position++];
         if (!foundCR) {
           if (b == CR) {
             foundCR = true;
           } else
             if (b == LF) {
               handleCRLF(buffer, position, foundCR);
               int remaining = limit - position;
               System.arraycopy(buffer, position, buffer, 0, remaining);
               position = 0;
               limit = remaining;
 
               if (done) {
                 input.unread(buffer, position, remaining);
                 return response;
               }
             }
         } else
           if (b == LF) {
             handleCRLF(buffer, position, foundCR);
             int remaining = limit - position;
             System.arraycopy(buffer, position, buffer, 0, remaining);
             position = 0;
             limit = remaining;
             foundCR = false;
 
             if (done) {
               input.unread(buffer, position, remaining);
               return response;
             }
           }
       }
       previous = position;
       position = limit;
       limit = capacity;
     }
 
     return null;
   }
 
   private void handleCRLF(byte[] buffer, int offset, boolean foundCR) {
     int lineLength = offset - (foundCR ? 2 : 1);
    String line = new String(buffer, 0, lineLength, UTF8);
     handleLine(line);
   }
 
   private void handleLine(String line) {
     System.out.println(line);
     if (line.equals("")) {
       if (response != null) {
         done = true;
       }
     } else
       if (response == null) {
         response = new HttpResponse(line);
       } else {
         parseHeader(line);
       }
   }
 
   private void parseHeader(String line) {
     int split = line.indexOf(':');
     if (split != -1) {
       String name = line.substring(0, split);
       String value = line.substring(split + 2);
       response.addHeader(name, value);
     }
   }
 }
