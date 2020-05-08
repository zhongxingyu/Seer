 package uk.ac.cam.dashboard.controllers;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 import org.hibernate.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.dashboard.models.Settings;
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.queries.NotificationQuery;
 import uk.ac.cam.dashboard.util.HibernateUtil;
 
 import com.google.common.collect.ImmutableMap;
 
 @Path("/api/account")
 @Produces(MediaType.APPLICATION_JSON)
 public class AccountController extends ApplicationController {
 
 	// Create the logger
 	private static Logger log = LoggerFactory.getLogger(NotificationsController.class);
 
 	// Get current user from raven session
 	private User currentUser;
 
 	@GET
 	@Path("/")
 	public Map<String, ?> getAccountSettings(@QueryParam("userId") String userId) {
 		
 		try {
 			currentUser = validateUser();
 		} catch (Exception e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		return ImmutableMap.of("user", currentUser.getSettings(), "sidebar", getSidebarLinkHierarchy(currentUser));
 
 	}
 
 	@PUT @Path("/")
 	public Map<String, ?> changeAccountSettings(@QueryParam("signups") Boolean signups,
 												@QueryParam("questions") Boolean questions,
 												@QueryParam("handins") Boolean handins,
 												@QueryParam("userId") String userId) {
 		
 		try {
 			currentUser = validateUser();
 		} catch (Exception e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		Session session = HibernateUtil.getTransactionSession();
 		Settings newUserSettings = currentUser.getSettings();
 		
 		try {
 			if (signups != null) newUserSettings.setSignupsOptIn(signups);
 			if (questions != null) newUserSettings.setQuestionsOptIn(questions);
 			if (handins != null) newUserSettings.setHandinsOptIn(handins);
 			
 			currentUser.setSettings(newUserSettings);
 			session.save(newUserSettings);
 			session.update(currentUser);
 		} catch (Exception e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		return ImmutableMap.of("redirectTo", "dashboard/account");
 	}
 		
 	private List<Object> getSidebarLinkHierarchy(User user) {
 		
 		Settings settings = user.getSettings();
 		
 		// Get notifications associated with sections
		int signupsNotifications = NotificationQuery.all().byUser(user).inSection("signups").totalRows();
		int questionsNotifications = NotificationQuery.all().byUser(user).inSection("questions").totalRows();
		int handinsNotifications = NotificationQuery.all().byUser(user).inSection("handins").totalRows();
 		
		int dashboardNotifications = NotificationQuery.all().byUser(user).inSection("dashboard").totalRows() + signupsNotifications 
 																											 + questionsNotifications 
 																											 + handinsNotifications;
 		
 		List<Object> sidebar = new LinkedList<Object>();
 		
 		// Dashboard
 		List<Object> dashboard = new LinkedList<Object>();
 		dashboard.add(ImmutableMap.of("name", "Home", "link", "/", "icon", "icon-globe", "iconType", 1, "notificationCount", 0));
 		dashboard.add(ImmutableMap.of("name", "Notifications", "link", "notifications", "icon", "icon-newspaper", "iconType", 1, "notificationCount", 0));
 		dashboard.add(ImmutableMap.of("name", "Deadlines", "link", "deadlines", "icon", "icon-ringbell", "iconType", 1, "notificationCount", 0));
 		dashboard.add(ImmutableMap.of("name", "Groups", "link", "groups", "icon", "icon-users", "iconType", 1, "notificationCount", 0));
 		dashboard.add(ImmutableMap.of("name", "Supervisor Homepage", "link", "supervisor", "icon", "icon-users", "iconType", 1, "notificationCount", 0));
 		sidebar.add(ImmutableMap.of("name", "Dashboard", "links", dashboard, "icon", "a", "iconType", 2, "notificationCount", dashboardNotifications));
 		
 		// Signups
 		if (settings.isSignupsOptIn()) {
 			List<Object> signups = new LinkedList<Object>();
 			signups.add(ImmutableMap.of("name", "Events", "link", "signapp/events", "icon", "?", "iconType", 2, "notificationCount", 0));
 			signups.add(ImmutableMap.of("name", "Create new event", "link", "signapp/events/new", "icon", "?", "iconType", 2, "notificationCount", 0));
 			sidebar.add(ImmutableMap.of("name", "Timetable/Signups", "links", signups, "icon", "P", "iconType", 2, "notificationCount", signupsNotifications));
 		}
 		
 		// Questions
 		if (settings.isQuestionsOptIn()) {
 			List<Object> questions = new LinkedList<Object>();
 			questions.add(ImmutableMap.of("name", "Browse questions", "link", "q/search", "icon", "icon-list", "iconType", 1, "notificationCount", 0));
 			questions.add(ImmutableMap.of("name", "Browse question sets", "link", "sets", "icon", "icon-file_open", "iconType", 1, "notificationCount", 0));
 			questions.add(ImmutableMap.of("name", "Browse own content", "link", "users/me", "icon", "icon-file_open", "iconType", 1, "notificationCount", 0));
 			questions.add(ImmutableMap.of("name", "Create question set", "link", "sets/add", "icon", "icon-plus", "iconType", 1, "notificationCount", 0));
 			questions.add(ImmutableMap.of("name", "Fairytale land", "link", "fairytale", "icon", "icon-ringbell", "iconType", 1, "notificationCount", 0));
 			sidebar.add(ImmutableMap.of("name", "Setting Work", "links", questions, "icon", "a", "iconType", 2, "notificationCount", questionsNotifications));
 		}
 		
 		// Handins
 		if (settings.isHandinsOptIn()) {
 			List<Object> handins = new LinkedList<Object>();
 			handins.add(ImmutableMap.of("name", "Create bin", "link", "bins/create", "icon", ",", "iconType", 2, "notificationCount", 0));
 			handins.add(ImmutableMap.of("name", "Upload answers", "link", "bins", "icon", ",", "iconType", 2, "notificationCount", 0));
 			handins.add(ImmutableMap.of("name", "Mark answers", "link", "marking", "icon", "C", "iconType", 2, "notificationCount", 0));
 			sidebar.add(ImmutableMap.of("name", "Marking Work", "links", handins, "icon", "F", "iconType", 2, "notificationCount", handinsNotifications));
 		}
 		
 		return sidebar;
 		
 	} 
 	
 }
