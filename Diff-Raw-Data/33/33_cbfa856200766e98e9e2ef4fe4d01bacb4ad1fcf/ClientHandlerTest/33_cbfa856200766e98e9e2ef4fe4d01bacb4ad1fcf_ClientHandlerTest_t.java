 /*
  * Copyright 2012 The Netty Project
  *
  * The Netty Project licenses this file to you under the Apache License,
  * version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at:
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 package fr.kissy.hellion.client.handler;
 
 import com.google.protobuf.ByteString;
 import com.google.protobuf.InvalidProtocolBufferException;
 import fr.kissy.hellion.proto.message.Authenticated;
 import fr.kissy.hellion.proto.server.DownstreamMessageDto;
 import fr.kissy.hellion.proto.server.UpstreamMessageDto;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Handler implementation for the echo client.  It initiates the ping-pong
  * traffic between the echo client and server by sending the first event to
  * the server.
  */
 public class ClientHandlerTest extends SimpleChannelUpstreamHandler {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandlerTest.class.getName());
 
     /**
      * Creates a client-side handler.
      */
     public ClientHandlerTest() {
 
     }
 
     @Override
     public void channelConnected(ChannelHandlerContext context, ChannelStateEvent event) {
         DownstreamMessageDto.DownstreamMessageProto.Builder builder = DownstreamMessageDto.DownstreamMessageProto.newBuilder();
         builder.setType(DownstreamMessageDto.DownstreamMessageProto.Type.AUTHENTICATE);
         builder.setData(ByteString.copyFrom("Username".getBytes()));
         event.getChannel().write(builder.build());
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext context, MessageEvent event) {
         UpstreamMessageDto.UpstreamMessageProto message = (UpstreamMessageDto.UpstreamMessageProto) event.getMessage();
         LOGGER.debug("client : {}", message.getType());
 
         if (message.getType() == UpstreamMessageDto.UpstreamMessageProto.Type.AUTHENTICATED) {
             try {
                 Authenticated.AuthenticatedProto authenticated = Authenticated.AuthenticatedProto.parseFrom(message.getData());
                 LOGGER.debug("Received player {} \n{}", authenticated.getPlayer().getName(), authenticated.getPlayer().getSystemObjectsList());
             } catch (InvalidProtocolBufferException e) {
                 LOGGER.error("Cannot parse proto", e);
             }
 
            DownstreamMessageDto.DownstreamMessageProto.Builder downstream = DownstreamMessageDto.DownstreamMessageProto.newBuilder();
            downstream.setType(DownstreamMessageDto.DownstreamMessageProto.Type.SPAWN);
            event.getChannel().write(downstream.build());
        }
     }
 
     @Override
     public void exceptionCaught(ChannelHandlerContext context, ExceptionEvent event) {
         // Close the connection when an exception is raised.
         LOGGER.warn("Unexpected exception from downstream.", event);
         event.getChannel().close();
     }
 }
