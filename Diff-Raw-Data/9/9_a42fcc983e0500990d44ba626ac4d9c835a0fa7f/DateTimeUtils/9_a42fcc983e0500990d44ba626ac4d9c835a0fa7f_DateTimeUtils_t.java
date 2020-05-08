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
 import java.util.Calendar;
 import java.util.Date;
 
 import org.j2free.util.Constants;
 
 /**
  *
  * @author Ryan Wilson
  */
 public class DateTimeUtils {
 
     private static final long SECONDS_PER_HOUR = 60 * 60;
     private static final long SECONDS_PER_DAY  = 60 * 60 * 24;
 
     private static final long MILLIS_PER_DAY   = 1000 * SECONDS_PER_DAY;
 
     public static int getCountdown(java.util.Date event) {
 
         if (event == null)
             return -1;
         
         return ((Double)Math.floor(((event.getTime() - System.currentTimeMillis()) / SECONDS_PER_DAY) / 1000)).intValue();
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
         int hours   = -1;
         
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
 
         if (date == null) return Constants.EMPTY;
 
         Calendar spec = Calendar.getInstance();
         spec.setTime(date);
 
         long diff = System.currentTimeMillis() - date.getTime();
         if (diff < 0) return Constants.EMPTY;
         
         if (diff < (MILLIS_PER_DAY / 2) && diff > 0) {    // IF LESS THAN 12 HOURS AGO
             
             long seconds = diff / 1000;
             
             long hours = (seconds / SECONDS_PER_HOUR);
             seconds   -= (hours * SECONDS_PER_HOUR);
             
             long minutes = (seconds / 60);
             seconds     -= (minutes * 60);
             
             if (hours > 1) {
                 return hours + " hours ago";
             } else if (hours == 1) {
                 return "1 hour ago";
             } else if (minutes > 1) {
                 return minutes + " minutes ago";
             } else {
                 return "1 minute ago";
             }
             
         } else {
 
             Calendar now  = Calendar.getInstance();
             diff = now.get(Calendar.DAY_OF_YEAR) - spec.get(Calendar.DAY_OF_YEAR);
            
             if (diff == 0)
                 return "Today";
             else if (diff == 1)
                 return "Yesterday";
 
             SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEEE, MMM d");
             return df.format(date);
         }
     }
     
     public static String formatDateMinPrecision(Date date) {
 
         if (date == null) return Constants.EMPTY;
         
         Calendar now  = Calendar.getInstance();
         Calendar spec = Calendar.getInstance();
         spec.setTime(date);
         
         long diff = now.get(Calendar.DAY_OF_YEAR) - spec.get(Calendar.DAY_OF_YEAR);
         if (diff == 0)
             return "Today";
         else if (diff == 1)
             return "Yesterday";
         
         SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");
         return df.format(date);
     }
     
     public static String asEllapsedDays(Date date) {
 
         if (date == null) return Constants.EMPTY;
 
         Calendar now  = Calendar.getInstance();
         Calendar spec = Calendar.getInstance();
         spec.setTime(date);
 
        long diff = ((now.get(Calendar.YEAR) - 1970)*365 + now.get(Calendar.DAY_OF_YEAR))
                    - ((spec.get(Calendar.YEAR) - 1970)*365 + spec.get(Calendar.DAY_OF_YEAR));
        
         if (diff == 0)
             return "Today";
         else if (diff == 1)
             return "Yesterday";
         else
             return String.format("%d days ago", diff);
     }
 
     public static String formatDateNoTime(Date date) {
         
         if (date == null) return Constants.EMPTY;
         
         Calendar now  = Calendar.getInstance();
         Calendar spec = Calendar.getInstance();
         spec.setTime(date);
 
        long diff = ((now.get(Calendar.YEAR) - 1970)*365 + now.get(Calendar.DAY_OF_YEAR))
                    - ((spec.get(Calendar.YEAR) - 1970)*365 + spec.get(Calendar.DAY_OF_YEAR));

         if (diff == 0)
             return "Today";
         else if (diff == 1)
             return "Yesterday";
 
         SimpleDateFormat df = new SimpleDateFormat("EEEEEEEEEE, MMM d, yyyy");
         return df.format(date);
     }
     
     public static String formatDateTimeOnly(Date date) {
         
         if (date == null) return Constants.EMPTY;
         
         long diff = System.currentTimeMillis() - date.getTime();
         
         String output = "";
         if (diff < (1000*60*60*6)) {    // IF LESS THAN 6 HOURS AGO
 
             long seconds = diff / 1000;
             
             long hours = (seconds / SECONDS_PER_HOUR);
             seconds   -= (hours * SECONDS_PER_HOUR);
             
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
             return Constants.EMPTY;
         
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
             return Constants.EMPTY;
 
         long diff = System.currentTimeMillis() - date.getTime();
         String output = "";
         if (diff < (1000*60*60*6)) {    // IF LESS THAN 6 HOURS AGO
             long seconds = diff / 1000;
             
             long hours = (seconds / SECONDS_PER_HOUR);
             seconds   -= (hours * SECONDS_PER_HOUR);
             
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
             return Constants.EMPTY;
 
         long avgM = millis.longValue();
         
         String output = "";
         
         long seconds = avgM / 1000;
 
         long days = (seconds / SECONDS_PER_DAY);
         seconds  -= (days * SECONDS_PER_DAY);
         
         long hours = (seconds / SECONDS_PER_HOUR);
         seconds   -= (hours * SECONDS_PER_HOUR);
         
         long minutes = (seconds/60);
         seconds     -= (minutes*60);
 
         if (days > 0) {
             output = days + " days";
         } else if (hours > 0) {
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
