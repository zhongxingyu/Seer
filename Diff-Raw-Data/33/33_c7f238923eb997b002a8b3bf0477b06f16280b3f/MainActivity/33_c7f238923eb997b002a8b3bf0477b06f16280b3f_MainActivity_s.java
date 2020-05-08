 package com.lib4.circularviewpager;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//setContentView(R.layout.activity_main);
 
 		ViewPager myPager = new ViewPager(this);
 		PagerAdapter adapter = new CircularPagerAdapter(myPager, new int[] {
 				R.layout.first, R.layout.second,
 				R.layout.third });
 		myPager.setAdapter(adapter);
		myPager.setCurrentItem(3);
 		setContentView(myPager);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
