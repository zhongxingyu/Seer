 package org.motechproject.ananya.reports.kilkari.purge.service;
 
 import org.apache.commons.io.FileUtils;
 import org.motechproject.ananya.reports.kilkari.service.CampaignScheduleAlertService;
 import org.motechproject.ananya.reports.kilkari.service.SubscriberCallMeasureService;
 import org.motechproject.ananya.reports.kilkari.service.SubscriptionService;
 import org.motechproject.ananya.reports.kilkari.service.SubscriptionStatusMeasureService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 @Service
 public class ReportsPurgeService {
     private SubscriberCallMeasureService subscriberCallMeasureService;
     private SubscriptionStatusMeasureService subscriptionStatusMeasureService;
     private SubscriptionService subscriptionService;
     private CampaignScheduleAlertService campaignScheduleAlertService;
     private final Logger logger = LoggerFactory.getLogger(ReportsPurgeService.class);
 
     @Autowired
     public ReportsPurgeService(SubscriberCallMeasureService subscriberCallMeasureService, SubscriptionStatusMeasureService subscriptionStatusMeasureService, SubscriptionService subscriptionService, CampaignScheduleAlertService campaignScheduleAlertService) {
         this.subscriberCallMeasureService = subscriberCallMeasureService;
         this.subscriptionStatusMeasureService = subscriptionStatusMeasureService;
         this.subscriptionService = subscriptionService;
         this.campaignScheduleAlertService = campaignScheduleAlertService;
     }
 
     public void purgeSubscriptionData(String filePath) throws IOException {
         List<String> msisdnList = readFile(filePath);
 
         for (String msisdn : msisdnList) {
             purgeFor(msisdn);
         }
     }
 
     private List<String> readFile(String filePath) throws IOException {
         File inputFile = new File(filePath);
         return FileUtils.readLines(inputFile);
     }
 
     private void purgeFor(String msisdn) {
         msisdn = msisdn.trim();
         if(msisdn.isEmpty()) {
             return;
         }
 
        logger.info("Started purging report records for msisdn: " + msisdn);
         Long msisdnInLong;
         try{
             msisdnInLong = Long.valueOf(msisdn);
         }catch (Exception e){
            logger.info("Invalid non numeric msisdn: " + msisdn);
             return;
 
         }
         subscriberCallMeasureService.deleteFor(msisdnInLong);
         subscriptionStatusMeasureService.deleteFor(msisdnInLong);
         campaignScheduleAlertService.deleteFor(msisdnInLong);
         subscriptionService.deleteFor(msisdnInLong);
        logger.info("Finished purging report records for msisdn: " + msisdn);
     }
 }
