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
 package org.apache.tomcat.util.net;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.net.SocketOption;
 import java.nio.ByteBuffer;
 import java.nio.channels.AlreadyBoundException;
 import java.nio.channels.AlreadyConnectedException;
 import java.nio.channels.AsynchronousByteChannel;
 import java.nio.channels.AsynchronousChannelGroup;
 import java.nio.channels.AsynchronousCloseException;
 import java.nio.channels.AsynchronousServerSocketChannel;
 import java.nio.channels.AsynchronousSocketChannel;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.CompletionHandler;
 import java.nio.channels.ConnectionPendingException;
 import java.nio.channels.InterruptedByTimeoutException;
 import java.nio.channels.NetworkChannel;
 import java.nio.channels.NotYetConnectedException;
 import java.nio.channels.ReadPendingException;
 import java.nio.channels.ShutdownChannelGroupException;
 import java.nio.channels.UnresolvedAddressException;
 import java.nio.channels.UnsupportedAddressTypeException;
 import java.nio.channels.WritePendingException;
 import java.nio.channels.spi.AsynchronousChannelProvider;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * {@code NioChannel}
  * 
  * <p>
  * An asynchronous channel for stream-oriented connecting sockets.
  * </p>
  * <p>
  * NIO Asynchronous socket channels are created in one of two ways. A
  * newly-created {@code NioChannel} is created by invoking one of the
  * {@link #open open} methods defined by this class. A newly-created channel is
  * open but not yet connected. A connected {@code NioChannel} is created when a
  * connection is made to the socket of an
  * {@link AsynchronousServerSocketChannel}. It is not possible to create an
  * asynchronous socket channel for an arbitrary, pre-existing
  * {@link java.net.Socket socket}.
  * </p>
  * <p>
  * A newly-created channel is connected by invoking its {@link #connect connect}
  * method; once connected, a channel remains connected until it is closed.
  * Whether or not a socket channel is connected may be determined by invoking
  * its {@link #getRemoteAddress getRemoteAddress} method. An attempt to invoke
  * an I/O operation upon an unconnected channel will cause a
  * {@link NotYetConnectedException} to be thrown.
  * </p>
  * <p>
  * Channels of this type are safe for use by multiple concurrent threads. They
  * support concurrent reading and writing, though at most one read operation and
  * one write operation can be outstanding at any time. If a thread initiates a
  * read operation before a previous read operation has completed then a
  * {@link ReadPendingException} will be thrown. Similarly, an attempt to
  * initiate a write operation before a previous write has completed will throw a
  * {@link WritePendingException}.
  * </p>
  * <p>
  * Socket options are configured using the
  * {@link #setOption(SocketOption,Object) setOption} method. NIO Asynchronous
  * socket channels support the following options:
  * </p>
  * <blockquote>
  * <table border>
  * <tr>
  * <th>Option Name</th>
  * <th>Description</th>
  * </tr>
  * <tr>
  * <td> {@link java.net.StandardSocketOptions#SO_SNDBUF SO_SNDBUF}</td>
  * <td>The size of the socket send buffer</td>
  * </tr>
  * <tr>
  * <td> {@link java.net.StandardSocketOptions#SO_RCVBUF SO_RCVBUF}</td>
  * <td>The size of the socket receive buffer</td>
  * </tr>
  * <tr>
  * <td> {@link java.net.StandardSocketOptions#SO_KEEPALIVE SO_KEEPALIVE}</td>
  * <td>Keep connection alive</td>
  * </tr>
  * <tr>
  * <td> {@link java.net.StandardSocketOptions#SO_REUSEADDR SO_REUSEADDR}</td>
  * <td>Re-use address</td>
  * </tr>
  * <tr>
  * <td> {@link java.net.StandardSocketOptions#TCP_NODELAY TCP_NODELAY}</td>
  * <td>Disable the Nagle algorithm</td>
  * </tr>
  * </table>
  * </blockquote> Additional (implementation specific) options may also be
  * supported.
  * 
  * <h4>Timeouts</h4>
  * 
  * <p>
  * The {@link #read(ByteBuffer,long,TimeUnit,Object,CompletionHandler) read} and
  * {@link #write(ByteBuffer,long,TimeUnit,Object,CompletionHandler) write}
  * methods defined by this class allow a timeout to be specified when initiating
  * a read or write operation. If the timeout elapses before an operation
  * completes then the operation completes with the exception
  * {@link java.nio.channels.InterruptedByTimeoutException}. A timeout may leave
  * the channel, or the underlying connection, in an inconsistent state. Where
  * the implementation cannot guarantee that bytes have not been read from the
  * channel then it puts the channel into an implementation specific
  * <em>error state</em>. A subsequent attempt to initiate a {@code read}
  * operation causes an unspecified runtime exception to be thrown. Similarly if
  * a {@code write} operation times out and the implementation cannot guarantee
  * bytes have not been written to the channel then further attempts to
  * {@code write} to the channel cause an unspecified runtime exception to be
  * thrown. When a timeout elapses then the state of the
  * {@link java.nio.ByteBuffer}, or the sequence of buffers, for the I/O
  * operation is not defined. Buffers should be discarded or at least care must
  * be taken to ensure that the buffers are not accessed while the channel
  * remains open. All methods that accept timeout parameters treat values less
  * than or equal to zero to mean that the I/O operation does not timeout.
  * </p>
  * 
  * Created on Dec 19, 2011 at 11:40:18 AM
  * 
  * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
  */
 public class NioChannel implements AsynchronousByteChannel, NetworkChannel {
 
 	/**
 	 * 
 	 */
 	public static final int OP_STATUS_NORMAL = 0;
 
 	/**
 	 * Channel closed status code
 	 */
 	public static final int OP_STATUS_CLOSED = -1;
 	/**
 	 * Read timeout status code
 	 */
 	public static final int OP_STATUS_READ_TIMEOUT = -2;
 	/**
 	 * Write timeout status code
 	 */
 	public static final int OP_STATUS_WRITE_TIMEOUT = -3;
 
 	/**
 	 * Read pending status code
 	 */
 	public static final int OP_STATUS_READ_PENDING = -4;
 
 	/**
 	 * Write pending status code
 	 */
 	public static final int OP_STATUS_WRITE_PENDING = -5;
 
 	/**
 	 * 
 	 */
 	public static final int OP_STATUS_READ_KILLED = -6;
 
 	/**
 	 * 
 	 */
 	public static final int OP_STATUS_WRITE_KILLED = -7;
 
 	/**
 	 * Read/write operation error code
 	 */
 	public static final int OP_STATUS_ERROR = -17;
 
 	private static final AtomicLong counter = new AtomicLong(0);
 	protected AsynchronousSocketChannel channel;
 	private long id;
 	private ByteBuffer buffer;
 	private volatile boolean reading = false;
 	private volatile boolean writing = false;
 
 	/**
 	 * Create a new instance of {@code NioChannel}
 	 * 
 	 * @param channel
 	 *            The {@link java.nio.channels.AsynchronousSocketChannel}
 	 *            attached to this channel
 	 * @throws NullPointerException
 	 *             if the channel parameter is null
 	 */
 	protected NioChannel(AsynchronousSocketChannel channel) {
 		if (channel == null) {
 			throw new NullPointerException("null channel parameter");
 		}
 		this.channel = channel;
 		this.id = counter.getAndIncrement();
 		this.buffer = ByteBuffer.allocateDirect(1);
 	}
 
 	/**
 	 * Opens an asynchronous socket channel.
 	 * 
 	 * <p>
 	 * The new channel is created by invoking the
 	 * {@link AsynchronousChannelProvider#openAsynchronousSocketChannel
 	 * openAsynchronousSocketChannel} method on the
 	 * {@link AsynchronousChannelProvider} that created the group. If the group
 	 * parameter is {@code null} then the resulting channel is created by the
 	 * system-wide default provider, and bound to the <em>default group</em>.
 	 * 
 	 * @param group
 	 *            The group to which the newly constructed channel should be
 	 *            bound, or {@code null} for the default group
 	 * 
 	 * @return A new asynchronous socket channel
 	 * 
 	 * @throws ShutdownChannelGroupException
 	 *             If the channel group is shutdown
 	 * @throws IOException
 	 *             If an I/O error occurs
 	 */
 	public static NioChannel open(AsynchronousChannelGroup group) throws IOException {
 		AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(group);
 		return new NioChannel(channel);
 	}
 
 	/**
 	 * Opens a {@code NioChannel}.
 	 * 
 	 * <p>
 	 * This method returns an {@code NioChannel} that is bound to the
 	 * <em>default group</em>.This method is equivalent to evaluating the
 	 * expression: <blockquote>
 	 * 
 	 * <pre>
 	 * open((AsynchronousChannelGroup) null);
 	 * </pre>
 	 * 
 	 * </blockquote>
 	 * 
 	 * @return A new {@code NioChannel}
 	 * 
 	 * @throws IOException
 	 *             If an I/O error occurs
 	 */
 	public static NioChannel open() throws IOException {
 		return open(null);
 	}
 
 	/**
 	 * Binds the channel's socket to a local address.
 	 * 
 	 * <p>
 	 * This method is used to establish an association between the socket and a
 	 * local address. Once an association is established then the socket remains
 	 * bound until the channel is closed. If the {@code local} parameter has the
 	 * value {@code null} then the socket will be bound to an address that is
 	 * assigned automatically.
 	 * 
 	 * @param local
 	 *            The address to bind the socket, or {@code null} to bind the
 	 *            socket to an automatically assigned socket address
 	 * 
 	 * @return This channel
 	 * 
 	 * @throws AlreadyBoundException
 	 *             If the socket is already bound
 	 * @throws UnsupportedAddressTypeException
 	 *             If the type of the given address is not supported
 	 * @throws ClosedChannelException
 	 *             If the channel is closed
 	 * @throws IOException
 	 *             If some other I/O error occurs
 	 * @throws SecurityException
 	 *             If a security manager is installed and it denies an
 	 *             unspecified permission. An implementation of this interface
 	 *             should specify any required permissions.
 	 * 
 	 * @see #getLocalAddress
 	 */
 	public NioChannel bind(SocketAddress local) throws IOException {
 		this.channel.bind(local);
 		return this;
 	}
 
 	/**
 	 * Binds the channel's socket to a local address.
 	 * 
 	 * <p>
 	 * This method is used to establish an association between the socket and a
 	 * local address. Once an association is established then the socket remains
 	 * bound until the channel is closed.
 	 * 
 	 * @param hostname
 	 *            the Host name
 	 * @param port
 	 *            The port number
 	 * 
 	 * @return This channel
 	 * @throws AlreadyBoundException
 	 *             If the socket is already bound
 	 * @throws UnsupportedAddressTypeException
 	 *             If the type of the given address is not supported
 	 * @throws ClosedChannelException
 	 *             If the channel is closed
 	 * @throws IOException
 	 *             If some other I/O error occurs
 	 * @throws IllegalArgumentException
 	 *             if the port parameter is outside the range of valid port
 	 *             values, or if the hostname parameter is <TT>null</TT>.
 	 * @throws SecurityException
 	 *             If a security manager is installed and it denies an
 	 *             unspecified permission. An implementation of this interface
 	 *             should specify any required permissions.
 	 * 
 	 * @see #bind(SocketAddress)
 	 */
 	public NioChannel bind(String hostname, int port) throws IOException {
 		return bind(new InetSocketAddress(hostname, port));
 	}
 
 	/**
 	 * Connects this channel.
 	 * 
 	 * <p>
 	 * This method initiates an operation to connect this channel. The
 	 * {@code handler} parameter is a completion handler that is invoked when
 	 * the connection is successfully established or connection cannot be
 	 * established. If the connection cannot be established then the channel is
 	 * closed.
 	 * 
 	 * <p>
 	 * This method performs exactly the same security checks as the
 	 * {@link java.net.Socket} class. That is, if a security manager has been
 	 * installed then this method verifies that its
 	 * {@link java.lang.SecurityManager#checkConnect checkConnect} method
 	 * permits connecting to the address and port number of the given remote
 	 * endpoint.
 	 * 
 	 * @param remote
 	 *            The remote address to which this channel is to be connected
 	 * @param attachment
 	 *            The object to attach to the I/O operation; can be {@code null}
 	 * @param handler
 	 *            The handler for consuming the result
 	 * 
 	 * @throws UnresolvedAddressException
 	 *             If the given remote address is not fully resolved
 	 * @throws UnsupportedAddressTypeException
 	 *             If the type of the given remote address is not supported
 	 * @throws AlreadyConnectedException
 	 *             If this channel is already connected
 	 * @throws ConnectionPendingException
 	 *             If a connection operation is already in progress on this
 	 *             channel
 	 * @throws ShutdownChannelGroupException
 	 *             If the channel group has terminated
 	 * @throws SecurityException
 	 *             If a security manager has been installed and it does not
 	 *             permit access to the given remote endpoint
 	 * 
 	 * @see #getRemoteAddress
 	 */
 	public <A> void connect(SocketAddress remote, A attachment,
 			CompletionHandler<Void, ? super A> handler) {
 		this.channel.connect(remote, attachment, handler);
 	}
 
 	/**
 	 * Connects this channel.
 	 * 
 	 * <p>
 	 * This method initiates an operation to connect this channel. This method
 	 * behaves in exactly the same manner as the
 	 * {@link #connect(SocketAddress, Object, CompletionHandler)} method except
 	 * that instead of specifying a completion handler, this method returns a
 	 * {@code Future} representing the pending result. The {@code Future}'s
 	 * {@link Future#get() get} method returns {@code null} on successful
 	 * completion.
 	 * 
 	 * @param remote
 	 *            The remote address to which this channel is to be connected
 	 * 
 	 * @return A {@code Future} object representing the pending result
 	 * 
 	 * @throws UnresolvedAddressException
 	 *             If the given remote address is not fully resolved
 	 * @throws UnsupportedAddressTypeException
 	 *             If the type of the given remote address is not supported
 	 * @throws AlreadyConnectedException
 	 *             If this channel is already connected
 	 * @throws ConnectionPendingException
 	 *             If a connection operation is already in progress on this
 	 *             channel
 	 * @throws SecurityException
 	 *             If a security manager has been installed and it does not
 	 *             permit access to the given remote endpoint
 	 */
 	public Future<Void> connect(SocketAddress remote) {
 		return this.channel.connect(remote);
 	}
 
 	/**
 	 * Reset the internal buffer
 	 */
 	protected void reset() {
 		this.buffer.clear();
 	}
 
 	/**
 	 * Retrieve data, if any, from the internal buffer, put it in the
 	 * <tt>dst</tt> buffer and clear the internal buffer
 	 * 
 	 * @param dst
 	 *            the destination buffer
 	 * @return the number of bytes retrieved
 	 */
 	protected int reset(final ByteBuffer dst) {
 		this.buffer.flip();
 		int x = this.buffer.remaining();
 		dst.put(this.buffer);
 		this.buffer.clear();
 
 		return x;
 	}
 
 	/**
 	 * @return <tt>TRUE</tt> if the channel is secure (i.e., use SSL), else
 	 *         <tt>FALSE</tt>
 	 */
 	public boolean isSecure() {
 		return false;
 	}
 
 	/**
 	 * @return the internal buffer
 	 */
 	protected ByteBuffer getBuffer() {
 		return this.buffer;
 	}
 
 	/**
 	 * @return the channel id
 	 */
 	public long getId() {
 		return this.id;
 	}
 
 	/**
 	 * Getter for name
 	 * 
 	 * @return the name
 	 */
 	public String getName() {
 		return getClass().getName() + "[" + getId() + "]";
 	}
 
 	/**
 	 * Returns the provider that created this channel.
 	 * 
 	 * @return the channel provider
 	 */
 	public final AsynchronousChannelProvider provider() {
 		return this.channel.provider();
 	}
 
 	/**
 	 * Tells whether or not this channel is open. </p>
 	 * 
 	 * @return <tt>true</tt> if, and only if, this channel is open
 	 */
 	@Override
 	public boolean isOpen() {
 		return this.channel.isOpen();
 	}
 
 	/**
 	 * Tells whether or not this channel is closed. </p>
 	 * 
 	 * @return <tt>true</tt> if, and only if, this channel is closed
 	 */
 	public boolean isClosed() {
 		return !isOpen();
 	}
 
 	/**
 	 * Closes this channel.
 	 * 
 	 * <p>
 	 * Any outstanding asynchronous operations upon this channel will complete
 	 * with the exception {@link AsynchronousCloseException}. After a channel is
 	 * closed, further attempts to initiate asynchronous I/O operations complete
 	 * immediately with cause {@link ClosedChannelException}.
 	 * </p>
 	 * <p>
 	 * This method otherwise behaves exactly as specified by the
 	 * {@link java.nio.channels.Channel} interface.
 	 * </p>
 	 * 
 	 * @throws IOException
 	 *             If an I/O error occurs
 	 */
 	@Override
 	public void close() throws IOException {
		System.out.println("Closing " + this);
 		this.channel.close();
 	}
 
 	/**
 	 * Try to close this channel. If the channel is already closed or the
 	 * {@code force} parameter is false, nothing will happen. This method has an
 	 * impact only if the channel is open and the {@code force} parameter is
 	 * <tt>true</tt>
 	 * 
 	 * @param force
 	 *            a boolean value indicating if we need to force closing the
 	 *            channel
 	 * @throws IOException
 	 */
 	public void close(boolean force) throws IOException {
 		if (isOpen() && force) {
 			this.close();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.nio.channels.AsynchronousByteChannel#read(java.nio.ByteBuffer)
 	 * 
 	 * @deprecated (use readBytes(...) instead)
 	 */
 	@Deprecated
 	@Override
 	public Future<Integer> read(ByteBuffer dst) {
 		if (reading) {
 			throw new ReadPendingException();
 		}
 
 		disableReading();
 		this.reset(dst);
 		Future<Integer> f = this.channel.read(dst);
 		enableReading();
 		return f;
 	}
 
 	/**
 	 * Read a sequence of bytes in blocking mode. This method works exactly in
 	 * the same way as
 	 * <code>readBytes(dst, Integer.MAX_VALUE, TimeUnit.MILLISECONDS)</code>.
 	 * 
 	 * @param dst
 	 *            the buffer containing the read bytes
 	 * @return the number of bytes read
 	 * @throws Exception
 	 * @throws ExecutionException
 	 * @throws InterruptedException
 	 */
 	public int readBytes(ByteBuffer dst) throws Exception {
 		return this.readBytes(dst, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
 	}
 
 	/**
 	 * Read a sequence of bytes in blocking mode
 	 * 
 	 * @param dst
 	 *            the buffer containing the read bytes
 	 * @param timeout
 	 *            the read timeout
 	 * @param unit
 	 *            the timeout unit
 	 * @return The possible returned values are :
 	 *         <ul>
 	 *         <li>The number of bytes read if the operation was succeed</li>
 	 *         <li>{@link NioChannel#OP_STATUS_CLOSED} if the channel is closed</li>
 	 *         <li>{@link NioChannel#OP_STATUS_READ_TIMEOUT} if the operation
 	 *         was timed out</li>
 	 *         </ul>
 	 * @throws Exception
 	 * @throws ExecutionException
 	 * @throws InterruptedException
 	 */
 	public int readBytes(final ByteBuffer dst, final long timeout, final TimeUnit unit)
 			throws Exception {
 		if (this.reading) {
 			throw new ReadPendingException();
 		}
 		try {
 			disableReading();
 			int x = this.reset(dst);
 			return (x + this.channel.read(dst).get(timeout, unit));
 		} catch (Exception exp) {
 			if (exp instanceof ClosedChannelException) {
 				return OP_STATUS_CLOSED;
 			} else if (exp instanceof TimeoutException) {
 				return OP_STATUS_READ_TIMEOUT;
 			} else {
 				throw exp;
 			}
 		} finally {
 			enableReading();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.nio.channels.AsynchronousByteChannel#read(java.nio.ByteBuffer,
 	 * java.lang.Object, java.nio.channels.CompletionHandler)
 	 */
 	@Override
 	public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
 		this.read(dst, 0L, TimeUnit.MILLISECONDS, attachment, handler);
 	}
 
 	/**
 	 * Reads a sequence of bytes from this channel into the given buffer.
 	 * 
 	 * <p>
 	 * This method initiates an asynchronous read operation to read a sequence
 	 * of bytes from this channel into the given buffer. The {@code handler}
 	 * parameter is a completion handler that is invoked when the read operation
 	 * completes (or fails). The result passed to the completion handler is the
 	 * number of bytes read or {@code -1} if no bytes could be read because the
 	 * channel has reached end-of-stream.
 	 * 
 	 * <p>
 	 * If a timeout is specified and the timeout elapses before the operation
 	 * completes then the operation completes with the exception
 	 * {@link InterruptedByTimeoutException}. Where a timeout occurs, and the
 	 * implementation cannot guarantee that bytes have not been read, or will
 	 * not be read from the channel into the given buffer, then further attempts
 	 * to read from the channel will cause an unspecific runtime exception to be
 	 * thrown.
 	 * 
 	 * <p>
 	 * Otherwise this method works in the same manner as the
 	 * {@link NioChannel#read(ByteBuffer,Object,CompletionHandler)} method.
 	 * 
 	 * @param dst
 	 *            The buffer into which bytes are to be transferred
 	 * @param timeout
 	 *            The maximum time for the I/O operation to complete
 	 * @param unit
 	 *            The time unit of the {@code timeout} argument
 	 * @param attachment
 	 *            The object to attach to the I/O operation; can be {@code null}
 	 * @param handler
 	 *            The handler for consuming the result
 	 * 
 	 * @throws IllegalArgumentException
 	 *             If the buffer is read-only
 	 * @throws ReadPendingException
 	 *             If a read operation is already in progress on this channel
 	 * @throws NotYetConnectedException
 	 *             If this channel is not yet connected
 	 * @throws ShutdownChannelGroupException
 	 *             If the channel group has terminated
 	 */
 	public <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment,
 			final CompletionHandler<Integer, ? super A> handler) {
 		if (this.reading) {
 			throw new ReadPendingException();
 		}
 		// Disable reading
 		disableReading();
 		final int x = this.reset(dst);
 		this.channel.read(dst, timeout, unit, attachment, new CompletionHandler<Integer, A>() {
 
 			@Override
 			public void completed(Integer result, A attach) {
 				// Enable reading
 				enableReading();
 				handler.completed(result + x, attach);
 			}
 
 			@Override
 			public void failed(Throwable exc, A attach) {
 				// Enable reading
 				enableReading();
 				handler.failed(exc, attach);
 			}
 		});
 	}
 
 	/**
 	 * Reads a sequence of bytes from this channel into a subsequence of the
 	 * given buffers. This operation, sometimes called a
 	 * <em>scattering read</em>, is often useful when implementing network
 	 * protocols that group data into segments consisting of one or more
 	 * fixed-length headers followed by a variable-length body. The
 	 * {@code handler} parameter is a completion handler that is invoked when
 	 * the read operation completes (or fails). The result passed to the
 	 * completion handler is the number of bytes read or {@code -1} if no bytes
 	 * could be read because the channel has reached end-of-stream.
 	 * 
 	 * <p>
 	 * This method initiates a read of up to <i>r</i> bytes from this channel,
 	 * where <i>r</i> is the total number of bytes remaining in the specified
 	 * subsequence of the given buffer array, that is,
 	 * 
 	 * <blockquote>
 	 * 
 	 * <pre>
 	 * dsts[offset].remaining()
 	 *     + dsts[offset+1].remaining()
 	 *     + ... + dsts[offset+length-1].remaining()
 	 * </pre>
 	 * 
 	 * </blockquote>
 	 * 
 	 * at the moment that the read is attempted.
 	 * 
 	 * <p>
 	 * Suppose that a byte sequence of length <i>n</i> is read, where <tt>0</tt>
 	 * &nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>. Up
 	 * to the first <tt>dsts[offset].remaining()</tt> bytes of this sequence are
 	 * transferred into buffer <tt>dsts[offset]</tt>, up to the next
 	 * <tt>dsts[offset+1].remaining()</tt> bytes are transferred into buffer
 	 * <tt>dsts[offset+1]</tt>, and so forth, until the entire byte sequence is
 	 * transferred into the given buffers. As many bytes as possible are
 	 * transferred into each buffer, hence the final position of each updated
 	 * buffer, except the last updated buffer, is guaranteed to be equal to that
 	 * buffer's limit. The underlying operating system may impose a limit on the
 	 * number of buffers that may be used in an I/O operation. Where the number
 	 * of buffers (with bytes remaining), exceeds this limit, then the I/O
 	 * operation is performed with the maximum number of buffers allowed by the
 	 * operating system.
 	 * 
 	 * <p>
 	 * If a timeout is specified and the timeout elapses before the operation
 	 * completes then it completes with the exception
 	 * {@link InterruptedByTimeoutException}. Where a timeout occurs, and the
 	 * implementation cannot guarantee that bytes have not been read, or will
 	 * not be read from the channel into the given buffers, then further
 	 * attempts to read from the channel will cause an unspecific runtime
 	 * exception to be thrown.
 	 * 
 	 * @param dsts
 	 *            The buffers into which bytes are to be transferred
 	 * @param offset
 	 *            The offset within the buffer array of the first buffer into
 	 *            which bytes are to be transferred; must be non-negative and no
 	 *            larger than {@code dsts.length}
 	 * @param length
 	 *            The maximum number of buffers to be accessed; must be
 	 *            non-negative and no larger than {@code dsts.length - offset}
 	 * @param timeout
 	 *            The maximum time for the I/O operation to complete
 	 * @param unit
 	 *            The time unit of the {@code timeout} argument
 	 * @param attachment
 	 *            The object to attach to the I/O operation; can be {@code null}
 	 * @param handler
 	 *            The handler for consuming the result
 	 * 
 	 * @throws IndexOutOfBoundsException
 	 *             If the pre-conditions for the {@code offset} and
 	 *             {@code length} parameter aren't met
 	 * @throws IllegalArgumentException
 	 *             If the buffer is read-only
 	 * @throws ReadPendingException
 	 *             If a read operation is already in progress on this channel
 	 * @throws NotYetConnectedException
 	 *             If this channel is not yet connected
 	 * @throws ShutdownChannelGroupException
 	 *             If the channel group has terminated
 	 */
 	public <A> void read(ByteBuffer[] dsts, int offset, int length, long timeout, TimeUnit unit,
 			A attachment, final CompletionHandler<Long, ? super A> handler) {
 		if (reading) {
 			throw new ReadPendingException();
 		}
 
 		disableReading();
 		// Retrieve bytes, if any, from the internal buffer
 		final int x = this.reset(dsts[0]);
 
 		this.channel.read(dsts, offset, length, timeout, unit, attachment,
 				new CompletionHandler<Long, A>() {
 
 					@Override
 					public void completed(Long result, A attach) {
 						enableReading();
 						handler.completed(result + x, attach);
 					}
 
 					@Override
 					public void failed(Throwable exc, A attach) {
 						enableReading();
 						handler.failed(exc, attach);
 					}
 				});
 	}
 
 	/**
 	 * <p>
 	 * Wait for incoming data in a non-blocking mode. The received data will be
 	 * stored by default in the internal buffer (By default, just one byte).
 	 * </p>
 	 * <p>
 	 * The byte read using this method will be available for the next read
 	 * operation. That is, When attempting to read after receiving a read
 	 * notification saying that there is data available on this stream, the byte
 	 * will be first copied in the destination byte buffer and then perform the
 	 * read operation which may be a blocking or non-blocking operation. So the
 	 * user does not have to do that manually.
 	 * </p>
 	 * 
 	 * @param timeout
 	 *            The maximum time for the I/O operation to complete
 	 * @param unit
 	 *            The time unit of the {@code timeout} argument
 	 * @param attachment
 	 *            The object to attach to the I/O operation; can be {@code null}
 	 * @param handler
 	 *            The handler for consuming the result
 	 * @throws ReadPendingException
 	 *             If a read operation is already in progress on this channel
 	 * @throws NotYetConnectedException
 	 *             If this channel is not yet connected
 	 * @throws ShutdownChannelGroupException
 	 *             If the channel group has terminated
 	 */
 	public <A> void awaitRead(long timeout, TimeUnit unit, final A attachment,
 			final CompletionHandler<Integer, ? super A> handler) {
 		if (this.reading) {
 			throw new ReadPendingException();
 		}
 
 		disableReading();
 		// Clear the internal buffer
 		this.buffer.clear();
 
 		// Perform an asynchronous read operation using the internal buffer
 		this.channel.read(this.buffer, timeout, unit, attachment,
 				new CompletionHandler<Integer, A>() {
 
 					@Override
 					public void completed(Integer result, A attachment) {
 						enableReading();
 						handler.completed(result, attachment);
 					}
 
 					@Override
 					public void failed(Throwable exc, A attachment) {
 						enableReading();
 						handler.failed(exc, attachment);
 					}
 				});
 	}
 
 	/**
 	 * <p>
 	 * Wait for incoming data in a non-blocking mode. The received data will be
 	 * stored by default in the internal buffer (By default, just one byte).
 	 * </p>
 	 * 
 	 * <p>
 	 * This method behaves exactly in the same manner as
 	 * {@link #awaitRead(long, TimeUnit, Object, CompletionHandler)} but without
 	 * a timeout defined.<br/>
 	 * <code>awaitRead(0L, TimeUnit.MILLISECONDS, attachment, handler);</code>
 	 * </p>
 	 * 
 	 * @param attachment
 	 * @param handler
 	 * @see #awaitRead(long, TimeUnit, Object, CompletionHandler)
 	 */
 	public <A> void awaitRead(final A attachment,
 			final CompletionHandler<Integer, ? super A> handler) {
 		awaitRead(0L, TimeUnit.MILLISECONDS, attachment, handler);
 	}
 
 	/**
 	 * Write a sequence of bytes to this channel from the given buffer.
 	 * 
 	 * @param src
 	 *            The buffer from which bytes are to be retrieved
 	 * @return an instance of {@link java.util.concurrent.Future} containing the
 	 *         number of bytes written
 	 * @see java.nio.channels.AsynchronousByteChannel#write(java.nio.ByteBuffer)
 	 * @deprecated (use writeBytes(...) instead)
 	 */
 	@Deprecated
 	@Override
 	public Future<Integer> write(ByteBuffer src) {
 		if (writing) {
 			throw new WritePendingException();
 		}
 
 		disableWriting();
 		Future<Integer> future = this.channel.write(src);
 		enableWriting();
 		return future;
 	}
 
 	/**
 	 * Writes a sequence of bytes to this channel from the given buffer.
 	 * 
 	 * <p>
 	 * This method initiates an asynchronous write operation to write a sequence
 	 * of bytes to this channel from the given buffer. The method behaves in
 	 * exactly the same manner as the
 	 * {@link #writeBytes(ByteBuffer, long, TimeUnit)} with
 	 * {@link java.lang.Integer#MAX_VALUE} as a timeout and
 	 * {@link java.util.concurrent.TimeUnit#MILLISECONDS} as a time unit.
 	 * 
 	 * @param src
 	 *            The buffer from which bytes are to be retrieved
 	 * 
 	 * @return The number of bytes written
 	 * @see #writeBytes(ByteBuffer, long, TimeUnit)
 	 * @throws WritePendingException
 	 *             If the channel does not allow more than one write to be
 	 *             outstanding and a previous write has not completed
 	 * @throws ExecutionException
 	 * @throws InterruptedException
 	 * @throws Exception
 	 *             If any other type of errors occurs
 	 */
 	public int writeBytes(ByteBuffer src) throws Exception {
 		return writeBytes(src, Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
 	}
 
 	/**
 	 * Write a sequence of bytes in blocking mode.
 	 * 
 	 * @param src
 	 *            the buffer containing the bytes to write
 	 * @param timeout
 	 *            the read timeout
 	 * @param unit
 	 *            the timeout unit
 	 * @return The possible returned values are :
 	 *         <ul>
 	 *         <li>The number of bytes written if the operation was succeed</li>
 	 *         <li>{@link NioChannel#OP_STATUS_CLOSED} if the channel is closed</li>
 	 *         <li>{@link NioChannel#OP_STATUS_WRITE_TIMEOUT} if the operation
 	 *         was timed out</li>
 	 *         </ul>
 	 * @throws Exception
 	 * @throws ExecutionException
 	 * @throws InterruptedException
 	 */
 	public int writeBytes(ByteBuffer src, long timeout, TimeUnit unit) throws Exception {
 		if (this.writing) {
 			throw new WritePendingException();
 		}
 		try {
 			disableWriting();
 			return this.channel.write(src).get(timeout, unit);
 		} catch (Exception exp) {
 			if (exp instanceof ClosedChannelException) {
 				return OP_STATUS_CLOSED;
 			} else if (exp instanceof TimeoutException) {
 				return OP_STATUS_WRITE_TIMEOUT;
 			} else {
 				throw exp;
 			}
 		} finally {
 			enableWriting();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.nio.channels.AsynchronousByteChannel#write(java.nio.ByteBuffer,
 	 * java.lang.Object, java.nio.channels.CompletionHandler)
 	 */
 	@Override
 	public <A> void write(ByteBuffer src, A attachment,
 			CompletionHandler<Integer, ? super A> handler) {
 		write(src, 0L, TimeUnit.MILLISECONDS, attachment, handler);
 	}
 
 	/**
 	 * Writes a sequence of bytes to this channel from the given buffer.
 	 * 
 	 * <p>
 	 * This method initiates an asynchronous write operation to write a sequence
 	 * of bytes to this channel from the given buffer. The {@code handler}
 	 * parameter is a completion handler that is invoked when the write
 	 * operation completes (or fails). The result passed to the completion
 	 * handler is the number of bytes written.
 	 * 
 	 * <p>
 	 * If a timeout is specified and the timeout elapses before the operation
 	 * completes then it completes with the exception
 	 * {@link InterruptedByTimeoutException}. Where a timeout occurs, and the
 	 * implementation cannot guarantee that bytes have not been written, or will
 	 * not be written to the channel from the given buffer, then further
 	 * attempts to write to the channel will cause an unspecific runtime
 	 * exception to be thrown.
 	 * 
 	 * <p>
 	 * Otherwise this method works in the same manner as the
 	 * {@link NioChannel#write(ByteBuffer,Object,CompletionHandler)} method.
 	 * 
 	 * @param src
 	 *            The buffer from which bytes are to be retrieved
 	 * @param timeout
 	 *            The maximum time for the I/O operation to complete
 	 * @param unit
 	 *            The time unit of the {@code timeout} argument
 	 * @param attachment
 	 *            The object to attach to the I/O operation; can be {@code null}
 	 * @param handler
 	 *            The handler for consuming the result
 	 * 
 	 * @throws WritePendingException
 	 *             If a write operation is already in progress on this channel
 	 * @throws NotYetConnectedException
 	 *             If this channel is not yet connected
 	 * @throws ShutdownChannelGroupException
 	 *             If the channel group has terminated
 	 */
 	public <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment,
 			final CompletionHandler<Integer, ? super A> handler) {
 		if (writing) {
 			throw new WritePendingException();
 		}
 
 		disableWriting();
 		this.channel.write(src, timeout, unit, attachment, new CompletionHandler<Integer, A>() {
 
 			@Override
 			public void completed(Integer result, A attachment) {
 				enableWriting();
 				handler.completed(result, attachment);
 			}
 
 			@Override
 			public void failed(Throwable exc, A attachment) {
 				enableWriting();
 				handler.failed(exc, attachment);
 			}
 		});
 	}
 
 	/**
 	 * Writes a sequence of bytes to this channel from a subsequence of the
 	 * given buffers. This operation, sometimes called a
 	 * <em>gathering write</em>, is often useful when implementing network
 	 * protocols that group data into segments consisting of one or more
 	 * fixed-length headers followed by a variable-length body. The
 	 * {@code handler} parameter is a completion handler that is invoked when
 	 * the write operation completes (or fails). The result passed to the
 	 * completion handler is the number of bytes written.
 	 * 
 	 * <p>
 	 * This method initiates a write of up to <i>r</i> bytes to this channel,
 	 * where <i>r</i> is the total number of bytes remaining in the specified
 	 * subsequence of the given buffer array, that is,
 	 * 
 	 * <blockquote>
 	 * 
 	 * <pre>
 	 * srcs[offset].remaining()
 	 *     + srcs[offset+1].remaining()
 	 *     + ... + srcs[offset+length-1].remaining()
 	 * </pre>
 	 * 
 	 * </blockquote>
 	 * 
 	 * at the moment that the write is attempted.
 	 * 
 	 * <p>
 	 * Suppose that a byte sequence of length <i>n</i> is written, where
 	 * <tt>0</tt>&nbsp;<tt>&lt;</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>
 	 * &nbsp;<i>r</i>. Up to the first <tt>srcs[offset].remaining()</tt> bytes
 	 * of this sequence are written from buffer <tt>srcs[offset]</tt>, up to the
 	 * next <tt>srcs[offset+1].remaining()</tt> bytes are written from buffer
 	 * <tt>srcs[offset+1]</tt>, and so forth, until the entire byte sequence is
 	 * written. As many bytes as possible are written from each buffer, hence
 	 * the final position of each updated buffer, except the last updated
 	 * buffer, is guaranteed to be equal to that buffer's limit. The underlying
 	 * operating system may impose a limit on the number of buffers that may be
 	 * used in an I/O operation. Where the number of buffers (with bytes
 	 * remaining), exceeds this limit, then the I/O operation is performed with
 	 * the maximum number of buffers allowed by the operating system.
 	 * 
 	 * <p>
 	 * If a timeout is specified and the timeout elapses before the operation
 	 * completes then it completes with the exception
 	 * {@link InterruptedByTimeoutException}. Where a timeout occurs, and the
 	 * implementation cannot guarantee that bytes have not been written, or will
 	 * not be written to the channel from the given buffers, then further
 	 * attempts to write to the channel will cause an unspecific runtime
 	 * exception to be thrown.
 	 * 
 	 * @param srcs
 	 *            The buffers from which bytes are to be retrieved
 	 * @param offset
 	 *            The offset within the buffer array of the first buffer from
 	 *            which bytes are to be retrieved; must be non-negative and no
 	 *            larger than {@code srcs.length}
 	 * @param length
 	 *            The maximum number of buffers to be accessed; must be
 	 *            non-negative and no larger than {@code srcs.length - offset}
 	 * @param timeout
 	 *            The maximum time for the I/O operation to complete
 	 * @param unit
 	 *            The time unit of the {@code timeout} argument
 	 * @param attachment
 	 *            The object to attach to the I/O operation; can be {@code null}
 	 * @param handler
 	 *            The handler for consuming the result
 	 * 
 	 * @throws IndexOutOfBoundsException
 	 *             If the pre-conditions for the {@code offset} and
 	 *             {@code length} parameter aren't met
 	 * @throws WritePendingException
 	 *             If a write operation is already in progress on this channel
 	 * @throws NotYetConnectedException
 	 *             If this channel is not yet connected
 	 * @throws ShutdownChannelGroupException
 	 *             If the channel group has terminated
 	 */
 	public <A> void write(ByteBuffer[] srcs, int offset, int length, long timeout, TimeUnit unit,
 			A attachment, final CompletionHandler<Long, ? super A> handler) {
 
 		if (writing) {
 			throw new WritePendingException();
 		}
 
 		disableWriting();
 		this.channel.write(srcs, offset, length, timeout, unit, attachment,
 				new CompletionHandler<Long, A>() {
 
 					@Override
 					public void completed(Long result, A attachment) {
 						enableWriting();
 						handler.completed(result, attachment);
 					}
 
 					@Override
 					public void failed(Throwable exc, A attachment) {
 						enableWriting();
 						handler.failed(exc, attachment);
 					}
 				});
 	}
 
 	/**
 	 * @return the local address
 	 * @throws IOException
 	 */
 	public SocketAddress getLocalAddress() throws IOException {
 		return this.channel.getLocalAddress();
 	}
 
 	/**
 	 * @return the remote address
 	 * @throws IOException
 	 */
 	public SocketAddress getRemoteAddress() throws IOException {
 		return this.channel.getRemoteAddress();
 	}
 
 	/**
 	 * Shutdown the connection for reading without closing the channel.
 	 * 
 	 * <p>
 	 * Once shutdown for reading then further reads on the channel will return
 	 * {@code -1}, the end-of-stream indication. If the input side of the
 	 * connection is already shutdown then invoking this method has no effect.
 	 * The effect on an outstanding read operation is system dependent and
 	 * therefore not specified. The effect, if any, when there is data in the
 	 * socket receive buffer that has not been read, or data arrives
 	 * subsequently, is also system dependent.
 	 * 
 	 * @return This channel
 	 * 
 	 * @throws NotYetConnectedException
 	 *             If this channel is not yet connected
 	 * @throws ClosedChannelException
 	 *             If this channel is closed
 	 * @throws IOException
 	 *             If some other I/O error occurs
 	 */
 	public final NioChannel shutdownInput() throws IOException {
 		this.channel.shutdownInput();
 		return this;
 	}
 
 	/**
 	 * Shutdown the connection for writing without closing the channel.
 	 * 
 	 * <p>
 	 * Once shutdown for writing then further attempts to write to the channel
 	 * will throw {@link ClosedChannelException}. If the output side of the
 	 * connection is already shutdown then invoking this method has no effect.
 	 * The effect on an outstanding write operation is system dependent and
 	 * therefore not specified.
 	 * 
 	 * @return This channel
 	 * 
 	 * @throws NotYetConnectedException
 	 *             If this channel is not yet connected
 	 * @throws ClosedChannelException
 	 *             If this channel is closed
 	 * @throws IOException
 	 *             If some other I/O error occurs
 	 */
 	public final NioChannel shutdownOutput() throws IOException {
 		this.channel.shutdownOutput();
 		return this;
 	}
 
 	/**
 	 * Sets the value of a socket option.
 	 * 
 	 * @param name
 	 *            The socket option name
 	 * @param value
 	 *            The value of the socket option. A value of {@code null} may be
 	 *            a valid value for some socket options.
 	 * 
 	 * @return This channel
 	 * 
 	 * @throws UnsupportedOperationException
 	 *             If the socket option is not supported by this channel
 	 * @throws IllegalArgumentException
 	 *             If the value is not a valid value for this socket option
 	 * @throws ClosedChannelException
 	 *             If this channel is closed
 	 * @throws IOException
 	 *             If an I/O error occurs
 	 * 
 	 * @see java.net.StandardSocketOptions
 	 */
 	public <T> NioChannel setOption(SocketOption<T> name, T value) throws IOException {
 		this.channel.setOption(name, value);
 		return this;
 	}
 
 	/**
 	 * Returns the value of a socket option.
 	 * 
 	 * @param name
 	 *            The socket option
 	 * 
 	 * @return The value of the socket option. A value of {@code null} may be a
 	 *         valid value for some socket options.
 	 * 
 	 * @throws UnsupportedOperationException
 	 *             If the socket option is not supported by this channel
 	 * @throws ClosedChannelException
 	 *             If this channel is closed
 	 * @throws IOException
 	 *             If an I/O error occurs
 	 * 
 	 * @see java.net.StandardSocketOptions
 	 */
 	public <T> T getOption(SocketOption<T> name) throws IOException {
 		return this.channel.getOption(name);
 	}
 
 	/**
 	 * Enable/Disable read operations
 	 * 
 	 * @param enabled
 	 */
 	protected final void enableReading(boolean enabled) {
 		this.reading = !enabled;
 	}
 
 	/**
 	 * Enable read operations
 	 */
 	protected final void enableReading() {
 		// synchronized (this.readLock) {
 		this.reading = false;
 		// this.readLock.notifyAll();
 		// }
 	}
 
 	/**
 	 * Disable read operations
 	 */
 	protected final void disableReading() {
 		this.reading = true;
 	}
 
 	/**
 	 * Enable/Disable write operations
 	 * 
 	 * @param enabled
 	 */
 	protected final void enableWriting(boolean enabled) {
 		this.writing = !enabled;
 	}
 
 	/**
 	 * Enable write operations
 	 */
 	protected final void enableWriting() {
 		// synchronized (this.writeLock) {
 		this.writing = false;
 		// this.writeLock.notifyAll();
 		// }
 	}
 
 	/**
 	 * Disable write operations
 	 */
 	protected final void disableWriting() {
 		this.writing = true;
 	}
 
 	/**
 	 * Check if a read operation is in progress or not.
 	 * 
 	 * @return <tt>true</tt> if a read operation is already in progress, else
 	 *         <tt>false</tt>
 	 */
 	public boolean isReadPending() {
 		return this.reading;
 	}
 
 	/**
 	 * Check if the channel is ready for read operation
 	 * 
 	 * @return <tt>true</tt> if no read operation is in progress, else
 	 *         <tt>false</tt>
 	 */
 	public boolean isReadReady() {
 		return !isReadPending();
 	}
 
 	/**
 	 * Check if a write operation is in progress or not.
 	 * 
 	 * @return <tt>true</tt> if a write operation is already in progress, else
 	 *         <tt>false</tt>
 	 */
 	public boolean isWritePending() {
 		return this.writing;
 	}
 
 	/**
 	 * Check if the channel is ready for write.
 	 * 
 	 * @return <tt>true</tt> if no write operation is already in progress, else
 	 *         <tt>false</tt>
 	 */
 	public boolean isWriteReady() {
 		return !isWritePending();
 	}
 
 	/**
 	 * Returns a set of the socket options supported by this channel.
 	 * 
 	 * <p>
 	 * This method will continue to return the set of options even after the
 	 * channel has been closed.
 	 * 
 	 * @return A set of the socket options supported by this channel
 	 */
 	public Set<SocketOption<?>> supportedOptions() {
 		return this.channel.supportedOptions();
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 }
