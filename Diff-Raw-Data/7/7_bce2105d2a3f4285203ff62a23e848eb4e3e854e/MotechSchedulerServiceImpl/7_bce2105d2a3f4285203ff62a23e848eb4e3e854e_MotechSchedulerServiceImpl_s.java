 package org.motechproject.scheduler;
 
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.MotechObject;
 import org.motechproject.model.CronSchedulableJob;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.model.RepeatingSchedulableJob;
 import org.motechproject.model.RunOnceSchedulableJob;
 import org.motechproject.scheduler.domain.JobId;
 import org.motechproject.util.DateUtil;
 import org.motechproject.util.StringUtil;
 import org.quartz.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 
 import java.text.ParseException;
 import java.util.Date;
 
 /**
  * Motech Scheduler Service implementation
  *
  * @see MotechSchedulerService
  */
 public class MotechSchedulerServiceImpl extends MotechObject implements MotechSchedulerService {
     public static final String JOB_GROUP_NAME = "default";
     final int MAX_REPEAT_COUNT = 999999;
     private SchedulerFactoryBean schedulerFactoryBean;
 
     @Value("#{quartzProperties['org.quartz.scheduler.cron.trigger.misfire.policy']}")
     private String cronTriggerMisfirePolicy;
 
     @Value("#{quartzProperties['org.quartz.scheduler.repeating.trigger.misfire.policy']}")
     private String repeatingTriggerMisfirePolicy;
 
     private MotechSchedulerServiceImpl() {
     }
 
     @Autowired
     public MotechSchedulerServiceImpl(SchedulerFactoryBean schedulerFactoryBean) {
         this.schedulerFactoryBean = schedulerFactoryBean;
     }
 
     @Override
     public void scheduleJob(CronSchedulableJob cronSchedulableJob) {
         MotechEvent motechEvent = assertCronJob(cronSchedulableJob);
 
         JobId jobId = new JobId(motechEvent);
         JobDetail jobDetail = new JobDetail(jobId.value(), JOB_GROUP_NAME, MotechScheduledJob.class);
         putMotechEventDataToJobDataMap(jobDetail.getJobDataMap(), motechEvent);
 
         Trigger trigger;
 
         try {
             trigger = new CronTrigger(jobId.value(), JOB_GROUP_NAME, cronSchedulableJob.getCronExpression());
 
             setMisfirePolicy(trigger, cronTriggerMisfirePolicy, Trigger.MISFIRE_INSTRUCTION_SMART_POLICY);
 
             Date startTime = cronSchedulableJob.getStartTime();
             Date endTime = cronSchedulableJob.getEndTime();
             if (startTime != null) {
                 trigger.setStartTime(startTime);
             }
             if (endTime != null) {
                 trigger.setEndTime(endTime);
             }
         } catch (ParseException e) {
             String errorMessage = "Can not schedule the job: " + jobId + "\n invalid Cron expression: " +
                     cronSchedulableJob.getCronExpression();
             logError(errorMessage);
             throw new MotechSchedulerException(errorMessage);
         }
 
         Scheduler scheduler = schedulerFactoryBean.getScheduler();
         Trigger existingTrigger;
         try {
             existingTrigger = scheduler.getTrigger(jobId.value(), JOB_GROUP_NAME);
         } catch (SchedulerException e) {
             String errorMessage = "Schedule or reschedule the job: " + jobId +
                     ".\n  " + e.getMessage();
             logError(errorMessage, e);
             throw new MotechSchedulerException(errorMessage);
         }
 
         if (existingTrigger != null) {
             unscheduleJob(jobId.value());
         }
 
         scheduleJob(jobDetail, trigger);
     }
 
     private void setMisfirePolicy(Trigger trigger, String misfireInstruction, int defaultMisfireInstruction) {
        if (StringUtil.isNullOrEmpty(cronTriggerMisfirePolicy)) {
            trigger.setMisfireInstruction(Integer.valueOf(misfireInstruction));
        } else {
             trigger.setMisfireInstruction(defaultMisfireInstruction);
         }
     }
 
     private MotechEvent assertCronJob(CronSchedulableJob cronSchedulableJob) {
         assertArgumentNotNull("SchedulableJob", cronSchedulableJob);
         logInfo("Scheduling the job: %s", cronSchedulableJob);
 
         MotechEvent motechEvent = cronSchedulableJob.getMotechEvent();
         assertArgumentNotNull("MotechEvent of the SchedulableJob", motechEvent);
         return motechEvent;
     }
 
     @Override
     public void safeScheduleJob(CronSchedulableJob cronSchedulableJob) {
         assertCronJob(cronSchedulableJob);
         JobId jobId = new JobId(cronSchedulableJob.getMotechEvent());
         try {
             unscheduleJob(jobId.value());
         } catch (MotechSchedulerException ignored) {
         }
         scheduleJob(cronSchedulableJob);
     }
 
     @Override
     public void updateScheduledJob(MotechEvent motechEvent) {
         logInfo("Updating the scheduled job: %s", motechEvent);
         assertArgumentNotNull("MotechEvent", motechEvent);
 
         Scheduler scheduler = schedulerFactoryBean.getScheduler();
         JobId jobId = new JobId(motechEvent);
         Trigger trigger;
 
         try {
             trigger = scheduler.getTrigger(jobId.value(), JOB_GROUP_NAME);
 
             if (trigger == null) {
                 String errorMessage = "Can not update the job: " + jobId + " The job does not exist (not scheduled)";
                 logError(errorMessage);
                 throw new MotechSchedulerException(errorMessage);
             }
 
         } catch (SchedulerException e) {
             String errorMessage = "Can not update the job: " + jobId +
                     ".\n Can not get a trigger associated with that job " + e.getMessage();
             logError(errorMessage, e);
             throw new MotechSchedulerException(errorMessage);
         }
 
         try {
             scheduler.deleteJob(jobId.value(), JOB_GROUP_NAME);
         } catch (SchedulerException e) {
             handleException(String.format("Can not update the job: %s.\n Can not delete old instance of the job %s", jobId, e.getMessage()), e);
         }
 
         JobDetail jobDetail = new JobDetail(jobId.value(), JOB_GROUP_NAME, MotechScheduledJob.class);
         putMotechEventDataToJobDataMap(jobDetail.getJobDataMap(), motechEvent);
 
         scheduleJob(jobDetail, trigger);
     }
 
 
     @Override
     public void rescheduleJob(String subject, String externalId, String cronExpression) {
         assertArgumentNotNull("Subject", subject);
         assertArgumentNotNull("ExternalId", externalId);
         assertArgumentNotNull("Cron expression", cronExpression);
 
         JobId jobId = new JobId(subject, externalId);
         logInfo("Rescheduling the Job: %s new cron expression: %s", jobId, cronExpression);
 
         Scheduler scheduler = schedulerFactoryBean.getScheduler();
         CronTrigger trigger = null;
         try {
             trigger = (CronTrigger) scheduler.getTrigger(jobId.value(), JOB_GROUP_NAME);
 
             if (trigger == null) {
                 logError("Can not reschedule the job: %s The job does not exist (not scheduled)", jobId);
                 throw new MotechSchedulerException();
             }
         } catch (SchedulerException e) {
             handleException(String.format("Can not reschedule the job: %s.\n Can not get a trigger associated with that job %s", jobId, e.getMessage()), e);
         } catch (ClassCastException e) {
             handleException(String.format("Can not reschedule the job: %s.\n The trigger associated with that job is not a CronTrigger", jobId), e);
         }
 
         try {
             trigger.setCronExpression(cronExpression);
         } catch (ParseException e) {
             handleException(String.format("Can not reschedule the job: %s Invalid Cron expression: %s", jobId, cronExpression), e);
         }
 
         try {
             schedulerFactoryBean.getScheduler().rescheduleJob(jobId.value(), JOB_GROUP_NAME, trigger);
         } catch (SchedulerException e) {
             handleException(String.format("Can not reschedule the job: %s %s", jobId, e.getMessage()), e);
         }
     }
 
     private void handleException(String errorMessage, Exception e) {
         logError(errorMessage, e);
         throw new MotechSchedulerException(errorMessage);
     }
 
     @Override
     public void scheduleRepeatingJob(RepeatingSchedulableJob repeatingSchedulableJob) {
         MotechEvent motechEvent = assertArgumentNotNull(repeatingSchedulableJob);
 
         Date jobStartDate = repeatingSchedulableJob.getStartTime();
         Date jobEndDate = repeatingSchedulableJob.getEndTime();
         assertArgumentNotNull("Job start date", jobStartDate);
 
         long jobRepeatInterval = repeatingSchedulableJob.getRepeatInterval();
         if (jobRepeatInterval == 0) {
             String errorMessage = "Invalid RepeatingSchedulableJob. The job repeat interval can not be 0";
             logError(errorMessage);
             throw new IllegalArgumentException(errorMessage);
         }
 
         Integer jobRepeatCount = repeatingSchedulableJob.getRepeatCount();
         if (null == jobRepeatCount) {
             jobRepeatCount = MAX_REPEAT_COUNT;
         }
 
         JobId jobId = new JobId(motechEvent);
         JobDetail jobDetail = new JobDetail(jobId.repeatingId(), JOB_GROUP_NAME, MotechScheduledJob.class);
         putMotechEventDataToJobDataMap(jobDetail.getJobDataMap(), motechEvent);
 
         Trigger trigger;
         trigger = new SimpleTrigger(jobId.repeatingId(), JOB_GROUP_NAME, jobStartDate, jobEndDate,
                 jobRepeatCount,
                 jobRepeatInterval);
         setMisfirePolicy(trigger, repeatingTriggerMisfirePolicy, SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT);
         scheduleJob(jobDetail, trigger);
     }
 
     private MotechEvent assertArgumentNotNull(RepeatingSchedulableJob repeatingSchedulableJob) {
         assertArgumentNotNull("SchedulableJob", repeatingSchedulableJob);
         logInfo("Scheduling the Job: %s", repeatingSchedulableJob);
         MotechEvent motechEvent = repeatingSchedulableJob.getMotechEvent();
         assertArgumentNotNull("Invalid SchedulableJob. MotechEvent of the SchedulableJob", motechEvent);
         return motechEvent;
     }
 
     @Override
     public void safeScheduleRepeatingJob(RepeatingSchedulableJob repeatingSchedulableJob) {
         assertArgumentNotNull(repeatingSchedulableJob);
         JobId jobId = new JobId(repeatingSchedulableJob.getMotechEvent());
         try {
             unscheduleJob(jobId.repeatingId());
         } catch (MotechSchedulerException ignored) {
         }
         scheduleRepeatingJob(repeatingSchedulableJob);
     }
 
     @Override
     public void scheduleRunOnceJob(RunOnceSchedulableJob schedulableJob) {
         MotechEvent motechEvent = assertArgumentNotNull(schedulableJob);
 
         Date jobStartDate = schedulableJob.getStartDate();
         assertArgumentNotNull("Job start date", jobStartDate);
         Date currentDate = DateUtil.today().toDate();
         if (jobStartDate.before(currentDate)) {
             String errorMessage = "Invalid RunOnceSchedulableJob. The job start date can not be in the past. \n" +
                     " Job start date: " + jobStartDate.toString() +
                     " Attempted to schedule at:" + currentDate.toString();
             logError(errorMessage);
             throw new IllegalArgumentException();
         }
 
         JobId jobId = new JobId(motechEvent);
         JobDetail jobDetail = new JobDetail(jobId.value(), JOB_GROUP_NAME, MotechScheduledJob.class);
         putMotechEventDataToJobDataMap(jobDetail.getJobDataMap(), motechEvent);
 
         Trigger trigger = new SimpleTrigger(jobId.value(), JOB_GROUP_NAME, jobStartDate);
         scheduleJob(jobDetail, trigger);
     }
 
     private MotechEvent assertArgumentNotNull(RunOnceSchedulableJob schedulableJob) {
         assertArgumentNotNull("SchedulableJob", schedulableJob);
         logInfo("Scheduling the Job: %s", schedulableJob);
 
         MotechEvent motechEvent = schedulableJob.getMotechEvent();
         assertArgumentNotNull("MotechEvent of the SchedulableJob", motechEvent);
         return motechEvent;
     }
 
     public void safeScheduleRunOnceJob(RunOnceSchedulableJob schedulableJob) {
         assertArgumentNotNull(schedulableJob);
         JobId jobId = new JobId(schedulableJob.getMotechEvent());
         try {
             unscheduleJob(jobId.value());
         } catch (MotechSchedulerException ignored) {
         }
         scheduleRunOnceJob(schedulableJob);
     }
 
     @Override
     public void unscheduleRepeatingJob(String subject, String externalId) {
         JobId jobId = new JobId(subject, externalId);
         logInfo("Unscheduling repeating the Job: %s", jobId);
         unscheduleJob(jobId.repeatingId());
     }
 
     @Override
     public void safeUnscheduleRepeatingJob(String subject, String externalId) {
         try {
             unscheduleRepeatingJob(subject, externalId);
         } catch (Exception ignored) {
         }
     }
 
     @Override
     public void unscheduleJob(String subject, String externalId) {
         JobId jobId = new JobId(subject, externalId);
         logInfo("Unscheduling the Job: %s", jobId);
         unscheduleJob(jobId.value());
     }
 
     @Override
     public void safeUnscheduleJob(String subject, String externalId) {
         try {
             unscheduleJob(subject, externalId);
         } catch (Exception ignored) {
         }
     }
 
     private void unscheduleJob(String jobId) {
         try {
             assertArgumentNotNull("ScheduledJobID", jobId);
             schedulerFactoryBean.getScheduler().unscheduleJob(jobId, JOB_GROUP_NAME);
         } catch (SchedulerException e) {
             handleException(String.format("Can not unschedule the job: %s %s", jobId, e.getMessage()), e);
         }
     }
 
     private void safeUnscheduleJob(String jobId) {
         try {
             assertArgumentNotNull("ScheduledJobID", jobId);
             schedulerFactoryBean.getScheduler().unscheduleJob(jobId, JOB_GROUP_NAME);
         } catch (SchedulerException ignored) {
         }
     }
 
     @Override
     public void safeUnscheduleAllJobs(String jobIdPrefix) {
         try {
             logInfo("Safe unscheduling the Jobs given jobIdPrefix: %s", jobIdPrefix);
             String[] triggerNames = schedulerFactoryBean.getScheduler().getTriggerNames(JOB_GROUP_NAME);
             for (String triggerName : triggerNames) {
                 if (StringUtils.isNotEmpty(jobIdPrefix) && triggerName.contains(jobIdPrefix)) {
                     safeUnscheduleJob(triggerName);
                 }
             }
         } catch (SchedulerException ignored) {
         }
     }
 
     @Override
     public void unscheduleAllJobs(String jobIdPrefix) {
         try {
             logInfo("Unscheduling the Jobs given jobIdPrefix: %s", jobIdPrefix);
             String[] triggerNames = schedulerFactoryBean.getScheduler().getTriggerNames(JOB_GROUP_NAME);
             for (String triggerName : triggerNames) {
                 if (StringUtils.isNotEmpty(jobIdPrefix) && triggerName.contains(jobIdPrefix)) {
                     unscheduleJob(triggerName);
                 }
             }
         } catch (SchedulerException e) {
             handleException(String.format("Can not unschedule jobs given jobIdPrefix: %s %s", jobIdPrefix, e.getMessage()), e);
         }
     }
 
     private void scheduleJob(JobDetail jobDetail, Trigger trigger) {
 
         try {
             schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, trigger);
         } catch (SchedulerException e) {
             handleException(String.format("Can not schedule the job:\n %s\n%s\n%s", jobDetail.toString(), trigger.toString(), e.getMessage()), e);
         }
     }
 
     private void putMotechEventDataToJobDataMap(JobDataMap jobDataMap, MotechEvent motechEvent) {
         jobDataMap.putAll(motechEvent.getParameters());
         jobDataMap.put(MotechEvent.EVENT_TYPE_KEY_NAME, motechEvent.getSubject());
     }
 }
