 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared;
 
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.AccountEngine;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.core.flatstorage.FxFlatStorageManager;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.io.Serializable;
 import java.net.URLDecoder;
 import java.util.Locale;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Collections;
 
 /**
  * The [fleXive] context - user session specific data like UserTickets, etc.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxContext implements Serializable {
     private static final long serialVersionUID = -54895743895893486L;
 
     /**
      * Global division id
      */
     public final static int DIV_GLOBAL_CONFIGURATION = 0;
     /**
      * Undefined division id
      */
     public final static int DIV_UNDEFINED = -1;
     /**
      * Session key set if the user successfully logged into the admin area
      */
     public static final String ADMIN_AUTHENTICATED = "$flexive_admin_auth$";
     /**
      * Session key for the session division ID.
      */
     public static final String SESSION_DIVISIONID = "$flexive_division_id$";
 
     private static final Log LOG = LogFactory.getLog(FxContext.class);
     private static ThreadLocal<FxContext> info = new ThreadLocal<FxContext>() {
     };
     private static boolean MANUAL_INIT_CALLED = false;
 
     private final String requestURI;
     private final String remoteHost;
     private final boolean webDAV;
     private final String serverName;
     private final int serverPort;
     private final Map<Object, Object> attributes = new HashMap<Object, Object>();
 
     private String sessionID;
     private boolean treeWasModified;
     private String contextPath;
     private String requestUriNoContext;
     private boolean globalAuthenticated;
     private int division;
     private int runAsSystem;
     private UserTicket ticket;
     private long nodeId = -1;
     private FxTreeMode treeMode;
     private DivisionData divisionData;
 
     private static UserTicket getLastUserTicket(HttpSession session) {
         return (UserTicket) session.getAttribute("LAST_USERTICKET");
     }
 
     private static void setLastUserTicket(HttpSession session, UserTicket lastUserTicket) {
         session.setAttribute("LAST_USERTICKET", lastUserTicket);
     }
 
     public UserTicket getTicket() {
         if (getRunAsSystem()) {
             return ticket.cloneAsGlobalSupervisor();
         }
         return ticket;
     }
 
     public void setTicket(UserTicket ticket) {
         this.ticket = ticket;
     }
 
     /**
      * Get the current users active tree node id, used for FxSQL tree queries
      *
      * @return the current users active tree node id
      * @deprecated  will be removed as soon as tree search is fixed
      */
     public long getNodeId() {
         return nodeId;
     }
 
     /**
      * Set the current users active tree node id used for FxSQL tree queries
      *
      * @param nodeId active tree node id
      * @deprecated  will be removed as soon as tree search is fixed
      */
     public void setNodeId(long nodeId) {
         this.nodeId = nodeId;
     }
 
     /**
      * Get the current users tree mode, used for FxSQL tree queries
      *
      * @return the current users active tree node id
      * @deprecated  will be removed as soon as tree search is fixed
      */
     public FxTreeMode getTreeMode() {
         return treeMode;
     }
 
     /**
      * Set the current users tree mode, used for FxSQL tree queries
      *
      * @return the current users active tree node id
      * @deprecated  will be removed as soon as tree search is fixed
      */
     public void setTreeMode(FxTreeMode treeMode) {
         this.treeMode = treeMode;
     }
 
     /**
      * Returns true if the tree was modified within this thread by the
      * user belonging to this thread.
      *
      * @return true if the tree was modified
      */
     public boolean getTreeWasModified() {
         return treeWasModified;
     }
 
     /**
      * Flag the tree as modified
      */
     public void setTreeWasModified() {
         this.treeWasModified = true;
         CacheAdmin.setTreeWasModified();
     }
 
     /**
      * Get the current users preferred locale (based on his preferred language)
      *
      * @return the current users preferred locale (based on his preferred language)
      */
     public Locale getLocale() {
         return ticket.getLanguage().getLocale();
     }
 
     /**
      * Get the current users preferred language
      *
      * @return the current users preferred language
      */
     public FxLanguage getLanguage() {
         return ticket.getLanguage();
     }
 
     /**
      * Tries to login a user.
      * <p/>
      * The next getUserTicket() call will return the new ticket.
      *
      * @param loginname the unique user name
      * @param password  the password
      * @param takeOver  the take over flag
      * @throws FxLoginFailedException  if the login failed
      * @throws FxAccountInUseException if take over was false and the account is in use
      */
     public void login(String loginname, String password, boolean takeOver) throws FxLoginFailedException,
             FxAccountInUseException {
         // Anything to do at all?
         if (ticket != null && ticket.getLoginName().equals(loginname)) {
             return;
         }
         // Try the login
         AccountEngine acc = EJBLookup.getAccountEngine();
         acc.login(loginname, password, takeOver);
         setTicket(acc.getUserTicket());
     }
 
 
     /**
      * Logout of the current user.
      *
      * @throws FxLogoutFailedException if the function fails
      */
     public void logout() throws FxLogoutFailedException {
         AccountEngine acc = EJBLookup.getAccountEngine();
         acc.logout();
         setTicket(acc.getUserTicket());
     }
 
     /**
      * Override the used ticket.
      * Please do not use this method! Its only purpose is to feed FxContext with a
      * UserTicket when no user is logged in - ie during system startup
      *
      * @param ticket ticket to override with
      */
     public void overrideTicket(UserTicket ticket) {
         if (!getRunAsSystem()) {
             setTicket(ticket);
         }
     }
 
     /**
      * Constructor
      *
      * @param request    the request
      * @param divisionId the division
      * @param isWebdav   true if this is an webdav request
      */
     private FxContext(HttpServletRequest request, int divisionId, boolean isWebdav) {
         this.sessionID = request.getSession().getId();
         this.requestURI = request.getRequestURI();
         this.contextPath = request.getContextPath();
         this.serverName = request.getServerName();
         this.serverPort = request.getServerPort();
         this.requestUriNoContext = request.getRequestURI().substring(request.getContextPath().length());
         this.webDAV = isWebdav;
         if (this.webDAV) {
             // Cut away servlet path, eg. "/webdav/"
             this.requestUriNoContext = this.requestUriNoContext.substring(request.getServletPath().length());
         }
         this.globalAuthenticated = request.getSession().getAttribute(ADMIN_AUTHENTICATED) != null;
         this.remoteHost = request.getRemoteHost();
         this.division = divisionId;
     }
 
     /**
      * Gets the user ticket from the ejb layer, and stores it in the session as 'last used user ticket'
      *
      * @param session the session
      * @return the user ticket
      */
     public static UserTicket getTicketFromEJB(final HttpSession session) {
         UserTicket ticket = EJBLookup.getAccountEngine().getUserTicket();
         setLastUserTicket(session, ticket);
         return ticket;
     }
 
 
     /**
      * Constructor
      */
     private FxContext() {
         sessionID = "EJB_" + System.currentTimeMillis();
         requestURI = "";
         division = -1;
         remoteHost = "127.0.0.1 (SYSTEM)";
         webDAV = false;
         serverName = "localhost";
         serverPort = 80;
     }
 
     /**
      * Returns true if the division is the global configuration division.
      *
      * @return true if the division is the global configuration division
      */
     public boolean isGlobalConfigDivision() {
         return division == DivisionData.DIVISION_GLOBAL;
     }
 
     /**
      * Return true if the current context runs in the test division.
      *
      * @return true if the current context runs in the test division.
      */
     public boolean isTestDivision() {
         return division == DivisionData.DIVISION_TEST;
     }
 
     /**
      * Returns the id of the division.
      * <p/>
      *
      * @return the id of the division.
      */
     public int getDivisionId() {
         return this.division;
     }
 
     /**
      * Changes the division ID. Use with care!
      * (Currently needed for embedded container testing.)
      *
      * @param division the division id
      */
     public void setDivisionId(int division) {
         this.division = division;
     }
 
     /**
      * Changes the context path (Currently needed for embedded container testing.)
      *
      * @param contextPath the context path
      */
     public void setContextPath(String contextPath) {
         this.contextPath = contextPath;
     }
 
     /**
      * Runs all further calls as SYSTEM user with full permissions until stopRunAsSystem
      * gets called. Multiple calls to this function get stacked and the runAsSystem
      * flag is only removed when the stack is empty.
      */
     public void runAsSystem() {
         runAsSystem++;
     }
 
     /**
      * Removes one runeAsSystem flag from the stack.
      */
     public void stopRunAsSystem() {
         if (runAsSystem <= 0) {
             LOG.fatal("stopRunAsSystem called with no system flag on the stack");
         } else
             runAsSystem--;
     }
 
     /**
      * Returns true if all calls are done without permission checks for the time beeing.
      *
      * @return true if all calls are done without permission checks for the time beeing
      */
     public boolean getRunAsSystem() {
         return runAsSystem != 0;
     }
 
 
     /**
      * Returns the session id, which is unique at call time
      *
      * @return the session's id
      */
     public String getSessionId() {
         return sessionID;
     }
 
     /**
      * Sets the session ID.
      *
      * @param sessionID the new session ID
      */
     public void setSessionID(String sessionID) {
         this.sessionID = sessionID;
     }
 
     /**
      * Returns the request URI.
      * <p/>
      * This URI contains the context path, use getRelativeRequestURI() to retrieve the path
      * without it.
      *
      * @return the request URI
      */
     public String getRequestURI() {
         return requestURI;
     }
 
     /**
      * Returns the decoded relative request URI.
      * <p/>
      * This function is the same as calling getRelativeRequestURI(true).
      *
      * @return the URI without its context path
      */
     public String getRelativeRequestURI() {
         return getRelativeRequestURI(true);
     }
 
 
     /**
      * Returns the relative request URI.
      *
      * @param decode if set to true the URI will be decoded (eg "%20" to a space), using UTF-8
      * @return the URI without its context path
      */
     @SuppressWarnings("deprecation")
     public String getRelativeRequestURI(boolean decode) {
         String result = requestURI.substring(contextPath.length());
         if (decode) {
             try {
                 result = URLDecoder.decode(result, "UTF-8");
             } catch (Throwable t) {
                 System.out.print("Failed to decode the URI using UTF-8, using fallback decoding. msg=" + t.getMessage());
                 result = URLDecoder.decode(result);
             }
         }
         return result;
     }
 
     /**
      * Returns the name of the server handling this request, e.g. www.flexive.com
      *
      * @return the name of the server handling this request, e.g. www.flexive.com
      */
     public String getServerName() {
         return serverName;
     }
 
     /**
      * Returns the port of the server handling this request, e.g. 80
      *
      * @return the port of the server handling this request, e.g. 80
      */
     public int getServerPort() {
         return serverPort;
     }
 
     /**
      * Returns the full server URL including the port for this request, e.g. http://www.flexive.com:8080
      *
      * @return the full server URL including the port for this request, e.g. http://www.flexive.com:8080
      */
     public String getServer() {
         return "http://" + serverName + (serverPort != 80 ? ":" + serverPort : "");
     }
 
     /**
      * Returns the calling remote host.
      *
      * @return the remote host.
      */
     public String getRemoteHost() {
         return remoteHost;
     }
 
     /**
      * Returns the id of the appication the request was made in.
      * <p/>
      * In webapps the application id equals the context path
      *
      * @return the id of the appication the request was made in.
      */
     public String getApplicationId() {
         return (contextPath != null && contextPath.length() > 0 && contextPath.charAt(0) == '/') ?
                 contextPath.substring(1) : contextPath;
     }
 
     /**
      * Returns the absolute path for the given resource (i.e. the application name + the path).
      *
      * @param path the path of the resource (e.g. /pub/css/demo.css)
      * @return the absolute path for the given resource
      */
     public String getAbsolutePath(String path) {
         return "/" + getApplicationId() + path;
     }
 
 
     /**
      * Reload the UserTicket, needed i.e. when language settings change
      */
     public void _reloadUserTicket() {
         setTicket(EJBLookup.getAccountEngine().getUserTicket());
     }
 
     /**
      * Returns true if this request is triggered by a webdav operation.
      *
      * @return true if this request is triggered by a webdav operation
      */
     public boolean isWebDAV() {
         return webDAV;
     }
 
     /**
      * Return true if the user successfully authenticated for the
      * global configuration area
      *
      * @return true if the user successfully authenticated for the global configuration
      */
     public boolean isGlobalAuthenticated() {
         return globalAuthenticated;
     }
 
 
     /**
      * Authorize the user for the global configuration area
      *
      * @param globalAuthenticated true if the user should be authorized for the global configuration
      */
     public void setGlobalAuthenticated(boolean globalAuthenticated) {
         this.globalAuthenticated = globalAuthenticated;
     }
 
     /**
      * Returns the request URI without its context.
      *
      * @return the request URI without its context.
      */
     public String getRequestUriNoContext() {
         return requestUriNoContext;
     }
 
     /**
      * Return the context path of this request.
      *
      * @return the context path of this request.
      */
     public String getContextPath() {
         return contextPath;
     }
 
     public DivisionData getDivisionData() {
         if (divisionData == null) {
             if (!DivisionData.isValidDivisionId(division)) {
                 throw new IllegalArgumentException("Unable to obtain DivisionData: Division not defined (" + division + ")");
             }
             try {
                 divisionData = EJBLookup.getGlobalConfigurationEngine().getDivisionData(division);
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
         return divisionData;
     }
 
     /**
      * Returns an independent copy of this context. Also clones the user ticket.
      *
      * @return  an independent copy of this context
      * @since 3.1
      */
     public FxContext copy() {
         final FxContext result = new FxContext();
         result.setTicket(ticket.copy());
         result.setDivisionId(division);
         result.setContextPath(contextPath);
         result.setGlobalAuthenticated(globalAuthenticated);
         result.setNodeId(nodeId);
         result.setSessionID(sessionID);
         if (treeWasModified) {
             result.setTreeWasModified();
         }
         return result;
     }
 
     /**
      * Stores the FxContext instance in the current thread. Will overwrite an existing context.
      *
      * @since 3.1
      */
     public void replace() {
         info.set(this);
     }
 
     /**
      * Store a value under the given key in the current request's FxContext.
      * <p>
      * A value stored in the context exists for the entire time of the fleXive request, for a
      * web request this is slightly shorter than request scope. The main advantage is that the
      * fleXive context is available for any request, not just requests from a web application,
      * and that no overhead for setting or retrieving values exists.
      * </p>
      *
      * @param key   the attribute key
      * @param value the attribute value. If null, the attribute will be removed.
      * @since 3.1
      */
     public void setAttribute(Object key, Object value) {
         if (value == null) {
             attributes.remove(key);
         } else {
             attributes.put(key, value);
         }
     }
 
     /**
      * Return the value stored under the given key.
      * <p>
      * A value stored in the context exists for the entire time of the fleXive request, for a
      * web request this is slightly shorter than request scope. The main advantage is that the
      * fleXive context is available for any request, not just requests from a web application,
      * and that no overhead for setting or retrieving values exists.
      * </p>
      *
      * @param key   the attribute key
      * @return      the value stored under the given key.
      * @since 3.1
      */
     public Object getAttribute(Object key) {
         return attributes.get(key);
     }
 
     /**
      * Return a (unmodifiable) map of all attributes stored in the context.
      *
      * @return  a (unmodifiable) map of all attributes stored in the context.
      * @since 3.1
      */
     public Map<Object, Object> getAttributeMap() {
         return Collections.unmodifiableMap(attributes);
     }
 
     /**
      * Stores the needed informations about the sessions.
      *
      * @param request        the users request
      * @param dynamicContent is the content dynamic?
      * @param divisionId     the division id
      * @param isWebdav       true if this is an webdav request
      * @return FxContext
      */
     public static FxContext storeInfos(HttpServletRequest request, boolean dynamicContent, int divisionId, boolean isWebdav) {
         FxContext si = new FxContext(request, divisionId, isWebdav);
         // Set basic informations needed for the user ticket retrieval
         info.set(si);
         // Do user ticket retrieval and store it in the threadlocal
         final HttpSession session = request.getSession();
         if (session.getAttribute(SESSION_DIVISIONID) == null) {
             session.setAttribute(SESSION_DIVISIONID, divisionId);
         }
         if (dynamicContent || isWebdav) {
             UserTicket last = getLastUserTicket(session);
             // Always determine the current user ticket for dynamic pages and webdav requests.
             // This takes about 1 x 5ms for every request on a development machine
             si.setTicket(getTicketFromEJB(session));
             if (si.ticket.isGuest()) {
                 try {
                     if (last == null)
                         si.ticket.setLanguage(EJBLookup.getLanguageEngine().load(request.getLocale().getLanguage()));
                     else
                         si.ticket.setLanguage(last.getLanguage());
                 } catch (FxInvalidLanguageException e) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Failed to use request locale from browser - unknown language: " + request.getLocale().getLanguage());
                     }
                 } catch (FxApplicationException e) {
                     if (LOG.isInfoEnabled()) {
                         LOG.info("Failed to use request locale from browser: " + e.getMessage(), e);
                     }
                 }
             }
         } else {
             // For static content like images we use the last user ticket stored in the session
             // to speed up the request.
             si.setTicket(getLastUserTicket(session));
             if (si.ticket != null) {
                 if (si.ticket.isGuest() && (si.ticket.getACLAssignments() == null || si.ticket.getACLAssignments().length == 0)) {
                     //reload from EJB layer if we have a guest ticket with no ACL assignments
                     //this can happen during initial loading
                     si.setTicket(getTicketFromEJB(session));
                 }
             }
             if (si.ticket == null) {
                 si.setTicket(getTicketFromEJB(session));
             }
         }
         info.set(si);
         return si;
     }
 
     /**
      * Performs a cleanup of the stored informations.
      */
     public static void cleanup() {
         if (info.get() != null) {
             info.remove();
         }
     }
 
     /**
      * Helper method to bootstrap a [fleXive] system outside an application server
      * (e.g. in unit tests or an standalone application). Should only be called on application
      * startup. Will initialize a FxContext in the current thread with guest user privileges.
      *
      * @param divisionId    the desired division ID (will determin the application datasource)
      * @param applicationName   the application name (mostly used as "imaginary context path")
      * @since 3.1
      */
     public static synchronized void initializeSystem(int divisionId, String applicationName) {
         get().setDivisionId(divisionId);
         get().setContextPath(applicationName);
         get().setTicket(EJBLookup.getAccountEngine().getGuestTicket());
         // initialize flat storage, if available
         FxFlatStorageManager.getInstance().getDefaultStorage();
 
         // force flexive initialization
         get().runAsSystem();
         try {
             CacheAdmin.getEnvironment();
         } finally {
             get().stopRunAsSystem();
         }
         
         if (MANUAL_INIT_CALLED && divisionId == -2) {
             // don't replace userticket
             return;
         }
 
         // load guest ticket
         get().setTicket(EJBLookup.getAccountEngine().getGuestTicket());
 
         MANUAL_INIT_CALLED = true;
     }
 
     /**
      * Returns a string representation of the object.
      *
      * @return a string representation of the object.
      */
     @Override
     public String toString() {
         return this.getClass() + "[sessionId:" + sessionID + ";requestUri:" + requestURI + "]";
     }
 
     // ------ static accessors for frequently used operations -----------
 
     /**
      * Gets the session information for the running thread
      *
      * @return the session information for the running thread
      */
     public static FxContext get() {
         FxContext result = info.get();
         if (result == null) {
             result = new FxContext();
             info.set(result);
         }
         return result;
     }
 
     /**
      * Returns the user ticket associated to the current thread.
      *
      * @return  the user ticket associated to the current thread.
      */
     public static UserTicket getUserTicket() {
         final FxContext context = get();
         if (context == null) {
             throw new NullPointerException("FxContext not set in current thread.");
         }
         return context.getTicket();
     }
 
     /**
      * Replace the threadlocal context with another one.
      * This method provides a mean to escalate the current context to other threads.
      * As a safeguard, the context can only be replaced if the current UserTicket is <code>null</code>
      *
      * @param context the FxContext to use as replacement
      * @deprecated use {@link com.flexive.shared.FxContext#replace()}
      */
     @Deprecated
     public static void replace(FxContext context) {
         if (FxContext.getUserTicket() == null)
             context.replace();
     }
 
     /**
      * Shortcut for {@code FxContext.get().runAsSystem()}.
      *
      * @since 3.1
      */
     public static void startRunningAsSystem() {
         FxContext.get().runAsSystem();
     }
 
     /**
      * Shortcut for {@code FxContext.get().stopRunAsSystem()}.
      *
      * @since 3.1
      */
     public static void stopRunningAsSystem() {
         FxContext.get().stopRunAsSystem();
     }
 
 
     /**
      * Get a FxContext instance to use at the EJB layer.
      * The returned context is a guest user only!
      * This method should only be used internally (for use in different threads, etc.)
      *
      * @param template the template to use for the division
      * @return FxContext
      */
     public static FxContext _getEJBContext(FxContext template) {
         FxContext ctx = new FxContext();
         ctx.division = template.division;
         return ctx;
     }
 }
