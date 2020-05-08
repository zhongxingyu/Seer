 /**
  * This file is part of Plingnote.
  * Copyright (C) 2012 Barnabas sapan
  * 
  * Plingnote is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.plingnote;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.os.Handler;
 import android.view.MotionEvent;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 /**
  * A long press implementation for the map. This is a workaround
  * for the Android's missing built in support for this kind of action.
  * This class extends Overlay so it can be added to the map via the
  * standard way to enable long press. This class onLongpress method
  * will be fired on detected long press action.
  * 
  * @author Barnabas Sapan
  */
 public class MapOverlayLongpressHandler extends Overlay implements OnMapViewLongpressListener {
	private static final int THRESHOLD_LONGPRESS = 600;
 	private MapView map;
 	private Context context;  
 	private GeoPoint mapCenterLast;
 	private Timer timerLongpress = new Timer();
 	private final Handler handler = new Handler();
 	
 	public MapOverlayLongpressHandler(Context context, MapView mapView) {
 		this.map = mapView;
 		this.context = context;
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
 		//Check to see if this touch event is a longpress. If yes, then run
 		//the onLongPress method
 		this.handleLongpress(event);
		
 		return false;
 	}
 	
 	/**
 	 * Runs if the user have long pressed on a location
 	 */
 	public void onLongpress(MotionEvent event) {
 		GeoPoint p = map.getProjection().fromPixels((int)event.getX(), (int)event.getY());
 		Toast.makeText(context, "Location: " + p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6() / 1E6, Toast.LENGTH_SHORT).show();
 		//TODO Call create new note with the touch coordinates
 	}
 
 	/**
 	 * Handle the long press and call onLongPress if
 	 * a longpress is detected
 	 * @param event the current motion event
 	 */
 	private void handleLongpress(final MotionEvent event) {
 		//A press gesture have started
		if (event.getAction() == MotionEvent.ACTION_UP) {
 			timerLongpress = new Timer();
 			timerLongpress.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					handler.post(new Runnable() {
 						public void run() {
 							onLongpress(event);
 						}
 					});	                    
 				}
 			}, THRESHOLD_LONGPRESS);
 
 			mapCenterLast = map.getMapCenter();
 		}
 
 
 		//User moved the finger, no long press action
 		if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			if (map.getMapCenter().equals(mapCenterLast) == false)
 				timerLongpress.cancel();
 
 			mapCenterLast = map.getMapCenter();
 		}
 
 		//User removed the finger, no long press action
 		if (event.getAction() == MotionEvent.ACTION_UP)
 			timerLongpress.cancel();
 
 		//User have more then 1 finger on screen, no long press action
 		if (event.getPointerCount() > 1)
 			timerLongpress.cancel();
 	}
 }
