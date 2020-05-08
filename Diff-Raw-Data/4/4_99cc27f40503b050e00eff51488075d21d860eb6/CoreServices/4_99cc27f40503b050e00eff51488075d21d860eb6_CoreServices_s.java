 /*
  * Copyright (c) 2011.  Korwe Software
  *
  *  This file is part of TheCore.
  *
  *  TheCore is free software: you can redistribute it and/or modify it
  *  under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  TheCore is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with TheCore.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.korwe.thecore.service;
 
 import com.google.common.util.concurrent.Service;
 import com.google.inject.servlet.GuiceFilter;
 import com.korwe.thecore.api.CoreConfig;
 import com.korwe.thecore.service.ping.CorePingService;
 import com.korwe.thecore.service.ping.PingServiceImpl;
 import com.korwe.thecore.service.syndication.CoreSyndicationService;
 import com.korwe.thecore.service.syndication.SyndicationServiceImpl;
 import com.korwe.thecore.session.SessionManager;
 import com.korwe.thecore.webcore.listener.WebCoreListener;
 import com.korwe.thecore.webcore.servlet.GuiceServletConfig;
 import org.apache.log4j.Logger;
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.util.thread.ExecutorThreadPool;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author <a href="mailto:nithia.govender@korwe.com>Nithia Govender</a>
  */
 public class CoreServices {
 
     private static final Logger LOG = Logger.getLogger(CoreServices.class);
 
     private static Set<Service> services = new HashSet<Service>(5);
 
     public static void main(String[] args) {
         CoreConfig.initialize(CoreServices.class.getResourceAsStream("/coreconfig.xml"));
         final Server servletServer = configureServer();
        services.add(new CorePingService());
        services.add(new CoreSyndicationService());
 
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() {
                 //sessionManager.stop();
 //                webCoreListener.stop();
                 for (Service service: services) {
                     service.stop();
                 }
 
                 try {
                     servletServer.stop();
                 }
                 catch (Exception e) {
                     LOG.error(e);
                 }
             }
         });
 
         //sessionManager.start();
 //        webCoreListener.start();
         for (Service service : services) {
             service.start();
         }
 
         try {
             servletServer.start();
             servletServer.join();
         }
         catch (Exception e) {
             LOG.error(e);
         }
     }
 
     private static Server configureServer() {
         Server server = new Server();
         Connector connector = new SelectChannelConnector();
         connector.setHost("0.0.0.0");
         connector.setPort(8090);
         server.setConnectors(new Connector[] {connector});
         ServletContextHandler contextHandler = new ServletContextHandler(server, "/", ServletContextHandler.NO_SESSIONS);
         contextHandler.addFilter(GuiceFilter.class, "/*", 0);
         contextHandler.addEventListener(new GuiceServletConfig());
         contextHandler.addServlet(DefaultServlet.class, "/");
         server.setThreadPool(new ExecutorThreadPool());
         return server;
     }
 }
