 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.moria.configuration;
 
 import no.feide.moria.controller.MoriaController;
 import no.feide.moria.log.MessageLogger;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * ConfigurationManager watches configuration files and reloads them when the are changed.
  * The constructor requires the <code>no.feide.moria.configuration.cm</code> property to be
  * set, and the property has to point to the configuration file for the ConfigurationManager
  * module. The file can be referenced by either full file path or as a resource in the
  * classpath.<br/>
  * <br/>
  * The configuration file has to contain properties that points to the other modules
  * properties files. These files kan be referenced by either full file path or as a resource
  * in the classpath. The <code>no.feide.moria.configuration.fileListenerIntervalSeconds</code>
  * attribute specifies the interval between each file poll. <br/>
  * <p/>
  * <pre>
  * # Example content for ConfigurationManager properties
  * no.feide.moria.configuration.fileListenerIntervalSeconds=1
  * no.feide.moria.configuration.sm=/sm-test-valid.properties
  * no.feide.moria.configuration.dm=/dm-test-valid.properties
  * no.feide.moria.configuration.am=/am-data.xml
  * </pre>
  * <p/>
  * When a configuration file is changed the content is read into a properties object which
  * is sent to the MoriaController.
  *
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  * @see no.feide.moria.controller.MoriaController
  */
 public class ConfigurationManager {
 
     /** For logging events that does not throw exceptions to the layer above. */
     private MessageLogger messageLogger = new MessageLogger(ConfigurationManager.class);
 
     /**
      * Name of the Store module, used in configuration properties.
      */
     public static final String MODULE_SM = "sm";
 
     /**
      * Name of the Directory module, used in configuration properties.
      */
     public static final String MODULE_DM = "dm";
 
     /**
      * Name of the Authorization module, used in configuration properties.
      */
     public static final String MODULE_AM = "am";
 
     /**
      * Name of the Configuration module, used in configuration properties.
      */
     public static final String MODULE_CM = "base";
 
     /**
      * Name of the Web module, used in configuration properties.
      */
     public static final String MODULE_WEB = "web";
 
     /**
      * Attribute name for timer delay
      */
     private static final String TIMER_DELAY = "fileListenerIntervalSeconds";
 
     /**
      * Attribute name prefix for file name properties
      */
     private static final String PROPS_PREFIX = "no.feide.moria.configuration.";
 
     /**
      * List of the modules that have configuration to watch
      */
     private static final String[] NEEDS_LISTENER = new String[]{MODULE_SM, MODULE_DM, MODULE_AM, MODULE_WEB};
 
     /**
      * Timer for the configuration files
      */
     private Timer timer = new Timer(true);
 
     /**
      * Storage for all timers
      */
     private HashMap timerEntries = new HashMap();
 
     /**
      * Constructor. The constructor reads the ConfigurationManagers properties from file
      * (set by System.properties) and starts file listeners for all modules configuration
      * files.
      *
      * @throws ConfigurationManagerException if there are any problems with the configuration file
      */
     public ConfigurationManager() throws ConfigurationManagerException {
 
         /* Read configuration manager properties file */
         Properties cmProps = new Properties();
         String cmPropsFile = System.getProperty(PROPS_PREFIX + MODULE_CM);
         String filePrefix = new File(cmPropsFile).getParent() + System.getProperty("file.separator");
 
         if (cmPropsFile == null || cmPropsFile.equals("")) {
             throw new BaseConfigException("System property '" + PROPS_PREFIX + MODULE_CM + "' must be a non-empty string.");
         }
 
         /* Read configuration manager's properties file */
         try {
             cmProps = readProperties(cmPropsFile);
         } catch (FileNotFoundException e) {
             throw new ConfigurationManagerException("Configuration manager's configuration file not found: " + cmPropsFile);
         } catch (IOException e) {
             throw new ConfigurationManagerException("IOException while loading configuration managers properties file: " + cmPropsFile);
         }
 
         /* Timer delay */
         int timerDelay = 0;
         String timerDelayStr = cmProps.getProperty(PROPS_PREFIX + TIMER_DELAY);
         if (timerDelayStr == null || timerDelayStr.equals("")) {
             throw new ConfigurationManagerException("'" + PROPS_PREFIX + TIMER_DELAY + "' in configuration manager properties cannot be a null value.");
         }
         timerDelay = new Integer(timerDelayStr).intValue();
         if (timerDelay < 1) {
             throw new ConfigurationManagerException("'" + PROPS_PREFIX + TIMER_DELAY + "' in configuration manager properties must be >= 1.");
         }
 
         /* Create listener for every module config file */
         for (int i = 0; i < NEEDS_LISTENER.length; i++) {
             String module = NEEDS_LISTENER[i];
             String fileName = cmProps.getProperty(PROPS_PREFIX + module);
 
             try {
                addFileChangeListener(filePrefix + fileName, module, timerDelay);
             } catch (FileNotFoundException e) {
                 throw new ConfigurationManagerException("Unable to watch file, file not found: " + fileName);
             }
         }
     }
 
     /**
      * Remove all file listeners.
      */
     public void stop() {
         HashMap timers = new HashMap(timerEntries);
         for (Iterator it = timers.keySet().iterator(); it.hasNext();) {
             String entry = (String) it.next();
             removeFileChangeListener(entry);
         }
     }
 
     /**
      * Number of active file listeners. Basicly needed for testing.
      *
      * @return the number of active file listeners
      */
     int numFileListeners() {
         return timerEntries.size();
     }
 
     /**
      * Read properties from file. The fileURI can be absolute path to file or relative
      * to the classpath. If the fieleURI does not resolve to a readeble file, a
      * <code>ConfigurationManagerException</code> is thrown.
      *
      * @param fileURI the reference to the properties file
      * @return properties from the file
      * @throws FileNotFoundException if no file is found
      * @throws IOException           if something goes wrong during file read
      */
     private Properties readProperties(final String fileURI) throws FileNotFoundException, IOException {
         Properties props = new Properties();
         File file = null;
 
         /* Validate parameter */
         if (fileURI == null || fileURI.equals("")) {
             throw new IllegalArgumentException("URI to properties file must be a non-empty string.");
         }
 
         /* Read properties file */
         file = fileForURI(fileURI);
         props.load(new FileInputStream(file));
 
         return props;
     }
 
     /**
      * Monitor a file. A new file listener is started for the modules properties file. If
      * the file cannot be read, a <code>FileNotFoundException</code> is thrown.
      *
      * @param fileName    full path or relative (classpath) path to the properties file
      * @param module      the module the configuration file belongs to
      * @param intervalSec polling period in seconds
      * @throws FileNotFoundException if the file is not found
      */
     private void addFileChangeListener(final String fileName, final String module, final int intervalSec)
             throws FileNotFoundException {
         removeFileChangeListener(fileName);
         long delay = intervalSec * 1000;
 
         FileListenerTask task = new FileListenerTask(fileName, module);
         timerEntries.put(fileName, task);
         timer.schedule(task, delay, delay);
     }
 
     /**
      * Stop monitoring of file.
      *
      * @param fileName the file name to stop monitoring
      */
     private void removeFileChangeListener(final String fileName) {
         FileListenerTask task = (FileListenerTask) timerEntries.remove(fileName);
         if (task != null) {
             task.cancel();
         }
     }
 
     /**
      * Send changed configuration to <code>MoriaController</code>.
      *
      * @param module            the module the configuration file belongs to
      * @param configurationFile a <code>File</code> object representing the changed file
      * @see no.feide.moria.controller.MoriaController#setConfig
      */
     private void fileChangeEvent(final String module, final File configurationFile) {
         Properties props = null;
 
         try {
 
             /* Authorization database */
             if (module.equals(MODULE_AM)) {
                 props = new Properties();
                 props.put("authorizationDatabase", configurationFile.getAbsolutePath());
             }
 
             /* Other (normal) configuration files */
             else {
                 props = readProperties(configurationFile.getAbsolutePath());
             }
 
         } catch (FileNotFoundException e) {
             props = null;
             messageLogger.logCritical("Watched file disappeared from the file system, fileChangeEvent cancelled. File: " + configurationFile.getAbsolutePath());
         } catch (IOException e) {
             props = null;
             messageLogger.logCritical("IOException during reading of authorization database, fileChangeEvent cancelled. File: " + configurationFile.getAbsolutePath());
         }
 
         if (props != null) {
             MoriaController.setConfig(module, props);
         } else {
             messageLogger.logCritical("Unable to create properties from file: " + configurationFile);
         }
     }
 
     /**
      * Resolves a fileURI to a <code>File</code> object.
      *
      * @param fileURI reference to the file (full path or relative within the classpath)
      * @return a <code>File</code> object referenced by the fileURI
      * @throws FileNotFoundException if the fileURI cannot be resolved to a readable file
      */
     private File fileForURI(final String fileURI) throws FileNotFoundException {
 
         if (fileURI == null || fileURI.equals("")) {
             throw new FileNotFoundException("File reference cannot be null.");
         }
 
         File file = new File(fileURI);
         return file;
     }
 
     /**
      * This class is used to monitor the configuration files. An instance of this class
      * is created for every file to watch. The work is done by the run() method which
      * is called by the timer.
      */
     class FileListenerTask extends TimerTask {
 
         /**
          * The module that the configuration file belongs to
          */
         private String module;
 
         /**
          * The file object representation of the file that is beeing watched
          */
         private File monitoredFile;
 
         /**
          * Last modification of the watched file
          */
         private long lastModified;
 
         /**
          * Constructor.
          *
          * @param fileURI the URI for the file to watch
          * @param module  the module the file belongs to
          * @throws FileNotFoundException if the file does not exist
          */
         public FileListenerTask(final String fileURI, final String module) throws FileNotFoundException {
             monitoredFile = fileForURI(fileURI);
             fileChangeEvent(module, monitoredFile);
             this.lastModified = 0;
             this.module = module;
             this.lastModified = monitoredFile.lastModified();
         }
 
         /**
          * Called by the timer. If the file has changed the fileChangeEvent() is called.
          *
          * @see ConfigurationManager#fileChangeEvent(String, File)
          */
         public final void run() {
             long lastModified = monitoredFile.lastModified();
             if (lastModified != this.lastModified) {
                 this.lastModified = lastModified;
                 fileChangeEvent(this.module, this.monitoredFile);
 
             }
         }
     }
 }
