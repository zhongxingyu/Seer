 /*
 * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
 * @version $Id: PortletServlet.java 5032 2006-08-17 18:15:06Z novotny $
 */
 package org.gridsphere.provider.portlet.jsr;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.gridsphere.portlet.impl.*;
 import org.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridsphere.portletcontainer.ApplicationPortlet;
 import org.gridsphere.portletcontainer.PortletPreferencesManager;
 import org.gridsphere.portletcontainer.PortletStatus;
 import org.gridsphere.portletcontainer.impl.ApplicationPortletImpl;
 import org.gridsphere.portletcontainer.impl.PortletWebApplicationImpl;
 import org.gridsphere.portletcontainer.impl.descriptor.*;
 import org.gridsphere.portletcontainer.impl.descriptor.types.TransportGuaranteeType;
 import org.gridsphere.services.core.persistence.PersistenceManagerRdbms;
 import org.gridsphere.services.core.persistence.PersistenceManagerService;
 import org.gridsphere.services.core.portal.PortalConfigService;
 import org.gridsphere.services.core.registry.PortletManagerService;
 import org.gridsphere.services.core.registry.PortletRegistryService;
 import org.gridsphere.services.core.security.auth.AuthModuleService;
 import org.gridsphere.services.core.user.User;
 
 import javax.portlet.*;
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.*;
 import java.util.ResourceBundle;
 
 public class PortletServlet extends HttpServlet
         implements Servlet, ServletConfig,
         HttpSessionAttributeListener, HttpSessionListener, HttpSessionActivationListener {
 
     private transient Log log = LogFactory.getLog(PortletServlet.class);
 
     private transient PortletRegistryService registryService = null;
     private transient PortalConfigService configService = null;
 
     private PortletWebApplicationImpl portletWebApp = null;
 
     private PortletContext portletContext = null;
 
     private Map<String, Portlet> portlets = null;
     private Map<String, String> portletclasses = null;
     private Map<String, ApplicationPortlet> portletApps = null;
     private Map<String, PortletConfig> portletConfigHash = null;
 
     private Map<String, String> userKeys = new HashMap<String, String>();
     private List<String> securePortlets = new ArrayList<String>();
 
     private transient PersistenceManagerService pms = (PersistenceManagerService) PortletServiceFactory.createPortletService(PersistenceManagerService.class, true);
 
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         log.info("in init of PortletServlet");
         portlets = new Hashtable<String, Portlet>();
         portletclasses = new Hashtable<String, String>();
         portletApps = new Hashtable<String, ApplicationPortlet>();
         portletConfigHash = new Hashtable<String, PortletConfig>();
     }
 
     public void initJSRPortletWebapp() {
         registryService = (PortletRegistryService) PortletServiceFactory.createPortletService(PortletRegistryService.class, true);
         configService = (PortalConfigService) PortletServiceFactory.createPortletService(PortalConfigService.class, true);
 
         ServletContext ctx = this.getServletContext();
 
         portletWebApp = new PortletWebApplicationImpl(ctx, Thread.currentThread().getContextClassLoader());
         if (portletWebApp.getWebApplicationStatus().equals(PortletStatus.FAILURE)) return;
 
         Collection<ApplicationPortlet> appPortlets = portletWebApp.getAllApplicationPortlets();
         for (ApplicationPortlet appPortlet : appPortlets) {
             String portletClass = appPortlet.getApplicationPortletClassName();
             String portletName = appPortlet.getApplicationPortletName();
             try {
                 // instantiate portlet classes
                 Portlet portletInstance = (Portlet) Class.forName(portletClass).newInstance();
 
                 portletApps.put(portletName, appPortlet);
 
                 //portlets.put(portletClass, portletInstance);
                 portlets.put(portletName, portletInstance);
 
                 // mappings between names and classes
                 portletclasses.put(portletClass, portletName);
                 log.debug("Creating new portlet instance: " + portletClass);
 
                 // put portlet web app in registry
 
             } catch (Exception e) {
                 String msg = "Unable to create jsr portlet instance: " + portletClass;
                 log.error(msg, e);
                 appPortlet.setApplicationPortletStatus(PortletStatus.FAILURE);
                 appPortlet.setApplicationPortletStatusMessage(msg);
                 portletWebApp.setWebApplicationStatusMessage("FAILURE to instantiate one or more portlet instances");
                 portletWebApp.setWebApplicationStatus(PortletStatus.FAILURE);
             } finally {
                 registryService.addWebApplication(portletWebApp);
             }
         }
 
         UserAttribute[] userAttrs = portletWebApp.getUserAttributes();
         if (userAttrs != null) {
             String key = null;
             for (int i = 0; i < userAttrs.length; i++) {
                 key = userAttrs[i].getName().getContent();
                 userKeys.put(key, "");
             }
         }
 
         SecurityConstraint[] secConstraints = portletWebApp.getSecurityConstraints();
         if (secConstraints != null) {
             for (int i = 0; i < secConstraints.length; i++) {
                 PortletCollection portlets = secConstraints[i].getPortletCollection();
                 PortletName[] names = portlets.getPortletName();
                 UserDataConstraint userConstraint = secConstraints[i].getUserDataConstraint();
                 TransportGuaranteeType guaranteeType = userConstraint.getTransportGuarantee();
                 if (guaranteeType.equals(TransportGuaranteeType.NONE)) {
                     names = null;
                 }
                 if (names != null) {
                     for (int j = 0; j < names.length; j++) {
                         securePortlets.add(names[j].getContent());
                     }
                 }
             }
         }
 
         // create portlet context
         portletContext = new PortletContextImpl(ctx);
 
         // load in any authentication modules if found-- this is a GridSphere extension
 
         AuthModuleService authModuleService = (AuthModuleService) PortletServiceFactory.createPortletService(AuthModuleService.class, true);
         InputStream is = getServletContext().getResourceAsStream("/WEB-INF/authmodules.xml");
         if (is != null) {
             String authModulePath = this.getServletContext().getRealPath("/WEB-INF/authmodules.xml");
             authModuleService.loadAuthModules(authModulePath, Thread.currentThread().getContextClassLoader());
             log.info("loading authentication modules from: " + authModulePath);
         } else {
             log.debug("no auth module descriptor found");
         }
 
     }
 
     public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // if no lifecycle method exists, redirect to error page!
         String method = (String) request.getAttribute(SportletProperties.PORTLET_LIFECYCLE_METHOD);
         if (method == null) {
             response.sendRedirect("/" + configService.getProperty("gridsphere.deploy") +
                     "/" + configService.getProperty("gridsphere.context") +
                     "?" + SportletProperties.LAYOUT_PAGE_PARAM + "=" + "ErrorLayout" + "&" + "errorPage=unauthorized.jsp");
             return;
         }
 
         if (method.equals(SportletProperties.INIT)) {
             initJSRPortletWebapp();
             if (portletWebApp.getWebApplicationStatus().equals(PortletStatus.FAILURE)) return;
             Set set = portlets.keySet();
             Iterator it = set.iterator();
             while (it.hasNext()) {
                 String portletName = (String) it.next();
                 ApplicationPortletImpl appPortlet = (ApplicationPortletImpl) portletApps.get(portletName);
                 Portlet portlet = (Portlet) portlets.get(portletName);
                 log.debug("in PortletServlet: service(): Initializing portlet " + portletName);
                 PortletDefinition portletDef = portletWebApp.getPortletDefinition(portletName);
                 PortletConfig portletConfig = new PortletConfigImpl(getServletConfig(), portletDef, Thread.currentThread().getContextClassLoader());
                 try {
                     portlet.init(portletConfig);
                     portletConfigHash.put(portletName, portletConfig);
                 } catch (Exception e) {
                     appPortlet.setApplicationPortletStatus(PortletStatus.FAILURE);
                     StringWriter sw = new StringWriter();
                     PrintWriter pout = new PrintWriter(sw);
                     e.printStackTrace(pout);
                     appPortlet.setApplicationPortletStatusMessage("Unable to initialize portlet " + portletName + "\n\n" + sw.getBuffer());
                     log.error("in PortletServlet: service(): Unable to INIT portlet " + portletName, e);
                     // PLT.5.5.2.1 Portlet that fails to initialize must not be placed in active service
                     it.remove();
                     portletWebApp.setWebApplicationStatus(PortletStatus.FAILURE);
                     portletWebApp.setWebApplicationStatusMessage("Failed to initialize one or more portlets");
                 }
             }
 
             PortletManagerService manager = (PortletManagerService) PortletServiceFactory.createPortletService(PortletManagerService.class, true);
             manager.addPortletWebApplication(portletWebApp);
             return;
         } else if (method.equals(SportletProperties.DESTROY)) {
             Iterator it = portlets.keySet().iterator();
             while (it.hasNext()) {
                 String portletName = (String) it.next();
                 Portlet portlet = (Portlet) portlets.get(portletName);
                 log.debug("in PortletServlet: service(): Destroying portlet " + portletName);
                 try {
                     portlet.destroy();
                     it.remove();
                 } catch (RuntimeException e) {
                     log.error("Caught exception during portlet destroy", e);
                 }
             }
             return;
         } else if (method.equals(SportletProperties.LOGIN)) {
 
         } else if (method.equals(SportletProperties.LOGOUT)) {
             request.getSession(true).invalidate();
         }
 
         // There must be a portlet ID to know which portlet to service
         String pid = (String) request.getAttribute(SportletProperties.PORTLETID);
         String cid = (String) request.getAttribute(SportletProperties.COMPONENT_ID);
 
         if (pid == null) {
             // it may be in the request parameter
             pid = request.getParameter(SportletProperties.PORTLETID);
             if (pid == null) {
                 log.debug("in PortletServlet: service(): No PortletID found in request!");
                 return;
             }
             request.setAttribute(SportletProperties.PORTLETID, pid);
         }
 
         log.debug("have a portlet id " + pid + " component id= " + cid);
 
         String portletName = "";
         int idx = pid.indexOf("#");
         Portlet portlet = null;
         if (idx > 0) {
             portletName = pid.substring(idx + 1);
             // this hack uses the portletclasses hash that identifies classname to portlet mappings
         } else {
             portletName = (String) portletclasses.get(pid);
         }
         if (portletName == null) {
             log.debug("Check the layout descriptors to make sure the portlet identified as " + pid + " matches with the class and/or portlet name of the portlet.xml");
             return;
         }
         portlet = (Portlet) portlets.get(portletName);
         request.setAttribute(SportletProperties.PORTLET_CONFIG, portletConfigHash.get(portletName));
 
         ApplicationPortlet appPortlet = registryService.getApplicationPortlet(pid);
 
         if (appPortlet == null) {
             log.error("Unable to get portlet from registry identified by: " + pid);
             return;
         }
 
         // perform user conversion from gridsphere to JSR model
         User user = (User) request.getAttribute(SportletProperties.PORTLET_USER);
         Map<String, String> userInfo = new HashMap<String, String>();
         ;
         String userId = null;
         if (user != null) {
             userId = user.getID();
             userInfo.putAll(userKeys);
             if (userInfo.containsKey("user.name")) userInfo.put("user.name", user.getUserName());
             if (userInfo.containsKey("user.id")) userInfo.put("user.id", user.getID());
             if (userInfo.containsKey("user.email")) userInfo.put("user.email", user.getEmailAddress());
             if (userInfo.containsKey("user.organization")) userInfo.put("user.organization", user.getOrganization());
             if (userInfo.containsKey("user.lastlogintime"))
                 userInfo.put("user.lastlogintime", String.valueOf(user.getLastLoginTime()));
             if (userInfo.containsKey("user.name.full")) userInfo.put("user.name.full", user.getFullName());
             if (userInfo.containsKey("user.name.first")) userInfo.put("user.name.first", user.getFirstName());
             if (userInfo.containsKey("user.name.last")) userInfo.put("user.name.last", user.getLastName());
             if (userInfo.containsKey("user.timezone"))
                 userInfo.put("user.timezone", (String) user.getAttribute(User.TIMEZONE));
             if (userInfo.containsKey("user.locale"))
                 userInfo.put("user.locale", (String) user.getAttribute(User.LOCALE));
             if (userInfo.containsKey("user.theme")) userInfo.put("user.theme", (String) user.getAttribute(User.THEME));
 
             if (userInfo.containsKey("user.login.id")) userInfo.put("user.login.id", user.getUserName());
 
             Enumeration e = user.getAttributeNames();
             while (e.hasMoreElements()) {
                 String key = (String) e.nextElement();
                 if (userInfo.containsKey(key)) userInfo.put(key, (String) user.getAttribute(key));
             }
 

             request.setAttribute(PortletRequest.USER_INFO, userInfo);
         }
 
         // portlet preferences
         PortalContext portalContext = appPortlet.getPortalContext();
         request.setAttribute(SportletProperties.PORTAL_CONTEXT, portalContext);
         if (portlet == null) {
             log.error("in PortletServlet: service(): No portlet matching " + pid + " found!");
             return;
         }
 
         request.removeAttribute(SportletProperties.SSL_REQUIRED);
         if (securePortlets.contains(pid)) {
             request.setAttribute(SportletProperties.SSL_REQUIRED, "true");
         }
 
         if (method.equals(SportletProperties.SERVICE)) {
 
             String action = (String) request.getAttribute(SportletProperties.PORTLET_ACTION_METHOD);
             if (action != null) {
                 log.debug("in PortletServlet: action is not NULL");
                 if (action.equals(SportletProperties.DO_TITLE)) {
                     RenderRequest renderRequest = new RenderRequestImpl(request, portletContext);
                     RenderResponse renderResponse = new RenderResponseImpl(request, response);
                     renderRequest.setAttribute(SportletProperties.RENDER_REQUEST, renderRequest);
                     renderRequest.setAttribute(SportletProperties.RENDER_RESPONSE, renderResponse);
                     log.debug("in PortletServlet: do title " + pid);
                     try {
                         doTitle(portlet, renderRequest, renderResponse);
                     } catch (Exception e) {
                         log.error("Error during doTitle:", e);
                         request.getSession(true).setAttribute(SportletProperties.PORTLETERROR + pid, new PortletException(e));
                     }
                 } else if (action.equals(SportletProperties.WINDOW_EVENT)) {
                     // do nothing
                 } else if (action.equals(SportletProperties.ACTION_PERFORMED)) {
                     // create portlet preferences manager
                     log.debug("in PortletServlet: do processAction " + pid);
                     PortletPreferencesManager prefsManager = appPortlet.getPortletPreferencesManager(pid, userId, false);
                     request.setAttribute(SportletProperties.PORTLET_PREFERENCES_MANAGER, prefsManager);
 
                     processAction(portlet, portalContext, request, response, cid, pid);
                 }
             } else {
                 // create portlet preferences manager
                 PortletPreferencesManager prefsManager = appPortlet.getPortletPreferencesManager(pid, userId, true);
                 request.setAttribute(SportletProperties.PORTLET_PREFERENCES_MANAGER, prefsManager);
 
                 render(portlet, request, response, pid);
 
             }
         } else {
             log.error("in PortletServlet: service(): No " + SportletProperties.PORTLET_LIFECYCLE_METHOD + " found in request!");
         }
     }
 
     protected void processAction(Portlet portlet, PortalContext portalContext, HttpServletRequest request, HttpServletResponse response, String cid, String pid) throws ServletException {
 
         ActionRequestImpl actionRequest = new ActionRequestImpl(request, portletContext);
         ActionResponse actionResponse = new ActionResponseImpl(request, response);
 
         String webappname = portletWebApp.getWebApplicationName();
         PersistenceManagerRdbms pm = pms.getPersistenceManagerRdbms(webappname);
 
         try {
 
             if (pm != null) {
                 log.debug("Starting a database transaction for webapp: " + webappname);
                 pm.beginTransaction();
             }
 
             log.debug("in PortletServlet: action handling portlet " + pid);
 
             // INVOKE PORTLET ACTION
             portlet.processAction(actionRequest, actionResponse);
             Map params = ((ActionResponseImpl) actionResponse).getRenderParameters();
             request.setAttribute(SportletProperties.RENDER_PARAM_PREFIX + pid + "_" + cid, params);
             log.debug("placing render params in session : key= " + SportletProperties.RENDER_PARAM_PREFIX + pid + "_" + cid);
 
             // Commit and cleanup
             log.info("Committing the database transaction");
 
             if (pm != null) pm.endTransaction();
 
         } catch (Throwable ex) {
             //log.error("Error during processAction:", ex);
             request.setAttribute(SportletProperties.PORTLETERROR + pid, ex);
 
             if (pm != null) {
                 pm.endTransaction();
                 try {
                     if (pm != null) pm.rollbackTransaction();
                 } catch (Throwable rbEx) {
                     log.error("Could not rollback transaction after exception!", rbEx.getCause());
                 }
             }
             // Let others handle it... maybe another interceptor for exceptions?
             //throw new ServletException(ex.getCause());
         } finally {
             try {
                 redirect(request, response, actionRequest, actionResponse, portalContext);
             } catch (IOException e) {
                 log.error("Unable to handle redirect", e);
             }
         }
 
     }
 
 
     protected void render(Portlet portlet, HttpServletRequest request, HttpServletResponse response, String pid) throws ServletException {
         RenderRequest renderRequest = new RenderRequestImpl(request, portletContext);
         RenderResponse renderResponse = new RenderResponseImpl(request, response);
 
         renderRequest.setAttribute(SportletProperties.RENDER_REQUEST, renderRequest);
         renderRequest.setAttribute(SportletProperties.RENDER_RESPONSE, renderResponse);
 
         log.debug("in PortletServlet: rendering  portlet " + pid);
         if (renderRequest.getAttribute(SportletProperties.RESPONSE_COMMITTED) == null) {
             String webappname = portletWebApp.getWebApplicationName();
             PersistenceManagerRdbms pm = pms.getPersistenceManagerRdbms(webappname);
             try {
                 if (pm != null) pm.beginTransaction();
                 portlet.render(renderRequest, renderResponse);
                 if (pm != null) pm.endTransaction();
 
             } catch (UnavailableException e) {
                 try {
                     portlet.destroy();
                 } catch (Exception d) {
                     log.error("in PortletServlet(): destroy caught exception: ", d);
                 }
             } catch (Throwable ex) {
                 //log.error("in render: caught exception: ", ex);
 
                 try {
                     if (pm != null) {
                         log.info("Committing database transaction for webapp: " + portletWebApp.getWebApplicationName());
                         pm.endTransaction();
                         pm.rollbackTransaction();
                     }
                 } catch (Throwable rbEx) {
                     throw new ServletException("Could not rollback transaction after exception!", rbEx);
                 }
 
                 throw new ServletException(ex);
             }
         }
     }
 
     protected void doTitle(Portlet portlet, RenderRequest request, RenderResponse response) throws IOException, PortletException {
         Portlet por = (Portlet) portlet;
         if (por instanceof GenericPortlet) {
             GenericPortlet genPortlet = ((GenericPortlet) portlet);
             if (genPortlet.getPortletConfig() == null)
                 throw new PortletException("Unable to get PortletConfig from Portlet");
             ResourceBundle resBundle = genPortlet.getPortletConfig().getResourceBundle(request.getLocale());
             String title = resBundle.getString("javax.portlet.title");
             response.setContentType("text/html");
             PrintWriter out = response.getWriter();
             out.println(title);
         }
     }
 
     protected void doGet(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         super.doGet(req, res);
     }
 
     protected void doPut(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         super.doPut(req, res);
     }
 
     protected void doPost(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         super.doPost(req, res);
     }
 
     protected void doTrace(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         super.doTrace(req, res);
     }
 
     protected void doDelete(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         super.doDelete(req, res);
     }
 
     protected void redirect(HttpServletRequest servletRequest,
                             HttpServletResponse servletResponse,
                             ActionRequest actionRequest,
                             ActionResponse actionResponse, PortalContext portalContext)
             throws IOException {
         if (actionResponse instanceof ActionResponseImpl) {
             ActionResponseImpl aResponse = (ActionResponseImpl) actionResponse;
             String location = aResponse.getRedirectLocation();
 
             if (location != null) {
                 javax.servlet.http.HttpServletResponse redirectResponse = servletResponse;
                 while (redirectResponse instanceof javax.servlet.http.HttpServletResponseWrapper) {
                     redirectResponse = (javax.servlet.http.HttpServletResponse)
                             ((javax.servlet.http.HttpServletResponseWrapper) redirectResponse).getResponse();
                 }
 
                 log.debug("redirecting to location= " + location);
 
                 servletRequest.setAttribute(SportletProperties.PORTAL_REDIRECT_PATH, location);
                 //redirectResponse.sendRedirect(location);
 
             } else {
 
                 // redirect as a GET render url back to the portal
                 PortletURL url = new PortletURLImpl(servletRequest, servletResponse, true);
                 Map params = aResponse.getRenderParameters();
                 url.setParameters(params);
                 servletRequest.setAttribute(SportletProperties.PORTAL_REDIRECT_PATH, url.toString());
             }
 
         }
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
      * Record the fact that a session has been created.
      *
      * @param event The session event
      */
     public void sessionCreated(HttpSessionEvent event) {
         log.debug("in PS sessionCreated('" + event.getSession().getId() + "')");
         //sessionManager.sessionCreated(event);
     }
 
 
     /**
      * Record the fact that a session has been destroyed.
      *
      * @param event The session event
      */
     public void sessionDestroyed(HttpSessionEvent event) {
         //sessionManager.sessionDestroyed(event);
         //loginService.sessionDestroyed(event.getSession());
         log.debug("in PS sessionDestroyed('" + event.getSession().getId() + "')");
 
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
         //sessionManager.sessionCreated(event);
     }
 
 
     /**
      * Record the fact that a session has been destroyed.
      *
      * @param event The session event
      */
     public void sessionWillPassivate(HttpSessionEvent event) {
         //sessionManager.sessionDestroyed(event);
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
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
