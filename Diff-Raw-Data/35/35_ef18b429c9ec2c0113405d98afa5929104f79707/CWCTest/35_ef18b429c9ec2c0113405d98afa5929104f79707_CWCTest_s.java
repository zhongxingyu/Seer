 package org.motechproject.ghana.national.functional.patient;
 
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.domain.CwcCareHistory;
 import org.motechproject.ghana.national.functional.OpenMRSAwareFunctionalTest;
 import org.motechproject.ghana.national.functional.data.TestCWCEnrollment;
 import org.motechproject.ghana.national.functional.data.TestPatient;
 import org.motechproject.ghana.national.functional.pages.BasePage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSEncounterPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSPatientPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.vo.OpenMRSObservationVO;
 import org.motechproject.ghana.national.functional.pages.patient.CWCEnrollmentPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientEditPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientPage;
 import org.motechproject.ghana.national.functional.pages.patient.SearchPatientPage;
 import org.motechproject.ghana.national.functional.util.DataGenerator;
 import org.motechproject.util.DateUtil;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.Arrays;
 
 import static java.util.Arrays.asList;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class CWCTest extends OpenMRSAwareFunctionalTest {
     private DataGenerator dataGenerator;
 
     @BeforeMethod
     public void setUp() {
         dataGenerator = new DataGenerator();
     }
 
     @Test
     public void shouldEnrollForCWCForAPatient() {
         String staffId = staffGenerator.createStaff(browser, homePage);
 
         String patientFirstName = "First Name" + dataGenerator.randomString(5);
         PatientPage patientPage = browser.toCreatePatient(homePage);
         TestPatient patient = TestPatient.with(patientFirstName, staffId)
                 .patientType(TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE)
                 .estimatedDateOfBirth(false)
                 .dateOfBirth(DateUtil.newDate(DateUtil.today().getYear() - 1, 11, 11));
 
         patientPage.create(patient);
 
         String motechId = patientPage.motechId();
 
         // create
         SearchPatientPage searchPatientPage = browser.toSearchPatient(homePage);
         searchPatientPage.searchWithName(patient.firstName());
 
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.create().withStaffId(staffId).withRegistrationDate(DateUtil.today()).withHistoryDays3WeeksBeforeRegistrationDate(3);
 
         PatientEditPage patientEditPage = browser.toPatientEditPage(searchPatientPage, patient);
         CWCEnrollmentPage cwcEnrollmentPage = browser.toEnrollCWCPage(patientEditPage);
         cwcEnrollmentPage.save(testCWCEnrollment);
 
         patientEditPage = searchPatient(patientFirstName, patient, cwcEnrollmentPage);
         cwcEnrollmentPage = browser.toEnrollCWCPage(patientEditPage);
 
         cwcEnrollmentPage.displaying(testCWCEnrollment);
 
         // edit
         testCWCEnrollment.withAddCareHistory(Arrays.asList(CwcCareHistory.MEASLES)).withLastMeaslesDate(testCWCEnrollment.getRegistrationDate().minusDays(3));
         cwcEnrollmentPage.save(testCWCEnrollment);
 
         patientEditPage = searchPatient(patientFirstName, patient, cwcEnrollmentPage);
         cwcEnrollmentPage = browser.toEnrollCWCPage(patientEditPage);
 
         cwcEnrollmentPage.displaying(testCWCEnrollment);
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(motechId));
         String encounterId = openMRSPatientPage.chooseEncounter("CWCREGVISIT");
 
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(asList(
                 new OpenMRSObservationVO("SERIAL NUMBER", "serialNumber"),
                new OpenMRSObservationVO("MEASLES VACCINATION", "1.0")
         ));
     }
 
 
 
     private PatientEditPage searchPatient(String patientFirstName, TestPatient testPatient, BasePage basePage) {
         SearchPatientPage searchPatientPage = browser.toSearchPatient(basePage);
 
         searchPatientPage.searchWithName(patientFirstName);
         searchPatientPage.displaying(testPatient);
 
         return browser.toPatientEditPage(searchPatientPage, testPatient);
     }
 
 }
