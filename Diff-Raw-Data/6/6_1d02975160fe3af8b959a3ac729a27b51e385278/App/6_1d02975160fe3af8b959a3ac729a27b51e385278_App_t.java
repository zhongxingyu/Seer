 /*
  * App.java
  * Copyright (C) 2011,2012 Wannes De Smet
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.xenmaster;
 
 import java.net.URL;
 
 import net.wgr.core.access.Authorize;
 import net.wgr.core.data.DataPool;
 import net.wgr.server.application.DefaultApplication;
 import net.wgr.server.http.Server;
 import net.wgr.server.web.handling.ServerHook;
 import net.wgr.server.web.hooks.SinglePageHook;
 import net.wgr.settings.Settings;
 import net.wgr.utility.GlobalExecutorService;
 import org.apache.commons.daemon.Daemon;
 import org.apache.commons.daemon.DaemonContext;
 import org.apache.commons.daemon.DaemonInitException;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.EnhancedPatternLayout;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.xenmaster.api.util.CachingFacility;
 import org.xenmaster.controller.Controller;
 import org.xenmaster.monitoring.MonitoringAgent;
 import org.xenmaster.pool.Pool;
 import org.xenmaster.setup.debian.Bootstrapper;
 import org.xenmaster.web.Hook;
 import org.xenmaster.web.SetupHook;
 import org.xenmaster.web.TemplateHook;
 import org.xenmaster.web.VNCHook;
 
 public class App implements Daemon {
 
     protected Server server;
     public static final String LOGPATTERN = "%d{HH:mm:ss,SSS} | %-5p | %t | %c{1.} %m%n";
 
     public static void main(String[] args) {
         try {
             final App app = new App();
             app.init(null);
             app.start();
         } catch (Exception ex) {
             Logger.getLogger(App.class).error("An unhandled exception ocurred", ex);
         }
     }
 
     @Override
     public void init(DaemonContext context) throws DaemonInitException, Exception {
         Logger root = Logger.getRootLogger();
        root.setLevel(Level.INFO);
         root.addAppender(new ConsoleAppender(new EnhancedPatternLayout(LOGPATTERN)));
         
         if (context != null) {
             Logger.getLogger(getClass()).info("Starting XenMaster service");
             if (context.getArguments() != null && context.getArguments()[0] != null) {
                 Settings.loadFromFile(context.getArguments()[0]);
             }
         }
        
        // Reset level
        Level.toLevel(Settings.getInstance().getString("Logging.Level"));
 
         if (context == null) {
             Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         stop();
                     } catch (Exception ex) {
                         Logger.getLogger(App.class).error("Failed to shut down", ex);
                     }
                 }
             }));
         }
     }
 
     @Override
     public void start() throws Exception {
         Settings s = Settings.getInstance();
         DataPool.simpleBoot(s.getString("Cassandra.PoolName"), s.getString("Cassandra.Host"), s.getString("Cassandra.Keyspace"));
         Bootstrapper b = new Bootstrapper();
         b.boot();
 
         Controller.build(new URL(s.getString("Xen.URL")));
         Controller.getSession().loginWithPassword(s.getString("Xen.User"), s.getString("Xen.Password"));
 
         server = new Server();
         server.boot();
 
         CachingFacility.instance(false);
         Authorize.disable();
 
         if (Controller.getSession().getReference() == null) {
             Logger.getLogger(getClass()).error("Failed to connect to XAPI instance, running in bootstrap mode");
 
             ServerHook sh = new ServerHook("/*");
             sh.addWebHook(new SetupHook());
             sh.addWebHook(new SinglePageHook(IOUtils.toString(getClass().getResourceAsStream("/content/error.html")), "Failed to connect to the Xen server."
                     + "<br /><a href=\"http://wiki.xen-master.org/wiki/Bootstrap\">Bootstrap</a> only mode has been engaged."));
             sh.hookIntoServer(server);
             server.start();
 
             b.waitForServerToQuit();
             return;
         }
 
         Pool.get().boot();
         MonitoringAgent.instance().boot();
         MonitoringAgent.instance().start();
 
         server = new Server();
         server.boot();
 
         ServerHook sh = new ServerHook("/*");
         sh.addWebHook(new Hook());
         sh.addWebHook(new TemplateHook());
         sh.addWebHook(new SetupHook());
         sh.addWebHook(new VNCHook());
 
         server.addServlet(sh.getHttpHandler());
         server.addServlet(sh.getWebSocketHandler());
 
         DefaultApplication da = DefaultApplication.create("/", Settings.getInstance().getString("WebContentPath"));
         server.addHook(da);
         server.start();
     }
 
     @Override
     public void stop() throws Exception {
         server.stop();
         Pool.get().stop();
         DataPool.stop();
         CachingFacility.instance().stop();
         GlobalExecutorService.get().shutdownNow();
     }
 
     @Override
     public void destroy() {
         server = null;
     }
 }
