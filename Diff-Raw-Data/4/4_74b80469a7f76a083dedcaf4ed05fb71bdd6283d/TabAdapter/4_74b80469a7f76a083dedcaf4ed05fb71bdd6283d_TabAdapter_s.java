 /*******************************************************************************
  * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
  * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
  ******************************************************************************/
 package net.alexjf.tmm.adapters;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import net.alexjf.tmm.R;
 import net.alexjf.tmm.utils.PreferenceManager;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 /**
  * Based on http://stackoverflow.com/a/10082836/441265
  */
 public class TabAdapter extends FragmentPagerAdapter implements
         ActionBar.TabListener, ViewPager.OnPageChangeListener {
     private final static String KEY_TABNAV = "pref_key_tab_navigation";
 
     private final Activity activity;
     private final ActionBar actionBar;
     private final ViewPager viewPager;
     private final ArrayList<TabInfo> tabs = new ArrayList<TabInfo>();
     private boolean tabsShown = true;
     private boolean swipeEnabled = true;
 
     private OnTabChangeListener onTabChangeListener;
     private OnTabFragmentCreateListener onTabFragmentCreateListener;
 
 
     public interface OnTabChangeListener {
         public void onTabChanged(int position);
     }
 
     public interface OnTabFragmentCreateListener {
         public void onTabFragmentCreated(Fragment fragment, int position);
     }
 
     static final class TabInfo {
         private final Class<?> clss;
         private final Bundle args;
         private Fragment fragment;
 
         TabInfo(Class<?> clss, Bundle args) {
             this.clss = clss;
             this.args = args;
             this.fragment = null;
         }
     }
 
     public TabAdapter(SherlockFragmentActivity activity, ViewPager pager) {
         super(activity.getSupportFragmentManager());
         this.activity = activity;
         actionBar = activity.getSupportActionBar();
         viewPager = pager;
         viewPager.setOnPageChangeListener(this);
         viewPager.setAdapter(this);
 
         refreshPreferences();
     }
 
     public void refreshPreferences() {
         PreferenceManager prefManager = PreferenceManager.getInstance();
         
         String[] tabNavigationTypes = 
             activity.getResources().getStringArray(R.array.pref_tab_navigation_values);
         String tabNavigation = 
             prefManager.readUserStringPreference(KEY_TABNAV, "");
 
        int navIndex = Arrays.asList(tabNavigationTypes).indexOf(tabNavigation);
 
         tabsShown = navIndex % 2 == 0;
         swipeEnabled = navIndex > 0;
 
         if (tabsShown) {
             actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         } else {
             actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
         }
 
         if (!swipeEnabled) {
             viewPager.setOnTouchListener(new OnTouchListener() {
                 public boolean onTouch(View v, MotionEvent event) {
                     return true;
                 }
             });
         } else {
             viewPager.setOnTouchListener(null);
         }
     }
 
     @Override
     public Object instantiateItem(ViewGroup container, int position) {
         Fragment fragment = (Fragment) super.instantiateItem(container, position);
         tabs.get(position).fragment = fragment;
         onTabFragmentCreateListener.onTabFragmentCreated(fragment, position);
         return fragment;
     }
 
     public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
         TabInfo info = new TabInfo(clss, args);
         tab.setTag(info);
         tab.setTabListener(this);
         tabs.add(info);
         actionBar.addTab(tab);
         notifyDataSetChanged();
     }
 
     @Override
     public int getCount() {
         return tabs.size();
     }
 
     @Override
     public Fragment getItem(int position) {
         TabInfo info = tabs.get(position);
         Fragment fragment = Fragment.instantiate(activity, info.clss.getName(), 
                 info.args);
         return fragment;
     }
 
     @Override
     public void destroyItem(ViewGroup container, int position, Object object) {
         super.destroyItem(container, position, object);
         tabs.get(position).fragment = null;
     }
 
     public Fragment getFragment(int position) {
         return tabs.get(position).fragment;
     }
 
     public void onPageScrolled(int position, float positionOffset,
             int positionOffsetPixels) {
     }
 
     public void onPageSelected(int position) {
         if (tabsShown) {
             actionBar.setSelectedNavigationItem(position);
         }
         notifyTabChanged(position);
     }
 
     public void onPageScrollStateChanged(int state) {
     }
 
     public void onTabSelected(Tab tab, FragmentTransaction ft) {
         int position = tab.getPosition();
         viewPager.setCurrentItem(position);
         notifyTabChanged(position);
     }
 
     public void onTabUnselected(Tab tab, FragmentTransaction ft) {
     }
 
     public void onTabReselected(Tab tab, FragmentTransaction ft) {
     }
 
     public void setOnTabFragmentCreateListener(OnTabFragmentCreateListener listener) {
         this.onTabFragmentCreateListener = listener;
     }
 
     public void setOnTabChangeListener(OnTabChangeListener listener) {
         this.onTabChangeListener = listener;
     }
 
     private void notifyTabChanged(int position) {
         if (onTabChangeListener != null) {
             onTabChangeListener.onTabChanged(position);
         }
     }
 }
 
