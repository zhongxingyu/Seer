 package js.incomplete;
 
 import java.util.Calendar;
 
 public class JSDateUtil {
 
 	public static String getRelativeDate(Calendar date) {
 		Calendar now = Calendar.getInstance();
 		long diff = now.getTimeInMillis() - date.getTimeInMillis();
 		long days = diff / 86400000;
 		
 		if (days < 0) {
 			return "in " + Math.abs(days) + " days";
 		} else if (days > 0) {
 			return days + " days ago";
 		} else {
 			return "today";
 		}
 	}
 	
 	public static String getRelativeTime(Calendar date) {
 		Calendar now = Calendar.getInstance();
 		long diff = now.getTimeInMillis() - date.getTimeInMillis();
 		if (Math.abs(diff) >= 86400000) {
 			return getRelativeDate(date);
 		} else if (Math.abs(diff) >= 3600000) {
 			long hours = diff / 3600000;
 			if (hours < 0) {
				return "in " + Math.abs(hours) + " hours";
 			} else {
 				return hours + " hours ago";
 			}
 		} else if (Math.abs(diff) >= 60000) {
 			long mins = diff / 60000;
 			if (mins < 0) {
				return "in " + Math.abs(mins) + " minutes";
 			} else {
 				return mins + " minutes ago";
 			}
 		} else if (Math.abs(diff) >= 1000) {
 			long secs = diff / 1000;
 			if (secs < 0) {
				return "in " + Math.abs(secs) + " seconds";
 			} else {
 				return secs + " seconds ago";
 			}
 		} else {
 			return "now";
 		}
 	}
 	
 	public static boolean isSameDate(Calendar a, Calendar b) {
 		return (a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
 				&& a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
 				&& a.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH));
 	}
 	
 }
