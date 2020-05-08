 package uk.ac.cam.dashboard.controllers;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.dashboard.exceptions.AuthException;
 //Import models
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.util.Strings;
 
 import com.google.common.collect.ImmutableMap;
 
 @Path("/api/")
 @Produces(MediaType.APPLICATION_JSON)
 public class HomePageController extends ApplicationController{
 	
 	@Context
 	private HttpServletRequest request;
 	
 	// Logger
 	@SuppressWarnings("unused")
 	private static Logger log = LoggerFactory.getLogger(HomePageController.class);
 	
 	private User currentUser;
 	
 	@GET @Path("/")
 	public Map<String, ?> homePage() {
 		
 		try {
 			currentUser = validateUser();
 		} catch(AuthException e){
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		Map<String, Object> userData = currentUser.getUserDetails();
 		
 
 		
		return ImmutableMap.of("user", userData, "supervisor", currentUser.getSettings().getSupervisor(), "services", currentUser.getSettings().toMap(), "dos", currentUser.getSettings().isDos());
 	}
 	
 	@GET @Path("/help")
 	public ImmutableMap<String, ?> aboutHelpPage() {
 		Map<String,?> about = ImmutableMap.of("info", Strings.ABOUT_INFO, "moreinfo", Strings.ABOUT_MOREINFO, "dtglink", Strings.ABOUT_DTGLINK, "gitinfo", Strings.ABOUT_GIT, "links", Strings.ABOUT_GITLINKS);
 		Map<String,?> help = ImmutableMap.of("info", Strings.HELP_INFO, "email", Strings.HELP_EMAIL);
 		return ImmutableMap.of("about", about, "help", help, "faq", new ArrayList<String>());
 	}
 
 }
