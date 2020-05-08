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
 package org.nuxeo.ecm.platform.replication.exporter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import org.nuxeo.ecm.core.api.Blob;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentException;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.api.VersionModel;
 import org.nuxeo.ecm.core.api.facet.VersioningDocument;
 import org.nuxeo.ecm.core.io.DocumentTranslationMap;
 import org.nuxeo.ecm.core.io.ExportedDocument;
 import org.nuxeo.ecm.core.io.impl.plugins.XMLDirectoryWriter;
 import org.nuxeo.ecm.core.schema.types.primitives.DateType;
 import org.nuxeo.ecm.platform.replication.common.ReplicationConstants;
 
 /**
  * Extends XMLDirectoryWriter to provide additional metadata .
 * 
  * @author cpriceputu@nuxeo.com
 * 
  */
 public class ReplicationWriter extends XMLDirectoryWriter {
     private static final Logger log = Logger.getLogger(ReplicationWriter.class);
 
     private static final Object mutex = new Object();
 
     private CoreSession session = null;
 
     public ReplicationWriter(File file, CoreSession session) throws IOException {
         super(file);
         this.session = session;
     }
 
     @Override
     public DocumentTranslationMap write(ExportedDocument doc)
             throws IOException {
 
         File parent = new File(getDestination().toString(),
                 ReplicationConstants.DOCUMENTARY_BASE_LOCATION_NAME);
         try {
             DocumentModel document = session.getDocument(new IdRef(doc.getId()));
             OutputFormat format = OutputFormat.createPrettyPrint();
             if (!document.isVersion()) {
                 parent = new File(parent,
                         ReplicationConstants.USUAL_DOCUMENTS_LOCATION_NAME);
                 parent = new File(parent, doc.getPath().toString());
             } else {
                 parent = new File(parent,
                         ReplicationConstants.VERSIONS_LOCATION_NAME);
                 parent = new File(parent, document.getId());
             }
             synchronized (mutex) {
                 parent.mkdirs();
             }
 
             XMLWriter writer = new XMLWriter(new FileOutputStream(new File(
                     parent, "document.xml")), format);
             writer.write(doc.getDocument());
             writer.close();
             Map<String, Blob> blobs = doc.getBlobs();
             for (Map.Entry<String, Blob> entry : blobs.entrySet()) {
                 entry.getValue().transferTo(new File(parent, entry.getKey()));
             }
 
             // write external documents
             for (Map.Entry<String, Document> entry : doc.getDocuments().entrySet()) {
 
                 writer = new XMLWriter(new FileOutputStream(new File(parent,
                         entry.getKey() + ".xml")), format);
                 writer.write(entry.getValue());
                 writer.close();
             }
 
             Properties metadata = getDocumentMetadata(session, document);
             File metadataFile = new File(parent, "metadata.properties");
             metadata.store(new FileOutputStream(metadataFile),
                     "Document Metadata");
         } catch (Exception e) {
             log.error(parent.getAbsolutePath() + " missing!", e);
            throw new IOException(e.getMessage());
         }
         return null;
     }
 
     public static Properties getDocumentMetadata(CoreSession documentManager,
             DocumentModel document) throws ClientException, DocumentException {
         Properties props = new Properties();
         DocumentRef ref = document.getRef();
         if (document.isProxy()) {
             DocumentModel version = documentManager.getSourceDocument(ref);
             DocumentModel sourceDocument = documentManager.getSourceDocument(version.getRef());
             props.setProperty(CoreSession.IMPORT_PROXY_TARGET_ID,
                     version.getId() == null ? "" : version.getId());
             props.setProperty(CoreSession.IMPORT_PROXY_VERSIONABLE_ID,
                     sourceDocument.getId() == null ? ""
                             : sourceDocument.getId());
         } else if (document.isVersion()) {
             DocumentModel sourceDocument = documentManager.getSourceDocument(ref);
             props.setProperty(CoreSession.IMPORT_VERSION_VERSIONABLE_ID,
                     sourceDocument.getId() == null ? ""
                             : sourceDocument.getId());
             props.setProperty(CoreSession.IMPORT_VERSION_LABEL,
                     document.getVersionLabel() == null ? ""
                             : document.getVersionLabel());
             List<VersionModel> versions = documentManager.getVersionsForDocument(sourceDocument.getRef());
             for (VersionModel version : versions) {
                 // add version description
                 props.setProperty(CoreSession.IMPORT_VERSION_DESCRIPTION,
                         version.getDescription() == null ? ""
                                 : version.getDescription());
                 // add version creation date
                 props.setProperty(
                         CoreSession.IMPORT_VERSION_CREATED,
                         new DateType().encode(version.getCreated()) == null ? ""
                                 : new DateType().encode(version.getCreated()));
             }
             VersioningDocument docVer = document.getAdapter(VersioningDocument.class);
             String minorVer = docVer.getMinorVersion().toString();
             String majorVer = docVer.getMajorVersion().toString();
             props.setProperty(CoreSession.IMPORT_VERSION_MAJOR,
                     majorVer == null ? "" : majorVer);
             props.setProperty(CoreSession.IMPORT_VERSION_MINOR,
                     minorVer == null ? "" : minorVer);
         } else {
             props.setProperty(CoreSession.IMPORT_LOCK,
                     document.getLock() == null ? "" : document.getLock());
             if (document.isVersionable()) {
                 props.setProperty(CoreSession.IMPORT_CHECKED_IN,
                         Boolean.FALSE.toString());
                 // add the id of the last version, which represents the base for
                 // the current state of the document
                 DocumentModel version = documentManager.getLastDocumentVersion(ref);
                 if ((version != null)
                         && version.getId().equals(document.getId())) {
                     props.setProperty(CoreSession.IMPORT_BASE_VERSION_ID,
                             version.getId() == null ? "" : version.getId());
                 }
                 VersioningDocument docVer = document.getAdapter(VersioningDocument.class);
                 if (docVer != null) {
                     String minorVer = docVer.getMinorVersion().toString();
                     String majorVer = docVer.getMajorVersion().toString();
                     // add major version
                     props.setProperty(CoreSession.IMPORT_VERSION_MAJOR,
                             majorVer == null ? "" : majorVer);
                     // add minor version
                     props.setProperty(CoreSession.IMPORT_VERSION_MINOR,
                             minorVer == null ? "" : minorVer);
                 }
             }
         }
 
         props.setProperty(CoreSession.IMPORT_LIFECYCLE_STATE,
                 document.getCurrentLifeCycleState() == null ? ""
                         : document.getCurrentLifeCycleState());
         props.setProperty(CoreSession.IMPORT_LIFECYCLE_POLICY,
                 document.getLifeCyclePolicy() == null ? ""
                         : document.getLifeCyclePolicy());
         return props;
     }
 }
