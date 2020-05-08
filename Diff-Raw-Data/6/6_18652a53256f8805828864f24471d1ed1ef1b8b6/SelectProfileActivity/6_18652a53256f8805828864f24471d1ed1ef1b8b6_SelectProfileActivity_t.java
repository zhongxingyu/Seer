 package com.softevol.appsystemimpl.activity;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import com.softevol.appsystemimpl.R;
 import com.softevol.appsystemimpl.fragment.TabsFragment;
 
 /**
  * User: antony
  * Date: 1/22/13
  * Time: 5:08 PM
  */
 public class SelectProfileActivity extends BaseActivity {
 
     public static void launch(Context context) {
         launch(context, null);
     }
 
     public static void launch(Context context, Bundle params) {
         Intent intent = new Intent(context, SelectProfileActivity.class);
         intent.replaceExtras(params);
         context.startActivity(intent);
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_select_profile);
     }
 
     public void handleOpenMain(View view) {
         MainActivity.launch(this);
         // We don't need to give user an ability to return here
         finish();
     }
 
     @Override
     protected void onResumeFragments() {
         super.onResumeFragments();
 
         TabsFragment tabsFragment = (TabsFragment) getSupportFragmentManager().findFragmentById(R.id.tabs_fragment);
         tabsFragment.clearTabs();
 
        TabsFragment.TabView createdTabView = new TabsFragment.TabView(this, getString(R.string.tab_profile));
         createdTabView.setId(1);
         tabsFragment.addTab(createdTabView);
     }
 
     public void handleLogin(View view) {
         MainActivity.launch(this);
     }
 }
