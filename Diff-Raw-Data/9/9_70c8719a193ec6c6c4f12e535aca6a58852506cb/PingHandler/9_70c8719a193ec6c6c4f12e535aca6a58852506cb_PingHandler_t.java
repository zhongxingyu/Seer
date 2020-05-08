 /**
  * Copyright (c) 2011 Red Hat, Inc.
  *
  * This software is licensed to you under the GNU General Public License,
  * version 2 (GPLv2). There is NO WARRANTY for this software, express or
  * implied, including the implied warranties of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
  * along with this software; if not, see
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
  *
  * Red Hat trademarks are not licensed under GPLv2. No permission is
  * granted to use or replicate Red Hat trademarks that are incorporated
  * in this software or its documentation.
  */
 package org.candlepin.thumbslug;
 
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_GATEWAY;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
 import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
 
 import org.candlepin.thumbslug.HttpCandlepinClient.CandlepinClientResponseHandler;
 
 import org.apache.log4j.Logger;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 
 /**
 * Simple handler to intercept calls to /ping and return a 204. This lets the
  * user know that Thumbslug is actually running.
  */
 public class PingHandler extends SimpleChannelUpstreamHandler {
 
     private static Logger log = Logger.getLogger(PingHandler.class);
 
     private Config config;
     private ChannelFactory channelFactory;
 
     PingHandler(Config config, ChannelFactory channelFactory) {
         this.config = config;
         this.channelFactory = channelFactory;
     }
 
     @Override
     public void messageReceived(final ChannelHandlerContext ctx, MessageEvent e)
         throws Exception {
 
         HttpRequest request = (HttpRequest) e.getMessage();
 
         if ("/ping".equals(request.getUri())) {
             // if dynamicSsl is off, we're in testing mode and shouldn't ping
             // candlepin.
             if (config.getBoolean("ssl.client.dynamicSsl")) {
                 HttpCandlepinClient client = new HttpCandlepinClient(config,
                     new CandlepinClientResponseHandler() {
                         @Override
                         public void onResponse(String buffer) throws Exception {
                             log.debug("Ping received from candlepin.");
                             sendResponseToClient(ctx, NO_CONTENT);
                         }
 
                         @Override
                         public void onError(Throwable reason) {
                             log.error("Error talking to candlepin", reason);
                             sendResponseToClient(ctx, BAD_GATEWAY);
                         }
 
                         @Override
                         public void onNotFound() {
                            log.error("Resource not found!");
                            sendResponseToClient(ctx, BAD_GATEWAY);
                         }
 
                         @Override
                         public void onOtherResponse(int code) {
                             log.error("Unexpected response code from candlepin: " +
                                 code);
                             sendResponseToClient(ctx, BAD_GATEWAY);
                         }
 
                     }, channelFactory);
 
                 client.getCandlepinStatus();
             }
             else {
                 sendResponseToClient(ctx, NO_CONTENT);
             }
         }
         else {
             super.messageReceived(ctx, e);
         }
     }
 
     private void sendResponseToClient(ChannelHandlerContext ctx,
         HttpResponseStatus status) {
         HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
         ChannelFuture future = ctx.getChannel().write(response);
         future.addListener(ChannelFutureListener.CLOSE);
     }
 }
