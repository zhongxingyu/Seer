 package com.pifive.makemyrun;
 
 import android.os.Handler;
 import android.widget.TextView;
 
 /**
  * Displays and updates a stopwatch for a running interface
  * for as long as this object exists.
  */
 public class Timer {
 
	private int startTime;
 	private int timeElapsed;
 
 	private final Thread timerThread;
 	private final TimerRunnable timerRunnable;
 	
 	/**
 	 * Creates a stopwatch which continuously updates its clockText
 	 * with current time since start.
 	 * @param clockText The GUI view to print the text on.
 	 */
 	public Timer(final TextView clockText) {
 		timerRunnable = new TimerRunnable(clockText);
 		timerThread = new Thread(timerRunnable);
 	}
 	
 	/**
 	 * Starts the stopwatch
 	 */
 	public void start(){
		startTime = (int)System.currentTimeMillis();
 		timerThread.start();
 	}
 	
 	/**
 	 * Stops the stopwatch
 	 */
 	public void stop() {
 		timerRunnable.running = false;
 	}
 	
 	public boolean isRunning() {
 		return timerRunnable.running;
 	}
 	
 	/**
 	 * 
 	 * @return Unix Time in seconds when the timer was started
 	 */
	public int getStartTime(){
 		return startTime;
 	}
 	/**
 	 * A runnable which can be stopped like a timer.
 	 */
 	private class TimerRunnable implements Runnable {
 		
 		private final TextView clockText;
 		private Handler handler = new Handler();
 		private boolean running = true;
 		
 		protected TimerRunnable(TextView view) {
 			this.clockText = view;
 		}
 		
 		/**
 		 * Continuously updates Timer's clockText with time elapsed since start.
 		 */
 		@Override
 		public void run() {
 
 			timeElapsed = (int) (((System.currentTimeMillis() - startTime) / 1000) + 0.5);
 			final int sec = timeElapsed % 60;
 			final int min = timeElapsed / 60;
 			final int hour = timeElapsed / 3600;
 			clockText.post(new Runnable() {
 				public void run() {
 					clockText.setText(
 							  (hour < 10 ? "0" + hour : hour) + ":"
 							+ (min < 10 ? "0" + min : min) + ":"
 							+ (sec < 10 ? "0" + sec : sec));
 				}
 			});
 
 			if (running) {
 				// ask current thread to rerun after 100 ms
 				handler.postDelayed(this, 100); 
 			}					
 		}
 	}
 }
