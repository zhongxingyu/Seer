 /*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
 package org.gridlab.gridsphere.provider.portlet.jsr;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.jsrimpl.*;
 
 import org.gridlab.gridsphere.portlet.GuestUser;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
 
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 
 import org.gridlab.gridsphere.portlet.impl.ClientImpl;
 
 import org.gridlab.gridsphere.portlet.jsrimpl.*;
 
 import org.gridlab.gridsphere.portletcontainer.PortletRegistry;
 import org.gridlab.gridsphere.portletcontainer.ApplicationPortletConfig;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.JSRApplicationPortletImpl;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.JSRPortletWebApplicationImpl;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.PortletDefinition;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.Supports;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.UserAttribute;
 import org.gridlab.gridsphere.services.core.registry.impl.PortletManager;
 import org.gridlab.gridsphere.services.core.user.LoginService;
 
 import javax.portlet.*;
 import javax.portlet.PortletMode;
 import javax.portlet.PortletPreferences;
 import javax.portlet.Portlet;
 import javax.portlet.PortletConfig;
 import javax.portlet.PortletContext;
 import javax.portlet.PortletException;
 import javax.portlet.PortletRequest;
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSessionActivationListener;
 import javax.servlet.http.HttpSessionAttributeListener;
 import javax.servlet.http.HttpSessionBindingEvent;
 import javax.servlet.http.HttpSessionEvent;
 import javax.servlet.http.HttpSessionListener;
 import javax.portlet.UnavailableException;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.InputStream;
 import java.util.*;
 
 public class PortletServlet extends HttpServlet
         implements Servlet, ServletConfig, ServletContextListener,
         HttpSessionAttributeListener, HttpSessionListener, HttpSessionActivationListener {
 
     protected transient static PortletLog log = SportletLog.getInstance(PortletServlet.class);
     protected transient static PortletRegistry registry = null;
 
     protected JSRPortletWebApplicationImpl portletWebApp = null;
     protected String webAppName = null;
 
     private PortletManager manager = PortletManager.getInstance();
 
     protected PortletContext portletContext = null;
 
     protected Map portlets = null;
     protected Map portletclasses = null;
 
     protected Map portletConfigHash = null;
 
     private PortletPreferencesManager prefsManager = null;
 
     private Map userKeys = new HashMap();
 
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
        
         // load descriptor files
         log.debug("in init of PortletServlet");
 
         ServletContext ctx = config.getServletContext();
 
         portlets = new Hashtable();
         portletclasses = new Hashtable();
         portletConfigHash = new Hashtable();
         portletWebApp = new JSRPortletWebApplicationImpl(ctx, Thread.currentThread().getContextClassLoader());
 
         Collection appPortlets = portletWebApp.getAllApplicationPortlets();
         Iterator it = appPortlets.iterator();
         while (it.hasNext()) {
             JSRApplicationPortletImpl appPortlet = (JSRApplicationPortletImpl) it.next();
             String portletClass = appPortlet.getApplicationPortletClassName();
             String portletName = appPortlet.getApplicationPortletName();
             try {
                 // instantiate portlet classes
                 Portlet portletInstance = (Portlet) Class.forName(portletClass).newInstance();
                 //portlets.put(portletClass, portletInstance);
                 portlets.put(portletName, portletInstance);
 
                 // mappings between names and classes
                 portletclasses.put(portletClass, portletName);
                 log.debug("Creating new portlet instance: " + portletClass);
 
                 // put portlet web app in registry
 
             } catch (Exception e) {
                 log.error("Unable to create jsr portlet instance: " + portletClass, e);
                 throw new ServletException("Unable to create jsr portlet instance: " + portletClass, e);
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
 
         // create portlet context
         portletContext = new PortletContextImpl(ctx);
 
         prefsManager = PortletPreferencesManager.getInstance();
 
         // load in any authentication modules if found-- this is a GridSphere extension
         PortletServiceFactory factory = SportletServiceFactory.getInstance();
         LoginService loginService = (LoginService)factory.createPortletService(LoginService.class, ctx, true);
         InputStream is = config.getServletContext().getResourceAsStream("/WEB-INF/authmodules.xml");
         if (is != null) {
             String authModulePath = config.getServletContext().getRealPath("/WEB-INF/authmodules.xml");
             loginService.loadAuthModules(authModulePath, Thread.currentThread().getContextClassLoader());
             log.info("loading authentication modules from: " + authModulePath);
         } else {
             log.debug("no auth module descriptor found");
         }
     }
 
     public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         registry = PortletRegistry.getInstance();
         // If no portlet ID exists, this may be a command to init or shutdown a portlet instance
 
         // currently either all portlets are initailized or shutdown, not one individually...
         String method = (String) request.getAttribute(SportletProperties.PORTLET_LIFECYCLE_METHOD);
 
         if (method.equals(SportletProperties.INIT)) {
             Set set = portlets.keySet();
             Iterator it = set.iterator();
             while (it.hasNext()) {
                 String portletName = (String) it.next();
                 Portlet portlet = (Portlet) portlets.get(portletName);
                 log.debug("in PortletServlet: service(): Initializing portlet " + portletName);
                 PortletDefinition portletDef = portletWebApp.getPortletDefinition(portletName);
 
                 PortletConfig portletConfig = new PortletConfigImpl(getServletConfig(), portletDef, Thread.currentThread().getContextClassLoader());
                 try {
                     portlet.init(portletConfig);
                     portletConfigHash.put(portletName, portletConfig);
                 } catch (Exception e) {
                     log.error("in PortletServlet: service(): Unable to INIT portlet " + portletName, e);
                     // PLT.5.5.2.1 Portlet that fails to initialize must not be placed in active service
                     it.remove();
                 }
             }
 
             manager.addWebApp(portletWebApp);
             return;
         } else if (method.equals(SportletProperties.INIT_CONCRETE)) {
             // do nothing for concrete portlets
             return;
         } else if (method.equals(SportletProperties.DESTROY)) {
             Iterator it = portlets.keySet().iterator();
             while (it.hasNext()) {
                 String portletName = (String) it.next();
                 Portlet portlet = (Portlet) portlets.get(portletName);
                 log.debug("in PortletServlet: service(): Destroying portlet " + portletName);
                 try {
                     portlet.destroy();
                 } catch (RuntimeException e) {
                     log.error("Caught exception during portlet destroy", e);
                 }
             }
             manager.removePortletWebApplication(portletWebApp);
             return;
         } else if (method.equals(SportletProperties.DESTROY_CONCRETE)) {
             // do nothing for concrete portlets
             return;
         }
 
         // There must be a portlet ID to know which portlet to service
         String pid = (String) request.getAttribute(SportletProperties.PORTLETID);
         String compId = (String) request.getAttribute(SportletProperties.COMPONENT_ID);
 
         if (pid == null) {
             // it may be in the request parameter
             pid = request.getParameter(SportletProperties.PORTLETID);
             if (pid == null) {
                 log.debug("in PortletServlet: service(): No PortletID found in request!");
                 return;
             }
             request.setAttribute(SportletProperties.PORTLETID, pid);
         }
 
         log.debug("have a portlet id " + pid + " component id= " + compId);
 
         int idx = pid.indexOf("#");
         Portlet portlet = null;
         if (idx > 0) {
             String pname = pid.substring(idx+1);
             portlet = (Portlet) portlets.get(pname);
             request.setAttribute(SportletProperties.PORTLET_CONFIG, portletConfigHash.get(pname));
 
             // this hack uses the portletclasses hash that identifies classname to portlet mappings
         } else {
             String pname = (String)portletclasses.get(pid);
             portlet = (Portlet)portlets.get(pname);
             request.setAttribute(SportletProperties.PORTLET_CONFIG, portletConfigHash.get(pname));
         }
 
         JSRApplicationPortletImpl appPortlet =
                 (JSRApplicationPortletImpl) registry.getApplicationPortlet(pid);
 
         if (appPortlet == null) {
             log.error("Unable to get portlet from registry identified by: " + pid);
             return;
         }
 
        // set the supported mime types
         Supports[] supports = appPortlet.getSupports();
        List mimeTypes = new ArrayList();
        for (int i = 0; i < supports.length; i++) {
            Supports s = supports[i];
            String mimeType = s.getMimeType().getContent();
            mimeTypes.add(mimeType);
            request.setAttribute(SportletProperties.MIME_TYPES, mimeTypes);
        }
 
        setPortletModes(request, appPortlet);
 
         // perform user conversion from gridsphere to JSR model
         User user = (User) request.getAttribute(SportletProperties.PORTLET_USER);
         Map userInfo;
 
         if (user instanceof GuestUser) {
             userInfo = null;
         } else {
             userInfo = new HashMap();
             userInfo.putAll(userKeys);
             if (userInfo.containsKey("user.name")) userInfo.put("user.name", user.getUserName());
             if (userInfo.containsKey("user.id")) userInfo.put("user.id", user.getID());
             if (userInfo.containsKey("user.email")) userInfo.put("user.email", user.getEmailAddress());
             if (userInfo.containsKey("user.organization")) userInfo.put("user.organization", user.getOrganization());
             if (userInfo.containsKey("user.lastlogintime")) userInfo.put("user.lastlogintime", new Long(user.getLastLoginTime()).toString());
             if (userInfo.containsKey("user.name.full")) userInfo.put("user.name.full", user.getFullName());
             if (userInfo.containsKey("user.timezone")) userInfo.put("user.timezone", user.getAttribute(User.TIMEZONE));
             if (userInfo.containsKey("user.locale")) userInfo.put("user.locale", user.getAttribute(User.LOCALE));
             if (userInfo.containsKey("user.theme")) userInfo.put("user.theme", user.getAttribute(User.THEME));
             if (userInfo.containsKey("user.role")) userInfo.put("user.role", ((PortletRole)request.getAttribute(SportletProperties.PORTLET_ROLE)).getName());
             //userInfo.put("user.name.given", user.getGivenName());
             //userInfo.put("user.name.family", user.getFamilyName());
             request.setAttribute(PortletRequest.USER_INFO, userInfo);
         }
 
         /*
         UserAttribute[] userAttrs = portletWebApp.getUserAttributes();
         for (int i = 0; i < userAttrs.length; i++) {
             UserAttribute userAttr = userAttrs[i];
             String name = userAttr.getName().getContent();
             userInfo.put(name, "");
         }
         request.setAttribute(PortletRequest.USER_INFO, userInfo);
         */
 
         // portlet preferences
 
         PortalContext portalContext = appPortlet.getPortalContext();
         request.setAttribute(SportletProperties.PORTAL_CONTEXT, portalContext);
 
 
         if (portlet == null) {
             log.error("in PortletServlet: service(): No portlet matching " + pid + " found!");
             return;
         }
 
         if (method.equals(SportletProperties.SERVICE)) {
             String action = (String) request.getAttribute(SportletProperties.PORTLET_ACTION_METHOD);
             if (action != null) {
                 log.debug("in PortletServlet: action is not NULL");
                 if (action.equals(SportletProperties.DO_TITLE)) {
                     RenderRequest renderRequest = new RenderRequestImpl(request, portalContext, portletContext, supports);
                     RenderResponse renderResponse = new RenderResponseImpl(request, response, portalContext);
                     renderRequest.setAttribute(SportletProperties.RENDER_REQUEST, renderRequest);
                     renderRequest.setAttribute(SportletProperties.RENDER_RESPONSE, renderResponse);
                     log.debug("in PortletServlet: do title " + pid);
                     try {
                         doTitle(portlet, renderRequest, renderResponse);
                     } catch (PortletException e) {
                         log.error("Error during doTitle:", e);
                     }
                 } else if (action.equals(SportletProperties.WINDOW_EVENT)) {
                     // do nothing
                 } else if (action.equals(SportletProperties.MESSAGE_RECEIVED)) {
                     // do nothing
                 } else if (action.equals(SportletProperties.ACTION_PERFORMED)) {
                     PortletPreferences prefs = prefsManager.getPortletPreferences(appPortlet, user, Thread.currentThread().getContextClassLoader(), false);
                     request.setAttribute(SportletProperties.PORTLET_PREFERENCES, prefs);
                     ActionRequestImpl actionRequest = new ActionRequestImpl(request, portalContext, portletContext, supports);
                     ActionResponse actionResponse = new ActionResponseImpl(request, response, portalContext);
                     //setGroupAndRole(actionRequest, actionResponse);
                     log.debug("in PortletServlet: action handling portlet " + pid);
                     try {
                         portlet.processAction(actionRequest, actionResponse);
 
                         Map params = ((ActionResponseImpl) actionResponse).getRenderParameters();
 
                         String cid = (String) request.getAttribute(SportletProperties.COMPONENT_ID);
                         actionRequest.setAttribute(SportletProperties.RENDER_PARAM_PREFIX + pid + "_" + cid, params);
                         log.debug("placing render params in attribute: key= " + SportletProperties.RENDER_PARAM_PREFIX + pid + "_" + cid);
                         redirect(request, response, actionRequest, actionResponse, portalContext);
                     } catch (Exception e) {
                         log.error("Error during processAction:", e);
                         request.setAttribute(SportletProperties.PORTLETERROR + pid, new org.gridlab.gridsphere.portlet.PortletException(e));
                     }
 
                     //actionRequest.clearParameters();
 
                 }
             } else {
                 PortletPreferences prefs = prefsManager.getPortletPreferences(appPortlet, user, Thread.currentThread().getContextClassLoader(), true);
                 request.setAttribute(SportletProperties.PORTLET_PREFERENCES, prefs);
 
 
                 RenderRequest renderRequest = new RenderRequestImpl(request, portalContext, portletContext, supports);
                 RenderResponse renderResponse = new RenderResponseImpl(request, response, portalContext);
 
                 renderRequest.setAttribute(SportletProperties.RENDER_REQUEST, renderRequest);
                 renderRequest.setAttribute(SportletProperties.RENDER_RESPONSE, renderResponse);
 
                 //setGroupAndRole(renderRequest, renderResponse);
                 log.debug("in PortletServlet: rendering  portlet " + pid);
                 if (renderRequest.getAttribute(SportletProperties.RESPONSE_COMMITTED) == null) {
                     try {
 
                         portlet.render(renderRequest, renderResponse);
                     } catch (UnavailableException e) {
                         log.error("in PortletServlet(): doRender() caught unavailable exception: ");
                         try {
                             portlet.destroy();
                         } catch (Exception d) {
                             log.error("in PortletServlet(): destroy caught unavailable exception: ", d);
                         }
                     } catch (Exception e) {
                         if (request.getAttribute(SportletProperties.PORTLETERROR + pid) == null) {
                             request.setAttribute(SportletProperties.PORTLETERROR + pid, e);
                         }
                         log.error("in PortletServlet(): doRender() caught exception", e);
                         throw new ServletException(e);
                     } finally {
                         //request.removeAttribute(SportletProperties.PORTLET_MODE_JSR);
                     }                   
                 }
             }
             request.removeAttribute(SportletProperties.PORTLET_ACTION_METHOD);
         } else {
             log.error("in PortletServlet: service(): No " + SportletProperties.PORTLET_LIFECYCLE_METHOD + " found in request!");
         }
         request.removeAttribute(SportletProperties.PORTLET_LIFECYCLE_METHOD);
     }
 
 /*
 protected void setGroupAndRole(PortletRequest request, PortletResponse response) {
 String ctxPath = this.getServletContext().getRealPath("");
 int i = ctxPath.lastIndexOf(File.separator);
 String groupName = ctxPath.substring(i+1);
 
 PortletGroup group = aclService.getGroupByName(groupName);
 if (group == null)
 group = PortletGroupFactory.createPortletGroup(groupName);
 
 PortletRole role = aclService.getRoleInGroup(request.getUser(), group);
 
 log.debug("Setting Group: " + group.toString() + " Role: " + role.toString());
 
 request.setAttribute(SportletProperties.PORTLET_GROUP, group);
 request.setAttribute(SportletProperties.PORTLET_ROLE, role);
 }
 */
     protected void setPortletModes(HttpServletRequest request, JSRApplicationPortletImpl appPortlet) {
         ApplicationPortletConfig appPortletConfig = appPortlet.getApplicationPortletConfig();
 
         Client client = (Client)request.getSession().getAttribute(SportletProperties.CLIENT);
         if (client == null) {
             client = new ClientImpl(request);
             request.getSession().setAttribute(SportletProperties.CLIENT, client);
         }
 
         List appModes = appPortletConfig.getSupportedModes(client.getMimeType());
         // convert modes from GridSphere type to JSR
         Iterator it = appModes.iterator();
         List myModes = new ArrayList();
         PortletMode m = PortletMode.VIEW;
         while (it.hasNext()) {
             org.gridlab.gridsphere.portlet.Portlet.Mode mode = (org.gridlab.gridsphere.portlet.Portlet.Mode)it.next();
             if (mode == org.gridlab.gridsphere.portlet.Portlet.Mode.VIEW) {
                 m = PortletMode.VIEW;
             } else if (mode == org.gridlab.gridsphere.portlet.Portlet.Mode.EDIT) {
                 m = PortletMode.EDIT;
             } else if (mode == org.gridlab.gridsphere.portlet.Portlet.Mode.HELP) {
                 m = PortletMode.HELP;
             } else if (mode == org.gridlab.gridsphere.portlet.Portlet.Mode.CONFIGURE) {
                 m = new PortletMode("config");
             } else {
                 m = new PortletMode(mode.toString());
             }
             myModes.add(m.toString());
         }
 
         request.setAttribute(SportletProperties.ALLOWED_MODES, myModes);
 
     }
 
     protected void doTitle(Portlet portlet, RenderRequest request, RenderResponse response) throws PortletException {
         try {
             Portlet por = (Portlet)portlet;
             if (por instanceof GenericPortlet) {
                 GenericPortlet genPortlet = ((GenericPortlet) portlet);
                 if (genPortlet.getPortletConfig() == null) throw new PortletException("Unable to get PortletConfig from Portlet");
                 ResourceBundle resBundle = genPortlet.getPortletConfig().getResourceBundle(request.getLocale());
                 String title = resBundle.getString("javax.portlet.title");
                 response.setContentType("text/html");
                 PrintWriter out = response.getWriter();
                 out.println(title);
             }
         } catch (IOException e) {
             log.error("printing title failed", e);
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
 
     public void destroy() {
         portletWebApp.destroy();
         super.destroy();
         //portletManager.destroyPortletWebApplication(portletWebApp);
     }
 
 
     protected void redirect(HttpServletRequest servletRequest,
                             HttpServletResponse servletResponse,
                             ActionRequest actionRequest,
                             ActionResponse actionResponse, PortalContext portalContext)
             throws IOException {
         String location = null;
         if (actionResponse instanceof ActionResponseImpl) {
             ActionResponseImpl aResponse = (ActionResponseImpl) actionResponse;
             location = aResponse.getRedirectLocation();
 
             if (location != null) {
                 javax.servlet.http.HttpServletResponse redirectResponse = servletResponse;
                 while (redirectResponse instanceof javax.servlet.http.HttpServletResponseWrapper) {
                     redirectResponse = (javax.servlet.http.HttpServletResponse)
                             ((javax.servlet.http.HttpServletResponseWrapper) redirectResponse).getResponse();
                 }
 
                 log.debug("redirecting to location= " + location);
                 redirectResponse.sendRedirect(location);
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
         log.debug("sessionDestroyed('" + event.getSession().getId() + "')");
 
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
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
