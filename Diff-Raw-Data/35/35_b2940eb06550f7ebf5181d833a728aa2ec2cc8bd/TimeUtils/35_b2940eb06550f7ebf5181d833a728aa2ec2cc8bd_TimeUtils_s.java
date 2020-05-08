 package com.music.fmv.utils;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * User: Vitalii Lebedynskyi
  * Date: 10/10/13
  * Time: 5:15 PM
  */
 public class TimeUtils {
     public static final SimpleDateFormat MM_SS_DATE_FORMAT = new SimpleDateFormat("mm:ss");
 
    public static CharSequence getNiceTimeForSongs(int duration) {
        Date d = new Date(duration * 1000);
         return MM_SS_DATE_FORMAT.format(d);
     }
 }
