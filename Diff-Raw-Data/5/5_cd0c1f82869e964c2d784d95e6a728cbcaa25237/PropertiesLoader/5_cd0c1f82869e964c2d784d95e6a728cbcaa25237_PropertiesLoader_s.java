 package com.philihp.boatswag.facebook;
 
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.ResourceBundle;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 /**
  * Application Lifecycle Listener implementation class PropertiesLoader
  * 
  */
 public class PropertiesLoader implements ServletContextListener {
 
 	/**
 	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
 	 */
 	public void contextInitialized(ServletContextEvent sce) {
 
 		ResourceBundle bundle = ResourceBundle.getBundle("facebook");
 		for (String key : Collections.list(bundle.getKeys())) {
 			String value = bundle.getString(key);
 			
 			//sets in case maven filtering didn't happen (because we're running in eclipse)
 			//this is a hack because m2eclipse doesn't do filtering in wtp
 			if (value.equals("${facebook.id}"))
 				value = System.getenv("facebook.id");
			if (value.equals("${facebook.secret}"))
 				value = System.getenv("facebook.secret");
			if (value.equals("${facebook.redirect}"))
 				value = System.getenv("facebook.redirect");
 			
 			sce.getServletContext().setAttribute(key, value);
 		}
 	}
 
 	/**
 	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
 	 */
 	public void contextDestroyed(ServletContextEvent sce) {
 		// meh
 	}
 
 }
