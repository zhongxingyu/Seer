 
 package com.hrw.android.tdd.activity;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
import com.hrw.android.tdd.R;
 import com.xtremelabs.robolectric.RobolectricTestRunner;
 
 @RunWith(RobolectricTestRunner.class)
 public class HomeActivityTest {
     HomeActivity mHomeActivity;
 
     @Before
     public void setUp() {
         mHomeActivity = new HomeActivity();
         mHomeActivity.onCreate(null);
     }
 
     @Test
     public void should_return_app_name() throws Exception {
         assertEquals("Android Tdd Sample",
                 mHomeActivity.getResources().getString(R.string.app_name));
     }
 
     @Test
     public void should_return_home_button() throws Exception {
         assertNotNull(mHomeActivity.mHomeButton);
         assertEquals(R.id.home_button,
                  mHomeActivity.mHomeButton.getId());
     }
 }
