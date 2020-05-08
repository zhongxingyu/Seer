 package uk.ac.ebi.arrayexpress.components;
 
 import net.sf.saxon.om.DocumentInfo;
 import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
 import uk.ac.ebi.arrayexpress.utils.DocumentTypes;
 import uk.ac.ebi.arrayexpress.utils.persistence.DocumentPersister;
 import uk.ac.ebi.arrayexpress.utils.persistence.PersistableDocument;
 import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;
 
 import java.io.File;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Map;
 
 /*
  * Copyright 2009-2010 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 public class DocumentContainer extends ApplicationComponent {
 
     private EnumMap<DocumentTypes, DocumentInfo> documents = new EnumMap<DocumentTypes, DocumentInfo>(DocumentTypes.class);
 
     //ToDo: it's better to use dependency injection
     private DocumentPersister documentPersister = new DocumentPersister();
 
     public DocumentContainer() {
         super("DocumentContainer");
     }
 
     /**
      * Implements ApplicaitonComponent.initialize()
      *
      * @throws Exception
      */
     public void initialize() throws Exception {
     }
 
     /**
      * Implements ApplicationComponent.terminate()
      *
      * @throws Exception
      */
     public void terminate() throws Exception {
     }
 
     /**
      * Checks if the document with given ID exists in the storage container
      *
      * @param documentId
      * @return
      */
     //ToDo: maybe it's better to use Enum instead of Strings
     public boolean hasDocument(String documentId) {
         return documents.containsKey(documentId);
     }
 
     /**
      * Returns Saxon TinyTree document object by its document ID
      *
      * @param documentId
      * @return
      */
     public DocumentInfo getDocument(String documentId) throws Exception {
        return getDocument(DocumentTypes.getInstanceByName(documentId));
     }
 
     /**
      * Puts Saxon TinyTree document associated with its ID
      *
      * @param documentId
      * @param document
      */
     public void putDocument(String documentId, DocumentInfo document) {
 
         putDocument(DocumentTypes.getInstanceByName(documentId), document);
 
     }
 
 
     /**
      * Returns Saxon TinyTree document object by its document ID
      *
      * @param documentId
      * @return
      */
     public DocumentInfo getDocument(DocumentTypes documentId) throws Exception {
         DocumentInfo document = documents.get(documentId);
         if (document == null || documentPersister.isEmpty(document)) {
 
            document = documentPersister.loadObject(documentId.getPersistenceDocumetLocation());
         }
         return document;
     }
 
     /**
      * Puts Saxon TinyTree document associated with its ID
      *
      * @param documentId
      * @param document
      */
     public void putDocument(DocumentTypes documentId, DocumentInfo document) {
         documents.put(documentId, document);
         // do index that doc as well
         SearchEngine search = (SearchEngine) getComponent("SearchEngine");
         search.getController().index(documentId.getTextName(), document);
 
        //Todo: persist document
        documentPersister.saveObject(document, documentId.getPersistenceDocumetLocation());
     }
 
      /**
      * Checks if the document with given ID exists in the storage container
      *
      * @param documentId
      * @return
      */
     //ToDo: maybe it's better to use Enum instead of Strings
     public boolean hasDocument(DocumentTypes documentId) {
         return documents.containsKey(documentId);
     }
 
 }
