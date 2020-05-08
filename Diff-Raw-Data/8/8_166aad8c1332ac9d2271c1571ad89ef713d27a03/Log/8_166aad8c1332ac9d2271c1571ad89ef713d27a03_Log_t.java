 package net.codjo.mad.common;
 import org.apache.log4j.Logger;
 /**
  * Classe de Log marchant par dlgation sur Log4J.
  */
 public final class Log {
     private static final Logger APP = Logger.getLogger(Log.class);
 

    private Log() {
    }

 
     public static void debug(Object msg) {
         APP.debug(msg);
     }
 
 
     public static void debug(Object msg, Throwable error) {
         APP.debug(msg, error);
     }
 
 
     public static void error(Object msg) {
         APP.error(msg);
     }
 
 
     public static void error(Object msg, Throwable error) {
        APP.error(msg, error);
     }
 
 
     public static void fatal(Object msg) {
         APP.fatal(msg);
     }
 
 
     public static void fatal(Object msg, Throwable error) {
         APP.fatal(msg, error);
     }
 
 
     public static void info(Object msg) {
         APP.info(msg);
     }
 
 
     public static void info(Object msg, Throwable error) {
         APP.info(msg, error);
     }
 
 
     public static void warn(Object msg) {
         APP.warn(msg);
     }
 
 
     public static void warn(Object msg, Throwable error) {
         APP.warn(msg, error);
     }
 }
