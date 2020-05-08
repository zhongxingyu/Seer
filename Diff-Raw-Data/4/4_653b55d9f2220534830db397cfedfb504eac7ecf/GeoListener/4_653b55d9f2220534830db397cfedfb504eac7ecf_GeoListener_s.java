 package com.phonegap.demo;
 
 import android.content.Context;
 import android.location.Location;
 import android.webkit.WebView;
 
 public class GeoListener {
 	String id;
 	String successCallback;
 	String failCallback;
     GpsListener mGps; 
     NetworkListener mNetwork;
     Context mCtx;
     private WebView mAppView;
     private ArgTable arguments;
 
 	int interval;
 	
 	GeoListener(String i, Context ctx, int time, WebView appView, ArgTable args)
 	{
 		id = i;
 		interval = time;
 		mCtx = ctx;
 		mGps = new GpsListener(mCtx, interval, this);
 		mNetwork = new NetworkListener(mCtx, interval, this);
 		mAppView = appView;
 		arguments = args;
 	}
 	
 	void success(Location loc)
 	{
 		arguments.put("gpsLat", loc.getLatitude());
 		arguments.put("gpsLng", loc.getLongitude());
 		/*
 		 * We only need to figure out what we do when we succeed!
 		 */
 		if(id != "global")
 		{
 			arguments.put("gpsId", id);
 			mAppView.loadUrl("javascript:navigator.geolocation.success()");
 		}
 		else
 		{
 			
 			mAppView.loadUrl("javascript:Geolocation.gotCurrentPosition()");
 			this.stop();
 		}
 	}
 	
 	void fail()
 	{
 		// Do we need to know why?  How would we handle this?
 		mAppView.loadUrl("javascript:GeoLocation.fail()");
 	}
 	
 	// This stops the listener
 	void stop()
 	{
 		mGps.stop();
 		mNetwork.stop();
 	}
 
 	public Location getCurrentLocation() {
 		Location loc = mGps.getLocation();
 		if (loc == null)
 			loc = mNetwork.getLocation();
 		return loc;
 	}
 }
