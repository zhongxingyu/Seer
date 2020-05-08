 package com.bitgrind.protobuf.rpc.http;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.bitgrind.protobuf.rpc.MessagePb;
 import com.bitgrind.protobuf.rpc.MessagePb.Request;
 import com.bitgrind.protobuf.rpc.MessagePb.Response;
 import com.bitgrind.protobuf.rpc.MessagePb.Status;
 import com.google.protobuf.MessageLite;
 
 public class ProtobufServiceServlet extends HttpServlet {
   private static final long serialVersionUID = -8795840255637688901L;
   private final Logger logger = Logger.getLogger(getClass().getName());
 
   private static final Map<String, ProtobufService<? extends MessageLite>> services =
     new HashMap<String, ProtobufService<? extends MessageLite>>();
 
   public static void registerService(String requestName, ProtobufService<?> service) {
     services.put(requestName, service);
   }
 
   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response)
       throws ServletException {
     try {
       Request message = MessagePb.Request.parseFrom(request.getInputStream());
 
       String messageName = message.getName();
       ProtobufService<? extends MessageLite> service = services.get(messageName);
       if (service == null) {
         logger.log(Level.INFO, "No service registered for request: {0}", message.getName());
         Response.Builder error = Response.newBuilder();
         error.setStatus(Status.FAILURE);
         error.setStatusText(String.format("No service registered for request: %s", message.getName()));
         error.build().writeTo(response.getOutputStream());
         response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
         return;
       }
 
       MessageLite requestMessage = service.newBuilder().mergeFrom(message.getBuffer()).build();
 
       if (logger.isLoggable(Level.FINE)) {
         logger.log(Level.FINE, "Received request: {0}", requestMessage.toString());
       }
 
       MessageLite responseMessage = service.service(requestMessage);
 
       if (logger.isLoggable(Level.FINE)) {
         logger.log(Level.FINE, "Sending response: {0}", responseMessage.toString());
       }
 
       Response.newBuilder()
         .setStatus(Status.SUCCESS)
         .setBuffer(responseMessage.toByteString())
         .build().writeTo(response.getOutputStream());
     }
     catch (Exception ex) {
       try {
         Response.Builder error = Response.newBuilder();
         error.setStatus(Status.FAILURE);
         error.setStatusText(ex.toString());
         error.build().writeTo(response.getOutputStream());
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
       }
       catch (IOException e) {
         throw new ServletException(e);
       }
     }
   }
 }
