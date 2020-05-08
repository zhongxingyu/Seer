 package ddth.dasp.hetty.front;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.handler.codec.http.Cookie;
 import org.jboss.netty.handler.codec.http.CookieDecoder;
 import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
 import org.jboss.netty.handler.codec.http.HttpChunk;
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.jboss.netty.handler.codec.http.HttpVersion;
 import org.jboss.netty.handler.codec.http.QueryStringDecoder;
 import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
 import org.jboss.netty.handler.timeout.IdleStateEvent;
 import org.jboss.netty.util.CharsetUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.protobuf.ByteString;
 
 import ddth.dasp.common.id.IdGenerator;
 import ddth.dasp.hetty.message.HettyProtoBuf;
 import ddth.dasp.hetty.qnt.IQueueWriter;
 
 public class HettyHttpHandler extends IdleStateAwareChannelHandler {
     private static Logger LOGGER = LoggerFactory.getLogger(HettyHttpHandler.class);
     private HttpRequest currentRequest;
     // private long maxRequestContentlength = 64 * 1024;
     private ByteArrayOutputStream currentRequestContent = new ByteArrayOutputStream(4096);
 
     // private long currentRequestContentLength = 0;
     private IQueueWriter queueWriter;
 
     public HettyHttpHandler(IQueueWriter queeuWriter) {
         this.queueWriter = queeuWriter;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
         HettyConnServer.ALL_CHANNELS.add(e.getChannel());
     }
 
     protected void validateHttpChunk(HttpChunk httpChunk) {
         if (currentRequest == null || currentRequest.getContent() == null) {
             throw new IllegalStateException("No chunk started!");
         }
     }
 
     protected void handleRequest(HttpRequest request, byte[] requestContent, Channel userChannel)
             throws Exception {
         String uri = request.getUri();
        if (uri.startsWith("/status/") || uri.startsWith("/status?") || uri.equals("/status")) {
             StringBuilder content = new StringBuilder();
             content.append("Connections: " + HettyConnServer.ALL_CHANNELS.size());
 
             HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                     HttpResponseStatus.OK);
             response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
             response.setContent(ChannelBuffers.copiedBuffer(content.toString(), CharsetUtil.UTF_8));
             userChannel.write(response).addListener(new ChannelFutureListener() {
                 public void operationComplete(ChannelFuture future) throws Exception {
                     future.getChannel().close();
                 }
             });
             return;
         }
 
         HettyProtoBuf.Request.Builder requestProtoBuf = HettyProtoBuf.Request.newBuilder();
         String requestId = IdGenerator.getInstance(IdGenerator.getMacAddr()).generateId64Ascii();
 
         // populate required fields
         requestProtoBuf.setTimestamp(System.currentTimeMillis());
         requestProtoBuf.setId(requestId);
         requestProtoBuf.setResponseTopic(requestId);
         requestProtoBuf.setChannelId(userChannel.getId());
         requestProtoBuf.setMethod(request.getMethod().toString());
         requestProtoBuf.setUri(uri);
         // parse path parameters
         QueryStringDecoder qsd = new QueryStringDecoder(uri, CharsetUtil.UTF_8);
         String path = qsd.getPath();
         String[] pathTokens = path.replaceAll("^\\/+", "").replaceAll("\\/+$", "").split("\\/");
         requestProtoBuf.addAllPathParams(Arrays.asList(pathTokens));
         // parse url parameters
         Map<String, List<String>> urlParams = qsd.getParameters();
         for (Entry<String, List<String>> urlParam : urlParams.entrySet()) {
             String name = urlParam.getKey();
             String value = urlParam.getValue().size() > 0 ? urlParam.getValue().get(0) : "";
             HettyProtoBuf.NameValue param = HettyProtoBuf.NameValue.newBuilder().setName(name)
                     .setValue(value).build();
             requestProtoBuf.addUrlParams(param);
         }
         // parse host & port
         String hostAndPort = request.getHeader("Host");
         String[] tokens = hostAndPort.split(":");
         requestProtoBuf.setDomain(tokens[0]);
         try {
             requestProtoBuf.setPort(Integer.parseInt(tokens[1]));
         } catch (Exception e) {
             requestProtoBuf.setPort(80);
         }
 
         String strCookie = request.getHeader("Cookie");
         if (!StringUtils.isBlank(strCookie)) {
             Set<Cookie> cookies = new CookieDecoder().decode(strCookie);
             for (Cookie cookie : cookies) {
                 HettyProtoBuf.Cookie.Builder cookieProtoBuf = HettyProtoBuf.Cookie.newBuilder();
                 cookieProtoBuf.setName(cookie.getName());
                 cookieProtoBuf.setValue(cookie.getValue());
                 cookieProtoBuf.setDomain(cookie.getDomain() != null ? cookie.getDomain() : "");
                 cookieProtoBuf.setPath(cookie.getPath() != null ? cookie.getPath() : "");
                 cookieProtoBuf.setMaxAge(cookie.getMaxAge());
                 requestProtoBuf.addCookies(cookieProtoBuf);
             }
         }
 
         request.removeHeader("Host");
         request.removeHeader("Cookie");
         for (Entry<String, String> entry : request.getHeaders()) {
             HettyProtoBuf.NameValue.Builder headerProtoBuf = HettyProtoBuf.NameValue.newBuilder();
             headerProtoBuf.setName(entry.getKey());
             headerProtoBuf.setValue(entry.getValue());
             requestProtoBuf.addHeaders(headerProtoBuf);
         }
 
         requestProtoBuf.setContent(ByteString.copyFrom(requestContent));
         queueWriter.writeToQueue(requestProtoBuf.build().toByteArray());
 
         // HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
         // HttpResponseStatus.OK);
         // response.setHeader(HttpHeaders.Names.CONTENT_TYPE,
         // "text/plain; charset=UTF-8");
         // response.setContent(ChannelBuffers.copiedBuffer(requestId,
         // CharsetUtil.UTF_8));
         // userChannel.write(response).addListener(new ChannelFutureListener() {
         // public void operationComplete(ChannelFuture future) throws Exception
         // {
         // future.getChannel().close();
         // }
         // });
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
         Object message = e.getMessage();
         boolean processMessage = false;
         if (message instanceof HttpChunk) {
             validateHttpChunk((HttpChunk) message);
 
             HttpChunk httpChunk = (HttpChunk) message;
             currentRequestContent.write(httpChunk.getContent().array());
             ChannelBuffer compositeBuffer = ChannelBuffers.wrappedBuffer(
                     currentRequest.getContent(), httpChunk.getContent());
             currentRequest.setContent(compositeBuffer);
             processMessage = httpChunk.isLast();
         } else if (message instanceof HttpRequest) {
             HttpRequest httpRequest = (HttpRequest) message;
             currentRequestContent.write(httpRequest.getContent().array());
             currentRequest = httpRequest;
             processMessage = !currentRequest.isChunked();
         }
         if (processMessage) {
             handleRequest(currentRequest, currentRequestContent.toByteArray(), e.getChannel());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
         ChannelFuture future = e.getFuture();
         if (future != null) {
             e.getFuture().cancel();
         }
         e.getChannel().close();
        if (LOGGER.isDebugEnabled()) {
            String msg = "Timeout [" + e.getState() + "]: " + e.getChannel();
            LOGGER.debug(msg);
        }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
         Throwable t = e.getCause();
         LOGGER.error(t.getMessage(), t);
         ChannelFuture future = e.getFuture();
         if (future != null) {
             e.getFuture().cancel();
         }
         e.getChannel().close();
     }
 }
