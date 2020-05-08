 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id: PortletRegistry.java 4496 2006-02-08 20:27:04Z wehrens $
  */
 package org.gridsphere.services.core.registry.impl;
 
 import org.gridsphere.portlet.PortletLog;
 import org.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridsphere.portlet.impl.SportletLog;
 import org.gridsphere.services.core.registry.PortletRegistryService;
 import org.gridsphere.portletcontainer.ApplicationPortlet;
 import org.gridsphere.portletcontainer.ConcretePortlet;
 
 import java.util.*;
 
 /**
  * The <code>PortletRegistry</code> is simply a static <code>Hashtable</code> that keeps track
  * of all the application portlets known to the container.
  */
 public class PortletRegistryServiceImpl implements PortletRegistryService, PortletServiceProvider {
 
 
     private PortletLog log = SportletLog.getInstance(PortletRegistryServiceImpl.class);
 
     private static Map allApplicationPortlets = new Hashtable();
 
     public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
 
     }
 
     public void destroy() {}
 
     /**
      * Adds an application portlet to the registry
      *
      * @param appPortlet an <code>ApplicationPortlet</code>
      */
     public void addApplicationPortlet(ApplicationPortlet appPortlet) {
         String key = appPortlet.getApplicationPortletID();
         if (key.indexOf("#") < 0) {
             key = appPortlet.getWebApplicationName() + "#" + appPortlet.getApplicationPortletID();
         }
         if (allApplicationPortlets.get(key) != null) {
             log.debug("Replacing existing app portlet: " + key);
         }
         allApplicationPortlets.put(key, appPortlet);
     }
 
     /**
      * Removes an application portlet from the registry
      *
      * @param applicationPortlet the application portlet
      */
     public void removeApplicationPortlet(ApplicationPortlet applicationPortlet) {
         allApplicationPortlets.remove(applicationPortlet.getApplicationPortletID());
     }
 
     /**
      * Return the application portlet with the corresponding id
      *
      * @param applicationPortletID the application portlet id
      * @return an application portlet
      */
     public ApplicationPortlet getApplicationPortlet(String applicationPortletID) {
         int idx = applicationPortletID.indexOf("#");
         //String portletName = "";
         //String webapp = "";
         if (idx > 0) {
             //webapp = applicationPortletID.substring(0, idx);
             //portletName = applicationPortletID.substring(idx+1);
             //System.err.println("webapp= " + webapp + " pname= " + portletName);
             return (ApplicationPortlet)allApplicationPortlets.get(applicationPortletID);
         }
         Collection coll = allApplicationPortlets.values();
         Iterator it = coll.iterator();
         while (it.hasNext()) {
             ApplicationPortlet app = (ApplicationPortlet) it.next();
             if (app.getApplicationPortletID().equals(applicationPortletID)) return app;
             if (app.getApplicationPortletClassName().equals(applicationPortletID)) return app;
         }
         log.debug("Unable to find " + applicationPortletID + " in registry");
         return null;
     }
 
  
     /**
      * Returns all application portlets from the registry
      *
      * @return the collection of application portlets
      */
     public Collection getAllApplicationPortlets() {
         return allApplicationPortlets.values();
     }
 
 
     /**
      * Returns all application portlets from the supplied portlet web application
      *
      * @param webApplicationName the portlet web application name
      * @return the list of application portlets
      */
     public List getApplicationPortlets(String webApplicationName) {
         List webappPortlets = new Vector();
         Set set = allApplicationPortlets.keySet();
         ApplicationPortlet appPortlet;
         Iterator it = set.iterator();
         while (it.hasNext()) {
             String concretePortletID = (String) it.next();
             appPortlet = (ApplicationPortlet) allApplicationPortlets.get(concretePortletID);
             if (appPortlet.getWebApplicationName().equals((webApplicationName))) {
                 webappPortlets.add(appPortlet);
             }
         }
         return webappPortlets;
     }
 
     /**
      * Returns the application portlet id given a concrete portlet id
      *
      * @param concretePortletID the concrete portlet id
      * @return the application portlet id or an empty string
      */
     public String getApplicationPortletID(String concretePortletID) {
         int i = concretePortletID.lastIndexOf(".");
         // do nothing if id is on form <web app name>#<portlet name>
         if (concretePortletID.indexOf("#") > 0) return concretePortletID;
         if (i < 0) return concretePortletID;
         // check to see if it has number at the end
         String numStr = concretePortletID.substring(i + 1);
         String appID = "";
         try {
             Integer.parseInt(numStr);
             appID = concretePortletID.substring(0, i);
         } catch (NumberFormatException e) {
             appID = concretePortletID;
         }
         return appID;
     }
 
     public void logRegistry() {
         log.debug("Displaying Portlet registry contents:\n");
         Iterator it = allApplicationPortlets.keySet().iterator();
         while (it.hasNext()) {
             String appID = (String) it.next();
             ApplicationPortlet appPortlet = (ApplicationPortlet) allApplicationPortlets.get(appID);
             log.debug("\tApplication portlet : " + appPortlet.getApplicationPortletID() + "\n");
             log.debug("\t" + appPortlet + "\n");
             List concPortlets = appPortlet.getConcretePortlets();
             Iterator concIt = concPortlets.iterator();
             while (concIt.hasNext()) {
                 ConcretePortlet conc = (ConcretePortlet) concIt.next();
                 log.debug("\t\tConcrete portlet : " + conc.getConcretePortletID() + "\n");
                 log.debug("\t" + conc + "\n");
             }
         }
 
 
     }
 }
