 package org.motechproject.care.reporting.scheduler;
 
 import org.motechproject.care.reporting.constants.EventConstants;
 import org.motechproject.care.reporting.constants.PropertyConstants;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.scheduler.MotechSchedulerService;
 import org.motechproject.scheduler.domain.CronSchedulableJob;
 import org.motechproject.server.config.SettingsFacade;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.Lazy;
 import org.springframework.stereotype.Component;
 
 @Component
 @Lazy(false)
 public class ComputeFieldsJobScheduler {
 
     @Autowired
     public ComputeFieldsJobScheduler(@Qualifier("careReportingSettings") SettingsFacade settings, MotechSchedulerService motechSchedulerService) {
         scheduleCronJob(motechSchedulerService, settings.getProperty(PropertyConstants.COMPUTE_FIELDS_JOB_CRON_EXPRESSION), EventConstants.COMPUTE_FIELDS);
     }
 
     private void scheduleCronJob(MotechSchedulerService motechSchedulerService, String cronExpression, String eventSubject) {
         CronSchedulableJob providerSyncCronJob = new CronSchedulableJob(new MotechEvent(eventSubject), cronExpression);
         motechSchedulerService.scheduleJob(providerSyncCronJob);
     }
 
 }
