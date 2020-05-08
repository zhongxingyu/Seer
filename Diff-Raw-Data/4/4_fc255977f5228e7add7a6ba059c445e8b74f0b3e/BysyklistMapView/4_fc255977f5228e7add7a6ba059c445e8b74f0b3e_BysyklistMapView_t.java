 /**
  *   Copyright (C) 2010-2011, Roger Kind Kristiansen <roger@kind-kristiansen.no>
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package no.rkkc.bysykkel.views;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 
 public class BysyklistMapView extends MapView {
 	
 public interface OnLongpressListener {
     public void onLongpress(MapView view, GeoPoint longpressLocation);
 }
 	
 public interface OnZoomChangeListener {
     public void onZoomChange(MapView view, int newZoom, int oldZoom);
 }
 
 public interface OnPanChangeListener {
     public void onPanChange(MapView view, GeoPoint newCenter, GeoPoint oldCenter);
 }
 
 static final float LONGPRESS_MOVEMENT_THRESHOLD = 0.5f;
 
 // Set this variable to your preferred timeout
 private long eventsTimeout = 200L;
 private GeoPoint lastMapCenter;
 private int lastZoom;
 private Timer zoomEventDelayTimer = new Timer();
 private Timer panEventDelayTimer = new Timer();
 private Timer longpressTimer = new Timer();
 
 private BysyklistMapView.OnZoomChangeListener zoomChangeListener;
 private BysyklistMapView.OnPanChangeListener panChangeListener;
 private BysyklistMapView.OnLongpressListener longpressListener;
 
 public BysyklistMapView(Context context, String apiKey) {
     super(context, apiKey);
     getCenterAndZoom();
 }
 
 public BysyklistMapView(Context context, AttributeSet attrs) {
     super(context, attrs);
     getCenterAndZoom();
 }
 
 public BysyklistMapView(Context context, AttributeSet attrs, int defStyle) {
     super(context, attrs, defStyle);
     getCenterAndZoom();
 }
 
 /**
  * Retrieves current map enter and zoom level. To be run from all the overloaded constructors to
  * make sure it is run.
  */
 private void getCenterAndZoom() {
 	lastMapCenter = this.getMapCenter();
 	lastZoom = this.getZoomLevel();
 }
 
 public void setOnLongpressListener(BysyklistMapView.OnLongpressListener listener) {
 	longpressListener = listener;
 }
 
 public void setOnZoomChangeListener(BysyklistMapView.OnZoomChangeListener listener) {
 	lastZoom = this.getZoomLevel();
     zoomChangeListener = listener;
 }
 
 public void setOnPanChangeListener(BysyklistMapView.OnPanChangeListener listener) {
 	lastMapCenter = this.getMapCenter();
     panChangeListener = listener;
 }
 
 @Override
 public boolean onTouchEvent(MotionEvent event) {
 	handleLongpress(event);
 	
 	return super.onTouchEvent(event);
 }
 
 private void handleLongpress(final MotionEvent event) {
 	if (event.getAction() == MotionEvent.ACTION_DOWN) {
 		longpressTimer = new Timer();
 		longpressTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				GeoPoint longpressLocation = getProjection().fromPixels((int)event.getX(), 
 						(int)event.getY());
 				longpressListener.onLongpress(BysyklistMapView.this, longpressLocation);
 			}
 			
 		}, 500);
 		
 		lastMapCenter = getMapCenter();
 	}
 	
 	if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			
 		if (!getMapCenter().equals(lastMapCenter)) {
 			longpressTimer.cancel();
 		}
 		
 		lastMapCenter = getMapCenter();
 	}
 	
 	if (event.getAction() == MotionEvent.ACTION_UP) {
 		longpressTimer.cancel();
 	}
	
	if (event.getPointerCount() > 1) {
		longpressTimer.cancel();
	}
 }
 
 @Override
 public void computeScroll() {
     super.computeScroll();
     
     // Catch zoom level change
     if (getZoomLevel() != lastZoom) {
         // if computeScroll called before timer counts down we should drop it and start it over again
         zoomEventDelayTimer.cancel();
         zoomEventDelayTimer = new Timer();
         zoomEventDelayTimer.schedule(new TimerTask() {
             @Override
             public void run() {
                 zoomChangeListener.onZoomChange(BysyklistMapView.this, getZoomLevel(), lastZoom);
                 lastZoom = getZoomLevel();
             }
         }, eventsTimeout);
     }
 
     // Catch panning
     if (!lastMapCenter.equals(getMapCenter())) {
         panEventDelayTimer.cancel();
         panEventDelayTimer = new Timer();
         panEventDelayTimer.schedule(new TimerTask() {
             @Override
             public void run() {
                 panChangeListener.onPanChange(BysyklistMapView.this, getMapCenter(), lastMapCenter);
                 lastMapCenter = getMapCenter();
             }
         }, eventsTimeout);
     }
 }
 
 }
