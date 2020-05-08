 package de.wak_sh.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
 
public class SectionPagerAdapter extends FragmentStatePagerAdapter {
 	private List<Fragment> fragments;
 	private Context context;
 
 	public SectionPagerAdapter(FragmentManager fm, Context context) {
 		super(fm);
 		fragments = new ArrayList<Fragment>();
 		this.context = context;
 	}
 
 	public void addFragment(Fragment fragment) {
 		fragments.add(fragment);
 	}
 
 	@Override
 	public Fragment getItem(int pos) {
 		return fragments.get(pos);
 	}
 
 	@Override
 	public int getCount() {
 		return fragments.size();
 	}
 
 	@Override
 	public CharSequence getPageTitle(int position) {
 		switch (position) {
 		case 0:
 			return context.getString(R.string.nachrichten);
 		case 1:
 			return context.getString(R.string.noten);
 		default:
 			return null;
 		}
 	}
 }
