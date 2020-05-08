 package com.bignerdranch.android.criminalintent;
 
 import java.util.ArrayList;
import java.util.UUID;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.ViewPager;
 
 public class CrimePagerActivity extends FragmentActivity {
 
 	private ViewPager mViewPager;
 	private ArrayList<Crime> mCrimes;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		mViewPager = new ViewPager(this);
 		mViewPager.setId(R.id.viewPager);
 		setContentView(mViewPager);
 		
 		mCrimes = (CrimeLab.getInstance(this)).getCrimes();
 		
 		FragmentManager fm = this.getSupportFragmentManager();
 		mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
 
 			@Override
 			public Fragment getItem(int item) {
 				Crime crime = mCrimes.get(item);
 				return CrimeFragment.newInstance(crime.getId());
 			}
 
 			@Override
 			public int getCount() {
 				return mCrimes.size();
 			}
 			
 		});
 		
		UUID crimeId = (UUID) this.getIntent().getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
		for (int i = 0; i < mCrimes.size(); i++) {
			if (mCrimes.get(i).getId().equals(crimeId)) {
				mViewPager.setCurrentItem(i);
				break;
			}
		}
 	}
 }
