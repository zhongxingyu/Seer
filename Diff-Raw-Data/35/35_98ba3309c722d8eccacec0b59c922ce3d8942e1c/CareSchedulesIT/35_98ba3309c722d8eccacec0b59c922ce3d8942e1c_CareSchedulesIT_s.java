 package org.motechproject.ghana.national.configuration;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.util.DateUtil;
 import org.quartz.SchedulerException;
 import org.quartz.SimpleTrigger;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.Date;
import java.util.HashSet;
 import java.util.List;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/testApplicationContext-core.xml"})
 public class CareSchedulesIT extends BaseScheduleTrackingIT {
     private Time preferredAlertTime;
 
     @Before
     public void setUp() {
         super.setUp();
         preferredAlertTime = new Time(10, 10);
     }
 
     @Test
     public void verifyPregnancySchedule() throws SchedulerException {
         LocalDate today = DateUtil.newDate(2000, 1, 1);
         mockCurrentDate(today);
         final LocalDate conceptionDate = today.minusWeeks(1);
 
         EnrollmentRequest enrollmentRequest = new EnrollmentRequest("123", CareScheduleNames.DELIVERY, preferredAlertTime, conceptionDate);
         String enrollmentId = scheduleTrackingService.enroll(enrollmentRequest);
 
         List<SimpleTrigger> alerts = captureAlertsForNextMilestone(enrollmentId);
        assertAlerts(alerts, new HashSet<Date>() {{
             add(onDate(conceptionDate, 39, preferredAlertTime));
             add(onDate(conceptionDate, 40, preferredAlertTime));
             add(onDate(conceptionDate, 41, preferredAlertTime));
             add(onDate(conceptionDate, 42, preferredAlertTime));
         }});
     }
 
 }
