 /*  copyit-server
  *  Copyright (C) 2013  Toon Schoenmakers
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.mms_projects.copy_it.api.http;
 
 import io.netty.buffer.ByteBuf;
 import io.netty.buffer.Unpooled;
 import io.netty.channel.ChannelFutureListener;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.SimpleChannelInboundHandler;
 import io.netty.handler.codec.http.DefaultFullHttpResponse;
 import io.netty.handler.codec.http.FullHttpResponse;
 import io.netty.handler.codec.http.HttpContent;
 import io.netty.handler.codec.http.HttpHeaders;
 import io.netty.handler.codec.http.HttpMethod;
 import io.netty.handler.codec.http.HttpObject;
 import io.netty.handler.codec.http.HttpRequest;
 import io.netty.handler.codec.http.LastHttpContent;
 import io.netty.util.CharsetUtil;
 import net.mms_projects.copy_it.api.http.pages.TestPage;
 import net.mms_projects.copy_it.api.oauth.HeaderVerifier;
 import net.mms_projects.copy_it.api.oauth.exceptions.OAuthException;
 import net.mms_projects.copy_it.server.database.Database;
 import net.mms_projects.copy_it.server.database.DatabasePool;
 
 import java.util.HashMap;
 
 import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
 import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
 import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
 import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
 import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
 import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
 import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
 import static io.netty.handler.codec.http.HttpResponseStatus.OK;
 import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
 
 public class Handler extends SimpleChannelInboundHandler<HttpObject> {
     private static final class Pages {
         private static final HashMap<String, Page> oauth_pages = new HashMap<String, Page>();
         static {
             oauth_pages.put("/test", new TestPage());
         }
     }
     protected void messageReceived(final ChannelHandlerContext chx, final HttpObject o) throws Exception {
         System.err.println(o.getClass().getName());
         if (o instanceof HttpRequest) {
             final HttpRequest http = (HttpRequest) o;
             this.request = http;
             buf.setLength(0);
             try {
                 headerVerifier = new HeaderVerifier(http);
                 database = DatabasePool.getDBConnection();
                 headerVerifier.verifyConsumer(database);
                 headerVerifier.verifyOAuthToken(database);
                 headerVerifier.verifyOAuthNonce(database);
                 headerVerifier.checkSignature(false);
                 Page page = Pages.oauth_pages.get(headerVerifier.getUri().getPath());
                 if (page != null) {
                     final FullHttpResponse response = page.onGetRequest(request);
                     if (isKeepAlive(request)) {
                         response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                         response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                         chx.write(response);
                     } else
                         chx.write(response).addListener(ChannelFutureListener.CLOSE);
                 } else {
                     final FullHttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion()
                             ,NOT_FOUND, Unpooled.copiedBuffer("404", CharsetUtil.UTF_8));
                     chx.write(response).addListener(ChannelFutureListener.CLOSE);
                 }
             } catch (OAuthException e) {
                 final FullHttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion()
                         ,UNAUTHORIZED, Unpooled.copiedBuffer(e.toString(), CharsetUtil.UTF_8));
                 chx.write(response).addListener(ChannelFutureListener.CLOSE);
             } catch (Exception e) {
                 e.printStackTrace();
                 final FullHttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion()
                         ,INTERNAL_SERVER_ERROR);
                 chx.write(response).addListener(ChannelFutureListener.CLOSE);
             }
         } else if (o instanceof HttpContent && request != null && request.getMethod() == HttpMethod.POST) {
             final HttpContent httpContent = (HttpContent) o;
             final ByteBuf content = httpContent.content();
             System.err.println("POST Data: " + content.toString(CharsetUtil.UTF_8));
             if (o instanceof LastHttpContent) {
                 final FullHttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion()
                         ,o.getDecoderResult().isSuccess() ? OK : BAD_REQUEST
                         ,Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
                 response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
                 if (isKeepAlive(request)) {
                     response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                     response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                     chx.write(response);
                 } else
                     chx.write(response).addListener(ChannelFutureListener.CLOSE);
             }
         }
        if (o instanceof LastHttpContent && database != null)
            database.free();
     }
 
     private final StringBuilder buf = new StringBuilder();
     private Database database;
     private HeaderVerifier headerVerifier;
     private HttpRequest request;
 }
