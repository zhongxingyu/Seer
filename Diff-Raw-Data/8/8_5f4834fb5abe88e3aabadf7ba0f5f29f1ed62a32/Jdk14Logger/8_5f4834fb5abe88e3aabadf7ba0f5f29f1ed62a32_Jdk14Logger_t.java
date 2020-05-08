 /**
  * Created Mar 14, 2011
  */
 package yala.jdk14;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 
 import yala.Logger;
 
 /**
  * @author Alistair A. Israel
  * @since 0.1
  */
 public class Jdk14Logger implements Logger {
 
     private static final ConcurrentHashMap<String, Jdk14Logger> CACHE = new ConcurrentHashMap<String, Jdk14Logger>();
 
     private final java.util.logging.Logger logger;
 
     /**
      * @param name
      *        the logger name
     * @return the Jdk14Logger
      */
     public static Jdk14Logger forName(final String name) {
         if (CACHE.containsKey(name)) {
             return CACHE.get(name);
         }
         final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
         final Jdk14Logger newLogger = new Jdk14Logger(logger);
         final Jdk14Logger existing = CACHE.putIfAbsent(name, newLogger);
         if (existing != null) {
             return existing;
         }
         return newLogger;
     }
 
     /**
      * @param logger
      *        the actual {@link java.util.logging.Logger} to use
      */
     public Jdk14Logger(final java.util.logging.Logger logger) {
         this.logger = logger;
     }
 
     /**
      * @param level
      *        the {@link Level}
      * @return {@code true} if a log message with the given level would be logged by the underlying
      *         {@link java.util.logging.Logger}
      */
     private boolean isLoggable(final Level level) {
         return logger.isLoggable(level);
     }
 
     /**
      * @param level
      *        the {@link Level}
      * @param thrown
      *        the associated {@link Throwable}
      * @param message
      *        the log message
      * @param args
      *        the message parameters
      */
     private void log(final Level level, final Throwable thrown, final String message, final Object[] args) {
         if (!logger.isLoggable(level)) {
             return;
         }
         final LogRecord record = new LogRecord(level, message);
         if (args != null && args.length > 0) {
             record.setParameters(args);
         }
         if (thrown != null) {
             record.setThrown(thrown);
         }
         final StackTraceElement ste = getCallerFrame();
         record.setSourceClassName(ste.getClassName());
         record.setSourceMethodName(ste.getMethodName());
         logger.log(record);
     }
 
     /**
      * {@value #THREAD_CLASS_NAME}
      */
     private static final String THREAD_CLASS_NAME = Thread.class.getName();
 
     /**
      * {@value #JDK14LOGGER_CLASS_NAME}s
      */
     private static final String JDK14LOGGER_CLASS_NAME = Jdk14Logger.class.getName();
 
     /**
      * @return the caller {@link StackTraceElement}
      */
     private StackTraceElement getCallerFrame() {
         final StackTraceElement[] st = Thread.currentThread().getStackTrace();
         final int len = st.length;
         int i = 0;
         while (i < len && THREAD_CLASS_NAME.equals(st[i].getClassName())) {
             ++i;
         }
         while (i < len && JDK14LOGGER_CLASS_NAME.equals(st[i].getClassName())) {
             ++i;
         }
         return st[i];
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#isErrorLoggable()
      */
     public boolean isErrorLoggable() {
         return isLoggable(Level.SEVERE);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#error(java.lang.String, java.lang.Object[])
      */
     public void error(final String message, final Object... args) {
         log(Level.SEVERE, null, message, args);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#error(java.lang.Throwable, java.lang.String, java.lang.Object[])
      */
     public void error(final Throwable thrown, final String message, final Object... args) {
         log(Level.SEVERE, thrown, message, args);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#isWarningLoggable()
      */
     public boolean isWarningLoggable() {
         return isLoggable(Level.WARNING);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#warning(java.lang.String, java.lang.Object[])
      */
     public void warning(final String message, final Object... args) {
         log(Level.WARNING, null, message, args);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#warning(java.lang.Throwable, java.lang.String, java.lang.Object[])
      */
     public void warning(final Throwable thrown, final String message, final Object... args) {
         log(Level.WARNING, thrown, message, args);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#isInfoLoggable()
      */
     public boolean isInfoLoggable() {
         return isLoggable(Level.INFO);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#info(java.lang.String, java.lang.Object[])
      */
     public void info(final String message, final Object... args) {
         log(Level.INFO, null, message, args);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#info(java.lang.Throwable, java.lang.String, java.lang.Object[])
      */
     public void info(final Throwable thrown, final String message, final Object... args) {
         log(Level.INFO, thrown, message, args);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#isDebugLoggable()
      */
     public boolean isDebugLoggable() {
         return isLoggable(Level.FINE);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#debug(java.lang.String, java.lang.Object[])
      */
     public void debug(final String message, final Object... args) {
         log(Level.FINE, null, message, args);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#debug(java.lang.Throwable, java.lang.String, java.lang.Object[])
      */
     public void debug(final Throwable thrown, final String message, final Object... args) {
         log(Level.FINE, thrown, message, args);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#isTraceLoggable()
      */
     public boolean isTraceLoggable() {
         return isLoggable(Level.FINEST);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#trace(java.lang.String, java.lang.Object[])
      */
     public void trace(final String message, final Object... args) {
         log(Level.FINEST, null, message, args);
     }
 
     /**
      * {@inheritDoc}
      *
      * @see yala.Logger#trace(java.lang.Throwable, java.lang.String, java.lang.Object[])
      */
     public void trace(final Throwable thrown, final String message, final Object... args) {
         log(Level.FINEST, thrown, message, args);
     }
 
 }
