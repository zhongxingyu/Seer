 /*
  *  This file is a part of GPS Lock-Lock Android application.
  *  Copyright (C) 2011 Tomasz Dudziak
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.tdudziak.gps_lock_lock;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.view.View;
 import android.support.v4.content.LocalBroadcastManager;
 import android.text.Html;
 
 public class ControlActivity extends Activity implements OnItemClickListener
 {
     private TextView mTextStatus;
     private ProgressBar mProgressStatus;
     private BroadcastReceiver mUiUpdateBroadcastReceiver;
 
     private ListView mListMenu;
     private String[] mListMenuItems;
     private ArrayAdapter<String> mListMenuAdapter;
 
     private static final int MENU_RESTART = 0;
     private static final int MENU_SETTINGS = 1;
     private static final int MENU_HELP = 2;
     private static final int MENU_STOP = 3;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         mTextStatus = (TextView) findViewById(R.id.textStatus);
         mProgressStatus = (ProgressBar) findViewById(R.id.progressStatus);
 
         // setup the menu ListView
         mListMenu = (ListView) findViewById(R.id.listMenu);
         mListMenuItems = new String[] {
                 "", // dynamically generated in onResume()
                 getString(R.string.menu_settings),
                 getString(R.string.menu_help),
                 getString(R.string.menu_stop)
         };
         mListMenuAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListMenuItems);
         mListMenu.setAdapter(mListMenuAdapter);
         mListMenu.setOnItemClickListener(this);
 
         mUiUpdateBroadcastReceiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 int left = intent.getIntExtra(LockService.EXTRA_TIME_LEFT, -1);
                 assert left != -1;
 
                 setStatus(left);
 
                 if(left <= 0) {
                     // This is the last message; no time left. Shutdown.
                     finish();
                 }
             }
         };
     }
 
     @Override
     protected void onPause() {
         LocalBroadcastManager.getInstance(this).unregisterReceiver(mUiUpdateBroadcastReceiver);
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
         IntentFilter filter = new IntentFilter(LockService.ACTION_UI_UPDATE);
         bm.registerReceiver(mUiUpdateBroadcastReceiver, filter);
 
         // request UI update broadcast from the service
         Intent intent = new Intent(LockService.ACTION_UI_UPDATE);
         intent.setClass(this, LockService.class);
         startService(intent);
         setStatus(0);
 
         // if service is already running we can update status text immediately
         LockService service = ((LockApplication) getApplication()).getLockService();
         if(service != null) {
             setStatus(service.getRemainingTime());
         }
 
         // update text on "Restart" list item
         String format = getResources().getString(R.string.menu_restart);
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         int lock_time = prefs.getInt("lockTime", 5);
         mListMenuItems[MENU_RESTART] = String.format(format, lock_time);
         mListMenuAdapter.notifyDataSetChanged();
 
         super.onResume();
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         Intent intent;
 
         switch(position) {
         case MENU_SETTINGS:
             intent = new Intent(this, AppPreferenceActivity.class);
             startActivityForResult(intent, 0);
             break;
 
         case MENU_HELP:
             intent = new Intent(this, AboutActivity.class);
             startActivityForResult(intent, 0);
             break;
 
         case MENU_RESTART:
             intent = new Intent(LockService.ACTION_RESTART);
             intent.setClass(this, LockService.class);
             startService(intent);
             break;
 
         case MENU_STOP:
             intent = new Intent(LockService.ACTION_SHUTDOWN);
             intent.setClass(this, LockService.class);
             startService(intent);
             break;
         }
     }
 
     private void setStatus(int minutes) {
         if(minutes == 0) {
            mTextStatus.setText("");
             mProgressStatus.setVisibility(View.VISIBLE);
         } else {
             String s_format = getResources().getString(R.string.text_status);
             String text = String.format(s_format, minutes);
             mTextStatus.setText(Html.fromHtml(text));
             mProgressStatus.setVisibility(View.INVISIBLE);
         }
     }
 }
