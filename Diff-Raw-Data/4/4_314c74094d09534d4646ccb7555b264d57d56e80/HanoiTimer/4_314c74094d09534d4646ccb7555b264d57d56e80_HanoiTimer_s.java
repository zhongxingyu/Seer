 package edu.ucsb.cs56.projects.games.towers_of_hanoi.utility;
 
 
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.GregorianCalendar;
 
 import javax.swing.JLabel;
 
 /**
 * @author Aaron Wexler
  * This is a timer class that will begin running as soon as it is created. A JLabel is required for it to display in format mm:ss
  */
 public class HanoiTimer {
 	private long startTime = 0;
 	private JLabel timeLabel = null;
 	private boolean stopped = true;
 	
 	/**
 	 * Consructs a new HanoiTimer and starts the timer running.
 	 * @param label The label that will receive the formatted elapsed time.
 	 */
 	public HanoiTimer(JLabel label) {
 		timeLabel=label;
         Timer timer = new Timer();
         timer.schedule(new TimerTask() {
 
             @Override
             public void run() {
                updateTimer();
             }
         }, 0, 1000);
         start();
 	}
 	
 	public void updateTimer() {
 		if(timeLabel != null) {
 			this.SetTimeElapsedText();
 		}
 	}
 	
 	/**
 	 * Stops the timer then restarts it.
 	 */
 	public void restart() { 
 		this.stop();
 		this.start();
 	}
 	
 	/**
 	 * If stopped, the timer will restart. Otherwise, do nothing
 	 */
 	public void start() {//a start method that acts as a continue was not included in this class because this game would never make use of it
 		if(!stopped)return;
 		startTime = System.currentTimeMillis();
 		stopped = false;
 	}
 	
 	/**
 	 * Stops processing of timer events
 	 */
 	public void stop() {
 		stopped = true;
 	}
 	
 	/**
 	 * Sets the label text of the JLabel to the elapsed time in proper format
 	 */
 	public void SetTimeElapsedText() {
 		if(stopped || (timeLabel == null))return;
 		GregorianCalendar gc = new GregorianCalendar();
 		gc.setTimeInMillis(System.currentTimeMillis() - startTime);
 		String time = String.format("%02d", gc.get(GregorianCalendar.MINUTE)) + ":" + String.format("%02d", gc.get(GregorianCalendar.SECOND));
 		timeLabel.setText(time);
 	}
   
 }
