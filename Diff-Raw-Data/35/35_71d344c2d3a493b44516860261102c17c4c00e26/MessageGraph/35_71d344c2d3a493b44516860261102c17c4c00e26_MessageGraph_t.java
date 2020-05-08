 package de.anycook.api;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.container.AsyncResponse;
 import javax.ws.rs.container.Suspended;
 import javax.ws.rs.container.TimeoutHandler;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import de.anycook.db.mysql.DBMessage;
 import de.anycook.messages.MessageSession;
 import de.anycook.user.User;
 import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ManagedAsync;
 import org.json.simple.JSONArray;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import de.anycook.messages.Message;
 import de.anycook.session.Session;
 
 @Path("/message")
 public class MessageGraph  {
 	
 	private final Logger logger;
     @Context private HttpServletRequest req;
     @Context private HttpHeaders hh;
 	
 	/**
 	 * 
 	 */
 	public MessageGraph() {
 		logger = Logger.getLogger(getClass());
 	}
 
     @GET
    @ManagedAsync
     @Produces(MediaType.APPLICATION_JSON)
     public void get(@Suspended final AsyncResponse asyncResponse,
                     @QueryParam("lastChange") Long lastChange){
         asyncResponse.setTimeoutHandler(new TimeoutHandler() {
             @Override
             public void handleTimeout(AsyncResponse asyncResponse) {
                 asyncResponse.resume(Response.ok().build());
             }
         });
 
         asyncResponse.setTimeout(5, TimeUnit.MINUTES);
 
 
         User user = Session.init(req.getSession()).getUser();
 
         if(lastChange == null){
             try {
                 asyncResponse.resume(MessageSession.getSessionsFromUser(user.getId()));
             } catch (SQLException e) {
                 logger.error(e,e);
                 asyncResponse.resume(new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR));
             }
             return;
         }
 
         Date changeDate = new Date(lastChange);
         try{
             while(!asyncResponse.isCancelled() && !asyncResponse.isDone()){
                 List<MessageSession> sessions = MessageSession.getSessionsFromUser(user.getId(), changeDate);
                 if(!sessions.isEmpty() && asyncResponse.isSuspended()) asyncResponse.resume(sessions);
                 else Thread.sleep(1000);
             }
         } catch (SQLException | InterruptedException e){
             logger.error(e,e);
             asyncResponse.resume(new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR));
         }
 
     }
 	
 	@PUT
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public void newMessage(
 			@FormParam("message") String message,
 			@FormParam("recipients") String recipientsString,
 			@Context HttpHeaders hh,
 			@Context HttpServletRequest request
 			){
 
 		
 		if(message == null){
 			logger.info("message was null");
 			throw new WebApplicationException(400);
 		}
 		
 		if(recipientsString == null){
 			logger.info("recipients was null");
 			throw new WebApplicationException(400);
 		}
 		
 		try {
 			message = URLDecoder.decode(message, "UTF-8");
 			JSONParser parser = new JSONParser();
 			JSONArray recipientsJSON = (JSONArray)parser.parse(recipientsString);
 			Session session = Session.init(request.getSession());
 			session.checkLogin(hh.getCookies());
 			List<Integer> recipients = new LinkedList<>();
 			for(Object recipientString : recipientsJSON)
 				recipients.add(Integer.parseInt(recipientString.toString()));
 			int userid = session.getUser().getId();
 			recipients.add(userid);
 			MessageSession.getSession(recipients).newMessage(userid, message);
 		} catch (ParseException | UnsupportedEncodingException | SQLException e ) {
 			logger.error(e);
 			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
 		}
 //		return CorsFilter.buildResponse(origin);
 	}
 
     @GET
     @Path("number")
     @Produces(MediaType.APPLICATION_JSON)
     public void getMessageNumber(@Suspended AsyncResponse asyncResponse,
                                  @QueryParam("lastNum") int lastNumber){
         Session session = Session.init(req.getSession());
         session.checkLogin(hh.getCookies());
 
         int userId = session.getUser().getId();
 
         try(DBMessage dbmessage = new DBMessage()){
             int newNumber = -1;
             while(!asyncResponse.isCancelled() && !asyncResponse.isDone()){
                 int newMessageNum = dbmessage.getNewMessageNum(userId);
                 if(newMessageNum == lastNumber){
                     Thread.sleep(2500);
                     continue;
                 }
 
                 if(asyncResponse.isSuspended())
                     asyncResponse.resume(newMessageNum);
             }
         } catch (InterruptedException | SQLException e) {
             logger.error(e, e);
             asyncResponse.resume(new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR));
         }
 
     }

    @GET
    @Path("{sessionId}")
    @ManagedAsync
    @Produces(MediaType.APPLICATION_JSON)
    public void getMessagesFromSession(@Suspended AsyncResponse asyncResponse,
                                       @PathParam("sessionId") int sessionId,
                                       @QueryParam("lastId") Integer lastId){
        Session session = Session.init(req.getSession());
        int userId = session.getUser().getId();

        try {
            if(lastId == null) {
                asyncResponse.resume(MessageSession.getSession(sessionId, userId));
                return;
            }

            while (!asyncResponse.isDone() && !asyncResponse.isCancelled()){
                    MessageSession messageSession = MessageSession.getSession(sessionId, userId, lastId);
                    if(!messageSession.isEmpty() && asyncResponse.isSuspended())
                        asyncResponse.resume(messageSession);
                    else
                        Thread.sleep(1000);
            }
        } catch (InterruptedException | SQLException e) {
            logger.error(e, e);
            asyncResponse.resume(new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR));
        }


    }
 	
 	@PUT
 	@Path("{sessionId}")
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public void answerSession(@PathParam("sessionId") int sessionid,
 			@FormParam("message") String message){
 		if(message == null){
 			logger.info("message was null");
 			throw new WebApplicationException(400);
 		}
 		
 		Session session = Session.init(req.getSession());
 		session.checkLogin(hh.getCookies());
 		int userid = session.getUser().getId();
         try {
             MessageSession.getSession(sessionid, userid).newMessage(userid, message);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	@PUT
 	@Path("{sessionId}/{messageid}")
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public void readMessage(@PathParam("sessionId") int sessionid,
 			@PathParam("messageid") int messageid){
 		Session session = Session.init(req.getSession());
 		session.checkLogin(hh.getCookies());
 		int userid = session.getUser().getId();
         try {
             Message.read(sessionid, messageid, userid);
         } catch (SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 
 }
