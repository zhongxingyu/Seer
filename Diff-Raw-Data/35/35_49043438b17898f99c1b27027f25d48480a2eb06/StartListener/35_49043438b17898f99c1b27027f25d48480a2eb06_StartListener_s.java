 package de.anycook.api.listener;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.annotation.WebListener;
 
 import de.anycook.api.DiscussionGraph;
 import de.anycook.conf.Configuration;
 import org.apache.log4j.Logger;
 
 import de.anycook.db.mysql.DBHandler;
 import de.anycook.api.DraftGraph;
 import de.anycook.api.MessageGraph;
 
 
 /**
  * Application Lifecycle Listener implementation class StartListener
  *
  */
 @WebListener
 public class StartListener implements ServletContextListener {
 	private Logger logger;
     /**
      * Default constructor. 
      */
     public StartListener() {
     	logger = Logger.getLogger(getClass());
     }
 
 	/**
      * @see ServletContextListener#contextInitialized(ServletContextEvent)
      */
     public void contextInitialized(ServletContextEvent arg0) {
         DBHandler.init();
         MessageGraph.init();
         DraftGraph.init();
         DiscussionGraph.init();
 
 
 
         logger.info("Server started");
     }
 
 	/**
      * @see ServletContextListener#contextDestroyed(ServletContextEvent)
      */
     public void contextDestroyed(ServletContextEvent arg0) {
     	MessageGraph.destroyThreadPool();
         DiscussionGraph.destroyThreadPool();
     	DraftGraph.destroyThreadPool();
     	DBHandler.closeSource();
     	logger.info("Server stopped");
     }
 	
 }
