 package org.motechproject.ghana.national.functional.mobile;
 
 import org.joda.time.LocalDate;
 import org.junit.runner.RunWith;
 import org.motechproject.appointments.api.EventKeys;
 import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.functional.OpenMRSAwareFunctionalTest;
 import org.motechproject.ghana.national.functional.data.TestANCEnrollment;
 import org.motechproject.ghana.national.functional.data.TestPatient;
 import org.motechproject.ghana.national.functional.framework.XformHttpClient;
 import org.motechproject.ghana.national.functional.mobileforms.MobileForm;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSEncounterPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSPatientPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.vo.OpenMRSObservationVO;
 import org.motechproject.ghana.national.functional.pages.patient.ANCEnrollmentPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientEditPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientPage;
 import org.motechproject.ghana.national.functional.pages.patient.SearchPatientPage;
 import org.motechproject.ghana.national.functional.util.DataGenerator;
 import org.motechproject.scheduler.MotechSchedulerServiceImpl;
 import org.motechproject.util.DateUtil;
 import org.quartz.*;
 import org.quartz.impl.matchers.GroupMatcher;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.Test;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import static java.lang.String.format;
 import static java.util.Arrays.asList;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.motechproject.util.DateUtil.today;
 import static org.testng.AssertJUnit.assertEquals;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class ANCVisitFormUploadTest extends OpenMRSAwareFunctionalTest {
 
     @Autowired
     private SchedulerFactoryBean schedulerFactoryBean;
 
     @Test
     public void shouldUploadANCVisitFormSuccessfully() throws SchedulerException, ParseException {
         // create
         final String staffId = staffGenerator.createStaff(browser, homePage);
 
         DataGenerator dataGenerator = new DataGenerator();
         String patientFirstName = "patient first name" + dataGenerator.randomString(5);
         final TestPatient testPatient = TestPatient.with(patientFirstName, staffId)
                 .patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER)
                 .estimatedDateOfBirth(false);
 
         PatientPage patientPage = browser.toCreatePatient(homePage);
         patientPage.create(testPatient);
 
         LocalDate estimatedDateOfDelivery = today().plusMonths(6);
         LocalDate registrationDate = today().minusDays(4);
         TestANCEnrollment ancEnrollment = TestANCEnrollment.create().withStaffId(staffId)
                 .withRegistrationToday(RegistrationToday.IN_PAST).withRegistrationDate(registrationDate).withEstimatedDateOfDelivery(estimatedDateOfDelivery);
         SearchPatientPage searchPatientPage = browser.toSearchPatient(patientPage);
 
         searchPatientPage.searchWithName(patientFirstName);
         searchPatientPage.displaying(testPatient);
 
         PatientEditPage patientEditPage = browser.toPatientEditPage(searchPatientPage, testPatient);
         ANCEnrollmentPage ancEnrollmentPage = browser.toEnrollANCPage(patientEditPage);
         ancEnrollmentPage.save(ancEnrollment);
 
         final LocalDate nextANCVisitDate = today().plusWeeks(6);
         XformHttpClient.XformResponse xformResponse = createAncVisit(staffId, testPatient, ancEnrollmentPage, nextANCVisitDate);
         verifyAncVisitSchedules(ancEnrollmentPage, xformResponse, nextANCVisitDate.minusWeeks(1).toDate(),
                 nextANCVisitDate.toDate(), nextANCVisitDate.plusWeeks(1).toDate(), nextANCVisitDate.plusWeeks(2).toDate());
 
         String motechId = ancEnrollmentPage.motechId();
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(motechId));
 
         String encounterId = openMRSPatientPage.chooseEncounter("ANCVISIT");
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(asList(
                 new OpenMRSObservationVO("WEIGHT (KG)", "65.67"),
                 new OpenMRSObservationVO("URINE GLUCOSE TEST", "POSITIVE"),
                 new OpenMRSObservationVO("HIV POST-TEST COUNSELING", "true"),
                 new OpenMRSObservationVO("HIV TEST RESULT", "POSITIVE"),
                 new OpenMRSObservationVO("PMTCT TREATMENT", "true"),
                 new OpenMRSObservationVO("PMTCT", "true"),
                 new OpenMRSObservationVO("MALE INVOLVEMENT", "false"),
                 new OpenMRSObservationVO("VDRL TREATMENT", "true"),
                 new OpenMRSObservationVO("COMMENTS", "comments"),
                 new OpenMRSObservationVO("ESTIMATED DATE OF CONFINEMENT", formatDate(today().plusMonths(5))),
                 new OpenMRSObservationVO("PREGNANCY STATUS", "true"),
                 new OpenMRSObservationVO("DATE OF CONFINEMENT CONFIRMED", "true"),
                 new OpenMRSObservationVO("TETANUS TOXOID DOSE", "1.0"),
                 new OpenMRSObservationVO("IPT REACTION", "REACTIVE"),
                 new OpenMRSObservationVO("URINE PROTEIN TEST", "POSITIVE"),
                 new OpenMRSObservationVO("COMMUNITY", "community"),
                 new OpenMRSObservationVO("SERIAL NUMBER", "4ds65"),
                 new OpenMRSObservationVO("INTERMITTENT PREVENTATIVE TREATMENT DOSE", "1.0"),
                 new OpenMRSObservationVO("FUNDAL HEIGHT", "4.3"),
                 new OpenMRSObservationVO("SYSTOLIC BLOOD PRESSURE", "10.0"),
                 new OpenMRSObservationVO("HEMOGLOBIN", "13.8"),
                 new OpenMRSObservationVO("FETAL HEART RATE", "4.0"),
                 new OpenMRSObservationVO("HIV PRE-TEST COUNSELING", "true"),
                 new OpenMRSObservationVO("VDRL", "NON-REACTIVE"),
                 new OpenMRSObservationVO("VISIT NUMBER", "4.0"),
                 new OpenMRSObservationVO("ANC PNC LOCATION", "2.0"),
                 new OpenMRSObservationVO("REFERRED", "true"),
                 new OpenMRSObservationVO("DEWORMER", "true"),
                 new OpenMRSObservationVO("NEXT ANC DATE", formatDate(nextANCVisitDate)),
                 new OpenMRSObservationVO("DIASTOLIC BLOOD PRESSURE", "67.0"),
                 new OpenMRSObservationVO("INSECTICIDE TREATED NET USAGE", "false"),
                 new OpenMRSObservationVO("HOUSE", "house")
         ));
 
 
         searchPatientPage = browser.toSearchPatient(browser.gotoHomePage());
         searchPatientPage.searchWithName(patientFirstName);
 
         patientEditPage = browser.toPatientEditPage(searchPatientPage, testPatient);
         ancEnrollmentPage = browser.toEnrollANCPage(patientEditPage);
 
         LocalDate newANCVisitDate = DateUtil.today().plusDays(35);
         xformResponse = createAncVisit(staffId, testPatient, ancEnrollmentPage, newANCVisitDate);
         verifyAncVisitSchedules(ancEnrollmentPage, xformResponse, newANCVisitDate.minusWeeks(1).toDate(),
                 newANCVisitDate.toDate(), newANCVisitDate.plusWeeks(1).toDate(), newANCVisitDate.plusWeeks(2).toDate());
     }
 
     private XformHttpClient.XformResponse createAncVisit(final String staffId, final TestPatient testPatient, final ANCEnrollmentPage ancEnrollmentPage, final LocalDate nextANCVisitDate) {
         final LocalDate updatedEDD = today().plusMonths(5);
         return mobile.upload(MobileForm.ancVisitForm(), new HashMap<String, String>() {{
             put("staffId", staffId);
             put("facilityId", testPatient.facilityId());
             put("motechId", ancEnrollmentPage.getMotechPatientId());
             put("date",new SimpleDateFormat("yyyy-MM-dd").format(today().toDate()));
             put("estDeliveryDate", new SimpleDateFormat("yyyy-MM-dd").format(updatedEDD.toDate()));
             put("serialNumber", "4ds65");
             put("visitNumber", "4");
             put("bpDiastolic", "67");
             put("bpSystolic", "10");
             put("weight", "65.67");
             put("comments", "comments");
             put("ttdose", "1");
             put("iptdose", "1");
             put("iptReactive", "Y");
             put("itnUse", "N");
             put("fht", "4.3");
             put("fhr", "4");
             put("urineTestGlucosePositive", "1");
             put("urineTestProteinPositive", "1");
             put("hemoglobin", "13.8");
             put("vdrlReactive", "N");
             put("vdrlTreatment", "Y");
             put("dewormer", "Y");
             put("pmtct", "Y");
             put("preTestCounseled", "Y");
             put("hivTestResult", "POSITIVE");
             put("postTestCounseled", "Y");
             put("preTestCounseled", "Y");
             put("pmtctTreament", "Y");
             put("location", "2");
             put("house", "house");
             put("community", "community");
             put("referred", "Y");
             put("maleInvolved", "N");
             put("nextANCDate", new SimpleDateFormat("yyyy-MM-dd").format(nextANCVisitDate.toDate()));
         }});
     }
 
     private void verifyAncVisitSchedules(ANCEnrollmentPage ancEnrollmentPage, XformHttpClient.XformResponse xformResponse, Date minDate, Date dueDate, Date lateDate1, Date lateDate2) throws SchedulerException {
         assertEquals(1, xformResponse.getSuccessCount());
         List<CronTrigger> cronTriggers = captureAlertsForNextMilestone(ancEnrollmentPage.getMotechPatientId());
         assertEquals(4, cronTriggers.size());
 
        CronTrigger dueTrigger = cronTriggers.get(0);
        CronTrigger lateTrigger1 = cronTriggers.get(1);
        CronTrigger lateTrigger2 = cronTriggers.get(2);
        CronTrigger lateTrigger3 = cronTriggers.get(3);
 
         assertThat(dueTrigger.getNextFireTime(), is(minDate));
         assertThat(lateTrigger1.getNextFireTime(), is(dueDate));
         assertThat(lateTrigger2.getNextFireTime(), is(lateDate1));
         assertThat(lateTrigger3.getNextFireTime(), is(lateDate2));
 
         assertThat(dueTrigger.getCronExpression(), is("0 0 0 ? * *"));
         assertThat(lateTrigger1.getCronExpression(), is("0 0 0 ? * *"));
         assertThat(lateTrigger2.getCronExpression(), is("0 0 0 ? * *"));
         assertThat(lateTrigger3.getCronExpression(), is("0 0 0 ? * *"));
     }
 
     protected List<CronTrigger> captureAlertsForNextMilestone(String enrollmentId) throws SchedulerException {
         final Scheduler scheduler = schedulerFactoryBean.getScheduler();
         final String jobGroupName = MotechSchedulerServiceImpl.JOB_GROUP_NAME;
         Set<JobKey> jobNames = scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(jobGroupName));
         List<CronTrigger> alertTriggers = new ArrayList<CronTrigger>();
 
         for (JobKey jobKey : jobNames) {
             if (jobKey.getName().contains(format("%s-%s", EventKeys.APPOINTMENT_REMINDER_EVENT_SUBJECT, enrollmentId))) {
                 List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);
                 assertEquals(1, triggersOfJob.size());
                 alertTriggers.add((CronTrigger) triggersOfJob.get(0));
             }
         }
         return alertTriggers;
     }
 
     private String formatDate(LocalDate date) {
         return new SimpleDateFormat("dd MMMM yyyy HH:mm:ss z").format(date.toDate());
     }
 }
