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
 
 package nerds.antelax.commons.net.pubsub;
 
 import static nerds.antelax.commons.net.NetUtil.hostPortPairsFromString;
 
 import java.net.InetSocketAddress;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.UUID;
 import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.ChannelDownstreamHandler;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.ChannelGroupFuture;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Predicate;
 
 public final class PubSubServer {
 
    public static final InetSocketAddress       DEFAULT_ADDRESS = new InetSocketAddress(6302);
 
     private static final Logger                 logger          = LoggerFactory.getLogger(PubSubServer.class);
 
     private final Collection<InetSocketAddress> listenAddresses;
     private final ExecutorService               bossService;
     private final ExecutorService               workerService;
     private final ChannelFactory                factory;
     private final ServerBootstrap               bootstrap;
     private final ChannelGroup                  openChannels;
 
     private final ServerMessageHandler          sharedMessageHandler;
 
     public PubSubServer(final Collection<InetSocketAddress> listenAddresses, final Collection<InetSocketAddress> otherServers) {
         final Collection<InetSocketAddress> laddrs = new LinkedList<InetSocketAddress>();
         if (listenAddresses != null && !listenAddresses.isEmpty())
             for (final InetSocketAddress address : listenAddresses)
                 laddrs.add(address);
         else
             laddrs.add(DEFAULT_ADDRESS);
         this.listenAddresses = Collections.unmodifiableCollection(laddrs);
         openChannels = new DefaultChannelGroup(getClass().getName());
         bossService = Executors.newCachedThreadPool();
         workerService = Executors.newCachedThreadPool();
         factory = new NioServerSocketChannelFactory(bossService, workerService);
         bootstrap = new ServerBootstrap(factory);
         final UUID ourServerID = UUID.randomUUID();
         logger.info("New server created with ID: {}", ourServerID);
         sharedMessageHandler = new ServerMessageHandler(new Predicate<Object>() {
 
             @Override
             public boolean apply(final Object o) {
                 if (o instanceof Message) {
                     final Message m = (Message) o;
                     return m.ttl() > 0 && !m.serverID().equals(ourServerID);
                 } else
                     return true;
             }
 
         }, otherServers);
         final ChannelDownstreamHandler uuidPopulatingHandler = new SimpleChannelDownstreamHandler() {
 
             @Override
             public void writeRequested(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
                 final Object o = e.getMessage();
                 if (o instanceof Message) {
                     final Message m = (Message) o;
                     if (m.serverID() == null || m.serverID().equals(Message.NO_UUID))
                         m.serverID(ourServerID);
                 }
                 super.writeRequested(ctx, e);
             }
 
         };
         bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
 
             @Override
             public ChannelPipeline getPipeline() {
                 return Channels.pipeline(MessageCodec.decoder(), MessageCodec.encoder(), uuidPopulatingHandler,
                         sharedMessageHandler);
             }
 
         });
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
     }
 
     public void start() {
         sharedMessageHandler.start();
         for (final InetSocketAddress address : listenAddresses) {
             logger.info("Starting listener on {}", address);
             openChannels.add(bootstrap.bind(address));
         }
         logger.info("Server startup complete");
     }
 
     public void stop() throws InterruptedException {
         logger.info("Server shutting down...");
         final ChannelGroupFuture future = openChannels.close();
         try {
             future.await();
         } finally {
             sharedMessageHandler.stop();
             factory.releaseExternalResources();
             workerService.shutdown();
             bossService.shutdown();
         }
         logger.info("Server shut down.");
     }
 
     /**
      * @param args
      *            host:port pairs to listen on, if none given then the default of port 9000 on all interfaces is used.
      * @throws Throwable
      *             if unable to parse command line arguments as host:port pairs
      */
     public static void main(final String[] args) throws Throwable {
         final Collection<InetSocketAddress> listenAddrs = args.length > 0 ? hostPortPairsFromString(args[0],
                 DEFAULT_ADDRESS.getPort()) : new LinkedList<InetSocketAddress>();
         final Collection<InetSocketAddress> remoteAddrs = args.length > 1 ? hostPortPairsFromString(args[1],
                 DEFAULT_ADDRESS.getPort()) : new LinkedList<InetSocketAddress>();
         final PubSubServer pss = new PubSubServer(listenAddrs, remoteAddrs);
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     pss.stop();
                 } catch (final InterruptedException ie) {
                     logger.error("Interrupted while waiting for server to shut down", ie);
                 }
             }
 
         }));
         pss.start();
     }
 }
