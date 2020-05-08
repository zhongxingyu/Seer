 /*
  *      This program is free software; you can redistribute it and/or modify
  *      it under the terms of the GNU General Public License as published by
  *      the Free Software Foundation; either version 2 of the License, or
  *      (at your option) any later version.
  *      
  *      This program is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU General Public License for more details.
  *      
  *      You should have received a copy of the GNU General Public License
  *      along with this program; if not, write to the Free Software
  *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  *      MA 02110-1301, USA.
  */
 package se.ltu.dicnix;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import ssc.SenseSmartCity;
 import ssc.Sensor;
 import ssc.SnowPressure;
 import android.app.Application;
 import android.content.ContentValues;
 import android.util.Log;
 import database.Snowdata;
 import database.Snowsensor;
 
 /**
  * @author Jim Gunnarsson, di98jgu
  *
  */
 public class DicNixApp extends Application {
    
    private static String TAG = DicNixApp.class.getSimpleName();
    
    /** Library Sense Smart City */
    private SenseSmartCity ssc = null;
    
    /** Local database table snow data */
    public Snowdata snowdata = null;
    /** Local database table snow sensor */
    public Snowsensor snowsensor = null;
 
    /**
     * Create a new application object for DicNix
     * 
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
       super.onCreate();
       
       Log.i(TAG, "Start of application");
       
       snowdata = new Snowdata(this);
       snowsensor = new Snowsensor(this);
       
 <<<<<<< HEAD
       openSSC();
       fetchSSC();
       
 =======
       snowdata.open();
       snowsensor.open();
       
       openSSC();
       fetchSSC();
 >>>>>>> master
    }
    
    public SenseSmartCity openSSC() {
       
       if (this.ssc == null) {
          
          // Need preferences to load username and password
          this.ssc = new SenseSmartCity("", "");
       }
       
       return this.ssc;
       
    }
    
    public synchronized void fetchSSC() {
       
       Log.d(TAG, "Fetching data from SSC");
 
       Map<Sensor, List<SnowPressure>> ssc_data = ssc.requestAll();
       Set<Map.Entry<Sensor, List<SnowPressure>>> set = ssc_data.entrySet();
       
       for (Map.Entry<Sensor, List<SnowPressure>> entry: set) {
          
          addSensor(entry.getKey());
          List<SnowPressure> list = entry.getValue();
          
          for (SnowPressure reading: list) {
          
             addSnowdata(reading);
             
          }
       }
       
       return;
       
    }
    
    public synchronized long addSensor(Sensor sensor) {
       
       ContentValues values = new ContentValues();
       
       values.put(Snowsensor.SERIAL, sensor.getSerial());
       values.put(Snowsensor.NAME, sensor.getName());
       values.put(Snowsensor.LOCATION, sensor.getLocation());
       values.put(Snowsensor.LATITUDE, sensor.getLatitude());
       values.put(Snowsensor.LONGITUDE, sensor.getLongitude());
       values.put(Snowsensor.TYPENAME, sensor.getTypeName());
       values.put(Snowsensor.DEPLOYEDSTATE, sensor.getDeployedState());
       values.put(Snowsensor.VISIBILITY, sensor.getVisibility());
       values.put(Snowsensor.INFO, sensor.getInfo());
       values.put(Snowsensor.DOMAIN, sensor.getDomain());
       values.put(Snowsensor.CREATED, sensor.getCreated().toString());
       values.put(Snowsensor.UPDATED, sensor.getUpdated().toString());
       
       return snowsensor.insert(values);
       
    }
    
    public synchronized long addSnowdata(SnowPressure reading) {
       
       ContentValues values = new ContentValues();
       
       values.put(Snowdata.SERIAL, reading.getSensorSerial());
       values.put(Snowdata.INFO, reading.getInfo());
       values.put(Snowdata.SHOVELED, reading.getShoveld());
       values.put(Snowdata.WEIGHT, reading.getWeight());
       values.put(Snowdata.DEPTH, reading.getDepth());
       values.put(Snowdata.TEMPERATURE, reading.getTemperature());
       values.put(Snowdata.HUMIDITY, reading.getHumidity());
       values.put(Snowdata.DATA_TIME, reading.getDataTime().toString());
       values.put(Snowdata.TIMESTAMP, reading.getDataTime().getEpoch());
       
       return snowdata.insert(values);
       
    }
 
 }
