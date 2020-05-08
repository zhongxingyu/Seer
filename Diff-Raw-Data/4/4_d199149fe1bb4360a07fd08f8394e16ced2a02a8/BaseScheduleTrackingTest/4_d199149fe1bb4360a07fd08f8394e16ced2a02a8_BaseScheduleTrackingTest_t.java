 package org.motechproject.ghana.national.configuration;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.junit.After;
 import org.junit.Before;
 import org.motechproject.model.Time;
 import org.motechproject.scheduler.MotechSchedulerService;
 import org.motechproject.scheduler.MotechSchedulerServiceImpl;
 import org.motechproject.scheduletracking.api.domain.Enrollment;
 import org.motechproject.scheduletracking.api.domain.WindowName;
 import org.motechproject.scheduletracking.api.events.constants.EventDataKeys;
 import org.motechproject.scheduletracking.api.events.constants.EventSubjects;
 import org.motechproject.scheduletracking.api.repository.AllEnrollments;
 import org.motechproject.scheduletracking.api.repository.AllTrackedSchedules;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.scheduletracking.api.service.impl.*;
 import org.motechproject.testing.utils.BaseUnitTest;
 import org.motechproject.util.DateUtil;
 import org.quartz.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.Calendar;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static java.lang.String.format;
 import static org.apache.commons.lang.time.DateUtils.parseDate;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.motechproject.util.DateUtil.newDateTime;
 import static org.motechproject.util.DateUtil.today;
 
 public abstract class BaseScheduleTrackingTest extends BaseUnitTest {
 
     protected static final String PATIENT_ID = "Patient id";
     protected String externalId = PATIENT_ID;
 
     @Autowired
     private AllTrackedSchedules allTrackedSchedules;
 
     @Autowired
     protected MotechSchedulerService motechSchedulerService;
 
     @Autowired
     private AllEnrollments allEnrollments;
 
     @Autowired
     private SchedulerFactoryBean schedulerFactoryBean;
 
     @Autowired
     private EnrollmentRecordMapper enrollmentRecordMapper;
 
     protected ScheduleTrackingServiceImpl scheduleTrackingService;
 
     protected String enrollmentId;
     private Pattern ALERT_ORDER_INDEX_REGEX = Pattern.compile("^.*\\.(.*?)-repeat$");
     protected Time preferredAlertTime;
     String scheduleName;
 
     @Before
     public void setUp() {
         preferredAlertTime = new Time(10, 10);
        EnrollmentAlertService enrollmentAlertService = new EnrollmentAlertService(motechSchedulerService);
        EnrollmentDefaultmentService enrollmentDefaultmentService = new EnrollmentDefaultmentService(motechSchedulerService);
         EnrollmentService enrollmentService = new EnrollmentService(allTrackedSchedules, allEnrollments, enrollmentAlertService, enrollmentDefaultmentService);
         scheduleTrackingService = new ScheduleTrackingServiceImpl(allTrackedSchedules, allEnrollments, enrollmentService, null, enrollmentRecordMapper);
     }
 
     @After
     public void deleteAllJobs() throws SchedulerException {
         super.tearDown();
         for (Enrollment enrollment : allEnrollments.getAll()) {
             allEnrollments.remove(enrollment);
         }
         Scheduler scheduler = schedulerFactoryBean.getScheduler();
         for (String jobGroup : scheduler.getJobGroupNames()) {
             for (String jobName : scheduler.getJobNames(jobGroup)) {
                 scheduler.deleteJob(jobName, jobGroup);
             }
         }
     }
 
     protected List<TestJobDetail> captureAlertsForNextMilestone(String enrollmentId) throws SchedulerException {
         final Scheduler scheduler = schedulerFactoryBean.getScheduler();
         final String jobGroupName = MotechSchedulerServiceImpl.JOB_GROUP_NAME;
         String[] jobNames = scheduler.getJobNames(jobGroupName);
         List<TestJobDetail> alertTriggers = new ArrayList<TestJobDetail>();
 
         for (String jobName : jobNames) {
             if (jobName.contains(format("%s-%s", EventSubjects.MILESTONE_ALERT, enrollmentId))) {
                 Trigger[] triggersOfJob = scheduler.getTriggersOfJob(jobName, jobGroupName);
                 assertEquals(1, triggersOfJob.length);
                 alertTriggers.add(new TestJobDetail((SimpleTrigger) triggersOfJob[0], scheduler.getJobDetail(jobName, jobGroupName)));
             }
         }
         return alertTriggers;
     }
 
     protected void assertAlerts(List<TestJobDetail> testJobDetails, List<Date> alertTimes) {
 
         sortBasedOnIndexInAlertName(testJobDetails);
 
         List<Date> actualAlertTimes = new ArrayList<Date>();
         for (TestJobDetail testJobDetail : testJobDetails) {
             SimpleTrigger alert = testJobDetail.trigger();
             Date nextFireTime = alert.getNextFireTime();
             actualAlertTimes.add(nextFireTime);
             for (int i = 1; i <= alert.getRepeatCount(); i++) {
                 Calendar calendar = Calendar.getInstance();
                 calendar.setTime((Date) nextFireTime.clone());
                 calendar.add(Calendar.DAY_OF_MONTH, toDays(i * alert.getRepeatInterval()));
                 actualAlertTimes.add(calendar.getTime());
             }
         }
         assertThat(actualAlertTimes, is(equalTo(alertTimes)));
     }
 
     protected void assertTestAlerts(List<TestJobDetail> alerts, List<TestAlert> alertTimes) {
         assertThat(createActualTestAlertTimes(alerts), is(equalTo(alertTimes)));
     }
 
     private List<TestAlert> createActualTestAlertTimes(List<TestJobDetail> alertsJobDetails) {
         sortBasedOnIndexInAlertName(alertsJobDetails);
 
         List<TestAlert> actualAlertTimes = new ArrayList<TestAlert>();
         for (TestJobDetail testJobDetail : alertsJobDetails) {
             SimpleTrigger alert = testJobDetail.trigger();
             Date nextFireTime = alert.getNextFireTime();
             JobDataMap dataMap = testJobDetail.getJobDetail().getJobDataMap();
             actualAlertTimes.add(new TestAlert(window(dataMap), nextFireTime));
             for (int i = 1; i <= alert.getRepeatCount(); i++) {
                 Calendar calendar = Calendar.getInstance();
                 calendar.setTime((Date) nextFireTime.clone());
                 calendar.add(Calendar.DAY_OF_MONTH, toDays(i * alert.getRepeatInterval()));
                 actualAlertTimes.add(new TestAlert(window(dataMap), calendar.getTime()));
             }
         }
         return actualAlertTimes;
     }
 
     private WindowName window(JobDataMap dataMap) {
         return WindowName.valueOf((String) dataMap.get(EventDataKeys.WINDOW_NAME));
     }
 
     private Integer extractIndexFromAlertName(String name) {
         Matcher matcher = ALERT_ORDER_INDEX_REGEX.matcher(name);
         return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
     }
 
     private void sortBasedOnIndexInAlertName(List<TestJobDetail> alertJobDetails) {
         Collections.sort(alertJobDetails, new Comparator<TestJobDetail>() {
             @Override
             public int compare(TestJobDetail testJobDetail1, TestJobDetail testJobDetail2) {
                 return extractIndexFromAlertName(testJobDetail1.trigger().getName()).compareTo(extractIndexFromAlertName(testJobDetail2.trigger().getName()));
             }
         });
     }
 
     private int toDays(long milliseconds) {
         return (int) (milliseconds / 1000 / 60 / 60 / 24);
     }
 
     protected Date onDate(LocalDate referenceDate, int numberOfWeeks, Time alertTime) {
         return newDateTime(referenceDate.plusWeeks(numberOfWeeks), alertTime).toDate();
     }
 
     protected Date onDate(LocalDate referenceDate, Time alertTime) {
         return newDateTime(referenceDate, alertTime).toDate();
     }
 
     protected Date onDate(LocalDate localDate) {
         return newDateTime(localDate, preferredAlertTime).toDate();
     }
 
     protected Date onDate(String date) {
         return newDateTime(newDate(date), preferredAlertTime).toDate();
     }
 
     protected LocalDate mockToday(LocalDate today) {
         mockCurrentDate(today);
         return today;
     }
 
     protected DateTime mockToday(DateTime today) {
         mockCurrentDate(today);
         return today;
     }
 
     protected LocalDate newDate(String date) {
         try {
             return DateUtil.newDate(new SimpleDateFormat("dd-MMM-yyyy").parse(date));
         } catch (ParseException e) {
             throw new IllegalArgumentException(e);
         }
     }
 
     protected DateTime newDateWithTime(String date, String time) {
         try {
             String dateToParse = date + " " + time;
             return newDateTime(parseDate(dateToParse, new String[]{"dd-MMM-yyyy HH:mm", "dd-MMM-yyyy HH:mm:ss"}));
         } catch (ParseException e) {
             throw new IllegalArgumentException(e);
         }
     }
 
     protected TestAlert alert(WindowName windowName, Date alertDate) {
         return new TestAlert(windowName, alertDate);
     }
 
     protected ArrayList<Date> dates(LocalDate... dates) {
         ArrayList<Date> dateList = new ArrayList<Date>();
         for (LocalDate localDate : dates) {
             dateList.add(onDate(localDate));
         }
         return dateList;
     }
 
     protected ArrayList<Date> dateTimes(DateTime... dates) {
         ArrayList<Date> dateList = new ArrayList<Date>();
         for (DateTime date : dates) {
             dateList.add(date.toDate());
         }
         return dateList;
     }
 
     protected String enroll(LocalDate referenceDate) {
         EnrollmentRequest enrollmentRequest = new EnrollmentRequest(externalId, scheduleName, preferredAlertTime, referenceDate, null, today(), null, null);
         return scheduleTrackingService.enroll(enrollmentRequest);
     }
 
     protected void fulfillCurrentMilestone(LocalDate fulfillmentDate) {
         scheduleTrackingService.fulfillCurrentMilestone(externalId, scheduleName, fulfillmentDate);
     }
 }
