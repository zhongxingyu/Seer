 /*
  */
 package de.unibamberg.itfs.univis.util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  *
  * @author gtudan
  */
 public class DateParser {
 
     private final static SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm");
     private final static SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyy-MM-dd");
 
     public static String dateToString(Date date) {
        if (date != null) {
             return SDF_DATE.format(date);
         } else {
             return "";
         }
     }
 
     public static Date stringToDate(String string) throws ParseException {
         return SDF_DATE.parse(string);
     }
 
     public static String timeToString(Date time) {
         if (time != null) {
             return SDF_TIME.format(time);
         } else {
             return "";
         }
     }
 
     public static Date stringToTime(String string) throws ParseException {
         return SDF_TIME.parse(string);
     }
 }
