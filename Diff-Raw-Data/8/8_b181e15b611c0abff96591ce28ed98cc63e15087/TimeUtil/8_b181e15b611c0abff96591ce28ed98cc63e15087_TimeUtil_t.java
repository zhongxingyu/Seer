 package fi.testbed2.util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class TimeUtil {
 
     /**
      * Converts the given timestamp in GMT format to local format.
      *
      * @param gmtTimestamp
      * @return
      */
     public static String getLocalTimestampFromGMTTimestamp(String gmtTimestamp) {
 
        if (gmtTimestamp==null || gmtTimestamp.length()==0) {
             return null;
         }
 
         try {
 
             // 201210281940 --> yyyyMMddHHmm
             SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
             simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
             Date date = simpleDateFormat.parse(gmtTimestamp);
 
             SimpleDateFormat sd = new SimpleDateFormat("HH:mm");
             sd.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
             return sd.format(date);
 
         } catch (ParseException e) {
             return null;
         }
 
     }
 
 }
