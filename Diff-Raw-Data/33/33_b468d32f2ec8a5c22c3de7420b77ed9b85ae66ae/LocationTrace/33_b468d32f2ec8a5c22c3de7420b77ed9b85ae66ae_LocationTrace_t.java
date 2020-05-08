 package edu.ucla.cens.whatsnoisy.services;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 
 import android.app.Activity;
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 
import edu.ucla.cens.whatsnoisy.services.ILocationService;
import edu.ucla.cens.whatsnoisy.services.ILocationServiceCallback;
 import edu.ucla.cens.whatsnoisy.R;
 import edu.ucla.cens.whatsnoisy.Settings;
 import edu.ucla.cens.whatsnoisy.data.LocationDatabase;
 import edu.ucla.cens.whatsnoisy.tools.CustomHttpClient;
 
 import org.apache.http.params.BasicHttpParams;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 public class LocationTrace extends Service {
 
 	protected static final String TAG = "LocationTrace";
 	private LocationDatabase ldb;
 	private ServiceConnection conn;
 	private SharedPreferences preferences;
 	ILocationService mService;
 	private Location last_saved;
 
 	private final ILocationServiceCallback ILocationServiceCallback = new ILocationServiceCallback.Stub(){
 
 		public void locationUpdated(Location l) throws RemoteException {
 			//Log.d(TAG, "Starting Location updated");
 
 			if(last_saved == null) {
 				saveToDatabase(l);
 				return;
 			}
 			
 			//Log.d(TAG, "distance between points = " + l.distanceTo(last_saved) + " " + new Long(preferences.getString("min_update_distance", "1")));
 			
 			if(last_saved != null && l.distanceTo(last_saved) >= new Long(preferences.getString("min_update_distance", "1"))){
 				saveToDatabase(l);
 			}
 
 		}
 
 	};
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate() {
 		Log.d(TAG, "Service Started");
 		super.onCreate();
 		
 		preferences = this.getSharedPreferences(Settings.NAME, Activity.MODE_PRIVATE);
 		
 		//do not start location trace if setting is off
 		if(!preferences.getBoolean("toggle_location_trace", false))
 		{
 			stopSelf();
 		}
 
 		conn = new ServiceConnection() {  
 			public void onServiceConnected(ComponentName name, IBinder binder) {
 				mService = ILocationService.Stub.asInterface(binder);
 
 				try {
 					mService.registerCallback(ILocationServiceCallback);
 				} catch (RemoteException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			public void onServiceDisconnected(ComponentName name) {
 				mService = null;
 			}
 		};
 
 		Intent service = new Intent(this, LocationService.class);        
 		this.bindService(service, conn, BIND_AUTO_CREATE);     
 		
 		ldb = new LocationDatabase(this);
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.d(TAG, "Service Stopped");
 		super.onDestroy();        
 
 		try {
 			if(mService != null)
 				mService.unregisterCallback(ILocationServiceCallback);
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.unbindService(conn);
 	}
 
 	private void saveToDatabase(Location l) {
 		Log.d(TAG, "Saving location to database");
 		
 		ldb.open();
 		ldb.createPoint(l);
 		ldb.close();
 		
 		last_saved = l;
 	
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 }
