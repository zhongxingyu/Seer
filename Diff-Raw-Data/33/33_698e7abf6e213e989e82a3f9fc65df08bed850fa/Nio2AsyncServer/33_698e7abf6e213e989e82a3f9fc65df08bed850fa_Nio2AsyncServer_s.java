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
 
 import java.net.InetSocketAddress;
 import java.net.StandardSocketOptions;
 import java.nio.ByteBuffer;
 import java.nio.channels.AsynchronousChannelGroup;
 import java.nio.channels.AsynchronousServerSocketChannel;
 import java.nio.channels.AsynchronousSocketChannel;
 import java.util.UUID;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.jboss.logging.Logger;
 
 /**
  * {@code NioAsyncServer}
  * 
  * Created on Oct 27, 2011 at 5:47:30 PM
  * 
  * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
  */
 public class Nio2AsyncServer {
 
 	/**
 	 * 
 	 */
 	public static final String CRLF = "\r\n";
 	/**
 	 * 
 	 */
 	public static final int SERVER_PORT = 8081;
 	private static final Logger logger = Logger.getLogger(Nio2AsyncServer.class.getName());
 	protected static final long TIMEOUT = 20;
 	protected static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
 	private static final ExecutorService pool = Executors.newFixedThreadPool(500);
 
 	/**
 	 * Create a new instance of {@code Nio2AsyncServer}
 	 */
 	public Nio2AsyncServer() {
 		super();
 	}
 
 	/**
 	 * Generate a random and unique session Id
 	 * 
 	 * @return a random and unique session Id
 	 */
 	public static String generateId() {
 		UUID uuid = UUID.randomUUID();
 		return uuid.toString();
 	}
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 
 		int port = SERVER_PORT;
 		if (args.length > 0) {
 			try {
 				port = Integer.valueOf(args[0]);
 			} catch (NumberFormatException e) {
 				logger.error(e.getMessage(), e);
 			}
 		}
 
 		logger.infov("Starting NIO2 Synchronous Sever on port {0} ...", port);
 		AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withThreadPool(pool);
 		final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open(
 				threadGroup).bind(new InetSocketAddress(port));
 
 		boolean running = true;
 		logger.info("Asynchronous Sever started...");
 
 		while (running) {
 			logger.info("Waiting for new connections...");
 			Future<AsynchronousSocketChannel> future = listener.accept();
 			final AsynchronousSocketChannel channel = future.get();
 			// Generate a new session id
 			String sessionId = generateId();
 			final ByteBuffer buffer = ByteBuffer.allocate(512);
 			// Initialize the session
 			initSession(channel, buffer, sessionId);
			
			channel.setOption(StandardSocketOptions.SO_SNDBUF, 8*1024);
			
 			channel.read(buffer, TIMEOUT, TIME_UNIT, channel, new ReadCompletionHandler(sessionId,
 					buffer));
 		}
 
 		listener.close();
 	}
 
 	/**
 	 * 
 	 * @param channel
 	 * @param buffer
 	 * @param sessionId
 	 * @throws Exception
 	 */
 	protected static void initSession(AsynchronousSocketChannel channel, ByteBuffer buffer,
 			String sessionId) throws Exception {
 		buffer.clear();
 		Future<Integer> future = channel.read(buffer);
 		int nBytes = future.get();
 		buffer.flip();
 		byte bytes[] = new byte[nBytes];
 		buffer.get(bytes);
 		System.out.println("[" + sessionId + "] " + new String(bytes).trim());
 		String response = "jSessionId: " + sessionId + CRLF;
 		// write initialization response to client
 		buffer.clear();
 		buffer.put(response.getBytes());
 		buffer.flip();
 		channel.write(buffer);
 		buffer.clear();
 	}
 
 }
