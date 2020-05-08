 /*
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.core.persistence.castor;
 
 import org.exolab.castor.mapping.Mapping;
 import org.exolab.castor.mapping.MappingException;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.Marshaller;
 import org.exolab.castor.xml.Unmarshaller;
 import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;
 
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerXml;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * The PersistenceManagerXmlImpl provides easy access to marshal/unmarshal Java objects to XML files
  */
 public class PersistenceManagerXmlImpl implements PersistenceManagerXml {
 
     protected final static PortletLog log = SportletLog.getInstance(PersistenceManagerXmlImpl.class);
 
     private String mappingPath = null;
     private String descriptorPath = null;
 
     /**
      * PersistenceManagerXmlImpl default constructor
      */
     private PersistenceManagerXmlImpl() {}
 
     /**
      * Creates an instance of PersistenceManagerXmlImpl from a descriptor and mapping URL
      * @param descriptorPath the descriptor location
      * @param mappingPath the mapping location
      */
     public PersistenceManagerXmlImpl(String descriptorPath, String mappingPath) {
         this.descriptorPath = descriptorPath;
         this.mappingPath = mappingPath;
     }
 
     /**
      * Sets the mapping file path
      *
      * @param mappingPath the mapping file path
      */
     public void setMappingPath(String mappingPath) {
         this.mappingPath = mappingPath;
     }
 
     /**
      * Return the mapping file path
      *
      * @return the mapping file path
      */
     public String getMappingPath() {
         return mappingPath;
     }
 
     /**
      * Sets the descriptor file path
      *
      * @param descriptorPath the file path of the descriptor
      */
     public void setDescriptorPath(String descriptorPath) {
         this.descriptorPath = descriptorPath;
     }
 
     /**
      * Returns the filename of the mappingfile
      *
      * @return name of the mappingfile
      */
     public String getDescriptorPath() {
         return descriptorPath;
     }
 
     /**
      * Marshals the given object to an xml file
      *
      * @param object object to be marshalled
      * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException if the configuration was wrong
      * @throws IOException if an I/O error occurs
      */
     public void save(Object object) throws PersistenceManagerException, IOException {
         try {
             FileWriter filewriter = new FileWriter(descriptorPath);
 
             Marshaller marshal = new Marshaller(filewriter);
             Mapping map = new Mapping();
             map.loadMapping(mappingPath);
             marshal.setMapping(map);
             marshal.marshal(object);
             filewriter.close();
             Class cl = object.getClass();
             log.debug("Wrote object of type " + cl.getName() + " to XMLFile " + descriptorPath);
         } catch (ValidationException e) {
             log.error("Unable to marshal object: " + e.getException().toString());
             throw new PersistenceManagerException("Validation Error" + e.getException().toString());
         } catch (MarshalException e) {
             log.error("Unable to marshal object: " + e.getException().toString());
             throw new PersistenceManagerException("Marshal Error: " + e.getException().toString());
         } catch (MappingException e) {
             log.error("Unable to marshal object: " + e.getException().toString());
             e.printStackTrace();
             throw new PersistenceManagerException("Mapping Error" + e.getException().toString());
         }
     }
 
     /**
      * restores an object from an xml file
      *
      * @return object which was unmarshalled
      * @throws PersistenceManagerException if restore was not succsessful
      * @throws IOException if there was a configurationerror
      */
     public Object load() throws  IOException, PersistenceManagerException {
         Object object = null;
         try {
             log.debug("Using getConnectionURL() " + descriptorPath);
 
	    InputSource xmlSource = new InputSource(descriptorPath);
 
             Mapping mapping = new Mapping();
 
             mapping.loadMapping(mappingPath);
             log.debug("Using  getMappingFile()" + mappingPath);
 
             Unmarshaller unmarshal = new Unmarshaller(mapping);
            object = unmarshal.unmarshal(xmlSource);
 
         } catch (MappingException e) {
             log.error("MappingException: " + e.getException().toString());
             throw new PersistenceManagerException("Mapping Error" + e.getException().toString());
         } catch (MarshalException e) {
             log.error("MarshalException " + e.getException().toString());
             throw new PersistenceManagerException("Marshal Error" + e.getException().toString());
         } catch (ValidationException e) {
             log.error("ValidationException " + e.getException().toString());
             throw new PersistenceManagerException("Validation Error" + e.getException().toString());
         }
         return object;
     }
 
 
 }
 
 
