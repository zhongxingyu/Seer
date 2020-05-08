 package de.javandry.testutils;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 
import static org.junit.Assert.*;
 
 public class DateBuilderTests {
 
     private Calendar calendar;
 
     @Before
     public void setUp() throws Exception {
         calendar = GregorianCalendar.getInstance();
     }
 
     @Test
     public void testToday() {
         assertEqualsCalendar(calendar, DateBuilder.today());
     }
 
     @Test
     public void testGivenDate() {
         DateBuilder givenDate = DateBuilder.givenDate(14, 12, 1972);
 
         assertEquals(14, givenDate.getDay());
         assertEquals(12, givenDate.getMonth());
         assertEquals(1972, givenDate.getYear());
     }
 
     @Test
     public void testValueOfUtilDate() {
         java.util.Date utilDate = calendar.getTime();
         assertEqualsCalendar(calendar, DateBuilder.valueOf(utilDate));
     }
 
     @Test
     public void testValueOfSqlDate() {
         java.sql.Date sqlDate = new java.sql.Date(calendar.getTimeInMillis());
         assertEqualsCalendar(calendar, DateBuilder.valueOf(sqlDate));
     }
 
     @Test
     public void testParseString() {
         Locale.setDefault(Locale.GERMANY);
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.parse("14.12.1972"));
         Locale.setDefault(Locale.US);
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.parse("Dec 14, 1972"));
         Locale.setDefault(Locale.UK);
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.parse("14-Dec-1972"));
     }
 
     @Test
     public void testParseStringWithGivenLocale() {
         Locale.setDefault(Locale.CHINESE);
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.parse("14.12.1972", Locale.GERMANY));
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.parse("Dec 14, 1972", Locale.US));
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.parse("14-Dec-1972", Locale.UK));
     }
 
     @Test
     public void testParseStringWithGivenFormat() {
         Locale.setDefault(Locale.US);
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.parse("14.12.1972", "dd.MM.yyyy"));
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.parse("Dec 14, 1972", "MMM dd, yyyy"));
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.parse("14-Dec-1972", "dd-MMM-yyyy"));
     }
 
     @Test
     public void testDaysAgo() {
         Locale.setDefault(Locale.GERMANY);
         assertEquals(DateBuilder.givenDate(12, 12, 1972), DateBuilder.givenDate(14, 12, 1972).daysAgo(2));
         assertEquals(DateBuilder.givenDate( 9, 12, 1972), DateBuilder.givenDate(14, 12, 1972).daysAgo(5));
         assertEquals(DateBuilder.givenDate(30, 11, 1972), DateBuilder.givenDate( 1, 12, 1972).daysAgo(1));
         assertEquals(DateBuilder.givenDate(31, 12, 1971), DateBuilder.givenDate( 1,  1, 1972).daysAgo(1));
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.givenDate(14, 12, 1972).daysAgo(0));
         assertEquals(DateBuilder.givenDate(16, 12, 1972), DateBuilder.givenDate(14, 12, 1972).daysAgo(-2));
     }
 
     @Test
     public void previousDay() {
         assertEquals(DateBuilder.givenDate(13, 12, 1972), DateBuilder.givenDate(14, 12, 1972).previousDay());
         assertEquals(DateBuilder.givenDate(31, 12, 1971), DateBuilder.givenDate( 1,  1, 1972).previousDay());
     }
 
     @Test
     public void testDaysAhead() {
         Locale.setDefault(Locale.GERMANY);
         assertEquals(DateBuilder.givenDate(16, 12, 1972), DateBuilder.givenDate(14, 12, 1972).daysAhead(2));
         assertEquals(DateBuilder.givenDate(19, 12, 1972), DateBuilder.givenDate(14, 12, 1972).daysAhead(5));
         assertEquals(DateBuilder.givenDate( 1, 12, 1972), DateBuilder.givenDate(30, 11, 1972).daysAhead(1));
         assertEquals(DateBuilder.givenDate( 1,  1, 1973), DateBuilder.givenDate(31, 12, 1972).daysAhead(1));
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.givenDate(14, 12, 1972).daysAhead(0));
         assertEquals(DateBuilder.givenDate(12, 12, 1972), DateBuilder.givenDate(14, 12, 1972).daysAhead(-2));
     }
 
     @Test
     public void nextDay() {
         assertEquals(DateBuilder.givenDate(15, 12, 1972), DateBuilder.givenDate(14, 12, 1972).nextDay());
         assertEquals(DateBuilder.givenDate( 1,  1, 1973), DateBuilder.givenDate(31, 12, 1972).nextDay());
     }
 
     @Test
     public void testDay() {
         assertEquals(DateBuilder.givenDate( 1, 12, 1972), DateBuilder.givenDate(14, 12, 1972).day(1));
         assertEquals(DateBuilder.givenDate(10, 12, 1972), DateBuilder.givenDate(14, 12, 1972).day(10));
         assertEquals(DateBuilder.givenDate( 1,  1, 1973), DateBuilder.givenDate(14, 12, 1972).day(32));
     }
 
     @Test
     public void testFirstDay() {
         assertEquals(DateBuilder.givenDate( 1, 12, 1972), DateBuilder.givenDate(14, 12, 1972).firstDay());
         assertEquals(DateBuilder.givenDate( 1, 12, 1972), DateBuilder.givenDate(31, 12, 1972).firstDay());
     }
 
     @Test
     public void testLastDay() {
         assertEquals(DateBuilder.givenDate(31, 12, 1972), DateBuilder.givenDate(14, 12, 1972).lastDay());
         assertEquals(DateBuilder.givenDate(30,  4, 1972), DateBuilder.givenDate(11,  4, 1972).lastDay());
         assertEquals(DateBuilder.givenDate(29,  2, 2012), DateBuilder.givenDate(11,  2, 2012).lastDay());
     }
 
     @Test
     public void testMonthsAgo() {
         assertEquals(DateBuilder.givenDate(14, 11, 1972), DateBuilder.givenDate(14, 12, 1972).monthsAgo(1));
         assertEquals(DateBuilder.givenDate(14,  7, 1972), DateBuilder.givenDate(14, 12, 1972).monthsAgo(5));
         assertEquals(DateBuilder.givenDate( 1, 12, 1971), DateBuilder.givenDate( 1,  1, 1972).monthsAgo(1));
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.givenDate(14, 12, 1972).monthsAgo(0));
         assertEquals(DateBuilder.givenDate(14,  3, 1972), DateBuilder.givenDate(14,  1, 1972).monthsAgo(-2));
     }
 
     @Test
     public void testPreviousMonth() {
         assertEquals(DateBuilder.givenDate(14, 11, 1972), DateBuilder.givenDate(14, 12, 1972).previousMonth());
         assertEquals(DateBuilder.givenDate(14,  7, 1972), DateBuilder.givenDate(14,  8, 1972).previousMonth());
     }
 
     @Test
     public void testMonthsAhead() {
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.givenDate(14, 10, 1972).monthsAhead(2));
         assertEquals(DateBuilder.givenDate(14,  6, 1972), DateBuilder.givenDate(14,  1, 1972).monthsAhead(5));
         assertEquals(DateBuilder.givenDate( 1,  1, 1973), DateBuilder.givenDate( 1, 12, 1972).monthsAhead(1));
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.givenDate(14, 12, 1972).monthsAhead(0));
         assertEquals(DateBuilder.givenDate(14, 10, 1972), DateBuilder.givenDate(14, 12, 1972).monthsAhead(-2));
     }
 
     @Test
     public void nextMonth() {
         assertEquals(DateBuilder.givenDate(14, 11, 1972), DateBuilder.givenDate(14, 10, 1972).nextMonth());
         assertEquals(DateBuilder.givenDate(14,  1, 1973), DateBuilder.givenDate(14, 12, 1972).nextMonth());
     }
 
     @Test
     public void testMonth() {
         assertEquals(DateBuilder.givenDate(14,  1, 1972), DateBuilder.givenDate(14, 12, 1972).month(1));
         assertEquals(DateBuilder.givenDate(14, 10, 1972), DateBuilder.givenDate(14, 12, 1972).month(10));
         assertEquals(DateBuilder.givenDate(14,  1, 1973), DateBuilder.givenDate(14, 12, 1972).month(13));
     }
 
     @Test
     public void testFirstMonth() {
         assertEquals(DateBuilder.givenDate(14,  1, 1972), DateBuilder.givenDate(14, 12, 1972).firstMonth());
         assertEquals(DateBuilder.givenDate(14,  1, 1972), DateBuilder.givenDate(14,  3, 1972).firstMonth());
     }
 
     @Test
     public void testLastMonth() {
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.givenDate(14, 10, 1972).lastMonth());
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.givenDate(14,  3, 1972).lastMonth());
     }
 
     @Test
     public void testYearsAgo() {
         Locale.setDefault(Locale.GERMANY);
         assertEquals(DateBuilder.givenDate(14, 12, 1971), DateBuilder.givenDate(14, 12, 1972).yearsAgo(1));
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.givenDate(14, 12, 1972).yearsAgo(0));
         assertEquals(DateBuilder.givenDate(14, 12, 1974), DateBuilder.givenDate(14, 12, 1972).yearsAgo(-2));
     }
 
     @Test
     public void testPreviousYear() {
         assertEquals(DateBuilder.givenDate(14, 12, 1971), DateBuilder.givenDate(14, 12, 1972).previousYear());
         assertEquals(DateBuilder.givenDate( 1,  1, 2011), DateBuilder.givenDate( 1,  1, 2012).previousYear());
     }
 
     @Test
     public void testYearsAhead() {
         Locale.setDefault(Locale.GERMANY);
         assertEquals(DateBuilder.givenDate(14, 12, 1973), DateBuilder.givenDate(14, 12, 1972).yearsAhead(1));
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.givenDate(14, 12, 1972).yearsAhead(0));
         assertEquals(DateBuilder.givenDate(14, 12, 1970), DateBuilder.givenDate(14, 12, 1972).yearsAhead(-2));
     }
 
     @Test
     public void testNextYear() {
         assertEquals(DateBuilder.givenDate(14, 12, 1972), DateBuilder.givenDate(14, 12, 1971).nextYear());
         assertEquals(DateBuilder.givenDate( 1,  1, 2012), DateBuilder.givenDate( 1,  1, 2011).nextYear());
     }
 
     @Test
     public void testYear() {
         assertEquals(DateBuilder.givenDate(14, 12, 1970), DateBuilder.givenDate(14, 12, 1972).year(1970));
         assertEquals(DateBuilder.givenDate(14, 12, 2012), DateBuilder.givenDate(14, 12, 1972).year(2012));
     }
 
     @Test
     public void testEquals() {
         Locale.setDefault(Locale.GERMANY);
         DateBuilder givenDate = DateBuilder.givenDate(14, 12, 1972);
 
         assertTrue (givenDate.equals(givenDate));
         assertTrue (givenDate.equals(DateBuilder.givenDate(14, 12, 1972)));
         assertTrue (givenDate.equals(DateBuilder.parse("14.12.1972")));
         assertFalse(givenDate.equals(DateBuilder.givenDate(14, 12, 1973)));
         assertFalse(givenDate.equals(DateBuilder.givenDate(14, 11, 1972)));
         assertFalse(givenDate.equals(DateBuilder.givenDate(13, 12, 1972)));
     }
 
     @Test
     public void testToUtilDate() {
         DateBuilder dateBuilder = DateBuilder.givenDate(14, 12, 1972);
 
         Calendar utilDateCalendar = calendarFor(dateBuilder.toDate());
 
         assertEquals(14, getDayOfMonth(utilDateCalendar));
         assertEquals(12, getMonth(utilDateCalendar));
         assertEquals(1972, getYear(utilDateCalendar));
         assertNoTime(utilDateCalendar);
     }
 
     @Test
     public void testToSqlDate() {
         DateBuilder dateBuilder = DateBuilder.givenDate(14, 12, 1972);
 
         java.sql.Date sqlDate = dateBuilder.toSqlDate();
         Calendar sqlDateCalendar = calendarFor(sqlDate);
 
         assertEquals(14, getDayOfMonth(sqlDateCalendar));
         assertEquals(12, getMonth(sqlDateCalendar));
         assertEquals(1972, getYear(sqlDateCalendar));
         assertNoTime(sqlDateCalendar);
     }
 
     @Test
     public void testToString() {
         Locale.setDefault(Locale.GERMANY);
         assertEquals(DateBuilder.givenDate(14, 12, 1972).toString(), "14.12.1972");
         Locale.setDefault(Locale.US);
         assertEquals(DateBuilder.givenDate(14, 12, 1972).toString(), "Dec 14, 1972");
         Locale.setDefault(Locale.UK);
         assertEquals(DateBuilder.givenDate(14, 12, 1972).toString(), "14-Dec-1972");
     }
 
     private void assertEqualsCalendar(Calendar cal, DateBuilder today) {
         assertEquals(getDayOfMonth(cal), today.getDay());
         assertEquals(getMonth(cal), today.getMonth());
         assertEquals(getYear(cal), today.getYear());
     }
 
     private void assertNoTime(Calendar utilDateCalendar) {
         assertEquals(0, utilDateCalendar.get(Calendar.HOUR_OF_DAY));
         assertEquals(0, utilDateCalendar.get(Calendar.MINUTE));
         assertEquals(0, utilDateCalendar.get(Calendar.SECOND));
         assertEquals(0, utilDateCalendar.get(Calendar.MILLISECOND));
     }
 
     private Calendar calendarFor(Date utilDate) {
         Calendar cal = GregorianCalendar.getInstance();
         cal.setTime(utilDate);
         return cal;
     }
 
     private int getDayOfMonth(Calendar utilDateCalendar) {
         return utilDateCalendar.get(Calendar.DAY_OF_MONTH);
     }
 
     private int getMonth(Calendar sqlDateCalendar) {
         return sqlDateCalendar.get(Calendar.MONTH) + 1;
     }
 
     private int getYear(Calendar sqlDateCalendar) {
         return sqlDateCalendar.get(Calendar.YEAR);
     }
 }
