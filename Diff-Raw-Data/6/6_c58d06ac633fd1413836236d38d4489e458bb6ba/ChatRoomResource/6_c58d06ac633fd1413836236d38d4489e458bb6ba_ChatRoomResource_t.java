 package ch.windmobile.server.resource;
 
 import java.net.HttpURLConnection;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import ch.windmobile.server.datasourcemodel.DataSourceException;
 import ch.windmobile.server.datasourcemodel.xml.Error;
 import ch.windmobile.server.security.SecurityHelper;
 import ch.windmobile.server.security.WindMobileAuthenticationProvider;
 import ch.windmobile.server.socialmodel.ChatService;
 import ch.windmobile.server.socialmodel.ServiceLocator;
 import ch.windmobile.server.socialmodel.UserService;
 import ch.windmobile.server.socialmodel.xml.Messages;
 
 public class ChatRoomResource {
     private ServiceLocator serviceLocator;
 
     private String chatRoomId;
 
     @Context
     UriInfo uriInfo;
     @Context
     Request request;
     String stationId;
 
     public ChatRoomResource(UriInfo uriInfo, Request request, ServiceLocator serviceLocator, String chatRoomId) {
         this.uriInfo = uriInfo;
         this.request = request;
         this.serviceLocator = serviceLocator;
         this.chatRoomId = chatRoomId;
     }
 
     @POST
     @Path("postmessage")
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
     public void postMessage(String message) {
         try {
             if (SecurityHelper.hasRole(WindMobileAuthenticationProvider.roleUser) == false) {
                 Error error = new Error();
                 error.setCode(DataSourceException.Error.UNAUTHORIZED.getCode());
                 throw new WebApplicationException(Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(error).build());
             }
 
             ChatService chatService = serviceLocator.getService(ChatService.class);
             UserService userService = serviceLocator.getService(UserService.class);
             String email = SecurityContextHolder.getContext().getAuthentication().getName();
             String pseudo = userService.findByEmail(email).getPseudo();
             chatService.postMessage(chatRoomId, pseudo, message);
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
}
