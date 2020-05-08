 package com.rimproject.andsensor;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Environment;
 import android.util.Log;
 
 /* Example of how this class and its timer's will work:
  * 
  * Timer 1 -> 10 seconds has passed 
  * 		Sensor recording start
  * 		Perform work
  * 		sensor recording stop
  * 
  * Timer 1 -> 10 seconds has passed 
  * 		Sensor recording start
  * 		Perform work
  * 		sensor recording stop
  * 	etc.
  */
 
 public abstract class BasicLogger extends TimerTask  implements LoggerInterface, SensorEventListener
 {
 	private Timer delayBetweenLoggingTimer; // Timer 1
 	private long delayBetweenLogging;
 	public static final String  DIR="LifeLogger"; // constant for the application main folder that will be created
 												 // it would be in the sdcard folder
 
 
 	public BasicLogger() 
 	{
 		super();		
 		
 		this.delayBetweenLogging = 60 * 1000;
 	}
 	
 	public void initiateRepeatedLogging() 
 	{
 		System.out.println(this+" : "+Calendar.getInstance().getTime()+" @ Logging Initiated");
 		this.delayBetweenLoggingTimer = new Timer();
 		this.delayBetweenLoggingTimer.scheduleAtFixedRate(this, 0, this.delayBetweenLogging); //0 == triggers immediately
 	}
 	
 	public void terminateRepeatedLogging(boolean immidiate) 
 	{
 		this.cancel();
 		this.delayBetweenLoggingTimer.cancel();
 		System.out.println(this+" : "+Calendar.getInstance().getTime()+" @ Logging Terminated");
 		this.delayBetweenLoggingTimer = null;
 	
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
 	
 	
 
 	public long getDelayBetweenLogging() {
 		return delayBetweenLogging;
 	}
 
 	public void setDelayBetweenLogging(long delayBetweenLogging) {
 		this.delayBetweenLogging = delayBetweenLogging;
 	}
 
 	
 	@Override
 	public void run() {
 		//timer 1
 		System.out.println(this+" : "+Calendar.getInstance().getTime()+" @ Trigger Logging");
 		performLogging();
 	}
 	
 	public String toString() {
 		return this.getClass().getSimpleName();
 	}
 	
 	protected void performLogging() {
 		System.out.println(this+" : "+Calendar.getInstance().getTime()+" @ Perform logging");
 	}
 	
 	
 	/*
 	 * it will form the file name based on sensorName_date
 	 * example light_23-06-2011.txt
 	 */
     public String getFileName(String sensorName)
 	 {
 	    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
 			
 			String date = sdf.format(Calendar.getInstance().getTime());
 			
 			String fileName = sensorName+"_"+date+".txt";
 			return fileName;
 	 }
 	    
 	    /*
 	     * @data : is the raw data to be logged
 	     * @sensorName : the name of the sensor
 	     * @closeConnection: option for the user to colse the connection or keep it
 	     * in case of frequent logging operations , it should be set as false and later
 	     * in last call it should be set to true
 	     * 
 	     */
 	 public void writeToLogFile(String data, String sensorName)
 	 {
 	     BufferedWriter bw = null;
 	         
 	         try
 	         {
 	        	Log.v("Logger",Environment.getExternalStorageState()); 
 	         	File sdCardDir = Environment.getExternalStorageDirectory();
 	         	File dir = new File (sdCardDir.getAbsolutePath() + "/"+DIR);
 	         	dir.mkdirs();
 	            File file = new File(dir,getFileName(sensorName));
 	            
 	         	bw = new BufferedWriter(new FileWriter(file,true));
 	            bw.append(data);
 	            bw.newLine();
 	            
 	            //bw.flush(); // should not be used when we are closing the file as it will write 
 	            			 // the same log line twice. shoule only be used if we are not closing the file   
 	             
 	         }
 	         catch(IOException e)
 	         {
 	         	e.printStackTrace();
 	         }
 	        finally
 	         {
 	         	try
 	         	{
 	         		if (bw !=null)
 	         			bw.close();
 	         	}
 	         	  catch(IOException e)
 	               {
 	               	e.printStackTrace();
 	               }
 	         }
 	    }
 }
