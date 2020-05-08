 package com.akash.android.sample.test;
 
 import android.support.v4.app.Fragment;
 import android.test.ActivityInstrumentationTestCase2;
 import com.akash.android.sample.*;
 
 public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {
 
    HomeActivity homeActivity;
    Fragment[] fragments;

     public HomeActivityTest() {
         super(HomeActivity.class);
     }
 
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         homeActivity = getActivity();
         fragments = homeActivity.getFragments();
     }
 
     public void testPreConditions() {
         assertNotNull(homeActivity);
         for(Fragment itr: fragments )
             assertNotNull(itr);
     }
 
     public void testFragments() {
         // TODO - Write test cases for fragments
     }
 
     public void testActivity() {
         // TODO - Write test cases for activities
     }
 }
 
