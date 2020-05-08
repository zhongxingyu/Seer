 package org.emamotor.perf.memleak.util;
 
 import org.eclipse.jetty.annotations.AnnotationConfiguration;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.Configuration;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.eclipse.jetty.webapp.WebInfConfiguration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static ch.qos.logback.classic.Level.*;
 
 /**
  * @author emag
  */
 public class JettyServer {
 
     private static final int JETTY_PORT = Integer.parseInt(System.getProperty(
             /* Property Name */ "org.emamotor.perf.memleak.util.jetty_port",
             /* Default       */ "8080"));
 
    private static final String JETTY_LOG_LVEL = System.getProperty(
             /* Property Name */ "org.emamotor.perf.memleak.util.jetty_log_level",
             /* Default       */ "INFO");
 
     public static void main(String... args) throws Exception {
         configureLogLevel();
 
         Server server = new Server(JETTY_PORT);
         WebAppContext webapp = new WebAppContext();
         webapp.setContextPath("/memleak");
         webapp.setWar("target/memleak.war");
         webapp.setConfigurations(new Configuration[] {
                 new AnnotationConfiguration(),
                 new WebInfConfiguration(),
         });
         server.setHandler(webapp);
         server.start();
         server.join();
     }
 
     private static void configureLogLevel() {
         final Logger LOGGER = LoggerFactory.getLogger("org.eclipse.jetty");
         if (! (LOGGER instanceof ch.qos.logback.classic.Logger)) return;
         ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) LOGGER;
 
        if (JETTY_LOG_LVEL.equals("DEBUG"))
             logbackLogger.setLevel(DEBUG);
         else
             logbackLogger.setLevel(INFO);
     }
 
 }
