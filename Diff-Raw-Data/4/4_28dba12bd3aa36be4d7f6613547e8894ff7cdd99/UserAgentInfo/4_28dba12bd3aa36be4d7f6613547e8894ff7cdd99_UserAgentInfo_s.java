 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.icemobile.util;
 
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class UserAgentInfo implements Serializable{
     
     private static Logger log = Logger.getLogger(UserAgentInfo.class.getName());
 
     //info about browser and device
     private String userAgentString;
 
     //each device has list of info about device and capabilities
     public static final String IPHONE = "iphone";
     public static final String IPAD = "ipad";
     public static final String IPOD = "ipod";
     
     public static final String MAC = "macintosh"; 
     public static final String WINDOWS = "windows";
     public static final String MSIE = "msie";
     public static final String MSIE6 = "msie 6.0";
     public static final String MSIE7 = "msie 7.0";
     public static final String MSIE8 = "msie 8.0";
     public static final String MSIE9 = "msie 9";
     public static final String MSIE10 = "msie 10";
     public static final String ANDROID = "android";
     public static final String MOBILE = "mobile";
     public static final String BLACKBERRY = "blackberry";
     public static final String BLACKBERRY_CURVE = "blackberry89"; //curve2
     public static final String BLACKBERRY_TORCH = "blackberry 98"; //torch
     public static final String BLACKBERRY10 = "bb10";
     public static final String IOS5 = " os 5_";
     public static final String IOS6 = " os 6_";
     public static final String IOS7 = " os 7_";
     public static final String TABLET = "tablet";
     public static final String TABLET_GALAXY = "gt-p1000";
     public static final String TABLET_TRANSORMER_PRIME = "transformer prime";
     public static final String TABLET_IDEATAB = "ideatab";
     public static final String TABLET_KINDLE_FIRE = "kindle fire";
     public static final String PHONE_DROID2 = "droid2";
     public static final String MOBILE_SAFARI = "mobile safari";
     public static final String PHONE_HTC_SENSATION = "sensation_4g";
     public static final String ANDROID_CONTAINER = "apache-httpclient";
     public static final String PLAYBOOK = "playbook";
     public static final String FIREFOX_ANDROID_TABLET = "android; tablet;";
     public static final String FIREFOX_ANDROID_MOBILE = "android; mobile;";
     public static final String CHROME = "chrome";
     public static final String ANDROID2 = "android 2";
     
 
     public UserAgentInfo(String userAgent) {
         if (userAgent != null) {
             this.userAgentString = userAgent.toLowerCase();
         }
     }
 
     public boolean isIpod() {
         boolean result = userAgentString.indexOf(IPOD) != -1;
         log(result, "iPod", userAgentString);
         return result;
     }
 
     public boolean isIphone() {
         boolean result = userAgentString.indexOf(IPHONE) != -1
                 && !isIpod()
                 && !isIpad();
         return result;
     }
 
     public boolean isIOS(){
         boolean result = isIphone() || isIpod() ||  isIpad();
         log(result, "iOS", userAgentString);
         return result;
     }
 
     public boolean isIOS5(){
         boolean result = userAgentString.indexOf(IOS5) != -1;
         log(result, "iOS5", userAgentString);
         return result;
     }
     
     public boolean isIOS6(){
         boolean result = userAgentString.indexOf(IOS6) != -1;
         log(result, "iOS6", userAgentString);
         return result;
     }
     
     public boolean isIOS7(){
         boolean result = userAgentString.indexOf(IOS7) != -1;
         log(result, "iOS7", userAgentString);
         return result;
     }
 
     public boolean isIpad() {
         boolean result = userAgentString.indexOf(IPAD) != -1;
         log(result, "iPad", userAgentString);
         return result;
     }
 
     public boolean isAndroidOS() {
         boolean foundAndroid = (userAgentString.contains(ANDROID) || userAgentString.contains(ANDROID_CONTAINER) )
                 && !userAgentString.contains(PLAYBOOK);
         log(foundAndroid, "Android", userAgentString);
         return foundAndroid;
     }
 
     public boolean isBlackberryOS() {
         boolean result = userAgentString.contains(BLACKBERRY) || isBlackberry10OS();
         log(result, "BlackBerry", userAgentString);
         return result;
     }
     
     public boolean isBlackberry6OS() {
         boolean result = userAgentString.contains(BLACKBERRY) && !isBlackberry10OS();
         log(result, "BlackBerry", userAgentString);
         return result;
     }
     
     public boolean isBlackberry10OS() {
         boolean result = userAgentString.contains(BLACKBERRY10) || userAgentString.contains(PLAYBOOK);
         log(result, "BlackBerry 10", userAgentString);
         return result;
     }
     
     public boolean isMacOS(){
         boolean result = userAgentString.contains(MAC);
         log(result, "Macintosh", userAgentString);
         return result;
     }
     
     public boolean isWindowsOS(){
         boolean result = userAgentString.contains(WINDOWS);
         log(result, "Windows", userAgentString);
         return result;
     }
 
     private void log(boolean result, String device, 
                           String userAgent)  {
         if (log.isLoggable(Level.FINEST))  {
             if (result)  {
                 log.finest("Detected " + device + " " + userAgentString);
             }
         }
     }
     
     public boolean isDesktopBrowser(){
         if ((null != userAgentString) && userAgentString.contains(ANDROID_CONTAINER))  {
             //hack for android container
             return false;
         } 
         return !isMobileBrowser() && !isTabletBrowser();
     }
 
    /**
     * Test from http://detectmobilebrowsers.com
     * @param userAgent
     * @return true if mobile
     */
    public boolean isMobileBrowser(){
        return (userAgentString.matches("(?i).*(android.+mobile|droid2|avantgo|bada\\/|blackberry|bb10|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|lge |maemo|meego.+mobile|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|sensation_4g|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino).*")
                ||userAgentString.substring(0,4).matches("(?i)1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\-(n|u)|c55\\/|capi|ccwa|cdm\\-|cell|chtm|cldc|cmd\\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\-|_)|g1 u|g560|gene|gf\\-5|g\\-mo|go(\\.w|od)|gr(ad|un)|haie|hcit|hd\\-(m|p|t)|hei\\-|hi(pt|ta)|hp( i|ip)|hs\\-c|ht(c(\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\-(20|go|ma)|i230|iac( |\\-|\\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\/)|klon|kpt |kwc\\-|kyo(c|k)|le(no|xi)|lg( g|\\/(k|l|u)|50|54|\\-[a-w])|libw|lynx|m1\\-w|m3ga|m50\\/|ma(te|ui|xo)|mc(01|21|ca)|m\\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\-2|po(ck|rt|se)|prox|psio|pt\\-g|qa\\-a|qc(07|12|21|32|60|\\-[2-7]|i\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\-|oo|p\\-)|sdk\\/|se(c(\\-|0|1)|47|mc|nd|ri)|sgh\\-|shar|sie(\\-|m)|sk\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\\-|v\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\-|tdg\\-|tel(i|m)|tim\\-|t\\-mo|to(pl|sh)|ts(70|m\\-|m3|m5)|tx\\-9|up(\\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\\-|your|zeto|zte\\-"))
                && !userAgentString.contains(TABLET_GALAXY) 
                && !userAgentString.contains(TABLET_TRANSORMER_PRIME)
                && !userAgentString.contains(TABLET_IDEATAB)
                && !userAgentString.contains(TABLET_KINDLE_FIRE)
                && !userAgentString.contains(FIREFOX_ANDROID_TABLET);
        
    }
 
    /**
     * Adapted from http://detectmobilebrowsers.com
     * @param userAgent
     * @return true if mobile
     */
    public boolean isTabletBrowser(){
        return userAgentString.matches("(?i).*(android|ipad|playbook|silk|pocket|psp|gt-p1000|transformer prime).*")
               && !userAgentString.contains(MOBILE_SAFARI) 
               && !userAgentString.contains(PHONE_DROID2)
               && !userAgentString.contains(PHONE_HTC_SENSATION)
               && !userAgentString.contains(FIREFOX_ANDROID_MOBILE)
               || userAgentString.contains(TABLET_IDEATAB)
               || userAgentString.contains(TABLET_KINDLE_FIRE);
    }
    
    public boolean isIE(){
        return userAgentString.contains(MSIE);
    }
    
    public boolean isIE8orLess(){
        return userAgentString.contains(MSIE6) || userAgentString.contains(MSIE7) || userAgentString.contains(MSIE8);
    }
    
    public boolean isIE9(){
        return userAgentString.contains(MSIE9);
    }
    
    public boolean isIE10(){
        return userAgentString.contains(MSIE10);
    }
    
    public boolean isChrome(){
        return userAgentString.contains(CHROME);
    }
    
    public boolean isFirefoxAndroid(){
        return userAgentString.contains(FIREFOX_ANDROID_MOBILE) || userAgentString.contains(FIREFOX_ANDROID_TABLET);
    }
    
    /* Android browser and webview share the same useragent */
    public boolean isAndroidBrowserOrWebView(){
        return isAndroidOS() && (!isChrome() && !isFirefoxAndroid());
    }
    
    public boolean isAndroid2(){
        return userAgentString.contains(ANDROID2);
    }
 
 
 }
