 package edu.ucla.raddet.collector;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 //import android.app.AlarmManager;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.telephony.PhoneStateListener;
 import android.telephony.SignalStrength;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.widget.Toast;
 
 
 //Android Service which collects radiation information, as well as GPS information
 public class DataCollector extends Service{
 
 	//Service Features		
 	private TelephonyManager Tel;
 	private LocationManager locManager;
 	//private AlarmManager alarmManager;
 	private Handler handler;
 	private FileOutputStream fOut;
 	private OutputStreamWriter osw;
 
 	private double sig; // Valid values are 0-10
 	private Location bestLocation;
 	private Location lastLocation;
 	private boolean isFirstUpdate;
 	
 	public static final String TAG = "RadDet";
 	
     //Upload Features
 	URL connectURL;
     String params;
     String responseString;
     String fileName;
     byte[] dataToServer;  
 	
     String logFile = "temp.txt";
     String sendFile = "received.txt";
     
     //Current IP Address
     //String urlServer = "http://192.168.110.121/handle_upload.php";
     
     //Peter's Home IP Address
     String urlServer = "http://76.89.156.78/handle_upload.php";
     
 	private PhoneStateListener signalListener = new PhoneStateListener() {
         public void onSignalStrengthsChanged(SignalStrength signalStrength) {
         	double p = signalStrength.getCdmaDbm();
         	
         	if(p > -76)
         		sig = 10;
         	else if(p < -98)
         		sig = 0;
         	else
         	{
         		double power = java.lang.Math.pow(10,((p-30)/10));
         		double upper = -76;
         		double lower = -98;
         		//sig = ((power-(java.lang.Math.pow(10,-12)))/(9*java.lang.Math.pow(10,-12)))*10;
         		sig = 10*((power-(java.lang.Math.pow(10,(lower-30)/10)))/(java.lang.Math.pow(10,(upper-30)/10)-java.lang.Math.pow(10,(lower-30)/10)));
         	}
         	Log.d(TAG, "New Signal Strength: " + p + " dBm" + "& " + sig + " ratio");
         }
     }; 
 
 	private LocationListener locListener = new LocationListener() {
 		public void onLocationChanged(Location loc) {
 			if(isFirstUpdate) {
 				handler.sendEmptyMessageDelayed(0, 5000);	// Receive locations for five seconds
 				isFirstUpdate = false;
 			}
 			//Keep track of best location
 			//Having a location > no location
 			if (bestLocation == null)
 				bestLocation = loc;
 			//GPS Location > Network Location
 			else if (bestLocation.getProvider().equals(LocationManager.NETWORK_PROVIDER) &&
 					 loc.getProvider().equals(LocationManager.GPS_PROVIDER))
 				bestLocation = loc;
 			//More accuracy > Less accuracy
			else if (bestLocation.getProvider().equals(loc.getProvider())) {
				if (bestLocation.distanceTo(loc) > loc.getAccuracy())
					bestLocation = loc;
 			}
 			Log.d(TAG, "Best location is currently: " + bestLocation.getLatitude() + ", " + bestLocation.getLongitude() + " Type of network: " + bestLocation.getProvider());
 		}
 
 		// Functions declared for sake of interface satisfaction
 		public void onStatusChanged(String provider, int status, Bundle extras) {}
 		public void onProviderDisabled(String provider) {}
 		public void onProviderEnabled(String provider) {}
 	};
 	
 	public void onCreate() {
 		
 		//Set up GPS Signal Strength
         Tel = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
         Tel.listen(signalListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
 		
         //Set up location manager
 		locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		
 		//Set up alarm manager
 		//alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 		
 		//Set up handler
 		handler = new Handler(new Handler.Callback() {
 			public boolean handleMessage(Message msg) {
 				if (msg.what == 1) {
 					// Turns on the location providers
 					Toast.makeText(getApplicationContext(), "Gathering Location Data...", Toast.LENGTH_SHORT).show();
 					locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
 					locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
 					bestLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 					isFirstUpdate = true;
 				}
 				else {
 					// Check if we have location
 					if (bestLocation == null 
 							|| (lastLocation != null && (bestLocation.getLongitude() == lastLocation.getLongitude() 
 									&& bestLocation.getLatitude() == lastLocation.getLatitude())))
 					{
 						handler.sendEmptyMessageDelayed(0, 5000);
 						return true;
 					}
 					
 					// Turns off the location providers
 					locManager.removeUpdates(locListener);
 					handler.sendEmptyMessageDelayed(1, 55000);	// Wait 55 seconds before turning on again
 					
 					//Submit data to server
 					String s = bestLocation.getLatitude() + "," + bestLocation.getLongitude();
 					s += "," + sig + ",";
 					s += bestLocation.getTime() + "\n";
 					try {
 						osw.write(s);
 						osw.flush();
 						Log.d(TAG, "New location added to file");
 						Toast.makeText(getApplicationContext(), "New location added to file", Toast.LENGTH_SHORT).show();
 					} catch (IOException e) {
 						Log.e(TAG, "Problem with file writing");
 						e.printStackTrace();
 					}
 					Log.i(TAG, "(Latitude,Longitude,Signal,Time)" + s);
 					
 					Log.i(TAG, "Sending data to server...");
 					Toast.makeText(getApplicationContext(), "Sending data to server...", Toast.LENGTH_LONG).show();
 					File currentFile = new File(getApplicationContext().getFilesDir(),logFile);		
 					File bufferFile = new File(getApplicationContext().getFilesDir(), sendFile);
 					
 					// Rename current.txt file to a new file
 					if (copyFile(currentFile, bufferFile))
 						uploadFile(sendFile, urlServer);
 					
 					//Set last location
 					lastLocation = bestLocation;
 				}
 				return true;
 			}}
 		);
 		
 		try {
 			fOut = openFileOutput(logFile, MODE_WORLD_READABLE);
 			Log.d(TAG, "File Created");
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, "Could not open file output");
 			e.printStackTrace();
 		}
 		osw = new OutputStreamWriter(fOut);
 		
 		try {
 		    // Create a URL for the desired page
 		    URL syncURL = new URL("http://76.89.156.78/uploads/" + sendFile);
 
 		    // Read all the text returned by the server
 		    BufferedReader syncIn = new BufferedReader(new InputStreamReader(syncURL.openStream()));
 		    String str = syncIn.readLine();
 		    while (str != null) {
 		    	osw.write(str + '\n');
 		    	str = syncIn.readLine();
 		    }
 		    syncIn.close();
 		    osw.flush();
 			Toast.makeText(getApplicationContext(), "Existing Data File Sucessfully Retreived from Server", Toast.LENGTH_LONG).show();
 		} catch (MalformedURLException e) {
 			Log.e(TAG, "Bad Server File URL");
 		} catch (IOException e) {
 			Log.e(TAG, "Server File IOE");
 			try {
 				osw.write("Lat,Long,Signal,Time\n");
 				osw.flush();
 				Toast.makeText(getApplicationContext(), "Server Data File Does Not Exist. Creating New File.", Toast.LENGTH_LONG).show();
 			} catch (IOException e1) {
 				Log.e(TAG, "Could not open output file");
 				e1.printStackTrace();
 			}
 		}
 			
 		//Set up output file
 /*
 		boolean fileExists = false;;
 		for (String name : fileList()) {
 			if (name.equals(logFile)) {
 				fileExists = true;
 				break;
 			}
 		}
 		try {
 			fOut = openFileOutput(logFile, MODE_WORLD_READABLE);
 			Log.d(TAG, "File Created");
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, "Could not open file output");
 			e.printStackTrace();
 		}
 		
 		osw = new OutputStreamWriter(fOut);
 		
 		if (!fileExists) {
 			//Insert row header for CSV file
 			try {
 				osw.write("Lat,Long,Signal,Time\n");
 				osw.flush();
 				
 				//Set an alarm to send out the file
 				Calendar cal = Calendar.getInstance();	//Get current time
 				cal.set(Calendar.HOUR, 0);				//We only care about the actual date
 				cal.set(Calendar.MINUTE, 0);
 				cal.set(Calendar.SECOND, 0);
 				cal.add(Calendar.DAY_OF_MONTH, 1);		//Set calendar to midnight of tomorrow
 				
 				// Call the DataPrep receiver to send file at midnight of tomorrow
 				PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(this, DataPrep.class), 0);
 				alarmManager.set(AlarmManager.RTC, cal.getTimeInMillis(), pi);
 				Log.i(TAG, "Alarm set to send file on " + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH));
 			} catch (IOException e) {
 				Log.e(TAG, "Could not open output file");
 				e.printStackTrace();
 			}
 		}
 */
 		
 		//Initialize handler
 		handler.sendEmptyMessage(1);
 		Log.i(TAG, "DataCollector started");
 		Menu.started = true;	//Notify Activity that service has started
 	}
 	
 	public void onDestroy() {
 		locManager.removeUpdates(locListener);
 		Tel.listen(signalListener,PhoneStateListener.LISTEN_NONE);
 		try {
 			osw.close();
 		} catch (IOException e) {
 			Log.e(TAG, "Could not close file writer");
 			e.printStackTrace();
 		}
 		handler.removeMessages(1);
 		handler.removeMessages(0);
 		Log.i(TAG, "DataCollector stopped");
 		Menu.started = false;	//Notify Activity that service has stopped
 	}
 	
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 	
 	public void uploadFile(String filename, String url){
 		try {
 			FileInputStream fis = openFileInput(filename);
 			HttpFileUploader htfu = new HttpFileUploader(url,"noparamshere", filename);
 			htfu.doStart(fis, filename);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean copyFile(File src, File dst){
 		FileInputStream in = null;
 		FileOutputStream out = null;
 		
 		try {
 			in = new FileInputStream(src);
 			out = new FileOutputStream(dst);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 	    // Transfer bytes from in to out
 	    byte[] buf = new byte[1024];
 	    int len;
 	    try  {
 		    while ((len = in.read(buf)) > 0) {
 		        out.write(buf, 0, len);
 		    }
 		    in.close();
 		    out.close();
 		    return true;
 	    } catch (IOException e) {
 	    	e.printStackTrace();
 	    	return false;
 	    }
 	}
 	
 }
