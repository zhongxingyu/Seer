 package net.argius.stew;
 
 import java.util.logging.*;
 
 /**
  * The Logger which has Apache Logging style interface.
  *
  * <p>The log levels map to core API's log level as below.</p><ul>
  * <li> fatal: Level.SEVERE (and "fatal" message)
  * <li> error: Level.SEVERE
  * <li> warn: Level.WARNING
  * <li> info: Level.INFO
  * <li> debug: Level.FINE
  * <li> trace: Level.FINER
  * </ul>
  * Level.CONFIG is not used.
  */
 public final class Logger {
 
     private final java.util.logging.Logger log;
 
     private String enteredMethodName;
 
     Logger(String name) {
         this.log = java.util.logging.Logger.getLogger(name);
         removeRootLoggerHandlers();
     }
 
     Logger(Class<?> c) {
         this(c.getName());
     }
 
     static void removeRootLoggerHandlers() {
         java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
         for (Handler handler : rootLogger.getHandlers()) {
             rootLogger.removeHandler(handler);
         }
     }
 
     public static Logger getLogger(Object o) {
         final String name;
         if (o instanceof Class) {
             Class<?> c = (Class<?>)o;
             name = c.getName();
         } else if (o instanceof String) {
             name = (String)o;
         } else {
             name = String.valueOf(o);
         }
         return new Logger(name);
     }
 
     /**
      * The INFO level maps to logging.Level.INFO .
      * @return info level is enabled
      */
     public boolean isInfoEnabled() {
         return log.isLoggable(Level.INFO);
     }
 
     /**
      * The DEBUG level maps to logging.Level.FINE .
      * @return debug level is enabled
      */
     public boolean isDebugEnabled() {
         return log.isLoggable(Level.FINE);
     }
 
     /**
      * The TRACE level maps to logging.Level.FINER .
      * @return debug level is enabled
      */
     public boolean isTraceEnabled() {
         return log.isLoggable(Level.FINER);
     }
 
     public String getEnteredMethodName() {
         return (enteredMethodName == null) ? "" : enteredMethodName;
     }
 
     public void setEnteredMethodName(String methodName) {
         enteredMethodName = (methodName == null) ? "" : methodName;
     }
 
     public void log(Level level, Throwable th, String format, Object... args) {
         if (log.isLoggable(level)) {
             final String cn = log.getName();
             final String mn = (enteredMethodName == null) ? "(unknown method)" : enteredMethodName;
            final String msg = (args.length == 0) ? format : String.format(format, args);
             if (th == null) {
                log.logp(level, cn, mn, msg);
             } else {
                log.logp(level, cn, mn, msg, th);
             }
         }
     }
 
     public void fatal(Throwable th) {
         if (log.isLoggable(Level.SEVERE)) {
             log(Level.SEVERE, th, "*FATAL*");
         }
     }
 
     public void fatal(Throwable th, String format, Object... args) {
         if (log.isLoggable(Level.SEVERE)) {
             log(Level.SEVERE, th, "*FATAL* " + String.format(format, args));
         }
     }
 
     public void error(Throwable th) {
         if (log.isLoggable(Level.SEVERE)) {
             log(Level.SEVERE, th, "");
         }
     }
 
     public void error(Throwable th, Object o) {
         if (log.isLoggable(Level.SEVERE)) {
             log(Level.SEVERE, th, String.valueOf(o));
         }
     }
 
     public void error(Throwable th, String format, Object arg) {
         if (log.isLoggable(Level.SEVERE)) {
             log(Level.SEVERE, th, format, arg);
         }
     }
 
     public void warn(Throwable th) {
         if (log.isLoggable(Level.WARNING)) {
             log(Level.WARNING, th, "");
         }
     }
 
     public void warn(Throwable th, Object o) {
         if (log.isLoggable(Level.WARNING)) {
             log(Level.WARNING, th, String.valueOf(o));
         }
     }
 
     public void warn(String format, Object... args) {
         if (log.isLoggable(Level.WARNING)) {
             log(Level.WARNING, null, format, args);
         }
     }
 
     public void info(Object o) {
         if (isInfoEnabled()) {
             log(Level.INFO, null, String.valueOf(o));
         }
     }
 
     public void info(String format, Object arg) {
         if (isInfoEnabled()) {
             log(Level.INFO, null, format, arg);
         }
     }
 
     public void info(String format, Object arg1, Object arg2) {
         if (isInfoEnabled()) {
             log(Level.INFO, null, format, arg1, arg2);
         }
     }
 
     public void info(String format, Object... args) {
         if (isInfoEnabled()) {
             log(Level.INFO, null, format, args);
         }
     }
 
     public void debug(Object o) {
         if (isDebugEnabled()) {
             log(Level.FINE, null, String.valueOf(o));
         }
     }
 
     public void debug(String format, Object arg) {
         if (isDebugEnabled()) {
             log(Level.FINE, null, format, arg);
         }
     }
 
     public void debug(String format, Object arg1, Object arg2) {
         if (isDebugEnabled()) {
             log(Level.FINE, null, format, arg1, arg2);
         }
     }
 
     public void debug(String format, Object... args) {
         if (isDebugEnabled()) {
             log(Level.FINE, null, format, args);
         }
     }
 
     public void trace(Throwable th) {
         if (isTraceEnabled()) {
             log(Level.FINER, th, "");
         }
     }
 
     public void trace(Object o) {
         if (isTraceEnabled()) {
             log(Level.FINER, null, String.valueOf(o));
         }
     }
 
     public void trace(String format, Object arg) {
         if (isTraceEnabled()) {
             log(Level.FINER, null, format, arg);
         }
     }
 
     public void trace(String format, Object... args) {
         if (isTraceEnabled()) {
             log(Level.FINER, null, format, args);
         }
     }
 
     public void atEnter(String method, Object... args) {
         // trace("entering method [%s]", method);
         setEnteredMethodName(method);
         log.entering(log.getName(), method, args);
     }
 
     public void atExit(String method) {
         // trace("exiting method [%s] with return value [%s]", method, returnValue);
         log.exiting(log.getName(), method);
         setEnteredMethodName("");
     }
 
     public <T> T atExit(String method, T returnValue) {
         // trace("exiting method [%s] with return value [%s]", method, returnValue);
         log.exiting(log.getName(), method, returnValue);
         setEnteredMethodName("");
         return returnValue;
     }
 
 }
