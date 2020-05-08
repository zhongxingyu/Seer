 package org.motechproject.ananya.kilkari.web.controller.page;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.motechproject.ananya.kilkari.obd.domain.Channel;
 import org.motechproject.ananya.kilkari.service.KilkariSubscriptionService;
 import org.motechproject.ananya.kilkari.subscription.service.response.SubscriptionDetailsResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Component
 public class InquiryPage {
 
     private KilkariSubscriptionService kilkariSubscriptionService;
 
     @Autowired
     public InquiryPage(KilkariSubscriptionService kilkariSubscriptionService) {
         this.kilkariSubscriptionService = kilkariSubscriptionService;
     }
 
     public Map<String, Object> getSubscriptionDetails(String msisdn) {
         Map<String, Object> model = new HashMap<>();
         try {
             List<SubscriptionDetailsResponse> subscriptionDetails = kilkariSubscriptionService.getSubscriptionDetails(msisdn, Channel.CONTACT_CENTER);
             model.put("subscriptionDetails", mapToSubscriptionGrid(subscriptionDetails));
         } catch (Exception e) {
            model.put("subscriberError", ExceptionUtils.getFullStackTrace(e));
         }
         return model;
     }
 
     private SubscriptionDataGrid mapToSubscriptionGrid(List<SubscriptionDetailsResponse> subscriptionDetails) {
         return new SubscriptionDataGrid(subscriptionDetails);
     }
 
     public ModelAndView display() {
         return new ModelAndView("admin/inquiry");
     }
 }
