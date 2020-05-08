 package com.portman.panel.uh1h;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 
 public final class RadioCompass extends View {
 
 	private static final String TAG = RadioCompass.class.getSimpleName();
 
 	// drawing tools
 	private RectF rimRect;
 	private Paint rimPaint;
 	private Paint rimCirclePaint;
 	
 	private RectF faceRect;
 	private Paint facePaint;
 	
 	private Paint scalePaint;
 	private RectF scaleRect;
 	
 	private Paint handPaint;
 	
 	private Paint backgroundPaint; 
 	// end drawing tools
 	
 	private Bitmap background; // holds the cached static part
 	
 	// scale configuration
 	private static final int totalNicks = 72;
 	private static final float degreesPerNick = 360.0f / totalNicks;	
 	private static final float minValue = 0f;
 	private static final float maxValue = 360f;
 	
 	// hand dynamics
 	private float compassHeading = 0f;
 	private float coursePointer1 = 0f;
 	private float coursePointer2 = 0f;
 		
 	public RadioCompass(Context context) {
 		super(context);
 		init();
 	}
 
 	public RadioCompass(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public RadioCompass(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Parcelable state) {
 		Bundle bundle = (Bundle) state;
 		Parcelable superState = bundle.getParcelable("superState");
 		super.onRestoreInstanceState(superState);
 		
 		compassHeading = bundle.getFloat("compassHeading");
 		coursePointer1 = bundle.getFloat("coursePointer1");
 		coursePointer2 = bundle.getFloat("coursePointer2");
 	}
 
 	@Override
 	protected Parcelable onSaveInstanceState() {
 		Parcelable superState = super.onSaveInstanceState();
 		
 		Bundle state = new Bundle();
 		state.putParcelable("superState", superState);
 		state.putFloat("compassHeading", compassHeading);
 		state.putFloat("coursePointer1", coursePointer1);
 		state.putFloat("coursePointer2", coursePointer2);
 		return state;
 	}
 
 	private void init() {
 		initDrawingTools();
 	}
 
 	private void initDrawingTools() {
 		rimRect = new RectF(1f, 1f, 99f, 99f);
 
 		rimPaint = new Paint();
 		rimPaint.setAntiAlias(true);
 		rimPaint.setColor(Color.LTGRAY);
 
 		rimCirclePaint = new Paint();
 		rimCirclePaint.setAntiAlias(true);
 		rimCirclePaint.setStyle(Paint.Style.STROKE);
 		rimCirclePaint.setColor(Color.GRAY);
 		rimCirclePaint.setStrokeWidth(1f);
 
 		float rimSize = 2f;
 		faceRect = new RectF();
 		faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize, 
 			     rimRect.right - rimSize, rimRect.bottom - rimSize);
 		
 		facePaint = new Paint();
 		facePaint.setStyle(Paint.Style.FILL);
 		facePaint.setColor(Color.BLACK);
 
 		scalePaint = new Paint();
 		scalePaint.setStyle(Paint.Style.FILL_AND_STROKE);
 		scalePaint.setColor(Color.WHITE);
 		scalePaint.setStrokeWidth(0.5f);
 		scalePaint.setAntiAlias(true);
 		
 		scalePaint.setTextSize(8f);
 		scalePaint.setTypeface(Typeface.SANS_SERIF);
 		scalePaint.setTextAlign(Paint.Align.CENTER);
 		
 		float scalePosition = 3f;
 		scaleRect = new RectF();
 		scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
 					  faceRect.right - scalePosition, faceRect.bottom - scalePosition);
 
 		handPaint = new Paint();
 		handPaint.setAntiAlias(true);
 		handPaint.setColor(Color.WHITE);
 		handPaint.setStrokeWidth(2f);
 		handPaint.setStyle(Paint.Style.FILL_AND_STROKE);	
 		
 		backgroundPaint = new Paint();
 		backgroundPaint.setFilterBitmap(true);
 	}
 	
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
 		Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));
 		
 		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
 		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
 		
 		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
 		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
 		
 		int chosenWidth = chooseDimension(widthMode, widthSize);
 		int chosenHeight = chooseDimension(heightMode, heightSize);
 		
 		int chosenDimension = Math.min(chosenWidth, chosenHeight);
 		
 		setMeasuredDimension(chosenDimension, chosenDimension);
 	}
 	
 	private int chooseDimension(int mode, int size) {
 		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
 			return size;
 		} else { // (mode == MeasureSpec.UNSPECIFIED)
 			return getPreferredSize();
 		} 
 	}
 	
 	// in case there is no size specified
 	private int getPreferredSize() {
 		return 300;
 	}
 
 	private void drawRim(Canvas canvas) {
 		// first, draw the metallic body
 		canvas.drawOval(rimRect, rimPaint);
 	}
 	
 	private void drawFace(Canvas canvas) {		
 		canvas.drawOval(faceRect, facePaint);
 		// draw the inner rim circle
 		canvas.drawOval(faceRect, rimCirclePaint);
 	}
 
 	private void drawScale(Canvas canvas) {
 		canvas.save(Canvas.MATRIX_SAVE_FLAG);
 		
 		canvas.rotate(-compassHeading, 50f, 50f);
 		
 		for (int i = 0; i < totalNicks; ++i) {
 			float y1 = scaleRect.top;
 			float y2 = y1 + 6f;
 			
 			canvas.drawLine(50f, y1 + 4f, 50f, y2, scalePaint);
 			
 			if (i % 2 == 0) { // every 2
 				canvas.drawLine(50f, y1 + 2f, 50f, y2, scalePaint);
 				
 				if (i % 6 == 0) { // every 6
 					canvas.drawLine(50f, y1, 50f, y2, scalePaint);
 					float value = nickToValue(i);
 					String valueString = Integer.toString((int) value/10);
 					if ("0".equals(valueString))
 						valueString = "N";
 					if ("9".equals(valueString))
 						valueString = "E";
 					if ("18".equals(valueString))
 						valueString = "S";
 					if ("27".equals(valueString))
 						valueString = "W";
 				
 					// draw vertical text
 					canvas.save(Canvas.MATRIX_SAVE_FLAG);
 					//canvas.rotate(degreesPerNick * i, 50f, y2 + 8f);
 					canvas.drawText(valueString, 50f, y2 + 10f, scalePaint);
 					canvas.restore();
 				}
 			}
 			
 			canvas.rotate(degreesPerNick, 50f, 50f);
 		}
 		canvas.restore();
 		
 		// draw heading triangle
 		canvas.drawLine(50f, 10f, 47f, 5f, scalePaint);
 		canvas.drawLine(50f, 10f, 53f, 5f, scalePaint);
 		canvas.drawLine(47f, 5f, 53f, 5f, scalePaint);
 	}
 	
 	private float nickToValue(int nick) {
 		return  minValue + nick * (maxValue - minValue) / totalNicks;
 	}
 	
 	private float valueToAngle(float value) {
 		float valuePerNick = (float)(maxValue - minValue) / totalNicks;
 		return degreesPerNick * (value - minValue) / valuePerNick;
 	}
 
 	private void drawPointers(Canvas canvas) {
 		float handAngle = valueToAngle(coursePointer1);
 		canvas.save(Canvas.MATRIX_SAVE_FLAG);
 		canvas.rotate(-compassHeading + handAngle, 50f, 50f);
 		canvas.drawLine(47f, 30f, 47f, 70f, handPaint);
 		canvas.drawLine(53f, 30f, 53f, 70f, handPaint);
 		canvas.drawLine(45f, 30f, 55f, 30f, handPaint);
 		canvas.drawLine(45f, 30f, 50f, 25f, handPaint);
 		canvas.drawLine(55f, 30f, 50f, 25f, handPaint);
 		canvas.drawLine(50f, 10f, 50f, 25f, handPaint);
 		canvas.restore();
 		
 		handAngle = valueToAngle(coursePointer2);
 		canvas.save(Canvas.MATRIX_SAVE_FLAG);
 		canvas.rotate(-compassHeading + handAngle, 50f, 50f);
 		canvas.drawLine(50f, 10f, 50f, 90f, handPaint);
 		canvas.drawLine(50f, 10f, 48f, 20f, handPaint);
 		canvas.drawLine(50f, 10f, 52f, 20f, handPaint);
 		canvas.restore();
 	}
 
 	private void drawBackground(Canvas canvas) {
 		if (background == null) {
 			Log.w(TAG, "Background not created");
 		} else {
 			canvas.drawBitmap(background, 0, 0, backgroundPaint);
 		}
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		drawBackground(canvas);
 
 		float scale = (float) getWidth();		
 		canvas.save(Canvas.MATRIX_SAVE_FLAG);
 		canvas.scale(scale / 100f, scale / 100f);
 
 		drawScale(canvas);
 		drawPointers(canvas);
 		
 		canvas.restore();
 	}
 
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		Log.d(TAG, "Size changed to " + w + "x" + h);
 		
 		regenerateBackground();
 	}
 	
 	private void regenerateBackground() {
 		// free the old bitmap
 		if (background != null) {
 			background.recycle();
 		}
 		
 		background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
 		Canvas backgroundCanvas = new Canvas(background);
 		float scale = (float) getWidth();		
 		backgroundCanvas.scale(scale / 100f, scale / 100f);
 		
 		drawRim(backgroundCanvas);
 		drawFace(backgroundCanvas);
 	}
 		
	public void setHeading(float coursePointer1, float coursePointer2, float compassHeading) {
		this.compassHeading = compassHeading * 180f/(float) Math.PI;
		this.coursePointer1 = coursePointer1 * 180f/(float) Math.PI;
		this.coursePointer2 = coursePointer2 * 180f/(float) Math.PI;
 		invalidate();
 	}
 }
