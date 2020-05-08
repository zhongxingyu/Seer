 package se.chalmers.pd.playlistmanager;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity {
 
 	private static final int FIRST_PAGE = 0;
 	private static final int SECOND_PAGE = 1;
 	private static final int THIRD_PAGE = 2;
 	private static final int TOTAL_PAGES = 3;
 
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide
 	 * fragments for each of the sections. We use a
 	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
 	 * will keep every loaded fragment in memory. If this becomes too memory
 	 * intensive, it may be best to switch to a
 	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 	 */
 	SectionsPagerAdapter mSectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	ViewPager mViewPager;
 
 	private ApplicationController controller;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 		controller = new ApplicationController(this);
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			Fragment fragment = new DummySectionFragment();
 			Bundle args = new Bundle();
 
 			switch (position) {
 			case FIRST_PAGE:
 				break;
 			case SECOND_PAGE:
 				break;
 			case THIRD_PAGE:
 				break;
 			}
 
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
 			return fragment;
 		}
 
 		@Override
 		public int getCount() {
 			return TOTAL_PAGES;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case FIRST_PAGE:
 				return getString(R.string.title_section1).toUpperCase();
 			case SECOND_PAGE:
 				return getString(R.string.title_section2).toUpperCase();
 			case THIRD_PAGE:
 				return getString(R.string.title_section3).toUpperCase();
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class DummySectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		public DummySectionFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 			// Create a new TextView and set its text to the fragment's section
 			// number argument value.
 			TextView textView = new TextView(getActivity());
 			textView.setGravity(Gravity.CENTER);
 			textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
 			return textView;
 		}
 	}
 
 }
