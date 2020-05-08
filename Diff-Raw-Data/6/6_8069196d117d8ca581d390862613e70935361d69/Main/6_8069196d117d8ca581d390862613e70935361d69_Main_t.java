 package be.mira.adastra3.server;
 
 import be.mira.adastra3.server.controller.Controller;
 import be.mira.adastra3.server.network.NetworkMonitor;
 import be.mira.adastra3.server.exceptions.ServiceSetupException;
 import be.mira.adastra3.server.exceptions.ServiceRunException;
 import be.mira.adastra3.server.repository.RepositoryMonitor;
 import be.mira.adastra3.server.website.EmbeddedTomcat;
import java.io.IOException;
 import java.util.EnumMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import javax.servlet.ServletException;
 import org.apache.catalina.LifecycleException;
import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 /**
  * Hello world!
  *
  */
 public class Main {
     //
     // Auxiliary classes
     //
 
     private enum Status {
         IDLE,
         INITIALIZING,
         STARTING,
         RUNNING,
         STOPPING
     }
 
     private enum ServiceType {
         NETWORK,
         REPOSITORY,
         WEBSITE,
         CONTROLLER
     }
 
     //
     // Routines
     //
     
     public final void quit() {
         if (STATUS == Status.RUNNING) {
             LOGGER.info("Stopping subsystems");
             STATUS = Status.STOPPING;
             stop();
 
             LOGGER.info("Exiting");
             STATUS = Status.IDLE;
             System.exit(0);
         } else if (STATUS != Status.STOPPING) {
             LOGGER.info("Exiting");
             STATUS = Status.IDLE;
             System.exit(0);
         } else {
             LOGGER.debug("Ignoring call to quit() as the application is " + STATUS.name());
         }
     }
 
 
     //
     // Static
     //
 
     private static Map<ServiceType, Service> SUBSERVICES;
     private final static Map<ServiceType, String> cServiceNames;
     static {
         cServiceNames = new EnumMap<ServiceType, String>(ServiceType.class);
         cServiceNames.put(ServiceType.NETWORK, "network monitor");
         cServiceNames.put(ServiceType.REPOSITORY, "repository monitor");
         cServiceNames.put(ServiceType.WEBSITE, "web server");
         cServiceNames.put(ServiceType.CONTROLLER, "application controller");
     }
     private static Logger LOGGER;
     private static Status STATUS = Status.IDLE;
 
     public static void main(final String[] iParameters) throws ServletException, LifecycleException {
         //
         // Set-up
         //
 
         // Logging
         BasicConfigurator.configure();
         LOGGER = Logger.getLogger(Main.class);
         try {
             Iterator tKeyIterator = Service.getConfiguration().getKeys("log4j");
             Properties tLoggingProperties = new Properties();
             while (tKeyIterator.hasNext()) {
                 String tKey = (String) tKeyIterator.next();
                String tValue = StringUtils.join(Service.getConfiguration().getStringArray(tKey), ", ");
                 tLoggingProperties.put(tKey, tValue);
             }
             PropertyConfigurator.configure(tLoggingProperties);
         } catch (ServiceSetupException tException) {
             LOGGER.warn("Could not configure the logging subsystem, using a very basic configuration", tException);
         }
 
         // Subsystems
         LOGGER.info("Initializing subsystems");
         STATUS = Status.INITIALIZING;
         if (!initialize()) {
             LOGGER.error("Some subsystems failed to initialize, bailing out");
 
             STATUS = Status.IDLE;
             System.exit(0);
         }
 
         //
         // Start
         //
 
         // Subsystems
         LOGGER.info("Starting subsystems");
         STATUS = Status.STARTING;
         if (!start()) {
             LOGGER.error("Some subsystems failed to start, bailing out");
 
             STATUS = Status.STOPPING;
             stop();
 
             STATUS = Status.IDLE;
             System.exit(0);
         }
 
 
         //
         // Sleep
         //
 
         LOGGER.info("Entering main loop");
         STATUS = Status.RUNNING;
         while (true) {
             try {
                 Thread.sleep(50);
             } catch (InterruptedException tException) {
                 LOGGER.warn("Main loop interrupted", tException);
                 break;
             }
         }
 
         LOGGER.info("Stopping subsystems");
         STATUS = Status.STOPPING;
         stop();
 
         LOGGER.info("Exiting");
         STATUS = Status.IDLE;
         System.exit(0);
     }
 
     private static boolean initialize() {
         // Map with service objects
         SUBSERVICES = new EnumMap<ServiceType, Service>(ServiceType.class);
 
         // Process all subservices
         for (ServiceType tServiceType : ServiceType.values()) {
             LOGGER.debug("Initializing the " + cServiceNames.get(tServiceType));
             try {
                 Service tService = null;
                 switch (tServiceType) {
                     case REPOSITORY:
                         tService = new RepositoryMonitor();
                         break;
                     case WEBSITE:
                         tService = new EmbeddedTomcat();
                         ((EmbeddedTomcat)tService).addWebapp("status");
                         break;
                     case NETWORK:
                         tService = new NetworkMonitor();
                         break;
                     case CONTROLLER:
                         tService = new Controller();
                         break;
                     default:
                         throw new ServiceSetupException("I don't know how to initialize the " + cServiceNames.get(tServiceType));
                 }
                 SUBSERVICES.put(tServiceType, tService);
             } catch (ServiceSetupException tException) {
                 LOGGER.error("Could not initialize the " + cServiceNames.get(tServiceType), tException);
                 return false;
             }
         }
 
         return true;
     }
 
     private static boolean start() {
         for (ServiceType tServiceType : SUBSERVICES.keySet()) {
             Service tService = SUBSERVICES.get(tServiceType);
             LOGGER.debug("Starting the " + cServiceNames.get(tServiceType));
             try {
                 tService.run();
             } catch (ServiceRunException tException) {
                 LOGGER.error("Could not start the " + cServiceNames.get(tServiceType), tException);
                 return false;
             }
         }
 
         return true;
     }
 
     private static void stop() {
         for (ServiceType tServiceType : SUBSERVICES.keySet()) {
             Service tService = SUBSERVICES.get(tServiceType);
             LOGGER.debug("Stopping the " + cServiceNames.get(tServiceType));
             try {
                 tService.stop();
                 SUBSERVICES.remove(tServiceType);
             } catch (ServiceRunException tException) {
                 LOGGER.error("Could not stop the " + cServiceNames.get(tServiceType), tException);
             }
         }
     }
 }
