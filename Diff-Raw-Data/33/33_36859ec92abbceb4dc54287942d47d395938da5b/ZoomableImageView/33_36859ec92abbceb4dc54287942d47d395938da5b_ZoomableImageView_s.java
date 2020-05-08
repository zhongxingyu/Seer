 /**
  * @author Mario Velasco Casquero
  * @version 2.00
  */
 
 package com.gloria.offlineexperiments;
 
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.PointF;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.ScaleGestureDetector;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ZoomableImageView extends ImageView {
 
 	public interface ZoomableImageViewListener {
 		public void onWolfNumberUpdated(int newValue);
 	};
 	
 	Matrix matrix;
 
 	// We can be in one of these 3 states
 	static final int NONE = 0;
 	static final int DRAG = 1;
 	static final int ZOOM = 2;
 	static final int DRAG_PIN = 3;
 	int mode = NONE;
 
 	// Remember some things for zooming
 	PointF last = new PointF();
 	PointF start = new PointF();
 	float minScale = 1f;
 	float maxScale = 3f;
 	float[] m;
 
 
 	int viewWidth, viewHeight;
 	static final int CLICK = 3;
 	float saveScale = 1f;
 	protected float origWidth, origHeight;
 	int oldMeasuredWidth, oldMeasuredHeight;
 	float redundantYSpace, redundantXSpace;
 	float scale = 1; // scale used to fit the image to the screen size;
 
 	private ZoomableImageViewListener listener = null;
 	
 	ScaleGestureDetector mScaleDetector;
 
 	Context context;
 
 	private ArrayList<ZoomablePinView> pins = new ArrayList<ZoomablePinView>();
 	private int selectedPin = -1;
 	private int dragginPin = -1;
 	// Maximum distance between the clicked point and the pin, to be selected
 	private static final int SELECTION_DISTANCE = 0;
 
 	// Center of the focused area in pixels
 	private PointF centerFocus = new PointF();
 	// Center point of TouchImageView
	private final PointF centerPointView = new PointF();
 
 	private int wolfNumber = 0;
 	//observatory factor or the personal reduction coefficient
 	private static final int K = 1;
 
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 	}
 
 	public ZoomableImageView(Context context) {
 		super(context);
 		sharedConstructing(context);
 	}
 
 	public ZoomableImageView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		sharedConstructing(context);
 	}
 
 	public ZoomableImageViewListener getListener() {
 		return listener;
 	}
 
 	public void setListener(ZoomableImageViewListener listener) {
 		this.listener = listener;
 	}
 
 	private void sharedConstructing(Context context) {
 		super.setClickable(true);
 		this.context = context;
 		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
 		matrix = new Matrix();
 		m = new float[9];
 		setImageMatrix(matrix);
 		setScaleType(ScaleType.MATRIX);
 
 		setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mScaleDetector.onTouchEvent(event);
 				PointF curr = new PointF(event.getX(), event.getY());
 
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					dragginPin = searchClosestPin(curr.x, curr.y);
 					if (dragginPin > -1) {
 						mode = DRAG_PIN;
 						last.set(pins.get(dragginPin).getPosX(), pins.get(dragginPin).getPosY());
 					}
 					else {
 						mode = DRAG;
 						last.set(curr);
 					}
 					start.set(curr);
 					break;
 
 				case MotionEvent.ACTION_MOVE:
 					if (mode == DRAG) {
 						float deltaX = curr.x - last.x;
 						float deltaY = curr.y - last.y;
 						float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);
 						float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);
 						matrix.postTranslate(fixTransX, fixTransY);
 						fixTrans();
 						last.set(curr.x, curr.y);
 						moveCenterFocusOnDrag(fixTransX, fixTransY);
 						movePinsOnDrag(fixTransX, fixTransY);
 					}
 					else if (mode == DRAG_PIN) {
 						if (isInTheImage(curr.x, curr.y))
 							pins.get(dragginPin).setPosition(curr.x, curr.y, centerPointView, 
 									centerFocus, saveScale, scale, redundantXSpace, redundantYSpace);
 					}
 					break;
 
 				case MotionEvent.ACTION_UP:
 					int xDiff = (int) Math.abs(curr.x - start.x);
 					int yDiff = (int) Math.abs(curr.y - start.y);
 					if (mode == DRAG_PIN) {
 						selectPin(dragginPin);
 						if (xDiff < CLICK && yDiff < CLICK)
 							pins.get(dragginPin).setPosition(last.x, last.y, centerPointView, 
 									centerFocus, saveScale, scale, redundantXSpace, redundantYSpace);
 					}
 					else {
 						if (xDiff < CLICK && yDiff < CLICK) {
 							performClick();
 							if (isInTheImage(curr.x, curr.y))
 								addPin(curr.x, curr.y, centerPointView, centerFocus, saveScale, 
 										scale, redundantXSpace, redundantYSpace);
 						}
 					}
 					mode = NONE;
 					break;
 
 				case MotionEvent.ACTION_POINTER_UP:
 					mode = NONE;
 					break;
 				}
 
 				setImageMatrix(matrix);
 				invalidate();
 				return true; // indicate event was handled
 			}
 
 		});
 	}
 
 	public void setMaxZoom(float x) {
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
 			float origScale = saveScale;
 			saveScale *= mScaleFactor;
 			if (saveScale > maxScale) {
 				saveScale = maxScale;
 				mScaleFactor = maxScale / origScale;
 			} else if (saveScale < minScale) {
 				saveScale = minScale;
 				mScaleFactor = minScale / origScale;
 			}
 
 			if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight) {
 				matrix.postScale(mScaleFactor, mScaleFactor, centerPointView.x, centerPointView.y);
 				moveCenterFocusOnZoom(centerPointView.x, centerPointView.y, mScaleFactor);
 				movePinsOnZoom(centerPointView.x, centerPointView.y, mScaleFactor);
 			}
 			else {
 				matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
 				moveCenterFocusOnZoom(detector.getFocusX(), detector.getFocusY(), mScaleFactor);
 				movePinsOnZoom(detector.getFocusX(), detector.getFocusY(), mScaleFactor);
 			}
 			fixTrans();
 			return true;
 		}
 	}
 
 	void fixTrans() {
 		matrix.getValues(m);
 		float transX = m[Matrix.MTRANS_X];
 		float transY = m[Matrix.MTRANS_Y];
 
 		float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
 		float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);
 
 		if (fixTransX != 0 || fixTransY != 0) {
 			matrix.postTranslate(fixTransX, fixTransY);
 			moveCenterFocusOnDrag(fixTransX, fixTransY);
 			movePinsOnDrag(fixTransX, fixTransY);
 		}
 	}
 
 	float getFixTrans(float trans, float viewSize, float contentSize) {
 		float minTrans, maxTrans;
 
 		if (contentSize <= viewSize) {
 			minTrans = 0;
 			maxTrans = viewSize - contentSize;
 		} else {
 			minTrans = viewSize - contentSize;
 			maxTrans = 0;
 		}
 
 		if (trans < minTrans)
 			return -trans + minTrans;
 		else if (trans > maxTrans)
 			return -trans + maxTrans;
 		else if (contentSize <= viewSize && mode == ZOOM)
 			return ((minTrans + maxTrans) / 2) - trans;
 		return 0;
 	}
 
 	float getFixDragTrans(float delta, float viewSize, float contentSize) {
 		if (contentSize <= viewSize) {
 			return 0;
 		}
 		return delta;
 	}
 
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		viewWidth = MeasureSpec.getSize(widthMeasureSpec);
 		viewHeight = MeasureSpec.getSize(heightMeasureSpec);
 
 		centerFocus.x = centerPointView.x = (float) viewWidth/2;
 		centerFocus.y = centerPointView.y = (float) viewHeight/2;
 
 		//
 		// Rescales image on rotation
 		//
 		if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
 				|| viewWidth == 0 || viewHeight == 0)
 			return;
 		oldMeasuredHeight = viewHeight;
 		oldMeasuredWidth = viewWidth;
 
 		if (saveScale == 1) {
 			//Fit to screen.
 
 			Drawable drawable = getDrawable();
 			if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)
 				return;
 			int bmWidth = drawable.getIntrinsicWidth();
 			int bmHeight = drawable.getIntrinsicHeight();
 
 			float scaleX = (float) viewWidth / (float) bmWidth;
 			float scaleY = (float) viewHeight / (float) bmHeight;
 			this.scale = Math.min(scaleX, scaleY);
 			matrix.setScale(scale, scale);
 
 			// Center the image
 			redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);
 			redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);
 			redundantYSpace /= 2;
 			redundantXSpace /= 2;
 
 			matrix.postTranslate(redundantXSpace, redundantYSpace);
 
 			origWidth = viewWidth - 2 * redundantXSpace;
 			origHeight = viewHeight - 2 * redundantYSpace;
 			setImageMatrix(matrix);
 
 
 			// reload saved state
 			for (int i = 0; i < pins.size(); i++) {
 				ZoomablePinView pin = pins.get(i);
 				pin.setPosition(scale, redundantXSpace, redundantYSpace);
 			}
 			selectPin(selectedPin);
 			
 		}
 		fixTrans();
 	}
 
 	private boolean isInTheImage (float posX, float posY) {
 		boolean result = false;
 		float imageWidth = origWidth * saveScale;
 		float redundantWidthSpace =  (viewWidth - imageWidth) / 2;
 		if (redundantWidthSpace < 0)
 			redundantWidthSpace = 0;
 		float imageHeight = origHeight * saveScale;
 		float redundantHeightSpace =  (viewHeight - imageHeight) / 2;
 		if (redundantHeightSpace < 0)
 			redundantHeightSpace = 0;
 		if (posX >= redundantWidthSpace && posX <= redundantWidthSpace + imageWidth
 				&& posY >= redundantHeightSpace && posY <= redundantHeightSpace + imageHeight)
 			result = true;
 		return result;
 	}
 
 	private void addPin(float posX, float posY, PointF centerPoint, PointF centerFocus, float saveScale, 
 			float scale, float redundantXSpace, float redundantYSpace) {
 		ZoomablePinView pin = new ZoomablePinView(context);
 		pin.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
 				ViewGroup.LayoutParams.WRAP_CONTENT));
 		pins.add(pin);
 		ViewGroup parent = (ViewGroup) getParent();
 		parent.addView(pin);
 		TextView numberView = new TextView(context);
 		parent.addView(numberView);
 		pin.setNumberView(numberView);
 		selectPin(pins.size() - 1);
 		pin.setPosition(posX, posY, centerPoint, centerFocus, saveScale, 
 				scale, redundantXSpace, redundantYSpace);
 		calculateWolfNumber();
 	}
 	
 	public void addPin(ZoomablePinView pin) {
 		ViewGroup parent = (ViewGroup) getParent();
 		parent.addView(pin);
 		TextView numberView = new TextView(context);
 		parent.addView(numberView);
 		pin.setNumberView(numberView);
 	}
 
 	public void removePin(){
 		if (selectedPin > -1){
 			ViewGroup parent = (ViewGroup) getParent();
 			parent.removeView(pins.get(selectedPin).getNumberView());
 			parent.removeView(pins.get(selectedPin));
 			pins.remove(selectedPin);
 			selectedPin = -1;
 //			selectPin(pins.size() - 1);
 			calculateWolfNumber();
 
 			((SunMarkerActivity) context).hideGroupControls();
 		}
 	}
 
 	public void selectPin(int pinNumber){
 		if (selectedPin > -1) 
 			pins.get(selectedPin).unselect();
 		if (pinNumber > -1 && pinNumber < pins.size()) {
 			selectedPin = pinNumber;
 			pins.get(selectedPin).select();
 			pins.get(selectedPin).bringToFront();
 			((SunMarkerActivity) context).displayGroupControls(pins.get(selectedPin).getNumber());
 		}
 	}
 
 	public void increasePin () {
 		if (selectedPin > -1) {
 			pins.get(selectedPin).increaseNumber();
 			calculateWolfNumber();
 		}
 	}
 
 	public void decreasePin () {
 		if (selectedPin > -1){
 			pins.get(selectedPin).decreaseNumber();
 			calculateWolfNumber();
 		}
 	}
 	
 	public void setPinNumber(int number) {
 		if (selectedPin > -1) {
 			pins.get(selectedPin).setNumber(number);
 			calculateWolfNumber();
 		}
 	}
 
 	public ZoomablePinView getPin() {
 		if (selectedPin == -1)
 			return null;
 		else
 			return pins.get(selectedPin);
 	}
 
 	private void moveCenterFocusOnZoom (float focusX, float focusY, float scale) {
 		float focusDistanceX = centerPointView.x - focusX;
 		float focusDistanceY = centerPointView.y - focusY;
 		float deltaX = focusDistanceX / scale - focusDistanceX;
 		float deltaY = focusDistanceY / scale - focusDistanceY;
 		centerFocus.x += deltaX / (saveScale / scale);
 		centerFocus.y += deltaY / (saveScale / scale);
 	}
 
 	private void moveCenterFocusOnDrag (float deltaX, float deltaY) {
 		centerFocus.x -= deltaX / saveScale;
 		centerFocus.y -= deltaY / saveScale;
 	}
 
 	private void movePinsOnZoom(float focusX, float focusY, float scale) {
 		for (int i = 0; i < pins.size(); i++) 
 			pins.get(i).moveOnZoom(focusX, focusY, scale);
 	}
 
 	private void movePinsOnDrag(float deltaX, float deltaY) {
 		for (int i = 0; i < pins.size(); i++) 
 			pins.get(i).moveOnDrag(deltaX, deltaY);
 	}
 
 	private int searchClosestPin (float posX, float posY){
 		int index = -1;
 		int maxDistanceX = 0;
 		int maxDistanceY = 0;
 		double distanceClosestPin = 1000f;
 		for (int i = 0; i < pins.size(); i++) {
 			ZoomablePinView pin = pins.get(i);
 			if (i == 0){
 				maxDistanceX = pin.getWidth() + SELECTION_DISTANCE;
 				maxDistanceY = pin.getHeight() + SELECTION_DISTANCE;
 			}
 			float distanceX = Math.abs(pin.getPosX() - posX);
 			float distanceY = Math.abs(pin.getPosY() - posY);
 			double distance = Math.hypot(distanceX, distanceY);
 			if (distanceX < maxDistanceX && distanceY < maxDistanceY && distance < distanceClosestPin) {
 				index = i;
 				distanceClosestPin = distance;
 			}
 		}
 		return index;
 	}
 
 	private void calculateWolfNumber() {
 		int individualSpots = 0;
 		for (int i = 0; i < pins.size(); i++) {
 			individualSpots += pins.get(i).getNumber();
 		}
 		wolfNumber = K * (10 * pins.size() + individualSpots);
 		if (listener != null) {
 			listener.onWolfNumberUpdated(wolfNumber);
 		}
 	}
 
 	public void resetAttributes(){
 		int numPins = pins.size();
 		for (int i = 0; i < numPins; i++) {
 			selectedPin = 0;
 			removePin();
 		}
 		centerFocus.x = centerPointView.x;
 		centerFocus.y = centerPointView.y;
 		saveScale = 1f;
 	}
 
 	public ArrayList<ZoomablePinView> getPins() {
 		return pins;
 	}
 
 	public void setPins(ArrayList<ZoomablePinView> pins) {
 		this.pins = pins;
 		calculateWolfNumber();
 	}
 
 	public int getSelectedPin() {
 		return selectedPin;
 	}
 
 	public void setSelectedPin(int selectedPin) {
 		this.selectedPin = selectedPin;
 	}
 
 	public int getRealWidth() {
 		return getDrawable().getIntrinsicWidth();
 	}
 
 	public int getRealHeight() {
 		return getDrawable().getIntrinsicHeight();
 	}
 
 
 }
