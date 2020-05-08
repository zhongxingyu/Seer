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
 import android.content.Intent;
 import android.os.Handler;
import android.util.TypedValue;
import android.view.Display;
 import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;
 
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
 	private static final int THRESHOLD_LONGPRESS = 200;
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
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getRotation();
		int dp;
		if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
			//Android 4.0+ default acionbar values:
			//48dip in portrait
			//40dip in landscape
			//
			//We use 96dip as we also have a tabbed navigation below
			//the actionbar which is the same height as the actionbar
			dp = 96;
		} else {
			dp = 40;
		}
		
		//Convert DIP to Pixels
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
		
		//Subtract the pixels from the Y coordinate to compensate for using a map
		//in a fragment in a tabbed navigation. Otherwise the getY() would return 
		//wrong
		GeoPoint point = map.getProjection().fromPixels((int)event.getX(), (int)(event.getY() - px));
 		Intent intent = new Intent(context, ActivityNote.class);
		intent.putExtra(IntentExtra.longitude.toString(), point.getLongitudeE6() / 1E6);
		intent.putExtra(IntentExtra.latitude.toString(), point.getLatitudeE6() / 1E6);
 		intent.putExtra(IntentExtra.id.toString(), -1);
 		context.startActivity(intent);
 	}
 
 	/**
 	 * Handle the long press and call onLongPress if
 	 * a longpress is detected
 	 * @param event the current motion event
 	 */
 	private void handleLongpress(final MotionEvent event) {
 		//A press gesture have started
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
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
