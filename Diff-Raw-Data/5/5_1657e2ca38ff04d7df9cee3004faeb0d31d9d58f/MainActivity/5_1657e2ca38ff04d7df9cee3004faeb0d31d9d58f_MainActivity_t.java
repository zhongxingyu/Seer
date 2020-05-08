 package edu.cmu.west.mysandbox;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.os.BatteryManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.telephony.CellInfo;
 //import android.telephony.NeighboringCellInfo;
 import android.telephony.TelephonyManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.content.Intent;
 import android.content.Context;
 import android.content.IntentFilter;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.lang.Float;
 import java.math.BigInteger;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.hardware.Sensor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpEntity;
 
 import android.os.AsyncTask;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 
 
 
 public class MainActivity extends Activity implements SensorEventListener {
 	
 	public TextView pressureTV, humidityTV, accelerometerTV, temperatureTV,
 					lightTV, longitudeTV, latitudeTV, altitudeTV, bearingTV, accuracyTV, callcountTV,
 					batteryTV, cellcountTV, wifiTV, deviceidTV, locidTV, serveruriTV, issendingTV,
 					sentcountTV, debugTV;
 	public EditText deviceidED, locidED, serveruriED;
 	public Button togglesendingB, updatesettingsB;
 	Intent batteryStatus;
 	IntentFilter batteryintent;
 
 	Context context;
 	SensorManager sensormanager;
 	List<Sensor> sensorlist;
 	Sensor humidityS, pressureS, accelerometerS, temperatureS, lightS;
 	BigInteger callcount;
 	List<Float> pressurevals, humidityvals, accelerometervals, temperaturevals, lightvals;
 	List<Double> gpsvals;
 	LocationListener gpslistener;
 	Boolean gps_is_enabled, packet_is_being_sent = false, is_sending = false;
 	LocationManager locman;
 	Integer batterylevel = -1;
 	TelephonyManager telephonymanager;
 	List<CellInfo> cellinfo;
 //	List<NeighboringCellInfo> cellinfo;
 	WifiManager wifimanager;
 	List<ScanResult> wifipoints;
 	String location_id, device_id, server_uri;
 	int sent_count;
 
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         batteryintent = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
         context = getBaseContext();
         pressureTV = (TextView)findViewById(R.id.pressureTV);
         humidityTV = (TextView)findViewById(R.id.humidityTV);
         accelerometerTV = (TextView)findViewById(R.id.accTV);
         temperatureTV = (TextView)findViewById(R.id.temperatureTV);
         lightTV = (TextView)findViewById(R.id.lightTV);
         latitudeTV = (TextView)findViewById(R.id.latitudeTV);
         longitudeTV = (TextView)findViewById(R.id.longitudeTV);
         altitudeTV = (TextView)findViewById(R.id.altitudeTV);
         bearingTV = (TextView)findViewById(R.id.bearingTV);
         accuracyTV = (TextView)findViewById(R.id.accuracyTV);
         callcountTV = (TextView)findViewById(R.id.callcountTV);
         batteryTV = (TextView)findViewById(R.id.batteryTV);
         cellcountTV = (TextView)findViewById(R.id.cellcountTV);
         wifiTV = (TextView)findViewById(R.id.wifiTV);
 
         deviceidTV = (TextView)findViewById(R.id.deviceidTV);
         locidTV = (TextView)findViewById(R.id.locationidTV);
         serveruriTV = (TextView)findViewById(R.id.serveruri2TV);
         issendingTV = (TextView)findViewById(R.id.issendingTV);
         sentcountTV = (TextView)findViewById(R.id.sentcountTV);
         debugTV = (TextView)findViewById(R.id.debugTV);
         
         
         
         
         deviceidED = (EditText)findViewById(R.id.deviceIDeditText);
         locidED = (EditText)findViewById(R.id.locationIDeditText);
         serveruriED = (EditText)findViewById(R.id.serveruriED);
 
         
         
         sensormanager = (SensorManager)getSystemService(SENSOR_SERVICE);
         pressureS = sensormanager.getDefaultSensor(Sensor.TYPE_PRESSURE);
         humidityS = sensormanager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
         accelerometerS = sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         temperatureS = sensormanager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
         lightS = sensormanager.getDefaultSensor(Sensor.TYPE_LIGHT);
 
         sensormanager.registerListener(this, humidityS, SensorManager.SENSOR_DELAY_NORMAL);
         sensormanager.registerListener(this, pressureS, SensorManager.SENSOR_DELAY_NORMAL);
         sensormanager.registerListener(this, accelerometerS, SensorManager.SENSOR_DELAY_NORMAL);
         sensormanager.registerListener(this, temperatureS, SensorManager.SENSOR_DELAY_NORMAL);
         sensormanager.registerListener(this, lightS, SensorManager.SENSOR_DELAY_NORMAL);
         gpslistener = new MyGPSListener();
         locman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpslistener);
         batteryStatus = context.registerReceiver(null, batteryintent);
         
         telephonymanager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
         wifimanager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
         
         togglesendingB = (Button)findViewById(R.id.togglesendingB);
         togglesendingB.setOnClickListener(new onSendToggleClicked());
         
         updatesettingsB = (Button)findViewById(R.id.updatesettingsB);
         updatesettingsB.setOnClickListener(new onReadSettingsClicked());
 
         callcount = BigInteger.valueOf(0);
         sent_count = 0;
         
         readSettings();
         
 
     }
  
     public void readSettings()
     {
     	location_id = locidED.getText().toString();
     	device_id = deviceidED.getText().toString();
     	server_uri = serveruriED.getText().toString();  	
     }
     
     class onSendToggleClicked implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			Button b = (Button)v;
 			if (is_sending) {
 				is_sending = false;
 				b.setText("Start sending");
 			}
 			else {
 				is_sending = true;
 				b.setText("Stop sending");
 			}
 		}
     	
     }
 
     class onReadSettingsClicked implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			readSettings();
 			updateAllGUIFields();
 		}
     	
     }
     
     
     
     public void updateAllGUIFields() {
 /*   	 
     	if (locidED.hasFocus()) {
     		locidED.clearFocus();
     	}
     	
     	if (deviceidED.hasFocus()) {
     		deviceidED.clearFocus();
     	}
     	
     	if (serveruriED.hasFocus()) {
     		serveruriED.clearFocus();
     	}
    	*/
     	callcountTV.setText("Updates received: " + callcount.toString());
     	
     	if (device_id != null) {
     		deviceidTV.setText("Device ID: " + device_id);
     	}
     	if (location_id != null) {
     		locidTV.setText("Location ID: " + location_id);
     	}    	
 
     	if (server_uri != null) {
     		serveruriTV.setText("Server URI: " + server_uri);
     	}
     	
     	if(is_sending) {
     		issendingTV.setText("Sending to server is on");
     	}
     	else {
     		issendingTV.setText("Sending to server is off");
     	}
     	
     	sentcountTV.setText("Packets sent: " + Integer.toString(sent_count));
     	
     	if (batterylevel >= 0) {
     		batteryTV.setText("Battery level: " + Integer.toString(batterylevel) + "%");
     	}
     	
     	if (pressurevals != null) {
 	    	String text = "Pressure: ";
 	    	for(Float val: pressurevals) {
 	    		text += Float.toString(val) + "; ";
 	    	}
 	    	pressureTV.setText(text);
     	}
     	if (humidityvals != null) {
 	    	String text = "Humidity: ";
 	    	for(Float val: humidityvals) {
 	    		text += Float.toString(val) + "; ";
 	    	}
 	    	humidityTV.setText(text);
     	}
 
     	if (accelerometervals != null) {
 	    	String text = "Accelerometer: ";
 	    	for(Float val: accelerometervals) {
 	    		text += Float.toString(val) + "; ";
 	    	}
 	    	accelerometerTV.setText(text);
     	}
  
     	if (temperaturevals != null) {
 	    	String text = "Temperature: ";
 	    	for(Float val: temperaturevals) {
 	    		text += Float.toString(val) + "; ";
 	    	}
 	    	temperatureTV.setText(text);
     	}
  
     	if (lightvals != null) {
 	    	String text = "Light: ";
 	    	for(Float val: lightvals) {
 	    		text += Float.toString(val) + "; ";
 	    	}
 	    	lightTV.setText(text);
     	}
     	if (gpsvals != null) {
     		latitudeTV.setText("Latitude: " + Double.toString(gpsvals.get(0)));
     		longitudeTV.setText("Longitude: " + Double.toString(gpsvals.get(1)));
     		altitudeTV.setText("Altitude: " + Double.toString(gpsvals.get(2)));
     		bearingTV.setText("Bearing: " + Double.toString(gpsvals.get(3)));
     		accuracyTV.setText("Accuracy: " + Double.toString(gpsvals.get(4)));
     	}
     	
     	if (cellinfo != null) {
     		String text = "";
     		text += "Cell count: " + cellinfo.size();
     		for (CellInfo cell: cellinfo) {
 //    		for (NeighboringCellInfo cell: cellinfo) {
     			text += "\n" + cell.toString();
     		}
     		cellcountTV.setText(text);
     	}
     	
     	
     	if (wifipoints != null) {
     		String text = "Wifi points count: " + wifipoints.size();
     		for (ScanResult scanres: wifipoints) {
     			text += scanres.toString();
     		}
     		wifiTV.setText(text);
     	}
     	
     }
     
     public void onSensorChanged(SensorEvent event) {
     	callcount = callcount.add(BigInteger.valueOf(1));
     	
     	if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
 			pressurevals = new ArrayList<Float>();
     		for(float val: event.values) {
     			pressurevals.add(Float.valueOf(val));
     		}
     	}
     	if (event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
 			humidityvals = new ArrayList<Float>();
     		for(float val: event.values) {
     			humidityvals.add(Float.valueOf(val));
     		} 			
     	}
 
     	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
 			accelerometervals = new ArrayList<Float>();
     		for(float val: event.values) {
     			accelerometervals.add(Float.valueOf(val));
     		}	
     	}
 
     	if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
 			temperaturevals = new ArrayList<Float>();
     		for(float val: event.values) {
     			temperaturevals.add(Float.valueOf(val));
     		}
     	}
 
     	if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
 			lightvals = new ArrayList<Float>();
     		for(float val: event.values) {
     			lightvals.add(Float.valueOf(val));
     		}
     	}
     	onSomethingChanged();
  
     }
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void onSomethingChanged(){
 //		getBatteryLevel();
 //		getNeighboringCellInfo();
 //		getWifiListAndScan();
 		if (is_sending) {
 			sendAllData();
 		}
 		updateAllGUIFields();
 		
 	}
 	
 	public void sendAllData(){
 		if (!packet_is_being_sent) {
 			String json_str = "{\"id\": \"" + device_id + "\"";
 			long unixTime = System.currentTimeMillis();
 			json_str += ", \"timestamp\": " + Long.toString(unixTime); 
 			if (temperaturevals != null) {
 				json_str += ", \"temp\": " + Float.toString(temperaturevals.get(0));
 			}
 			json_str += "}";
 			new JSONSender().execute(json_str);
 		}
 	}
 	
 	public void getNeighboringCellInfo() {
 		cellinfo = telephonymanager.getAllCellInfo();
 //		cellinfo = telephonymanager.getNeighboringCellInfo();
 	}
 	
 	public void getBatteryLevel() {
 		
 		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
 		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
 
 		float batteryPct = level / (float)scale;
 		
 		batterylevel = Integer.valueOf((int) (100*batteryPct));
 	}
 	
 	public void getWifiListAndScan() {
 		//new WifiGetter().execute();
 		wifimanager.startScan();
 		wifipoints = wifimanager.getScanResults();
 	}
 	
 	public class MyGPSListener implements LocationListener {
 
 		@Override
 		public void onLocationChanged(Location location) {
 	    	callcount = callcount.add(BigInteger.valueOf(1));
 			gpsvals = new ArrayList<Double>();
 			gpsvals.add(Double.valueOf(location.getLatitude()));
 			gpsvals.add(Double.valueOf(location.getLongitude()));	
 			gpsvals.add(Double.valueOf(location.getAltitude()));			
 			gpsvals.add(Double.valueOf(location.getBearing()));
 			gpsvals.add(Double.valueOf(location.getAccuracy()));
 			onSomethingChanged();
 		}
 
 		@Override
 		public void onProviderDisabled(String provider) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onProviderEnabled(String provider) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		
 	}
 	
 	public class WifiGetter extends AsyncTask<Object, Object, Object> {
 
 		@Override
 		protected Object doInBackground(Object... params) {
 			publishProgress();
 			wifimanager.startScan();
 			wifipoints = wifimanager.getScanResults();
 			return null;
 		}
 		
 		protected void onProgressUpdate(Object... params){
 			wifiTV.setText("Wifi scanning has started");
 		}
 		
 	}
 
 	public class JSONSender extends AsyncTask<String, Object, Boolean>{
 
 		String debug_string = "";
 		@Override
 		protected Boolean doInBackground(String... params) {
 			String json_str = params[0];
 			if (!packet_is_being_sent) {
 				publishProgress("Sending start");
 				publishProgress("Will send: " + json_str);
 				HttpClient client = new DefaultHttpClient();
 				publishProgress("client created");
 				HttpPost postMethod = new HttpPost(server_uri);
 				postMethod.addHeader("content-type", "application/json");
 				publishProgress("httpost created");
 				try {
 					postMethod.setEntity(new StringEntity(json_str, "UTF-8"));
 					publishProgress("entity set");
 
 					HttpResponse response = client.execute(postMethod);
 					publishProgress(response.toString());
 					publishProgress(response.getStatusLine().getReasonPhrase());
 					HttpEntity entity = response.getEntity();
 					InputStream istream = entity.getContent();
 					BufferedReader rd = new BufferedReader(new InputStreamReader(istream));
 					String res = "", line;
 					while( (line = rd.readLine()) != null) {
 						
 						res += line + "\n";
 
 					}
 					istream.close();
 					publishProgress(res);
 					return true;
 
 					
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					publishProgress(e.toString());
 				}
 			}
 				
 			
 			return false;
 		}
 		
 		@Override
 		protected void onProgressUpdate(Object... params) {
 			packet_is_being_sent = true;
 			String str = (String)params[0];
 			if (str.length() != 0) {
 				if(debug_string != "") debug_string += "\n";
 				 debug_string += str;
 			}
 			debugTV.setText(debug_string);
 		}
 		
 		@Override
 		protected void onPostExecute(Boolean param) {
 			packet_is_being_sent = false;
 			if(param) {
 				sent_count += 1;
 			}
 		}
 		
 	}
 	
 }
