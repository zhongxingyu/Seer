 /*
  * Copyright (C) 2013 The MoKee OpenSource Project
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
 
 package com.mokee.helper.activities;
 
 import com.mokee.helper.R;
 import com.mokee.helper.adapters.TabsAdapter;
 
 import android.app.ActionBar;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 
 public class MoKeeCenter extends FragmentActivity {
 
     public static final String KEY_MOKEE_SERVICE = "key_mokee_service";
     public static final String KEY_MOKEE_UPDATER = "key_mokee_updater";
 
     private ViewPager mViewPager;
     private TabsAdapter mTabsAdapter;
     private static MenuItem refreshMenuItem;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mViewPager = new ViewPager(this);
         mViewPager.setId(R.id.viewPager);
         setContentView(mViewPager);
 
         final ActionBar bar = getActionBar();
         bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_SHOW_TITLE);
         bar.setDisplayHomeAsUpEnabled(true);
         bar.setTitle(R.string.mokee_center_title);
 
         mTabsAdapter = new TabsAdapter(this, mViewPager);
         mTabsAdapter.addTab(bar.newTab().setText(R.string.mokee_updater_title),
                 MoKeeUpdater.class, null);
         mTabsAdapter.addTab(bar.newTab().setText(R.string.mokee_support_title),
                 MoKeeSupport.class, null);
 
         if (savedInstanceState != null) {
             bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
         }
 
         // Turn on the Options Menu
         invalidateOptionsMenu();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.mokee_updater, menu);
         refreshMenuItem = menu.findItem(R.id.menu_refresh);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(final MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_refresh:
                 MoKeeUpdater.mTmpEntry.setTitle(R.string.tmp_entry1);
             case android.R.id.home:
                 onBackPressed();
                return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     public static void refreshMenuItem(int position) {
         refreshMenuItem.setVisible(position == 0);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
     }
 
 }
