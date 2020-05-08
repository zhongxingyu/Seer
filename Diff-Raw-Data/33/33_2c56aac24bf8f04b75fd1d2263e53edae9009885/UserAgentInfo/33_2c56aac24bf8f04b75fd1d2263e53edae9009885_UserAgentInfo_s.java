 /*
  * Copyright 2004-2011 ICEsoft Technologies Canada Corp. (c)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions an
  * limitations under the License.
  */
 package org.icefaces.component.utils;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * backbone of deviceStylesheet as it requires these
  * objects for device detection.
  */
 public class UserAgentInfo {
     private static Logger log = Logger.getLogger(
             UserAgentInfo.class.getName());
 
     //info about browser and device
     private String userAgentString;
 
     //what browser can display
     private String httpAccepted;
 
     //each device has list of info about device and capabilities
     public static final String deviceIphone = "iphone";
     public static final String deviceIpad = "ipad";
     public static final String deviceIpod = "ipod";
     public static final String deviceMac = "macintosh"; //test laptop
     public static final String deviceAndroid = "android";
     public static final String deviceBlackB = "blackberry";
     public static final String deviceBBCurve = "blackberry89"; //curve2
     public static final String deviceBBTorch = "blackberry 98"; //torch
     public static final String vndRIM = "vnd.rim";  //found when emulating IE or FF on BB
     public static final String deviceIOS5 = " os 5_0";
 
     public UserAgentInfo(String userAgent, String httpAcc) {
         if (userAgent != null) {
             this.userAgentString = userAgent.toLowerCase();
         }
         if (httpAcc != null) {
             this.httpAccepted = httpAcc.toLowerCase();
         }
     }
 
     public UserAgentInfo(String userAgent) {
         if (userAgent != null) {
             this.userAgentString = userAgent.toLowerCase();
         }
         this.httpAccepted = "*/*";
     }
 
     public boolean sniffIpod() {
         boolean result = userAgentString.indexOf(deviceIpod) != -1;
         logSniff(result, "iPod", userAgentString);
         return result;
     }
 
     //don't get iPhone confused with iPod touch
     public boolean sniffIphone() {
         boolean result = userAgentString.indexOf(deviceIphone) != -1
                 && !sniffIpod()
                 && !sniffIpad();
         logSniff(result, "iPod", userAgentString);
         return result;
     }
 
     public boolean sniffIOS5(){
         boolean result = userAgentString.indexOf(deviceIOS5) != -1;
         logSniff(result, "iOS5", userAgentString);
         return result;
     }
 
     public boolean sniffIpad() {
         boolean result = userAgentString.indexOf(deviceIpad) != -1;
         logSniff(result, "iPad", userAgentString);
         return result;
     }
 
     public boolean sniffAndroid() {
         boolean foundAndroid = userAgentString.contains(deviceAndroid) &&
                 userAgentString.contains("mobile safari");
 
         logSniff(foundAndroid, "Android Mobile", userAgentString);
         return foundAndroid;
     }
 
     public boolean sniffAndroidTablet() {
         boolean result = userAgentString.contains(deviceAndroid) &&
                 !userAgentString.contains("mobile safari");
         // android tablet won't have the "mobile" on the agent at least for 3.x
         logSniff(result, "Android Tablet", userAgentString);
         return result;
     }
 
     public boolean sniffBlackberry() {
         boolean result = userAgentString.contains(deviceBlackB) 
             || httpAccepted.contains(vndRIM);
         logSniff(result, "BlackBerry", userAgentString);
         return result;
     }
 
     private void logSniff(boolean result, String device, 
                           String userAgent)  {
         if (log.isLoggable(Level.FINEST))  {
             if (result)  {
                 log.finest("Detected " + device + " " + userAgentString);
             }
         }
     }
 }
