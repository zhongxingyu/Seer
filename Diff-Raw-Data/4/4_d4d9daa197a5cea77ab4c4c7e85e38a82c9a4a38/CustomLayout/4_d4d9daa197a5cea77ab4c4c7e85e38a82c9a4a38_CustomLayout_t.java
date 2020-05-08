 package org.ktln2.android.customview;
 
 import android.view.ViewGroup;
 import android.view.View;
 import android.content.Context;
 import android.util.AttributeSet;
 
 
 /*
  * This layout will mimic the behaviour of LinearLayout (without
  * gravity handling of course).
  *
  * The children are placed vertically with the same size unless one of
  * them has an "match_parent" attribute.
  */
 public class CustomLayout extends ViewGroup {
     private final String TAG = "CustomLayout";
 
     public CustomLayout(Context context) {
         super(context);
         android.util.Log.d(TAG, "CustomLayout(context)");
     }
 
     public CustomLayout(Context context, AttributeSet attrs) {
         super(context, attrs);
         android.util.Log.d(TAG, "CustomLayout(context, attrs)");
     }
 
     /*
      * In the onMeasure() the layout decide its own dimensions and call
      * the measure step for all of its children.
      *
      * One important method is
      *
      *  static ViewGroup.getChildMeasureSpec(int spec, int padding, int childDimension) 
      *
      * where childDimension can be a size or one of MATCH_PARENT, FILL_PARENT and WRAP_CONTENT.
      *
      * The method returns a MeasureSpec good to be used with the given children.
      */
     @Override
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
         android.util.Log.d(TAG, "onMeasure()");
         measureChildren(widthMeasureSpec, heightMeasureSpec);
 
         // loop over the children using their layout parameters to arrange them
         for (int cycle = 0, nChilds = getChildCount() ; cycle < nChilds ; cycle++) {
             View child = getChildAt(cycle);
             ViewGroup.LayoutParams childLp = child.getLayoutParams();
         }
 
         // as final step call the function below to set the dimension for real
         setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
     }
 
     /*
      * We are using directly left, right since we want that the childs fill
      * all the width.
      */
     @Override
     public void onLayout(boolean changed,int left,int top,int right, int bottom) {
         android.util.Log.d(TAG, "onLayout()");
 
         top = 0;
         for (int cycle = 0, nChild = getChildCount() ; cycle < nChild ; cycle++) {
             View child = getChildAt(cycle);
             int childHeight = child.getMeasuredHeight();
            int childWidth = child.getMeasuredWidth();
 
            child.layout(left, top, left + childWidth, top + childHeight);
 
             top += childHeight;
         }
     }
 }
