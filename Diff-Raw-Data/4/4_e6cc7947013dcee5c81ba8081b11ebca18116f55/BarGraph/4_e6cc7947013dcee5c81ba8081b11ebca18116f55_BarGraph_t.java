 package com.dgsd.android.graphing;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.*;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.View;
 import android.view.animation.DecelerateInterpolator;
 import android.view.animation.Interpolator;
 import com.nineoldandroids.animation.ObjectAnimator;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class BarGraph extends View {
     private static final String TAG = BarGraph.class.getSimpleName();
 
     private static final Interpolator sInterpolator = new DecelerateInterpolator(1.2f);
 
     private Paint mPaint;
     private Paint mAxisLabelPaint;
     private Paint mAxisLinePaint;
 
     private int mWidth;
     private int mHeight;
 
     private List<DataDimension> mData;
 
     private String[] mYValues;
 
     private float mYValueHeight;
     private Rect mTextHeightRect;
 
     private float mMaxXValueWidth;
     private float mMaxYValueWidth;
 
     private int mMinValue;
     private int mMaxValue;
 
     private float mXAxisLabelTopPadding;
     private float mYAxisLabelRightPadding;
 
     private float mBarPadding;
     private float mBorderWidth;
     private int mBarRoundingValue;
     private int mAnimationDuration;
 
     private RectF mRectF;
 
     private float mCurrentAnimValue;
 
     public BarGraph(final Context context) {
         super(context);
         init(null);
     }
 
     public BarGraph(final Context context, final AttributeSet attrs) {
         super(context, attrs);
         init(attrs);
     }
 
     public BarGraph(final Context context, final AttributeSet attrs, final int defStyle) {
         super(context, attrs, defStyle);
         init(attrs);
     }
 
     private void init(final AttributeSet attrs) {
         final Resources r = getResources();
 
         mPaint = new Paint();
         mPaint.setAntiAlias(true);
         mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
 
         mAxisLabelPaint = new Paint();
         mAxisLabelPaint.setAntiAlias(true);
         mAxisLabelPaint.setColor(r.getColor(R.color.bar_graph_axis_label_color));
         mAxisLabelPaint.setTextSize(r.getDimensionPixelSize(R.dimen.bar_graph_axis_label_size));
         mAxisLabelPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
 
         mAxisLinePaint = new Paint();
         mAxisLinePaint.setAntiAlias(true);
         mAxisLinePaint.setStrokeWidth(r.getDimension(R.dimen.bar_graph_axis_width));
         mAxisLinePaint.setStyle(Paint.Style.STROKE);
         mAxisLinePaint.setColor(r.getColor(R.color.bar_graph_axis_line_color));
 
         mTextHeightRect = new Rect();
         mRectF = new RectF();
 
         mAxisLabelPaint.getTextBounds("888", 0, 3, mTextHeightRect);
         mYValueHeight = mTextHeightRect.height();
 
 
         mXAxisLabelTopPadding = r.getDimensionPixelSize(R.dimen.bar_graph_x_axis_top_padding);
         mYAxisLabelRightPadding = r.getDimensionPixelSize(R.dimen.bar_graph_y_axis_right_padding);
         mBorderWidth = r.getDimensionPixelSize(R.dimen.bar_graph_border_width);
         mBarRoundingValue = r.getInteger(R.integer.bar_graph_rounding);
         mBarPadding = r.getDimensionPixelSize(R.dimen.bar_graph_bar_padding);
         mAnimationDuration = r.getInteger(R.integer.bar_graph_animation_duration);
     }
 
     @Override
     protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec);
         mWidth = getWidth();
         mHeight = getHeight();
 
         calculateYValues();
     }
 
     @Override
     protected void onDraw(final Canvas canvas) {
 
         //Y-axis
         canvas.drawLine(mMaxYValueWidth + mYAxisLabelRightPadding,
                 0,
                 mMaxYValueWidth + mYAxisLabelRightPadding,
                 mHeight - getPaddingBottom() - mMaxXValueWidth - mXAxisLabelTopPadding,
                 mAxisLinePaint);
 
         //X-axis
         canvas.drawLine(mMaxYValueWidth + mYAxisLabelRightPadding,
                 mHeight - getPaddingBottom() - mMaxXValueWidth - mXAxisLabelTopPadding,
                 mWidth,
                 mHeight - getPaddingBottom() - mMaxXValueWidth - mXAxisLabelTopPadding,
                 mAxisLinePaint);
 
         if (mData != null) {
             canvas.save();
             {
                 canvas.translate(mMaxYValueWidth + mYAxisLabelRightPadding, 0);
 
                 final float totalWidth = (mWidth - mMaxYValueWidth - mYAxisLabelRightPadding);
                 final float widthOfEachBar = totalWidth / mData.size();
                 final float bottom = mHeight - mMaxXValueWidth - mXAxisLabelTopPadding
                         - (mAxisLinePaint.getStrokeWidth() / 2f) - getPaddingBottom();
                final float totalHeight = bottom;
 
                 for (int i = 0 ; i < mData.size(); i++) {
                     //Draw the border
                     mRectF.bottom = bottom;
                     mRectF.left = (i * widthOfEachBar) + mBarPadding;
                     mRectF.right = mRectF.left + widthOfEachBar - mBarPadding;
                    mRectF.top = totalHeight * (1f - ((mData.get(i).getValue() * mCurrentAnimValue) / mMaxValue));
 
                     mPaint.setColor(mData.get(i).getBorderColor());
                     canvas.drawRoundRect(mRectF, mBarRoundingValue, mBarRoundingValue, mPaint);
 
                     //Draw the bar itself..
                     mRectF.left += mBorderWidth;
                     mRectF.right -= mBorderWidth;
                     mRectF.top += mBorderWidth;
 
                     mPaint.setColor(mData.get(i).getColor());
                     canvas.drawRoundRect(mRectF, mBarRoundingValue, mBarRoundingValue, mPaint);
 
                     //X-Axis Label
                     canvas.save();
                     {
                         final float x = (i * widthOfEachBar) + (widthOfEachBar / 2f);
                         final float y = mHeight - getPaddingBottom();
                         canvas.rotate(-45, x, y);
                         canvas.drawText(mData.get(i).getName(), x, y, mAxisLabelPaint);
                     }
                     canvas.restore();
                 }
 
             }
             canvas.restore();
         }
 
 
         //Y-axis labels
         if (mYValues != null) {
             canvas.save();
             {
                 //Translate canvas up above x-axis labels..
                 canvas.translate(0, -mMaxXValueWidth);
 
                 for (int i = 0; i < mYValues.length; i++) {
                     canvas.drawText(mYValues[i],
                             getPaddingLeft(),
                             mHeight - (i * ((mHeight - mMaxXValueWidth) / mYValues.length)) - (mYValueHeight / 2f) - mXAxisLabelTopPadding,
                             mAxisLabelPaint);
                 }
             }
             canvas.restore();
         }
 
     }
 
     public void add(String name, int value, int color) {
         add(name, value, color, color);
     }
 
     public void add(String name, int value, int color, int borderColor) {
         if (mData == null)
             mData = new LinkedList<DataDimension>();
 
         mData.add(new DataDimension(name, value, color, borderColor));
 
         calculateYValues();
         calculateMaxXLabelLength();
     }
 
     private void calculateMaxXLabelLength() {
         int maxStrLen = Integer.MIN_VALUE;
         if (mData != null) {
             for (DataDimension d : mData) {
                 if (d.getName() != null && d.getName().length() > maxStrLen) {
                     maxStrLen = d.getName().length();
                     mMaxXValueWidth = mAxisLabelPaint.measureText(d.getName());
                 }
             }
         }
     }
 
     private void calculateYValues() {
         if (mData == null || mData.isEmpty()) {
             mYValues = null;
             return;
         }
 
         int maxValues = (int) ((mHeight - mMaxXValueWidth - mXAxisLabelTopPadding) / (mYValueHeight * 2f) - 1);
 
         log("Max Values: " + maxValues);
 
         if (maxValues <= 2) {
             log("View is too small vertically to fit values!");
             return;
         }
 
         mYValues = new String[maxValues];
         mMinValue = 0; //We always want 0 to be the min (unless we have a negative value)/
         mMaxValue = Integer.MIN_VALUE;
         for (DataDimension data : mData) {
             if (data.getValue() < mMinValue)
                 mMinValue = data.getValue();
             if (data.getValue() > mMaxValue)
                 mMaxValue = data.getValue();
         }
 
         mMaxYValueWidth = mAxisLabelPaint.measureText(String.valueOf(mMaxValue));
 
         final int stepVal = (mMaxValue - mMinValue) / (maxValues - 1);
         for (int i = 0; i < maxValues; i++) {
             if (i == 0) {
                 mYValues[i] = String.valueOf(mMinValue);
             } else if (i == maxValues - 1) {
                 mYValues[i] = String.valueOf(mMaxValue);
             } else {
                 mYValues[i] = String.valueOf(mMinValue + (i * stepVal));
             }
         }
     }
 
     public void animateGraph() {
         ObjectAnimator anim = ObjectAnimator.ofFloat(this, "CurrentAnimValue", 0.0f, 1.0f);
         anim.setInterpolator(sInterpolator);
         anim.setDuration(mAnimationDuration);
         anim.start();
     }
 
     public void setCurrentAnimValue(float val) {
         mCurrentAnimValue = val;
         invalidate();
     }
 
     public float getCurrentAnimValue() {
         return mCurrentAnimValue;
     }
 
     public static class DataDimension {
         private String mName;
         private int mValue;
         private int mColor;
         private int mBorderColor;
 
         public DataDimension(final String name, final int value, final int color, final int borderColor) {
             mName = name;
             mValue = value;
             mColor = color;
             mBorderColor = borderColor;
         }
 
         public String getName() {
             return mName;
         }
 
         public void setName(final String name) {
             mName = name;
         }
 
         public int getValue() {
             return mValue;
         }
 
         public void setValue(final int value) {
             mValue = value;
         }
 
         public int getColor() {
             return mColor;
         }
 
         public void setColor(final int color) {
             mColor = color;
         }
 
         public int getBorderColor() {
             return mBorderColor;
         }
 
         public void setBorderColor(final int borderColor) {
             mBorderColor = borderColor;
         }
     }
 
     private static void log(String msg) {
         if (BuildConfig.DEBUG)
             Log.d(TAG, msg);
     }
 }
