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
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
 import android.text.method.LinkMovementMethod;
 
 public class ControlActivity extends Activity implements OnClickListener
 {
     private TextView mTextStatus;
     private ProgressBar mProgressStatus;
     private BroadcastReceiver mUiUpdateBroadcastReceiver;
     private Button mButtonRestart;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         // Enable links in textInfo.
         TextView info = (TextView) findViewById(R.id.textInfo);
         info.setMovementMethod(LinkMovementMethod.getInstance());
 
         findViewById(R.id.buttonStop).setOnClickListener(this);
 
         mButtonRestart = (Button) findViewById(R.id.buttonRestart);
         mButtonRestart.setOnClickListener(this);
 
         mTextStatus = (TextView) findViewById(R.id.textStatus);
         mProgressStatus = (ProgressBar) findViewById(R.id.progressStatus);
 
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
     public void onClick(View v) {
         Intent intent;
 
         switch(v.getId()) {
         case R.id.buttonRestart:
             intent = new Intent(LockService.ACTION_RESTART);
             intent.setClass(this, LockService.class);
             startService(intent);
             break;
 
         case R.id.buttonStop:
             intent = new Intent(LockService.ACTION_SHUTDOWN);
             intent.setClass(this, LockService.class);
             startService(intent);
             break;
         }
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
         setStatus(null);
 
         // update the text on restart button
         String r_format = getResources().getString(R.string.button_restart);
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
         int lock_time = prefs.getInt("lockTime", 5);
         mButtonRestart.setText(String.format(r_format, lock_time));
 
         super.onResume();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.options_menu, menu);
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         Intent intent;
 
         switch(item.getItemId()) {
         case R.id.menuItemSettings:
             intent = new Intent(this, AppPreferenceActivity.class);
             startActivityForResult(intent, 0);
             return true;
 
         case R.id.menuItemAbout:
             intent = new Intent(this, AboutActivity.class);
             startActivityForResult(intent, 0);
             return true;
         }
 
         return super.onMenuItemSelected(featureId, item);
     }
 
     private void setStatus(Integer minutes) {
         if(minutes == null) {
             mTextStatus.setVisibility(View.INVISIBLE);
             mProgressStatus.setVisibility(View.VISIBLE);
         } else {
             String s_format = getResources().getString(R.string.text_status);
            String text = String.format(s_format, minutes);
            mTextStatus.setText(Html.fromHtml(text));
             mProgressStatus.setVisibility(View.INVISIBLE);
             mTextStatus.setVisibility(View.VISIBLE);
         }
     }
 }
