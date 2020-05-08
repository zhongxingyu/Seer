 package my.commons.lang.util;
 
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * 日期与时间工具类
  * 
  * @author xiegang
  * @since 2013-2-1
  *
  */
 public class DateUtils {
 	/**
 	 * ignore Calendar time
 	 * @param cal
 	 * @return
 	 */
 	public static Calendar ignoreTime(Calendar cal) {
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		
 		return cal;
 	}
 	
 	/**
 	 * ignore Date time
 	 * 
 	 * @param date
 	 * @return
 	 */
 	public static Date ignoreTime(Date date) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		
 		return cal.getTime();
 	}
 	
 	/**
 	 * 获取指定时间前一天的最后时间
 	 * @return
 	 */
 	public static Calendar getDayEnd(Calendar cal) {
 		// ignore time
 		cal = ignoreTime(cal);
 		cal.add(Calendar.MILLISECOND, -1);
 		return cal;
 	}
 	
 	/**
 	 * 获取指定时间前一天的最后时间
 	 * @return
 	 */
 	public static Date getDayEnd(Date date) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		
 		cal = getDayEnd(cal);
 		
 		return cal.getTime();
 	}
 	
 	/**
 	 * 获取今天前一天的最后时间
 	 * @return
 	 */
 	public static Date getDayEnd() {
 		Calendar cal = Calendar.getInstance();
 		
 		cal = getDayEnd(cal);
 		
 		return cal.getTime();
 	}
 	
 	/**
 	 * 获取指定时间前一周的最后时间
 	 * @return
 	 */
 	public static Calendar getWeekEnd(Calendar cal) {
 		cal = ignoreTime(cal);
 		
 		int dow = cal.get(Calendar.DAY_OF_WEEK); // day of week
 		dow = dow == 1 ? 6 : dow - 2;
 		cal.add(Calendar.DAY_OF_MONTH, -dow);
 		
 		cal.add(Calendar.MILLISECOND, -1);
 		
 		return cal;
 	}
 	
 	/**
 	 * 获取指定时间前一周的最后时间
 	 * @return
 	 */
 	public static Date getWeekEnd(Date date) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		
 		cal = getWeekEnd(cal);
 		
 		return cal.getTime();
 	}
 	
 	/**
 	 * 获取本周前一周的最后时间
 	 * @return
 	 */
 	public static Date getWeekEnd() {
 		Calendar cal = Calendar.getInstance();
 		
 		cal = getWeekEnd(cal);
 		
 		return cal.getTime();
 	}
 	
 	/**
 	 * 获取指定时间前一月的最后时间
 	 * @param cal
 	 * @return
 	 */
 	public static Calendar getMonthEnd(Calendar cal) {
 		cal = ignoreTime(cal);
 		
 		int dom = cal.get(Calendar.DAY_OF_MONTH); // day of month
 		dom = dom - 1;
 		cal.add(Calendar.DAY_OF_MONTH, -dom);
 		
 		cal.add(Calendar.MILLISECOND, -1);
 		
 		return cal;
 	}
 	
 	/**
 	 * 获取指定时间前一月的最后时间
 	 * @return
 	 */
 	public static Date getMonthEnd(Date date) {
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		
 		cal = getMonthEnd(cal);
 		
 		return cal.getTime();
 	}
 	
 	/**
 	 * 获取本月前一月的最后时间
 	 * @return
 	 */
 	public static Date getMonthEnd() {
 		Calendar cal = Calendar.getInstance();
 		
 		cal = getMonthEnd(cal);
 		
 		return cal.getTime();
 	}
 	
 	/**
 	 * 判断两个两个时间是不是同一天
 	 * @param date1
 	 * @param date2
 	 * @return
 	 */
 	public static boolean isSameDay(Date date1, Date date2) {
 		Calendar cal1 = Calendar.getInstance();
 		cal1.setTime(date1);
 		Calendar cal2 = Calendar.getInstance();
 		cal2.setTime(date2);
 		return isSameDay(cal1, cal2);
 	}
 	
 	/**
 	 * 判断两个两个时间是不是同一天
 	 * @param cal1
 	 * @param cal2
 	 * @return
 	 */
 	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
 		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
 				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1
 				.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
 	}
 	
 	/**
 	 * 判断两个两个时间是不是同一个星期
 	 * @param date1
 	 * @param date2
 	 * @return
 	 */
 	public static boolean isSameWeek(Date date1, Date date2) {
 		Calendar cal1 = Calendar.getInstance();
 		cal1.setTime(date1);
 		Calendar cal2 = Calendar.getInstance();
 		cal2.setTime(date2);
		return isSameDay(cal1, cal2);
 	}
 	
 	/**
 	 * 判断两个两个时间是不是同一个星期
 	 * @param cal1
 	 * @param cal2
 	 * @return
 	 */
 	public static boolean isSameWeek(Calendar cal1, Calendar cal2) {
 		cal1.setFirstDayOfWeek(Calendar.MONDAY);
 		cal2.setFirstDayOfWeek(Calendar.MONDAY);
 		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
 				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1
 				.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR));
 	}
 	
 	/**
 	 * 判断两个两个时间是不是同一个月
 	 * @param date1
 	 * @param date2
 	 * @return
 	 */
 	public static boolean isSameMonth(Date date1, Date date2) {
 		Calendar cal1 = Calendar.getInstance();
 		cal1.setTime(date1);
 		Calendar cal2 = Calendar.getInstance();
 		cal2.setTime(date2);
		return isSameDay(cal1, cal2);
 	}
 	
 	/**
 	 * 判断两个两个时间是不是同一个月
 	 * @param cal1
 	 * @param cal2
 	 * @return
 	 */
 	public static boolean isSameMonth(Calendar cal1, Calendar cal2) {
 		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
 				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1
 				.get(Calendar.MONTH) == cal2.get(Calendar.MONTH));
 	}
 }
