 package samples.tabhost.andreaoid.net;
 
 import java.util.ArrayList;
 import java.util.List;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.widget.TabHost;
 import android.widget.TabHost.OnTabChangeListener;
 
 /**
  * ----------------------------------------------------------------------------------------------------------
  * ------------------------------ Progetto TabHostAndFragments Project v1.01 19.07.2013 - MainActivity.java
  * -------------------------------------------------------------------------------------------- Andrea Carrer
  * ----------------------------------------------------------------------------------------------------------
  * ---------------------------------------------------------------------------------------Freely inspired by: 
  * --------------- http://thepseudocoder.wordpress.com/2011/10/13/android-tabs-viewpager-swipe-able-tabs-ftw/
 
 */
 
 public class MainActivity extends FragmentActivity implements
 		OnTabChangeListener, OnPageChangeListener {
 
 	MyPageAdapter pageAdapter;
 	private ViewPager mViewPager;
 	private TabHost mTabHost;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		mViewPager = (ViewPager) findViewById(R.id.viewpager);
 
 		// Tab Initialization
 		initialiseTabHost();
 
 		// Fragments and ViewPager Initialization
 		List<Fragment> fragments = getFragments();
 		pageAdapter = new MyPageAdapter(getSupportFragmentManager(), fragments);
 		mViewPager.setAdapter(pageAdapter);
 		mViewPager.setOnPageChangeListener(MainActivity.this);
 	}
 
 	// Method to add a TabHost
 	private static void AddTab(MainActivity activity, TabHost tabHost,
 			TabHost.TabSpec tabSpec) {
 		tabSpec.setContent(new MyTabFactory(activity));
 		tabHost.addTab(tabSpec);
 	}
 
 	// Manages the Tab changes, synchronizing it with Pages
 	public void onTabChanged(String tag) {
 		int pos = this.mTabHost.getCurrentTab();
 		this.mViewPager.setCurrentItem(pos);
 	}
 
 	@Override
 	public void onPageScrollStateChanged(int arg0) {
 	}
 
 	// Manages the Page changes, synchronizing it with Tabs
 	@Override
 	public void onPageScrolled(int arg0, float arg1, int arg2) {
 		int pos = this.mViewPager.getCurrentItem();
 		this.mTabHost.setCurrentTab(pos);
 	}
 
 	@Override
 	public void onPageSelected(int arg0) {
 	}
 
 	private List<Fragment> getFragments() {
 		List<Fragment> fList = new ArrayList<Fragment>();
 
 		// TODO Put here your Fragments
 		MySampleFragment f1 = MySampleFragment.newInstance("Sample Fragment 1");
 		MySampleFragment f2 = MySampleFragment.newInstance("Sample Fragment 2");
 		MySampleFragment f3 = MySampleFragment.newInstance("Sample Fragment 3");
 		fList.add(f1);
 		fList.add(f2);
 		fList.add(f3);
 
 		return fList;
 	}
 
 	// Tabs Creation
 	private void initialiseTabHost() {
 		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
 		mTabHost.setup();
 
 		// TODO Put here your Tabs
 		MainActivity.AddTab(this, this.mTabHost,
 				this.mTabHost.newTabSpec("Tab1").setIndicator("Tab1"));
 		MainActivity.AddTab(this, this.mTabHost,
 				this.mTabHost.newTabSpec("Tab2").setIndicator("Tab2"));
 		MainActivity.AddTab(this, this.mTabHost,
 				this.mTabHost.newTabSpec("Tab3").setIndicator("Tab3"));
 
 		mTabHost.setOnTabChangedListener(this);
 	}
 }
