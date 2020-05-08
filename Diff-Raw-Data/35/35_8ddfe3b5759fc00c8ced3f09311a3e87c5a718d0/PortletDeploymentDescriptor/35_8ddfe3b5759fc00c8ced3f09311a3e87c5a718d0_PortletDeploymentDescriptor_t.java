 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portletcontainer.descriptor;
 
 import org.gridlab.gridsphere.core.persistence.ConfigurationException;
 import org.gridlab.gridsphere.core.persistence.RestoreException;
 import org.gridlab.gridsphere.core.persistence.castor.PersistenceManagerXml;
 import org.gridlab.gridsphere.portlet.PortletLog;
 
import javax.servlet.ServletConfig;
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.List;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
 
 public class PortletDeploymentDescriptor {
 
     private static PortletLog log = org.gridlab.gridsphere.portlet.impl.SportletLog.getInstance(PortletDeploymentDescriptor.class);
     private Vector PortletDef = new Vector();
 
    public PortletDeploymentDescriptor(ServletConfig config) throws PortletDeploymentDescriptorException {
            // load in layout.xml file
            String appRoot = config.getServletContext().getRealPath("") + "/";
            String portletConfigFile = config.getInitParameter("portlet.xml");
            String portletMappingFile = config.getInitParameter("portlet-mapping.xml");
            if (portletConfigFile == null) {
                portletConfigFile = "WEB-INF/conf/portlet.xml";
            }
            if (portletMappingFile == null) {
                portletMappingFile = "WEB-INF/conf/portlet-mapping.xml";
            }
            String fullPath = appRoot + portletConfigFile;
            String mappingPath = appRoot + portletMappingFile;
            try {
                FileInputStream fistream = new FileInputStream(fullPath);
            } catch (FileNotFoundException e) {
                log.error("Can't find file: " + fullPath);
                throw new PortletDeploymentDescriptorException("Unable to create descriptor from " + fullPath + " using " + mappingPath);
            }
            load(fullPath, mappingPath);
        }

     /**
      * Constructs a PortletDeploymentDescriptor from a portlet.xml and mapping file
      *
     * @param portletFilePath location of the portlet.xml
     * @param mappingFilePath location of the mapping file
      * @throws PortletDeploymentDescriptorException if the PortletDeploymentDescriptor cannot be created
      */
     public PortletDeploymentDescriptor(String portletFilePath, String mappingFilePath) throws PortletDeploymentDescriptorException  {
         load(portletFilePath, mappingFilePath);
     }
 
     /**
      * Returns a list of PortletApps
      *
      * @return a list of PortletApps
      */
     public List getPortletDef() {
         return PortletDef;
     }
 
     /**
      * Creates a new PortletApp
      *
      * @return a new PortletApp
      */
     public PortletDefinition createPortletDef() {
         return new PortletDefinition();
     }
 
     /**
      * Add a new portlet app to the descriptor
      * <b>not implemented yet</b>
      */
     public void addPortletDef(PortletDefinition portletDef) {}
 
     /**
      * Loads the PortletDeploymentDescriptor from the portlet.xml
      *
      * @param url the location of the portlet.xml file
      * @param mapping the location of the portlet xml mapping file
      * @throws PortletDeploymentDescriptorException if the PortletDeploymentDescriptor cannot be created
      */
     public void load(String url, String mapping) throws PortletDeploymentDescriptorException  {
         PersistenceManagerXml pmx = new PersistenceManagerXml();
 
         // where is the portlet.xml ?
         pmx.setConnectionURL(url);
 
         // set the path to the mapping file
         pmx.setMappingFile(mapping);
 
         PortletCollection pc = null;
 
         // try to get it
         try {
              pc = (PortletCollection)pmx.restoreObject();
         } catch (RestoreException e) {
             log.error("RestoreError ("+pmx.getMappingFile()+", "+pmx.getConnectionURL()+") "+e.getMessage());
             throw new PortletDeploymentDescriptorException("Unable to restore: "+e.getMessage());
         } catch (ConfigurationException e) {
             log.error("ConfigurationError ("+pmx.getMappingFile()+", "+pmx.getConnectionURL()+") "+e);
             throw new PortletDeploymentDescriptorException("Configuration error: "+e.getMessage());
         }
         this.PortletDef = (Vector)pc.getPortletDefList();
     }
 
     /**
      * Save the portlet deployment descriptor to portlet.xml
      * <b>not implemented yet</b>
      */
     public void save() {}
 
 }
