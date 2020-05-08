 package com.teamblobby.studybeacon;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MapView.LayoutParams;
 import com.teamblobby.studybeacon.datastructures.BeaconInfo;
 
 public class BeaconItemizedOverlay extends ItemizedOverlay {
 
 	private static final String TAG = "BeaconItemizedOverlay";
 	protected List<BeaconOverlayItem> mOverlays;
 	protected Context mContext;
 	protected SBMapView mapView;
 	protected MapController mc;
 	private String courseToDisplay;
 	
 	// These things take care of the balloon
 	private int BubbleOffset = 30;
 	private BalloonOverlayView balloonView;
 	private int selectedIndex = -1;
 	private boolean balloonVisible = true;
 	public boolean balloonsEnabled = true;
 
 	
 	
 	public BeaconItemizedOverlay(Drawable defaultMarker, Context context, SBMapView mv) {
 		super(boundCenterBottom(defaultMarker));
 		mContext = context;
 		this.mapView = mv;
 		this.mc = mv.getController();
 		mOverlays = new ArrayList<BeaconOverlayItem>();
 	}
 
 	public void setCourseToDisplay(String courseToDisplay) {
 
 		this.courseToDisplay = courseToDisplay;
 
 		// Iterate over all the BeaconOverlayItems, and set their displayed state.
 		for (BeaconOverlayItem item : mOverlays) {
 			item.setDisplayed(courseToDisplay);
 		}
 
 		// If the balloon is shown and needs to be hidden, do so
 		if (balloonVisible && (balloonView != null)) {
 			BeaconOverlayItem balloonItem = (BeaconOverlayItem)getItem(selectedIndex);
 			if ( !balloonItem.isDisplayed() ) {
 				mapView.removeView(balloonView);
 				balloonVisible = false;
 				selectedIndex = -1;
 			}
 		}
 	}
 
 	/**
 	 * Adds, replaces, or removes a beacon from the overlays.
 	 * If the ItemizedOverlay does not have a beacon with the
 	 * same BeaconId, then it is added (unless it has 0 visitors).
 	 * If the ItemizedOverlay has a beacon with the same BeaconId,
 	 * then it is replaced (unless it has 0 visitors, in which case
 	 * it is removed) 
 	 * @param beacon The beacon to be added, replaced, or removed.
 	 */
 	public void addReplaceRemoveBeacon(BeaconInfo beacon) {
 		BeaconOverlayItem foundOverlay = getByBeaconId(beacon.getBeaconId());
 
 		if (foundOverlay == null) {
 			if (beacon.getVisitors() > 0) {
 				// ADD
 				BeaconOverlayItem newOverlay = new BeaconOverlayItem(beacon);
 				newOverlay.setDisplayed(courseToDisplay);
 				addOverlay(newOverlay);
 			}
 		} else {
 			// REPLACE OR REMOVE
 			if (beacon.getVisitors() > 0) {
				if( !beacon.equals(foundOverlay.getBeacon()) ) {
					// REPLACE
					foundOverlay.setBeacon(beacon);
					// TODO If this beacon has a currently displayed balloon, update the balloon.
				}
 			} else {
 				// REMOVE
 				removeOverlay(foundOverlay);
				// TODO If this beacon has a currently displayed balloon, get rid of it
 			}
 		}
 		populate();
 	}
 
 	protected void addOverlay(BeaconOverlayItem overlay) {
 	    mOverlays.add(overlay);
 	    populate();
 	}
 	
 	protected void removeOverlay(BeaconOverlayItem overlay) {
 		mOverlays.remove(overlay);
 		populate();
 	}
 	
 	protected void removeByBeaconId(int BeaconId) {
 		removeOverlay(getByBeaconId(BeaconId));
 	}
 	
 	public BeaconOverlayItem getByBeaconId(int BeaconId) {
 		Iterator<BeaconOverlayItem> iter = mOverlays.iterator();
 		while (iter.hasNext()) {
 			BeaconOverlayItem item = iter.next();
 			if (BeaconId == item.getBeacon().getBeaconId())
 				return item;
 		}
 		// Failure?
 		return null;
 	}
 	
 	@Override
 	protected BeaconOverlayItem createItem(int i) {
 		return mOverlays.get(i);
 	}
 
 	@Override
 	public int size() {
 		return mOverlays.size();
 	}
 	
 	@Override
     protected boolean onTap(int index) {
 	 
 	 	if (!balloonsEnabled) return false;
 	 
 	 	mapView.tapped_overlay = true;
 	 
 	 	if (balloonView==null) balloonVisible = false;
 	 	else if (!balloonView.isShown()) balloonVisible = false;
 	 	
 	 	if (selectedIndex == index) {
 	        final BeaconOverlayItem item = (BeaconOverlayItem) mOverlays.get(index);
 
 	        // If this item is not to be shown, don't make a balloon!
 	        if ( ! item.isDisplayed() )
 	        	return false;
 
 	 		if (!balloonVisible) {
 	 			makeBalloon(item);
 	 		}
 	 		balloonVisible = true;
 	 	} else {
  			mapView.removeView(balloonView);
 	 		balloonVisible = false;
 	 	}
 	 
 	 	selectedIndex = index;
 
         return true;
         
     }
 	
 	/*
 	 * 
 	 * This code is borrowed from the MIT mobile app
 	 * 
 	 * 
 	 */
 	protected void makeBalloon(final BeaconOverlayItem p) {
 		 
 		Log.d(TAG,"Making balloon");
 
 		if (p == null)
 			return;
 		
 		 GeoPoint gp = p.getPoint();
 		 
 		 mapView.removeView(balloonView);
 		    
 		 balloonView = new BalloonOverlayView(mContext, BubbleOffset );  
 		 
 		 View clickableRegion = balloonView.findViewById(R.id.balloon_inner_layout);
 		 
 		 clickableRegion.setOnTouchListener(new OnTouchListener() {
 			 public boolean onTouch(View v, MotionEvent event) {
 				 View l =  ((View) v.getParent()).findViewById(R.id.balloon_main_layout); 
 				 Drawable d = l.getBackground();
 				 if (event.getAction() == MotionEvent.ACTION_DOWN) {
 					 int[] states = {android.R.attr.state_pressed};
 					 if (d.setState(states)) {
 						 d.invalidateSelf();
 					 }
 					 return true;
 				 } else if (event.getAction() == MotionEvent.ACTION_UP) {
 					 int newStates[] = {};
 					 if (d.setState(newStates)) {
 						 d.invalidateSelf();
 					 }
 					
 					 handleTap(p);
 					 
 					 return true;
 				 } else {
 					 return false;
 				 }
 			 }
 		 });
 
 		 balloonView.setData(p);
 				 
 	     MapView.LayoutParams params = new MapView.LayoutParams(
 	                     LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, gp,
 	                     MapView.LayoutParams.BOTTOM_CENTER);
 	     
 	     params.mode = MapView.LayoutParams.MODE_MAP;
 	     
 	     balloonView.setVisibility(View.VISIBLE);
 	
 	     balloonView.setLayoutParams(params);
 	    
 	     mc.animateTo(gp); 
 
 		 mapView.addView(balloonView);
         //mapView.addView(balloonView, params);
 			
 	 }
 
 
 	 protected void handleTap(BeaconOverlayItem p) {
 		 Intent intent = new Intent(mContext, BeaconEditActivity.class);
 		 // Check if this is the present beacon
 		 BeaconInfo presentBeacon = Global.getCurrentBeacon();
 		 if ((presentBeacon != null)
 				 && (p.getBeacon() != null)
 				 && (presentBeacon.getBeaconId() == p.getBeacon().getBeaconId())) {
 			 intent.setAction(BeaconEditActivity.ACTION_EDIT);
 		 } else {
 			 intent.setAction(BeaconEditActivity.ACTION_VIEW);
 			 intent.putExtra(BeaconEditActivity.EXTRA_BEACON, p.getBeacon());
 		 }
 		 mContext.startActivity(intent);
 
 	 }
 
 	 // I'm cheating
 	 public static Drawable boundCenterBottom(Drawable d) {
 		 return ItemizedOverlay.boundCenterBottom(d);
 	 }
 }
