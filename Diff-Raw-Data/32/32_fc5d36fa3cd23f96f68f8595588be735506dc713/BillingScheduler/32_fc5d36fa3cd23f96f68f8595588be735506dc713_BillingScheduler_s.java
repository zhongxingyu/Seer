 package org.motechproject.ghana.mtn.billing.service;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.motechproject.ghana.mtn.billing.dto.BillingCycleRequest;
 import org.motechproject.model.CronSchedulableJob;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.scheduler.MotechSchedulerService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import java.util.Date;
 
 import static java.lang.String.format;
 
 @Component
 public class BillingScheduler {
     private static final Logger log = Logger.getLogger(BillingScheduler.class);
     public static final String MONTHLY_BILLING_SCHEDULE_SUBJECT = "org.motechproject.ghana.mtn.service.billingschedule";
     public final static String PROGRAM_KEY = "Program";
     public final static String EXTERNAL_ID_KEY = "ExternalID";
 
     private MotechSchedulerService schedulerService;
     private String cron;
 
     @Autowired
     public BillingScheduler(MotechSchedulerService schedulerService,
                             @Value(value = "#{billingProperties['job.cron']}") String cron) {
         this.schedulerService = schedulerService;
         this.cron = cron;
     }
 
     public void startFor(BillingCycleRequest request) {
         String mobileNumber = request.getMobileNumber();
         String programKey = request.programKey();
         DateTime cycleStartDate = request.getCycleStartDate();
         Date startTime = cycleStartDate.monthOfYear().addToCopy(1).toDate();
         String cronJobExpression = format(cron, cycleStartDate.getDayOfMonth());
 
         String jobId = jobId(mobileNumber, programKey);
         MotechEvent motechEvent = new MotechEvent(MONTHLY_BILLING_SCHEDULE_SUBJECT, new SchedulerParamsBuilder()
                 .withJobId(jobId)
                 .withExternalId(mobileNumber)
                 .withProgram(programKey)
                 .params());
 
         CronSchedulableJob schedulableJob = new CronSchedulableJob(motechEvent, cronJobExpression, startTime, null);
         schedulerService.scheduleJob(schedulableJob);
         log.info("Billing job scheduled for [" + mobileNumber + "|" + programKey + "|" + startTime + "]");
     }
 
     public void stopFor(BillingCycleRequest request) {
         String mobileNumber = request.getMobileNumber();
         String programName = request.programKey();
         String jobId = jobId(mobileNumber, programName);
 
        schedulerService.unscheduleJob(jobId);
         log.info("Billing job unscheduled for [" + mobileNumber + "|" + programName + "]");
     }
 
     private String jobId(String mobileNumber, String programName) {
        return format("%s.%s.%s", MONTHLY_BILLING_SCHEDULE_SUBJECT, programName, mobileNumber);
     }
 }
