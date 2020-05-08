 /*
  * (C) Copyright 2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  */
 
 package org.nuxeo.ecm.platform.replication.importer;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.dom4j.io.SAXReader;
 import org.nuxeo.common.utils.Path;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.PathRef;
 import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
 import org.nuxeo.ecm.core.schema.types.primitives.DateType;
 
 /**
  * Core imports various types of documents. The document can't be updated, so it
  * has not to exist before.
  *
  * @author rux
  */
 public class ImporterDocumentCreator {
 
     private static final Log log = LogFactory.getLog(ImporterDocumentCreator.class);
 
     /**
      * Imports a document through core session.Depending on what properties are
      * received, a proxy or version or normal document is further imported.
      *
      * @param session - the nuxeo core session used to import the document
      * @param type - the type of the document that will be imported
      * @param id - the uuid of the document that will be imported
      * @param name - the name of the document that will be imported
      * @param parentPath - the parent path of the document that will be imported
      * @param properties - the properties file that will contain the information
      *            needed to proceed properly the import
      * @return the new imported document
      * @throws ClientException
      */
     public static DocumentModel importDocument(CoreSession session,
             String type, String id, String name, String parentPath,
             Properties properties) throws ClientException {
 
         if (properties == null) {
             log.debug("The received .properties file cannot be NULL ...");
             return null;
         }
 
         if (properties.getProperty(CoreSession.IMPORT_PROXY_TARGET_ID) != null
                 && properties.getProperty(CoreSession.IMPORT_PROXY_VERSIONABLE_ID) != null) {
             // import a proxy document
             return importProxyDocument(session, id, name, parentPath,
                     properties);
         } else if (properties.getProperty(CoreSession.IMPORT_VERSION_VERSIONABLE_ID) != null
                 && properties.getProperty(CoreSession.IMPORT_VERSION_MAJOR) != null
                 && properties.getProperty(CoreSession.IMPORT_VERSION_MINOR) != null) {
             // import a version document
             return importVersionDocument(session, type, id, name, properties);
         } else {
             // import a normal document
             return importUsualDocument(session, type, id, name, parentPath,
                     properties);
         }
 
     }
 
     /**
      * Looks in properties if it is about a proxy to import.
      */
     public static boolean isProxy(Properties properties) {
         return properties.getProperty(CoreSession.IMPORT_PROXY_TARGET_ID) != null
                 && properties.getProperty(CoreSession.IMPORT_PROXY_VERSIONABLE_ID) != null;
     }
 
     /**
      * It is the same loadXML method from core IO.
      */
     public static Document loadXML(File file) throws ClientException {
         BufferedInputStream in = null;
         try {
             in = new BufferedInputStream(new FileInputStream(file));
             return new SAXReader().read(in);
         } catch (Exception e) {
             throw new ClientException("Failed to read schemes for "
                     + file.getPath());
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException ioe) {
                     // who cares?
                 }
             }
         }
     }
 
     /**
      * Imports an usual document through core session.
      *
      * @param session - the nuxeo core session used to import the document
      * @param type - the type of the document that will be imported
      * @param id - the uuid of the document that will be imported
      * @param name - the name of the document that will be imported
      * @param parentPath - the parent path of the document that will be imported
      * @param properties - the properties file that will contain the information
      *            needed to proceed properly the import
      * @return the new imported document
      * @throws ClientException
      */
     public static DocumentModel importUsualDocument(CoreSession session,
             String type, String id, String name, String parentPath,
             Properties properties) throws ClientException {
 
         if (properties == null) {
             log.debug("The received .properties file cannot be NULL ...");
             return null;
         }
 
         DocumentModel document = new DocumentModelImpl((String) null, type, id,
                 new Path(name), null, null, new PathRef(parentPath), null,
                 null, null, session.getRepositoryName());
 
         String value = properties.getProperty(CoreSession.IMPORT_LOCK);
         if (value != null && !value.equals("")) {
             document.putContextData(CoreSession.IMPORT_LOCK, value);
         }
         value = properties.getProperty(CoreSession.IMPORT_CHECKED_IN);
         if (value != null) {
             document.putContextData(CoreSession.IMPORT_CHECKED_IN, new Boolean(
                     value));
         }
         value = properties.getProperty(CoreSession.IMPORT_BASE_VERSION_ID);
         if (value != null) {
             document.putContextData(CoreSession.IMPORT_BASE_VERSION_ID, value);
         }
         value = properties.getProperty(CoreSession.IMPORT_VERSION_MAJOR);
         if (value != null) {
             document.putContextData(CoreSession.IMPORT_VERSION_MAJOR,
                     Long.valueOf(value));
         }
         value = properties.getProperty(CoreSession.IMPORT_VERSION_MINOR);
         if (value != null) {
             document.putContextData(CoreSession.IMPORT_VERSION_MINOR,
                     Long.valueOf(value));
         }
 
         document.putContextData(CoreSession.IMPORT_LIFECYCLE_STATE,
                 properties.getProperty(CoreSession.IMPORT_LIFECYCLE_STATE));
         document.putContextData(CoreSession.IMPORT_LIFECYCLE_POLICY,
                 properties.getProperty(CoreSession.IMPORT_LIFECYCLE_POLICY));
 
         document.setPathInfo(parentPath, name);
         session.importDocuments(Collections.singletonList(document));
         session.save();
         return document;
     }
 
     /**
      * Import a version document through the core session
      *
      * @param session - the nuxeo core session used to import the document
      * @param type - the type of the document that will be imported
      * @param id - the uuid of the document that will be imported
      * @param name - the name of the document that will be imported
      * @param properties - the properties file that will contain the information
      *            needed to proceed properly the import
      *
      * @return the new imported document
      */
     public static DocumentModel importVersionDocument(CoreSession session,
             String type, String id, String name, Properties properties) {
 
         if (properties == null) {
             log.debug("The received .properties file cannot be NULL ...");
             return null;
         }
 
         DocumentModel document = new DocumentModelImpl((String) null, type, id,
                 new Path(name), null, null, null, null, null, null,
                 session.getRepositoryName());
 
         document.putContextData(
                 CoreSession.IMPORT_VERSION_VERSIONABLE_ID,
                 properties.getProperty(CoreSession.IMPORT_VERSION_VERSIONABLE_ID));
         String propertyValue = properties.getProperty(CoreSession.IMPORT_VERSION_LABEL);
         if (propertyValue != null) {
             document.putContextData(CoreSession.IMPORT_VERSION_LABEL,
                     propertyValue);
         }
         propertyValue = properties.getProperty(CoreSession.IMPORT_VERSION_DESCRIPTION);
         if (propertyValue != null) {
             document.putContextData(CoreSession.IMPORT_VERSION_DESCRIPTION,
                     propertyValue);
         }
 
         Calendar c = (Calendar) new DateType().decode(properties.getProperty(CoreSession.IMPORT_VERSION_CREATED));
         document.putContextData(CoreSession.IMPORT_VERSION_CREATED, c);
 
         document.putContextData(
                 CoreSession.IMPORT_VERSION_MAJOR,
                 Long.valueOf(properties.getProperty(CoreSession.IMPORT_VERSION_MAJOR)));
         document.putContextData(
                 CoreSession.IMPORT_VERSION_MINOR,
                 Long.valueOf(properties.getProperty(CoreSession.IMPORT_VERSION_MINOR)));
 
         document.putContextData(CoreSession.IMPORT_LIFECYCLE_STATE,
                 properties.getProperty(CoreSession.IMPORT_LIFECYCLE_STATE));
         document.putContextData(CoreSession.IMPORT_LIFECYCLE_POLICY,
                 properties.getProperty(CoreSession.IMPORT_LIFECYCLE_POLICY));

        document.putContextData(CoreSession.IMPORT_IS_VERSION, true);
         return document;
 
     }
 
     /**
      * Import a proxy document through the core session.The proxy documents will
      * have the type ecm:proxy
      *
      * @param session - the nuxeo core session used to import the document
      * @param id - the uuid of the document that will be imported
      * @param name - the name of the document that will be imported
      * @param parentPath - the parent path of the document that will be imported
      * @param properties - the properties file that will contain the information
      *            needed to proceed properly the import
      *
      * @return the new imported document
      */
     public static DocumentModel importProxyDocument(CoreSession session,
             String id, String name, String parentPath, Properties properties) {
 
         if (properties == null) {
             log.debug("The received .properties file cannot be NULL ...");
             return null;
         }
 
         DocumentModel document = new DocumentModelImpl((String) null,
                 CoreSession.IMPORT_PROXY_TYPE, id, new Path(name), null, null,
                 new PathRef(parentPath), null, null, null, null);
 
         document.putContextData(CoreSession.IMPORT_PROXY_TARGET_ID,
                 properties.getProperty(CoreSession.IMPORT_PROXY_TARGET_ID));
         document.putContextData(CoreSession.IMPORT_PROXY_VERSIONABLE_ID,
                 properties.getProperty(CoreSession.IMPORT_PROXY_VERSIONABLE_ID));
 
         return document;
     }
 
 }
