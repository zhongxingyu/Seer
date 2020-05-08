 package com.teamblobby.studybeacon;
 
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.OverlayItem;
 import com.teamblobby.studybeacon.datastructures.BeaconInfo;
 
 public class BeaconOverlayItem extends OverlayItem {
 
 	BeaconInfo mBeacon;
 
 	static final Drawable presentBeaconDrawable = Global.res.getDrawable(R.drawable.present_beacon);
 
 	private static final String TAG = "BeaconOverlayItem";
 	
 	public BeaconOverlayItem(GeoPoint point, String title, String snippet, BeaconInfo beacon) {
 		super(point, title, snippet);
 		mBeacon = beacon;
 		mBeacon.setLoc(point);
 	}
 
 	public BeaconOverlayItem(BeaconInfo beacon) {
 		this(beacon.getLoc(), getShortTitle(beacon),
 				getShortSnippet(beacon), beacon);
 	}
 
 	public static String getShortTitle(BeaconInfo beacon) {
 		return beacon.getCourseName();
 	}
 
 	public static String getShortSnippet(BeaconInfo beacon) {
 		return  "×" + Integer.toString(beacon.getVisitors()) + " here";
 	}
 
 	public BeaconInfo getBeacon() {
 		return mBeacon;
 	}
 
 	@Override
 	public Drawable getMarker(int stateBitset) {
 		// Find out if this is the beacon where we currently are
 		BeaconInfo presentBeaconInfo = Global.getCurrentBeacon();
 		if ((presentBeaconInfo != null)
 				&& (mBeacon != null)
 				&& (presentBeaconInfo.getBeaconId() == mBeacon.getBeaconId())) {
			Log.d(TAG,"This is my boi!");
			return presentBeaconDrawable;
 		} else {
 			return super.getMarker(stateBitset);
 		}
 	}
 	
 }
