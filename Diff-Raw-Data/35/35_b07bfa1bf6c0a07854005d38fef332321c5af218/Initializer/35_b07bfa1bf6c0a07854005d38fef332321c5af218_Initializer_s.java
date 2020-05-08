 package com.github.zhongl.stompize;
 
 import io.netty.channel.Channel;
 import io.netty.channel.ChannelInitializer;
 import java.util.concurrent.CountDownLatch;
 
 /** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
 public class Initializer<T extends StompV1_2> extends ChannelInitializer<Channel> {
 
     public Initializer(Class<T> spec, int maxFrameLength, Object[] arguments) {
         this.spec = spec;
         this.maxFrameLength = maxFrameLength;
         this.arguments = arguments;
         latch = new CountDownLatch(1);
     }
 
     @Override
     protected void initChannel(Channel ch) throws Exception {
         handle = Stompize.newInstance(spec, ch, arguments);
        ch.pipeline().addLast(/*new ErrorHandler(handle),*/ Stompize.inboundHandler(handle, maxFrameLength));
         latch.countDown();
     }
 
     public T initialized() throws InterruptedException {
         latch.await();
         return handle;
     }
 
     private final    CountDownLatch latch;
     private final    Class<T>       spec;
     private final    int            maxFrameLength;
     private final    Object[]       arguments;
     private volatile T              handle;
 }
