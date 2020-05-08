 /**
  * 
  */
 package ch.uzh.ddis.katts.utils;
 
 import java.util.Date;
 import java.util.regex.Pattern;
 
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 
 /**
  * General utiltiy class.
  * 
  * @author "Lorenz Fischer" <lfischer@ifi.uzh.ch>
  */
 public final class Util {
 
 	/** The regular expression pattern to match long values. */
 	private static Pattern longPattern = Pattern.compile("-?\\d+");
 
 	/** The regular expression pattern to match double values. */
	private static Pattern doublePattern = Pattern.compile("[-+]?((\\d*\\.\\d+)|\\d+)[Dd]?");
 
 	/** The regular expression pattern to match iso dates values. */
 	private static Pattern isoDatePattern = Pattern
 			.compile("([\\+-]?\\d{4}(?!\\d{2}\\b))((-?)((0[1-9]|1[0-2])(\\3([12]\\d|0[1-9]|3[01]))?|W([0-4]\\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\\d|[12]\\d{2}|3([0-5]\\d|6[1-6])))([T\\s]((([01]\\d|2[0-3])((:?)[0-5]\\d)?|24\\:?00)([\\.,]\\d+(?!:))?)?(\\17[0-5]\\d([\\.,]\\d+)?)?([zZ]|([\\+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?)?)?");
 
 	/** This formatter is used to parse dateTime string values */
 	private static DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeParser();
 
 	/**
 	 * Tests if the value of a string is a long (integer). This method supports positive and negative numbers.
 	 * 
 	 * @param value
 	 *            the string value to test.
 	 * @return true if the value is an integer and can be parsed using the {@link Long#parseLong(String)} method , false
 	 *         otherwise.
 	 */
 	public static boolean isLong(String value) {
 		return longPattern.matcher(value).matches();
 	}
 
 	/**
 	 * This method tests if a value is a floating point value.
 	 * 
 	 * @param value
 	 *            the string value to test.
 	 * @return true if value represents a floating point value and can be converted using the
 	 *         {@link Double#parseDouble(String)} method, false otherwise.
 	 */
 	public static boolean isDouble(String value) {
 		return doublePattern.matcher(value).matches();
 	}
 
 	/**
 	 * This method tests if a value is a date value in ISO point value.
 	 * 
 	 * @param value
 	 *            the string value to test.
 	 * @return true if value represents a floating point value and can be converted using the
 	 *         {@link Double#parseDouble(String)} method, false otherwise.
 	 */
 	public static boolean isIsoDate(String value) {
 		return isoDatePattern.matcher(value).matches();
 	}
 
 	/**
 	 * Converts a string into a proper Java object.
 	 * <p/>
 	 * The order in which we try to convert the values is: Long - Double - Date - String.
 	 * 
 	 * @param value
 	 *            the string value to convert into an object.
 	 * @return the created object.
 	 */
 	public static Object convertStringToObject(String value) {
 		Object result = value;
 
 		if (Util.isLong(value)) {
 			result = Long.valueOf(value);
 		} else if (Util.isDouble(value)) {
 			result = Double.valueOf(value);
 		} else if (Util.isIsoDate(value)) {
 			result = Util.isoFormat.parseDateTime(value).toDate();
 		}
 
 		return result;
 	}
 
 	/**
 	 * This method parses a string and converts it into a date using the class {@link ISODateTimeFormat}.
 	 * 
 	 * @param dateString
 	 *            the string to be converted.
 	 * @return the resulting date value.
 	 */
 	public static Date parseDateTime(String dateString) {
 		return Util.isoFormat.parseDateTime(dateString).toDate();
 	}
 
 	/** Utility classes should not be initiatlized. */
 	private Util() {
 	}
 
 }
