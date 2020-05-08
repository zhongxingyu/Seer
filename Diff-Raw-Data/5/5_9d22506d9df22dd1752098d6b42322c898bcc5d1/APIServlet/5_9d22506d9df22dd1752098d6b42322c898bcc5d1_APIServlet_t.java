 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.xins.common.MandatoryArgumentChecker;
 
 /**
  * HTTP servlet that forwards requests to an <code>API</code>.
  *
  * <h3>HTTP status codes</h3>
  *
  * <p>This servlet supports various HTTP methods, depending on the calling
  * conventions. A request with an unsupported method makes this servlet
  * return the HTTP status code <code>405 Method Not Allowed</code>.
  *
  * <p>If no matching function is found, then this servlet returns HTTP status
  * code <code>404 Not Found</code>.
  *
  * <p>If the servlet is temporarily unavailable, then the HTTP status
  * <code>503 Service Unavailable</code> is returned.
  *
  * <p>If the servlet encountered an initialization error, then the HTTP status
  * code <code>500 Internal Server Error</code> is returned.
  *
  * <p>If the state is <em>ready</em> then the HTTP status code
  * <code>200 OK</code> is returned.
  *
  *
  * <h3>Initialization</h3>
  *
  * <p>When the servlet is initialized, it gathers configuration information
  * from different sources:
  *
  * <dl>
  *    <dt><strong>1. Build-time settings</strong></dt>
  *    <dd>The application package contains a <code>web.xml</code> file with
  *        build-time settings. Some of these settings are required in order
  *        for the XINS/Java Server Framework to start up, while others are
  *        optional. These build-time settings are passed to the servlet by the
  *        application server as a {@link ServletConfig} object. See
  *        {@link #init(ServletConfig)}.
  *        <br>The servlet configuration is the responsibility of the
  *        <em>assembler</em>.</dd>
  *
  *    <dt><strong>2. System properties</strong></dt>
  *    <dd>The location of the configuration file must be passed to the Java VM
  *        at startup, as a system property.
  *        <br>System properties are the responsibility of the
  *        <em>system administrator</em>.
  *        <br>Example:
  *        <br><code>java -Dorg.xins.server.config=`pwd`/config/xins.properties
  *        -jar orion.jar</code></dd>
  *
  *    <dt><strong>3. Configuration file</strong></dt>
  *    <dd>The configuration file should contain runtime configuration
  *        settings, like the settings for the logging subsystem.
  *        <br>Runtime properties are the responsibility of the
  *        <em>system administrator</em>.
  *        <br>Example contents for a configuration file:
  *        <blockquote><code>log4j.rootLogger=DEBUG, console
  *           <br>log4j.appender.console=org.apache.log4j.ConsoleAppender
  *           <br>log4j.appender.console.layout=org.apache.log4j.PatternLayout
  *           <br>log4j.appender.console.layout.ConversionPattern=%d %-5p [%c]
  *           %m%n</code></blockquote></dd>
  * </dl>
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  * @author <a href="mailto:mees.witteman@orange-ftgroup.com">Mees Witteman</a>
  *
  * @since XINS 1.0.0
  */
 public final class APIServlet
 extends HttpServlet {
 
    // TODO: Log 3611 and return an appropriate HTTP result when the API is not
    //       usable.
    /**
     * Serial version UID. Used for serialization. The assigned value is for
     * compatibility with XINS 1.2.5.
     */
    private static final long serialVersionUID = -1117062458458353841L;
 
    /**
     * The name of the system property that specifies the location of the
     * configuration file.
     */
    public static final String CONFIG_FILE_SYSTEM_PROPERTY =
       "org.xins.server.config";
 
    /**
     * The name of the runtime property that specifies the interval
     * for the configuration file modification checks, in seconds.
     */
    public static final String CONFIG_RELOAD_INTERVAL_PROPERTY =
       "org.xins.server.config.reload";
 
    /**
     * The name of the runtime property that hostname for the server
     * running the API.
     */
    public static final String HOSTNAME_PROPERTY = "org.xins.server.hostname";
 
    /**
     * The default configuration file modification check interval, in seconds.
     */
    public static final int DEFAULT_CONFIG_RELOAD_INTERVAL = 60;
 
    /**
     * The name of the build property that specifies the name of the
     * API class to load.
     */
    public static final String API_CLASS_PROPERTY = "org.xins.api.class";
 
    /**
     * The name of the build property that specifies the name of the
     * API.
     */
    public static final String API_NAME_PROPERTY = "org.xins.api.name";
 
    /**
     * The name of the build property that specifies the version with which the
     * API was built.
     */
    public static final String API_BUILD_VERSION_PROPERTY =
       "org.xins.api.build.version";
 
    /**
     * The name of the build property that specifies the name of the default
     * calling convention.
     */
    public static final String API_CALLING_CONVENTION_PROPERTY =
       "org.xins.api.calling.convention";
 
    /**
     * The name of the build property that specifies the class of the default
     * calling convention.
     */
    public static final String API_CALLING_CONVENTION_CLASS_PROPERTY =
       "org.xins.api.calling.convention.class";
 
    /**
     * The name of the request parameter that specifies the name of the calling
     * convention to use.
     */
    public static final String CALLING_CONVENTION_PARAMETER = "_convention";
 
    /**
     * The name of the XINS standard calling convention.
     */
    public static final String STANDARD_CALLING_CONVENTION = "_xins-std";
 
    /**
     * The XINS XML calling convention.
     */
    public static final String XML_CALLING_CONVENTION = "_xins-xml";
 
    /**
     * The XINS XSLT calling convention.
     */
    public static final String XSLT_CALLING_CONVENTION = "_xins-xslt";
 
    /**
     * The name of the SOAP calling convention.
     *
     * @since XINS 1.3.0
     */
    public static final String SOAP_CALLING_CONVENTION = "_xins-soap";
 
    /**
     * The name of the XML-RPC calling convention.
     *
     * @since XINS 1.3.0
     */
    public static final String XML_RPC_CALLING_CONVENTION = "_xins-xmlrpc";
 
    /**
     * The name of the JSON-RPC calling convention.
     *
     * @since XINS 2.0.
     */
    public static final String JSON_RPC_CALLING_CONVENTION = "_xins-jsonrpc";
 
    /**
     * The name of the JSON calling convention (Yahoo! style).
     *
     * @since XINS 2.0.
     */
    public static final String JSON_CALLING_CONVENTION = "_xins-json";
 
    /**
     * XINS server engine. Initially <code>null</code> but set to a
     * non-<code>null</code> value in the {@link #init(ServletConfig)} method.
     */
    private Engine _engine;
 
    /**
     * Initializes the loggers to log to the console using a simple format
     * and no threshold. This is done by calling
     * {@link Engine#configureLoggerFallback()}.
     */
    static {
       ConfigManager.configureLoggerFallback();
    }
 
    /**
     * Constructs a new <code>APIServlet</code> object.
     */
    public APIServlet() {
       // empty
    }
 
    /**
     * Returns information about this servlet, as plain text.
     *
     * @return
     *    textual description of this servlet, not <code>null</code> and not an
     *    empty character string.
     */
    public String getServletInfo() {
       return "XINS/Java Server Framework " + Library.getVersion();
    }
 
    /**
     * Initializes this servlet using the specified configuration.
     *
     * @param config
     *    the {@link ServletConfig} object which contains build properties for
     *    this servlet, as specified by the <em>assembler</em>, cannot be
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>config == null
     *    || config.{@link ServletConfig#getServletContext()} == null</code>.
     *
     * @throws ServletException
     *    if the servlet could not be initialized.
     */
    public void init(ServletConfig config)
    throws IllegalArgumentException, ServletException {
 
       // Check arguments
       MandatoryArgumentChecker.check("config", config);
 
       // Get the ServletContext
       ServletContext context = config.getServletContext();
       if (context == null) {
          String message = "config.getServletContext() == null";
          Log.log_3202(message);
          throw new IllegalArgumentException(message);
       }
 
       // Compare the expected with the implemented Java Servlet API version;
      // versions 2.2, 2.3, 2.4 and 2.5 are supported
       int major = context.getMajorVersion();
       int minor = context.getMinorVersion();
       if (major != 2 || minor < 2 || minor > 5) {
          String expected = "2.2/2.3/2.4/2.5";
         String actual   = major + "." + minor;
          Log.log_3203(actual, expected);
       }
 
       // Construct an engine
       try {
          _engine = new Engine(config);
 
       // Fail silently, so that the servlet container will not keep trying to
       // re-initialize this servlet (possibly on each call!)
       } catch (Throwable exception) {
          String thisClass  = APIServlet.class.getName();
          String thisMethod = "init(javax.servlet.ServletConfig)";
          String thatClass  = Engine.class.getName();
          String thatMethod = "<init>(javax.servlet.ServletConfig)";
          String detail     = null;
          org.xins.common.Log.log_1052(exception,
                                       thisClass, thisMethod,
                                       thatClass, thatMethod,
                                       detail);
          return;
       }
    }
 
    /**
     * Returns the <code>ServletConfig</code> object which contains the
     * build properties for this servlet. The returned {@link ServletConfig}
     * object is the one passed to the {@link #init(ServletConfig)} method.
     *
     * @return
     *    the {@link ServletConfig} object that was used to initialize this
     *    servlet, or <code>null</code> if this servlet is not yet
     *    initialized.
     */
    public ServletConfig getServletConfig() {
       return (_engine == null) ? null : _engine.getServletConfig();
    }
 
    /**
     * Handles a request to this servlet. If any of the arguments is
     * <code>null</code>, then the behaviour of this method is undefined.
     *
     * @param request
     *    the servlet request, should not be <code>null</code>.
     *
     * @param response
     *    the servlet response, should not be <code>null</code>.
     *
     * @throws NullPointerException
     *    if this servlet is yet uninitialized.
     *
     * @throws ClassCastException
     *    if <code>! (request instanceof {@link HttpServletRequest}
     *    &amp;&amp; response instanceof {@link HttpServletResponse})</code>.
     *
     * @throws ServletException
     *    if this servlet failed for some other reason that an I/O error.
     *
     * @throws IOException
     *    if there is an error error writing to the response output stream.
     */
    public void service(ServletRequest  request,
                        ServletResponse response)
    throws NullPointerException,
           ClassCastException,
           ServletException,
           IOException {
 
       // Convert request and response to HTTP-specific variants
       HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
       HttpServletResponse httpResponse = (HttpServletResponse) response;
 
       // Engine failed to initialize, return '500 Internal Server Error'
       if (_engine == null) {
          httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          return;
       }
 
       // Pass control to the Engine
       _engine.service(httpRequest, httpResponse);
    }
 
    /**
     * Handles an HTTP request to this servlet. If any of the arguments is
     * <code>null</code>, then the behaviour of this method is undefined.
     *
     * @param request
     *    the servlet request, should not be <code>null</code>.
     *
     * @param response
     *    the servlet response, should not be <code>null</code>.
     *
     * @throws NullPointerException
     *    if this servlet is yet uninitialized.
     *
     * @throws IOException
     *    if there is an error error writing to the response output stream.
     */
    public void service(HttpServletRequest  request,
                        HttpServletResponse response)
    throws NullPointerException,
           IOException {
 
       // Engine failed to initialize, return '500 Internal Server Error'
       if (_engine == null) {
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          return;
       }
 
       // Pass control to the Engine
       _engine.service(request, response);
    }
 
    /**
     * Destroys this servlet. A best attempt will be made to release all
     * resources.
     *
     * <p>After this method has finished, no more requests will be handled
     * successfully.
     */
    public void destroy() {
       if (_engine != null) {
          _engine.destroy();
          _engine = null;
       }
    }
 }
