 package com.cloudbees.gasp.twitter;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.ResultReceiver;
 import android.test.ServiceTestCase;
 import android.util.Base64;
 
 import com.cloudbees.gasp.model.TwitterTokenResponse;
 import com.cloudbees.gasp.service.RESTIntentService;
 import com.google.gson.Gson;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Copyright (c) 2013 Mark Prichard, CloudBees
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 public class TwitterAuthServiceTest extends ServiceTestCase<RESTIntentService> {
 
     private CountDownLatch signal = new CountDownLatch(1);
     private ResultReceiver mResultReceiver;
 
     public TwitterAuthServiceTest() {
         super(RESTIntentService.class);
 
         mResultReceiver = new ResultReceiver(new Handler()) {
 
             @Override
             protected void onReceiveResult(int resultCode, Bundle resultData) {
                 if (resultData != null && resultData.containsKey(RESTIntentService.REST_RESULT)) {
                     onRESTResult(resultCode, resultData.getString(RESTIntentService.REST_RESULT));
                 }
                 else {
                     onRESTResult(resultCode, null);
                 }
             }
         };
     }
 
     public void onRESTResult(int code, String result) {
         if (code == 200 && result != null) {
             TwitterTokenResponse twitterToken = new Gson().fromJson(result, TwitterTokenResponse.class);
             assertNotNull(twitterToken);
            assert (twitterToken.getToken_type().equalsIgnoreCase("bearer"));
         }
         else {
             fail();
         }
         signal.countDown();
     }
 
     public void testTwitterAuthUtils() {
         assertNotNull(TwitterAuthentication.getEncodedBase64Credentials());
         try{
             String authEncodedBase64 =
                     TwitterAuthentication.getEncodedBase64Credentials().replaceFirst("Basic ", "");
             Base64.decode(authEncodedBase64, Base64.NO_WRAP);
         }
         catch (Exception e) {
             fail();
         }
         assertNotNull(TwitterAuthentication.getTwitterApiOAuthToken());
     }
 
     public void testTwitterAuth() {
         try {
             Intent intent = new Intent(getContext(), RESTIntentService.class);
             intent.setData(Uri.parse(TwitterAuthentication.getTwitterApiOAuthToken()));
 
             Bundle params = new Bundle();
             params.putString("grant_type", "client_credentials");
 
             Bundle headers = new Bundle();
             headers.putString("Authorization", TwitterAuthentication.getEncodedBase64Credentials());
 
             intent.putExtra(RESTIntentService.EXTRA_HTTP_VERB, RESTIntentService.POST);
             intent.putExtra(RESTIntentService.EXTRA_HEADERS, headers);
             intent.putExtra(RESTIntentService.EXTRA_PARAMS, params);
             intent.putExtra(RESTIntentService.EXTRA_RESULT_RECEIVER, mResultReceiver);
 
             signal.await(10, TimeUnit.SECONDS);
         }
         catch (Exception e) {
             fail();
         }
     }
 }
