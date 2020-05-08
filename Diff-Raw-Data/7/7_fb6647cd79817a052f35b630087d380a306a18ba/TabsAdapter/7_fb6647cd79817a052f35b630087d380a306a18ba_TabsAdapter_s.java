 package com.github.alexesprit.lostfilm.adapter;
 
 import android.content.Context;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
     private final Context mContext;
     private final ActionBar mActionBar;
     private final ViewPager mViewPager;
 
     public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
         super(activity.getSupportFragmentManager());
         mContext = activity;
         mActionBar = activity.getSupportActionBar();
         mViewPager = pager;
         mViewPager.setAdapter(this);
         mViewPager.setOnPageChangeListener(this);
     }
 
     public void addTab(ActionBar.Tab tab, Class<?> cls) {
         tab.setTag(cls);
         tab.setTabListener(this);
         mActionBar.addTab(tab);
         notifyDataSetChanged();
     }
 
     @Override
     public int getCount() {
         return mActionBar.getTabCount();
     }
 
     @Override
     public Fragment getItem(int position) {
         Class<?> cls = (Class<?>)mActionBar.getTabAt(position).getTag();
         return Fragment.instantiate(mContext, cls.getName());
     }
 
     @Override
     public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
     }
 
     @Override
     public void onPageSelected(int position) {
         mActionBar.setSelectedNavigationItem(position);
     }
 
     @Override
     public void onPageScrollStateChanged(int state) {
     }
 
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        Object tag = tab.getTag();
        for (int i = 0; i < mActionBar.getTabCount(); i++) {
            if (mActionBar.getTabAt(i) == tag) {
                mViewPager.setCurrentItem(i);
            }
        }
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
     }
 }
 
