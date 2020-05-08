 /*
  * Copyright (C) 2011 The CyanogenMod Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.cyanogenmod.cmparts.activities;
 
 import com.cyanogenmod.cmparts.R;
 
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.os.SystemProperties;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.TabHost;
 import android.widget.TextView;
 import android.widget.TabHost.TabSpec;
 
 import com.cyanogenmod.cmparts.utils.MathUtils;
 
 public class InputTweaksActivity extends TabActivity {
 
     private Intent intent;
     private ActionBar mActionBar;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
         if ("1".equals(SystemProperties.get("ro.squadzone.build", "0"))) {
            setContentView(R.xml.cmpartsinput);
         } else {
            return;
         }
         getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
         mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mActionBar.setTitle(R.string.inputtweaks_title);
         mActionBar.setHomeLogo(R.drawable.cm_icon, new OnClickListener() {
                   @Override
                   public void onClick(View v) {
                         InputTweaksActivity.this.finish();
                   }
         });
 		Intent intent; // Reusable Intent for each tab
 
 
 		intent = new Intent().setClass(InputTweaksActivity.this, HapticTweaksActivity.class);
 		setupTab(new TextView(this), getString(R.string.haptic_title), intent);
 
 		intent = new Intent().setClass(InputTweaksActivity.this, LongPressHomeActivity.class);
 		setupTab(new TextView(this), getString(R.string.long_press_home_title), intent);
 
 		intent = new Intent().setClass(InputTweaksActivity.this, LongPressMenuActivity.class);
 		setupTab(new TextView(this), getString(R.string.long_press_menu_title), intent);
     }
 
     public static class FlingableTabHost extends TabHost {
         GestureDetector mGestureDetector;
 
         Animation mRightInAnimation;
         Animation mRightOutAnimation;
         Animation mLeftInAnimation;
         Animation mLeftOutAnimation;
 
         public FlingableTabHost(Context context, AttributeSet attrs) {
             super(context, attrs);
 
             mRightInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_right_in);
             mRightOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_right_out);
             mLeftInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_in);
             mLeftOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_out);
 
             final int minScaledFlingVelocity = ViewConfiguration.get(context)
                     .getScaledMinimumFlingVelocity() * 10; // 10 = fudge by experimentation
 
             mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
                 @Override
                 public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                         float velocityY) {
                     int tabCount = getTabWidget().getTabCount();
                     int currentTab = getCurrentTab();
                     if (Math.abs(velocityX) > minScaledFlingVelocity &&
                         Math.abs(velocityY) < minScaledFlingVelocity) {
 
                         final boolean right = velocityX < 0;
                         final int newTab = MathUtils.constrain(currentTab + (right ? 1 : -1),
                                 0, tabCount - 1);
                         if (newTab != currentTab) {
                             // Somewhat hacky, depends on current implementation of TabHost:
                             // http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;
                             // f=core/java/android/widget/TabHost.java
                             View currentView = getCurrentView();
                             setCurrentTab(newTab);
                             View newView = getCurrentView();
 
                             newView.startAnimation(right ? mRightInAnimation : mLeftInAnimation);
                             currentView.startAnimation(
                                     right ? mRightOutAnimation : mLeftOutAnimation);
                         }
                     }
                     return super.onFling(e1, e2, velocityX, velocityY);
                 }
             });
         }
 
         @Override
         public boolean onInterceptTouchEvent(MotionEvent ev) {
             if (mGestureDetector.onTouchEvent(ev)) {
                 return true;
             }
             return super.onInterceptTouchEvent(ev);
         }
     }
 
 	private void setupTab(final View view, final String tag, final Intent myIntent) {
 
                 final TabHost mTabHost = getTabHost();
 
 		View tabview = createTabView(mTabHost.getContext(), tag);
 		TabSpec setContent =  mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(myIntent);
 		mTabHost.addTab(setContent);
 	}
 
 	private static View createTabView(final Context context, final String text) {
 
 		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bginput, null);
 		TextView tv = (TextView) view.findViewById(R.id.tabsText);
 		tv.setText(text);
 		return view;
 	}
 }
