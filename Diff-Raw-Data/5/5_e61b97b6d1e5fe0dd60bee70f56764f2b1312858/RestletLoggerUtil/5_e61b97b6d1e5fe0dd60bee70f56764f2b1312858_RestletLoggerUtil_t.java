 package org.hackystat.utilities.logger;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import org.hackystat.utilities.home.HackystatUserHome;
 
 /**
  * Provides a convenience method for Restlet logging that adjusts the output Handlers.
  * @author Philip Johnson
  */
 public class RestletLoggerUtil {
   
   /**
    * Adjusts the Restlet Loggers so that they send their output to a file, not the console. 
    * @param serviceDir The directory within .hackystat that this data will be sent to.
    */
   public static void useFileHandler(String serviceDir) {
     LogManager logManager = LogManager.getLogManager();
     //System.out.println("In useFileHandler");
     for (Enumeration<String> en = logManager.getLoggerNames(); en.hasMoreElements() ;) {
       String logName = en.nextElement();
       //System.out.println("logName is: " + logName);
      if ((logName.startsWith("com.noelios") || logName.startsWith("org.restlet")) 
          &&
          (logManager.getLogger(logName) != null)) {
         // First, get rid of current Handlers
         Logger logger = logManager.getLogger(logName);
         //System.out.println("logger is: " + logger);
         logger = logger.getParent();
         //System.out.println("parent logger is: " + logger);
         Handler[] handlers = logger.getHandlers();
         for (Handler handler : handlers) {
           logger.removeHandler(handler);
         }
         //System.out.println("Removed handlers.");
         // Define a file handler that writes to the ~/.hackystat/<service>/logs directory
         File logDir = new File(HackystatUserHome.getHome(), ".hackystat/" + serviceDir + "/logs/");
         logDir.mkdirs();
         //System.out.println("Made this directory: " + logDir);
         String fileName = logDir + "/" + logName + ".%u.log";
         FileHandler fileHandler;
         try {
           fileHandler = new FileHandler(fileName, 500000, 1, true);
           fileHandler.setFormatter(new SimpleFormatter());
           logger.addHandler(fileHandler);
         }
         catch (IOException e) {
           throw new RuntimeException("Could not open the log file for this Hackystat service.", e);
         }
       }
     }
   }
 }
