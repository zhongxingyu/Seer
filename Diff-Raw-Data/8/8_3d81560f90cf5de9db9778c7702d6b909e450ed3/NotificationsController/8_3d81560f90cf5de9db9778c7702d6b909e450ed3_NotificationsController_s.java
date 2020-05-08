 package uk.ac.cam.dashboard.controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.jboss.resteasy.annotations.Form;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.dashboard.forms.GetNotificationForm;
 import uk.ac.cam.dashboard.forms.CreateNotificationForm;
 import uk.ac.cam.dashboard.models.NotificationUser;
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.queries.NotificationQuery;
 
 import com.google.common.collect.ImmutableMap;
 import com.googlecode.htmleasy.RedirectException;
 
 @Path("api/dashboard/notifications")
 public class NotificationsController extends ApplicationController {
 	
 		// Create the logger
 		private static Logger log = LoggerFactory.getLogger(NotificationsController.class);
 		
 		// Get current user from raven session
 		private User currentUser;
 		
 		// Index
 		@GET @Path("/")
 		@Produces(MediaType.APPLICATION_JSON)
		public Map<String, ?> getNotifications(@Form GetNotificationForm notificationForm) throws RedirectException {
 			
 			currentUser = initialiseUser();
 			ImmutableMap<String, List<String>> errors = notificationForm.validate();
 
 			if (errors.isEmpty()) {
 				return notificationForm.handle(currentUser);
 			} else {
 				return ImmutableMap.of("errors", errors);
 			}
 			
 		}
 		
 		// Create
 		@POST @Path("/")
		public Map<String, ?> createNotification(@Form CreateNotificationForm notificationForm) throws RedirectException {
 			
 			ImmutableMap<String, List<String>> errors = notificationForm.validate();
 
 			if (errors.isEmpty()) {
 				notificationForm.handle();
 				return ImmutableMap.of("redirectTo", "dashboard/notifications");
 			} else {
 				return ImmutableMap.of("errors", errors);
 			}
 			
 		}
 		
 		// Update
 		@PUT @Path("/{id}")
 		public void markNotificationAsRead(@PathParam("id") int id) {
 	
 			currentUser = initialiseUser();
 			
 			NotificationUser.markAsRead(currentUser, id);
 			
 			log.debug("Redirecting to notifications page");
 			throw new RedirectException("/app#dashboard/notifications");
 		}
 		
 }
