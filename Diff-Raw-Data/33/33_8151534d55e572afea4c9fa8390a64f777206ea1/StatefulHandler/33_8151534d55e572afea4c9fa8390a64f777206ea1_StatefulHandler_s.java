 package com.sfdc.http.client.handler;
 
 import com.ning.http.client.AsyncHandler;
 import com.ning.http.client.Response;
 import com.sfdc.http.client.NingResponse;
 import com.sfdc.http.smc.StreamingClient;
 import com.sfdc.stats.StatsManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author psrinivasan
  *         Date: 9/10/12
  *         Time: 9:42 PM
  *         Reason this class exists is to store the client's state object, and call it on
  *         completion of a streaming operation.  ie., to make each client responsible for it's
  *         own life cycle since we're in non blocking heaven :)
  *         <p/>
  *         This class, just like it's parent, needs to be very light.
  */
 public class StatefulHandler extends GenericAsyncHandler implements AsyncHandler {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(StatefulHandler.class);
     private StreamingClient streamingClient;
     private StatsManager statsManager;
 
     public StatefulHandler(StreamingClient sc, StatsManager statsManager) {
         super();
         this.streamingClient = sc;
         this.statsManager = statsManager;
     }
 
     @Override
     public void startRequestTimer() {
         super.startRequestTimer();
     }
 
     public Object onSuccessfulHttpResponse(Object retVal) throws Exception {
         NingResponse response = new NingResponse((Response) retVal);
         if (!isResponseSucessful(response)) {
             LOGGER.error("Request failed!");
             LOGGER.error("Request failed. State is: " + streamingClient.getState() + " Response is: " + response.getResponseBody());
             //TODO:  have more meaningful log line that includes info about which request failed.
             return retVal;
         }
         String s = getOperationType(response);
         LOGGER.debug("RESPONSE OPERATION = " + s);
         if (s.equals("/meta/handshake")) {
             if (statsManager != null) {
                 statsManager.incrementHandshakeCount();
             }
             System.out.println("handshake complete - calling onHandshakeComplete ...");
             streamingClient.onHandshakeComplete(response.getCookies(), response.getClientId());
 
         } else if (s.equals("/meta/subscribe")) {
             if (statsManager != null) {
                 statsManager.incrementSubscriptionCount();
             }
             System.out.println("subscription complete - calling onSubscribeComplete ...");
             streamingClient.onSubscribeComplete();
 
         } else if (s.equals("/meta/connect")) {
             if (statsManager != null) {
                 statsManager.incrementConnectCount();
             }
             System.out.println("connect complete - calling onConnectComplete ...");
             streamingClient.onConnectComplete();
 
         } else {
             if (statsManager != null) {
                statsManager.getOtherHttp200Count();
             }
             LOGGER.warn("Fell through completed operation recognition! Could not classify response as an expected streaming operation");
 
         }
         return retVal;
 
     }
 
     @Override
     public Object onCompleted() throws Exception {
         Object retVal = super.onCompleted();
         Response response = (Response) retVal;
         switch (response.getStatusCode()) {
             case 200:
                 onSuccessfulHttpResponse(retVal);
                 break;
             case 401:
                 //bad auth credentials.  ie., invalid session id.
                 //make the client go to Failed state.
                 streamingClient.onInvalidAuthCredentials(response);
                 break;
             case 500:
                 //Oooo.  No saying what happened here.
                 //Go to Failed state.
                 break;
             default:
                 //Surprise, Go to failed state
                 break;
         }
         return retVal;
     }
 
     public boolean isResponseSucessful(NingResponse response) throws Exception {
         return (response.getBayeuxSuccessResponseField() && (response.getStatusCode() == 200));
     }
 
 
 }
