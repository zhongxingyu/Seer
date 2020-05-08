 package com.github.zhongl.nij.nio;
 
 import static java.nio.channels.SelectionKey.*;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.*;
 import java.util.*;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class Multiplexors {
   private static final Logger LOGGER = LoggerFactory.getLogger(Multiplexors.class);
   private static final Set<Multiplexor> SET = Collections.synchronizedSet(new HashSet<Multiplexor>());
 
   public static void startWith(ServerSocketChannel serverSocketChannel) throws IOException {
     new Multiplexor(serverSocketChannel).start();
   }
 
   public static void shutdownAll() {
     for (Multiplexor multiplexor : SET)
       multiplexor.shutdown();
   }
 
   private static void silentClose(Selector selector) {
     if (selector == null) return;
     try { selector.close(); } catch (IOException e) { }
   }
 
   private static void silentClose(Closeable closeable) {
     if (closeable == null) return;
     if (closeable instanceof SocketChannel) {
       final SocketChannel channel = (SocketChannel) closeable;
       try { channel.socket().shutdownOutput(); } catch (IOException e) { }
     }
     try { closeable.close(); } catch (IOException e) { }
   }
 
   private static void accept(SelectionKey key, Selector selector) {
     final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
     SocketChannel channel = null;
     try {
       channel = serverSocketChannel.accept();
       if (channel == null) return;
 //    channel.socket().setSoLinger(false, 0);
       channel.socket().setTcpNoDelay(true);
       channel.socket().setSendBufferSize(64 * 1024);
       channel.configureBlocking(false);
       channel.register(selector, OP_READ /*| OP_WRITE*/ /*, stateObject*/);
     } catch (IOException e) {
       silentClose(channel);
       LOGGER.error("Closed broken " + channel, e);
     }
 
   }
 
   private static void silentCloseChannelOf(Set<SelectionKey> keys) {
     for (SelectionKey key : keys) silentClose(key.channel());
   }
 
   /** {@link Multiplexor} */
   private static class Multiplexor extends Thread {
     private static final int THRESHOLD = 256;
 
     private final Selector selector;
     private volatile boolean running = true;
     private static final ByteBuffer OK_200 = ByteBuffer.wrap("HTTP/1.0 200 OK\r\nContent-Length:1\r\n\r\na".getBytes());
     private static final ByteBuffer BUFFER = ByteBuffer.allocateDirect(1024);
 
     private Multiplexor(Selector selector) {
       setName(getClass().getSimpleName());
       this.selector = selector;
       setDaemon(false);
       SET.add(this);
     }
 
     private Multiplexor(ServerSocketChannel serverSocketChannel) throws IOException {
       this(Selector.open());
       serverSocketChannel.register(selector, OP_ACCEPT);
     }
 
     private void shutdown() {
       selector.wakeup();
       running = false;
     }
 
     @Override
     public void run() {
       while (running) try { select(); } catch (Exception e) { LOGGER.error("Unexpect error in selecting.", e); }
       silentCloseChannelOf(selector.keys());
       silentClose(selector);
       SET.remove(this);
     }
 
     // TODO limit max acceptance.
     private void select() throws Exception {
       final int selected = selector.select(500L);
       final int registered = selector.keys().size();
 
       if (registered == 0 && thenShutdown()) return;
 
       if (selected > 0) {
         SelectionKey acceptableKey = null;
 
         final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
         while (iterator.hasNext()) {
           final SelectionKey key = iterator.next();
           iterator.remove();
           if (key.isAcceptable()) acceptableKey = key;
          if (key.isReadable()) read(key);
          if (key.isWritable()) write(key);
         }
 
         if (acceptableKey == null) return;
         accept(acceptableKey, selector);
         /** Solve too many keys may slow down selecting. */
 //        if (registered < THRESHOLD) accept(acceptableKey, selector);
 //        else startANewMultiplexorWithout(acceptableKey);
       }
     }
 
 
     private void startANewMultiplexorWithout(SelectionKey acceptableKey) {
       Selector nSelector;
       try { nSelector = Selector.open(); } catch (IOException e) {
         LOGGER.error("Try accept channel to origin, because can't open new selector", e);
         accept(acceptableKey, selector);
         return;
       }
 
       accept(acceptableKey, nSelector);
 
       final Set<SelectionKey> keys = selector.keys();
       for (SelectionKey key : keys) {
         if (key.equals(acceptableKey)) continue;
 
         try {
           key.channel().register(nSelector, key.interestOps());
           key.cancel();
         } catch (ClosedChannelException e) {
           LOGGER.warn(key.channel() + " already closed.", e);
         }
       }
 
     }
 
     private boolean thenShutdown() { return !(running = false); }
 
     private void write(final SelectionKey key) {
       uninterest(OP_WRITE, key);
       service.execute(new Runnable() {
         @Override
         public void run() {
           Object state = key.attachment();
           // TODO write channel if state ready.
           interest(OP_WRITE, key);
         }
       });
     }
 
     private void read(final SelectionKey key) {
       uninterest(OP_READ, key);
       service.execute(new Runnable() {
         @Override
         public void run() {
           final SocketChannel channel = (SocketChannel) key.channel();
           try {
             channel.read(BUFFER);
             channel.write(OK_200);
           } catch (IOException e) {
             LOGGER.error("Close broken channel " + channel, e);
           } finally {
             silentClose(channel);
           }
 //          interest(OP_READ,key);
         }
       });
     }
 
   }
 
   private static void uninterest(int ops, SelectionKey key) {key.interestOps(key.interestOps() & ~ops);}
 
   private static void interest(int ops, SelectionKey key) {key.interestOps(key.interestOps() | ops);}
 
   private final static ExecutorService service = Executors
       .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
 }
