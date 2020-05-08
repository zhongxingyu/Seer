 package com.scurab.java.ftpleechergui;
 
 import java.text.DecimalFormat;
 
 public class TextUtils {
 
     private static DecimalFormat NUMBER_FORMAT = new DecimalFormat();
 
     static {
         NUMBER_FORMAT.setGroupingSize(3);
         NUMBER_FORMAT.setGroupingUsed(true);
     }
 
     public static boolean isNullOrEmpty(String v) {
         return v == null || v.length() == 0;
     }
 
     /**
      * <code>isNullOrEmpty(v) ? ifnull : v;</code>
      *
      * @param v
      * @param ifnull
      * @return
      */
     public static String nz(String v, String ifnull) {
         return isNullOrEmpty(v) ? ifnull : v;
     }
 
     /**
      * Returns more readable number where is space like thousands separator
      *
      * @param value
      * @return
      */
    public static String getNumberReadable(long value) {
         return NUMBER_FORMAT.format(value);
     }
 
     /**
      * Get speed like more human readable value, MiB/s, KiB/s or B/s
      *
      * @param value
      * @return
      */
     public static String getSpeedReadable(int value) {
         if (value > 1000000) {
             return String.format("%.2f MiB/s", (value / 1000000f));
         } else if (value > 1000) {
             return String.format("%.0f KiB/s", (value / 1000f));
         } else {
             return value + " B/s";
         }
     }
 }
