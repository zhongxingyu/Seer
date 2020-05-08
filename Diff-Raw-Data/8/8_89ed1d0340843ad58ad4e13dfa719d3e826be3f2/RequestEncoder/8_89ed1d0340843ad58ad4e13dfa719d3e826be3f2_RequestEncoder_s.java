 package com.virtualdisk.network.codec;
 
 import com.virtualdisk.network.util.*;
 
 import org.jboss.netty.channel.*;
 import org.jboss.netty.buffer.*;
 import org.jboss.netty.handler.codec.oneone.*;
 
 public class RequestEncoder
 extends OneToOneEncoder
 {
     /*
     public void writeRequested(ChannelHandlerContext context, MessageEvent event)
     {
         Sendable message = (Sendable) event.getMessage();
         
         ChannelBuffer buffer = message.encode();
 
         Channels.write(context, event.getFuture(), buffer);
     }
     */
 
     protected Object encode( ChannelHandlerContext context
                            , Channel channel
                            , Object rawMessage
                            )
     {
         if (rawMessage instanceof Sendable)
         {
             Sendable message = (Sendable) rawMessage;
             ChannelBuffer buffer = message.encodeWithHeader();
            System.out.println("Encoded message");
             return buffer;
         }
         else
         {
             return rawMessage;
         }
     }
 }
 
