 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CountDownLatch;
 
 /**
  * @author Kristen Mills
  * 
  *         System time keeper. Tracks all time in the system without knowing
  *         about the rest of the system. Also formats times into appropriate
  *         format.
  */
 public class FirmTime {
 
 	/**
 	 * The hour of the beginning of day on a 24 hour clock.
 	 */
 	private static final int START_HOUR = 8;
 
 	/**
 	 * The number of minutes that would equate to end of day.
 	 */
 	private static final int END_OF_DAY = 540;
 
 	/**
 	 * CDL for starting to run. Ensures FirmTime starts at same time as
 	 * employees.
 	 */
 	private final CountDownLatch startcdl;
 
 	/**
 	 * The timer.
 	 */
 	private final Timer timer = new Timer();
 
 	/**
 	 * Time in minutes elapsed since the program started.
 	 */
 	private long timeElapsed;
 
 	/**
 	 * @param cd
 	 *            CountDownLatch that is passed to each employee
 	 */
 	public FirmTime(CountDownLatch cd) {
 		startcdl = cd;
 	}
 
 	/**
 	 * Starts the Timer. Schedules a task to increment time elapsed every 10 ms.
 	 */
 	public void start() {
 
 		TimerTask task = new TimerTask() {
 			public void run() {
 				timeElapsed += 1;
 			}
 		};
 
 		startcdl.countDown();
 		try {
 			startcdl.await();
 		} catch (InterruptedException e) {
 		}
 
 		timer.schedule(task, 0, 10);
 	}
 
 	/**
 	 * Terminates the timer.
 	 */
 	public void cancel() {
 		timer.cancel();
 	}
 
 	/**
 	 * Calculates the difference between two times. The two times must both
 	 * formatted as if they were printed by an object from this class. Final
 	 * result will be t2-t1, returned as integer minutes.
 	 * 
 	 * @param t1
 	 *            formatted initial string time
 	 * @parm t2 formatted final string time
 	 * @return t2-t1 in minutes
 	 */
 	public static int calculateDifference(String t1, String t2) {
 		String[] split1 = t1.split("[ :]"); // HH:MM AM/PM is split into 3
 											// strings.
 		String[] split2 = t2.split("[ :]");
 		int time1 = 0;
 		int time2 = 0;
 
 		try {
 			// If neither of the two arrays have a length of 3, then an
 			// incorrect String was passed.
 			if ((split1.length != 3) && (split2.length != 3)) {
 				throw new IllegalStateException();
 			}
 
 			// If the two times are not both AM or not both PM,
 			// then time2 (Which must be PM) is moved 12 hours ahead if it's not
 			// 12 PM.
 			if (!split1[2].equalsIgnoreCase(split2[2])) {
 				time2 = 12 * 60;
 			}
 			time1 += (Integer.parseInt(split1[0]) % 12 * 60)
 					+ (Integer.parseInt(split1[1])); // The hours are converted
 														// to minutes and added
 														// to the minute
 			time2 += (Integer.parseInt(split2[0]) % 12 * 60)
 					+ (Integer.parseInt(split2[1])); // If the hour is "12" then
 														// it will be converted
 														// to essentially "0"
 		} catch (NumberFormatException e) {
 			System.out.println(e.getMessage());
 		} catch (IllegalStateException e) {
 			System.out.println(e.getMessage());
 		}
		return time2 - time1;
		
 	}
 
 	/**
 	 * Checks whether the number of minutes elapsed is equivalent to end of day.
 	 * 
 	 * @return true if it is end of day false if it isn't
 	 */
 	public boolean isEndOfDay() {
 		if (timeElapsed == END_OF_DAY)
 			return true;
 		return false;
 	}
 
 	/**
 	 * Getter for time elapsed
 	 * 
 	 * @return time elapsed since beginning of day in minutes
 	 */
 	public long getTimeElapsed() {
 		return timeElapsed;
 	}
 
 	/**
 	 * Converts the long of minutes into an hour minutes representation.
 	 * 
 	 * @return a long array where the first value is hours and second value is
 	 *         minutes
 	 */
 	private long[] hourMinutes() {
 		long curTimeElapsed = timeElapsed;
 		long hour = (START_HOUR + curTimeElapsed / 60) % 12;
 		long minutes = curTimeElapsed % 60;
 		long[] time = { hour, minutes };
 		return time;
 	}
 
 	/**
 	 * Formatted string of the current time.
 	 * 
 	 * @return a string of the current time in the form HH:MM AM/PM
 	 */
 	public String formatTime() {
 		long[] time = hourMinutes();
 		String timeOfDay = "AM";
 		if (time[0] < START_HOUR) {
 			timeOfDay = "PM";
 		}
 		if (time[0] == 0) {
 			time[0] = 12;
 		}
 
 		return String.format("%d:%02d %s", time[0], time[1], timeOfDay);
 	}
 
 	/**
 	 * Test main for testing calculateDifference method.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String t1 = "12:00 PM";
 		String t2 = "1:00 PM";
 
 		System.out.println(FirmTime.calculateDifference(t1, t2));
 	}
 }
