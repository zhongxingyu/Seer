 /**
  * Copyright 2007-2012 Jason Pelzer.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.pelzer.util;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.ErrorManager;
 import java.util.logging.FileHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.LogRecord;
 import java.util.logging.StreamHandler;
 
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

 /**
  * The Logging system is used to output informational and logging messages to
  * the console. Uses the java.util.logging system.
  */
 public final class Logging {
   private static Logger           loggingLogger  = new Logger(java.util.logging.Logger.getLogger(Logging.class.getName()));
   static java.util.logging.Logger rootLogger     = java.util.logging.Logger.getLogger("");
   private static volatile boolean mute           = false;
   private static boolean          logMethodNames = false;
   private static LogManager       logManager     = LogManager.getLogManager();
   private static StreamHandler    streamHandler  = new StreamHandler(System.out, new LogFormatter());
 
   static {
     // Mute the logging system?
     mute = StringMan.isStringTrue(System.getProperty("pelzer.log.mute"));
 
     // Enable method-name logging?
     logMethodNames = StringMan.isStringTrue(System.getProperty("pelzer.log.methods"));
 
     // Set up a rolling log file?
     final String logfile = System.getProperty("pelzer.log");
     if (logfile != null)
       try {
         final FileHandler fileHandler = new FileHandler(logfile, 2000000000, 5, false);
         fileHandler.setFormatter(new LogFormatter());
         fileHandler.setErrorManager(new SimpleErrorManager());
         // If we got this far, the file got opened correctly to set our only
         // handler to be the fileHandler.
         System.out.println("System will now begin rolling logging to '" + logfile + ".0'");
         streamHandler = fileHandler;
       }
       catch (final IOException ex) {
         System.out.println("IOException while opening '" + logfile + "' for logging, unable to start.");
         ex.printStackTrace();
         System.exit(-1);
       }
 
     // Remove the annoying default handler from the root logger
     final Handler rootHandlers[] = rootLogger.getHandlers();
     for (final Handler rootHandler : rootHandlers)
       rootLogger.removeHandler(rootHandler);
 
     // Set up our own formatter...
     streamHandler.setLevel(Priority.ALL.getLevel());
     rootLogger.addHandler(streamHandler);
     java.util.logging.Logger.getLogger("com.pelzer").setLevel(Priority.ALL.getLevel());
 
     if (StringMan.isStringTrue(PropertyManager.getProperty("pelzer.log.configurelog4j")))
       configureLog4j();
 
     loggingLogger.info("Build Info: Build #" + PropertyManager.getBuildNumber() + " - " + PropertyManager.getProperty("", "build.date") + " (" + PropertyManager.getProperty("", "build.user") + ")");
     loggingLogger.info("Logging is now initialized.");
   }
 
 
   /**
    * Called during init, potentially resets log4j to follow the settings
    * configured by pelzer.util.
    */
   private static void configureLog4j() {
     try {
       // Check to see if log4j is in the classpath
       Class.forName("org.apache.log4j.Logger");
     }
     catch (Exception ex) {
       loggingLogger.warn("Attempted to override log4j configuration, but log4j wasn't found");
       return;
     }
 
     loggingLogger.info("Reseting log4j to use pelzer logging instead.");
     org.apache.log4j.LogManager.resetConfiguration();
    org.apache.log4j.Logger.getRootLogger().addAppender(new Appender() {
       private String name;

       @Override
       public void setName(String name) {
         this.name = name;
       }
 
       @Override
      public void setLayout(Layout layout) {
       }
 
       @Override
      public void setErrorHandler(ErrorHandler errorHandler) {
       }
 
       @Override
       public boolean requiresLayout() {
         return false;
       }
 
       @Override
       public String getName() {
         return name;
       }
 
       @Override
      public Layout getLayout() {
         return null;
       }
 
       @Override
      public Filter getFilter() {
         return null;
       }
 
       @Override
      public ErrorHandler getErrorHandler() {
         return null;
       }
 
       @Override
      public void doAppend(LoggingEvent event) {
         Logger logger = getLogger(event.getLoggerName());
         logger.genericLog(event.getMessage() + "", event.getThrowableInformation() == null ? null : event.getThrowableInformation().getThrowable(), convertPriority(event.getLevel()));
        // TODO Auto-generated method stub
       }
 
       private Priority convertPriority(org.apache.log4j.Level level) {
         switch (level.toInt()) {
           case org.apache.log4j.Level.ALL_INT:
             return Priority.ALL;
           case org.apache.log4j.Level.TRACE_INT:
             return Priority.VERBOSE;
           case org.apache.log4j.Level.DEBUG_INT:
             return Priority.DEBUG;
           case org.apache.log4j.Level.WARN_INT:
             return Priority.WARN;
           case org.apache.log4j.Level.INFO_INT:
             return Priority.INFO;
           case org.apache.log4j.Level.ERROR_INT:
             return Priority.ERROR;
           case org.apache.log4j.Level.FATAL_INT:
             return Priority.FATAL;
         }
         return Priority.OFF;
       }
 
       @Override
       public void close() {
       }
 
       @Override
       public void clearFilters() {
       }
 
       @Override
      public void addFilter(Filter newFilter) {
       }
     });
   }
 
   private Logging() {
   }
 
   /**
    * This is the new, official way to get a logger from the Logging system.
    * Returns a custom wrapper class that allows the normal debug(), info(),
    * warn(), error(), fatal() methods, as well as custom stuff.
    */
   public static Logging.Logger getLogger(final String node) {
     return new Logging.Logger(initialize(node, true));
   }
 
   /**
    * Convenience method, same as doing
    * <code>getLogger(loggedClass.getName())</code>
    */
   public static Logging.Logger getLogger(final Class<?> loggedClass) {
     return getLogger(loggedClass.getName());
   }
 
   /**
    * Convenience method, same as doing
    * <code>getLogger(loggedObject.getClass().getName())</code>
    */
   public static Logging.Logger getLogger(final Object loggedObject) {
     return getLogger(loggedObject.getClass());
   }
 
   /**
    * Turns off all debugging until {@link #unmute()} is called or the system
    * shuts down.
    */
   public synchronized static void mute() {
     rootLogger.severe("Muting logging system.");
     mute = true;
   }
 
   /** Turns logging back on after calling {@link #mute()} */
   public synchronized static void unmute() {
     rootLogger.severe("Unmuting logging system.");
     mute = false;
   }
 
   /**
    * @return true if the logging system is currently muted.
    * @see #mute()
    * @see #unmute()
    */
   public static boolean isMuted() {
     return mute;
   }
 
   private static void initializeParent(final String node) {
     if (node.lastIndexOf(".") > -1)
       initializeParent(node.substring(0, node.lastIndexOf(".")));
     initialize(node, false);
   }
 
   private static java.util.logging.Logger initialize(final String node, final boolean initParents) {
     java.util.logging.Logger logger = logManager.getLogger(node);
     if (logger == null) {
       if (initParents && node.indexOf(".") > -1)
         // Make sure parent nodes initialize first:
         // ie. do 'com', then 'com.pelzer', then 'com.pelzer.util'
         initializeParent(node.substring(0, node.lastIndexOf(".")));
       // Create the new logger
       logger = java.util.logging.Logger.getLogger(node);
       final String logPath = "com.pelzer.util.Logging.";
       String appenderString = PropertyManager.getLocalizedProperty(logPath + node, "appender");
       if (appenderString == null)
         appenderString = PropertyManager.getProperty(logPath + node, "appender");
       final String priorityString = PropertyManager.getProperty(logPath + node, "priority");
       // Priority priority = Priority.ALL; // Default to ALL
       Priority priority = null;
       // set priority
       if (priorityString != null)
         if (priorityString.equals("FATAL"))
           priority = Priority.FATAL;
         else if (priorityString.equals("ERROR"))
           priority = Priority.ERROR;
         else if (priorityString.equals("WARN"))
           priority = Priority.WARN;
         else if (priorityString.equals("INFO"))
           priority = Priority.INFO;
         else if (priorityString.equals("DEBUG"))
           priority = Priority.DEBUG;
         else if (priorityString.equals("VERBOSE"))
           priority = Priority.VERBOSE;
         else if (priorityString.equals("OBNOXIOUS"))
           priority = Priority.OBNOXIOUS;
         else if (priorityString.equals("ALL"))
           priority = Priority.ALL;
       initialize(node, priority, appenderString);
     }
 
     return logger;
   }
 
   private static Map<String, java.util.logging.Logger> loggers = new HashMap<String, java.util.logging.Logger>();
 
   private synchronized static java.util.logging.Logger initialize(final String node, final Priority priority, final String appenderString) {
     java.util.logging.Logger logger = loggers.get(node);
     if (logger == null) {
       logger = java.util.logging.Logger.getLogger(node);
       loggingLogger.warn("Doing initialization for node '" + node + "', appender '" + appenderString + "', priority '" + priority + "'");
       if (priority != null)
         logger.setLevel(priority.getLevel());
       loggers.put(node, logger);
     } else {
       // debug.debug("Node '" + node + "' already initialized.");
     }
     return logger;
   }
 
   /**
    * Wrapper class around our logging... Will allow us to migrate and add
    * functionality without breaking things in the future.
    */
   public static class Logger implements java.io.Serializable {
     private static final long serialVersionUID = 1L;
     java.util.logging.Logger  logger;
 
     private Level             unmutedLevel     = null;
 
     /**
      * Mutes this particular logger. Has no effect if already muted (safe to
      * call more than once)
      */
     public void mute() {
       if (logger.getLevel() != Level.OFF) {
         unmutedLevel = logger.getLevel();
         logger.setLevel(Level.OFF);
       }
     }
 
     /**
      * Unmutes this particular logger, or does nothing if this logger is not
      * currently muted.
      */
     public void unmute() {
       if (logger.getLevel() == Level.OFF)
         logger.setLevel(unmutedLevel);
     }
 
     Logger(final java.util.logging.Logger logger) {
       this.logger = logger;
     }
 
     /**
      * Since our underlying logging architecture is not serializable, we have to
      * manually serialize this object... We only send the name of this node,
      * then have the Logging object on the deserialization side recreate a new
      * item based on the name. Simple.
      */
     private void writeObject(final java.io.ObjectOutputStream out) throws java.io.IOException {
       out.writeObject(logger.getName());
     }
 
     private void readObject(final java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
       logger = Logging.getLogger(in.readObject().toString()).logger;
     }
 
     public boolean isDebugEnabled() {
       return logger.isLoggable(Priority.DEBUG.getLevel());
     }
 
     public boolean isInfoEnabled() {
       return logger.isLoggable(Priority.INFO.getLevel());
     }
 
     private void genericLog(String message, final Throwable ex, final Priority priority, final Object... objects) {
       if (!mute) {
         final String replacements[] = new String[objects.length];
         for (int i = 0; i < objects.length; i++)
           replacements[i] = (objects[i] == null) ? "null" : objects[i].toString();
         message = StringMan.replace(message, "{}", replacements);
         if (ex == null)
           logger.log(priority.getLevel(), message);
         else
           logger.log(priority.getLevel(), message, ex);
         streamHandler.flush();
       }
     }
 
     /**
      * Takes a message and an unspecified number of tokens to put into that
      * message, looking for the pattern "{}". For instance, logging something
      * like debug("Attempt {} of {}.",1,2) would put out "Attempt 1 of 2". If
      * you have an exception, put it after the message but before the
      * replacement tokens, ie error("Failed in attempt {}", ex, 2). The reason
      * to use this mechanism instead of "Failed in attempt "+2 (for instance) is
      * that the overhead of String concatenation is internally skipped if
      * isDebugEnabled() or isInfoEnabled() are false, significantly improving
      * debug performance without resorting the bulky:
      * 
      * <pre>
      * if (log.isDebugEnabled())
      *   log.debug(&quot;Foo&quot; + &quot; is the answer!&quot;);
      * </pre>
      * 
      * The new hotness looks like this:
      * 
      * <pre>
      * log.debug(&quot;Foo {}&quot;, &quot;is the answer!&quot;);
      * </pre>
      */
     public void debug(final String message, final Object... replacementTokens) {
       if (!isDebugEnabled())
         return;
       genericLog(message, null, Priority.DEBUG, replacementTokens);
     }
 
     /**
      * Takes a message and an unspecified number of tokens to put into that
      * message, looking for the pattern "{}". For instance, logging something
      * like debug("Attempt {} of {}.",1,2) would put out "Attempt 1 of 2". If
      * you have an exception, put it after the message but before the
      * replacement tokens, ie error("Failed in attempt {}", ex, 2). The reason
      * to use this mechanism instead of "Failed in attempt "+2 (for instance) is
      * that the overhead of String concatenation is internally skipped if
      * isDebugEnabled() or isInfoEnabled() are false, significantly improving
      * debug performance without resorting the bulky:
      * 
      * <pre>
      * if (log.isDebugEnabled())
      *   log.debug(&quot;Foo&quot; + &quot; is the answer!&quot;);
      * </pre>
      * 
      * The new hotness looks like this:
      * 
      * <pre>
      * log.debug(&quot;Foo {}&quot;, &quot;is the answer!&quot;);
      * </pre>
      */
     public void debug(final String message, final Throwable ex, final Object... replacementTokens) {
       if (!isDebugEnabled())
         return;
       genericLog(message, ex, Priority.DEBUG, replacementTokens);
     }
 
     /**
      * Takes a message and an unspecified number of tokens to put into that
      * message, looking for the pattern "{}". For instance, logging something
      * like debug("Attempt {} of {}.",1,2) would put out "Attempt 1 of 2". If
      * you have an exception, put it after the message but before the
      * replacement tokens, ie error("Failed in attempt {}", ex, 2). The reason
      * to use this mechanism instead of "Failed in attempt "+2 (for instance) is
      * that the overhead of String concatenation is internally skipped if
      * isDebugEnabled() or isInfoEnabled() are false, significantly improving
      * debug performance without resorting the bulky:
      * 
      * <pre>
      * if (log.isDebugEnabled())
      *   log.debug(&quot;Foo&quot; + &quot; is the answer!&quot;);
      * </pre>
      * 
      * The new hotness looks like this:
      * 
      * <pre>
      * log.debug(&quot;Foo {}&quot;, &quot;is the answer!&quot;);
      * </pre>
      */
     public void info(final String message, final Object... replacementTokens) {
       if (!isInfoEnabled())
         return;
       genericLog(message, null, Priority.INFO, replacementTokens);
     }
 
     /**
      * Takes a message and an unspecified number of tokens to put into that
      * message, looking for the pattern "{}". For instance, logging something
      * like debug("Attempt {} of {}.",1,2) would put out "Attempt 1 of 2". If
      * you have an exception, put it after the message but before the
      * replacement tokens, ie error("Failed in attempt {}", ex, 2). The reason
      * to use this mechanism instead of "Failed in attempt "+2 (for instance) is
      * that the overhead of String concatenation is internally skipped if
      * isDebugEnabled() or isInfoEnabled() are false, significantly improving
      * debug performance without resorting the bulky:
      * 
      * <pre>
      * if (log.isDebugEnabled())
      *   log.debug(&quot;Foo&quot; + &quot; is the answer!&quot;);
      * </pre>
      * 
      * The new hotness looks like this:
      * 
      * <pre>
      * log.debug(&quot;Foo {}&quot;, &quot;is the answer!&quot;);
      * </pre>
      */
     public void info(final String message, final Throwable ex, final Object... replacementTokens) {
       if (!isInfoEnabled())
         return;
       genericLog(message, ex, Priority.INFO, replacementTokens);
     }
 
     /**
      * Takes a message and an unspecified number of tokens to put into that
      * message, looking for the pattern "{}". For instance, logging something
      * like debug("Attempt {} of {}.",1,2) would put out "Attempt 1 of 2". If
      * you have an exception, put it after the message but before the
      * replacement tokens, ie error("Failed in attempt {}", ex, 2). The reason
      * to use this mechanism instead of "Failed in attempt "+2 (for instance) is
      * that the overhead of String concatenation is internally skipped if
      * isDebugEnabled() or isInfoEnabled() are false, significantly improving
      * debug performance without resorting the bulky:
      * 
      * <pre>
      * if (log.isDebugEnabled())
      *   log.debug(&quot;Foo&quot; + &quot; is the answer!&quot;);
      * </pre>
      * 
      * The new hotness looks like this:
      * 
      * <pre>
      * log.debug(&quot;Foo {}&quot;, &quot;is the answer!&quot;);
      * </pre>
      */
     public void warn(final String message, final Object... replacementTokens) {
       genericLog(message, null, Priority.WARN, replacementTokens);
     }
 
     /**
      * Takes a message and an unspecified number of tokens to put into that
      * message, looking for the pattern "{}". For instance, logging something
      * like debug("Attempt {} of {}.",1,2) would put out "Attempt 1 of 2". If
      * you have an exception, put it after the message but before the
      * replacement tokens, ie error("Failed in attempt {}", ex, 2). The reason
      * to use this mechanism instead of "Failed in attempt "+2 (for instance) is
      * that the overhead of String concatenation is internally skipped if
      * isDebugEnabled() or isInfoEnabled() are false, significantly improving
      * debug performance without resorting the bulky:
      * 
      * <pre>
      * if (log.isDebugEnabled())
      *   log.debug(&quot;Foo&quot; + &quot; is the answer!&quot;);
      * </pre>
      * 
      * The new hotness looks like this:
      * 
      * <pre>
      * log.debug(&quot;Foo {}&quot;, &quot;is the answer!&quot;);
      * </pre>
      */
     public void warn(final String message, final Throwable ex, final Object... replacementTokens) {
       genericLog(message, ex, Priority.WARN, replacementTokens);
     }
 
     /**
      * Takes a message and an unspecified number of tokens to put into that
      * message, looking for the pattern "{}". For instance, logging something
      * like debug("Attempt {} of {}.",1,2) would put out "Attempt 1 of 2". If
      * you have an exception, put it after the message but before the
      * replacement tokens, ie error("Failed in attempt {}", ex, 2). The reason
      * to use this mechanism instead of "Failed in attempt "+2 (for instance) is
      * that the overhead of String concatenation is internally skipped if
      * isDebugEnabled() or isInfoEnabled() are false, significantly improving
      * debug performance without resorting the bulky:
      * 
      * <pre>
      * if (log.isDebugEnabled())
      *   log.debug(&quot;Foo&quot; + &quot; is the answer!&quot;);
      * </pre>
      * 
      * The new hotness looks like this:
      * 
      * <pre>
      * log.debug(&quot;Foo {}&quot;, &quot;is the answer!&quot;);
      * </pre>
      */
     public void error(final String message, final Object... replacementTokens) {
       genericLog(message, null, Priority.ERROR, replacementTokens);
     }
 
     /**
      * Takes a message and an unspecified number of tokens to put into that
      * message, looking for the pattern "{}". For instance, logging something
      * like debug("Attempt {} of {}.",1,2) would put out "Attempt 1 of 2". If
      * you have an exception, put it after the message but before the
      * replacement tokens, ie error("Failed in attempt {}", ex, 2). The reason
      * to use this mechanism instead of "Failed in attempt "+2 (for instance) is
      * that the overhead of String concatenation is internally skipped if
      * isDebugEnabled() or isInfoEnabled() are false, significantly improving
      * debug performance without resorting the bulky:
      * 
      * <pre>
      * if (log.isDebugEnabled())
      *   log.debug(&quot;Foo&quot; + &quot; is the answer!&quot;);
      * </pre>
      * 
      * The new hotness looks like this:
      * 
      * <pre>
      * log.debug(&quot;Foo {}&quot;, &quot;is the answer!&quot;);
      * </pre>
      */
     public void error(final String message, final Throwable ex, final Object... replacementTokens) {
       genericLog(message, ex, Priority.ERROR, replacementTokens);
     }
 
     /**
      * Takes a message and an unspecified number of tokens to put into that
      * message, looking for the pattern "{}". For instance, logging something
      * like debug("Attempt {} of {}.",1,2) would put out "Attempt 1 of 2". If
      * you have an exception, put it after the message but before the
      * replacement tokens, ie error("Failed in attempt {}", ex, 2). The reason
      * to use this mechanism instead of "Failed in attempt "+2 (for instance) is
      * that the overhead of String concatenation is internally skipped if
      * isDebugEnabled() or isInfoEnabled() are false, significantly improving
      * debug performance without resorting the bulky:
      * 
      * <pre>
      * if (log.isDebugEnabled())
      *   log.debug(&quot;Foo&quot; + &quot; is the answer!&quot;);
      * </pre>
      * 
      * The new hotness looks like this:
      * 
      * <pre>
      * log.debug(&quot;Foo {}&quot;, &quot;is the answer!&quot;);
      * </pre>
      */
     public void fatal(final String message, final Object... replacementTokens) {
       genericLog(message, null, Priority.FATAL, replacementTokens);
     }
 
     /**
      * Takes a message and an unspecified number of tokens to put into that
      * message, looking for the pattern "{}". For instance, logging something
      * like debug("Attempt {} of {}.",1,2) would put out "Attempt 1 of 2". If
      * you have an exception, put it after the message but before the
      * replacement tokens, ie error("Failed in attempt {}", ex, 2). The reason
      * to use this mechanism instead of "Failed in attempt "+2 (for instance) is
      * that the overhead of String concatenation is internally skipped if
      * isDebugEnabled() or isInfoEnabled() are false, significantly improving
      * debug performance without resorting the bulky:
      * 
      * <pre>
      * if (log.isDebugEnabled())
      *   log.debug(&quot;Foo&quot; + &quot; is the answer!&quot;);
      * </pre>
      * 
      * The new hotness looks like this:
      * 
      * <pre>
      * log.debug(&quot;Foo {}&quot;, &quot;is the answer!&quot;);
      * </pre>
      */
     public void fatal(final String message, final Throwable ex, final Object... replacementTokens) {
       genericLog(message, ex, Priority.FATAL, replacementTokens);
     }
   }
 
   private static final class SimpleErrorManager extends ErrorManager {
     @Override
     public synchronized void error(final String msg, final Exception ex, final int code) {
       System.out.println("ERROR: " + msg);
     }
   }
 
   public static final class LogFormatter extends Formatter {
     @Override
     public final String format(final LogRecord record) {
       final StringBuilder buffer = new StringBuilder(512);
       final StringBuilder header = new StringBuilder(100);
       header.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date(record.getMillis())));
       header.append(" [");
       header.append(Thread.currentThread().getName());
       header.append("] ");
       header.append(getDescriptionForLevel(record.getLevel()));
       header.append(" ");
       header.append(record.getLoggerName());
       if (Logging.logMethodNames) {
         header.append("#");
         header.append(getCallingMethodName());
         header.append("()");
       }
       header.append(" - ");
       buffer.append(header);
       buffer.append(record.getMessage());
       buffer.append("\n");
 
       // Write out the exception if there is one.
       if (record.getThrown() != null)
         try {
           final String headerString = "\n_____ " + header.toString();
           final StringWriter writer = new StringWriter();
           record.getThrown().printStackTrace(new PrintWriter(writer));
           String trace = "\n" + writer.toString();
           trace = replace(trace, "\n", headerString);
           // Strip off the first character because it's always a '\n'
           buffer.append(trace.substring(1));
           buffer.append("\n");
         }
         catch (final Throwable ex) {
           // Uh-oh!
           buffer.append("ERROR PRINTING STACK TRACE! " + ex.getMessage() + "\n");
         }
 
       return buffer.toString();
     }
 
     private String getCallingMethodName() {
       final StackTraceElement stack[] = (new Throwable()).getStackTrace();
       if (stack.length >= 10)
         return stack[9].getMethodName();
       return "unknown";
     }
 
     /**
      * Inlined from StringManipulator due to static init circular references,
      * called by format
      */
     private String replace(final String haystack, final String needle, final String replacement) {
       if (haystack == null || needle == null || replacement == null || needle.length() == 0)
         return haystack;
       final StringBuffer buf = new StringBuffer(haystack.length());
       int start = 0, end = 0;
       while ((end = haystack.indexOf(needle, start)) != -1) {
         buf.append(haystack.substring(start, end)).append(replacement);
         start = end + needle.length();
       }
       buf.append(haystack.substring(start));
       return buf.toString();
     }
 
     @Override
     public String getTail(final Handler handler) {
       if (!mute)
         return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date()) + " [SHUTDOWN]\n" + "************************************************************************\n"
             + "************************************************************************\n" + "                   JVM has begun shutdown procedures                    \n"
             + "************************************************************************\n" + "************************************************************************\n";
       return "";
     }
 
     private final String getDescriptionForLevel(final Level level) {
       // Put the three most likely levels first
       if (level == Level.FINE)
         return "DEBUG";
       if (level == Level.CONFIG)
         return "INFO";
       if (level == Level.INFO)
         return "WARN";
 
       // Less likely levels
       if (level == Level.FINEST)
         return "OBNOXIOUS";
       if (level == Level.FINER)
         return "VERBOSE";
       if (level == Level.WARNING)
         return "ERROR";
       if (level == Level.SEVERE)
         return "FATAL";
 
       // Otherwise return the real level name
       return level.getName();
     }
   }
 
   /**
    * This class has been added to wrap the older Log4j-style priorities into the
    * SDK-style Levels. The only way to access this class is to use the static
    * final objects.
    */
   public static final class Priority {
     /** Maps to {@link Level#FINEST} */
     public static final Priority OBNOXIOUS = new Priority(Level.FINEST);
     /** Maps to {@link Level#FINER} */
     public static final Priority VERBOSE   = new Priority(Level.FINER);
     /** Maps to {@link Level#FINE} */
     public static final Priority DEBUG     = new Priority(Level.FINE);
     /** Maps to {@link Level#CONFIG} */
     public static final Priority INFO      = new Priority(Level.CONFIG);
     /** Maps to {@link Level#INFO} */
     public static final Priority WARN      = new Priority(Level.INFO);
     /** Maps to {@link Level#WARNING} */
     public static final Priority ERROR     = new Priority(Level.WARNING);
     /** Maps to {@link Level#SEVERE} */
     public static final Priority FATAL     = new Priority(Level.SEVERE);
 
     /** Special priority, maps to {@link Level#ALL} */
     public static final Priority ALL       = new Priority(Level.ALL);
     /** Special priority, maps to {@link Level#OFF} */
     public static final Priority OFF       = new Priority(Level.OFF);
 
     private final Level          level;
 
     public Level getLevel() {
       return level;
     }
 
     private Priority(final Level level) {
       this.level = level;
     }
 
     @Override
     public String toString() {
       return level.getName();
     }
   }
 }
