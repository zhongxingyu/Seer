 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.servlet.ServletConfig;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.helpers.NullEnumeration;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.PropertiesPropertyReader;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderUtils;
 import org.xins.common.collections.StatsPropertyReader;
 import org.xins.common.collections.UniqueProperties;
 import org.xins.common.io.FileWatcher;
 import org.xins.common.io.HTTPFileWatcher;
 import org.xins.common.text.TextUtils;
 import org.xins.logdoc.LogCentral;
 import org.xins.logdoc.UnsupportedLocaleException;
 
 /**
  * Manager for the runtime configuration file. Owns the watcher for the config
  * file and is responsible for triggering actions when the file has actually
  * changed.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:mees.witteman@orange-ftgroup.com">Mees Witteman</a>
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 final class ConfigManager {
 
    /**
     * The name of the system property that specifies the location of the
     * configuration file.
     */
    static final String CONFIG_FILE_SYSTEM_PROPERTY =
       "org.xins.server.config";
 
    /**
     * The name of the runtime property that specifies the interval
     * for the configuration file modification checks, in seconds.
     */
    static final String CONFIG_RELOAD_INTERVAL_PROPERTY =
       "org.xins.server.config.reload";
 
    /**
     * The name of the runtime property that specifies the list of runtime
     * properties file to include. The paths must be relative to the
     * current config file.
     */
    static final String CONFIG_INCLUDE_PROPERTY =
       "org.xins.server.config.include";
 
    /**
     * The default configuration file modification check interval, in seconds.
     */
    static final int DEFAULT_CONFIG_RELOAD_INTERVAL = 60;
 
    // XXX: Consider adding state checking
    /**
     * The object to synchronize on when reading and initializing from the
     * runtime configuration file.
     */
    private static final Object RUNTIME_PROPERTIES_LOCK = new Object();
 
    /**
     * The <code>Engine</code> that owns this <code>ConfigManager</code>. Never
     * <code>null</code>.
     */
    private final Engine _engine;
 
    /**
     * Servlet configuration. Never <code>null</code>.
     */
    private final ServletConfig _config;
 
    /**
     * The listener that is notified when the configuration file changes. Only
     * one instance is created ever.
     */
    private final ConfigurationFileListener _configFileListener;
 
    /**
     * The name of the runtime configuration file. Initially <code>null</code>.
     */
    private String _configFile;
 
    /**
     * The name of the all runtime configuration files included in the main config file. 
     * Can be <code>null</code> or empty.
     */
    private String[] _configFiles;
 
    /**
     * The String representation of the config files. Initialy <code>null</code>.
     */
    private String _configFilesPath;
 
    /**
     * Watcher for the runtime configuration file. Initially <code>null</code>.
     */
    private FileWatcher _configFileWatcher;
 
    /**
     * The set of properties read from the runtime configuration file. Never
     * <code>null</code>.
     */
    private StatsPropertyReader _runtimeProperties;
 
    /**
     * Flag indicating that the runtime properties were read correcly.
     */
    private boolean _propertiesRead;
 
    /**
     * Constructs a new <code>ConfigManager</code> object.
     *
     * @param engine
     *    the {@link Engine} that owns this <code>ConfigManager</code>, cannot
     *    be <code>null</code>.
     *
     * @param config
     *    the servlet configuration, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>engine == null || config == null</code>.
     */
    ConfigManager(Engine engine, ServletConfig config)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("engine", engine, "config", config);
 
       // Initialize fields
       _engine             = engine;
       _config             = config;
       _configFileListener = new ConfigurationFileListener();
    }
 
    /**
     * Initializes the logging subsystem with fallback default settings.
     */
    static void configureLoggerFallback() {
 
       Properties settings = new Properties();
 
       // Send all log messages to the logger named 'console'
       settings.setProperty("log4j.rootLogger", "ALL, console");
 
       // Define an appender for the console
       settings.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
 
       // Use a pattern-layout for the appender
       settings.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
 
       // Define the pattern for the appender
       settings.setProperty("log4j.appender.console.layout.ConversionPattern", "%6c{1} %-6p %x %m%n");
 
       // Do not show the debug logs produced by XINS.
       settings.setProperty("log4j.logger.org.xins.", "INFO");
 
       // Perform Log4J configuration
       PropertyConfigurator.configure(settings);
    }
 
    /**
     * Determines the name of the runtime configuration file. The system
     * properties will be queried first. If they do not provide it, then the
     * servlet initialization properties are tried. Once determined, the name
     * will be stored internally.
     */
    void determineConfigFile() {
 
       // Get the value of the appropriate system property
       String configFile = null;
       try {
          configFile = System.getProperty(CONFIG_FILE_SYSTEM_PROPERTY);
       } catch (SecurityException exception) {
          Log.log_3230(exception, CONFIG_FILE_SYSTEM_PROPERTY);
       }
 
       // If the name of the configuration file is not set in a system property
       // (typically passed on the command-line) try to get it from the servlet
       // initialization properties (typically set in a web.xml file)
       if (configFile == null || configFile.length() < 1) {
          Log.log_3231(CONFIG_FILE_SYSTEM_PROPERTY);
          configFile = _config.getInitParameter(CONFIG_FILE_SYSTEM_PROPERTY);
       }
 
       // Store the name of the configuration file
       _configFile = configFile;
    }
 
    /**
     * Unifies the file separator character on the _configFile property and then
     * reads the runtime properties file, initializes the logging subsystem
     * with the read properties and then stores those properties on the engine.
     * If the _configFile is empty, then an empty set of properties is set on
     * the engine.
     */
    void readRuntimeProperties() {
 
       UniqueProperties properties = new UniqueProperties();
       InputStream in = null;
 
       // If the value is not set only localhost can access the API.
       // NOTE: Don't trim the configuration file name, since it may start
       //       with a space or other whitespace character.
       if (_configFile == null) {
 
          // Try to find a xins.properties file in the WEB-INF directory
          in = _engine.getResourceAsStream("/WEB-INF/xins.properties");
          if (in == null) {
 
             // Use the default settings
             Log.log_3205(CONFIG_FILE_SYSTEM_PROPERTY);
             _runtimeProperties = null;
             _propertiesRead = true;
             return;
          } else {
             Log.log_3248();
          }
 
       } else {
 
          // Unify the file separator character
          _configFile = _configFile.replace('/',  File.separatorChar);
          _configFile = _configFile.replace('\\', File.separatorChar);
       }
 
       boolean propertiesRead = false;
       _configFilesPath = _configFile;
 
       synchronized (ConfigManager.RUNTIME_PROPERTIES_LOCK) {
 
          try {
             if (in != null) {
                properties.load(in);
                in.close();
             } else if (!_configFile.startsWith("http://") && !_configFile.startsWith("https://")) {
                properties = readLocalRuntimeProperties();
             } else {
                properties = readHTTPRuntimeProperties();
             }
             propertiesRead = true;
 
          // Security issue
          } catch (SecurityException exception) {
             Log.log_3302(exception, _configFilesPath);
 
          // No such file
          } catch (FileNotFoundException exception) {
             String detail = TextUtils.trim(exception.getMessage(), null);
             Log.log_3301(_configFilesPath, detail);
 
          // Other I/O error
          } catch (IOException exception) {
             Log.log_3303(exception, _configFilesPath);
          }
 
          // Initialize the logging subsystem
          Log.log_3300(_configFilesPath);
 
          // Attempt to configure Log4J
          configureLogger(properties);
 
          if (!properties.isUnique()) {
             Log.log_3311(_configFilesPath);
             propertiesRead = false;
          }
 
          if (propertiesRead) {
 
             // Convert to a PropertyReader
             PropertyReader pr = new PropertiesPropertyReader(properties);
             _runtimeProperties = new StatsPropertyReader(pr);
          }
          _propertiesRead = propertiesRead;
       }
    }
 
    /**
     * Read the runtime properties files when files are specified locally.
     * 
     * @throws IOException
     *    if the file cannot be found or be read.
     */
    private UniqueProperties readLocalRuntimeProperties() throws IOException {
       UniqueProperties properties = new UniqueProperties();
       InputStream in = null;
       try {
 
          // Open file, load properties, close file
          in = new FileInputStream(_configFile);
          properties.load(in);
 
          // Read the included files
          if (properties.getProperty(CONFIG_INCLUDE_PROPERTY) != null &&
               !properties.getProperty(CONFIG_INCLUDE_PROPERTY).trim().equals("")) {
             StringTokenizer stInclude = new StringTokenizer(properties.getProperty(CONFIG_INCLUDE_PROPERTY), ",");
             File baseFile = new File(_configFile).getParentFile();
             _configFiles = new String[stInclude.countTokens() + 1];
             _configFiles[0] = _configFile;
             _configFilesPath += "+ [";
             int i = 0;
             while (stInclude.hasMoreTokens()) {
                String nextInclude = stInclude.nextToken().trim().replace('/', File.separatorChar).replace('\\',  File.separatorChar);
                File includeFile = new File(baseFile, nextInclude);
                FileInputStream isInclude = new FileInputStream(includeFile);
                properties.load(isInclude);
                isInclude.close();
                _configFiles[i + 1] = nextInclude;
                _configFilesPath += nextInclude + ";";
                i++;
             }
             _configFilesPath += "]";
          } else {
             _configFiles = new String[1];
             _configFiles[0] = _configFile;
          }
 
       // Always close the input stream
       } finally {
          if (in != null) {
             try {
                in.close();
             } catch (Throwable exception) {
                Utils.logIgnoredException(exception);
             }
          }
       }
       return properties;
    }
 
    /**
     * Read the runtime properties files when files are specified locally.
     * 
     * @throws IOException
     *    if the URL cannot be created or if the connection to the URL failed.
     */
    private UniqueProperties readHTTPRuntimeProperties() throws IOException {
       UniqueProperties properties = new UniqueProperties();
       InputStream in = null;
       try {
 
          // Open file, load properties, close file
          URL configURL = new URL(_configFile);
          in = configURL.openStream();
          properties.load(in);
 
          // Read the included files
          if (properties.getProperty(CONFIG_INCLUDE_PROPERTY) != null &&
               !properties.getProperty(CONFIG_INCLUDE_PROPERTY).trim().equals("")) {
             StringTokenizer stInclude = new StringTokenizer(properties.getProperty(CONFIG_INCLUDE_PROPERTY), ",");
             _configFiles = new String[stInclude.countTokens() + 1];
             _configFiles[0] = _configFile;
             _configFilesPath += "+ [";
             int i = 0;
             while (stInclude.hasMoreTokens()) {
                String nextInclude = stInclude.nextToken().trim().replace('/', File.separatorChar).replace('\\',  File.separatorChar);
                URL includeFile = new URL(configURL, nextInclude);
                InputStream isInclude = includeFile.openStream();
                properties.load(isInclude);
                isInclude.close();
                _configFiles[i + 1] = nextInclude;
                _configFilesPath += nextInclude + ";";
                i++;
             }
             _configFilesPath += "]";
          } else {
             _configFiles = new String[1];
             _configFiles[0] = _configFile;
          }
 
       // Always close the input stream
       } finally {
          if (in != null) {
             try {
                in.close();
             } catch (Throwable exception) {
                Utils.logIgnoredException(exception);
             }
          }
       }
       return properties;
    }
 
    /**
     * Gets the runtime properties.
     *
     * @return
     *    the runtime properties, never <code>null</code>.
     */
    PropertyReader getRuntimeProperties() {
       if (_runtimeProperties == null) {
          return PropertyReaderUtils.EMPTY_PROPERTY_READER;
       } else {
          return _runtimeProperties;
       }
    }
 
    /**
     * Determines the reload interval for the config file, initializes the API
     * if the interval has changed and starts the config file watcher.
     */
    void init() {
 
       // Determine the reload interval
       int interval = DEFAULT_CONFIG_RELOAD_INTERVAL;
       if (_configFile != null) {
          try {
             interval = determineConfigReloadInterval();
 
          // If the interval could not be parsed, then use the default
          } catch (InvalidPropertyValueException exception) {
             // ignore
          }
       }
 
       // Initialize the API
       boolean initialized = _engine.initAPI();
 
       // Start the configuration file watch interval, if the location of the
       // file is set and the interval is greater than 0
       if (_configFile != null && interval > 0) {
          startConfigFileWatcher(interval);
       }
 
       // API initialized successfully, so log each unused property...
       if (initialized) {
          logUnusedRuntimeProperties();
 
          // ...and log that the framework was (re)initialized
          Log.log_3415();
       }
    }
 
    /**
     * Logs the unused runtime properties. Properties for Log4J (those starting
     * with <code>"log4j."</code> are ignored.
     */
    private void logUnusedRuntimeProperties() {
       if (_runtimeProperties != null) {
          Iterator unused = _runtimeProperties.getUnused().getNames();
          while (unused.hasNext()) {
             String name = (String) unused.next();
             if (name != null) {
                if (! name.startsWith("log4j.")) {
                   Log.log_3434(name);
                }
             }
          }
       }
    }
 
    /**
     * Starts the runtime configuration file watch thread.
     *
     * @param interval
     *    the interval in seconds, must be greater than or equal to 1.
     *
     * @throws IllegalStateException
     *    if no runtime configuration file is specified or if there is already
     *    a file watcher.
     *
     * @throws IllegalArgumentException
     *    if <code>interval &lt; 1</code>.
     */
    void startConfigFileWatcher(int interval)
    throws IllegalStateException, IllegalArgumentException {
 
       // Check state: Config file must be set
       if (_configFile == null || _configFile.length() < 1) {
          throw new IllegalStateException(
             "Name of runtime configuration file not set.");
 
       // Check state: File watcher cannot exist yet
       } else if (_configFileWatcher != null) {
          throw new IllegalStateException(
             "Runtime configuration file watcher exists.");
 
       // Check arguments
       } else if (interval < 1) {
          throw new IllegalArgumentException("interval (" + interval + ") < 1");
       }
 
       // Create and start a file watch thread
       if (_configFile.startsWith("http://") ||_configFile.startsWith("https://")) {
          _configFileWatcher = new HTTPFileWatcher(_configFiles, interval, _configFileListener);
       } else {
          _configFileWatcher = new FileWatcher(_configFiles, interval, _configFileListener);
       }
       _configFileWatcher.start();
    }
 
    /**
     * Re-initializes the configuration file listener if there is no file
     * watcher; otherwise interrupts the file watcher.
     */
    void reloadPropertiesIfChanged() {
       if (_configFileWatcher == null) {
          _configFileListener.reinit();
       } else {
          synchronized (_configFileWatcher) {
             _configFileWatcher.notifyAll();
          }
       }
    }
 
    /**
     * Initializes the logging subsystem.
     *
     * @param properties
     *    the runtime properties containing the settings for the logging
     *    subsystem, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>properties == null</code>.
     */
    void configureLogger(Properties properties)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("properties", properties);
 
       // Reset Log4J configuration
       LogManager.getLoggerRepository().resetConfiguration();
 
       // Make possible to have an API specific logger
       String apiLogger = properties.getProperty("log4j.rootLogger." + _config.getServletName());
       if (apiLogger != null) {
          properties.setProperty("log4j.rootLogger", apiLogger);
       }
 
       // Reconfigure Log4J
       PropertyConfigurator.configure(properties);
 
       // Determine if Log4J is properly initialized
       Enumeration appenders =
          LogManager.getLoggerRepository().getRootLogger().getAllAppenders();
 
       // If the properties did not include Log4J configuration settings, then
       // fallback to default settings
       if (appenders instanceof NullEnumeration) {
          Log.log_3304(_configFilesPath);
          configureLoggerFallback();
 
       // Otherwise log that custom Log4J configuration settings were applied
       } else {
          Log.log_3305();
       }
    }
 
    /**
     * Determines the interval for checking the runtime properties file for
     * modifications.
     *
     * @return
     *    the interval to use, always &gt;= 1.
     *
     * @throws InvalidPropertyValueException
     *    if the interval cannot be determined because it does not qualify as a
     *    positive 32-bit unsigned integer number.
     */
    int determineConfigReloadInterval()
    throws InvalidPropertyValueException {
 
       // Check state
       if (_configFile == null || _configFile.length() < 1) {
          throw new IllegalStateException("Name of runtime configuration file not set.");
       }
 
       // Get the runtime property
       String prop = CONFIG_RELOAD_INTERVAL_PROPERTY;
       String s = _runtimeProperties.get(prop);
       int interval;
 
       // If the property is set, parse it
       if (s != null && s.length() >= 1) {
          try {
             interval = Integer.parseInt(s);
 
             // Negative value
             if (interval < 0) {
                Log.log_3409(_configFilesPath, prop, s);
                throw new InvalidPropertyValueException(prop, s, "Negative value.");
 
             // Non-negative value
             } else {
                Log.log_3410(_configFilesPath, s);
             }
 
          // Not a valid number string
          } catch (NumberFormatException nfe) {
             Log.log_3409(_configFilesPath, prop, s);
             throw new InvalidPropertyValueException(prop, s, "Not a 32-bit integer number.");
          }
 
       // Property not set, use the default
       } else {
          Log.log_3408(_configFilesPath, prop, DEFAULT_CONFIG_RELOAD_INTERVAL);
          interval = DEFAULT_CONFIG_RELOAD_INTERVAL;
       }
 
       return interval;
    }
 
    /**
     * Determines the log locale.
     *
     * @return
     *    <code>false</code> if the specified locale is not supported,
     *    <code>true</code> otherwise.
     */
    boolean determineLogLocale() {
 
       String newLocale = null;
 
       // If we have runtime properties, then get the log locale
       if (_runtimeProperties != null) {
          newLocale = _runtimeProperties.get(LogCentral.LOG_LOCALE_PROPERTY);
       }
 
       // If the log locale is set, apply it
       if (newLocale != null) {
          String currentLocale = LogCentral.getLocale();
          if (! currentLocale.equals(newLocale)) {
             Log.log_3306(currentLocale, newLocale);
             try {
                LogCentral.setLocale(newLocale);
                Log.log_3307(currentLocale, newLocale);
             } catch (UnsupportedLocaleException exception) {
                Log.log_3308(currentLocale, newLocale);
                return false;
             }
          }
 
       // No property defines the locale, use the default
       } else {
          LogCentral.useDefaultLocale();
       }
 
       return true;
    }
 
    /**
     * Indicates whether the runtime property file was read successfully.
     *
     * @return
     *    <code>true</code> if the runtime properties are loaded correctly,
     *    <code>false</code> otherwise.
     */
    boolean propertiesRead() {
       return _propertiesRead;
    }
 
    /**
     * Stops the config file watcher thread.
     */
    void destroy() {
 
       // Stop the FileWatcher
       if (_configFileWatcher != null) {
          try {
             _configFileWatcher.end();
          } catch (Throwable exception) {
             Utils.logIgnoredException(exception);
          }
          _configFileWatcher = null;
       }
    }
 
    /**
     * Listener that reloads the configuration file if it changes.
     *
     * @version $Revision$ $Date$
     * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
     *
     * @since XINS 1.0.0
     */
    private final class ConfigurationFileListener implements FileWatcher.Listener {
 
       /**
        * Constructs a new <code>ConfigurationFileListener</code> object.
        */
       private ConfigurationFileListener() {
          // empty
       }
 
       /**
        * Re-initializes the framework. The run-time properties are re-read,
        * the configuration file reload interval is determined, the API is
        * re-initialized and then the new interval is applied to the watch
        * thread for the configuration file.
        */
       private void reinit() {
 
          if (_configFile != null) {
             Log.log_3407(_configFile);
          } else {
             Log.log_3407("/WEB-INF/xins.properties");
          }
 
          boolean reinitialized;
 
          synchronized (RUNTIME_PROPERTIES_LOCK) {
 
             // Apply the new runtime settings to the logging subsystem
             readRuntimeProperties();
 
             // Re-initialize the API
             reinitialized = _engine.initAPI();
 
             // Update the file watch interval if needed
             updateFileWatcher();
          }
 
          // API re-initialized successfully, so log each unused property...
          if (reinitialized) {
             logUnusedRuntimeProperties();
 
             // ...and log that the framework was reinitialized
             Log.log_3415();
          }
       }
 
       /**
        * Updates the file watch interval and initializes the file watcher if
        * needed.
        */
       private void updateFileWatcher() {
 
          if (_configFileWatcher == null) {
             return;
          }
 
          // Determine the interval
          int newInterval;
          try {
             newInterval = determineConfigReloadInterval();
          } catch (InvalidPropertyValueException exception) {
             // Logging is already done in determineConfigReloadInterval()
             return;
          }
 
          // Update the file watch interval
          int oldInterval = _configFileWatcher.getInterval();
 
          if (oldInterval != newInterval) {
             if (newInterval == 0 && _configFileWatcher != null) {
                _configFileWatcher.end();
                _configFileWatcher = null;
             } else if (newInterval > 0 && _configFileWatcher == null) {
                if (_configFile.startsWith("http://") ||_configFile.startsWith("https://")) {
                   _configFileWatcher = new HTTPFileWatcher(_configFiles, newInterval, _configFileListener);
                } else {
                   _configFileWatcher = new FileWatcher(_configFiles, newInterval, _configFileListener);
                }
                _configFileWatcher.start();
             } else {
                _configFileWatcher.setInterval(newInterval);
                Log.log_3403(_configFilesPath, oldInterval, newInterval);
             }
          }
       }
 
       /**
        * Callback method called when the configuration file is found while it
        * was previously not found.
        *
        * <p>This will trigger re-initialization.
        */
       public void fileFound() {
          reinit();
       }
 
       /**
        * Callback method called when the configuration file is (still) not
        * found.
        *
        * <p>The implementation of this method does not perform any actions.
        */
       public void fileNotFound() {
          Log.log_3400(_configFilesPath);
       }
 
       /**
        * Callback method called when the configuration file is (still) not
        * modified.
        *
        * <p>The implementation of this method does not perform any actions.
        */
       public void fileNotModified() {
       }
 
       /**
        * Callback method called when the configuration file could not be
        * examined due to a <code>SecurityException</code>.
        *
        * <p>The implementation of this method does not perform any actions.
        *
        * @param exception
        *    the caught security exception, should not be <code>null</code>
        *    (although this is not checked).
        */
       public void securityException(SecurityException exception) {
          Log.log_3401(exception, _configFilesPath);
       }
 
       /**
        * Callback method called when the configuration file is modified since
        * the last time it was checked.
        *
        * <p>This will trigger re-initialization.
        */
       public void fileModified() {
          reinit();
       }
    }
 }
