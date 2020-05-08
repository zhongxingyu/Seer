 /******************************************************************************
  * Copyright 2010 Alexander Kiel                                              *
  *                                                                            *
  * Licensed under the Apache License, Version 2.0 (the "License");            *
  * you may not use this file except in compliance with the License.           *
  * You may obtain a copy of the License at                                    *
  *                                                                            *
  *     http://www.apache.org/licenses/LICENSE-2.0                             *
  *                                                                            *
  * Unless required by applicable law or agreed to in writing, software        *
  * distributed under the License is distributed on an "AS IS" BASIS,          *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
  * See the License for the specific language governing permissions and        *
  * limitations under the License.                                             *
  ******************************************************************************/
 
 package net.alexanderkiel.junit.http;
 
 import com.sun.net.httpserver.HttpExchange;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.nio.charset.Charset;
 import java.util.regex.Pattern;
 
 import static java.lang.String.format;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author Alexander Kiel
  */
 class WritableOngoingMocking extends BaseOngoingMocking {
 
     private static final Charset UTF_8 = Charset.forName("UTF-8");
     private static final Pattern SEMICOLON_SPLIT_PATTERN = Pattern.compile(";");
 
     private final HttpMock.Method method;
     private final String path;
     private final String payloadContentType;
     private final String payload;
     private final char[] charBuffer;
     private boolean called;
     private URI requestUri;
 
     @Nullable
     private String requestContentType;
     private String requestBody;
 
     WritableOngoingMocking(@NotNull HttpMock.Method method, @NotNull String path, @NotNull String payloadContentType,
                            @NotNull String payload) {
         this.method = method;
         this.path = path;
         this.payloadContentType = payloadContentType;
         this.payload = payload;
         charBuffer = new char[BUFFER_SIZE];
     }
 
     public void handle(HttpExchange httpExchange) throws IOException {
         logRequest(httpExchange);
         setCalled();
         verifyPayload(httpExchange);
         checkAuthorisation(httpExchange);
         setResponseHeaders(httpExchange.getResponseHeaders());
         sendResponseHeaders(httpExchange);
         sendResponseBody(httpExchange.getResponseBody());
         httpExchange.close();
     }
 
     private void setCalled() {
         called = true;
     }
 
     private void verifyPayload(HttpExchange httpExchange) throws IOException {
         requestUri = httpExchange.getRequestURI();
         requestContentType = extractContentType(httpExchange);
         requestBody = readBody(httpExchange.getRequestBody());
     }
 
     @Nullable
     static String extractContentType(HttpExchange httpExchange) {
         String header = httpExchange.getRequestHeaders().getFirst("Content-Type");
         if (header == null) {
             return null;
         } else {
             String[] headerParts = SEMICOLON_SPLIT_PATTERN.split(header);
             return headerParts[0].trim();
         }
     }
 
     private String readBody(InputStream requestBodyInputStream) throws IOException {
         BufferedReader reader = new BufferedReader(new InputStreamReader(requestBodyInputStream, UTF_8));
         try {
             StringBuilder buffer = new StringBuilder();
             int length = 0;
             do {
                 buffer.append(charBuffer, 0, length);
                 length = reader.read(charBuffer);
             } while (length >= 0);
             return buffer.toString();
         } finally {
             reader.close();
         }
     }
 
     public void verify() {
         assertTrue(format("request %s %s called", method, path), called);
        assertEquals(format("request %s %s content type", method, path), payloadContentType, requestContentType);
        assertEquals(format("request %s %s payload", method, path), payload, requestBody);
     }
 
     @Override
     public String toString() {
         return "WritableOngoingMocking[" + super.toString() + ", payload = '" + payload + "']";
     }
 }
