 package org.patientview.radar.test.service;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.patientview.model.Centre;
 import org.patientview.model.Patient;
 import org.patientview.model.enums.NhsNumberType;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.radar.dao.PatientLinkDao;
 import org.patientview.radar.dao.UserDao;
 import org.patientview.radar.model.user.PatientUser;
 import org.patientview.radar.service.DemographicsManager;
 import org.patientview.radar.service.UserManager;
 import org.patientview.radar.test.TestDataHelper;
 import org.patientview.radar.test.TestPvDbSchema;
 import org.springframework.test.context.ContextConfiguration;
 
 import javax.inject.Inject;
 import java.util.Date;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 @RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:test-context.xml"})
 public class UserManagerTest extends TestPvDbSchema {
 
     @Inject
     private UserDao userDao;
 
     @Inject
     private UserManager userManager;
 
     @Inject
     private DemographicsManager demographicsManager;
 
     @Inject
     private TestDataHelper testDataHelper;
 
     @Inject
     private PatientLinkDao patientLinkDao;
 
     private DiseaseGroup diseaseGroup;
 
     private Centre centre;
 
 
     @Before
     public void setUp() {
         diseaseGroup = new DiseaseGroup();
         diseaseGroup.setId("1");
         diseaseGroup.setName("testGroup");
         diseaseGroup.setShortName("shortName");
 
         centre = new Centre();
         centre.setUnitCode("testCodeA");
     }
 
     /**
      * Create the unit admin who will create the user:
      *      - user, tbl_users, specialuserrole (unitadmin) etc
      *      - usermapping (PV) of username, unitcode e.g. ALPORTS unit, nhsno, specialty (mock this)
      *      - usermapping (RDR) of radarUserId (tbl_users.uID), role (ROLE_PROFESSIONAL)
      *
      * Create a patient using this unit admin (the first patient in the system so not linking madness)
      *      - user, tbl_patient_users, specialuserrole (patient) etc
      *      - usermapping (PV) of username, unitcode e.g. ALPORTS unit, nhsno, specialty (mock this)
      *      - patient record
      */
     @Test
     public void testRadarUnitAdminCanCreatePatientFromScratch() {
 
     }
 
     /**
      * This is testing the creation of a record in the tbl patient users table
      *
      * @throws Exception
      */
     @Test
     public void testPatientUserRegistration() throws Exception {
         testDataHelper.createSpecialty();
         // create a user row as per patient view
         PatientUser patientUser = new PatientUser();
         patientUser.setName("my user");
         patientUser.setUsername("testusername");
         patientUser.setEmail("test@test.com");
         patientUser.setPassword("passwordhash");
         userDao.createUser(patientUser);
 
         // create a demographic
         Date dob = new Date();
         Patient patient = createDemographics("Test", "User", centre, "NHS123", "test@test.com", dob);
 
         userManager.savePatientUser(patient);
 
         // Try and register - will throw an exception as no matching radar number
         patientUser = userManager.getPatientUser(patient.getEmailAddress());
 
         assertNotNull("registered user is null", patientUser);
         assertNotNull("no password generated", patientUser.getPassword());
         assertEquals("Email not set on user", patient.getEmailAddress(), patientUser.getEmail());
         assertNotNull("Dob not set on user", patientUser.getDateOfBirth());
     }
 
     private Patient createDemographics(String forename, String surname, Centre centre, String nhsno,
                                             String email, Date dateOfBirth) {
         Patient patient = new Patient();
         patient.setForename(forename);
         patient.setSurname(surname);
         patient.setNhsNumberType(NhsNumberType.NHS_NUMBER);
         patient.setNhsno(nhsno);
         patient.setRenalUnit(centre);
         patient.setEmailAddress(email);
         patient.setDob(dateOfBirth);
         patient.setDiseaseGroup(diseaseGroup);
         demographicsManager.saveDemographics(patient);
         assertNotNull(patient.getId());
         return patient;
     }
 
 }
