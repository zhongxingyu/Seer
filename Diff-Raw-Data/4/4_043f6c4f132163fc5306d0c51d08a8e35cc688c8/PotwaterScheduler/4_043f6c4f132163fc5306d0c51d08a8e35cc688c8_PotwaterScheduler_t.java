 package com.haakenstad.potwater.scheduler;
 
 import org.apache.commons.lang3.StringUtils;
 import org.quartz.*;
 import org.quartz.impl.StdSchedulerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Date;
 
 import static org.quartz.CronScheduleBuilder.cronSchedule;
 import static org.quartz.JobBuilder.newJob;
 import static org.quartz.TriggerBuilder.newTrigger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bjornhaa
  * Date: 04.07.13
  * Time: 22:26
  * To change this template use File | Settings | File Templates.
  */
 public class PotwaterScheduler {
 
     private static final Integer VALVE_1 = 7;
     private static final Integer VALVE_2 = 6;
     private static final Integer VALVE_3 = 5;
     private static final Integer VALVE_4 = 4;
 
     private static final Integer DURATION_VALVE_1 = 10;
     private static final Integer DURATION_VALVE_2 = 10;
     private static final Integer DURATION_VALVE_3 = 10;
     private static final Integer DURATION_VALVE_4 = 25;
 
     private static final Integer PUMP = 2;
 
     public static void main(String[] args) throws Exception {
         System.out.println("*********** Starting PotWater ****************");
         System.out.println("Args:" + StringUtils.join(args));
         boolean debug = false;
         if (args.length > 0) {
             if ("debug".equalsIgnoreCase(args[0])) {
                 debug = true;
             }
         }
 
 
         Logger log = LoggerFactory.getLogger(PotwaterScheduler.class);
 
         SchedulerFactory sf = new StdSchedulerFactory();
         Scheduler sched = sf.getScheduler();
 
         JobDetail job = newJob(PotWaterJob.class)
                 .withIdentity("wetjob")
                 .usingJobData("debug", debug)
                 .usingJobData("pump", PUMP)
                 .usingJobData(VALVE_1.toString(), DURATION_VALVE_1)
                 .usingJobData(VALVE_2.toString(), DURATION_VALVE_2)
                 .usingJobData(VALVE_3.toString(), DURATION_VALVE_3)
                 .usingJobData(VALVE_4.toString(), DURATION_VALVE_4)
                 .build();
 
         CronTrigger trigger = newTrigger()
                 .withIdentity("trigger1")
                 .withSchedule(cronSchedule("0 0 20 * * ?"))
                 .build();
         Trigger testTrigger = newTrigger()
                 .startNow()
                 .build();
         Date ft = null;
        ft = sched.scheduleJob(job, trigger);
        //ft = sched.scheduleJob(job, testTrigger);
         log.info(job.getKey() + " has been scheduled to run at: " + ft
                 + " and repeat based on expression: "
                 + trigger.getCronExpression());
 
         sched.start();
 
         log.info("------- Started Scheduler -----------------");
 
 
         while (true) ;
 
     }
 }
