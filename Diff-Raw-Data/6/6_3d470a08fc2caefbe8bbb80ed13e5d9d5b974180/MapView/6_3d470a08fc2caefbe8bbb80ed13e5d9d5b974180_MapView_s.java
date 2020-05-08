 package org.dyndns.pawitp.salayatrammap.map;
 
 import android.content.Context;
 import android.graphics.Matrix;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.widget.ImageView;
 
 public class MapView extends ImageView {
 	
 	private static final float DEFAULT_ZOOM = 0.8F;
 	
 	public MapView(Context context) {
 		super(context);
 	}
 
 	public MapView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 
 	public MapView(Context context, AttributeSet attrs) {
 		super(context, attrs);
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
 				float scaleHeight = getHeight() / (float) getDrawable().getIntrinsicHeight();
 				float scaleWidth = getWidth() / (float) getDrawable().getIntrinsicWidth();
 				scale = Math.max(scaleHeight, scaleWidth);
 			}
 			
 			float scaleDiff = scale / values[Matrix.MSCALE_X];
 			matrix.postScale(scaleDiff, scaleDiff); // TODO: Animation
 			
			float transX = (-e.getX() + getWidth() / 2) * scale;
			float transY = (-e.getY() + getHeight() / 2) * scale;
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
 		public boolean onDown(MotionEvent e) {
 			return true; // Required so that events are triggered
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
 	});
 
 }
