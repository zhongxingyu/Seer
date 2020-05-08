 package dk.itu.ecdar.text.generator.framework;
 
 /**
  * A custom timer class using currentTimeMillis as a basis. 
  */
 public class AutomatonTimer {
 
 	// resolution the timer has to the user
 	private int resolution;
 	
	// timestamp for starting and pausing the timer
 	private long t_start, t_pause;
 	
 	// indicates if the timer is running
 	private boolean running;
 	
 	/**
 	 * 
 	 * @param resolution Sets the resolution
 	 */
 	public AutomatonTimer(int resolution) {
 		this.resolution = resolution;
 		reset();
 		running = true;
 	}
 	
 	/**
 	 * Uses a standard resolution of 1
 	 */
 	public AutomatonTimer() {
 		this(1);
 	}
 	
 	/**
 	 * Reset the clock to 0
 	 */
 	public void reset() {
 		t_start = System.currentTimeMillis();
 		t_pause = t_start;
 	}
 	
 	/**
 	 * Reset the clock with offset
 	 * @param x The offset added to the reset of the time
 	 */
 	public void reset(long x) {
 		reset();
 		addTime(x);
 	}
 	
 	/**
 	 * @return The current time with resolution
 	 */
 	public long getTime() {
 		long time;
 		if (running)
 			time = System.currentTimeMillis() - t_start;
 		else
 			time = t_pause - t_start;
 		return time / resolution;
 	}
 	
 	/**
 	 * Pausing uses a different time stamp to calculate how long
 	 * the timer has been paused
 	 */
 	public void pause() {
 		if (!running)
 			return;
 		running = false;
 		t_pause = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Resume the timer, ignoring the spend time during pause
 	 * by adding it to the starting time stamp.
 	 */
 	public void resume() {
 		if (running)
 			return;
 		t_start += t_pause - System.currentTimeMillis();
 		running = true;
 	}
 	
 	/**
 	 * Add time to the clock
 	 * @param x Time to add
 	 */
 	public void addTime(long x) {
 		t_start -= x * resolution;
 	}
 	
 	/**
	 * Substract time from the clock
	 * @param x Time to substract
 	 */
 	public void subTime(long x) {
 		addTime(-x);
 	}
 	
 	/**
 	 * @return True if the timer is running, false otherwise
 	 */
 	public boolean isRunning() {
 		return running;
 	}
 }
