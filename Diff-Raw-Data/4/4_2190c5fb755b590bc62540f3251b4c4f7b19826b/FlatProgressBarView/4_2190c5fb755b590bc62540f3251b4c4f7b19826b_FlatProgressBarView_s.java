 package com.mntnorv.wrdl_holo.views;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.mntnorv.wrdl_holo.R;
 
 public class FlatProgressBarView extends View {
 	
 	private float width;
 	private float height;
 	
 	private float progress;
 	private float maxProgress;
 	
 	private Paint backgroundPaint;
 	private Paint foregroundPaint;
 	private Paint textPaint;
 	
 	private Rect textBounds;
 	private RectF wholeBounds;
 	private RectF progressBounds;
 	
 	private String progressText;
 
 	public FlatProgressBarView(Context context) {
 		super(context);
 		initProgressBar();
 	}
 
 	public FlatProgressBarView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initProgressBar();
 		
 		TypedArray a = context.obtainStyledAttributes(attrs,
                 R.styleable.FlatProgressBarView);
 		
 		setBackgroundColor(a.getColor(R.styleable.FlatProgressBarView_backgroundColor, 0x00000000));
 		setProgressColor(a.getColor(R.styleable.FlatProgressBarView_progressBarColor, 0xFF000000));
 		setTextColor(a.getColor(R.styleable.FlatProgressBarView_android_textColor, 0xFF000000));
 		
 		int textSize = a.getDimensionPixelSize(R.styleable.FlatProgressBarView_android_textSize, 0);
         if (textSize != 0) {
         	setTextSize(textSize);
         }
 		
 		a.recycle();
 	}
 
 	private void initProgressBar() {
 		width = 120;
 		height = 30;
 		progress = 0;
 		maxProgress = 100;
 		
 		progressText = "";
 		
 		// Create bounds
 		wholeBounds = new RectF(0.0f, 0.0f, width, height);
 		progressBounds = new RectF(0.0f, 0.0f, 0.0f, height);
 		
 		// Default background paint
 		backgroundPaint = new Paint();
 		backgroundPaint.setColor(0x00000000);
 		
 		// Default foreground paint
 		foregroundPaint = new Paint();
 		foregroundPaint.setColor(0xFF000000);
 		
 		// Default text paint
 		textPaint = new Paint();
 		textPaint.setAntiAlias(true);
 		textPaint.setSubpixelText(true);
 		textPaint.setColor(0xFF000000);
 		textPaint.setTextSize(16 * getResources().getDisplayMetrics().density);
 		textPaint.setTextAlign(Align.CENTER);
 		
 		// Get text bounds
 		textBounds = new Rect();
 		textPaint.getTextBounds(progressText, 0, progressText.length(), textBounds);
 	}
 	
 	/* SETTERS */
 	public void setSize(float width, float height) {
 		this.width = width;
 		this.height = height;
 		
 		updateBounds();
 		requestLayout();
 		invalidate();
 	}
 	
 	public void setWidth(float width) {
 		this.width = width;
 		
 		updateBounds();
 		requestLayout();
 		invalidate();
 	}
 
 	public void setHeight(float height) {
 		this.height = height;
 		
 		updateBounds();
 		requestLayout();
 		invalidate();
 	}
 	
 	public void setTextSize(float size) {
 		textPaint.setTextSize(size);
 		textPaint.getTextBounds(progressText, 0, progressText.length(), textBounds);
 		
 		invalidate();
 	}
 	
 	public void setProgress(float progress) {
 		if (progress >= 0 && progress <= maxProgress) {
 			this.progress = progress;
 			updateProgressBounds();
 			invalidate();
 		}
 	}
 	
 	public void setMaxProgress(float maxProgress) {
 		if (maxProgress > 0) {
 			this.maxProgress = maxProgress;
 			updateProgressBounds();
 			invalidate();
 		}
 	}
 	
 	public void setBackgroundColor(int color) {
 		backgroundPaint.setColor(color);
 		invalidate();
 	}
 	
 	public void setProgressColor(int color) {
 		foregroundPaint.setColor(color);
 		invalidate();
 	}
 	
 	public void setTextColor(int color) {
 		textPaint.setColor(color);
 		invalidate();
 	}
 	
 	public void setText(String text) {
		this.progressText = text;
 		invalidate();
 	}
 	
 	/* LAYOUT UPDATE */
 	private void updateBounds() {
 		wholeBounds = new RectF(0.0f, 0.0f, width, height);
 		updateProgressBounds();
 	}
 	
 	private void updateProgressBounds() {
 		progressBounds = new RectF(0.0f, 0.0f, width * (progress/maxProgress), height);
 	}
 
 	/* MEASURE */
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		ViewGroup.LayoutParams lp = this.getLayoutParams();
 		
 		int width = measureWidth(widthMeasureSpec, lp.width);
 		int height = measureHeight(heightMeasureSpec, lp.height);
 		
 		this.setSize(width, height);
 		setMeasuredDimension(width, height);
 	}
 	
 	private int measureWidth(int measureSpec, int layoutParam) {
 		int result = 0;
         int specMode = MeasureSpec.getMode(measureSpec);
         int specSize = MeasureSpec.getSize(measureSpec);
 
         if (specMode == MeasureSpec.EXACTLY) {
             result = specSize;
         } else {
             // Measure the text
             result = (int)width + getPaddingLeft() + getPaddingRight();
             if (specMode == MeasureSpec.AT_MOST) {
             	if (layoutParam == ViewGroup.LayoutParams.MATCH_PARENT) {
             		result = Math.max(result, specSize);
             	} else if (layoutParam != 0) {
             		result = Math.min(result, specSize);
             	}
             }
         }
 
         return result;
 	}
 	
 	private int measureHeight(int measureSpec, int layoutParam) {
 		int result = 0;
         int specMode = MeasureSpec.getMode(measureSpec);
         int specSize = MeasureSpec.getSize(measureSpec);
 
         if (specMode == MeasureSpec.EXACTLY) {
             result = specSize;
         } else {
             // Measure the text
             result = (int)height + getPaddingTop() + getPaddingBottom();
             if (specMode == MeasureSpec.AT_MOST) {
             	if (layoutParam == ViewGroup.LayoutParams.MATCH_PARENT) {
             		result = Math.max(result, specSize);
             	} else if (layoutParam != 0) {
             		result = Math.min(result, specSize);
             	}
             }
         }
 
         return result;
 	}
 	
 	/* DRAW */
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		canvas.drawRect(wholeBounds, backgroundPaint);
 		canvas.drawRect(progressBounds, foregroundPaint);
 		canvas.drawText(progressText, width/2, (height + textBounds.bottom - textBounds.top)/2, textPaint);
 	}
 }
