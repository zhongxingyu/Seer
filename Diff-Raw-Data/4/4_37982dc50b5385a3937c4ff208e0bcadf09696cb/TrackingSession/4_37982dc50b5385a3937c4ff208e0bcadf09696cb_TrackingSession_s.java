 package com.delin.speedlogger;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import android.location.*;
 import android.os.Bundle;
 import android.content.Context;
 
 public class TrackingSession implements LocationListener {
 	enum TrackingState { WARMUP, READY, TRACKING, ERROR, DONE, IDLE }
 	enum WarmupState { WAITING_FIX, HIGH_SPEED }
 	
 	final static String UNCERT_LOC = "uncertainty";
 	final static int MAX_LOC_COUNT = 300; // 300/60 fixes per minute = 5 min
 	final static float HOR_ACCURACY = 20.f; // horizontal accuracy, in meters
 	final static float SPEED_THRESHOLD = 6.f; // speed threshold to detect start, in kmph
 	
 	//--- session measured parameters
 	float mMaxSpeed = 0.f;
 	List<Location> mLocList = new ArrayList<Location>();
 	
 	Location mBaseLocation = null; // last location
 	Location mReadyLoc = null;
 	
 	final Context mContext;
 	LocationManager mLocationManager = null;
 	boolean mEnabled = false; // gps receiver status, useless
 	boolean mWriteGPX = true;
 	GPXSerializer mGpxLog = null;
 	TrackingState mState = TrackingState.IDLE;
 	WarmupState mWarmupState = WarmupState.WAITING_FIX;
 	Vector<TrackingSessionListener> mListeners = null;
 	
 	public TrackingSession(Context Context) {
 		mContext = Context;
 		// Acquire a reference to the system Location Manager
 		mLocationManager = (LocationManager) mContext.getSystemService(android.content.Context.LOCATION_SERVICE);
 		mListeners = new Vector<TrackingSessionListener>();
 		StartService();
 	}
 	
 	protected void finalize () {
 		mListeners = null;
 	}
 	
 	public void AddListener(TrackingSessionListener newListener) {
 		mListeners.add(newListener);
 		// notify about current state!
 		switch (mState)
 		{
 		case WARMUP:
 			newListener.onSessionWarmingUp(mWarmupState);
 			break;
 		case READY:
 			newListener.onSessionReady();
 			break;
 		case TRACKING:
 			newListener.onSessionStart();
 			break;
 		default:
 			break;
 		}
 	}
 	
 	public void RemoveListener(TrackingSessionListener obsoleteListener) {
 		mListeners.remove(obsoleteListener);
 	}
 	
 	public Location GetLastLocation() {
 		return mBaseLocation;
 	}
 	
 	public void StartService() {
 		if (mState==TrackingState.IDLE) { // start only from IDLE state
 			// Register the listener with the Location Manager to receive location updates
 			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this); // TODO: remove
 			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
 			ResetSessionValues();
 			mEnabled = true; // in case gps is off we'll receive onDisabled and disable it
 			mBaseLocation = new Location(UNCERT_LOC);
 			mReadyLoc = new Location(UNCERT_LOC);
 			mState = TrackingState.WARMUP;
 			if (mWriteGPX) {
 				mGpxLog = new GPXSerializer("/sdcard/track.gpx");
 			}
 			for (TrackingSessionListener listener : mListeners)
 			{
 				listener.onSessionWarmingUp(mWarmupState);
 			}
 		}
 	}
 	
 	public void StopService() {
 		if (mState!=TrackingState.IDLE) { // stop only active
 			// Remove the listener you previously added
 			mLocationManager.removeUpdates(this);
 			
 			mBaseLocation = null;
 			mReadyLoc = null;
 			if (mWriteGPX && mGpxLog!=null) {
 				mGpxLog.Stop();
 			}
 			mState = TrackingState.IDLE;
 			// TODO: stop all activities, notify listeners
 			for (TrackingSessionListener listener : mListeners)
 			{
 				listener.onSessionStopped();
 			}
 		}
 	}
 	
 	public void StopTracking() {
 		if (mState!=TrackingState.TRACKING)
 			return;
 		// proceed only if in tracking state
 		// TODO: not sure this is needed
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		// we've got a new fix
 		mBaseLocation = location; // update last known location
 		for (TrackingSessionListener listener : mListeners)
 		{ // notify listeners
 			listener.onSessionLocationUpdate(location);
 		}
 		if (mWriteGPX) { // save fix to gpx track
 			mGpxLog.AddFix(location);
 		}
 		switch (mState)
 		{ // update logic
 		case WARMUP:
 			// here we check for problems to solve before we can start
 			if (location.getAccuracy()>HOR_ACCURACY)
 			{ 
 				if (mWarmupState != WarmupState.WAITING_FIX)
 				{
 					mWarmupState = WarmupState.WAITING_FIX;
 					for (TrackingSessionListener listener : mListeners)
 					{ 
 						listener.onSessionWarmingUp(mWarmupState);
 					}
 				}
 				
 			}
 			else if (location.getSpeed() > SPEED_THRESHOLD)
 			{
 				if (mWarmupState != WarmupState.HIGH_SPEED)
 				{
 					mWarmupState = WarmupState.HIGH_SPEED;
 					for (TrackingSessionListener listener : mListeners)
 					{ 
 						listener.onSessionWarmingUp(mWarmupState);
 					}
 				}
 			}
 			else{
 				// there are no problems, we're ready
 				mState = TrackingState.READY;	
 				for (TrackingSessionListener listener : mListeners)
 				{ //onSessionReady()
 					listener.onSessionReady();
 				}
 			}
 			break;
 		case READY:
 			// here we should determine when to start or get back to WARMUP in case of bad fix
 			if (location.getAccuracy()>HOR_ACCURACY)
 			{ // in case of bad fix stop tracking
 				mState = TrackingState.WARMUP;
 				mWarmupState = WarmupState.WAITING_FIX;
 				for (TrackingSessionListener listener : mListeners)
 				{ //onSessionWarmingUp()
 					listener.onSessionWarmingUp(mWarmupState);
 				}
 			}
 			// here is some logic to make it start
 			else if (location.getSpeed()>SPEED_THRESHOLD) {
 				mState = TrackingState.TRACKING;
 				for (TrackingSessionListener listener : mListeners)
 				{
 					listener.onSessionStart();
 				}
 				// push mReadyLoc to list
 				mLocList.add(mReadyLoc);
 				// push current loc to list
 				mLocList.add(location);
 				mMaxSpeed=location.getSpeed();
 			}
 			else {
 				// if not start - just resave prestart loc
 				mReadyLoc = location;
 			}
 			break;
 		case TRACKING:
 			if (location.getAccuracy()>HOR_ACCURACY)
 			{ // in case of overflow or bad fix stop trackingSessionDone
 				SessionDone();
 			}
			else if (mLocCount>=MAX_LOC_COUNT || location.getSpeed()<mMaxSpeed)
 			{ // good stop, there is no difference with bad stop
 				SessionDone();
 			}
 			else {
 				// and just save loc if all goes normal
 				mLocList.add(location);
 				mMaxSpeed=location.getSpeed();
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		mEnabled = false;
 		// TODO: native toast: please enable GPS
 		// provide bad location to stop measurement
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		mEnabled = true;
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 	}
 	
 	private void SessionDone() {
 		// here is some logic to make it stop the good way
 		mState = TrackingState.DONE;
 		for (TrackingSessionListener listener : mListeners)
 		{ // stop tracking
 			listener.onSessionFinished(mLocList);
 		}	
 	}
 	
 	private void ResetSessionValues() {
 		mMaxSpeed = 0.f;
		mLocCount = 0;
 		mLocList.clear();
 	}
 }
