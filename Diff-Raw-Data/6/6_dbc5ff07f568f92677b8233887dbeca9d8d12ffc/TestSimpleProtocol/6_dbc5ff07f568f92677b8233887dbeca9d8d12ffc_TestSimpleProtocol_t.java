 /*
  * Copyright (c) 2013 Hal Hildebrand, all rights reserved.
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
 package com.hellblazer.pinkie.buffer.fsmExample;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 
 import org.junit.Test;
 
 import com.hellblazer.pinkie.CommunicationsHandler;
 import com.hellblazer.pinkie.SocketChannelHandler;
 import com.hellblazer.pinkie.buffer.BufferProtocol;
 import com.hellblazer.pinkie.buffer.BufferProtocolHandler;
 import com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocol.MessageType;
 import com.hellblazer.pinkie.buffer.fsmExample.SimpleProtocolImpl.MessageHandler;
 
 public class TestSimpleProtocol {
 
     @Test
     public void testClientEstablish() throws IOException {
         SimpleProtocolImpl protocol = new SimpleProtocolImpl(null);
         BufferProtocolHandler handler = protocol.getBufferProtocolHandler();
         BufferProtocol bufferProtocol = new BufferProtocol(
                                                            protocol.getBufferProtocolHandler());
         CommunicationsHandler commHandler = bufferProtocol.getHandler();
 
         SocketChannelHandler socketHandler = mock(SocketChannelHandler.class);
         SocketChannel socketChannel = mock(SocketChannel.class);
         when(socketHandler.getChannel()).thenReturn(socketChannel);
 
         when(socketChannel.getLocalAddress()).thenReturn(new InetSocketAddress(
                                                                                666));
         when(socketChannel.getRemoteAddress()).thenReturn(new InetSocketAddress(
                                                                                 668));
         when(socketChannel.isConnected()).thenReturn(true);
         assertEquals(SimpleProtocolContext.Simple.Initial,
                      protocol.getCurrentState());
 
         commHandler.connect(socketHandler);
         assertEquals(SimpleProtocolContext.SimpleClient.Connect,
                      protocol.getCurrentState());
 
         verify(socketHandler).selectForWrite();
         handler.writeReady();
 
         assertEquals(SimpleProtocolContext.SimpleClient.EstablishSession,
                      protocol.getCurrentState());
         verify(socketHandler).selectForRead();
         bufferProtocol.getReadBuffer().put((byte) MessageType.ACK.ordinal());
         handler.readReady();
         assertEquals(SimpleProtocolContext.SimpleClient.SendMessage,
                      protocol.getCurrentState());
 
         bufferProtocol.getReadBuffer().rewind();
         String message = "HELLO CLEVELAND!";
         protocol.send(message);
        assertEquals(SimpleProtocolContext.SimpleClient.MessageSent,
                      protocol.getCurrentState());
 
        handler.writeReady();
        assertEquals(SimpleProtocolContext.SimpleClient.AwaitAck,
                protocol.getCurrentState());
         bufferProtocol.getReadBuffer().put((byte) MessageType.ACK.ordinal());
         handler.readReady();
         assertEquals(SimpleProtocolContext.SimpleClient.SendMessage,
                      protocol.getCurrentState());
 
         protocol.close();
         commHandler.closing();
         assertEquals(SimpleProtocolContext.Simple.Closed,
                      protocol.getCurrentState());
     }
 
     @Test
     public void testServerEstablish() throws IOException {
         SimpleProtocolImpl protocol = new SimpleProtocolImpl(
                                                              new MessageHandler() {
 
                                                                  @Override
                                                                  public void handle(String message) {
                                                                      System.out.println(message);
 
                                                                  }
                                                              });
         BufferProtocolHandler handler = protocol.getBufferProtocolHandler();
         BufferProtocol bufferProtocol = new BufferProtocol(
                                                            protocol.getBufferProtocolHandler());
         CommunicationsHandler commHandler = bufferProtocol.getHandler();
 
         SocketChannelHandler socketHandler = mock(SocketChannelHandler.class);
         SocketChannel socketChannel = mock(SocketChannel.class);
         when(socketHandler.getChannel()).thenReturn(socketChannel);
 
         when(socketChannel.getLocalAddress()).thenReturn(new InetSocketAddress(
                                                                                666));
         when(socketChannel.getRemoteAddress()).thenReturn(new InetSocketAddress(
                                                                                 668));
         when(socketChannel.isConnected()).thenReturn(true);
         assertEquals(SimpleProtocolContext.Simple.Initial,
                      protocol.getCurrentState());
 
         commHandler.accept(socketHandler);
         assertEquals(SimpleProtocolContext.SimpleServer.ConnectionAccepted,
                      protocol.getCurrentState());
 
         verify(socketHandler).selectForRead();
 
         bufferProtocol.getReadBuffer().put((byte) MessageType.ESTABLISH.ordinal());
         handler.readReady();
         assertEquals(SimpleProtocolContext.SimpleServer.SessionEstablished,
                      protocol.getCurrentState());
 
         assertEquals(1, bufferProtocol.getWriteBuffer().limit());
         assertEquals((byte) MessageType.ACK.ordinal(),
                      bufferProtocol.getWriteBuffer().get(0));
 
         bufferProtocol.getWriteBuffer().rewind();
 
         handler.writeReady();
 
         assertEquals(SimpleProtocolContext.SimpleServer.AwaitMessage,
                      protocol.getCurrentState());
 
         ByteBuffer buffer = bufferProtocol.getReadBuffer();
         buffer.put((byte) MessageType.MSG.ordinal());
 
         String msg = "Hello Cleveland";
         buffer.put((byte) msg.length());
         buffer.put(msg.getBytes());
         handler.readReady();
 
         assertEquals(SimpleProtocolContext.SimpleServer.AwaitMessage,
                      protocol.getCurrentState());
 
         buffer.put((byte) MessageType.GOOD_BYE.ordinal());
         handler.readReady();
         assertEquals(SimpleProtocolContext.Simple.Closed,
                      protocol.getCurrentState());
 
     }
 
 }
