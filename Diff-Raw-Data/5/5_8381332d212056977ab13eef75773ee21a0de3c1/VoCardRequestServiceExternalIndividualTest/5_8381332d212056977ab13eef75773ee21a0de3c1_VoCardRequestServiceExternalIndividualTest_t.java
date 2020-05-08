 package fr.cg95.cvq.service.request.ecitizen;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import fr.cg95.cvq.business.request.MeansOfContact;
 import fr.cg95.cvq.business.request.MeansOfContactEnum;
 import fr.cg95.cvq.business.request.ecitizen.VoCardRequest;
 import fr.cg95.cvq.business.users.Address;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.Child;
 import fr.cg95.cvq.business.users.FamilyStatusType;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.RoleType;
 import fr.cg95.cvq.business.users.SexType;
 import fr.cg95.cvq.business.users.TitleType;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.request.IRequestExportService;
 import fr.cg95.cvq.service.request.RequestTestCase;
 import fr.cg95.cvq.util.development.BusinessObjectsFactory;
 import fr.cg95.cvq.xml.common.IndividualType;
 import fr.cg95.cvq.xml.request.ecitizen.impl.VoCardRequestDocumentImpl;
 
 public class VoCardRequestServiceExternalIndividualTest extends RequestTestCase {
 
     @Autowired
     private IRequestExportService requestExportService;
 
    @Test
     public void testAccountWithExternalIndividuals() throws Exception {
         
         startTransaction();
         
         /////////////////////////////////////////////
         // Create the VO Card request in DB        //
         /////////////////////////////////////////////
 
         SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.FRONT_OFFICE_CONTEXT);
 
         VoCardRequest dcvo = new VoCardRequest();
         MeansOfContact meansOfContact = meansOfContactService.getMeansOfContactByType(
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
         homeFolderService.addHomeFolderRole(homeFolderResponsible, RoleType.HOME_FOLDER_RESPONSIBLE);
 
         List<Adult> adultSet = new ArrayList<Adult>();
         adultSet.add(homeFolderResponsible);
 
         Adult externalIndividual = BusinessObjectsFactory.gimmeAdult(TitleType.MISTER, "TUTOR", "outside", null, 
                 FamilyStatusType.MARRIED);
         homeFolderService.addHomeFolderRole(externalIndividual, RoleType.TUTOR);
 
         List<Adult> foreignAdultSet = new ArrayList<Adult>();
         foreignAdultSet.add(externalIndividual);
         
         Child child1 = BusinessObjectsFactory.gimmeChild("LASTNAME", "Child1");
         child1.setBirthCity("Paris");
         child1.setBirthCountry("France");
         child1.setBirthDate(new Date());
         child1.setBirthPostalCode("75012");
         child1.setFirstName2("Yargla");
         child1.setFirstName3("Djaba");
         child1.setSex(SexType.MALE);
         homeFolderService.addIndividualRole(homeFolderResponsible, child1, RoleType.CLR_FATHER);
         homeFolderService.addIndividualRole(externalIndividual, child1, RoleType.CLR_TUTOR);
 
         List<Child> childSet = new ArrayList<Child>();
         childSet.add(child1);
 
         requestWorkflowService.createAccountCreationRequest(dcvo, adultSet, childSet, foreignAdultSet, address, null);
 
         homeFolderVoCardRequestIds.put(dcvo.getHomeFolderId(), dcvo.getId()); 
 
         continueWithNewTransaction();
         
         List<Individual> externalIndividuals =
             homeFolderService.getExternalIndividuals(dcvo.getHomeFolderId());
         assertNotNull(externalIndividuals);
         assertEquals(1, externalIndividuals.size());
         
         VoCardRequestDocumentImpl xmlRequestDocument = 
             (VoCardRequestDocumentImpl) requestExportService.fillRequestXml(dcvo);
         fr.cg95.cvq.xml.request.ecitizen.VoCardRequestDocument.VoCardRequest xmlRequest = 
             xmlRequestDocument.getVoCardRequest();
         assertNotNull(xmlRequest.getHomeFolder().getExternalIndividualsArray());
         IndividualType[] xmlExternalIndividuals = 
             xmlRequest.getHomeFolder().getExternalIndividualsArray();
         assertEquals(1, xmlExternalIndividuals.length);
         assertEquals("TUTOR", xmlExternalIndividuals[0].getLastName());
 
         //     Write tele-service xml data file
         File xmlFile = File.createTempFile("tmp" + dcvo.getId(), ".xml");
         FileOutputStream xmlFos = new FileOutputStream(xmlFile);
         xmlFos.write(requestExportService.fillRequestXml(requestSearchService.getById(dcvo.getId(), true)).toString().getBytes());
 
        homeFolderService.delete(dcvo.getHomeFolderId());
         individualService.delete(individualService.getById(externalIndividual.getId()));
     }
 }
