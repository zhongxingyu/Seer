 package com.narrowtux.blueberry.websockets;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.SocketException;
 import java.net.URI;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.apache.commons.codec.binary.Base64;
 
 import com.narrowtux.blueberry.HttpException;
 import com.narrowtux.blueberry.handler.HttpRequestHandler;
 import com.narrowtux.blueberry.http.HttpExchange;
 import com.narrowtux.blueberry.http.HttpRequestMethod;
 import com.narrowtux.blueberry.http.HttpVersion;
 import com.narrowtux.blueberry.http.headers.HttpHeaders;
 import com.narrowtux.blueberry.http.headers.HttpStatusCode;
 import com.narrowtux.blueberry.util.HeaderUtils;
 
 public abstract class WebSocketRequestHandler extends HttpRequestHandler {
 	public static final String GLOBALLY_UNIQUE_IDENTIFIER = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
 	private static final MessageDigest SHA1;
 	static {
 		MessageDigest sha1 = null;
 		try {
 			sha1 = MessageDigest.getInstance("SHA-1");
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} finally {
 			SHA1 = sha1;
 		}
 	}
 	@Override
 	public void handle(HttpExchange exchange) throws IOException, HttpException {
 		exchange.disableCaching();
 		exchange.getResponseHeaders().addHeader("Connection", "Upgrade");
 		exchange.getResponseHeaders().setHeader("Content-Type", null);
 		
		/// Security handshake
 		String sec = HeaderUtils.getHeaderValueAs(exchange.getRequestHeaders(), "Sec-WebSocket-Key", String.class);
 		sec = sec + GLOBALLY_UNIQUE_IDENTIFIER;
		sec = Base64.encodeBase64String(SHA1.digest(sec.getBytes()));
 		
 		String origin = HeaderUtils.getHeaderValueAs(exchange.getRequestHeaders(), "Origin", String.class);
 		if (origin != null) {
 			exchange.getResponseHeaders().setHeader("Access-Control-Allow-Origin", origin);
 		}
 		exchange.getResponseHeaders().setHeader("Access-Control-Allow-Credentials", "true");
 		exchange.getResponseHeaders().setHeader("Access-Control-Allow-Headers", "content-type");
 		
 		exchange.getResponseHeaders().setHeader("Sec-WebSocket-Accept", sec);
 		exchange.getResponseHeaders().addHeader("Upgrade", "websocket");
 		
 		exchange.sendResponseHeaders(HttpStatusCode.HTTP_101_SWITCHING_PROTOCOLS, 0);
 		
 		final WebSocketExchange wse = new WebSocketExchange(exchange.getInputStream(), exchange.getOutputStream(), exchange);
 		
 		(new Thread() {
 			Frame unfinishedFrame = null;
 			
 			public void run() {
 				try {
 					onConnect(wse);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				
 				while (true) {
 					try {
 						if (!readFrameData(wse, unfinishedFrame)) {
 							onClose(wse);
 							break;
 						}
 					} catch (SocketException e1) {
 						//Silently fail
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 			private boolean readFrameData(final WebSocketExchange wse, Frame unfinishedFrame) throws IOException {
 				InputStream in = wse.getInputStream();
 				byte opCode;
 				byte rsv;
 				boolean fin;
 				boolean maskEnabled;
 				long len;
 				
 				int first = in.read();
 				if (first == -1) {
 					wse.close();
 					return false;
 				}
 				int second = in.read();
 				fin = (first & 0x80) > 0;
 				rsv = (byte) ((first >> 4) & 0x07);
 				opCode = (byte) (first & 0x0F);
 				maskEnabled = (second & 0x80) > 0;
 				len = (second & 0x7F);
 //				System.out.println("fin = "+fin);
 //				System.out.println("rsv = "+rsv);
 //				System.out.println("opcode = "+opCode);
 //				System.out.println("mask enabled: "+maskEnabled);
 				
 				if (len == 126) { // length is 16 bit long
 					len = in.read() << 8 | in.read();
 				} else if (len == 127) { // length is 64 bit long
 					// Fun fact: maximum size of a frames payload is 8 ExaByte
 					len = in.read() << 24 | in.read() << 16 | in.read() << 8 | in.read();
 				}
 				
 //				System.out.println("len = "+len);
 				
 				byte mask[] = new byte[4];
 				if (maskEnabled) {
 					in.read(mask);
 				}
 				
 //				System.out.println("mask = "+mask);
 				
 				byte data[] = new byte[(int) (opCode == Frame.OP_TEXT ? len * 2 : len)];
 				wse.getInputStream().read(data);
 				
 				if (maskEnabled) {
 					for (int i = 0; i < len; i++) {
 						int original_octet_i = 0xFF & data[i];
 						int mask_octet_j = 0xFF & mask[i % 4];
 						data[i] = (byte) (original_octet_i ^ mask_octet_j);
 					}
 				}
 				
 				if (opCode == Frame.OP_CLOSE) {
 					wse.close();
 					return false;
 				}
 				
 				if (opCode == Frame.OP_CONT) {
 					if (unfinishedFrame != null) {
 						unfinishedFrame.readPayload(data);
 					} else {
 						wse.close();
 						return false;
 					}
 				} else {
 					unfinishedFrame = Frame.newFrame(opCode);
 					unfinishedFrame.readPayload(data);
 				}
 				if (fin) {
 					onFrameReceived(wse, unfinishedFrame);
 					unfinishedFrame = null;
 				}
 				
 				return true;
 			};
 		}).start();
 	}
 	
 	public void onConnect(WebSocketExchange exchange) throws IOException {
 		
 	}
 	
 	public void onFrameReceived(WebSocketExchange exchange, Frame frame) throws IOException {
 		
 	}
 	
 	public void onClose(WebSocketExchange exchange) throws IOException {
 		
 	}
 	
 	@Override
 	public boolean doesMatch(HttpVersion version, HttpRequestMethod method, URI uri, HttpHeaders requestHeaders) {
 		if (HeaderUtils.headerEquals(requestHeaders, "Upgrade", "websocket") &&
 				HeaderUtils.headerEquals(requestHeaders, "Connection", "Upgrade")) {
 			if (method == HttpRequestMethod.GET || method == HttpRequestMethod.POST) {
 				return uri.getPath().startsWith(getFilter());
 			} else {
 				return false;
 			}
 		}
 		return false;	
 	}
 	
 	@Override
 	public boolean shouldClose() {
 		return false;
 	}
 }
