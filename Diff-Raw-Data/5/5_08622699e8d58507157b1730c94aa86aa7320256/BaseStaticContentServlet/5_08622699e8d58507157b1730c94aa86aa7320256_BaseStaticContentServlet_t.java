 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 
 import java.net.URL;
 import java.net.URLDecoder;
 
 import java.util.Calendar;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Enumeration;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.Validate;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** A base servlet used to serve static content (gif, png, css, etc) from the
  * classpath.
  *
  * This class handles all the process related to sending the content to the
  * client. The location of the content is delegated to the subclasses.
  *
  * Subclasses must implement findInputStream and getContentType.
  *
  * You must call this class init(ServletConfig) if you override it.
  *
  * This accepts the following configuration parameters:<br>
  *
  * requestCacheContent: whether to send the cache headers to the client with an
  * expiration date in the future, or the headers that state that the content
  * should not be cached (true / false). It is false by default.<br>
  *
  * debug: whether to enable debug mode or not. In debug mode, the servlet
  * attempts to load the requested content directly from the file system. This
  * makes it possible to edit the resources directly from disk and see the
  * results inmediately without a redeploy. It is false by default.<br>
  *
  * All other initialization parameters are ignored, so subclasses can define
  * adittional config parameters.
  */
 public abstract class BaseStaticContentServlet extends HttpServlet {
 
   /** The serialization version number.
    *
    * This number must change every time a new serialization incompatible change
    * is introduced in the class.
    */
   private static final long serialVersionUID = 1;
 
   /** The buffer size used to transfer bytes to the client.
    */
   private static final int BUFFER_SIZE = 4096;
 
   /** The class logger.
    */
   private static Logger log = LoggerFactory.getLogger(
       BaseStaticContentServlet.class);
 
   /** Provide a formatted date for setting heading information when caching
    * static content.
    */
   private final Calendar lastModified = Calendar.getInstance();
 
   /** Whether to send the client the cache header asking to cache the content
    * served by this servlet or not.
    */
   private boolean requestCacheContent = false;
 
   /** Whether debug mode is enabled.
    *
    * Initialized from the debug servlet parameter.
    */
   private boolean debug = false;
 
   /** Initializes the servlet.
    *
    * It sets the default packages for static resources.
    *
    * @param config The servlet configuration. It cannot be null.
    */
   public void init(final ServletConfig config) throws ServletException {
     log.trace("Entering init");
 
     Validate.notNull(config, "The servlet config cannot be null.");
 
     String applyCacheInfo = config.getInitParameter("requestCacheContent");
     requestCacheContent = Boolean.valueOf(applyCacheInfo);
 
     String debugValue = config.getInitParameter("debug");
     debug = Boolean.valueOf(debugValue);
 
     log.trace("Leaving init");
   }
 
   /** Serves a get request.
    *
    * @param request The request object.
    *
    * @param response The response object.
    *
    * @throws IOException in case of an io error.
    *
    * @throws ServletException in case of error.
    */
   @Override
   protected void doGet(final HttpServletRequest request, final
       HttpServletResponse response) throws ServletException, IOException {
     serveStaticContent(request, response);
   }
 
   /** Serves a post request.
    *
    * @param request The request object.
    *
    * @param response The response object.
    *
    * @throws IOException in case of an io error.
    *
    * @throws ServletException in case of error.
    */
   @Override
   protected void doPost(final HttpServletRequest request, final
       HttpServletResponse response) throws ServletException, IOException {
     serveStaticContent(request, response);
   }
 
   /** Serves some static content.
    *
    * @param request The request object.
    *
    * @param response The response object.
    *
    * @throws IOException in case of an io error.
    *
    * @throws ServletException in case of error.
    *
    * TODO See if it shuold use pathInfo instead of servletPath.
    */
   private void serveStaticContent(final HttpServletRequest request, final
       HttpServletResponse response) throws ServletException, IOException {
     log.trace("Entering serveStaticContent");
     String resourcePath = getServletPath(request);
     findStaticResource(resourcePath, request, response);
     log.trace("Leaving serveStaticContent");
   }
 
   /** Locate a static resource and copy directly to the response, setting the
    * appropriate caching headers.
    *
    * A URL decoder is run on the resource path and it is configured to use the
    * UTF-8 encoding because according to the World Wide Web Consortium
    * Recommendation UTF-8 should be used and not doing so may introduce
    * incompatibilites.
    *
    * @param theName The resource name
    *
    * @param request The request
    *
    * @param response The response
    *
    * @throws IOException If anything goes wrong
    */
   private void findStaticResource(final String theName, final
       HttpServletRequest request, final HttpServletResponse response) throws
       IOException {
     log.trace("Entering findStaticResource('{}', ...)", theName);
 
     String name = URLDecoder.decode(theName, "UTF-8");
 
     // Checks if the requested resource matches a recognized content type.
     String contentType = getContentType(name);
     if (contentType == null) {
       response.sendError(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write(
          "<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN'"
          + " 'http://www.w3.org/TR/html4/strict.dtd'>"
          + "<html><head><title>404</title></head>"
           + "<body>Resource not found</body></html>");
       log.trace("Leaving findStaticResource with SC_NOT_FOUND");
       response.flushBuffer();
       return;
     }
 
     // Looks for the resource.
     InputStream is = findInputStream(name);
     if (is == null) {
       response.sendError(HttpServletResponse.SC_NOT_FOUND);
       log.trace("Leaving findStaticResource with SC_NOT_FOUND");
       response.getWriter().write(
           "<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01//EN'"
           + " 'http://www.w3.org/TR/html4/strict.dtd'> "
           + "<html><head><title>404</title></head>"
           + "<body>Resource not found</body></html>");
       log.trace("Leaving findStaticResource with SC_NOT_FOUND");
       response.flushBuffer();
       return;
     }
 
     Calendar cal = Calendar.getInstance();
     // check for if-modified-since, prior to any other headers
     long requestedOn = 0;
     try {
       requestedOn = request.getDateHeader("If-Modified-Since");
     } catch (Exception e) {
       log.warn("Invalid If-Modified-Since header value: '"
           + request.getHeader("If-Modified-Since") + "', ignoring");
     }
     long lastModifiedMillis = lastModified.getTimeInMillis();
     long now = cal.getTimeInMillis();
     cal.add(Calendar.DAY_OF_MONTH, 1);
     long expires = cal.getTimeInMillis();
 
     boolean notModified;
     notModified =  0 < requestedOn && requestedOn <= lastModifiedMillis;
 
     if (!debug && notModified) {
       // not modified, content is not sent - only basic headers and status
       // SC_NOT_MODIFIED
       response.setDateHeader("Expires", expires);
       response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
       is.close();
 
       log.trace("Leaving findStaticResource with SC_NOT_MODIFIED");
       return;
     }
 
     // set the content-type header
     response.setContentType(contentType);
 
     if (!debug && requestCacheContent) {
       // set heading information for caching static content
       response.setDateHeader("Date", now);
       response.setDateHeader("Expires", expires);
       response.setDateHeader("Retry-After", expires);
       response.setHeader("Cache-Control", "public");
       response.setDateHeader("Last-Modified", lastModifiedMillis);
     } else {
       response.setHeader("Cache-Control", "no-cache");
       response.setHeader("Pragma", "no-cache");
       response.setHeader("Expires", "-1");
     }
 
     try {
       copy(is, response.getOutputStream());
     } finally {
       is.close();
     }
     log.trace("Leaving findStaticResource");
   }
 
   /**
    * Determine the content type for the resource name.
    *
    * @param name The resource name. It cannot be null.
    *
    * @return The mime type, null if the resource name is not recognized.
    */
   protected abstract String getContentType(final String name);
 
   /**
    * Copy bytes from the input stream to the output stream.
    *
    * @param input The input stream
    * @param output The output stream
    * @throws IOException If anytSrtringhing goes wrong
    */
   private void copy(final InputStream input, final OutputStream output) throws
       IOException {
     final byte[] buffer = new byte[BUFFER_SIZE];
     int n;
     while (-1 != (n = input.read(buffer))) {
         output.write(buffer, 0, n);
     }
     output.flush();
   }
 
   /** Look for a static resource in the classpath.
    *
    * In debug mode, it looks for the resource in the file system, using
    * debugPrefix as the base file name.
    *
    * @param name The resource name. It cannot be null.
    *
    * @return the inputstream of the resource, null if the resource could not be
    * found.
    *
    * @throws IOException If there is a problem locating the resource.
    */
   protected abstract InputStream findInputStream(final String name)
     throws IOException;
 
   /** Concatenates two path names.
    *
    * This is protected as an aid for subclasses.
    *
    * @param prefix The first component of the file name. It cannot be null.
    *
    * @param name The second component of the file name. It cannot be null.
    *
    * @return A file name of the form prefix/name with the correct number of /.
    */
   protected String buildPath(final String prefix, final String name) {
     Validate.notNull(prefix, "The file component prefix cannot be null.");
     Validate.notNull(name, "The second file component cannot be null.");
 
     if (prefix.endsWith("/") && name.startsWith("/")) {
       return prefix + name.substring(1);
     } else if (prefix.endsWith("/") || name.startsWith("/")) {
       return prefix + name;
     }
     return prefix + "/" +  name;
   }
 
   /** This is a convenience method to load a resource as a stream.
    *
    * The algorithm used to find the resource is given in getResource().
    *
    * @param resourceName The name of the resource to load. It cannot be null
    * nor start with '/'.
    *
    * @return Returns an input stream representing the resource, null if not
    * found.
    */
   protected InputStream getResourceAsStream(final String resourceName) {
     Validate.notNull(resourceName, "The resource name cannot be null.");
     Validate.isTrue(!resourceName.startsWith("/"),
         "The resource cannot start with /");
     URL url = getResource(resourceName);
     if (url == null) {
       return null;
     }
     try {
       return url.openStream();
     } catch (IOException e) {
       log.debug("Exception opening resource: " + resourceName, e);
       return null;
     }
   }
 
   /**
    * Load a given resource.
    * <p/>
    * This method will try to load the resource using the following methods (in
    * order):
    *
    * <ul>
    *
    * <li>From {@link Thread#getContextClassLoader()
    * Thread.currentThread().getContextClassLoader()}
    *
    * <li>From the {@link Class#getClassLoader() getClass().getClassLoader() }
    *
    * </ul>
    *
    * @param resourceName The name of the resource to load
    *
    * @return Returns the url of the reesource, null if not found.
    */
   protected URL getResource(final String resourceName) {
     URL url = null;
 
     // Try the context class loader.
     ClassLoader contextClassLoader;
     contextClassLoader = Thread.currentThread().getContextClassLoader();
     if (null != contextClassLoader) {
       url = contextClassLoader.getResource(resourceName);
     }
 
     // Try the current class class loader if the context class loader failed.
     if (url == null) {
       url = getClass().getClassLoader().getResource(resourceName);
     }
 
     return url;
   }
 
   /**
    * Retrieves the current request servlet path.
    * Deals with differences between servlet specs (2.2 vs 2.3+)
    *
    * @param request the request
    * @return the servlet path
    */
   private String getServletPath(final HttpServletRequest request) {
     String servletPath = request.getServletPath();
 
     if (null != servletPath && !"".equals(servletPath)) {
       return servletPath;
     }
 
     String requestUri = request.getRequestURI();
     int startIndex = request.getContextPath().length();
     int endIndex = 0;
     if (request.getPathInfo() == null) {
       endIndex = requestUri.length();
     } else {
       endIndex = requestUri.lastIndexOf(request.getPathInfo());
     }
     if (startIndex > endIndex) { // this should not happen
       endIndex = startIndex;
     }
     return requestUri.substring(startIndex, endIndex);
   }
 
   /** True if in debug mode.
    *
    * @return true in debug mode.
    */
   public boolean isInDebugMode() {
     return debug;
   }
 }
 
