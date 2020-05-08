 package com.rimproject.context;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.LocationManager;
 import android.media.AudioManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.BatteryManager;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 import com.rimproject.andsensor.*;
 import com.rimproject.fileio.FileLoggingIO;
 import com.rimproject.logreadings.AccelerometerReading;
 import com.rimproject.logreadings.LightReading;
 
 public class DeviceStatus {
 	
 	private String location;
 	static int batteryCharging = -1;
 	
 	public boolean isIdle(int duration){
 		boolean result = false;
 		if(!isDeviceInUse(duration) && isStationary(duration)){
 			result = true;
 		}
 		return result;
 	}
 	
 	public boolean isActive(int duration){
 		boolean result = false;
 		TelephonyManager telephonyManager = (TelephonyManager) AndSensor.getContext().getSystemService(Context.TELEPHONY_SERVICE);
 		if(telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE){
 			result = false;
 		}
 		else if(telephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK){
 			result = true;
 		}
 		return result;
 	}
 	
 	public boolean isPassive(int duration){
 		boolean result = false;
 		AudioManager audioManager = (AudioManager) AndSensor.getContext().getSystemService(Context.AUDIO_SERVICE);
 		if(audioManager.isMusicActive()){
 			result = true;
 		}
 		return result;
 	}
 	
 	public boolean isDeviceInUse(int duration){
 		boolean result = false;
 		if(isActive(duration) || isPassive(duration)) {
 			result = true;
 		}
 		return result;
 	}
 	
 	public boolean isStationary(int duration){
 		boolean result = false;
 		double accelerometerActivityLevel = checkAccelerometerActivityLevel(duration);
 		if((accelerometerActivityLevel > SensorConstants.MIN_ACCELEROMETER_STATIONARY_LEVEL 
 		  && accelerometerActivityLevel < SensorConstants.MAX_ACCELEROMETER_STATIONARY_LEVEL) 
 		 // commented the logic for isLocationChanged as it is not complete yet 
 		  //&& isLocationChanged(duration)
 		  ){
 			result = true;
 		}
 		return result;
 	}
 	
 	
 	
 	public boolean isLocationChanged(int duration){
 		boolean result = false;
 		if(isGPSAvailable()){
 			
 			if(checkIfGPSChanging(duration) >= SensorConstants.STATIONARY){
 				result = true;
 			}
 		}
 		else{
 			if(isWIFIAvailable()){
 					
 			}
 			else if(isBluetoothAvailable()){
 				
 			}
 			else if(isWIFIAvailable()){
 				
 			}
 			else if(isNetworkAvailable()){
 				
 			}
 		}
 		return result;
 	}
 	
 	
 		public int isDeviceCharging(){
 			
 			
 			AndSensor.getContext().registerReceiver(broadCastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
 			
 			if(batteryCharging == -1){
 				try {
 					Thread.sleep(10);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				return batteryCharging;
 			}
 			else{
 				return batteryCharging;
 			}
 			
 		}
 	
 		BroadcastReceiver broadCastReceiver = new BroadcastReceiver(){
 			//int batteryCharging = 0;
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				int batteryStatus = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
 				if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)){
 					batteryCharging = batteryStatus;
 					Log.v("InIF TestBat", batteryStatus+"");
 				}
 				
 			}
 
 		};
 	public double checkLightLevel(int duration){
 		double result = -1.0;
 		
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.SECOND, -duration);
 		Date d1 = cal.getTime();
 	    Date d2 = Calendar.getInstance().getTime();
 
 	    FileLoggingIO<LightReading> fio = new FileLoggingIO<LightReading>();
         HashMap<Date,List<LightReading>> map = fio.readFromTXTLogFile(LightSensorLogger.SENSOR_NAME, new LightReading(), null,d1,d2);
         
         //=========== MEDIAN ===========
 //        Collection<List<LightReading>> c = map.values();
 //        if (c.size() == 0) {
 //        	//no readings
 //        	return 999999.9;
 //        }
 //        
 //        //obtain an Iterator for Collection
 //        Iterator<List<LightReading>> itr = c.iterator();
 //        
 //        ArrayList<LightReading> lightReadings = new ArrayList<LightReading>();
 //        //iterate through HashMap values iterator
 //        while(itr.hasNext()) {
 //        	List<LightReading> readings = (List<LightReading>)itr.next();
 //        	for (LightReading lightReading : readings) {
 //				lightReadings.add(lightReading);
 //			}
 //      	}
 //        
 //        Collections.sort(lightReadings);
 //        LightReading middleReading = lightReadings.get(lightReadings.size()/2);
 //        result = middleReading.getLightValue();
       //=========== END MEDIAN ===========
         
       //=========== AVERAGE ===========
         Set<Date> es = map.keySet();
         TreeSet<Date> ts = new TreeSet<Date>(es);
         
 		int numberOfReadings = 0;
 		double accumulator = 0.0;
 		for (Date key : ts) {
 			for (LightReading reading: map.get(key)) {
 				numberOfReadings++;
 				 accumulator += reading.getLightValue();
 			}
 		}
 		result = accumulator / numberOfReadings; //average of all readings
         //=========== END AVERAGE ===========
 		
 		return result;
 	}
 	
 	public double checkAccelerometerActivityLevel(int duration){
 		double result = 0;
 		
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.SECOND, -duration);
 		Date d1 = cal.getTime();
 	    Date d2 = Calendar.getInstance().getTime();
 
 	    FileLoggingIO<AccelerometerReading> fio = new FileLoggingIO<AccelerometerReading>();
         HashMap<Date,List<AccelerometerReading>> map = fio.readFromTXTLogFile(AccelerometerLogger.SENSOR_NAME, new AccelerometerReading(), null,d1,d2);
         
         Set<Date> es = map.keySet();
         TreeSet<Date> ts = new TreeSet<Date>(es);
         
 		int numberOfReadings = 0;
 		double accumulator = 0.0;
 		for (Date key : ts) {
 			for (AccelerometerReading reading: map.get(key)) {
 				numberOfReadings++;
 				 accumulator += reading.getACCVector();
 			}
 		}
 		result = accumulator / numberOfReadings; //average of all readings
 
 		return result;
 	}
 	
 	public boolean isGPSAvailable(){
 		boolean result = false;
		LocationManager loc_manager = (LocationManager) AndSensor.getContext().getSystemService(Context.LOCATION_SERVICE); 
		List<String> str = loc_manager.getProviders(true); 
		if(str.size() > 0) {
 			result = true;
 		}
 		return result;
 	}
 	
 	public boolean isNetworkAvailable(){
 		boolean result = false;
 		ConnectivityManager connectivity = (ConnectivityManager) AndSensor.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
 		if(networkInfo.isAvailable()){
 			result = true;
 		}
 		return result;				
 	}
 	
 	public boolean isWIFIAvailable(){
 		boolean result = false;
 		ConnectivityManager connectivity = (ConnectivityManager) AndSensor.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		if(wifiInfo.isAvailable()){
 			result = true;
 		}
 		return result;
 	}
 	
 	public boolean isBluetoothAvailable(){
 		boolean result = false;
 		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		if(bluetoothAdapter == null){
 			result = false;
 		}
 		else{
 			if(bluetoothAdapter.isEnabled()){
 				result = true;
 			}
 		}
 		return result;
 	}
 	
 	public double checkIfGPSChanging(int duration){
 		double result = 0.0;
 		
 		return result;
 	}
 	
 	public double checkIfWIFIChanging(int duration){
 		double result = 0.0;
 		
 		return result;
 	}
 	
 	public double checkIfNetworkChanging(int duration){
 		double result = 0.0;
 		
 		return result;
 	}
 	
 	public void setLocation(String location){
 		this.location = location;
 	}
 	
 	public String getLocation(){
 		return location;
 	}
 	
 	
 	
 }
