 /*    Dice in the dark. D & D app for the blind and seeing impaired,
 *    Copyright (C) <2013r>  <Lovisa Irpa Helgadottir>
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.plovergames.framework.impl;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.plovergames.framework.Input.GestureEvent;
 import com.plovergames.framework.Input.TouchEvent;
 import com.plovergames.framework.Pool.PoolObjectFactory;
 import com.plovergames.framework.Pool;
 
 
 import android.content.Context;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.GestureDetector.OnDoubleTapListener;
 import android.view.GestureDetector.OnGestureListener;
 
 public class GestureHandler implements TouchHandler{
 	 private static final int MAX_TOUCHPOINTS = 10;
 		
 	    boolean[] isTouched = new boolean[MAX_TOUCHPOINTS];
 	    int[] touchX = new int[MAX_TOUCHPOINTS];
 	    int[] touchY = new int[MAX_TOUCHPOINTS];
 	    int[] id = new int[MAX_TOUCHPOINTS];
 	    
 		private GestureDetector gestures;
 		Pool<GestureEvent> gestureEventPool;
 		List<GestureEvent> gestureEvents = new ArrayList<GestureEvent>();
 		List<GestureEvent> gestureEventsBuffer = new ArrayList<GestureEvent>();
 
 	    
 
 	    
 	    public GestureHandler(Context context, View view) {
 
 	        
 	        PoolObjectFactory<GestureEvent> factory1 = new PoolObjectFactory<GestureEvent>() {
 	            public GestureEvent createObject() {
 	                return new GestureEvent();
 	            }
 	        };
 	        gestureEventPool = new Pool<GestureEvent>(factory1, 100);
 	        gestures = new GestureDetector(context, new GestureListener(this));
 	        
 	        view.setOnTouchListener(this);
 
 	    }
 
 	    public boolean onTouch(View v, MotionEvent event) {
 	        synchronized (this) {
 	        	gestures.onTouchEvent(event);
 	            return true;
 	        }
 	    }
 
 	    public boolean isTouchDown(int pointer) {
 	        synchronized (this) {
 	            int index = getIndex(pointer);
 	            if (index < 0 || index >= MAX_TOUCHPOINTS)
 	                return false;
 	            else
 	                return isTouched[index];
 	        }
 	    }
 
 	    public int getTouchX(int pointer) {
 	        synchronized (this) {
 	            int index = getIndex(pointer);
 	            if (index < 0 || index >= MAX_TOUCHPOINTS)
 	                return 0;
 	            else
 	                return touchX[index];
 	        }
 	    }
 
 	    public int getTouchY(int pointer) {
 	        synchronized (this) {
 	            int index = getIndex(pointer);
 	            if (index < 0 || index >= MAX_TOUCHPOINTS)
 	                return 0;
 	            else
 	                return touchY[index];
 	        }
 	    }
 
 	    public List<TouchEvent> getTouchEvents() {
 	    	return null;
 	    }
 	    public List<GestureEvent> getGestureEvents() {
 	        synchronized (this) {
 	            int len = gestureEvents.size();
 	            for (int i = 0; i < len; i++)
 	                gestureEventPool.free(gestureEvents.get(i));
 	            gestureEvents.clear();
 	            gestureEvents.addAll(gestureEventsBuffer);
 	            gestureEventsBuffer.clear();
 	            return gestureEvents;
 	        }
 	    }
 	    
 	    // returns the index for a given pointerId or -1 if no index.
 	    private int getIndex(int pointerId) {
 	        for (int i = 0; i < MAX_TOUCHPOINTS; i++) {
 	            if (id[i] == pointerId) {
 	                return i;
 	            }
 	        }
 	        return -1;
 	    }
 	}
 	class GestureListener implements OnGestureListener, OnDoubleTapListener {
 	    private static final String DEBUG_TAG = "GestureListener";
 		GestureHandler handler; 
 		
 		public GestureListener(GestureHandler handler){
 			this.handler = handler;
 		}
 		 @Override
 	     public boolean onDown(MotionEvent e) {
 	         Log.v(DEBUG_TAG, "onDown");
 	         return true;
 	     }
 
 	     @Override
 	     public boolean onFling(MotionEvent e1, MotionEvent e2,
 	             final float velocityX, final float velocityY) {
 	         Log.v(DEBUG_TAG, "onFling");
 	    	 GestureEvent event;
 	    	 event = handler.gestureEventPool.newObject();
 	    	 float diffX =e1.getRawX()-e2.getRawX();
 	    	 float diffY =e1.getRawY()-e2.getRawY();
 	    	 if(Math.abs(diffX)>Math.abs(diffY)){
 	    	 if(diffX<0)
 	    		 event.type= GestureEvent.FLING_RIGHT;
 	    	 else 
 	    		 event.type = GestureEvent.FLING_LEFT;
 	    	 
 	    	
 	    	 }else{
 	    		 if(diffY<0)
 	    			 event.type=GestureEvent.SCROLL_UP;
 	    		 else
 	    			 event.type = GestureEvent.SCROLL_DOWN;
 	    				 
 	    	 }
 	    	 handler.gestureEventsBuffer.add(event);	 
 	    	 
 
 	         
 	         return true;
 	     }
 
 	     @Override
 	     public boolean onDoubleTap(MotionEvent e) {
 	         Log.v(DEBUG_TAG, "onDoubleTap");
 	         GestureEvent event;
 	    	 event = handler.gestureEventPool.newObject();
 	    	 event.type = GestureEvent.DOUBLE_TAP;
 	    	 handler.gestureEventsBuffer.add(event);
 	         return true;
 	     }
 
 	     @Override
 	     public void onLongPress(MotionEvent e) {
 	         Log.v(DEBUG_TAG, "onLongPress");
 	     }
 
 	     @Override
 	     public boolean onScroll(MotionEvent e1, MotionEvent e2,
 	             float distanceX, float distanceY) {
 	         Log.v(DEBUG_TAG, "onScroll");
 /*	    	 GestureEvent event;
 	    	 event = handler.gestureEventPool.newObject();
 	    	 float diff =e1.getRawY()-e2.getRawY();
 	    	 if(Math.abs(diff)>Math.abs(e1.getRawX()-e2.getRawX())){
 	    		 if(diff<0)
 	    			 event.type= GestureEvent.SCROLL_UP;
 	    		 else 
 	    			 event.type = GestureEvent.SCROLL_DOWN;
 	    		 handler.gestureEventsBuffer.add(event);
 	    	 }*/
 	         return false;
 	     }
 
 	     @Override
 	     public void onShowPress(MotionEvent e) {
 	         Log.v(DEBUG_TAG, "onShowPress");
 	     }
 
 	     @Override
 	     public boolean onSingleTapUp(MotionEvent e) {
 	         Log.v(DEBUG_TAG, "onSingleTapUp");
 	         GestureEvent event;
 	    	 event = handler.gestureEventPool.newObject();
 	    	 event.type = GestureEvent.SINGLE_TAP;
 	    	 handler.gestureEventsBuffer.add(event);
 	         return true;
 	     }
 
 	     @Override
 	     public boolean onDoubleTapEvent(MotionEvent e) {
 	         Log.v(DEBUG_TAG, "onDoubleTapEvent");
 	         return false;
 	     }
 
 	     @Override
 	     public boolean onSingleTapConfirmed(MotionEvent e) {
 	         Log.v(DEBUG_TAG, "onSingleTapConfirmed");
 	         return false;
 	     }
 
 	 }
