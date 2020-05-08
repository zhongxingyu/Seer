 /*
  * Increments date 
  * Returns date String
  */
 package com.mgeiger.datadriver;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 
 /**
  * @class DateDataFixture
  */
 public class DateDataFixture {
 
     private static DateDataFixture INSTANCE;
     private int incrementMonth;
     private int incrementDay;
     private int incrementYear;
     private int incrementHour;
     private int incrementMinute;
 
     public static DateDataFixture getInstance() {
         if (INSTANCE == null) {
             INSTANCE = new DateDataFixture();
         }
 
         return INSTANCE;
     }
 
     public void setIncrementMonth(int incrementMonth) {
         this.incrementMonth = incrementMonth;
     }
 
     public void setIncrementDay(int incrementDay) {
         this.incrementDay = incrementDay;
     }
 
     public void setIncrementYear(int incrementYear) {
         this.incrementYear = incrementYear;
     }
 
     public void setIncrementHour(int incrementHour) {
         this.incrementHour = incrementHour;
     }
 
     public void setIncrementMinute(int incrementMinute) {
         this.incrementMinute = incrementMinute;
     }
 
     public int getIncrementMonth() {
         return incrementMonth;
     }
 
     public int getIncrementDay() {
         return incrementDay;
     }
 
     public int getIncrementYear() {
         return incrementYear;
     }
 
     public int getIncrementHour() {
         return incrementHour;
     }
 
     public int getIncrementMinute() {
         return incrementMinute;
     }
 
     public String showYear() {
         DateFormat dateFormat = new SimpleDateFormat("yyyy");
         Calendar cal = Calendar.getInstance(Locale.ENGLISH);
         cal.setTime(cal.getTime());
         cal.add(Calendar.YEAR, this.getIncrementYear());
         String format = dateFormat.format(cal.getTime());
 
         return format;
     }
 
     public String showMonth() {
         DateFormat dateFormat = new SimpleDateFormat("MM");
         Calendar cal = Calendar.getInstance(Locale.ENGLISH);
         cal.setTime(cal.getTime());
         cal.add(Calendar.MONTH, this.getIncrementMonth());
         String format = dateFormat.format(cal.getTime());
 
         return format;
     }
 
     public String showDay() {
         DateFormat dateFormat = new SimpleDateFormat("dd");
         Calendar cal = Calendar.getInstance(Locale.ENGLISH);
         cal.setTime(cal.getTime());
         cal.add(Calendar.DATE, this.getIncrementDay());
         String format = dateFormat.format(cal.getTime());
 
         return format;
     }
 
     public String minuteDivisibleByFive() {
         int minute = Calendar.getInstance().get(Calendar.MINUTE);
 
         if ((minute % 5) > 0) {
             minute = (minute / 5) * 5 + 5;
        }

        if (minute == 60) {
             minute = 0;
         }
 
         String min;
         if ((minute < 10)) {
             min = "0" + minute;
         } else {
             min = Integer.toString(minute, 10);
         }
 
         return min;
     }
 
     public String verifyIsDivisibleByFive() {
         return ((Integer.parseInt(this.minuteDivisibleByFive()) % 5) == 0) ? "PASS" : "FAIL";
     }
 
     public String incrementDate() {
         DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
         //get current date time with Calendar()
         Calendar cal = Calendar.getInstance(Locale.ENGLISH);
         cal.setTime(cal.getTime());
         cal.add(Calendar.DATE, this.getIncrementDay());
         String format = dateFormat.format(cal.getTime());
 
         return format;
     }
     
 }
