 package com.example.educationalapp;
 
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ArrayAdapter;
 import android.support.v4.view.PagerAdapter;
 
 public class Tabs extends FragmentActivity implements TabListener {
 	ActionBar actionBar;
 	ViewPager viewPager;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.tabs_layout);
     	
     	viewPager = (ViewPager) findViewById(R.id.pager);
     	viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
     	viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 			
 			@Override
 			public void onPageSelected(int arg0) {
 				//Log.d("MathPath", "onPageSelected at position " + arg0);
 				actionBar.setSelectedNavigationItem(arg0);
 			}
 			
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				//Log.d("MathPath", "onPageScrolled at position " + arg0 + " from " + arg1 + " with number of pixels = " + arg2);
 				
 			}
 			
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 //				if (arg0 == ViewPager.SCROLL_STATE_IDLE)
 //					Log.d("MathPath", "onPageScrollStateChanged Idle");
 //				if (arg0 == ViewPager.SCROLL_STATE_DRAGGING)
 //					Log.d("MathPath", "onPageScrollStateChanged Dragging");
 //				if (arg0 == ViewPager.SCROLL_STATE_SETTLING)
 //					Log.d("MathPath", "onPageScrollStateChanged Settling");
 				
 			}
 		});
     	
     	actionBar = getActionBar();
     	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
     	
     	ActionBar.Tab videoTab = actionBar.newTab();
     	videoTab.setText("Videos");
     	videoTab.setTabListener(this);
     	
     	ActionBar.Tab erTab = actionBar.newTab();
     	erTab.setText("External Resources");
     	erTab.setTabListener(this);
     	
     	actionBar.addTab(videoTab);
     	actionBar.addTab(erTab);
     }
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		//Log.d("MathPath", "onTabReselected at position " + tab.getPosition() + " name " + tab.getText());
 		
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		//Log.d("MathPath", "onTabSelected at position " + tab.getPosition() + " name " + tab.getText());
 		viewPager.setCurrentItem(tab.getPosition());
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		//Log.d("MathPath", "onTabUnselected at position " + tab.getPosition() + " name " + tab.getText());
 		
 	}
 }
 
 class MyAdapter extends FragmentStatePagerAdapter {
 
 	public MyAdapter(FragmentManager fm) {
 		super(fm);
 	}
 
 	@Override
 	public Fragment getItem(int arg0) {
 		Fragment fragment = null;
 		
 		if (arg0 == 0)
 			fragment = new VideosTab();
		if (arg0 == 1)
 			fragment = new ERTab();
 		
 		return fragment;
 	}
 
 	@Override
 	public int getCount() {
 		return 2;
 	}
 	
 }
