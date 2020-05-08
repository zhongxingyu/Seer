 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.websocket;
 
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.URI;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.araqne.websocket.WebSocketFrame.Opcode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @since 0.1.0
  * @author xeraph
  * 
  */
 public class WebSocket {
 	private static final String WEBSOCKET_KEY_TRAILER = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
 	private final Logger logger = LoggerFactory.getLogger(WebSocket.class);
 	private URI uri;
 	private Socket socket;
 	private CopyOnWriteArraySet<WebSocketListener> listeners;
 	private Receiver receiver;
 	private boolean closed;
 
 	public WebSocket(URI uri) throws IOException {
 		this.uri = uri;
 		this.listeners = new CopyOnWriteArraySet<WebSocketListener>();
 
 		connect();
 	}
 
 	public boolean isClosed() {
 		return closed;
 	}
 
 	private void connect() throws UnknownHostException, IOException {
 		int port = uri.getPort();
 		if (port == -1)
 			port = 80;
 
 		socket = new Socket(uri.getHost(), port);
 		socket.setSoTimeout(10000);
 
 		try {
 			String webSocketKey = newWebSocketKey();
 			String handshake = newHandshakeRequest(webSocketKey);
 
 			OutputStream os = socket.getOutputStream();
 			os.write(handshake.getBytes("utf-8"));
 			os.flush();
 
 			String response = "";
 			byte[] b = new byte[8096];
 			InputStream is = socket.getInputStream();
 			while (true) {
 				int readBytes = is.read(b);
 				if (readBytes <= 0)
 					break;
 				response += new String(b, 0, readBytes);
 				if (response.endsWith("\r\n\r\n"))
 					break;
 			}
 
 			if (!response.startsWith("HTTP/1.1 101 "))
 				throw new IOException("websocket not suported");
 
 			Map<String, String> headers = new HashMap<String, String>();
 			String[] lines = response.split("\r\n");
 			for (String line : lines) {
 				int p = line.indexOf(':');
 				if (p < 0)
 					continue;
 
 				String key = line.substring(0, p).trim().toLowerCase();
 				String value = line.substring(p + 1).trim();
 				headers.put(key, value);
 			}
 
 			String upgrade = getHeader(headers, "upgrade");
 			String connection = getHeader(headers, "connection");
 			String accept = getHeader(headers, "sec-websocket-accept");
 
 			if (!upgrade.equalsIgnoreCase("websocket"))
 				throw new IOException("Unexpected Upgrade value: " + upgrade);
 
 			if (!connection.equals("Upgrade"))
 				throw new IOException("Unexpected Connection value: " + connection);
 
 			// calculate accept key and match
 			try {
 				MessageDigest md = MessageDigest.getInstance("SHA-1");
 				String input = webSocketKey + WEBSOCKET_KEY_TRAILER;
 				String expected = new String(Base64.encode(md.digest(input.getBytes())));
 				if (!expected.equals(accept))
 					throw new IOException("invalid websocket accept: key " + webSocketKey + ", expected " + expected
 							+ ", actual " + accept);
 
 			} catch (NoSuchAlgorithmException e) {
 				throw new IllegalStateException("SHA-1 digest not supported");
 			}
 
 			receiver = new Receiver();
 			receiver.start();
 		} catch (IOException e) {
 			socket.close();
 			throw e;
 		} catch (IllegalStateException e) {
 			socket.close();
 			throw e;
 		}
 	}
 
 	private String getHeader(Map<String, String> headers, String name) throws IOException {
 		String value = headers.get(name);
 		if (value == null)
 			throw new IOException(name + " header not found");
 		return value;
 	}
 
 	private String newWebSocketKey() {
 		Random r = new Random();
 		byte[] b = new byte[16];
 		r.nextBytes(b);
 		return new String(Base64.encode(b));
 	}
 
 	private String newHandshakeRequest(String webSocketKey) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("GET /websocket HTTP/1.1\r\n");
 		sb.append("Host: " + uri.getHost() + "\r\n");
 		sb.append("Upgrade: websocket\r\n");
 		sb.append("Connection: Upgrade\r\n");
 		sb.append("Sec-WebSocket-Key: " + webSocketKey + "\r\n");
 		// sb.append("Sec-WebSocket-Protocol: chat\r\n");
 		sb.append("Sec-WebSocket-Version: 13\r\n");
 		sb.append("Content-Length: 0");
 		sb.append("\r\n\r\n");
 
 		return sb.toString();
 	}
 
 	public void close() throws IOException {
 		if (closed)
 			return;
 
 		Socket captured = socket;
 		if (captured != null) {
 			try {
 				// getOutputStream can raise exception
				ensureClose(captured.getOutputStream());
 			} catch (Throwable t) {
 			}
 
 			try {
 				// getInputStream can raise exception
				ensureClose(captured.getInputStream());
 			} catch (Throwable t) {
 			}
 
 			captured.close();
 		}
 
 		closed = true;
 	}
 
 	private void ensureClose(Closeable c) {
 		if (c != null) {
 			try {
 				c.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	public void addListener(WebSocketListener listener) {
 		this.listeners.add(listener);
 	}
 
 	public void removeListener(WebSocketListener listener) {
 		this.listeners.remove(listener);
 	}
 
 	public void send(String text) throws IOException {
 		if (closed)
 			throw new IOException("websocket is closed");
 
 		WebSocketFrame frame = new WebSocketFrame(text);
 		byte[] encoded = frame.encode();
 		socket.getOutputStream().write(encoded);
 		socket.getOutputStream().flush();
 	}
 
 	private class Receiver extends Thread {
 		private boolean doStop;
 		private ByteArrayOutputStream os = new ByteArrayOutputStream();
 
 		@Override
 		public void run() {
 			try {
 				while (!doStop) {
 					try {
 						if (socket.isClosed())
 							break;
 
 						recv();
 					} catch (SocketTimeoutException e) {
 					} catch (SocketException e) {
 						if (e.getMessage().equalsIgnoreCase("Connection reset"))
 							return;
 						if (socket.isClosed())
 							return;
 						logger.debug("araqne websocket: socket exception", e);
 					} catch (IOException e) {
 						logger.debug("araqne websocket: io exception", e);
 					}
 				}
 			} finally {
 				if (socket != null) {
 					try {
 						socket.close();
 					} catch (IOException e) {
 					}
 					socket = null;
 				}
 
 				closed = true;
 			}
 		}
 
 		private void ensureRead(byte[] b) throws IOException {
 			ensureRead(b, b.length);
 		}
 
 		private void ensureRead(byte[] b, int len) throws IOException {
 			int total = 0;
 			InputStream is = socket.getInputStream();
 			while (total != len) {
 				int readBytes = is.read(b, total, len - total);
 				if (readBytes < 0)
 					throw new SocketException("Connection reset");
 				total += readBytes;
 			}
 		}
 
 		private void recv() throws IOException {
 			byte[] sb = new byte[2];
 			ensureRead(sb);
 
 			boolean fin = (sb[0] & 0x80) == 0x80;
 			int opcode = (sb[0]) & 0xf;
 			@SuppressWarnings("unused")
 			boolean mask = (sb[1] & 0x80) == 0x80;
 
 			long payloadLen = 0;
 			int len = sb[1] & 0x7f;
 			if (len < 126) {
 				payloadLen = len;
 			} else if (len == 126) {
 				ensureRead(sb);
 				payloadLen = ByteBuffer.wrap(sb).getShort() & 0xffff;
 			} else if (len == 127) {
 				byte[] longbuf = new byte[8];
 				ensureRead(longbuf);
 				payloadLen = ByteBuffer.wrap(longbuf).getLong();
 			}
 
 			byte[] b = new byte[(int) payloadLen];
 			ensureRead(b);
 
 			if (opcode == Opcode.TEXT.getCode() || (!fin && opcode == Opcode.CONTINUATION.getCode())) {
 				os.write(b);
 			}
 
 			if (fin) {
 				String text = new String(os.toByteArray());
 				os = new ByteArrayOutputStream();
 
 				for (WebSocketListener listener : listeners) {
 					try {
 						listener.onMessage(new WebSocketMessage(Opcode.TEXT.getCode(), text));
 					} catch (Throwable t) {
 						logger.warn("araqne websocket: websocket listener should not throw any exception", t);
 					}
 				}
 			}
 		}
 	}
 }
