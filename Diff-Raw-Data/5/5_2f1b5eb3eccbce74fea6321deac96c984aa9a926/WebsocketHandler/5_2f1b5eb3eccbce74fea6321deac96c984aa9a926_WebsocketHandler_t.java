 package org.oobium.client.websockets;
 
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.SWITCHING_PROTOCOLS;
 
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
 import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
 import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.jboss.netty.handler.codec.http.HttpVersion;
 import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
 import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
 import org.oobium.client.websockets.Websocket.State;
 
 public class WebsocketHandler extends SimpleChannelUpstreamHandler {
 
 	private final ClientBootstrap bootstrap;
 	final Websocket socket;
 	final List<WebsocketListener> listeners;
 	
 	Channel channel;
 	boolean autoReconnect;
 	
 	WebsocketHandler(ClientBootstrap bootstrap, URI uri) {
 		this.bootstrap = bootstrap;
 		this.socket = new Websocket(this, uri);
 		this.listeners = new ArrayList<WebsocketListener>();
 	}
 
 	@Override
     public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
 		socket.state = State.Connecting;
 		
     	String host = socket.getHost();
     	String path = socket.getFullPath();
         HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
         request.addHeader(Names.UPGRADE, Values.WEBSOCKET);
         request.addHeader(Names.CONNECTION, Values.UPGRADE);
         request.addHeader(Names.HOST, host);
         request.addHeader(Names.ORIGIN, "http://" + host);
         
 		channel = ctx.getChannel();
         channel.write(request);
         channel.getPipeline().replace("encoder", "wsencoder", new WebSocketFrameEncoder());
     }
 	
 	@Override
 	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
 		if(autoReconnect) {
 			connect();
 		} else {
 			socket.state = State.Closed;
 		}
 		notifyDisconnect();
 	}
 
     ChannelFuture connect() {
     	if(channel != null && channel.isConnected()) {
     		return null;
     	}
 		socket.state = State.Connecting;
         return bootstrap.connect(new InetSocketAddress(socket.getHost(), socket.getPort()));
     }
 
     ChannelFuture disconnect() {
    	socket.state = State.Closing;
    	if(channel == null) {
    		// when disconnect is called before channelConnected
    		return null;
    	}
     	return channel.close();
     }
     
     @Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
 		notifyError(e.getCause());
 		ctx.getChannel().close();
 	}
 	
 	private void handshake(ChannelHandlerContext ctx, HttpResponse response) {
     	HttpResponseStatus status = response.getStatus();
     	if(status.getCode() == SWITCHING_PROTOCOLS.getCode()) {
     		if(Values.WEBSOCKET.equals(response.getHeader(Names.UPGRADE))) {
     			if(Values.UPGRADE.equals(response.getHeader(Names.CONNECTION))) {
     				socket.state = State.Open;
     	            ctx.getPipeline().replace("decoder", "wsdecoder", new WebSocketFrameDecoder());
     	            notifyConnect();
     	            return;
     			}
     		}
     	}
     	throw new RuntimeException("Invalid handshake response");
     }
 	
 	private WebsocketListener[] listeners() {
 		if(listeners.isEmpty()) {
 			return new WebsocketListener[0];
 		}
 		return listeners.toArray(new WebsocketListener[listeners.size()]);
 	}
 	
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
     	Object msg = e.getMessage();
     	if(msg instanceof HttpResponse) {
     		handshake(ctx, (HttpResponse) msg);
     	} else {
             notifyMessage((WebSocketFrame) msg);
     	}
 	}
 
 	private void notifyConnect() {
 		for(WebsocketListener l : listeners()) {
 			l.onConnect(socket);
 		}
 	}
 	
     private void notifyDisconnect() {
 		for(WebsocketListener l : listeners()) {
 			l.onDisconnect(socket);
 		}
 	}
 
     private void notifyError(Throwable t) {
 		for(WebsocketListener l : listeners()) {
 			l.onError(socket, t);
 		}
 	}
     
     private void notifyMessage(WebSocketFrame frame) {
 		for(WebsocketListener l : listeners()) {
 			l.onMessage(socket, frame);
 		}
 	}
 	
 }
