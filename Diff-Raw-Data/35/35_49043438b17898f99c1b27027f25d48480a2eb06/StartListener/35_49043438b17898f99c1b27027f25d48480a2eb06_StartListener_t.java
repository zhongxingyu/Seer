/*
 * This file is part of anycook. The new internet cookbook
 * Copyright (C) 2013 Jan Graßegger
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
