 package org.dyndns.pawitp.salayatrammap.map;
 
 import android.content.Context;
 import android.graphics.Matrix;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 import android.widget.Scroller;
 
 public class MapView extends ImageView {
 	
 	private static final float DEFAULT_ZOOM = 0.8F;
 	
 	private Scroller mScroller;
 	
 	public MapView(Context context) {
 		super(context);
 		init();
 	}
 
 	public MapView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 	public MapView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 	
 	public void init() {
 		mScroller = new Scroller(getContext());
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		if (event.getPointerCount() == 1) {
 			return mGestureDetector.onTouchEvent(event);
 		}
 		else {
 			return false; // TODO: Multitouch
 		}
 	}
 
 	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		
 		// Zoom the map out by default and center it
 		// Cannot be done in the constructor because the size of the view is not known yet
 		Matrix matrix = new Matrix();
 		float scale = findFullscreenScale();
 		matrix.setScale(scale, scale);
 		
 		float width = -(getDrawable().getIntrinsicWidth() * scale - getWidth()) / 2;
 		float height = -(getDrawable().getIntrinsicHeight() * scale - getHeight()) / 2;
 		matrix.postTranslate(width, height);
 		
 		setImageMatrix(matrix);
 	}
 
 	@Override
 	public void computeScroll() {
 		if (mScroller.computeScrollOffset()) {
 			Matrix matrix = new Matrix(getImageMatrix());
 			
 			float[] values = new float[9];
 			matrix.getValues(values);
 			
 			matrix.postTranslate(-values[Matrix.MTRANS_X] + mScroller.getCurrX(),
 								 -values[Matrix.MTRANS_Y] + mScroller.getCurrY());
 			
 			setImageMatrix(matrix);
 			
 			invalidate();
 		}
 	}
 	
 	// TODO: Restore state on orientation change
 	
 	GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
 
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 			Matrix matrix = new Matrix(getImageMatrix());
 			
 			float[] values = new float[9];
 			matrix.getValues(values);
 			
 			float scale;
 			if (values[Matrix.MSCALE_X] < DEFAULT_ZOOM - 0.01F /* floating point inaccuracy */) { // scale x == scale y
 				scale = DEFAULT_ZOOM;
 			}
 			else {
 				scale = findFullscreenScale();
 			}
 			
 			float scaleDiff = scale / values[Matrix.MSCALE_X];
 			matrix.postScale(scaleDiff, scaleDiff); // TODO: Animation
 			
 			float hWidth = getWidth() / 2;
 			float hHeight = getHeight() / 2;
 			float transX = (e.getX() > hWidth) ? -e.getX() * scaleDiff + hWidth : hWidth - e.getX() * scaleDiff;
 			float transY = (e.getY() > hHeight) ? -e.getY() * scaleDiff + hHeight : hHeight - e.getY() * scaleDiff;
 			matrix.postTranslate(transX, transY);
 			
 			checkEdges(matrix);
 			
 			setImageMatrix(matrix);
 			return true;
 		}
 
 		@Override
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			Matrix matrix = new Matrix(getImageMatrix());
 			matrix.postTranslate(-distanceX, -distanceY);
 			
 			checkEdges(matrix);
 			
 			setImageMatrix(matrix);
 			return true;
 		}
 
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			float[] values = new float[9];
 			getImageMatrix().getValues(values);
 			
 			int minWidth = (int) (-getDrawable().getIntrinsicWidth() * values[Matrix.MSCALE_X]) + getWidth();
 			int minHeight = (int) (-getDrawable().getIntrinsicHeight() * values[Matrix.MSCALE_Y]) + getHeight();
 			
 			mScroller.fling((int) values[Matrix.MTRANS_X], (int) values[Matrix.MTRANS_Y],
 							(int) velocityX, (int) velocityY, minWidth, 0, minHeight, 0);
 			
 			invalidate();
 			
 			return true;
 		}
 
 		@Override
 		public boolean onDown(MotionEvent e) {
 			if (!mScroller.isFinished()) { // Abort fling on user touch
 				mScroller.abortAnimation();
 			}
 			return true;
 		}
 		
 	});
 	
 	private float findFullscreenScale() {
 		// Find a scale such that the image fills the view
 		float scaleHeight = getHeight() / (float) getDrawable().getIntrinsicHeight();
 		float scaleWidth = getWidth() / (float) getDrawable().getIntrinsicWidth();
 		return Math.max(scaleHeight, scaleWidth);
 	}
 	
 	private void checkEdges(Matrix matrix) {
 		float[] values = new float[9];
 		matrix.getValues(values);
 		
 		if (values[Matrix.MTRANS_X] > 0) {
 			matrix.postTranslate(-values[Matrix.MTRANS_X], 0);
 		}
 		
 		float maxWidth = -getDrawable().getIntrinsicWidth() * values[Matrix.MSCALE_X] + getWidth();
 		if (values[Matrix.MTRANS_X] < maxWidth) {
 			matrix.postTranslate(maxWidth - values[Matrix.MTRANS_X], 0);
 		}
 		
 		if (values[Matrix.MTRANS_Y] > 0) {
 			matrix.postTranslate(0, -values[Matrix.MTRANS_Y]);
 		}
 		
 		float maxHeight = -getDrawable().getIntrinsicHeight() * values[Matrix.MSCALE_X] + getHeight();
 		if (values[Matrix.MTRANS_Y] < maxHeight) {
 			matrix.postTranslate(0, maxHeight - values[Matrix.MTRANS_Y]);
 		}
 	}
 
 }
