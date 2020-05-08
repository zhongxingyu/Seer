 package com.megginson.sloopsql;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 
 /**
  * A single tab-content instance.
  *
  * This class represents both the instance of a single tab's content, and
  * the listeners to respond to tabbar events.
  *
  * Note that the handle currently places the tab instance in the activity root,
  * replacing the activity's previous layout.  I will need to change this once
  * we have a multi-pane layout.
  *
  * Adapted from the example at 
  * http://developer.android.com/guide/topics/ui/actionbar.html
  */
 public class TabListener implements ActionBar.TabListener
 {
 
  	private final Fragment mFragment;
	
	private boolean mIsAdded = false;
	
	private final int mParentId;
 
 	/** 
 	 * Construct a new tab-content instance.
 	 *
 	 * @param activity  The host Activity, used to instantiate the fragment
 	 * @param fragmentClass  The fragment's Class, used to instantiate the fragment
 	 */
	public TabListener(int parentId, Fragment fragment)
 	{
 		mFragment = fragment;
		mParentId = parentId;
 	}
 
 	/* The following are each of the ActionBar.TabListener callbacks */
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft)
 	{
		if (!mIsAdded)
		{
			ft.replace(mParentId, mFragment);
			mIsAdded = true;
		} else {
 			ft.attach(mFragment);
			}
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft)
 	{
 		ft.detach(mFragment);
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft)
 	{
 		// User selected the already selected tab. Usually do nothing.
 	}
 
 	/**
 	 * Return the fragment.
 	 */
 	public Fragment getFragment()
 	{
 		return mFragment;
 	}
 
 }
 
