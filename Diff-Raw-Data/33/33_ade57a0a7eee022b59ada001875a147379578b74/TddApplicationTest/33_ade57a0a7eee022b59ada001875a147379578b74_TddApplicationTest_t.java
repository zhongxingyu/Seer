 
 package com.hrw.android.tdd.activity;
 
import static org.junit.Assert.assertNotNull;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
import com.hrw.android.tdd.R;
 import com.hrw.android.tdd.TddApplication;
import com.xtremelabs.robolectric.Robolectric;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 
 @RunWith(RobolectricTestRunner.class)
 public class TddApplicationTest {
 
     private TddApplication mTddApplication;
 
     @Before
     public void setUp() {
        mTddApplication = (TddApplication) Robolectric.application;
         mTddApplication.onCreate();
     }
 
     @Test
     public void should_return_not_null() {
         assertNotNull(mTddApplication);
     }
 
    @Test
    public void should_return_get_res_not_null() {
        assertNotNull(mTddApplication.getResources());
        System.out.println(mTddApplication.getResources().toString());
        assertNotNull(mTddApplication.getResources().getString(R.string.app_name));
    }

 }
