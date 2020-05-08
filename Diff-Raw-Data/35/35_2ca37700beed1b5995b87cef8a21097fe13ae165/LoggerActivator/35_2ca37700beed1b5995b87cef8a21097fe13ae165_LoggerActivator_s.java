 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names
  *    of its contributors may be used to endorse or promote products derived from
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 28th December 2009
  */
 
 package au.edu.uts.eng.remotelabs.schedserver.logger;
 
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.util.tracker.ServiceTracker;
 
 import au.edu.uts.eng.remotelabs.schedserver.config.Config;
 import au.edu.uts.eng.remotelabs.schedserver.logger.impl.FileLogger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.impl.RolledFileLogger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.impl.SyslogLogger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.impl.SystemErrLogger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.impl.WinEventsLogger;
 
 /**
  * Activator for the logger class which sets the logger singleton.
  */
 public class LoggerActivator implements BundleActivator
 {
     /**
      * Types of loggers.
      */
     public enum LoggerType
     {
         SYSTEM_ERROR, FILE, ROLLED_FILE, SYSLOG, WINEVENTS
     }
 
     /** Logger bundle configuration bundle. */
     private static final Dictionary<String, String> loggerProperties = new Hashtable<String, String>();
     
     /** Configuration confService tracker. */
     private ServiceTracker confService;
     
     /** Logger object. */
     private static Logger logger = null;
     
     /** Logger confService registration. */
     private ServiceRegistration reg;
 
     /* Sets the configuration defaults. */
     static
     {
         /* Common configuration. */
         LoggerActivator.loggerProperties.put("Logger_Type", "File");
         LoggerActivator.loggerProperties.put("Log_Level", "INFO");
         LoggerActivator.loggerProperties.put("Default_Log_Format", "[__LEVEL__] - [__ISO8601__] - __MESSAGE__");
 
         /* File and rolled file configuration. */
         LoggerActivator.loggerProperties.put("Log_File_Name", "SchedServer.log");
         LoggerActivator.loggerProperties.put("Log_File_Max_Size", "10");
         LoggerActivator.loggerProperties.put("Log_File_Backups", "5");
         
         /* Syslog logger configuration. */
         LoggerActivator.loggerProperties.put("Syslog_Host", "127.0.0.1");
         LoggerActivator.loggerProperties.put("Syslog_Local_Facility_Num", "1");
     }
 
     @Override
     public void start(final BundleContext context) throws Exception
     {
         ServiceReference ref = context.getServiceReference(Config.class.getName());
         
         if (ref == null)
         {
             /* Configuration confService not running, so attempt to start it. */
             Bundle[] bundles = context.getBundles();
             for (Bundle b : bundles)
             {
                 if (b.getSymbolicName().equals("SchedulingServer-Configuration") && b.getState() == Bundle.INSTALLED
                         || b.getState() == Bundle.RESOLVED) 
                 {
                     b.start();
                 }
             }
             ref = context.getServiceReference(Config.class.getName());
         }
         
         if (ref != null)
         {
             /* Load all configuration properties. */
             this.confService = new ServiceTracker(context, ref, null);
             this.confService.open();
            Config conf = (Config)this.confService.getService();
             
             /* Common configuration. */
             LoggerActivator.loggerProperties.put("Logger_Type", conf.getProperty("Logger_Type", "File"));
             LoggerActivator.loggerProperties.put("Log_Level", conf.getProperty("Log_Level", "INFO"));
             LoggerActivator.loggerProperties.put("Default_Log_Format", 
                     conf.getProperty("Default_Log_Format", "[__LEVEL__] - [__ISO8601__] - __MESSAGE__"));
 
             /* File and rolled file configuration. */
             LoggerActivator.loggerProperties.put("Log_File_Name", 
                     conf.getProperty("Log_File_Name", "SchedServer.log"));
             LoggerActivator.loggerProperties.put("Log_File_Max_Size", conf.getProperty("Log_File_Max_Size", "10"));
             LoggerActivator.loggerProperties.put("Log_File_Backups", conf.getProperty("Log_File_Backups", "5"));
             
             /* Syslog logger configuration. */
             LoggerActivator.loggerProperties.put("Syslog_Host", conf.getProperty("Syslog_Host", "127.0.0.1"));
             LoggerActivator.loggerProperties.put("Syslog_Local_Facility_Num", 
                     conf.getProperty("Syslog_Local_Facility_Num", "1"));
         }
         else
         {
             System.err.println("Unable to load logger configuration, so using defaults.");
         }
 
         /* Load up logger type. */
         switch (LoggerActivator.getLoggerType())
         {
             case FILE:
                 LoggerActivator.logger = new FileLogger();
                 break;
             case ROLLED_FILE:
                 LoggerActivator.logger = new RolledFileLogger();
                 break;
             case SYSLOG:
                 LoggerActivator.logger = new SyslogLogger();
                 break;
             case SYSTEM_ERROR:
                 LoggerActivator.logger = new SystemErrLogger();
                 break;
             case WINEVENTS:
                 LoggerActivator.logger = new WinEventsLogger();
                 break;
         }
         
         /* Register a logger confService. */        
         this.reg  = context.registerService(Logger.class.getName(), LoggerActivator.logger, 
                 LoggerActivator.loggerProperties);        
     }
 
     @Override
     public void stop(final BundleContext context) throws Exception
     {
         this.confService.close();
         this.reg.unregister();
     }
 
     /**
      * Returns the configured type. If the configuration <code>Logger_Type</code>
      * fails to be loaded, the standard error stream logger is used as the
      * default.
      * 
      * @return LoggerType enumeration value
      */
     public static LoggerType getLoggerType()
     {
         final String type = LoggerActivator.loggerProperties.get("Logger_Type");
 
         if (type == null)
         {
             System.err.println("FATAL: Failed loading configuration of logger type (Logger_Type).");
             System.err.println("This specifies the destination of logging messages and is needed for correct" +
             " operation of the rig client.");
             System.err.println("Shutting down...");
             System.exit(-1);
         }
 
         if (type.equalsIgnoreCase("SystemErr"))
         {
             return LoggerType.SYSTEM_ERROR;
         }
         else if (type.equalsIgnoreCase("File"))
         {
             return LoggerType.FILE;
         }
         else if (type.equalsIgnoreCase("RolledFile"))
         {
             return LoggerType.ROLLED_FILE;
         }
         else if (type.equalsIgnoreCase("Syslog"))
         {
             return LoggerType.SYSLOG;
         }
         else if (type.equalsIgnoreCase("WinEvents"))
         {
             return LoggerType.WINEVENTS;
         }
         else
         {
             return LoggerType.SYSTEM_ERROR;
         }
     }
 
     /**
      * Returns the configured logging level. If the configured value fails
      * to be loaded, </code>Logger.DEBUG</code> is returned as the default.
      * 
      * @return logging level
      */
     public static short getLoggingLevel()
     {
         final String type = LoggerActivator.loggerProperties.get("Log_Level");
         if (type == null)
         {
             System.err
                     .println("FATAL: Failed loading configuration of logging level (Log_Level).");
             System.err
                     .println("This specifies the level of logging messages to provide and is needed for correct"
                             + " operation of the rig client.");
             System.err.println("Shutting down...");
             System.exit(-1);
         }
 
         if (type.equalsIgnoreCase("ERROR"))
         {
             return Logger.ERROR;
         }
         else if (type.equalsIgnoreCase("WARN"))
         {
             return Logger.WARN;
         }
         else if (type.equalsIgnoreCase("INFO"))
         {
             return Logger.INFO;
         }
         else
         {
             return Logger.DEBUG;
         }
     }
 
     /**
      * Returns the configured formatting strings for each of the log types.
      * 
      * @return formatting strings
      */
     public static Map<Integer, String> getFormatStrings()
     {
         final Map<Integer, String> frmStrings = new HashMap<Integer, String>();
         final String def = LoggerActivator.loggerProperties.get("Default_Log_Format");
        String frm = LoggerActivator.loggerProperties.get("FATAL_Log_Format");
         frmStrings.put(Logger.FATAL, frm == null ? def : frm);
 
        frm = LoggerActivator.loggerProperties.get("PRIORITY_Log_Format");
         frmStrings.put(Logger.PRIORITY, frm == null ? def : frm);
 
        frm = LoggerActivator.loggerProperties.get("ERROR_Log_Format");
         frmStrings.put(Logger.ERROR, frm == null ? def : frm);
 
        frm = LoggerActivator.loggerProperties.get("WARN_Log_Format");
         frmStrings.put(Logger.WARN, frm == null ? def : frm);
 
        frm = LoggerActivator.loggerProperties.get("INFO_Log_Format");
         frmStrings.put(Logger.INFO, frm == null ? def : frm);
 
        frm = LoggerActivator.loggerProperties.get("DEBUG_Log_Format");
         frmStrings.put(Logger.DEBUG, frm == null ? def : frm);
 
         return frmStrings;
     }
 
     /**
      * Gets a logging configuration property.
      * 
      * @param confName configuration property
      * @return configuration value or null if not found
      */
     public static String getProperty(final String confName)
     {
         return LoggerActivator.loggerProperties.get(confName);
     }
     
     /**
      * Gets a logger.
      * 
      * @return logger
      */
     public static Logger getLogger()
     {
         return LoggerActivator.logger;
     }
 }
