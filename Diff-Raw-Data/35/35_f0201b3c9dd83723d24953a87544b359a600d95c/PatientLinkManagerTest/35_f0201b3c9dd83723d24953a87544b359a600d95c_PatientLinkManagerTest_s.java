 package org.patientview.radar.test.service;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.patientview.model.Patient;
 import org.patientview.model.PatientLink;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.radar.dao.DemographicsDao;
 import org.patientview.radar.dao.UserDao;
 import org.patientview.radar.dao.UtilityDao;
 import org.patientview.radar.service.PatientLinkManager;
 import org.patientview.radar.service.UserManager;
 import org.patientview.radar.test.TestPvDbSchema;
 import org.patientview.radar.test.roles.unitadmin.RoleHelper;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.util.StringUtils;
 
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * User: james@solidstategroup.com
  * Date: 15/11/13
  * Time: 14:32
  */
 @RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:test-context.xml"})
 public class PatientLinkManagerTest extends TestPvDbSchema {
 
     private final static String testDiseaseUnit = "Allports";
     private final static String testRenalUnit = "RENALA";
 
     @Inject
     private PatientLinkManager patientLinkManager;
 
     @Inject
     private DemographicsDao demographicsDao;
 
     @Inject
     private UtilityDao utilityDao;
 
 
     @Inject
     private RoleHelper roleHelper;
 
     @Inject
     private UserDao userDao;
 
     @Inject
     private UserManager userManager;
     
     /**
      * A unit admin is created by the superadmin in PV.
      * <p/>
      * Create a basic PV user structure - User - Role as unit admin etc
      */
     @Before
     public void setup() {
         // Setup a Renal Unit and Disease Group
         try {
             utilityDao.createUnit(testRenalUnit);
             utilityDao.createUnit(testDiseaseUnit);
         } catch (Exception e) {
 
         }
     }
 
 
     /**
      * Test to create a link record and then query to find is that record is linked.
      *
      */
     @Test
     public void testLinkingPatientRecord() throws Exception {
 
         Patient patient = new Patient();
 
         patient.setUnitcode(testRenalUnit);
         patient.setSurname("Test");
         patient.setForename("Test");
         patient.setDob(new Date());
         patient.setNhsno("234235242");
 
         DiseaseGroup diseaseGroup = new DiseaseGroup();
         diseaseGroup.setId(testDiseaseUnit);
         patient.setDiseaseGroup(diseaseGroup);
         // Save the patient record before linking because the record should already exist
         demographicsDao.saveDemographics(patient);
 
         patientLinkManager.linkPatientRecord(patient);
 
         PatientLink patientLink = patientLinkManager.getPatientLink(patient.getNhsno(), testRenalUnit);
         Assert.assertTrue("The list return should not be empty." , patientLink != null);
         Assert.assertTrue("The source and destination unit should be different", !patientLink.getSourceUnit().equalsIgnoreCase(patientLink.getDestinationUnit()));
 
     }
 
     /**
      * Test to see where the masking of the vital fields works
      *
      * @throws Exception
      */
     @Test
     public void testPatientRecordMerge() throws Exception {
 
         Patient sourcePatient = roleHelper.createPatient("231231", testRenalUnit, testDiseaseUnit);
         demographicsDao.saveDemographics(sourcePatient);
         patientLinkManager.linkPatientRecord(sourcePatient);
         PatientLink patientLink = patientLinkManager.getPatientLink(sourcePatient.getNhsno(), sourcePatient.getUnitcode());
         Patient radarPatient = demographicsDao.getDemographicsByNhsNoAndUnitCode(patientLink.getDestinationNhsNo(),patientLink.getDestinationUnit());
 
         Assert.assertTrue("The link patient must not have a first name", StringUtils.isEmpty(radarPatient.getForename()) );
         Assert.assertTrue("The link patient must not have a last name", StringUtils.isEmpty(radarPatient.getSurname()) );
         Assert.assertTrue("The link patient must not have a DOB", radarPatient.getDob() == null );
         Assert.assertTrue("The link patient must not have a address", StringUtils.isEmpty(radarPatient.getAddress1()));
         Assert.assertTrue("The link patient must not have a postcode", StringUtils.isEmpty(radarPatient.getAddress1()));
         Assert.assertTrue("The link patient must not have a sex", StringUtils.isEmpty(radarPatient.getSex()));
         Assert.assertTrue("The link patient must not have a hospital number", StringUtils.isEmpty(radarPatient.getHospitalnumber()));
         Assert.assertTrue("The link patient must not have a Renal Unit", radarPatient.getRenalUnit() == null);
 
 
         radarPatient = patientLinkManager.getMergePatient(sourcePatient);
 
         Assert.assertTrue("The link patient must inherit a first name", !StringUtils.isEmpty(radarPatient.getForename()) );
         Assert.assertTrue("The link patient must inherit a last name", !StringUtils.isEmpty(radarPatient.getSurname()) );
         Assert.assertTrue("The link patient must inherit a DOB", radarPatient.getDob() != null );
         Assert.assertTrue("The link patient must inherit a address", !StringUtils.isEmpty(radarPatient.getAddress1()));
         Assert.assertTrue("The link patient must inherit a postcode", !StringUtils.isEmpty(radarPatient.getAddress1()));
         Assert.assertTrue("The link patient must inherit a sex", !StringUtils.isEmpty(radarPatient.getSex()));
         Assert.assertTrue("The link patient must inherit a hospital number", !StringUtils.isEmpty(radarPatient.getHospitalnumber()));
         Assert.assertTrue("The link patient must inherit a Renal Unit", radarPatient.getRenalUnit() != null);
 
 
     }
 
     /**
      * Test to see if the data saved in the link record is now written back over the after it's been saved.
      *
      */
     @Test
     public void testMergeWithRadarSpecificFields() throws Exception {
 
         String radarMobile = "7896786";
         String radarComments = "These are some specific radar comments";
         String radarEthnicGroup = "This is a specific radar ethnic group";
         Calendar calendar = Calendar.getInstance();
 
         calendar.roll(Calendar.DAY_OF_YEAR, -100);
 
         Date radarDiagnosisDate = calendar.getTime();
 
         Patient sourcePatient = roleHelper.createPatient("231231", testRenalUnit, testDiseaseUnit);
         demographicsDao.saveDemographics(sourcePatient);
         patientLinkManager.linkPatientRecord(sourcePatient);
         PatientLink patientLink = patientLinkManager.getPatientLink(sourcePatient.getNhsno(), sourcePatient.getUnitcode());
         Patient radarPatient = demographicsDao.getDemographicsByNhsNoAndUnitCode(patientLink.getDestinationNhsNo(),patientLink.getDestinationUnit());
 
 
         radarPatient.setMobile(radarMobile);
         radarPatient.setDiagnosisDate(radarDiagnosisDate);
         radarPatient.setEthnicGp(radarEthnicGroup);
         radarPatient.setComments(radarComments);
 
         // Lets save the radar specific comments and then requery them through the merge
         demographicsDao.saveDemographics(radarPatient);
         radarPatient = patientLinkManager.getMergePatient(sourcePatient);
 
         Assert.assertTrue("The link patient must inherit the radar mobile", radarPatient.getMobile().equals(radarMobile));
         Assert.assertTrue("The link patient must inherit the radar comments", radarPatient.getComments().equals(radarComments) );
        // Assert.assertTrue("The link patient must inherit the radar ethnic group", radarPatient.getEthnicGp().equals(radarEthnicGroup));
        // Assert.assertTrue("The link patient must inherit a diagnosis date", radarPatient.getDiagnosisDate().equals(radarDiagnosisDate));
 
 
     }
 
     /**
     * When we are listing by patient record by unitcode the patient record must display
      *
      *
      * @throws Exception
      */
     @Test
     public void testLinkedRecordsAreReturnedWhenListingByUnitCode() throws Exception {
 
         Patient sourcePatient = roleHelper.createPatient("231231", testRenalUnit, testDiseaseUnit);
         userManager.savePatientUser(sourcePatient);
         patientLinkManager.linkPatientRecord(sourcePatient);
         PatientLink patientLink = patientLinkManager.getPatientLink(sourcePatient.getNhsno(), sourcePatient.getUnitcode());
         Patient radarPatient = demographicsDao.getDemographicsByNhsNoAndUnitCode(patientLink.getDestinationNhsNo(),patientLink.getDestinationUnit());
 
         List<String> units = new ArrayList<String>();
         units.add(testRenalUnit);
 
         List<Patient> patients = demographicsDao.getDemographicsByUnitCode(units);
 
         boolean found = false;
         for (Patient patient : patients) {
             if (patient.getUnitcode().equals(radarPatient.getUnitcode()) && patient.getNhsno().equals(radarPatient.getNhsno())) {
                 found = true;
             }
         }
 
         Assert.assertTrue("There linked patient record must be returned", found);
 
     }
 
 
     @After
     public void tearDown(){
         try {
             this.clearData();
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
     }
 
 }
