 package org.motechproject.ananya.kilkari.web.domain;
 
 import org.apache.commons.lang.StringUtils;
 
 public enum CallbackStatus {
    FAILURE, SUCCESS,ERROR,BAL_LOW,GRACE,SUS;
 
     public static CallbackStatus getFor(String status) {
         return CallbackStatus.valueOf(StringUtils.trimToEmpty(status).toUpperCase());
     }
 
     public static boolean isValid(String callbackStatus) {
         return (callbackStatus != null && CallbackStatus.contains(callbackStatus));
     }
 
     private static boolean contains(String value) {
         for (CallbackStatus callbackStatus : CallbackStatus.values()) {
             if (callbackStatus.name().equals(StringUtils.trimToEmpty(value).toUpperCase())) {
                 return true;
             }
         }
         return false;
     }
 }
