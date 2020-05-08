 package com.github.zhongl.stompize;
 
import io.netty.buffer.ByteBuf;
 import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;
 
 /** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class ErrorHandler extends ChannelInboundByteHandlerAdapter {
 
     public ErrorHandler(StompV1_2 handle) {
         this.handle = handle;
     }
 
     @Override
    protected void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ctx.fireInboundBufferUpdated();
    }

    @Override
     public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
         handle.error(cause.getMessage(), Content.NONE);
     }
 
     private final StompV1_2 handle;
 }
