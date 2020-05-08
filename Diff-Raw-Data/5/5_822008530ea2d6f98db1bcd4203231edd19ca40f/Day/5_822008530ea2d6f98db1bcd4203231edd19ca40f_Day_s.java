 
 package pt.uac.cafeteria.model.domain;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * Represents a date, ignoring time.
  * <p>
  * Based on an instance of Calendar. Can be converted both ways (from and to)
  * <code>Calendar</code> and <code>Date</code> objects.
  */
public class Day {
 
     /** Default format when converting to a string. */
     private final String FORMAT = "yyyy-MM-dd";
 
     /** Calendar instance where <code>Day</code> is based off. */
     private final Calendar day;
 
     /** Creates a new Day instance, set for the current day. */
     public Day() {
         day = Calendar.getInstance();
     }
 
     /** Creates a new Day instance, from a <code>Calendar</code> object. */
     public Day(Calendar day) {
         this.day = day;
     }
 
     /** Creates a new Day instance, from a <code>Date</code> object. */
     public Day(Date date) {
         this();
         day.setTime(date);
     }
 
     /**
      * Creates a new Day instance for any given day of the year,
      * ignoring time. Based on the gregorian calendar.
      *
      * @param year the year value.
      * @param month the month value (1 = January)
      * @param dayOfMonth the day of the month.
      */
     public Day(int year, int month, int dayOfMonth) {
         day = new GregorianCalendar(year, month+1, dayOfMonth);
     }
 
     /** Gets the day as a <code>Calendar</code> object. */
     public Calendar getCalendar() {
         return this.day;
     }
 
     /** Gets the day as a <code>Date</code> object. */
     public Date getDate() {
         return this.day.getTime();
     }
 
     /**
      * Converts the <code>Day</code> to a string, according to a given format.
      * <p>
      * Format documentation is available on <code>SimpleDateFormat</code>.
      *
      * @see SimpleDateFormat
      *
      * @param format the format to convert the day to.
      * @return A string representing the day, as defined by <code>format</code>.
      */
     public String format(String format) {
         DateFormat df = new SimpleDateFormat(format);
         return df.format(getDate());
     }
 
     /** Checks if the day is today. */
     public boolean isToday() {
         return equals(new Day());
     }
 
     /**
      * Checks if another day is the same as this one.
      *
      * @param day the other day to compare.
      * @return true if the other day is the same, or false otherwise.
      */
     public boolean equals(Day day) {
         if (day == null) {
             return false;
         }
         Calendar test = day.getCalendar();
         return
             this.day.get(Calendar.ERA) == test.get(Calendar.ERA) &&
             this.day.get(Calendar.YEAR) == test.get(Calendar.YEAR) &&
             this.day.get(Calendar.DAY_OF_YEAR) == test.get(Calendar.DAY_OF_YEAR)
         ;
     }
 
     @Override
     public String toString() {
         return format(FORMAT);
     }
 }
