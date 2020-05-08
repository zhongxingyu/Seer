 package com.cellphones.mobilelunchmeet;
 
 import android.app.*;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.*;
 import com.google.android.maps.*;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.view.View;
 import android.content.res.Configuration;
 
 public class GPSActivity extends MapActivity {
     private MapController mapController;
     private TapControlledMapView mapView;
     private LocationManager locationManager;
     private Overlays itemizedoverlay;
     protected MyLocationOverlay myLocationOverlay;
     protected Location previous;
     protected int id;
     protected int match;
     private boolean locationCentered;
 
     private boolean logged_in;
     
     private Activity this_reference;
     private Button locateButton;
     private Button matchButton;
     
     private SharedPreferences settings;
     private SharedPreferences.Editor editor;
 
     private Handler handler;
     
     private NotificationManager nManager;
     private Notification notification;
     
     public static final String PREFS_NAME = "PrefsFile";
     public static final String TAG = "GPSActivity";
     private static final int NOTIFICATION_ID = 1;
     private static final int LOGIN_REQUEST_CODE = 1;
     private static final int SPLASH_REQUEST_CODE = 3;
     private static final int CHANGE_INFO_REQUEST_CODE = 4;
 
     public void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         this_reference = this;
         
         //inflater = getLayoutInflater();
         
         setContentView(R.layout.map);
         
         Intent i = new Intent(GPSActivity.this, SplashActivity.class);
         startActivityForResult(i, SPLASH_REQUEST_CODE);
         
         settings = getSharedPreferences(PREFS_NAME, 0);
         editor = settings.edit();
             
         mapView = (TapControlledMapView) findViewById(R.id.map);
         mapView.setBuiltInZoomControls(true);
         
         mapController = mapView.getController();
         mapController.setZoom(17);
        
         locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                 10, (LocationListener) new GeoUpdateHandler());
 
         previous = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         myLocationOverlay = new MyLocationOverlay(this, mapView);
         mapView.getOverlays().add(myLocationOverlay);
         myLocationOverlay.enableMyLocation();
         myLocationOverlay.runOnFirstFix(new Runnable() {
             //@Override
             public void run() {
                 Location me = myLocationOverlay.getLastFix();
                 mapController.animateTo(myLocationOverlay.getMyLocation());
                 Server.sendLocation(id, me.getLatitude(), me.getLongitude());
             }
         });
 
         Thread partnerUpdate = new Thread() {
             @Override
             public void run() {
                 while (true) {
                     GPSActivity.this.match = Server.partner(GPSActivity.this.id);
                     if (GPSActivity.this.match > 0) {//you were matched
                         GPSActivity.this.runOnUiThread(new Runnable() {
                             public void run() {
                                 final class CancelOnClickListener implements
                                         DialogInterface.OnClickListener {
                                     public void onClick(DialogInterface dialog, int which) {
                                         GPSActivity.this.rejectMatchNotification();
                                         Server.reject(GPSActivity.this.id, GPSActivity.this.match);
                                         GPSActivity.this.match = -1;
                                     }
                                 }
 
                                 final class OkOnClickListener implements
                                         DialogInterface.OnClickListener {
 
                                     public void onClick(DialogInterface dialog, int which) {
                                         Toast.makeText(GPSActivity.this, "You have accepted the match.", Toast.LENGTH_LONG).show();
                                         Server.accept(GPSActivity.this.id, GPSActivity.this.match);
                                         showDirections(GPSActivity.this.match);
                                     }
                                 }
 
                                 GPSActivity.this.matchNotification();
 
                                 AlertDialog.Builder builder = new AlertDialog.Builder(GPSActivity.this);
                                 builder.setMessage(Server.getName(GPSActivity.this.match) + " would like to eat lunch with you.");
                                 builder.setCancelable(true);
                                 builder.setPositiveButton("Accept", new OkOnClickListener());
                                 builder.setNegativeButton("Reject", new CancelOnClickListener());
                                 AlertDialog dialog = builder.create();
                                 dialog.show();
                             }
                         });
                         break;
                     }
                     try {
                         Thread.sleep(5000);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                 }
             }
         };
 
         partnerUpdate.start();
 
         initButtons();
         createButtonListeners();
         
         handler = new Handler();
 
         Runnable refreshTask = new Runnable()
         {
             public void run()
             {
                 handler.removeCallbacks(this);
 
                 mapView.invalidate();
                 mapView.postInvalidate();
 
                 handler.postDelayed(this, 1000);
 
             }
         };
         refreshTask.run();
     }
 
     private void showDirections(int id) {
         GeoPoint p = Server.getLocation(id);
         double end_lat = p.getLatitudeE6()/1000000.0;
         double end_long = p.getLongitudeE6()/1000000.0;
         
         if (previous != null) {
             double lat = previous.getLatitude();
             double lon = previous.getLongitude();
             Intent intent = new Intent(Intent.ACTION_VIEW,
                     Uri.parse("http://maps.google.com/maps?saddr=" +
                             lat + "," + lon +"&daddr=" +
                             end_lat + "," + end_long));
             startActivity(intent);
         }
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     public void waitForResponse(int otherid) {
         Integer [] stuff = new Integer[1];
         stuff[0] = new Integer(otherid);
         WaitForResponseTask task = new WaitForResponseTask(this);
         task.execute(stuff);
     }
 
     public class GeoUpdateHandler implements LocationListener {
 
         //@Override
         public void onLocationChanged(Location location) {
             double lat = location.getLatitude();
             double lng = location.getLongitude();
             Server.sendLocation(id, lat, lng);
             if(!location.equals(previous)) {
                 previous = location;
                 getLocations(Server.showLocations());
             }
         }
 
         //@Override
         public void onProviderDisabled(String provider) {
         }
 
         //@Override
         public void onProviderEnabled(String provider) {
         }
 
         //@Override
         public void onStatusChanged(String provider, int status, Bundle extras) {
         }
     }
 
     private void getLocations(JSONArray locations) {
         mapView.getOverlays().clear();
         try {
             for (int i = 0; i < locations.length(); i++) {
                 JSONObject item = (JSONObject) locations.get(i);
                 JSONObject location = (JSONObject) item.get("location");
                 int loc_id = location.getInt("user_id");
                 double lat = location.getDouble("lat");
                 double lon = location.getDouble("long");
                 Log.d(TAG, "##########" + lat + " " + (int)(1E6 * lat));
                 Log.d(TAG, "##########" + lon + " " + (int) (1E6 * lon));
                 GeoPoint p = new GeoPoint((int)(1E6 * lat), (int)(1E6 * lon));
                 OverlayItem overlayItem;
                 if (loc_id != id)
                     overlayItem = new OverlayItem(p, Server.getName(loc_id), "(" + lat +", " + lon + ")");
                 else {//your location
                     overlayItem = new OverlayItem(p, "You", "(" + lat + ", " + lon + ")");
                     if (!locationCentered) {
                         mapController.animateTo(p);
                         locationCentered = true;
                     }
                 }
                 itemizedoverlay.addOverlay(overlayItem);
 
                 if (itemizedoverlay.size() > 0) {
                     mapView.getOverlays().add(itemizedoverlay);
                 }
             }
         } catch (Exception e) {
         	System.out.println("Error in getLocations");
             e.printStackTrace();
         }
         mapView.postInvalidate();
     }
 
     private void createMarker() {
         GeoPoint p = mapView.getMapCenter();
         OverlayItem overlayitem = new OverlayItem(p, "", "");
         itemizedoverlay.addOverlay(overlayitem);
         if (itemizedoverlay.size() > 0) {
             mapView.getOverlays().add(itemizedoverlay);
         }
     }
 
     private void createMarker(Location location, String title, String snippet) {
         GeoPoint p = new GeoPoint((int)(1E6 * location.getLatitude()),(int)(1E6 * location.getLongitude()));
         OverlayItem overlayitem = new OverlayItem(p, title, snippet);
         itemizedoverlay.addOverlay(overlayitem);
         if (itemizedoverlay.size() > 0) {
             mapView.getOverlays().add(itemizedoverlay);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         myLocationOverlay.enableMyLocation();
         myLocationOverlay.enableCompass();
     }
 
     @Override
     protected void onPause() {
         super.onResume();
         myLocationOverlay.disableMyLocation();
         myLocationOverlay.disableCompass();
     }
     
     @Override
 	protected void onDestroy(){
 		super.onDestroy();
 		
 		Server.logout(settings.getString("login", "").toLowerCase());
 		
 		try{
 			nManager.cancelAll();
 		}catch(Exception e){
 			// if it doesn't work, program exited before nManager instanced, so who cares
 		}
 	}
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data){
     	super.onActivityResult(requestCode,  resultCode, data);
     	
     	if(requestCode == LOGIN_REQUEST_CODE){
     		switch(resultCode){
 	    	case 1: // user successfully logged in
 	    		logged_in = true;
 	    		mapView.setVisibility(View.VISIBLE);
 	    		createNotification();
 	    		
 	    		settings = getSharedPreferences(PREFS_NAME, 0);
 	            editor = settings.edit();
 	            id = settings.getInt("id", -1);
 	            
 	    		break;
 	    	case 5: // quit button pressed
 	    		super.finish();
 	    		break;
 	    	default:
 	    		break;
 	    	}	
     	}else if(requestCode == SPLASH_REQUEST_CODE){
     		//Log.d("GPSActivity.onActivityResult", "splash activity finished");
     		switch(resultCode){
     		case 1:
     			login();
     			break;
     		default:
     			break;
     		}
     	}else if(requestCode == CHANGE_INFO_REQUEST_CODE){
     		switch(resultCode){
     		case 1:
     			mapView.setVisibility(View.VISIBLE);
     			break;
     		case 5:
     			super.finish();
     			break;
     		default:
     			break;
     		}
     	}
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig){
     	super.onConfigurationChanged(newConfig);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	Intent i;
         switch (item.getItemId()) {
 			case R.id.quit_button:
 				super.finish();
 				nManager.cancelAll();
 				return true;
 			case R.id.about:
                 startActivity(new Intent(this, AboutActivity.class));
                 return true;
 			case R.id.logout_button:
 				// switch to login screen
 				//super.finish();
 				logged_in = false;
 				
 				Server.logout(settings.getString("login", "").toLowerCase());
 				nManager.cancelAll();
 				Toast.makeText(this, settings.getString("login", "") + " logged out", Toast.LENGTH_SHORT).show();
 				
 				editor.putInt("id", -1);
 				editor.commit();
 				
 				mapView.setVisibility(View.INVISIBLE);
 				i = new Intent(GPSActivity.this, LoginActivity.class);
 				startActivityForResult(i, LOGIN_REQUEST_CODE);
 				
 				return true;
 			case R.id.info_button:
 				mapView.setVisibility(View.INVISIBLE);
 				i = new Intent(GPSActivity.this, InfoActivity.class);
 				startActivityForResult(i, CHANGE_INFO_REQUEST_CODE);
 				
 				return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     protected void login(){
     	id = settings.getInt("id", -1);
         
         if(id < 0){
         	mapView.setVisibility(View.INVISIBLE);
         	logged_in = false;
         	
         	Intent i = new Intent(GPSActivity.this, LoginActivity.class);
 	    	startActivityForResult(i, LOGIN_REQUEST_CODE);
 	    	
         }else{
         	logged_in = true;
 
             boolean loggedin = Server.login(settings.getString("login", "").toLowerCase(), settings.getString("password", ""));
 
             if(!loggedin){
                 Toast.makeText(this_reference, "Login to server failed", Toast.LENGTH_LONG).show();
                 
                 mapView.setVisibility(View.INVISIBLE);
             	logged_in = false;
             	
             	Intent i = new Intent(GPSActivity.this, LoginActivity.class);
     	    	startActivityForResult(i, LOGIN_REQUEST_CODE);
     	    	return;
             }
         	
         	Drawable drawable = this.getResources().getDrawable(R.drawable.point);
             itemizedoverlay = new Overlays(this, drawable, id, myLocationOverlay, this, mapView);
 
             getLocations(Server.showLocations());
             
             createNotification();
         }
     }
     
     protected void createNotification(){
     	nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	
     	int icon = R.drawable.ic_launcher;
    	CharSequence tickerText = "Logged onto Lunchee";                                                                                                                                                                                                                                                                                                      ";
     	long when = System.currentTimeMillis();  	
     	notification = new Notification(icon, tickerText, when);
     
     	Context context = getApplicationContext();
     	CharSequence contentTitle = "Lunchee";
     	CharSequence contentText = "Looking for a lunch partner";
     	Intent notificationIntent = new Intent(this, GPSActivity.class);
     	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
     	
     	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
     	notification.flags |= Notification.FLAG_ONGOING_EVENT;
         
     	try{
     		nManager.notify(NOTIFICATION_ID, notification);
     	}catch(Exception e){
     		Log.e("GPSActivity.createNotification", "Error while creating notification: " + e.toString());
     		e.printStackTrace();
     	}
     	
     	notification.defaults |= Notification.DEFAULT_VIBRATE;
     }
     
     protected void matchNotification(){
     	Context context = getApplicationContext();
     	CharSequence contentTitle = "Mobile Lunch Meet";
     	CharSequence contentText = "Someone wants to have lunch with you!";
     	Intent notificationIntent = new Intent(this, GPSActivity.class);
     	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
     	
     	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
     	try{
     		nManager.notify(NOTIFICATION_ID, notification);
     	}catch(Exception e){
     		Log.e("GPSActivity.matchNotification", "Error while attempting match notification: " + e.toString());
     		e.printStackTrace();
     	}
     }
     
     protected void rejectMatchNotification(){
     	Context context = getApplicationContext();
     	CharSequence contentTitle = "Mobile Lunch Meet";
     	CharSequence contentText = "Looking for a lunch partner!";
     	Intent notificationIntent = new Intent(this, GPSActivity.class);
     	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
     	
     	notification.defaults |= Notification.DEFAULT_VIBRATE;
     	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
     	try{
     		nManager.notify(NOTIFICATION_ID, notification);
     	}catch(Exception e){
     		Log.e("GPSActivity.rejectMatchNotification", "Error while attempting reject match notification: " + e.toString());
     		e.printStackTrace();
     	}
     	notification.defaults |= Notification.DEFAULT_VIBRATE;
     }
     
     protected void initButtons(){
     	locateButton = (Button) findViewById(R.id.locate_button);
      	matchButton = (Button) findViewById(R.id.find_match_button);
     }
     
     protected void createButtonListeners(){
     	try{
     		locateButton.setOnClickListener(new View.OnClickListener(){
     			//@Override
     			public void onClick(View view){ 
     				// center map on self
     	             GeoPoint p = new GeoPoint((int)(1E6 * previous.getLatitude()), (int)(1E6 * previous.getLongitude()));
     	             mapController.animateTo(p);
     	             locationCentered = true;
     			}
     		});
     		matchButton.setOnClickListener(new View.OnClickListener(){
     			//@Override
     			public void onClick(View view){
                     int id = match();
                     Toast.makeText(GPSActivity.this, "" + id, Toast.LENGTH_LONG).show();
     			}
     		});
     	}catch(Exception e){
     		System.out.println("Error while creating login listeners");
     		Log.e("GPSActivity.creatButtonListeners", e.toString());
     		e.printStackTrace();
     	}
     }
 
     public int match() {
         JSONObject match = Server.match(id);
 
         try {
             Object o = match.get("location");
             if (o == null)
                 throw new JSONException("No match was found");
             JSONObject location = (JSONObject)o;
             int loc_id = location.getInt("user_id");
             return loc_id;
         } catch (JSONException e) {
             e.printStackTrace();
             Toast.makeText(this, "Brb something broke.", Toast.LENGTH_LONG).show();
         }
         return -1;
     }
 
     public int matchTo(int otherid) {
         JSONObject match = Server.match(id, otherid);
         Log.e(TAG, id + " maches to " + otherid);
 
         try {
             Object o = match.get("location");
             if (o == null)
                 throw new JSONException("No match was found");
             JSONObject location = (JSONObject)o;
             int loc_id = location.getInt("user_id");
             return loc_id;
         } catch (JSONException e) {
             e.printStackTrace();
             Toast.makeText(this, "Brb something broke.", Toast.LENGTH_LONG).show();
         }
         return -1;
     }
 
     private class WaitForResponseTask extends AsyncTask<Integer, Integer, Integer> {
 
         private GPSActivity activity;
         private ProgressDialog dialog;
 
         public WaitForResponseTask(GPSActivity activity) {
             this.activity = activity;
             dialog = new ProgressDialog(activity);
         }
 
         protected void onPreExecute() {
             this.dialog.setMessage("Waiting for response");
             this.dialog.show();
         }
 
         @Override
         protected Integer doInBackground(Integer... integers) {
             int id = integers[0];
             int result = 0;
             while(true) {
                 result = Server.partner(id);
                 if (result == -1 || result == -2)
                     break;
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
             return result;
         }
 
         protected void onProgressUpdate(Integer... progress) {
 
         }
 
         protected void onPostExecute(Integer result) {
             if (dialog.isShowing()) {
                 dialog.dismiss();
             }
 
             activity.match = result;
         }
     }
 }
