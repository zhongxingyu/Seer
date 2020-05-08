 /*
  * BeanShell Servlet
  * Copyright (C) 2011 Stefano Fornari
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
  * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  */
 
 package ste.web.beanshell;
 
 import java.io.*;
 import java.util.logging.Logger;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import bsh.*;
 import java.util.Enumeration;
 import java.util.logging.Level;
 
 /**
  * Executes the bsh script specified by the URL. The script is the controller
  * and shall set the view that should be rendered after the execution of the 
  * script in the bsh variable <i>view</i>.
  * 
  * The lookup of the scripts and views are controlled by the following pattern:
  * <pre>
  *   {context}{controllers-prefix}/{script-pathname}
  *   {context}{vies-prefix}/{jsp-pathname}
  * </pre>
  * For example, if context=/myapp, controllers-prefix=c and views-prefix=v,
  * the URL http://myserver:8080/myapp/mycontroller.bsh will read the script
  * {CONTAINER_HOME}/myapp/c/mycontroller.bsh. If the controller sets <i>view</i>
  * to myview.jsp, the request is forwarded to http://myserver:8080/myapp/v/myview.jsp
  * 
  * <i>controllers-prefix</i> and <i>views-prefix</i> can be set as context 
  * parameters in web.xml:
  * 
  * &lt;context-param&gt;
  *   &lt;param-name&gt;<b>controllers-prefix</b>&lt;/param-name&gt;
  *   &lt;param-value&gt;<b>controllers</b>&lt;/param-value&gt;
  * &lt;/context-param&gt;
  * &lt;context-param&gt;
  *   &lt;param-name&gt;<b>views-prefix</b>&lt;/param-name&gt;
  *   &lt;param-value&gt;<b>views</b>&lt;/param-value&gt;
  * &lt;/context-param&gt;
  * 
  * <i>controllers-prefix</i> and <i>views-prefix</i> defauult to "".
  * 
  * @author ste
  */
 public class BeanShellServlet
 extends HttpServlet {
     // --------------------------------------------------------------- Constants
 
     public static final String LOG_NAME = "ste.web";
     
     public static final String PARAM_CONTROLLERS          = "controllers-prefix";
     public static final String PARAM_VIEWS                = "views-prefix"      ;
     public static final String DEFAULT_CONTROLLERS_PREFIX = "/"                 ;
     public static final String DEFAULT_VIEWS_PREFIX       = "/"                 ;
 
     // ------------------------------------------------------------ Private data
 
     private static final Logger log = Logger.getLogger(LOG_NAME);
     
     private static String controllersPrefix = "controllers";
     private static String viewsPrefix       = "views"      ;
 
     // ---------------------------------------------------------- Public methods
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
 
         ServletContext context = getServletContext();
         
         controllersPrefix = context.getInitParameter(PARAM_CONTROLLERS);
         if (controllersPrefix == null) {
             controllersPrefix = context.getInitParameter(DEFAULT_CONTROLLERS_PREFIX);
         } else {
             //
             // let's fix a common mistake :)
             //
             if (!controllersPrefix.startsWith("/")) {
                 controllersPrefix = '/' + controllersPrefix;
             }
         }
         
         viewsPrefix = context.getInitParameter(PARAM_VIEWS);
         if (viewsPrefix == null) {
             context.getInitParameter(DEFAULT_VIEWS_PREFIX);
         }  else {
             //
             // let's fix a common mistake :)
             //
             if (!viewsPrefix.endsWith("/")) {
                 viewsPrefix = viewsPrefix + '/';
             }
         }
         
         if (log.isLoggable(Level.FINE)) {
             log.log(Level.FINE, "controllers-prefix: {0}", controllersPrefix);
             log.log(Level.FINE, "views-prefix: {0}", viewsPrefix);
         }
     }
 
     @Override
     public void doGet(final HttpServletRequest  request ,
                       final HttpServletResponse response)
     throws ServletException, IOException {
         doWork(request, response);
     }
 
     @Override
     public void doPost(final HttpServletRequest  request ,
                       final HttpServletResponse response)
     throws ServletException, IOException {
         doWork(request, response);
     }
 
     // --------------------------------------------------------- Private methods
 
     /**
      * This methods does the real work regardless the HTTP protocol used.
      * It grabs the pathname of the script to be executed by the URI used to 
      * invoke. 
      * Note that if the request comes from an internal redirect, getRequestURI 
      * returns the external URI (i.e. the one typed in the browser); in this 
      * case the servlet engine sets the redirected URI properties in the 
      * following attributes:
      * <pre>
      *   javax.servlet.forward.request_uri
      *   javax.servlet.forward.context_path
      *   javax.servlet.forward.servlet_path
      *   javax.servlet.forward.path_info
      *   javax.servlet.forward.query_string
      * </pre>
      * 
      * Therefore we first check if the attribute <code>javax.servlet.forward.request_uri</code>
      * is set; if set we directly use it, if not, we get the servlet URI calling
      * <code>request.getRequestURI()</code>
      * 
      * @param request the request
      * @param response the response
      * @throws ServletException in case of engine exceptions
      * @throws IOException in case of IO errors 
      */
     private void doWork(final HttpServletRequest  request ,
                         final HttpServletResponse response)
     throws ServletException, IOException {
         if (log.isLoggable(Level.FINE)) {
             Enumeration<String> names = request.getAttributeNames();
             while(names.hasMoreElements()) {
                 String name = (String)names.nextElement();
                 if (name.startsWith("javx.servlet"))  {
                     log.log(Level.FINE, ">> {0}: {1}", new Object[]{name, request.getAttribute(name)});
                 }
             }
             
         }
         
         String uri = (String)request.getAttribute("javax.servlet.include.request_uri");
         if (uri == null) {
            uri = request.getServletPath();
         } else {
             uri = uri.substring(((String)request.getAttribute("javax.servlet.include.context_path")).length());
         }
         if (log.isLoggable(Level.FINE)) {
             log.log(Level.FINE, "Serving {0}", uri);
         }
 
         try {
             Interpreter interpreter = createInterpreter(request, response);
 
             interpreter.eval(getScript(getServletContext().getRealPath(uri)));
             
             String nextView = (String)interpreter.get("view");
             if ((nextView != null) && (nextView instanceof String)) {
                 nextView = viewsPrefix + nextView;
                 
                 if (log.isLoggable(Level.FINE)) {
                     log.log(Level.FINE, "Forwarding to {0}", nextView);
                 }
                 
                 request.getRequestDispatcher(nextView).include(request, response);
             }
         } catch (Exception e) {
             handleError(request, response, e);
         }
 
     }
 
     private Interpreter createInterpreter(final HttpServletRequest  request ,
                                           final HttpServletResponse response)
     throws EvalError, IOException {
         Interpreter interpreter = new Interpreter();
 
         //
         // Set request parameters as script variables
         //
         String key;
         for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
             key = (String)e.nextElement();
             interpreter.set(key, request.getParameter(key));
         }
 
         interpreter.set("request" , request                   );
         interpreter.set("response", response                  );
         interpreter.set("session" , request.getSession(false) );
         interpreter.set("out"     , response.getWriter()      );
         interpreter.set("log"     , log                       );
 
         //
         // Import common commands... I am commenting it out for now; I will 
         // reintroduce it if and when I will add the helpers
         //
         //interpreter.eval("importCommands(\"commands\");");
 
         return interpreter;
     }
 
     /**
      * Returns the script to be invoked. @see doWork() for more information on
      * how local redirects are handled.
      * 
      * @param request
      * 
      * @return the beanshell script to be invoked
      * 
      * @throws IOException if there are issues reading the script
      */
     private String getScript(final String script)
     throws IOException {
         File   scriptFile     = new File(script);
         String controllerPath = scriptFile.getParent() + controllersPrefix;
         File   controllerFile = new File(controllerPath, scriptFile.getName());
         
         if (log.isLoggable(Level.FINE)) {
             log.log(Level.FINE, "script: {0}", script);
             log.log(Level.FINE, "controllerFile: {0}", controllerFile);
         }
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         FileInputStream       is   = null;
 
         try {
             is = new FileInputStream(controllerFile);
 
             if (is == null) {
                 throw new FileNotFoundException(script);
             }
 
             byte[] buf = new byte[1024];
             int n = 0;
             while ((n = is.read(buf))>=0) {
                 baos.write(buf, 0, n);
             }
 
             return "try { " + baos.toString() + "} catch (Throwable t) { t.printStackTrace(); throw t; }";
         } finally {
             if (is != null) {
                 is.close();
             }
             if (baos != null) {
                 baos.close();
             }
         }
     }
 
     /**
      * Handles errors conditions returning an appropriate content to the client.
      *
      * @param request the request object
      * @param response the response object
      * @t     a throwable object
      *
      */
     private void handleError(final HttpServletRequest   request,
                              final HttpServletResponse response,
                              final Throwable                  t) {
 
         String msg = t.getMessage();
 
         if (log.isLoggable(Level.SEVERE)) {
             log.log(Level.SEVERE, "Error message: {0}", msg);
             log.throwing(getClass().getName(), "handleError", t);
         }
         
         try {
             if (t instanceof FileNotFoundException) {
                 response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
             } else {
                 response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
             }
         } catch (IOException e) {
             if (log.isLoggable(Level.SEVERE)) {
                 log.severe(e.getMessage());
                 log.throwing(getClass().getName(), "handleError", e);
             }
         }
     }
 }
