 package com.github.lassana.animated_sorting.util;
 
 import android.content.res.Resources;
 import android.view.View;
 import android.view.animation.DecelerateInterpolator;
 import android.view.animation.Interpolator;
 import android.view.animation.TranslateAnimation;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 
 import java.util.HashMap;
 
 /**
  * @author lassana
  * @since 10/9/13
  */
 public class SortingHelper<AdapterItem> {
 
     private static final int DURATION_NON_SYSTEM_VALUE = 333;
     private final ListView mListView;
 
     private HashMap<AdapterItem, Integer> mSavedState = new HashMap<AdapterItem, Integer>();
     private Interpolator mInterpolator = new DecelerateInterpolator();
 
     public SortingHelper(ListView listView) {
         this.mListView = listView;
         saveOldState();
     }
 
     private void saveOldState() {
         mSavedState.clear();
         int first = mListView.getFirstVisiblePosition();
         int last = mListView.getLastVisiblePosition();
         BaseAdapter adapter = (BaseAdapter) mListView.getAdapter();
         for (int i = 0; i < adapter.getCount(); i++) {
             if (i >= first && i <= last) {
                 View v = mListView.getChildAt(i - first);
                 Integer top = v.getTop();
                 AdapterItem dataId = (AdapterItem) adapter.getItem(i);
                 mSavedState.put(dataId, top);
             } else if (i < first) {
                 Integer top = mListView.getTop() - mListView.getHeight() / 2;
                 AdapterItem dataId = (AdapterItem) adapter.getItem(i);
                 mSavedState.put(dataId, top);
             } else if (i > last) {
                 Integer top = mListView.getBottom() + mListView.getHeight() / 2;
                 AdapterItem dataId = (AdapterItem) adapter.getItem(i);
                 mSavedState.put(dataId, top);
             }
         }
         for (int i = 0; i < mListView.getChildCount(); i++) {
             View v = mListView.getChildAt(i);
             Integer top = v.getTop();
             int dataIdx = first + i;
             AdapterItem dataId = (AdapterItem) adapter.getItem(dataIdx);
             mSavedState.put(dataId, top);
         }
     }
 
    public static int getDefaultAnimationTime() {
         Resources res = Resources.getSystem();
         return  res == null
                 ? DURATION_NON_SYSTEM_VALUE
                 : res.getInteger(android.R.integer.config_mediumAnimTime);
     }
 
     public void animateNewState() {
        animateNewState(getDefaultAnimationTime());
     }
 
     public void animateNewState(int animationTime) {
         int first = mListView.getFirstVisiblePosition();
         int last = mListView.getLastVisiblePosition();
         BaseAdapter adapter = (BaseAdapter) mListView.getAdapter();
         for (int i = 0; i < mListView.getChildCount(); i++) {
             int dataIdx = first + i;
             Object dataId = adapter.getItem(dataIdx);
             if (mSavedState.containsKey(dataId)) {
                 View v = mListView.getChildAt(i);
                 int top = v.getTop();
                 int oldTop = mSavedState.get(dataId);
                 int hDiff = top - oldTop;
                 TranslateAnimation anim = new TranslateAnimation(0, 0, -hDiff, 0);
                 anim.setInterpolator(mInterpolator);
                 anim.setDuration(animationTime);
                 v.startAnimation(anim);
             }
         }
         adapter.notifyDataSetChanged();
     }
 }
