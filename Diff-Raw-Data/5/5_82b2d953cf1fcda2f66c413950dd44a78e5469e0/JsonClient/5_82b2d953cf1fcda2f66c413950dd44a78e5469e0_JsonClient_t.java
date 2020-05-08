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
 
 package de.cosmocode.palava.ipc.json;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.jackson.map.ObjectMapper;
 
 import de.cosmocode.palava.ipc.netty.AbstractClient;
 import de.cosmocode.palava.ipc.netty.Client;
 import de.cosmocode.palava.ipc.netty.ClientConnection;
 import de.cosmocode.palava.ipc.netty.NettyClient;
 
 /**
  * Json specific {@link Client} implementation.
  *
  * @since 1.0
  * @author Willi Schoenborn
  */
 public final class JsonClient extends AbstractClient implements Client {
 
     private final ObjectMapper mapper = new ObjectMapper();
     
     private final Client client = new NettyClient();
     
     @Override
     public JsonClientConnection connect(InetSocketAddress address) {
         final ClientConnection connection = client.connect(address);
         return new InternalJsonConnection(connection);
     }
     
     /**
      * Internal implementation of the {@link JsonClientConnection} interface.
      *
      * @since 1.0
      * @author Willi Schoenborn
      */
     private final class InternalJsonConnection implements JsonClientConnection {
         
         private final ClientConnection connection;
         
         public InternalJsonConnection(ClientConnection connection) {
             this.connection = connection;
         }
 
         @Override
         public String send(String request) {
             return connection.send(request);
         }
         
         @Override
         public void disconnect() {
             connection.disconnect();
         }
         
         @Override
         public <T> T send(Map<?, ?> request) {
            return this.<T>send(Object.class.cast(request));
         }
         
         @Override
         public <T> T send(List<?> request) {
            return this.<T>send(Object.class.cast(request));
         }
         
         private <T> T send(Object request) {
             try {
                 @SuppressWarnings("unchecked")
                 final T result = (T) read(send(mapper.writeValueAsString(request)));
                 return result;
             } catch (IOException e) {
                 throw new IllegalArgumentException(e);
             }
         }
         
         private Object read(String value) throws IOException {
             if (value.charAt(0) == '[') {
                 return mapper.readValue(value, List.class);
             } else if (value.charAt(0) == '{') {
                 return mapper.readValue(value, Map.class);
             } else {
                 throw new IllegalArgumentException(String.format("%s is no valid json", value));
             }
         }
         
     }
 
     @Override
     public void shutdown() {
         client.shutdown();
     }
 
 }
