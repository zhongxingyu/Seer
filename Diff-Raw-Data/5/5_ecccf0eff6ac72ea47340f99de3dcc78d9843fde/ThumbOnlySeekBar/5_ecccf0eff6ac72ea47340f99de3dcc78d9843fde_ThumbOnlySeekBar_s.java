 package com.krystal.videocohere;
 
 import android.content.Context;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.widget.SeekBar;
 
 public class ThumbOnlySeekBar extends SeekBar {
 
 	private Drawable mThumb;
 	private String TAG = "ThumbOnlySeekBar";
 
 	public ThumbOnlySeekBar(Context context) {
 		super(context);
 	}
 
 	public ThumbOnlySeekBar(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 
 	public ThumbOnlySeekBar(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 
 	@Override
 	public void setThumb(Drawable thumb) {
 		Log.d("Swati", "Entered set thumb");
 		super.setThumb(thumb);
 		mThumb = thumb;
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		Log.d("Swati", "Event x = " + (int) event.getX() + " Event y = "
 				+ (int) event.getY());
 		Log.d ("Swati", "Bounds = " + mThumb.getBounds());
 		Rect mBounds = mThumb.getBounds();
		Rect newBounds = new Rect (mBounds.left - 10, mBounds.top - 10, mBounds.bottom +10,mBounds.right +10 );
 		Log.d ("Swati", "New Bounds = " + newBounds.toString());
 		
		if (!mBounds
 				.contains((int) event.getX(), (int) event.getY())) {
 			Log.d (TAG , "Returning false");
 			return false;
 		}
 		Log.d (TAG , "Returning super");
 		return super.onTouchEvent(event);
 	}
 }
