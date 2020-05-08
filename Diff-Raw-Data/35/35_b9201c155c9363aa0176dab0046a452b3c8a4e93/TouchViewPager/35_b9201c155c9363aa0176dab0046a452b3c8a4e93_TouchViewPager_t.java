 package com.gris.ege.pager;
 
 import android.content.Context;
 import android.support.v4.view.ViewPager;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 
 public class TouchViewPager extends ViewPager
 {
    private int mCurrentPage;
 
     public TouchViewPager(Context context)
     {
         super(context);
 
        mCurrentPage=0;
     }
 
     public TouchViewPager(Context context, AttributeSet attrs)
     {
         super(context, attrs);
 
        mCurrentPage=0;
     }
 
     public boolean onInterceptTouchEvent(MotionEvent event)
     {
        TaskFragment aPage=null;
        TasksPageAdapter aAdapter=(TasksPageAdapter)getAdapter();

        if (aAdapter!=null)
        {
            aPage=aAdapter.getFragment(mCurrentPage);
        }

        if (aPage!=null && aPage.isScaled())
         {
             this.requestDisallowInterceptTouchEvent(true);
         }
         else
         {
             this.requestDisallowInterceptTouchEvent(false);
         }
 
         return super.onInterceptTouchEvent(event);
     }
 
    public int getCurrentPage()
     {
         return mCurrentPage;
     }
 
    public void setCurrentPage(int aCurrentPage)
     {
         mCurrentPage=aCurrentPage;
     }
 }
