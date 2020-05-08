 /* **********************************************************************
     Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package org.bedework.timezones.server;
 
import org.bedework.timezones.common.TzServerUtil;

 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.Enumeration;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpSessionEvent;
 import javax.servlet.http.HttpSessionListener;
 
 /** Serve up timezone information. This server is an intermediate step towards
  * a real global timezone service being promoted in CalConnect
  *
  * <p>This server provides timezones stored in a zip file at a given remote
  * location accessible over http. This allows easy replacement of timezone
  * definitions.
  *
  * @author Mike Douglass
  *
  */
 public class TzServer extends HttpServlet
         implements HttpSessionListener {
   private boolean debug;
 
   protected boolean dumpContent;
 
   protected transient Logger log;
 
  private TzServerUtil util;

   @Override
   public void init(ServletConfig config) throws ServletException {
     try {
       super.init(config);
 
      util = TzServerUtil.getInstance();

       String debugStr = getInitParameter("debug");
       if (debugStr != null) {
         debug = !"0".equals(debugStr);
       }
 
       dumpContent = "true".equals(config.getInitParameter("dumpContent"));
 
       //Properties props = TzServerUtil.getResources(this, config);
 
     } catch (ServletException se) {
       throw se;
     } catch (Throwable t) {
       throw new ServletException(t);
     }
   }
 
   @Override
   protected void service(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException {
     try {
       String methodName = req.getMethod();
 
       if (debug) {
         debugMsg("entry: " + methodName);
         dumpRequest(req);
       }
 
       if (methodName.equals("OPTIONS")) {
         new OptionsMethod(debug).doMethod(req, resp);
       } else if (methodName.equals("GET")) {
         new GetMethod(debug).doMethod(req, resp);
       } else if (methodName.equals("POST")) {
         new PostMethod(debug).doMethod(req, resp);
       } else {
 
       }
     } finally {
       /* We're stateless - toss away any session */
       try {
         HttpSession sess = req.getSession(false);
         if (sess != null) {
           sess.invalidate();
         }
       } catch (Throwable t) {}
     }
   }
 
   /** Debug
    *
    * @param req
    */
   @SuppressWarnings("unchecked")
   public void dumpRequest(HttpServletRequest req) {
     Logger log = getLogger();
 
     try {
       Enumeration<String> names = req.getHeaderNames();
 
       String title = "Request headers";
 
       log.debug(title);
 
       while (names.hasMoreElements()) {
         String key = names.nextElement();
         String val = req.getHeader(key);
         log.debug("  " + key + " = \"" + val + "\"");
       }
 
       names = req.getParameterNames();
 
       title = "Request parameters";
 
       log.debug(title + " - global info and uris");
       log.debug("getRemoteAddr = " + req.getRemoteAddr());
       log.debug("getRequestURI = " + req.getRequestURI());
       log.debug("getRemoteUser = " + req.getRemoteUser());
       log.debug("getRequestedSessionId = " + req.getRequestedSessionId());
       log.debug("HttpUtils.getRequestURL(req) = " + req.getRequestURL());
       log.debug("contextPath=" + req.getContextPath());
       log.debug("query=" + req.getQueryString());
       log.debug("contentlen=" + req.getContentLength());
       log.debug("request=" + req);
       log.debug("parameters:");
 
       log.debug(title);
 
       while (names.hasMoreElements()) {
         String key = names.nextElement();
         String val = req.getParameter(key);
         log.debug("  " + key + " = \"" + val + "\"");
       }
     } catch (Throwable t) {
     }
   }
 
   /**
    * @return Logger
    */
   public Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   /** Debug
    *
    * @param msg
    */
   public void debugMsg(String msg) {
     getLogger().debug(msg);
   }
 
   /** Info messages
    *
    * @param msg
    */
   public void logIt(String msg) {
     getLogger().info(msg);
   }
 
   protected void error(String msg) {
     getLogger().error(msg);
   }
 
   protected void error(Throwable t) {
     getLogger().error(this, t);
   }
 
   /* (non-Javadoc)
    * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
    */
   public void sessionCreated(HttpSessionEvent se) {
   }
 
   /* (non-Javadoc)
    * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
    */
   public void sessionDestroyed(HttpSessionEvent se) {
   }
 }
