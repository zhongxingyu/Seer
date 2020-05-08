 package com.jas.devotional;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class LearningActivity  extends FragmentActivity implements ActionBar.TabListener{
 	
 
 	 LearningPagerAdapter mLearningPagerAdapter;
 	 ViewPager mViewPager;
 	 
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_learning);
 		mLearningPagerAdapter = new LearningPagerAdapter(getSupportFragmentManager());
 		final ActionBar actionBar  = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(false );
 		
 		
 		// Specify that we will be displaying tabs in the action bar.
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
        
         Log.d(Constants.DEVICE_DEBUG_APP_CODE, "Loading Learning Screen");
         // Set up the ViewPager, attaching the adapter.
         mViewPager = (ViewPager) findViewById(R.id.pager);
         mViewPager.setAdapter(mLearningPagerAdapter);
         mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 // When swiping between different app sections, select the corresponding tab.
                 // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                 // Tab.
                 actionBar.setSelectedNavigationItem(position);
             }
         });
         for (int i = 0; i < mLearningPagerAdapter.getCount(); i++) {
             // Create a tab with text corresponding to the page title defined by the adapter.
             // Also specify this Activity object, which implements the TabListener interface, as the
             // listener for when this tab is selected.
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(mLearningPagerAdapter.getPageTitle(i))
                             .setTabListener(this));
         }
 	}
 	
 	
 	 
 	 /**
 	     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
 	     * sections of the app.
 	     */
 	    public static class LearningPagerAdapter extends FragmentPagerAdapter {
 	    	
 	    	 LessonFragment mLessonFragment;
 	    	 DummyFragment mDummyFragment;
 
 	        public LearningPagerAdapter(FragmentManager fm) {
 	            super(fm);
 	        }
 
 	        @Override
 	        public Fragment getItem(int i) {
 	        	
 	        	switch(i){
 	        	case 0:{
 	        		if(mLessonFragment==null){
 	        			mLessonFragment = new LessonFragment();
 	        		}
 	        		return mLessonFragment;
 	        	}
 	        	 default:{
	        		if(mDummyFragment==null){
	        			mDummyFragment = new DummyFragment();
	        		}
	        		return mDummyFragment;
 	        		
 	        	 }
 	        	}
 	        		
 	        	
 	        }
 
 	        @Override
 	        public int getCount() {
 	            return 5;
 	        }
 
 	        @Override
 	        public CharSequence getPageTitle(int position) {
 	        	switch (position){
 	        	
 	        	case 0:
 	        		return "Lessons";
 	        	case 1:
 	        		return "Downloads";
 	        	case 2:
 	        		return "Progress";
 	        	case 3:
 	        		return "About";
 	        	case 4:
 	        		return "More";
 	        	default:
 	        		return "Lessons";
 	        	
 	        	
 	        	}
 	        }
 	    }
 	
 	    @Override
 		public boolean onCreateOptionsMenu(Menu menu) {
 			// Inflate the menu; this adds items to the action bar if it is present.
 			getMenuInflater().inflate(R.menu.learning_menu, menu);
 			 return super.onCreateOptionsMenu(menu);
 		}
 	    
 	    
 	    @Override
 		public boolean onOptionsItemSelected(MenuItem item) {
 			switch (item.getItemId()) {
 			case R.id.menu_item_share:
 
 				Intent i = new Intent(this, ActivityUnderConstruction.class);
 
 				startActivity(i);
 				return true;
 			case R.id.donate:
 				
 
 				startActivity( new Intent(this, ActivityUnderConstruction.class));
 				return true;
 			case R.id.classmates:
 				
 
 				startActivity( new Intent(this, ActivityUnderConstruction.class));
 				return true;
 			case R.id.progress:
 				
 
 				startActivity( new Intent(this, ActivityUnderConstruction.class));
 				return true;
 				
 			
 			default:
 				return super.onOptionsItemSelected(item);
 			}
 		}
 
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		 mViewPager.setCurrentItem(tab.getPosition());
 		
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		Log.d(Constants.DEVICE_DEBUG_APP_CODE, "back pressed");
 		LessonFragment lessonFragment = (LessonFragment) mLearningPagerAdapter.getItem(0);
 		MediaPlayer mPlayer = lessonFragment.getMediaPlayer();
 		if(mPlayer!=null){
 			if(mPlayer.isPlaying()){
 				mPlayer.stop();
 			}
 			mPlayer.release();
 			lessonFragment.setMediaPlayer(null);
 			
 		}
 		
 		super.onBackPressed();
 	}
 	
 
 }
