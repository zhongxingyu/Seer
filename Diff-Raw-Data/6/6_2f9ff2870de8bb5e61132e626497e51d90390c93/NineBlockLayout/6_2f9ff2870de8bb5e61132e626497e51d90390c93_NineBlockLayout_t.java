 package com.simon.example.layout.view;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 
 /**
  * @author Simon Yu
  */
 public class NineBlockLayout extends ViewGroup {
 
     public NineBlockLayout(Context context) {
         this(context, null, 0);
     }
 
     public NineBlockLayout(Context context, AttributeSet attrs) {
         this(context, attrs, 0);
     }
 
     public NineBlockLayout(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//                   widthMeasureSpec
		// & 00111111111111111111111111111111
         final int widthSize = widthMeasureSpec & ~(0x3 << 30);

		//                   widthMeasureSpec
		// & 11000000000000000000000000000000
         final int widthMode = widthMeasureSpec & (0x3 << 30);
 
         final int heightSize = heightMeasureSpec & ~(0x3 << 30);
         final int heightMode = heightMeasureSpec & (0x3 << 30);
 
         final int childWidth = (int) (widthSize / 3f);
         final int childHeight = (int) (heightSize / 3f);
 
         final int count = getChildCount();
 
         View child;
         for (int i = 0; i < count; i++) {
             child = getChildAt(i);
             if (child != null) {
                 child.measure(childWidth + MeasureSpec.EXACTLY, childHeight + MeasureSpec.EXACTLY);
             }
         }
 
         setMeasuredDimension(widthSize, heightSize);
     }
 
     @Override
     protected void onLayout(boolean changed, int l, int t, int r, int b) {
         final int count = getChildCount();
 
         View child;
         int left = 0, top = 0;
 
         for (int i = 0; i < count; i++) {
             child = getChildAt(i);
             if (child != null) {
                 child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
                 left += child.getMeasuredWidth();
             } else {
                 continue;
             }
 
             if ((i + 1) % 3 == 0) {
                 left = 0;
                 top += child.getMeasuredHeight();
             }
         }
     }
 }
