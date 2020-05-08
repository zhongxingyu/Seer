 package com.logentries.net;
 
 import io.netty.bootstrap.Bootstrap;
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelFuture;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.socket.nio.NioSocketChannel;
 import io.netty.util.concurrent.Future;
 import io.netty.util.concurrent.GenericFutureListener;
 
 /**
  * Created with IntelliJ IDEA.
  * Author: Edmondo Porcu
  * Date: 19/08/13
  * Time: 10:09
  */
 public class NettyBasedAsyncLogger extends AbstractAsyncLogger{
     EventLoopGroup group = new NioEventLoopGroup();
     Bootstrap bootstrap;
 
 
 
     Channel channel;
     // Read commands from the stdin.
     ChannelFuture lastWriteFuture = null;
 
     @Override
     public void initialize(){
         if(logentriesConnectionMode==null)
             throw new IllegalStateException("Cannot have an appender with null connection mode");
         if(exceptionListener==null){
             throw new IllegalStateException("Cannot have an appender without an exception listener");
         }
         logentriesConnectionMode.checkCredentials();
         try{
             bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class).handler(getLogentriesConnectionMode().connectionModeChannelInitializer());
             channel = bootstrap.connect(logentriesConnectionMode.getConnectionSocketFactory().getSocketAddress()).sync().channel();
             if(!channel.isWritable()){
                 throw new IllegalStateException("Unwriteable netty channel" + channel);
             }
             //TODO: fix this, see    http://stackoverflow.com/questions/18314986/order-of-initialization-in-java-exception-with-netty-4-0-7
//            ChannelConfig config = channel.config();
//            config.setAllocator(new PooledByteBufAllocator(false));
         }
         catch(InterruptedException e){
             throw new IllegalStateException("Interrupted ",e);
         }
 
     }
 
     @Override
     public void logLine(String line) {
         lastWriteFuture = channel.writeAndFlush(line);
         lastWriteFuture.addListener(new GenericFutureListener<Future<Void>>() {
             @Override
             public void operationComplete(Future<Void> future) throws Exception {
                 if(future.cause()!=null){
                     exceptionListener.notifyException(future.cause());
                 }
             }
         });
     }
 
     @Override
     public boolean hasMessagesInQueue() {
         return (lastWriteFuture==null || !lastWriteFuture.isDone());
     }
 
     @Override
     public void close() {
         try{
             if(lastWriteFuture!=null)
                 lastWriteFuture.sync();
 
         }
         catch(Exception exception){
             exceptionListener.notifyException(exception);
         }
         finally {
             group.shutdownGracefully();
         }
 
 
     }
 }
