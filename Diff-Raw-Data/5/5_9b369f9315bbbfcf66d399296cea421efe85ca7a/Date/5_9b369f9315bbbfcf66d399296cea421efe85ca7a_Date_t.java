 package com.otpp.domain.date;
 
 import java.io.Serializable;
 import java.util.Calendar;
 
 /**
  * The intention of this class is to abstract away the implementation of Dates because let's face it Java's implementation of
  * dates and dates stuff is not optimal. This class is intended to be immutable (what need is there to mutate this?)
  * 
  * @author chanj
  */
 public class Date implements Comparable<Date>, Serializable {
 
     /**
      * A Constant to represent an unspecific date in the future. In smalltalk this is analogous to the infinite future.
      */
     public static final Date FUTURE = Date.createFutureDate();
 
     /**
      * A Constant to represent an unspecific date in the past. In smalltalk this is analogous to the infinite past.
      */
     public static final Date PAST = Date.createPastDate();
 
     public static final DayOfWeek MONDAY = DayOfWeek.MONDAY;
     public static final DayOfWeek TUESDAY = DayOfWeek.TUESDAY;
     public static final DayOfWeek WEDNESDAY = DayOfWeek.WEDNESDAY;
     public static final DayOfWeek THURSDAY = DayOfWeek.THURSDAY;
     public static final DayOfWeek FRIDAY = DayOfWeek.FRIDAY;
     public static final DayOfWeek SATURDAY = DayOfWeek.SATURDAY;
     public static final DayOfWeek SUNDAY = DayOfWeek.SUNDAY;
 
     public static final MonthOfYear JANUARY = MonthOfYear.JANUARY;
     public static final MonthOfYear FEBRUARY = MonthOfYear.FEBRUARY;
     public static final MonthOfYear MARCH = MonthOfYear.MARCH;
     public static final MonthOfYear APRIL = MonthOfYear.APRIL;
     public static final MonthOfYear MAY = MonthOfYear.MAY;
     public static final MonthOfYear JUNE = MonthOfYear.JUNE;
     public static final MonthOfYear JULY = MonthOfYear.JULY;
     public static final MonthOfYear AUGUST = MonthOfYear.AUGUST;
     public static final MonthOfYear SEPTEMBER = MonthOfYear.SEPTEMBER;
     public static final MonthOfYear OCTOBER = MonthOfYear.OCTOBER;
     public static final MonthOfYear NOVEMBER = MonthOfYear.NOVEMBER;
     public static final MonthOfYear DECEMBER = MonthOfYear.DECEMBER;
 
     private static enum DateType {
         FINITE_DATE, PAST_DATE, FUTURE_DATE;
 
         boolean isFuture() {
             return equals(FUTURE_DATE);
         }
 
         boolean isPast() {
             return equals(PAST_DATE);
         }
 
         boolean isFinite() {
             return equals(FINITE_DATE);
         }
     }
 
     private static final long serialVersionUID = 1L;
 
     /**
      * Wrapped date implementation class. It represents a specific date. To represent past and future dates, the year component
      * is set to either the minimum or maximum value, respectively.
      */
     private final Calendar date;
 
     /**
      * This is the type of date that we are working with.
      * This was created for special handling of past/future dates.
      */
     private DateType dateType = DateType.FINITE_DATE;
 
     private static ThreadLocal<Date> threadLocalToday = new ThreadLocal<Date>();
 
     /**
      * Constructor for instantiating a date from a calendar "date." The time component is stripped from the date.
      * 
      * @param aCalendar
      *            A Java calendar that represents the date you want to represent.
      */
     public Date(final Calendar aCalendar) {
         if (aCalendar == null) {
             throw new IllegalArgumentException("Cannot get date from null Calendar.");
         }
         // align to TimeZone of Calendar to interpret date, then drop all TimeZone info 
         Calendar aLocalDate = Calendar.getInstance();
         aLocalDate.set(aCalendar.get(Calendar.YEAR), aCalendar.get(Calendar.MONTH), aCalendar.get(Calendar.DAY_OF_MONTH));
         date = aLocalDate;
     }
 
     /**
      * Constructor for instantiating a date with separated integer components.
      * 
      * @param year
      *            the year as an integer
      * @param month
      *            the month as an integer [1 - 12]
      * @param dayOfMonth
      *            the day of the month as an integer [1 - 31]
      */
     public Date(final int year, final int month, final int dayOfMonth) {
         Calendar aLocalDate = Calendar.getInstance();
         aLocalDate.set(year, month-1, dayOfMonth);
         date = aLocalDate;
     }
 
     /**
      * Constructor for instantiating a date from an integer representation of YYYYMMDD, which we often get from the db.
      * 
      * @param dateNumber
      *            An integer in YYYYMMDD format.
      */
     public Date(final Integer dateNumber) {
         if (dateNumber == null) {
             throw new IllegalArgumentException("Cannot create date from null number.");
         }
         final String aDate = String.valueOf(dateNumber);
         Calendar aLocalDate = Calendar.getInstance();
         final int year = Integer.valueOf(aDate.substring(0, 4));
         final int month = Integer.valueOf(aDate.substring(4, 6));
         final int day = Integer.valueOf(aDate.substring(6, 8));
        aLocalDate.set(year, month-1, day);
         date = aLocalDate;
     }
 
     private Date(DateType aDateType) {
         if (aDateType.isFinite()) {
             throw new IllegalArgumentException("Cannot instantiate finite date with this constructor.");
         }
         else {
             dateType = aDateType;
         }
         date = null;
     }
 
     /**
      * A Constant to represent the current date.
      */
     public static Date today() {
         Date today = Date.threadLocalToday.get();
         return today == null ? Date.valueOf(Calendar.getInstance()) : today;
     }
 
     /**
      * This will override the value of "today" for the call {@link Date#today()}
      * for the execution of the current thread.
      * 
      * @param todaysDateOverride
      */
     public static void setToday(Date todaysDateOverride) {
         Date.threadLocalToday.set(todaysDateOverride);
     }
 
     /**
       * This method will check if the date object passed in is an actual calendar date (i.e. not a null, or future, or past date,
       * etc).
       * 
       * @param aDate
       * @return - true if date is actual date, false otherwise
       */
     public static boolean isFiniteDate(Date aDate) {
         return (aDate != null) && !aDate.isFutureDate() && !aDate.isPastDate();
     }
 
     /**
      * Returns a date from a calendar "date." The time component is stripped from the date.
      * 
      * @param aCalendar
      *            A Java calendar that represents the date you want to represent.
      */
     public static Date valueOf(Calendar aCalendar) {
         return new Date(aCalendar);
     }
 
     /**
      * Returns a date with separated integer components.
      * 
      * @param year
      *            the year as an integer
      * @param month
      *            the month as an integer [1 - 12]
      * @param dayOfMonth
      *            the day of the month as an integer [1 - 31]
      */
     public static Date valueOf(int year, int month, int dayOfMonth) {
         return new Date(year, month, dayOfMonth);
     }
 
     /**
      * Returns the day of the week of this date. Undefined if it is a past date or future date.
      * 
      * @return a {@link DayOfWeek} representing the day if it is a specific date. Undefined if it is a past date or future date.
      */
     public DayOfWeek getDayOfWeek() {
         int dayOfWeek = getDate().get(Calendar.DAY_OF_WEEK);
         switch (dayOfWeek) {
             case 1:
                 return DayOfWeek.MONDAY;
             case 2:
                 return DayOfWeek.TUESDAY;
             case 3:
                 return DayOfWeek.WEDNESDAY;
             case 4:
                 return DayOfWeek.THURSDAY;
             case 5:
                 return DayOfWeek.FRIDAY;
             case 6:
                 return DayOfWeek.SATURDAY;
             case 7:
                 return DayOfWeek.SUNDAY;
             default:
                 return null;
         }
     }
 
     /**
      * Returns the day of the month of this date. Undefined if it is a past date or future date.
      * 
      * @return an integer representing the day if it is a specific date. Undefined if it is a past date or future date.
      */
     public int getDayOfMonth() {
         return getDate().get(Calendar.DAY_OF_MONTH);
     }
 
     /**
      * Returns the index month of this date where January = 1 and December = 12. Undefined if it is a past date or future date.
      * 
      * @return an integer representing the month if it is a specific date. Undefined if it is a past date or future date.
      */
     public int getMonthIndex() {
        return getDate().get(Calendar.MONTH)+1;
     }
 
     /**
      * Returns the month of this date. Undefined if it is a past date or future date.
      * 
      * @return a {@link MonthOfYear} representing the month if it is a specific date. Undefined if it is a past date or future
      *         date.
      */
     public MonthOfYear getMonthOfYear() {
         if (isPastDate() || isFutureDate()) {
             return null;
         }
         int monthIndex = getDate().get(Calendar.MONTH);
         return MonthOfYear.getMonthOfYear(monthIndex);
     }
 
     /**
      * Returns the year of this date. Undefined if this date is a past date or future date.
      * 
      * @return an integer representing the year if it is a specific date. Undefined if this date is a past date or future date.
      */
     public int getYear() {
         return getDate().get(Calendar.YEAR);
     }
 
     /**
      * Returns true of this Date is after anotherDate. Undefined if both are past dates, or both are future dates.
      * 
      * @param anotherDate
      *            the other date to compare to.
      * @return a boolean, true if this date is strictly before the other date. Undefined if both are past dates or both are
      *         future dates.
      */
     public boolean isAfter(final Date anotherDate) {
         return compareTo(anotherDate) > 0;
     }
 
     /**
      * Returns true of this Date is before anotherDate. Undefined if 2 past dates or 2 future dates are compared.
      * 
      * @param anotherDate
      *            the other date to compare to.
      * @return a boolean, true if this date is strictly before the other date. Undefined if both dates are past dates or future
      *         dates.
      */
     public boolean isBefore(final Date anotherDate) {
         return compareTo(anotherDate) < 0;
     }
 
     /**
      * Returns true if this date is an unspecified future date.
      * 
      * @return True if it is a future date, false otherwise
      */
     public boolean isFutureDate() {
         return dateType.isFuture();
     }
 
     /**
      * Returns true of this Date is after anotherDate. Undefined if both are past dates, or both are future dates.
      * 
      * @param anotherDate
      *            the other date to compare to.
      * @return a boolean, true if this date is strictly before the other date. Undefined if both are past dates or both are
      *         future dates.
      */
     public boolean isOnOrAfter(final Date anotherDate) {
         return compareTo(anotherDate) >= 0;
     }
 
     /**
      * Returns true of this Date is on or before anotherDate. Undefined if 2 past dates or 2 future dates are compared.
      * 
      * @param anotherDate
      *            the other date to compare to.
      * @return a boolean, true if this date is strictly on or before the other date. Undefined if both dates are past dates or
      *         future dates.
      */
     public boolean isOnOrBefore(final Date anotherDate) {
         return compareTo(anotherDate) <= 0;
     }
 
     /**
      * Returns true if this date is an unspecified past date.
      * 
      * @return True if it is a past date, false if it is a specific date or a date in the future.
      */
     public boolean isPastDate() {
         return dateType.isPast();
     }
 
     public Date max(final Date anotherDate) {
         if (anotherDate == null) {
             return this;
         }
         if (isBefore(anotherDate)) {
             return anotherDate;
         }
         return this;
     }
 
     public Date min(final Date anotherDate) {
         if (anotherDate == null) {
             return this;
         }
         if (isAfter(anotherDate)) {
             return anotherDate;
         }
         return this;
     }
 
     /**
      * Returns this Date converted into a Calendar. The time component will be set to noon to
      * avoid any issues with time zone conversion that has been observed MANY TIMES
      * when machines use different locales (Toronto, New York, etc.) with different DST settings.
      * 
      * If this Date is a future or past date, the result is undefined.
      * 
      * @return a Calendar with the time components as close to midnight as possible
      */
     public Calendar toCalendar() {
         return date;
     }
 
     /**
      * Returns a negative integer if this date is before another date, 0 if equal and a positive if it is later. Comparing 2
      * unspecified dates of the same type is undefined.
      * 
      * @param anotherDate
      *            the other date to compare to.
      * @return a negative integer if this date is before the other, 0 if they are equal and positive if it is later than the
      *         other. Undefined if both dates are past dates, or future dates.
      */
     public int compareTo(final Date anotherDate) {
         /*
          * In the case where both dates are FINITE the comparison is simple
          */
         if (Date.isFiniteDate(this) && Date.isFiniteDate(anotherDate)) {
             return getDate().compareTo(anotherDate.getDate());
         }
         /*
          * In the case where this date is an infinite date
          */
         else if (isFutureDate() || isPastDate()) {
             if (isPastDate()) {
                 return anotherDate.isPastDate() ? 0 : -1;
             }
             else if (isFutureDate()) {
                 return anotherDate.isFutureDate() ? 0 : 1;
             }
         }
         /*
          * In the case where the input date is an infinite date
          */
         else if (anotherDate.isFutureDate() || anotherDate.isPastDate()) {
             return -1 * anotherDate.compareTo(this);
         }
         else {
             throw new IllegalStateException("Some odd state has been achieved in the " + this.getClass().getName() + " class");
         }
         return 0;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Date other = (Date) obj;
         if (date == null) {
             if (other.date != null) {
                 return false;
             }
         }
         else if (!date.equals(other.date)) {
             return false;
         }
         if (dateType == null) {
             if (other.dateType != null) {
                 return false;
             }
         }
         else if (!dateType.equals(other.dateType)) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = (prime * result) + ((date == null) ? 0 : date.hashCode());
         result = (prime * result) + ((dateType == null) ? 0 : dateType.hashCode());
         return result;
     }
 
     @Override
     public String toString() {
         if (isFutureDate()) {
             return "Future Date";
         }
         else if (isPastDate()) {
             return "Past Date";
         }
         else {
             return String.format("%4d-%02d-%02d", getYear(), getMonthIndex(), getDayOfMonth());
         }
     }
 
     /**
      * Constructor for instantiating a future date. It is an invariant date that is composed of the maximal values in each of
      * the fields for year, month and day.
      */
     private static Date createFutureDate() {
         return new Date(DateType.FUTURE_DATE);
     }
 
     /**
      * Constructor for instantiating a past date. It is an invariant date that is composed of the minimal values in each of the
      * fields for year, month and day.
      */
     private static Date createPastDate() {
         return new Date(DateType.PAST_DATE);
     }
 
     private Calendar getDate() {
         if (isFutureDate() || isPastDate()) {
             throw new IllegalStateException("You cannot perform this operation on a PAST/FUTURE date");
         }
         return date;
     }
 }
