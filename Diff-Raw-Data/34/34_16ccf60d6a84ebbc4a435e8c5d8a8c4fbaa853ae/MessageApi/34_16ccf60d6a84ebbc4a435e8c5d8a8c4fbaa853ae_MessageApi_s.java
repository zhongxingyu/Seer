 /*
  * This file is part of anycook. The new internet cookbook
  * Copyright (C) 2014 Jan Graßegger
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see [http://www.gnu.org/licenses/].
  */
 
 package de.anycook.api;
 
 import de.anycook.api.util.MediaType;
 import de.anycook.db.mysql.DBMessage;
 import de.anycook.messages.Message;
import de.anycook.messages.MessageProvider;
 import de.anycook.messages.MessageSession;
 import de.anycook.session.Session;
 import de.anycook.user.User;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.glassfish.jersey.server.ManagedAsync;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.container.AsyncResponse;
 import javax.ws.rs.container.Suspended;
 import javax.ws.rs.container.TimeoutHandler;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.Response;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 @Path("/message")
 public class MessageApi {
 	
 	private final Logger logger;
     @Context private HttpServletRequest req;
     @Context private HttpHeaders hh;
 	
 	/**
 	 * 
 	 */
 	public MessageApi() {
 		logger = Logger.getLogger(getClass());
 	}
 
     @GET
     @ManagedAsync
     @Produces(MediaType.APPLICATION_JSON)
     public void get(@Suspended final AsyncResponse asyncResponse, @QueryParam("lastChange") Long lastChange){
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
                 logger.error(e, e);
                 asyncResponse.resume(new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR));
             }
             return;
         }
 
         Date changeDate = new Date(lastChange);
         try{
             List<MessageSession> sessions = MessageSession.getSessionsFromUser(user.getId(), changeDate);
             if(!sessions.isEmpty()) asyncResponse.resume(sessions);
            else MessageProvider.INSTANCE.suspend(user.getId(), asyncResponse);
         } catch (SQLException | DBMessage.SessionNotFoundException e){
             logger.error(e,e);
             asyncResponse.resume(new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR));
         }
 
     }
 	
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	public void newMessage(NewMessage message, @Context HttpHeaders hh, @Context HttpServletRequest request){
         if(message == null)
             throw new WebApplicationException(Response.Status.BAD_REQUEST);
 
         logger.debug(message);
 
 		try {
 			Session session = Session.init(request.getSession());
 			session.checkLogin(hh.getCookies());
 			int userId = session.getUser().getId();
             message.recipients.add(userId);
 			MessageSession.getSession(message.recipients).newMessage(userId, message.text);
 		} catch (IOException | SQLException | DBMessage.SessionNotFoundException e ) {
 			logger.error(e, e);
 			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
 		}
 	}
 
     @GET
     @Path("number")
     @Produces(MediaType.APPLICATION_JSON)
     public void getMessageNumber(@Suspended AsyncResponse asyncResponse, @QueryParam("lastNum") int lastNumber){
         Session session = Session.init(req.getSession());

        try(DBMessage dbmessage = new DBMessage()){
             session.checkLogin(hh.getCookies());
             int userId = session.getUser().getId();
 
            while(!asyncResponse.isCancelled() && !asyncResponse.isDone()){
                int newMessageNum = dbmessage.getNewMessageNum(userId);
                if(newMessageNum == lastNumber){
                    Thread.sleep(2500);
                    continue;
                }

                if(asyncResponse.isSuspended())
                    asyncResponse.resume(newMessageNum);
             }
        } catch (IOException | InterruptedException | SQLException e) {
             logger.error(e, e);
             asyncResponse.resume(new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR));
         }
 
     }
 
     @GET
     @Path("{sessionId}")
     @ManagedAsync
     @Produces(MediaType.APPLICATION_JSON)
     public void getMessagesFromSession(@Suspended AsyncResponse asyncResponse, @PathParam("sessionId") int sessionId,
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
         } catch (InterruptedException | SQLException | DBMessage.SessionNotFoundException e) {
             logger.error(e, e);
             asyncResponse.resume(new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR));
         }
 
 
     }
 	
 	@POST
 	@Path("{sessionId}")
 	@Consumes(MediaType.APPLICATION_JSON)
 	public void answerSession(@PathParam("sessionId") int sessionId, AnswerMessage message){
 		if(message == null){
 			logger.info("text was null");
 			throw new WebApplicationException(400);
 		}
 		
 		Session session = Session.init(req.getSession());
 
         try {
             session.checkLogin(hh.getCookies());
             int userId = session.getUser().getId();
             MessageSession.getSession(sessionId, userId).newMessage(userId, message.text);
         } catch (IOException | SQLException | DBMessage.SessionNotFoundException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 	
 	@PUT
 	@Path("{sessionId}/{messageId}")
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public void readMessage(@PathParam("sessionId") int sessionId, @PathParam("messageId") int messageId){
 		Session session = Session.init(req.getSession());
 
         try {
             session.checkLogin(hh.getCookies());
             int userId = session.getUser().getId();
             Message.read(sessionId, messageId, userId);
         } catch (IOException | SQLException e) {
             logger.error(e);
             throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
         }
     }
 
     public static class NewMessage {
         public List<Integer> recipients;
         public String text;
 
         @Override
         public String toString() {
             return String.format("{recipients : %s, text : %s}", StringUtils.join(recipients, ","), text);
         }
     }
 
     public static class AnswerMessage {
         public String text;
     }
 
 }
