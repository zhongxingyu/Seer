 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: LogManager.java,v 1.3 2007-03-16 18:44:04 bigfatrat Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.log;
 
 import java.io.IOException;
 import java.io.File;
 import java.lang.SecurityException;
 import java.lang.reflect.Constructor;
 import java.net.URL;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.Enumeration;
 import java.util.StringTokenizer;
 import java.util.logging.Handler;
 import java.util.HashSet;
 import java.util.logging.Formatter;
 
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.naming.WebtopNaming;
 import com.sun.identity.log.spi.Debug;
 import com.sun.identity.log.s1is.LogConfigReader;
 
 /**
  * This class keeps track of all the logger objects and does all the
  * bookkeeping work. It is extended from JDK's <code>LogManager</code> to add
  * functionalities, such as adding our logger listening to DS changes, etc.
  * @supported.all.api
  */
 
 public class LogManager extends java.util.logging.LogManager {
 
     /**
      * Is the Log Service running locally or remotely
      */
     public static boolean isLocal = false;
 
     /**
      * The handler which will be added to each logger object
      */
     public static String HANDLER = "Handler";
 
     /**
      * The formatter which depends on the log settings
      */
     public static String FORMATTER = "Formatter";
 
     /**
      * Indicator for having set Monitoring Status
      */
     public static boolean setMonitoringStatus;
 
     /**
      * Adds a logger to the Log Manager.
      *
      * @param logger Logger object to be added to the Log Manager.
      * @return true if the logger is added.
      */
     public boolean addLogger(java.util.logging.Logger logger) {
         String name = logger.getName();
         /* we have to pass root logger and global logger */
         if (name != null && name.length() != 0 && !name.equals("global")) {
             /* we have to take care of the resourcebundle logger may have */
             String rbName = logger.getResourceBundleName();
             logger = new Logger(name,rbName);
         }
         return super.addLogger(logger);
     }
 
     /* security status updated by readConfigruation */
     private boolean securityStatus = false;
 
     /* all fields read during read configuration */
     private String[] allFields;
 
     private Set selectedFieldSet;
 
     protected Level loggingLevel = null;
 
     /**
      * Return whether secure logging is specified.
      *
      * @return <code>securityStatus</code>
      */
     public final boolean isSecure() {
         return securityStatus;
     }
 
     /**
      * Return the array of all LogRecord fields available for selection.
      *
      * @return <code>allFields</code>
      */
     public final String[] getAllFields() {
         return allFields;
     }
 
     /**
      * Return the LogRecord fields selected to be included.
      *
      * @return <code>selectedFieldSet</code>
      */
     public final Set getSelectedFieldSet() {
         return selectedFieldSet;
     }
 
     private final void readAllFields() {
         String strAllFields = getProperty(LogConstants.ALL_FIELDS);
         StringTokenizer strToken = new StringTokenizer(strAllFields, ", ");
         int count = strToken.countTokens();
         String localAllFields[] = new String[count];
         count = 0;
         while(strToken.hasMoreElements()) {
             localAllFields[count++] = strToken.nextToken().trim();
         }
         allFields = localAllFields;
     }
 
     private final void readSelectedFieldSet() {
         HashSet fieldSet = new HashSet();
 
         String strSelectedFields = getProperty(LogConstants.LOG_FIELDS);
 
         if ((strSelectedFields != null) && (strSelectedFields.length() != 0))
         {
             StringTokenizer stoken =
                 new StringTokenizer(strSelectedFields, ", ");
 
             while(stoken.hasMoreElements()) {
                 fieldSet.add(stoken.nextToken());
             }
         }
         selectedFieldSet = fieldSet;
         return;
     }
 
     /**
      * This method overrides the <code>readConfiguration</code> method in
      * JDK <code>LogManager</code> class.
      * The base class method resets the loggers in memory. This method
      * must add handlers to the loggers in memory according to the
      * new configuration.
      *
      * @throws IOException if there are IO problems reading the configuration.
      * @throws SecurityException if a security manager exists and if the caller
      * does not have <code>LoggingPermission("control")</code>.
      */
     public final void readConfiguration()
         throws IOException, SecurityException
     {
         try {
             /*
              * This writeLock ensures that no logging threads will execute
              * a logger.log call after this point since they request for
              * a readLock.
              */
             Logger.rwLock.writeRequest();
 
             /*
              * This sync is for avoiding this thread snathing away
              * time slice from a thread executing getLogger() method
              * which is also sync on Logger.class
              * which may lead to two handlers being added to the same logger.
              */
             synchronized (Logger.class) {
                 Enumeration loggerNames = getLoggerNames();
                 String oldLocation = getProperty(LogConstants.LOG_LOCATION);
                 LogManagerUtil.setupEnv();
                 try {
                     /*
                      * This change is done for deploying AM as a single
                      * war. In server mode we will always use our
                      * LogConfigReader. On the client side the
                      * the JVM property will define whether to use the
                      * LogConfigReader or the remote handlers. If no
                      * JVM property is set, the remote handlers will
                      * be used.
                      */
                     if (WebtopNaming.isServerMode()) {
                         LogConfigReader logConfigReader =
                             new LogConfigReader();
                     } else {
                         super.readConfiguration();
                     }
                 } catch(Exception ex) {
                     /* no debug since our debugging system is not up. */
                 } finally {
                     LogManagerUtil.resetEnv();
                 }
 
                 if (isLocal) {
                     securityStatus = false;
                     readAllFields();
                     readSelectedFieldSet();
 
                     if (getProperty(LogConstants.BACKEND).equals("DB")) {
                         HANDLER = getProperty(LogConstants.DB_HANDLER);
                         FORMATTER = getProperty(LogConstants.DB_FORMATTER);
                         String driver = getProperty(LogConstants.DB_DRIVER);
                     } else if (getProperty(LogConstants.SECURITY_STATUS)
                         .equalsIgnoreCase("ON"))
                     {
                         securityStatus = true;
                         HANDLER = getProperty(LogConstants.SECURE_FILE_HANDLER);
                         FORMATTER =
                             getProperty(LogConstants.SECURE_ELF_FORMATTER);
                     } else {
                         HANDLER = getProperty(LogConstants.FILE_HANDLER);
                         FORMATTER = getProperty(LogConstants.ELF_FORMATTER);
                     }
                     if (getProperty(LogConstants.BACKEND).equals("File")) {
                         /*
                          * create new log directory if it has changed and
                          * the new directory does not exist.
                          */
                         String newLocation = getProperty(
                             LogConstants.LOG_LOCATION);
 
                         if ((newLocation != null) &&
                             (oldLocation != null) &&
                             !oldLocation.equals(newLocation))
                         {
                             File dir = new File(newLocation);
                             if (!dir.exists()) {
                                 if (!dir.mkdirs()) {
                                     Debug.error(
                                     "LogManager:readConfiguration:" +
                                     "Unable to create the new log directory." +
                                     " Verify that the process has necessary" +
                                     " permissions");
                                 }
                             }
                         }
                     }
 
                     String strLogLevel =
                         getProperty(LogConstants.LOGGING_LEVEL);
                     try {
                         loggingLevel = Level.parse(strLogLevel);
                     } catch (IllegalArgumentException iaex) {
                         loggingLevel = Level.INFO;  // default
                         Debug.error("LogManager:readConfiguration:" +
                             "Log level '" + strLogLevel +
                             "' unknown; setting to Level.INFO.");
                     }
                 } else {
                     HANDLER = getProperty(LogConstants.REMOTE_HANDLER);
                     if (HANDLER == null) {
                         HANDLER = LogConstants.DEFAULT_REMOTE_HANDER;
                     }
                     if (FORMATTER == null) {
                         FORMATTER = LogConstants.DEFAULT_REMOTE_FORMATTER;
                     }
                 }
 
                 /*
                  * modify existing loggers in memory according
                  * to the new configuration
                  */
                 loggerNames = getLoggerNames();
                 
                 while (loggerNames.hasMoreElements()) {
                     String curEl = (String)loggerNames.nextElement();
                     /* avoid root logger */
                     if (curEl.length() != 0 && curEl.length() != 0 &&
                         !curEl.equals("global"))
                     {
                         if (Debug.messageEnabled()) {
                             Debug.message(
                                 "LogManager:readConfiguration:" +
                                 "Processing Logger: " + curEl);
                         }
 
                         /*
                          * remove all handlers and add new handlers for
                          * this logger
                          */
                         Logger l = (Logger)Logger.getLogger(curEl);
                         String handlerClass = LogManager.HANDLER;
                         Class clz = null;
                         Class [] parameters = {String.class};
                         Object [] parameterObjects = {new String(l.getName())};
                         Constructor cons =null;
                         Handler h = null;
                         try {
                             clz = Class.forName(handlerClass);
                         } catch (Exception e) {
                             Debug.error(
                                 "LogManager.readConfiguration:could not load "
                                 + handlerClass, e);
                         }
                         try {
                             cons = clz.getDeclaredConstructor(parameters);
                         } catch (Exception e) {
                             Debug.error(
                                 "LogManager.readConfiguration:could not" +
                                 " instantiate" + handlerClass, e);
                         }
                         try {
                             h = (Handler)cons.newInstance(parameterObjects);
                         } catch (Exception e) {
                             Debug.error(
                                 "LogManager.readConfiguration:could not" +
                                 " instantiate" + handlerClass, e);
                         }
                         String formatterClass = LogManager.FORMATTER;
                         Formatter f = null;
                         try {
                             f = (Formatter)Class.forName(formatterClass).
                                 newInstance();
                         } catch (Exception e) {
                             Debug.error(
                                 "LogManager.readConfiguration:could not" +
                                 " instantiate Formatter " + formatterClass, e);
                         }
                         h.setFormatter(f);
                         l.addHandler(h);
 
                         String levelProp = LogConstants.LOG_PROP_PREFIX + "." +
                             l.getName() + ".level";
                         String lvlStr = SystemProperties.get (levelProp);
                         Level tlevel = loggingLevel;
 
                         if ((lvlStr != null) && (lvlStr.length() > 0)) {
                             try {
                                 tlevel = Level.parse(lvlStr);
                             } catch (IllegalArgumentException iaex) {
                                 // use value for all others
                             }
                         }
                         if (loggingLevel != null) {  // only if isLocal
                             // update logging level
                             l.setLevel(tlevel);
                         }
                     } /* end of avoid rootlogger */
                 } /* end of while(loggerNames.hasMoreElements) */
             } /* end of synchronized(Logger.class) */
         } finally {
             Logger.rwLock.writeDone();
         }
     } /* end of readConfiguration() */
 }
