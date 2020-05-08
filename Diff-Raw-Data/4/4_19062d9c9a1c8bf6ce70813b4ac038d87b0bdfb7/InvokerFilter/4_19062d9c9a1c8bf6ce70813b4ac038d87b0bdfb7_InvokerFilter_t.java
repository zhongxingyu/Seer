 /*
  * InvokerFilter.java
  *
  * Created on June 13th, 2008, 12:54 PM
  *
  */
 package org.j2free.servlet.filter;
 
 import java.io.*;
 import java.net.URL;
 
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CountDownLatch;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.j2free.annotations.URLMapping;
 
 import org.j2free.annotations.URLMapping.SSLOption;
 import org.j2free.jpa.Controller;
 import org.j2free.jpa.ControllerServlet;
 
 
 import static org.j2free.util.ServletUtils.*;
 import static org.j2free.util.Constants.*;
 
 import org.scannotation.AnnotationDB;
 import org.scannotation.ClasspathUrlFinder;
 
 /**
  *
  * @author  Ryan
  */
 public class InvokerFilter implements Filter {
 
     private static final Log log = LogFactory.getLog(InvokerFilter.class);
 
     private static final class Static extends HttpServlet { }
 
     /* Ignored User-Agents
     panscient.com
     Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)
     nutchsearch/Nutch-0.9 (Nutch Search 1.0; herceg_novi at yahoo dot com)
     Yahoo! Slurp
     Powerset
     Ask Jeeves/Teoma
     msnbot
     Mozilla/5.0 (compatible; MJ12bot/v1.2.1; http://www.majestic12.co.uk/bot.php?+)
     Mozilla/5.0 (Twiceler-0.9 http://www.cuill.com/twiceler/robot.html)
     Gigabot/3.0 (http://www.gigablast.com/spider.html)
     Mozilla/5.0 (compatible; attributor/1.13.2 +http://www.attributor.com)
     lwp-request/2.07
      */
     private static final String IGNORED_AGENTS =
                                 ".*?(" +
                                 "(spider|robot|bot)\\.[a-z]*?|" +
                                 "panscient|" +
                                 "Googlebot|" +
                                 "nutchsearch|" +
                                 "Yahoo! Slurp|" +
                                 "powerset|" +
                                 "Ask Jeeves/Teoma|" +
                                 "msnbot|" +
                                 "Twiceler|" +
                                 "attributor|" +
                                 ").*?";
 
 
     private static final AtomicBoolean benchmark                     = new AtomicBoolean(false);
     private static final AtomicInteger sslRedirectPort               = new AtomicInteger(-1);
     private static final AtomicInteger nonSslRedirectPort            = new AtomicInteger(-1);
 
     private static final AtomicReference<String> controllerClassName = new AtomicReference(EMPTY);
     private static final AtomicReference<String> bypassPath          = new AtomicReference(EMPTY);
 
     // ConcurrentMap holds mappings from urls to classes (specifically)
     private static final ConcurrentMap<String, Class<? extends HttpServlet>> urlMap = 
             new ConcurrentHashMap<String, Class<? extends HttpServlet>>(30000,0.8f,50);
 
     // A map of regex's to test urls against
     private static final ConcurrentMap<String,Class<? extends HttpServlet>> regexMap =
             new ConcurrentHashMap<String,Class<? extends HttpServlet>>();
 
     // A set of klass's that require an SSL connection
     private static final ConcurrentHashMap<Class<? extends HttpServlet>, SSLOption> sslMap =
             new ConcurrentHashMap<Class<? extends HttpServlet>, SSLOption>(100);
 
     private static final CountDownLatch latch   = new CountDownLatch(1);
     private static final AtomicBoolean  enabled = new AtomicBoolean(false);
 
     // The filter configuration object we are associated with.  If
     // this value is null, this filter instance is not currently
     // configured.
     private FilterConfig filterConfig = null;
 
     /**
      *
      * @param request The servlet request we are processing
      * @param result The servlet response we are creating
      * @param chain The filter chain we are processing
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a servlet error occurs
      */
     public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
             throws IOException, ServletException {
 
         HttpServletRequest request   = (HttpServletRequest) req;
         HttpServletResponse response = (HttpServletResponse) resp;
 
         String currentPath = request.getRequestURI().replaceFirst(request.getContextPath(), "");
 
         long start   = System.currentTimeMillis(),  // start time
              find    = 0,                           // time after figuring out what to do
              run     = 0,                           // time after processing
              finish  = 0;                           // finish time
 
         // Set cache-control based on content
         if (currentPath.matches(".*?\\.(jpg|gif|png|jpeg)") && !currentPath.contains("captcha.jpg")) {
             response.setHeader("Cache-Control", "max-age=3600");
             response.setHeader("Pragma", "cache");
         } else if (currentPath.matches(".*?\\.(swf|js|css|flv)")) {
             response.setHeader("Cache-Control", "max-age=31449600");
             response.setHeader("Pragma", "cache");
         }
 
         // This will let all threads through if the Invoker isn't enabled
         if (!enabled.get()) {
             log.debug("InvokerFilter disabled");
             chain.doFilter(req, resp);
             return;
         }
 
         // This will block all threads until the gate opens
         try {
             latch.await();
         } catch (InterruptedException ie) {
             log.warn("Interrupted while waiting for urlMap to be initialized...");
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             return;
         }
 
         // Get the mapping
         Class<? extends HttpServlet> klass = urlMap.get(currentPath);
 
         /*
          * If we don't already have an exact match for this path,
          * try to break it down.
          * 
          * Certain extensions are known to be mapped in web.xml,
          * known to never be dynamic resources (e.g. .swf), or
          * were discovered earlier to be static content, so don't
          * process those.
          */
         if (klass == null && !currentPath.matches(bypassPath.get())) {
 
             if (log.isDebugEnabled())
                 log.debug("InvokerFilter for path: " + currentPath);
 
             // (1) If the exact match wasn't found, look for wildcard matches
 
             String partial;
 
             // If the path contains a "." then check for the *.ext patterns
             if (currentPath.indexOf(".") != -1) {
                 partial = currentPath.substring(currentPath.lastIndexOf("."));
                 klass = urlMap.get(partial);
             }
 
             // (2) If we still haven't found it, check if any
             // registered regex mappings against the path
             if (klass == null) {
 
                 String regex;
                 Iterator<String> itr = regexMap.keySet().iterator();
                 while (itr.hasNext()) {
 
                     regex = itr.next();
 
                     if (currentPath.matches(regex)) {
                         klass = regexMap.get(regex);
 
                         // gotta make sure the klass was still in there, since it could have been altered
                         // since we got the iterator... (although, likely it wasn't)
                         if (klass != null)
                             break;
                     }
                 }
             }
 
             // (3) If we didn't find a .* pattern and if the path includes
             // a / then it's possible there is a mapping to a /* pattern
             if (klass == null && currentPath.lastIndexOf("/") > 0) {
 
                 partial = currentPath.substring(0, currentPath.lastIndexOf("/") + 1);
 
                 // check for possible /*, /something/* patterns starting with most specific
                 // and moving down to /*
                 while (partial.lastIndexOf("/") > 0) {
 
                     if (log.isDebugEnabled()) log.debug("trying to find wildcard resource " + partial);
 
                     klass = urlMap.get(partial);
 
                     // if we found a match, get out of this loop asap
                     if (klass != null)
                         break;
 
                     // if it's only a slash and it wasn't found, get out asap
                     if (partial.equals("/"))
                         break;
 
                     // chop the ending '/' off
                     partial = partial.substring(0, partial.length() - 2);
 
                     // then set the string to be the remnants
                     partial = partial.substring(0, partial.lastIndexOf("/") + 1);
                 }
             }
 
             // (4) If we found a class in any way, register it with the currentPath for faster future lookups
             if (klass != null) {
                 if (log.isDebugEnabled())
                     log.debug("Matched path " + currentPath + " to " + klass.getName());
 
                 urlMap.putIfAbsent(currentPath, klass);
             }
         }
 
         // If we didn't find it, then just pass it on
         if (klass == null) {
 
             if (log.isDebugEnabled()) log.debug("Dynamic resource not found for path: " + currentPath);
 
             // Save this path in the staticSet so we don't have to look it up next time
             urlMap.putIfAbsent(currentPath,Static.class);
 
             find = System.currentTimeMillis();
             chain.doFilter(req, resp);
             run  = System.currentTimeMillis();
 
         } else if (klass == Static.class) {
 
             // If it's known to be static, then pass it on
             if (log.isDebugEnabled())
                 log.debug("Processing known static path: " + currentPath);
 
             find = System.currentTimeMillis();
             chain.doFilter(req, resp);
             run  = System.currentTimeMillis();
 
         } else {
 
             // If the klass requires SSL, make sure we're on an SSL connection
             SSLOption sslOpt = sslMap.get(klass);
             boolean isSsl    = request.isSecure();
             if (sslOpt == SSLOption.REQUIRE && !isSsl) {
                 redirectOverSSL(request, response, sslRedirectPort.get());
                 return;
             } else if (sslOpt == SSLOption.DENY && isSsl) {
                 redirectOverNonSSL(request, response, nonSslRedirectPort.get());
                 return;
             }
 
             Controller controller = null;
 
             try {
 
                 // This is a hack to create a servlet config to
                 // properly initialize the servlet.
                 final String className = klass.getSimpleName();
                 ServletConfig servletConfig = new ServletConfig() {
 
                     public String getServletName() {
                         return className;
                     }
 
                     public ServletContext getServletContext() {
                         return filterConfig.getServletContext();
                     }
 
                     public String getInitParameter(String arg0) {
                         return null;
                     }
 
                     public Enumeration getInitParameterNames() {
                         return null;
                     }
                 };
 
                 if (klass.getSuperclass() == ControllerServlet.class) {
 
                     if (controllerClassName.get().equals(EMPTY)) {
                         controller = new Controller();
                     } else {
                         controller = (Controller) (Class.forName(controllerClassName.get()).newInstance());
                     }
 
                     if (log.isDebugEnabled())
                         log.debug("Dynamic resource found, instance of ControllerServlet: " + klass.getName());
 
                     ControllerServlet servlet = (ControllerServlet) klass.newInstance();
                     servlet.init(servletConfig);
 
                     find = System.currentTimeMillis();
 
                     controller.startTransaction();
 
                     if (!controller.isTransactionOpen()) {
                         log.error("Cannot service reuest, tx not open [" +
                                     "url=" + currentPath + ", " +
                                     "servlet=" + klass.getSimpleName() + ", " +
                                     "tx.status=" + controller.getTransactionStatus() +
                                   "]");
 
                         response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                         return;
                     }
 
                     servlet.setController(controller);
                     request.setAttribute("controller", controller);
 
                     try {
                         servlet.service(request, response);
                     } finally {
                         controller.endTransaction();
                     }
 
                     run  = System.currentTimeMillis();
 
                     if (request.getParameter("benchmark") != null) {
                         log.info(klass.getName() + " execution time: " + (System.currentTimeMillis() - start));
                     }
 
                 } else {
 
                     if (log.isDebugEnabled())
                         log.debug("Dynamic resource found, instance of HttpServlet, servicing with " + klass.getName());
 
                     HttpServlet servlet = (HttpServlet)klass.newInstance();
                     servlet.init(servletConfig);
 
                     find = System.currentTimeMillis();
                     servlet.service(request, response);
                     run  = System.currentTimeMillis();
 
                 }
 
             } catch (Exception e) {
 
                 run = System.currentTimeMillis();
 
                 if (RUN_MODE.compareTo(RunMode.PRODUCTION) < 1) {
                     log.error("Error servicing " + currentPath, e);
                 } else {
                     String userAgent = request.getHeader("User-Agent");
                     if (userAgent != null && !userAgent.matches(IGNORED_AGENTS)) {
                         /*
                         Emailer mailer = Emailer.getInstance();
                         String exceptionReport = describeRequest(request) + "\n\nStack Trace:\n" + ServletUtils.throwableToString(e);
                         try {
                         mailer.sendPlain("ryan@foobrew.com,arjun@foobrew.com","Exception in FilterChain " + new Date().toString(),exceptionReport);
                         } catch (Exception e0) {
                         log.fatal("Error sending Exception report email. Content follows:\n\n" + exceptionReport);
                         }
                          */
                         log.error("Error servicing " + currentPath, e);
                     }
                 }
             }
         }
 
         finish = System.currentTimeMillis();
 
         if (benchmark.get())
             log.info(start + "\t" + (find - start) + "\t" + (run - find) + "\t" + (finish - run) + "\t" + currentPath);
     }
 
     public InvokerFilter() {
     }
 
     /**
      * Return the filter configuration object for this filter.
      */
     public FilterConfig getFilterConfig() {
         return (this.filterConfig);
     }
 
     /**
      * Set the filter configuration object for this filter.
      *
      * @param filterConfig The filter configuration object
      */
     public void setFilterConfig(FilterConfig filterConfig) {
         this.filterConfig = filterConfig;
     }
 
     /**
      * Destroy method for this filter
      *
      */
     public void destroy() {
     }
 
     /**
      *  @description Finds all classes annotated with URLMapping and maps the class to
      *               the url specified in the annotation.  Wildcard urls are allowed in
      *               the form of *.extension or /some/path/*
      */
     public void init(FilterConfig filterConfig) {
         this.filterConfig = filterConfig;
     }
 
     public static void enable(String bypass, String controllerClass, boolean doBenchmark, Integer nonSslPort, Integer sslPort) {
 
         if (enabled.get())
             return;
 
         log.debug("Enabling InvokerFilter...");
 
         // In case the user has let us know about paths that are guaranteed static
         bypassPath.set(bypass);
 
         // In case the application using J2Free has extended Controller...
         controllerClassName.set(controllerClass);
 
         // Can enable benchmarking globally
         benchmark.set(doBenchmark);
 
         // Set the SSL redirect port
         if (sslPort != null && sslPort > 0)
             sslRedirectPort.set(sslPort);
 
         // Set the SSL redirect port
         if (nonSslPort != null && nonSslPort > 0)
             nonSslRedirectPort.set(nonSslPort);
 
         // (1) Look for any classes annotated with @URLMapping
         try {
             LinkedList<URL> urlList = new LinkedList<URL>();
             urlList.addAll(Arrays.asList(ClasspathUrlFinder.findResourceBases("")));
             
             URL[] urls = new URL[urlList.size()];
             urls = urlList.toArray(urls);
 
             AnnotationDB aDB = new AnnotationDB();
             aDB.setScanClassAnnotations(true);
             aDB.setScanFieldAnnotations(false);
             aDB.setScanMethodAnnotations(false);
             aDB.setScanParameterAnnotations(false);
             aDB.scanArchives(urls);
 
             HashMap<String, Set<String>> annotationIndex = (HashMap<String, Set<String>>) aDB.getAnnotationIndex();
             if (annotationIndex != null && !annotationIndex.isEmpty()) {
                 Set<String> classNames = annotationIndex.get("org.j2free.annotations.URLMapping");
                 if (classNames != null) {
                     for (String c : classNames) {
                         try {
                             
                             Class<? extends HttpServlet> klass = (Class<? extends HttpServlet>)Class.forName(c);
                             
                             if (klass.isAnnotationPresent(URLMapping.class)) {
 
                                 URLMapping anno = (URLMapping) klass.getAnnotation(URLMapping.class);
 
                                 if (anno.urls() != null) {
                                     for (String url : anno.urls()) {
 
                                         if (url.matches("(^\\*[^*]*?)|([^*]*?/\\*$)"))
                                             url = url.replaceAll("\\*", "");
 
                                         if (urlMap.putIfAbsent(url, klass) == null)
                                             log.debug("Mapping servlet " + klass.getName() + " to path " + url);
                                         else
                                             log.error("Unable to map servlet  " + klass.getName() + " to path " + url + ", path already mapped to " + urlMap.get(url).getName());
 
                                     }
                                 }
 
                                 if (!empty(anno.regex())) {
                                     regexMap.putIfAbsent(anno.regex(), klass);
                                     log.debug("Mapping servlet " + klass.getName() + " to regex path " + anno.regex());
                                 }
 
                                 if (anno.ssl() != SSLOption.OPTIONAL) {
                                    if (anno.ssl() == SSLOption.REQUIRE) {
                                        log.warn("Servlet " + klass.getName() + " will only accept SSL connections.");
                                    }
                                     sslMap.put(klass, anno.ssl());
                                 }
                             }
                             
                         } catch (ClassNotFoundException e) {
                             log.error("Error processing URLMapping",e);
                         }
                     }
                 }
             }
         } catch (IOException e) {
             log.error("Error initializing urlMappings",e);
         }
 
         // Mark that we've run this and open the gate
         enabled.set(true);
         latch.countDown();
     }
 
     /**
      * @param path The path to map a Servlet to
      * @param klass The Servlet to map
      * @return null if the servlet was mapped to the path, or a class if another servlet was already mapped to that path
      */
     public static Class<? extends HttpServlet> addServletMapping(String path, Class<? extends HttpServlet> klass, SSLOption sslOpt) {
 
         log.debug("Mapping servlet " + klass.getName() + " to path " + path);
         
         if (path.matches("(^\\*[^*]*?)|([^*]*?/\\*$)"))
             path = path.replaceAll("\\*","");
         
         Class<? extends HttpServlet> result = urlMap.putIfAbsent(path, klass);
         if (result == null && sslOpt != SSLOption.OPTIONAL) {
             sslMap.put(klass, sslOpt);
         }
         return result;
     }
 }
