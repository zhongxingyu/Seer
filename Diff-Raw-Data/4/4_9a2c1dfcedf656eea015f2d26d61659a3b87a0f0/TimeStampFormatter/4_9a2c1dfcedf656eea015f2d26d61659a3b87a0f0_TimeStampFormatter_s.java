 package global;
 
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Locale;
 
 import org.apache.commons.lang.StringUtils;
 
 import play.data.format.Formatters;
 
 public class TimeStampFormatter extends Formatters.SimpleFormatter<Timestamp> {
 	
 	@Override
 	public Timestamp parse(final String input,
 			final Locale locale) throws ParseException {
 		return Timestamp.valueOf(input);
 	}
 
 	@Override
 	public String print(final Timestamp timestamp,
 			final Locale locale) {
 		if (timestamp == null) {
 			return "";
 		}
 		return timestamp.toString();
 	}
 	
 	public static String reformatLocale(String input, Locale locale) {
 		if (StringUtils.isEmpty(input)) {
 			return "";
 		}
 		java.util.Date date = Timestamp.valueOf(input);
 		return formatLocale(date,locale);
 	}
 	
 	public static String formatLocale(java.util.Date date, Locale locale) {
 		if (date==null) { 
 			return "";
 		}
 		DateFormat localeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT,locale);
 		return localeFormat.format(date);
 	}
 	
 	public static String getDatePart(String input) {
 		if (StringUtils.isEmpty(input)) {
 			return "";
 		}
 		String value = StringUtils.substringBefore(input, " ");
 		return value;
 	}
 	
 	public static String getTimePart(String input) {
 		if (StringUtils.isEmpty(input)) {
 			return "";
 		}
 		String value = StringUtils.substringBefore(StringUtils.substringAfter(input, " "),".");
		value = value.substring(0,5);
 		return value;
 	}
 	
 }
