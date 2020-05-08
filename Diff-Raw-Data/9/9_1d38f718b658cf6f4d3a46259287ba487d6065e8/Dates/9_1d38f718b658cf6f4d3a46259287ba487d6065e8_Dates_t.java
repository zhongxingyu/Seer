 package jp.gr.java_conf.afterthesunrise.commons.object;
 
 import static java.util.concurrent.TimeUnit.DAYS;
 import static java.util.concurrent.TimeUnit.MILLISECONDS;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.common.annotations.VisibleForTesting;
 
 /**
  * @author takanori.takase
  */
 public class Dates {
 
 	private Dates() {
 		throw new IllegalAccessError("Utility class shouldn't be instantiated.");
 	}
 
 	public static final long MILLIS_IN_DAY = MILLISECONDS.convert(1L, DAYS);
 
 	public static final long MILLIS_IN_EOD = MILLIS_IN_DAY - 1L;
 
 	private static final ThreadLocal<Calendar> CALS = new ThreadLocal<Calendar>() {
 		@Override
 		protected Calendar initialValue() {
 			return Calendar.getInstance();
 		}
 	};
 
 	private static final String DEBUG = "jp.gr.java_conf.afterthesunrise.commons.debug";
 
 	@VisibleForTesting
 	@Deprecated
 	public static final void enableDebug(long timestamp) {
 		System.setProperty(DEBUG, String.valueOf(timestamp));
 	}
 
 	@VisibleForTesting
 	@Deprecated
 	public static final void disableDebug() {
 		System.clearProperty(DEBUG);
 	}
 
 	public static long getCurrentTime() {
 
 		String value = System.getProperty(DEBUG);
 
 		if (value != null) {
 			return Long.parseLong(value);
 		}
 
 		return System.currentTimeMillis();
 
 	}
 
 	public static Date getCurrentDate() {
 		return new Date(getCurrentTime());
 	}
 
 	public static TimeZone getTimeZone(String id) {
 
 		if (StringUtils.isBlank(id)) {
 			return null;
 		}
 
 		TimeZone timeZone = TimeZone.getTimeZone(id);
 
 		if (!StringUtils.equals(id, timeZone.getID())) {
 			return null;
 		}
 
 		return timeZone;
 
 	}
 
 	public static Long adjustStartOfDay(Long timestamp, TimeZone timeZone) {
 
 		if (timestamp == null || timeZone == null) {
 			return null;
 		}
 
 		Calendar cal = CALS.get();
 
 		cal.setTimeZone(timeZone);
 		cal.setTimeInMillis(timestamp);
 
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 
 		return cal.getTimeInMillis();
 
 	}
 
 	public static Long adjustStartOfDay(Date timestamp, TimeZone timeZone) {
 
 		if (timestamp == null) {
 			return null;
 		}
 
 		return adjustStartOfDay(timestamp.getTime(), timeZone);
 
 	}
 
 	public static Long adjustEndOfDay(Long timestamp, TimeZone timeZone) {
 
 		Long sod = adjustStartOfDay(timestamp, timeZone);
 
 		if (sod == null) {
 			return null;
 		}
 
 		Calendar cal = CALS.get();
 
 		cal.setTimeInMillis(sod);
 
 		cal.add(Calendar.DATE, 1);
 
 		return cal.getTimeInMillis() - 1L;
 
 	}
 
 	public static Long adjustEndOfDay(Date timestamp, TimeZone timeZone) {
 
 		if (timestamp == null) {
 			return null;
 		}
 
 		return adjustEndOfDay(timestamp.getTime(), timeZone);
 
 	}
 
 	public static Long swapTimeZone(Long timestamp, TimeZone from, TimeZone to) {
 
 		if (timestamp == null || from == null || to == null) {
 			return null;
 		}
 
 		return timestamp + from.getOffset(timestamp) - to.getOffset(timestamp);
 
 	}
 
 	public static java.util.Date sanitize(Date date) {
 
 		if (date == null) {
 			return null;
 		}
 
 		if (date.getClass() == Date.class) {
 			return date;
 		}
 
 		return new Date(date.getTime());
 
 	}
 
 	public static java.sql.Date toSqlDate(Long date) {
 
 		if (date == null) {
 			return null;
 		}
 
 		return new java.sql.Date(date.longValue());
 
 	}
 
 	public static java.sql.Date toSqlDate(Date date) {
 
 		if (date == null) {
 			return null;
 		}
 
		if (date instanceof java.sql.Date) {
			return (java.sql.Date) date;
 		}
 
 		return new java.sql.Date(date.getTime());
 
 	}
 
 	public static java.sql.Timestamp toSqlTime(Long date) {
 
 		if (date == null) {
 			return null;
 		}
 
 		return new java.sql.Timestamp(date.longValue());
 
 	}
 
 	public static java.sql.Timestamp toSqlTime(Date date) {
 
 		if (date == null) {
 			return null;
 		}
 
		if (date instanceof java.sql.Timestamp) {
			return (java.sql.Timestamp) date;
 		}
 
 		return new java.sql.Timestamp(date.getTime());
 
 	}
 
 }
