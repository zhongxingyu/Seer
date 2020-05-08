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
 import org.gridlab.gridsphere.core.persistence.*;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The Class provides easy access to marshal/unmarshal objects to XML files
  */
 public class PersistenceManagerXml  {
 
    protected transient static PortletLog log = SportletLog.getInstance(PersistenceManagerXml.class);
 
     private static PersistenceManagerXml instance = new PersistenceManagerXml();
     private String mappingFile = null;
     private String Url = null;
 
     /**
      * PersistenceManagerXml default constructor
      */
     private PersistenceManagerXml() {}
 
     public static PersistenceManagerXml getInstance() {
         return instance;
     }
 
     /**
      * Return the connectionURL
      *
      * @return filename
      */
     public String getConnectionURL() {
         return Url;
     }
 
     /**
      * Sets the connection URL
      *
      * @param url
      *
      */
     public void setConnectionURL(String url) {
         Url = url;
     }
 
 
     /**
      * Sets the mapping file
      *
      * @param mappingFile file containing the mapping
      */
     public void setMappingFile(String mappingFile) {
         this.mappingFile = mappingFile;
     }
 
 
     /**
      * Returns the filename of the mappingfile
      *
      * @return name of the mappingfile
      */
     public String getMappingFile() {
         return mappingFile;
     }
 
 
     /**
      * Checks if all setting for marshalling xml data are done
      *
      * @throws ConfigurationException if one variable is not set
      */
     private void checkSetting() throws ConfigurationException {
 
         // Mapping can be null
         if (getConnectionURL().equals(null)) throw new ConfigurationException("Configuration Error");
 
     }
 
     /**
      * Updates an object in the xml file (same as create)
      *
      * @throws UpdateException if the update failed
      * @param object update 'object'
      */
     public void update(Object object) throws IOException, PersistenceManagerException {
         create(object);
     }
 
     /**
      * Marshals the given object to an xml file
      *
      * @throws CreateException if the creation failed
      * @throws ConfigurationException if the configuration was wrong
      * @param object object to be marshalled
      */
     public void create(Object object) throws PersistenceManagerException, IOException {
 
         checkSetting();
 
         try {
             FileWriter filewriter = new FileWriter(getConnectionURL());
 
             Marshaller marshal = new Marshaller(filewriter);
 
             Mapping map = new Mapping();
             map.loadMapping(getMappingFile());
             marshal.setMapping(map);
 
             marshal.marshal(object);
             filewriter.close();
             Class cl = object.getClass();
             log.debug("Wrote object of type " + cl.getName() + " to XMLFile " + getConnectionURL());
         } catch (ValidationException e) {
            log.error("Unable to marshal object: ", e);
            throw new PersistenceManagerException("Validation Error", e);
         } catch (MarshalException e) {
            log.error("Unable to marshal object: ", e);
            throw new PersistenceManagerException("Marshal Error", e);
         } catch (MappingException e) {
            log.error("Unable to marshal object: ", e);
            throw new PersistenceManagerException("Mapping Error", e);
         }
 
     }
 
     /**
      * restores an object from an xml file
      *
      * @throws RestoreException if restore was not succsessful
      * @throws ConfigurationException if there was a configurationerror
      * @return object which was unmarshalled
      */
     public Object restoreObject() throws IOException, PersistenceManagerException {
 
         checkSetting();
 
         Object object = null;
 
         try {
             FileReader filereader = new FileReader(getConnectionURL());
             Mapping mapping = new Mapping();
             mapping.loadMapping(getMappingFile());
             Unmarshaller unmarshal = new Unmarshaller(mapping);
             object = unmarshal.unmarshal(filereader);
         } catch (MappingException e) {
            log.error("Exception ", e);
            throw new PersistenceManagerException("Mapping Error", e);
         } catch (MarshalException e) {
            log.error("MarshalException ", e);
            throw new PersistenceManagerException("Marshal Error", e);
         } catch (ValidationException e) {
            log.error("ValidationException ", e);
            throw new PersistenceManagerException("Validation Error", e);
         }
         return object;
     }
 
     /**
      * Restore objects from xml file, just here to justify interface could be empty
      * since it makes no sense in xml files
      *
      * @returns list of objects (here just with one object)
      * @throws IOException if an I/O error occured
      * @throws PersistenceException if config was wrong
      */
     public List restoreList() throws IOException, PersistenceManagerException {
         List list = new ArrayList();
         list.add(restoreObject());
         return list;
     }
 
 }
 
 
