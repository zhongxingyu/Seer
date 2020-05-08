 /*
  * TouchImageView.java
  * By: Michael Ortiz
  * Updated By: Patrick Lackemacher
  * -------------------
  * Extends Android ImageView to include pinch zooming and panning.
  */
 
 package com.rndapp.t;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.graphics.PointF;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.View;
 import android.widget.ImageView;
 
 public class TouchImageView extends ImageView {
 
     Matrix matrix = new Matrix();
 
     // We can be in one of these 3 states
     static final int NONE = 0;
     static final int DRAG = 1;
     static final int DRAGGED = 2;
     static final int ZOOM = 3;
     int mode = NONE;
 
     // Remember some things for zooming
     PointF last = new PointF();
     PointF start = new PointF();
     float minScale = 1f;
     float maxScale = 10f;
     float[] m;
     
     float redundantXSpace, redundantYSpace;
     
     float width, height;
     static final int CLICK = 3;
     float saveScale = 1f;
     float right, bottom, origWidth, origHeight, bmWidth, bmHeight;
     
     boolean lastDTapWasZIn = false;
     PointF lastDTapCenter;
     
     ScaleGestureDetector mScaleDetector;
     GestureDetector mDetector;
     
     Context context;
 
     public TouchImageView(Context context) {
         super(context);
         sharedConstructing(context);
     }
     
     public TouchImageView(Context context, AttributeSet attrs) {
     	super(context, attrs);
     	sharedConstructing(context);
     }
     
     private void sharedConstructing(Context cont) {
     	super.setClickable(true);
         this.context = cont;
         setScaleType(ScaleType.MATRIX);
        matrix = this.getImageMatrix();
         mScaleDetector = new ScaleGestureDetector(cont, new ScaleListener());
        mDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
             @Override
             public boolean onDown(MotionEvent motionEvent) {
                 return false;
             }
 
             @Override
             public void onShowPress(MotionEvent motionEvent) {
 
             }
 
             @Override
             public boolean onSingleTapUp(MotionEvent motionEvent) {
                 return false;
             }
 
             @Override
             public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
                 return false;
             }
 
             @Override
             public void onLongPress(MotionEvent motionEvent) {
 
             }
 
             @Override
             public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
                 return false;
             }
        });
         mDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener(){
 
             @Override
             public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                 return false;
             }
 
             @Override
             public boolean onDoubleTap(MotionEvent motionEvent) {
                 Log.d("TouchImageView", "double tap heard");
                 PointF curr = new PointF(motionEvent.getX(), motionEvent.getY());
                 if (!lastDTapWasZIn){
                     (new ScaleListener()).scaleIt(3f,curr.x,curr.y);
                     lastDTapCenter = curr;
                     lastDTapWasZIn = true;
                 }else {
                     (new ScaleListener()).scaleIt(.33f,lastDTapCenter.x,lastDTapCenter.y);
                     lastDTapWasZIn = false;
                 }
                 return true;
             }
 
             @Override
             public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                 return false;
             }
         });
         matrix.setTranslate(1f, 1f);
         m = new float[9];
         setImageMatrix(matrix);
 
         setOnTouchListener(new DoubleTapPinchZoomListener());
     }
 
     @Override
     public void setImageBitmap(Bitmap bm) { 
         super.setImageBitmap(bm);
         if(bm != null) {
             if (context.getApplicationInfo().targetSdkVersion > 17){
                 bmWidth = bm.getWidth();
                 bmHeight = bm.getHeight();
             } else {
         	    bmWidth = bm.getWidth();
         	    bmHeight = bm.getHeight();
             }
         }
     }
     
     public void setMaxZoom(float x)
     {
     	maxScale = x;
     }
     
     private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
     	@Override
     	public boolean onScaleBegin(ScaleGestureDetector detector) {
     		mode = ZOOM;
     		return true;
     	}
     	
 		@Override
 	    public boolean onScale(ScaleGestureDetector detector) {
 			float mScaleFactor = detector.getScaleFactor();
 		 	scaleIt(mScaleFactor, detector.getFocusX(), detector.getFocusY());
 	        return true;
 	    }
 		
 		public void scaleIt(float mScaleFactor, float focusx, float focusy){
 			float origScale = saveScale;
 	        saveScale *= mScaleFactor;
 	        if (saveScale > maxScale) {
 	        	saveScale = maxScale;
 	        	mScaleFactor = maxScale / origScale;
 	        } else if (saveScale < minScale) {
 	        	saveScale = minScale;
 	        	mScaleFactor = minScale / origScale;
 	        }
         	right = width * saveScale - width - (2 * redundantXSpace * saveScale);
             bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
         	if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
         		matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
             	if (mScaleFactor < 1) {
             		matrix.getValues(m);
             		float x = m[Matrix.MTRANS_X];
                 	float y = m[Matrix.MTRANS_Y];
                 	if (mScaleFactor < 1) {
         	        	if (Math.round(origWidth * saveScale) < width) {
         	        		if (y < -bottom)
             	        		matrix.postTranslate(0, -(y + bottom));
         	        		else if (y > 0)
             	        		matrix.postTranslate(0, -y);
         	        	} else {
 	                		if (x < -right) 
 	        	        		matrix.postTranslate(-(x + right), 0);
 	                		else if (x > 0) 
 	        	        		matrix.postTranslate(-x, 0);
         	        	}
                 	}
             	}
         	} else {
             	matrix.postScale(mScaleFactor, mScaleFactor, focusx, focusy);
             	matrix.getValues(m);
             	float x = m[Matrix.MTRANS_X];
             	float y = m[Matrix.MTRANS_Y];
             	if (mScaleFactor < 1) {
     	        	if (x < -right) 
     	        		matrix.postTranslate(-(x + right), 0);
     	        	else if (x > 0) 
     	        		matrix.postTranslate(-x, 0);
     	        	if (y < -bottom)
     	        		matrix.postTranslate(0, -(y + bottom));
     	        	else if (y > 0)
     	        		matrix.postTranslate(0, -y);
             	}
         	}
 		}
 	}
     
     @Override
     protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
     {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         width = MeasureSpec.getSize(widthMeasureSpec);
         height = MeasureSpec.getSize(heightMeasureSpec);
         //Fit to screen.
         float scale;
         float scaleX =  (float)width / (float)(bmWidth);
         float scaleY = (float)height / (float)(bmHeight);
         scale = Math.min(scaleX, scaleY);
         matrix.setScale(scale, scale);
         setImageMatrix(matrix);
         saveScale = 1f;
 
         // Center the image
         redundantYSpace = (float)height - (scale * (float)bmHeight) ;
         redundantXSpace = (float)width - (scale * (float)bmWidth);
         redundantYSpace /= (float)2;
         redundantXSpace /= (float)2;
 
         matrix.postTranslate(redundantXSpace, redundantYSpace);
         
         origWidth = width - 2 * redundantXSpace;
         origHeight = height - 2 * redundantYSpace;
         right = width * saveScale - width - (2 * redundantXSpace * saveScale);
         bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
         setImageMatrix(matrix);
     }
     
     class DoubleTapPinchZoomListener implements OnTouchListener {
     	private long lastTouchTime = -1;
     	
         @Override
         public boolean onTouch(View v, MotionEvent event) {
         	mScaleDetector.onTouchEvent(event);
 
             mDetector.onTouchEvent(event);
 
         	matrix.getValues(m);
         	float x = m[Matrix.MTRANS_X];
         	float y = m[Matrix.MTRANS_Y];
         	PointF curr = new PointF(event.getX(), event.getY());
         	
         	switch (event.getAction()) {
             	case MotionEvent.ACTION_DOWN:
                     last.set(event.getX(), event.getY());
                     start.set(last);
                     mode = DRAG;
                     break;
             	case MotionEvent.ACTION_MOVE:
             		if (mode == DRAG) {
             			float deltaX = curr.x - last.x;
             			float deltaY = curr.y - last.y;
             			float scaleWidth = Math.round(origWidth * saveScale);
             			float scaleHeight = Math.round(origHeight * saveScale);
         				if (scaleWidth < width) {
             				deltaX = 0;
             				if (y + deltaY > 0)
 	            				deltaY = -y;
             				else if (y + deltaY < -bottom)
 	            				deltaY = -(y + bottom); 
         				} else if (scaleHeight < height) {
             				deltaY = 0;
             				if (x + deltaX > 0)
 	            				deltaX = -x;
 	            			else if (x + deltaX < -right)
 	            				deltaX = -(x + right);
         				} else {
             				if (x + deltaX > 0)
 	            				deltaX = -x;
 	            			else if (x + deltaX < -right)
 	            				deltaX = -(x + right);
 	            			
             				if (y + deltaY > 0)
 	            				deltaY = -y;
 	            			else if (y + deltaY < -bottom)
 	            				deltaY = -(y + bottom);
             			}
                     	matrix.postTranslate(deltaX, deltaY);
                     	last.set(curr.x, curr.y);
                     }
             		break;
             		
 //            	case MotionEvent.ACTION_UP:
 //            		int xDiff = (int) Math.abs(curr.x - start.x);
 //                    int yDiff = (int) Math.abs(curr.y - start.y);
 //                	long thisTime = System.currentTimeMillis();
 //                    if (xDiff < CLICK && yDiff < CLICK){
 //                        performClick();
 //              	      	if (thisTime - lastTouchTime < 250000) {
 //              	      		// Double tap
 //              	      		lastTouchTime = -1;
 //              	      		if (!lastDTapWasZIn){
 //              	      			(new ScaleListener()).scaleIt(3f,curr.x,curr.y);
 //              	      			lastDTapCenter = curr;
 //              	      			lastDTapWasZIn = true;
 //              	      		}else {
 //              	      			(new ScaleListener()).scaleIt(.33f,lastDTapCenter.x,lastDTapCenter.y);
 //              	      			lastDTapWasZIn = false;
 //              	      		}
 //              	     	 } else {
 //              	     		 // Too slow :)
 //              	     		 lastTouchTime = thisTime;
 //              	     	 }
 //                    }
 //            		mode = NONE;
 //            		break;
             		
             	case MotionEvent.ACTION_POINTER_UP:
             		mode = NONE;
             		break;
         	}
             setImageMatrix(matrix);
             invalidate();
             return true; // indicate event was handled
         }
     }
 }
