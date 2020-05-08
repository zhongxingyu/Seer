 // See the COPYRIGHT file for copyright and license information
 package org.znerd.logdoc.slf4j;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.MDC;
import org.znerd.logdoc.LogBridge;
 import org.znerd.util.log.LogLevel;
 
public class Slf4jLogBridge extends LogBridge {
 
     private static final String CONTEXT_ID_KEY = "contextID";
 
     @Override
     public void putContextId(String newContextId) {
         MDC.put(CONTEXT_ID_KEY, newContextId);
     }
 
     @Override
     public void unputContextId() {
         MDC.remove(CONTEXT_ID_KEY);
     }
 
     @Override
     public String getContextId() {
         return MDC.get(CONTEXT_ID_KEY);
     }
 
     @Override
     public boolean shouldLog(String domain, String groupId, String entryId, LogLevel level) {
         Logger logger = getLogger(domain, groupId, entryId);
         return isLevelEnabled(logger, level);
     }
 
     private Logger getLogger(String domain, String groupId, String entryId) {
         final String loggerName = determineLoggerName(domain, groupId, entryId);
         return LoggerFactory.getLogger(loggerName);
     }
 
     private String determineLoggerName(String domain, String groupId, String entryId) {
         final String categoryId = domain + '.' + groupId + '.' + entryId;
         return categoryId;
     }
 
     private boolean isLevelEnabled(Logger logger, LogLevel level) {
         if (LogLevel.DEBUG.equals(level)) {
             return logger.isDebugEnabled();
         } else if (LogLevel.INFO.equals(level) || LogLevel.NOTICE.equals(level)) {
             return logger.isInfoEnabled();
         } else if (LogLevel.WARNING.equals(level)) {
             return logger.isWarnEnabled();
         } else {
             return logger.isErrorEnabled();
         }
     }
 
     @Override
     public void logOneMessage(String fqcn, String domain, String groupId, String entryId, LogLevel level, String message, Throwable exception) {
         Logger logger = getLogger(domain, groupId, entryId);
         logOneMessage(logger, level, message, exception);
     }
 
     private void logOneMessage(Logger logger, LogLevel level, String message, Throwable exception) {
         final String outputMessage = createOutputMessage(level, message);
         logOneMessageImpl(logger, level, outputMessage, exception);
     }
 
     private String createOutputMessage(LogLevel level, String message) {
         if (LogLevel.NOTICE.equals(level)) {
             return "NOTICE: " + message;
         } else if (LogLevel.FATAL.equals(level)) {
             return "FATAL: " + message;
         } else {
             return message;
         }
     }
 
     private void logOneMessageImpl(Logger logger, LogLevel level, String message, Throwable exception) {
         if (LogLevel.DEBUG.equals(level)) {
             logger.debug(message, exception);
         } else if (LogLevel.INFO.equals(level) || LogLevel.NOTICE.equals(level)) {
             logger.info(message, exception);
         } else if (LogLevel.WARNING.equals(level)) {
             logger.warn(message, exception);
         } else {
             logger.error(message, exception);
         }
     }
 }
