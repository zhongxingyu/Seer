 package it.chalmers.tendu.gamemodel;
 
 public class SimpleTimer {
 
 	public static enum STATE {
 		RUNNING, PAUSED, DONE
 
 	}
 
 	private long totalTime;
 	private long remainingTime;
 	private long endTime;
 	private SimpleTimer.STATE state;
 
 	/**
 	 * Creates a simple timer
 	 */
 	public SimpleTimer() {
 		state = STATE.PAUSED;
 	}
 
 	/**
 	 * Starts a timer with the specified amount of time.
 	 * If the timer is already
 	 * running or has ended calling this method will do nothing
 	 * 
 	 * @param time
 	 *            time in milliseconds
 	 */
 	public void start(long time) {
 		if (state == STATE.RUNNING || state == STATE.DONE) {
 			return;
 		}
 
 		totalTime = time;
 		remainingTime = totalTime;
 		setEndTime(totalTime);
 		state = STATE.RUNNING;
 	}
 
 	/**
 	 * Restarts the timer with the specified amount of time.
 	 * Will overwrite any previously running timer
 	 * 
 	 * @param time
 	 *            time in milliseconds
 	 */
 	public void restart(long time) {
 		state = STATE.PAUSED; //start timer will change this to running
 		start(time);
 	}
 
 	/**
 	 * Pause the timer
 	 */
 	public void pause() {
		if(state == STATE.DONE) { //can't pause if done
			return;
		}
 		getRemainingTime();
 		state = STATE.PAUSED;
 	}
 
 	/**
 	 * Resume the timer
 	 */
 	public void resume() {
 		setEndTime(getRemainingTime());
 		state = STATE.RUNNING;
 	}
 
 	/**
 	 * @param time
 	 *            the change in milliseconds, can be positive or negative;
 	 */
 	public void change(long time) {
 		endTime = endTime + time;
 	}
 
 	/**
 	 * Gets the remaining time
 	 * 
 	 * @return time left in milliseconds
 	 */
 	public long getRemainingTime() {
 		if (state == STATE.RUNNING) {
 			if (remainingTime > 0) {
 				remainingTime = endTime - System.currentTimeMillis();
 			} else {
 				remainingTime = 0;
 				state = STATE.DONE; // the timer is done
 			}
 		}
 		return remainingTime;
 	}
 
 	private void setEndTime(long time) {
 		this.endTime = time + System.currentTimeMillis();
 	}
 	
 	public boolean isRunning() {
 		getRemainingTime();
 		if(state == STATE.RUNNING) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean isDone() {
 		getRemainingTime();
 		if(state == STATE.DONE) {
 			return true;
 		}	
 		return false;
 	}
 
 	public void stop() {
 		getRemainingTime();
 		state = STATE.DONE;
 	}
 }
