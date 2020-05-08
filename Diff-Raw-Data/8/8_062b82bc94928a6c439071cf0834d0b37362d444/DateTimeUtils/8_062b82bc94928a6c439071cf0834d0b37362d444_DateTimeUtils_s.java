 /*
  * DateTimeUtils.java
  *
  * Created on March 21, 2008, 9:41 AM
  *
  */
 
 package org.j2free.jsp.el;
 
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  *
  * @author ryan
  */
 public class DateTimeUtils {
     
     public static int getCountdown(java.util.Date event) {
 
         if (event == null)
             return -1;
         
         Date today  = new Date();
         long todayEpoch  = today.getTime();
         long targetEpoch = event.getTime();
         
         return ((Double)Math.floor(((targetEpoch - todayEpoch) / (60*60*24)) / 1000)).intValue();
     }
     
     /**
      * @author  Ryan
      * @return  a formatted version of the timestamp
      *
      *  2007-04-19 17:41:02.0  becomes  April 19th, 2007 5:41pm
      */
     private static String getTimestampInEnglish(java.sql.Timestamp t) throws IllegalArgumentException {
 
         if (t == null)
             return "";
 
         String s = t.toString();
         if (s.indexOf(".0") > 0)
             s = s.substring(0,s.indexOf(".0"));
         
         String[] sr = s.split(" ");
         
         int month   = -1;
         int day     = -1;
         int year    = -1;
         int hours   = -1;
         int minutes = -1;
         
         String mth = "", dy = "", yr = "", hrs = "", mnts = "", ampm = "";
         
         try {
             String[] sd = sr[0].split("-");
             String[] st = sr[1].split(":");
             
             try {
                 hours = Integer.parseInt(st[0]);
                 month = Integer.parseInt(sd[1]);
                 day   = Integer.parseInt(sd[2]);
             } catch (Exception e) {
                 throw new IllegalArgumentException("Invalid hour");
             }
             
             if (hours < 0)
                 throw new IllegalArgumentException("Invalid timestamp");
             
             if (hours >= 12) {
                 hours = hours - 12;
                 ampm = "pm";
             } else {
                 ampm = "am";
             }
             
             if (hours == 0)
                 hrs = "12";
             else
                 hrs = "" + hours;
             
             mnts = st[1];
             
             yr  = sd[0];
             mth = getMonth(month);
             dy  = formatNumberWithExtension(day);
             
         } catch (IndexOutOfBoundsException ioobe) {
             throw new IllegalArgumentException("Invalid timestamp format");
         }
         return mth + " " + dy + ", " + yr + " " + hrs + ":" + mnts + ampm;
     }
     
     /**
      * @author  Ryan
      * @return  the String representation of the month param
      */
     private static String getMonth(int month) throws IllegalArgumentException {
         switch (month) {
             case 1:
                 return "January";
             case 2:
                 return "February";
             case 3:
                 return "March";
             case 4:
                 return "April";
             case 5:
                 return "May";
             case 6:
                 return "June";
             case 7:
                 return "July";
             case 8:
                 return "August";
             case 9:
                 return "September";
             case 10:
                 return "October";
             case 11:
                 return "November";
             case 12:
                 return "December";
             default:
                 return "";
         }
     }
     
     /*
      * @author  Ryan
      * @return  formatted String representation for the day
      *
      * 16 becomes 16th, 23 becomes 23rd
      */
     public static String formatNumberWithExtension(int num) {
         int n = num;
         while (n > 10)
             n = n % 10;
         
         // (1,21,31) st, (2,22) nd, (3,23) rd, (4:20,24:30) th
         String append = "";
         if (n == 1)
             append = (num == 11 ? "th" : "st");
         else if (n == 2)
             append = (num == 12 ? "th" : "nd");
         else if (n == 3)
             append = (num == 13 ? "th" : "rd");
         else
             append = "th";
         
         return (num + append);
     }
     
     public static String formatDateLowPrecision(Date date) {
 
         if (date == null)
             return "";
         
         Date now = new Date();
         String output = "";
         SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMM");
         SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
         SimpleDateFormat dayFormat = new SimpleDateFormat("d");
         
         long diff = now.getTime() - date.getTime();
         
        if (diff < (1000*60*60*12)) {    // IF LESS THAN 12 HOURS AGO
             long seconds = diff / 1000;
             
             long hours = (seconds/(60*60));
             seconds   -= (hours*60*60);
             
             long minutes = (seconds/60);
             seconds     -= (minutes*60);
             
             if (hours > 1) {
                 output = hours + " hours ago";
             } else if (hours == 1) {
                 output = "1 hour ago";
             } else if (minutes > 1) {
                 output = minutes + " minutes ago";
             } else {
                 output = "1 minute ago";
             }
             
         } else {
         
             if (monthFormat.format(date).equals(monthFormat.format(now)) && yearFormat.format(date).equals(yearFormat.format(now))) {
                 diff = Integer.valueOf(dayFormat.format(now)) - Integer.valueOf(dayFormat.format(date));
                 if (diff == 0) return "Today";
                 if (diff == 1) return "Yesterday";
             }
 
             SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEEE, MMM d");
             return df.format(date);
         }
         
         return output;
     }
     
     public static String formatDateMinPrecision(Date date) {
 
         if (date == null)
             return "";
         
         Date now = new Date();
         
         long diff = now.getTime() - date.getTime();
         
        if (diff < (1000*60*60*24)) {    // IF LESS THAN 12 HOURS AGO
             
             return "Today";
             
        } else if (diff < (1000*60*60*48)) {
             
             return "Yesterday";
             
         } else {
         
             SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");
             return df.format(date);
         }
     }
     
     public static String asEllapsedDays(Date date) {
 
         if (date == null)
             return "";
 
         int days = ((Double)Math.floor((System.currentTimeMillis() - date.getTime()) / (1000 * 60 * 60 * 24))).intValue();
 
         if (days == 0) {
 
             return "Today";
 
         } else if (days == 1) {
 
             return "Yesterday";
 
         } else {
 
             return days + " days ago";
         }
     }
 
     public static String formatDateNoTime(Date date) {
         
         if (date == null)
             return "";
         
         Date now = new Date();
         String output = "";
         SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMM");
         SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
         SimpleDateFormat dayFormat = new SimpleDateFormat("d");
         
         if (monthFormat.format(date).equals(monthFormat.format(now)) && yearFormat.format(date).equals(yearFormat.format(now))) {
             int diff = Integer.valueOf(dayFormat.format(now)) - Integer.valueOf(dayFormat.format(date));
             if (diff == 0) return "Today";
             if (diff == 1) return "Yesterday";
         }
         
         SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEEE, MMM d, yyyy");
         return df.format(date);
     }
     
     public static String formatDateTimeOnly(Date date) {
         
         if (date == null)
             return "";
         
         long diff = System.currentTimeMillis() - date.getTime();
         String output = "";
         if (diff < (1000*60*60*6)) {    // IF LESS THAN 6 HOURS AGO
             long seconds = diff / 1000;
             
             long hours = (seconds/(60*60));
             seconds   -= (hours*60*60);
             
             long minutes = (seconds/60);
             seconds     -= (minutes*60);
             
             if (hours > 0) {
                 String min = ((minutes > 0) ? ", " + minutes + " minutes " : "");
                 output = hours + " hours " + min + "ago";
             } else if (minutes > 0) {
                 output = minutes + " minutes ago";
             } else {
                 output = seconds + " seconds ago";
             }
         } else {
             SimpleDateFormat df = new SimpleDateFormat("h:mm a");
             output = df.format(date);
         }
         return output;
     }
     
     public static String formatDate(Date date, String pattern, boolean relativeToNow) {
         
         if (date == null)
             return "";
         
         // Only knows how to handle relative dates in terms of hours, minutes, seconds ago
         if (relativeToNow) {
             long diff    = System.currentTimeMillis() - date.getTime();
             long seconds = diff / 1000;
             
             long hours = (seconds / (60 * 60));
             seconds   -= (hours * 60 * 60);
             
             long minutes = (seconds / 60);
             seconds     -= (minutes * 60);
             
             StringBuilder output = new StringBuilder();
             
             if (hours > 0) {
                 String min = ((minutes > 0) ? ", " + minutes + " minutes " : "");
                 output.append(hours + " hours " + min + "ago");
             } else if (minutes > 0) {
                 output.append(minutes + " minutes ago");
             } else {
                 output.append(seconds + " seconds ago");
             }
             
             return output.toString();
         }
         
         DateFormat df = new SimpleDateFormat(pattern);
         return df.format(date);
     }
     
     public static String formatDate(Date date) {
 
         if (date == null)
             return "";
 
         long diff = System.currentTimeMillis() - date.getTime();
         String output = "";
         if (diff < (1000*60*60*6)) {    // IF LESS THAN 6 HOURS AGO
             long seconds = diff / 1000;
             
             long hours = (seconds/(60*60));
             seconds   -= (hours*60*60);
             
             long minutes = (seconds/60);
             seconds     -= (minutes*60);
             
             if (hours > 0) {
                 String min = ((minutes > 0) ? ", " + minutes + " minutes " : "");
                 output = hours + " hours " + min + "ago";
             } else if (minutes > 0) {
                 output = minutes + " minutes ago";
             } else {
                 output = seconds + " seconds ago";
             }
         } else {
             try {
                 output = getTimestampInEnglish(new Timestamp(date.getTime()));
             } catch (IllegalArgumentException iae) {
                 output = date.toString();
             }
         }
         return output;
     }
 
     public static String asEllapsedTimeAgo(Double millis) {
         return asEllapsedTime(System.currentTimeMillis() - millis);
     }
 
     public static String asEllapsedTime(Double millis) {
 
         if (millis == null)
             return "";
 
         long avgM = millis.longValue();
         
         String output = new String();
         
         long seconds = avgM / 1000;
         
         long hours = (seconds/(60*60));
         seconds   -= (hours*60*60);
         
         long minutes = (seconds/60);
         seconds     -= (minutes*60);
         
         if (hours > 0) {
             String min = ((minutes > 0) ? ", " + minutes + " minutes " : "");
             output = hours + " hours " + min;
         } else if (minutes > 0) {
             output = minutes + " minutes";
         } else {
             output = seconds + " seconds";
         }
         
         return output;
     }
 
     public static long currentTimeMillis() {
         return System.currentTimeMillis();
     }
 }
