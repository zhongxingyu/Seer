 package lab;
 
 import java.io.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Logger {
 	public static int LOG_LEVEL_DEBUG = 5;
 	public static int LOG_LEVEL_INFO = 4;
 	public static int LOG_LEVEL_WARNING = 3;
 	public static int LOG_LEVEL_ERROR = 2;
 	public static int LOG_LEVEL_CRITICAL = 1;
 
     private static Logger logger;
 
     private PrintWriter logFile;
     private ConfigManager config;
 
     public static void log(String line, int level) {
         if (logger == null) {
             logger = new Logger();
         }
         logger.logLine(line, level);
 
     }
 
     public Logger() {
         config = ConfigManager.getInstance();
         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
         try {
             // open today's log file in append mode
             logFile = new PrintWriter(new BufferedWriter(new FileWriter(
                     dateFormat.format(new Date()) + ".txt", true)));
 
         } catch (IOException e) {
             System.err.println("Can't open logfile!");
             e.printStackTrace();
         }
     }
 
     public void logLine(String line, int level) {
         if (logFile != null && config.getFileDebugLevel() >= level) {
            logFile.println(line);
         }
 
         if (config.getScreenDebugLevel() >= level) {
            System.err.println(line);
         }
     }
 
 	public static void debug(String line) {
 		log(line, LOG_LEVEL_DEBUG);
 	}
 	
 	public static void info(String line) {
         log(line, LOG_LEVEL_INFO);
 
     }
 	
 	public static void warning(String line) {
         log(line, LOG_LEVEL_WARNING);
 
     }
 	
 	public static void error(String line) {
         log(line, LOG_LEVEL_ERROR);
 
     }
 	
 	public static void critical(String line) {
         log(line, LOG_LEVEL_CRITICAL);
 
     }
 
 }
