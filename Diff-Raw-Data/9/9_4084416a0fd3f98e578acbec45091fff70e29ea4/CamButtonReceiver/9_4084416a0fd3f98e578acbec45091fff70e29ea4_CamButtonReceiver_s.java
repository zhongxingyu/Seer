 /*
     Cam Button Disabler - Disables Glass camera button when device is upside down.
     Copyright (C) 2013 James Betker
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.appliedanalog.glass.disablecam;
 
 import java.util.concurrent.Semaphore;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorListener;
 import android.hardware.SensorManager;
 import android.util.Log;
 
 public class CamButtonReceiver extends BroadcastReceiver implements SensorEventListener {
 	final String TAG = "CamButtonInterceptor";
 	
 	//The threshold for the y-axis gravity value after which we will intercept camera button calls
 	final float Y_AXIS_GRAVITY_THRESH = -8.f;
 	
 	//Special bypass intent extra
 	final String EXTRA_BYPASS_CAMBUTTON_FILTER = "CamButtonFilterBypass";
 	
 	SensorManager senseMan = null;
 	String lastAction;
 	Context lastContext;
 	Semaphore dataLock = new Semaphore(1);
 	int buttonsPressed = 0;
 
     @Override
     public void onReceive(Context context, Intent intent) {
     	if(intent.getExtras() != null &&
     	   intent.getExtras().getBoolean(EXTRA_BYPASS_CAMBUTTON_FILTER, false)){
     		return;
     	}
     	
     	try{
 	    	dataLock.acquire();
 	    	
 	    	lastAction = intent.getAction();
 	    	lastContext = context;
 	    	buttonsPressed = 1;
 	    	
 	    	//fetch the sensor manager for some quick sensing.
 	    	senseMan = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
 	    	Sensor sensor = senseMan.getDefaultSensor(Sensor.TYPE_GRAVITY);
 	    	senseMan.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
 	        abortBroadcast(); //Although this is not actually an ordered broadcast, calling this method actually cancels it nonetheless!
 	        				  //it's messy though; exceptions are thrown. Don't expect this to work forever.
     	}catch(Exception e){
     		e.printStackTrace();
     	}finally{
     		dataLock.release();
     	}
     }
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) { }
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		if(event.sensor.getType() == Sensor.TYPE_GRAVITY){
 			try{
 				dataLock.acquire();
 				
 				if(buttonsPressed <= 0){
 					return;
 				}
 				
 				if(event.values[1] < Y_AXIS_GRAVITY_THRESH){
 					buttonsPressed--; //thwart this press.
 			        Log.v(TAG, "Cam button intercepted and thwarted. Have a happy life!");
 				}else{
 					buttonsPressed--; //allow this one through.
 					Log.v(TAG, "This cam button press is fine, let it through..");
 					//create a "Special" camera button intent that will pass through the onReceive filter above.
 					Intent specIntent = new Intent(lastAction);
 					specIntent.putExtra(EXTRA_BYPASS_CAMBUTTON_FILTER, true);
 					lastContext.sendBroadcast(specIntent);
 				}
 				
 				//We don't need the grav sensor anymore.
 				senseMan.unregisterListener(this);
 			}catch(Exception e){
 				e.printStackTrace();
 			}finally{
 				dataLock.release();
 			}
 		}
 	}
 }
