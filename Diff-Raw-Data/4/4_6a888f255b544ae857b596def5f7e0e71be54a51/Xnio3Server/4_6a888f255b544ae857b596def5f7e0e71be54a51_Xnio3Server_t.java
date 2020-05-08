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
 package org.jboss.xnio3.server;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channel;
 import java.util.UUID;
 
 import org.jboss.logging.Logger;
 import org.xnio.ChannelListener;
 import org.xnio.ChannelListeners;
 import org.xnio.Option;
 import org.xnio.OptionMap;
 import org.xnio.Options;
 import org.xnio.Xnio;
 import org.xnio.XnioWorker;
 import org.xnio.channels.AcceptingChannel;
 import org.xnio.channels.ConnectedStreamChannel;
 import org.xnio.channels.StreamChannel;
 
 /**
  * {@code Xnio3Server}
  * 
  * Created on Nov 10, 2011 at 3:41:02 PM
  * 
  * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
  */
 public class Xnio3Server {
 
 	private static final Logger logger = Logger.getLogger(Xnio3Server.class.getName());
 	static int counter = 0;
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		int port = XnioUtils.SERVER_PORT;
 		if (args.length > 0) {
 			try {
 				port = Integer.valueOf(args[0]);
 			} catch (NumberFormatException e) {
 				logger.error(e.getMessage(), e);
 			}
 		}
 
 		logger.infov("Starting XNIO3 Server on port {0} ...", port);
 		// Get the Xnio instance
 		final Xnio xnio = Xnio.getInstance("nio", Xnio3Server.class.getClassLoader());
 
 		int cores = Runtime.getRuntime().availableProcessors();
 		logger.infof("Number of cores detected %s", cores);
 
 		// Create the OptionMap for the worker
 		OptionMap optionMap = OptionMap.create(Options.WORKER_WRITE_THREADS, cores,
 				Options.WORKER_READ_THREADS, cores);
 		// Create the worker
 		final XnioWorker worker = xnio.createWorker(optionMap);
 		final SocketAddress address = new InetSocketAddress(port);
 		final ChannelListener<? super AcceptingChannel<ConnectedStreamChannel>> acceptListener = ChannelListeners
 				.openListenerAdapter(new AcceptChannelListenerImpl());
 		// configure the number of worker task max threads
 		worker.setOption(Options.WORKER_TASK_MAX_THREADS, 510);
 
 		final AcceptingChannel<? extends ConnectedStreamChannel> server = worker
 				.createStreamServer(address, acceptListener,
 						OptionMap.create(Options.REUSE_ADDRESSES, Boolean.TRUE));
 		server.resumeAccepts();
 	}
 
 	/**
 	 * Generate a random and unique session ID.
 	 * 
 	 * @return a random and unique session ID
 	 */
 	public static String generateSessionId() {
 		UUID uuid = UUID.randomUUID();
 		return uuid.toString();
 	}
 
 	/**
 	 * 
 	 * @param channel
 	 * @param sessionId
 	 * @throws IOException
 	 */
 	protected static void initSession(StreamChannel channel, String sessionId) throws IOException {
 		ByteBuffer buffer = ByteBuffer.allocate(512);
 		buffer.clear();
 		int nBytes = channel.read(buffer);
 		buffer.flip();
 		byte bytes[] = new byte[nBytes];
 		buffer.get(bytes);
 		System.out.println("[" + sessionId + "] " + new String(bytes).trim());
 		String response = "jSessionId: " + sessionId + XnioUtils.CRLF;
 		// write initialization response to client
 		buffer.clear();
 		buffer.put(response.getBytes());
 		buffer.flip();
 		channel.write(buffer);
 	}
 
 	/**
 	 * {@code AcceptChannelListenerImpl}
 	 * 
 	 * Created on Nov 10, 2011 at 4:03:10 PM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	protected static class AcceptChannelListenerImpl implements ChannelListener<Channel> {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.xnio.ChannelListener#handleEvent(java.nio.channels.Channel)
 		 */
 		public void handleEvent(Channel channel) {
 			logger.info("New connection accepted -> " + (counter++));
 			final StreamChannel streamChannel = (StreamChannel) channel;
 			String sessionId = generateSessionId();
 			try {
 				// Fix the size of the send buffer to 8KB
 				streamChannel.setOption(Options.SEND_BUFFER, 8 * 1024);
 				initSession(streamChannel, sessionId);
 				ReadChannelListener readListener = new ReadChannelListener();
 				readListener.setSessionId(sessionId);
 				CloseChannelListener closeListener = new CloseChannelListener();
 				closeListener.sessionId = sessionId;
 				WriteChannelListener writeListener = new WriteChannelListener();
 				writeListener.setSessionId(sessionId);
 
 				streamChannel.getReadSetter().set(readListener);
 				streamChannel.getWriteSetter().set(writeListener);
 				streamChannel.getCloseSetter().set(closeListener);
				streamChannel.resumeReads();
				streamChannel.resumeWrites();
 			} catch (IOException e) {
 				e.printStackTrace();
 				return;
 			}
 		}
 	}
 
 	/**
 	 * {@code CloseChannelListener}
 	 * 
 	 * Created on Nov 11, 2011 at 1:58:40 PM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	protected static class CloseChannelListener implements ChannelListener<StreamChannel> {
 
 		private String sessionId;
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.xnio.ChannelListener#handleEvent(java.nio.channels.Channel)
 		 */
 		public void handleEvent(StreamChannel channel) {
 			logger.info("Closing remote connection for session: [" + sessionId + "]");
 		}
 	}
 }
