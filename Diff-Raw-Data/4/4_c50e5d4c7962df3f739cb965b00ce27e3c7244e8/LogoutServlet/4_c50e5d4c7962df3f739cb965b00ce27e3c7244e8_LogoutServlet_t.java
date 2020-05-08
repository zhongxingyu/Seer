 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.moria.servlet;
 
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import no.feide.moria.controller.IllegalInputException;
 import no.feide.moria.controller.InoperableStateException;
 import no.feide.moria.controller.MoriaController;
 import no.feide.moria.log.MessageLogger;
 
 /**
  * This servlet handles logout request. It will invalidate the SSO ticket in the underlying store and remove the cookie
  * from the client.
  * <p/>
  * It uses two properties from the config: <dl> <dt>no.feide.moria.web.sso_cookie.name</dt> <dd>The the cookie name</dd>
  * <dt>no.feide.moria.web.logout.url_param</dt> <dd>The name of the optional parameter in the request holding the
  * redirect url.</dd> </dl>
  *
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 public final class LogoutServlet extends HttpServlet {
 
     /**
      * The logger used in this class.
      */
     private MessageLogger messageLogger;
 
     /**
      * Intitiates the servlet.
      */
     public void init() {
         messageLogger = new MessageLogger(LogoutServlet.class);
     }
 
     /**
      * Handles the GET requests.
      *
      * @param request  The HTTP request object.
      * @param response The HTTP response object.
      */
     public void doGet(final HttpServletRequest request, final HttpServletResponse response) {
 
         final Properties config = RequestUtil.getConfig(getServletContext());
 
         String ssoCookieName = config.getProperty(RequestUtil.PROP_COOKIE_SSO);
 
         if (ssoCookieName == null || ssoCookieName.equals("")) {
             ssoCookieName = "MoriaSSOCookie";
             messageLogger.logWarn("Parameter: " + RequestUtil.PROP_COOKIE_SSO
                                   + " not set in config. Using default value: "
                                   + ssoCookieName);
         }
 
         final Cookie[] cookies = request.getCookies();
         String cookieValue = null;
 
         if (cookies != null) {
             cookieValue = RequestUtil.getCookieValue(ssoCookieName, cookies);
         } else {
             showPage(request, response);
         }
 
         if (cookieValue == null) {
             // TODO: Check for redir parameter first
             showPage(request, response);
         }
 
         /* Invalidate ticket. */
         boolean controllerFailed = false;
 
         try {
             MoriaController.invalidateSSOTicket(cookieValue);
         } catch (InoperableStateException ise) {
             messageLogger.logWarn("Controller in inoperable state.", cookieValue, ise);
             controllerFailed = true;
        } catch (IllegalInputException e) {
            messageLogger.logWarn("Illegal SSO ticket value", cookieValue, e);
         }
 
         if (controllerFailed) {
             final RequestDispatcher requestDispatcher = getServletContext().getNamedDispatcher("JSP-Error.JSP");
 
             try {
                 requestDispatcher.forward(request, response);
             } catch (Exception e) {
                 messageLogger.logCritical("Dispatch to JSP-Error.JSP failed", cookieValue, e);
             }
             /* If everything fails there's not much to do but return. */
             return;
         }
 
         /* Remove cookie if set. */
         if (cookieValue != null) {
             final Cookie ssoCookie = RequestUtil.createCookie(config.getProperty(RequestUtil.PROP_COOKIE_SSO),
                                                               cookieValue, 0);
             response.addCookie(ssoCookie);
         }
 
         /* If redirect url is given in the request; redirect. Else
          * display default response page.
          */
         String urlParam = config.getProperty(RequestUtil.PROP_LOGOUT_URL_PARAM);
 
         if (urlParam == null) {
             urlParam = "redirURL";
             messageLogger.logWarn("Parameter: " + RequestUtil.PROP_LOGOUT_URL_PARAM
                                   + " not set in config. Using default value: "
                                   + urlParam);
         }
 
         final String url = request.getParameter(urlParam);
 
         if (url != null) {
             response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
             response.addHeader("Location", url);
         } else {
             showPage(request, response);
         }
     }
 
     /**
      * Handles POST requests.  Just calls doGet().
      *
      * @param request  The HTTP request object.
      * @param response The HTTP response object.
      */
     public void doPost(final HttpServletRequest request, final HttpServletResponse response) {
         doGet(request, response);
     }
 
     /**
      * Dispatches request to JSP.
      *
      * @param request  The HTTP request object.
      * @param response The HTTP response object,
      */
     private void showPage(final HttpServletRequest request, final HttpServletResponse response) {
         /* Resource bundle. */
         String langFromCookie = null;
         if (request.getCookies() != null) {
             langFromCookie = RequestUtil.getCookieValue((String) RequestUtil.getConfig(getServletContext()).get(
                     RequestUtil.PROP_COOKIE_LANG), request.getCookies());
         }
         final ResourceBundle bundle = RequestUtil.getBundle("logout", request.getParameter("lang"), langFromCookie,
                                                             null,
                                                             request.getHeader("Accept-Language"),
                                                             "en");
         request.setAttribute("bundle", bundle);
 
         final RequestDispatcher requestDispatcher = getServletContext().getNamedDispatcher("Logout.JSP");
 
         try {
             requestDispatcher.forward(request, response);
         } catch (Exception e) {
             messageLogger.logCritical("Dispatch to Logout.JSP failed", e);
         }
     }
 }
