 /**
  * 
  */
 package com.github.glue.httpq.transport;
 
 import java.net.InetSocketAddress;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelState;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ChannelUpstreamHandler;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.timeout.IdleStateEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author eric
  *
  */
 public abstract class NettyHandler implements ChannelUpstreamHandler{
 	private Logger log = LoggerFactory.getLogger(getClass());
 	
 	public void handleUpstream(ChannelHandlerContext context, ChannelEvent event)
 			throws Exception {
 		if(event instanceof MessageEvent){
 			handle((HttpRequest)((MessageEvent) event).getMessage(),event.getChannel());
 		}else if(event instanceof ExceptionEvent){
 			log.error("Exception caught ",event);
 			handleException(((ExceptionEvent)event).getCause(), event.getChannel());
 			event.getChannel().close();
 		}else if(event instanceof ChannelStateEvent){
 			ChannelStateEvent state = (ChannelStateEvent)event;
 			Channel channel =  state.getChannel();
 			InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
 			if(state.getState().equals(ChannelState.CONNECTED) && state.getValue() == null){
 				
 			}else if(state.getState().equals(ChannelState.OPEN) && Boolean.TRUE.equals(state.getValue())){
				log.debug("New session from "+remoteAddress.getAddress().getHostAddress()+":"+remoteAddress.getPort());
 			}else if(state.getState().equals(ChannelState.OPEN) && Boolean.FALSE.equals(state.getValue())){
				log.debug("Session close "+remoteAddress.getAddress().getHostAddress()+":"+remoteAddress.getPort());
 			}
 		}else if(event instanceof IdleStateEvent){
 			log.debug("Idle timeout on session %s", event.getChannel());
 			event.getChannel().close();
 		}else{
 			context.sendUpstream(event);
 		}
 	}
 
 	protected abstract void handle(HttpRequest request, Channel channel);
 	protected abstract void handleException(Throwable e, Channel channel);
 }
