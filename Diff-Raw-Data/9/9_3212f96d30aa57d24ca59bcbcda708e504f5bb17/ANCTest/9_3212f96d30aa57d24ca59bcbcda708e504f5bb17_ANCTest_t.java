 package org.motechproject.ghana.national.functional.patient;
 
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.functional.OpenMRSAwareFunctionalTest;
 import org.motechproject.ghana.national.functional.data.TestANCEnrollment;
 import org.motechproject.ghana.national.functional.data.TestPatient;
 import org.motechproject.ghana.national.functional.pages.BasePage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSEncounterPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSPatientPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.vo.OpenMRSObservationVO;
 import org.motechproject.ghana.national.functional.pages.patient.ANCEnrollmentPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientEditPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientPage;
 import org.motechproject.ghana.national.functional.pages.patient.SearchPatientPage;
 import org.motechproject.ghana.national.functional.util.DataGenerator;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.Test;
 
 import java.text.ParseException;
 
 import static java.util.Arrays.asList;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class ANCTest extends OpenMRSAwareFunctionalTest {
 
     @Test
     public void shouldEnrollForANCForAPatientAndUpdate() throws ParseException {
         DataGenerator dataGenerator = new DataGenerator();
         // create
         String staffId = staffGenerator.createStaff(browser, homePage);
 
         String patientFirstName = "ANC patient first name" + dataGenerator.randomString(5);
         TestPatient testPatient = TestPatient.with(patientFirstName, staffId)
                 .patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER)
                 .estimatedDateOfBirth(false);
 
         PatientPage patientPage = browser.toCreatePatient(homePage);
         patientPage.create(testPatient);
 
         TestANCEnrollment ancEnrollment = TestANCEnrollment.create().withStaffId(staffId).withRegistrationToday(RegistrationToday.IN_PAST);
         PatientEditPage patientEditPage = searchPatient(patientFirstName, testPatient, patientPage);
         ANCEnrollmentPage ancEnrollmentPage = browser.toEnrollANCPage(patientEditPage);
         ancEnrollmentPage.save(ancEnrollment);
 
         patientEditPage = searchPatient(patientFirstName, testPatient, ancEnrollmentPage);
         ancEnrollmentPage = browser.toEnrollANCPage(patientEditPage);
 
         ancEnrollmentPage.displaying(ancEnrollment);
 
         // edit
         ancEnrollment.withSubDistrict("Senya").withFacility("Senya HC").withGravida("4").withHeight("160.9");
         ancEnrollmentPage.save(ancEnrollment);
 
         patientEditPage = searchPatient(patientFirstName, testPatient, ancEnrollmentPage);
         ancEnrollmentPage = browser.toEnrollANCPage(patientEditPage);
 
         ancEnrollmentPage.displaying(ancEnrollment);
 
         String motechId = ancEnrollmentPage.motechId();
         String estimatedDeliveryDate = ancEnrollmentPage.getEstimatedDateOfDelivery();
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(motechId));
         
 
         String encounterId = openMRSPatientPage.chooseEncounter("PREGREGVISIT");
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(asList(
                 new OpenMRSObservationVO("PREGNANCY STATUS", "true"),
                 new OpenMRSObservationVO("ESTIMATED DATE OF CONFINEMENT", getParsedDate(estimatedDeliveryDate)),
                 new OpenMRSObservationVO("DATE OF CONFINEMENT CONFIRMED","true")
         ));
 
         openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(motechId));
         encounterId = openMRSPatientPage.chooseEncounter("ANCREGVISIT");
 
         openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(asList(
                 new OpenMRSObservationVO("INTERMITTENT PREVENTATIVE TREATMENT DOSE", "1.0"),
                 new OpenMRSObservationVO("GRAVIDA", "4.0"),
                 new OpenMRSObservationVO("PARITY", "4.0"),
                new OpenMRSObservationVO("SERIAL NUMBER", "serialNumber"),
                 new OpenMRSObservationVO("HEIGHT (CM)", "160.9")
         ));
 
     }
 
     private PatientEditPage searchPatient(String patientFirstName, TestPatient testPatient, BasePage basePage) {
         SearchPatientPage searchPatientPage = browser.toSearchPatient(basePage);
 
         searchPatientPage.searchWithName(patientFirstName);
         searchPatientPage.displaying(testPatient);
 
         return browser.toPatientEditPage(searchPatientPage, testPatient);
     }
 }
