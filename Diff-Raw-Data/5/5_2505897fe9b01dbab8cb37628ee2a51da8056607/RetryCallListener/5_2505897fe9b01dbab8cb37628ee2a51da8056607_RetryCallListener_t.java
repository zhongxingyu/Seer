 package org.who.mcheck.core.listener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.event.listener.annotations.MotechListener;
 import org.motechproject.ivr.service.CallRequest;
 import org.motechproject.ivr.service.IVRService;
 import org.motechproject.scheduler.MotechSchedulerService;
 import org.motechproject.scheduler.domain.RunOnceSchedulableJob;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 import org.who.mcheck.core.AllConstants;
 import org.who.mcheck.core.domain.CallStatus;
 import org.who.mcheck.core.domain.CallStatusToken;
 import org.who.mcheck.core.repository.AllCallStatusTokens;
 import org.who.mcheck.core.util.IntegerUtil;
 import org.who.mcheck.core.util.LocalTimeUtil;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.UUID;
 
 import static java.text.MessageFormat.format;
 import static org.who.mcheck.core.AllConstants.BirthRegistrationFormFields.CONTACT_NUMBER;
 
 @Component
 public class RetryCallListener {
 
     private final Log log = LogFactory.getLog(RetryCallListener.class);
     private final AllCallStatusTokens allCallStatusTokens;
     private final MotechSchedulerService motechSchedulerService;
     private final IVRService callService;
     private final String callbackUrl;
     private final int retryInterval;
     private final int maximumNumberOfRetries;
 
     @Autowired
     public RetryCallListener(AllCallStatusTokens allCallStatusTokens,
                              MotechSchedulerService motechSchedulerService,
                              IVRService callService,
                              @Value("#{mCheck['ivr.callback.url']}") String callbackUrl,
                              @Value("#{mCheck['ivr.retry.interval']}") String retryInterval,
                              @Value("#{mCheck['ivr.max.retries']}") String maximumNumberOfRetries) {
         this.allCallStatusTokens = allCallStatusTokens;
         this.motechSchedulerService = motechSchedulerService;
         this.callService = callService;
         this.callbackUrl = callbackUrl;
         this.retryInterval = IntegerUtil.tryParse(retryInterval, AllConstants.DEFAULT_VALUE_FOR_RETRY_INTERVAL);
         this.maximumNumberOfRetries = IntegerUtil.tryParse(maximumNumberOfRetries, AllConstants.DEFAULT_VALUE_FOR_MAXIMUM_NUMBER_OF_RETRIES);
     }
 
     @MotechListener(subjects = AllConstants.RETRY_CALL_EVENT_SUBJECT)
     public void retry(MotechEvent event) throws Exception {
        log.info(format("Got a retry call event: {0}", event));
 
         CallStatusToken token = allCallStatusTokens.findByContactNumber(getParameter(event, CONTACT_NUMBER));
        log.info(format("Found a call status token: {0}", token));
 
         if (CallStatus.Successful.equals(token.callStatus())) {
             log.info(format("Not attempting a retry of call as the previous attempt was successful. CallStatusToken: {0}", token));
             return;
         }
         if (token.attemptNumber() > maximumNumberOfRetries) {
             log.info(format("Not attempting a retry of call as maximum number of attempts have already been made. Maximum number of retries: {0}, CallStatusToken: {1}",
                     maximumNumberOfRetries, token));
             return;
         }
 
         CallStatusToken tokenForNextRetry = new CallStatusToken(token.contactNumber(),
                 CallStatus.Unsuccessful)
                 .withDaySinceDelivery(token.daySinceDelivery())
                 .withCallAttemptNumber(token.attemptNumber() + 1);
         log.info(format("Updating CallStatusToken for next retry attempt to: {0}", tokenForNextRetry));
         allCallStatusTokens.addOrReplaceByPhoneNumber(tokenForNextRetry);
 
         Date retryTime = LocalTimeUtil.now().plusMinutes(retryInterval).toDateTimeToday().toDate();
         HashMap<String, Object> parameters = new HashMap<>();
         parameters.put(CONTACT_NUMBER, token.contactNumber());
         parameters.put(MotechSchedulerService.JOB_ID_KEY, UUID.randomUUID().toString());
         MotechEvent nextRetryEvent = new MotechEvent(AllConstants.RETRY_CALL_EVENT_SUBJECT, parameters);
         RunOnceSchedulableJob job = new RunOnceSchedulableJob(nextRetryEvent, retryTime);
 
         log.info(format("Scheduling a retry call job with the following information: {0}", job));
         motechSchedulerService.safeScheduleRunOnceJob(job);
 
         CallRequest callRequest = new CallRequest(
                 token.contactNumber(),
                 new HashMap<String, String>(),
                 format(callbackUrl, token.daySinceDelivery()));
         callService.initiateCall(callRequest);
     }
 
     private String getParameter(MotechEvent event, String name) {
         return (String) event.getParameters().get(name);
     }
 }
