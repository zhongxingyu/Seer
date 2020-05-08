 /*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
 package org.gridlab.gridsphere.portletcontainer;
 
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
 import org.gridlab.gridsphere.layout.PortletLayoutEngine;
 import org.gridlab.gridsphere.layout.PortletPageFactory;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletContext;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portlet.impl.SportletGroup;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.portletcontainer.impl.GridSphereEventImpl;
 import org.gridlab.gridsphere.portletcontainer.impl.SportletMessageManager;
 import org.gridlab.gridsphere.services.core.registry.PortletManagerService;
 import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
 import org.gridlab.gridsphere.services.core.security.auth.AuthorizationException;
 import org.gridlab.gridsphere.services.core.user.LoginService;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 import org.gridlab.gridsphere.services.core.user.UserSessionManager;
 
 import javax.activation.DataHandler;
 import javax.activation.FileDataSource;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.*;
 
 
 /**
  * The <code>GridSphereServlet</code> is the GridSphere portlet container.
  * All portlet requests get proccessed by the GridSphereServlet before they
  * are rendered.
  */
 public class GridSphereServlet extends HttpServlet implements ServletContextListener,
         HttpSessionAttributeListener, HttpSessionListener, HttpSessionActivationListener {
 
     /* GridSphere logger */
     private static PortletLog log = SportletLog.getInstance(GridSphereServlet.class);
 
     /* GridSphere service factory */
     private static SportletServiceFactory factory = null;
 
     /* GridSphere Portlet Registry Service */
     private static PortletManagerService portletManager = null;
 
     /* GridSphere Access Control Service */
     private static AccessControlManagerService aclService = null;
 
     private static UserManagerService userManagerService = null;
 
     private static LoginService loginService = null;
 
     private PortletMessageManager messageManager = SportletMessageManager.getInstance();
 
     /* GridSphere Portlet layout Engine handles rendering */
     private static PortletLayoutEngine layoutEngine = null;
 
     /* Session manager maps users to sessions */
     private UserSessionManager userSessionManager = UserSessionManager.getInstance();
 
     private PortletContext context = null;
     private static Boolean firstDoGet = Boolean.TRUE;
 
     private static PortletSessionManager sessionManager = PortletSessionManager.getInstance();
 
     /**
      * Initializes the GridSphere portlet container
      *
      * @param config the <code>ServletConfig</code>
      * @throws ServletException if an error occurs during initialization
      */
     public final void init(ServletConfig config) throws ServletException {
         super.init(config);
         GridSphereConfig.setServletConfig(config);
         this.context = new SportletContext(config);
         factory = SportletServiceFactory.getInstance();
         log.debug("in init of GridSphereServlet");
     }
 
     public synchronized void initializeServices() throws PortletServiceException {
         // discover portlets
         log.debug("Creating portlet manager service");
         portletManager = (PortletManagerService) factory.createUserPortletService(PortletManagerService.class, GuestUser.getInstance(), getServletConfig().getServletContext(), true);
         // create groups from portlet web apps
         log.debug("Creating access control manager service");
         aclService = (AccessControlManagerService) factory.createUserPortletService(AccessControlManagerService.class, GuestUser.getInstance(), getServletConfig().getServletContext(), true);
         // create root user in default group if necessary
         log.debug("Creating user manager service");
         userManagerService = (UserManagerService) factory.createUserPortletService(UserManagerService.class, GuestUser.getInstance(), getServletConfig().getServletContext(), true);
         loginService = (LoginService) factory.createUserPortletService(LoginService.class, GuestUser.getInstance(), getServletConfig().getServletContext(), true);
     }
 
     /**
      * Processes GridSphere portal framework requests
      *
      * @param req the <code>HttpServletRequest</code>
      * @param res the <code>HttpServletResponse</code>
      * @throws IOException if an I/O error occurs
      * @throws ServletException if a servlet error occurs
      */
     public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
         processRequest(req, res);
     }
 
     public void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
         GridSphereEvent event = new GridSphereEventImpl(aclService, context, req, res);
         PortletRequest portletReq = event.getPortletRequest();
         PortletResponse portletRes = event.getPortletResponse();
 
         // If first time being called, instantiate all portlets
         if (firstDoGet.equals(Boolean.TRUE)) {
             synchronized (firstDoGet) {
                 log.debug("Initializing portlets and services");
                 try {
                     // initailize needed services
                     initializeServices();
                     // initialize all portlets
                     PortletInvoker.initAllPortlets(portletReq, portletRes);
                 } catch (PortletException e) {
                     req.setAttribute(SportletProperties.ERROR, e);
                 }
                 layoutEngine = PortletLayoutEngine.getInstance();
                 firstDoGet = Boolean.FALSE;
             }
         }
 
         System.err.println("Server name= " + req.getServerName());
         System.err.println("Server port= " + req.getServerPort());
 
         setUserAndGroups(portletReq);
 
         // Handle user login and logout
         if (event.hasAction()) {
             if (event.getAction().getName().equals(SportletProperties.LOGIN)) {
                 login(event);
                 //event = new GridSphereEventImpl(aclService, context, req, res);
             }
             if (event.getAction().getName().equals(SportletProperties.LOGOUT)) {
                 logout(event);
                 // since event is now invalidated, must create new one
                 event = new GridSphereEventImpl(aclService, context, req, res);
             }
         }
 
         layoutEngine.actionPerformed(event);
 
         // is this a file download operation?
         downloadFile(event);
 
         // Handle any outstanding messages
         // This needs work certainly!!!
         Map portletMessageLists = messageManager.retrieveAllMessages();
         if (!portletMessageLists.isEmpty()) {
             Set keys = portletMessageLists.keySet();
             Iterator it = keys.iterator();
             String concPortletID = null;
             List messages = null;
             while (it.hasNext()) {
                 concPortletID = (String)it.next();
                 messages = (List)portletMessageLists.get(concPortletID);
                 Iterator newit = messages.iterator();
                 while (newit.hasNext()) {
                     PortletMessage msg = (PortletMessage)newit.next();
                     layoutEngine.messageEvent(concPortletID, msg, event);
                     newit.remove();
                 }
             }
         }
 
         setUserAndGroups(portletReq);
 
         layoutEngine.service(event);
 
         log.debug("Session stats");
         userSessionManager.dumpSessions();
 
         log.debug("Portlet service factory stats");
         factory.logStatistics();
 
         log.debug("Portlet page factory stats");
         try {
             PortletPageFactory pageFactory = PortletPageFactory.getInstance();
             pageFactory.logStatistics();
         } catch (Exception e) {
             log.error("Unable to get page factory", e);
         }
     }
 
     public void setUserAndGroups(PortletRequest req) {
         // Retrieve user if there is one
         User user = null;
         if (req.getPortletSession() != null) {
             String uid = (String)req.getPortletSession().getAttribute(SportletProperties.PORTLET_USER);
             if (uid != null) {
                 user = userManagerService.getUser(uid);
             }
         }
         List groups = null;
         PortletRole role = PortletRole.GUEST;
         if (user == null) {
             user = GuestUser.getInstance();
             groups = new ArrayList();
             groups.add(PortletGroupFactory.GRIDSPHERE_GROUP);
         } else {
             groups = aclService.getGroups(user);
             role = aclService.getRoleInGroup(user, SportletGroup.CORE);
 
             System.err.println("SETTING ROLE=" + role);
         }
         req.setAttribute(SportletProperties.PORTLET_USER, user);
 
 
         req.setAttribute(SportletProperties.PORTLETGROUPS, groups);
         req.setAttribute(SportletProperties.PORTLET_ROLE, role);
     }
 
     /**
      * Handles login requests
      *
      * @param event a <code>GridSphereEvent</code>
      */
     protected void login(GridSphereEvent event) {
         log.debug("in login of GridSphere Servlet");
 
         String LOGIN_ERROR_FLAG = "LOGIN_FAILED";
 
         PortletRequest req = event.getPortletRequest();
         PortletSession session = req.getPortletSession(true);
 
         String username = req.getParameter("username");
         String password = req.getParameter("password");
 
         try {
 
             User user = loginService.login(username, password);
 
             req.setAttribute(SportletProperties.PORTLET_USER, user);
             session.setAttribute(SportletProperties.PORTLET_USER, user.getID());
             if (aclService.hasSuperRole(user)) {
                 log.debug("User: " + user.getUserName() + " logged in as SUPER");
                 //req.setAttribute(SportletProperties.PORTLET_ROLE, PortletRole.SUPER);
             }
             List groups = aclService.getGroups(req.getUser());
             Iterator it = groups.iterator();
             while (it.hasNext()) {
                 PortletGroup g = (PortletGroup)it.next();
                 log.debug("groups:" + g.toString());
             }
             PortletRole role = aclService.getRoleInGroup(user, SportletGroup.CORE);
             req.setAttribute(SportletProperties.PORTLET_ROLE, role);
             req.setAttribute(SportletProperties.PORTLETGROUPS, groups);
             log.debug("Adding User: " + user.getID() + " with session:" + session.getId() + " to usersessionmanager");
             userSessionManager.addSession(user, session);
             layoutEngine.loginPortlets(event);
         } catch (AuthorizationException err) {
             log.debug(err.getMessage());
             req.setAttribute(LOGIN_ERROR_FLAG, err.getMessage());
         }
     }
 
 
     /**
      * Handles logout requests
      *
      * @param event a <code>GridSphereEvent</code>
      */
    protected void logout(GridSphereEvent event) {
         log.debug("in logout of GridSphere Servlet");
         PortletRequest req = event.getPortletRequest();
         PortletSession session = req.getPortletSession();
         session.removeAttribute(SportletProperties.PORTLET_USER);
         userSessionManager.removeSessions(req.getUser());
        layoutEngine.logoutPortlets(event);
     }
 
     /**
      * Method to set the response headers to perform file downloads to a browser
      *
      * @param event the gridsphere event
      * @throws PortletException
      */
     public void downloadFile(GridSphereEvent event) throws PortletException {
         PortletResponse res = event.getPortletResponse();
         PortletRequest req = event.getPortletRequest();
         try {
             String fileName = (String)req.getAttribute("FMP_filename");
             String path = (String)req.getAttribute("FMP_filepath");
             if ((fileName == null) || (path == null)) return;
             log.debug("in downloadFile");
             log.debug("filename: " + fileName + " filepath= " + path);
             File f = new File(path);
 
             FileDataSource fds = new FileDataSource(f);
             res.setContentType(fds.getContentType());
             res.setHeader("Content-Disposition","attachment; filename=" + fileName);
 
             // you should use a datahandler to write out data from a datasource.
             DataHandler handler = new DataHandler(fds);
             handler.writeTo(res.getOutputStream());
         } catch(FileNotFoundException e) {
             log.error("Unable to find file!", e);
         } catch(SecurityException e) {
             // this gets thrown if a security policy applies to the file. see java.io.File for details.
             log.error("A security exception occured!", e);
         } catch(IOException e) {
             log.error("Caught IOException", e);
             //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER,e.getMessage());
 
         }
     }
 
     /**
      * @see #doGet
      */
     public final void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
         doGet(req, res);
     }
 
     /**
      * Return the servlet info.
      *
      * @return a string with the servlet information.
      */
     public final String getServletInfo() {
         return "GridSphere Servlet 0.9";
     }
 
     /**
      * Shuts down the GridSphere portlet container
      */
     public final void destroy() {
         log.debug("in destroy: Shutting down services");
         userSessionManager.destroy();
         layoutEngine.destroy();
         // Shutdown services
         factory.shutdownServices();
         // shutdown the persistencemanagers
         PersistenceManagerFactory.shutdown();
         System.gc();
     }
 
     /**
      * Record the fact that a servlet context attribute was added.
      *
      * @param event The session attribute event
      */
     public void attributeAdded(HttpSessionBindingEvent event) {
 
         log.debug("attributeAdded('" + event.getSession().getId() + "', '" +
                 event.getName() + "', '" + event.getValue() + "')");
 
     }
 
 
     /**
      * Record the fact that a servlet context attribute was removed.
      *
      * @param event The session attribute event
      */
     public void attributeRemoved(HttpSessionBindingEvent event) {
 
         log.debug("attributeRemoved('" + event.getSession().getId() + "', '" +
                 event.getName() + "', '" + event.getValue() + "')");
 
     }
 
 
     /**
      * Record the fact that a servlet context attribute was replaced.
      *
      * @param event The session attribute event
      */
     public void attributeReplaced(HttpSessionBindingEvent event) {
 
         log.debug("attributeReplaced('" + event.getSession().getId() + "', '" +
                 event.getName() + "', '" + event.getValue() + "')");
 
     }
 
 
     /**
      * Record the fact that this ui application has been destroyed.
      *
      * @param event The servlet context event
      */
     public void contextDestroyed(ServletContextEvent event) {
         ServletContext ctx = event.getServletContext();
         log.debug("contextDestroyed()");
         log.debug("contextName: " + ctx.getServletContextName());
         log.debug("context path: " + ctx.getRealPath(""));
 
     }
 
 
     /**
      * Record the fact that this ui application has been initialized.
      *
      * @param event The servlet context event
      */
     public void contextInitialized(ServletContextEvent event) {
         log.debug("contextInitialized()");
         ServletContext ctx = event.getServletContext();
         log.debug("contextName: " + ctx.getServletContextName());
         log.debug("context path: " + ctx.getRealPath(""));
 
     }
 
     /**
      * Record the fact that a session has been created.
      *
      * @param event The session event
      */
     public void sessionCreated(HttpSessionEvent event) {
         log.debug("sessionCreated('" + event.getSession().getId() + "')");
         sessionManager.sessionCreated(event);
     }
 
 
     /**
      * Record the fact that a session has been destroyed.
      *
      * @param event The session event
      */
     public void sessionDestroyed(HttpSessionEvent event) {
         sessionManager.sessionDestroyed(event);
         //loginService.sessionDestroyed(event.getSession());
         log.debug("sessionDestroyed('" + event.getSession().getId() + "')");
         HttpSession s = event.getSession();
 
         //HttpSession session = event.getSession();
         //User user = (User) session.getAttribute(SportletProperties.PORTLET_USER);
         //System.err.println("user : " + user.getUserID() + " expired!");
         //PortletLayoutEngine engine = PortletLayoutEngine.getDefault();
         //engine.removeUser(user);
         //engine.logoutPortlets(event);
     }
 
     /**
      * Record the fact that a session has been created.
      *
      * @param event The session event
      */
     public void sessionDidActivate(HttpSessionEvent event) {
         log.debug("sessionDidActivate('" + event.getSession().getId() + "')");
         sessionManager.sessionCreated(event);
     }
 
 
     /**
      * Record the fact that a session has been destroyed.
      *
      * @param event The session event
      */
     public void sessionWillPassivate(HttpSessionEvent event) {
         sessionManager.sessionDestroyed(event);
         //loginService.sessionDestroyed(event.getSession());
         log.debug("sessionWillPassivate('" + event.getSession().getId() + "')");
         //HttpSession session = event.getSession();
         //User user = (User) session.getAttribute(SportletProperties.USER);
         //System.err.println("user : " + user.getUserID() + " expired!");
         //PortletLayoutEngine engine = PortletLayoutEngine.getDefault();
         //engine.removeUser(user);
         //engine.logoutPortlets(event);
     }
 
 }
