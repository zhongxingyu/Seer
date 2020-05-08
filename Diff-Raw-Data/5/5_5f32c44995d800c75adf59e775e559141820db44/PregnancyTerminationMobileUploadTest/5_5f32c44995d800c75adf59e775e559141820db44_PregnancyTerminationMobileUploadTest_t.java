 package org.motechproject.ghana.national.functional.mobile;
 
 
 import org.joda.time.LocalDate;
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.domain.mobilemidwife.ServiceType;
 import org.motechproject.ghana.national.functional.OpenMRSAwareFunctionalTest;
 import org.motechproject.ghana.national.functional.data.TestANCEnrollment;
 import org.motechproject.ghana.national.functional.data.TestMobileMidwifeEnrollment;
 import org.motechproject.ghana.national.functional.data.TestPatient;
 import org.motechproject.ghana.national.functional.framework.XformHttpClient;
 import org.motechproject.ghana.national.functional.pages.BasePage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSEncounterPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSPatientPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.vo.OpenMRSObservationVO;
 import org.motechproject.ghana.national.functional.pages.patient.ANCEnrollmentPage;
 import org.motechproject.ghana.national.functional.pages.patient.MobileMidwifeEnrollmentPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientEditPage;
 import org.motechproject.ghana.national.functional.pages.patient.SearchPatientPage;
 import org.motechproject.ghana.national.functional.util.DataGenerator;
 import org.motechproject.util.DateUtil;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.Test;
 
 import java.util.HashMap;
 
 import static java.util.Arrays.asList;
 import static junit.framework.Assert.assertEquals;
 import static org.joda.time.format.DateTimeFormat.forPattern;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class PregnancyTerminationMobileUploadTest extends OpenMRSAwareFunctionalTest {
 
     @Test
     public void shouldUploadPregnancyTerminationFormSuccessfully() throws Exception {
         DataGenerator dataGenerator = new DataGenerator();
         final String staffId = staffGenerator.createStaff(browser, homePage);
         final String facilityId = facilityGenerator.createFacility(browser, homePage);
         String patientFirstName = "patient first name" + dataGenerator.randomString(5);
 
         TestPatient patient = TestPatient.with(patientFirstName, staffId).
                 patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER).estimatedDateOfBirth(false);
         final String patientId = patientGenerator.createPatient(patient, browser, homePage);
 
         SearchPatientPage searchPatientPage = browser.toSearchPatient();
         searchPatientPage.searchWithMotechId(patientId);
 
         TestANCEnrollment ancEnrollment = TestANCEnrollment.create().withStaffId(staffId).withRegistrationToday(RegistrationToday.IN_PAST);
         PatientEditPage editPage = browser.toPatientEditPage(searchPatientPage, patient);
         ANCEnrollmentPage ancEnrollmentPage = browser.toEnrollANCPage(editPage);
         ancEnrollmentPage.save(ancEnrollment);
 
         final LocalDate terminationDate = DateUtil.today();
 
         final XformHttpClient.XformResponse response = XformHttpClient.execute("http://localhost:8080/ghana-national-web/formupload",
                 "NurseDataEntry", XformHttpClient.XFormParser.parse("pregnancy-termination-template.xml", new HashMap<String, String>() {{
             put("staffId", staffId);
             put("facilityId", facilityId);
             put("motechId", patientId);
             put("date", terminationDate.toString(forPattern("yyyy-MM-dd")));
             put("terminationType", "1");
             put("procedure", "2");
             put("complications", "1,2");
             put("maternalDeath", "N");
             put("postAbortionFPCounseled", "Y");
             put("postAbortionFPAccepted", "Y");
             put("referred", "Y");
             put("comments", "blah blah");
         }}));
 
         assertEquals(1, response.getSuccessCount());
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(patientId));
         String encounterId = openMRSPatientPage.chooseEncounter("PREGTERMVISIT");
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
 
         openMRSEncounterPage.displaying(asList(
                 new OpenMRSObservationVO("MATERNAL DEATH", "false"),
                 new OpenMRSObservationVO("POST-ABORTION FP COUNSELING", "true"),
                 new OpenMRSObservationVO("POST-ABORTION FP ACCEPTED", "true"),
                 new OpenMRSObservationVO("PREGNANCY STATUS", "false"),
                 new OpenMRSObservationVO("PREGNANCY, TERMINATION PROCEDURE", "2.0"),
                 new OpenMRSObservationVO("REFERRED", "true"),
                 new OpenMRSObservationVO("TERMINATION COMPLICATION", "2.0"),
                 new OpenMRSObservationVO("COMMENTS", "blah blah"),
                 new OpenMRSObservationVO("TERMINATION TYPE", "1.0")
         ));
     }
 
     @Test
    public void shouldNotUnenrollFromMobileMidwifeOnlyIfMotherIsAlive_IVRService() throws Exception {
         DataGenerator dataGenerator = new DataGenerator();
         final String staffId = staffGenerator.createStaff(browser, homePage);
         final String facilityId = facilityGenerator.createFacility(browser, homePage);
         String patientFirstName = "patient first name" + dataGenerator.randomString(5);
 
         TestPatient patient = TestPatient.with(patientFirstName, staffId).
                 patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER).estimatedDateOfBirth(false);
         final String patientId = patientGenerator.createPatient(patient, browser, homePage);
 
         SearchPatientPage searchPatientPage = browser.toSearchPatient();
         searchPatientPage.searchWithMotechId(patientId);
 
         TestANCEnrollment ancEnrollment = TestANCEnrollment.create().withStaffId(staffId).withRegistrationToday(RegistrationToday.IN_PAST);
         PatientEditPage editPage = browser.toPatientEditPage(searchPatientPage, patient);
         ANCEnrollmentPage ancEnrollmentPage = browser.toEnrollANCPage(editPage);
         ancEnrollmentPage.save(ancEnrollment);
 
         TestMobileMidwifeEnrollment enrollmentDetails = TestMobileMidwifeEnrollment.with(staffId).patientId(patientId).withServiceType(ServiceType.PREGNANCY).withMediumAsVoice();
         MobileMidwifeEnrollmentPage enrollmentPage = toMobileMidwifeEnrollmentPage(patient, homePage);
         enrollmentPage.enroll(enrollmentDetails);
 
         final LocalDate terminationDate = DateUtil.today();
 
         final XformHttpClient.XformResponse response = XformHttpClient.execute("http://localhost:8080/ghana-national-web/formupload",
                 "NurseDataEntry", XformHttpClient.XFormParser.parse("pregnancy-termination-template.xml", new HashMap<String, String>() {{
             put("staffId", staffId);
             put("facilityId", facilityId);
             put("motechId", patientId);
             put("date", terminationDate.toString(forPattern("yyyy-MM-dd")));
             put("terminationType", "1");
             put("procedure", "2");
             put("complications", "1,2");
             put("maternalDeath", "N");
             put("postAbortionFPCounseled", "Y");
             put("postAbortionFPAccepted", "Y");
             put("referred", "Y");
             put("comments", "blah blah");
         }}));
 
 
         assertEquals(1, response.getSuccessCount());
 
         MobileMidwifeEnrollmentPage mobileMidwifeEnrollmentPage = toMobileMidwifeEnrollmentPage(patient, homePage);
        assertEquals(enrollmentDetails.status("ACTIVE").toString(), mobileMidwifeEnrollmentPage.details().toString());
     }
 
     private MobileMidwifeEnrollmentPage toMobileMidwifeEnrollmentPage(TestPatient patient, BasePage basePage) {
         SearchPatientPage searchPatientPage = browser.toSearchPatient(basePage);
         searchPatientPage.searchWithName(patient.firstName());
         PatientEditPage patientEditPage = browser.toPatientEditPage(searchPatientPage, patient);
         return browser.toMobileMidwifeEnrollmentForm(patientEditPage);
     }
 }
