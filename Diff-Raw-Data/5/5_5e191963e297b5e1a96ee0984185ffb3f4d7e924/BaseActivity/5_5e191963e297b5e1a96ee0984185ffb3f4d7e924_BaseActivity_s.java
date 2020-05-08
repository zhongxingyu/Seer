 package com.feedme.activity;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import com.feedme.R;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 import java.security.PublicKey;
 
 /**
  * User: steve quick
  * Date: 1/22/12
  * Time: 9:34 AM
  */
 
 public abstract class BaseActivity extends Activity
 {
     public GoogleAnalyticsTracker googleAnalyticsTracker = GoogleAnalyticsTracker.getInstance();
     public static final String TRACKING_ID = "UA-29019171-1";
     public static final String PUBLISHER_ID = "a14f34a6ae2ff13";
 
     public static final int DATE_DIALOG_ID = 0;
     public static final int START_TIME_DIALOG_ID = 1;
     public static final int END_TIME_DIALOG_ID = 2;
     public static final int VIEW_BABY_ACTIVITY_ID = 3;
     public static final int ADD_CHILD_ACTIVITY_ID = 5;
     public static final int EDIT_CHILD_ACTIVITY_ID = 6;
     public static final int ADD_DIAPER_ACTIVITY_ID = 7;
     public static final int EDIT_DIAPER_ACTIVITY_ID = 8;
     public static final int VIEW_DIAPER_ACTIVITY_ID = 9;
 
     public AdView adView;
 
     public void styleActivity(String babyGender)
     {
         final RelativeLayout topBanner = (RelativeLayout) findViewById(R.id.topBanner);
         final RelativeLayout bottomBanner = (RelativeLayout) findViewById(R.id.bottomBanner);
 
         if (babyGender.equals("Male")) {
             topBanner.setBackgroundColor(0xFF7ED0FF);
             bottomBanner.setBackgroundColor(0xFF7ED0FF);
         } else {
             topBanner.setBackgroundColor(0xFFFF99CC);
             bottomBanner.setBackgroundColor(0xFFFF99CC);
         }
     }
 
     @Override
     public void onDestroy()
     {
        adView.destroy();
         super.onDestroy();
     }
 
     public void displayAd(Activity activity)
     {
         AdRequest adRequest = new AdRequest();
         adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
         adRequest.addTestDevice("7D1B47DD60946FBC9C2BF3D70A9DB3E0");
 
         adView = new AdView(activity, AdSize.BANNER, PUBLISHER_ID);
 
         LinearLayout layout = (LinearLayout)findViewById(R.id.adBar);
         layout.addView(adView);
         adView.loadAd(new AdRequest());
     }
     
     /**
      * Resizes a Bitmap based on the passed in newHeight and newWidth and rotates the image by rotateInDegrees.
      *
      * @param bitMap
      * @param newHeight
      * @param newWidth
      * @param rotateInDegrees
      * @return
      */
     public Bitmap getResizedBitmap(Bitmap bitMap, int newHeight, int newWidth, int rotateInDegrees)
     {
         int width = bitMap.getWidth();
         int height = bitMap.getHeight();
         float scaleWidth = ((float) newWidth) / width;
         float scaleHeight = ((float) newHeight) / height;
 
         // create a matrix for the manipulation
         Matrix matrix = new Matrix();
 
         // resize the bit map
         matrix.postScale(scaleWidth, scaleHeight);
         //matrix.postRotate(rotateInDegrees);
 
         // recreate the new Bitmap
         Bitmap resizedBitmap = Bitmap.createBitmap(bitMap, 0, 0, width, height, matrix, false);
 
         return resizedBitmap;
     }
     
 }
