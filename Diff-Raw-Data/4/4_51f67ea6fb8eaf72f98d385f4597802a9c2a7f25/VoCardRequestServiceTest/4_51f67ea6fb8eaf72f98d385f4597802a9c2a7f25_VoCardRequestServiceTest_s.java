 package fr.cg95.cvq.service.request.ecitizen;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import fr.cg95.cvq.business.document.DepositOrigin;
 import fr.cg95.cvq.business.document.DepositType;
 import fr.cg95.cvq.business.document.Document;
 import fr.cg95.cvq.business.document.DocumentBinary;
 import fr.cg95.cvq.business.document.DocumentType;
 import fr.cg95.cvq.business.request.MeansOfContact;
 import fr.cg95.cvq.business.request.MeansOfContactEnum;
 import fr.cg95.cvq.business.request.Request;
 import fr.cg95.cvq.business.request.RequestAction;
 import fr.cg95.cvq.business.request.RequestDocument;
 import fr.cg95.cvq.business.request.RequestNote;
 import fr.cg95.cvq.business.request.RequestNoteType;
 import fr.cg95.cvq.business.request.RequestState;
 import fr.cg95.cvq.business.request.ecitizen.VoCardRequest;
 import fr.cg95.cvq.business.users.ActorState;
 import fr.cg95.cvq.business.users.Address;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.FamilyStatusType;
 import fr.cg95.cvq.business.users.HomeFolder;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.RoleType;
 import fr.cg95.cvq.business.users.SexType;
 import fr.cg95.cvq.business.users.TitleType;
 import fr.cg95.cvq.exception.CvqAuthenticationFailedException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqInvalidTransitionException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.document.IDocumentTypeService;
 import fr.cg95.cvq.testtool.BusinessObjectsFactory;
 import fr.cg95.cvq.testtool.ServiceTestCase;
 import fr.cg95.cvq.util.Critere;
 
 /**
  * The tests for the VO card request service.
  *
  * @author bor@zenexity.fr
  */
 public class VoCardRequestServiceTest extends ServiceTestCase {
 
     public void testAll()
         throws CvqException, CvqInvalidTransitionException,
                CvqObjectNotFoundException, CvqAuthenticationFailedException,
                java.io.IOException, java.io.FileNotFoundException {
 
         startTransaction();
         
         /////////////////////////////////////////////
         // Create the VO Card request in DB        //
         /////////////////////////////////////////////
 
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
 
         VoCardRequest dcvo = new VoCardRequest();
         MeansOfContact meansOfContact = iMeansOfContactService.getMeansOfContactByType(
                 MeansOfContactEnum.EMAIL);
         dcvo.setMeansOfContact(meansOfContact);
 
         Address address = BusinessObjectsFactory.gimmeAdress("12","Rue d'Aligre", "Paris", "75012");
 
         Adult homeFolderResponsible = new Adult();
         homeFolderResponsible.setTitle(TitleType.MISTER);
         homeFolderResponsible.setLastName("LASTNAME");
         homeFolderResponsible.setFirstName("Vedad");
         homeFolderResponsible.setFamilyStatus(FamilyStatusType.MARRIED);
         homeFolderResponsible.setAdress(address);
         homeFolderResponsible.setQuestion("Qui est magique ?");
         homeFolderResponsible.setAnswer("Paris !");
         homeFolderResponsible.setPassword("totopwd");
         homeFolderResponsible.setHomePhone("0101010101");
         homeFolderResponsible.setOfficePhone("0101010101");
         homeFolderResponsible.setMobilePhone("0606060606");
         homeFolderResponsible.setBirthCity("Paris");
         homeFolderResponsible.setBirthCountry("France");
         homeFolderResponsible.setBirthDate(new Date());
         homeFolderResponsible.setBirthPostalCode("75012");
         homeFolderResponsible.setFirstName2("Branko");
         homeFolderResponsible.setFirstName3("Safet");
         homeFolderResponsible.setCfbn("5050505E");
         homeFolderResponsible.setEmail("bor@zenexity.fr");
         homeFolderResponsible.setNameOfUse("NAMEOFUSE");
         iHomeFolderService.addHomeFolderRole(homeFolderResponsible, RoleType.HOME_FOLDER_RESPONSIBLE);
 
         Adult mother = new Adult();
         mother.setTitle(TitleType.MADAM);
         mother.setLastName("LASTNAME");
         mother.setFirstName("Tania");
         mother.setFamilyStatus(FamilyStatusType.MARRIED);
         mother.setAdress(address);
         mother.setQuestion("Qui est magique ?");
         mother.setAnswer("Paris !");
         mother.setPassword("totopwd");
         mother.setHomePhone("0101010101");
         mother.setOfficePhone("0101010101");
         mother.setMobilePhone("0606060606");
         mother.setBirthCity("Paris");
         mother.setBirthCountry("France");
         mother.setBirthDate(new Date());
         mother.setBirthPostalCode("75012");
         mother.setFirstName2("Irina");
         mother.setFirstName3("Natacha");
         mother.setSex(SexType.FEMALE);
         mother.setCfbn("5050505E");
         mother.setEmail("bor@zenexity.fr");
         mother.setNameOfUse("NAMEOFUSE");
         mother.setMaidenName("SUSIC");
         
         Adult adultGrandMother = 
             BusinessObjectsFactory.gimmeAdult(TitleType.MADAM, "LASTNAME","josiane", null, 
                     FamilyStatusType.WIDOW);
         iHomeFolderService.addIndividualRole(mother, adultGrandMother, RoleType.TUTOR);
 
         List<Adult> adultSet = new ArrayList<Adult>();
         adultSet.add(mother);
         adultSet.add(adultGrandMother);
         adultSet.add(homeFolderResponsible);
 
         Child child1 = BusinessObjectsFactory.gimmeChild("LASTNAME", "Child1");
         child1.setBirthCity("Paris");
         child1.setBirthCountry("France");
         child1.setBirthDate(new Date());
         child1.setBirthPostalCode("75012");
         child1.setFirstName2("Yargla");
         child1.setFirstName3("Djaba");
         child1.setSex(SexType.MALE);
         iHomeFolderService.addIndividualRole(homeFolderResponsible, child1, RoleType.CLR_FATHER);
         iHomeFolderService.addIndividualRole(mother, child1, RoleType.CLR_MOTHER);
 
         Adult tutorNotInHomeFolder = 
             BusinessObjectsFactory.gimmeAdult(TitleType.MISTER, "TUTOR", "outside", null, 
                     FamilyStatusType.MARRIED);
         Address tutorAddress = BusinessObjectsFactory.gimmeAdress("1","Rue de Cotte", "Paris", "75012");
         tutorNotInHomeFolder.setAdress(tutorAddress);
         
         Child child2 = BusinessObjectsFactory.gimmeChild("LASTNAME", "Child2");
         List<Child> childSet = new ArrayList<Child>();
         childSet.add(child1);
         childSet.add(child2);
         iHomeFolderService.addIndividualRole(homeFolderResponsible, child2, RoleType.CLR_FATHER);
         iHomeFolderService.addIndividualRole(mother, child2, RoleType.CLR_MOTHER);
         iHomeFolderService.addIndividualRole(tutorNotInHomeFolder, child2, RoleType.CLR_TUTOR);
 
         iVoCardRequestService.create(dcvo, adultSet, childSet, null, address, null);
 
         homeFolderVoCardRequestIds.put(dcvo.getHomeFolderId(), dcvo.getId()); 
 
         // close current session and re-open a new one
         continueWithNewTransaction();
         
         // now that we have an user, set it in the context
         SecurityContext.setCurrentEcitizen(homeFolderResponsible.getLogin());
 
         // retrieve a fresh new home folder
         assertNotNull(dcvo.getHomeFolderId());
         HomeFolder homeFolder = iHomeFolderService.getById(dcvo.getHomeFolderId());
         homeFolderResponsible = iHomeFolderService.getHomeFolderResponsible(homeFolder.getId());
         assertNotNull(homeFolderResponsible);
         assertEquals("Vedad", homeFolderResponsible.getFirstName());
         
         //////////////////////////////////////////////////
         // test addition of the request and its attributes
         //////////////////////////////////////////////////
 
         Long requestId = dcvo.getId();
         VoCardRequest dcvoFromDb = (VoCardRequest) iVoCardRequestService.getById(requestId);
         assertEquals(dcvoFromDb.getState(), RequestState.PENDING);
         assertNotNull(dcvoFromDb.getRequesterId());
         assertEquals(homeFolderResponsible.getId(), dcvoFromDb.getRequesterId());
         assertNotNull(dcvoFromDb.getRequesterLastName());
         assertEquals(homeFolderResponsible.getLastName(), dcvoFromDb.getRequesterLastName());
         assertNull(dcvoFromDb.getSubjectId());
         assertNull(dcvoFromDb.getSubjectLastName());
         
         assertNotNull(iVoCardRequestService.getCertificate(dcvoFromDb.getId(), RequestState.PENDING));
         
         Adult homeFolderResponsibleFromDb = 
             iIndividualService.getAdultById(dcvoFromDb.getRequesterId());
         assertNotNull("Associated object of class Adult not saved !", homeFolderResponsibleFromDb);
         assertEquals(homeFolderResponsibleFromDb.getLastName(),"LASTNAME");
         assertEquals(homeFolderResponsibleFromDb.getId(), dcvoFromDb.getRequesterId());
         assertEquals(homeFolderResponsibleFromDb.getState(), ActorState.PENDING);
         
         Address adresseFromDb = homeFolderResponsibleFromDb.getAdress();
         assertNotNull("Associated object of class Adress not saved !", adresseFromDb);
         assertEquals(adresseFromDb.getCity(),"PARIS");
 
         homeFolder = iHomeFolderService.getById(dcvoFromDb.getHomeFolderId());
         assertNotNull(homeFolder.getId());
         assertEquals(homeFolder.getState(), ActorState.PENDING);
         assertNotNull(homeFolder.getAdress());
         assertEquals(homeFolder.getBoundToRequest(), Boolean.FALSE);
         assertEquals(homeFolder.getIndividuals().size(), 5);
         
         HomeFolder homeFolderOtherWay = iHomeFolderService.getById(dcvo.getHomeFolderId());
         assertNotNull(homeFolderOtherWay.getId());
         assertEquals(homeFolder.getId(), homeFolderOtherWay.getId());
         
         //////////////////////////////////////////////////
         // Perform some searches                        //
         //////////////////////////////////////////////////
 
         // close current session and re-open a new one
         continueWithNewTransaction();
         
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.BACK_OFFICE_CONTEXT);
         SecurityContext.setCurrentAgent(agentNameWithCategoriesRoles);
 
         Critere crit = new Critere();
         crit.setAttribut(Request.SEARCH_BY_REQUESTER_LASTNAME);
         crit.setComparatif(Critere.EQUALS);
         crit.setValue(homeFolderResponsibleFromDb.getLastName());
         Set<Critere> criteriaSet = new HashSet<Critere>();
         criteriaSet.add(crit);
         Critere crit2 = new Critere();
         crit2.setAttribut(Request.SEARCH_BY_CREATION_DATE);
         crit2.setComparatif(Critere.LT);
         crit2.setValue(new Date());
         criteriaSet.add(crit2);
         Critere crit3 = new Critere();
         crit3.setAttribut(Request.SEARCH_BY_HOME_FOLDER_ID);
         crit3.setComparatif(Critere.EQUALS);
         crit3.setValue(homeFolder.getId());
         criteriaSet.add(crit3);
         List<Request> carteVoList = iVoCardRequestService.get(criteriaSet, null, null, -1, 0);
         assertEquals(carteVoList.size(),1);
 
         crit = new Critere();
         crit.setAttribut(Request.SEARCH_BY_REQUESTER_LASTNAME);
         crit.setComparatif(Critere.EQUALS);
         crit.setValue("connaispascegarsla!");
         criteriaSet = new HashSet<Critere>();
         criteriaSet.add(crit);
         carteVoList = iVoCardRequestService.get(criteriaSet, null, null, -1, 0);
         assertEquals(carteVoList.size(),0);
 
         crit = new Critere();
         crit.setAttribut(Request.SEARCH_BY_REQUEST_ID);
         crit.setComparatif(Critere.EQUALS);
         crit.setValue(requestId);
         criteriaSet = new HashSet<Critere>();
         criteriaSet.add(crit);
         carteVoList = iVoCardRequestService.get(criteriaSet, null, null, -1, 0);
         assertEquals(carteVoList.size(), 1);
 
         crit = new Critere();
         crit.setAttribut(Request.SEARCH_BY_HOME_FOLDER_ID);
         crit.setComparatif(Critere.NEQUALS);
         crit.setValue(String.valueOf(homeFolder.getId()));
         criteriaSet = new HashSet<Critere>();
         criteriaSet.add(crit);
         carteVoList = iVoCardRequestService.get(criteriaSet, null, null, -1, 0);
         for (Request req : carteVoList) {
             assertFalse(req.getId() == homeFolder.getId());
         }
 
         /////////////////////////////////////////////
         // Add the necessary pieces                //
         /////////////////////////////////////////////
 
         // close current session and re-open a new one
         continueWithNewTransaction();
         
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
         SecurityContext.setCurrentEcitizen(homeFolderResponsible.getLogin());
 
         // create a document and attach it to the request
         Document doc = new Document();
         doc.setEcitizenNote("Mon livret de famille");
         doc.setDepositOrigin(DepositOrigin.ECITIZEN);
         doc.setDepositType(DepositType.PC);
         doc.setIndividualId(homeFolderResponsible.getId());
         doc.setHomeFolderId(homeFolder.getId());
         DocumentType documentType = 
             iDocumentTypeService.getDocumentTypeByType(IDocumentTypeService.IDENTITY_RECEIPT_TYPE);
         doc.setDocumentType(documentType);
         Long documentId = iDocumentService.create(doc);
         iVoCardRequestService.addDocument(requestId, documentId);
 
         // add binary data
         DocumentBinary docBin = new DocumentBinary();
         File file = getResourceFile("family_notebook.jpg");
         byte[] data = new byte[(int) file.length()];
         FileInputStream fis = new FileInputStream(file);
         fis.read(data);
         docBin.setData(data);
         iDocumentService.addPage(documentId, docBin);
 
         // retrieve the associated document
         Set<RequestDocument> docSetFromDb = iVoCardRequestService.getAssociatedDocuments(requestId);
         assertEquals(1, docSetFromDb.size());
         RequestDocument docFromDb = docSetFromDb.iterator().next();
         assertEquals(documentId, docFromDb.getDocumentId());
 
         /////////////////////////////////////////////////////////
         // Authenticate and retrieve home folder               //
         /////////////////////////////////////////////////////////
 
         // close current session and re-open a new one
         continueWithNewTransaction();
         
         homeFolder = iAuthenticationService.authenticate(homeFolderResponsible.getLogin(),"totopwd");
         assertNotNull("Retrieved home folder is null !", homeFolder);
         Adult respHomeFolderRetr = iHomeFolderService.getHomeFolderResponsible(homeFolder.getId());
         assertNotNull("Retrieved home folder responsible is null !", respHomeFolderRetr);
         List<Individual> individuSetRetr = homeFolder.getIndividuals();
         assertEquals(individuSetRetr.size(),5);
         List<Request> folderRequests = iRequestService.getByRequesterId(homeFolderResponsible.getId());
         assertEquals(1, folderRequests.size());
         VoCardRequest dcvoRetr = (VoCardRequest) folderRequests.get(0);
         assertNotNull("Retrieved cartevaloise request is null !", dcvoRetr);
 
         // test attachment of the documents
         List<Document> homeFolderDocuments = iDocumentService.getHomeFolderDocuments(homeFolder.getId(), -1);
         assertEquals(1, homeFolderDocuments.size());
         Document homeFolderDoc = homeFolderDocuments.get(0);
         assertEquals("Mon livret de famille", homeFolderDoc.getEcitizenNote());
 
         /////////////////////////////////////////////////////////
         // Complete & Validate the home folder                 //
         /////////////////////////////////////////////////////////
 
         // close current session and re-open a new one
         continueWithNewTransaction();
         
         // be an agent to perform request state changes
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.BACK_OFFICE_CONTEXT);
         SecurityContext.setCurrentAgent(agentNameWithCategoriesRoles);
 
         iRequestWorkflowService.updateRequestState(dcvoFromDb.getId(), RequestState.COMPLETE, null);
         RequestState[] rs = iRequestWorkflowService.getPossibleTransitions(RequestState.COMPLETE);
         assertEquals(rs.length, 3);
 
         crit = new Critere();
         crit.setAttribut(Request.SEARCH_BY_LAST_INTERVENING_USER_ID);
         crit.setComparatif(Critere.EQUALS);
         crit.setValue(SecurityContext.getCurrentAgent().getId());
         criteriaSet = new HashSet<Critere>();
         criteriaSet.add(crit);
         carteVoList = iRequestService.get(criteriaSet, null, null, -1, 0);
         assertTrue(carteVoList.size() > 0);
 
         // close current session and re-open a new one
         continueWithNewTransaction();
         
         // become back an ecitizen to test permission exception
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
         SecurityContext.setCurrentEcitizen(homeFolderResponsible.getLogin());
 
         String noteMsg = "Une petite note par le citoyen";
         iVoCardRequestService.addNote(dcvoFromDb.getId(), RequestNoteType.PUBLIC, noteMsg);
 
         // close current session and re-open a new one
         continueWithNewTransaction();
         
         // be an agent and add the note
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.BACK_OFFICE_CONTEXT);
         SecurityContext.setCurrentAgent(agentNameWithCategoriesRoles);
 
         noteMsg = "Une petite note par l'agent";
         iVoCardRequestService.addNote(dcvoFromDb.getId(), RequestNoteType.PUBLIC, noteMsg);
 
         iRequestWorkflowService.updateRequestState(dcvoFromDb.getId(), RequestState.VALIDATED, null);
         iRequestWorkflowService.updateRequestState(dcvoFromDb.getId(), RequestState.NOTIFIED,
             "Close me baby");
         iRequestWorkflowService.updateRequestState(dcvoFromDb.getId(), RequestState.CLOSED, null);
 
         // close current session and re-open a new one
         continueWithNewTransaction();
         
         // test certificate generation
         byte[] generatedCertificate = 
             iRequestService.getCertificate(dcvoFromDb.getId(), RequestState.VALIDATED);
         if (generatedCertificate == null)
             fail("No certificate found");
 
         // DEBUG ONLY : and print it out on the disk
         File tempFile = File.createTempFile("tmp" + dcvoFromDb.getId(), ".pdf");
         FileOutputStream fos = new FileOutputStream(tempFile);
         fos.write(generatedCertificate);
         // END DEBUG
 
        assertEquals(dcvoFromDb.getActions().size(), 5);

         // close current session and re-open a new one
         continueWithNewTransaction();
         
         // become back an ecitizen
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
         SecurityContext.setCurrentEcitizen(homeFolderResponsible.getLogin());
 
         Request yaRequest = iVoCardRequestService.getById(dcvoFromDb.getId());
 
         // test addition and modification of the note
         Set<RequestNote> notes = yaRequest.getNotes();
         assertNotNull(notes);
         assertEquals(2, notes.size());
 
         /////////////////////////////////////////////////////////
         // Change user's password                              //
         /////////////////////////////////////////////////////////
         
         homeFolderResponsible = 
             iIndividualService.getAdultById(yaRequest.getRequesterId());
         String generatedPassword = iAuthenticationService.generatePassword();
         iIndividualService.modifyPassword(homeFolderResponsible, "totopwd", generatedPassword);
         try {
             iAuthenticationService.authenticate(homeFolderResponsible.getLogin(), generatedPassword);
         } catch (CvqAuthenticationFailedException cafe) {
             fail("Unable to authenticate user with its new password !");
         }
     }
 }
