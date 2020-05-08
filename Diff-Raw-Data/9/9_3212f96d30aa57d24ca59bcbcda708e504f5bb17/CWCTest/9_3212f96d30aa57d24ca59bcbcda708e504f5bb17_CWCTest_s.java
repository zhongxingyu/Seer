 package org.motechproject.ghana.national.functional.patient;
 
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.domain.CwcCareHistory;
 import org.motechproject.ghana.national.functional.OpenMRSAwareFunctionalTest;
 import org.motechproject.ghana.national.functional.data.TestCWCEnrollment;
 import org.motechproject.ghana.national.functional.data.TestPatient;
 import org.motechproject.ghana.national.functional.framework.XformHttpClient;
 import org.motechproject.ghana.national.functional.mobileforms.MobileForm;
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
 import java.util.HashMap;
 
 import static java.util.Arrays.asList;
 import static junit.framework.Assert.assertEquals;
 import static org.joda.time.format.DateTimeFormat.forPattern;
 
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
 
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.create().withStaffId(staffId);
 
         PatientEditPage patientEditPage = browser.toPatientEditPage(searchPatientPage, patient);
         CWCEnrollmentPage cwcEnrollmentPage = browser.toEnrollCWCPage(patientEditPage);
         cwcEnrollmentPage.save(testCWCEnrollment);
 
         patientEditPage = searchPatient(patientFirstName, patient, cwcEnrollmentPage);
         cwcEnrollmentPage = browser.toEnrollCWCPage(patientEditPage);
 
         cwcEnrollmentPage.displaying(testCWCEnrollment);
 
         // edit
         testCWCEnrollment.withAddCareHistory(Arrays.asList(CwcCareHistory.MEASLES)).withLastMeaslesDate(DateUtil.newDate(2006, 12, 2));
         cwcEnrollmentPage.save(testCWCEnrollment);
 
         patientEditPage = searchPatient(patientFirstName, patient, cwcEnrollmentPage);
         cwcEnrollmentPage = browser.toEnrollCWCPage(patientEditPage);
 
         cwcEnrollmentPage.displaying(testCWCEnrollment);
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(motechId));
         String encounterId = openMRSPatientPage.chooseEncounter("CWCREGVISIT");
 
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(asList(
                new OpenMRSObservationVO("CWC REGISTRATION NUMBER", "serialNumber"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "MEASLES VACCINATION")
         ));
     }
 
     @Test
     public void shouldCreateCWCVisit() {
         final String staffId = staffGenerator.createStaff(browser, homePage);
         final String facilityId = facilityGenerator.createFacility(browser, homePage);
         String patientFirstName = "First Name" + dataGenerator.randomString(5);
         final PatientPage patientPage = browser.toCreatePatient(homePage);
         final TestPatient patient = TestPatient.with(patientFirstName, staffId)
                 .patientType(TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE)
                 .estimatedDateOfBirth(false)
                 .dateOfBirth(DateUtil.now().minusYears(2).toLocalDate());
         patientPage.create(patient);
         SearchPatientPage searchPatientPage = browser.toSearchPatient(homePage);
         searchPatientPage.searchWithName(patient.firstName());
         PatientEditPage patientEditPage = browser.toPatientEditPage(searchPatientPage, patient);
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.create().withStaffId(staffId)
                 .withMotechPatientId(patientEditPage.motechId())
                 .withAddCareHistory(asList(CwcCareHistory.YF, CwcCareHistory.BCG, CwcCareHistory.OPV))
                 .withLastYellowFeverDate(DateUtil.now().toLocalDate())
                 .withLastBcgDate(DateUtil.now().toLocalDate())
                 .withLastOPV("1")
                 .withLastOPVDate(DateUtil.now().toLocalDate());
         final CWCEnrollmentPage cwcEnrollmentPage = browser.toEnrollCWCPage(patientEditPage);
         cwcEnrollmentPage.save(testCWCEnrollment);
 
         final String motechId = cwcEnrollmentPage.getPatientMotechId();
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.cwcVisitForm(), new HashMap<String, String>() {{
             put("staffId", staffId);
             put("facilityId", facilityId);
             put("date", DateUtil.now().toString(forPattern("yyyy-MM-dd")));
             put("motechId", motechId);
             put("serialNumber", "1234567");
             put("immunizations", "BCG,OPV,YF");
             put("opvdose", "1");
             put("pentadose", "1");
             put("iptidose", "1");
             put("weight", "23");
             put("muac", "12");
             put("height", "13");
             put("maleInvolved", "Y");
             put("cwcLocation", "2");
             put("house", "32");
             put("community", "Home");
             put("comments", "Unknwon");
         }});
 
         assertEquals(0, response.getErrors().size());
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(motechId));
         String encounterId = openMRSPatientPage.chooseEncounter("CWCVISIT");
 
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(asList(
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "ORAL POLIO VACCINATION DOSE"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "BACILLE CAMILE-GUERIN VACCINATION"),
                 new OpenMRSObservationVO("INTERMITTENT PREVENTATIVE TREATMENT INFANTS DOSE", "1.0"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "YELLOW FEVER VACCINATION"),
                 new OpenMRSObservationVO("HOUSE", "32"),
                 new OpenMRSObservationVO("CWC LOCATION", "2.0"),
                 new OpenMRSObservationVO("WEIGHT (KG)", "23.0"),
                 new OpenMRSObservationVO("COMMENTS", "Unknwon"),
                 new OpenMRSObservationVO("COMMUNITY", "Home"),
                 new OpenMRSObservationVO("HEIGHT (CM)", "13.0"),
                 new OpenMRSObservationVO("PENTA VACCINATION DOSE", "1.0"),
                 new OpenMRSObservationVO("MID-UPPER ARM CIRCUMFERENCE", "12.0"),
                 new OpenMRSObservationVO("ORAL POLIO VACCINATION DOSE", "1.0"),
                 new OpenMRSObservationVO("SERIAL NUMBER", "1234567"),
                 new OpenMRSObservationVO("MALE INVOLVEMENT", "true")
         ));
     }
 
     private PatientEditPage searchPatient(String patientFirstName, TestPatient testPatient, BasePage basePage) {
         SearchPatientPage searchPatientPage = browser.toSearchPatient(basePage);
 
         searchPatientPage.searchWithName(patientFirstName);
         searchPatientPage.displaying(testPatient);
 
         return browser.toPatientEditPage(searchPatientPage, testPatient);
     }
 
 }
