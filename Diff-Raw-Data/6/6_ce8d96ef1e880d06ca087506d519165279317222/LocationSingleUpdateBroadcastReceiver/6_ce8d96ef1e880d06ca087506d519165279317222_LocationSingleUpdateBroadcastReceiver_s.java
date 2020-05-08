 package com.tikalk.wifilistener;
 
 import java.util.List;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.tikalk.tools.DBTool;
 import com.tikalk.tools.Defined;
 import com.tikalk.tools.PendingEvent;
 import com.tikalk.tools.Shared;
import com.tikalk.wifinotify.R;
 /**
  * NOTE ABOUT MULTIPLE CHECKIN
  * THIS PLUGGIN ASSUMES ONLY ONE PROJECT CAN BE CHECKED IN IN AT A TIME
  * IF THERE IS A REQUEST TO CHECKIN A PROJECT(PROJB) AND ANOTHER PROJECT(PROJA)
  * IS ALREADY CHECKED IN THEN THE REQUEST WILL FAIL AND THE ORIGIONAL PROJECT(PROJA) 
  * WILL REMAIN CHECKED IN
  */
 public class LocationSingleUpdateBroadcastReceiver extends BroadcastReceiver {	
 	/**
 	 * CONSTANTS
 	 */
 	public static final String TAG = LocationSingleUpdateBroadcastReceiver.class.getName();
 	//constant for singleupdate intent filter
 	public static final String SINGLE_LOCATION_UPDATE_ACTION = "com.radioactiveyak.places.SINGLE_LOCATION_UPDATE_ACTION";
 	//constant for max distance from origional location to perform login
 	public static final float MAX_DIST = 500.0f;
 	//constant for min distance for a logout if user is less than that distance away won't perform logout
 	public static final float MIN_DIST = 30.0f;
 	//string for intent filter on phonegap app
 	public static final String INTENT_FILTER_STRING = "com.tikal.location.tracker";
 	//KEYS FOR BUNDLE
 	//value that lets the phonegap activity know that it is being called form a notification
 	public static final String KEY_CALLER = "caller";
 
 
 	/**
 	 * MEMBERS
 	 */
 	private DBTool mDB;
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Shared.mRequestingLocation = false;;
 		Log.d("location test", "Location update fired");
 		//added bceause a previous call could have already unregistered the reciever
 		try{
 			context.unregisterReceiver(this);
 		}
 		catch(IllegalArgumentException il){
 			//sometimes update can be called twice in proseession so already removed receiver
 
 		}
 		//grab database
 		mDB = new DBTool(context);
 
 		String key = LocationManager.KEY_LOCATION_CHANGED;
 		Location location = (Location)intent.getExtras().get(key);
 		if(location != null){
 			Log.d("location test", "location recieved at " + System.currentTimeMillis() + 
 					" location is <" + printLocation(location) + ">");
 			//check queue and dequeue all items and perform actions
 			while(Shared.queueHasNext()){
 				PendingEvent pending = Shared.queueGetNext();
 				/*if(pending.getType() == PendingEvent.EVENT_ADD_POINT)
 	    			addSpot(pending.getSSID(), location);
 	    		else*/ if(pending.getType() == PendingEvent.EVENT_VERIFY_LOGIN){
 	    			verifyLogin(pending, location, context);
 	    		}
 	    		else if(pending.getType() == PendingEvent.EVENT_VERIFY_LOGOUT){
 	    			verifyLogout(pending, location, context);
 	    		}
 			}
 		}
 		mDB.close();
 	}
 	//verify user is logging into correct location
 	private void verifyLogin(PendingEvent pending, Location verifyLoc, Context context) {
 		//verify that spot is within a certain distance if so then post notification
 		Location origLoc = mDB.getLocation(pending.getBSSID(), pending.getProjectID());
 		float dist = origLoc.distanceTo(verifyLoc);
 		if(dist < MAX_DIST){
 			String projectID = pending.getProjectID();
 			String projectName = mDB.getProjectNameFromID(projectID);
 
 			//if project is set for auto-update then login/logout
 			if(mDB.getAutoUpdate(projectID)){
 				//if login  passes, then log timestamp
 				if(mDB.setLoggedIn(true, projectID)){
 					mDB.setLoggedInTimestamp(true, projectID);
 					Toast.makeText(context, "You just auto-logged into " + projectName, Toast.LENGTH_LONG).show();
 				}
 			}else{
 				int id = mDB.getNotificationID(projectName);
 				addNotification(true, projectName, id, context);
 			}
 		}
 	}
 
 	//verify user is far enough away(MIN_DIST) to logout
 	private void verifyLogout(PendingEvent pending, Location verifyLoc, Context context) {
 		//verify that spot is within a certain distance if so then post notification
 		Location origLoc = mDB.getLocation(pending.getBSSID(), pending.getProjectID());
 		float dist = origLoc.distanceTo(verifyLoc);
 		if(dist > MIN_DIST){
 			String projectID = pending.getProjectID();
 			String projectName = mDB.getProjectNameFromID(projectID);
 
 			//if project is set for auto-update then login/logout
 
 			if(mDB.getAutoUpdate(projectID)){
 				//if loggin in passes, then log timestamp
 				if(mDB.setLoggedIn(false, projectID)){
 					mDB.setLoggedInTimestamp(false, projectID);
 					//toast user that they just logged out
 					Toast.makeText(context, "You just auto-logged out of " + projectName, Toast.LENGTH_LONG).show();
 				}
 			}else{
 				int id = mDB.getNotificationID(projectName);
 				addNotification(false, projectName, id, context);
 			}
 		}
 	}
 	//
 	//	//add spot to database
 	//	private void addSpot(String ssid, Location location) {
 	//		mDB.addPoint(ssid, "", "" +  location.getLongitude(), "" + location.getLatitude());
 	//	}
 
 
 	//TODO used for testing
 	private String printLocation(Location location) {
 		String retVal = "";
 		if(!(location == null)){
 			retVal += "|long:" + location.getLongitude() + "|";
 			retVal += "|lat:" + location.getLatitude() + "|";
 			retVal += "|speed:" + location.getSpeed() + "|";
 			retVal += "|accuracy:" + location.getAccuracy() + "|";
 			retVal += "|alt:" + location.getAltitude() + "|";
 			retVal += "|bearing:" + location.getBearing()+ "|";
 			retVal += "|provider:" + location.getProvider()+ "|";
 			retVal += "|time:" + location.getTime() + "|";
 		}
 		return retVal;
 	}
 
 	//user has encountered or left a tagged spot so pop notificaton
 	private void addNotification(boolean loggingIn, String projectName,int notifyID,  Context context) {
 
 		/*Location spotLoc = mDB.getLocation(ssid);
 		Log.d("notify", "user is close to " + ssid + " at " + spotLoc.getLatitude() + "," + spotLoc.getLongitude());
 		 *///init login/logout variables
 		String loginString1 = "", loginString2 = "";
 		String title = "";
 		if(loggingIn){
 			loginString1 = "Checkin to ";
 			loginString2 = "?";
 			title = "Checkin";
 		}
 		else{
 			loginString1 = "Checkout of ";
 			loginString2 = "?";
 			title = "Checkout";
 		}
 		String ns = Context.NOTIFICATION_SERVICE;
 		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		int icon = R.drawable.notification_icon;
 		CharSequence tickerText = loginString1 + projectName + loginString2;
 		long when = System.currentTimeMillis();
 
 		Notification notification = new Notification(icon, tickerText, when);
 
 		CharSequence contentTitle = title;
 		CharSequence contentText = loginString1 + projectName + loginString2;
 		Intent notificationIntent = new Intent(INTENT_FILTER_STRING);
 		notificationIntent.putExtra(KEY_CALLER, "notification");
 		notificationIntent.putExtra(Defined.KEY_PROJECT_NAME, projectName);
 		notificationIntent.putExtra(Defined.KEY_LOGGING_IN, loggingIn);
 		notificationIntent.putExtra(Defined.KEY_PROJECT_ID, mDB.getID(projectName));
 
 		PendingIntent contentIntent = PendingIntent.getActivity(context,notifyID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 
 		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 
 		//add vibrate
 		long[] vibrate = {0,100,200,300, 0, 0, 100, 100, 0, 0, 0, 100, 100, 0, 0, 0, 0, 0, 300, 300, 300};
 		notification.vibrate = vibrate;
 
 		notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL;
 		mNotificationManager.notify(notifyID, notification);
 	}
 
 	/**
 	 * STATIC METHODS
 	 */
 
 	public static void startSingleUpdate(Context context){
 		//if we are already requesting the location then don't re-request
 		if(Shared.mRequestingLocation)
 			return;
 
 		Shared.mRequestingLocation = true;
 		//set listener
 		Log.d("location test", "starting single update at " + System.currentTimeMillis());
 		IntentFilter accurateLocation = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);
 		context.registerReceiver(new LocationSingleUpdateBroadcastReceiver(), accurateLocation);
 		//create pending intent to fire
 		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 		Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);  
 		PendingIntent singleUpatePI = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 		Criteria criteria = new Criteria();
 		String majorProvider = locationManager.getBestProvider(criteria, true);
 		Log.d("location test", "major provider " + majorProvider);
 		List<String> allProviders = locationManager.getAllProviders();
 		for(int i=0; i < allProviders.size();i++){
 			Log.d("location test", "provider " + i + ": " + allProviders.get(i));
 			//set listener
 			locationManager.requestSingleUpdate(allProviders.get(i), singleUpatePI);
 		}
 		Log.d("location test", "enabled...");
 		allProviders = locationManager.getProviders(true);
 		for(int i=0; i < allProviders.size();i++){
 			Log.d("location test", "enabled provider " + i + ": " + allProviders.get(i));
 
 		}
 	}
 }
