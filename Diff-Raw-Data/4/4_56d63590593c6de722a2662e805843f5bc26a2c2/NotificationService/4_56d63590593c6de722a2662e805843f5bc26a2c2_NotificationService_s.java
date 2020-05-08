 package com.appchallenge.android;
 
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.conn.ConnectTimeoutException;
 
 import com.google.android.gms.maps.model.LatLng;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 
 
 public class NotificationService extends Service implements LocationListener {
 	/**
 	 * Storage for the events we receive from the backend.
 	 */
 	ArrayList<Event> latestEvents;
 
     /**
      * Location of the user for this service instance.
      * This value is kept for passing through intents from notifications.
      */
     LatLng userLocation;
 
 	/**
      * Provides access to our local sqlite database.
      */
     private LocalDatabase localDB;
 
 	@Override
 	public void onCreate() {
 	    super.onCreate();
 	    Log.d("NotificationService.onCreate", "NotificationService starting.");
 	}
 
 	@Override
     public int onStartCommand(Intent intent, int flags, int startId) {
 		Log.d("NotificationService.onStartCommand", "NotificationService received command.");
 
 		if (!this.isOnline()) {
 			Log.d("NotificationService.onStartCommand", "No internet connection for NotificationService.");
 			stopSelf();
 			return Service.START_NOT_STICKY;
 		}
 
 		LocationFinder locationFinder = new LocationFinder();
         boolean sourcesExist = locationFinder.getLocation(this);
         if (!sourcesExist) {
         	Log.d("NotificationService.onStartCommand", "NotificationService had no location sources!");
         	stopSelf();
         	return Service.START_NOT_STICKY;
         }
 
         return Service.START_STICKY;
     }
 
 	@Override
 	public void onDestroy() {
 		Log.d("NotificationService.onDestroy", "NotificationService stopping.");
 	    super.onDestroy();
 	    if (localDB != null)
 	    	localDB.close();
 	    
 	}
 
 	// We do not need to bind anything to this service.
 	public IBinder onBind(Intent intent) { return null; }
 
 	/**
 	 * Creates our service notifications from a list of new events.
 	 * @param newEvents Events the user has not seen before and should be notified about.
 	 * @return A builder for our notification that can be given to the Android system.
 	 */
 	private NotificationCompat.Builder buildNotification(ArrayList<Event> newEvents) {
 		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
 		mBuilder.setSmallIcon(R.drawable.logo);
 		String title = getResources().getQuantityString(R.plurals.new_events_nearby, newEvents.size(), newEvents.size());
 		mBuilder.setContentTitle(title);
 		mBuilder.setContentText(newEvents.get(0).getTitle());
 		mBuilder.setAutoCancel(true);
 
 		// Enable vibrate if the user requested it.
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		if (prefs.getBoolean("notificationVibrate", false))
 		    mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
 
 		// Create an explicit intent for an Activity in your app
         Intent resultIntent;
 
 		// Create a "big view" style that shows the names of the first few events when expanded (>4.1 only)
 		if (newEvents.size() > 1) {
 		    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
 		    inboxStyle.setBigContentTitle(title);
 		    for (int i = 0; i < newEvents.size(); ++i)
                 inboxStyle.addLine(newEvents.get(i).getTitle());
 		    mBuilder.setStyle(inboxStyle);
 
 		    // With multiple events, send the user to the map.
 		    resultIntent = new Intent(this, EventViewer.class);
 		}
 		else {
 			// A single event notification should send the user to the event details page directly.
 			resultIntent = new Intent(this, EventDetails.class);
 			resultIntent.putExtra("event", newEvents.get(0));
 			resultIntent.putExtra("userLocation", this.userLocation);
 		}
 
 		// The stack builder object will contain an artificial back stack for the started Activity.
 		// This ensures that navigating backward from the Activity leads out of your application to the Home screen.
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 
 		// Adds the back stack for the Intent (but not the Intent itself)
 		stackBuilder.addParentStack(newEvents.size() > 1 ? EventViewer.class : EventDetails.class);
 		// Adds the Intent that starts the Activity to the top of the stack
 		stackBuilder.addNextIntent(resultIntent);
 		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
 		mBuilder.setContentIntent(resultPendingIntent);
 		return mBuilder;
 	}
 
 	// Method implementations for LocationListener.
 	@Override
 	public void onLocationChanged(Location loc) {
 		Log.d("NotificationService.onLocationChanged", "NotificationService received location information.");
 
 		this.userLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
 
 		// We are in a Service so this blocking call should not cause UI responsiveness issues.
 		// If it does, a solution might be listening for the AsyncTask to finish.
 		getEventsNearLocationAPICaller caller = new getEventsNearLocationAPICaller();
 		try {
 			latestEvents = caller.execute(this.userLocation).get();
 		}
        catch (InterruptedException e) { e.printStackTrace(); stopSelf(); }
        catch (ExecutionException e) { e.printStackTrace(); stopSelf(); }
 
 		if (latestEvents == null || latestEvents.size() == 0) {
 			stopSelf();
 			return;
 		}
 
 		Log.d("NotificationService.onLocationChanged", "NotificationService received new event information.");
 		
 		if (localDB == null)
 			localDB = new LocalDatabase(this);
 
 		// Inform the local cache of these new events.
 		ArrayList<Event> newEvents = localDB.updateLocalEventCache(latestEvents);
 		Log.d("NotificationService.onLocationChanged", "We found " + newEvents.size() + " new events.");
 		
 		// If we have received some events we had not previously cached, notify the user as necessary.
 		if (newEvents.size() > 0) {
 			NotificationCompat.Builder builder = this.buildNotification(newEvents);
 		    NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
 		    mNotificationManager.notify(564534231, builder.build());
 		}
 
 		stopSelf();
 	}
 
 	public void onProviderDisabled(String arg0) {}
 	public void onProviderEnabled(String provider) {}
 	public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 	/**
 	 * Determines if the device has internet connectivity.
 	 * @return Whether a data connection is available.
 	 */
 	private boolean isOnline() {
         ConnectivityManager connectivityManager =
           (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
 
         return (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable());
     }
 
 	/**
 	 * Performs an asynchronous API call to find nearby events.
 	 */
 	private class getEventsNearLocationAPICaller extends AsyncTask<LatLng, Void, ArrayList<Event>> {
 
 		protected void onPreExecute() {}
 		protected void onPostExecute(ArrayList<Event> result) {}
 
 		protected ArrayList<Event> doInBackground(LatLng... location) {
 			// Perform the network call to retreive nearby events.
 			try {
 				return APICalls.getEventsNearLocation(location[0]);
 			} catch (ConnectTimeoutException cte) {
 				Log.e("getEventsNearLocationAPICaller", "Connection could not be established. Please try again later!");
 				cte.printStackTrace();
 			} catch (SocketTimeoutException ste) {
 				Log.e("getEventsNearLocationAPICaller",  "Issue receiving server data. Please try again later!");
 				ste.printStackTrace();
 			}
 			return null;
 		}
 	}
 }
