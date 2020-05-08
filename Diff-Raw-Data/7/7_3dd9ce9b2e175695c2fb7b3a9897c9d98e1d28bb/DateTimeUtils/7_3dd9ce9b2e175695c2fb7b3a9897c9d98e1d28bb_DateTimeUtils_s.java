 package topshelf.utils.common;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 public class DateTimeUtils {
 
 	public static Calendar getCalendar(Date date) {
 	    Calendar c = Calendar.getInstance();
 	    c.setTime(date);
 	    return c;
 	}
 	 
 	public static Calendar getCalendar() {
 	    Calendar c = Calendar.getInstance();
 	    c.setTime(new Date());
 	    return c;
 	}
 	
 	/**
 	 * Build Date from date/month/year. Zero-based for Month (0=January).
 	 * 
 	 * @return
 	 */
 	public static Date buildDate(int year, int month, int date) {
 	    Calendar calendar = getCalendar();
 	    calendar.set(year, month, date);
 	    return calendar.getTime();
 	}
 	 
 	/**
 	 * Build Datetime from date/month/year/hour/minute/second. Zero-based for
 	 * Month (0=January).
 	 * 
 	 * @param year
 	 * @param month
 	 * @param date
 	 * @param hour
 	 * @param minute
 	 * @param second
 	 * @return
 	 */
 	public static Date buildDateTime(int year, int month, int date, int hour, int minute, int second) {
 	    Calendar calendar = Calendar.getInstance();
 	    calendar.set(year, month, date, hour, minute, second);
 	    return calendar.getTime();
 	}
 	
 	/**
 	 * Delete time from Date.
 	 * 
 	 * @param date
 	 * @return
 	 */
 	public static Date removeTime(Date date) {      
 	    if (date == null) {
 	        return null;
 	    }
 	 
 	    // Obtain an instance of the Calendar.
 	    Calendar calendar = getCalendar(date);
 	 
 	    // Mark no automatic correction
 	    calendar.setLenient(false);
 	 
 	    // Remove the hours, minutes, seconds and milliseconds.
 	    calendar.set(Calendar.HOUR_OF_DAY, 0);
 	    calendar.set(Calendar.MINUTE, 0);
 	    calendar.set(Calendar.SECOND, 0);
 	    calendar.set(Calendar.MILLISECOND, 0);
 	 
 	    // Return the date again.
 	    return calendar.getTime();
 	}
 	
 	/**
      * Adjust date, eg to reduce 10 days from date: adjustDate(date,
      * Calendar.DAY_OF_YEAR, -10);
      * 
      * @param date
      * @param type
      * @param amount
      * @return
      */
     public static Date adjustDate(Date date, int type, int amount) {
         Calendar calendar = getCalendar(date);
         calendar.add(type, amount);
         return calendar.getTime();
     }
  
     /**
      * Get the date before, eg: yesterday
      * 
      * @param date
      * @return
      */
     public static Date getTheDateBefore(Date date) {
         return adjustDate(date, Calendar.DAY_OF_YEAR, -1);
     }
  
     /**
      * Get the date after, eg: tomorrow
      * 
      * @param date
      * @return
      */
     public static Date getTheDateAfter(Date date) {
         return adjustDate(date, Calendar.DAY_OF_YEAR, -1);
     }
     /**
      * Get time different in millisecond.
      * 
      * @param date1
      * @param date2
      * @return
      */
     public static long getTimeDiffInMillis(Date date1, Date date2) {
         Calendar gc1 = getCalendar(date1);
         Calendar gc2 = getCalendar(date2);
  
         return gc2.getTimeInMillis() - gc1.getTimeInMillis();
     }
  
     /**
      * Get Day Different between 2 days.
      * 
      * @param date1
      * @param date2
      * @return
      */
     public static int getDaysDiff(Date date1, Date date2) {
         Calendar gc1 = getCalendar();
         Calendar gc2 = getCalendar();
  
         if (date1.before(date2)) {
             gc1.setTime(date1);
             gc2.setTime(date2);
         }
  
         else {
             gc1.setTime(date2);
             gc2.setTime(date1);
         }
  
         gc1.clear(Calendar.MILLISECOND);
         gc1.clear(Calendar.SECOND);
         gc1.clear(Calendar.MINUTE);
         gc1.clear(Calendar.HOUR_OF_DAY);
  
         gc2.clear(Calendar.MILLISECOND);
         gc2.clear(Calendar.SECOND);
         gc2.clear(Calendar.MINUTE);
         gc2.clear(Calendar.HOUR_OF_DAY);
  
         int daysDiff = 0;
         while (gc1.before(gc2)) {
             gc1.add(Calendar.DATE, 1);
             daysDiff++;
         }
         return daysDiff;
     }
     
     /**
      * Compare date and ignore time
      * 
      * @param firstDate
      * @param secondDate
      * @return
      */
     public static int compareDateOnly(Date firstDate, Date secondDate) {
  
         if (firstDate == null || secondDate == null)
             throw new IllegalArgumentException("The arguments cannot be null");
  
         Calendar firstCalendar = Calendar.getInstance();
         firstCalendar.setTime(firstDate);
  
         Calendar secondCalendar = Calendar.getInstance();
         secondCalendar.setTime(secondDate);
  
         int year = secondCalendar.get(Calendar.YEAR);
         int month = secondCalendar.get(Calendar.MONTH);
         int date = secondCalendar.get(Calendar.DATE);
  
         secondCalendar.setTime(firstDate);
         secondCalendar.set(Calendar.YEAR, year);
         secondCalendar.set(Calendar.MONTH, month);
         secondCalendar.set(Calendar.DATE, date);
  
         long oneDayInMillis = 24 * 60 * 60 * 1000L;
         return (int) ((firstCalendar.getTimeInMillis() - secondCalendar.getTimeInMillis()) / oneDayInMillis);
     }
     
     /**
      * Compare time and ignore date.
      * 
      * @param firstTime
      * @param secondTime
      * @return
      */
     public static int compareTimeOnly(Date firstTime, Date secondTime) {
  
         if (firstTime == null || secondTime == null)
             throw new IllegalArgumentException("The arguments cannot be null");
  
         Calendar firstCalendar = Calendar.getInstance();
         firstCalendar.setTime(firstTime);
  
         Calendar secondCalendar = Calendar.getInstance();
         secondCalendar.setTime(secondTime);
  
         secondCalendar.set(Calendar.YEAR, firstCalendar.get(Calendar.YEAR));
         secondCalendar.set(Calendar.MONTH, firstCalendar.get(Calendar.MONTH));
         secondCalendar.set(Calendar.DATE, firstCalendar.get(Calendar.DATE));
  
         return (int) ((firstCalendar.getTimeInMillis() - secondCalendar.getTimeInMillis()) / 1000);
     }
     
     /**
      * Check if date is in range of dateFrom and dateTo.
      * 
      * @param date
      * @param dateFrom
      * @param dateTo
      * @return
      */
     public static boolean isDateInRange(Date date, Date dateFrom, Date dateTo) {
         return (date != null && dateFrom != null && (date.equals(dateFrom) || date.equals(dateTo) || (date
                 .after(dateFrom) && (dateTo == null || date.before(dateTo)))));
     }
  
     /**
      * Check if time is in range of timeFrom and timeTo, ignore date.
      * 
      * @param time
      * @param timeFrom
      * @param timeTo
      * @return
      */
     public static boolean isTimeInRange(Date time, Date timeFrom, Date timeTo) {
         return time != null && timeFrom != null && timeTo != null
                 && DateTimeUtils.compareTimeOnly(timeFrom, time) <= 0
                 && DateTimeUtils.compareTimeOnly(time, timeTo) <= 0;
     }
  
     /**
      * Check if given year is leap year
      * 
      * @param year
      * @return
      */
     public static boolean isLeapYear(int year) {
         return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
     }
     
     /**
      * Get the beginning moment of the given date.
      * 
      * @param date
      * @return
      */
     public static Date getLowerBound(Date date) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(date);
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         return calendar.getTime();
     }
 
     /**
      * Get the ending moment of the given date.
      * 
      * @param date
      * @return
      */
     public static Date getUpperBound(Date date) {
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(date);
         calendar.set(Calendar.HOUR_OF_DAY, 23);
         calendar.set(Calendar.MINUTE, 59);
         calendar.set(Calendar.SECOND, 59);
         calendar.set(Calendar.MILLISECOND, 999);
         return calendar.getTime();
     }
     
 	public static Date adjustRelativeDateToUtcDate(Date relDate, TimeZone relTimeZone) {
		if (null == relDate) return null;
		int farmOffsetMs = relTimeZone.getRawOffset();
 		Calendar relCal = Calendar.getInstance();
 		relCal.setTime(relDate);
		relCal.add(Calendar.MILLISECOND, (-1*farmOffsetMs));
 		return relCal.getTime();
 	}
 
 }
