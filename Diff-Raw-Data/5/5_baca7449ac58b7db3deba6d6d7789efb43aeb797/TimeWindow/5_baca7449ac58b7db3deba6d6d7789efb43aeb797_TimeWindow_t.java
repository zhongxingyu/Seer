 package org.kisst.util;
 
 import java.util.Calendar;
 
 
 public class TimeWindow {
 	private final int startHour;
 	private final int startMinute;
 	private final int endHour;
 	private final int endMinute;
 	private final int dayOfWeek;
 	
 	public TimeWindow(String window) {
		int pos=window.indexOf("!");
 		if (pos<0)
 			dayOfWeek=-1;
 		else if (pos==2) {
 			dayOfWeek=getDayOfWeek(window.substring(0,2));
 			window=window.substring(3);
 		}
 		else
			throw new RuntimeException("TimeWindow may only contain two letter day of week name before exclamation mark "+window);
 		String[] parts=window.split("-");
 		if (parts.length!=2)
 			throw new IllegalArgumentException("time window string ["+window+"] should contain exactly one - symbol");
 		startHour=getHour(parts[0]);
 		startMinute=getMinute(parts[0]);
 		endHour=getHour(parts[1]);
 		endMinute=getMinute(parts[1]);
 		if (startHour>endHour)
 			throw new IllegalArgumentException("Time interval "+this+" should not have higher start hour than end hour");
 		if (startHour==endHour && startMinute>endMinute)
 			throw new IllegalArgumentException("Time interval "+this+" should not have higher start time than end time");
 		
 	}
 
 	private int getDayOfWeek(String str) {
 		str=str.toLowerCase();
 		if ("su".equals(str)) return Calendar.SUNDAY;
 		if ("mo".equals(str)) return Calendar.MONDAY;
 		if ("tu".equals(str)) return Calendar.TUESDAY;
 		if ("we".equals(str)) return Calendar.WEDNESDAY;
 		if ("th".equals(str)) return Calendar.THURSDAY;
 		if ("fr".equals(str)) return Calendar.FRIDAY;
 		if ("sa".equals(str)) return Calendar.SATURDAY;
 
 		if ("zo".equals(str)) return Calendar.SUNDAY;
 		if ("ma".equals(str)) return Calendar.MONDAY;
 		if ("di".equals(str)) return Calendar.TUESDAY;
 		if ("wo".equals(str)) return Calendar.WEDNESDAY;
 		if ("do".equals(str)) return Calendar.THURSDAY;
 		if ("vr".equals(str)) return Calendar.FRIDAY;
 		if ("za".equals(str)) return Calendar.SATURDAY;
 		
 		throw new RuntimeException("Unknown day of week "+str);
 	}
 
 	public String toString() { return startHour+":"+startMinute+"-"+endHour+":"+endMinute; }
 	
 	public boolean isTimeInWindow(int dow, int hour, int minute) {
 		if (dayOfWeek>0)
 			if (dow!=dayOfWeek)
 				return false;
 		if (hour<startHour || hour>endHour)
 			return false;
 		if (hour>startHour || minute>=startMinute)
 			if (hour<endHour || minute<=endMinute)
 				return true;
 		return false;
 	}
 	
 	private int getHour(String time) {
 		String[] parts=time.split(":");
 		if (parts.length!=2)
 			throw new IllegalArgumentException("time string ["+time+"] should contain exactly one : symbol");
 		return Integer.parseInt(parts[0].trim());
 	}
 	private int getMinute(String time) {
 		String[] parts=time.split(":");
 		if (parts.length!=2)
 			throw new IllegalArgumentException("time string ["+time+"] should contain exactly one : symbol");
 		return Integer.parseInt(parts[1].trim());
 	}
 }
