 package com.teamblobby.studybeacon;
 
 import android.graphics.Canvas;
 import android.graphics.ColorFilter;
 import android.graphics.drawable.Drawable;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.OverlayItem;
 import com.teamblobby.studybeacon.datastructures.BeaconInfo;
 
 public class BeaconOverlayItem extends OverlayItem {
 
 	BeaconInfo mBeacon;
 	boolean displayed = true;
 
 	static final Drawable presentBeaconDrawable = Global.res.getDrawable(R.drawable.present_beacon);
	// TODO Simplify the following?
 	static final Drawable emptyDrawable = new Drawable() {
 		
 		@Override
 		public void setColorFilter(ColorFilter cf) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		@Override
 		public void setAlpha(int alpha) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		@Override
 		public int getOpacity() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 		
 		@Override
 		public void draw(Canvas canvas) {
 			// TODO Auto-generated method stub
 			
 		}
 	};
 
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
 
	@Override
	public String getSnippet() {
		return "×" + Integer.toString(mBeacon.getVisitors()) + " here";
	}

	@Override
	public String getTitle() {
		return mBeacon.getCourseName();
	}

	private static String getShortTitle(BeaconInfo beacon) {
 		return beacon.getCourseName();
 	}
 
	private static String getShortSnippet(BeaconInfo beacon) {
 		return  "×" + Integer.toString(beacon.getVisitors()) + " here";
 	}
 
 	public BeaconInfo getBeacon() {
 		return mBeacon;
 	}
 
 	public void setBeacon(BeaconInfo beacon) {
 		mBeacon = beacon;
 	}
 
 	/**
 	 * Sets whether or not this BeaconOverlayItem should be displayed.
 	 * This function should be called after creating the overlay.
 	 * @param course Course name which should be displayed.
 	 * A value of null corresponds to being displayed.
 	 */
 	public void setDisplayed(String course) {
 		if (course == null) {
 			displayed = true;
 		} else if (course.equals(mBeacon.getCourseName())) {
 			displayed = true;
 		} else displayed = false;
 	}
 
 	public boolean isDisplayed() {
 		return displayed;
 	}
 
 	@Override
 	public Drawable getMarker(int stateBitset) {
 
 		if (displayed == false)
 			return emptyDrawable;
 
 		// Find out if this is the beacon where we currently are
 		BeaconInfo presentBeaconInfo = Global.getCurrentBeacon();
 		if ((presentBeaconInfo != null)
 				&& (mBeacon != null)
 				&& (presentBeaconInfo.getBeaconId() == mBeacon.getBeaconId())) {
 			return BeaconItemizedOverlay.boundCenterBottom(presentBeaconDrawable);
 		} else {
 			return super.getMarker(stateBitset);
 		}
 	}
 
 }
