 /*
  * ConfluenceCasFilter.java
  * Created: 2 March 2006
  *
  * Copyright (C) 2006 Carl Harris, Jr.  All rights reserved.
  * This software is OSI Certified Open Source Software licensed under the
  * the GNU Lesser General Public License (LGPL).  The full text of the LGPL
  * is available at http://www.gnu.org/copyleft/lesser.html.
  *
  * This software extends the CASFilter class developed at Yale and distributed
  * as part of their Java Client for CAS.  Many insights needed to correctly
  * implement the ConfluenceCasFilter class were derived by studying the CASFilter
  * source code.  The author is grateful that the Yale client source code is
  * publicly available.
  *
  */
 
 package org.soulwing.confluence.cas;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jasig.cas.client.web.filter.AbstractCasFilter;
 
 /**
  * Filter designed to enable CAS authentication for Confluence. Implements URL
  * path exclusion, makes both Atlassian-user authentication and CAS
  * authentication possible, and provides CAS global logout via the Confluence
  * logout action.
  * 
  * Intended to be configured by Spring (or other ioc container). Specifically,
  * it should be given a reference to the CAS AuthenticationFilter via the
  * <code>setFilter()</code> method.
  * 
  * @author Carl Harris
  * @author Matt Drees
  */
 public class ConfluenceCasFilter implements Filter {
 
   private static final String CAS_FILTER_BYPASS = "CAS_FILTER_BYPASS";
 
   private static final String CAS_FILTER_TICKET = "CAS_FILTER_TICKET";
 
   private static Log log = LogFactory.getLog(ConfluenceCasFilter.class);
 
   private String loginPath;
 
   private String logoutPath;
 
   private String logoutUrl;
 
   private Collection<String> bypassPrefixes;
 
   private Filter filter;
 
   /** Initialize the filter. */
   public void init(FilterConfig config) throws ServletException {
   }
 
   /** Filter processing. */
   public void doFilter(ServletRequest request, ServletResponse response,
       FilterChain fc) throws ServletException, IOException {
 
     // make sure we've got an HTTP request
     if (!(request instanceof HttpServletRequest)
         || !(response instanceof HttpServletResponse)) {
       log
           .error("doFilter() called on a request or response that was not an HttpServletRequest or response.");
       throw new ServletException(
           "ConfluenceCasFilter protects only HTTP resources");
     }
 
     HttpServletRequest httpRequest = (HttpServletRequest) request;
     HttpServletResponse httpResponse = (HttpServletResponse) response;
 
     // The user's ticket won't appear in every request, so we grab it whenever
     // we it and store it as a session attribute that we can use in the logout
     // method.
     storeTicket(httpRequest);
 
     // is this a logout request?
     if (isLogout(httpRequest)) {
       log.debug("intercepted logout URL... bypassing CASFilter");
       // we need to chain to the other filters first, since we need to be logged
       // in in order to log out. :-)
       fc.doFilter(httpRequest, httpResponse);
       logout(httpRequest, httpResponse);
     }
     // is this session already marked for CAS bypass?
     else if (isBypassed(httpRequest)) {
       log.debug("CASFilter bypass already active");
       fc.doFilter(httpRequest, httpResponse);
     }
     // is this a request for the login URL?
     else if (isLogin(httpRequest)) {
       log.debug("CASFilter bypassed: matched loginPattern");
       fc.doFilter(httpRequest, httpResponse);
     }
     // should this request bypass CAS?
     else if (shouldBypass(httpRequest)) {
       // pass to filter chain, bypassing CASFilter
       log.debug("CASFilter bypassed: matched bypassPrefix");
       fc.doFilter(httpRequest, httpResponse);
     } else {
       // delegate to assigned filter (probably cas AuthenticationFilter)
       filter.doFilter(request, response, fc);
     }
   }
 
   /**
    * Determines whether the CASFilter has been bypassed for the specified
    * <code>request</code>.
    * 
    * @param request
    *          request to check for CAS bypass
    * @return <code>true</code> if CAS has been bypassed for this request.
    */
   public static boolean isBypassed(HttpServletRequest request) {
     Object bypass = request.getSession().getAttribute(CAS_FILTER_BYPASS);
     return bypass != null;
   }
 
   /**
    * Tests a <code>HttpServletRequest</code> to determine if it should bypass
    * the CAS filter. Sets a flag in the session to indicate that CAS has been
    * bypassed for this <code>request</code>.
    * 
    * @param request
    *          request to test for bypass.
    * @return <code>true</code> if <code>request</code> should bypass the CAS
    *         filter.
    */
   private boolean shouldBypass(HttpServletRequest request) {
     // is this a request for a URL prefix that should not use CAS?
     String path = request.getServletPath();
     for (String prefix : bypassPrefixes) {
       if (path.startsWith(prefix)) {
         log.debug("path " + path + " matches bypass prefix " + prefix);
         return true;
       }
     }
     return false;
   }
 
   /**
    * Determine if the specified <code>request</code> is a request for the
    * Confluence login page. This method recognizes the login path only if
    * <strong>both</strong> the loginPath and logoutPath fields have been set by
    * filter init parameters.
    * 
    * @param request
    *          request to test against login path
    * @returns <code>true</code> if <code>request</code> is a request for the
    *          login page
    */
   private boolean isLogin(HttpServletRequest request) {
     // Requiring both the login and logout paths to be set ensures that we
     // won't set CAS_FILTER_BYPASS for the login action without having the means
     // to clear it at logout. Clearing the bypass flag prevents some
     // browser-back-button confusion but isn't required for security reasons.
     String path = request.getServletPath();
     if (loginPath != null && logoutPath != null && path.equals(loginPath)) {
       setBypassAttribute(request);
       return true;
     }
     return false;
   }
 
   /**
    * Determine if the specified <code>request</code> is a logout request.
    * 
    * @param request
    *          request to test for logout
    * @returns <code>true</code> if <code>request</code> is a logout request.
    */
   private boolean isLogout(HttpServletRequest request) {
     String path = request.getServletPath();
     if (logoutPath != null && path.equals(logoutPath)) {
       return true;
     }
     return false;
   }
 
   /**
    * Performs a CAS global logout by removing the CAS ticket/receipt information
    * from this <code>request</code>.
    * 
    * @param request
    *          request to logout
    */
   private void logout(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException {
 
     javax.servlet.http.HttpSession session = request.getSession();
 
     // if CAS has been bypassed, we don't need to bother with CAS logout.
     if (isBypassed(request)) {
       session.removeAttribute(CAS_FILTER_BYPASS);
       return;
     }
 
     // If the ticket parameter was specified, we prefer it. Otherwise, we
     // fallback to using the copy we stored in the session.
     String ticket = request.getParameter(AbstractCasFilter.PARAM_TICKET);
     if (ticket == null) {
       ticket = (String) request.getSession().getAttribute(CAS_FILTER_TICKET);
     }
     session.removeAttribute(AbstractCasFilter.CONST_ASSERTION);
     request.removeAttribute(AbstractCasFilter.CONST_PRINCIPAL);
     session.removeAttribute(CAS_FILTER_TICKET);
 
     // If we have a logoutUrl and we have a ticket for this user, redirect the
     // browser to the CAS global logout page.
     if (logoutUrl != null && ticket != null) {
       StringBuilder sb = new StringBuilder();
       sb.append(logoutUrl);
       if (logoutUrl.contains("?")) {
         sb.append('&');
       } else {
         sb.append('?');
       }
       sb.append(AbstractCasFilter.PARAM_TICKET);
       sb.append(ticket);
       String casLogout = sb.toString();
       log.debug("sending redirect to " + casLogout);
       response.sendRedirect(casLogout);
     }
   }
 
   /**
    * Stores the value of the <code>ticket</code> parameter in the session
    * attributes for the specified <code>request</code>
    * 
    * @param request
    *          request that may contain a ticket parameter.
    */
   private void storeTicket(HttpServletRequest request) {
     String ticket = request.getParameter(AbstractCasFilter.PARAM_TICKET);
     if (ticket != null) {
       request.getSession().setAttribute(CAS_FILTER_TICKET, ticket);
     }
   }
 
   /**
    * Sets the CAS_FILTER_BYPASS session attribute in the specified
    * <code>request</code>.
    * 
    * @param request
    *          request to mark for CAS bypass
    */
   private void setBypassAttribute(HttpServletRequest request) {
     log.debug("set CAS_FILTER_BYPASS session attribute");
     request.getSession().setAttribute(CAS_FILTER_BYPASS, Boolean.TRUE);
   }
 
   /**
    * Sets the path for the Confluence login action.
    * 
    * @param loginPath
    *          path to login action (e.g. <code>/login.action</code>)
    */
   public void setLoginPath(String loginPath) {
     if (loginPath != null) {
       loginPath.trim();
     }
     this.loginPath = loginPath;
     log.debug("login path set to " + loginPath);
   }
 
   /**
    * Sets the path for the Confluence logout action.
    * 
    * @param logoutPath
    *          path to logout action (e.g. <code>/logout.action</code>)
    */
   public void setLogoutPath(String logoutPath) {
     if (logoutPath != null) {
       logoutPath.trim();
     }
     this.logoutPath = logoutPath;
     log.debug("logout path set to " + logoutPath);
   }
 
   /**
    * Sets the URL for CAS logout.
    * 
    * @param logoutUrl
    *          URL for the CAS service logout function.
    */
   public void setLogoutUrl(String logoutUrl) {
     if (logoutUrl != null) {
       logoutUrl.trim();
     }
     this.logoutUrl = logoutUrl;
     log.debug("logout URL set to " + logoutUrl);
   }
 
   /**
    * Sets the URL path prefixes that should be bypassed.
    * 
    * @param prefixes
    *          a comma-delimited list of path prefixes typically from an init
    *          parameter; e.g. <code>/rpc,/login.action</code>
    */
   public void setBypassPrefixes(String prefixes) {
     if (prefixes != null) {
       bypassPrefixes = Arrays.asList(prefixes.split("\\p{Space}*,\\p{Space}*"));
       for (String prefix : bypassPrefixes) {
         log.debug("paths prefixed with " + prefix + " will bypass CASFilter");
       }
     } else {
       bypassPrefixes = new ArrayList<String>();
     }
   }
 
  public Collection<String> getBypassPrefixes() {
     return bypassPrefixes;
   }
 
  public void setBypassPrefixes(Collection<String> bypassPrefixes) {
     this.bypassPrefixes = bypassPrefixes;
   }
 
   public String getLoginPath() {
     return loginPath;
   }
 
   public String getLogoutPath() {
     return logoutPath;
   }
 
   public String getLogoutUrl() {
     return logoutUrl;
   }
 
   public Filter getFilter() {
     return filter;
   }
 
   public void setFilter(Filter filter) {
     this.filter = filter;
   }
 
   public void destroy() {
   }
 }
