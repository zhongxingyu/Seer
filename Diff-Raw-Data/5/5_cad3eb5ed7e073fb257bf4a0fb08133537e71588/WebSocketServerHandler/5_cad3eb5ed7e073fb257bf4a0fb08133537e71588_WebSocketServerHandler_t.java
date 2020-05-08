 /*
  * Copyright 2012 The Netty Project
  *
  * The Netty Project licenses this file to you under the Apache License,
  * version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at:
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 package org.jboss.tools.web.pagereloader.internal.remote.websocketx;
 
 import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
 import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
 import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.HOST;
 import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
 import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
 import org.jboss.netty.logging.InternalLogger;
 import org.jboss.netty.logging.InternalLoggerFactory;
 import org.jboss.netty.util.CharsetUtil;
 import org.jboss.tools.web.pagereloader.internal.util.Logger;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 /**
  * Handles handshakes and messages
  */
 public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
 	private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocketServerHandler.class);
 
	private static final String WEBSOCKET_PATH = "/livereload";
 
 	private WebSocketServerHandshaker handshaker;
 
 	private final List<Channel> channels = new ArrayList<Channel>();
 
 	private final ObjectMapper objectMapper = new ObjectMapper();
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
 		Object msg = e.getMessage();
 		if (msg instanceof HttpRequest) {
 			handleHttpRequest(ctx, (HttpRequest) msg);
 		} else if (msg instanceof WebSocketFrame) {
 			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
 		}
 	}
 
 	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
 		// Allow only GET methods.
 		if (req.getMethod() != GET) {
 			sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
 			return;
 		}
 
 		// Handshake
		if (req.getUri().equals(WEBSOCKET_PATH)) {
 			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
 					getWebSocketLocation(req), null, false);
 			handshaker = wsFactory.newHandshaker(req);
 			if (handshaker == null) {
 				wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
 			} else {
 				handshaker.handshake(ctx.getChannel(), req).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
 			}
 		}
 	}
 
 	private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
 		// Generate an error page if response status code is not OK (200).
 		if (res.getStatus().getCode() != 200) {
 			res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
 			setContentLength(res, res.getContent().readableBytes());
 		}
 
 		// Send the response and close the connection if necessary.
 		ChannelFuture f = ctx.getChannel().write(res);
 		if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
 			f.addListener(ChannelFutureListener.CLOSE);
 		}
 	}
 
 	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws InterruptedException {
 
 		// Check for closing frame
 		final Channel channel = ctx.getChannel();
 		if (frame instanceof CloseWebSocketFrame) {
 			handshaker.close(channel, (CloseWebSocketFrame) frame);
 			//channels.remove(channel);
 			System.out.println("Browser disconnected");
 			return;
 		} else if (frame instanceof PingWebSocketFrame) {
 			channel.write(new PongWebSocketFrame(frame.getBinaryData()));
 			return;
 		} else if (!(frame instanceof TextWebSocketFrame)) {
 			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
 					.getName()));
 		}
 
 		String request = ((TextWebSocketFrame) frame).getText();
 		System.out.println("Browser connecting with request [" + request + "]");
 		if (logger.isDebugEnabled()) {
 			logger.debug(String.format("Channel %s received %s", channel.getId(), request));
 		}
 		final ChannelFuture await = channel.write(new TextWebSocketFrame("!!ver:1.6")).await();
 		while (!await.isDone()) {
 			Thread.sleep(100);
 		}
 		if (await.isSuccess()) {
 			channels.add(channel);
 			System.out.println("Browser connected");
 		} else {
 			System.err.println("Browser not connected:" + await.getCause());
 
 		}
 	}
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
 		e.getCause().printStackTrace();
 		e.getChannel().close();
 	}
 
 	private static String getWebSocketLocation(HttpRequest req) {
 		return "ws://" + req.getHeader(HOST) + WEBSOCKET_PATH;
 	}
 
 	public void notifyResourceChange(final String path) {
 		try {
 			List<Object> command = new ArrayList<Object>();
 			Map<String, Object> refreshArgs = new HashMap<String, Object>();
 			command.add("refresh");
 			refreshArgs.put("path", path);
 			refreshArgs.put("apply_js_live", true);
 			refreshArgs.put("apply_css_live", true);
 			command.add(refreshArgs);
 			StringWriter commandWriter = new StringWriter();
 			objectMapper.writeValue(commandWriter, command);
 			String cmd = commandWriter.toString();
 			for (Iterator<Channel> iterator = channels.iterator(); iterator.hasNext();) {
 				Channel channel = iterator.next();
 				if (channel.isOpen()) {
 					System.err.println("[" + Thread.currentThread().getName() + "] Sending command '" + cmd
 							+ "' to browser...");
 					channel.write(new TextWebSocketFrame(cmd));
 				} else {
 					iterator.remove();
 				}
 			}
 		} catch (Exception e) {
 			Logger.error("Failed to notify browser(s)", e);
 		}
 	}
 
 	public void closeChannels() {
 		for (Channel channel : channels) {
 			try {
 				channel.close().await();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
