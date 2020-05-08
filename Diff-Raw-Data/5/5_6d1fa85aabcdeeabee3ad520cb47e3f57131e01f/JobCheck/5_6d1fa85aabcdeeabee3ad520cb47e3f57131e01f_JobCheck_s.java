 package com.bia.monitor.service;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Date;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author intesar
  */
 class JobCheck implements Runnable {
 
     private Job job;
     protected static Logger logger = Logger.getLogger(JobCheck.class);
 
     JobCheck(Job job) {
         this.job = job;
     }
 
     public void run() {
         int responseCode = 0;
         try {
             if (logger.isTraceEnabled()) {
                 logger.trace(" pinging " + job.getUrl());
             }
             HttpURLConnection connection = (HttpURLConnection) new URL(job.getUrl()).openConnection();
             connection.setRequestMethod("GET");
             responseCode = connection.getResponseCode();
             if (responseCode == 200) {
                 // OK.
                 if (logger.isTraceEnabled()) {
                     logger.trace(" ping successful " + job.getUrl());
                 }
                 job.setStatus("Running");
                 if (!job.isLastUp()) {
                     job.setLastUp(true);
                     job.setUpSince(new Date());
                     // send site up notification
                     int mins = (int) ((new Date().getTime() / 60000) - (job.getDownSince().getTime() / 60000));
                     StringBuilder body = new StringBuilder();
                     body.append(job.getUrl()).append(" is Up after ").append(mins).append(" mins of downtime!");
                     EmailService.getInstance().sendEmail(job.getEmail(), job.getUrl() + " is Up!", body.toString());
                 }
                 return;
             }

             // < 100 is undertermined.
             // 1nn is informal (shouldn't happen on a GET/HEAD)
             // 2nn is success
             // 3nn is redirect
             // 4nn is client error
             // 5nn is server error
         } catch (Exception ex) {
             logger.error("Ping returned status code : " + responseCode );
         }
         if (logger.isTraceEnabled()) {
             logger.trace(" ping failed " + job.getUrl() + " status code : " + responseCode);
         }
         // only send mail for the first time
         if (job.isLastUp()) {
             job.setLastUp(false);
             job.setDownSince(new Date());
             job.setStatus("Down");
             // send alert email
             StringBuilder body = new StringBuilder();
             body.append(job.getUrl()).append(" is Down! ");
             EmailService.getInstance().sendEmail(job.getEmail(), job.getUrl() + " is Down!", "Detected down on : " + (new Date()));
         }
 
     }
 }
