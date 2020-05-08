 package org.nohope.protobuf.rpc.server;
 
 import com.google.protobuf.BlockingService;
 import com.google.protobuf.ByteString;
 import com.google.protobuf.Descriptors.MethodDescriptor;
 import com.google.protobuf.ExtensionRegistry;
 import com.google.protobuf.InvalidProtocolBufferException;
 import com.google.protobuf.Message;
 import com.google.protobuf.RpcController;
 import com.google.protobuf.ServiceException;
 import com.google.protobuf.UninitializedMessageException;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandler;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.nohope.logging.Logger;
 import org.nohope.logging.LoggerFactory;
 import org.nohope.protobuf.core.Controller;
 import org.nohope.protobuf.core.IBlockingServiceRegistry;
 import org.nohope.protobuf.core.MessageUtils;
 import org.nohope.protobuf.core.exception.ExpectedServiceException;
 import org.nohope.protobuf.core.exception.InvalidRpcRequestException;
 import org.nohope.protobuf.core.exception.NoSuchServiceException;
 import org.nohope.protobuf.core.exception.NoSuchServiceMethodException;
 import org.nohope.protobuf.core.exception.RpcException;
 import org.nohope.protobuf.core.exception.RpcServiceException;
 import org.nohope.protobuf.core.exception.ServerSideException;
 import org.nohope.rpc.protocol.RPC;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 8/21/13 3:42 PM
  */
 @ChannelHandler.Sharable
 class RpcServerHandler extends SimpleChannelUpstreamHandler implements IBlockingServiceRegistry {
     private static final Logger LOG = LoggerFactory.getLogger(RpcServerHandler.class);
     private final Map<String, Map.Entry<BlockingService, ExtensionRegistry>> blockingServiceMap =
             new ConcurrentHashMap<>();
 
     @Override
     public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
         final RPC.RpcRequest request = (RPC.RpcRequest) e.getMessage();
         final String serviceName = request.getServiceName();
         final String methodName = request.getMethodName();
 
         LOG.debug("Received request for serviceName: {}, method: {}", serviceName, methodName);
 
         final Map.Entry<BlockingService, ExtensionRegistry> pair = blockingServiceMap.get(serviceName);
         if (pair == null) {
             throw new NoSuchServiceException(request, serviceName);
         }
         final BlockingService blockingService = pair.getKey();
         final ExtensionRegistry registry = pair.getValue();
         if (blockingService.getDescriptorForType().findMethodByName(methodName) == null) {
             throw new NoSuchServiceMethodException(request, methodName);
         }
 
         final MethodDescriptor methodDescriptor = blockingService.getDescriptorForType().findMethodByName(methodName);
         final Message methodRequest;
         try {
             methodRequest = buildMessageFromPrototype(
                     blockingService.getRequestPrototype(methodDescriptor),
                     registry,
                     request.getPayload());
         } catch (InvalidProtocolBufferException | UninitializedMessageException ex) {
             throw new InvalidRpcRequestException(ex, request,
                     String.format("Could not build method request message for %s.%s", serviceName, methodName));
         }
         final RpcController controller = new Controller();
 
         final Message methodResponse;
         try {
             methodResponse = blockingService.callBlockingMethod(methodDescriptor, controller, methodRequest);
         } catch (final ExpectedServiceException ex) {
             throw RpcServiceException.wrapExpectedException(ex, request);
         } catch (final ServiceException ex) {
             throw new RpcServiceException(ex, request,
                     String.format("%s.%s RPC call threw ServiceException", serviceName, methodName));
         } catch (final Exception ex) {
             throw new RpcException(ex, request,
                     String.format("%s.%s RPC call threw unexpected exception", serviceName, methodName));
         }
         if (controller.failed()) {
             throw new RpcException(request,
                     String.format("%s.%s RPC failed: %s", serviceName, methodName, controller.errorText()));
         } else if (methodResponse == null) {
             throw new RpcException(request,
                     String.format("%s.%s RPC returned null response", serviceName, methodName));
         }
         final RPC.RpcResponse response =
                 RPC.RpcResponse.newBuilder()
                    .setId(request.getId())
                    .setPayload(methodResponse.toByteString())
                    .build();
 
         writeResponse(e.getChannel(), response);
     }
 
     private static void writeResponse(final Channel c, final RPC.RpcResponse response) {
         c.write(response).addListener(new WriteListener(response));
     }
 
     @Override
     public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) {
         final Throwable cause = e.getCause();
         final RPC.RpcResponse.Builder responseBuilder = RPC.RpcResponse.newBuilder();
 
         /* Cannot respond to this exception, because it is not tied to a request */
         if (!(cause instanceof ServerSideException)) {
             LOG.error("Cannot respond to handler exception", cause);
             return;
         }
 
        if (cause instanceof RpcException) {
            LOG.error("Server-side exception caught", cause);
        }

         final ServerSideException ex = (ServerSideException) cause;
         if (ex.getRequest().hasId()) {
             responseBuilder.setId(ex.getRequest().getId());
             responseBuilder.setError(ex.getError());
             writeResponse(e.getChannel(), responseBuilder.build());
         } else {
             LOG.warn("Cannot respond to handler exception", ex);
         }
     }
 
     private static Message buildMessageFromPrototype(final Message prototype,
                                                      final ExtensionRegistry registry,
                                                      final ByteString messageToBuild)
             throws InvalidProtocolBufferException {
         return prototype.newBuilderForType().mergeFrom(messageToBuild, registry).build();
     }
 
     @Override
     public void registerService(final BlockingService service) {
         if (blockingServiceMap.containsKey(service.getDescriptorForType().getFullName())) {
             throw new IllegalArgumentException("BlockingService already registered");
         }
 
         final ExtensionRegistry extensionRegistry =
                 MessageUtils.getExtensionRegistry(service.getDescriptorForType().getFile());
         blockingServiceMap.put(service.getDescriptorForType().getFullName(),
                 new Pair<>(service, extensionRegistry));
     }
 
     @Override
     public void unregisterService(final BlockingService service) {
         if (!blockingServiceMap.containsKey(service.getDescriptorForType().getFullName())) {
             throw new IllegalArgumentException("BlockingService not already registered");
         }
         blockingServiceMap.remove(service.getDescriptorForType().getFullName());
     }
 
     private static class WriteListener implements ChannelFutureListener {
         private final RPC.RpcResponse response;
 
         private WriteListener(final RPC.RpcResponse response) {
             this.response = response;
         }
 
         @Override
         public void operationComplete(final ChannelFuture future) throws Exception {
             if (future.isSuccess()) {
                 LOG.debug("Message '{}' successfully sent", response);
             } else {
                 final Throwable cause = future.getCause();
                 if (cause != null) {
                     LOG.warn("Unable to send message '{}' ({})", response, cause);
                 } else {
                     LOG.error("ChannelFuture for writing message '{}' complete "
                               + "unsuccessfully with unknown throwable (cancelled={})",
                             response, future.isCancelled());
                 }
             }
         }
     }
 
     private static final class Pair<K, V> implements Map.Entry<K, V> {
         private final K key;
         private final V value;
 
         private Pair(final K key, final V value) {
             this.key = key;
             this.value = value;
         }
 
         @Override
         public K getKey() {
             return key;
         }
 
         @Override
         public V getValue() {
             return value;
         }
 
         @Override
         public V setValue(final V value) {
             throw new UnsupportedOperationException();
         }
     }
 }
