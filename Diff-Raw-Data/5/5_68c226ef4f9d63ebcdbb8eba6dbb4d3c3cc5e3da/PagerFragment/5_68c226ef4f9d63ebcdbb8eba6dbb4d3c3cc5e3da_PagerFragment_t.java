 package com.bjerva.tsplex;
 
 import java.util.Locale;
 
 import org.holoeverywhere.LayoutInflater;
 import org.holoeverywhere.app.Fragment;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.viewpagerindicator.TabPageIndicator;
 
 public class PagerFragment extends Fragment {
 	private static final String TAG = "PagerFragment";
 	private static final String[] CONTENT = new String[] { "Tecken", "Kategorier", "Favoriter"};
 
 	private FragmentPagerAdapter mAdapter;
 	private ViewPager mPager;
 	private View myView;
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState){
 		myView = inflater.inflate(R.layout.pager_fragment, container, false);
 		setHasOptionsMenu(true);
 		return myView;
 	}
 
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		MainActivity ma = (MainActivity) getActivity();
 
 		mAdapter = new SignAlternativesAdapter(this);
 
 		mPager = (ViewPager) ma.findViewById(R.id.pager);
 		mPager.setAdapter(mAdapter);
 		mPager.setOffscreenPageLimit(3);
 
 		TabPageIndicator indicator = (TabPageIndicator) ma.findViewById(R.id.indicator);
 		indicator.setViewPager(mPager);
		indicator.setOnPageChangeListener(mOnPageChangeListener);
 	}
 
 	class SignAlternativesAdapter extends FragmentPagerAdapter {
 		public SignAlternativesAdapter(android.support.v4.app.Fragment fragment){
 			super(fragment.getChildFragmentManager());
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			if(position==0){
 				return new SignListFragment();
 			} else if(position==1){
 				return new SignCategoryFragment();
 			} else if(position==2){
 				return new FavouritesFragment();
 			}
 			return null;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			final Locale swedishLocale = new Locale("sv", "SE");
 			return CONTENT[position % CONTENT.length].toUpperCase(swedishLocale);
 		}
 
 		@Override
 		public int getCount() {
 			return CONTENT.length;
 		}
 	}
 	
 	private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener(){
 		public void onPageSelected(int position){
 			if(position==2){
 				((FavouritesFragment)mAdapter.getItem(2)).notifyChange();
 			}
 			Log.d(TAG, ""+position);
 		}
 
 		@Override
 		public void onPageScrollStateChanged(int arg0) {
 		}
 
 		@Override
 		public void onPageScrolled(int arg0, float arg1, int arg2) {
 		}
 	};
 }
