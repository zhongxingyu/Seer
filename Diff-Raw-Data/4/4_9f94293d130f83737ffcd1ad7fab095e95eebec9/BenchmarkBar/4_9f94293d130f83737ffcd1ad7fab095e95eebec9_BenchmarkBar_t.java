 package com.example.swp_ucd_2013_eule.view;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.LinearGradient;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Rect;
 import android.graphics.Shader;
 import android.graphics.Shader.TileMode;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class BenchmarkBar extends View {
 	private boolean mVertical = true;
 	private int mWeight = 40; // TODO get from attr
 	private int mReferenceWeight = 10;
 	private int mWidth, mHeight;
 
 	private Paint mBorderPaint;
 	private Paint mGoodPaint;
 	private Paint mBadPaint;
 
 	private float mMax = 100;
 	private float mReferenceValue = 65;
 	private float mValue = 85;
 
 	float mBorderTop, mBorderLeft, mBorderRight, mBorderBottom;
 	float mGoodBarTop, mGoodBarLeft, mGoodBarRight, mGoodBarBottom;
 	float mBadBarTop, mBadBarLeft, mBadBarRight, mBadBarBottom;
 	float mReferenceX1, mReferenceX2, mReferenceY1, mReferenceY2;
 
 	public BenchmarkBar(Context context) {
 		super(context);
 		initBenchmarkBar();
 	}
 
 	public BenchmarkBar(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initBenchmarkBar();
 	}
 
 	// TODO on setters: repaint
 
 	public void setReferenceValue(float val) {
 		mReferenceValue = val;
 		updateDimensions();
 	}
 
 	public void setValue(float val) {
 		mValue = val;
 		updateDimensions();
 	}
 
 	public void setMax(float max) {
 		mMax = max;
 		updateDimensions();
 	}
 
 	private void initBenchmarkBar() {
 		mBorderPaint = new Paint();
 		mBorderPaint.setAntiAlias(true);
 		mBorderPaint.setColor(0xFF2b3246);
 		mBorderPaint.setStyle(Style.STROKE);
 		mBorderPaint.setStrokeWidth(2);
 
 		mGoodPaint = new Paint();
 		mGoodPaint.setAntiAlias(true);
 		// mGoodPaint.setColor(0xFF1689bd);
 
 		mBadPaint = new Paint();
 		mBadPaint.setAntiAlias(true);
 		// mBadPaint.setColor(0xFFbd1616);
 
 		updateColors();
 	}
 
 	private void updateColors() {
 		Shader goodShader = new LinearGradient(0, mGoodBarBottom, 0,
 				mGoodBarTop, 0xFFc9dcf9, 0xFF3c80e7, TileMode.CLAMP);
 		mGoodPaint.setShader(goodShader);
 
 		Shader badShader = new LinearGradient(0, mBadBarBottom, 0, mBadBarTop,
 				0xFF3c80e7, 0xFFdd1616, TileMode.CLAMP);
 		mBadPaint.setShader(badShader);
 	}
 
 	private void updateDimensions() {
 		// TODO vertically and horizontally!
 
 		Rect barDimensions = getBarDimensions();
 		float strokeWidth = mBorderPaint.getStrokeWidth(), hStrokeWidth = strokeWidth / 2;
 
 		float referenceHeight = mReferenceValue / mMax
 				* (barDimensions.height() - (2 * strokeWidth));
 		float currentHeight = mValue / mMax
 				* (barDimensions.height() - (2 * strokeWidth));
 
 		float goodHeight = currentHeight;
 		if (goodHeight > referenceHeight) {
 			goodHeight = referenceHeight;
 		}
 
 		// Border
 		mBorderLeft = barDimensions.left + hStrokeWidth;
 		mBorderTop = barDimensions.top + hStrokeWidth;
 		mBorderRight = barDimensions.right - hStrokeWidth;
 		mBorderBottom = barDimensions.bottom - hStrokeWidth;
 
 		// Reference Indicator
 		mReferenceY1 = mReferenceY2 = barDimensions.bottom - referenceHeight
 				- hStrokeWidth;
 		mReferenceX1 = mBorderRight + hStrokeWidth;
 		mReferenceX2 = mReferenceX1 + mReferenceWeight;
 
 		// Good-Rect
 		mGoodBarLeft = barDimensions.left + strokeWidth;
 		mGoodBarBottom = barDimensions.bottom - strokeWidth;
 		mGoodBarTop = mGoodBarBottom - goodHeight;
 		mGoodBarRight = barDimensions.right - strokeWidth;
 
 		// Bad-Rect
 		if (currentHeight > referenceHeight) {
 			mBadBarTop = mGoodBarBottom - currentHeight;
 			mBadBarLeft = mGoodBarLeft;
 			mBadBarRight = mGoodBarRight;
 			mBadBarBottom = mGoodBarTop;
 		} else {
 			mBadBarTop = mBadBarLeft = mBadBarRight = mBadBarBottom = 0;
 		}
 
 		updateColors();
 	}
 
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		// Border
 		canvas.drawRect(mBorderLeft, mBorderTop, mBorderRight, mBorderBottom,
 				mBorderPaint);
 
 		// Reference Indicator
 		canvas.drawLine(mReferenceX1, mReferenceY1, mReferenceX2, mReferenceY2,
 				mBorderPaint);
 
 		// Good-Rect
 		canvas.drawRect(mGoodBarLeft, mGoodBarTop, mGoodBarRight,
 				mGoodBarBottom, mGoodPaint);
 
 		// Bad-Rect
 		if (mBadBarLeft != 0 || mBadBarBottom != 0 || mBadBarRight != 0
 				|| mBadBarTop != 0) {
			canvas.drawRect(mBadBarLeft, mBadBarTop, mBadBarRight,
					mBadBarBottom, mBadPaint);
 		}
 
 	}
 
 	private Rect getBarDimensions() {
 		int barWidth = Math.min(mWeight, mVertical ? mWidth : mHeight);
 
 		// initial values, respecting paddings
 		int top = getPaddingTop(), bottom = mHeight - getPaddingBottom();
 		int left = getPaddingLeft(), right = mWidth - getPaddingRight();
 
 		if (mVertical) {
 			right = Math.min(right, left + barWidth);
 			if (right < left) {
 				right = left;
 			}
 		} else {
 			bottom = Math.min(bottom, top + barWidth);
 			if (bottom < left) {
 				bottom = left;
 			}
 		}
 		return new Rect(left, top, right, bottom);
 	}
 
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		mWidth = measureWidth(widthMeasureSpec);
 		mHeight = measureHeight(heightMeasureSpec);
 		updateDimensions();
 		setMeasuredDimension(mWidth, mHeight);
 	}
 
 	private int measureWidth(int measureSpec) {
 		int result = 0;
 		int specMode = MeasureSpec.getMode(measureSpec);
 		int specSize = MeasureSpec.getSize(measureSpec);
 
 		if (specMode == MeasureSpec.EXACTLY) {
 			// We were told how big to be
 			result = specSize;
 		} else {
 			if (mVertical) {
 				result = (int) mWeight + getPaddingLeft() + getPaddingRight()
 						+ mReferenceWeight;
 				if (specMode == MeasureSpec.AT_MOST) {
 					// Respect AT_MOST value if that was what is called for by
 					// measureSpec
 					result = Math.min(result, specSize);
 				}
 
 			} else {
 				result = specSize;
 			}
 		}
 		return result;
 	}
 
 	private int measureHeight(int measureSpec) {
 		int result = 0;
 		int specMode = MeasureSpec.getMode(measureSpec);
 		int specSize = MeasureSpec.getSize(measureSpec);
 
 		if (specMode == MeasureSpec.EXACTLY) {
 			// We were told how big to be
 			result = specSize;
 		} else {
 			if (mVertical) {
 				result = specSize;
 
 			} else {
 				result = (int) mWeight + getPaddingLeft() + getPaddingRight()
 						+ mReferenceWeight;
 				if (specMode == MeasureSpec.AT_MOST) {
 					// Respect AT_MOST value if that was what is called for by
 					// measureSpec
 					result = Math.min(result, specSize);
 				}
 			}
 		}
 		return result;
 	}
 }
