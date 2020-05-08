 /*
  * Copyright (C) 2012 Gregory S. Meiste  <http://gregmeiste.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.meiste.greg.ptw;
 
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.view.ViewPager;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.ads.Ad;
 import com.google.ads.AdListener;
 import com.google.ads.AdRequest;
 import com.google.ads.AdRequest.ErrorCode;
 import com.google.ads.AdView;
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class MainActivity extends SherlockFragmentActivity implements Eula.OnEulaAgreedTo {
 
     public static final String INTENT_TAB = "tab_select";
     private final String LAST_TAB = "tab.last";
 
     private ViewPager mPager;
     private TitlePageIndicator mIndicator;
     private AdView mAdView;
     private AlertDialog mLegalDialog;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         if (Eula.show(this)) {
             onEulaAgreedTo();
         }
 
         setContentView(R.layout.main);
 
         mPager = (ViewPager)findViewById(R.id.pager);
         mPager.setOffscreenPageLimit(2);
         mPager.setAdapter(new TabFragmentAdapter(getSupportFragmentManager(), this));
 
         mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
         mIndicator.setViewPager(mPager);
         mIndicator.setCurrentItem(getTab(getIntent()));
 
         AdRequest adRequest = new AdRequest();
         adRequest.addKeyword("NASCAR");
         adRequest.addKeyword("racing");
 
         if (BuildConfig.DEBUG) {
             adRequest.addTestDevice("CB529BCBD1E778FAD10EE145EE29045F"); // Atrix 4G
             adRequest.addTestDevice("36A52B9CBB347B995EA40ACDD0D36376"); // XOOM
             adRequest.addTestDevice("E64392AEFC7C9A13D2A6A76E9EA034C4"); // RAZR
         }
 
         mAdView = (AdView)findViewById(R.id.ad);
         mAdView.setAdListener(new MyAdListener());
         mAdView.loadAd(adRequest);
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         Util.log("Saving state: tab=" + mPager.getCurrentItem());
         Util.getState(this).edit().putInt(LAST_TAB, mPager.getCurrentItem()).commit();
 
         // Hide dialogs to prevent window leaks on orientation changes
         Eula.hide();
         if ((mLegalDialog != null) && (mLegalDialog.isShowing())) {
             mLegalDialog.dismiss();
         }
     }
 
     @Override
     public void onDestroy() {
        mAdView.removeAllViews();
         mAdView.destroy();
        super.onDestroy();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.settings:
             startActivity(new Intent(this, EditPreferences.class));
             return true;
 
         case R.id.legal:
             final AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle(R.string.legal);
             builder.setCancelable(true);
             builder.setPositiveButton(R.string.ok, null);
             builder.setMessage(R.string.legal_content);
             mLegalDialog = builder.create();
             mLegalDialog.show();
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onEulaAgreedTo() {
         RaceAlarm.set(this);
         QuestionAlarm.set(this);
     }
 
     private int getTab(Intent intent) {
         // Recent applications caches intent with extras. Only want to listen
         // to INTENT_TAB extra if launched from notification.
         if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
             int intent_tab = intent.getIntExtra(INTENT_TAB, -1);
             if (intent_tab >= 0) {
                 return intent_tab;
             }
         }
 
         return Util.getState(this).getInt(LAST_TAB, 0);
     }
 
     private class MyAdListener implements AdListener {
 
         @Override
         public void onReceiveAd(Ad ad) {
             Util.log("onReceiveAd");
         }
 
         @Override
         public void onFailedToReceiveAd(Ad ad, ErrorCode err) {
             Util.log("onFailedToReceiveAd: " + err);
         }
 
         @Override
         public void onLeaveApplication(Ad ad) {}
 
         @Override
         public void onPresentScreen(Ad ad) {}
 
         @Override
         public void onDismissScreen(Ad ad) {}
     }
 }
