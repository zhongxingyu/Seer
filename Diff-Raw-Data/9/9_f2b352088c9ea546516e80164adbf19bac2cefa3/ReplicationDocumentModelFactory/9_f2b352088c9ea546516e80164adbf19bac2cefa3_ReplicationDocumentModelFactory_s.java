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
 
 import static org.nuxeo.ecm.platform.replication.common.ReplicationConstants.DOCUMENTARY_BASE_LOCATION_NAME;
 import static org.nuxeo.ecm.platform.replication.common.ReplicationConstants.METADATA_FILE_NAME;
 import static org.nuxeo.ecm.platform.replication.common.ReplicationConstants.VERSIONS_LOCATION_NAME;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.nuxeo.common.utils.Path;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
 import org.nuxeo.ecm.core.api.security.ACE;
 import org.nuxeo.ecm.core.api.security.ACL;
 import org.nuxeo.ecm.core.api.security.ACP;
 import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
 import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
 import org.nuxeo.ecm.core.io.DocumentWriter;
 import org.nuxeo.ecm.core.io.ExportConstants;
 import org.nuxeo.ecm.core.io.ExportedDocument;
 import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
 import org.nuxeo.ecm.platform.importer.base.TxHelper;
 import org.nuxeo.ecm.platform.importer.factories.ImporterDocumentModelFactory;
 import org.nuxeo.ecm.platform.importer.source.SourceNode;
 import org.nuxeo.ecm.platform.replication.common.StatusListener;
 import org.nuxeo.ecm.platform.replication.importer.reporter.ImporterReporter;
 import org.nuxeo.runtime.services.streaming.FileSource;
 
 /**
  * Implements the document model factory as for replication needs. It core
  * imports document and sets the document properties.
  *
  * @author rux
  *
  */
 public class ReplicationDocumentModelFactory implements
         ImporterDocumentModelFactory {
 
     private static final Log log = LogFactory.getLog(ReplicationDocumentModelFactory.class);
 
     protected CoreSession session;
 
     protected SourceNode fileNode;
 
     protected final boolean importProxies;
 
     protected StatusListener listener;
 
     protected DocumentXmlTransformer xmlTransformer;
 
     protected DocumentTypeSelector typeSelector;
 
     public ReplicationDocumentModelFactory(StatusListener listener,
             boolean importProxies) {
         this.listener = listener;
         this.importProxies = importProxies;
         xmlTransformer = null;
         typeSelector = null;
     }
 
     @Override
     public DocumentModel createFolderishNode(CoreSession session,
             DocumentModel parent, SourceNode node) throws Exception {
         if (node == null) {
             // unexpected, but possible when the folder is not created but
             // passed away
             log.warn("Trying to create null folderish node!");
             return null;
         }
         if (session == null) {
             // this is really unexpected
             log.error("Null session, but trying to create folderish node "
                     + node.getName());
             return null;
         }
         fileNode = node;
         this.session = session;
         return importDocument();
     }
 
     @Override
     public DocumentModel createLeafNode(CoreSession session,
             DocumentModel parent, SourceNode node) throws Exception {
         if (node == null) {
             // unexpected, but possible when the folder is not created but
             // passed away
             log.warn("Trying to create null leaf node!");
             return null;
         }
         if (session == null) {
             // this is really unexpected
             log.error("Null session, but trying to create leaf node "
                     + node.getName());
             return null;
         }
         fileNode = node;
         this.session = session;
         return importDocument();
     }
 
     @Override
     public boolean isTargetDocumentModelFolderish(SourceNode node) {
         if (node == null) {
             // kind of unexpected, but possible when the parent is not created
             log.warn("Null node: is it folderish? No.");
             return false;
         }
         return node.isFolderish();
     }
 
     protected void removeChildrenDepthFirst(DocumentRef ref)
             throws ClientException {
         DocumentModelList children = session.getChildren(ref);
         for (DocumentModel child : children) {
             removeChildrenDepthFirst(child.getRef());
             session.removeDocument(child.getRef());
         }
     }
 
     protected DocumentModel cleanUpRoot() throws ClientException {
         DocumentModel root = session.getRootDocument();
         removeChildrenDepthFirst(root.getRef());
         session.save();
         TxHelper txHelper = new TxHelper();
         txHelper.grabCurrentTransaction(null);
         txHelper.commitOrRollbackTransaction();
         log.debug("starts new transaction");
         txHelper.beginNewTransaction();
         return root;
     }
 
     protected DocumentModel importDocument() {
         if (fileNode == null || session == null) {
             // shouldn't be here
             log.error("Can't import: nulls");
             return null;
         }
         String documentLocation = fileNode.getName();
         if (documentLocation.endsWith(DOCUMENTARY_BASE_LOCATION_NAME
                 + File.separator + VERSIONS_LOCATION_NAME)) {
             // the directory /Versions is only container
             return null;
         }
         ExportedDocument xdoc = new ExportedDocumentImpl();
         try {
             xdoc.setDocument(ImporterDocumentCreator.loadXML(new File(
                     documentLocation + File.separator + "document.xml")));
         } catch (Exception e) {
             // don't log twice: when proxies is twice
             if (!importProxies) {
                 ImporterReporter.getInstance().incrementDocumentNumber();
                log.error("Didn't find xml for" + documentLocation);
                 ImporterReporter.getInstance().logDocumentImport(
                         documentLocation, "xml file is missing or corrupt");
             }
             return null;
         }
         Properties properties = getPropertiesFile();
         if (properties.isEmpty()) {
             if (!importProxies) {
                 ImporterReporter.getInstance().incrementDocumentNumber();
                 log.error("Didn't find metadata for " + documentLocation);
                 ImporterReporter.getInstance().logDocumentImport(
                         documentLocation, "metadata file is missing");
             }
             return null;
         }
         boolean isProxy = ImporterDocumentCreator.isProxy(properties);
         if ((importProxies && !isProxy) || (!importProxies && isProxy)) {
             // not to import
             if (importProxies) {
                 // must still return a folder for the recursion to continue
                 try {
                     return session.getDocument(new IdRef(xdoc.getId()));
                 } catch (Exception e) {
                     log.error("Couldn't refetch " + xdoc.getId(), e);
                     return null;
                 }
             } else {
                 return null;
             }
         }
         // can't simply have it at the start of the method as the document
         // should not be to import because the mixed condition above
         ImporterReporter.getInstance().incrementDocumentNumber();
         // filter the not wanted types
         if (typeSelector != null) {
             String docType = xdoc.getType();
             if (!typeSelector.accept(docType)) {
                 // document to not be imported
                 log.info("Document " + documentLocation + " of type " + docType
                         + " rejected");
                 ImporterReporter.getInstance().logTypeBlocked(documentLocation,
                         docType);
                 return null;
             }
         }
         // offer a chance to transform the document properties
         if (xmlTransformer != null) {
             try {
                 Document transformedDocument = xmlTransformer.transform(xdoc.getDocument());
                 if (transformedDocument != null) {
                     xdoc.setDocument(transformedDocument);
                 }
             } catch (Exception e) {
                 // don't crash in customized code
                 log.warn("Transformation failed", e);
                 ImporterReporter.getInstance().logFailUpdate(documentLocation,
                         e.getMessage());
             }
         }
         // create document
         DocumentModel documentModel = null;
         if (!xdoc.getType().equals("Root")) {
             Path path = new Path(
                     "/"
                             + ((Element) xdoc.getDocument().selectNodes(
                                     "//system/path").get(0)).getText());
             xdoc.setPath(new Path(path.lastSegment()));
             path = path.removeLastSegments(1);
             try {
                 documentModel = coreImportDocument(xdoc, path.toString(),
                         properties);
             } catch (Exception e) {
                 log.error("Couldn't clone the documents in repository", e);
                 ImporterReporter.getInstance().logDocumentImport(
                         documentLocation, e.getMessage());
                 return null;
             }
             try {
                 loadSystemInfo(documentModel, xdoc.getDocument());
             } catch (Exception e) {
                 log.warn("Couldn't set ACL for " + documentLocation, e);
                 ImporterReporter.getInstance().logACLFailed(documentLocation);
                 // no acl, but doc is there, continue
             }
         } else {
             try {
                 if (importProxies) {
                     return session.getRootDocument();
                 } else {
                     return cleanUpRoot();
                 }
             } catch (Exception e) {
                 log.fatal("Couldn't import the root: import fails", e);
                 return null;
             }
         }
 
         sendStatus(StatusListener.DOC_PROCESS_SUCCESS, documentModel);
 
         // update document properties, basically set up the blobs and update
         DocumentWriter writer = new ReplicationDocumentModelWriter(session,
                 documentModel, 1);
         File[] blobFiles = new File(documentLocation).listFiles(new FilenameFilter() {
             @Override
             public boolean accept(File dir, String name) {
                 return name.contains(".blob");
             }
         });
         if (blobFiles.length > 0) {
             // set all the blobs
             for (File blobFile : blobFiles) {
                 xdoc.putBlob(blobFile.getName(), new StreamingBlob(
                         new FileSource(blobFile)));
             }
         }
         try {
             writer.write(xdoc);
         } catch (Exception e) {
             log.error("Couldn't update the document structure for "
                     + documentLocation, e);
             ImporterReporter.getInstance().logDocumentStructure(
                     documentLocation);
         }
         return documentModel;
     }
 
     /**
      * Imports the document in repository. Actually only if it is an usual
      * document, otherwise it only prepares the document for being imported. The
      * actual import of the versions and proxies occurs after the schema are
      * loaded in the DocumetnWriter part. The usual documents need to be
      * imported in order to apply the ACLs.
      *
      * @param doc
      * @param parentPath
      * @param properties
      * @return
      * @throws ClientException
      */
     protected DocumentModel coreImportDocument(ExportedDocument doc,
             String parentPath, Properties properties) throws ClientException {
         return ImporterDocumentCreator.importDocument(session, doc.getType(),
                 doc.getId(), doc.getPath().toString(), parentPath, properties);
     }
 
     /**
      * Utility method used to retrieve the .properties file from a source
      * node.This file contains data of a document that will be used in the
      * process of import.
      */
     private Properties getPropertiesFile() {
         Properties properties = new Properties();
         FileInputStream fis = null;
         try {
             fis = new FileInputStream(fileNode.getName() + File.separator
                     + METADATA_FILE_NAME);
             properties.load(fis);
         } catch (IOException e) {
             log.debug(String.format("Problems retrieving the %s file",
                     fileNode.getName() + METADATA_FILE_NAME));
         } finally {
             if (fis != null) {
                 try {
                     fis.close();
                 } catch (IOException e) {
                 }
             }
         }
         return properties;
     }
 
     public void setListener(StatusListener listener) {
         this.listener = listener;
     }
 
     public StatusListener getListener() {
         return listener;
     }
 
     protected void sendStatus(Object... params) {
         if (listener != null) {
             listener.onUpdateStatus(params);
         }
     }
 
     public void setDocumentXmlTransformer(DocumentXmlTransformer transformer) {
         xmlTransformer = transformer;
     }
 
     public void setDocumentTypeSelector(DocumentTypeSelector selector) {
         typeSelector = selector;
     }
 
     @SuppressWarnings("unchecked")
     protected void loadSystemInfo(DocumentModel docModel, Document doc)
             throws ClientException {
         Element system = doc.getRootElement().element(
                 ExportConstants.SYSTEM_TAG);
         Element accessControl = system.element(ExportConstants.ACCESS_CONTROL_TAG);
         if (docModel.getRef() == null || accessControl == null) {
             return;
         }
         Iterator<Element> it = accessControl.elementIterator(ExportConstants.ACL_TAG);
         while (it.hasNext()) {
             Element element = it.next();
             // import only the local acl
             if (ACL.LOCAL_ACL.equals(element.attributeValue(ExportConstants.NAME_ATTR))) {
                 // this is the local ACL - import it
                 List<Element> entries = element.elements();
                 int size = entries.size();
                 if (size > 0) {
                     ACP acp = new ACPImpl();
                     ACL acl = new ACLImpl(ACL.LOCAL_ACL);
                     acp.addACL(acl);
                     for (int i = 0; i < size; i++) {
                         Element el = entries.get(i);
                         String username = el.attributeValue(ExportConstants.PRINCIPAL_ATTR);
                         String permission = el.attributeValue(ExportConstants.PERMISSION_ATTR);
                         String grant = el.attributeValue(ExportConstants.GRANT_ATTR);
                         ACE ace = new ACE(username, permission,
                                 Boolean.parseBoolean(grant));
                         acl.add(ace);
                     }
                     acp.addACL(acl);
                     session.setACP(docModel.getRef(), acp, false);
                 }
             }
 
             if (ACL.INHERITED_ACL.equals(element.attributeValue(ExportConstants.NAME_ATTR))) {
                 // this is the local ACL - import it
                 List<Element> entries = element.elements();
                 int size = entries.size();
                 if (size > 0) {
                     ACP acp = new ACPImpl();
                     ACL acl = new ACLImpl(ACL.INHERITED_ACL);
                     acp.addACL(acl);
                     for (int i = 0; i < size; i++) {
                         Element el = entries.get(i);
                         String username = el.attributeValue(ExportConstants.PRINCIPAL_ATTR);
                         String permission = el.attributeValue(ExportConstants.PERMISSION_ATTR);
                         String grant = el.attributeValue(ExportConstants.GRANT_ATTR);
                         ACE ace = new ACE(username, permission,
                                 Boolean.parseBoolean(grant));
                         acl.add(ace);
                     }
                     acp.addACL(acl);
                     session.setACP(docModel.getRef(), acp, false);
                 }
             }
         }
     }
 
 }
