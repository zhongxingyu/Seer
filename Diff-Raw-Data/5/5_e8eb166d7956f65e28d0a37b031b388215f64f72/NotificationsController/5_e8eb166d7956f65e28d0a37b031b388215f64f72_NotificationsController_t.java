 package uk.ac.cam.dashboard.controllers;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.jboss.resteasy.annotations.Form;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.dashboard.exceptions.AuthException;
 import uk.ac.cam.dashboard.forms.CreateNotificationForm;
 import uk.ac.cam.dashboard.forms.GetNotificationForm;
 import uk.ac.cam.dashboard.models.Notification;
 import uk.ac.cam.dashboard.models.NotificationUser;
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.queries.NotificationQuery;
 import uk.ac.cam.dashboard.util.Mail;
 import uk.ac.cam.dashboard.util.Strings;
 
 import com.google.common.collect.ImmutableMap;
 
 @Path("/api/notifications")
 @Produces(MediaType.APPLICATION_JSON)
 public class NotificationsController extends ApplicationController {
 	
 		// Create the logger
 		private static Logger log = LoggerFactory.getLogger(NotificationsController.class);
 		
 		// Get current user from raven session
 		private User currentUser;
 		
 		// Get notifications
 		public Map<String, ?> getNotifications(GetNotificationForm notificationForm, boolean read) {
 			
 			try {
 				currentUser = validateUser();
 			} catch (AuthException e) {
 				log.error("Error validating user: " +e.getMessage());
 				return ImmutableMap.of("error", e.getMessage());
 			}
 			
 			ImmutableMap<String, List<String>> errors = notificationForm.validate();
 
 			if (errors.isEmpty()) {
 				log.debug("No errors in form, getting notifications");
 				return notificationForm.handle(currentUser, read);
 			} else {
 				log.debug("Errors in form, returning form data with error flags");
 				return ImmutableMap.of("formErrors", errors, "data", notificationForm.toMap());
 			}
 			
 		}
 		
 		// Unread notifications
 		@GET @Path("/")
 		public Map<String, ?> getUnreadNotifications(@Form GetNotificationForm notificationForm) {
 			
 			return getNotifications(notificationForm, false);		
 		}
 
 		// Read notifications
 		@GET @Path("/archive")
 		public Map<String, ?> getReadNotifications(@Form GetNotificationForm notificationForm) {
 
 			return getNotifications(notificationForm, true);	
 		}
 		
 		// Individual notification
 		@GET @Path("/{id}")
 		public Map<String, ?> getNotification(@PathParam("id") int id) {
 			
 			try {
 				currentUser = validateUser();
 			} catch (AuthException e) {
 				log.error("Error validating user: " +e.getMessage());
 				return ImmutableMap.of("error", e.getMessage());
 			}
 			
 			Notification notification = NotificationQuery.get(id);
 			
 			if (notification != null) {
 				log.debug("Notification with id " + id + " retrieved, returning JSON");
 				return notification.toMap();
 			} else {
 				log.debug("Notification with id " + id + " does not exist, returning error");
 				return ImmutableMap.of("error", Strings.NOTIFICATION_GET_ERROR_INDIVIDUAL + id);
 			}
 			
 		}
 		
 		// Create
 		@POST @Path("/")
 		public Map<String, ?> createNotification(@Form CreateNotificationForm notificationForm) {
 			
 			try {
 				validateGlobal();
 			} catch (Exception e) {
 				log.error("Error validating global: " +e.getMessage());
 				return ImmutableMap.of("error", e.getMessage());
 			}
 			
 			ImmutableMap<String, List<String>> errors = notificationForm.validate();
 
 			if (errors.isEmpty()) {
 				int id = notificationForm.handle();
 				log.debug("Notification created successfully, redirecting");
				return ImmutableMap.of("redirectTo", "notifications");
 			} else {
 				log.debug("Errors in form, returning form data with error flags");
 				return ImmutableMap.of("formErrors", errors, "data", notificationForm.toMap());
 			}
 			
 		}
 		
 		// Update
 		@PUT @Path("/{id}")
 		public ImmutableMap<String, String> markNotificationAsRead(@PathParam("id") int id, @QueryParam("read") Boolean read) {
 			
 			try {
 				currentUser = validateUser();
 			} catch (AuthException e) {
 				log.error("Error validating user: " +e.getMessage());
 				return ImmutableMap.of("error", e.getMessage());
 			}
 			
 			ImmutableMap<String, String> error;
 			if (read != null) {
 				if (read == true) {
 					log.error("Error marking notification as read");
 					error = ImmutableMap.of("formErrors", Strings.NOTIFICATION_UPDATE_ERROR + "read");
 				} else {
 					log.error("Error marking notification as read");
 					error = ImmutableMap.of("formErrors", Strings.NOTIFICATION_UPDATE_ERROR + "unread");
 				}
 			} else {
 				log.error("Errors in form, no value provide for read");
 				error = ImmutableMap.of("formErrors", Strings.NOTIFICATION_UPDATE_NO_READ_PARAM);
 				return error;
 			}
 			
 			NotificationUser.markAsReadUnread(currentUser, id, read);
 			log.debug("Marked notification "+id+" to " + read);			
			return ImmutableMap.of("redirectTo", "notifications");
 		}
 		
 }
