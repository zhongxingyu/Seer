 package uk.co.mpkdashx.CBTT;
 
 import java.util.ArrayList;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class MainActivity.
  *
  * @author Martin Kemp <martin.kemp@capgemini.com>
  * @version 1.0
  * @since 10/05/2012
  */
 
 public class MainActivity extends SherlockFragmentActivity {
 
 	ViewPager mViewPager;
 	TabsAdapter mTabsAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setTheme(R.style.Theme_Sherlock);
 		super.onCreate(savedInstanceState);
 
 		mViewPager = new ViewPager(this);
 		mViewPager.setId(R.id.pager);
 		setContentView(mViewPager);
 		ActionBar bar = getSupportActionBar();
 		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		bar.setDisplayShowTitleEnabled(false);
 		bar.setDisplayShowHomeEnabled(false);
 
 		mTabsAdapter = new TabsAdapter(this, mViewPager);
 		mTabsAdapter.addTab(bar.newTab().setText("morning"),
 				MorningFragment.class, null);
 		mTabsAdapter.addTab(bar.newTab().setText("midday"),
 				MiddayFragment.class, null);
 		mTabsAdapter.addTab(bar.newTab().setText("afternoon"),
 				AfternoonFragment.class, null);
 
 	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		finish();
	}
 
 	public static class TabsAdapter extends FragmentPagerAdapter implements
 			ActionBar.TabListener, ViewPager.OnPageChangeListener {
 		private final Context mContext;
 		private final ActionBar mActionBar;
 		private final ViewPager mViewPager;
 		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 
 		static final class TabInfo {
 			private final Class<?> clss;
 			private final Bundle args;
 
 			TabInfo(Class<?> _class, Bundle _args) {
 				clss = _class;
 				args = _args;
 			}
 		}
 
 		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
 			super(activity.getSupportFragmentManager());
 			mContext = activity;
 			mActionBar = activity.getSupportActionBar();
 			mViewPager = pager;
 			mViewPager.setAdapter(this);
 			mViewPager.setOnPageChangeListener(this);
 		}
 
 		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
 			TabInfo info = new TabInfo(clss, args);
 			tab.setTag(info);
 			tab.setTabListener(this);
 			mTabs.add(info);
 			mActionBar.addTab(tab);
 			notifyDataSetChanged();
 		}
 
 		public int getCount() {
 			return mTabs.size();
 		}
 
 		public SherlockFragment getItem(int position) {
 			TabInfo info = mTabs.get(position);
 			return (SherlockFragment) Fragment.instantiate(mContext,
 					info.clss.getName(), info.args);
 		}
 
 		public void onPageScrolled(int position, float positionOffset,
 				int positionOffsetPixels) {
 		}
 
 		public void onPageSelected(int position) {
 			mActionBar.setSelectedNavigationItem(position);
 		}
 
 		public void onPageScrollStateChanged(int state) {
 		}
 
 		public void onTabSelected(Tab tab, FragmentTransaction ft) {
 			mViewPager.setCurrentItem(tab.getPosition());
 			// Log.v(TAG, "clicked");
 			Object tag = tab.getTag();
 			for (int i = 0; i < mTabs.size(); i++) {
 				if (mTabs.get(i) == tag) {
 					mViewPager.setCurrentItem(i);
 				}
 			}
 		}
 
 		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		}
 
 		public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		}
 
 		public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
 		}
 
 		public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
 		}
 	}
 
 }
