 package com.kinnack.nthings.helper;
 
 import java.util.Calendar;
 import java.util.Date;
 
 public class PrettyDate {
     public static String format(Date then_) {
         Date now = new Date();
         long msBetween = now.getTime() - then_.getTime();
         long daysBetween = msBetween / 86400000;
         long hoursBetween = msBetween / 3600000;
        long minutesBetween = msBetween / 3600;
         long secondsBetween = msBetween / 1000;
         
         if (daysBetween > 0) {
             return daysBetween + " day"+(daysBetween == 1?"":"s")+" ago";
         } else if (hoursBetween > 0){
             return hoursBetween + " hour"+(hoursBetween == 1?"":"s")+" ago";
         } else if (minutesBetween > 0) {
             return minutesBetween + " minute"+(minutesBetween == 1?"":"s")+" ago";
         } else {
             return secondsBetween + " second"+(secondsBetween == 1?"":"s")+" ago";
         }
         
     }
 }
