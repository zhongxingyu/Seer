 package com.additt.custom_views;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.PointF;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 
 
 public class LogoView extends ImageView implements OnTouchListener {	
 	
 	private PointF mPrevTouch;
 	
 	private Bitmap mCurrLogo;
 	private Bitmap mCurrAd;
 	private float mRatio;
 
 
 	private ScaleGestureDetector mScaleDetector;
 	private float mScaleFactor = 1.f;
 	
 
 	// Scaled size of logo bitmap
 	private RectF mLogoRect;
 	
 	public LogoView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
 		mLogoRect = new RectF();
 	}
 	
 	/**
 	 * Sets the ad. Also takes a ratio so that we can scale the logo to the same size later.
 	 * @param ad
 	 * @param ratio
 	 */
 	public void setAd(Bitmap ad, float ratio) {
 		setImageBitmap(ad);
 		mCurrAd = ad;
 				
 		mRatio = ratio;
 	}
 
 	/**
 	 * Sets the logo to a specific x and y value. Will keep the same scale for the logo
 	 * @param logo
 	 * @param x
 	 * @param y
 	 */
 	public void setLogo(Bitmap logo) {
 		// Only create new rect if we are adding an ad for the first time. Else just set the new x and y
 		
 		
 		float x = 0f;
 		float y = 0f;
 		
 		if (mCurrLogo == null) {
 			// getPaddingTop/Left return twice the actual padding (god knows why).
 			x += (this.getPaddingLeft() / 2);
 			y += (this.getPaddingTop() / 2);
 		} else {
 			x = mLogoRect.left;
 			y = mLogoRect.top;
 		}
 		mLogoRect.set(x, y, x + mRatio * logo.getWidth(), y + mRatio * logo.getHeight());
 		mCurrLogo = logo;
 		invalidate();
 	}
 	
 	/**
 	 * Generates scaled logo based on the gesture tracking scale factor.
 	 * @return
 	 */
 	private RectF generateScaledLogo() {
 		RectF scaledRect = new RectF(mLogoRect);
 		
 		float deltaWidth = mLogoRect.width() - mLogoRect.width()*mScaleFactor;
 		float deltaHeight = mLogoRect.height() - mLogoRect.height()*mScaleFactor;
 		
 		scaledRect.inset(deltaWidth, deltaHeight);
 		return scaledRect;
 	}
 	
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		if (mCurrLogo != null) {
 			canvas.drawBitmap(mCurrLogo, null, generateScaledLogo(), null);
 		}
 	}
 	/**
 	 * When it detects a scaling gesture, it will scale. Moves the logo if there is only
 	 * one pointer and it's on the logo.
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
 	    // Let the ScaleGestureDetector inspect all events.
 	    mScaleDetector.onTouchEvent(ev);
 	    
 	    if (ev.getPointerCount() == 1 && mLogoRect.contains((int)ev.getX(), (int)ev.getY())) {
 		    switch (ev.getAction()) {
 			    case MotionEvent.ACTION_DOWN:
 			    	if (mPrevTouch == null) {
 			    		mPrevTouch = new PointF(ev.getX(), ev.getY());
 			    	} else {
 			    		mPrevTouch.set(ev.getX(), ev.getY());	
 		    		}
 			    	break;
 				case MotionEvent.ACTION_MOVE:
 					if (mPrevTouch == null) {
 						mPrevTouch = new PointF(ev.getX(), ev.getY());
 					} else {
 						moveLogo(ev.getX() - mPrevTouch.x, ev.getY() - mPrevTouch.y);
 						mPrevTouch.set(ev.getX(), ev.getY());
 					}
 		    		
 					break;
 				case MotionEvent.ACTION_CANCEL:
 					break;
 				case MotionEvent.ACTION_UP:
 					break;
 				default:
 					break;
 		    }
 	    }
 	    return true;
 	}
 	
 	
 	public void moveLogo(float deltaX, float deltaY) {
 		mLogoRect.offset(deltaX, deltaY);	
 		invalidate();
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		return false;
 	}
 	
 	/** Combines the set logo with the current ad
 	 * and returns the result. If a logo hasn't yet been
 	 * set this function returns null
 	 * @return The ad with the logo embedded. Null if no logo set
 	 */
 	public Bitmap addLogo() {
 		// if a logo hasn't yet been set, return null
 		if (mCurrLogo == null) return null;
 		
 
 		
         Bitmap newAd = Bitmap.createBitmap(mCurrAd.getWidth(), mCurrAd.getHeight(), mCurrAd.getConfig());
         Canvas canvas = new Canvas(newAd);
         canvas.drawBitmap(mCurrAd, new Matrix(), null);
         
         
         // We must scale down the logo now that we aren't displaying it on a scaled Ad.
         RectF scaledLogo = generateScaledLogo();
         RectF scaledDown = new RectF();
         
        float scaledLeft = ((scaledLogo.left - (this.getPaddingLeft() / 2)) / mRatio) ;
        float scaledTop = ((scaledLogo.top - (this.getPaddingTop() / 2))/ mRatio);
         float scaledRight = scaledLeft + (scaledLogo.width() / mRatio);
         float scaledBottom = scaledTop + (scaledLogo.height() / mRatio);
         scaledDown.set(scaledLeft, scaledTop, scaledRight, scaledBottom);
         canvas.drawBitmap(mCurrLogo, null, scaledDown, null);
 
 
 		return newAd;
 	}
 	
 	/**
 	 * Android has built in scale listener.
 	 * 
 	 *
 	 */
 	private class ScaleListener 
         extends ScaleGestureDetector.SimpleOnScaleGestureListener {
 	    @Override
 	    public boolean onScale(ScaleGestureDetector detector) {
 	        mScaleFactor *= detector.getScaleFactor();
 	
 	        // Don't let the object get too small or too large.
 	        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
 	        
 	        invalidate();
 	        return true;
 	    }
 	}
 }
