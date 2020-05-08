 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.reflect.Field;
 import java.util.Properties;
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.helpers.NullEnumeration;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.servlet.ServletUtils;
 import org.xins.util.text.Replacer;
 
 /**
  * Servlet that forwards request to an <code>API</code>.
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
     * Constant indicating the <em>uninitialized</em> state. See
     * {@link #_state}.
     */
    private static final int UNINITIALIZED = 0;
 
    /**
     * Constant indicating the <em>initializing</em> state. See
     * {@link #_state}.
     */
    private static final int INITIALIZING = 1;
 
    /**
     * Constant indicating the <em>ready</em> state. See
     * {@link #_state}.
     */
    private static final int READY = 2;
 
    /**
     * Constant indicating the <em>disposing</em> state. See
     * {@link #_state}.
     */
    private static final int DISPOSING = 3;
 
    /**
     * Constant indicating the <em>disposed</em> state. See
     * {@link #_state}.
     */
    private static final int DISPOSED = 4;
 
    /**
     * The name of the initialization property that specifies the name of the
     * API class to load.
     */
    private static final String API_CLASS_PROPERTY = "org.xins.api.class";
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Configures the logger using the specified servlet configuration. This
     * method is called from {@link #init(ServletConfig)}.
     *
     * @param config
     *    the servlet configuration, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>config == null</code>.
     */
    private static void configureLogger(ServletConfig config)
    throws IllegalArgumentException, ServletException {
 
       // Convert the ServletConfig to a Properties object
       Properties settings = ServletUtils.settingsAsProperties(config);
 
       // Apply replacements
       try {
          settings = Replacer.replace(settings, '[', ']', System.getProperties());
       } catch (Replacer.Exception exception) {
          configureLoggerFallback();
          String message = "Failed to apply replacements to servlet initialization settings.";
          Library.LIFESPAN_LOG.fatal(message, exception);
          throw new ServletException(message, exception);
       }
 
       // First see if a config file has been specified
       String configFile = settings.getProperty("org.apache.log4j.config");
       boolean doConfigure = true;
       if (configFile != null && configFile.length() > 0) {
          if (new File(configFile).exists()) {
             // TODO: configure delay for configureAndWatch
             PropertyConfigurator.configureAndWatch(configFile);
             doConfigure = false;
             Library.LIFESPAN_LOG.info("Using Log4J configuration file \"" + configFile + "\".");
          } else {
             configureLoggerFallback();
             doConfigure = false;
             Library.LIFESPAN_LOG.error("Log4J configuration file \"" + configFile + "\" does not exist. Using fallback defaults.");
          }
 
       // If not, perform initialization with init settings
       } else {
          PropertyConfigurator.configure(settings);
       }
 
       // If Log4J is not initialized at this point, use fallback defaults
       if (doConfigure && Library.LIFESPAN_LOG.getAllAppenders() instanceof NullEnumeration) {
          configureLoggerFallback();
          Library.LIFESPAN_LOG.warn("No initialization settings found for Log4J. Using fallback defaults.");
       }
    }
 
    /**
     * Initializes Log4J with fallback default settings.
     */
    private static final void configureLoggerFallback() {
       Properties settings = new Properties();
 
       settings.setProperty("log4j.rootLogger",              "ALL, console");
       settings.setProperty("log4j.appender.console",        "org.apache.log4j.ConsoleAppender");
       settings.setProperty("log4j.appender.console.layout", "org.apache.log4j.SimpleLayout");
 
       PropertyConfigurator.configure(settings);
    }
 
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
          final String message = "API class name not set in initialization parameter \"" + API_CLASS_PROPERTY + "\".";
          Library.LIFESPAN_LOG.fatal(message);
          throw new ServletException(message);
       }
 
       // Load the API class
       Class apiClass;
       try {
          apiClass = Class.forName(apiClassName);
       } catch (Exception e) {
          String message = "Failed to load API class set in initialization parameter \"" + API_CLASS_PROPERTY + "\": \"" + apiClassName + "\".";
          Library.LIFESPAN_LOG.fatal(message, e);
          throw new ServletException(message);
       }
 
       // Get the SINGLETON field
       Field singletonField;
       try {
          singletonField = apiClass.getDeclaredField("SINGLETON");
       } catch (Exception e) {
          String message = "Failed to lookup class field SINGLETON in API class \"" + apiClassName + "\".";
          Library.LIFESPAN_LOG.fatal(message, e);
          throw new ServletException(message);
       }
 
       // Get the value of the SINGLETON field
       try {
          api = (API) singletonField.get(null);
       } catch (Exception e) {
          String message = "Failed to get value of SINGLETON field of API class \"" + apiClassName + "\".";
          Library.LIFESPAN_LOG.fatal(message, e);
          throw new ServletException(message);
       }
       if (Library.LIFESPAN_LOG.isDebugEnabled()) {
          Library.LIFESPAN_LOG.debug("Obtained API instance of class: \"" + apiClassName + "\".");
       }
 
       // Initialize the API
       if (Library.LIFESPAN_LOG.isDebugEnabled()) {
          Library.LIFESPAN_LOG.debug("Initializing API.");
       }
       Properties settings = ServletUtils.settingsAsProperties(config);
       try {
          api.init(settings);
       } catch (Throwable e) {
          String message = "Failed to initialize API.";
          Library.LIFESPAN_LOG.fatal(message, e);
 
          try {
             api.destroy();
          } catch (Throwable e2) {
             Library.LIFESPAN_LOG.error("Caught " + e2.getClass().getName() + " while destroying API instance of class " + api.getClass().getName() + ". Ignoring.", e2);
          }
 
          throw new ServletException(message);
       }
 
       if (Library.LIFESPAN_LOG.isDebugEnabled()) {
          Library.LIFESPAN_LOG.debug("Initialized API.");
       }
 
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
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The current state.
     */
    private int _state;
 
    /**
     * The object to synchronize on when the state is changed.
     */
    private Object _stateLock;
 
    /**
     * The stored servlet configuration object.
     */
    private ServletConfig _config;
 
    /**
     * The API that this servlet forwards requests to.
     */
    private API _api;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void init(ServletConfig config)
    throws ServletException {
 
       // Check preconditions
       if (config == null) {
          throw new ServletException("No servlet configuration.");
       }
 
       synchronized (_stateLock) {
          initImpl(config);
       }
    }
 
    /**
     * Actually initializes this servlet.  This method is called from
     * {@link #init(ServletConfig)}.
     *
     * @param config
     *    the servlet configuration object, guaranteed not to be
     *    <code>null</code>.
     *
     * @throws ServletException
     *    if the initialization fails.
     */
    private void initImpl(ServletConfig config)
    throws ServletException {
 
       // Check preconditions
       if (_state != UNINITIALIZED) {
          throw new ServletException("Unable to initialize, state is not UNINITIALIZED.");
       } else if (config == null) {
          throw new ServletException("No servlet configuration, unable to initialize.");
       }
 
       // Set the state
       _state = INITIALIZING;
 
       // Store the ServletConfig object, per the Servlet API Spec, see:
       // http://java.sun.com/products/servlet/2.3/javadoc/javax/servlet/Servlet.html#getServletConfig()
       _config = config;
 
       // Initialize Log4J
       configureLogger(config);
 
       // Initialization starting
       String version = org.xins.server.Library.getVersion();
       if (Library.LIFESPAN_LOG.isDebugEnabled()) {
          Library.LIFESPAN_LOG.debug("XINS/Java Server Framework " + version + " is initializing.");
       }
 
       // Initialize API instance
       _api = configureAPI(config);
 
       // Initialization done
       if (Library.LIFESPAN_LOG.isInfoEnabled()) {
          Library.LIFESPAN_LOG.info("XINS/Java Server Framework " + version + " is initialized.");
       }
 
       // Finally enter the ready state
       _state = READY;
    }
 
    public ServletConfig getServletConfig() {
       return _config;
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
 
    public String getServletInfo() {
       return "XINS " + Library.getVersion() + " API Servlet";
    }
 
    public void destroy() {
       if (Library.LIFESPAN_LOG.isDebugEnabled()) {
          Library.LIFESPAN_LOG.debug("XINS/Java Server Framework shutdown initiated.");
       }
 
       synchronized (_stateLock) {
          _state = DISPOSING;
         _api.destroy();
          Library.LIFESPAN_LOG.info("XINS/Java Server Framework shutdown completed.");
          _state = DISPOSED;
       }
    }
 }
