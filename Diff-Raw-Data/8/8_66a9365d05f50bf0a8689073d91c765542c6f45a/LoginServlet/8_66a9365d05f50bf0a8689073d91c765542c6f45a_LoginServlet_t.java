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
 
import no.feide.moria.controller.AuthorizationException;
import no.feide.moria.controller.IllegalInputException;
 import no.feide.moria.controller.InoperableStateException;
 import no.feide.moria.controller.MoriaController;
 import no.feide.moria.controller.UnknownTicketException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 /**
  * Use this servlet to bootstrap the system.
  * Set &lt;load-on-startup&gt;1&lt;/load-on-startup&gt; in web.xml.
  * 
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 public class LoginServlet extends HttpServlet {
 
     /**
      * Intitiates the controller.
      */
     public void init() {
         MoriaController.initController(getServletContext());
     }
 
     /**
      * Handles the GET requests.
      *
      * @param request  the HTTP request object
      * @param response the HTTP response object
      * @throws IOException
      * @throws ServletException
      */
     public final void doGet(final HttpServletRequest request, final HttpServletResponse response)
             throws IOException, ServletException {
         showLoginPage(request, response, null);
     }
 
     public final void doPost(final HttpServletRequest request, final HttpServletResponse response)
             throws IOException, ServletException {
         Properties config = getConfig();
 
         /* Login ticket */
         String loginTicketId = request.getParameter(config.getProperty("loginTicketID"));
 
         /* SSO ticket */
         String ssoTicketId = request.getParameter(config.getProperty("ssoTicketID"));
 
         String username = request.getParameter("username");
         String password = request.getParameter("password");
         String org = request.getParameter("org");
 
         /* Parse username */
         if (username.indexOf("@") != -1) {
             org = username.substring(username.indexOf("@") + 1, username.length());
         }
 
         /* Validate input */
         if (username == null || username.equals("")) {
             showLoginPage(request, response, "nocred");
         } else if (password == null || password.equals("")) {
             showLoginPage(request, response, "nocred");
         } else if (org == null || org.equals("") || org.equals("null")) {
             showLoginPage(request, response, "noorg");
         } else if (RequestUtil.parseConfig(getConfig(), "org", "en").get(org) == null) { // TODO: Requires english bundle
             showLoginPage(request, response, "errorg");
         }
 
         // TODO: Attempt login
         // TODO: GEt redirect url
         // TODO: Set SSO ticket in cookie
         // TODO: Redirect to web service if successful
 
         // TODO: If error other than UnknownTicketException, show loginpage with correct error
     }
 
     /**
      * Displays the login page. The method fills the request object with values and
      * then pass the request to the jsp.
      *
      * @param request
      * @param response
      * @throws IOException
      * @throws ServletException
      */
     private void showLoginPage(final HttpServletRequest request, final HttpServletResponse response, String errorType)
             throws IOException, ServletException {
 
         // TODO: Do not throw exceptions, set INTERNAL SERVER ERRROR status
         String jspLocation = getServletContext().getInitParameter("jsp.location");
         Properties config = getConfig();
         HashMap serviceProperties = null;
 
         /* Ticket */
         String loginTicketId = request.getParameter(config.getProperty("loginTicketID"));
 
         /* Base URL */
         request.setAttribute("baseURL", config.getProperty("loginURLPrefix") + "?" + config.getProperty("loginTicketID") + "=" + loginTicketId);
 
         try {
             /* Service properties */
             serviceProperties = MoriaController.getServiceProperties(loginTicketId);
             /* Seclevel */
             request.setAttribute("secLevel", "" + MoriaController.getSecLevel(loginTicketId));
        } catch (AuthorizationException e) {
            errorType = "authorization";
         } catch (UnknownTicketException e) {
             errorType = "unknownTicket";
        } catch (IllegalInputException e) {
            errorType = "illegalInput";
         } catch (InoperableStateException e) {
             errorType = "inoperableState";
         }
 
         /* Error message */
         request.setAttribute("errorType", errorType);
 
         /* Resource bundle */
         String serviceLang = null;
         if (serviceProperties != null) {
             serviceLang = (String) serviceProperties.get("lang");
         }
         ResourceBundle bundle = RequestUtil.getBundle("login",
                 request.getParameter("lang"),
                 request.getCookies(),
                 serviceLang,
                 request.getHeader("Accept-Language"),
                 "en");
         request.setAttribute("bundle", bundle);
 
         /* Configured values */
         request.setAttribute("organizationNames", RequestUtil.parseConfig(getConfig(), "org", bundle.getLocale().getLanguage()));
         request.setAttribute("languages", RequestUtil.parseConfig(getConfig(), "lang", "common"));
 
         /* Selected realm */
         String selectedOrg = request.getParameter("org");
         if (selectedOrg == null || selectedOrg.equals("")) {
             RequestUtil.getCookieValue("org", request.getCookies());
             if (selectedOrg == null || selectedOrg.equals("")) {
                 if (serviceProperties != null) {
                     selectedOrg = (String) serviceProperties.get("home");
                 }
             }
         }
         request.setAttribute("selectedOrg", selectedOrg);
 
 
         /* Selected language */
         request.setAttribute("selectedLang", bundle.getLocale());
         if (request.getParameter("lang") != null) {
             // TODO: Cookie TTL should come from config
             response.addCookie(RequestUtil.createCookie("lang", request.getParameter("lang"), 7));
         }
 
         /* Service attributes */
         if (serviceProperties != null) {
             request.setAttribute("clientName", (String) serviceProperties.get("displayName"));
             request.setAttribute("clientURL", (String) serviceProperties.get("url"));
         }
 
         // TODO: Include instead of forward
         /* Process jsp */
         RequestDispatcher rd = getServletContext().getRequestDispatcher(jspLocation + "/login.jsp");
         rd.include(request, response);
     }
 
     /**
      * Get the config from the context. The configuration is expected to be set
      * by the controller before requests are sent to this servlet.
      *
      * @return the configuration
      * @throws IllegalStateException if the config is not set in the context
      */
     private Properties getConfig() {
         Properties config;
 
         /* Validate config */
         try {
             config = (Properties) getServletContext().getAttribute("config");
         } catch (ClassCastException e) {
             // TODO: Log
             throw new IllegalStateException("Config is not correctly set in context.");
         }
         if (config == null) {
             // TODO: Log
             throw new IllegalStateException("Config is not set in context.");
         }
 
         return config;
     }
 }
