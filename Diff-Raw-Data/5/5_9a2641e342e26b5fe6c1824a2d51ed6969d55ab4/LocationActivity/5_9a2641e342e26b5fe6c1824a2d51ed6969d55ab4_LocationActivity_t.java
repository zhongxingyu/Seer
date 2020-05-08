 package com.timetogo.activity;
 
 import android.annotation.SuppressLint;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.res.Configuration;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.AudioManager;
 import android.media.ToneGenerator;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.text.format.DateFormat;
 import android.webkit.JavascriptInterface;
 import android.webkit.JsResult;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.widget.Toast;
 
 import com.google.inject.Inject;
 import com.timetogo.Contants;
 import com.timetogo.R;
 import com.timetogo.facade.ILocationController;
 import com.timetogo.model.LocationResult;
 import com.timetogo.model.RouteResult;
 import com.timetogo.model.RouteResultForLocations;
 import com.timetogo.service.ETAService;
 import com.timetogo.service.IETAService;
 import com.timetogo.view.ILocationView;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import de.akquinet.android.androlog.Log;
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 
 //@ContentView(R.layout.main)
 public class LocationActivity extends RoboActivity implements ILocationView, LocationListener {
 
 	@InjectView(R.id.webview)
 	WebView mywebview;
 
 	@Inject
 	ILocationController locationController;
 
 	@Inject
 	AlarmManager alarmManager;
 
 	LocationManager locationManager;
 
 	private PendingIntent pintent;
 	Intent intent;
 
 	// private long drivingTime;
 	// private Date lastUpdated;
 	// private String routeName;
 
 	private LocationResult fromLocation;
 	private LocationResult toLocation;
 
 	Handler h = new Handler();
 	DateFormat sdf = new DateFormat();
 	private IETAService service;
 
   private LocationControl locationControlTask;
   SuppliesLocation suppliesLocation;
 
 
 
 	private final ServiceConnection mConnection = new ServiceConnection() {
 
 		public void onServiceConnected(final ComponentName className,
 				final IBinder binder) {
 			service = ((ETAService.MyBinder) binder).getService();
 			Toast.makeText(LocationActivity.this, "Connected",
 					Toast.LENGTH_SHORT).show();
 			Log.i(Contants.TIME_TO_GO, "@@ got reference to service");
 		}
 
 		public void onServiceDisconnected(final ComponentName className) {
 		}
 	};
 
 	long maxDrivingTime;
 
 	@SuppressWarnings("unused")
 	private long initialDrivingTime;
 
 	private String provider;
 
     public LocationActivity() {
 	}
 
 	final class MyWebChromeClient extends WebChromeClient {
 		@Override
 		public boolean onJsAlert(final WebView view, final String url,
 				final String message, final JsResult result) {
 			Log.i(Contants.TIME_TO_GO, "alert: "+message);
 			result.confirm();
 			return true;
 		}
         public void onConsoleMessage(String message, int lineNumber, String sourceID) {
             Log.i(Contants.TIME_TO_GO+" FROM BROWSER: "+message + " -- From line "
                     + lineNumber + " of "
                     + sourceID);
         }
 
 	}
 
 	private class MyJavascriptInterface {
 		private final LocationActivity activity;
 
 		public MyJavascriptInterface(final LocationActivity activity) {
 			this.activity = activity;
 		}
 
 		@SuppressWarnings("unused")
         @JavascriptInterface
 		public void onGo(final String startLat,final String startLng, final String destinationLat, final String destinationLng)
         throws ClientProtocolException, IOException, JSONException,
 				URISyntaxException {
 			Log.i(Contants.TIME_TO_GO, "onGO [" + startLat+","+startLng + "]->["+ destinationLat+","+destinationLng+ "]");
             LocationResult startLocation = new LocationResult("", startLat, startLng);
             LocationResult destinationLocation = new LocationResult("", destinationLat, destinationLng);
 			activity.onGoBtnClicked(startLocation,destinationLocation);
 		}
 
 		@SuppressWarnings("unused")
         @JavascriptInterface
 		public void onNotify(final long mdt) {
 			Log.i(Contants.TIME_TO_GO,
 					"@@ NotifyME button was clicked  with maxDrivingTimeInMin="
 							+ mdt);
 			maxDrivingTime = mdt;
 			service.setParameters(fromLocation, toLocation, maxDrivingTime);
 		}
     @JavascriptInterface
         public String getLocation() {
           Log.i(Contants.TIME_TO_GO, "@@ getLocation");
       final LocationActivity act = this.activity;
       h.postDelayed(new Runnable() {
         public void run() {
 
           suppliesLocation.getLocation();
           locationControlTask = new LocationControl();
           locationControlTask.execute(act);
 
         }
       }, 100);
           return "";
         }
 	}
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         Log.i(Contants.TIME_TO_GO, "@@ HERE AT ON CONFIGURATION CHANGED "+newConfig.orientation);
     }
 
 	@SuppressLint("SetJavaScriptEnabled")
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		Log.i(Contants.TIME_TO_GO, "@@ onCreate");
 		super.onCreate(savedInstanceState);
   		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 
 		setContentView(R.layout.main);
     suppliesLocation = new SuppliesLocation(locationManager);
 		mywebview.loadUrl("file:///android_asset/angularApp/timeToGo.html?_="+System.currentTimeMillis());
 //        mywebview.loadUrl("http://mavry.github.io/angularApp/timeToGo.html?_="+System.currentTimeMillis());
 
         mywebview.getSettings().setJavaScriptEnabled(true);
 		mywebview.addJavascriptInterface(new MyJavascriptInterface(this), "androidInterface");
     mywebview.getSettings().setAllowUniversalAccessFromFileURLs(true);
     mywebview.getSettings().setDomStorageEnabled(true);
    mywebview.getSettings().setDatabasePath("/data/data/com.timetogo/databases/");
 

    mywebview.setWebChromeClient(new MyWebChromeClient());
 
         h.postDelayed(new Runnable() {
             public void run() {
                 invokeJS("onCreate");
             }
         }, 1000);
 
 		intent = new Intent(this, ETAService.class);
 		pintent = PendingIntent.getService(this, 0, intent, 0);
 
 		locationController.setView(this);
 //		alarmManager.cancel(pintent);
 //		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
 //				System.currentTimeMillis() + 1000, 60 * 1000, pintent);
 		doBindService();
         obtainProvider();
 		
 		h.postDelayed(new Runnable() {
 			public void run() {
 				if (service != null && maxDrivingTime > 0) {
 					updateUI(service.getDrivingTime(), service.getRouteName(),
 							service.getLastExecutionDate());
 				}
 				h.postDelayed(this, 20 * 1000);
 			}
 
 		}, 60 * 1000);
 
 
 	}
 
 	// private void updateDrivingTimeFeilds(final long drivingTime, final String
 	// routeName, final Date lastExecutionDate) {
 	// this.drivingTime = drivingTime;
 	// this.routeName = routeName;
 	// lastUpdated = lastExecutionDate;
 	// }
 
 	private void obtainProvider() {
         Criteria criteria = new Criteria();
 //        criteria.setAccuracy(Criteria.ACCURACY_FINE | Criteria.ACCURACY_COARSE );
         criteria.setAccuracy(Criteria.NO_REQUIREMENT);
         criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
 
 		provider = locationManager.getBestProvider(criteria, true);
 		Log.i(Contants.TIME_TO_GO, "provider = " + provider);
 	}
 
 	private void printLocation(Location location) {
     if (location==null) {
       Log.w(Contants.TIME_TO_GO, "got Location: null");
     } else {
 		  String lat = String.valueOf((location.getLatitude()));
 		  String lng = String.valueOf(location.getLongitude());
 		  Log.i(Contants.TIME_TO_GO, "got Location: " + lat + "," + lng);
     }
 	}
 
 	void doBindService() {
 		final boolean bind = bindService(new Intent(this, ETAService.class),
                 mConnection, Context.BIND_AUTO_CREATE);
 		Log.i(Contants.TIME_TO_GO, "bind success = " + bind);
 	}
 
 	public void onGoBtnClicked(LocationResult fromLocation, LocationResult toLocation)
 			throws ClientProtocolException, IOException, JSONException,
 			URISyntaxException {
 		locationController.retrievesGeoLocations(fromLocation, toLocation);
 	}
 
 
 	public void onTimeToGo() {
 		final ToneGenerator tg = new ToneGenerator(
 				AudioManager.STREAM_NOTIFICATION, 100);
 		tg.startTone(ToneGenerator.TONE_PROP_BEEP);
 	}
 
 	public void onGeoLocations(
 			final RouteResultForLocations routeResultForLocations) {
     Log.i(Contants.TIME_TO_GO, "@@ in LocationActivity.onGeoLocations() routeResultForLocations="+ routeResultForLocations);
     final RouteResult routeResult = routeResultForLocations.getRouteResult();
 		Log.i(Contants.TIME_TO_GO, "@@ in LocationActivity.onGeoLocations() routeResult="+ routeResult);
 		Log.i(Contants.TIME_TO_GO, String.format(Locale.getDefault(), "%d min via %s", routeResult.getDrivingTimeInMinutes(), routeResult.getRouteName()));
 		updateRequestField(routeResultForLocations, routeResult);
 		//
 		h.post(new Runnable() {
 
 			public void run() {
 				invokeJS("onDrivingTime",
                 String.valueOf(routeResult.getDrivingTimeInMinutes()), "'" + routeResult.getRouteName() + "'");
 			}
 
 		});
 
 	}
 
 	@Override
 	protected void onNewIntent(final Intent intent) {
 		Log.i(Contants.TIME_TO_GO, "onNewIntent");
 		if (intent.getExtras() == null) {
 			Log.i(Contants.TIME_TO_GO, "no extras");
 		} else {
 			final boolean timeToGo = intent.getExtras().getBoolean("timeToGo");
 			Log.i(Contants.TIME_TO_GO, "timeToGo = " + timeToGo);
 			invokeJS("onTimeToGo", String.valueOf(service.getDrivingTime())
 					+ " min", service.getRouteName(),
 					formatUpdateTime(service.getLastExecutionDate()));
 		}
 		super.onNewIntent(intent);
 
 	}
 
 	private void updateRequestField(
 			final RouteResultForLocations routeResultForLocations,
 			final RouteResult routeResult) {
 		initialDrivingTime = routeResult.getDrivingTimeInMinutes();
 		fromLocation = routeResultForLocations.getFrom();
 		toLocation = routeResultForLocations.getTo();
 	}
 
 	private void updateUI(final long drivingTime, final String routeName,
 			final Date lastUpdated) {
 		final String text = formatUpdateTime(lastUpdated);
 		invokeJS("onUpdate", String.valueOf(drivingTime), routeName, text);
 	}
 
 	private void invokeJS(final String methodName, final String... args) {
 		final StringBuilder sb = new StringBuilder("javascript:");
         sb.append("window.Application.");
         sb.append(methodName + "(");
 		boolean firstTime = true;
 		for (final String arg : args) {
 			if (!firstTime) {
 				sb.append(",");
 			}
 //			sb.append("'");
 			sb.append(arg);
 //			sb.append("'");
 			firstTime = false;
 		}
 		sb.append(");");
         if (methodName.equals("onCurrentLocation") || methodName.equals("onDrivingTime") ){
             Log.i(Contants.TIME_TO_GO, "@@ "+methodName+ " "+sb.toString());
             mywebview.loadUrl(sb.toString());
         }
 	}
 
 	private String formatUpdateTime(final Date d) {
 		final Date now = new Date();
 		final int minDiff = now.getMinutes() - d.getMinutes();
 		if (minDiff <= 0) {
 			return "now";
 		}
 		if (minDiff <= 1) {
 			return "1 min ago";
 		}
 		if (minDiff <= 10) {
 			return minDiff + " minutes ago";
 		}
 		return DateFormat.format("kk:mm", d).toString();
 	}
     /* Request updates at startup */
     @Override
     protected void onResume() {
         super.onResume();
         Log.i(Contants.TIME_TO_GO, "@@ onResume");
         invokeJS("onResume");
     }
 
     /* Remove the locationlistener updates when Activity is paused */
     @Override
     protected void onPause() {
         super.onPause();
         Log.i(Contants.TIME_TO_GO, "@@ onPause");
         locationManager.removeUpdates(this);
         invokeJS("onPause");
     }
 
     public void onLocationChanged(Location location){
         Log.i(Contants.TIME_TO_GO,"onLocationChanged");
         if (location==null)
         {
             Log.i(Contants.TIME_TO_GO,"null");
             return;
         }
         updateLocation(location);
     }
 
     private void updateLocation(final Location location) {
         printLocation(location);
         h.post(
                 new Runnable() {
                     public void run() {
                         if (location == null) {
                             invokeJS("onCurrentLocation");
                         } else {
                             invokeJS("onCurrentLocation", getLocationAsJson(location), "'"+provider+"'");
                         }
                     }
                 });
     }
 
     protected void onStart () {
         super.onStart();
         Log.i(Contants.TIME_TO_GO, "on Start");
         invokeJS("onStart");
 
     }
 
   protected void onStop () {
     super.onStop();
     Log.i(Contants.TIME_TO_GO, "on Stop");
     suppliesLocation.stop();
   }
     public String getLocationAsJson(Location location){
         return String.format("{\"lat\": \"%s\", \"lng\":\"%s\", \"accuracy\":\"%s\", \"provider\":\"%s\" }", location.getLatitude() , location.getLongitude(), location.getAccuracy(), location.getProvider());
     }
 
     public void onStatusChanged(java.lang.String provider, int status, android.os.Bundle extras){
         Log.i(Contants.TIME_TO_GO,"provider "+provider+" status is now set to "+status);
     }
     public void onProviderEnabled(java.lang.String provider){
         Log.i(Contants.TIME_TO_GO,"provider "+provider+" was enabled");
     }
     public void onProviderDisabled(java.lang.String provider){
         Log.i(Contants.TIME_TO_GO,"provider "+provider+" was disabled");
     }
 
 
   private class LocationControl extends AsyncTask<Context, Void, Void>
   {
     private final ProgressDialog dialog = new ProgressDialog(LocationActivity.this);
 
     protected void onPreExecute()
     {
 
     }
 
     protected Void doInBackground(Context... params)
     {
       //Wait 10 seconds to see if we can get a location from either network or GPS, otherwise stop
       Log.i(Contants.TIME_TO_GO, "@@ **** background checking... ");
 
       Long t0 = Calendar.getInstance().getTimeInMillis();
       while (!suppliesLocation.canUseLocation() && stillHaveTimeToWaitUntilGettingAGoodLocation(t0)) {
         try {
           Log.i(Contants.TIME_TO_GO, "@@ **** no good enough location");
 
           Thread.sleep(1000);
         } catch (InterruptedException e) {
           e.printStackTrace();
         }
       }
       return null;
     }
 
     private boolean stillHaveTimeToWaitUntilGettingAGoodLocation(Long t) {
       return Calendar.getInstance().getTimeInMillis() - t < 200000;
     }
 
     protected void onPostExecute(final Void unused)
     {
       Location loc = suppliesLocation.getBestLocation();
       SuppliesLocation.LocationQuality q = suppliesLocation.getQuality();
       int counter = suppliesLocation.getCounter();
       Log.i(Contants.TIME_TO_GO, "@@ **** got location with q = " + q + " counter = " + counter + " *** " + getLocationAsJson(loc));
       if (suppliesLocation.canUseLocation())
       {
         Log.i(Contants.TIME_TO_GO, "@@ It is a great location");
       }
       else
       {
         Log.w(Contants.TIME_TO_GO, "@@ **** NOT good enough location *** ");
       }
       suppliesLocation.stop();
       updateLocation(loc);
     }
   }
 
 
 }
