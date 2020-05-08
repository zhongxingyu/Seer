 /*
  * Copyright 2012-2013 The Open Source Electronic Health Record Agent
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.osehra.vista.camel.rpc.service;
 
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 
 import org.osehra.vista.camel.rpc.RpcConstants;
 import org.osehra.vista.camel.rpc.codec.RpcCodecUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class TracerHandler extends SimpleChannelHandler {
     private final static Logger LOG = LoggerFactory.getLogger(TracerHandler.class);
     final AtomicInteger count = new AtomicInteger(0);
 
     private final AtomicLong transferredBytes = new AtomicLong();
 
     public long getTransferredBytes() {
         return transferredBytes.get();
     }
 
     @Override
     public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
         if (e instanceof ChannelStateEvent) {
             LOG.info(e.toString());
         }
         // Let SimpleChannelHandler call actual event handler methods below.
         super.handleUpstream(ctx, e);
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
         // Discard received data silently by doing nothing.
         int received = ((ChannelBuffer)e.getMessage()).readableBytes();
         long total = transferredBytes.addAndGet(((ChannelBuffer)e.getMessage()).readableBytes());
         LOG.info("Received {}/{} bytes", received, total);
 
         ChannelBuffer cb = ChannelBuffers.dynamicBuffer();
         cb.writeByte((byte)0x00);
        cb.writeByte((byte)0x00);
        cb.writeByte((byte)0x04);
 
         e.getChannel().write(cb);
     }
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
         // Close the connection when an exception is raised.
         LOG.warn("Unexpected exception from downstream.", e.getCause());
         e.getChannel().close();
     }
 
 }
 
