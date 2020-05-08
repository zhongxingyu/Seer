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
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.net.StandardSocketOptions;
 import java.nio.ByteBuffer;
 import java.nio.channels.AsynchronousSocketChannel;
 import java.nio.channels.CompletionHandler;
 import java.nio.channels.FileChannel;
 import java.util.concurrent.Future;
 
 import org.jboss.logging.Logger;
 import org.jboss.nio2.common.Nio2Utils;
 
 /**
  * {@code ReadCompletionHandler}
  * 
  * Created on Nov 16, 2011 at 8:59:51 PM
  * 
  * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
  */
 class ReadCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
 
 	private static final Logger logger = Logger.getLogger(CompletionHandler.class.getName());
 	private String sessionId;
 	// The read buffer
 	private ByteBuffer readBuffer;
 	// An array of byte buffers for write operations
 	private ByteBuffer writeBuffers[];
 	private long fileLength;
 
 	/**
 	 * Create a new instance of {@code ReadCompletionHandler}
 	 * 
 	 * @param sessionId
 	 * @param byteBuffer
 	 */
 	public ReadCompletionHandler(String sessionId, ByteBuffer byteBuffer) {
 		this.sessionId = sessionId;
 		this.readBuffer = byteBuffer;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.nio.channels.CompletionHandler#completed(java.lang.Object,
 	 * java.lang.Object)
 	 */
 	@Override
 	public void completed(Integer nBytes, AsynchronousSocketChannel channel) {
 		if (nBytes < 0) {
 			try {
 				logger.info("Connection closed remotely");
 				channel.close();
 			} catch (Exception exp) {
 				logger.error(exp.getMessage(), exp);
 			}
 			return;
 		}
 
 		if (nBytes > 0) {
 			readBuffer.flip();
 			byte bytes[] = new byte[nBytes];
 			readBuffer.get(bytes);
 			System.out.println("[" + sessionId + "] " + new String(bytes).trim());
 
 			try {
 				// write response to client
 				writeResponse(channel);
 			} catch (Exception e) {
 				logger.error(e.getMessage(), e);
 				e.printStackTrace();
 			}
 		}
 		// Read again with the this CompletionHandler
 		readBuffer.clear();
 		channel.read(readBuffer, Nio2AsyncServer.TIMEOUT, Nio2AsyncServer.TIME_UNIT, channel, this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.nio.channels.CompletionHandler#failed(java.lang.Throwable,
 	 * java.lang.Object)
 	 */
 	@Override
 	public void failed(Throwable exc, AsynchronousSocketChannel channel) {
 		System.out.println("[" + this.sessionId + "] Read Operation failed");
 		exc.printStackTrace();
 		try {
 			System.out.println("[" + this.sessionId + "] Closing remote connection");
 			channel.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Write the response to client
 	 * 
 	 * @param channel
 	 *            the {@code AsynchronousSocketChannel} channel to which write
 	 * @throws Exception
 	 */
 	protected void writeResponse(AsynchronousSocketChannel channel) throws Exception {
 		try {
 			if (this.writeBuffers == null) {
 				initWriteBuffers();
 			}
 			// Write the file content to the channel
 			write(channel, this.writeBuffers, fileLength);
 		} catch (Exception exp) {
 			logger.error("Exception: " + exp.getMessage(), exp);
 			exp.printStackTrace();
 		}
 	}
 
 	/**
 	 * Flip all the write byte buffers
 	 * 
 	 * @param buffers
 	 */
 	protected static void flipAll(ByteBuffer[] buffers) {
 		for (ByteBuffer bb : buffers) {
 			bb.flip();
 		}
 	}
 
 	/**
 	 * Read the file from HD and initialize the write byte buffers array.
 	 * 
 	 * @throws IOException
 	 */
 	private void initWriteBuffers() throws IOException {
 
		File file = new File("data" + File.separatorChar + "file32k.txt");
 		// File file = new File("data" + File.separatorChar + "file64k.txt");
		// File file = new File("data" + File.separatorChar + "file128k.txt");
 		RandomAccessFile raf = new RandomAccessFile(file, "r");
 		FileChannel fileChannel = raf.getChannel();
 
 		fileLength = fileChannel.size() + Nio2Utils.CRLF.getBytes().length;
 		double tmp = (double) fileLength / Nio2Utils.WRITE_BUFFER_SIZE;
 		int length = (int) Math.ceil(tmp);
 		writeBuffers = new ByteBuffer[length];
 
 		for (int i = 0; i < writeBuffers.length - 1; i++) {
 			writeBuffers[i] = ByteBuffer.allocateDirect(Nio2Utils.WRITE_BUFFER_SIZE);
 		}
 
 		int temp = (int) (fileLength % Nio2Utils.WRITE_BUFFER_SIZE);
 		writeBuffers[writeBuffers.length - 1] = ByteBuffer.allocateDirect(temp);
 		// Read the whole file in one pass
 		fileChannel.read(writeBuffers);
 		// Close the file channel
 		raf.close();
 		// Put the <i>CRLF</i> chars at the end of the last byte buffer to mark
 		// the end of data
 		writeBuffers[writeBuffers.length - 1].put(Nio2Utils.CRLF.getBytes());
 	}
 
 	/**
 	 * 
 	 * @param channel
 	 * @param buffers
 	 * @param length
 	 * @throws Exception
 	 */
 	protected void write(final AsynchronousSocketChannel channel, final ByteBuffer[] buffers,
 			final long total) throws Exception {
 
 		// Flip all the write byte buffers
 		flipAll(buffers);
 
 		int bufferSize = channel.getOption(StandardSocketOptions.SO_SNDBUF);
 		System.out.println("BUFFER SIZE: " + bufferSize);
 		channel.write(buffers, 0, buffers.length, Nio2AsyncServer.TIMEOUT,
 				Nio2AsyncServer.TIME_UNIT, total, new CompletionHandler<Long, Long>() {
 					private int offset = 0;
 					private long written = 0;
 
 					@Override
 					public void completed(Long nBytes, Long total) {
 						System.out.println("[" + sessionId + "] Number of bytes written: " + nBytes
 								+ " from total: " + total);
 						written += nBytes;
 						if (written < total) {
 							offset = (int) (written / buffers[0].capacity());
 							channel.write(buffers, offset, buffers.length - offset,
 									Nio2AsyncServer.TIMEOUT, Nio2AsyncServer.TIME_UNIT, total, this);
 						}
 					}
 
 					@Override
 					public void failed(Throwable exc, Long attachment) {
 						logger.error("WRITE OPERATION FAILED : " + exc.getMessage(), exc);
 						exc.printStackTrace();
 					}
 				});
 	}
 
 	/**
 	 * 
 	 * @param channel
 	 * @param buffers
 	 * @throws Exception
 	 */
 	protected void write(final AsynchronousSocketChannel channel, final ByteBuffer[] buffers)
 			throws Exception {
 		for (ByteBuffer buffer : buffers) {
 			write(channel, buffer);
 		}
 	}
 
 	/**
 	 * Write the byte buffer to the specified channel
 	 * 
 	 * @param channel
 	 *            the {@code AsynchronousSocketChannel} channel
 	 * @param byteBuffer
 	 *            the data that will be written to the channel
 	 * @throws IOException
 	 */
 	protected void write(AsynchronousSocketChannel channel, ByteBuffer byteBuffer) throws Exception {
 		byteBuffer.flip();
 		Future<Integer> count = channel.write(byteBuffer);
 		int written = count.get();
 		byteBuffer.clear();
 	}
 }
