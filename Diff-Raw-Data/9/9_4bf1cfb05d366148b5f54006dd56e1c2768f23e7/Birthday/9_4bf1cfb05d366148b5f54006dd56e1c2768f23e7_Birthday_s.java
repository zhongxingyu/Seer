 package com.thoughtworks.android.model;
 
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
        int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - 1900;
        return new Date(year, month, day).getTime();
     }
 
     @Override
     public String toString() {
         return date;
     }
 }
