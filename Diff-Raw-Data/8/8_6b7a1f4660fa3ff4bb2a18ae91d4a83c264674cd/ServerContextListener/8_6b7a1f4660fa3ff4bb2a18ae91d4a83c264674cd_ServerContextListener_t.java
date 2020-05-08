 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com>
  */
 package org.lafayette.server;
 
 //import de.weltraumschaf.citer.tpl.SiteLayout;
 //import freemarker.template.Configuration;
 //import freemarker.template.ObjectWrapper;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 /**
  * Implements a servlet context listener.
  *
  * Creates and provides an instance of a Neo4j database.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class ServerContextListener implements ServletContextListener {
 
//    private static final String TEMPLATE_PREFIX = "/org/lafayette/server/resources";
 
     public ServerContextListener() {
         super();
     }
 
     @Override
     public void contextInitialized(ServletContextEvent sce) {
 //        sce.getServletContext().setAttribute(CiterRegistry.KEY, registry);
     }
 
     @Override
     public void contextDestroyed(final ServletContextEvent sce) {
 
     }
 
 }
