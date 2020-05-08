 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id: PortletManager.java 4733 2006-04-10 18:29:36Z novotny $
  */
 package org.gridsphere.services.core.registry.impl;
 
 import org.gridsphere.portlet.PortletException;
 import org.gridsphere.portlet.PortletLog;
 import org.gridsphere.portlet.impl.SportletLog;
 import org.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridsphere.portlet.service.PortletServiceException;
 import org.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridsphere.portletcontainer.*;
 import org.gridsphere.portletcontainer.impl.PortletWebApplicationImpl;
 import org.gridsphere.portletcontainer.impl.PortletInvoker;
 import org.gridsphere.services.core.registry.PortletManagerService;
 import org.gridsphere.services.core.registry.PortletRegistryService;
 import org.gridsphere.services.core.registry.impl.tomcat.TomcatManagerWrapper;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * The <code>PortletManager</code> is a singleton responsible for maintaining the registry of portlet
  * web applications known to the portlet container.
  */
 public class PortletManagerServiceImpl implements PortletManagerService, PortletServiceProvider {
 
     private PortletLog log = SportletLog.getInstance(PortletManagerServiceImpl.class);
 
     private ServletContext context = null;
 
     private PortletRegistryService registryService = null;
 
     private PortletInvoker portletInvoker = null;
 
     // A multi-valued hashtable with a webapp key and a List value containing portletAppID's
     private List webapps = new Vector();
 
     // An ordered list of known portlet webapps
     private String[] webappFiles = null;
 
     private static String PORTLETS_PATH = "/WEB-INF/CustomPortal/portlets";
 
     private static class WebappComparator implements Comparator {
 
         public int compare(Object webapp1, Object webapp2) {
 
             if (!(webapp1 instanceof String) || !(webapp2 instanceof String)) {
                 throw new ClassCastException("Can only compare string webapp names!!");
             }
             String _webapp1 = (String) webapp1;
             String _webapp2 = (String) webapp2;
 
             int a = _webapp1.lastIndexOf(".");
 
             int b = _webapp2.lastIndexOf(".");
 
             // check if a and b do not have a priority then use alphabetical
             if ((a > 0) && (b > 0)) {
                 try {
                     int a1 = Integer.valueOf(_webapp1.substring(a + 1)).intValue();
                     int a2 = Integer.valueOf(_webapp2.substring(b + 1)).intValue();
                     if (a1 > a2) return 1;
                     if (a1 < a2) return -1;
                     if (a1 == a2) return 0;
                 } catch (NumberFormatException e) {
                     // oh well
                 }
             }
 
             // check if a has a priority and b does not
             if ((a > 0) && (b < 0)) {
                 return -1;
             }
             // check if b has a priority and a does not
             if ((a < 0) && (b > 0)) {
                 return 1;
             }
 
             // use alphabetical comparison
             return _webapp1.compareTo(_webapp2);
 
         }
 
     }
 
     /**
      * Initializes portlet web applications that are defined by the <code>PortletManagerService</code>
      *
      * @param config the <code>PortletServiceConfig</code>
      */
     public  void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
         log.debug("in init of PM");
 
         try {
             registryService = (PortletRegistryService)PortletServiceFactory.createPortletService(PortletRegistryService.class, true);
         } catch (PortletServiceException e) {
             throw new PortletServiceUnavailableException("Unable to create instance of PortletRegistryService");
         }
 
         portletInvoker = new PortletInvoker();
 
         context = config.getServletContext();
 
         String portletsPath = context.getRealPath(PORTLETS_PATH);
         File f = new File(portletsPath);
         if (f.exists() && f.isDirectory()) {
             webappFiles = f.list();
 
             // sort webapps by priority
             Arrays.sort(webappFiles, new WebappComparator());
 
             // get rid of any priority numbers
             String webapp = "";
             int idx;
             for (int i = 0; i < webappFiles.length; i++) {
                 webapp = webappFiles[i];
                 if ((idx = webapp.lastIndexOf(".")) > 0) {
                     webappFiles[i] = webapp.substring(0, idx);
                 }
             }
         } else {
             log.error("Portlet application " + portletsPath + " does not exist!");
             throw new PortletServiceUnavailableException("Portlet application " + portletsPath + " does not exist!");
         }
         String name = config.getInitParameter("username");
         String pass = config.getInitParameter("password");
         TomcatManagerWrapper.setCredentials(name, pass);
     }
 
     public void destroy() {}
 
     /**
      * Adds a portlet web application to the registry
      *
      * @param portletWebApp the portlet web application name
      */
     public synchronized void addPortletWebApplication(PortletWebApplication portletWebApp) {
         log.debug("adding webapp: " + portletWebApp.getWebApplicationName());
         addPortletFile(portletWebApp.getWebApplicationName());
         Collection appPortlets = portletWebApp.getAllApplicationPortlets();
         Iterator it = appPortlets.iterator();
         while (it.hasNext()) {
             ApplicationPortlet appPortlet = (ApplicationPortlet) it.next();
             log.debug("Adding application portlet: " + appPortlet.getApplicationPortletID());
             registryService.addApplicationPortlet(appPortlet);
         }
         webapps.add(portletWebApp);
     }
 
     private void addPortletFile(String webappName) {
         String portletsPath = context.getRealPath(PORTLETS_PATH);
 
         File f = new File(portletsPath);
         if (f.exists() && f.isDirectory()) {
             String[] webappFiles = f.list();
             for (int i = 0; i < webappFiles.length; i++) {
                 if (webappFiles[i].startsWith(webappName) || webappName.startsWith(webappFiles[i])) return;
             }
         }
         File newfile = new File(portletsPath + File.separator + webappName);
         try {
             newfile.createNewFile();
         } catch (IOException e) {
             log.error("Unable to create portlet app file: " + newfile.getAbsolutePath());
         }
     }
 
     private void removePortletFile(String webappName) {
         String portletsPath = context.getRealPath(PORTLETS_PATH);
         File f = new File(portletsPath);
         if (f.exists() && f.isDirectory()) {
             String[] webappFiles = f.list();
             for (int i = 0; i < webappFiles.length; i++) {
                 if (webappFiles[i].startsWith(webappName) || webappName.startsWith(webappFiles[i])) {
                     File newfile = new File(portletsPath + File.separator + webappFiles[i]);
                     newfile.delete();
                 }
             }
         }
     }
 
     /**
      * Removes the portlet application
      *
      * @param webApplicationName the portlet application name
      */
     protected synchronized void removePortletWebApplication(String webApplicationName) {
         log.debug("in removePortletWebApplication: " + webApplicationName);
         Iterator it = webapps.iterator();
 
         while (it.hasNext()) {
             PortletWebApplication webApp = (PortletWebApplication) it.next();

             if (webApp.getWebApplicationName().equalsIgnoreCase(webApplicationName)) {
                 webApp.destroy();
                 Collection appPortlets = webApp.getAllApplicationPortlets();
                 Iterator appsit = appPortlets.iterator();
                 while (appsit.hasNext()) {
                     ApplicationPortlet appPortlet = (ApplicationPortlet) appsit.next();
                     registryService.removeApplicationPortlet(appPortlet);
                 }
                 log.debug("removing " + webApp.getWebApplicationName());
                 it.remove();
             }
         }
         //removePortletFile(webApplicationName);
     }
 
     /**
      * Removes the portlet application
      *
      * @param webApplication the portlet application name
      */
     public synchronized void removePortletWebApplication(PortletWebApplication webApplication) {
         log.debug("in removePortletWebApplication: " + webApplication);
         Iterator it = webapps.iterator();
 
         while (it.hasNext()) {
             PortletWebApplication webApp = (PortletWebApplication) it.next();
 
             if (webApp.getWebApplicationName().equalsIgnoreCase(webApplication.getWebApplicationName())) {
 
                 Collection appPortlets = webApp.getAllApplicationPortlets();
                 Iterator appsit = appPortlets.iterator();
                 while (appsit.hasNext()) {
                     ApplicationPortlet appPortlet = (ApplicationPortlet) appsit.next();
                     registryService.removeApplicationPortlet(appPortlet);
                 }
                 log.debug("removing " + webApp.getWebApplicationName());
 
                 webApp.destroy();
                 it.remove();
             }
         }
         removePortletFile(webApplication.getWebApplicationName());
     }
 
     /**
      * Initializes all known portlet web applications in order
      *
      * @param req                the <code>HttpServletRequest</code>
      * @param res                the <code>HttpServletResponse</code>
      * @throws PortletDispatcherException      if a dispatching error occurs
      * @throws PortletException if an exception occurs initializing the portlet web application
      */
     public synchronized void initAllPortletWebApplications(HttpServletRequest req, HttpServletResponse res) throws PortletDispatcherException, PortletException {
         for (int i = 0; i < webappFiles.length; i++) {
             if (webappFiles[i].startsWith("README")) continue;
             initPortletWebApplication(webappFiles[i], req, res);
         }
     }
 
     /**
      * Initializes a portlet web application
      *
      * @param webApplicationName the name of the portlet web application
      * @param req                the <code>HttpServletRequest</code>
      * @param res                the <code>HttpServletresponse</code>
      * @throws PortletDispatcherException      if a dispatching error occurs
      * @throws PortletException if an exception occurs initializing the portlet web application
      */
     public synchronized void initPortletWebApplication(String webApplicationName, HttpServletRequest req, HttpServletResponse res) throws PortletDispatcherException, PortletException {
         log.debug("initing web app " + webApplicationName);
         PortletWebApplication portletWebApp = new PortletWebApplicationImpl(webApplicationName, context);
         addPortletWebApplication(portletWebApp);
         portletInvoker.initPortletWebApp(webApplicationName, req, res);
     }
 
     /**
      * Shuts down a currently active portlet web application from the portlet container
      *
      * @param webApplicationName the name of the portlet web application
      * @param req                the <code>HttpServletRequest</code>
      * @param res                the <code>HttpServletResponse</code>
      * @throws PortletDispatcherException      if a dispatching error occurs
      */
     public synchronized void destroyPortletWebApplication(String webApplicationName, HttpServletRequest req, HttpServletResponse res) throws PortletDispatcherException  {
         log.debug("in destroyPortletWebApplication: " + webApplicationName);
         portletInvoker.destroyPortletWebApp(webApplicationName, req, res);
         removePortletWebApplication(webApplicationName);
     }
 
     /**
      * Lists all the portlet web applications known to the portlet container
      *
      * @return the list of web application names as <code>String</code> elements
      */
     public List getPortletWebApplicationNames() {
         List l = new Vector();
         // get rid of duplicates -- in the case of JSR portlets two webapps exist by the same name
         // since the first one represents the "classical" webapp which itself adds the jsr webapp to the
         // registry with the same name
         /*
         for (int i = 0; i < webapps.size(); i++) {
             PortletWebApplication webapp = (PortletWebApplication) webapps.get(i);
             String webappName = webapp.getWebApplicationName();
             if (!l.contains(webappName)) l.add(webappName);
         }
         */
         for (int i = 0; i < webappFiles.length; i++) {
             if (webappFiles[i].startsWith("README")) continue;
             l.add(webappFiles[i]);
         }
         return l;
     }
 
     /**
      * Returns the deployed web application names
      *
      * @return the known web application names
      */
     public List getWebApplicationNames() {
         List l = new Vector();
         // get rid of duplicates -- in the case of JSR portlets two webapps exist by the same name
         // since the first one represents the "classical" webapp which itself adds the jsr webapp to the
         // registry with the same name
         for (int i = 0; i < webapps.size(); i++) {
             PortletWebApplication webapp = (PortletWebApplication) webapps.get(i);
             String webappName = webapp.getWebApplicationName();
             if (!l.contains(webappName)) l.add(webappName);
         }
         Iterator it = l.iterator();
         while (it.hasNext()) {
             String s = (String) it.next();
             int idx = s.indexOf(".");
             if (idx < 0) {
                 if (l.contains(s + ".1")) it.remove();
             }
         }
         return l;
     }
 
     /**
      * Returns the description of the supplied web application
      *
      * @param webApplicationName
      * @return the description of this web application
      */
     public String getPortletWebApplicationDescription(String webApplicationName) {
         String webappDesc = "Undefined portlet web application";
         for (int i = 0; i < webapps.size(); i++) {
             PortletWebApplication webApp = (PortletWebApplication) webapps.get(i);
             if (webApp.getWebApplicationName().equalsIgnoreCase(webApplicationName)) {
                 return webApp.getWebApplicationDescription();
             }
         }
         return webappDesc;
     }
 
 }
