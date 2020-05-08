 package com.delin.speedlogger;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import android.location.*;
 import android.os.Bundle;
 import android.util.Log;
 import android.content.Context;
 
 public class TrackingSession implements LocationListener {
 	enum TrackingState { WARMUP, READY, TRACKING, ERROR, DONE, IDLE }
 	enum WarmupState { WAITING_FIX, HIGH_SPEED }
 	
 	final static String UNCERT_LOC = "uncertainty";
 	final static int MAX_LOC_COUNT = 300; // 300/60 fixes per minute = 5 min
 	final static float HOR_ACCURACY = 25.f; // horizontal accuracy, in meters
 	final static float SPEED_THRESHOLD = 5.f; // speed threshold to detect start, in kmph
 	final static float MAX_SPEED_MULTIPLY_THRESHOLD = 0.9f; // stop tracking if speed falls more that 90% of current max
 	
 	//--- session measured parameters
 	float mMaxSpeed = 0.f;
 	List<Location> mLocList = new ArrayList<Location>();
 	
 	Location mBaseLocation = null; // last location
 	Location mReadyLoc = null;
 	GPSProvider mGpsProvider = null;	
 	boolean mEnabled = false; // gps receiver status, useless
 	boolean mWriteGPX = true;
 	GPXSerializer mGpxLog = null;
 	TrackingState mState = TrackingState.IDLE;
 	WarmupState mWarmupState = WarmupState.WAITING_FIX;
 	Vector<TrackingSessionListener> mListeners = new Vector<TrackingSessionListener>();;
 	
 	public TrackingSession(Context Context) {
 		//mGpsProvider = new RealGPSProvider(Context, this);
 		mGpsProvider = new FileGPSProvider(Context, this);
 		StartService();
 	}
 	
 	protected void finalize () {
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
 			ResetSessionValues();
 			mEnabled = true; // in case gps is off we'll receive onDisabled and disable it
 			mBaseLocation = new Location(UNCERT_LOC);
 			mReadyLoc = new Location(UNCERT_LOC);
 			mState = TrackingState.WARMUP;
 			if (mWriteGPX) {
 				mGpxLog = new GPXSerializer();
 			}
 			for (TrackingSessionListener listener : mListeners)
 			{
 				listener.onSessionWarmingUp(mWarmupState);
 			}
			mGpsProvider.Start();
 		}
 	}
 	
 	public void StopService() {
 		if (mState!=TrackingState.IDLE) { // stop only active
 			mGpsProvider.Stop();
 			
 			mBaseLocation = null;
 			mReadyLoc = null;
 			if (mWriteGPX) {
 				mGpxLog.Stop();
 				mGpxLog = null;
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
 		Log.i("TrackingSession", "Fix has come - onLocationChanged");
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
 			Log.i("TrackingSession", "Warming up...");
 			// here we check for problems to solve before we can start
 			if (location.hasAccuracy() && location.getAccuracy()>HOR_ACCURACY)
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
 			else if (location.hasSpeed() && location.getSpeed() > SPEED_THRESHOLD)
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
 			Log.i("TrackingSession", "We are ready");
 			// here we should determine when to start or get back to WARMUP in case of bad fix
 			if (location.hasAccuracy() && location.getAccuracy()>HOR_ACCURACY)
 			{ // in case of bad fix stop tracking
 				mState = TrackingState.WARMUP;
 				mWarmupState = WarmupState.WAITING_FIX;
 				for (TrackingSessionListener listener : mListeners)
 				{ //onSessionWarmingUp()
 					listener.onSessionWarmingUp(mWarmupState);
 				}
 			}
 			// here is some logic to make it start
 			else if (location.hasSpeed() && location.getSpeed()>SPEED_THRESHOLD) {
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
 				// we can calculate origin by taking average fix, not last one
 			}
 			break;
 		case TRACKING:
 			Log.i("TrackingSession", "Tracking, go-go-go");
 			if (false/*location.getAccuracy()>HOR_ACCURACY*/)
 			{ // in case of overflow or bad fix stop trackingSessionDone
 				SessionDone();
 			}
 			else if (location.hasSpeed()== false || location.getSpeed()<mMaxSpeed*MAX_SPEED_MULTIPLY_THRESHOLD
 					|| mLocList.size()>=MAX_LOC_COUNT)
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
 		Log.i("TrackingSession", "Session done");
 		// here is some logic to make it stop the good way
 		mState = TrackingState.DONE;
 		for (TrackingSessionListener listener : mListeners)
 		{ // stop tracking
 			listener.onSessionFinished(mLocList);
 		}	
 	}
 	
 	private void ResetSessionValues() {
 		mMaxSpeed = 0.f;
 		mLocList.clear();
 	}
 }
