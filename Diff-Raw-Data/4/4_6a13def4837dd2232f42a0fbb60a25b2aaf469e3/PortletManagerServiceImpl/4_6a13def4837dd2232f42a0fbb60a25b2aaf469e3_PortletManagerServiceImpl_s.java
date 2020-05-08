 /*
  * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
  * @version $Id: PortletManager.java 4733 2006-04-10 18:29:36Z novotny $
  */
 package org.gridsphere.services.core.registry.impl;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridsphere.portletcontainer.ApplicationPortlet;
 import org.gridsphere.portletcontainer.PortletDispatcherException;
 import org.gridsphere.portletcontainer.PortletWebApplication;
 import org.gridsphere.portletcontainer.impl.PortletInvoker;
 import org.gridsphere.portletcontainer.impl.PortletWebApplicationLoader;
 import org.gridsphere.services.core.customization.SettingsService;
 import org.gridsphere.services.core.registry.PortletManagerService;
 import org.gridsphere.services.core.registry.PortletRegistryService;
 
 import javax.portlet.PortletException;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.IOException;
 import static java.util.Arrays.sort;
 import java.util.*;
 
 /**
  * The <code>PortletManager</code> is a singleton responsible for maintaining the registry of portlet
  * web applications known to the portlet container.
  */
 public class PortletManagerServiceImpl implements PortletManagerService, PortletServiceProvider {
 
     private Log log = LogFactory.getLog(PortletManagerServiceImpl.class);
 
     private ServletContext context = null;
 
     private PortletRegistryService registryService = null;
 
     private PortletInvoker portletInvoker = null;
 
     // A multi-valued hashtable with a webapp key and a List value containing portletAppID's
     private List<PortletWebApplication> webapps = new Vector<PortletWebApplication>();
 
     private Map<String, PortletWebApplicationLoader> webappLoaders = new HashMap<String, PortletWebApplicationLoader>();
 
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
     public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
 
         registryService = (PortletRegistryService) PortletServiceFactory.createPortletService(PortletRegistryService.class, true);
 
         portletInvoker = new PortletInvoker();
 
         context = config.getServletContext();
 
         // get the real path where the info is (will be) stored
         SettingsService settingsService = (SettingsService) PortletServiceFactory.createPortletService(SettingsService.class, true);
         PORTLETS_PATH = settingsService.getRealSettingsPath("portlets");
 
         File f = new File(settingsService.getRealSettingsPath("portlets"));
         if (f.exists() && f.isDirectory()) {
 
             // add the portal to the top spot (usually the name is '..../gridsphere' but could be something else, look at
             // gridsphere.properties
             String realPath = context.getRealPath("");
             // patch for jetty, path has always a / at the end as opposed to tomcat, so remove it
             if (realPath.endsWith("/")) {
                 realPath = realPath.substring(0, realPath.length() - 1);
             }
             int l = realPath.lastIndexOf(File.separator);
             String webappName = realPath.substring(l + 1);
            String file = PORTLETS_PATH + "/" + webappName + ".1";
             File myportletapp = new File(file);
             try {
                 myportletapp.createNewFile();
             } catch (IOException e) {
                 throw new PortletServiceUnavailableException("Unable to create blank file:" + file);
             }
 
             webappFiles = f.list();
 
             // sort webapps by priority
             sort(webappFiles, new WebappComparator());
 
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
             log.error("Portlet application " + PORTLETS_PATH + " does not exist!");
             throw new PortletServiceUnavailableException("Portlet application " + PORTLETS_PATH + " does not exist!");
         }
     }
 
     public void destroy() {
     }
 
     /**
      * Adds a portlet web application to the registry
      *
      * @param portletWebApp the portlet web application name
      */
     public synchronized void addPortletWebApplication(PortletWebApplication portletWebApp) {
         log.debug("adding webapp: " + portletWebApp.getWebApplicationName());
         addPortletFile(portletWebApp.getWebApplicationName());
         Collection<ApplicationPortlet> appPortlets = portletWebApp.getAllApplicationPortlets();
 
         for (ApplicationPortlet appPortlet : appPortlets) {
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
      * Initializes all known portlet web applications in order
      *
      * @param req the <code>HttpServletRequest</code>
      * @param res the <code>HttpServletResponse</code>
      * @throws PortletDispatcherException if a dispatching error occurs
      * @throws PortletException           if an exception occurs initializing the portlet web application
      */
     public synchronized void initAllPortletWebApplications(HttpServletRequest req, HttpServletResponse res) throws PortletDispatcherException, PortletException {
         for (int i = 0; i < webappFiles.length; i++) {
             if (webappFiles[i].startsWith("README")) continue;
             initPortletWebApplication(webappFiles[i], req, res);
         }
     }
 
     public synchronized void initPortletWebApplication(String webApplicationName, HttpServletRequest req, HttpServletResponse res) throws PortletDispatcherException, PortletException {
         log.debug("initing web app " + webApplicationName);
         PortletWebApplicationLoader webAppLoader = new PortletWebApplicationLoader(webApplicationName, context);
         webappLoaders.put(webApplicationName, webAppLoader);
         portletInvoker.initPortletWebApp(webAppLoader, req, res);
     }
 
     /**
      * Shuts down a currently active portlet web application from the portlet container
      *
      * @param webApplicationName the name of the portlet web application
      * @param req                the <code>HttpServletRequest</code>
      * @param res                the <code>HttpServletResponse</code>
      * @throws PortletDispatcherException if a dispatching error occurs
      */
     public synchronized void destroyPortletWebApplication(String webApplicationName, HttpServletRequest req, HttpServletResponse res) throws PortletDispatcherException {
         log.debug("in destroyPortletWebApplication: " + webApplicationName);
         PortletWebApplicationLoader webAppLoader = webappLoaders.get(webApplicationName);
         portletInvoker.destroyPortletWebApp(webAppLoader, req, res);
     }
 
     /**
      * Logs out a portlet web application
      *
      * @param webApplicationName the name of the portlet web application
      * @param req                the <code>HttpServletRequest</code>
      * @param res                the <code>HttpServletresponse</code>
      * @throws PortletDispatcherException if a dispatching error occurs
      */
     public synchronized void logoutPortletWebApplication(String webApplicationName, HttpServletRequest req, HttpServletResponse res) throws PortletDispatcherException {
         log.debug("logout web app " + webApplicationName);
         PortletWebApplicationLoader webAppLoader = webappLoaders.get(webApplicationName);
         portletInvoker.logoutPortletWebApp(webAppLoader, req, res);
     }
 
     public synchronized void logoutAllPortletWebApplications(HttpServletRequest req, HttpServletResponse res) throws PortletDispatcherException {
         for (int i = 0; i < webappFiles.length; i++) {
             if (webappFiles[i].startsWith("README")) continue;
             logoutPortletWebApplication(webappFiles[i], req, res);
         }
     }
 
     /**
      * Lists all the portlet web applications known to the portlet container
      *
      * @return the list of web application names as <code>String</code> elements
      */
     public List<String> getPortletWebApplicationNames() {
         List<String> l = new Vector<String>();
         // get rid of duplicates -- in the case of JSR portlets two webapps exist by the same name
         // since the first one represents the "classical" webapp which itself adds the jsr webapp to the
         // registry with the same name
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
     public List<String> getWebApplicationNames() {
         List<String> l = new Vector<String>();
         // get rid of duplicates -- in the case of JSR portlets two webapps exist by the same name
         // since the first one represents the "classical" webapp which itself adds the jsr webapp to the
         // registry with the same name
         for (int i = 0; i < webapps.size(); i++) {
             PortletWebApplication webapp = webapps.get(i);
             String webappName = webapp.getWebApplicationName();
             if (!l.contains(webappName)) l.add(webappName);
         }
         Iterator<String> it = l.iterator();
         while (it.hasNext()) {
             String s = it.next();
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
      * @param webApplicationName the web application name
      * @return the description of this web application
      */
     public String getPortletWebApplicationDescription(String webApplicationName) {
         String webappDesc = "Undefined portlet web application";
         for (int i = 0; i < webapps.size(); i++) {
             PortletWebApplication webApp = webapps.get(i);
             if (webApp.getWebApplicationName().equalsIgnoreCase(webApplicationName)) {
                 return webApp.getWebApplicationDescription();
             }
         }
         return webappDesc;
     }
 
 }
