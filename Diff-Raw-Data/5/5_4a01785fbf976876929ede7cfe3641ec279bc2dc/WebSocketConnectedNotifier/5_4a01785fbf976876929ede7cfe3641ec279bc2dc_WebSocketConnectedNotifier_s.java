 package com.barchart.netty.common.pipeline;
 
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundHandlerAdapter;
 import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
 
 /**
  * Blocks downstream channelActive() notifications until a websocket handshake
  * completes.
  */
 public class WebSocketConnectedNotifier extends
 		ChannelInboundHandlerAdapter {
 
 	@Override
 	public void userEventTriggered(final ChannelHandlerContext ctx,
 			final Object evt) throws Exception {
 
		if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
 
 			ctx.fireChannelActive();
 
 			ctx.pipeline().remove(this);
 
 		}
 
 	}
 
 	@Override
 	public void channelActive(final ChannelHandlerContext ctx)
 			throws Exception {
 		// Block downstream relay until handshake completes
 	}
 
 }
