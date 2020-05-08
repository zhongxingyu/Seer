 /**
  * JBoss, Home of Professional Open Source. Copyright 2011, Red Hat, Inc., and
  * individual contributors as indicated by the @author tags. See the
  * copyright.txt file in the distribution for a full listing of individual
  * contributors.
  * 
  * This is free software; you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this software; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
  * site: http://www.fsf.org.
  */
 package org.jboss.nio2.server.async;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.AsynchronousServerSocketChannel;
 import java.nio.channels.AsynchronousSocketChannel;
 import java.nio.channels.CompletionHandler;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jboss.nio2.server.SessionGenerator;
 
 /**
  * {@code NioAsyncServer}
  * 
  * Created on Oct 27, 2011 at 5:47:30 PM
  * 
  * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
  */
 public class Nio2AsyncServer {
 
 	public static final int SERVER_PORT = 8081;
 	private static final Logger logger = Logger.getLogger(Nio2AsyncServer.class.getName());
 
 	/**
 	 * Create a new instance of {@code Nio2AsyncServer}
 	 */
 	public Nio2AsyncServer() {
 		super();
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 */
 	public static void main(String[] args) throws Exception {
 
 		int port = SERVER_PORT;
 		if (args.length > 0) {
 			try {
 				port = Integer.valueOf(args[0]);
 			} catch (NumberFormatException e) {
 				logger.log(Level.SEVERE, e.getMessage(), e);
 			}
 		}
 
 		logger.log(Level.INFO, "Starting NIO2 Synchronous Sever on port {0} ...", port);
 		final ExecutorService pool = Executors.newFixedThreadPool(400);
 		// AsynchronousChannelGroup threadGroup =
 		// AsynchronousChannelGroup.withThreadPool(pool);
 		final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open()
 				.bind(new InetSocketAddress(port));
 
 		boolean running = true;
 		logger.log(Level.INFO, "Asynchronous Sever started...");
 
 		while (running) {
 			// server.accept(null, new CompletionHandlerImpl());
 			//Future<AsynchronousSocketChannel> future = listener.accept();
 			logger.info("Waiting for new connection...");
 			listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
 
 				@Override
 				public void completed(AsynchronousSocketChannel channel, Void attachment) {
					try {
 					Nio2AsyncClientManager manager = new Nio2AsyncClientManager(channel);
 					manager.setSessionId(SessionGenerator.generateId());
 					pool.execute(manager);
 					logger.info("Waiting for new connection...");
 					listener.accept(attachment, this);
					} catch(Exception e) {
						logger.log(Level.SEVERE, e.getMessage(), e);
					}
 				}
 
 				@Override
 				public void failed(Throwable exc, Void attachment) {
 
 				}
 			});
 
 			/*
 			 * Nio2AsyncClientManager manager = new
 			 * Nio2AsyncClientManager(future.get());
 			 * manager.setSessionId(SessionGenerator.generateId());
 			 * pool.execute(manager);
 			 */
 		}
 
 		listener.close();
 	}
 
 	/**
 	 * {@code CompletionHandlerImpl}
 	 * 
 	 * Created on Oct 28, 2011 at 4:09:21 PM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	protected static class CompletionHandlerImpl implements
 			CompletionHandler<AsynchronousSocketChannel, Void> {
 
 		ByteBuffer bb = ByteBuffer.allocate(1024);
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.nio.channels.CompletionHandler#completed(java.lang.Object,
 		 * java.lang.Object)
 		 */
 		@Override
 		public void completed(AsynchronousSocketChannel channel, Void attachment) {
 
 			int count = -1;
 			try {
 				while (true) {
 					bb.clear();
 					count = channel.read(bb).get();
 					bb.flip();
 
 					byte bytes[] = new byte[count];
 					bb.get(bytes);
 					System.out.println("Request from client : " + new String(bytes));
 					bb.clear();
 					bb.put("Pong from server".getBytes());
 					bb.flip();
 					channel.write(bb);
 				}
 			} catch (Exception exp) {
 				logger.log(Level.SEVERE, "ERROR from client side");
 			} finally {
 				try {
 					channel.close();
 				} catch (IOException ex) {
 					logger.log(Level.SEVERE, "ERROR from server side", ex);
 				}
 			}
 
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.nio.channels.CompletionHandler#failed(java.lang.Throwable,
 		 * java.lang.Object)
 		 */
 		@Override
 		public void failed(Throwable exc, Void attachment) {
 			logger.log(Level.SEVERE, exc.getMessage(), exc);
 
 		}
 	}
 
 }
