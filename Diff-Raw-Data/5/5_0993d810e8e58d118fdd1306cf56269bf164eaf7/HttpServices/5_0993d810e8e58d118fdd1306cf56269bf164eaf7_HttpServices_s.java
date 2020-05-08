 /*
  * The contents of this file are subject to the Open Software License
  * Version 3.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.opensource.org/licenses/osl-3.0.txt
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  */
 
 package org.mulgara.server;
 
 import static org.mortbay.jetty.servlet.Context.SESSIONS;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.Servlet;
 
 import org.apache.log4j.Logger;
 import org.mortbay.jetty.AbstractConnector;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.ContextHandler;
 import org.mortbay.jetty.nio.BlockingChannelConnector;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.mortbay.jetty.webapp.WebAppClassLoader;
 import org.mortbay.jetty.webapp.WebAppContext;
 import org.mortbay.util.MultiException;
 import org.mulgara.config.Connector;
 import org.mulgara.config.MulgaraConfig;
 import org.mulgara.config.PublicConnector;
 import org.mulgara.util.MortbayLogger;
 import org.mulgara.util.Reflect;
 import org.mulgara.util.TempDir;
 import org.mulgara.util.functional.Fn1E;
 import org.mulgara.util.functional.Pair;
 import org.xml.sax.SAXException;
 
 /**
  * Manages all the HTTP services provided by a Mulgara server.
  *
  * @created Sep 5, 2008
  * @author Paul Gearon
  * @copyright &copy; 2008 <a href="http://www.topazproject.org/">The Topaz Project</a>
  * @licence <a href="{@docRoot}/../../LICENCE.txt">Open Software License v3.0</a>
  */
 public class HttpServices {
 
   /** A virtual typedef for a context starter. */
   private interface ContextStarter extends Fn1E<Server,Pair<String,String>,IOException> { }
 
   /** A virtual typedef for a service path. */
   private class Service extends Pair<String,String> { Service(String f, String s) { super(f,s); } }
 
   /** The logging category to log to. */
   protected static Logger logger = Logger.getLogger(HttpServices.class.getName());
 
   /** The web application file path. */
   private final static String WEBAPP_PATH = "webapps";
 
   /** The Web Services web application file. */
   private final static String WEBSERVICES_WEBAPP = "webservices.war";
 
   /** The Web Services path. */
   private final static String WEBSERVICES_PATH = "webservices";
 
   /** The Web Query path. */
   private final static String WEBQUERY_PATH = "webui";
 
   /** The Web Tutorial path. */
   private final static String WEBTUTORIAL_PATH = "tutorial";
 
   /** The sparql path. */
   private final static String SPARQL_PATH = "sparql";
 
   /** The tql path. */
   private final static String TQL_PATH = "tql";
 
   /** The default service path. */
   private final static String DEFAULT_SERVICE = WEBQUERY_PATH;
 
   /** The key to the bound host name in the attribute map of the servlet context. */
   public final static String BOUND_HOST_NAME_KEY = "boundHostname";
 
   /** Key to the bound server model uri in the attribute map of the servlet context. */
   public final static String SERVER_MODEL_URI_KEY = "serverModelURI";
 
   /** The maximum number of acceptors that Jetty can handle. It locks above this number. */
   private static final int WEIRD_JETTY_THREAD_LIMIT = 24;
 
   /** The HTTP server instance. */
   private final Server httpServer;
 
   /** The Public HTTP server instance. */
   private final Server httpPublicServer;
 
   /** The configuration for the server. */
   private final MulgaraConfig config;
 
   /** The name for the host. */
   private String hostName;
 
   /** The host server. This may contain information useful to services. */
   private final EmbeddedMulgaraServer hostServer;
 
 
   /**
    * Creates the web services object.
    * @param hostServer The Server that started these Web services.
    * @param hostName The name of the HTTP host this object is setting up.
    * @param config The configuration to use.
    * @throws IOException Exception setting up with files or network.
    * @throws SAXException Problem reading XML configurations.
    * @throws ClassNotFoundException An expected class was not found.
    * @throws NoSuchMethodException A configured class was not built as expected.
    * @throws InvocationTargetException A configured class did not behave as expected.
    * @throws IllegalAccessException A configured class was not accessible.
    */
   public HttpServices(EmbeddedMulgaraServer hostServer, String hostName, MulgaraConfig config)  throws IOException, SAXException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
     this.hostServer = hostServer;
     this.config = config;
     this.hostName = hostName;
     assert !config.getJetty().isDisabled();
     // get servers as a pair so we can set them here. Needed because they are final.
     Pair<Server,Server> servers = createHttpServers();
     httpServer = servers.first();
     httpPublicServer = servers.second();
   }
 
 
   /**
    * Starts the web server and all services.
    * @throws ExceptionList Caused by a MultiException in the HTTP Server.
    * @throws Exception Both the server and the services are able to throw exceptions.
    */
   @SuppressWarnings("unchecked")
   public void start() throws ExceptionList, Exception {
     try {
       if (httpServer != null) httpServer.start();
       if (httpPublicServer != null) httpPublicServer.start();
     } catch (MultiException e) {
       throw new ExceptionList(e.getThrowables());
     }
   }
 
 
   /**
    * Stops the web server and all services.
    * @throws Exception Both the server and the services are able to throw exceptions.
    */
   public void stop() throws Exception {
     try {
       if (httpServer != null) httpServer.stop();
     } finally {
       if (httpPublicServer != null) httpPublicServer.stop();
     }
   }
 
 
   /**
    * Creates an HTTP server.
    * @return a pair of private/public servers.
    * @throws IOException if the server configuration cannot be found
    * @throws SAXException if the HTTP server configuration file is invalid
    * @throws ClassNotFoundException if the HTTP server configuration file contains a reference to an unkown class
    * @throws NoSuchMethodException if the HTTP server configuration file contains a reference to an unkown method
    * @throws InvocationTargetException if an error ocurrs while trying to configure the HTTP server
    * @throws IllegalAccessException If a class loaded by the server is accessed in an unexpected way.
    */
   public Pair<Server,Server> createHttpServers() throws IOException, SAXException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
     if (logger.isDebugEnabled()) logger.debug("Creating HTTP server instance");
 
     // Set the magic logging property for Jetty to use Log4j
     System.setProperty(MortbayLogger.LOGGING_CLASS_PROPERTY, MortbayLogger.class.getCanonicalName());
     MortbayLogger.setEnabled(true);
 
     // create and register a new HTTP server
     Server privateServer = buildAndConfigure(new JettyConnector(config.getJetty().getConnector()), ServerInfo.getHttpPort());
     Server publicServer = buildAndConfigure(new JettyConnector(config.getJetty().getPublicConnector()), ServerInfo.getPublicHttpPort());
 
 
     // Accumulator for all the services
     Map<String,String> privateServices = new HashMap<String,String>();
 
     // start all the private configured services.
     for (ContextStarter starter: getContextStarters()) {
       try {
         starter.fn(privateServer).addTo(privateServices);
       } catch (IllegalStateException e) {
         // not fatal, so just log the problem and go on
         logger.warn("Unable to start web service", e.getCause());
       }
     }
 
     // we have all the services, so now instantiate the service listing service
     addWebServiceListingContext(privateServer, privateServices);
 
     // start the public contexts
     for (ContextStarter starter: getPublicContextStarters()) {
       try {
         starter.fn(publicServer);
       } catch (IllegalStateException e) {
         logger.warn("Unable to start public web service", e.getCause());
       }
     }
 
     // get all the handlers in use by both servers
     List<Handler> handlers = new ArrayList<Handler>(Arrays.asList(privateServer.getChildHandlers()));
     handlers.addAll(Arrays.asList(publicServer.getChildHandlers()));
 
     // add our class loader as the classloader of all contexts, unless this is a webapp in which case we wrap it
     ClassLoader classLoader = this.getClass().getClassLoader();
     for (Handler handler: handlers) {
       if (handler instanceof WebAppContext) ((WebAppContext)handler).setClassLoader(new WebAppClassLoader(classLoader, (WebAppContext)handler));
       else if (handler instanceof ContextHandler) ((ContextHandler)handler).setClassLoader(classLoader);
     }
 
     // return the servers
     return new Pair<Server,Server>(privateServer, publicServer);
   }
 
 
   /**
    * Create a server object, and configure it.
    * @param cfg The Jetty configuration for the server.
    * @return The created server.
    * @throws UnknownHostException The configured host name is invalid.
    */
   Server buildAndConfigure(JettyConnector cfg, int port) throws UnknownHostException {
     Server s;
     if (cfg.isProvided()) {
      s = new Server(port);
    } else {
       s = new Server();
       addConnector(s, cfg);
     }
     return s;
   }
 
 
   /**
    * Creates a list of functions for starting contexts.
    * <strong>This defines the list of services to be run.</strong>
    * @return A list that can start all the configured contexts.
    */
   private List<ContextStarter> getContextStarters() {
     List<ContextStarter> starters = new ArrayList<ContextStarter>();
     starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
       return addWebServicesWebAppContext(s);
     } });
     starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
       return addWebQueryContext(s, "User Interface", "org.mulgara.webquery.QueryServlet", WEBQUERY_PATH);
     } });
     starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
       return addWebQueryContext(s, "User Tutorial", "org.mulgara.webquery.TutorialServlet", WEBTUTORIAL_PATH);
     } });
     // expect to get the following from a config file
     // TODO: create a decent configuration object, instead of just handing out a Server
     starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
       return addServletContext(s, "org.mulgara.protocol.http.SparqlServlet", SPARQL_PATH, "SPARQL HTTP Service");
     } });
     starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
       return addServletContext(s, "org.mulgara.protocol.http.TqlServlet", TQL_PATH, "TQL HTTP Service");
     } });
     return starters;
   }
 
 
   /**
    * Creates a list of functions for starting public contexts.
    * <strong>This defines the list of services to be run.</strong>
    * @return A list that can start all the configured contexts.
    */
   private List<ContextStarter> getPublicContextStarters() {
     List<ContextStarter> starters = new ArrayList<ContextStarter>();
     starters.add(new ContextStarter() { public Service fn(Server s) throws IOException {
       return addServletContext(s, "org.mulgara.protocol.http.PublicSparqlServlet", SPARQL_PATH, "SPARQL HTTP Service");
     } });
     return starters;
   }
 
 
   /**
    * Adds a listener to the <code>httpServer</code>. The listener is created and configured
    * according to the Jetty configuration.
    * @param httpServer the server to add the listener to
    * @throws UnknownHostException if an invalid hostname was specified in the Mulgara server configuration
    */
   private void addConnector(Server httpServer, JettyConnector jettyConfig) throws UnknownHostException {
     if (httpServer == null) throw new IllegalArgumentException("Null \"httpServer\" parameter");
 
     if (logger.isDebugEnabled()) logger.debug("Adding socket listener");
 
     // create and configure a listener
     AbstractConnector connector = new BlockingChannelConnector();
     if ((hostName != null) && !hostName.equals("")) {
       connector.setHost(hostName);
       if (logger.isDebugEnabled()) logger.debug("Servlet container listening on host " + hostName);
     } else {
       hostName = EmbeddedMulgaraServer.getResolvedLocalHost();
       if (logger.isDebugEnabled()) logger.debug("Servlet container listening on all host interfaces");
     }
 
 
     if (jettyConfig.hasPort()) connector.setPort(jettyConfig.getPort());
     if (jettyConfig.hasMaxIdleTimeMs()) connector.setMaxIdleTime(jettyConfig.getMaxIdleTimeMs());
     if (jettyConfig.hasLowResourceMaxIdleTimeMs()) connector.setLowResourceMaxIdleTime(jettyConfig.getLowResourceMaxIdleTimeMs());
     if (jettyConfig.hasAcceptors()) {
       int acceptors = jettyConfig.getAcceptors();
       if (acceptors > WEIRD_JETTY_THREAD_LIMIT) {
         logger.warn("Acceptor threads set beyond HTTP Server limits. Reducing from" + acceptors + " to " + WEIRD_JETTY_THREAD_LIMIT);
         acceptors = WEIRD_JETTY_THREAD_LIMIT;
       }
       connector.setAcceptors(acceptors);
     }
 
     // add the listener to the http server
     httpServer.addConnector(connector);
   }
 
 
   /**
    * Creates the Mulgara Descriptor UI
    * @throws IOException if the driver WAR file is not readable
    */
   private Service addWebServicesWebAppContext(Server server) throws IOException {
     // get the URL to the WAR file
     URL webServicesWebAppURL = ClassLoader.getSystemResource(WEBAPP_PATH + "/" + WEBSERVICES_WEBAPP);
 
     if (webServicesWebAppURL == null) {
       logger.warn("Couldn't find resource: " + WEBAPP_PATH + "/" + WEBSERVICES_WEBAPP);
       return null;
     }
 
     String warPath = extractToTemp(WEBAPP_PATH + "/" + WEBSERVICES_WEBAPP);
     
     // Add Descriptors and Axis
     String webPath = "/" + WEBSERVICES_PATH;
     WebAppContext descriptorWARContext = new WebAppContext(server, warPath, webPath);
 
     // make some attributes available
     descriptorWARContext.setAttribute(BOUND_HOST_NAME_KEY, ServerInfo.getBoundHostname());
     descriptorWARContext.setAttribute(SERVER_MODEL_URI_KEY, ServerInfo.getServerURI().toString());
 
     // log that we're adding the test webapp context
     if (logger.isDebugEnabled()) logger.debug("Added Web Services webapp context");
     return new Service("Web Services", webPath);
   }
 
 
   /**
    * Creates and registers a web servlet.
    * @throws IOException if the servlet cannot talk to the network.
    */
   private Service addWebQueryContext(Server server, String name, String servletClass, String servletPath) throws IOException {
     if (logger.isDebugEnabled()) logger.debug("Adding Web servlet context: " + name);
 
     // create the web context
     try {
       AbstractServer serverMBean = (AbstractServer)hostServer.getServerMBean();
       String rmiName = hostServer.getServerName();
       Servlet servlet = (Servlet)Reflect.newInstance(Class.forName(servletClass), hostName, rmiName, serverMBean);
       String webPath = "/" + servletPath;
       new org.mortbay.jetty.servlet.Context(server, webPath, SESSIONS).addServlet(new ServletHolder(servlet), "/*");
       return new Service(name, webPath);
     } catch (ClassNotFoundException e) {
       throw new IllegalStateException("Not configured to use the requested servlet: " + name + "(" + servletClass + ")");
     }
   }
 
 
   /**
    * Creates a servlet that requires a Server as a constructor parameter.
    * @param server The server needed by the servlet.
    * @param servletClass The name of the servlet class.
    * @param path A relative HTTP path for attaching the servlet. 
    * @param description A description for the servlet.
    * @return The Service running the new servlet.
    * @throws IOException Due to problems with the file system or the network.
    * @throws IllegalStateException if an unavailable servlet has been requested.
    */
   private Service addServletContext(Server server, String servletClass, String path, String description) throws IOException {
     if (logger.isDebugEnabled()) logger.debug("Adding " + description + " servlet context");
 
     // create the web query context
     try {
       Servlet servlet = (Servlet)Reflect.newInstance(Class.forName(servletClass), hostServer);
       String webPath = "/" + path;
       new org.mortbay.jetty.servlet.Context(server, webPath, SESSIONS).addServlet(new ServletHolder(servlet), "/*");
       return new Service(description, webPath);
     } catch (ClassNotFoundException e) {
       throw new IllegalStateException("Not configured to use the requested servlet: " + description);
     }
   }
 
   /**
    * Creates the servlet used to list the other servlets.
    * @param server The server to register this servlet with.
    * @param services The list of service names and paths.
    * @throws IOException If the servlet cannot talk to the network.
    */
   private void addWebServiceListingContext(Server server, Map<String,String> services) throws IOException {
     if (logger.isDebugEnabled()) logger.debug("Adding the service lister context");
     Servlet servlet = new ServiceListingServlet(services, "/" + DEFAULT_SERVICE);
     new org.mortbay.jetty.servlet.Context(server, "/", SESSIONS).addServlet(new ServletHolder(servlet), "/*");
   }
 
 
   /**
    * Extracts a resource from the environment (a jar in the classpath) and writes
    * this to a file in the working temporary directory.
    * @param resourceName The name of the resource. This is a relative file path in the jar file.
    * @return The absolute path of the file the resource is extracted to, or <code>null</code>
    *         if the resource does not exist.
    * @throws IOException If there was an error reading the resource, or writing to the extracted file.
    */
   private String extractToTemp(String resourceName) throws IOException {
     // Find the resource
     URL resourceUrl = ClassLoader.getSystemResource(resourceName);
     if (resourceUrl == null) return null;
 
     // open the resource and the file where it will be copied to
     InputStream in = resourceUrl.openStream();
     File outFile = new File(TempDir.getTempDir(), new File(resourceName).getName());
     logger.info("Extracting: " + resourceUrl + " to " + outFile);
     OutputStream out = new FileOutputStream(outFile);
 
     // loop to copy from the resource to the output file
     byte[] buffer = new byte[10240];
     int len;
     while ((len = in.read(buffer)) >= 0) out.write(buffer, 0, len);
     in.close();
     out.close();
 
     // return the file that the resource was extracted to
     return outFile.getAbsolutePath();
   }
 
 
   /**
    * A common class for representing the identical configuration found in the
    * separate Connector and PublicConnector classes.
    */
   private class JettyConnector {
     boolean provided = false;
     Boolean disabled = null;
     String host = null;
     Integer port = null;
     Integer acceptors = null;
     Integer maxIdleTimeMs = null;
     Integer lowResourceMaxIdleTimeMs = null;
 
     /**
      * Creates a config from a Connector object.
      * @param c The Connector to build the config from.
      */
     public JettyConnector(Connector c) {
       if (c == null) return;
       provided = true;
       if (c.hasDisabled()) disabled = c.isDisabled();
       host = c.getHost();
       if (c.hasPort()) port = c.getPort();
       if (c.hasAcceptors()) acceptors = c.getAcceptors();
       if (c.hasMaxIdleTimeMs()) maxIdleTimeMs = c.getMaxIdleTimeMs();
       if (c.hasLowResourceMaxIdleTimeMs()) lowResourceMaxIdleTimeMs = c.getLowResourceMaxIdleTimeMs();
     }
 
     /**
      * Creates a config from a PublicConnector object.
      * @param c The PublicConnector to build the config from.
      */
     public JettyConnector(PublicConnector c) {
       if (c == null) return;
       provided = true;
       if (c.hasDisabled()) disabled = c.isDisabled();
       host = c.getHost();
       if (c.hasPort()) port = c.getPort();
       if (c.hasAcceptors()) acceptors = c.getAcceptors();
       if (c.hasMaxIdleTimeMs()) maxIdleTimeMs = c.getMaxIdleTimeMs();
       if (c.hasLowResourceMaxIdleTimeMs()) lowResourceMaxIdleTimeMs = c.getLowResourceMaxIdleTimeMs();
     }
 
     /** @return if this config was provided. */
     public boolean isProvided() {
       return provided;
     }
 
     /** @return if this config is disabled or not. */
     public boolean isDisabled() {
       return disabled;
     }
 
     /** @return the host name */
     public String getHost() {
       return host;
     }
 
     /** @return the port to use */
     public int getPort() {
       return port;
     }
 
     /** @return the number of acceptors to use */
     public int getAcceptors() {
       return acceptors;
     }
 
     /** @return the maxIdleTimeMs */
     public int getMaxIdleTimeMs() {
       return maxIdleTimeMs;
     }
 
     /** @return the lowResourceMaxIdleTimeMs */
     public int getLowResourceMaxIdleTimeMs() {
       return lowResourceMaxIdleTimeMs;
     }
 
     /** @return <code>true</code> if the Disabled value was provided. */
     public boolean hasDisabled() {
       return disabled != null;
     }
 
     /** @return <code>true</code> if the Port value was provided. */
     public boolean hasPort() {
       return port != null;
     }
 
     /** @return <code>true</code> if the Accepted value was provided. */
     public boolean hasAcceptors() {
       return acceptors != null;
     }
 
     /** @return <code>true</code> if the MaxIdelTimeMs value was provided. */
     public boolean hasMaxIdleTimeMs() {
       return maxIdleTimeMs != null;
     }
 
     /** @return <code>true</code> if the LogResourceMaxIdeTimeMs value was provided. */
     public boolean hasLowResourceMaxIdleTimeMs() {
       return lowResourceMaxIdleTimeMs != null;
     }
   }
 }
