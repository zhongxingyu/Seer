 package net.clonecomputers.lab.todo;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 public class ScheduledAlarm {
 
	public static final int WILDCARD = 60;
 	public static final int NO_VALUE = 0;
 	
 	private final int ye;
 	private final int mo;
 	private final int da;
 	private final int ho;
 	private final int mi;
 	
 	public ScheduledAlarm(int year, int month, int day, int hour, int minute) {
 		ye = year;
 		mo = month;
 		da = day;
 		ho = hour;
 		mi = minute;
 	}
 	
 	Date getSoonestDate() {
		Calendar curTime = new GregorianCalendar();
		curTime.setTime(new Date());
		int year;
		int month;
		int day;
		int hour;
		int minute;
		if(mi != WILDCARD) {
			minute = mi;
		} else {
			minute = curTime.get(Calendar.MINUTE) + 1;
		}
		if(ho != WILDCARD) {
			hour = ho;
 		} else {
			hour = curTime.get(Calendar.HOUR_OF_DAY);
 		}
				
 	}
 
 }
