 package com.rimproject.andsensor;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
 /* Example of how this class and its timer's will work:
  * 
  * Timer 1 -> 10 seconds has passed 
  * 		Timer 2 created -> let me know when 3 seconds has passed
  * 		Sensor recording start
  * 			10:55:10 v:11
  * 			10:55:10 v:12
  * 			10:55:11 v:11
  * 			10:55:11 v:10
  * 			10:55:12 v:15
  * 			10:55:12 v:16
  * 			10:55:13 v:11
  * 		Timer 2 -> 3 seconds has passed
  * 		sensor recording stop
  * 
  * Timer 1 -> 10 seconds has passed 
  * 		Timer 2 created -> let me know when 3 seconds has passed
  * 		Sensor recording start
  * 			10:55:20 v:11
  * 			10:55:20 v:12
  * 			10:55:21 v:11
  * 			10:55:21 v:10
  * 			10:55:22 v:15
  * 			10:55:22 v:16
  * 			10:55:23 v:11
  * 		Timer 2 -> 3 seconds has passed
  * 		sensor recording stop
  * 	etc.
  */
 
 public abstract class BasicLogger extends TimerTask  implements LoggerInterface, SensorEventListener
 {
 	private Timer delayBetweenLoggingTimer; // Timer 1
 	private long delayBetweenLogging;
 	private long loggingDuration;
 	private DataLogger dataLogger;
 	protected SensorManager sensorManager;
 	protected Sensor sensor;
 
 	public BasicLogger() 
 	{
 		super();		
 		
 		this.delayBetweenLogging = 60 * 1000;
 		this.loggingDuration = 10 * 1000;
 	}
 	
 	public void initiateRepeatedLogging() 
 	{
 		System.out.println(this+" : "+Calendar.getInstance().getTime()+" @ Logging Initiated");
 		this.delayBetweenLoggingTimer = new Timer();
 		this.delayBetweenLoggingTimer.scheduleAtFixedRate(this, 0, this.delayBetweenLogging); //0 == triggers immediately
 	}
 	
 	public void terminateRepeatedLogging(boolean immidiate) 
 	{
 		this.delayBetweenLoggingTimer.cancel();
 		if (immidiate) {
			this.dataLogger.run();
 		}
 		System.out.println(this+" : "+Calendar.getInstance().getTime()+" @ Logging Terminated");
 	}
 	
 	protected void startLogging() 
 	{
 		System.out.println(this+" : "+Calendar.getInstance().getTime()+" @ Logging Started");
 	}
 	
 	protected void stopLogging() 
 	{
 		System.out.println(this+" : "+Calendar.getInstance().getTime()+" @ Logging Stopped");
 	}
 	
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		System.out.println(this+" : "+"onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
 		
 	}
 	
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		System.out.println(this+" : "+"onSensorChanged: " + event);
 	}
 	
 	class DataLogger extends TimerTask
 	{
 		private BasicLogger logger;
 		private Timer loggingDurationTimer; // Timer 2
 		public DataLogger(BasicLogger logger)
 		{
 			this.logger = logger;
 			this.loggingDurationTimer = new Timer();
 			this.loggingDurationTimer.scheduleAtFixedRate(this, loggingDuration, loggingDuration);
 			this.logger.startLogging();
 		}
 		
 		public void run()
 		{
 			this.logger.stopLogging();
 			//stop the timer as we don't want timer 2 to repeat
 			this.loggingDurationTimer.cancel();
 		}
 		
 	}
 
 	public long getDelayBetweenLogging() {
 		return delayBetweenLogging;
 	}
 
 	public void setDelayBetweenLogging(long delayBetweenLogging) {
 		this.delayBetweenLogging = delayBetweenLogging;
 	}
 
 	public long getLoggingDuration() {
 		return loggingDuration;
 	}
 
 
 	public void setLoggingDuration(long loggingDuration) {
 		this.loggingDuration = loggingDuration;
 	}
 
 	
 	@Override
 	public void run() {
 		//timer 1
 		System.out.println(this+" : "+Calendar.getInstance().getTime()+" @ Trigger Logging");
 		this.dataLogger = new DataLogger(this);
 	}
 	
 	public String toString() {
 		return this.getClass().getSimpleName();
 	}
 }
