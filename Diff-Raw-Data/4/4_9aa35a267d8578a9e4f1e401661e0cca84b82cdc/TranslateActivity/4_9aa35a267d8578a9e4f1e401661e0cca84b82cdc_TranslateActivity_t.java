 package com.johndaniel.glosar;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.Window;
 
 import android.os.Bundle;
 import android.content.Intent;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 
 public class TranslateActivity extends SherlockFragmentActivity {
 	/*
 	 *  For testing. The NUM_PAGES should have the value of 
 	 * the amount of translations.
 	 * NUM_PAGES decides how pages the ViewPager should hold.
 	 */
 	public static final String TRANSLATION = "com.johndaniel.glosar.TRASNLATION";
 	
 	private int NUM_PAGES;
 	
 	private boolean REVERSE_TRANSLATION;
 	
 	private String[] translations;
 	
 	private ViewPager pager;
 	
 	private PagerAdapter pagerAdapter;
 	
 	private int colorChooser;
 	/* 
 	 * 1 = blue
 	 * 2 = red
 	 * 3 = purple
 	 * 4 = yellow
 	 * 5 = green
 	 */
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_translate);
 		
 		Intent intent = getIntent();
 		NUM_PAGES = intent.getExtras().getInt(OverviewFragment.NUM_TRANS);
 		REVERSE_TRANSLATION = intent.getExtras().getBoolean(OverviewFragment.REVERSE_TRANSLATION);
 		
 		//Edit ActionBar
 		final ActionBar ab = getSupportActionBar();
 		ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.translate_activity_ab_bg));
 		ab.setTitle("Word 1 of " + NUM_PAGES);
 		ab.setHomeButtonEnabled(true);
 		ab.setDisplayHomeAsUpEnabled(true);
 		
 		//Pair id and setup
 		pager = (ViewPager) findViewById(R.id.translate_pager);
 		pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
 		pager.setAdapter(pagerAdapter);
 		
 		translations = intent.getExtras().getStringArray(OverviewFragment.TRANSLATIONS);
 		
 		pager.setOnPageChangeListener(new OnPageChangeListener(){
 
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onPageSelected(int arg0) {
 				// TODO Auto-generated method stub
 				int thisPage = arg0 + 1;
 				ab.setTitle("Word " + thisPage + " of " + NUM_PAGES);
 				
 			}
 			
 		});
 		
 		
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		outState.putInt("PAGE", pager.getCurrentItem());
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		
 		super.onRestoreInstanceState(savedInstanceState);
 		
 		pager.setCurrentItem(savedInstanceState.getInt("PAGE"));
 	}
 
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getSupportMenuInflater().inflate(R.menu.translate, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch (item.getItemId()){
 		case android.R.id.home: 
			/*Intent intent = new Intent(this, StartPoint.class);
			startActivity(intent);*/
 			finish();
 			return true;
 		default: return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
         public ScreenSlidePagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
         	TranslateHolder tHolder = new TranslateHolder();
         	Bundle args = new Bundle();
         	args.putString(TRANSLATION, translations[position]);
         	args.putBoolean(OverviewFragment.REVERSE_TRANSLATION, REVERSE_TRANSLATION);
         	colorChooser = position % 5;
         	args.putInt("COLOR", colorChooser);
         	tHolder.setArguments(args);
             return tHolder;
         }
 
         @Override
         public int getCount() {
             return NUM_PAGES;
         }
     }
 }
