 package com.cateye.ui.android;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.PointF;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 
 import com.cateye.core.IPreciseBitmap;
 import com.cateye.core.jni.PreciseBitmap;
 
 public class PreciseBitmapView extends View
 {
 	private IPreciseBitmap pb;
 
 	private void startUpdater()
 	{
 		if (updatingThread == null || !updatingThread.isAlive())
 		{
 			updatingThread = new Thread(new Runnable() {
 				
 				public void run() 
 				{
 					while (PreciseBitmapView.this.isShown())
 					{
						bitmapWidth = getWidth();
						bitmapHeight = getHeight();

 						if (pb != null && bitmapWidth > 0 && bitmapHeight > 0)
 						{
 							float deltaXOld = deltaX;
 							float deltaYOld = deltaY;
 							panX -= deltaXOld;
 							panY -= deltaYOld;
 							
 							Bitmap newImage = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
 							pixels = pb.getPixels(pixels, (int)(panX), (int)(panY), bitmapWidth, bitmapHeight, 500);
 							if (newImage != null && !newImage.isRecycled())
 							{
 								newImage.setPixels(pixels, 0, bitmapWidth, 0, 0, bitmapWidth, bitmapHeight);
 							}
 							
 					       	synchronized (this) 
 					       	{
 						       	image = newImage;
 								deltaX -= deltaXOld;
 								deltaY -= deltaYOld;
 								fingerStartPosition = new PointF(currentFingerPosition.x - deltaX,
 								                                 currentFingerPosition.y - deltaY);
 							}
 					       	PreciseBitmapView.this.postInvalidate();
 						}
 					}
 					Log.i("PreciseBitmapView", "Updating thread stopped");
 				}
 			});
 			
 			updatingThread.start();
 		}
 		
 	}
 	public void setPreciseBitmap(IPreciseBitmap value) 
 	{
 		pb = value; 
 	}
 	
 	@Override
 	protected void onDetachedFromWindow() {
 		// TODO Auto-generated method stub
 		super.onDetachedFromWindow();
 	}
 	
     public PreciseBitmapView(Context context, AttributeSet attrs) 
     {
         super(context, attrs);
         startUpdater();
     }
     
     @Override
     protected void onVisibilityChanged(View changedView, int visibility) 
     {
     	super.onVisibilityChanged(changedView, visibility);
     	if (visibility == View.VISIBLE)
     	{
     		startUpdater();
     	}
     	else
     	{
     		try {
         		Log.i("PreciseBitmapView", "Joining updater...");
 				updatingThread.join();
 	    		Log.i("PreciseBitmapView", "Joined.");
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     	}
     }
     
     private volatile float deltaX, deltaY;
     private volatile float panX, panY;
     private volatile PointF fingerStartPosition = new PointF(0,0), currentFingerPosition = new PointF(0,0);
     private volatile Bitmap image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
     private volatile int[] pixels;
     private volatile int bitmapWidth = 1, bitmapHeight = 1;
     private Thread updatingThread = null;
     
 	@Override
 	protected void onDraw(Canvas canvas) 
 	{
 		/*
 		Paint pnt = new Paint();
 		pnt.setStyle(Paint.Style.FILL_AND_STROKE);
 		pnt.setARGB(128, 128, 128, 192);
 		canvas.drawLine(0, 0, getWidth(), getHeight(), pnt);
 		canvas.drawBitmap(image, 0, 0, null);*/
 		
         synchronized (this)
         {
         	canvas.drawBitmap(image, deltaX, deltaY, null);
         }
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) 
 	{
		if (event.getActionMasked() == MotionEvent.ACTION_DOWN ||
		    event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)
		{
	        synchronized (this)
	        {
	        	fingerStartPosition = new PointF(event.getX(0), event.getY(0));
	        	currentFingerPosition = fingerStartPosition;
	        }
			return true;
		}
		else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)
 		{
 	        synchronized (this)
 	        {
	        	int k = 0;
	        	if (k == event.getActionIndex()) k++;
	        	fingerStartPosition = new PointF(event.getX(k), event.getY(k));
 	        	currentFingerPosition = fingerStartPosition;
 	        }
 			return true;
 		}
 		else if (event.getActionMasked() == MotionEvent.ACTION_MOVE)
 		{
 	        synchronized (this)
 	        {
 				currentFingerPosition = new PointF(event.getX(), event.getY());
 				deltaX = currentFingerPosition.x - fingerStartPosition.x;
 				deltaY = currentFingerPosition.y - fingerStartPosition.y;
 	        }
 			
 			invalidate();
 			return true;
 		}
 		else if (event.getActionMasked() == MotionEvent.ACTION_UP)
 		{
 			invalidate();
 			return true;
 		}

		
 		return super.onTouchEvent(event);
 	}
 
 }
