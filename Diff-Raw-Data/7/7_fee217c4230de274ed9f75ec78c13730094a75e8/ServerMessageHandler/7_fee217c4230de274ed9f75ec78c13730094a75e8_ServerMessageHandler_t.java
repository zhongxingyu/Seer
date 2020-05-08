 // Copyright 2011 Nathaniel Harward
 //
 // This file is part of commons-j.
 //
 // commons-j is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // commons-j is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with commons-j. If not, see <http://www.gnu.org/licenses/>.
 
 package us.harward.commons.net.pubsub;
 
 import java.net.InetSocketAddress;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandler.Sharable;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.util.internal.ConcurrentHashMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 
 @Sharable
 final class ServerMessageHandler extends SimpleChannelUpstreamHandler {
 
     private static final Logger                    logger = LoggerFactory.getLogger(ServerMessageHandler.class);
 
     private final Map<String, DefaultChannelGroup> subscribers;
     private final Lock                             lock;
     private final ExecutorService                  service;
     private final Collection<PubSubClient>         remoteServers;
 
     ServerMessageHandler(final Predicate<Object> serverToServerFilter, final Collection<InetSocketAddress> remoteServers) {
         Preconditions.checkNotNull(serverToServerFilter);
         Preconditions.checkNotNull(remoteServers);
         subscribers = new ConcurrentHashMap<String, DefaultChannelGroup>();
         lock = new ReentrantLock();
         this.remoteServers = new LinkedList<PubSubClient>();
         service = Executors.newCachedThreadPool();
         for (final InetSocketAddress remote : remoteServers)
             this.remoteServers.add(new PubSubClient(this, serverToServerFilter, service, null,
                     PubSubClient.DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS, Collections.nCopies(1, remote)));
         logger.trace("Will connect to the following remote servers: {}", remoteServers);
     }
 
     void start() {
         logger.trace("start() called, firing up pub/sub remotes");
         for (final PubSubClient remote : remoteServers)
             remote.start();
         logger.trace("start() finished");
     }
 
     void stop() {
         logger.trace("stop() called, stopping remote connections");
         for (final PubSubClient remote : remoteServers)
             try {
                 remote.stop();
             } catch (final InterruptedException ie) {
                 logger.warn("Caught exception while stopping server-to-server connection", ie);
             }
         logger.trace("stopping executor service");
         service.shutdownNow();
         logger.trace("stop() finished");
     }
 
     @Override
     public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
         final Object o = e.getMessage();
         if (o instanceof ApplicationMessage)
             handleApplicationMessage(ctx.getChannel(), (ApplicationMessage) o);
         else if (o instanceof SubscriptionMessage)
             handleSubscriptionRequest(ctx.getChannel(), (SubscriptionMessage) o);
         else
             super.messageReceived(ctx, e);
     }
 
     /*
      * Broadcast this incoming application message to all subscribers of the topic (except for the one who broadcast it)
      */
     private void handleApplicationMessage(final Channel source, final ApplicationMessage msg) {
         final DefaultChannelGroup group = subscribers.get(msg.topic);
         logger.trace("Incoming application message on topic[{}] from remote {}, channel broadcast group is: {}", new Object[] {
                 msg.topic, source.getRemoteAddress(), group });
         if (group != null) {
             final Iterator<Channel> pos = group.iterator();
             while (pos.hasNext()) {
                 final Channel channel = pos.next();
                 if (channel.getId() != source.getId()) {
                     logger.trace("Broadcasting message to channel[{}]", channel.getRemoteAddress());
                     channel.write(msg);
                 }
             }
         }
         logger.trace("Application message finished broadcasting");
     }
 
     private void handleSubscriptionRequest(final Channel channel, final SubscriptionMessage msg) {
         if (msg.subscribe)
             subscribe(channel, msg.topics);
         else
             unsubscribe(channel, msg.topics);
     }
 
     private void subscribe(final Channel channel, final String... topics) {
         logger.trace("Subscription message on channel[{}] for topics: [{}]",
                 new Object[] { channel.getRemoteAddress(), Arrays.toString(topics) });
         for (final String topic : topics) {
             lock.lock();
             try {
                 final DefaultChannelGroup group;
                 if (subscribers.containsKey(topic))
                     group = subscribers.get(topic);
                 else {
                     group = new DefaultChannelGroup(topic);
                     subscribers.put(topic, group);
                     logger.trace("Creating new subscriber group for topic[{}]", topic);
                 }
                 logger.trace("Subscribing channel[{}] to topic[{}]", new Object[] { channel.getRemoteAddress(), topic });
                 group.add(channel);
             } finally {
                 lock.unlock();
             }
            logger.trace("Subscribing topic[{}] on remote servers to receive forwarded messages", topic);
            for (final PubSubClient remote : remoteServers)
                remote.subscribe(topic);
         }
         logger.trace("Subscription message finished processing");
     }
 
     private void unsubscribe(final Channel channel, final String... topics) {
         logger.trace("[un]Subscription message for channel[{}] on topics[{}]",
                 new Object[] { channel.getRemoteAddress(), Arrays.toString(topics) });
         for (final String topic : topics) {
             lock.lock();
             try {
                 final DefaultChannelGroup group = subscribers.get(topic);
                 if (group != null) {
                     group.remove(channel);
                     if (group.isEmpty()) {
                         logger.trace("Removing empty subscriber group for topic[{}]", topic);
                         subscribers.remove(topic);
                     }
                 }
             } finally {
                 lock.unlock();
             }
            logger.trace("Un-subscribing frmo topic[{}] on remote servers to stop receiving forwarded messages", topic);
            for (final PubSubClient remote : remoteServers)
                remote.unsubscribe(topic);
         }
         logger.trace("[un]Subscription message finished processing");
     }
 
 }
