 package org.motechproject.commcare.provider.sync.diagnostics;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Transformer;
 import org.joda.time.DateTime;
 import org.motechproject.commons.date.util.DateUtil;
 import org.quartz.JobKey;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.Trigger;
 import org.quartz.TriggerKey;
 import org.quartz.impl.matchers.GroupMatcher;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.ApplicationContext;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 @Service("schedulerDiagnosticService")
 public class SchedulerDiagnosticServiceImpl implements SchedulerDiagnosticService {
 
     private Scheduler scheduler;
 
     @Autowired
    public SchedulerDiagnosticServiceImpl(ApplicationContext applicationContext, @Qualifier("quartzProperties") Properties quartzProperties) throws Exception {
        SchedulerFactoryBean schedulerFactoryBean = initializeSchedulerFactoryBean(applicationContext, quartzProperties);
         scheduler = schedulerFactoryBean.getScheduler();
     }
 
     private SchedulerFactoryBean initializeSchedulerFactoryBean(ApplicationContext applicationContext, Properties quartzProperties) throws Exception {
         SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
         schedulerFactoryBean.setQuartzProperties(quartzProperties);
         schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");
         schedulerFactoryBean.setApplicationContext(applicationContext);
         schedulerFactoryBean.setAutoStartup(false);
         schedulerFactoryBean.afterPropertiesSet();
         return schedulerFactoryBean;
     }
 
     public SchedulerDiagnosticServiceImpl(SchedulerFactoryBean schedulerFactoryBean) {
         this.scheduler = schedulerFactoryBean.getScheduler();
     }
 
     public DiagnosticsResult diagnoseSchedules(List<String> schedules) throws SchedulerException {
 
         List<JobDetails> jobDetailsList = getJobDetailsFor(schedules);
 
         DiagnosticsResult diagnosticsResult = checkIfAllJobsAreScheduled(schedules, jobDetailsList);
         return diagnosticsResult.getStatus().equals(DiagnosticsStatus.FAIL) ?
                 diagnosticsResult :
                 checkIfJobsAreScheduledAtTheRightTime(jobDetailsList);
     }
 
     private DiagnosticsResult checkIfAllJobsAreScheduled(List<String> jobs, List<JobDetails> jobDetailsList) {
         DiagnosticLog diagnosticLog = new DiagnosticLog();
 
         if (jobDetailsList.size() != jobs.size()) {
             ArrayList<String> unScheduledJobs = getUnscheduledJobs(jobs, jobDetailsList);
             for (String unScheduledJob : unScheduledJobs)
                 diagnosticLog.add("Unscheduled Job: " + unScheduledJob);
             return new DiagnosticsResult(DiagnosticsStatus.FAIL, diagnosticLog.toString());
         }
         return new DiagnosticsResult(DiagnosticsStatus.PASS, diagnosticLog.toString());
     }
 
     private DiagnosticsResult checkIfJobsAreScheduledAtTheRightTime(List<JobDetails> jobDetailsList) throws SchedulerException {
         DiagnosticLog diagnosticLog = new DiagnosticLog();
         DiagnosticsStatus status = DiagnosticsStatus.PASS;
         for (JobDetails jobDetails : jobDetailsList) {
             diagnosticLog.add("Job : " + jobDetails.getName());
             Date previousFireTime = jobDetails.getPreviousFireTime();
             diagnosticLog.add("Previous Fire Time : " + (previousFireTime == null ? "This job has not yet run" : previousFireTime.toString()));
             diagnosticLog.add("Next Fire Time : " + jobDetails.getNextFireTime());
 
             if (!hasJobRunInPreviousWeek(jobDetails, diagnosticLog)) {
                 status = DiagnosticsStatus.FAIL;
             }
             diagnosticLog.add("");
         }
 
         return new DiagnosticsResult(status, diagnosticLog.toString());
     }
 
     private ArrayList<String> getUnscheduledJobs(List<String> jobs, List<JobDetails> jobDetailsList) {
         ArrayList<String> jobDetailNamesList = (ArrayList<String>) CollectionUtils.collect(jobDetailsList, new Transformer() {
             @Override
             public Object transform(Object input) {
                 JobDetails jobDetails = (JobDetails) input;
                 return jobDetails.getName();
             }
         });
         return (ArrayList<String>) CollectionUtils.disjunction(jobs, jobDetailNamesList);
     }
 
     private boolean hasJobRunInPreviousWeek(JobDetails jobDetails, DiagnosticLog diagnosticLog) {
         String log = "Has job run in previous day : %s";
         Date previousFireTime = jobDetails.getPreviousFireTime();
         if (previousFireTime == null) {
             diagnosticLog.add(String.format(log, "N/A"));
             return true;
         }
 
         boolean hasRunInPreviousWeek = !DateUtil.newDateTime(previousFireTime).isBefore(DateTime.now().minusWeeks(1));
         diagnosticLog.add(String.format(log, hasRunInPreviousWeek ? "Yes" : "No"));
         return hasRunInPreviousWeek;
     }
 
     private List<JobDetails> getJobDetailsFor(List<String> jobs) throws SchedulerException {
         List<TriggerKey> triggerKeys = new ArrayList<>(scheduler.getTriggerKeys(GroupMatcher.triggerGroupContains("default")));
         List<JobDetails> jobDetailsList = new ArrayList<>();
         for (TriggerKey triggerKey : triggerKeys) {
             Trigger trigger = scheduler.getTrigger(triggerKey);
             JobKey jobKey = trigger.getJobKey();
             if (scheduler.getTriggersOfJob(jobKey).size() > 0 && isForJob(jobKey.getName(), jobs)) {
                 Date previousFireTime = trigger.getPreviousFireTime();
                 jobDetailsList.add(new JobDetails(previousFireTime, jobKey.getName(), trigger.getNextFireTime()));
             }
         }
         return jobDetailsList;
     }
 
     private boolean isForJob(String name, List<String> jobs) {
         for (String job : jobs) {
             if (name.contains(job))
                 return true;
         }
         return false;
     }
 }
