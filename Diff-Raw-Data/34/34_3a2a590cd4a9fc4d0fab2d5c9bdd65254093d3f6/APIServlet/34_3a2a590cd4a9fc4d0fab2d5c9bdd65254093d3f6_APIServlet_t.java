 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.Field;
 import java.util.Enumeration;
 import java.util.Properties;
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.helpers.NullEnumeration;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.io.FileWatcher;
 import org.xins.util.servlet.ServletUtils;
 import org.xins.util.text.Replacer;
 
 /**
  * Servlet that forwards requests to an <code>API</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class APIServlet
 extends Object
 implements Servlet {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The <em>uninitialized</em> state. See {@link #_state}.
     */
    private static final State UNINITIALIZED = new State("UNINITIALIZED");
 
    /**
     * The <em>initializing</em> state. See {@link #_state}.
     */
    private static final State INITIALIZING = new State("INITIALIZING");
 
    /**
     * The <em>ready</em> state. See {@link #_state}.
     */
    private static final State READY = new State("READY");
 
    /**
     * The <em>disposing</em> state. See {@link #_state}.
     */
    private static final State DISPOSING = new State("DISPOSING");
 
    /**
     * The <em>disposed</em> state. See {@link #_state}.
     */
    private static final State DISPOSED = new State("DISPOSED");
 
    /**
     * The name of the system property that specifies the location of the
     * configuration file.
     */
    public static final String CONFIG_FILE_SYSTEM_PROPERTY = "org.xins.server.config";
 
    /**
     * The name of the initialization property that specifies the name of the
     * API class to load.
     */
    public static final String API_CLASS_PROPERTY = "org.xins.api.class";
 
    /**
     * The name of the configuration property that specifies the interval
     * for the configuration file modification checks, in seconds.
     */
    public static final String CONFIG_RELOAD_INTERVAL_PROPERTY = "org.xins.server.config.reload";
 
    /**
     * The default configuration file modification check interval, in seconds.
     */
    public static final int DEFAULT_CONFIG_RELOAD_INTERVAL = 60;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes an API instance based on the specified servlet
     * configuration.
     *
     * @param config
     *    the servlet configuration, cannot be <code>null</code>.
     *
     * @throws ServletException
     *    if an API instance could not be initialized.
     */
    private static API configureAPI(ServletConfig config)
    throws ServletException { 
 
       API api;
 
       // Determine the API class
       String apiClassName = config.getInitParameter(API_CLASS_PROPERTY);
       if (apiClassName == null || apiClassName.trim().length() < 1) {
          final String message = "Invalid application package. API class name not set in initialization parameter \"" + API_CLASS_PROPERTY + "\".";
          Library.LIFESPAN_LOG.fatal(message);
          throw new ServletException(message);
       }
 
       // Load the API class
       Class apiClass;
       try {
          apiClass = Class.forName(apiClassName);
       } catch (Exception e) {
          String message = "Invalid application package. Failed to load API class set in initialization parameter \"" + API_CLASS_PROPERTY + "\": \"" + apiClassName + "\".";
          Library.LIFESPAN_LOG.fatal(message, e);
          throw new ServletException(message);
       }
 
      // Check that the loaded API class is derived from the API base class
      if (! API.class.isAssignableFrom(apiClass)) {
         String message = "Invalid application package. The \"" + apiClassName + "\" is not derived from class " + API.class.getName() + '.';
         Library.LIFESPAN_LOG.fatal(message);
         throw new ServletException(message);
      }
 
       // Get the SINGLETON field
       Field singletonField;
       try {
          singletonField = apiClass.getDeclaredField("SINGLETON");
       } catch (Exception e) {
          String message = "Invalid application package. Failed to lookup class field SINGLETON in API class \"" + apiClassName + "\".";
          Library.LIFESPAN_LOG.fatal(message, e);
          throw new ServletException(message);
       }
 
       // Get the value of the SINGLETON field
       try {
          api = (API) singletonField.get(null);
       } catch (Exception e) {
         String message = "Invalid application package. Failed to get value of the SINGLETON field of API class \"" + apiClassName + "\".";
          Library.LIFESPAN_LOG.fatal(message, e);
          throw new ServletException(message);
       }
 
      // Make sure that the field is an instance of that same class
      if (api == null) {
         String message = "Invalid application package. The value of the SINGLETON field of API class \"" + apiClassName + "\" is null.";
         Library.LIFESPAN_LOG.fatal(message);
         throw new ServletException(message);
      } else if (api.getClass() != apiClass) {
         String message = "Invalid application package. The value of the SINGLETON field of API class \"" + apiClassName + "\" is not an instance of that class.";
         Library.LIFESPAN_LOG.fatal(message);
         throw new ServletException(message);
       }
 
       // Initialize the API
      Library.LIFESPAN_LOG.debug("Initializing API instance of class: \"" + apiClassName + "\".");
       Properties settings = ServletUtils.settingsAsProperties(config);
       try {
          api.init(settings);
       } catch (Throwable e) {
          String message = "Failed to initialize API.";
          Library.LIFESPAN_LOG.fatal(message, e);
 
          // TODO: Let the API.init() rollback the initialization self
          try {
             api.destroy();
          } catch (Throwable e2) {
             Library.LIFESPAN_LOG.error("Caught " + e2.getClass().getName() + " while destroying API instance of class " + api.getClass().getName() + ". Ignoring.", e2);
          }
 
          throw new ServletException(message);
       }
 
      Library.LIFESPAN_LOG.debug("Initialized \"" + api.getName() + "\" API.");
 
       return api;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>APIServlet</code> object.
     */
    public APIServlet() {
       _stateLock = new Object();
       _state     = UNINITIALIZED;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The current state.
     */
    private State _state;
 
    /**
     * Lock for <code>_state</code>
     */
    private Object _stateLock;
 
    /**
     * The stored servlet configuration object.
     */
    private ServletConfig _servletConfig;
 
    /**
     * The name of the configuration file.
     */
    private String _configFile;
 
    /**
     * The API that this servlet forwards requests to.
     */
    private API _api;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Initializes this servlet using the specified configuration. The
     * (required) {@link ServletConfig} argument is stored internally and is
     * returned from {@link #getServletConfig()}.
     *
     * <p>The initialization procedure will take required information from 3
     * sources, initially:
     *
     * <dl>
     *    <dt><strong>1. Build-time settings</strong></dt>
     *    <dd>The application package contains a <code>web.xml</code> file with
     *        build-time settings. Some of these settings are required in order
     *        for the XINS/Java Server Framework to start up, while others are
     *        optional. These build-time settings are passed to the servlet by
     *        the application server as a {@link ServletConfig} object. See
     *        {@link #init(ServletConfig)}.
     *        <br />The servlet configuration is the responsibility of the
     *        <em>assembler</em>.</dd>
     *
     *    <dt><strong>2. System properties</strong></dt>
     *    <dd>The location of the configuration file must be passed to the
     *        Java VM at startup, as a system property.
     *        <br />System properties are the responsibility of the
     *        <em>system administrator</em>.
     *        <br />Example:
     *        <br /><code>java -Dorg.xins.server.config=`pwd`/conf/xins.properties orion.jar</code></dd>
     *
     *    <dt><strong>3. Configuration file</strong></dt>
     *    <dd>The configuration file should contain runtime configuration
     *        settings, like the settings for the logging subsystem.
     *        <br />System properties are the responsibility of the
     *        <em>system administrator</em>.
     *        <br />Example contents for a configuration file:
     *        <blockquote><code>log4j.rootLogger=DEBUG, console
     *        <br />log4j.appender.console=org.apache.log4j.ConsoleAppender
     *        <br />log4j.appender.console.layout=org.apache.log4j.PatternLayout
     *        <br />log4j.appender.console.layout.ConversionPattern=%d %-5p [%c] %m%n</code></blockquote>
     * </dl>
     *
     * <p>The initialization is performed as follows:
     *
     * <ol>
     *    <li>if this servlet is not currently <em>uninitialized</em>, then a
     *        {@link ServletException} is thrown; this indicates a problem with
     *        the application server;
     *    <li>if <code>config</code> argument is <code>null</code> then a
     *        {@link ServletException} is thrown; this indicates a problem
     *        with the application server;
     *    <li>the {@link ServletContext} is retrieved from the
     *        {@link ServletConfig}, using the
     *        {@link ServletConfig#getServletContext()} method; if the returned
     *        value is <code>null</code>, then a {@link ServletException} is
     *        thrown; this indicates a problem with the application server;
     *    <li>the state is set to <em>initializing</em>;
     *    <li>the value of the required system property named
     *        {@link #CONFIG_FILE_SYSTEM_PROPERTY} is determined, this is the
     *        relative or absolute path to the runtime configuration file; if
     *        it is not set then a {@link ServletException} is thrown;
     *    <li>the indicated configuration file is loaded and all
     *        configuration properties in this file are read according to
     *        {@link Properties#load(InputStream) the specifications for a property file};
     *        if this fails, then a {@link ServletException} is thrown;
     *    <li>the logging subsystem is initialized using the properties from
     *        the configuration file, see
     *        {@link PropertyConfigurator#doConfigure(String,org.apache.log4j.spi.LoggerRepository) the Log4J documentation};
     *    <li>the logging system is investigated to check if it is properly
     *        initialized, if it is not then apparently the configuration file
     *        contained no initialization properties for it; in this case the
     *        logging subsystem will be configured to log to the standard
     *        output stream using a simple output method, with no log level
     *        threshold and a warning message is immediately logged;
     *    <li>at this point the logging subsystem is definitely initialized;
     *        the interval for the configuration file modification checks is
     *        determined by reading the
     *        {@link #CONFIG_RELOAD_INTERVAL_PROPERTY} configuration property;
     *        if this property is not set, then
     *        {@link #DEFAULT_CONFIG_RELOAD_INTERVAL} is assumed;
     *        if this property exists but has an invalid value, then a
     *        <em>warning</em> message is logged and
     *        {@link #DEFAULT_CONFIG_RELOAD_INTERVAL} is also assumed;
     *    <li>the initialization property {@link #API_CLASS_PROPERTY} is read
     *        from the {@link ServletConfig servlet configuration} (not from
     *        the configuration file); if it is not set then a
     *        {@link ServletException} is thrown.
     *    <li>the API class, specified in the {@link #API_CLASS_PROPERTY}
     *        property, is loaded; it must be derived from the {@link API}
     *        class in the XINS/Java Server Framework; if this fails then a
     *        {@link ServletException} is thrown;
     *    <li>in the API class a static field called <code>SINGLETON</code> is
     *        looked up and the value is determined; the value must be an
     *        instance of that same class, and cannot be <code>null</code>;
     *        if this fails, then a {@link ServletException} is thrown;
     *    <li>the API instance will be initialized by calling
     *        {@link API#init(Properties)}; if this fails a
     *        {@link ServletException} is thrown;
     *    <li>the {@link ServletConfig config} object is stored internally, to
     *        be returned by {@link #getServletConfig()}.
     *    <li>the state is set to <em>ready</em>.
     *    <li>the configuration file watch thread is started;
     * </ol>
     *
     * <p>Note that if a {@link ServletException} is thrown, the state is reset
     * to <em>uninitialized</em>.
     *
     * <p>Also note that if the logging subsystem is already initialized and a
     * {@link ServletException} is thrown, a <em>fatal</em> message is logged
     * just before the exception is actually thrown. However, if the logging
     * subsystem is not yet initialized, then a message is printed to the
     * standard error stream.
     *
     * @param config
     *    the {@link ServletConfig} object which contains initialization and
     *    startup parameters for this servlet, as specified by the
     *    <em>assembler</em>, cannot be <code>null</code>.
     *
     * @throws ServletException
     *    if <code>config == null</code>, if this servlet is not uninitialized
     *    or if the initialization failed for some other reason.
     */
    public void init(ServletConfig config)
    throws ServletException {
 
       // Make sure the Library class is initialized
       String version = Library.getVersion();
 
       // Hold the state lock
       synchronized (_stateLock) {
 
          // Check preconditions
          if (_state != UNINITIALIZED) {
             String message = "Application server malfunction detected. State is " + _state + " instead of " + UNINITIALIZED + '.';
             Library.LIFESPAN_LOG.fatal(message);
             throw new ServletException(message);
          } else if (config == null) {
             String message = "Application server malfunction detected. No servlet configuration object passed.";
             Library.LIFESPAN_LOG.fatal(message);
             throw new ServletException(message);
          }
 
          // Get the ServletContext
          ServletContext context = config.getServletContext();
          if (context == null) {
             String message = "Application server malfunction detected. No servlet context available.";
             Library.LIFESPAN_LOG.fatal(message);
             throw new ServletException(message);
          }
 
          // Check the implemented vs expected Java Servlet API version
          final int expectedMajor = 2;
          final int expectedMinor = 3;
          int major = context.getMajorVersion();
          int minor = context.getMinorVersion();
          if (major != expectedMajor || minor != expectedMinor) {
             Library.LIFESPAN_LOG.warn("Application server implements Java Servlet API version " + major + '.' + minor + " instead of the expected version " + expectedMajor + '.' + expectedMajor + ". The application may or may not work correctly.");
          }
 
          // Set the state
          _state = INITIALIZING;
 
          try {
             // Determine configuration file location
             _configFile = System.getProperty(CONFIG_FILE_SYSTEM_PROPERTY);
 
             // Read properties from the config file
             if (_configFile == null || _configFile.length() < 1) {
                Library.LIFESPAN_LOG.error("System administration issue detected. System property \"" + CONFIG_FILE_SYSTEM_PROPERTY + "\" is not set.");
             } else {
                applyConfigFile();
             }
 
             // Initialization starting
             Library.LIFESPAN_LOG.debug("XINS/Java Server Framework " + version + " is initializing.");
 
             // Initialize API instance
             _api = configureAPI(config);
 
             // Watch the configuration file
             if (_configFile != null) {
                FileWatcher.Listener listener = new ConfigurationFileListener();
                final int delay = 10; // TODO: Read from config file
                FileWatcher watcher = new FileWatcher(_configFile, 10, listener);
                watcher.start();
                Library.LIFESPAN_LOG.info("Using config file \"" + _configFile + "\". Checking for changes every " + delay + " seconds.");
             }
 
             // Initialization done
             Library.LIFESPAN_LOG.info("XINS/Java Server Framework " + version + " is initialized.");
 
             // Finally enter the ready state
             _state = READY;
 
             // Store the ServletConfig object, per the Servlet API Spec, see:
             // http://java.sun.com/products/servlet/2.3/javadoc/javax/servlet/Servlet.html#getServletConfig()
             _servletConfig = config;
 
          // If an exception is thrown, then reset the state
          } finally {
             if (_state != READY) {
                _state = UNINITIALIZED;
             }
          }
       }
    }
 
    private void applyConfigFile() {
       try {
          FileInputStream in = new FileInputStream(_configFile);
          Properties properties = new Properties();
          properties.load(in);
 
          Library.configure(properties);
       } catch (FileNotFoundException exception) {
          Library.LIFESPAN_LOG.error("System administration issue detected. Configuration file \"" + _configFile + "\" cannot be opened.");
       } catch (SecurityException exception) {
          Library.LIFESPAN_LOG.error("System administration issue detected. Access denied while loading configuration file \"" + _configFile + "\".");
       } catch (IOException exception) {
          Library.LIFESPAN_LOG.error("System administration issue detected. Unable to read configuration file \"" + _configFile + "\".");
       }
    }
 
    /**
     * Returns the <code>ServletConfig</code> object which contains the
     * initialization and startup parameters for this servlet. The returned
     * {@link ServletConfig} object is the one passed to the
     * {@link #init(ServletConfig)} method. 
     *
     * @return
     *    the {@link ServletConfig} object that was used to initialize this
     *    servlet, not <code>null</code> if this servlet is indeed already
     *    initialized.
     */
    public ServletConfig getServletConfig() {
       return _servletConfig;
    }
 
    public void service(ServletRequest request, ServletResponse response)
    throws ServletException, IOException {
 
       // Determine current time
       long start = System.currentTimeMillis();
 
       // Check state
       if (_state != READY) {
          if (_state == UNINITIALIZED) {
             throw new ServletException("This servlet is not yet initialized.");
          } else if (_state == DISPOSING) {
             throw new ServletException("This servlet is currently being disposed.");
          } else if (_state == DISPOSED) {
             throw new ServletException("This servlet is disposed.");
          } else {
             throw new Error("This servlet is not ready, the state is unknown.");
          }
       }
 
       // TODO: Support and use OutputStream instead of Writer, for improved
       //       performance
 
       // Call the API
       CallResult result = _api.handleCall(start, request);
 
       // Determine the XSLT to link to
       String xslt = request.getParameter("_xslt");
 
       // Send the XML output to the stream and flush
       PrintWriter out = response.getWriter(); 
       response.setContentType("text/xml");
       CallResultOutputter.output(out, result, xslt);
       out.flush();
    }
 
    /**
     * Returns information about this servlet, as plain text.
     *
     * @return
     *    textual description of this servlet, not <code>null</code> and not an
     *    empty character string.
     */
    public String getServletInfo() {
       return "XINS " + Library.getVersion() + " API Servlet";
    }
 
    public void destroy() {
       if (Library.LIFESPAN_LOG.isDebugEnabled()) {
          Library.LIFESPAN_LOG.debug("XINS/Java Server Framework shutdown initiated.");
       }
 
       synchronized (_stateLock) {
          _state = DISPOSING;
          if (_api != null) {
             _api.destroy();
          }
          Library.LIFESPAN_LOG.info("XINS/Java Server Framework shutdown completed.");
          _state = DISPOSED;
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * State of an <code>APIServlet</code>.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     *
     * @since XINS 0.121
     */
    private static final class State extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>State</code> object.
        *
        * @param name
        *    the name of this state, cannot be <code>null</code>.
        *
        * @throws IllegalArgumentException
        *    if <code>name == null</code>.
        */
       private State(String name) throws IllegalArgumentException {
 
          // Check preconditions
          MandatoryArgumentChecker.check("name", name);
 
          _name = name;
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The name of this state. Cannot be <code>null</code>.
        */
       private final String _name; 
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the name of this state.
        *
        * @return
        *    the name of this state, cannot be <code>null</code>.
        */
       String getName() {
          return _name;
       }
 
       public String toString() {
          return _name;
       }
    }
 
    /**
     * Listener that reloads the configuration file if it changes.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     *
     * @since XINS 0.121
     */
    private final class ConfigurationFileListener
    extends Object
    implements FileWatcher.Listener {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>ConfigurationFileListener</code> object.
        */
       private ConfigurationFileListener() {
          // empty
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       public void fileModified() {
          Library.LIFESPAN_LOG.info("Configuration file \"" + _configFile + "\" changed. Re-initializing XINS/Java Server Framework.");
          applyConfigFile();
          // TODO: reinit API
          Library.LIFESPAN_LOG.info("Re-initialized XINS/Java Server Framework.");
       }
 
       public void fileNotFound() {
          Library.LIFESPAN_LOG.error("System administration issue detected. Configuration file \"" + _configFile + "\" cannot be opened.");
       }
 
       public void fileNotModified() {
          Library.LIFESPAN_LOG.debug("Configuration file \"" + _configFile + "\" is not modified.");
       }
 
       public void securityException(SecurityException exception) {
          Library.LIFESPAN_LOG.error("System administration issue detected. Access denied while reading file \"" + _configFile + "\".");
       }
    }
 }
