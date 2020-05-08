 package org.ajantis.nettyfun;
 
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInitializer;
 import io.netty.channel.ChannelPipeline;
 import io.netty.channel.socket.SocketChannel;
 import io.netty.handler.codec.http.HttpContentCompressor;
 import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
 import io.netty.handler.timeout.IdleStateEvent;
 import io.netty.handler.timeout.IdleStateHandler;
 
 
 /**
  * Copyright iFunSoftware 2011
  *
  * @author Dmitry Ivanov
  */
 public class AkkaChannelInitializer extends ChannelInitializer<SocketChannel>{
     @Override
     public void initChannel(SocketChannel ch) {
         ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("timeout", new IdleStateHandler(0, 110, 0){
             public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent edx){
                 ctx.channel().close();
             }
         });
         pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());
         pipeline.addLast("deflater", new HttpContentCompressor());
         pipeline.addLast("handler", new AkkaServerHandler());
     }
 }
