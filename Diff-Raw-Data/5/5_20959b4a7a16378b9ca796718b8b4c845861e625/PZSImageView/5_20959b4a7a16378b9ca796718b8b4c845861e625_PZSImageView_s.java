 package com.huewu.lib.view;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.PointF;
 import android.util.AttributeSet;
 import android.util.FloatMath;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 
 /**
  * class Pinch Zoom & Swipe Image View.
  * @author huewu.yang
  * @date 2012. 08. 23
  */
 public class PZSImageView extends ImageView {
 
 	private static final String TAG = "GalleryImageView";
 	
 	public static final int PZS_ACTION_INIT = 100;
 	public static final int PZS_ACTION_SCALE = 1001;
 	public static final int PZS_ACTION_TRANSLATE = 1002;
 	public static final int PZS_ACTION_SCALE_TO_TRANSLATE = 1003;
 	public static final int PZS_ACTION_TRANSLATE_TO_SCALE = 1004;	
 	public static final int PZS_ACTION_FIT_CENTER = 1005;
 	public static final int PZS_ACTION_CANCEL = -1;
 	
 	//TODO below 3 values should be able to set from attributes.
 	private static final float MIN_SCALE_FACTOR = 0.5f;
 	private static final float MAX_SCALE_FACTOR = 2.f;
 	private static final long DOUBLE_TAP_MARGIN_TIME = 200;
 	
 	private boolean mIsFirstDraw = true;
 	private int mImageWidth;
 	private int mImageHeight;
 
 	public PZSImageView(Context context) {
 		super(context);
 		init();
 	}
 
 	public PZSImageView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public PZSImageView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 	private void init() {
 		setScaleType(ScaleType.MATRIX);
 		Matrix mat = getImageMatrix();
 		mat.reset();
 		setImageMatrix(mat);
 	}
 	
 	@Override
 	public void setImageBitmap(Bitmap bm) {
 		super.setImageBitmap(bm);
 
 		mIsFirstDraw = true;
 		mImageWidth = bm.getWidth();
 		mImageHeight = bm.getHeight();
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		
 		if( mIsFirstDraw  == true ){
 			mIsFirstDraw = false;
 			fitCenter();
 		}
 		
 		setImageMatrix(mCurrentMatrix);
 		canvas.drawRGB(200, 0, 0);
 		
 		super.onDraw(canvas);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		
 		int action = parseMotionEvent(event);
 		
 		switch(action){
 		case PZS_ACTION_INIT:
 			initGestureAction(event.getX(), event.getY());
 			break;
 		case PZS_ACTION_SCALE:
 			handleScale(event);
 			break;
 		case PZS_ACTION_TRANSLATE:
 			handleTranslate(event);
 			break;
 		case PZS_ACTION_TRANSLATE_TO_SCALE:
 			initGestureAction(event.getX(), event.getY());
 			break;
 		case PZS_ACTION_SCALE_TO_TRANSLATE:
 			int activeIndex = (event.getActionIndex() == 0 ? 1 : 0);
 			initGestureAction(event.getX(activeIndex), event.getY(activeIndex));
 			break;
 		case PZS_ACTION_FIT_CENTER:
 			fitCenter();
 			break;
 		case PZS_ACTION_CANCEL:
 			break;
 		}
 		return true; // indicate event was handled
 	}
 
 	private int parseMotionEvent(MotionEvent ev) {
 		
 		switch (ev.getAction() & MotionEvent.ACTION_MASK) {
 		case MotionEvent.ACTION_DOWN:
 			if( isDoubleTap(ev) )
 				return PZS_ACTION_FIT_CENTER;
 			else
 				return PZS_ACTION_INIT;
 		case MotionEvent.ACTION_POINTER_DOWN:
 			//more than one pointer is pressed...
 			return PZS_ACTION_TRANSLATE_TO_SCALE;
 		case MotionEvent.ACTION_UP:
 		case MotionEvent.ACTION_POINTER_UP:
 			if( ev.getPointerCount() == 2 ){
 				return PZS_ACTION_SCALE_TO_TRANSLATE;
 			}else{
 				return PZS_ACTION_INIT;
 			}
 		case MotionEvent.ACTION_MOVE:
 			if( ev.getPointerCount() == 1 )
 				return PZS_ACTION_TRANSLATE;
 			else if( ev.getPointerCount() == 2 )
 				return PZS_ACTION_SCALE;
 			return 0;
 		}
 		return 0;
 	}
 
 	/*
 	 * protected methods.
 	 */
 	
 	private Matrix mCurrentMatrix = new Matrix();
 	private Matrix mSavedMatrix = new Matrix();
 
 	// Remember some things for zooming
 	private PointF mStartPoint = new PointF();
 	private PointF mMidPoint = new PointF();
 	private float mOldDist = 1f;
 	
	private long mLastTocuhDownTime = 0;
 	private int mLastTouchPointerIndex = -1;
 	
 	/**
 	 * check 
 	 * @param current motion event.
 	 * @return true if user double tapped this view.
 	 */
 	protected boolean isDoubleTap(MotionEvent ev){
 
 		//TODO should check point index.
 		long downTime = ev.getDownTime();
 		long diff = downTime - mLastTocuhDownTime; 
 		Log.d(TAG, "TouchTime diff: " + diff);
 		mLastTocuhDownTime = downTime;
 		
 		return diff < DOUBLE_TAP_MARGIN_TIME;
 	}
 
 	protected void initGestureAction(float x, float y) {
 		mSavedMatrix.set(mCurrentMatrix);
 		mStartPoint.set(x, y);
 		mOldDist = 0.f;
 	}
 	
 	protected void handleScale(MotionEvent event){
 		float newDist = spacing(event);
 		if( mOldDist == 0.f ){
 			mOldDist = newDist;
 			midPoint(mMidPoint, event);
 			return;
 		}
 		
 		if (newDist > 2f) {
 			mCurrentMatrix.set(mSavedMatrix);
 			float scale = newDist / mOldDist;
 			mCurrentMatrix.postScale(scale, scale, mMidPoint.x, mMidPoint.y);
 			setImageMatrix(mCurrentMatrix);
 		}
 	}
 	
 	protected void handleTranslate(MotionEvent event){
 		mCurrentMatrix.set(mSavedMatrix);
 		mCurrentMatrix.postTranslate(event.getX() - mStartPoint.x,
 				event.getY() - mStartPoint.y);
 		setImageMatrix(mCurrentMatrix);
 	}
 
 	private long mLastTocuhDownTime = 0;
 	protected boolean isDoubleTab(MotionEvent ev){
 		long downTime = ev.getDownTime();
 		long diff = downTime - mLastTocuhDownTime; 
 		Log.d(TAG, "TouchTime diff: " + diff);
 		mLastTocuhDownTime = downTime;
 		
		
		return diff < DOUBLE_TAB_MARGIN;
 	}
 	
 	protected void handleScale(){
 //		float newDist = spacing(event);
 //		Log.d(TAG, "newDist=" + newDist);
 //		if (newDist > 2f) {
 //			mCurrentMatrix.set(mSavedMatrix);
 //			float scale = newDist / mOldDist;
 //			mCurrentMatrix.postScale(scale, scale, mMidPoint.x, mMidPoint.y);
 //		}
 	}
 	
 	protected void handleTranslate(){
 		
 	}
 	
 	protected void fitCenter(){
 		//move image to center....
 		mCurrentMatrix.reset();
 		
 		float scaleX = (getWidth() - getPaddingLeft() - getPaddingRight()) / (float)mImageWidth;
 		float scaleY = (getHeight() - getPaddingTop() - getPaddingBottom()) / (float)mImageHeight;
 		float scale = Math.min(scaleX, scaleY);
 		
 		float dx = (getWidth() - getPaddingLeft() - getPaddingRight() - mImageWidth * scale) / 2.f;
 		float dy = (getHeight() - getPaddingTop() - getPaddingBottom() - mImageHeight * scale) / 2.f;
 		
 		mCurrentMatrix.postScale(scale, scale);
 		mCurrentMatrix.postTranslate(dx, dy);
 		setImageMatrix(mCurrentMatrix);
 	}
 	
 	/** Determine the space between the first two fingers */
 	private float spacing(MotionEvent event) {
 		// ...
 		float x = event.getX(0) - event.getX(1);
 		float y = event.getY(0) - event.getY(1);
 		return FloatMath.sqrt(x * x + y * y);
 	}
 
 	/** Calculate the mid point of the first two fingers */
 	private void midPoint(PointF point, MotionEvent event) {
 		// ...
 		float x = event.getX(0) + event.getX(1);
 		float y = event.getY(0) + event.getY(1);
 		point.set(x / 2, y / 2);
 	}
 
 }//end of class
