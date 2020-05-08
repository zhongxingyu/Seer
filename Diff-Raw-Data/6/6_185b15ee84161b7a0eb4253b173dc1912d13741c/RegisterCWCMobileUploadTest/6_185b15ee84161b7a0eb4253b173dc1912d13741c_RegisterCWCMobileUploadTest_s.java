 package org.motechproject.ghana.national.functional.mobile;
 
 import org.apache.commons.collections.MapUtils;
 import org.joda.time.LocalDate;
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.domain.CwcCareHistory;
 import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.functional.OpenMRSAwareFunctionalTest;
 import org.motechproject.ghana.national.functional.data.TestCWCEnrollment;
 import org.motechproject.ghana.national.functional.data.TestMobileMidwifeEnrollment;
 import org.motechproject.ghana.national.functional.data.TestPatient;
 import org.motechproject.ghana.national.functional.framework.OpenMRSDB;
 import org.motechproject.ghana.national.functional.framework.ScheduleTracker;
 import org.motechproject.ghana.national.functional.framework.XformHttpClient;
 import org.motechproject.ghana.national.functional.helper.ScheduleHelper;
 import org.motechproject.ghana.national.functional.mobileforms.MobileForm;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSEncounterPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSPatientPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.vo.OpenMRSObservationVO;
 import org.motechproject.ghana.national.functional.pages.patient.CWCEnrollmentPage;
 import org.motechproject.ghana.national.functional.pages.patient.MobileMidwifeEnrollmentPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientEditPage;
 import org.motechproject.ghana.national.functional.pages.patient.SearchPatientPage;
 import org.motechproject.ghana.national.functional.util.DataGenerator;
 import org.motechproject.ghana.national.tools.Utility;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.Test;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static ch.lambdaj.Lambda.join;
 import static ch.lambdaj.Lambda.on;
 import static java.util.Arrays.asList;
 import static junit.framework.Assert.assertNull;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.*;
 import static org.motechproject.ghana.national.functional.data.TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE;
 import static org.motechproject.util.DateUtil.today;
 import static org.testng.Assert.assertEquals;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class RegisterCWCMobileUploadTest extends OpenMRSAwareFunctionalTest {
 
     @Autowired
     private OpenMRSDB openMRSDB;
 
     @Autowired
     ScheduleTracker scheduleTracker;
 
     @Test
     public void shouldCheckForAllMandatoryDetails() throws Exception {
         final XformHttpClient.XformResponse xformResponse = mobile.upload(MobileForm.registerCWCForm(), MapUtils.EMPTY_MAP);
         final List<XformHttpClient.Error> errors = xformResponse.getErrors();
 
         assertEquals(errors.size(), 1);
         final Map<String, List<String>> errorsMap = errors.iterator().next().getErrors();
 
         assertThat(errorsMap.get("staffId"), hasItem("is mandatory"));
         assertThat(errorsMap.get("facilityId"), hasItem("is mandatory"));
         assertThat(errorsMap.get("motechId"), hasItem("is mandatory"));
         assertThat(errorsMap.get("registrationToday"), hasItem("is mandatory"));
         assertThat(errorsMap.get("registrationDate"), hasItem("is mandatory"));
         assertThat(errorsMap.get("serialNumber"), hasItem("is mandatory"));
     }
 
     @Test
     public void shouldValidateIfRegistrationIsDoneByAProperUserForAnExistingPatientAndFacility() throws Exception {
         final XformHttpClient.XformResponse xformResponse = mobile.upload(MobileForm.registerCWCForm(), new HashMap<String, String>() {{
             put("motechId", "-1");
             put("facilityId", "-1");
             put("staffId", "-1");
             put("registrationToday", RegistrationToday.TODAY.toString());
             put("registrationDate", "2012-01-03");
             put("serialNumber", "1234243243");
         }});
         final List<XformHttpClient.Error> errors = xformResponse.getErrors();
         assertEquals(errors.size(), 1);
         final Map<String, List<String>> errorsMap = errors.iterator().next().getErrors();
 
         assertThat(errorsMap.get("staffId"), hasItem("not found"));
         assertThat(errorsMap.get("facilityId"), hasItem("not found"));
         assertThat(errorsMap.get("motechId"), hasItem("not found"));
     }
 
     @Test
     public void shouldRegisterAPatientForCWCAndMobileMidwifeProgramUsingMobileDeviceAndSearchForItInWeb() {
         DataGenerator dataGenerator = new DataGenerator();
         String staffId = staffGenerator.createStaff(browser, homePage);
 
         TestPatient testPatient = TestPatient.with("First Name" + dataGenerator.randomString(5), staffId)
                 .patientType(TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE)
                 .estimatedDateOfBirth(false)
                 .dateOfBirth(DateUtil.newDate(DateUtil.today().getYear() - 1, 11, 11));
 
         String patientId = patientGenerator.createPatient(testPatient, browser, homePage);
 
        TestCWCEnrollment cwcEnrollment = TestCWCEnrollment.create().withMotechPatientId(patientId).withStaffId(staffId);
         TestMobileMidwifeEnrollment mmEnrollmentDetails = TestMobileMidwifeEnrollment.with(staffId, testPatient.facilityId()).patientId(patientId);
 
         Map<String, String> data = cwcEnrollment.withMobileMidwifeEnrollmentThroughMobile(mmEnrollmentDetails);
        data.put("registrationDate", Utility.nullSafeToString(DateUtil.today().plusDays(10), "yyyy-MM-dd"));
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerCWCForm(), data);
         assertEquals(1, response.getSuccessCount());
 
         PatientEditPage patientEditPage = toPatientEditPage(testPatient);
         CWCEnrollmentPage cwcEnrollmentPage = browser.toEnrollCWCPage(patientEditPage);
         cwcEnrollmentPage.displaying(cwcEnrollment);
 
         patientEditPage = toPatientEditPage(testPatient);
         MobileMidwifeEnrollmentPage mobileMidwifeEnrollmentPage = browser.toMobileMidwifeEnrollmentForm(patientEditPage);
         assertThat(mobileMidwifeEnrollmentPage.details(), is(equalTo(mmEnrollmentDetails)));
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(patientId));
         String encounterId = openMRSPatientPage.chooseEncounter("CWCREGVISIT");
 
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(asList(
                 new OpenMRSObservationVO("PENTA VACCINATION DOSE", "3.0"),
                 new OpenMRSObservationVO("INTERMITTENT PREVENTATIVE TREATMENT INFANTS DOSE", "2.0"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "VITAMIN A"),
                 new OpenMRSObservationVO("SERIAL NUMBER", "serialNumber"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "MEASLES VACCINATION"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "BACILLE CAMILE-GUERIN VACCINATION"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "YELLOW FEVER VACCINATION"),
                 new OpenMRSObservationVO("ORAL POLIO VACCINATION DOSE", "1.0")
         ));
     }
 
     @Test
     public void shouldUnRegisterExistingMobileMidWifeWhileCWCRegistration() {
         DataGenerator dataGenerator = new DataGenerator();
 
         String staffId = staffGenerator.createStaff(browser, homePage);
 
         TestPatient testPatient = TestPatient.with("First Name" + dataGenerator.randomString(5), staffId)
                 .patientType(TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE)
                 .estimatedDateOfBirth(false)
                 .dateOfBirth(DateUtil.newDate(DateUtil.today().getYear() - 1, 11, 11));
 
         String patientId = patientGenerator.createPatient(testPatient, browser, homePage);
 
         PatientEditPage patientEditPage = toPatientEditPage(testPatient);
         MobileMidwifeEnrollmentPage mobileMidwifeEnrollmentPage = browser.toMobileMidwifeEnrollmentForm(patientEditPage);
         mobileMidwifeEnrollmentPage.enroll(TestMobileMidwifeEnrollment.with(staffId));
 
         TestCWCEnrollment cwcEnrollment = TestCWCEnrollment.create().withMotechPatientId(patientId).withStaffId(staffId);
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerCWCForm(), cwcEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
         assertEquals(1, response.getSuccessCount());
 
         PatientEditPage patientPageAfterEdit = toPatientEditPage(testPatient);
         mobileMidwifeEnrollmentPage = browser.toMobileMidwifeEnrollmentForm(patientPageAfterEdit);
 
 
         assertThat(mobileMidwifeEnrollmentPage.status(),is("INACTIVE"));
     }
 
     @Test
     public void shouldCreatePentaScheduleDuringCWCRegistrationIfTheTodayFallsWithin10WeeksFromDateOfBirth() {
         String staffId = staffGenerator.createStaff(browser, homePage);
         TestPatient patient = TestPatient.with("name", staffId).dateOfBirth(today().minusWeeks(5)).patientType(CHILD_UNDER_FIVE);
         String patientId = patientGenerator.createPatient(patient, browser, homePage);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         LocalDate registrationDate = DateUtil.today();
 
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.createWithoutHistory().withMotechPatientId(patientId).withStaffId(staffId)
                 .withRegistrationDate(registrationDate);
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerCWCForm(), testCWCEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
         assertThat(join(response.getErrors(), on(XformHttpClient.Error.class).toString()), response.getErrors().size(), is(equalTo(0)));
 
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PENTA).getAlertAsLocalDate(), expectedFirstAlertDate(CWC_PENTA, patient.dateOfBirth())
         );
     }
 
     @Test
     public void shouldNotCreatePentaScheduleDuringCWCRegistrationIfTheTodayFallsAfter10WeeksFromDateOfBirth() {
         String staffId = staffGenerator.createStaff(browser, homePage);
         TestPatient patient = TestPatient.with("name-new", staffId).dateOfBirth(today().minusWeeks(11)).patientType(CHILD_UNDER_FIVE);
         String patientId = patientGenerator.createPatient(patient, browser, homePage);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         LocalDate registrationDate = DateUtil.today();
 
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.createWithoutHistory().withMotechPatientId(patientId).withStaffId(staffId)
                 .withRegistrationDate(registrationDate);
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerCWCForm(), testCWCEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
         assertThat(join(response.getErrors(), on(XformHttpClient.Error.class).toString()), response.getErrors().size(), is(equalTo(0)));
 
         assertNull(scheduleTracker.activeEnrollment(openMRSId, CWC_PENTA));
     }
 
     @Test
     public void shouldCreateScheduleFromAppropriateMilestoneIfHistoryIsProvided(){
         String staffId = staffGenerator.createStaff(browser, homePage);
         LocalDate dateOfBirth = DateUtil.newDate(2012, 4, 3);
         TestPatient patient = TestPatient.with("name-new", staffId).dateOfBirth(dateOfBirth).patientType(CHILD_UNDER_FIVE);
         String patientId = patientGenerator.createPatient(patient, browser, homePage);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         LocalDate registrationDate = DateUtil.newDate(2012, 4, 18);
 
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.create().withMotechPatientId(patientId).withStaffId(staffId)
                 .withRegistrationDate(registrationDate).withAddHistory(true).withAddCareHistory(asList(CwcCareHistory.PENTA,CwcCareHistory.IPTI,CwcCareHistory.OPV))
                 .withLastIPTi("1").withLastIPTiDate(DateUtil.newDate(2012, 4, 16)).withLastPenta("1").withLastPentaDate(DateUtil.newDate(2012, 4, 16)).withLastOPV("1").withLastOPVDate(DateUtil.newDate(2012, 4, 16));
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerCWCForm(), testCWCEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
         assertThat(join(response.getErrors(), on(XformHttpClient.Error.class).toString()), response.getErrors().size(), is(equalTo(0)));
 
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PENTA).getAlertAsLocalDate(), expectedAlertDateFor(CWC_PENTA, testCWCEnrollment.getLastPentaDate(), "Penta2"));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_IPT_VACCINE).getAlertAsLocalDate(), expectedAlertDateFor(CWC_IPT_VACCINE, testCWCEnrollment.getLastIPTiDate(), "IPTi2"));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_OPV_OTHERS).getAlertAsLocalDate(), expectedAlertDateFor(CWC_OPV_OTHERS, testCWCEnrollment.getLastOPVDate(), "OPV2"));
     }
 
     private LocalDate expectedFirstAlertDate(String scheduleName, LocalDate referenceDate) {
         return scheduleTracker.firstAlert(scheduleName, referenceDate);
     }
 
     private LocalDate expectedAlertDateFor(String scheduleName, LocalDate referenceDate, String milestoneName) {
         return scheduleTracker.alertFor(scheduleName, referenceDate, milestoneName);
     }
 
     private PatientEditPage toPatientEditPage(TestPatient testPatient) {
         SearchPatientPage searchPatientPage = browser.toSearchPatient();
         searchPatientPage.searchWithName(testPatient.firstName());
         searchPatientPage.displaying(testPatient);
         return browser.toPatientEditPage(searchPatientPage, testPatient);
     }
 
 }
