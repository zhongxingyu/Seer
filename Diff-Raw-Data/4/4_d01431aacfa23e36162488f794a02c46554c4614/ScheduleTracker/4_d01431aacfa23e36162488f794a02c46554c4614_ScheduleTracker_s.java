 package org.motechproject.ghana.national.functional.framework;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.Period;
 import org.motechproject.ghana.national.functional.domain.*;
 import org.motechproject.ghana.national.functional.domain.JobDetail;
 import org.motechproject.model.Time;
 import org.motechproject.scheduler.MotechSchedulerService;
import org.motechproject.scheduler.MotechSchedulerServiceImpl;
 import org.motechproject.scheduletracking.api.domain.Enrollment;
 import org.motechproject.scheduletracking.api.domain.Milestone;
 import org.motechproject.scheduletracking.api.domain.MilestoneWindow;
 import org.motechproject.scheduletracking.api.domain.WindowName;
 import org.motechproject.scheduletracking.api.events.constants.EventDataKeys;
 import org.motechproject.scheduletracking.api.events.constants.EventSubjects;
 import org.motechproject.scheduletracking.api.repository.AllEnrollments;
 import org.motechproject.scheduletracking.api.repository.AllSchedules;
 import org.motechproject.scheduletracking.api.service.EnrollmentRecord;
 import org.motechproject.scheduletracking.api.service.impl.ScheduleTrackingServiceImpl;
 import org.motechproject.util.DateUtil;
 import org.quartz.*;
 import org.quartz.impl.matchers.GroupMatcher;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 import org.springframework.stereotype.Component;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.Calendar;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static java.lang.Math.ceil;
 import static java.lang.Math.min;
 import static java.lang.String.format;
 import static org.apache.commons.lang.time.DateUtils.parseDate;
 import static org.motechproject.ghana.national.tools.Utility.nullSafe;
 import static org.motechproject.util.DateUtil.newDateTime;
 
 @Component
 public class ScheduleTracker {
 
     @Autowired
     protected MotechSchedulerService motechSchedulerService;
 
     @Autowired
     private AllEnrollments allEnrollments;
 
     @Autowired
     private SchedulerFactoryBean schedulerFactoryBean;
 
     @Autowired
     protected ScheduleTrackingServiceImpl scheduleTrackingService;
 
     @Autowired
     AllSchedules allTrackedSchedules;
 
     private Pattern ALERT_ORDER_INDEX_REGEX = Pattern.compile("^.*\\.(.*?)-repeat$");
     protected Time preferredAlertTime;
 
     public void setUp() {
         preferredAlertTime = new Time(10, 10);
     }
 
     public String getActiveMilestone(String externalId, String scheduleName) {
         return allEnrollments.getActiveEnrollment(externalId, scheduleName).getCurrentMilestoneName();
     }
 
     public EnrollmentRecord activeEnrollment(String externalId, String scheduleName) {
         return scheduleTrackingService.getEnrollment(externalId, scheduleName);
     }
 
     private void assertNotNull(Object object) {
         if (object == null) throw new AssertionError("should not be null");
     }
 
     public List<org.motechproject.ghana.national.functional.domain.JobDetail> captureScheduleAlerts(String externalId, String scheduleName) {
         Enrollment activeEnrollment = allEnrollments.getActiveEnrollment(externalId, scheduleName);
         assertNotNull(activeEnrollment);
         return captureAlertsForNextMilestone(activeEnrollment.getId());
     }
 
     protected List<JobDetail> captureAlertsForNextMilestone(String enrollmentId) {
         final Scheduler scheduler = schedulerFactoryBean.getScheduler();
        final String jobGroupName = MotechSchedulerServiceImpl.JOB_GROUP_NAME;
         List<JobDetail> alertTriggers = new ArrayList<JobDetail>();
         try {
             Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(jobGroupName));
             for (JobKey jobKey : jobKeys) {
                 if (jobKey.getName().contains(format("%s-%s", EventSubjects.MILESTONE_ALERT, enrollmentId))) {
                     List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);
                     alertTriggers.add(new JobDetail((SimpleTrigger) triggersOfJob.get(0), scheduler.getJobDetail(jobKey)));
                 }
             }
         } catch (SchedulerException e) {
             throw new RuntimeException(e);
         }
         return alertTriggers;
     }
 
     public List<Date> alerts(List<org.motechproject.ghana.national.functional.domain.JobDetail> testJobDetails) {
 
         sortBasedOnIndexInAlertName(testJobDetails);
 
         List<Date> actualAlertTimes = new ArrayList<Date>();
         for (org.motechproject.ghana.national.functional.domain.JobDetail testJobDetail : testJobDetails) {
             SimpleTrigger alert = testJobDetail.trigger();
             Date nextFireTime = alert.getNextFireTime();
             actualAlertTimes.add(nextFireTime);
             for (int i = 1; i <= alert.getRepeatCount() - alert.getTimesTriggered(); i++) {
                 Calendar calendar = Calendar.getInstance();
                 calendar.setTime((Date) nextFireTime.clone());
                 calendar.add(Calendar.DAY_OF_MONTH, toDays(i * alert.getRepeatInterval()));
                 actualAlertTimes.add(calendar.getTime());
             }
         }
         return actualAlertTimes;
     }
 
     private List<Alert> createActualAlertTimes(List<org.motechproject.ghana.national.functional.domain.JobDetail> alertsJobDetails) {
         sortBasedOnIndexInAlertName(alertsJobDetails);
 
         List<Alert> actualAlertTimes = new ArrayList<Alert>();
         for (org.motechproject.ghana.national.functional.domain.JobDetail testJobDetail : alertsJobDetails) {
             SimpleTrigger alert = testJobDetail.trigger();
             Date nextFireTime = alert.getNextFireTime();
             JobDataMap dataMap = testJobDetail.getJobDetail().getJobDataMap();
             actualAlertTimes.add(new Alert(window(dataMap), nextFireTime));
             for (int i = 1; i <= alert.getRepeatCount(); i++) {
                 Calendar calendar = Calendar.getInstance();
                 calendar.setTime((Date) nextFireTime.clone());
                 calendar.add(Calendar.DAY_OF_MONTH, toDays(i * alert.getRepeatInterval()));
                 actualAlertTimes.add(new Alert(window(dataMap), calendar.getTime()));
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
 
     private void sortBasedOnIndexInAlertName(List<org.motechproject.ghana.national.functional.domain.JobDetail> alertJobDetails) {
         Collections.sort(alertJobDetails, new Comparator<org.motechproject.ghana.national.functional.domain.JobDetail>() {
             @Override
             public int compare(org.motechproject.ghana.national.functional.domain.JobDetail testJobDetail1, org.motechproject.ghana.national.functional.domain.JobDetail testJobDetail2) {
                 return extractIndexFromAlertName(testJobDetail1.trigger().getKey().getName()).compareTo(extractIndexFromAlertName(testJobDetail2.trigger().getKey().getName()));
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
             return newDateTime(parseDate(dateToParse, new String[]{"dd-MMM-yyyy HH:mm", "dd-MMM-yyyy HH:mm:ss", "dd-MMM-yyyy HH:mm:ss.SSS"}));
         } catch (ParseException e) {
             throw new IllegalArgumentException(e);
         }
     }
 
     protected Alert alert(WindowName windowName, Date alertDate) {
         return new Alert(windowName, alertDate);
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
 
     public Alert firstAlertScheduledFor(String externalId, String scheduleName) {
         return nullSafe(createActualAlertTimes(captureScheduleAlerts(externalId, scheduleName)), 0, null);
     }
 
     public LocalDate firstAlert(String scheduleName, LocalDate referenceDate) {
         return firstAlert(scheduleName, referenceDate, allTrackedSchedules.getByName(scheduleName).getFirstMilestone().getName());
     }
 
     public LocalDate alertFor(String scheduleName, LocalDate referenceDate, String milestoneName) {
         return firstAlert(scheduleName, referenceDate, allTrackedSchedules.getByName(scheduleName).getMilestone(milestoneName).getName());
     }
 
     public LocalDate firstAlert(String scheduleName, LocalDate referenceDate, String milestoneName) {
         org.motechproject.scheduletracking.api.domain.Schedule schedule = allTrackedSchedules.getByName(scheduleName);
         Milestone milestone = schedule.getMilestone(milestoneName);
         return findFirstApplicableAlert(milestone, referenceDate);
     }
 
     private LocalDate findFirstApplicableAlert(Milestone milestone, LocalDate referenceDate) {
         List<MilestoneWindow> milestoneWindows = milestone.getMilestoneWindows();
         for (MilestoneWindow milestoneWindow : milestoneWindows) {
             Period windowStart = milestone.getWindowStart(milestoneWindow.getName());
             Period windowEnd = milestone.getWindowEnd(milestoneWindow.getName());
             for (org.motechproject.scheduletracking.api.domain.Alert alert : milestoneWindow.getAlerts()) {
                 LocalDate referenceWindowStartDate = referenceDate.plus(windowStart);
                 LocalDate referenceWindowEndDate = referenceDate.plus(windowEnd);
 
                 int alertCount = getRemainingAlertCount(newDateTime(referenceWindowStartDate.toDate()), newDateTime(referenceWindowEndDate.toDate()), null, alert);
                 if (alertCount > 0) {
                     return getNextAlertDateTime(newDateTime(referenceWindowStartDate.toDate()), null, alert).toLocalDate();
                 }
             }
         }
         return null;
     }
 
 
     int getElapsedAlertCount(DateTime startReferenceDateTime, Time preferredAlertTime, org.motechproject.scheduletracking.api.domain.Alert alert) {
         DateTime idealStartDateWithPreferredTime = preferredAlertDateTime(startReferenceDateTime, preferredAlertTime, alert);
 
         DateTime now = DateUtil.now();
         if (idealStartDateWithPreferredTime.isBefore(now)) {
             long secsSinceIdealStartOfAlert = (now.getMillis() - idealStartDateWithPreferredTime.getMillis()) / 1000;
             int elapsedAlerts = possibleNumbersOfAlertsInDuration(secsSinceIdealStartOfAlert, alert);
             return min(elapsedAlerts, alert.getCount());
         }
         return 0;
     }
 
     public DateTime getNextAlertDateTime(DateTime startReferenceDateTime, Time preferredAlertTime, org.motechproject.scheduletracking.api.domain.Alert alert) {
         DateTime idealStartDateTime = startReferenceDateTime.plus(alert.getOffset());
         DateTime nextAlertDateTime = idealStartDateTime.plusDays(getElapsedAlertCount(startReferenceDateTime, preferredAlertTime, alert) * alert.getInterval().toStandardDays().getDays());
         if (preferredAlertTime != null)
             return newDateTime(nextAlertDateTime.toLocalDate(), preferredAlertTime.getHour(), preferredAlertTime.getMinute(), 0);
         return nextAlertDateTime;
     }
 
     public int getRemainingAlertCount(DateTime startTimeForAlerts, DateTime windowEndTime, Time preferredAlertTime, org.motechproject.scheduletracking.api.domain.Alert alert) {
         return min(alert.getCount() - getElapsedAlertCount(startTimeForAlerts, preferredAlertTime, alert), maximumPossibleAlertsCount(startTimeForAlerts, windowEndTime, preferredAlertTime, alert));
     }
 
     private int maximumPossibleAlertsCount(DateTime startTimeForAlerts, DateTime windowEndTime, Time preferredAlertTime, org.motechproject.scheduletracking.api.domain.Alert alert) {
         DateTime preferredStartTimeForAlerts = preferredAlertDateTime(startTimeForAlerts, preferredAlertTime, alert);
         long windowForAlerts = windowEndTime.minus(preferredStartTimeForAlerts.getMillis()).getMillis() / 1000;
         return possibleNumbersOfAlertsInDuration(windowForAlerts, alert);
     }
 
     private int possibleNumbersOfAlertsInDuration(long duration, org.motechproject.scheduletracking.api.domain.Alert alert) {
         return (int) ceil(duration / (double) alert.getInterval().toStandardSeconds().getSeconds());
     }
 
     private DateTime preferredAlertDateTime(DateTime startReferenceDateTime, Time preferredAlertTime, org.motechproject.scheduletracking.api.domain.Alert alert) {
         DateTime idealStartDateTime = startReferenceDateTime.plus(alert.getOffset());
         DateTime idealStartDateWithPreferredTime = idealStartDateTime;
         if (preferredAlertTime != null)
             idealStartDateWithPreferredTime = DateUtil.newDateTime(idealStartDateTime.toLocalDate(), preferredAlertTime.getHour(), preferredAlertTime.getMinute(), 0);
         return idealStartDateWithPreferredTime;
     }
 }
