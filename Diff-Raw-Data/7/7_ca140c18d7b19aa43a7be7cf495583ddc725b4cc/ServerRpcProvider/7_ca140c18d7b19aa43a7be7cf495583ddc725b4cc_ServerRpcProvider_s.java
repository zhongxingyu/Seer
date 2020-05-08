 /**
  * Copyright 2009 Google Inc.
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
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import com.google.protobuf.Descriptors;
 import com.google.protobuf.Message;
 import com.google.protobuf.RpcCallback;
 import com.google.protobuf.Service;
 import com.google.protobuf.UnknownFieldSet;
 import com.google.protobuf.Descriptors.MethodDescriptor;
 
 import org.waveprotocol.wave.examples.fedone.util.Log;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.websocket.WebSocket;
 import org.eclipse.jetty.websocket.WebSocketServlet;
 
 import java.io.IOException;
 import java.net.SocketAddress;
 import java.net.InetSocketAddress;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * ServerRpcProvider can provide instances of type Service over an incoming
  * network socket and service incoming RPCs to these services and their methods.
  * 
  *
  */
 public class ServerRpcProvider {
   private static final Log LOG = Log.get(ServerRpcProvider.class);
   
   private final SocketAddress rpcHostingAddress;
   private final String websocketHost;
   private final Integer websocketPort;
   private final Set<Connection> incomingConnections = Sets.newHashSet();
   private final ExecutorService threadPool;
   private ServerSocketChannel rpcServer = null;
   private Server websocketServer = null;
   private Future<?> acceptorThread = null;
   
   // Mapping from incoming protocol buffer type -> specific handler.
   private final Map<Descriptors.Descriptor, RegisteredServiceMethod> registeredServices =
       Maps.newHashMap();
 
   /**
    * Internal, static container class for any specific registered service method.
    */
   static class RegisteredServiceMethod {
     final Service service;
     final MethodDescriptor method;
 
     RegisteredServiceMethod(Service service, MethodDescriptor method) {
       this.service = service;
       this.method = method;
     }
   }
 
   class SequencedProtoChannelConnection extends Connection {
     private final SequencedProtoChannel protoChannel;
     private final SocketChannel channel;
 
     SequencedProtoChannelConnection(SocketChannel channel) {
       this.channel = channel;
       LOG.info("New Connection set up from " + this.channel);
       
       // Set up protoChannel, let it know to expect messages of all the
       // registered service/method types.
       // TODO: dynamic lookup for these types instead
       protoChannel = new SequencedProtoChannel(channel, this, threadPool);
       expectMessages(protoChannel);
       protoChannel.startAsyncRead();
     }
     
     protected void sendMessage(long sequenceNo, Message message) {
       protoChannel.sendMessage(sequenceNo, message);
     }
   }
   
   class WebSocketConnection extends Connection {
     private WebSocketServerChannel socketChannel;
 
     WebSocketConnection() {
       socketChannel = new WebSocketServerChannel(this);
       LOG.info("New websocket connection set up.");
       expectMessages(socketChannel);
     }
     
     protected void sendMessage(long sequenceNo, Message message) {
       socketChannel.sendMessage(sequenceNo, message);
     }
     
     public WebSocketServerChannel getWebSocketServerChannel() {
       return socketChannel;
     }
   }
 
   abstract class Connection implements ProtoCallback {
     private final Map<Long, ServerRpcController> activeRpcs =
         new ConcurrentHashMap<Long, ServerRpcController>();
 
     protected void expectMessages(MessageExpectingChannel channel) {
       synchronized (registeredServices) {
         for (RegisteredServiceMethod serviceMethod : registeredServices.values()) {
           channel.expectMessage(serviceMethod.service.getRequestPrototype(serviceMethod.method));
           LOG.fine("Expecting: " + serviceMethod.method.getFullName());
         }
       }
       channel.expectMessage(Rpc.CancelRpc.getDefaultInstance());
     }
     
     protected abstract void sendMessage(long sequenceNo, Message message);
 
     @Override
     public void message(final long sequenceNo, Message message) {
       if (message instanceof Rpc.CancelRpc) {
         final ServerRpcController controller = activeRpcs.get(sequenceNo);
         if (controller == null) {
           throw new IllegalStateException("Trying to cancel an RPC that is not active!");
         } else {
           LOG.info("Cancelling open RPC " + sequenceNo);
           controller.cancel();
         }
       } else if (registeredServices.containsKey(message.getDescriptorForType())) {
           if (activeRpcs.containsKey(sequenceNo)) {
             throw new IllegalStateException(
               "Can't invoke a new RPC with a sequence number already in use.");
           } else {
             final RegisteredServiceMethod serviceMethod =
               registeredServices.get(message.getDescriptorForType());
 
             // Create the internal ServerRpcController used to invoke the call.
             final ServerRpcController controller =
               new ServerRpcController(message, serviceMethod.service, serviceMethod.method,
                   new RpcCallback<Message>() {
                     @Override
                     synchronized public void run(Message message) {
                       if (message instanceof Rpc.RpcFinished
                           || !serviceMethod.method.getOptions().getExtension(Rpc.isStreamingRpc)) {
                         // This RPC is over - remove it from the map.
                         boolean failed = message instanceof Rpc.RpcFinished
                             ? ((Rpc.RpcFinished) message).getFailed() : false;
                         LOG.fine("RPC " + sequenceNo + " is now finished, failed = " + failed);
                         if (failed) {
                           LOG.info("error = " + ((Rpc.RpcFinished) message).getErrorText());
                         }
                         activeRpcs.remove(sequenceNo);
                       }
                       sendMessage(sequenceNo, message);
                     }
                   });
 
             // Kick off a new thread specific to this RPC.
             activeRpcs.put(sequenceNo, controller);
             threadPool.execute(controller);
           }
       } else {
         // Sent a message type we understand, but don't expect - erronous case!
         throw new IllegalStateException("Got expected but unknown message  (" + message
             + ") for sequence: " + sequenceNo);
       }
     }
 
     @Override
     public void unknown(long sequenceNo, String messageType, UnknownFieldSet message) {
       throw new IllegalStateException("Got unknown message (type: " + messageType + ", " + message
           + ") for sequence: " + sequenceNo);
     }
 
     @Override 
     public void unknown(long sequenceNo, String messageType, String message) {
       throw new IllegalStateException("Got unknown message (type: " + messageType + ", " + message
           + ") for sequence: " + sequenceNo);
     }
   }
 
   /**
    * Construct a new ServerRpcProvider, hosting on the passed SocketAddress
    * and WebSocket host and port. (The websocket isn't passed in as a 
    * SocketAddress beacuse Jetty requires host + port.)
    *
    * Also accepts an ExecutorService for spawning managing threads.
    * 
    * @param rpcHost the hosting socket
    * @param websocketHost host for websocket server
    * @param websocketPort port for websocket server
    * @param threadPool the service used to create threads
    */
   public ServerRpcProvider(SocketAddress rpcHost, 
       String websocketHost, Integer websocketPort,
       ExecutorService threadPool) {
     rpcHostingAddress = rpcHost;
     this.websocketHost = websocketHost;
     this.websocketPort = websocketPort;
     this.threadPool = threadPool;
   }
 
   /**
    * Constructs a new ServerRpcProvider with a default ExecutorService.
    */
   public ServerRpcProvider(SocketAddress rpcHost,
       String websocketHost, Integer websocketPort) {
     this(rpcHost, websocketHost, websocketPort, Executors.newCachedThreadPool());
   }
   
   @Inject
   public ServerRpcProvider(@Named("client_frontend_hostname") String rpcHost,
         @Named("client_frontend_port") Integer rpcPort,
         @Named("websocket_frontend_hostname") String websocketHost,
         @Named("websocket_frontend_port") Integer websocketPort) {
     this(new InetSocketAddress(rpcHost, rpcPort),
          websocketHost, websocketPort);
   }
 
   /**
    * Starts this server, binding to the previously passed SocketAddress.
    */
   public void startRpcServer() throws IOException {
     rpcServer = ServerSocketChannel.open();
     rpcServer.socket().setReuseAddress(true);
     rpcServer.socket().bind(rpcHostingAddress);
     rpcServer.configureBlocking(true);
 
     // Spawn a new server acceptor thread, which must accept incoming
     // connections indefinitely - until a ClosedChannelException is thrown.
     acceptorThread = threadPool.submit(new Runnable() {
       @Override
       public void run() {
         try {
           LOG.fine("ServerRpcProvider acceptorThread waiting for connections.");
           while (true) {
             SocketChannel serverSocket = rpcServer.accept();
             incomingConnections.add(new SequencedProtoChannelConnection(serverSocket));
           }
         } catch (ClosedChannelException e) {
           return;
         } catch (IOException e) {
           throw new IllegalStateException("Server should not throw a misunderstood IOException", e);
         }
       }
     });
   }
   
   public void startWebSocketServer() {
     websocketServer = new Server();
     Connector c = new SelectChannelConnector();
     c.setHost(websocketHost);
     c.setPort(websocketPort);
     
     websocketServer.addConnector(c);
     ServletContextHandler context = new ServletContextHandler();
     context.setContextPath("/");
     websocketServer.setHandler(context);
     
    context.addServlet(new ServletHolder(new WaveWebSocketServlet()), "/");

     try {
       websocketServer.start();      
     } catch (Exception e) { // yes, .start() throws "Exception"
       LOG.severe("Fatal error starting websocket server.", e);
       return;
     }
     LOG.fine("WebSocket server running.");
   }
 
   public class WaveWebSocketServlet extends WebSocketServlet {
     protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol)
     {
       WebSocketConnection connection = new WebSocketConnection();
       return connection.getWebSocketServerChannel();
     }
   }
 
   /**
    * Returns the bound socket. This is null if this server is not running.
    */
   public SocketAddress getBoundAddress() {
     return rpcServer != null ? rpcServer.socket().getLocalSocketAddress() : null;
   }
   
   /**
    * Returns the socket the WebSocket server is listening on.
    */
   public SocketAddress getWebSocketAddress() {
     if (websocketServer == null) {
       return null;
     } else {
       Connector c = websocketServer.getConnectors()[0];
       return new InetSocketAddress(c.getHost(), c.getLocalPort());
     }
   }
 
   /**
    * Stops this server.
    */
   public void stopServer() throws IOException {
     if (rpcServer != null) {
       rpcServer.close();
     }
     try {
       websocketServer.stop(); // yes, .stop() throws "Exception"
     } catch (Exception e) {
       LOG.warning("Fatal error stopping websocket server.", e);
     }
     if (acceptorThread != null) {
       try {
         acceptorThread.get();
       } catch (InterruptedException e) {
         throw new IllegalStateException();
       } catch (ExecutionException e) {
         throw new IllegalStateException("Server thread threw an exception", e.getCause());
       }
       if (!acceptorThread.isDone()) {
         throw new IllegalStateException("Server acceptor thread has not stopped.");
       }
     }
     LOG.fine("server shutdown.");
   }
 
   /**
    * Register all methods provided by the given service type.
    */
   public void registerService(Service service) {
     synchronized (registeredServices) {
       for (MethodDescriptor methodDescriptor : service.getDescriptorForType().getMethods()) {
         registeredServices.put(methodDescriptor.getInputType(),
             new RegisteredServiceMethod(service, methodDescriptor));
       }
     }
   }  
 }
