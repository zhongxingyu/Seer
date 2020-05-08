 package org.motechproject.ghana.national.functional.mobile;
 
 import junit.framework.Assert;
 import org.apache.commons.collections.MapUtils;
 import org.joda.time.LocalDate;
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.configuration.ScheduleNames;
 import org.motechproject.ghana.national.domain.CwcCareHistory;
 import org.motechproject.ghana.national.domain.IPTiDose;
 import org.motechproject.ghana.national.domain.OPVDose;
 import org.motechproject.ghana.national.domain.PneumococcalDose;
 import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.domain.RotavirusDose;
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
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.is;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_IPT_VACCINE;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_OPV_0;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_OPV_OTHERS;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_PENTA;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_PNEUMOCOCCAL;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_ROTAVIRUS;
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
 
     @Autowired
     DataGenerator dataGenerator;
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
         String staffId = staffGenerator.createStaff(browser, homePage);
 
         TestPatient testPatient = TestPatient.with("First Name" + dataGenerator.randomString(5), staffId)
                 .patientType(TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE)
                 .estimatedDateOfBirth(false)
                 .dateOfBirth(DateUtil.newDate(DateUtil.today().getYear() - 1, 11, 11));
 
         String patientId = patientGenerator.createPatient(testPatient, browser, homePage);
 
         LocalDate registrationDate = DateUtil.today();
         TestCWCEnrollment cwcEnrollment = TestCWCEnrollment.create().withMotechPatientId(patientId).withStaffId(staffId).withRegistrationDate(registrationDate)
                                             .withLastPneumococcal("1").withHistoryDays3WeeksBeforeRegistrationDate(3);
 
         TestMobileMidwifeEnrollment mmEnrollmentDetails = TestMobileMidwifeEnrollment.with(staffId, testPatient.facilityId()).patientId(patientId);
 
         Map<String, String> data = cwcEnrollment.withMobileMidwifeEnrollmentThroughMobile(mmEnrollmentDetails);
         data.put("registrationDate", Utility.nullSafeToString(registrationDate, "yyyy-MM-dd"));
 
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
                new OpenMRSObservationVO("VITAMIN A", "blue"),
                 new OpenMRSObservationVO("SERIAL NUMBER", "serialNumber"),
                new OpenMRSObservationVO("MEASLES VACCINATION", "1.0"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "BACILLE CAMILE-GUERIN VACCINATION"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "YELLOW FEVER VACCINATION"),
                 new OpenMRSObservationVO("ORAL POLIO VACCINATION DOSE", "1.0"),
                 new OpenMRSObservationVO("PNEUMOCOCCAL", "1.0")
         ));
     }
 
     @Test
     public void shouldCreatePentaRotavirusAndPneumococcalScheduleDuringCWCRegistrationIfTheTodayFallsWithin10WeeksFromDateOfBirth() {
         String staffId = staffGenerator.createStaff(browser, homePage);
         TestPatient patient = TestPatient.with("name", staffId).dateOfBirth(today().minusWeeks(5)).patientType(CHILD_UNDER_FIVE);
         String patientId = patientGenerator.createPatient(patient, browser, homePage);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         LocalDate registrationDate = DateUtil.today();
 
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.createWithoutHistory().withMotechPatientId(patientId).withStaffId(staffId)
                 .withRegistrationDate(registrationDate);
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerCWCForm(), testCWCEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
         assertThat(join(response.getErrors(), on(XformHttpClient.Error.class).toString()), response.getErrors().size(), is(equalTo(0)));
 
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PENTA.getName()).getAlertAsLocalDate(), expectedFirstAlertDate(CWC_PENTA.getName(), patient.dateOfBirth()));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_ROTAVIRUS.getName()).getAlertAsLocalDate(), expectedFirstAlertDate(CWC_PENTA.getName(), patient.dateOfBirth()));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PNEUMOCOCCAL.getName()).getAlertAsLocalDate(), expectedFirstAlertDate(CWC_PNEUMOCOCCAL.getName(), patient.dateOfBirth()));
     }
 
     @Test
     public void shouldNotCreatePentaAndRotavirusScheduleDuringCWCRegistrationIfTheTodayFallsAfter10WeeksFromDateOfBirth() {
         String staffId = staffGenerator.createStaff(browser, homePage);
         TestPatient patient = TestPatient.with("name-new", staffId).dateOfBirth(today().minusWeeks(11)).patientType(CHILD_UNDER_FIVE);
         String patientId = patientGenerator.createPatient(patient, browser, homePage);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         LocalDate registrationDate = DateUtil.today();
 
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.createWithoutHistory().withMotechPatientId(patientId).withStaffId(staffId)
                 .withRegistrationDate(registrationDate);
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerCWCForm(), testCWCEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
         assertThat(join(response.getErrors(), on(XformHttpClient.Error.class).toString()), response.getErrors().size(), is(equalTo(0)));
 
         assertNull(scheduleTracker.activeEnrollment(openMRSId, CWC_PENTA.getName()));
         assertNull(scheduleTracker.activeEnrollment(openMRSId, CWC_ROTAVIRUS.getName()));
     }
 
     @Test
     public void shouldCreateScheduleFromAppropriateMilestoneIfHistoryIsProvided() {
         String staffId = staffGenerator.createStaff(browser, homePage);
         LocalDate dateOfBirth = DateUtil.newDate(2012, 4, 3);
         TestPatient patient = TestPatient.with("name-new", staffId).dateOfBirth(dateOfBirth).patientType(CHILD_UNDER_FIVE);
         String patientId = patientGenerator.createPatient(patient, browser, homePage);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         LocalDate registrationDate = DateUtil.newDate(2012, 4, 18);
 
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.create().withMotechPatientId(patientId).withStaffId(staffId)
                 .withRegistrationDate(registrationDate).withAddHistory(true).withAddCareHistory(asList(CwcCareHistory.PENTA,CwcCareHistory.IPTI,CwcCareHistory.OPV))
                 .withLastIPTi("1").withLastIPTiDate(DateUtil.newDate(2012, 4, 16))
                 .withLastPenta("1").withLastPentaDate(DateUtil.newDate(2012, 4, 16))
                 .withLastOPV("1").withLastOPVDate(DateUtil.newDate(2012, 4, 16))
                 .withLastPneumococcal("1").withLastPneumococcalDate(DateUtil.newDate(2012, 4, 16))
                 .withLastRotavirus("1").withLastRotavirusDate(DateUtil.newDate(2012, 4, 16));
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerCWCForm(), testCWCEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
         assertThat(join(response.getErrors(), on(XformHttpClient.Error.class).toString()), response.getErrors().size(), is(equalTo(0)));
 
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PENTA.getName()).getAlertAsLocalDate(), today().plusWeeks(1));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_IPT_VACCINE.getName()).getAlertAsLocalDate(), today().plusWeeks(1));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_OPV_OTHERS.getName()).getAlertAsLocalDate(), today().plusWeeks(1));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PNEUMOCOCCAL.getName()).getAlertAsLocalDate(), today().plusWeeks(1));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_ROTAVIRUS.getName()).getAlertAsLocalDate(), today().plusWeeks(1));
     }
 
     @Test
     public void shouldCreateSchedulesForPatientTypeOtherWithCWCHistory(){
         LocalDate registrationDate = today();
         LocalDate dateOfBirth = registrationDate.minusYears(2).minusWeeks(25);
 
         String staffId = staffGenerator.createStaff(browser, homePage);
         TestPatient testPatient = TestPatient.with("OtherPatient "+dataGenerator.randomString(4),staffId)
                 .registrationDate(registrationDate).patientType(TestPatient.PATIENT_TYPE.OTHER).dateOfBirth(dateOfBirth);
         String patientId = patientGenerator.createPatient(testPatient, browser, homePage);
 
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.create().withStaffId(staffId).withMotechPatientId(patientId)
                 .withRegistrationDate(testPatient.getRegistrationDate())
                 .withLastIPTi("1").withLastIPTiDate(dateOfBirth.plusDays(10))
                 .withLastOPV("1").withLastOPVDate(dateOfBirth.plusDays(2))
                 .withLastPneumococcal("1").withLastPneumococcalDate(dateOfBirth.plusDays(2))
                 .withLastRotavirus("1").withLastRotavirusDate(dateOfBirth.plusDays(2))
                 .withLastPenta("1").withLastPentaDate(dateOfBirth.plusDays(2))
                 .withLastYellowFeverDate(null)
                 .withLastMeaslesDate(null)
                 .withLastBcgDate(null)
                 .withLastVitaminADate(null);
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerCWCForm(), testCWCEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
 
         assertEquals(1,response.getSuccessCount());
 
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_IPT_VACCINE.getName()).getAlertAsLocalDate(), Utility.getNextOf(IPTiDose.byValue(Integer.parseInt(testCWCEnrollment.getLastIPTi()))).milestoneName(),
                 today().plusWeeks(1), IPTiDose.IPTi2.milestoneName());
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, ScheduleNames.CWC_OPV_OTHERS.getName()).getAlertAsLocalDate(), Utility.getNextOf(OPVDose.byValue(testCWCEnrollment.getLastOPV())).name(),
                 today().plusWeeks(1), OPVDose.OPV_2.name());
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, ScheduleNames.CWC_PNEUMOCOCCAL.getName()).getAlertAsLocalDate(), Utility.getNextOf(PneumococcalDose.byValue(Integer.parseInt(testCWCEnrollment.getLastPneumococcal()))).name(),
                 today().plusWeeks(1), PneumococcalDose.PNEUMO2.name());
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, ScheduleNames.CWC_ROTAVIRUS.getName()).getAlertAsLocalDate(), Utility.getNextOf(RotavirusDose.byValue(Integer.parseInt(testCWCEnrollment.getLastRotavirus()))).name(),
                 today().plusWeeks(1), RotavirusDose.ROTAVIRUS2.name());
     }
 
     @Test
     public void shouldCreateCareHistoryEvenIfCareDateIsEqualToDateOfBirthOfChild() {
         String staffId = staffGenerator.createStaff(browser, homePage);
         LocalDate dateOfBirth = DateUtil.today();
         TestPatient patient = TestPatient.with(dataGenerator.randomString(8), staffId).patientType(TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE).dateOfBirth(dateOfBirth);
         String patientId = patientGenerator.createPatient(patient, browser, homePage);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         LocalDate registrationDate = DateUtil.today();
 
         TestCWCEnrollment cwcEnrollment = TestCWCEnrollment.createWithoutHistory()
                 .withMotechPatientId(patientId).withStaffId(staffId).withRegistrationDate(registrationDate)
                 .withAddHistory(true).withLastOPV("0").withLastOPVDate(dateOfBirth).withLastIPTi("2").withLastIPTiDate(dateOfBirth);
 
         mobile.upload(MobileForm.registerCWCForm(), cwcEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
         Assert.assertNull(scheduleTracker.activeEnrollment(openMRSId, CWC_OPV_0.getName()));
         Assert.assertNotNull(scheduleTracker.activeEnrollment(openMRSId, CWC_OPV_OTHERS.getName()));
         Assert.assertNotNull(scheduleTracker.activeEnrollment(openMRSId, CWC_IPT_VACCINE.getName()));
     }
 
     private LocalDate expectedFirstAlertDate(String scheduleName, LocalDate referenceDate) {
         return scheduleTracker.firstAlert(scheduleName, referenceDate);
     }
 
     private PatientEditPage toPatientEditPage(TestPatient testPatient) {
         SearchPatientPage searchPatientPage = browser.toSearchPatient();
         searchPatientPage.searchWithName(testPatient.firstName());
         searchPatientPage.displaying(testPatient);
         return browser.toPatientEditPage(searchPatientPage, testPatient);
     }
 
 }
