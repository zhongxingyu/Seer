 package fr.cg95.cvq.service.document;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.junit.Test;
 
 import fr.cg95.cvq.business.document.ContentType;
 import fr.cg95.cvq.business.document.DepositOrigin;
 import fr.cg95.cvq.business.document.DepositType;
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.document.DocumentBinary;
 import fr.cg95.cvq.business.document.DocumentState;
 import fr.cg95.cvq.business.document.DocumentType;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqModelException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.PermissionException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.util.development.BusinessObjectsFactory;
 
 /**
  * The tests for the {@link IDocumentService document service}.
  *
  * @author bor@zenexity.fr
  */
 public class DocumentServiceTest extends DocumentTestCase {
 
     @Test
     public void testAll()
         throws CvqException, java.io.IOException, java.io.FileNotFoundException {
 
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
 
         // ensure document digitalization is enabled
         assertTrue(SecurityContext.getCurrentSite().isDocumentDigitalizationEnabled());
         
        // ensure all document types have been bootstrapped
         List<DocumentType> allDocumentTypes = documentTypeService.getAllDocumentTypes();
        assertEquals(41, allDocumentTypes.size());
         
         SecurityContext.setCurrentEcitizen(fake.responsibleId);
 
         Individual anIndividual = userSearchService.getAdults(fake.id).get(0);
 
         // create a document
         Document doc = BusinessObjectsFactory.gimmeDocument("", DepositOrigin.ECITIZEN, DepositType.PC, 
                     documentTypeService.getDocumentTypeByType(IDocumentTypeService.IDENTITY_RECEIPT_TYPE));
         doc.setDepositId(anIndividual.getId());
         doc.setHomeFolderId(fake.id);
         doc.setIndividualId(anIndividual.getId());
         Long docId = documentService.create(doc);
 
         // add binary data
         try {
             documentService.addPage(docId, getImageDocumentBinary());
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
 
         // and another one ...
         try {
             documentService.addPage(docId, getImageDocumentBinary());
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
         
         continueWithNewTransaction();
         
         // check the document and its two binary have been successfully added ...
         // ... to the home folder
         List<Document> documentsList = documentService.getHomeFolderDocuments(fake.id, -1);
         assertEquals("Bad number of associated documents on home folder", 1, documentsList.size());
         List<DocumentBinary> docBinarys = documentService.getById(docId).getDatas();
         assertEquals("Bad number of associated data on document",2, doc.getDatas().size());
 
         // ... and to the individual
         documentsList = documentService.getIndividualDocuments(anIndividual.getId());
         assertEquals("Bad number of associated documents on individual", 1, documentsList.size());
         try {
             documentsList = documentService.getIndividualDocuments(new Long(0));
             fail("should have thrown an exception");
         } catch (PermissionException pe) {
             // that was expected
         }
         
         // modify a page
         try {
             documentService.modifyPage(docId, 0, getImageDocumentBinary());
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
 
         // remove a page
         doc.getDatas().remove(1);
         assertEquals("Bad number of associated data on document", 1, doc.getDatas().size());
         assertNotNull("Could find page", doc.getDatas().get(0));
 
         // try to retrieve the list of identity pieces for home folder
         DocumentType docType =
             documentTypeService.getDocumentTypeByType(IDocumentTypeService.IDENTITY_RECEIPT_TYPE);
         documentsList =
             documentService.getProvidedDocuments(docType, fake.id, null);
         assertEquals("Bad number of docs for home folder (1)", 1, documentsList.size());
         // and try other successful and unsuccessful searches among provided documents
         documentsList =
             documentService.getProvidedDocuments(docType, fake.id, anIndividual.getId());
         assertEquals("Bad number of docs for home folder and individual", 1, documentsList.size());
         docType =
             documentTypeService.getDocumentTypeByType(IDocumentTypeService.MEDICAL_CERTIFICATE_TYPE);
         documentsList =
             documentService.getProvidedDocuments(docType, fake.id, null);
         assertEquals("Bad number of docs for home folder (2)", 0, documentsList.size());
 
         // test end validity durations by creating different sort of doc types
         // based on example data from $BASE_DIR/db/init_ref_data.sql
 
         // ... a permanently durable
         doc = BusinessObjectsFactory.gimmeDocument("", DepositOrigin.ECITIZEN, DepositType.PC, 
                 documentTypeService.getDocumentTypeByType(IDocumentTypeService.IDENTITY_RECEIPT_TYPE));
         doc.setDepositId(anIndividual.getId());
         doc.setHomeFolderId(fake.id);
         doc.setIndividualId(anIndividual.getId());
         documentService.create(doc);
 
         // ... a 3-year valid
         doc = BusinessObjectsFactory.gimmeDocument("", DepositOrigin.ECITIZEN, DepositType.PC, 
                 documentTypeService.getDocumentTypeByType(IDocumentTypeService.DOMICILE_RECEIPT_TYPE));
         doc.setDepositId(anIndividual.getId());
         doc.setHomeFolderId(fake.id);
         documentService.create(doc);
 
         // ... a 2-month valid
         doc = BusinessObjectsFactory.gimmeDocument("", DepositOrigin.ECITIZEN, DepositType.PC, 
                 documentTypeService.getDocumentTypeByType(IDocumentTypeService.ID_CARD_LOSS_DECLARATION_TYPE));
         doc.setDepositId(anIndividual.getId());
         doc.setHomeFolderId(fake.id);
         Long docId3 = documentService.create(doc);
 
         // ... an end-of-the-year valid
         doc = new Document();
         doc.setDepositId(anIndividual.getId());
         doc.setDepositOrigin(DepositOrigin.ECITIZEN);
         doc.setDepositType(DepositType.PC);
         doc.setDocumentType(documentTypeService.getDocumentTypeByType(IDocumentTypeService.TAXES_NOTIFICATION_TYPE));
         doc.setHomeFolderId(fake.id);
         Long docId4 = documentService.create(doc);
 
         // ... an end-of-the-school-year valid
         doc = BusinessObjectsFactory.gimmeDocument("", DepositOrigin.ECITIZEN, DepositType.PC, 
                 documentTypeService.getDocumentTypeByType(IDocumentTypeService.VACATING_CERTIFICATE_TYPE));
         doc.setDepositId(anIndividual.getId());
         doc.setHomeFolderId(fake.id);
         documentService.create(doc);
 
         // delete a document
         documentService.delete(docId3);
         
         continueWithNewTransaction();
 
         // test modifications on a document
         Document docToModify = documentService.getById(docId4);
         doc.setDepositType(DepositType.TERMINAL);
         doc.setAgentNote("Quelle belle PJ");
         Calendar calendar = new GregorianCalendar();
         Date currentDate = new Date();
         calendar.setTime(currentDate);
         calendar.add(Calendar.MONTH, 3);
         doc.setEndValidityDate(calendar.getTime());
         documentService.modify(docToModify);
         docToModify = documentService.getById(docId4);
         assertNotNull("Argh, where my f****** document has gone ??!");
         assertEquals(doc.getAgentNote(), "Quelle belle PJ");
 
         // hmm ? just a test :-)
         try {
             documentService.modify(null);
             fail("should have thrown an exception");
         } catch (PermissionException pe) {
             // that was expected
         }
         
         // retrieve all known document types
         allDocumentTypes = documentTypeService.getAllDocumentTypes();
         assertNotNull(allDocumentTypes);
         
         SecurityContext.setCurrentEcitizen(fake.responsibleId);
         int count = documentService.searchCount(null);
         assertNotSame(count, 0);
         
         List<Document> docs = new ArrayList<Document>();
         Hashtable<String, Object> params = new Hashtable<String, Object>();
         params.put("documentType", documentTypeService.getDocumentTypeByType(
                 IDocumentTypeService.TAXES_NOTIFICATION_TYPE));
         
         docs = documentService.search(params,-1,-1);
         assertNotSame(docs.size(), 0);
         
         params = new Hashtable<String, Object>();
         params.put("homeFolderId", fake.id);
         
         count = documentService.searchCount(params);
         docs = documentService.search(params,-1,-1);
         assertEquals(docs.size(), count);
     }
     
     @Test
     public void testCreate() throws CvqException {
         
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
         SecurityContext.setCurrentEcitizen(fake.responsibleId);
         
         continueWithNewTransaction();
         
         Individual individual = userSearchService.getHomeFolderResponsible(fake.id);
         DocumentType documentType =
             documentTypeService.getDocumentTypeByType(IDocumentTypeService.ADOPTION_JUDGMENT_TYPE);
         
         Document document = 
             BusinessObjectsFactory.gimmeDocument("", null, null, documentType);
         document.setHomeFolderId(fake.id);
         document.setIndividualId(new Long(individual.getId().longValue()));
         documentService.create(document);
         Long documentId = document.getId();
    
         try {
             documentService.check(documentId, null);
             fail("should have thrown an exception");
         } catch (PermissionException pe) {
             // that was expected
         }
 
         document = BusinessObjectsFactory.gimmeDocument("", null, null, documentType);
         document.setHomeFolderId(Long.valueOf("0"));
         try {
             documentService.create(document);
             fail("should have thrown an exception");
         } catch (PermissionException pe) {
             // that was expected
         }
 
         continueWithNewTransaction();
 
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.BACK_OFFICE_CONTEXT);
         SecurityContext.setCurrentAgent(agentNameWithCategoriesRoles);
         
         documentService.check(documentId, null);
         documentService.getById(documentId);
     }
 
     @Test
     public void testHomeFolderDeleteEvent() throws CvqException {
         
         FakeHomeFolder fake = new FakeHomeFolder(false);
         
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
         SecurityContext.setCurrentEcitizen(fake.responsibleId);
 
         DocumentType documentType =
             documentTypeService.getDocumentTypeByType(IDocumentTypeService.ADOPTION_JUDGMENT_TYPE);
         
         Document document = BusinessObjectsFactory.gimmeDocument("", null, null, documentType);
         document.setHomeFolderId(fake.id);
         documentService.create(document);
    
         continueWithNewTransaction();
 
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.BACK_OFFICE_CONTEXT);
         SecurityContext.setCurrentAgent(agentNameWithCategoriesRoles);
         
         userWorkflowService.delete(fake.id);
         
         continueWithNewTransaction();
         
         List<Document> documents = 
             documentService.getHomeFolderDocuments(fake.id, -1);
         assertTrue(documents.isEmpty());
     }
 
     @Test
     public void testIndividualDeleteEvent() throws CvqException {
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
         SecurityContext.setCurrentEcitizen(fake.responsibleId);
 
         DocumentType documentType =
             documentTypeService.getDocumentTypeByType(IDocumentTypeService.ADOPTION_JUDGMENT_TYPE);
         
         Document document = BusinessObjectsFactory.gimmeDocument("", null, null, documentType);
         document.setHomeFolderId(fake.id);
         document.setIndividualId(fake.womanId);
         documentService.create(document);
    
         continueWithNewTransaction();
 
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.BACK_OFFICE_CONTEXT);
         SecurityContext.setCurrentAgent(agentNameWithCategoriesRoles);
         
         userWorkflowService.delete(userSearchService.getById(fake.womanId));
         
         continueWithNewTransaction();
         
         assertTrue(documentService.getIndividualDocuments(fake.womanId).isEmpty());
     }
 
     @Test
     public void testUnauthenticatedUseCases() throws CvqException, IOException {
 
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
 
         DocumentType documentType =
             documentTypeService.getDocumentTypeByType(IDocumentTypeService.ADOPTION_JUDGMENT_TYPE);
         Document document = new Document(null, "coucou", documentType, DocumentState.PENDING);
         documentService.create(document);
 
         continueWithNewTransaction();
 
         document = documentService.getById(document.getId());
         assertEquals("coucou", document.getEcitizenNote());
 
         document.setEcitizenNote("hello buddy");
         documentService.modify(document);
 
         continueWithNewTransaction();
 
         document = documentService.getById(document.getId());
         assertEquals("hello buddy", document.getEcitizenNote());
 
         try {
             documentService.addPage(document.getId(), getImageDocumentBinary());
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
 
         continueWithNewTransaction();
 
         document = documentService.getById(document.getId());
         try {
             documentService.modifyPage(document.getId(), 0, new byte[]{});
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
 
         continueWithNewTransaction();
 
         document = documentService.getById(document.getId());
         assertEquals(1, document.getDatas().size());
 
         documentService.deletePage(document.getId(), 0);
 
         continueWithNewTransaction();
 
         document = documentService.getById(document.getId());
         assertEquals(0, document.getDatas().size());
 
         documentService.delete(document.getId());
 
         continueWithNewTransaction();
         // TODO: test deletion
     }
     
     @Test
     public void testDocumentAddPage() throws CvqException, IOException {
        
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
         
         // create document
         DocumentType docType =
             documentTypeService.getDocumentTypeByType(IDocumentTypeService.IDENTITY_RECEIPT_TYPE);
         Document doc = BusinessObjectsFactory.gimmeDocument("", null, null, docType);
         Long docId = documentService.create(doc);
         
         continueWithNewTransaction();
         
         // first : add binaries with allowed content type (image)
         try {
             documentService.addPage(docId, getImageDocumentBinary());
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
         try {
             documentService.addPage(docId, getImageDocumentBinary());
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
         
         continueWithNewTransaction();
         
         // tests
         doc = documentService.getById(docId);
         assertEquals("There was a problem during add page to document",2,doc.getDatas().size());
         assertEquals("Problems with the content type of binaries",ContentType.JPEG,doc.getDatas().get(0).getContentType());
         
         // remove all binarie from document
         doc.getDatas().clear();
         assertEquals("There are binaries in document", true, doc.getDatas().isEmpty());
         
         continueWithNewTransaction();
         
         // second : add binaries with allowed content type (pdf)
         try {
             documentService.addPage(docId, getPdfDocumentBinary());
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
         try {
             documentService.addPage(docId, getPdfDocumentBinary());
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
         
         continueWithNewTransaction();
         
         // tests
         doc = documentService.getById(docId);
         assertEquals("There was a problem during add page to document",2,doc.getDatas().size());
         assertEquals("Problems with the content type of binaries",ContentType.PDF,doc.getDatas().get(0).getContentType());
         
         continueWithNewTransaction();
         
         // third : add a binary with a content type allowed but different from binaries in document
         try {
             documentService.addPage(docId, getImageDocumentBinary());
             fail("We must have an error");
         } catch (CvqModelException cme) {
          // that was expected
         }
         
         continueWithNewTransaction();
         
         // tests
         doc = documentService.getById(docId);
         assertEquals("The binary was added whereas it haven't a good content type",2,doc.getDatas().size());
         
         continueWithNewTransaction();
         
         // fourth : add a binary with content type not allowed
         try {
             documentService.addPage(docId, getBadTypeDocumentBinary());
             fail("We must have an error");
         } catch (CvqModelException cme) {
          // that was expected
         }
         
         continueWithNewTransaction();
         
         // tests
         doc = documentService.getById(docId);
         assertEquals("The binary was added whereas it haven't a good content type",2,doc.getDatas().size());
         
         continueWithNewTransaction();
         
         // fifth : test if the preview has been created for all binaries
         for (DocumentBinary bin : doc.getDatas()) {
             assertNotNull("The preview is not created",bin.getPreview());
         }
     }
     
     @Test
     public void testMergeBinariesToPdf() throws CvqObjectNotFoundException, CvqException, IOException {
         
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
         
         //create document
         DocumentType docType = 
             documentTypeService.getDocumentTypeByType(IDocumentTypeService.IDENTITY_RECEIPT_TYPE);
         Document doc = BusinessObjectsFactory.gimmeDocument("", null, null, docType);
         Long docId = documentService.create(doc);
         
 
         //first : add binaries encrypted
 
         File filePdf = getResourceFile("encrypted.pdf");
         byte[] dataPdf = new byte[(int) filePdf.length()];
         FileInputStream fis = new FileInputStream(filePdf);
         fis.read(dataPdf);
         try {
             documentService.addPage(docId, dataPdf);
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
 
         try {
             documentService.addPage(docId, dataPdf);
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
 
         continueWithNewTransaction();
 
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.BACK_OFFICE_CONTEXT);
         SecurityContext.setCurrentAgent(agentNameWithCategoriesRoles);
 
         //change state of doc
         documentService.updateDocumentState(docId, DocumentState.VALIDATED, null, null);
         
         continueWithNewTransaction();
 
         //tests
         doc = documentService.getById(docId);
         assertEquals("The merge worked whereas the binaries were encrypted",2,doc.getDatas().size());
         
         //remove all binaries from document
         doc.getDatas().clear();
         
         //second : add pdf binaries not encrypted
         try {
             documentService.addPage(docId, getPdfDocumentBinary());
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
         try {
             documentService.addPage(docId, getPdfDocumentBinary());
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
         
         continueWithNewTransaction();
 
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.BACK_OFFICE_CONTEXT);
         SecurityContext.setCurrentAgent(agentNameWithCategoriesRoles);
 
         //change state of doc
         documentService.updateDocumentState(docId, DocumentState.VALIDATED, null, null);
         
         continueWithNewTransaction();
 
         //tests
         doc = documentService.getById(docId);
         assertEquals("The merge didn't work",1,doc.getDatas().size());
         assertEquals("Content type is not equal to PDF",ContentType.PDF, doc.getDatas().get(0).getContentType());
         
         //remove all binaries from document
         doc.getDatas().clear();
         
         //third : add image binary
         filePdf = getResourceFile("test.jpg");
         dataPdf = new byte[(int) filePdf.length()];
         fis = new FileInputStream(filePdf);
         fis.read(dataPdf);
         try {
             documentService.addPage(docId, dataPdf);
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
         
         try {
             documentService.addPage(docId, dataPdf);
         } catch (CvqModelException cme) {
             fail("thrown cvq model exception : " + cme.getI18nKey());
         }
         
         continueWithNewTransaction();
 
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.ADMIN_CONTEXT);
 
         //change state of doc
         documentService.mergeDocumentBinary(documentService.getById(docId));
         
         continueWithNewTransaction();
 
         //tests
         doc = documentService.getById(docId);
         assertEquals("The merge didn't work",1,doc.getDatas().size());
         assertEquals("Content type is not equal to PDF",ContentType.PDF, doc.getDatas().get(0).getContentType());
     }
     
     @Test
     public void mergeImageToPdf() throws CvqException, IOException {
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.ADMIN_CONTEXT);
         
         Long docId = gimmeImageDocument();
         documentService.mergeDocumentBinary(documentService.getById(docId));
         
         //test
         Document doc = documentService.getById(docId);
         assertEquals("The merge didn't work",1, doc.getDatas().size());
         assertEquals("The content type is not available", ContentType.PDF, doc.getDatas().get(0).getContentType());
         
     }
 }
