 package com.bitgrind.protobuf.rpc.http;
 
 import java.io.IOException;
 
 import org.apache.commons.httpclient.HostConfiguration;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.PostMethod;
 
 import com.bitgrind.protobuf.rpc.MessagePb.Request;
 import com.bitgrind.protobuf.rpc.MessagePb.Response;
import com.bitgrind.protobuf.rpc.ResponseHandler;
import com.bitgrind.protobuf.rpc.ResponseHandler.Result;
 import com.google.protobuf.MessageLite;
 import com.google.protobuf.MessageLite.Builder;
 
 public class ProtobufServiceClient {
 
   private HttpClient client;
   private final String path;
 
   public ProtobufServiceClient(URI endpoint, String path) {
     this.path = path;
     this.client = new HttpClient();
     HostConfiguration hostCfg = new HostConfiguration();
     hostCfg.setHost(endpoint);
     client.setHostConfiguration(hostCfg);
   }
 
   @SuppressWarnings("unchecked")
   public <BuilderType extends Builder, ResponseType extends MessageLite> void send(String name,
       MessageLite message, BuilderType builder, ResponseHandler<ResponseType> handler) {
     Request request = Request.newBuilder().setName(name).setBuffer(message.toByteString()).build();
 
     PostMethod post = new PostMethod(path);
     post.setRequestEntity(new InputStreamRequestEntity(request.toByteString().newInput()));
 
     try {
       client.executeMethod(post);
       Response response;
       switch (post.getStatusCode()) {
         case 200:
           response = Response.parseFrom(post.getResponseBodyAsStream());
           switch (response.getStatus()) {
             case FAILURE:
               handler.onResult(new ResponseHandler.Result<ResponseType>(post.getStatusCode(), post
                 .getStatusText(), response));
               break;
             case SUCCESS:
               builder.mergeFrom(response.getBuffer());
               handler.onResult(new ResponseHandler.Result<ResponseType>(post.getStatusCode(), post
                 .getStatusText(), (ResponseType) builder.build()));
               break;
           }
           break;
         default:
           Result<ResponseType> result = null;
           if (post.getResponseContentLength() > 0) {
             try {
               response = Response.parseFrom(post.getResponseBodyAsStream());
               result = new ResponseHandler.Result<ResponseType>(post.getStatusCode(), post.getStatusText(), response);
             } catch (Exception ex) {
             }
           }
           if (result == null) {
             result = new ResponseHandler.Result<ResponseType>(post.getStatusCode(), post.getStatusText());
           }
           handler.onResult(result);
           break;
       }
     }
     catch (HttpException e) {
       handler.onResult(new ResponseHandler.Result<ResponseType>(e));
     }
     catch (IOException e) {
       handler.onResult(new ResponseHandler.Result<ResponseType>(e));
     }
   }
 }
