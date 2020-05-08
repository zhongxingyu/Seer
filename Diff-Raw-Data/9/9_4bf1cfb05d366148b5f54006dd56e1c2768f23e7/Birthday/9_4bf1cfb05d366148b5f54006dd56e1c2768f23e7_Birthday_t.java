 package com.thoughtworks.android.model;
 
import java.util.Calendar;
 import java.util.Date;
 
 public class Birthday {
     private String date;
 
     public Birthday(String date) {
         this.date = date;
     }
 
     public String getDate() {
         return date;
     }
 
     public long getTimeInMillis() {
         String[] dateFields = date.split("/");
         int day = Integer.parseInt(dateFields[1]);
         int month = Integer.parseInt(dateFields[0]) - 1;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR) - 1900;
        int timezoneOffset = calendar.get(Calendar.ZONE_OFFSET)
                + calendar.get(Calendar.DST_OFFSET);
        return new Date(year, month, day).getTime() + timezoneOffset;
     }
 
     @Override
     public String toString() {
         return date;
     }
 }
