 package com.barchart.http.server;
 
 import io.netty.channel.ChannelHandler.Sharable;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundMessageHandlerAdapter;
 import io.netty.handler.codec.http.HttpRequest;
 import io.netty.handler.codec.http.HttpResponseStatus;
 import io.netty.util.AttributeKey;
 
 import java.io.IOException;
 
 import com.barchart.http.request.RequestHandler;
 import com.barchart.http.request.RequestHandlerMapping;
 
 /**
  * Netty channel handler for routing inbound requests to the proper
  * RequestHandler.
  */
 @Sharable
 public class HttpRequestChannelHandler extends
 		ChannelInboundMessageHandlerAdapter<HttpRequest> {
 
 	private static final AttributeKey<PooledServerResponse> ATTR_RESPONSE =
 			new AttributeKey<PooledServerResponse>("response");
 
 	private final HttpServerConfig config;
 
 	public HttpRequestChannelHandler(final HttpServerConfig config_) {
 		super(HttpRequest.class);
 		config = config_;
 	}
 
 	@Override
 	public void messageReceived(final ChannelHandlerContext ctx,
 			final HttpRequest msg) throws Exception {
 
 		// Create request handler
 		final RequestHandlerMapping mapping =
 				config.getRequestMapping(msg.getUri());
 
 		if (mapping == null) {
 			handleError(ctx, msg, HttpResponseStatus.NOT_FOUND, null);
 			return;
 		}
 
 		final String relativePath =
 				msg.getUri().substring(mapping.path().length());
 
 		// Create request/response
 		final PooledServerRequest request = new PooledServerRequest();
 		request.init(msg, relativePath);
 
 		final PooledServerResponse response = new PooledServerResponse();
 		response.init(ctx, mapping.handler(), request);
 
 		// Store in ChannelHandlerContext for future reference
 		ctx.attr(ATTR_RESPONSE).set(response);
 
 		try {
 
 			// Process request
 			mapping.handler().onRequest(request, response);
 
 		} catch (final Exception e) {
 
 			// Catch server errors
 			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
 
 			try {
 				config.errorHandler().onError(request, response, e);
 			} catch (final Exception e2) {
 				response.write(e.getClass()
 						+ " was thrown while processing this request.  Additionally, "
 						+ e2.getClass()
 						+ " was thrown while handling this exception.");
 			}
 
 		} finally {
 
 			// If handler did not request async response, finish request
 			if (!response.isFinished() && !response.isSuspended()) {
 				response.finish();
 			}
 
 		}
 
 	}
 
 	private void handleError(final ChannelHandlerContext ctx,
 			final HttpRequest msg, final HttpResponseStatus status,
 			final Exception exception) throws IOException {
 
 		// Create request/response
 		final PooledServerRequest request = new PooledServerRequest();
 		request.init(msg, msg.getUri());
 
 		final PooledServerResponse response = new PooledServerResponse();
 		response.init(ctx, null, request);
 		response.setStatus(status);
 
 		// Store in ChannelHandlerContext for future reference
 		ctx.attr(ATTR_RESPONSE).set(response);
 
 		try {
 
 			// Process request
 			config.errorHandler().onError(request, response, exception);
 
 		} catch (final Exception e) {
 
 			response.write("The requested URL was not found.  Additionally, "
 					+ e.getClass() + " was thrown while handling this error.");
 
 		} finally {
 
 			// If handler did not request async response, finish request
 			if (!response.isFinished() && !response.isSuspended()) {
 				response.finish();
 			}
 
 		}
 
 	}
 
 	@Override
 	public void channelInactive(final ChannelHandlerContext ctx) {
 
 		final PooledServerResponse response = ctx.attr(ATTR_RESPONSE).get();
 
 		if (response != null && !response.isFinished()) {
 
 			final RequestHandler handler = response.handler();
 
 			if (handler != null) {
 				handler.onAbort(response.request(), response);
 			}
 
 			// response.close() calls handler.onComplete() also
 			response.close();
 
 		}
 
 	}
 
 	@Override
 	public void exceptionCaught(final ChannelHandlerContext ctx,
 			final Throwable exception) {
 
 		final PooledServerResponse response = ctx.attr(ATTR_RESPONSE).get();
 
 		if (response != null && !response.isFinished()) {
 
			response.close();

 			final RequestHandler handler = response.handler();
 
 			if (handler != null) {
 				handler.onException(response.request(), response, exception);
 			}
 
 		}
 
 	}
 
 }
