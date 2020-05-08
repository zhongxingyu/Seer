 package com.siu.android.andgapisutils.activity.ads;
 
 import com.mobclix.android.sdk.MobclixAdView;
 import com.siu.android.andgapisutils.activity.tracked.TrackedSherlockMapActivity;
 import com.siu.android.andutils.R;
 import com.siu.android.andutils.ads.AdViewBasicListener;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
  */
 public class AdsSherlockMapActivity extends TrackedSherlockMapActivity {
 
     protected MobclixAdView adBannerView;
 
     @Override
     protected void onResume() {
         super.onResume();
 
         if (null != adBannerView) {
             adBannerView.resume();
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         if (null != adBannerView) {
             adBannerView.pause();
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
 
         if (null != adBannerView) {
             adBannerView.destroy();
         }
     }
 
     protected void initAds() {
         adBannerView = (MobclixAdView) findViewById(R.id.ad_banner_view);
         adBannerView.addMobclixAdViewListener(new AdViewBasicListener());
     }
 }
