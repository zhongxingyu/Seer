 package com.slauson.dasher.other;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Utility methods
  * @author Josh Slauson
  *
  */
 public class Util {
 
 	/**
 	 * Returns time string for given number of seconds
 	 * @param seconds number of seconds
 	 * @return time string for given number of seconds
 	 */
 	public static String getTimeString(int seconds, boolean hours) {
 		
 		if (hours) {
 			int h = seconds/3600;
 			int m = (seconds%3600)/60;
 			int s = seconds%60;
 			
			String timeString = "" + h + ":";
 			
 			if (m < 10) {
 				timeString += "0";
 			}
			timeString += m;
 			
 			if (s < 10) {
 				timeString += "0";
 			}
 			timeString += s;
 			
 			return timeString;
 		} else {
 			int m = seconds/60;
 			int s = seconds%60;
 			
 			if (s < 10) {
 				return "" + m + ":0" + s;
 			} else {
 				return "" + m + ":" + s;
 			}			
 		}
 	}
 	
 	/**
 	 * Returns date string for given time
 	 * @param time time
 	 * @return date string for given time
 	 */
 	public static String getDateString(long time) {
 		
 		// no time
 		if (time == 0) {
 			return "";
 		}
 		
 		Date date = new Date(time);
 		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yy hh:mm aa");
 		
 		return format.format(date);
 		
 	}
 
 	
 }
