 package uk.ac.cam.signups.controllers;
 
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import com.google.common.collect.ImmutableMap;
 import com.googlecode.htmleasy.RedirectException;
 import com.googlecode.htmleasy.ViewWith;
 
 
 
 
 
 //Import models
 import uk.ac.cam.signups.models.*;
 import uk.ac.cam.signups.util.HibernateUtil;
 
 import org.hibernate.Session;
 //Import the following for logging
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Path("/")
 public class HomePageController extends ApplicationController{
 	
 	// Create the logger
 	private static Logger log = LoggerFactory.getLogger(HomePageController.class);
 	
 	private User user;
 	
 	@GET @Path("/")
 	public void appRedirect() {
		throw new RedirectException("/app/#signapp/");
 	}
 	
 	// Index
	@GET @Path("/signapp/") 
 	@Produces(MediaType.APPLICATION_JSON)
 	public Map indexHomePage() {
 		
 		// Initialise user
 		user = initialiseUser();
 		
 		// Get user details
 		log.debug("Index GET: Getting user details");
 		ImmutableMap<String, ?> userMap = ulm.getAll();
 		
 		// Return data for template
 		return ImmutableMap.of("user", userMap);
 	}
 	
 	// DOS Index
 	@GET @Path("signapp/DoS") @ViewWith("/soy/home_page.dos")
 	public Map dosHomePage() {
 		
 		// Initialise user
 		initialiseUser();
 		
 		// Does user have staff level access?
 		if(!isStaff()){
 			throw new RedirectException("/");
 		}
 		
 		return ImmutableMap.of();
 	}
 	
 	// Admin Index
 	@GET @Path("signapp/admin") @ViewWith("/soy/home_page.admin")
 	public Map adminHomePage() {
 		return ImmutableMap.of();
 	}
 	
 	// Authenticate staff
 	public boolean isStaff() {
 		try {
 			return (ulm.getStatus().equals("staff"));
 		} catch(NullPointerException e) {
 			log.error("User initialisation failed");
 			return false;
 		}
 	}
 }
