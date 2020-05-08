 package de.skuzzle.polly.sdk.time;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
import com.sun.org.apache.xpath.internal.operations.Minus;

 
 
 /**
  * Provides useful static methods for working with dates. Its based on the
  * {@link GregorianCalendar} class of the java framework.
  * 
  * @author Simon
  * @since 0.9
  */
 public class DateUtils {
     
     /**
      * Creates a Calendar object for the given date in the given timezone.
      * 
      * @param d The date.
      * @param zone The timezone.
      * @return The new calendar object.
      */
     public static Calendar calendarForDate(Date d, TimeZone zone) {
         Calendar cal = new GregorianCalendar(zone);
         cal.setTime(d);
         return cal;
     }
     
     
     
     /**
      * Creates a Calendar object for the given date in the default timezone.
      * 
      * @param d The date.
      * @return The new calendar object.
      */
     public static Calendar calendarForDate(Date d) {
         return DateUtils.calendarForDate(d, TimeZone.getDefault());
     }
     
     
     
     /**
      * Creates a new date from a day, month and year part. All other calendar
      * fields are set to 0.
      * 
      * @param day The day of month.
      * @param month The month (0-11)
      * @param year The year.
      * @return A new date object which represents the given date.
      */
     public static Date dateFor(int day, int month, int year) {
         Calendar then = new GregorianCalendar(year, month, day);
         return then.getTime();
     }
     
     
     
     /**
      * Gets the year of the given date.
      * 
      * @param d The date.
      * @return The year of the date.
      */
     public static int getYear(Date d) {
         Calendar then = DateUtils.calendarForDate(d);
         return then.get(Calendar.YEAR);
     }
     
     
     
     /**
      * Gets the year of the current date.
      * 
      * @return The year.
      */
     public static int getYear() {
         return DateUtils.getYear(new Date());
     }
     
     
     
     /**
      * Gets the month of the given date.
      * 
      * @param d The date.
      * @return The month.
      */
     public static int getMonth(Date d) {
         Calendar then = DateUtils.calendarForDate(d);
         return then.get(Calendar.MONTH);
     }
     
     
     
     /**
      * Gets the month of the current date.
      * 
      * @return The month.
      */
     public static int getMonth() {
         return DateUtils.getMonth(new Date());
     }
     
     
     
     /**
      * Gets the day of the given date.
      * 
      * @param d The date.
      * @return The day.
      */
     public static int getDay(Date d) {
         Calendar then = DateUtils.calendarForDate(d);
         return then.get(Calendar.DAY_OF_MONTH);
     }
     
     
     
     /**
      * Gets the day of the current date.
      * 
      * @return The day.
      */
     public static int getDay() {
         return DateUtils.getDay(new Date());
     }
     
     
     
     /**
      * Creates a new date of object for today with the given time.
      * 
      * @param hours The hours (0-23)
      * @param minutes The minutes.
      * @param seconds The seconds.
      * @return A new date object.
      */
     public static Date timeFor(int hours, int minutes, int seconds) {
         Calendar then = new GregorianCalendar();
         then.set(Calendar.HOUR_OF_DAY, hours);
         then.set(Calendar.MINUTE, minutes);
         then.set(Calendar.SECOND, seconds);
         return then.getTime();
     }
     
     
     
     /**
      * Tests if the both given dates refer to the same day.
      * 
      * @param d1 The first date.
      * @param d2 The second date.
      * @return <code>true</code> if both dates refer to the same day.
      */
     public static boolean isSameDay(Date d1, Date d2) {
         boolean day = getDay(d1) == getDay(d2);
         boolean month = getMonth(d1) == getMonth(d2);
         boolean year = getYear(d1) == getYear(d2);
         
         return day && month && year;
     }
     
     
 
     /**
      * Tests whether the given date refers to today.
      * 
      * @param d The date.
      * @return <code>true</code> if the given date is today.
      */
     public static boolean isToday(Date d) {
         return isSameDay(new Date(), d);
     }
     
     
     public static void main(String[] args) {
         System.out.println(Calendar.MONDAY);
         System.out.println(Calendar.SUNDAY);
         System.out.println(Calendar.SATURDAY);
     }
     
     
     /**
      * Returns a date starting at 00:00 on the specified day of week. Valid inputs for 
      * this method are any of {@link Calendar#MONDAY}, ..., {@link Calendar#SUNDAY}. 
      * Values that are not within the given range will be converted using a modulo
      * calculation to make up a valid day number.
      * 
      * @param day The day of week to get the date for.
      * @return A new date instance starting at the specified day of week.
      * @since 0.9.1
      */
     public static Date getDayDate(int day) {
         day = ((day - 1) % 7) + 1;
         Calendar c = Calendar.getInstance();
         int today = c.get(Calendar.DAY_OF_WEEK);
         int diff = Math.abs(day - today);
         diff = diff == 0 ? 7 : diff;
 
         c.add(Calendar.DAY_OF_MONTH, diff);
        c.set(Calendar.HOUR_OF_DAY, 0);
         c.set(Calendar.MINUTE, 0);
         c.set(Calendar.SECOND, 0);
         
         return c.getTime();
     }
 }
