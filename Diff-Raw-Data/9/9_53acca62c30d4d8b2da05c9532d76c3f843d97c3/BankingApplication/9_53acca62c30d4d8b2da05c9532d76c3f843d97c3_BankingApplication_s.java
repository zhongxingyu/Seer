 /*******************************************************************************
  * Copyright (c) 2012 MASConsult Ltd
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package eu.masconsult.bgbanking;
 
 import org.acra.ACRA;
 import org.acra.annotation.ReportsCrashes;
 
 import android.app.Application;
 
 import com.zubhium.ZubhiumSDK;
 
@ReportsCrashes(formUri = "https://api.zubhium.com/api2/acra/?secret_key=app_secret_key", formKey = "")
 public class BankingApplication extends Application {
 
     public static final String TAG = "bgB.";
 
     ZubhiumSDK zubhiumSDK;
 
     @Override
     public void onCreate() {
         ACRA.init(this);
 
         super.onCreate();
 
        zubhiumSDK = ZubhiumSDK.getZubhiumSDKInstance(this, getString(R.string.zubhium_key));
         zubhiumSDK.enableCrashReporting(false);
     }
 
     public ZubhiumSDK getZubhiumSDK() {
         return zubhiumSDK;
     }
 }
