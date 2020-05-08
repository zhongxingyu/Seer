 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CountDownLatch;
 
 public class FirmTime {
 	
 	/**
 	 * The hour of the beginning of day on a 24 hour clock
 	 */
 	private static final int START_HOUR = 8;
 
 	/**
 	 * The number of minutes that would equate to end of day
 	 */
 	private static final int END_OF_DAY = 540;
 
 	/**
 	 * CDL for starting to run. Ensures FirmTime starts at same time as employees
 	 */
 	private final CountDownLatch startcdl;
 	
 	/**
 	 * The timer
 	 */
 	private final Timer timer = new Timer();
 
 	/**
 	 * Time in minutes elapsed since the program started.
 	 */
	private volatile long timeElapsed;
 
 	/**
 	 * @param cd CountDownLatch that is passed to each employee
 	 */
 	public FirmTime(CountDownLatch cd){
 		startcdl = cd ; 
 	}
 	
 	/**
 	 * Starts the Timer. Schedules a task to increment time elapsed every 10 ms
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
 		} catch (InterruptedException e) {}
 				
 		timer.schedule(task, 0, 10);
 	}
 
 	/**
 	 * Terminates the timer.
 	 */
 	public void cancel() {
 		timer.cancel();
 	}
 
 	/**
 	 * Checks whether the number of minutes elapsed is equivalent to end of day
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
 	 * Converts the long of minutes into an hour minutes representation
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
 	 * Formatted string of the current time
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
 
 }
