 package com.egeniq.widget;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 
 /**
  * Grid layout.
  * 
  * Responsible for the component grid.
  */
 public class GridLayout extends ViewGroup {
     private int _rowCount = 1;
     private int _columnCount = 1;
     private int _extraColumnCount = 0;    
     
     private int _rowPadding = 0;
     private int _columnPadding = 0;
     
     /**
      * Layout parameters for component.
      */
     public static class LayoutParams extends ViewGroup.LayoutParams {
         public int row;
         public int rowSpan;
         public int column;
         public int columnSpan;
         public int leftAdjustment;
         public int topAdjustment;
         public int widthAdjustment;
         public int heightAdjustment;
         
         private int left;
         private int top;
         private int right;
         private int bottom;
         
         /**
          * Constructor.
          * 
          * @param row    row
          * @param column column
          */
         public LayoutParams(int row, int column) {
             this(row, 1, column, 1);
         }        
 
         /**
          * Constructor.
          * 
          * @param row        row
          * @param rowSpan    row span
          * @param column     column
          * @param columnSpan column span
          */
         public LayoutParams(int row, int rowSpan, int column, int columnSpan) {
            super(MATCH_PARENT, MATCH_PARENT);
             this.row = row;
             this.rowSpan = rowSpan;
             this.column = column;
             this.columnSpan = columnSpan;
         }
         
         /**
          * Adjusts the coordinates and/or width/height of the view with the given amount of display units.
          * 
          * @param left   Shifts the left offset of the view with the given amount of display units (a negative value is possible).
          * @param top    Shifts the top offset of the view with the given amount of display units (a negative value is possible).
          * @param width  Makes the view the given amount of display units wider (or smaller in case of a negative value).   
          * @param height Makes the view the given amount of display units taller (or shorter in case of a negative value).
          */
         public void setAdjustments(int left, int top, int width, int height) {
             leftAdjustment = left;
             topAdjustment = top;
             widthAdjustment = width;
             heightAdjustment = height;
         }
     }
     
     /**
      * Constructor.
      * 
      * @param context
      */
     public GridLayout(Context context) {
         super(context);
     }
     
     /**
      * Constructor.
      * 
      * @param context
      * @param attrs
      */
     public GridLayout(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
     
     /**
      * Constructor.
      * 
      * @param context
      * @param attrs
      * @param arg
      */
     public GridLayout(Context context, AttributeSet attrs, int arg) {
         super(context, attrs, arg);
     }    
      
     /**
      * Returns the row count.
      * 
      * @return row count
      */
     public int getRowCount() {
         return _rowCount;
     }
     
     /**
      * Sets the row count.
      * 
      * @param rowCount
      */
     public void setRowCount(int rowCount) {
         _rowCount = rowCount;
         requestLayout();
     }
  
     /**
      * Returns the column count.
      * 
      * @return column count
      */
     public int getColumnCount() {
         return _columnCount;
     }
     
     /**
      * Sets the column count (this includes any extra columns).
      * 
      * @param columnCount
      */
     public void setColumnCount(int columnCount) {
         _columnCount = columnCount;
         requestLayout();        
     }
     
     /**
      * Returns the extra column count (the number of columns
      * in the column count that are extra).
      * 
      * @return extra column count
      */
     public int getExtraColumnCount() {
         return _extraColumnCount;
     }    
         
     /**
      * Sets the extra column count.
      * 
      * @param extraColumnCount
      */
     public void setExtraColumnCount(int extraColumnCount) {
         _extraColumnCount = extraColumnCount;
         requestLayout();        
     }   
     
     /**
      * Returns the row padding.
      * 
      * @return row padding
      */
     public int getRowPadding() {
         return _rowPadding;
     }
     
     /**
      * Sets the row padding.
      * 
      * @param padding
      */
     public void setRowPadding(int padding) {
         _rowPadding = padding;
     }
     
     /**
      * Returns the column padding.
      * 
      * @return column padding
      */
     public int getColumnPadding() {
         return _columnPadding;
     }
     
     /**
      * Sets the column padding.
      * 
      * @param padding
      */
     public void setColumnPadding(int padding) {
         _columnPadding = padding;
     }
 
     /**
      * Set minimum width.
      */
     public void setMinimumWidth(int width) {
         super.setMinimumWidth(width);
         
         // workaround for the measured width not being recalculated
         if (getMeasuredWidth() < width && getMeasuredHeight() != 0) {
             setMeasuredDimension(width, getMeasuredHeight());
         }
     }
     
     /**
      * Set minimum height.
      */
     public void setMinimumHeight(int height) {
         super.setMinimumHeight(height);
         
         // workaround for the measured height not being recalculated        
         if (getMeasuredHeight() < height && getMeasuredWidth() != 0) {
             setMeasuredDimension(getMeasuredWidth(), height);
         }
     }
     
     /**
      * Measure components.
      * 
      * @Override
      */
     protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED || MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        
         int paddingLeft = getPaddingLeft();
         int paddingTop = getPaddingTop();
         int paddingRight = getPaddingRight();
         int paddingBottom = getPaddingBottom();
         
         int width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight;
         int height = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom;
         
         // make the grid wider than its parent so the extra columns are initially off screen
         double factor = (double)getColumnCount() / ((double)getColumnCount() - (double)getExtraColumnCount());
         width = (int)Math.floor(width * factor);
         
         int columnCount = getColumnCount();
         int columnWidth = (int)Math.floor((double)width / (double)columnCount);
         
         int rowCount = getRowCount();
         int rowHeight = (int)Math.floor((double)height / (double)rowCount);
         
         int widthRemainder = width - (columnCount * columnWidth);
         int heightRemainder = height - (rowCount * rowHeight);
         
         for (int i = 0; i < getChildCount(); i++) {
             View child = getChildAt(i);
             
             if (child.getVisibility() == GONE) {
                 continue;
             }
             
             LayoutParams params = (LayoutParams)child.getLayoutParams();
             
             int childLeft = paddingLeft + (params.column * columnWidth) + params.leftAdjustment;
             int childTop = paddingTop + (params.row * rowHeight) + params.topAdjustment;
             int childWidth = params.columnSpan * columnWidth + params.widthAdjustment;
             int childHeight = params.rowSpan * rowHeight + params.heightAdjustment;
             
             if (params.column + params.columnSpan == columnCount) {
                 // add the remaining space to the right most component
                 childWidth += widthRemainder;
             } else {
                 // add line between components
                 childWidth -= getColumnPadding();
             }
             
             if (params.row + params.rowSpan == rowCount) {
                 // add the remaining space to the bottom most component
                 childHeight += heightRemainder;
             } else {
                 // add line between components                
                 childHeight -= getRowPadding();
             }
 
             child.measure(
                 MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY),
                 MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.EXACTLY)
             );
             
             params.left = childLeft;
             params.top = childTop;
             params.right = childLeft + childWidth;
             params.bottom = childTop + childHeight;
         }
         
         setMeasuredDimension(
             MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), 
             MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
         );
     }
     
     /**
      * Layout the components.
      */
     @Override
     protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
         for (int i = 0; i < getChildCount(); i++) {
             View child = getChildAt(i);
             if (child.getVisibility() != GONE) {
                 LayoutParams params = (LayoutParams)child.getLayoutParams();
                 child.layout(params.left, params.top, params.right, params.bottom);
             }
         }
     }
 }
