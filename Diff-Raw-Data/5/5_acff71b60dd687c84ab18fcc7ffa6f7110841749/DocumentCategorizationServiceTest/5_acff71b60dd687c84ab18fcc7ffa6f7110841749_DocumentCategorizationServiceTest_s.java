 /*
  * (C) Copyright 2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
  * Contributors:
  *     Nuxeo - initial API and implementation
  */
 
 package org.nuxeo.ecm.platform.categorization;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.After;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import org.nuxeo.ecm.core.api.Blob;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
 import org.nuxeo.ecm.core.event.EventService;
 import org.nuxeo.ecm.core.event.EventServiceAdmin;
 import org.nuxeo.ecm.core.storage.sql.SQLRepositoryTestCase;
 import org.nuxeo.ecm.platform.categorization.service.DocumentCategorizationService;
 import org.nuxeo.runtime.api.Framework;
 
 public class DocumentCategorizationServiceTest extends SQLRepositoryTestCase {
 
     DocumentCategorizationService service;
 
     DocumentModel f1;
 
     DocumentModel f2;
 
     DocumentModel file1;
 
     DocumentModel file2;
 
     DocumentModel note1;
 
     @Before
     public void setUp() throws Exception {
         super.setUp();
 
         // force H2 access to avoid weird H2 driver error at tearDown time in
         // tests that do not use the repository
         openSession();
 
         // need text extraction converters for blobs
         deployBundle("org.nuxeo.ecm.core.convert.api");
         deployBundle("org.nuxeo.ecm.core.convert");
         deployBundle("org.nuxeo.ecm.core.convert.plugins");
         deployBundle("org.nuxeo.ecm.core.storage.sql"); // event listener
 
         // deploy the categorization framework and the default contribs
         deployBundle("org.nuxeo.ecm.platform.categorization.service");
         deployBundle("org.nuxeo.ecm.platform.categorization.language");
         deployBundle("org.nuxeo.ecm.platform.categorization.coverage");
         deployBundle("org.nuxeo.ecm.platform.categorization.subjects");
 
         service = Framework.getService(DocumentCategorizationService.class);
         assertNotNull(service);
     }
 
     @After
     public void tearDown() throws Exception {
         if (session != null) {
             closeSession();
         }
         super.tearDown();
     }
 
     public void makeSomeDocuments() throws Exception {
         DocumentModel rootDocument = session.getRootDocument();
 
         // create roots
         f1 = session.createDocumentModel(rootDocument.getPathAsString(),
                 "folder-1", "Folder");
         f1.setPropertyValue("dc:title", "Folder 1");
         f2 = session.createDocumentModel(rootDocument.getPathAsString(),
                 "folder-2", "Folder");
         f2.setPropertyValue("dc:title", "Folder 2");
         f2.setPropertyValue(
                 "dc:description",
                 "This is the english description of a folder to check that"
                         + " folderish content do not get categorized by default.");
         f1 = session.createDocument(f1);
         f2 = session.createDocument(f2);
 
         // create sub folder contents
         file1 = session.createDocumentModel(f1.getPathAsString(), "file-1",
                 "File");
         file1.setPropertyValue("dc:title", "File 1: My trip to Asia");
         Blob b1 = StreamingBlob.createFromString(
                 "Last month, I took the plane for a trip around Asia and visited Saigon, "
                         + "Hanoi and several buddhist monasteries on the way. To move arounds"
                         + " we mainly used the railroads and buses. Traveling by train"
                         + " was an amazing experience. Unfortunately most hotels were"
                         + " overcrowded with tourists and we had to go backpacking to find"
                         + " quiet beaches away from the luxury resorts around Phu Quoc Island"
                         + " and Nha Trang.", "text/plain");
         b1.setEncoding("UTF-8");
         b1.setFilename("file-1.txt");
         file1.setPropertyValue("file:content", (Serializable) b1);
         file1 = session.createDocument(file1);
 
         file2 = session.createDocumentModel(f2.getPathAsString(), "file-2",
                 "File");
         file2.setPropertyValue("dc:title", "Fichier 2");
         Blob b2 = StreamingBlob.createFromString(
                 "Cette cha\u00EEne de charact\u00E8res est le contenu"
                         + " du document qui a pour titre 'Fichier 2'",
                 "text/plain");
         b2.setEncoding("UTF-8");
         b2.setFilename("file-2.txt");
         file2.setPropertyValue("file:content", (Serializable) b2);
         file2 = session.createDocument(file2);
 
         // TODO: add file-3 with a binary blob (e.g. Open Document) to check
         // that the text converters chain works as expected
 
         note1 = session.createDocumentModel(f1.getPathAsString(), "note-1",
                 "Note");
         note1.setPropertyValue("dc:title", "Note 1");
         note1.setPropertyValue("note:note",
                 "<html><body><p>Some <em>HTML</em> Note content</p></body></html>");
         note1 = session.createDocument(note1);
 
         session.save();
         closeSession();
         openSession();
     }
 
     @Test
     public void testDefaultCategorizationUsingDefaultEventListener()
             throws Exception {
         // let us create some documents
         makeSomeDocuments();
         closeSession();
 
         // wait for the default core event listener to finish
         Framework.getLocalService(EventService.class).waitForAsyncCompletion();
 
         // check the language categorization
         openSession();
         f2 = session.getDocument(f2.getRef());
         // folderish documents are not processed by default
         assertEquals(null, f2.getPropertyValue("dc:language"));
 
         file1 = session.getDocument(file1.getRef());
         file2 = session.getDocument(file2.getRef());
         note1 = session.getDocument(note1.getRef());
 
         assertEquals("en", file1.getPropertyValue("dc:language"));
         assertEquals("asia/Viet_Nam", file1.getPropertyValue("dc:coverage"));
         assertEquals(Arrays.asList("human sciences/history",
                 "daily life/tourism"),
                 Arrays.asList((String[]) file1.getPropertyValue("dc:subjects")));
 
         assertEquals("fr", file2.getPropertyValue("dc:language"));
         assertEquals("europe/France", file2.getPropertyValue("dc:coverage"));
 
         // TODO: handle the string content of Note document too
         assertEquals(null, note1.getPropertyValue("dc:language"));
     }
 
     @Test
     public void testDefaultCategorizationUsingServiceAPI() throws Exception {
         // disable the document categorization core event listener
         EventServiceAdmin eventServiceAdmin = Framework.getService(EventServiceAdmin.class);
         eventServiceAdmin.setListenerEnabledFlag(
                 "documentCategorizationSyncListener", false);
 
         // let us create some documents
         makeSomeDocuments();
         assertEquals(null, f2.getPropertyValue("dc:language"));
         assertEquals(null, file1.getPropertyValue("dc:language"));
         assertEquals(null, file1.getPropertyValue("dc:coverage"));
         assertEquals(null, file2.getPropertyValue("dc:language"));
         assertEquals(null, note1.getPropertyValue("dc:language"));
 
         // run the engine again, this time all the documents should be processed
         List<DocumentModel> updatedDocuments = service.updateCategories(Arrays.asList(
                 file1, file2, note1, f2));
         assertEquals(3, updatedDocuments.size());
 
         assertEquals("en", file1.getPropertyValue("dc:language"));
         assertEquals("asia/Viet_Nam", file1.getPropertyValue("dc:coverage"));
 
         assertEquals("fr", file2.getPropertyValue("dc:language"));
         assertEquals("europe/France", file2.getPropertyValue("dc:coverage"));
 
         // TODO: handle the string content of Note document too
         assertEquals(null, note1.getPropertyValue("dc:language"));
 
         // folderish documents are ignored by the categorizers
         assertEquals(null, f2.getPropertyValue("dc:language"));
     }
 
     @Test
     public void testDefaultCategorizationWithOverrides() throws Exception {
         // deploy the overrides
         deployContrib("org.nuxeo.ecm.platform.document.categorization.test",
                 "OSGI-INF/document-categorization-test-contrib.xml");
 
         // let us create some documents
         makeSomeDocuments();
         closeSession();
 
         // wait for the default core event listener to finish
         Framework.getLocalService(EventService.class).waitForAsyncCompletion();
 
         // check the language categorization
         openSession();
 
         file1 = session.getDocument(file1.getRef());
         file2 = session.getDocument(file2.getRef());
 
         assertEquals("en", file1.getPropertyValue("dc:rights"));
         assertEquals(null, file1.getPropertyValue("dc:language"));
         assertEquals("asia/Viet_Nam", file1.getPropertyValue("dc:coverage"));
 
         assertEquals("fr", file2.getPropertyValue("dc:rights"));
         assertEquals(null, file2.getPropertyValue("dc:language"));
         assertEquals("europe/France", file2.getPropertyValue("dc:coverage"));
 
         // undeploy the override
         undeployContrib("org.nuxeo.ecm.platform.document.categorization.test",
                 "OSGI-INF/document-categorization-test-contrib.xml");
 
         // trigger a modified document event
         file1.setPropertyValue("dc:title", "New title for File 1");
         file1 = session.saveDocument(file1);
 
         assertEquals("en", file1.getPropertyValue("dc:language"));
         assertEquals("asia/Viet_Nam", file1.getPropertyValue("dc:coverage"));
 
         assertEquals(null, file2.getPropertyValue("dc:language"));
         assertEquals("europe/France", file2.getPropertyValue("dc:coverage"));
 
         file2.setPropertyValue("dc:title",
                 "Ceci est le nouveau titre pour le fichier 2");
         file2 = session.saveDocument(file2);
 
         assertEquals("en", file1.getPropertyValue("dc:language"));
         assertEquals("asia/Viet_Nam", file1.getPropertyValue("dc:coverage"));
 
         assertEquals("fr", file2.getPropertyValue("dc:language"));
         assertEquals("europe/France", file2.getPropertyValue("dc:coverage"));
     }
 }
