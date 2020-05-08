 package com.fbudassi.neddy.benchmark.handler;
 
 import com.fbudassi.neddy.benchmark.benchmarks.WebsocketBenchmark;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
 import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
 import org.jboss.netty.util.CharsetUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Websocket benchmark channel handler.
  *
  * @author fbudassi
  */
 public class WebSocketClientHandler extends SimpleChannelUpstreamHandler {
 
     private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);
 
     /**
      * Executed when a new message is received.
      *
      * @param ctx
      * @param e
      * @throws Exception
      */
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
         Channel ch = ctx.getChannel();
 
         // Finish with the Websocket handshake if it's not done yet.
         WebSocketClientHandshaker handshaker = (WebSocketClientHandshaker) ch.getAttachment();
         if (!handshaker.isHandshakeComplete()) {
             handshaker.finishHandshake(ch, (HttpResponse) e.getMessage());
 
             // Request the list of categories.
            //WebsocketBenchmark.getCategories(ch);
 
             return;
         }
 
         // Check for bad messages.
         if (e.getMessage() instanceof HttpResponse) {
             HttpResponse response = (HttpResponse) e.getMessage();
             throw new Exception("Unexpected HttpResponse (status=" + response.getStatus() + ", content="
                     + response.getContent().toString(CharsetUtil.UTF_8) + ")");
         }
 
         // Do the necessary stuff to process the websocket frame that was received.
         WebSocketFrame frame = (WebSocketFrame) e.getMessage();
         if (frame instanceof TextWebSocketFrame) {
             TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            WebsocketBenchmark.processFrame(ch, textFrame.getText());
         } else if (frame instanceof CloseWebSocketFrame) {
             ch.close();
         }
     }
 
     /**
      * Executed when an exception is caught.
      *
      * @param ctx
      * @param e
      * @throws Exception
      */
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
         logger.error("Error in handler", e.getCause());
         e.getChannel().close();
     }
 }
