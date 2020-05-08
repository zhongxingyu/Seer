 /*
  * Made with all the love in the world
  * by scireum in Remshalden, Germany
  *
  * Copyright by scireum GmbH
  * http://www.scireum.de - info@scireum.de
  */
 
 package sirius.web.http;
 
 import io.netty.channel.ChannelDuplexHandler;
 import io.netty.channel.ChannelFutureListener;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelPromise;
 import io.netty.handler.codec.http.*;
 import io.netty.handler.codec.http.multipart.Attribute;
 import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
 import io.netty.handler.timeout.IdleStateEvent;
 import io.netty.util.AttributeKey;
 import sirius.kernel.async.CallContext;
 import sirius.kernel.commons.Strings;
 import sirius.kernel.health.Exceptions;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.channels.ClosedChannelException;
 import java.util.List;
 
 /**
  * Handles incoming HTTP requests.
  * <p>
  * Takes care of gluing together chunks, handling file uploads etc. In order to participate in handling HTTP requests,
  * one has to provide a {@link WebDispatcher} rather than modifying this class.
  * </p>
  *
  * @author Andreas Haufler (aha@scireum.de)
  * @since 2013/08
  */
 class WebServerHandler extends ChannelDuplexHandler {
 
     private List<WebDispatcher> sortedDispatchers;
     private int numKeepAlive = 5;
     private HttpRequest currentRequest;
     private WebContext currentContext;
 
     /**
      * Creates a new instance based on a pre sorted list of dispatchers.
      *
      * @param sortedDispatchers the sorted list of dispatchers responsible for handling HTTP requests.
      */
     WebServerHandler(List<WebDispatcher> sortedDispatchers) {
         this.sortedDispatchers = sortedDispatchers;
     }
 
     public static final AttributeKey<CallContext> CALL_CONTEXT_ATTRIBUTE_KEY = AttributeKey.valueOf("sirius.CallContext");
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
         io.netty.util.Attribute<CallContext> attr = ctx.attr(CALL_CONTEXT_ATTRIBUTE_KEY);
         if (attr != null) {
             CallContext.setCurrent(attr.get());
         }
         if (e instanceof ClosedChannelException) {
             WebServer.LOG.FINE(e);
         } else if (e instanceof IOException && "Connection reset by peer".equals(e.getMessage())) {
             WebServer.LOG.FINE(e);
         } else {
             Exceptions.handle(WebServer.LOG, e);
             try {
                 if (ctx.channel().isOpen()) {
                     ctx.channel().close();
                 }
             } catch (Throwable t) {
                 Exceptions.ignore(t);
             }
         }
         currentRequest = null;
     }
 
     /*
      * Binds the request to the CallContext
      */
     private WebContext setupContext(ChannelHandlerContext ctx, HttpRequest req) {
         CallContext cc = CallContext.initialize();
         cc.addToMDC("uri", req.getUri());
         WebContext wc = cc.get(WebContext.class);
         wc.setCtx(ctx);
         wc.setRequest(req);
         ctx.attr(CALL_CONTEXT_ATTRIBUTE_KEY).set(cc);
         return wc;
     }
 
     @Override
     public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
         if (evt instanceof IdleStateEvent) {
             io.netty.util.Attribute<CallContext> attr = ctx.attr(CALL_CONTEXT_ATTRIBUTE_KEY);
             if (attr != null) {
                 CallContext callContext = attr.get();
                 if (callContext == null) {
                     ctx.channel().close();
                     return;
                 }
                 CallContext.setCurrent(callContext);
                 WebContext wc = callContext.get(WebContext.class);
                 if (wc == null) {
                     ctx.channel().close();
                     return;
                 }
                 if (!wc.isLongCall()) {
                     if (WebServer.LOG.isFINE()) {
                         WebServer.LOG.FINE("IDLE: " + wc.getRequestedURI());
                     }
                     WebServer.idleTimeouts++;
                     if (WebServer.idleTimeouts < 0) {
                         WebServer.idleTimeouts = 0;
                     }
                     ctx.channel().close();
                     return;
                 }
             }
         }
     }
 
     /**
      * Used by responses to determine if keepalive is supported.
      * <p>
      * Internally we used a countdown, to limit the max number of keepalives for a connection. Calling this method
      * decrements the internal counter, therefore this must not be called several times per request.
      * </p>
      *
      * @return <tt>true</tt> if keepalive is still supported, <tt>false</tt> otherwise.
      */
     public boolean shouldKeepAlive() {
         return numKeepAlive-- > 0;
     }
 
     @Override
     public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
         ctx.flush();
         super.channelReadComplete(ctx);
     }
 
     @Override
     public void disconnect(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
         cleanup();
         WebServer.openConnections.decrementAndGet();
         super.disconnect(ctx, future);
     }
 
     @Override
     public void channelRead(ChannelHandlerContext ctx, Object msg) {
         try {
             if (msg instanceof HttpRequest) {
                 handleRequest(ctx, (HttpRequest) msg);
             } else if (msg instanceof LastHttpContent) {
                 try {
                     if (currentRequest == null) {
                         WebServer.LOG.FINE("Ignoring CHUNK without request: " + msg);
                         return;
                     }
                     processContent(ctx, (HttpContent) msg);
                 } finally {
                     ((HttpContent) msg).release();
                 }
                 dispatch();
             } else if (msg instanceof HttpContent) {
                 try {
                     if (currentRequest == null) {
                         WebServer.LOG.FINE("Ignoring CHUNK without request: " + msg);
                         return;
                     }
                     if (!(currentRequest.getMethod() == HttpMethod.POST) && !(currentRequest.getMethod() == HttpMethod.PUT)) {
                         currentContext.respondWith()
                                       .error(HttpResponseStatus.BAD_REQUEST, "Only POST or PUT may sent chunked data");
                         currentRequest = null;
                         return;
                     }
                     WebServer.chunks++;
                     if (WebServer.chunks < 0) {
                         WebServer.chunks = 0;
                     }
                     processContent(ctx, (HttpContent) msg);
                 } finally {
                     ((HttpContent) msg).release();
                 }
             }
         } catch (Throwable t) {
             if (currentRequest != null) {
                 try {
                     currentContext.respondWith()
                                   .error(HttpResponseStatus.BAD_REQUEST,
                                          Exceptions.handle(WebServer.LOG, t).getMessage());
                 } catch (Exception e) {
                     Exceptions.ignore(e);
                 }
                 currentRequest = null;
             }
         }
     }
 
     /*
      * Signals that a bad or incomplete request was received
      */
     private void signalBadRequest(ChannelHandlerContext ctx) {
         WebServer.clientErrors++;
         if (WebServer.clientErrors < 0) {
             WebServer.clientErrors = 0;
         }
         ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST))
            .addListener(ChannelFutureListener.CLOSE);
         currentRequest = null;
     }
 
     /*
      * Handles a new request - called once the first chunk of data of a request is available.
      */
     private void handleRequest(ChannelHandlerContext ctx, HttpRequest req) {
         try {
             WebServer.requests++;
             if (WebServer.requests < 0) {
                 WebServer.requests = 0;
             }
 
             cleanup();
             if (WebServer.LOG.isFINE()) {
                 WebServer.LOG.FINE("OPEN: " + req.getUri());
             }
             // Handle a bad request.
             if (!req.getDecoderResult().isSuccess()) {
                 signalBadRequest(ctx);
                 return;
             }
             currentRequest = req;
             currentContext = setupContext(ctx, req);
 
             try {
                 if (!WebServer.getIPFilter().isEmpty()) {
                     if (!WebServer.getIPFilter().accepts(currentContext.getRemoteIP())) {
                         WebServer.blocks++;
                         if (WebServer.blocks < 0) {
                             WebServer.blocks = 0;
                         }
                         if (WebServer.LOG.isFINE()) {
                             WebServer.LOG.FINE("BLOCK: " + req.getUri());
                         }
                         ctx.channel().close();
                         return;
                     }
                 }
 
                 if (HttpHeaders.is100ContinueExpected(req)) {
                     if (WebServer.LOG.isFINE()) {
                         WebServer.LOG.FINE("CONTINUE: " + req.getUri());
                     }
                     send100Continue(ctx);
                 }
 
                 if (req.getMethod() == HttpMethod.POST || req.getMethod() == HttpMethod.POST) {
                     String contentType = req.headers().get(HttpHeaders.Names.CONTENT_TYPE);
                     if (Strings.isFilled(contentType) && (contentType.startsWith("multipart/form-data") || contentType.startsWith(
                             "application/x-www-form-urlencoded"))) {
                         if (WebServer.LOG.isFINE()) {
                             WebServer.LOG.FINE("POST/PUT-FORM: " + req.getUri());
                         }
                         HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(WebServer.getHttpDataFactory(),
                                                                                         req);
                         currentContext.setPostDecoder(postDecoder);
                     } else {
                         if (WebServer.LOG.isFINE()) {
                             WebServer.LOG.FINE("POST/PUT-DATA: " + req.getUri());
                         }
                         Attribute body = WebServer.getHttpDataFactory().createAttribute(req, "body");
                         if (req instanceof FullHttpRequest) {
                            body.setContent(((FullHttpRequest) req).content().retain());
                         }
                         currentContext.content = body;
                     }
                 } else if (!(currentRequest.getMethod() == HttpMethod.GET) && !(currentRequest.getMethod() == HttpMethod.HEAD) && !(currentRequest
                         .getMethod() == HttpMethod.DELETE)) {
                     currentContext.respondWith()
                                   .error(HttpResponseStatus.BAD_REQUEST,
                                          Strings.apply("Cannot %s as method. Use GET, POST, PUT, HEAD, DELETE",
                                                        req.getMethod().name()));
                     currentRequest = null;
                 }
             } catch (Throwable t) {
                 currentContext.respondWith()
                               .error(HttpResponseStatus.INTERNAL_SERVER_ERROR, Exceptions.handle(WebServer.LOG, t));
                 currentRequest = null;
             }
         } catch (Throwable t) {
             Exceptions.handle(WebServer.LOG, t);
             try {
                 ctx.channel().close();
             } catch (Exception ex) {
                 Exceptions.ignore(ex);
             }
             currentRequest = null;
         }
     }
 
     /*
      * Sends an 100 CONTINUE response to conform to the keepalive protocol
      */
     private void send100Continue(ChannelHandlerContext e) {
         HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
         e.channel().write(response);
     }
 
     /*
      * Releases the last context (request) which was processed by this handler.
      */
     private void cleanup() {
         if (currentContext != null) {
             currentContext.release();
             currentContext = null;
         }
     }
 
     /*
      * Reads another chunk of data for a previously started request
      */
     private void processContent(ChannelHandlerContext ctx, HttpContent chunk) {
         try {
             if (chunk.content().readableBytes() == 0) {
                 return;
             }
             if (currentContext.getPostDecoder() != null) {
                 if (WebServer.LOG.isFINE()) {
                     WebServer.LOG
                              .FINE("POST-CHUNK: " + currentContext.getRequestedURI() + " - " + chunk.content()
                                                                                                     .readableBytes() + " bytes");
                 }
                 currentContext.getPostDecoder().offer(chunk);
             } else if (currentContext.content != null) {
                 if (WebServer.LOG.isFINE()) {
                     WebServer.LOG
                              .FINE("DATA-CHUNK: " + currentContext.getRequestedURI() + " - " + chunk.content()
                                                                                                     .readableBytes() + " bytes");
                 }
                currentContext.content.addContent(chunk.content().retain(), chunk instanceof LastHttpContent);

                 if (!currentContext.content.isInMemory()) {
                     File file = currentContext.content.getFile();
                     checkUploadFileLimits(file);
                 }
             }
         } catch (Throwable ex) {
             currentContext.respondWith()
                           .error(HttpResponseStatus.INTERNAL_SERVER_ERROR, Exceptions.handle(WebServer.LOG, ex));
             currentRequest = null;
         }
     }
 
     /*
      * Checks if the can still upload more date
      */
     private void checkUploadFileLimits(File file) {
         if (file.getFreeSpace() < WebServer.getMinUploadFreespace() && WebServer.getMinUploadFreespace() > 0) {
             if (WebServer.LOG.isFINE()) {
                 WebServer.LOG.FINE("Not enough space to handle: " + currentContext.getRequestedURI());
             }
             currentContext.respondWith()
                           .error(HttpResponseStatus.INSUFFICIENT_STORAGE,
                                  Exceptions.handle()
                                            .withSystemErrorMessage(
                                                    "The web server is running out of temporary space to store the upload")
                                            .to(WebServer.LOG)
                                            .handle());
             currentRequest = null;
         }
         if (file.length() > WebServer.getMaxUploadSize() && WebServer.getMaxUploadSize() > 0) {
             if (WebServer.LOG.isFINE()) {
                 WebServer.LOG.FINE("Body is too large: " + currentContext.getRequestedURI());
             }
             currentContext.respondWith()
                           .error(HttpResponseStatus.INSUFFICIENT_STORAGE,
                                  Exceptions.handle()
                                            .withSystemErrorMessage(
                                                    "The uploaded file exceeds the maximal upload size of %d bytes",
                                                    WebServer.getMaxUploadSize())
                                            .to(WebServer.LOG)
                                            .handle());
             currentRequest = null;
         }
     }
 
     /*
      * Dispatches the completely read request.
      */
     private void dispatch() throws Exception {
         if (WebServer.LOG.isFINE() && currentContext != null) {
             WebServer.LOG.FINE("DISPATCHING: " + currentContext.getRequestedURI());
         }
 
         for (WebDispatcher wd : sortedDispatchers) {
             try {
                 if (wd.dispatch(currentContext)) {
                     if (WebServer.LOG.isFINE()) {
                         WebServer.LOG.FINE("DISPATCHED: " + currentContext.getRequestedURI() + " to " + wd);
                     }
                     currentRequest = null;
                     return;
                 }
             } catch (Exception e) {
                 Exceptions.handle(WebServer.LOG, e);
             }
         }
     }
 
 
 }
