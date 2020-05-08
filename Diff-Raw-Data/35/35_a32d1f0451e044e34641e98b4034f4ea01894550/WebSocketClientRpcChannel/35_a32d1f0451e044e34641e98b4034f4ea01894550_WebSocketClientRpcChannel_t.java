 /**
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */

 package org.waveprotocol.wave.examples.fedone.rpc;
 
 import com.google.common.base.Preconditions;
 
 import org.waveprotocol.wave.examples.fedone.util.Log;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.concurrent.ExecutorService;
 
 /**
 * Implementation of {@link ClientRpcChannel} based on a
 * {@link WebSocketClientChannel}.
  */
 public class WebSocketClientRpcChannel extends ClientRpcChannelImpl {
  private static final Log LOG = Log.get(WebSocketClientRpcChannel.class);
 
  public WebSocketClientRpcChannel(SocketAddress serverAddress, ExecutorService threadPool)
       throws IOException {
     super(serverAddress, threadPool);
   }
 
  public WebSocketClientRpcChannel(SocketAddress serverAddress) throws IOException {
     super(serverAddress);
   }
 
   @Override
  protected MessageExpectingChannel startChannel(SocketAddress serverAddress,
       ExecutorService threadPool, ProtoCallback callback) {
    Preconditions.checkNotNull(serverAddress, "null serverAddress");
 
     InetSocketAddress inetAddress = (InetSocketAddress) serverAddress;
    WebSocketClientChannel protoChannel = new WebSocketClientChannel(
        inetAddress.getHostName(), inetAddress.getPort(), callback, threadPool);
     protoChannel.expectMessage(Rpc.RpcFinished.getDefaultInstance());
     protoChannel.startAsyncRead();
    LOG.fine("Opened a new WebSocketClientRpcChannel to " + serverAddress);
     return protoChannel;
   }
}
