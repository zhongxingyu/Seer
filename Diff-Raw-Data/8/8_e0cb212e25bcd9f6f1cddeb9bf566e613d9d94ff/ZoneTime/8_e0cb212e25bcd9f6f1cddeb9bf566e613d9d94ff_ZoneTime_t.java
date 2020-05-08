 package jp.gr.java_conf.afterthesunrise.commons.time;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static java.lang.String.format;
 import static java.util.Objects.hash;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 /**
  * @author takanori.takase
  */
 public class ZoneTime implements Serializable, Comparable<ZoneTime> {
 
 	private static final long serialVersionUID = -4126226706081459619L;
 
	private static final long EOD = MILLISECONDS.convert(1L, DAYS) - 1L;

 	private static final ThreadLocal<Calendar> CAL = new ThreadLocal<Calendar>() {
 		@Override
 		protected Calendar initialValue() {
 			return Calendar.getInstance();
 		}
 	};
 
 	private final Time time;
 
 	private final TimeZone timeZone;
 
 	public ZoneTime(Time time, TimeZone tz) {
 		this.time = checkNotNull(time);
 		this.timeZone = (TimeZone) checkNotNull(tz).clone();
 	}
 
 	@Override
 	public String toString() {
 		return format("%s %s", getTime(), getTimeZoneId());
 	}
 
 	@Override
 	public int hashCode() {
 		return hash(getTime(), getTimeZoneId());
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof ZoneTime) {
 			return compareTo((ZoneTime) o) == 0;
 		}
 		return false;
 	}
 
 	@Override
 	public int compareTo(ZoneTime o) {
 
		long now = EOD;
 
 		long l1 = adjust(now);
 
 		long l2 = o.adjust(now);
 
 		return Long.compare(l1, l2);
 
 	}
 
 	public Long adjust(Long timestamp) {
 
 		if (timestamp == null) {
 			return null;
 		}
 
 		Calendar cal = CAL.get();
 		cal.setTimeZone(timeZone);
 		cal.setTimeInMillis(timestamp);
 		cal.set(Calendar.HOUR_OF_DAY, time.getHour());
 		cal.set(Calendar.MINUTE, time.getMinute());
 		cal.set(Calendar.SECOND, time.getSecond());
 		cal.set(Calendar.MILLISECOND, time.getMillisecond());
 		return cal.getTimeInMillis();
 	}
 
 	public Time getTime() {
 		return time;
 	}
 
 	public TimeZone getTimeZone() {
 		return (TimeZone) timeZone.clone();
 	}
 
 	public String getTimeZoneId() {
 		return timeZone.getID();
 	}
 
 }
