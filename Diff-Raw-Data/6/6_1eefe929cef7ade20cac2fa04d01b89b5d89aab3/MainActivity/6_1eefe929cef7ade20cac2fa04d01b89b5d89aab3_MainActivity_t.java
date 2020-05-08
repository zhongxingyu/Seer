 package com.frca.vsexam;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.widget.SlidingPaneLayout;
 import android.support.v7.app.ActionBarActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.frca.vsexam.fragments.BrowserPaneFragment;
 import com.frca.vsexam.fragments.LoadingFragment;
 import com.frca.vsexam.fragments.LoginFragment;
 import com.frca.vsexam.fragments.MainActivityFragment;
 import com.frca.vsexam.helper.DataHolder;
 import com.frca.vsexam.network.HttpRequestBuilder;
 
 public class MainActivity extends ActionBarActivity {
 
     private Fragment currentFragment;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         SharedPreferences preferences = DataHolder.getInstance(this).getPreferences();
         if (preferences.contains(HttpRequestBuilder.KEY_LOGIN) && preferences.contains(HttpRequestBuilder.KEY_PASSWORD)) {
             setFragment(new LoadingFragment("Preparing"));
         } else {
             setFragment(new LoginFragment());
         }
     }
 
     public void setFragment(Fragment fragment) {
         getSupportFragmentManager().beginTransaction().replace(R.id.container_base, fragment).commit();
         currentFragment = fragment;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
        // TODO
    }

    @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home: {
                 if (currentFragment instanceof BrowserPaneFragment) {
                     SlidingPaneLayout slidingPaneLayout = ((BrowserPaneFragment) currentFragment).getSlidingLayout();
                     if (slidingPaneLayout.isSlideable()) {
                         onBackPressed();
                         return true;
                     }
                 }
                 break;
             }
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onBackPressed() {
         if (currentFragment instanceof MainActivityFragment)
             if (((MainActivityFragment)currentFragment).onBackPressed())
                 return;
 
         super.onBackPressed();
     }
 }
