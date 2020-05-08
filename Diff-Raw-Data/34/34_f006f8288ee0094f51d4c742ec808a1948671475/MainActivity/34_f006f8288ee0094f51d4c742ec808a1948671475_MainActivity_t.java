 package com.kulkarni.gasmileagecalculator;
 
 import java.util.Locale;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
 import android.view.Menu;
 
 public class MainActivity extends FragmentActivity {
	
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
        
        mSectionsPagerAdapter = new SectionsPagerAdapter (getSupportFragmentManager());
        
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     
     
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
     	
     	public SectionsPagerAdapter (FragmentManager fm) {
     		super (fm);
     	}
     	
     	@Override
     	public Fragment getItem (int position) {
     		Fragment fragment;
     		
     		switch (position) {
     		case 0:
     			fragment = new FillupFragment ();
     			break;
     		case 1:
     			fragment = new HistoryFragment ();
     			break;
     		default:
     			fragment = null;
     		}
 			
     		return fragment;
     	}
     	
     	@Override
     	public int getCount () {
     		// We have 2 pages currently
     		return 2;
     	}
     	
     	
     	@Override
     	public CharSequence getPageTitle (int position) {
     		Locale l = Locale.getDefault();
     		String[] page_titles = getResources().getStringArray(R.array.page_titles);
     		
     		return page_titles[position].toUpperCase(l);
     	}
     }
     
 }
