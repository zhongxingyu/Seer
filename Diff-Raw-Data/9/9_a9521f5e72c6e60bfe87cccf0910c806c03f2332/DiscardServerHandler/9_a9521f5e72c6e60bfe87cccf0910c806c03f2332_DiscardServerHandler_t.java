 package de.netprojectev.netty.examples;
 
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundHandlerAdapter;
 
 public class DiscardServerHandler extends ChannelInboundHandlerAdapter {
 
 	@Override
 	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ctx.channel().writeAndFlush(msg);
 	}
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
 		// Close the connection when an exception is raised.
 		cause.printStackTrace();
 		ctx.close();
 	}
 }
