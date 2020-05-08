 package org.motechproject.ghana.mtn.process;
 
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ghana.mtn.domain.MessageBundle;
 import org.motechproject.ghana.mtn.domain.Subscription;
 import org.motechproject.ghana.mtn.domain.dto.SMSServiceRequest;
 import org.motechproject.ghana.mtn.service.SMSService;
 import org.motechproject.ghana.mtn.validation.ValidationError;
 
 import java.util.List;
 
 public abstract class BaseSubscriptionProcess {
 
     private SMSService smsService;
     private MessageBundle messageBundle;
 
     protected BaseSubscriptionProcess(SMSService smsService, MessageBundle messageBundle) {
         this.smsService = smsService;
         this.messageBundle = messageBundle;
     }
 
     protected String messageFor(String key) {
         return messageBundle.get(key);
     }
 
     protected String messageFor(List<ValidationError> validationErrors) {
         return messageBundle.get(validationErrors);
     }
 
     protected void sendMessage(Subscription subscription, String content) {
        String message = StringUtils.replace(content, MessageBundle.PROGRAM_NAME_MARKER, subscription.programName());
        SMSServiceRequest smsServiceRequest = new SMSServiceRequest(subscription.subscriberNumber(), message, subscription.getProgramType());
         smsService.send(smsServiceRequest);
     }
 
     protected void sendMessage(String mobileNumber, String content) {
        String message = StringUtils.replace(content, MessageBundle.PROGRAM_NAME_MARKER, StringUtils.EMPTY);
        SMSServiceRequest smsServiceRequest = new SMSServiceRequest(mobileNumber, message);
         smsService.send(smsServiceRequest);
     }
 
 }
