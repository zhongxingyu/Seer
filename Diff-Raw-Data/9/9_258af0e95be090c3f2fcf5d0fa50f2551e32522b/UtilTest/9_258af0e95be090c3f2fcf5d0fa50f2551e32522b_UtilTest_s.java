 package ch.uzh.ddis.katts.utils;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class UtilTest {
 
 	/** This formatter is used to parse dateTime string values */
 	private DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeParser();
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testIsLong() {
 		String[] valuesToTest = { "1.0", "0", ".1", "-1", "+1", "-321321", "+654654" };
 
 		System.out.println("testIsLong:");
 		for (String testValue : valuesToTest) {
 			boolean validValue;
 			System.out.println("trying " + testValue);
 			try {
 				Long.parseLong(testValue);
 				validValue = true;
 			} catch (NumberFormatException e) {
 				validValue = false;
 			}
 			assertEquals(validValue, Util.isLong(testValue));
 
 			// can an integer be parsed to a double?
 			assertFalse(Util.isLong("1.0"));
 		}
 	}
 
 	@Test
 	public void testIsDouble() {
		String[] valuesToTest = { "1.0", "+1.0", "-1.0", "0.0000", "0", "0.0001", "-0.0000", ".1" };
 
 		System.out.println("testIsDouble:");
 		for (String testValue : valuesToTest) {
 			boolean validValue;
 			System.out.println("trying " + testValue);
 			try {
 				Double.parseDouble(testValue);
 				validValue = true;
 			} catch (NumberFormatException e) {
 				validValue = false;
 			}
 			assertEquals(validValue, Util.isDouble(testValue));
 		}
 	}
 
 	@Test
 	public void testIsIsoDate() {
 		String[] valuesToTest = { "2004-01-02", "2004-02-03", "2004-02-03T12:12", "2004-01-01T00:00",
 				"2004-03-02T01:01", "2004-05-04T00:00", "2004-12-12T00:00", "2004-13-14T00:00", "2004-12-03T00:59" };
 
 		System.out.println("testIsIsoDate:");
 		for (String testValue : valuesToTest) {
 			boolean validValue;
 			System.out.println("trying " + testValue);
 			try {
 				this.isoFormat.parseDateTime(testValue);
 				validValue = true;
 			} catch (IllegalArgumentException e) {
 				validValue = false;
 			}
 			assertEquals(validValue, Util.isIsoDate(testValue));
 		}
 	}
 
 	@Test
 	public void testConverterLong() {
 		assertEquals(Long.valueOf(1L), Util.convertStringToObject("1"));
 	}
 
 	@Test
 	public void testConverterDouble() {
 		assertEquals(Double.valueOf(1.0D), Util.convertStringToObject("1.0D"));
 		assertEquals(Double.valueOf(1.0D), Util.convertStringToObject("1.0d"));
 	}
 
 	@Test
 	public void testConverterDateTime() {
 		Calendar cal = GregorianCalendar.getInstance();
 		cal.set(2001, 2, 4, 5, 6, 7);
 		cal.set(Calendar.MILLISECOND, 0);
 
 		long calDate = cal.getTime().getTime();
 		long parseDate = ((Date) Util.convertStringToObject("2001-03-04T05:06:07")).getTime();
 		assertEquals(calDate, parseDate);
 
 	}
 
 }
