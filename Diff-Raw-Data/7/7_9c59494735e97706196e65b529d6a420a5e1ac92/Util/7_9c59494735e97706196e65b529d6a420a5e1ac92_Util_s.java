 package com.slauson.asteroid_dasher.other;
 
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
 			
 			String timeString = "";
 			
 			if (h > 0) {
 				timeString += h + ":";
 				
 				// only add 0 if we have hours
 				if (m < 10) {
 					timeString += "0";
 				}
 			}
 			
 			timeString += m + ":";
 			
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
 
 	/**
 	 * Returns nice points string for given points
 	 * @param points points
 	 * @return nice points string for given points
 	 */
 	public static String getPointsString(int points) {
 		
 		// add comma if over 999
 		if (points > 999) {
 			
 			int tempPoints = points%1000;
			String pointsString = "" + ((points - tempPoints)/1000) + "," + tempPoints;
 			
 			if (tempPoints < 100) {
 				pointsString += "0";
 			}
 			if (tempPoints < 10) {
 				pointsString += "0";
 			}
 			
 			return pointsString;
 		}
 		
 		return "" + points;
 	}
 
 	
 }
