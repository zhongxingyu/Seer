 package com.it_tudes.util.date;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.apache.commons.lang3.time.DateUtils;
 
 public class DateUtil {
 	private final BigDecimal MILLISECONDS_PER_DAY = new BigDecimal(24 * 60 * 60 * 1000);
 	private static final DateUtil instance = new DateUtil();
 
 	private DateUtil() {
 	}
 
 	public static DateUtil getInstance() {
 		return instance;
 	}
 
 	public Date getToday() {
 		return new Date();
 	}
 
 	public Date getTomorrow() {
 		return DateUtils.addDays(getToday(), 1);
 	}
 
 	public Date getYesterday() {
 		return DateUtils.addDays(getToday(), -1);
 	}
 
 	public Date getNextWeek() {
 		return DateUtils.addDays(getToday(), 7);
 	}
 
 	public Date getPreviousWeek() {
 		return DateUtils.addDays(getToday(), -7);
 	}
 
 	public boolean isCurrentWeek(Date date) {
 		return isSameUnit(date, Calendar.WEEK_OF_YEAR);
 	}
 
 	public boolean isCurrentMonth(Date date) {
 		return isSameUnit(date, Calendar.MONTH);
 	}
 
 	public boolean isCurrentYear(Date date) {
 		return isSameUnit(date, Calendar.YEAR);
 	}
 
 	public boolean isAfterNDays(Date date, int n) {
 		return isAfter(date, Calendar.DAY_OF_YEAR, n);
 	}
 
 	public boolean isAfterNWeeks(Date date, int n) {
 		return isAfter(date, Calendar.WEEK_OF_YEAR, n);
 	}
 
 	public boolean isAfterNMonths(Date date, int n) {
 		return isAfter(date, Calendar.MONTH, n);
 	}
 
 	public boolean isAfterNYears(Date date, int n) {
 		return isAfter(date, Calendar.YEAR, n);
 	}
 
 	public long dayDifferences(Date dateOne, Date dateTwo) {
 		Calendar calendarOne = GregorianCalendar.getInstance();
 		Calendar calendarTwo = GregorianCalendar.getInstance();
 		calendarOne.setTime(dateOne);
 		calendarTwo.setTime(dateTwo);
 
 		long milliseconds1 = calendarOne.getTimeInMillis();
 		long milliseconds2 = calendarTwo.getTimeInMillis();
 		long diffMilliseconds = milliseconds2 - milliseconds1;
 		BigDecimal millisecondsDiff = new BigDecimal(diffMilliseconds);
 
		long diffDays = millisecondsDiff.divide(MILLISECONDS_PER_DAY, BigDecimal.ROUND_HALF_UP).longValue();
 		return diffDays;
 	}
 
 	private boolean isSameUnit(Date date, int calendarUnit) {
 		Calendar inputCalendar = GregorianCalendar.getInstance();
 		Calendar currentCalendar = GregorianCalendar.getInstance();
 		inputCalendar.setTime(date);
 		return inputCalendar.get(calendarUnit) == currentCalendar.get(calendarUnit);
 	}
 
 	private boolean isAfter(Date date, int calendarUnit, int howMany) {
 		Calendar inputCalendar = GregorianCalendar.getInstance();
 		Calendar currentCalendar = GregorianCalendar.getInstance();
 		inputCalendar.setTime(date);
 		currentCalendar.add(calendarUnit, howMany);
 		return inputCalendar.after(currentCalendar);
 	}
 
 }
