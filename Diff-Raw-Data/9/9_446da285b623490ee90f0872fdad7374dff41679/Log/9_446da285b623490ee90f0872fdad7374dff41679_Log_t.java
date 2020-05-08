 package com.toluju.util;
 
import java.text.MessageFormat;
 import org.apache.log4j.Logger;
 
 public class Log {
   protected static Boolean configured = new Boolean(false);
   protected Logger logger;
 
   public Log(String name) {
     logger = Logger.getLogger(name);
   }
 
   public Log(Class clazz) {
     logger = Logger.getLogger(clazz);
   }
 
   public void info(String msg) {
     logger.info(msg);
   }
 
   public void info(String msg, Object... args) {
     logger.info(MessageFormat.format(msg, args));
   }
 
   public void info(String msg, Throwable t) {
     logger.info(msg, t);
   }
 
   public void warn(String msg) {
     logger.warn(msg);
   }
 
   public void warn(String msg, Object... args) {
     logger.warn(MessageFormat.format(msg, args));
   }
 
   public void warn(String msg, Throwable t) {
     logger.warn(msg, t);
   }
 
   public void fatal(String msg) {
     logger.fatal(msg);
   }
 
   public void fatal(String msg, Object... args) {
     logger.fatal(MessageFormat.format(msg, args));
   }
 
   public void fatal(String msg, Throwable t) {
     logger.fatal(msg, t);
   }
 
   public void debug(String msg) {
     logger.debug(msg);
   }
 
   public void debug(String msg, Object... args) {
     logger.debug(MessageFormat.format(msg, args));
   }
 
   public void debug(String msg, Throwable t) {
     logger.debug(msg, t);
   }
 
   public static void main(String[] args) throws Exception {
     Log log = new Log(Log.class);
 
     log.info("Testing some logging: {0} {1}", new Integer(10), log);
    log.warn("Warning! {0} {1} {2}", "Test1", 2, null);
   }
 }
