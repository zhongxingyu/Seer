 package io.appery.tester.net.api;
 
 import io.appery.tester.net.RestClient.RequestMethod;
 import io.appery.tester.net.RestManager;
 import io.appery.tester.net.api.callback.LoginCallback;
 import io.appery.tester.utils.Base64;
 import io.appery.tester.utils.Constants;
 
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 
import android.util.Log;

 /**
  * This class provides functionality for Login API <BR/>
  * It used HTTP Base Authentification <BR/>
  * 
  * @author Daniel Lukashevich
  */
 public class Login extends BaseRequest {
 
     private static final String TAG = "Login";
 
     private LoginCallback callback;
 
     private String login = "";
 
     private String password = "";
 
     public Login(RestManager restManager, String login, String password, LoginCallback callback) {
         super(restManager, Constants.API.LOGIN);
 
         this.login = login;
         this.password = password;
         this.callback = callback;
     }
 
     @Override
     HttpResponse doRequest() throws ClientProtocolException, IOException, Exception {
         String credentials = Base64.encodeBytes((login + ":" + password).getBytes());
         getRestClient().addHeader("Authorization", "Basic " + credentials);
 
         return getRestClient().execute(RequestMethod.GET);
     }
 
     @Override
     public void onRequestReceived(BaseResponse response) {
         if (response.hasError()) {
             callback.onLoginFailed(response.getMessage());
         } else {
             callback.onLoginSuccess(response.getMessage());
         }
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
