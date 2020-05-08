 /*
  * Copyright (c) 2012, someone All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1.Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2.Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3.Neither the name of the Happyelements Ltd. nor the
  * names of its contributors may be used to endorse or promote products derived
  * from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.happyelements.zero.nio;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.happyelements.zero.nio.NIO.ConnectedListener;
 
 /**
  * a nio server
  * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
  */
 public class NIOServer {
 	private static Log LOGGER = LogFactory.getLog(NIOServer.class);
 
 	/**
 	 * a blocking style io handler,user by IOServer
 	 * 
 	 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 	 */
 	public static abstract class BlockingIOHandler {
 		public static final int ALLOCATE_UNIT = 64;
 		public int ESTIMATE = BlockingIOHandler.ALLOCATE_UNIT;
 
 		protected ConcurrentHashMap<SocketChannel, ByteBuffer> buffers = new ConcurrentHashMap<SocketChannel, ByteBuffer>();
 
 		/**
 		 * the io status
 		 * 
 		 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 		 */
 		public static enum IOStatus {
 			Exception, Done, Closed;
 		}
 
 		/**
 		 * the blocking procedure
 		 * 
 		 * @param channel
 		 *            the channel
 		 * @return the IO status
 		 */
 		public abstract IOStatus blockingIO(SocketChannel channel);
 
 		/**
 		 * remove the associated buffer
 		 * @param channel
 		 * @return
 		 */
 		public ByteBuffer removeAssociatedBuffer(SocketChannel channel) {
 			// check null
 			if (channel == null) {
 				return null;
 			}
 
 			// remove it
 			return this.buffers.remove(channel);
 		}
 
 		/**
 		 * find the channel`s associated buffer
 		 * @param channel
 		 * @return
 		 */
 		public ByteBuffer associateBuffer(SocketChannel channel) {
 			// null check
 			if (channel == null) {
 				return null;
 			}
 
 			// create if needed
 			ByteBuffer buffer = this.buffers.get(channel);
 			if (buffer == null) {
 				ByteBuffer old_buffer = this.buffers.putIfAbsent(channel,
 						buffer = ByteBuffer.allocateDirect(this.ESTIMATE));
 				buffer = old_buffer == null ? buffer : old_buffer;
 			}
 
 			return buffer;
 		}
 
 		/**
 		 * relax the buffer size
 		 * @param buffer
 		 * 		the origin buffer
 		 * @return
 		 * 		the return buffer
 		 */
 		public ByteBuffer relaxBuffer(ByteBuffer buffer) {
 			// null or not full
 			if (buffer == null || buffer.hasRemaining()) {
 				return buffer;
 			}
 
			// have consumed just compact it
			if (buffer.position() > 0) {
				buffer.compact();
				return buffer;
			}

 			// just modify limit
 			if (buffer.limit() < buffer.capacity()) {
 				buffer.limit(buffer.capacity());
 				return buffer;
 			}
 
 			// no more free space, reallocate it
 			// calculate size
 			int size = buffer.limit();
 
 			// should take a snapshot
 			int estimate_snapshot = this.ESTIMATE;
 
 			// allocate new
 			ByteBuffer new_buffer = ByteBuffer
 					.allocateDirect(size < estimate_snapshot ? estimate_snapshot
 							: ((size + BlockingIOHandler.ALLOCATE_UNIT) / BlockingIOHandler.ALLOCATE_UNIT)
 									* BlockingIOHandler.ALLOCATE_UNIT);
 
 			// cut meaningful content
 			buffer.flip();
 
 			// copy buffer
 			new_buffer.put(buffer);
 			return new_buffer;
 		}
 
 		/**
 		 * sample buffer size,utility function,use to hlep estimate buffer size
 		 * @param sample_size
 		 * 		the sample buffer size
 		 */
 		public void sample(int sample_size) {
 			this.ESTIMATE = (((sample_size + this.ESTIMATE) / 2) / BlockingIOHandler.ALLOCATE_UNIT)
 					* BlockingIOHandler.ALLOCATE_UNIT;
 		}
 	}
 
 	// io handler
 	private final BlockingIOHandler io_handler;
 
 	// the NIO object
 	private final NIO nio;
 
 	// the address
 	private final InetSocketAddress address;
 
 	/**
 	 * constructor
 	 * 
 	 * @param address
 	 *            the binding address
 	 * @param io_handler
 	 *            the io handler
 	 * @throws IOException
 	 *             throw when binding fail
 	 */
 	public NIOServer(NIO nio, InetSocketAddress address,
 			BlockingIOHandler io_handler) {
 		// set up address
 		if (address == null) {
 			throw new NullPointerException("provided address is null");
 		}
 		this.address = address;
 
 		// set up nio
 		if (nio == null) {
 			throw new NullPointerException("provided NIO is null");
 		}
 		this.nio = nio;
 
 		// set up block io handler
 		if (io_handler != null) {
 			this.io_handler = io_handler;
 		} else {
 			throw new NullPointerException("provided BlocingIOHandler is null");
 		}
 	}
 
 	/**
 	 * start the server
 	 * @throws IOException 
 	 * 		when binding fail
 	 */
 	public void start() throws IOException {
 		this.nio.start().listen(this.address, new ConnectedListener() {
 			/**
 			 * @see com.happyelements.zero.nio.NIO.ConnectedListener#notifyReadable(java.nio.channels.SocketChannel)
 			 */
 			@Override
 			public void notifyReadable(SocketChannel channel) throws Exception {
 				// got something
 				switch (NIOServer.this.io_handler.blockingIO(channel)) {
 				case Closed:
 				case Exception:
 					NIOServer.this.io_handler.removeAssociatedBuffer(channel);
 					channel.close();
 					break;
 				default:
 				case Done:
 					break;
 				}
 			}
 
 			/**
 			 * @see com.happyelements.zero.nio.NIO.ConnectedListener#notifyException(java.nio.channels.SocketChannel, java.lang.Exception)
 			 */
 			@Override
 			public void notifyException(SocketChannel channel,
 					Exception exception) {
 				NIOServer.LOGGER.error("fail when process channel:" + channel
 						+ " droping it", exception);
 				NIOServer.this.io_handler.removeAssociatedBuffer(channel);
 				try {
 					channel.close();
 				} catch (IOException e) {
 					NIOServer.LOGGER.error("fail to close channel", e);
 				}
 			}
 
 			/**
 			 * @see com.happyelements.zero.nio.NIO.ConnectedListener#notifyConnected(java.nio.channels.SocketChannel)
 			 */
 			@Override
 			public void notifyConnected(SocketChannel channel) {
 				// do nothing
 			}
 		});
 	}
 }
