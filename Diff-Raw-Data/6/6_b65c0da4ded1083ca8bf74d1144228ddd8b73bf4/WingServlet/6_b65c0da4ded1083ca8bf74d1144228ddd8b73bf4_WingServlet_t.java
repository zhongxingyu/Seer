 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
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
 import org.wings.RequestURL;
 import org.wings.externalizer.AbstractExternalizeManager;
 import org.wings.externalizer.ExternalizedResource;
 import org.wings.externalizer.SystemExternalizeManager;
 import org.wings.io.Device;
 import org.wings.io.ServletDevice;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.text.DateFormat;
 
 /**
  * Central servlet delegating all requests to the according wingS session servlet.
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  */
 public class WingServlet
         extends HttpServlet {
     protected final transient static Log log = LogFactory.getLog(WingServlet.class);
 
     /**
      * used to init session servlets
      */
     protected ServletConfig servletConfig = null;
 
     private String lookupName = "SessionServlet";
 
     public WingServlet() {
     }
 
 
     protected void initLookupName(ServletConfig config) {
         // with specified lookupname it is possible to handle different sessions
         // for servlet aliases/mappings
         lookupName = config.getInitParameter("wings.servlet.lookupname");
 
         if (lookupName == null || lookupName.trim().length() == 0) {
             lookupName = "SessionServlet:" + config.getInitParameter("wings.mainclass");
         }
 
         log.info("use session servlet lookup name " + lookupName);
     }
 
     /**
      * The following init parameters are known by wings.
      * <p/>
      * <dl compact>
      * <dt>externalizer.timeout</dt><dd> - The time, externalized objects
      * are kept, before they are removed</dd>
      * <p/>
      * <dt>content.maxlength</dt><dd> - Maximum content lengt for form posts.
      * Remember to increase this, if you make use of the SFileChooser
      * component</dd>
      * <p/>
      * <dt>filechooser.uploaddir</dt><dd> - The directory, where uploaded
      * files ar stored temporarily</dd>
      * </dl>
      * <p/>
      * <dt>wings.servlet.lookupname</dt><dd> - The name the wings sessions of
      * this servlet instance are stored in the servlet session hashtable</dd>
      * </dl>
      *
      * @throws ServletException
      */
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         servletConfig = config;
 
         if (log.isInfoEnabled()) {
             log.info("Initializing wingS global servlet with configuration:");
             for (Enumeration en = config.getInitParameterNames(); en.hasMoreElements();) {
                 String param = (String) en.nextElement();
                 log.info("    " + param + " = " + config.getInitParameter(param));
             }
         }
 
         initLookupName(config);
     }
 
     /**
      * returns the last modification of an externalized resource to allow the
      * browser to cache it.
      */
     protected long getLastModified(HttpServletRequest request) {
         AbstractExternalizeManager extMgr;
         try {
             extMgr = getExternalizeManager(request);
         } catch (Exception e) {
             return System.currentTimeMillis();
         }
         String pathInfo = getPathInfo(request);
         if (extMgr != null && pathInfo != null && pathInfo.length() > 1) {
             String identifier = pathInfo.substring(1);
             ExternalizedResource info = extMgr.getExternalizedResource(identifier);
             if (info != null) {
                 //System.err.println("  **>" + info.getLastModified());
                 return info.getLastModified();
             }
         }
         return -1;
     }
 
     /**
      * Parse POST request with <code>MultipartRequest</code> and passes to <code>doGet()</code>
      */
     public final void doPost(HttpServletRequest req, HttpServletResponse res)
             throws ServletException {
         SessionServlet sessionServlet = getSessionServlet(req, res, true);
 
         if (log.isDebugEnabled()) {
             log.debug((sessionServlet != null) ?
                     lookupName :
                     "no session yet ..");
         }
 
         // Wrap with MultipartRequest which can handle multipart/form-data
         // (file - upload), otherwise behaves like normal HttpServletRequest
         try {
             int maxContentLength = sessionServlet.getSession().getMaxContentLength();
             req = new MultipartRequest(req, maxContentLength * 1024);
         } catch (Exception e) {
             log.fatal(null, e);
         }
 
         if (log.isDebugEnabled()) {
             if (req instanceof MultipartRequest) {
                 MultipartRequest multi = (MultipartRequest) req;
                 log.debug("Files:");
                 Iterator files = multi.getFileNames();
                 while (files.hasNext()) {
                     String name = (String) files.next();
                     String filename = multi.getFileName(name);
                     String type = multi.getContentType(name);
                     File f = multi.getFile(name);
                     log.debug("name: " + name);
                     log.debug("filename: " + filename);
                     log.debug("type: " + type);
                     if (f != null) {
                         log.debug("f.toString(): " + f.toString());
                         log.debug("f.getDescription(): " + f.getName());
                         log.debug("f.exists(): " + f.exists());
                         log.debug("f.length(): " + f.length());
                         log.debug("\n");
                     }
                 }
             }
         }
 
         doGet(req, res);
     }
 
     private final SessionServlet newSession(HttpServletRequest request,
                                             HttpServletResponse response)
             throws ServletException {
         long timestamp = System.currentTimeMillis();
         try {
             log.debug("--- new SessionServlet()");
 
             SessionServlet sessionServlet = new SessionServlet();
             sessionServlet.setParent(this);
             sessionServlet.init(servletConfig, request, response);
 
             Session session = sessionServlet.getSession();
             /* the request URL is needed already in the setup-phase. Note,
              * that at this point, the URL will always be encoded, since
              * we (or better: the servlet engine) does not know yet, if setting
              * a cookie will be successful (it has to await the response).
              * Subsequent requests might decide, _not_ to encode the sessionid
              * in the URL (see SessionServlet::doGet())                   -hen
              */
             RequestURL requestURL = new RequestURL("", SessionServlet.getSessionEncoding(response));
 
             session.setProperty("request.url", requestURL);
 
             log.debug("--- Time needed to create new session " + (System.currentTimeMillis() - timestamp) + "ms");
 
             return sessionServlet;
         } catch (Exception e) {
             log.fatal("Error on creating new wingS session", e);
             throw new ServletException(e);
         }
     }
 
     public final SessionServlet getSessionServlet(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   boolean createSessionServlet)
             throws ServletException {
         final HttpSession httpSession = request.getSession(true);
 
         // it should be enough to synchronize on the http session object...
         synchronized (httpSession) {
             SessionServlet sessionServlet = null;
 
             if (httpSession != null) {
                 sessionServlet = (SessionServlet) httpSession.getAttribute(lookupName);
             }
 
             // Sanity check - maybe this is a stored/deserialized session servlet?
             if (sessionServlet != null && !sessionServlet.isValid()) {
                 sessionServlet.destroy();
                 sessionServlet = null;
             }
 
             /*
              * we are only interested in a new session, if the response is
              * not null. If it is null, then we just called getSessionServlet()
              * for lookup purposes and are satisfied, if we don't get anything.
              */
             if (sessionServlet == null) {
                 if (createSessionServlet) {
                     log.info("no session servlet, create new one");
                     sessionServlet = newSession(request, response);
                     httpSession.setAttribute(lookupName, sessionServlet);
                 } else {
                     return null;
                 }
             }
 
 
             if (log.isDebugEnabled()) {
                 StringBuilder message = new StringBuilder()
                         .append("session id: ").append(request.getRequestedSessionId())
                         .append(", created at: ")
                         .append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                 .format(new java.util.Date(httpSession.getCreationTime())))
                         .append(", identified via:").append(request.isRequestedSessionIdFromCookie() ? " cookie" : "")
                         .append(request.isRequestedSessionIdFromURL() ? " URL" : "")
                         .append(", expiring after: ")
                         .append(httpSession.getMaxInactiveInterval())
                         .append("s ");
                 log.debug(message.toString()
                 );
                 //log.debug("session valid " + request.isRequestedSessionIdValid());
                 //log.debug("session httpsession id " + httpSession.getId());
                 //log.debug("session httpsession new " + httpSession.isNew());
                 //log.debug("session last accessed at " +
                 //        new java.util.Date(httpSession.getLastAccessedTime()));
                 //log.debug("session expiration timeout (s) " +
                 //        httpSession.getMaxInactiveInterval());
                 //log.debug("session contains wings session " +
                 //        (httpSession.getAttribute(lookupName) != null));
             }
 
             sessionServlet.getSession().getExternalizeManager().setResponse(response);
 
             /* Handling of the requests character encoding.
              * --------------------------------------------
              * The following block is needed for a correct handling of
              * non-ISO-8859-1 data:
              *
              * Using LocaleCharacterSet and/or charset.properties we can
              * advise the client to use i.e. UTF-8 as character encoding.
              * Once told the browser consequently also encodes his requests
              * in the choosen characterset of the sings session. This is
              * achieved by adding the HTML code
              * <meta http-equiv="Content-Type" content="text/html;charset="<charset>">
              * to the generated pages.
              *
              * If the user hasn't overridden the encoding in their browser,
              * then all form data (e.g. mueller) is submitted with data encoded
              * like m%C3%BCller because byte pair C3 BC is how the german
              * u-umlaut is represented in UTF-8. If the form is
              * iso-8859-1 encoded then you get m%FCller, because byte FC is
              * how it is presented in iso-8859-1.
              *
              * So the browser behaves correctly by sending his form input
              * correctly encoded in the advised character encoding. The issue
              * is that the servlet container is typically unable to determine
              * the correct encoding of this form data. By proposal the browser
              * should als declare the used character encoding for his data.
              * But actual browsers omit this information and hence the servlet
              * container is unable to guess the right encoding (Tomcat actually
              * thenalways guesses ISO 8859-1). This results in totally
              * scrumbled up data for all non ISO-8859-1 character encodings.
              * With the block below we tell the servlet container about the
              * character encoding we expect in the browsers request and hence
              * the servlet container can do the correct decoding.
              * This has to be done at very first, otherwise the servlet
              * container will ignore this setting.
              */
             if ((request.getCharacterEncoding() == null)) { // was servlet container able to identify encoding?
                 try {
                     String sessionCharacterEncoding = sessionServlet.getSession().getCharacterEncoding();
                     // We know better about the used character encoding than tomcat
                     log.debug("Advising servlet container to interpret request as " + sessionCharacterEncoding);
                     request.setCharacterEncoding(sessionCharacterEncoding);
                 } catch (UnsupportedEncodingException e) {
                     log.warn("Problem on applying current session character encoding", e);
                 }
             }
 
             return sessionServlet;
         }
     }
 
     public static void installSession(HttpServletRequest req, HttpServletResponse res) {
         ServletContext context = req.getSession().getServletContext();
         String lookupName = context.getInitParameter("wings.servlet.lookupname");
 
         if (lookupName == null || lookupName.trim().length() == 0) {
             lookupName = "SessionServlet:" + context.getInitParameter("wings.mainclass");
         }
         SessionServlet sessionServlet = (SessionServlet) req.getSession().getAttribute(lookupName);
         if (sessionServlet != null) {
             Session session = sessionServlet.getSession();
             session.setServletRequest(req);
             session.setServletResponse(res);
             SessionManager.setSession(session);
         }
     }
 
     public static void uninstallSession() {
         SessionManager.removeSession();
     }
 
     /** -- externalization -- **/
 
     /**
      * returns, whether this request is to serve an externalize request.
      */
     protected boolean isSystemExternalizeRequest(HttpServletRequest request) {
         String pathInfo = getPathInfo(request);
         return (pathInfo != null
                 && pathInfo.length() > 1
                 && pathInfo.startsWith("/-"));
     }
 
 
     protected AbstractExternalizeManager getExternalizeManager(HttpServletRequest req)
             throws ServletException {
         if (isSystemExternalizeRequest(req)) {
             return SystemExternalizeManager.getSharedInstance();
         } else {
             SessionServlet sessionServlet = getSessionServlet(req, null, false);
             if (sessionServlet == null) {
                 return null;
             }
             return sessionServlet.getSession().getExternalizeManager();
         }
     }
 
     public void doGet(HttpServletRequest req,
                             HttpServletResponse response)
             throws ServletException {
 
         try {
             /*
              * make sure, that our context ends with '/'. Otherwise redirect
              * to the same location with appended slash.
              *
              * We need a '/' at the
              * end of the servlet, so that relative requests work. Relative
              * requests are either externalization requests, providing the
              * required resource name in the path info (like 'abc_121.gif')
              * or 'normal' requests which are just an empty URL with the
              * request parameter (like '?12_22=121').
              * The browser assembles the request URL from the current context
              * (the 'directory' it assumes it is in) plus the relative URL.
              * Thus emitted URLs are as short as possible and thus the
              * generated page size.
              */
             String pathInfo = getPathInfo(req);
            // XXX - response.isCommited() is needed because due to some bug
            // when using the tomcat form authentication the response WILL get commited.
             if (pathInfo == null || pathInfo.length() == 0 && !response.isCommitted()) {
                 StringBuffer pathUrl = req.getRequestURL();
                 pathUrl.append('/');
                 if (req.getQueryString() != null) {
                     pathUrl.append('?').append(req.getQueryString());
                 }
 
                 log.debug("redirect to " + pathUrl.toString());
                // XXX - response.isCommited() is needed because due to some bug
                // when using the tomcat form authentication the response WILL get commited.
                 if (!response.isCommitted()) {
                     response.sendRedirect(pathUrl.toString());
                     return;
                 }
             }
 
             /*
             * we either have a request for the system externalizer
             * (if there is something in the path info, that starts with '-')
             * or just a normal request to this servlet.
             */
             if (isSystemExternalizeRequest(req)) {
                 String identifier = pathInfo.substring(1);
                 AbstractExternalizeManager extManager =
                         SystemExternalizeManager.getSharedInstance();
                 ExternalizedResource extInfo = extManager
                         .getExternalizedResource(identifier);
                 if (extInfo != null) {
                     final Device outputDevice = createOutputDevice(req, response, extInfo);
                     try {
                         extManager.deliver(extInfo, response, outputDevice);
                     } finally {
                         outputDevice.close();
                     }
                 }
                 return;
             }
 
             SessionServlet sessionServlet = getSessionServlet(req, response, true);
 
             sessionServlet.doGet(req, response);
 
         } catch (ServletException e) {
             log.fatal("doGet", e);
             throw e;
         } catch (Throwable e) {
             log.fatal("doGet", e);
             throw new ServletException(e);
         }
     }
 
     /**
      * create a Device that is used to deliver the content, that is
      * not session specific, i.e. that is delivered by the SystemExternalizer.
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
         return new ServletDevice(response.getOutputStream(),
                                  response.getCharacterEncoding());
     }
 
     // TODO BSC: This issue is still pending. Refer to http://jira.j-wings.org/browse/WGS-84
 
     /**
      * Workaround implementation for WebSphere.
      *
      * @return "/" if <code>request.getPathInfo()</code> returns null but URL indicates a trailing slash.
      *         Otherwise original value is returned.
      */
     private static String getPathInfo(HttpServletRequest request) {
         final String pathInfo = request.getPathInfo();
         if (pathInfo == null) {
             final String requestURL = request.getRequestURL().toString();
             return (requestURL.lastIndexOf("/") == requestURL.length() - 1) ? "/" : null;
         } else {
             return pathInfo;
         }
     }
 }
 
 
