 package com.flaptor.util;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 
 /**
  * Utility class for managing dates 
  * @author Martin Massera
  *
  */
 public class DateUtil {
     
     public static final long MILLIS_IN_SECOND = 1000;
     public static final long MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
     public static final long MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;
     public static final long MILLIS_IN_DAY = MILLIS_IN_HOUR * 24;
     public static final long MILLIS_IN_WEEK = MILLIS_IN_DAY * 7;
 
     public static Calendar toCalendar(Date date) {
         Calendar c = new GregorianCalendar();
         c.setTime(date);
         return c;
     }
     
     public static Date toDate(Calendar calendar) {
         return calendar.getTime();
     }
     
     /**
      * counts how many days passed between two dates, counting days and not "24hs":
      * from 2008/1/1 13:00 to 2008/1/2 11:00 there is 1 day (even if it is only a 22 hour difference)   
      * 
      * @param from
      * @param to
      * @return 
      */
     public static int howManyDaysPassed(Calendar from, Calendar to) {
         long diff = getCanonicalDay(to).getTimeInMillis()- getCanonicalDay(from).getTimeInMillis();
        return (int)(diff / MILLIS_IN_DAY);
     }
     
     /**
      * Compares two calendars to determine if they are the same day 
      * @param cal1
      * @param cal2
      * @return 
      */
     public static boolean isSameDay(Calendar cal1, Calendar cal2) {
         return 
             cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
             cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);        
     }
 
     /**
      * Returns the canonical day calendar for a given calendar, which is the first millisecond of 
      * the day (2008/03/07 15:23:32 992ms --> 2008/03/07 0:0:0 0ms)
      * @param cal
      * @return 
      */
     public static Calendar getCanonicalDay(Calendar cal) {
         Calendar ret = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0 ,0 ,0);
         ret.set(Calendar.MILLISECOND, 0);
         return ret;
     }
     
     /**
      * Returns the last millisecond of the given day  
      * the day (2008/03/07 15:23:32 992ms --> 2008/03/07 23:59:59 999ms)
      * @param cal
      * @return 
      */
     public static Calendar getLastMilli(Calendar cal) {
         Calendar ret = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
         ret.set(Calendar.MILLISECOND, 999);
         return ret;
     }
     
     /**
      * Returns the canonical day of the day specified in days from now
      * @param daysFromToday today = 0, tomorrow = +1, yesterday = -1, same day next week = +7, etc
      * @return
      */
     public static Calendar getCanonicalDayFromToday(int daysFromToday) {
         Calendar day = new GregorianCalendar();
         day.add(Calendar.DAY_OF_YEAR, daysFromToday);
         return getCanonicalDay(day); 
     }
 
     /**
      * Returns the canonical day of the day specified in days from another date
      * @param daysFromToday today = 0, tomorrow = +1, yesterday = -1, same day next week = +7, etc
      * @return
      */
     public static Calendar getCanonicalDayFrom(Calendar day, int daysFrom) {
         Calendar ret = getCanonicalDay(day);
         ret.add(Calendar.DAY_OF_YEAR, daysFrom);
         return getCanonicalDay(ret); 
     }
 
     /**
      * returns an interable of canonical days in (inclusive) range of days
      * @param from
      * @param to
      * @return
      */
     public static Iterable<Calendar> dayIterable(Calendar from, Calendar to) {
         final Calendar nextDay = getCanonicalDay(from);
         final Calendar upto = getCanonicalDay(to);
         return new Iterable<Calendar>() {
             public Iterator<Calendar> iterator() {
                 return new Iterator<Calendar>() {
                     public boolean hasNext() {
                         return !nextDay.after(upto);
                     }
                     public Calendar next() {
                         Calendar ret = getCanonicalDay(nextDay);
                         nextDay.add(Calendar.DAY_OF_YEAR, 1);
                         return ret;
                     }
                     public void remove() {
                         //do nothing
                     }
                 };
             }
         };
     }
     
     public static Date toDate(long millis) {
         return new Date(millis);
     }
 
     public static Calendar toCalendar(long millis) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(new Date(millis));
         return cal;
     }
 }
