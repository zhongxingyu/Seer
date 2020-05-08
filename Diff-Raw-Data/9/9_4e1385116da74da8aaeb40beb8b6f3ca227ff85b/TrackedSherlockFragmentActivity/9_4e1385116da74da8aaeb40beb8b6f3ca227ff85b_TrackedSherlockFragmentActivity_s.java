 package com.siu.android.andutils.activity.tracker;
 
 import android.os.Bundle;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Window;
 import com.google.android.apps.analytics.easytracking.EasyTracker;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
  */
 public class TrackedSherlockFragmentActivity extends SherlockFragmentActivity {
 
     protected void onCreate(Bundle savedInstanceState, int layout) {
        onCreate(savedInstanceState);
 
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(layout);
         setSupportProgressBarIndeterminateVisibility(false);
 
         EasyTracker.getTracker().setContext(this);
     }
 
     @Override
     protected final void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         EasyTracker.getTracker().trackActivityStart(this);
     }
 
     @Override
     public Object onRetainCustomNonConfigurationInstance() {
         EasyTracker.getTracker().trackActivityRetainNonConfigurationInstance();
         return null;
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         EasyTracker.getTracker().trackActivityStop(this);
     }
 }
