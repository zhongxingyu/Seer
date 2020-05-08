 package org.zaproxy.zap.extension.saml;
 
 import org.parosproxy.paros.model.Model;
 
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
 import java.io.File;
 
 public class SAMLConfiguration {
 
     private static final String SAML_AUTOCHANGE_CONF_FILE = "saml_autochange_attributes.xml";
     private static final String SAML_ATTRIB_FILE = "saml_configured_attributes.xml";
     private static SAMLConfiguration configuration = new SAMLConfiguration();
 
     private boolean initialized;
     private AttributeSet availableAttributes;
     private AttributeSet autoChangeValues;
 
     public static SAMLConfiguration getConfiguration() throws SAMLException {
         if(!configuration.initialized){
             configuration.initialize();
            configuration.initialized = true;
         }
         return configuration;
     }
 
     /**
      * Initialize the attributes and configurations
      * @throws SAMLException
      */
     public void initialize() throws SAMLException {
         String allAttribFilePath = Model.getSingleton().getOptionsParam(). getUserDirectory().getAbsolutePath()+ "/" +
                 SAML_ATTRIB_FILE;
         File allAttribFile = new File(allAttribFilePath);
 
         if(allAttribFile.exists()){
             //load from the file
         } else {
             allAttribFile = new File(SAMLConfiguration.class.getResource(SAML_ATTRIB_FILE).getFile());
             if (!allAttribFile.exists()){
                 throw new SAMLException("Configuration file not found");
             }
             //todo:create a copy at user directory
         }
         //load the attributes
         AttributeSet attributeSet = (AttributeSet) loadXMLObject(AttributeSet.class,allAttribFile);
         availableAttributes = attributeSet;
 
         String autoChangeAttribFilePath = Model.getSingleton().getOptionsParam(). getUserDirectory().getAbsolutePath
                 ()+ "/" + SAML_AUTOCHANGE_CONF_FILE;
         File autoChangeAttribFile = new File(autoChangeAttribFilePath);
         if(autoChangeAttribFile.exists()){
             //todo parse
         }else {
             autoChangeValues = new AttributeSet();
         }
     }
 
     public AttributeSet getAvailableAttributes() {
         return availableAttributes;
     }
 
     public void setAvailableAttributes(AttributeSet availableAttributes) {
         this.availableAttributes = availableAttributes;
     }
 
     public AttributeSet getAutoChangeValues() {
         return autoChangeValues;
     }
 
     public void setAutoChangeValues(AttributeSet autoChangeValues) {
         this.autoChangeValues = autoChangeValues;
     }
 
     /**
      * Unmarshall the XML file and extract the object using JAXB
      * @param clazz class of the object
      * @param file xml file
      * @return
      * @throws SAMLException
      */
     private Object loadXMLObject(Class clazz, File file) throws SAMLException {
         try {
             JAXBContext context = JAXBContext.newInstance(clazz);
             Unmarshaller unmarshaller = context.createUnmarshaller();
             return unmarshaller.unmarshal(file);
         } catch (JAXBException e) {
             throw new SAMLException("XML loading failed",e);
         }
     }
 }
