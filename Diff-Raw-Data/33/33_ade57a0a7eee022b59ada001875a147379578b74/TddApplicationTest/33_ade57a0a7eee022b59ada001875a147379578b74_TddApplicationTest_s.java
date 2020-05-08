 
 package com.hrw.android.tdd.activity;
 
import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
import android.util.Log;

 import com.hrw.android.tdd.TddApplication;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 
 @RunWith(RobolectricTestRunner.class)
 public class TddApplicationTest {
 
     private TddApplication mTddApplication;
 
     @Before
     public void setUp() {
        mTddApplication = new TddApplication();
         mTddApplication.onCreate();
     }
 
     @Test
     public void should_return_not_null() {
         assertNotNull(mTddApplication);
     }
 
 }
