 package com.tuvistavie.meetup.event.activity;
 
 import android.app.ActionBar;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 
 import com.tuvistavie.meetup.R;
 import com.tuvistavie.meetup.contacts.model.Contact;
 import com.tuvistavie.meetup.contacts.util.ContactHelper;
 import com.tuvistavie.meetup.event.fragment.TimeLineFragment;
 
 import java.util.List;
 
 import roboguice.activity.RoboFragmentActivity;
 import roboguice.inject.InjectResource;
 import roboguice.inject.InjectView;
 
 public class TimeLineActivity extends RoboFragmentActivity implements ActionBar.TabListener {
 
     @InjectResource(R.string.timeLine) String timeLineString;
     @InjectResource(R.string.friend_list) String friendListString;
     @InjectView(R.id.pager) ViewPager viewPager;
 
     private SectionsPagerAdapter sectionsPager;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_timeline);
 
         final ActionBar actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         sectionsPager = new SectionsPagerAdapter(getSupportFragmentManager());
 
         viewPager.setAdapter(sectionsPager);
 
         viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
             @Override
             public void onPageSelected(int position) {
                 actionBar.setSelectedNavigationItem(position);
             }
         });
 
         for (int i = 0; i < sectionsPager.getCount(); i++) {
             actionBar.addTab(
                     actionBar.newTab()
                             .setText(sectionsPager.getPageTitle(i))
                             .setTabListener(this));
         }
 
         List<Contact> contacts = ContactHelper.getContactList(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.timeline, menu);
         return true;
     }
     
     @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         viewPager.setCurrentItem(tab.getPosition());
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
     }
 
     public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
         public SectionsPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             Fragment fragment = new TimeLineFragment();
             Bundle args = new Bundle();
             fragment.setArguments(args);
             return fragment;
         }
 
         @Override
         public int getCount() {
             return 2;
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             switch (position) {
                 case 0:
                     return timeLineString;
                 case 1:
                     return friendListString;
             }
             return null;
         }
     }
 }
