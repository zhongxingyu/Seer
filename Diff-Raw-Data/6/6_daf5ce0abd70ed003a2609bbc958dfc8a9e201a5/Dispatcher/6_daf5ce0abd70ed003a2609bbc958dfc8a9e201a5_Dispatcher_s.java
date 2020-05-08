 /* Copyright (c) 2009 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz;
 import java.io.*;
 import java.lang.reflect.*;
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import org.apache.log4j.*;
 
 /**
  * The Dispatcher class is used internally by Fiz to provide the top-level
  * entry point for handling requests.  Its main job is to pick another class
  * and method to handle the request, based on information in the URL, and
  * invoke that method.  This class also serves as a last-ditch handler
  * for errors that occur during the request.  This class should be
  * invisible to Fiz applications.
  */
 public class Dispatcher extends HttpServlet {
     /**
      * UnsupportedUriError is thrown when a URL arrives that can't
      * be mapped to an appopriate method to handle it.
      */
     protected static class UnsupportedUrlError extends Error {
         /**
          * Constructor for UnsupportedUriError.
          * @param url              The incoming URL (the full one).
          * @param message          More details about why we couldn't find
          *                         a method to handle this URL, or null
          *                         if no additional information available.
          */
         public UnsupportedUrlError(String url, String message) {
             super("unsupported URL \"" + url
                     + ((message != null) ? "\": " + message : "\""));
         }
     }
 
     // There exists one object of the following type for each method we
     // have discovered that can service an incoming request.
 
     protected static class UrlMethod {
         public Method method;      // Java reflection info about the method;
                                    // used to invoke it.
         public Interactor interactor;
                                    // The Interactor object to which "method"
                                    // belongs.  We invoke methods on this
                                    // object to service incoming requests.
                                    // Null means this URL is serviced by a
                                    // DirectAjax class, which means it invokes
                                    // a static method so no class instance
                                    // is needed.
         public Timer timer;        // Records processing time for URLs that
                                    // mapped to this method.
 
         public UrlMethod(Method method, Interactor interactor) {
            this.method = method;
            this.interactor = interactor;
        }
     }
 
     // The following table keeps track of all the Interactor classes to which
     // we have attempted to dispatch a URL.  Keys in the table are class names
     // and each value is an object of the Key's class, which we use to invoke
     // methods on the class.
     //
     // Note: this table may be accessed and updated concurrently by
     // requests running in parallel.
 
     protected HashMap<String,Interactor> interactorMap
             = new HashMap<String,Interactor>();
 
     // The following table maps from strings of the form "class/method"
     // to an InteractorMethod object that allows us to invoke the method.
     //
     // Note: this table may be accessed and updated concurrently by
     // requests running in parallel.
     protected HashMap<String, UrlMethod> methodMap
             = new HashMap<String, UrlMethod>();
 
     // The following variable is used for log4j-based logging.
     protected Logger logger = Logger.getLogger("org.fiz.Dispatcher");
 
     // The following variables hold information from the last uncaught
     // exception handled by the "service" method.  This information is
     // intended primarily for use during testing.
 
     protected String basicMessage;    // getMessage() value from uncaught
                                       // exception.
     protected String fullMessage;     // The complete error message logged
                                       // by "service", including stack trace.
 
     // If the following variable is true, it means we are running tests
     // and should configure slightly differently.
     protected static boolean testMode = false;
 
     // The following object is used to gather performance statistics
     // for requests that cannot be mapped to an Interactor method
     // (i.e., all of the requests that don't get logged in methodMap).
     protected UrlMethod unsupportedURL = new UrlMethod(null, null);
 
     // The following timer records performance statistics for requests
     // that cannot be mapped to an Interactor method.
     protected Timer unknownURLTimer = Timer.getNamedTimer("unknown URL");
 
     // Overhead time (in dispatcher, before calling interactor):
     protected Timer dispatcherTimer = Timer.getNamedTimer("dispatcher");
 
     // Time spent after interactor returns (cr.finish, etc.):
     protected Timer finishTimer = Timer.getNamedTimer("finish");
 
     // If the following variable is true, we flush all of our internal
     // caches on every request.  This variable mirrors the "clearCaches"
     // entry in the main configuration dataset.  If the dataset value
     // changes to false then we stop flushing caches (and will no longer
     // see any changes to that dataset; to reenable cache flushing you
     // will have to change the dataset and also restart the application).
     protected boolean clearCaches = true;
 
     /**
      * This method is invoked by the servlet container when the servlet
      * is first loaded; we use it to perform our own initialization.
      */
     @Override
     public void init(ServletConfig config) throws ServletException {
         Timer.measureNoopTime();
         super.init(config);
         if (testMode) {
             // Reduce the level of logging while running tests.
             logger.setLevel(Level.FATAL);
         }
         String contextRoot = config.getServletContext().getRealPath("");
         logger.info("Fiz initializing with context root " + contextRoot);
         logger.info("Fiz version: " + Version.version);
 
         // Log all of the initialization parameters, if any.
         StringBuilder message = new StringBuilder();
         for (Enumeration paramNames = config.getInitParameterNames();
                paramNames.hasMoreElements() ;) {
             String name = (String) paramNames.nextElement();
             message.append(String.format("\n    %s: %s", name,
                     config.getInitParameter(name)));
         }
         if (message.length() != 0) {
             logger.info("initialization parameters:" + message);
         }
 
         // Determine all locations to search for configuration and css files.
         // We need to include /WEB-INF/ext/*/config and /WEB-INF/ext/*/css,
         // so we must enumerate all those directories and include them
         // explicitly.
         File[] extFolders = new File(contextRoot + "/WEB-INF/ext/").listFiles();
 
         ArrayList<String> configFolders = new ArrayList<String>();
         configFolders.add(contextRoot + "/WEB-INF/app/config");
         configFolders.add(contextRoot + "/WEB-INF/app/ext");
         configFolders.add(contextRoot + "/WEB-INF/fiz/config");
         // Add the config folders for all extensions.
         if (extFolders != null) {
             for (File extFolder : extFolders) {
                 if (extFolder.isDirectory() && !extFolder.isHidden()) {
                     configFolders.add(extFolder.getPath() + "/config");
                 }
             }
         }
         Config.init(configFolders.toArray(new String[0]));
 
         initMainConfigDataset(contextRoot);
         String debug = Config.getDataset("main").check("debug");
         clearCaches = (debug != null) && (debug.equals("1"));
 
         logger.info("main configuration dataset:\n    " +
                 Config.getDataset("main").toString().trim().replace(
                 "\n", "\n    "));
 
         ArrayList<String> cssFolders = new ArrayList<String>();
         cssFolders.add(contextRoot + "/WEB-INF/app/css");
         cssFolders.add(contextRoot + "/WEB-INF/app/ext");
         cssFolders.add(contextRoot + "/WEB-INF/fiz/css");
         // Add the css folders for all extensions.
         if (extFolders != null) {
             for (File extFolder : extFolders) {
                 if (extFolder.isDirectory() && !extFolder.isHidden()) {
                     cssFolders.add(extFolder.getPath() + "/css");
                 }
             }
         }
         Css.init(cssFolders.toArray(new String[0]));
 
         // If you want more detailed logging, such as request URLs, uncomment
         // the following line.
         // logger.setLevel(Level.WARN);
     }
 
     /**
      * Clear all of the caches maintained by Fiz, so that it will be
      * reloaded from disk the next time is needed.  This is typically done
      * when debugging, so that file changes will be reflected immediately,
      * without restarting the application.
      */
     public static void clearCaches() {
         Config.clearCache();
         Css.clearCache();
         Html.clearJsDependencyCache();
         TabSection.clearCache();
         Util.clearCache();
     }
 
     /**
      * This method is invoked by the servlet container when the servlet
      * is about to be unloaded.  Provides us with an opportunity to do
      * internal cleanup.
      */
     @Override
     public void destroy() {
         logger.info("destroying dispatcher");
 
         // Invoke destroy methods in all of the Interactors that have been
         // loaded, so that they can clean themselves up.
         for (Interactor interactor : interactorMap.values()) {
             logger.info("destroying Interactor "
                     + interactor.getClass().getName());
             interactor.destroy();
         }
         interactorMap.clear();
 
         // Clean up all of the other Fiz modules.
     }
 
     /**
      * This method is invoked for each incoming HTTP request whose URL
      * matches this application.  PathInfo (the portion of the URL
      * "owned" by this servlet) must have the form {@code /class/method/...},
      * which means it should be handled by method "method" in an Interactor
      * object of class "class".  The Interactor class is loaded and
      * instantiated if necessary, then the Interactor method is invoked.
      * @param request              Information about the HTTP request.
      * @param response             Used to generate the response.
      */
     @Override
     public void service(HttpServletRequest request,
         HttpServletResponse response) {
         long startTime = System.nanoTime();
         UrlMethod method = null;
         String methodName = null;
         ClientRequest cr = null;
         ClientRequest.Type requestType = ClientRequest.Type.NORMAL;
         dispatchUrl: try {
             if (logger.isTraceEnabled()) {
                 logger.trace("incoming " + request.getMethod() +
                         ": " + Util.getUrlWithQuery(request));
             }
 
             // See if we are in a debugging mode where we should flush caches
             // before every request.
             if (clearCaches) {
                 clearCaches();
                 initMainConfigDataset(getServletContext().getRealPath(""));
             }
 
             // Use UTF-8 as the default encoding for all responses.
             response.setCharacterEncoding("UTF-8");
 
             // The "pathInfo" portion of the URL (the part that belongs to
             // us) must have the form /class/method/... Peel off the
             // "class/method" part and lookup the method.
 
             String pathInfo = request.getPathInfo();
             if (pathInfo == null) {
                 pathInfo = "";
             }
             int endOfMethod = -1;
             int endOfClass = pathInfo.indexOf('/', 1);
             if (endOfClass >= 1) {
                 endOfMethod = pathInfo.indexOf('/', endOfClass+1);
                 if (endOfMethod == -1) {
                     endOfMethod = pathInfo.length();
                 }
             }
             if ((endOfClass < 2) || (endOfMethod < (endOfClass+2))) {
                if (pathInfo.length() == 0) {
                    // This is the application's home URL; redirect to an
                    // interactor if one has been specified.
                     String homeUrl = Config.getDataset("main").check(
                             "homeRedirectUrl");
                     if (homeUrl != null) {
                         cr = new ClientRequest(this, request, response);
                         cr.redirect(homeUrl);
                         cr.finish();
                         break dispatchUrl;
                     }
                 }
                 throw new UnsupportedUrlError(request.getRequestURI(),
                         "URL doesn't contain class name and/or method "
                         + "name");
             }
             if (pathInfo.startsWith("ajax", endOfClass+1)) {
                 requestType = ClientRequest.Type.AJAX;
             } else if (pathInfo.startsWith("post", endOfClass+1)) {
                 requestType = ClientRequest.Type.POST;
             }
             methodName = pathInfo.substring(1, endOfMethod);
             method = findMethod(methodName, endOfClass-1, request);
 
             // At this point we have located the method to service this
             // request.  Package up relevant information into a ClientRequest
             // object and invoke the method.
             if (method.interactor != null) {
                 cr = method.interactor.getRequest(this, request, response);
             } else {
                 cr = new ClientRequest(this, request, response);
             }
             cr.setClientRequestType(requestType);
             dispatcherTimer.start(startTime);
             dispatcherTimer.stop();
             method.method.invoke(method.interactor, cr);
             finishTimer.start();
 
             // The service method has completed successfully.  Output the
             // response that was generated.
             cr.finish();
         }
         catch (Throwable e) {
             finishTimer.start();
 
             // If the exception happened in the Interactor method, the
             // real information we want is encapsulated inside e.
             Throwable cause =  e.getCause();
             if (cause == null) {
                 cause = e;
             }
 
             // If the error occurred before we created a ClientRequest,
             // create one here so we can use it for reporting the error.
             if (cr == null) {
                 cr = new ClientRequest(this, request, response);
                 cr.setClientRequestType(requestType);
             }
             if (cause instanceof HandledError) {
                 // There was an error, but it was already handled.  The
                 // error was thrown simply to terminate the processing of
                 // the request.
                 cr.finish();
                 return;
             }
 
             // Print details about the error to the log (unless this error
             // was caused by incorrect user behavior).
             StringWriter sWriter= new StringWriter();
             basicMessage = cause.getMessage();
             if (!(cause instanceof UserError)) {
                 cause.printStackTrace(new PrintWriter(sWriter));
                 fullMessage = "unhandled exception for URL \""
                         + Util.getUrlWithQuery(request) + "\":\n"
                         + sWriter.toString();
                 logger.error(fullMessage);
             }
 
             // If this is an AJAX or post request then return the error
             // message in a protocol-specific fashion.
             if (requestType != ClientRequest.Type.NORMAL) {
                 String style;
                 if (cause instanceof UserError) {
                     style = "bulletin.userError";
                 } else if (requestType == ClientRequest.Type.AJAX) {
                     style = "bulletin.uncaughtAjax";
                 } else {
                     style = "bulletin.uncaughtPost";
                 }
                 cr.addMessageToBulletin(Config.getPath("styles", style),
                         new Dataset("message", basicMessage));
                 cr.finish();
                 return;
             }
 
             // This is a normal HTML request; use a template from the
             // "styles" configuration dataset to generate HTML describing
             // the error.
             Html html = cr.getHtml();
             try {
                 if (Config.getPath("styles", "uncaught.clearHtml").equals(
                         "true")) {
                     html.clear();
                 }
                 Template.appendHtml(html.getBody(), Config.getPath("styles", "uncaught.html"),
                         new Dataset("message", basicMessage));
             }
             catch (Throwable t) {
                 // Things are really messed up: an exception happened while
                 // trying to report an exception.  Generate an HTML report.
                 sWriter.getBuffer().setLength(0);
                 t.printStackTrace(new PrintWriter(sWriter));
                 logger.error("unhandled exception while reporting " +
                         "an unhandled exception in Dispatcher:\n" +
                         sWriter.toString());
                 html.clear();
                 html.getBody().append("<div class=\"uncaughtException\">" +
                         "Multiple internal errors in the server!  Details " +
                         "are in the server's log</div>.\n");
             }
             cr.finish();
         }
         // Record how long this URL took to process.
         Timer timer;
         if (method != null) {
             timer = method.timer;
             if (timer == null) {
                 timer = Timer.getNamedTimer(methodName);
                 method.timer = timer;
             }
         } else {
             timer = unknownURLTimer;
         }
         long endTime = System.nanoTime();
         timer.start(startTime);
         timer.stop(endTime);
         finishTimer.stop(endTime);
     }
 
     /**
      * This utility method looks up a class and generates an appropriate
      * Error if the class can't be found.
      * @param className            Name of the desired class
      * @param request              Information about the HTTP request (used
      *                             for generating error messages).
      * @return                     The Class object corresponding to
      *                             className.
      */
     protected Class<?> findClass(String className, HttpServletRequest request) {
         try {
             return Class.forName(className);
         }
         catch (ClassNotFoundException e) {
             throw new UnsupportedUrlError(request.getRequestURI(),
                     "can't find class \"" + className + "\"");
         }
     }
 
     /**
      * Find the method that will handle a request.  If needed, information
      * is added to our cache of known methods.
      * @param classPlusMethod      Portion of the URL that identifies the
      *                             method to handle the request. Has the form
      *                             {@code class/method}.
      * @param slashIndex           Index within {@code classPlusMethod} of
      *                             the letter slash that separates the class
      *                             and method.
      * @param request              Information about the HTTP request (used
      *                             for generating error messages).
      * @return                     An UrlMethod object describing the method
      *                             corresponding to {@code classPlusMethod}.
      */
     protected synchronized UrlMethod findMethod(String classPlusMethod,
             int slashIndex, HttpServletRequest request) {
         UrlMethod method = methodMap.get(classPlusMethod);
         if (method != null) {
             return method;
         }
 
         // We don't currently have any information about this method.
         // If we haven't already done so, scan the class specified
         // in the URL and update our tables with information about it.
         // First, see if we already know about this class.  The class
         // name can have one of two forms:
         // * If it starts with a lower-case letter, e.g. "foo", it refers
         //   to an Interactor, whose name is computed by upper-casing the
         //   first character of the class in the URL and appending
         //   "Interactor", e.g. "FooInteractor".
         // * If the class in the URL starts with an upper-case letter, e.g.
         //   "TreeSection" then call findDirectMethod to handle it.
         if (Character.isUpperCase(classPlusMethod.charAt(0))) {
             return findDirectMethod(classPlusMethod, slashIndex, request);
         }
         String className = StringUtil.ucFirst(classPlusMethod.substring(0,
                 slashIndex)) + "Interactor";
         String methodName = classPlusMethod.substring(slashIndex+1);
         Interactor interactor = interactorMap.get(className);
         if (interactor == null) {
             // The class name is not already known; load the class
             // and create an instance of it.
             interactor = (Interactor) Util.newInstance(className,
                     "org.fiz.Interactor");
             interactorMap.put(className, interactor);
             interactor.init();
             logger.info("loaded Interactor " +
                     interactor.getClass().getName());
 
             // Scan the methods for the class and remember each method
             // that is public and takes a single argument that is a
             // subclass of ClientRequest.
             Class<?> requestClass = findClass("org.fiz.ClientRequest",
                     request);
             for (Method m : interactor.getClass().getMethods()) {
                 Class[] parameterTypes = m.getParameterTypes();
                 if ((parameterTypes.length != 1)
                         || !requestClass.isAssignableFrom(
                         parameterTypes[0])) {
                     continue;
                 }
                 String key = classPlusMethod.substring(0, slashIndex+1)
                         + m.getName();
                 methodMap.put(key, new UrlMethod(m, interactor));
             }
 
             // Try one more time to find the method we need.
             method = methodMap.get(classPlusMethod);
             if (method != null) {
                 return method;
             }
         }
 
         throw new UnsupportedUrlError(request.getRequestURI(),
                 "couldn't find method \"" + methodName +
                 "\" with proper signature in class " + className);
 
     }
 
     /**
      * Find a method that will handle a "direct" request (one that is
      * dispatched to a static method of a class implementing the DirectAjax
      * interface).  If the method is found its added to the cache of known
      * methods.
      * @param classPlusMethod      Portion of the URL that identifies the
      *                             method to handle the request. Has the form
      *                             {@code class/method}.
      * @param slashIndex           Index within {@code classPlusMethod} of
      *                             the letter slash that separates the class
      *                             and method.
      * @param request              Information about the HTTP request (used
      *                             for generating error messages).
      * @return                     An UrlMethod object describing the method
      *                             corresponding to {@code classPlusMethod}.
      */
     protected synchronized UrlMethod findDirectMethod(String classPlusMethod,
             int slashIndex, HttpServletRequest request) {
         String className = classPlusMethod.substring(0, slashIndex);
         String methodName = classPlusMethod.substring(slashIndex+1);
 
         // Make sure the method name starts with "ajax".
         if (!methodName.startsWith("ajax")) {
             throw new UnsupportedUrlError(request.getRequestURI(),
                     "method name must start with \"ajax\"");
         }
 
         // Lookup the method and verify the following:
         // * The class must exist and must implement the DirectAjax
         //   interface.
         // * The method must exist, must be static, and must take a single
         //   argument of type ClientRequest.
         Class<?> handlerClass = Util.findClass(className);
         if (handlerClass == null) {
             throw new UnsupportedUrlError(request.getRequestURI(),
                     "can't find class \"" + className + "\"");
         }
         Class<?> directAjaxInterface = findClass("org.fiz.DirectAjax",
                 request);
         checkInterface: {
             for (Class<?> iface : handlerClass.getInterfaces()) {
                 if (iface == directAjaxInterface) {
                     break checkInterface;
                 }
             }
             throw new UnsupportedUrlError(request.getRequestURI(),
                     "class " + className + " doesn't implement DirectAjax " +
                     "interface");
         }
         Method method;
         Class<?> clientRequestClass = findClass("org.fiz.ClientRequest",
                 request);
         try {
             method = handlerClass.getMethod(methodName,
                     clientRequestClass);
         }
         catch (NoSuchMethodException e) {
             throw new UnsupportedUrlError(request.getRequestURI(),
                     "couldn't find method \"" + methodName +
                     "\" with proper signature in class " + className);
         }
         if (!Modifier.isStatic(method.getModifiers())) {
             throw new UnsupportedUrlError(request.getRequestURI(),
                     "method \"" + methodName + "\" in class " + className +
                     " isn't static");
         }
         UrlMethod urlMethod = new UrlMethod(method, null);
         methodMap.put(classPlusMethod, urlMethod);
         return urlMethod;
     }
 
     /**
      * Load the main configuration data set and add a "home" entry to
      * it that refers to our context root (the directory that contains
      * the WEB-INF directory).  This method also sets a "debug" entry
      * if the "FIZ_DEBUG" environment variable is set.
      * @param home                 Root directory for this application.
      */
     protected static void initMainConfigDataset(String home) {
         Dataset main = Config.getDataset("main");
         if (main instanceof CompoundDataset) {
             main = ((CompoundDataset) main).getComponents()[0];
         }
         main.set("home", home);
         String debug = System.getenv("FIZ_DEBUG");
         if (debug != null) {
             main.set("debug", debug);
         }
     }
 }
