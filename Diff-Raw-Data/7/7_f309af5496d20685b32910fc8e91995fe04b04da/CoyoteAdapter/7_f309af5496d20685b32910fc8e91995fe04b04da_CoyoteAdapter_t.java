 /**
  * JBoss, Home of Professional Open Source. Copyright 2012, Red Hat, Inc., and
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
 package org.apache.catalina.connector;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.CompletionHandler;
 
 import org.apache.catalina.http.HttpResponseParser;
 import org.apache.coyote.Adapter;
 import org.apache.coyote.Request;
 import org.apache.coyote.Response;
 import org.apache.coyote.http11.AbstractHttp11Processor;
 import org.apache.coyote.http11.AbstractInternalInputBuffer;
 import org.apache.coyote.http11.AbstractInternalOutputBuffer;
 import org.apache.coyote.http11.InternalNioInputBuffer;
 import org.apache.coyote.http11.InternalNioOutputBuffer;
 import org.apache.tomcat.util.buf.ByteChunk;
 import org.apache.tomcat.util.buf.CharChunk;
 import org.apache.tomcat.util.buf.MessageBytes;
 import org.apache.tomcat.util.net.NioChannel;
 import org.apache.tomcat.util.net.SocketStatus;
 import org.jboss.cluster.proxy.container.Node;
 import org.jboss.logging.Logger;
 
 /**
  * {@code CoyoteAdapter}
  * <p>
  * Implementation of a request processor which delegates the processing to a
  * Coyote processor.
  * </p>
  * Created on Jun 20, 2012 at 2:21:46 PM
  * 
  * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
  */
 public class CoyoteAdapter implements Adapter {
 
 	private static final Logger logger = Logger.getLogger(CoyoteAdapter.class);
 
 	/**
 	 * 
 	 */
 	protected static final boolean ALLOW_BACKSLASH = Boolean.valueOf(
 			System.getProperty("org.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH",
 					"false")).booleanValue();
 
 	/**
 	 * The CoyoteConnector with which this processor is associated.
 	 */
 	private Connector connector = null;
 
 	/**
 	 * The string manager for this package.
 	 */
 	protected org.apache.tomcat.util.res.StringManager sm = org.apache.tomcat.util.res.StringManager
 			.getManager(Constants.Package);
 
 	/**
 	 * Create a new instance of {@code CoyoteAdapter}
 	 * <p>
 	 * Construct a new CoyoteProcessor associated with the specified connector.
 	 * </p>
 	 * 
 	 * @param connector
 	 *            CoyoteConnector that owns this processor
 	 */
 	public CoyoteAdapter(Connector connector) {
 		this.connector = connector;
 	}
 
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	public void init() throws Exception {
 		logger.info("Initializing CoyoteAdapter service");
 		// Nothing to do
 		logger.info("CoyoteAdapter Initialized successfully");
 	}
 
 	/**
 	 * Event method.
 	 * 
 	 * @return false to indicate an error, expected or not
 	 */
 	public boolean event(Request req, Response res, SocketStatus status) {
 		return false;
 	}
 
 	/**
 	 * Service method.
 	 */
 	public void service(final Request request, Response response) throws Exception {
 
 		if (prepare(request, response)) {
 			// Send the request to the selected node
 			sendToNode(request, response);
 		} else {
 			sendError(request, response);
 		}
 	}
 
 	/**
 	 * @param request
 	 * @param response
 	 * @throws IOException
 	 */
 	private void sendError(Request request, Response response) throws IOException {
 		AbstractInternalOutputBuffer outputBuffer = (AbstractInternalOutputBuffer) response
 				.getOutputBuffer();
 		outputBuffer.sendError();
 	}
 
 	/**
 	 * Forwards the client request to the selected node.
 	 * 
 	 * @param params
 	 *            a map containing all required parameters
 	 */
 	private void sendToNode(final Request request, final Response response) throws Exception {
 		final NioChannel nodeChannel = (NioChannel) response.getNote(Constants.NODE_CHANNEL_NOTE);
 		final ByteBuffer inBuffer = (ByteBuffer) response.getNote(Constants.IN_BUFFER_NOTE);
 
 		if (inBuffer.position() > 0) {
 			inBuffer.flip();
 		}
 		// Write the request to the node
 		nodeChannel.write(inBuffer, response, new CompletionHandler<Integer, Response>() {
 
 			@Override
 			public void completed(Integer nBytes, Response attachment) {
 				if (nBytes < 0) {
 					failed(new ClosedChannelException(), attachment);
 				} else {
 					ByteBuffer buff = (ByteBuffer) attachment.getNote(Constants.IN_BUFFER_NOTE);
 					if (buff.hasRemaining()) {
 						NioChannel ch = (NioChannel) attachment
 								.getNote(Constants.NODE_CHANNEL_NOTE);
 						ch.write(buff, attachment, this);
 					} else {
 						// Read response from the node and forward it
 						// back to client
 						try {
 							if (!checkPostMethod(attachment.getRequest(), attachment)) {
 								readFromNode(attachment.getRequest(), attachment);
 							}
 						} catch (Exception exp) {
 							failed(exp, attachment);
 						}
 					}
 				}
 			}
 
 			@Override
 			public void failed(Throwable exc, Response attachment) {
 				exc.printStackTrace();
 				try {
 					// try again with node
 					//tryWithNode(attachment.getRequest(), attachment);
 					//sendToNode(attachment.getRequest(), attachment);
 				} catch (Throwable e) {
 					try {
 						((AbstractInternalOutputBuffer) attachment.getOutputBuffer()).sendError();
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * @param params
 	 */
 	private void readFromNode(final org.apache.coyote.Request request,
 			final org.apache.coyote.Response response) throws Exception {
 
 		final NioChannel nodeChannel = (NioChannel) response.getNote(Constants.NODE_CHANNEL_NOTE);
 		final ByteBuffer buffer = (ByteBuffer) response.getNote(Constants.OUT_BUFFER_NOTE);
 		buffer.clear();
 
 		// Read bytes from node.
 		nodeChannel.read(buffer, response,
 				new CompletionHandler<Integer, org.apache.coyote.Response>() {
 
 					private long contentLength = 0;
 
 					@Override
 					public void completed(Integer nBytes, Response attachment) {
 						if (nBytes < 0) {
 							failed(new ClosedChannelException(), attachment);
 						} else if (nBytes > 0) {
 
 							contentLength += nBytes;
 							ByteBuffer buff = (ByteBuffer) response
 									.getNote(Constants.OUT_BUFFER_NOTE);
 							buff.flip();
 
 							AbstractInternalOutputBuffer outputBuffer = (AbstractInternalOutputBuffer) attachment
 									.getOutputBuffer();
 
 							// Parse the HTTP Header
 							HttpResponseParser httpResponseParser = attachment.getResponseParser();
 
 							byte data[] = outputBuffer.getBytes();
 							buff.get(data, 0, nBytes);
 
 							if (httpResponseParser.parsingHeader()) {
 								httpResponseParser.parse(attachment, data, nBytes);
 							}
 
 							outputBuffer.setContentLength(attachment.getContentLengthLong()
 									+ httpResponseParser.getHeaderLength());
 							buff.clear();
 							outputBuffer.writeToClient(outputBuffer.getBytes(), 0, nBytes);
 
 							if (this.contentLength < attachment.getContentLength()
 									+ httpResponseParser.getHeaderLength()) {
 								final NioChannel ch = (NioChannel) response
 										.getNote(Constants.NODE_CHANNEL_NOTE);
 								ch.read(buff, attachment, this);
 							} else {
 								AbstractHttp11Processor<?> processor = (AbstractHttp11Processor<?>) attachment.hook;
 								boolean chunked = attachment.isChunked();
 								processor.endRequest();
 								processor.nextRequest();
 								if (chunked) {
 									((InternalNioOutputBuffer) outputBuffer)
 											.configChunked(nodeChannel);
 								} else {
 									if (processor.isKeepAlive()) {
 										processor.awaitNext();
 									} else {
 										processor.closeSocket();
 									}
 									Node node = (Node) attachment.getNote(Constants.NODE_NOTE);
 									NioChannel channel = (NioChannel) attachment
 											.getNote(Constants.NODE_CHANNEL_NOTE);
 									connector.getConnectionManager().recycle(node.getJvmRoute(),
 											channel);
 								}
 							}
 						}
 					}
 
 					@Override
 					public void failed(Throwable exc, org.apache.coyote.Response attachment) {
 
 						try {
 							// try again with node
 							tryWithNode(attachment.getRequest(), attachment);
 							sendToNode(attachment.getRequest(), attachment);
 						} catch (Throwable e) {
 							try {
 								((AbstractInternalOutputBuffer) attachment.getOutputBuffer())
 										.sendError();
 							} catch (IOException e1) {
 								e1.printStackTrace();
 							}
 						}
 					}
 				});
 	}
 
 	/**
 	 * Prepare the request for processing
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws IOException
 	 */
 	private boolean prepare(final org.apache.coyote.Request request,
 			final org.apache.coyote.Response response) throws Exception {
 
 		postParseRequest(request, response);
 
 		try {
 			prepareNode(request, response, null, 0);
 		} catch (Throwable t) {
 			logger.error("No node is available", t);
 			return false;
 		}
 		// Client request
 		AbstractInternalInputBuffer inputBuffer = (AbstractInternalInputBuffer) request
 				.getInputBuffer();
 
 		final ByteBuffer inBuffer = (ByteBuffer) inputBuffer.getByteBuffer();
 		inBuffer.clear();
 
 		// Client response
 		AbstractInternalOutputBuffer outputBuffer = (AbstractInternalOutputBuffer) response
 				.getOutputBuffer();
 		final ByteBuffer outBuffer = (ByteBuffer) outputBuffer.getByteBuffer().clear();
 
 		// Put data to forward to the node in the byte buffer
 		inBuffer.put(inputBuffer.getBuffer(), 0, inputBuffer.getLastValid()).flip();
 
 		NioChannel clientChannel = ((InternalNioInputBuffer) inputBuffer).getChannel();
 
 		// Put relevant elements in the map attachment
 
 		response.setNote(Constants.IN_BUFFER_NOTE, inBuffer);
 		response.setNote(Constants.OUT_BUFFER_NOTE, outBuffer);
 		response.setNote(Constants.CLIENT_CHANNEL_NOTE, clientChannel);
 
 		return true;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws Exception
 	 */
 	private void prepareNode(final org.apache.coyote.Request request,
 			final org.apache.coyote.Response response, Node failedNode, int n) throws Exception {
 
 		if (n >= 3) {
 			throw new IOException("No node available");
 		}
 
 		boolean error = false;
 		Node node = this.connector.getNodeService().getNode(request, failedNode);
 		if (node == null) {
 			throw new IOException("No node available");
 		}
 
 		NioChannel nodeChannel = null;
 		try {
 			int tries = 0;
 			while ((tries++) <= 3) {
 				nodeChannel = this.connector.getConnectionManager().getChannel(node);
 				if (nodeChannel == null) {
 					throw new NullPointerException("Null node channel");
 				}
 			}
 		} catch (Throwable t) {
 			this.connector.getNodeService().nodeDown(node);
 			prepareNode(request, response, node, n + 1);
 			return;
 		}
 
 		response.setNote(Constants.NODE_NOTE, node);
 		response.setNote(Constants.NODE_CHANNEL_NOTE, nodeChannel);
 
 		if (error) {
 			response.setStatus(503);
 			response.setMessage("Service Unavailable");
 			response.addHeader("Server", "Apache-Coyote/1.1");
 
 			throw new IOException("Unable to connect to node");
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param response
 	 * @throws Exception
 	 */
 	private void tryWithNode(org.apache.coyote.Request request, org.apache.coyote.Response response)
 			throws Exception {
 		// Closing the current channel
 		NioChannel channel = (NioChannel) response.getNote(Constants.NODE_CHANNEL_NOTE);
 		this.connector.getConnectionManager().close(channel);
 
 		// Retrieve the failed node
 		Node failedNode = (Node) response.getNote(Constants.NODE_NOTE);
 		prepareNode(request, response, failedNode, 1);
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws IOException
 	 */
 	private boolean checkPostMethod(final org.apache.coyote.Request request,
 			final org.apache.coyote.Response response) throws Exception {
 
 		if (request.getContentLength() > 0) {
 			if (request.getContentLength() <= connector.getMaxPostSize()) {
 				doPost(request, response);
 				return true;
 			} else {
 				logger.warn("Parameters were not parsed because the size of the posted data was too big. "
 						+ "Use the maxPostSize attribute of the connector to resolve this if the "
 						+ "application should accept large POSTs.");
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws IOException
 	 */
 	private void doPost(final Request request, final Response response) throws Exception {
 
 		final AbstractInternalInputBuffer inputBuffer = (AbstractInternalInputBuffer) request
 				.getInputBuffer();
 		final NioChannel nodeChannel = (NioChannel) response.getNote(Constants.NODE_CHANNEL_NOTE);
 
 		int n = inputBuffer.getAvailable();
 		int lastValid = inputBuffer.getLastValid();
 		int end = inputBuffer.getEnd();
 		
 		if(lastValid >= end + request.getContentLength()) {
 			readFromNode(request, response);
 			return;
 		}
 		
 		final ByteBuffer buffer = inputBuffer.getByteBuffer();
 		buffer.clear();
 		int nRead = 0;
 		Throwable th = null;
 		try {
 			nRead = inputBuffer.readBytes(buffer);
 		} catch (Throwable t) {
			th = t;
 		}
 
 		if (nRead > 0) {
 			buffer.flip();
 			nodeChannel.write(buffer, n + nRead, new CompletionHandler<Integer, Integer>() {
 
 				@Override
 				public void completed(Integer result, Integer attachment) {
 					if (result < 0) {
 						failed(new ClosedChannelException(), attachment);
 						return;
 					}
 
 					if (buffer.hasRemaining()) {
 						nodeChannel.write(buffer, attachment, this);
 					} else {
 						int len = request.getContentLength();
 						try {
 							if (attachment < len) {
 								buffer.clear();
 								int nRead = inputBuffer.readBytes(buffer);
 								if (nRead > 0) {
 									buffer.flip();
 									nodeChannel.write(buffer, attachment + nRead, this);
 								} else {
 									failed(new ClosedChannelException(), attachment);
 								}
 
 							} else {
 								readFromNode(request, response);
 							}
 						} catch (Exception e) {
 							failed(e, attachment);
 						}
 					}
 				}
 
 				@Override
 				public void failed(Throwable exc, Integer attachment) {
 					exc.printStackTrace();
 				}
 			});
 		} else {
 			throw new IOException("Read operation fails", th);
 		}
 	}
 
 	/**
 	 * Parse additional request parameters.
 	 */
 	protected boolean postParseRequest(org.apache.coyote.Request req, org.apache.coyote.Response res)
 			throws Exception {
 
 		// FIXME: The processor needs to set a correct scheme and port prior to
 		// this point,
 		// in ajp13 protocol does not make sense to get the port from the
 		// connector..
 		// otherwise, use connector configuration
 		if (req.scheme().isNull()) {
 			// Use connector scheme and secure configuration, (defaults to
 			// "http" and false respectively)
 			req.scheme().setString(connector.getScheme());
 		}
 
 		// FIXME: the code below doesnt belongs to here,
 		// this is only have sense
 		// in Http11, not in ajp13..
 		// At this point the Host header has been processed.
 		// Override if the proxyPort/proxyHost are set
 		String proxyName = connector.getProxyName();
 		int proxyPort = connector.getProxyPort();
 		if (proxyPort != 0) {
 			req.setServerPort(proxyPort);
 		}
 		if (proxyName != null) {
 			req.serverName().setString(proxyName);
 		}
 
 		// URI decoding
 		MessageBytes decodedURI = req.decodedURI();
 		decodedURI.duplicate(req.requestURI());
 
 		if (decodedURI.getType() == MessageBytes.T_BYTES) {
 			// Remove any path parameters
 			ByteChunk uriBB = decodedURI.getByteChunk();
 			int semicolon = uriBB.indexOf(';', 0);
 			if (semicolon > 0) {
 				decodedURI.setBytes(uriBB.getBuffer(), uriBB.getStart(), semicolon);
 			}
 			// %xx decoding of the URL
 			try {
 				req.getURLDecoder().convert(decodedURI, false);
 			} catch (IOException ioe) {
 				res.setStatus(400);
 				res.setMessage("Invalid URI: " + ioe.getMessage());
 				return false;
 			}
 			// Normalization
 			if (!normalize(req.decodedURI())) {
 				res.setStatus(400);
 				res.setMessage("Invalid URI");
 				return false;
 			}
 			// Character decoding
 			convertURI(decodedURI, req);
 			// Check that the URI is still normalized
 			if (!checkNormalize(req.decodedURI())) {
 				res.setStatus(400);
 				res.setMessage("Invalid URI character encoding");
 				return false;
 			}
 		} else {
 			// The URL is chars or String, and has been sent using an in-memory
 			// protocol handler, we have to assume the URL has been properly
 			// decoded already
 			decodedURI.toChars();
 			// Remove any path parameters
 			CharChunk uriCC = decodedURI.getCharChunk();
 			int semicolon = uriCC.indexOf(';');
 			if (semicolon > 0) {
 				decodedURI.setChars(uriCC.getBuffer(), uriCC.getStart(), semicolon);
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Character conversion of the URI.
 	 */
 	protected void convertURI(MessageBytes uri, Request request) throws Exception {
 
 		ByteChunk bc = uri.getByteChunk();
 		int length = bc.getLength();
 		CharChunk cc = uri.getCharChunk();
 		cc.allocate(length, -1);
 
 		String enc = connector.getURIEncoding();
 
 		// Default encoding: fast conversion
 		byte[] bbuf = bc.getBuffer();
 		char[] cbuf = cc.getBuffer();
 		int start = bc.getStart();
 		for (int i = 0; i < length; i++) {
 			cbuf[i] = (char) (bbuf[i + start] & 0xff);
 		}
 		uri.setChars(cbuf, 0, length);
 
 	}
 
 	/**
 	 * Character conversion of the a US-ASCII MessageBytes.
 	 */
 	protected void convertMB(MessageBytes mb) {
 
 		// This is of course only meaningful for bytes
 		if (mb.getType() != MessageBytes.T_BYTES)
 			return;
 
 		ByteChunk bc = mb.getByteChunk();
 		CharChunk cc = mb.getCharChunk();
 		int length = bc.getLength();
 		cc.allocate(length, -1);
 
 		// Default encoding: fast conversion
 		byte[] bbuf = bc.getBuffer();
 		char[] cbuf = cc.getBuffer();
 		int start = bc.getStart();
 		for (int i = 0; i < length; i++) {
 			cbuf[i] = (char) (bbuf[i + start] & 0xff);
 		}
 		mb.setChars(cbuf, 0, length);
 
 	}
 
 	/**
 	 * Normalize URI.
 	 * <p>
 	 * This method normalizes "\", "//", "/./" and "/../". This method will
 	 * return false when trying to go above the root, or if the URI contains a
 	 * null byte.
 	 * 
 	 * @param uriMB
 	 *            URI to be normalized
 	 * @return true
 	 */
 	public static boolean normalize(MessageBytes uriMB) {
 
 		ByteChunk uriBC = uriMB.getByteChunk();
 		byte[] b = uriBC.getBytes();
 		int start = uriBC.getStart();
 		int end = uriBC.getEnd();
 
 		// An empty URL is not acceptable
 		if (start == end)
 			return false;
 
 		// URL * is acceptable
 		if ((end - start == 1) && b[start] == (byte) '*')
 			return true;
 
 		int pos = 0;
 		int index = 0;
 
 		// Replace '\' with '/'
 		// Check for null byte
 		for (pos = start; pos < end; pos++) {
 			if (b[pos] == (byte) '\\') {
 				if (ALLOW_BACKSLASH) {
 					b[pos] = (byte) '/';
 				} else {
 					return false;
 				}
 			}
 			if (b[pos] == (byte) 0) {
 				return false;
 			}
 		}
 
 		// The URL must start with '/'
 		if (b[start] != (byte) '/') {
 			return false;
 		}
 
 		// Replace "//" with "/"
 		for (pos = start; pos < (end - 1); pos++) {
 			if (b[pos] == (byte) '/') {
 				while ((pos + 1 < end) && (b[pos + 1] == (byte) '/')) {
 					copyBytes(b, pos, pos + 1, end - pos - 1);
 					end--;
 				}
 			}
 		}
 
 		// If the URI ends with "/." or "/..", then we append an extra "/"
 		// Note: It is possible to extend the URI by 1 without any side effect
 		// as the next character is a non-significant WS.
 		if (((end - start) >= 2) && (b[end - 1] == (byte) '.')) {
 			if ((b[end - 2] == (byte) '/')
 					|| ((b[end - 2] == (byte) '.') && (b[end - 3] == (byte) '/'))) {
 				b[end] = (byte) '/';
 				end++;
 			}
 		}
 
 		uriBC.setEnd(end);
 
 		index = 0;
 
 		// Resolve occurrences of "/./" in the normalized path
 		while (true) {
 			index = uriBC.indexOf("/./", 0, 3, index);
 			if (index < 0)
 				break;
 			copyBytes(b, start + index, start + index + 2, end - start - index - 2);
 			end = end - 2;
 			uriBC.setEnd(end);
 		}
 
 		index = 0;
 
 		// Resolve occurrences of "/../" in the normalized path
 		while (true) {
 			index = uriBC.indexOf("/../", 0, 4, index);
 			if (index < 0)
 				break;
 			// Prevent from going outside our context
 			if (index == 0)
 				return false;
 			int index2 = -1;
 			for (pos = start + index - 1; (pos >= 0) && (index2 < 0); pos--) {
 				if (b[pos] == (byte) '/') {
 					index2 = pos;
 				}
 			}
 			copyBytes(b, start + index2, start + index + 3, end - start - index - 3);
 			end = end + index2 - index - 3;
 			uriBC.setEnd(end);
 			index = index2;
 		}
 
 		uriBC.setBytes(b, start, end);
 
 		return true;
 
 	}
 
 	/**
 	 * Check that the URI is normalized following character decoding.
 	 * <p>
 	 * This method checks for "\", 0, "//", "/./" and "/../". This method will
 	 * return false if sequences that are supposed to be normalized are still
 	 * present in the URI.
 	 * 
 	 * @param uriMB
 	 *            URI to be checked (should be chars)
 	 * @return true if the URI is normalized
 	 */
 	public static boolean checkNormalize(MessageBytes uriMB) {
 
 		CharChunk uriCC = uriMB.getCharChunk();
 		char[] c = uriCC.getChars();
 		int start = uriCC.getStart();
 		int end = uriCC.getEnd();
 
 		int pos = 0;
 
 		// Check for '\' and 0
 		for (pos = start; pos < end; pos++) {
 			if (c[pos] == '\\') {
 				return false;
 			}
 			if (c[pos] == 0) {
 				return false;
 			}
 		}
 
 		// Check for "//"
 		for (pos = start; pos < (end - 1); pos++) {
 			if (c[pos] == '/') {
 				if (c[pos + 1] == '/') {
 					return false;
 				}
 			}
 		}
 
 		// Check for ending with "/." or "/.."
 		if (((end - start) >= 2) && (c[end - 1] == '.')) {
 			if ((c[end - 2] == '/') || ((c[end - 2] == '.') && (c[end - 3] == '/'))) {
 				return false;
 			}
 		}
 
 		// Check for "/./"
 		if (uriCC.indexOf("/./", 0, 3, 0) >= 0) {
 			return false;
 		}
 
 		// Check for "/../"
 		if (uriCC.indexOf("/../", 0, 4, 0) >= 0) {
 			return false;
 		}
 
 		return true;
 
 	}
 
 	// ------------------------------------------------------ Protected Methods
 
 	/**
 	 * Copy an array of bytes to a different position. Used during
 	 * normalization.
 	 */
 	protected static void copyBytes(byte[] b, int dest, int src, int len) {
 		for (int pos = 0; pos < len; pos++) {
 			b[pos + dest] = b[pos + src];
 		}
 	}
 
 	/**
 	 * {@code Pair}
 	 * 
 	 * @param <A>
 	 * @param <B>
 	 * 
 	 *            Created on Sep 4, 2012 at 11:41:47 AM
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	private static class Pair<A, B> {
 		A first;
 		B last;
 
 		public Pair(A a, B b) {
 			this.first = a;
 			this.last = b;
 		}
 	}
 }
