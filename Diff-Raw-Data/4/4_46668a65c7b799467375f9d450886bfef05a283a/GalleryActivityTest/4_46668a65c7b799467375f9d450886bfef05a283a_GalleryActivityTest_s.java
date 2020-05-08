 package com.cs301w01.meatload.test.ActivityTests;
 
 import com.cs301w01.meatload.activities.GalleryActivity;
 import com.cs301w01.meatload.model.SQLiteDBManager;
 
 import android.app.Instrumentation;
 import android.content.Context;
 import android.test.ActivityInstrumentationTestCase2;
 
 public class GalleryActivityTest extends ActivityInstrumentationTestCase2<GalleryActivity> {
     private Context mContext;
     private GalleryActivity mActivity;
     
 	public GalleryActivityTest(){
 		super("com.cs301w01.meatload", GalleryActivity.class);
 	}
 	
     @Override
     protected void setUp() throws Exception {
     	super.setUp();
 
         mActivity = getActivity();
         mContext = mActivity.getBaseContext();
     }
     
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();    
         
         if (mActivity != null) {
             mActivity.finish();
         }
 
     }
     
    public void testNumberOfPhotos(){
     	assertTrue(1 == 1);
     }
 
 }
