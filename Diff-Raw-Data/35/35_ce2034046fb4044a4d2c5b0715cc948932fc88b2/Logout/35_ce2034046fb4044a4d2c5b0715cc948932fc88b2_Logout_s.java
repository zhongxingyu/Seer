 package io.appery.tester.net.api;
 
 import io.appery.tester.net.RestClient.RequestMethod;
 import io.appery.tester.net.RestManager;
 import io.appery.tester.utils.Constants;
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 
 /**
  * @author Daniel Lukashevich
  */
 public class Logout extends BaseRequest {
 
     private static final String TAG = "Logout";
 
     public Logout(RestManager restManager) {
         super(restManager, Constants.API.LOGOUT);
     }
 
     @Override
     HttpResponse doRequest() throws ClientProtocolException, IOException, Exception {
        return getRestClient().execute(RequestMethod.DELETE);
     }
 
     @Override
     public void onRequestReceived(BaseResponse response) {
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see io.appery.tester.net.api.BaseRequest#getTag()
      */
     @Override
     protected String getTag() {
         return TAG;
     }
 
 }
