 package org.motechproject.ghana.national.configuration;
 
 import org.joda.time.LocalDate;
 import org.junit.After;
 import org.junit.Before;
 import org.motechproject.model.Time;
 import org.motechproject.scheduler.MotechSchedulerService;
 import org.motechproject.scheduler.MotechSchedulerServiceImpl;
 import org.motechproject.scheduletracking.api.repository.AllEnrollments;
 import org.motechproject.scheduletracking.api.repository.AllTrackedSchedules;
 import org.motechproject.scheduletracking.api.service.impl.EnrollmentAlertService;
 import org.motechproject.scheduletracking.api.service.impl.EnrollmentDefaultmentService;
 import org.motechproject.scheduletracking.api.service.impl.EnrollmentService;
 import org.motechproject.scheduletracking.api.service.impl.ScheduleTrackingServiceImpl;
 import org.motechproject.testing.utils.BaseUnitTest;
 import org.motechproject.util.DateUtil;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.SimpleTrigger;
 import org.quartz.Trigger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 
 import java.util.*;
 
import static java.lang.String.*;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 public abstract class BaseScheduleTrackingIT extends BaseUnitTest {
 
     @Autowired
     private AllTrackedSchedules allTrackedSchedules;
 
     @Autowired
     protected MotechSchedulerService motechSchedulerService;
 
     @Autowired
     private AllEnrollments allEnrollments;
 
     @Autowired
     private SchedulerFactoryBean schedulerFactoryBean;
 
 
     protected ScheduleTrackingServiceImpl scheduleTrackingService;
 
     private String enrollmentId = "enrollmentId";
 
     @Before
     public void setUp() {
         EnrollmentAlertService enrollmentAlertService = new EnrollmentAlertService(allTrackedSchedules, motechSchedulerService);
         EnrollmentDefaultmentService enrollmentDefaultmentService = new EnrollmentDefaultmentService(allTrackedSchedules, motechSchedulerService);
         EnrollmentService enrollmentService = new EnrollmentService(allTrackedSchedules, allEnrollments, enrollmentAlertService, enrollmentDefaultmentService);
         scheduleTrackingService = new ScheduleTrackingServiceImpl(allTrackedSchedules, allEnrollments, enrollmentService);
     }
 
     @After
     public void deleteAllJobs() throws SchedulerException {
         super.tearDown();
         Scheduler scheduler = schedulerFactoryBean.getScheduler();
         for (String jobGroup : scheduler.getJobGroupNames()) {
             for (String jobName : scheduler.getJobNames(jobGroup)) {
                 scheduler.deleteJob(jobName, jobGroup);
             }
         }
     }
 
 
     protected List<SimpleTrigger> captureAlertsForNextMilestone(String enrollmentId) throws SchedulerException {
 
         final Scheduler scheduler = schedulerFactoryBean.getScheduler();
         final String jobGroupName = MotechSchedulerServiceImpl.JOB_GROUP_NAME;
         String[] jobNames = scheduler.getJobNames(jobGroupName);
         List<SimpleTrigger> alertTriggers = new ArrayList<SimpleTrigger>();
         for (String jobName : jobNames) {
            if (jobName.contains(format("%s", enrollmentId))) {
                 Trigger[] triggersOfJob = scheduler.getTriggersOfJob(jobName, jobGroupName);
                 assertEquals(1, triggersOfJob.length);
                 alertTriggers.add((SimpleTrigger)triggersOfJob[0]);
             }
         }
         return alertTriggers;
     }
 
     protected void assertAlerts(List<SimpleTrigger> alerts, List<Date> alertTimes) {
 
         List<Date> actualAlertTimes = new ArrayList<Date>();
         for (SimpleTrigger alert : alerts) {
             Date nextFireTime = alert.getNextFireTime();
             actualAlertTimes.add(nextFireTime);
             for (int i = 1; i < alert.getRepeatCount(); i++) {
                 Calendar calendar = Calendar.getInstance();
                 calendar.setTime((Date)nextFireTime.clone());
                 calendar.add(Calendar.DAY_OF_MONTH, toDays(i * alert.getRepeatInterval()));
                 actualAlertTimes.add(calendar.getTime());
             }
         }
         assertThat(actualAlertTimes, is(equalTo(alertTimes)));
     }
 
     private int toDays(long milliseconds) {
         return (int)(milliseconds/1000/60/60/24);
     }
 
     protected Date onDate(LocalDate referenceDate, int numberOfWeeks, Time alertTime) {
         return DateUtil.newDateTime(referenceDate.plusWeeks(numberOfWeeks), alertTime).toDate();
     }
 }
