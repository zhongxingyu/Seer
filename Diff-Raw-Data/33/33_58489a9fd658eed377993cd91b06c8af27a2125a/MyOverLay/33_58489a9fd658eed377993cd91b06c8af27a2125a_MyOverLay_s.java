 package com.tracker;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.graphics.Paint.Style;
 import android.util.Log;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 
 
 public class MyOverLay  extends Overlay {
 
 	/**
 	 * Stored as global instances as one time initialization is enough
 	 */
     private Bitmap mBubbleIcon, mShadowIcon;
     
     private Bitmap mNowIcon;
     
     private MyGoogleMap mLocationViewers;
     
     private Paint	mInnerPaint, mBorderPaint, mTextPaint;
     
     private List<GeoPoint> gp;
 	
     private int infoWindowOffsetX;
     private int infoWindowOffsetY;
     
     private boolean showWinInfo;
     
     private boolean ReadyShowRange;
     
 	/**
 	 * It is used to track the visibility of information window and clicked location is known location or not 
 	 * of the currently selected Map Location
 	 */
     private MapLocation mSelectedMapLocation;  
     
 	public MyOverLay(MyGoogleMap mLocationViewers) {
 		
 		this.mLocationViewers = mLocationViewers;
 		
 		mBubbleIcon = BitmapFactory.decodeResource(mLocationViewers.getResources(),R.drawable.bubble);
 		mShadowIcon = BitmapFactory.decodeResource(mLocationViewers.getResources(),R.drawable.shadow);
 		mNowIcon = BitmapFactory.decodeResource(mLocationViewers.getResources(),R.drawable.mappin_blue);
 		showWinInfo = false;
 		
 		gp = new ArrayList<GeoPoint>();
 		ReadyShowRange = false;
 	}
 	
 	@Override
 	public boolean onTap(GeoPoint p, MapView mapView)  {
 		
 		/**
 		 * Track the popup display
 		 */
 		//boolean isRemovePriorPopup = mSelectedMapLocation != null;  
 
 		/**
 		 * Test whether a new popup should display
 		 */
 		/*
 		mSelectedMapLocation = getHitMapLocation(mapView, p);
 		if ( isRemovePriorPopup || mSelectedMapLocation != null) 
 		{
 	    mapView.invalidate();
 	    if (showWinInfo == true && mSelectedMapLocation != null)
 	    {
 	      mSelectedMapLocation = null;
 	      showWinInfo = false;
 	    }
 		}		
 		else
 		{
 		  showWinInfo = false;
 		}
 		*/
 		Log.i("TAG", "onTap");
 		
 		int newPointSize = gp.size();
 		
 		if (newPointSize < 4)
 		{
 		  //mark TAG
 		  mSelectedMapLocation = getHitMapLocation(mapView, p);
 		  gp.add(p);
 
 		  if (gp.size() == 4)
 	    {
 		    //choice done. link
 		    ReadyShowRange = true;
 	    }
 		}
 
 		/**
 		 *   Return true if we handled this onTap()
 		 */
 		return true;
 	}
 	
     @Override
 	public void draw(Canvas canvas, MapView	mapView, boolean shadow) 
   {
      //drawNowGeoMap(canvas, mapView, shadow);
    		drawMapLocations(canvas, mapView, shadow);
    		drawPointRange(canvas, mapView, shadow);
    		//drawInfoWindow(canvas, mapView, shadow);
   }
     
   public void clearRange()
   {
     ReadyShowRange = false;
     gp.clear();
   }
 
   /**
      * Test whether an information balloon should be displayed or a prior balloon hidden.
      */
     private MapLocation getHitMapLocation(MapView	mapView, GeoPoint	tapPoint) {
     	
     	/**
     	 *   Tracking the clicks on MapLocation
     	 */
     	MapLocation hitMapLocation = null;
 		
     	RectF hitTestRecr = new RectF();
 		  Point screenCoords = new Point();
     	Iterator<MapLocation> iterator = mLocationViewers.getMapLocations(false).iterator();
     	while(iterator.hasNext()) {
     		MapLocation testLocation = iterator.next();
     		
     		/**
     		 * This is used to translate the map's lat/long coordinates to screen's coordinates
     		 */
     		mapView.getProjection().toPixels(testLocation.getPoint(), screenCoords);
 
 	    	// Create a testing Rectangle with the size and coordinates of our icon
 	    	// Set the testing Rectangle with the size and coordinates of our on screen icon
     		hitTestRecr.set(-mBubbleIcon.getWidth()/2,-mBubbleIcon.getHeight(),mBubbleIcon.getWidth()/2,0);
     		hitTestRecr.offset(screenCoords.x,screenCoords.y);
 
 	    	//  At last test for a match between our Rectangle and the location clicked by the user
     		mapView.getProjection().toPixels(tapPoint, screenCoords);
     		
     		if (hitTestRecr.contains(screenCoords.x,screenCoords.y)) {
     			hitMapLocation = testLocation;
     			break;
     		}
     	}
     	
     	//  Finally clear the new MouseSelection as its process finished
     	tapPoint = null;
     	
     	return hitMapLocation; 
     }
 
     
     private void drawPointRange(Canvas canvas, MapView mapView, boolean shadow) 
     {
       
       if (ReadyShowRange == true)
       {
         Paint paint = new Paint();
         Point myScreenCoords1 = new Point();
         Point myScreenCoords2 = new Point();
         Point myScreenCoords3 = new Point();
         Point myScreenCoords4 = new Point();
         
         //calculate
         GeoPoint top_left = gp.get(0);        
         GeoPoint top_right =  gp.get(1);
         GeoPoint bottom_left =  gp.get(2);
         GeoPoint buttom_right = gp.get(3);        
         
         mapView.getProjection().toPixels(top_left, myScreenCoords1);
         mapView.getProjection().toPixels(top_right, myScreenCoords2);
         mapView.getProjection().toPixels(bottom_left, myScreenCoords3);
         mapView.getProjection().toPixels(buttom_right, myScreenCoords4);
         paint.setStrokeWidth(2);
         paint.setARGB(255, 255, 0, 0);
 
         canvas.drawLine((float) myScreenCoords1.x, (float) myScreenCoords1.y, (float) myScreenCoords2.x,
             (float) myScreenCoords2.y, paint);
         canvas.drawLine((float) myScreenCoords1.x, (float) myScreenCoords1.y, (float) myScreenCoords3.x,
             (float) myScreenCoords3.y, paint);
         canvas.drawLine((float) myScreenCoords2.x, (float) myScreenCoords2.y, (float) myScreenCoords4.x,
             (float) myScreenCoords4.y, paint);
         canvas.drawLine((float) myScreenCoords3.x, (float) myScreenCoords3.y, (float) myScreenCoords4.x,
             (float) myScreenCoords4.y, paint);
   
       }
     }
     
     
     private void drawNowGeoMap(Canvas canvas, MapView mapView, boolean shadow) 
     {
      Paint paint = new Paint();
      Point myScreenCoords = new Point();

      mapView.getProjection().toPixels(mLocationViewers.nowGeoPoint, myScreenCoords);
      paint.setStrokeWidth(1);
      paint.setARGB(255, 255, 0, 0);
      paint.setStyle(Paint.Style.STROKE);

      canvas.drawBitmap(mNowIcon, myScreenCoords.x, myScreenCoords.y, paint);
      canvas.drawText("{bm", myScreenCoords.x, myScreenCoords.y, paint);
     }
     
     private void drawMapLocations(Canvas canvas, MapView	mapView, boolean shadow) {
     	
 		Iterator<GeoPoint> iterator = gp.iterator();
 		Point screenCoords = new Point();
     	while(iterator.hasNext()) {	   
     	  GeoPoint location = iterator.next();
     		mapView.getProjection().toPixels(location, screenCoords);
 			
 	    	if (shadow) {
 	    		// Offset the shadow in the y axis as it is angled, so the base is at x=0
 	    		canvas.drawBitmap(mShadowIcon, screenCoords.x, screenCoords.y - mShadowIcon.getHeight(),null);
 	    	} else {
     			canvas.drawBitmap(mBubbleIcon, screenCoords.x - mBubbleIcon.getWidth()/2, screenCoords.y - mBubbleIcon.getHeight(),null);
 	    	}
     	}
     }
 
     private void drawInfoWindow(Canvas canvas, MapView	mapView, boolean shadow) {
     	
     	if ( mSelectedMapLocation != null) {
     		if ( shadow) {
     			//  Skip painting a shadow
     		} else {
 				//  First we need to determine the screen coordinates of the selected MapLocation
 				Point selDestinationOffset = new Point();
 				mapView.getProjection().toPixels(mSelectedMapLocation.getPoint(), selDestinationOffset);
 		    	
 		    	//  Setup the info window
 				int INFO_WINDOW_WIDTH = 175;
 				int INFO_WINDOW_HEIGHT = 40;
 				
 				RectF infoWindowRect = new RectF(0,0,INFO_WINDOW_WIDTH,INFO_WINDOW_HEIGHT);
 				
 				infoWindowOffsetX = selDestinationOffset.x-INFO_WINDOW_WIDTH/2;
 				infoWindowOffsetY = selDestinationOffset.y-INFO_WINDOW_HEIGHT-mBubbleIcon.getHeight();
 				infoWindowRect.offset(infoWindowOffsetX,infoWindowOffsetY);
 
 				//  Drawing the inner info window
 				canvas.drawRoundRect(infoWindowRect, 10, 10, getmInnerPaint());
 				
 				//  Drawing the border for info window
 				canvas.drawRoundRect(infoWindowRect, 10, 10, getmBorderPaint());
 					
 				//  Draw the MapLocation's name
 				int TEXT_OFFSET_X = 10;
 				int TEXT_OFFSET_Y = 15;
 
         int TEXT1_OFFSET_X = 20;
         int TEXT1_OFFSET_Y = 25;
 
 				//canvas.drawText(mSelectedMapLocation.getName(), infoWindowOffsetX+TEXT_OFFSET_X,infoWindowOffsetY+TEXT_OFFSET_Y, getmTextPaint());
         canvas.drawText("Ia, ÿ", infoWindowOffsetX+TEXT1_OFFSET_X,infoWindowOffsetY+TEXT1_OFFSET_Y, getmTextPaint());
         showWinInfo = true;
 			}
     	}
     }
 
 	public Paint getmInnerPaint() {
 		if ( mInnerPaint == null) {
 			mInnerPaint = new Paint();
 			mInnerPaint.setARGB(225, 50, 50, 50); //inner color
 			mInnerPaint.setAntiAlias(true);
 		}
 		return mInnerPaint;
 	}
 
 	public Paint getmBorderPaint() {
 		if ( mBorderPaint == null) {
 			mBorderPaint = new Paint();
 			mBorderPaint.setARGB(255, 255, 255, 255);
 			mBorderPaint.setAntiAlias(true);
 			mBorderPaint.setStyle(Style.STROKE);
 			mBorderPaint.setStrokeWidth(2);
 		}
 		return mBorderPaint;
 	}
 
 	public Paint getmTextPaint() {
 		if ( mTextPaint == null) {
 			mTextPaint = new Paint();
 			mTextPaint.setARGB(255, 255, 255, 255);
 			mTextPaint.setAntiAlias(true);
 		}
 		return mTextPaint;
 	}
 	
   public void SetPoint(GeoPoint G1, GeoPoint G2, GeoPoint G3, GeoPoint G4)
   {
       clearRange();
       
       gp.add(G1);
       gp.add(G2);
       gp.add(G3);
       gp.add(G4);
       
       ReadyShowRange = true;
   }	
 }
