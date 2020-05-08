 package com.hjkatz.sodamixer.activity;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.View;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.hjkatz.sodamixer.R;
 import com.hjkatz.sodamixer.fragment.CreateFragment;
 import com.hjkatz.sodamixer.fragment.MixesFragment;
 import com.viewpagerindicator.*;
 
 import java.util.ArrayList;
 
 /** Created by User: Harrison Katz on Date: 1/9/13 */
 public class MainActivity extends SherlockFragmentActivity
 {
 
     private static ArrayList<Fragment> fragments = new ArrayList<Fragment>();
     private static MixesFragment mixesFragment = new MixesFragment();
     private static CreateFragment createFragment = new CreateFragment();
 
     static
     {
         //Maintain Same Order as Adding Titles!!!!!!!!!
         fragments.add( mixesFragment );
         fragments.add( createFragment );
        fragments.add( mixesFragment );
        fragments.add( mixesFragment );
     }
 
     public void onCreate( Bundle savedInstanceState )
     {
         super.onCreate( savedInstanceState );
         this.setContentView( R.layout.launch_activity );
 
         Log.i( "SodaMixer",
                 "/¯¯¯¯¯/ ' /¯¯¯¯¯\\ |¯¯¯¯\\°'     /¯¯¯¯¯|          |¯¯¯\\/¯¯¯|    O    \\¯¯|¯¯/  /¯x¯¯\\ |¯¯¯¯\\ " );
         Log.i( "SodaMixer",
                 "\\ __¯¯¯\\' |     x    |'|  x     \\   /     !     |          |            '| |¯¯¯¯|   >  < °'|   (\\__/||   x  <|'" );
         Log.i( "SodaMixer",
                 "/______/| \\_____/ |_____/ /___/¯|__'|          |.__|\\/|__.| |____| /__|__\\  \\____\\ |__|\\__\\" );
 
         MainPagerAdapter fragmentPagerAdapter = new MainPagerAdapter( getSupportFragmentManager() );
         fragmentPagerAdapter.setFragments( fragments );
 
         ViewPager viewPager = (ViewPager) findViewById( R.id.viewPager );
         viewPager.setAdapter( fragmentPagerAdapter );
 
         com.viewpagerindicator.TitlePageIndicator indicator = (TitlePageIndicator) findViewById( R.id.indicator );
         indicator.setViewPager( viewPager );
 
     }
 
     public void addSodaRow( View v )
     {
         createFragment.addSodaRow( v );
     }
 
     public void deleteSodaRow( View v )
     {
         createFragment.deleteSodaRow( v );
     }
 
     public void addSodaDialog( View v )
     {
         createFragment.addSodaDialog( v );
     }
 
     private class MainPagerAdapter extends FragmentPagerAdapter
     {
         ArrayList<Fragment> mFragments;
 
         private final String[] titles = { "Mixes", "Create", "Mixes2", "Mixes3" };
 
         public MainPagerAdapter( FragmentManager fm )
         {
             super( fm );
         }
 
         public void setFragments( ArrayList<Fragment> fragments )
         {
             mFragments = fragments;
         }
 
         @Override
         public android.support.v4.app.Fragment getItem( int i )
         {
             return mFragments.get( i );
         }
 
         @Override
         public int getCount()
         {
             return mFragments.size();
         }
 
         @Override
         public CharSequence getPageTitle( int position )
         {
             return titles[position];
         }
     }
 }
