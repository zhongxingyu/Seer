 package org.app;
 
 import org.quartz.*;
 import org.quartz.impl.StdSchedulerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 
 public class ReadWebSiteServlet extends HttpServlet {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(ReadWebSiteServlet.class);
    private static final String cronJob = "0/10 * * * * ?";
    Scheduler scheduler;
     public void init() throws ServletException {
         System.out.println("==================================================");
         System.out.println("Initialized the cron job");
         try {
             System.out.println("Site reading cron job stated {"+cronJob+"}");
             SiteReadingCronJob(cronJob);
         } catch (Exception e) {
             LOGGER.error("Error occur while initialize the cron job", e);
         }
         System.out.println("==================================================");
     }
 
     public void SiteReadingCronJob(String cronExpression) throws Exception {
         JobDetail jobDetail = JobBuilder.newJob(DataReadingJob.class)
                 .build();
 
         Trigger trigger = TriggerBuilder.newTrigger()
                 .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                 .build();
 
        scheduler = new StdSchedulerFactory().getScheduler();
         scheduler.start();
         scheduler.scheduleJob(jobDetail, trigger);
     }

    public void destroy() {
        System.out.println("==================================================");
        System.out.println("Shutdown the cron job");
        try {
            System.out.println("Site reading cron job stopped {"+cronJob+"}");
            scheduler.shutdown();
        } catch (Exception e) {
            LOGGER.error("Error occur while shutdown the cron job", e);
        }
        System.out.println("==================================================");
    }
 }
