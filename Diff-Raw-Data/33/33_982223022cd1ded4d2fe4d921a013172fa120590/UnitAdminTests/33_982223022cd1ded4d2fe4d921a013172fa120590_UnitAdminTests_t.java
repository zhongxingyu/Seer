 package org.patientview.radar.test.roles.unitadmin;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.patientview.model.Patient;
 import org.patientview.radar.dao.UtilityDao;
 import org.patientview.radar.model.user.PatientUser;
 import org.patientview.radar.service.DemographicsManager;
 import org.patientview.radar.service.UserManager;
 import org.springframework.test.context.ContextConfiguration;
 
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Notes: Seen on frontend via ExistingPatientsListingPage,
  * the DemographicsDataProvider uses DemographicsManager.getDemographicsByRenalUnit.
  * <p/>
  * <p/>
  * Todo move the logic to get Centre from User out of ExistingPatientsListingPage so it can be covered by these tests
  * <p/>
  * todo don't apply the dataset.xml, and build the test context from scratch for each role
  */
 @RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:test-context.xml"})
 public class UnitAdminTests {
 
     @Inject
     RoleHelper roleHelper;
 
     @Inject
     UtilityDao utilityDao;
 
     @Inject
     UserManager userManager;
 
     @Inject
     DemographicsManager demographicsManager;
 
     /**
      * A unit admin is created by the superadmin in PV.
      * <p/>
      * Create a basic PV user structure - User - Role as unit admin etc
      */
     @Before
     public void setup() {
 
     }
 
     /**
      * Add a user mapping for Renal Unit A
      * Create patients into Renal Unit A
      * Show that the service call for get Patients as shown on the Radar patient list ExistingPatientsListingPage
      * will only return users with mappings into Unit A
      */
     @Test
     public void testUnitAdminCanSeePatientsInTheirRenalUnit() throws Exception {
         final String testUnit = "testUnit";
         List<Patient> patients = new ArrayList<Patient>();
         try {
             /* -- Create Data for test */
             utilityDao.createUnit(testUnit);
 
             PatientUser userAdmin = roleHelper.createUnitAdmin(StringUtils.right(Long.toString(new Date().getTime()), 10), testUnit);
             // Add 10 patients to the unit
             for (int i = 0; i < 9; i++) {
 
                 Patient patient = new Patient();
                 patient.setUnitcode(testUnit);
                 patient.setForename("Unit");
                 patient.setSurname("Tester");
                 patient.setDateofbirth("21-01-2013");
                 patients.add(patient);
 
                 userManager.registerPatient(patient);
 
             }
 
             // Test Bit
             demographicsManager.getDemographicsByUnitAdmin(userAdmin);
 
         } catch (Exception e) {
             Assert.fail(e.getMessage());
         } finally {
             //roleHelper.cleanTestData(patientUsers, testUnit);
         }
     }
 
     /**
      * Add a user mapping for Disease group Alports
      * Create patients into Disease group Alports
      * Show that the service call for get Patients as shown on the Radar patient list ExistingPatientsListingPage
      * will only return users with mappings into Disease group Alports
      */
     @Test
     public void testUnitAdminCanSeePatientsInTheirDiseaseGroup() {
 
     }
 
     /**
      * Add a user mapping for Renal Unit A AND Disease group Alports
      * Create patients into Renal Unit A AND Disease group Alports
      * Show that the service call for get Patients as shown on the Radar patient list ExistingPatientsListingPage
      * will only return users with mappings into Renal Unit A AND Disease group Alports
      */
     @Test
     public void testUnitAdminCanSeePatientsAggregatingRenalUnitAndDiseaseGroup() {
 
     }
 
 }
