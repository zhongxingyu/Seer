 package com.twu.refactoring;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.TimeZone;
 
 public class DateParser {
     private final String dateAndTimeString;
     private static final HashMap<String, TimeZone> KNOWN_TIME_ZONES = new HashMap<String, TimeZone>();
 
     static {
         KNOWN_TIME_ZONES.put("UTC", TimeZone.getTimeZone("UTC"));
     }
 
     /**
      * Takes a date in ISO 8601 format and returns a date
      *
      * @param dateAndTimeString - should be in format ISO 8601 format
      *                          examples -
      *                          2012-06-17 is 17th June 2012 - 00:00 in UTC TimeZone
      *                          2012-06-17TZ is 17th June 2012 - 00:00 in UTC TimeZone
      *                          2012-06-17T15:00Z is 17th June 2012 - 15:00 in UTC TimeZone
      */
     public DateParser(String dateAndTimeString) {
         this.dateAndTimeString = dateAndTimeString;
     }
 
     public Date parse() {
         int year, month, date, hour, minute;
 
         try {
             String yearString = dateAndTimeString.substring(0, 4);
             year = Integer.parseInt(yearString);
         } catch (StringIndexOutOfBoundsException e) {
             throw new IllegalArgumentException("Year string is less than 4 characters");
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException("Year is not an integer");
         }
         if (year < 2000 || year > 2012)
             throw new IllegalArgumentException("Year cannot be less than 2000 or more than 2012");
 
         try {
             String monthString = dateAndTimeString.substring(5, 7);
             month = Integer.parseInt(monthString);
         } catch (StringIndexOutOfBoundsException e) {
             throw new IllegalArgumentException("Month string is less than 2 characters");
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException("Month is not an integer");
         }
         if (month < 1 || month > 12)
             throw new IllegalArgumentException("Month cannot be less than 1 or more than 12");
 
         try {
             String dateString = dateAndTimeString.substring(8, 10);
             date = Integer.parseInt(dateString);
         } catch (StringIndexOutOfBoundsException e) {
             throw new IllegalArgumentException("Date string is less than 2 characters");
         } catch (NumberFormatException e) {
             throw new IllegalArgumentException("Date is not an integer");
         }
         if (date < 1 || date > 31)
             throw new IllegalArgumentException("Date cannot be less than 1 or more than 31");
 
         if (dateAndTimeString.substring(11, 12).equals("Z")) {
             hour = 0;
             minute = 0;
         } else {
             try {
                 String hourString = dateAndTimeString.substring(11, 13);
                 hour = Integer.parseInt(hourString);
             } catch (StringIndexOutOfBoundsException e) {
                 throw new IllegalArgumentException("Hour string is less than 2 characters");
             } catch (NumberFormatException e) {
                 throw new IllegalArgumentException("Hour is not an integer");
             }
             if (hour < 0 || hour > 23)
                 throw new IllegalArgumentException("Hour cannot be less than 0 or more than 23");
 
             try {
                 String minuteString = dateAndTimeString.substring(14, 16);
                 minute = Integer.parseInt(minuteString);
             } catch (StringIndexOutOfBoundsException e) {
                 throw new IllegalArgumentException("Minute string is less than 2 characters");
             } catch (NumberFormatException e) {
                 throw new IllegalArgumentException("Minute is not an integer");
             }
             if (minute < 0 || minute > 59)
                 throw new IllegalArgumentException("Minute cannot be less than 0 or more than 59");
 
         }
 
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
         calendar.set(year, month - 1, date, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);
         return calendar.getTime();
     }
 }
