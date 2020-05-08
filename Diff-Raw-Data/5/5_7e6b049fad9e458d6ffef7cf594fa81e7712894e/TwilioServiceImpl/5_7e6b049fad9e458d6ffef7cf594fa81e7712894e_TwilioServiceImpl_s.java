 package com.fingy.robocall.service.impl;
 
 import com.fingy.robocall.dao.CallRequestRepository;
 import com.fingy.robocall.model.CallRequest;
 import com.fingy.robocall.model.dto.RoboCallRequest;
 import com.fingy.robocall.service.TwilioService;
 import com.twilio.sdk.TwilioRestClient;
 import com.twilio.sdk.TwilioRestException;
 import com.twilio.sdk.resource.factory.CallFactory;
 import com.twilio.sdk.resource.instance.Call;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.scheduling.TaskScheduler;
 import org.springframework.stereotype.Service;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.fingy.robocall.util.DateTimeUtil.isAfterHalfPastEightPm;
 import static com.fingy.robocall.util.DateTimeUtil.isBeforeHalfPastEightAm;
 import static com.fingy.robocall.util.DateTimeUtil.toEightThirtyAmTomorrow;
 
 @Service
 public class TwilioServiceImpl implements TwilioService {
     private static final String URL_PARAM_NAME = "Url";
     private static final String TO_PARAM_NAME = "To";
     private static final String FROM_PARAM_NAME = "From";
     private static final String STATUS_CALLBACK_PARAM_NAME = "StatusCallback";
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private @Value("${application.key}") String key;
     private @Value("${twilio.sid}") String accountSid;
     private @Value("${twilio.auth}") String authenticationToken;
     private @Value("${twilio.phone}") String twilioPhoneNumber;
 
     private @Autowired CallRequestRepository requestRepository;
     private @Autowired TaskScheduler taskScheduler;
 
     @Override
     public CallRequest placeCall(RoboCallRequest callRequest, String rootUrl) throws TwilioRestException, UnsupportedEncodingException {
         List<NameValuePair> params = createCallParameters(callRequest, rootUrl);
         String sid = placeCall(params);
         CallRequest callDetails = toCallRequest(callRequest, rootUrl, sid);
         requestRepository.save(callDetails);
         return callDetails;
     }
 
     private List<NameValuePair> createCallParameters(RoboCallRequest callRequest, String rootUrl) throws UnsupportedEncodingException {
         String callbackUrl = rootUrl + "/twilio-callback?" + callRequest.toQueryParamString();
         String statusCallbackUrl = rootUrl + "/twilio-status-callback";
 
         List<NameValuePair> params = new ArrayList<>();
         params.add(new BasicNameValuePair(URL_PARAM_NAME, callbackUrl));
         params.add(new BasicNameValuePair(TO_PARAM_NAME, callRequest.getPhoneNumber()));
         params.add(new BasicNameValuePair(FROM_PARAM_NAME, twilioPhoneNumber));
         params.add(new BasicNameValuePair(STATUS_CALLBACK_PARAM_NAME, statusCallbackUrl));
         return params;
     }
 
     private String placeCall(List<NameValuePair> params) throws TwilioRestException {
         TwilioRestClient client = new TwilioRestClient(accountSid, authenticationToken);
         CallFactory callFactory = client.getAccount().getCallFactory();
         Call call = callFactory.create(params);
         return call.getSid();
     }
 
     private CallRequest toCallRequest(RoboCallRequest callRequest, String rootUrl, String sid) {
         return new CallRequest(sid, callRequest.getText(), callRequest.getLanguage(), callRequest.getPhoneNumber(), rootUrl);
     }
 
     @Override
     public void scheduleRedial(String callSid) {
         CallRequest callRequest = requestRepository.findBySid(callSid);
        DateTime current = new DateTime(DateTimeZone.forID("GMT+1"));
         DateTime redialTime = shouldReschedule(current) ? toEightThirtyAmTomorrow(current) : current.plusMinutes(15);
 
         logger.info("Scheduling a redial for SID {}, redial time is {}", callSid, redialTime);
         callRequest.setRedialTime(redialTime.toDate());
         CallRequest saved = requestRepository.save(callRequest);
         taskScheduler.schedule(new RedialRunnable(saved), redialTime.toDate());
     }
 
     @Override
     public void statusUpdate(String callSid, String callStatus) {
         if ("busy".equals(callStatus) || "no-answer".equals(callStatus)) {
             scheduleRedial(callSid);
        } else {
             CallRequest toDelete = requestRepository.findBySid(callSid);
             requestRepository.delete(toDelete);
         }
     }
 
     private boolean shouldReschedule(DateTime redialTime) {
         return isAfterHalfPastEightPm(redialTime) || isBeforeHalfPastEightAm(redialTime);
     }
 
     private class RedialRunnable implements Runnable {
         private final CallRequest callRequest;
 
         public RedialRunnable(CallRequest callRequest) {
             this.callRequest = callRequest;
         }
 
         @Override
         public void run() {
             try {
                 logger.info("Redialing call {}", callRequest.getSid());
                 RoboCallRequest request = toRoboCallRequest(callRequest);
                 placeCall(request, callRequest.getRootUrl());
                 requestRepository.delete(callRequest);
             } catch (Exception e) {
                 logger.error("Error redialing call " + callRequest.getSid(), e);
             }
         }
 
         private RoboCallRequest toRoboCallRequest(CallRequest callRequest) {
             return new RoboCallRequest(callRequest.getText(), callRequest.getLanguage(), callRequest.getPhoneNumber(), 1, key);
         }
     }
 }
