 package com.github.zhongl.stompize;
 
import io.netty.channel.ChannelHandlerAdapter;
 import io.netty.channel.ChannelHandlerContext;
 
 /** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class ErrorHandler extends ChannelHandlerAdapter {
 
     public ErrorHandler(StompV1_2 handle) {
         this.handle = handle;
     }
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
         handle.error(cause.getMessage(), Content.NONE);
     }
 
     private final StompV1_2 handle;
 }
