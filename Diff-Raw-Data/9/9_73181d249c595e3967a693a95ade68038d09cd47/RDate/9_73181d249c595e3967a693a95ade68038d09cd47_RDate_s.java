 package com.raawlls.date;
 
 import java.util.Date;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /**
  * User: reillyforshaw
  * RDate: 13-03-06
  * Time: 11:34 PM
  */
 
 public class RDate extends Date {
 
     public RDate() {
         super();
     }
 
     /**
      * Build a com.raawlls.date.RDate from a java.util.Date.
      *
      * @param date
      */
     public RDate(Date date) {
         super(date.getTime());
     }
 
     /**
      * @param date
      * @param field
      * @param amount
     * @return this RDate
      */
     private static Date _add(Date date, int field, int amount) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(date);
         cal.add(field, amount);
 
         return cal.getTime();
     }
 
     private RDate _add(int field, int amount) {
         setTime(_add(this, field, amount).getTime());
         return this;
     }
 
     // Convenience methods for adding to each field
 
     public static Date addMilliseconds(Date date, int milliseconds) {
         return _add(date, Calendar.MILLISECOND, milliseconds);
     }
 
     public RDate addMilliseconds(int milliseconds) {
         setTime(addMilliseconds(this, milliseconds).getTime());
         return this;
     }
 
     public static Date addSeconds(Date date, int seconds) {
         return _add(date, Calendar.SECOND, seconds);
     }
 
     public RDate addSeconds(int seconds) {
         setTime(addSeconds(this, seconds).getTime());
         return this;
     }
 
     public static Date addMinutes(Date date, int minutes) {
         return _add(date, Calendar.MINUTE, minutes);
     }
 
     public RDate addMinutes(int minutes) {
         setTime(addMinutes(this, minutes).getTime());
         return this;
     }
 
     public static Date addHours(Date date, int hours) {
         return _add(date, Calendar.HOUR_OF_DAY, hours);
     }
 
     public RDate addHours(int hours) {
         setTime(addHours(this, hours).getTime());
         return this;
     }
 
     public static Date addDays(Date date, int days) {
         return _add(date, Calendar.DATE, days);
     }
 
     public RDate addDays(int days) {
         setTime(addDays(this, days).getTime());
         return this;
     }
 
     public static Date addMonths(Date date, int months) {
         return _add(date, Calendar.MONTH, months);
     }
 
     public RDate addMonths(int months) {
         setTime(addMonths(this, months).getTime());
         return this;
     }
 
     public static Date addYears(Date date, int years) {
         return _add(date, Calendar.YEAR, years);
     }
 
     public RDate addYears(int years) {
         setTime(addYears(this, years).getTime());
         return this;
     }
 
    // Convenience method to set this date to midnight
     public static Date midnight(Date date) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(date);
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         return cal.getTime();
     }
 
     public RDate midnight() {
         setTime(midnight(this).getTime());
         return this;
     }
 
    // Convenience method to set this date to noon
     public static Date noon(Date date) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(date);
         cal.set(Calendar.HOUR_OF_DAY, 12);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         return cal.getTime();
     }
 
     public RDate noon() {
         setTime(noon(this).getTime());
         return this;
     }
 
     public static Date dayStart(Date date) {
         return midnight(date);
     }
 
     public RDate dayStart() {
         setTime(dayStart(this).getTime());
         return this;
     }
 
     public static Date weekStart(Date date) {
         return midnight(addDays(date, Calendar.SUNDAY - dayOfWeek(date)));
     }
     public RDate weekStart() {
         setTime(weekStart(this).getTime());
         return this;
     }
 
     public static Date monthStart(Date date) {
         return midnight(addDays(date, 1 - date(date)));
     }
     public RDate monthStart() {
         setTime(monthStart(this).getTime());
         return this;
     }
 
     public static Date yearStart(Date date) {
         return monthStart(addMonths(date, Calendar.JANUARY - month(date)));
     }
 
     public RDate yearStart() {
         setTime(yearStart(this).getTime());
         return this;
     }
 
    // Convenience methods for checking common attributes of the date

     public static boolean isAm(Date date) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(date);
         return Calendar.AM == cal.get(Calendar.AM_PM);
     }
 
     public boolean isAm() {
         return isAm(this);
     }
 
     public static boolean isPm(Date date) {
         return !isAm(date);
     }
 
     public boolean isPm() {
         return !isAm();
     }
 
     public static boolean onSameDate(Date d1, Date d2) {
         if ( !inSameYear(d1, d2) )
             return false;
         else if ( !inSameMonth(d1, d2) )
             return false;
         else
             return date(d1) == date(d2);
     }
 
     public boolean onSameDate(Date date) {
         return onSameDate(this, date);
     }
 
     public static boolean inSameWeek(Date d1, Date d2) {
         return weekStart(d1).equals(weekStart(d2));
     }
 
     public boolean inSameWeek(Date date) {
         return inSameWeek(this, date);
     }
 
     public static boolean inSameMonth(Date d1, Date d2) {
         if ( !inSameYear(d1, d2) )
             return false;
         else
             return month(d1) == month(d2);
     }
 
     public boolean inSameMonth(Date date) {
         return inSameMonth(this, date);
     }
 
     public static boolean inSameYear(Date d1, Date d2) {
         return year(d1) == year(d2);
     }
 
     public boolean inSameYear(Date date) {
         return inSameYear(this, date);
     }
 
     // Days
 
     public static int dayOfWeek(Date date) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(date);
         return cal.get(Calendar.DAY_OF_WEEK);
     }
 
     public int dayOfWeek() {
         return dayOfWeek(this);
     }
 
     public static boolean isSunday(Date date) {
         return Calendar.SUNDAY == dayOfWeek(date);
     }
 
     public boolean isSunday() {
         return isSunday(this);
     }
 
     public static boolean isMonday(Date date) {
         return Calendar.MONDAY == dayOfWeek(date);
     }
 
     public boolean isMonday() {
         return isMonday(this);
     }
 
     public static boolean isTuesday(Date date) {
         return Calendar.TUESDAY == dayOfWeek(date);
     }
 
     public boolean isTuesday() {
         return isTuesday(this);
     }
 
     public static boolean isWednesday(Date date) {
         return Calendar.WEDNESDAY == dayOfWeek(date);
     }
 
     public boolean isWednesday() {
         return isWednesday(this);
     }
 
     public static boolean isThursday(Date date) {
         return Calendar.THURSDAY == dayOfWeek(date);
     }
 
     public boolean isThursday() {
         return isThursday(this);
     }
 
     public static boolean isFriday(Date date) {
         return Calendar.FRIDAY == dayOfWeek(date);
     }
 
     public boolean isFriday() {
         return isFriday(this);
     }
 
     public static boolean isSaturday(Date date) {
         return Calendar.SATURDAY == dayOfWeek(date);
     }
 
     public boolean isSaturday() {
         return isSaturday(this);
     }
 
     public static boolean isWeekDay(Date date) {
         return !isWeekendDay(date);
     }
 
     public boolean isWeekDay() {
         return isWeekDay(this);
     }
 
     public static boolean isWeekendDay(Date date) {
         return isSunday(date) || isSaturday(date);
     }
 
     public boolean isWeekendDay() {
         return isWeekendDay(this);
     }
 
     // Months
 
     public static boolean isInJanuary(Date date) {
         return Calendar.JANUARY == month(date);
     }
 
     public boolean isInJanuary() {
         return isInJanuary(this);
     }
 
     public static boolean isInFebruary(Date date) {
         return Calendar.FEBRUARY == month(date);
     }
 
     public boolean isInFebruary() {
         return isInFebruary(this);
     }
 
     public static boolean isInMarch(Date date) {
         return Calendar.MARCH == month(date);
     }
 
     public boolean isInMarch() {
         return isInMarch(this);
     }
 
     public static boolean isInApril(Date date) {
         return Calendar.APRIL == month(date);
     }
 
     public boolean isInApril() {
         return isInApril(this);
     }
 
     public static boolean isInMay(Date date) {
         return Calendar.MAY == month(date);
     }
 
     public boolean isInMay() {
         return isInMay(this);
     }
 
     public static boolean isInJune(Date date) {
         return Calendar.JUNE == month(date);
     }
 
     public boolean isInJune() {
         return isInJune(this);
     }
 
     public static boolean isInJuly(Date date) {
         return Calendar.JULY == month(date);
     }
 
     public boolean isInJuly() {
         return isInJuly(this);
     }
 
     public static boolean isInAugust(Date date) {
         return Calendar.AUGUST == month(date);
     }
 
     public boolean isInAugust() {
         return isInAugust(this);
     }
 
     public static boolean isInSeptember(Date date) {
         return Calendar.SEPTEMBER == month(date);
     }
 
     public boolean isInSeptember() {
         return isInSeptember(this);
     }
 
     public static boolean isInOctober(Date date) {
         return Calendar.OCTOBER == month(date);
     }
 
     public boolean isInOctober() {
         return isInOctober(this);
     }
 
     public static boolean isInNovember(Date date) {
         return Calendar.NOVEMBER == month(date);
     }
 
     public boolean isInNovember() {
         return isInNovember(this);
     }
 
     public static boolean isInDecember(Date date) {
         return Calendar.DECEMBER == month(date);
     }
 
     public boolean isInDecember() {
         return isInDecember(this);
     }
 
     public static boolean isInLeapYear(Date date) {
         int y = year(date);
 
         if ( 0 == y % 400 )
             return true;
         else if ( 0 == y % 100 )
             return false;
 
         return 0 == y % 4;
     }
 
     public boolean isInLeapYear() {
         return isInLeapYear(this);
     }
 
     public static boolean isBetween(Date d1, Date d2, Date date) {
         return !date.before(d1) && !date.after(d2);
     }
 
     public boolean isBetween(Date d1, Date d2) {
         return isBetween(d1, d2, this);
     }
 
     // Getters
 
     public static int _get(Date date, int field) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(date);
         return cal.get(field);
     }
 
     public static int year(Date date) {
         return _get(date, Calendar.YEAR);
     }
 
     public int year() {
         return year(this);
     }
 
     public static int month(Date date) {
         return _get(date, Calendar.MONTH);
     }
 
     public int month() {
         return month(this);
     }
 
     public static int date(Date date) {
         return _get(date, Calendar.DATE);
     }
 
     public int date() {
         return date(this);
     }
 
     public static int hour(Date date) {
         return _get(date, Calendar.HOUR_OF_DAY);
     }
 
     public int hour() {
         return hour(this);
     }
 
     public static int minutes(Date date) {
         return _get(date, Calendar.MINUTE);
     }
 
     public int minute() {
         return minutes(this);
     }
 
     public static int seconds(Date date) {
         return _get(date, Calendar.SECOND);
     }
 
     public int second() {
         return seconds(this);
     }
 
     public static int milliseconds(Date date) {
         return _get(date, Calendar.MILLISECOND);
     }
 
     public int millisecond() {
         return milliseconds(this);
     }
 
     // Setters
 
     private static Date _set(Date date, int field, int amount) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(date);
         cal.set(field, amount);
         return cal.getTime();
     }
 
     public static Date setYear(Date date, int year) {
          return _set(date, Calendar.YEAR, year);
     }
 
     public RDate year(int year) {
         setTime(setYear(this, year).getTime());
         return this;
     }
 
     public static Date setMonth(Date date, int month) {
         return _set(date, Calendar.MONTH, month);
     }
 
     public RDate month(int month) {
         setTime(setMonth(this, month).getTime());
         return this;
     }
 
     public static Date setDate(Date date, int d) {
         return _set(date, Calendar.DATE, d);
     }
 
     public RDate date(int date) {
         setTime(setDate(this, date).getTime());
         return this;
     }
 
     public static Date setHour(Date date, int hour) {
         return _set(date, Calendar.HOUR_OF_DAY, hour);
     }
 
     public RDate hour(int hour) {
         setTime(setHour(this, hour).getTime());
         return this;
     }
 
     public static Date setMinute(Date date, int minute) {
         return _set(date, Calendar.MINUTE, minute);
     }
 
     public RDate minutes(int minute) {
         setTime(setHour(this, minute).getTime());
         return this;
     }
 
     public static Date setSecond(Date date, int second) {
         return _set(date, Calendar.SECOND, second);
     }
 
     public RDate seconds(int second) {
         setTime(setSecond(this, second).getTime());
         return this;
     }
 
     public static Date setMillisecond(Date date, int millisecond) {
         return _set(date, Calendar.MILLISECOND, millisecond);
     }
 
     public RDate milliseconds(int millisecond) {
         setTime(setMillisecond(this, millisecond).getTime());
         return this;
     }
 }
