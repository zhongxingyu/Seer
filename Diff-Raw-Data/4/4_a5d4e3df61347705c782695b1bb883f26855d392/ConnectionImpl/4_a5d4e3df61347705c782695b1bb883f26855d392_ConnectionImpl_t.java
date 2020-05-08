 package com.brewtab.irc.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.brewtab.irc.Connection;
 import com.brewtab.irc.ConnectionStateListener;
 import com.brewtab.irc.NotConnectedException;
 import com.brewtab.irc.messages.Message;
 import com.brewtab.irc.messages.MessageListener;
 import com.brewtab.irc.messages.filter.MessageFilter;
 import com.brewtab.irc.messages.filter.MessageFilters;
 
 class ConnectionImpl extends SimpleChannelHandler implements Connection {
     private static final Logger log = LoggerFactory.getLogger(ConnectionImpl.class);
 
     private Channel channel;
     private boolean connected;
     private Map<MessageListener, MessageFilter> messageListeners;
     private List<ConnectionStateListener> connectionStateListeners;
     private ExecutorService executor;
 
     public ConnectionImpl() {
         channel = null;
         connected = false;
 
         messageListeners = new ConcurrentHashMap<MessageListener, MessageFilter>();
        connectionStateListeners = new CopyOnWriteArrayList<ConnectionStateListener>();
 
         executor = Executors.newCachedThreadPool();
     }
 
     @Override
     public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
         log.debug("channel opened");
 
         channel = ctx.getChannel();
         super.channelOpen(ctx, e);
     }
 
     @Override
     public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
         log.debug("channel connected");
 
         connected = true;
 
         for (final ConnectionStateListener listener : connectionStateListeners) {
             executor.execute(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         listener.onConnectionConnected();
                     } catch (Exception e) {
                         log.error("caught exception from onConnectionConnected", e);
                     }
                 }
             });
         }
 
         super.channelConnected(ctx, e);
     }
 
     @Override
     public void closeRequested(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
         log.debug("channel closing");
 
         for (final ConnectionStateListener listener : connectionStateListeners) {
             executor.execute(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         listener.onConnectionClosing();
                     } catch (Exception e) {
                         log.error("caught exception from onConnectionClosing", e);
                     }
                 }
             });
         }
 
         super.closeRequested(ctx, e);
     }
 
     @Override
     public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
         log.debug("channel closed");
 
         connected = false;
 
         for (final ConnectionStateListener listener : connectionStateListeners) {
             executor.execute(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         listener.onConnectionClosed();
                     } catch (Exception e) {
                         log.error("caught exception from onConnectionClosed", e);
                     }
                 }
             });
         }
 
         super.channelClosed(ctx, e);
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
         final Message message = (Message) e.getMessage();
 
         if (log.isDebugEnabled()) {
             log.debug("<<< {}", message.toString().trim());
         }
 
         for (Map.Entry<MessageListener, MessageFilter> entry : messageListeners.entrySet()) {
             final MessageListener listener = entry.getKey();
             final MessageFilter filter = entry.getValue();
 
             if (filter.check(message)) {
                 executor.execute(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             listener.onMessage(message);
                         } catch (Exception e) {
                             log.error("caught exception from onMessage", e);
                         }
                     }
                 });
             }
         }
 
         super.messageReceived(ctx, e);
     }
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
         log.error("caught exception in Netty pipeline, closing channel", e.getCause());
 
         ctx.getChannel().close();
         ctx.sendUpstream(e);
     }
 
     /**
      * Send a message through this connection
      */
     public ChannelFuture send(Message message) throws NotConnectedException {
         if (!connected) {
             throw new NotConnectedException();
         }
 
         if (log.isDebugEnabled()) {
             log.debug(">>> {}", message.toString().trim());
         }
 
         return channel.write(message);
     }
 
     /**
      * Send multiple messages through this connection
      */
     public ChannelFuture send(Message... messages) throws NotConnectedException {
         if (!connected) {
             throw new NotConnectedException();
         }
 
         if (log.isDebugEnabled()) {
             for (Message message : messages) {
                 log.debug(">>> {}", message.toString().trim());
             }
         }
 
         return channel.write(messages);
     }
 
     @Override
     public List<Message> request(MessageFilter match, final MessageFilter last, Message message)
         throws InterruptedException {
         final List<Message> response = new LinkedList<Message>();
         final CountDownLatch responseReceived = new CountDownLatch(1);
 
         addMessageListener(
             MessageFilters.range(match, last),
             new MessageListener() {
                 @Override
                 public void onMessage(Message message) {
                     response.add(message);
 
                     if (last.check(message)) {
                         responseReceived.countDown();
                         removeMessageListener(this);
                     }
                 }
             });
 
         send(message);
         responseReceived.await();
 
         return response;
     }
 
     @Override
     public List<Message> request(MessageFilter match, final MessageFilter last, Message... messages)
         throws InterruptedException {
 
         final List<Message> response = new LinkedList<Message>();
         final CountDownLatch responseReceived = new CountDownLatch(1);
 
         addMessageListener(
             MessageFilters.range(match, last),
             new MessageListener() {
                 @Override
                 public void onMessage(Message message) {
                     response.add(message);
 
                     if (last.check(message)) {
                         responseReceived.countDown();
                         removeMessageListener(this);
                     }
                 }
             });
 
         send(messages);
         responseReceived.await();
 
         return response;
     }
 
     @Override
     public ChannelFuture close() {
         if (!connected) {
             throw new NotConnectedException();
         }
 
         return channel.close();
     }
 
     @Override
     public void addMessageListener(MessageFilter filter, MessageListener listener) {
         messageListeners.put(listener, filter);
     }
 
     @Override
     public void addConnectionStateListener(ConnectionStateListener listener) {
         connectionStateListeners.add(listener);
     }
 
     @Override
     public void removeMessageListener(MessageListener listener) {
         messageListeners.remove(listener);
     }
 
     @Override
     public void removeConnectionStateListener(ConnectionStateListener listener) {
         connectionStateListeners.remove(listener);
     }
 
     @Override
     public boolean isConnected() {
         return connected;
     }
 }
