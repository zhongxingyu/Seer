 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portletcontainer.impl;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.portlet.PortletException;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.PortletSettings;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletSettings;
 import org.gridlab.gridsphere.portletcontainer.ApplicationPortletConfig;
 import org.gridlab.gridsphere.portletcontainer.ConcretePortlet;
 import org.gridlab.gridsphere.portletcontainer.ConcretePortletConfig;
 import org.gridlab.gridsphere.portletcontainer.impl.descriptor.ConcreteSportletDefinition;
 import org.gridlab.gridsphere.portletcontainer.impl.descriptor.PortletDeploymentDescriptor;
 import org.gridlab.gridsphere.portletcontainer.impl.descriptor.ConcreteSportletConfig;
 
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * The <code>ConcreteSportlet</code> is an implementation of the <code>ConcretePortlet</code> that provides methods
  * for accessing concrete portlet objects obtained from the portlet deployment descriptors.
  */
 public class ConcreteSportlet implements ConcretePortlet {
 
     private static PortletLog log = SportletLog.getInstance(ConcreteSportlet.class);
 
     private PortletDeploymentDescriptor portletDD = null;
     private ConcreteSportletConfig concSportletConfig = null;
     private Hashtable contextHash = null;
     private String portletName = "Undefined Portlet Name";
     private String concreteID = null;
     private List languageList = null;
     private String defaultLocale = "en_US";
     private SportletSettings portletSettings = null;
 
     /**
      * Constructs an instance of ConcreteSportlet
      *
      * @param pdd a <code>PortletDeploymentDescriptor</code>
      * @param appPortletConfig an application portlet configuration
      * @param concSportletDef a concrete portlet descriptor
      */
     public ConcreteSportlet(PortletDeploymentDescriptor pdd, ApplicationPortletConfig appPortletConfig, ConcreteSportletDefinition concSportletDef) throws PortletException  {
         this.portletDD = pdd;
         this.concSportletConfig = concSportletDef.getConcreteSportletConfig();
         String appID, cappID;
         int index;
 
         // Get PortletApplication UID  e.g. classname.number
         appID = appPortletConfig.getApplicationPortletID();
 
        //portletName = appPortletConfig.getPortletName();

        portletName = concSportletDef.getConcreteSportletConfig().getName();
        
         concreteID = concSportletDef.getConcretePortletID();
         index = concreteID.lastIndexOf(".");
         cappID = concreteID.substring(0, index);
 
         if (!(cappID.equals(appID))) {
             String msg = "The portlet classname defined by the portlet application id: "
             + appID + " and the concrete portlet classname: " + cappID + " are not equal";
             log.error(msg);
             throw new PortletException(msg);
         }
 
         this.contextHash = concSportletDef.getContextAttributes();
 
         // Get locale information
         defaultLocale = concSportletConfig.getDefaultLocale();
         languageList = concSportletConfig.getLanguageList();
         portletSettings = new SportletSettings(this);
     }
 
     /**
      * Returns the sportlet settings for this concrete portlet
      *
      * @return the sportlet settings
      */
     public PortletSettings getPortletSettings() {
         return portletSettings;
     }
 
     /**
      * Return the concrete portlet configuration
      *
      * @return the concrete portlet configuration
      */
     public ConcretePortletConfig getConcretePortletConfig() {
         return concSportletConfig;
     }
 
     /**
      * Sets the concrete portlet configuration
      *
      * @param concPortletConfig the concrete portlet configuration
      */
     public void setConcretePortletConfig(ConcretePortletConfig concPortletConfig) {
         this.concSportletConfig = (ConcreteSportletConfig)concPortletConfig;
     }
 
     /**
      * Returns the concrete portlet id
      *
      * @return the concrete portlet id
      */
     public String getConcretePortletID() {
         return concreteID;
     }
 
     /**
      * Returns the portlet context attributes that are used in the
      * <code>PortletApplicationSettings</code> class
      *
      * @return the <code>Hashtable</code> containing portlet context attributes
      */
     public Hashtable getContextAttributes() {
         return contextHash;
     }
 
     /**
      * Sets the portlet context attributes that are used in the
      * <code>PortletApplicationSettings</code> class
      *
      * @param contextHash the Hashtable containing portlet context attributes
      */
     public void setContextAttributes(Hashtable contextHash) {
         this.contextHash = contextHash;
     }
 
     /**
      * Return the name of this portlet
      *
      * @return the portlet name
      */
     public String getPortletName() {
         return portletName;
     }
 
     /**
      * gets the default locale of a portlet
      *
      * @return the default locale of the portlet
      */
     public String getDefaultLocale() {
         return defaultLocale;
     }
 
     /**
      * Returns the language info of a portlet
      *
      * @return language info of the portlet
      */
     public List getLanguageList() {
         return languageList;
     }
 
     public String getDescription(Locale locale) {
         return portletSettings.getDescription(locale, null);
     }
 
     public String getDisplayName(Locale locale) {
         return portletName;
     }
 
     /**
      * Saves any concrete portlet changes to the descriptor
      *
      * @throws IOException if an I/O error occurs
      */
     public void save() throws IOException {
         try {
             portletDD.setConcreteSportlet(this);
             portletDD.save();
         } catch (PersistenceManagerException e) {
             log.error("Unable to save concrete portlet descriptor! " + concreteID, e);
         }
     }
 
     public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("\t name: " + portletName + "\n");
         sb.append("\t concrete ID: " + concreteID + "\n");
         return sb.toString();
     }
 }
