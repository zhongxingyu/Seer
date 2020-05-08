 
 package net.cattaka.android.ctkview;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.LinearGradient;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Shader;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.Shape;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 public class ResizableLinearLayout extends LinearLayout {
     private List<View> mSliders = new ArrayList<View>();
 
     private float mSliderWidth;
 
     private ShapeDrawable mDrawable;
 
     private OnTouchListener mOnTouchListener = new OnTouchListener() {
         @Override
         public boolean onTouch(View v, MotionEvent event) {
             if (mSliders.contains(v)) {
                 int idx = indexOfChild(v);
                 if (idx < 1 || getChildCount() - 1 <= idx) {
                     return false;
                 }
                 View upper = getChildAt(idx - 1);
                 View lower = getChildAt(idx + 1);
                 LayoutParams upperParams = (LayoutParams)upper.getLayoutParams();
                 LayoutParams lowerParams = (LayoutParams)lower.getLayoutParams();
                 float rate;
                 if (getOrientation() == LinearLayout.VERTICAL) {
                     int top = upper.getTop();
                     int bottom = lower.getBottom();
                     float y = event.getY() + v.getTop();
                     rate = (y - top) / (float)(bottom - top);
                 } else {
                     int left = upper.getLeft();
                     int right = lower.getRight();
                     float x = event.getX() + v.getLeft();
                     rate = (x - left) / (float)(right - left);
                 }
                 float weight = upperParams.weight + lowerParams.weight;
                 if (weight == 0) {
                     weight = 1;
                 }
                 upperParams.weight = rate * weight;
                 lowerParams.weight = (1.0f - rate) * weight;
                 upper.setLayoutParams(upperParams);
                 lower.setLayoutParams(lowerParams);
             }
             return false;
         }
     };
 
     public ResizableLinearLayout(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         init();
     }
 
     public ResizableLinearLayout(Context context, AttributeSet attrs) {
         super(context, attrs);
         init();
     }
 
     public ResizableLinearLayout(Context context) {
         super(context);
         init();
     }
 
     private void init() {
         mSliderWidth = 20 * getContext().getResources().getDisplayMetrics().density;
 
         final Paint myPaint = new Paint();
         Shader s = new LinearGradient(0, 0, 0, mSliderWidth, Color.LTGRAY, Color.GRAY,
                 Shader.TileMode.CLAMP);
 
         myPaint.setStrokeWidth(1);
         myPaint.setColor(0xFF000000);
         myPaint.setStyle(Style.FILL);
         myPaint.setShader(s);
         Shape shape = new Shape() {
             @Override
             public void draw(Canvas canvas, Paint paint) {
                 canvas.drawColor(Color.LTGRAY);
                 canvas.drawRect(2, 2, canvas.getWidth() - 4, canvas.getHeight() - 4, myPaint);
             }
         };
         mDrawable = new ShapeDrawable(shape);
     }
 
     @Override
     public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
         if (getChildCount() > 0) {
             Button slider = new Button(getContext());
             slider.setBackgroundDrawable(mDrawable);
             slider.setOnTouchListener(mOnTouchListener);
             mSliders.add(slider);
 
             LayoutParams sliderParams;
             if (getOrientation() == LinearLayout.VERTICAL) {
                 sliderParams = new LayoutParams(LayoutParams.MATCH_PARENT, (int)mSliderWidth);
             } else {
                 sliderParams = new LayoutParams((int)mSliderWidth, LayoutParams.MATCH_PARENT);
             }
             super.addView(slider, index, sliderParams);
         }
         super.addView(child, index, params);
     }
 }
