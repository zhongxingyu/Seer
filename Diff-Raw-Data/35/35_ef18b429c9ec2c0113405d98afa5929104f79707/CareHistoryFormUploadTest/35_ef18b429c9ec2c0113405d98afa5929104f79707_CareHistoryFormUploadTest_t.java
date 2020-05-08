 package org.motechproject.ghana.national.functional.mobile;
 
 import org.joda.time.LocalDate;
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.functional.OpenMRSAwareFunctionalTest;
 import org.motechproject.ghana.national.functional.data.TestANCEnrollment;
 import org.motechproject.ghana.national.functional.data.TestCWCEnrollment;
 import org.motechproject.ghana.national.functional.data.TestCareHistory;
 import org.motechproject.ghana.national.functional.data.TestPatient;
 import org.motechproject.ghana.national.functional.framework.OpenMRSDB;
 import org.motechproject.ghana.national.functional.framework.ScheduleTracker;
 import org.motechproject.ghana.national.functional.framework.XformHttpClient;
 import org.motechproject.ghana.national.functional.helper.ScheduleHelper;
 import org.motechproject.ghana.national.functional.mobileforms.MobileForm;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSEncounterPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSPatientPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.vo.OpenMRSObservationVO;
 import org.motechproject.ghana.national.vo.Pregnancy;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.Test;
 
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import static ch.lambdaj.Lambda.join;
 import static ch.lambdaj.Lambda.on;
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNull;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.joda.time.format.DateTimeFormat.forPattern;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.*;
 import static org.motechproject.ghana.national.domain.IPTDose.SP2;
 import static org.motechproject.ghana.national.functional.data.TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE;
 import static org.motechproject.util.DateUtil.today;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class CareHistoryFormUploadTest extends OpenMRSAwareFunctionalTest {
 
     @Autowired
     ScheduleTracker scheduleTracker;
 
     @Autowired
     private OpenMRSDB openMRSDB;
 
     @Test
     public void shouldUploadCareHistoryFormWithRelevantANCDetails() {
         final String staffId = staffGenerator.createStaff(browser, homePage);
         final String patientId = patientGenerator.createPatient(browser, homePage, staffId);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         final String ttDate = new SimpleDateFormat("yyyy-MM-dd").format(DateUtil.newDate(2012, 1, 1).toDate());
         final String iptDate = new SimpleDateFormat("yyyy-MM-dd").format(DateUtil.newDate(2011, 12, 15).toDate());
         final String date = new SimpleDateFormat("yyyy-MM-dd").format(DateUtil.newDate(2012, 1, 15).toDate());
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.careHistoryForm(), new HashMap<String, String>() {{
             put("staffId", staffId);
             put("facilityId", "13212");
             put("motechId", patientId);
             put("date", date);
             put("addHistory", "IPT_SP,TT");
             put("lastIPT", "1");
             put("lastIPTDate", iptDate);
             put("lastTT", "2");
             put("lastTTDate", ttDate);
         }});
 
         assertEquals(1, response.getSuccessCount());
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSId);
         String encounterId = openMRSPatientPage.chooseEncounter("PATIENTHISTORY");
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(Arrays.<OpenMRSObservationVO>asList(
                 new OpenMRSObservationVO("INTERMITTENT PREVENTATIVE TREATMENT DOSE", "1.0"),
                 new OpenMRSObservationVO("TETANUS TOXOID DOSE", "2.0")
         ));
     }
 
     @Test
     public void shouldUploadCareHistoryFormWithRelevantCWCDetails() {
         final String staffId = staffGenerator.createStaff(browser, homePage);
         final String patientId = patientGenerator.createPatient(browser, homePage, staffId);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         final LocalDate vaccinationDate = DateUtil.newDate(2012, 1, 1);
         final LocalDate date = DateUtil.newDate(2012, 1, 15);
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.careHistoryForm(), new HashMap<String, String>() {{
             put("staffId", staffId);
             put("facilityId", "13212");
             put("motechId", patientId);
             put("date", dateToString(date));
             put("addHistory", "VITA_A,IPTI,BCG,OPV,PENTA,MEASLES,YF,ROTAVIRUS,PNEUMOCOCCAL");
             put("bcgDate", dateToString(vaccinationDate));
             put("lastOPV", "1");
             put("lastOPVDate", dateToString(vaccinationDate));
             put("lastPenta", "1");
             put("lastPentaDate", dateToString(vaccinationDate));
             put("lastMeasles", "1");
             put("measlesDate", dateToString(vaccinationDate));
             put("yellowFeverDate", dateToString(vaccinationDate));
             put("lastIPTI", "1");
             put("lastRotavirus", "1");
             put("lastRotavirusDate", dateToString(vaccinationDate));
             put("lastPneumococcal", "1");
             put("lastPneumococcalDate", dateToString(vaccinationDate));
             put("lastIPTIDate", dateToString(vaccinationDate));
             put("lastVitaminA", "red");
             put("lastVitaminADate", dateToString(vaccinationDate));
         }});
 
         assertEquals(1, response.getSuccessCount());
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSId);
         String encounterId = openMRSPatientPage.chooseEncounter("PATIENTHISTORY");
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(Arrays.<OpenMRSObservationVO>asList(
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "BACILLE CAMILE-GUERIN VACCINATION"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "YELLOW FEVER VACCINATION"),
                new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "VITAMIN A RED"),
                new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "MEASLES VACCINATION 1"),
                 new OpenMRSObservationVO("INTERMITTENT PREVENTATIVE TREATMENT INFANTS DOSE", "1.0"),
                 new OpenMRSObservationVO("ORAL POLIO VACCINATION DOSE", "1.0"),
                 new OpenMRSObservationVO("ROTAVIRUS", "1.0"),
                 new OpenMRSObservationVO("PNEUMOCOCCAL", "1.0"),
                 new OpenMRSObservationVO("PENTA VACCINATION DOSE", "1.0")
         ));
     }
 
     private String dateToString(LocalDate date) {
         return date.toString(forPattern("yyyy-MM-dd"));
     }
 
 
     @Test
     public void shouldCreateSchedulesWhileHistoryFormUploadIfThereAreNoActiveSchedulesAndIfTheHistoryIsAssociatedWithTheCurrentPregnancy() {
         String staffId = staffGenerator.createStaff(browser, homePage);
         String patientId = patientGenerator.createPatient(browser, homePage, staffId);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         LocalDate registrationDate = DateUtil.today();
 
         Pregnancy pregnancyIn12thWeekOfPregnancy = Pregnancy.basedOnConceptionDate(DateUtil.today().minusWeeks(22));
 
         TestANCEnrollment ancEnrollment = TestANCEnrollment.createWithoutHistory().withMotechPatientId(patientId).withStaffId(staffId)
                 .withEstimatedDateOfDelivery(pregnancyIn12thWeekOfPregnancy.dateOfDelivery()).withRegistrationDate(registrationDate);
 
         mobile.upload(MobileForm.registerANCForm(), ancEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
 
         final LocalDate vaccineHistoryDate = registrationDate.minusWeeks(5);
 
         TestCareHistory careHistory = TestCareHistory.withoutHistory(patientId).staffId(staffId)
                 .withIPT("1", vaccineHistoryDate).withTT("2", vaccineHistoryDate).withDate(registrationDate);
 
         mobile.upload(MobileForm.careHistoryForm(), careHistory.forMobile());
 
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, ANC_IPT_VACCINE.getName()).getAlertAsLocalDate(),
                 expectedFirstAlertDate(SP2.milestone(), ANC_IPT_VACCINE.getName(), vaccineHistoryDate)
         );
     }
 
     @Test
     public void shouldCreateSchedulesWhileHistoryFormUploadOnlyIfThereAreNoActiveSchedulesDuringCWCHistoryUpload() {
         String staffId = staffGenerator.createStaff(browser, homePage);
         TestPatient patient = TestPatient.with("name", staffId).dateOfBirth(today().minusDays(5)).patientType(CHILD_UNDER_FIVE);
         String patientId = patientGenerator.createPatient(patient, browser, homePage);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         LocalDate registrationDate = DateUtil.today();
 
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.createWithoutHistory().withMotechPatientId(patientId).withStaffId(staffId)
                 .withRegistrationDate(registrationDate);
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerCWCForm(), testCWCEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
         assertThat(join(response.getErrors(), on(XformHttpClient.Error.class).toString()), response.getErrors().size(), is(equalTo(0)));
 
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PENTA.getName()).getAlertAsLocalDate(),
                 expectedFirstAlertDate(CWC_PENTA.getName(), patient.dateOfBirth()));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_ROTAVIRUS.getName()).getAlertAsLocalDate(),
                 expectedFirstAlertDate(CWC_ROTAVIRUS.getName(), patient.dateOfBirth()));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PNEUMOCOCCAL.getName()).getAlertAsLocalDate(),
                 expectedFirstAlertDate(CWC_PNEUMOCOCCAL.getName(), patient.dateOfBirth()));
 
 
         TestCareHistory careHistory = TestCareHistory.withoutHistory(patientId).staffId(staffId)
                 .withPenta("1", today().minusDays(3)).withDate(registrationDate)
                 .withRotavirus("1", today().minusDays(4))
                 .withPneumococcal("1", today().minusDays(5));
 
         response = mobile.upload(MobileForm.careHistoryForm(), careHistory.forMobile());
         assertThat(join(response.getErrors(), on(XformHttpClient.Error.class).toString()), response.getErrors().size(), is(equalTo(0)));
 
         // schedule dates should not change
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PENTA.getName()).getAlertAsLocalDate(),
                 expectedFirstAlertDate(CWC_PENTA.getName(), patient.dateOfBirth()));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_ROTAVIRUS.getName()).getAlertAsLocalDate(),
                 expectedFirstAlertDate(CWC_ROTAVIRUS.getName(), patient.dateOfBirth()));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PNEUMOCOCCAL.getName()).getAlertAsLocalDate(),
                 expectedFirstAlertDate(CWC_PNEUMOCOCCAL.getName(), patient.dateOfBirth()));
     }
 
     @Test
     public void shouldCreateSchedulesWhileHistoryFormUploadOnlyIfThereAreNoActiveSchedulesDuringANCHistoryUpload() {
         String staffId = staffGenerator.createStaff(browser, homePage);
         String patientId = patientGenerator.createPatient(browser, homePage, staffId);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         LocalDate registrationDate = DateUtil.today();
 
         Pregnancy pregnancyIn12thWeekOfPregnancy = Pregnancy.basedOnConceptionDate(DateUtil.today().minusWeeks(12));
         TestANCEnrollment ancEnrollment = TestANCEnrollment.createWithoutHistory().withMotechPatientId(patientId).withStaffId(staffId)
                 .withEstimatedDateOfDelivery(pregnancyIn12thWeekOfPregnancy.dateOfDelivery()).withRegistrationDate(registrationDate).addHistoryDetails();
 
         mobile.upload(MobileForm.registerANCForm(), ancEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
 
 
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, ANC_IPT_VACCINE.getName()).getAlertAsLocalDate(),
                 expectedFirstAlertDate(ANC_IPT_VACCINE.getName(), pregnancyIn12thWeekOfPregnancy.dateOfConception())
         );
 
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, TT_VACCINATION.getName()).getAlertAsLocalDate(),
                 expectedFirstAlertDate(TT_VACCINATION.getName(), registrationDate)
         );
 
         final LocalDate dateAfterConception = pregnancyIn12thWeekOfPregnancy.dateOfConception().plusWeeks(3);
 
         TestCareHistory careHistory = TestCareHistory.withoutHistory(patientId).staffId(staffId)
                 .withIPT("1", dateAfterConception).withTT("2", dateAfterConception).withDate(registrationDate);
 
         mobile.upload(MobileForm.careHistoryForm(), careHistory.forMobile());
 
         // schedule dates should not change
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, ANC_IPT_VACCINE.getName()).getAlertAsLocalDate(),
                 expectedFirstAlertDate(ANC_IPT_VACCINE.getName(), pregnancyIn12thWeekOfPregnancy.dateOfConception()));
 
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, TT_VACCINATION.getName()).getAlertAsLocalDate(),
                 expectedFirstAlertDate(TT_VACCINATION.getName(), registrationDate));
     }
 
     @Test
     public void shouldNotCreateSchedulesIfHistoryIsCapturedBeforeANC() {
         String staffId = staffGenerator.createStaff(browser, homePage);
         String patientId = patientGenerator.createPatient(browser, homePage, staffId);
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
 
         final LocalDate registrationDate = today();
         TestCareHistory careHistory = TestCareHistory.withoutHistory(patientId).staffId(staffId).withDate(registrationDate);
         careHistory.withIPT("1", registrationDate.minusWeeks(5));
         careHistory.withTT("2", registrationDate.minusWeeks(5));
 
         final XformHttpClient.XformResponse response = mobile.upload(MobileForm.careHistoryForm(), careHistory.forMobile());
         assertThat(join(response.getErrors(), on(XformHttpClient.Error.class).toString()), response.getErrors().size(), is(equalTo(0)));
 
         assertNull(scheduleTracker.activeEnrollment(openMRSId, ANC_IPT_VACCINE.getName()));
         assertNull(scheduleTracker.activeEnrollment(openMRSId, TT_VACCINATION.getName()));
     }
 
 
     private LocalDate expectedFirstAlertDate(String scheduleName, LocalDate referenceDate) {
         return scheduleTracker.firstAlert(scheduleName, referenceDate);
     }
 
     private LocalDate expectedFirstAlertDate(String milestoneName, String scheduleName, LocalDate referenceDate) {
         return scheduleTracker.firstAlert(scheduleName, referenceDate, milestoneName);
     }
 
 }
