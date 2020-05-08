 /**
  * Copyright (C) 2012 SINTEF <fabien@fleurey.com>
  *
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.gnu.org/licenses/lgpl-3.0.txt
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.sensapp.android.sensappdroid.clientsamples.sensorlogger;
 
 import android.app.Service;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.net.Uri;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import org.sensapp.android.sensappdroid.api.SensAppHelper;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Fabien Fleurey , Jonathan Nain
  * This class presents a minimalist service which use the SensApp android API to log the sensors.
  * It is started by the alarm manager and self stopped as soon every sensor has inserted a new measure.
  */
 public class SensorLoggerService extends Service implements SensorEventListener{
 
 	private static final String TAG = SensorLoggerService.class.getSimpleName();
 
     SensorManager sensorManager;
     static List<AbstractSensor> sensors;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		if (SensAppHelper.isSensAppInstalled(getApplicationContext())) {
             // Get all the sensors of the Android.
             sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
             initSensors();
             for(AbstractSensor as : sensors){
                 if(as.isListened()){
                     if(as.getClass() != AndroidSensor.class && (System.currentTimeMillis() - as.getLastMeasure() > as.getMeasureTime())) {
                         as.setData(this);
                         as.setMeasured();            //at least one measure
                         as.setFreshMeasure(true);    //a new measure has been made
                         insertMeasures();           //put it into local database
                         as.setLastMeasure();         //refresh time of the last measure
                         as.setFreshMeasure(false);   //no more last measure
                         if(allSensorsMeasured()) {
                             unsetSensorListening();
                         }
                     }
                 }
             }
             stopSelf();
 		} else {
             SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
             editor.putBoolean(SensorActivity.SERVICE_RUNNING, false).commit();
 			AlarmHelper.cancelAlarm(getApplicationContext());
 			Log.e(TAG, "SensApp has been uninstalled");
 		}
 
 	}
 
     private void initSensors(){
         if(noSensorListened())
             stopSelf();
 
         for(AbstractSensor s: sensors){
             try{
                 if(s.isListened())
                     sensorManager.registerListener(this, s.getSensor(), SensorManager.SENSOR_DELAY_UI);
                 else
                     sensorManager.unregisterListener(this, s.getSensor());
 
                 Uri sensorUri = s.registerInSensApp(getApplicationContext(), R.drawable.ic_launcher);
                 if (sensorUri == null) {
                     // The sensor is already registered.
                     Log.w(TAG, s.getName() + " is already registered");
                 } else {
                     // The sensor is newly inserted.
                     Log.i(TAG, s.getName() + " available at " + sensorUri);
                 }
             } catch (IllegalArgumentException e) {
                 Log.e(TAG, e.getMessage());
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public void onSensorChanged(SensorEvent sensorEvent) {
         AbstractSensor s = getSensorByType(sensorEvent.sensor.getType());
         if(s != null){
             if(s.isThreeDataSensor())
                 s.setData(this, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
             else
                 s.setData(this, sensorEvent.values[0]);
 
             if(s.getLastMeasure() == 0)
                 s.setLastMeasure();
 
             if((s.getLastMeasure() != 0) && (System.currentTimeMillis() - s.getLastMeasure() > s.getMeasureTime())){
                 s.setMeasured();
                 s.setFreshMeasure(true);
                 insertMeasures();
                 s.setLastMeasure();
                 s.setFreshMeasure(false);
                 if(allSensorsMeasured()) {
                     unsetSensorListening();
                 }
                 stopSelf();
             }
         }
     }
 
     private void unsetSensorListening(){
         sensorManager.unregisterListener(this);
     }
 
     private String listSensor() {
 
         StringBuffer sensorDesc = new StringBuffer();
         List<String> data = new ArrayList<String>();
         for (AbstractSensor AS : sensors) {
             sensorDesc.append("New sensor detected : \r\n");
             sensorDesc.append("\tName: " + AS.getSensor().getName() + "\r\n");
             sensorDesc.append("\tType: " + AS.getType() + "\r\n\r\n");
             data.add(sensorDesc.toString());
 
             sensorDesc.delete(0, sensorDesc.length()-1);
         }
 
         return data.toString();
     }
 
 	private void insertMeasures() {
 		for(AbstractSensor s: sensors){
             s.insertMeasure(this);
         }
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
     private String listData(){
         StringBuffer data = new StringBuffer();
         for(AbstractSensor s : sensors){
             data.append(s.getData());
         }
         return data.toString();
     }
 
     private AbstractSensor getSensorByType(int type){
         for(AbstractSensor s : sensors)
             if(s.getSensor().getType() == type)
                 return s;
         return null;
     }
 
     static public AbstractSensor getSensorByName(String name){
         for(AbstractSensor s: sensors){
             if(s.getName().equals(name))
                 return s;
         }
         return null;
     }
 
     static public void addSensor(AbstractSensor as){
         sensors.add(as);
     }
 
     static public void initSensorArray(){
         sensors = new ArrayList<AbstractSensor>();
     }
 
     private boolean allSensorsMeasured(){
         for(AbstractSensor s: sensors){
             if(!s.isMeasured() && s.isListened())
                 return false;
         }
         return true;
     }
 
     static public List<AbstractSensor> getSensors(){
         return sensors;
     }
 
     static public boolean noSensorListened(){
        for(AbstractSensor s : sensors){
            if(s.isListened())
                return false;
         }
         return true;
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int i) {
     }
 }
