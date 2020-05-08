 package ch.windmobile.server.resource;
 
 import java.net.HttpURLConnection;
 import java.security.MessageDigest;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.xml.bind.JAXBElement;
 
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import ch.windmobile.server.datasourcemodel.DataSourceException;
 import ch.windmobile.server.datasourcemodel.xml.Error;
 import ch.windmobile.server.security.SecurityHelper;
 import ch.windmobile.server.security.WindMobileAuthenticationProvider;
 import ch.windmobile.server.socialmodel.ChatService;
 import ch.windmobile.server.socialmodel.ServiceLocator;
 import ch.windmobile.server.socialmodel.UserService;
 import ch.windmobile.server.socialmodel.xml.Messages;
 import ch.windmobile.server.socialmodel.xml.ObjectFactory;
 import ch.windmobile.server.socialmodel.xml.User;
 
 public class ChatRoomResource {
     private ServiceLocator serviceLocator;
 
     private String chatRoomId;
 
     UriInfo uriInfo;
     Request request;
     HttpServletRequest servletRequest;
     String stationId;
 
     public ChatRoomResource(UriInfo uriInfo, Request request, HttpServletRequest servletRequest, ServiceLocator serviceLocator, String chatRoomId) {
         this.uriInfo = uriInfo;
         this.request = request;
         this.servletRequest = servletRequest;
         this.serviceLocator = serviceLocator;
         this.chatRoomId = chatRoomId;
     }
 
     @POST
     @Path("postmessage")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public void postMessage(String message) {
         try {
             String email;
             String pseudo;
             String emailHash;
 
             ChatService chatService = serviceLocator.getService(ChatService.class);
             if (SecurityHelper.hasRole(WindMobileAuthenticationProvider.roleUser) == false) {
                 if (chatService.allowAnonymousMessages(chatRoomId) == false) {
                     Error error = new Error();
                     error.setCode(DataSourceException.Error.UNAUTHORIZED.getCode());
                     error.setMessage("Chat room '" + chatRoomId + "' does not allow anonymous message");
                     throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(error).build());
                 }
 
                 String ip = servletRequest.getRemoteAddr();
                 email = "anonymous@" + ip;
                 pseudo = "@" + ip;
             } else {
                 email = SecurityContextHolder.getContext().getAuthentication().getName();
                 UserService userService = serviceLocator.getService(UserService.class);
                 User user = userService.findByEmail(email);
                 pseudo = user.getPseudo();
             }
 
             MessageDigest md = MessageDigest.getInstance("MD5");
             byte[] emailHashBytes = md.digest(email.trim().toLowerCase().getBytes());
             StringBuffer hexBuffer = new StringBuffer();
             for (int i = 0; i < emailHashBytes.length; i++) {
                String hex = Integer.toHexString(0xFF & emailHashBytes[i]);
                if (hex.length() == 1) {
                    hexBuffer.append("0" + hex);
                } else {
                    hexBuffer.append(hex);
                }
             }
             emailHash = hexBuffer.toString();
 
             chatService.postMessage(chatRoomId, pseudo, message, emailHash);
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
         }
     }
 
     @GET
     @Path("lastmessages/{maxCount}")
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON + ";charset=UTF-8" })
     public Messages getMessages(@PathParam("maxCount") int maxCount) {
         try {
             ChatService chatService = serviceLocator.getService(ChatService.class);
             return chatService.findMessages(chatRoomId, maxCount);
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         }
     }
 
     @GET
     @Path("lastmessage")
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public JAXBElement<Long> getLastMessageId() {
         try {
             ChatService chatService = serviceLocator.getService(ChatService.class);
             return new ObjectFactory().createMessageId(chatService.getLastMessageId(chatRoomId));
         } catch (Exception e) {
             ExceptionHandler.treatException(e);
             return null;
         }
     }
 }
