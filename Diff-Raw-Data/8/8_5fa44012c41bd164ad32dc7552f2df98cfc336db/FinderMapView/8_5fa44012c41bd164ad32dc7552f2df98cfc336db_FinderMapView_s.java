 package com.parse3.storefinder.views.controls;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 
 import com.google.android.maps.MapView;
 import com.parse3.storefinder.views.interfaces.IMapStoresView;
 
 public class FinderMapView extends MapView {
 	private long lastTouchTime = -1;
 	private int lastAction = 0;
 	private boolean panned = false;
 	private IMapStoresView view;
 	
 	public FinderMapView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	public void setView(IMapStoresView view) {
 		this.view = view;
 	}
 	
 	@Override
 	public boolean onInterceptTouchEvent(MotionEvent ev) {
 		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
 			long thisTime = System.currentTimeMillis();
 			if (thisTime - lastTouchTime < 250) {
 				this.getController().zoomInFixing((int)ev.getX(), (int)ev.getY());
 				lastTouchTime = -1;
 			} else {
 				lastTouchTime = thisTime;
 			}
 			
 			lastAction = MotionEvent.ACTION_DOWN;
 		}
 		
		
		
 		return super.onInterceptTouchEvent(ev);
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
 		if (ev.getAction() == MotionEvent.ACTION_MOVE && lastAction == MotionEvent.ACTION_MOVE) {
 			panned = true;
 		}
 		
 		if (ev.getAction() == MotionEvent.ACTION_UP){
 			if (!panned)
 			{
 				if (view != null)
 					view.mapClick();
 			}
 			else
 				panned = false;
 		}
 		
 		lastAction = ev.getAction();
 		
 		return super.onTouchEvent(ev);
 	}
 }
