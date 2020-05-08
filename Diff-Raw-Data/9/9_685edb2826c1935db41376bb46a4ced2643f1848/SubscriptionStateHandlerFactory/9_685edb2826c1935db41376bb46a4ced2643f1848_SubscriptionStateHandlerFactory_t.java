 package org.motechproject.ananya.kilkari.handlers;
 
 import org.motechproject.ananya.kilkari.domain.CallbackRequestWrapper;
 import org.motechproject.ananya.kilkari.service.SubscriptionService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.HashMap;
 
 @Component
 public class SubscriptionStateHandlerFactory {
     public static final String MAPPING_SEPARATOR = "|";
     public static final String SUCCESS = "SUCCESS";
     public static final String FAILURE = "FAILURE";
     private SubscriptionService subscriptionService;
 
     private static HashMap<String, Class> handlerMappings = new HashMap<String, Class>() {{
         put("ACT|SUCCESS", ActivateHandler.class);
         put("ACT|FAILURE", ActivationFailedHandler.class);
     }};
 
     @Autowired
     public SubscriptionStateHandlerFactory(SubscriptionService subscriptionService) {
         this.subscriptionService = subscriptionService;
     }
 
     public SubscriptionStateHandler getHandler(CallbackRequestWrapper callbackRequestWrapper) {
        String status = callbackRequestWrapper.getStatus().equals(SUCCESS) ? callbackRequestWrapper.getStatus() : FAILURE;
         String actionAndStatus = callbackRequestWrapper.getAction() + MAPPING_SEPARATOR + status;
         SubscriptionStateHandler subscriptionStateHandler = null;
         try {
            subscriptionStateHandler = (SubscriptionStateHandler) handlerMappings.get(actionAndStatus).newInstance();
         } catch (InstantiationException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IllegalAccessException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         subscriptionStateHandler.setSubscriptionService(subscriptionService);
         return subscriptionStateHandler;
     }
 
     public HashMap<String, Class> getHandlerMappings() {
         return handlerMappings;
     }
 }
