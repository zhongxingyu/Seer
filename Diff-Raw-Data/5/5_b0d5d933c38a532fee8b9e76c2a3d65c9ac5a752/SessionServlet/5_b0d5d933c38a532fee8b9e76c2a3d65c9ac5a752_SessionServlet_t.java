 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.session;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.*;
 import org.wings.event.ExitVetoException;
 import org.wings.event.SRequestEvent;
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.externalizer.ExternalizedResource;
 import org.wings.io.Device;
 import org.wings.io.DeviceFactory;
 import org.wings.io.ServletDevice;
 import org.wings.resource.DynamicCodeResource;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.*;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Enumeration;
 import java.util.Locale;
 import java.util.Arrays;
 
 /**
  * The servlet engine creates for each user a new HttpSession. This
  * HttpSession can be accessed by all Serlvets running in the engine. A
  * WingServlet creates one wings SessionServlet per HTTPSession and stores
  * it in its context.
  * <p>As the SessionServlets acts as Wrapper for the WingsServlet, you can
  * access from there as used the  ServletContext and the HttpSession.
  * Additionally the SessionServlet containts also the wingS-Session with
  * all important services and the superordinated SFrame. To this SFrame all
  * wings-Components and hence the complete application state is attached.
  * The developer can access from any place via the SessionManager a
  * reference to the wingS-Session. Additionally the SessionServlet
  * provides access to the all containing HttpSession.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 final class SessionServlet
         extends HttpServlet
         implements HttpSessionBindingListener {
     private final transient static Log log = LogFactory.getLog(SessionServlet.class);
 
     /**
      * The parent {@link WingServlet}
      */
     protected transient HttpServlet parent = this;
 
     /**
      * This should be a resource ..
      */
     protected String errorTemplateFile;
 
     /**
      * The session.
      */
     private Session session;
 
     private boolean firstRequest = true;
 
     /**
      * Default constructor.
      */
     protected SessionServlet() {
     }
 
     /**
      * Sets the parent servlet contianint this wings session
      * servlet (WingsServlet, delegating its requests to the SessionServlet).
      */
     protected final void setParent(HttpServlet p) {
         if (p != null) parent = p;
     }
 
     public final Session getSession() {
         return session;
     }
 
     /**
      * Overrides the session set for setLocaleFromHeader by a request parameter.
      * Hence you can force the wings session to adopt the clients Locale.
      */
     public final void setLocaleFromHeader(String[] args) {
         if (args == null)
             return;
 
         for (int i = 0; i < args.length; i++) {
             try {
                 getSession().setLocaleFromHeader(Boolean.valueOf(args[i]).booleanValue());
             } catch (Exception e) {
                 log.error("setLocaleFromHeader", e);
             }
         }
     }
 
     /**
      * The Locale of the current wings session servlet is determined by
      * the locale transmitted by the browser. The request parameter
      * <PRE>LocaleFromHeader</PRE> can override the behaviour
      * of a wings session servlet to adopt the clients browsers Locale.
      *
      * @param req The request to determine the local from.
      */
     protected final void handleLocale(HttpServletRequest req) {
         setLocaleFromHeader(req.getParameterValues("LocaleFromHeader"));
 
         if (getSession().getLocaleFromHeader()) {
             for (Enumeration en = req.getLocales(); en.hasMoreElements();) {
                 Locale locale = (Locale) en.nextElement();
                 try {
                     getSession().setLocale(locale);
                     return;
                 } catch (Exception ex) {
                     log.warn("locale not supported " + locale);
                 } // end of try-catch
             } // end of for ()
         }
     }
 
     // jetzt kommen alle Servlet Methoden, die an den parent deligiert
     // werden
 
 
     public ServletContext getServletContext() {
         if (parent != this)
             return parent.getServletContext();
         else
             return super.getServletContext();
     }
 
 
     public String getInitParameter(String name) {
         if (parent != this)
             return parent.getInitParameter(name);
         else
             return super.getInitParameter(name);
     }
 
 
     public Enumeration getInitParameterNames() {
         if (parent != this)
             return parent.getInitParameterNames();
         else
             return super.getInitParameterNames();
     }
 
     /**
      * Delegates log messages to the according WingsServlet or alternativly
      * to the HttpServlet logger.
      *
      * @param msg The logmessage
      */
     public void log(String msg) {
         if (parent != this)
             parent.log(msg);
         else
             super.log(msg);
     }
 
 
     public String getServletInfo() {
         if (parent != this)
             return parent.getServletInfo();
         else
             return super.getServletInfo();
     }
 
 
     public ServletConfig getServletConfig() {
         if (parent != this)
             return parent.getServletConfig();
         else
             return super.getServletConfig();
     }
 
     // bis hierhin
 
     /**
      * The error template which should be presented on any uncaught Exceptions can be set
      * via a property <code>wings.error.template</code> in the web.xml file.
      */
     protected void initErrorTemplate(ServletConfig config) {
         if (errorTemplateFile == null) {
             errorTemplateFile = config.getInitParameter("wings.error.template");
         }
     }
 
     /**
      * init
      */
     public final void init(ServletConfig config,
                            HttpServletRequest request,
                            HttpServletResponse response) throws ServletException {
         try {
             initErrorTemplate(config);
 
             session = new Session();
             SessionManager.setSession(session);
 
             // set request.url in session, if used in constructor of wings main classs
             if (request.isRequestedSessionIdValid()) {
                 // this will fire an event, if the encoding has changed ..
                 ((PropertyService) session).setProperty("request.url",
                         new RequestURL("", getSessionEncoding(response)));
             }
 
             session.init(config, request);
 
 
             try {
                 String mainClassName = config.getInitParameter("wings.mainclass");
                 Class mainClass = null;
                 try {
                     mainClass = Class.forName(mainClassName, true,
                             Thread.currentThread()
                             .getContextClassLoader());
                 } catch (ClassNotFoundException e) {
                     // fallback, in case the servlet container fails to set the
                     // context class loader.
                     mainClass = Class.forName(mainClassName);
                 }
                 Object main = mainClass.newInstance();
             } catch (Exception ex) {
                 log.fatal("could not load wings.mainclass: " +
                         config.getInitParameter("wings.mainclass"), ex);
                 throw new ServletException(ex);
             }
         } finally {
             // The session was set by the constructor. After init we
             // expect that only doPost/doGet is called, which set the
             // session also. So remove it here.
             SessionManager.removeSession();
         }
     }
 
     /**
      * this method references to
      * {@link #doGet(HttpServletRequest, HttpServletResponse)}
      */
     public final void doPost(HttpServletRequest req, HttpServletResponse res)
             throws IOException {
         //value chosen to limit denial of service
         if (req.getContentLength() > getSession().getMaxContentLength() * 1024) {
             res.setContentType("text/html");
             ServletOutputStream out = res.getOutputStream();
             out.println("<html><head><title>Too big</title></head>");
             out.println("<body><h1>Error - content length &gt; " +
                     getSession().getMaxContentLength() + "k");
             out.println("</h1></body></html>");
         } else {
             doGet(req, res);
         }
         // sollte man den obigen Block nicht durch folgende Zeile ersetzen?
         //throw new RuntimeException("this method must never be called!");
         // bsc: Wieso?
     }
 
 
     /**
      * Verarbeitet Informationen vom Browser:
      * <UL>
      * <LI> setzt Locale
      * <LI> Dispatch Get Parameter
      * <LI> feuert Form Events
      * </UL>
      * Ist synchronized, damit nur ein Frame gleichzeitig bearbeitet
      * werden kann.
      */
     public final synchronized void doGet(HttpServletRequest req,
                                          HttpServletResponse response) {
         SessionManager.setSession(session);
         session.setServletRequest(req);
         session.setServletResponse(response);
 
         session.fireRequestEvent(SRequestEvent.REQUEST_START);
 
         // in case, the previous thread did not clean up.
         SForm.clearArmedComponents();
 
         Device outputDevice = null;
 
         ReloadManager reloadManager = session.getReloadManager();
         
         boolean isErrorHandling = false;
         
         try {
             /*
              * The tomcat 3.x has a bug, in that it does not encode the URL
              * sometimes. It does so, when there is a cookie, containing some
              * tomcat sessionid but that is invalid (because, for instance,
              * we restarted the tomcat in-between). 
              * [I can't think of this being the correct behaviour, so I assume
              *  it is a bug. ]
              *
              * So we have to workaround this here: if we actually got the
              * session id from a cookie, but it is not valid, we don't do
              * the encodeURL() here: we just leave the requestURL as it is
              * in the properties .. and this is url-encoded, since
              * we had set it up in the very beginning of this session 
              * with URL-encoding on  (see WingServlet::newSession()).
              *
              * Vice versa: if the requestedSessionId is valid, then we can
              * do the encoding (which then does URL-encoding or not, depending
              * whether the servlet engine detected a cookie).
              * (hen)
              */
             RequestURL requestURL = null;
             if (req.isRequestedSessionIdValid()) {
                 requestURL = new RequestURL("", getSessionEncoding(response));
                 // this will fire an event, if the encoding has changed ..
                 ((PropertyService) session).setProperty("request.url",
                         requestURL);
             }
 
             if (log.isDebugEnabled()) {
                 log.debug("request URL: " + requestURL);
                 log.debug("HTTP header:");
                 for (Enumeration en = req.getHeaderNames(); en.hasMoreElements();) {
                     String header = (String) en.nextElement();
                     log.debug("   " + header + ": " + req.getHeader(header));
                 }
             }
             handleLocale(req);
 
             Enumeration en = req.getParameterNames();
             Cookie[] cookies = req.getCookies();
 
             // are there parameters/low level events to dispatch
             if (en.hasMoreElements()) {
                 // only fire DISPATCH_START if we have parameters to dispatch
                 session.fireRequestEvent(SRequestEvent.DISPATCH_START);
 
                 if (cookies != null) {
                     //dispatch cookies
                     for (int i = 0; i < cookies.length; i++) {
                         Cookie cookie = cookies[i];
                         String paramName = (String) cookie.getName();
                         String value = cookie.getValue();
     
                         if (log.isDebugEnabled())
                             log.debug("dispatching cookie " + paramName + " = " + value);
     
                         session.getDispatcher().dispatch(paramName, new String[] { value });
                     }
                 }
                 while (en.hasMoreElements()) {
                     String paramName = (String) en.nextElement();
                     String[] value = req.getParameterValues(paramName);
 
                     if (log.isDebugEnabled())
                         log.debug("dispatching " + paramName + " = " + Arrays.asList(value));
 
                     session.getDispatcher().dispatch(paramName, value);
                 }
 
                 SForm.fireEvents();
             
                 // only fire DISPATCH DONE if we have parameters to dispatch
                 session.fireRequestEvent(SRequestEvent.DISPATCH_DONE);
             }
 
             session.fireRequestEvent(SRequestEvent.PROCESS_REQUEST);
             
             
             // if the user chose to exit the session as a reaction on an
             // event, we got an URL to redirect after the session.
             /*
              * where is the right place?
              * The right place is 
              *    - _after_ we processed the events 
              *        (e.g. the 'Pressed Exit-Button'-event or gave
              *         the user the chance to exit this session in the custom
              *         processRequest())
              *    - but _before_ the rendering of the page,
              *      because otherwise an redirect won't work, since we must
              *      not have sent anything to the output stream).
              */
             if (session.getExitAddress() != null) {
 
                 try {
                     session.firePrepareExit();
 
                     String redirectAddress;
                     if (session.getExitAddress().length() > 0) {
                         // redirect to user requested URL.
                         redirectAddress = session.getExitAddress();
                     } else {
                         // redirect to a fresh session.
                         redirectAddress = req.getRequestURL().toString();
                     }
                     req.getSession().invalidate(); // calls destroy implicitly
                     response.sendRedirect(redirectAddress);
 
                     return;
                 } catch (ExitVetoException ex) {
                     session.exit(null);
                 } // end of try-catch
             }
 
             if (session.getRedirectAddress() != null) {
                 // redirect to user requested URL.
                 response.sendRedirect(session.getRedirectAddress());
                 session.setRedirectAddress(null);
                 return;
             }
             
             // invalidate frames and resources
             reloadManager.invalidateResources();
             reloadManager.notifyCGs();
 
             // deliver resource. The
             // externalizer is able to handle static and dynamic resources
             ExternalizeManager extManager = getSession().getExternalizeManager();
             String pathInfo = req.getPathInfo().substring(1);
             log.debug("pathInfo: " + pathInfo);
 
             /*
              * if we have no path info, or the special '_' path info
              * (that should be explained somewhere, Holger), then we
              * deliver the toplevel Frame of this application.
              */
             String externalizeIdentifier = null;
             if (pathInfo == null
                     || pathInfo.length() == 0
                     || "_".equals(pathInfo)
                     || firstRequest) {
                 log.debug("delivering default frame");
 
                 if (session.getFrames().size() == 0)
                     throw new ServletException("no frame visible");
                 
                 // get the first frame from the set and walk up the hierarchy.
                 // this should work in most cases. if there are more than one
                 // toplevel frames, the developer has to care about the resource
                 // ids anyway ..
                 SFrame defaultFrame = (SFrame) session.getFrames().iterator().next();
                 while (defaultFrame.getParent() != null)
                     defaultFrame = (SFrame) defaultFrame.getParent();
  
                 
                 Resource resource = defaultFrame.getDynamicResource(DynamicCodeResource.class);
                 externalizeIdentifier = resource.getId();
 
                 firstRequest = false;
             } else {
                 externalizeIdentifier = pathInfo;
             }
             
             // externalized this resource.
             ExternalizedResource extInfo = extManager
                     .getExternalizedResource(externalizeIdentifier);
             if (extInfo != null) {
                 outputDevice = DeviceFactory.createDevice(extInfo);
                 //outputDevice = createOutputDevice(req, response, extInfo);
 
                 session.fireRequestEvent(SRequestEvent.DELIVER_START, extInfo);
 
                 extManager.deliver(extInfo, response, outputDevice);
 
                 session.fireRequestEvent(SRequestEvent.DELIVER_DONE, extInfo);
             } else {
                 handleUnknownResourceRequested(req, response);
             }
 
         } catch (Throwable e) {
             /*
              * error handling...implement it in SFrame
              */
             SFrame defaultFrame = (SFrame) session.getFrames().iterator().next();
             while (defaultFrame.getParent() != null)
                 defaultFrame = (SFrame) defaultFrame.getParent();
             if (defaultFrame != null && defaultFrame.handleError(e)) {
                 doGet(req, response);
                 isErrorHandling = true;
                 return;
             } else {
                 log.fatal("exception: ", e);
                 handleException(response, e);
             }
             
         } finally {
            if (session != null && !isErrorHandling) {
                 session.fireRequestEvent(SRequestEvent.REQUEST_END);
             }
 
             if (outputDevice != null) {
                 try {
                     outputDevice.close();
                 } catch (Exception e) {
                 }
             }
 
             /*
              * the session might be null due to destroy().
              */
            if (session != null && !isErrorHandling) {
                 reloadManager.clear();
                 session.setServletRequest(null);
                 session.setServletResponse(null);
             }
 
 
             // make sure that the session association to the thread is removed
             // from the SessionManager
             SessionManager.removeSession();
             SForm.clearArmedComponents();
         }
     }
 
     /**
      * create a Device that is used to deliver the content, that is
      * session specific.
      * The default
      * implementation just creates a ServletDevice. You can override this
      * method to decide yourself what happens to the output. You might, for
      * instance, write some device, that logs the output for debugging
      * purposes, or one that creates a gziped output stream to transfer
      * data more efficiently. You get the request and response as well as
      * the ExternalizedResource to decide, what kind of device you want to create.
      * You can rely on the fact, that extInfo is not null.
      * Further, you can rely on the fact, that noting has been written yet
      * to the output, so that you can set you own set of Headers.
      *
      * @param request  the HttpServletRequest that is answered
      * @param response the HttpServletResponse.
      * @param extInfo  the externalized info of the resource about to be
      *                 delivered.
      */
     protected Device createOutputDevice(HttpServletRequest request,
                                         HttpServletResponse response,
                                         ExternalizedResource extInfo)
             throws IOException {
         return new ServletDevice(response.getOutputStream());
     }
 
 
 
     // Exception Handling
     private SFrame errorFrame;
     private SLabel errorStackTraceLabel;
     private SLabel errorMessageLabel;
     private SLabel versionLabel;
 
     /** 
      * In case of an error, display an error page to the user. This is only
      * done when there is a property <code>wings.error.template</code> present
      * in the web.xml file. This property must contain a path relative to the
      * webapp which leads to a wingS template. In this template, placeholders
      * must be defined for wingS components named 
      * <code>EXCEPTION_STACK_TRACE</code>, 
      * <code>EXCEPTION_MESSAGE</code> and <code>WINGS_VERSION</code>. 
      * @param res the HTTP Response to use
      * @param e the Exception to report
      */
     protected void handleException(HttpServletResponse res, Throwable e) {
         try {
             if (errorFrame == null) {
                 errorFrame = new SFrame();
                 /*
                  * if we don't have an errorTemplateFile defined, then this
                  * will throw an Exception, so the StackTrace is NOT exposed
                  * to the user (may be security relevant)
                  */
                 errorFrame.getContentPane().setLayout(
                         new STemplateLayout(SessionManager.getSession()
                                 .getServletContext().getRealPath(
                                         errorTemplateFile)));
 
                 errorStackTraceLabel = new SLabel();
                 errorFrame.getContentPane().add(errorStackTraceLabel,
                         "EXCEPTION_STACK_TRACE");
 
                 errorMessageLabel = new SLabel();
                 errorFrame.getContentPane().add(errorMessageLabel,
                         "EXCEPTION_MESSAGE");
 
                 versionLabel = new SLabel();
                 errorFrame.getContentPane().add(versionLabel,
                         "WINGS_VERSION");
                 
                 versionLabel.setText("wingS " + Version.getVersion() + " / " + Version.getCompileTime());
             }
 
             res.setContentType("text/html");
             ServletOutputStream out = res.getOutputStream();
             errorStackTraceLabel.setText(getStackTraceString(e));
             errorMessageLabel.setText(e.getMessage()!=null?e.getMessage():"none");
             errorFrame.write(new ServletDevice(out));
         } catch (Exception ex) {
             log.fatal("Exception handling failed.", ex);
         }
         
     }
 
     /**
      * This method is called, whenever a Resource is requested whose
      * name is not known within this session.
      *
      * @param req the causing HttpServletRequest
      * @param res the HttpServletResponse of this request
      */
     protected void handleUnknownResourceRequested(HttpServletRequest req,
                                                   HttpServletResponse res)
             throws IOException {
         res.setStatus(HttpServletResponse.SC_NOT_FOUND);
         res.setContentType("text/html");
         res.getOutputStream().println("<h1>404 Not Found</h1>Unknown Resource Requested");
     }
 
     /**
      * --- HttpSessionBindingListener --- *
      */
 
 
     public void valueBound(HttpSessionBindingEvent event) {
     }
 
 
     public void valueUnbound(HttpSessionBindingEvent event) {
         destroy();
     }
 
     /**
      * get the Session Encoding, that is appended to each URL.
      * Basically, this is response.encodeURL(""), but unfortuntatly, this
      * empty encoding isn't supported by Tomcat 4.x anymore.
      */
     public static String getSessionEncoding(HttpServletResponse response) {
         if (response == null) return "";
         // encode dummy non-empty URL.
         String enc = response.encodeURL("foo").substring(3);
         return enc;
     }
 
     public void destroy() {
         log.info("destroy called");
 
         // Session is needed on destroying the session 
         SessionManager.setSession(session);
         try {
             // hint the gc.
             setParent(null);
             session.destroy();
             session = null;
             errorFrame = null;
             errorStackTraceLabel = null;
             errorMessageLabel = null;
         } catch (Exception ex) {
             log.error("destroy", ex);
         } finally {
             SessionManager.removeSession();
         }
     }
 
     private String getStackTraceString(Throwable e) {
         StringWriter stringWriter = new StringWriter();
         PrintWriter printWriter = new PrintWriter(stringWriter);
         stringWriter.getBuffer().setLength(0);
         e.printStackTrace(printWriter);
         return stringWriter.toString();
     }
 }
 
 
