 /**
  * Copyright 2013  Bjørn Remseth (la3lma@gmail.com)
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package no.rmz.blobee.rpc;
 
import com.google.protobuf.Message;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.logging.Logger;
 import no.rmz.blobeeproto.api.proto.Rpc;
 import no.rmz.blobeeproto.api.proto.Rpc.MethodSignature;
 import no.rmz.testtools.Net;
 import no.rmz.testtools.Receiver;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import static org.mockito.Mockito.*;
 import org.mockito.runners.MockitoJUnitRunner;
 
 
 @RunWith(MockitoJUnitRunner.class)
 public final class RpcPeerStartupAndShutdownTest {
 
     private static final Logger log = Logger.getLogger(
             no.rmz.blobee.rpc.RpcPeerStartupAndShutdownTest.class.getName());
 
 
     private final static String HOST = "localhost";
     private final static Rpc.RpcControl HEARTBEAT_MESSAGE =
             Rpc.RpcControl.newBuilder().setMessageType(Rpc.MessageType.HEARTBEAT).build();
 
      private final static Rpc.RpcControl SHUTDOWN_MESSAGE =
             Rpc.RpcControl.newBuilder().setMessageType(Rpc.MessageType.SHUTDOWN).build();
 
 
     int port;
 
     @Before
     public void setUp() throws IOException {
         port = Net.getFreePort();
     }
 
     @Mock
    Receiver<Rpc.RpcControl> heartbeatReceiver;

    @Mock
     Receiver<Rpc.RpcControl> shutdownReceiver;
 
     @Test(timeout=10000)
     public void testTransmissionOfHeartbeatsAtStartup() {
 
         final RpcMessageListener ml = new RpcMessageListener() {
             @Override
             public void receiveMessage(
                     final Object message,
                     final ChannelHandlerContext ctx) {
                 if (message instanceof Rpc.RpcControl) {
                     final Rpc.RpcControl msg = (Rpc.RpcControl) message;
                    heartbeatReceiver.receive(msg);
 
                     ctx.getChannel().close();
                 }
             }
         };
 
         final RpcClient rpcClient = RpcSetup.newConnectingNode(
                 new InetSocketAddress(HOST, port));
         // XXX This is actually a bit bogus, since what the server
         //     needs is not a client that can connect to somewhere (in
         //     particular it doesn't need a client that can connect to itself
         //     as we're setting it up to do here), but it does need somewhere
         //     to send returning RPC invocations to, and that's not strictly
         //     a client, but something that I've not abstracted out yet.
         //     Nonetheless, in the present test environment, this should
         //     work.
         RpcSetup.setUpServer(port, executor, rpcClient, ml);
 
         rpcClient.start();
         // Need some time to let the startup transient settle.
         sleepHalfASec();
 
        verify(heartbeatReceiver, times(1)).receive(HEARTBEAT_MESSAGE);
     }
 
     public void sleepHalfASec() {
          try {
             Thread.currentThread().sleep(500);
         }
         catch (InterruptedException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     final RpcExecutionService executor = new RpcExecutionService() {
         public void execute(RemoteExecutionContext dc, ChannelHandlerContext ctx, Object param) {
             log.info("Executing dc = " + dc + ", param = " + param);
         }
 
         public Class getReturnType(MethodSignature sig) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         public Class getParameterType(MethodSignature sig) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         public void startCancel(ChannelHandlerContext ctx, long rpcIndex) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
     };
 
     @Test(timeout=10000)
     public void testReactionToShutdown() {
 


         final RpcMessageListener ml = new RpcMessageListener() {
             @Override
             public void receiveMessage(
                     final Object message,
                     final ChannelHandlerContext ctx) {
                 if (message instanceof Rpc.RpcControl) {
                     final Rpc.RpcControl msg = (Rpc.RpcControl) message;
 

                     if (msg.getMessageType() == Rpc.MessageType.HEARTBEAT) {
                        heartbeatReceiver.receive(msg);
                         ctx.getChannel().write(SHUTDOWN_MESSAGE);
                     } else if (msg.getMessageType() == Rpc.MessageType.SHUTDOWN) {
                         shutdownReceiver.receive(msg);
                     }
                 }
             }
         };
 
        final RpcClient rpcClient = RpcSetup.newConnectingNode(new InetSocketAddress(HOST, port));
         // XXX This is actually a bit bogus, since what the server
         //     needs is not a client that can connect to somewhere (in
         //     particular it doesn't need a client that can connect to itself
         //     as we're setting it up to do here), but it does need somewhere
         //     to send returning RPC invocations to, and that's not strictly
         //     a client, but something that I've not abstracted out yet.
         //     Nonetheless, in the present test environment, this should
         //     work.
         RpcSetup.setUpServer(port, executor, rpcClient, ml);
 
         rpcClient.start();
         // Need some time to let the startup transient settle.
        // XXX Should perhaps used a lock instead?
         sleepHalfASec();
 
        verify(heartbeatReceiver).receive(HEARTBEAT_MESSAGE);

        // XXX  This test is bogus, since it does not have the right
        //      probes in the right places.  It needs to be fixed.
        // verify(shutdownReceiver).receive(SHUTDOWN_MESSAGE);
     }
 
 }
