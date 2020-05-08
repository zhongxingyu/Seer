 //    Raspberry LEDs is an application to remotely control Raspberry Pi LEDs connected to the GPIO.
 //    Copyright (C) 2013 Adrián Romero Corchado.
 //
 //    This file is part of Web Common
 //
 //     Licensed under the Apache License, Version 2.0 (the "License");
 //     you may not use this file except in compliance with the License.
 //     You may obtain a copy of the License at
 //     
 //         http://www.apache.org/licenses/LICENSE-2.0
 //     
 //     Unless required by applicable law or agreed to in writing, software
 //     distributed under the License is distributed on an "AS IS" BASIS,
 //     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //     See the License for the specific language governing permissions and
 //     limitations under the License.
 
 package com.adr.raspberryleds;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.app.FragmentManager;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Switch;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MainActivity extends Activity implements LedDataFragment.LedDataCallbacks {
 
     private static final int REQUEST_CODE = 332341;
 
     private LedDataFragment leddata;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
 
         PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
         setContentView(R.layout.activity_main);
 
 //    ConnectivityManager connMgr = (ConnectivityManager)
 //            getSystemService(Context.CONNECTIVITY_SERVICE);
 //        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 //        if (networkInfo != null && networkInfo.isConnected()) {
 //            // fetch data
 //        } else {
 //            // display error
 //        }
 
         FragmentManager fm = getFragmentManager();
         leddata = (LedDataFragment) fm.findFragmentByTag(LedDataFragment.TAG);
         if (leddata == null) {
             leddata = new LedDataFragment();
             fm.beginTransaction().add(leddata, LedDataFragment.TAG).commit();
         }
 
 
     }
 
     @Override
     public void onResume() {
         super.onResume();
         Log.d("com.adr.raspberryleds.MainActivity", "Attach LedData");
         leddata.setLedDataListener(this);
         leddata.loadInit(getSettingRpiUrl());
     }
 
     @Override
     public void onPause() {
         super.onPause();
         Log.d("com.adr.raspberryleds.MainActivity", "Dettach LedData");
         leddata.setLedDataListener(null);
     }
 
     @Override
     public void onStartLoadLedData() {
 
         Log.d("com.adr.raspberryleds.MainActivity", "StartLoadedLedData");
 
         findViewById(R.id.progressView).setVisibility(View.VISIBLE);
     }
 
     @Override
     public void onFinishLoadLedData() {
 
         Log.d("com.adr.raspberryleds.MainActivity", "FinishLoadedLedData");
 
         if (leddata.hasDataException()) {
             Log.d("com.adr.raspberryleds.MainActivity", leddata.toString());
             DialogFragment newFragment = new CannotReachFragment();
             newFragment.show(MainActivity.this.getFragmentManager(), CannotReachFragment.TAG);
         }
     }
 
     @Override
     public void onCancelLoadLedData() {
 
         Log.d("com.adr.raspberryleds.MainActivity", "CancelLoadedLedData");
 
         if (leddata.hasDataException()) {
             Log.d("com.adr.raspberryleds.MainActivity", leddata.toString());
             DialogFragment newFragment = new CannotReachFragment();
             newFragment.show(MainActivity.this.getFragmentManager(), CannotReachFragment.TAG);
         }
     }
 
     @Override
     public void onRefreshLedData() {
 
         Log.d("com.adr.raspberryleds.MainActivity", "RefreshLedData");
 
        findViewById(R.id.progressView).setVisibility(leddata.hasData() ? View.GONE : View.VISIBLE);

         findViewById(R.id.switch0).setEnabled(leddata.getLedEnabled("LED0"));
         findViewById(R.id.switch1).setEnabled(leddata.getLedEnabled("LED1"));
         findViewById(R.id.switch2).setEnabled(leddata.getLedEnabled("LED2"));
         findViewById(R.id.switch3).setEnabled(leddata.getLedEnabled("LED3"));
         findViewById(R.id.switch4).setEnabled(leddata.getLedEnabled("LED4"));
         findViewById(R.id.switch5).setEnabled(leddata.getLedEnabled("LED5"));
         findViewById(R.id.switch6).setEnabled(leddata.getLedEnabled("LED6"));
         findViewById(R.id.switch7).setEnabled(leddata.getLedEnabled("LED7"));
         ((Switch) findViewById(R.id.switch0)).setChecked(leddata.getLedStatus("LED0"));
         ((Switch) findViewById(R.id.switch1)).setChecked(leddata.getLedStatus("LED1"));
         ((Switch) findViewById(R.id.switch2)).setChecked(leddata.getLedStatus("LED2"));
         ((Switch) findViewById(R.id.switch3)).setChecked(leddata.getLedStatus("LED3"));
         ((Switch) findViewById(R.id.switch4)).setChecked(leddata.getLedStatus("LED4"));
         ((Switch) findViewById(R.id.switch5)).setChecked(leddata.getLedStatus("LED5"));
         ((Switch) findViewById(R.id.switch6)).setChecked(leddata.getLedStatus("LED6"));
         ((Switch) findViewById(R.id.switch7)).setChecked(leddata.getLedStatus("LED7"));
 
         invalidateOptionsMenu();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
 
         super.onPrepareOptionsMenu(menu);
 
         // Set Enable status accofing
         MenuItem speakmenu = menu.findItem(R.id.action_speak);
         speakmenu.setEnabled(this.findViewById(R.id.switch0).isEnabled());
 
         // Disable button if no recognition service is present
         PackageManager pm = getPackageManager();
         List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
         if (activities.size() == 0) {
             speakmenu.setEnabled(false);
         }
 
         return true;
     }
 
     public void onToggleClicked(View view) {
 
         Switch sw = ((Switch) view);
         leddata.execute(new LedCommand(new String[]{(String) sw.getTag()}, sw.isChecked() ? LedCommand.CMD_SWITCH_ON : LedCommand.CMD_SWITCH_OFF));
     }
 
     public void onRefreshClicked(MenuItem item) {
         Log.d("com.adr.raspberryleds.LedInformation", "refreshing");
         leddata.loadForce();
     }
 
     public void onSpeakClicked(MenuItem item) {
         Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
         intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
         intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.adr.raspberryleds");
         intent.putExtra(RecognizerIntent.EXTRA_PROMPT, this.getResources().getString(R.string.msg_speak_prompt));
 
         startActivityForResult(intent, REQUEST_CODE);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
 
             ArrayList<String> speechmatches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
             LedCommandParser vcparser = new LedCommandParser(this.getResources());
             LedCommand vc = vcparser.parseVoiceCommand(speechmatches);
             if (vc == null) {
                 Toast toast = Toast.makeText(getApplicationContext(),
                         this.getResources().getString(R.string.msg_command_not_recognized),
                         Toast.LENGTH_SHORT);
                 toast.show();
             } else {
                 // parse command and return ..
                 leddata.execute(vc);
             }
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 
     public void onSettingsClicked(MenuItem item) {
 
         Intent intent = new Intent(this, SettingsActivity.class);
         startActivity(intent);
     }
 
     public void onAboutClicked(MenuItem item) {
 
         Intent intent = new Intent(this, AboutActivity.class);
         startActivity(intent);
     }
 
     private String getSettingRpiUrl() {
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
         return sharedPref.getString("pref_rpi_url", "");
     }
 }
