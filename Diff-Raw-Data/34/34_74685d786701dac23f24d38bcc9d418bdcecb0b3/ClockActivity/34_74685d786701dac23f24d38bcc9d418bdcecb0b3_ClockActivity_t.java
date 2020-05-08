 /*******************************************************************************
  * Copyright (c) 2011 Ethan Hall
  *
  * Permission is hereby granted, free of charge, to any person obtaining a
  * copy of this software and associated documentation files (the "Software"),
  *  to deal in the Software without restriction, including without limitation
  * the rights to use, copy, modify, merge, publish, distribute, sublicense,
  * and/or sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included
  * in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
  * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  ******************************************************************************/
 
 package com.kopysoft.chronos.activities;
 
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Shader;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.kopysoft.chronos.R;
 import com.kopysoft.chronos.activities.Editors.JobEditor;
 import com.kopysoft.chronos.activities.Editors.NewPunchActivity;
 import com.kopysoft.chronos.activities.Editors.NoteEditor;
 import com.kopysoft.chronos.activities.Editors.TaskList;
 import com.kopysoft.chronos.adapter.clock.PayPeriodAdapterList;
 import com.kopysoft.chronos.content.Chronos;
 import com.kopysoft.chronos.content.Email;
import com.kopysoft.chronos.content.EnableWidget;
 import com.kopysoft.chronos.enums.Defines;
 import com.kopysoft.chronos.enums.PayPeriodDuration;
 import com.kopysoft.chronos.types.Job;
 import com.kopysoft.chronos.types.holders.PayPeriodHolder;
 import com.kopysoft.chronos.types.holders.PunchTable;
 import com.kopysoft.chronos.views.ClockFragments.PayPeriod.PayPeriodSummaryView;
 import com.kopysoft.chronos.views.ClockFragments.Today.DatePairView;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Duration;
 
 public class ClockActivity extends SherlockActivity implements ActionBar.TabListener{
     
     private static String TAG = Defines.TAG + " - ClockActivity";
     private PunchTable localPunchTable;
     public static int FROM_CLOCK_ACTIVITY = 0;
     private Job jobId;
     private PayPeriodHolder payHolder;
 
     private static final boolean enableLog = Defines.DEBUG_PRINT;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.header);
 
         Chronos chronos = new Chronos(this);
         Job curJob = chronos.getAllJobs().get(0);
         jobId = curJob;
         localPunchTable = chronos.getAllPunchesForThisPayPeriodByJob(curJob);
         chronos.close();
         
         if(savedInstanceState != null){
             payHolder = (PayPeriodHolder)savedInstanceState.getSerializable("payPeriod");
         } else {
             payHolder = new PayPeriodHolder(curJob);
         }
 
         //getSupportActionBar().setListNavigationCallbacks(list, this)
         //This is a workaround for http://b.android.com/15340 from http://stackoverflow.com/a/5852198/132047
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(R.drawable.bg_striped);
             bg.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
             getSupportActionBar().setBackgroundDrawable(bg);
 
             BitmapDrawable bgSplit = (BitmapDrawable)getResources().getDrawable(R.drawable.bg_striped_split_img);
             bgSplit.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
             getSupportActionBar().setSplitBackgroundDrawable(bgSplit);
         }
 
         getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         ActionBar.Tab tab = getSupportActionBar().newTab();
         tab.setText("Today");
         tab.setTabListener(this);
         getSupportActionBar().addTab(tab);
 
         tab = getSupportActionBar().newTab();
         tab.setText("Pay Period");
         tab.setTabListener(this);
         getSupportActionBar().addTab(tab);
 
         if(savedInstanceState != null){
             getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("position"));
         }
 
         SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
         SharedPreferences.Editor edit = pref.edit();
         edit.putString("normal_pay", Float.toString(curJob.getPayRate()) );
         edit.putString("over_time_threshold", Float.toString(curJob.getOvertime()) );
         edit.putString("double_time_threshold", Float.toString(curJob.getDoubleTime()) );
         edit.putBoolean("enable_overtime", curJob.isOverTimeEnabled());
         edit.remove("8_or_40_hours");
         edit.commit();
 
         Duration dur = PayPeriodAdapterList.getTime(localPunchTable.getPunchPair(new DateTime()), true);
         Intent runIntent = new Intent().setClass(this,
                 com.kopysoft.chronos.content.NotificationBroadcast.class);
         runIntent.putExtra("timeToday", dur.getMillis());
         this.sendBroadcast(runIntent);
     }
 
     @Override
     protected void onSaveInstanceState (Bundle outState)
     {
         outState.putInt("position", getSupportActionBar().getSelectedTab().getPosition());
         outState.putSerializable("payPeriod", payHolder);
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         //getSupportMenuInflater().inflate(R.menu.action_bar, menu);
 
         int pos = getSupportActionBar().getSelectedTab().getPosition();
         if(pos == 1){
             //menu.findItem(R.id.menu_insert).setVisible(false);
             //menu.findItem(R.id.menu_note).setVisible(false);
             //menu.findItem(R.id.menu_quick_note).setVisible(false);
             getSupportMenuInflater().inflate(R.menu.action_bar_pay_period, menu);
         } else {
             //menu.findItem(R.id.menu_navigate).setVisible(false);
             getSupportMenuInflater().inflate(R.menu.action_bar_today, menu);
         }
 
         return super.onCreateOptionsMenu(menu);
     }
     
     private PunchTable getPunchesByDate(){
         Chronos chronos = new Chronos(this);
         PunchTable temp =  chronos.getAllPunchesForPayPeriodByJob(jobId,
                 payHolder.getStartOfPayPeriod(), payHolder.getEndOfPayPeriod());
         chronos.close();
         return temp;
     }
 
 
     //@Override
     public void onTabSelected(ActionBar.Tab tab) {
 
         invalidateOptionsMenu();    //Redo the menu
         if(tab.getPosition() == 0){
             setContentView(new DatePairView(this,
                     localPunchTable.getPunchesByDay(new DateTime()),
                     DateTime.now()));
         } else if(tab.getPosition() == 1){
             setContentView(new PayPeriodSummaryView(this, getPunchesByDate( ) ) );
         }
         if(enableLog) Log.d(TAG, "onTabSelected: " + tab);
         if(enableLog) Log.d(TAG, "onTabSelected Position: " + tab.getPosition());
     }
 
     //@Override
     public void onTabUnselected(ActionBar.Tab tab) {
         if(enableLog) Log.d(TAG, "onTabUnselected: " + tab);
     }
 
     //@Override
     public void onTabReselected(ActionBar.Tab tab) {
         if(enableLog) Log.d(TAG, "onTabReselected: " + tab);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(enableLog) Log.d(TAG, "Request Code: " + requestCode);
         if(enableLog) Log.d(TAG, "Result Code: " + resultCode);
         if(enableLog) Log.d(TAG, "Selected Navigation Index: " + getSupportActionBar().getSelectedNavigationIndex());
 
         if (requestCode == FROM_CLOCK_ACTIVITY) {
             Chronos chronos = new Chronos(this);
             localPunchTable = chronos.getAllPunchesForThisPayPeriodByJob(chronos.getAllJobs().get(0));
             chronos.close();
         } else if(requestCode == NewPunchActivity.NEW_PUNCH){
             if(enableLog) Log.d(TAG, "New Punch Created");
             Chronos chronos = new Chronos(this);
             localPunchTable = chronos.getAllPunchesForThisPayPeriodByJob(chronos.getAllJobs().get(0));
             chronos.close();
         } else if(requestCode == JobEditor.UPDATE_JOB){
             Chronos chron = new Chronos(this);
             Job thisJob = chron.getAllJobs().get(0);
             SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
             thisJob.setPayRate(Float.valueOf(pref.getString("normal_pay", "7.25")) );
             thisJob.setOvertimeEnabled(pref.getBoolean("enable_overtime", true));
             thisJob.setOvertime(Float.valueOf(pref.getString("over_time_threshold", "40")) );
             thisJob.setDoubletimeThreshold(Float.valueOf(pref.getString("double_time_threshold", "60")) );
             String date[] = pref.getString("date", "2011.1.17").split("\\p{Punct}");
             String time[] = pref.getString("time", "00:00").split("\\p{Punct}");
             thisJob.setStartOfPayPeriod(new DateTime(Integer.parseInt(date[0]),
                     Integer.parseInt(date[1]),
                     Integer.parseInt(date[2]),
                     Integer.parseInt(time[0]),
                     Integer.parseInt(time[1])
             ).withZone(DateTimeZone.getDefault()));
             switch (Integer.parseInt(pref.getString("len_of_month", "2"))){
                 case 1:
                     thisJob.setDuration(PayPeriodDuration.ONE_WEEK);
                     break;
                 case 2:
                     thisJob.setDuration(PayPeriodDuration.TWO_WEEKS);
                     break;
                 case 3:
                     thisJob.setDuration(PayPeriodDuration.THREE_WEEKS);
                     break;
                 case 4:
                     thisJob.setDuration(PayPeriodDuration.FOUR_WEEKS);
                     break;
                 case 5:
                     thisJob.setDuration(PayPeriodDuration.FULL_MONTH);
                     break;
                 case 6:
                     thisJob.setDuration(PayPeriodDuration.FIRST_FIFTEENTH);
                     break;
                 default:
                     thisJob.setDuration(PayPeriodDuration.TWO_WEEKS);
                     break;
             }
             chron.updateJob(thisJob);
             localPunchTable = chron.getAllPunchesForThisPayPeriodByJob(thisJob);
             payHolder = new PayPeriodHolder(thisJob);
             chron.close();
         } else if(requestCode == QuickBreakActivity.NEW_BREAK){
             if(enableLog)Log.d(TAG, "Got new break");
             Chronos chronos = new Chronos(this);
             localPunchTable = chronos.getAllPunchesForThisPayPeriodByJob(chronos.getAllJobs().get(0));
             chronos.close();
         } else {
             Chronos chronos = new Chronos(this);
             localPunchTable = chronos.getAllPunchesForThisPayPeriodByJob(chronos.getAllJobs().get(0));
             chronos.close();
         }
 
         //Send intent to create notification
         Duration dur = PayPeriodAdapterList.getTime(localPunchTable.getPunchPair(new DateTime()), true);
         Intent runIntent = new Intent().setClass(this,
                 com.kopysoft.chronos.content.NotificationBroadcast.class);
         runIntent.putExtra("timeToday", dur.getMillis());
         this.sendBroadcast(runIntent);
 
         if(getSupportActionBar().getSelectedNavigationIndex() == 0){
             setContentView(new DatePairView(this,
                     localPunchTable.getPunchesByDay(new DateTime()),
                     DateTime.now()));
             //setContentView(new DatePairView(this, new DateTime()));
         } else if(getSupportActionBar().getSelectedNavigationIndex() == 1){
             setContentView(new PayPeriodSummaryView(this, getPunchesByDate( ) ) );
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if(enableLog) Log.d(TAG, "Selected item: " + item);
         if(enableLog) Log.d(TAG, "Selected item id: " + item.getItemId());
         switch (item.getItemId()) {
             case R.id.menu_insert:
                 Intent newIntent =
                         new Intent().setClass(this,
                                 NewPunchActivity.class);
 
                 newIntent.putExtra("job", (long)jobId.getID());
                 newIntent.putExtra("date", DateTime.now().getMillis());
                 startActivityForResult(newIntent, NewPunchActivity.NEW_PUNCH);
                 return true;
             case R.id.menu_navigate_today:
                 payHolder.generate();
                 setContentView(new PayPeriodSummaryView(this, getPunchesByDate( ) ) );
                 return true;
             case R.id.menu_navigate_back:
                 payHolder.moveBackwards();
                 setContentView(new PayPeriodSummaryView(this, getPunchesByDate( ) ) );
                 return true;
             case R.id.menu_navigate_forward:
                 payHolder.moveForwards();
                 setContentView(new PayPeriodSummaryView(this, getPunchesByDate( ) ) );
                 return true;
             case R.id.menu_note:
                 newIntent =
                         new Intent().setClass(this,
                                 NoteEditor.class);
                 newIntent.putExtra("date", DateTime.now().getMillis());
 
                 startActivity(newIntent);
                 return true;
             case R.id.menu_configure_job:
                 newIntent =
                         new Intent().setClass(this,
                                 JobEditor.class);
 
                 startActivityForResult(newIntent, JobEditor.UPDATE_JOB);
                 return true;
             case R.id.menu_preferences:
                 newIntent =
                         new Intent().setClass(this,
                                 PreferencesActivity.class);
 
                 startActivityForResult(newIntent, JobEditor.UPDATE_JOB);
                 return true;
             case R.id.menu_configure_task:
                 newIntent =
                         new Intent().setClass(this,
                                 TaskList.class);
 
                 startActivity(newIntent);
                 return true;
 
             case R.id.menu_quick_note:
                 newIntent =
                         new Intent().setClass(this,
                                 QuickBreakActivity.class);
 
                 newIntent.putExtra("date", DateTime.now().getMillis());
                 startActivityForResult(newIntent, QuickBreakActivity.NEW_BREAK);
                 return true;
             case R.id.menu_email:
                 sendEmail();
                 return true;
             case android.R.id.home:
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     private void sendEmail(){
         //PayPeriodHolder payPeriodHolder, Job thisJob, Context context
 
         ProgressDialog dialog = ProgressDialog.show(ClockActivity.this, "",
                 "Generating. Please wait...");
         
         PayPeriodHolder pph;
         if(getSupportActionBar().getSelectedTab().getPosition() == 0){
             pph = new PayPeriodHolder(jobId);
         } else {
             pph = payHolder;
         }
         Email newEmail = new Email(pph, jobId, getApplicationContext());
 
         SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
         int reportLevel = Integer.valueOf(pref.getString("reportLevel", "1"));
 
         String returnValue;
         if(reportLevel == 2){
             returnValue = newEmail.getBriefView();
         } else {
             returnValue = newEmail.getExpandedView();
         }
 
         String emailBody = new String("Greetings!\n\tHere is my time card\n");
         emailBody += returnValue;
 
 
         dialog.dismiss();
 
         //Create email
         Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
         emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Time Card");
         emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);
 
         emailIntent.setType("message/rfc822");
         startActivity(emailIntent);
 
     }
 
     @Override
    public void onPause(){
        super.onPause();
        /*
        Intent runIntent = new Intent(this, com.kopysoft.chronos.content.EnableWidget.class);
        runIntent.setAction(EnableWidget.UPDATE_FROM_APP);
        this.sendBroadcast(runIntent);
        */
    }

    @Override
     public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
         invalidateOptionsMenu();    //Redo the menu
         if(tab.getPosition() == 0){
             setContentView(new DatePairView(this,
                     localPunchTable.getPunchesByDay(new DateTime()),
                     DateTime.now()));
         } else if(tab.getPosition() == 1){
             setContentView(new PayPeriodSummaryView(this, getPunchesByDate( ) ) );
         }
         if(enableLog) Log.d(TAG, "onTabSelected: " + tab);
         if(enableLog) Log.d(TAG, "onTabSelected Position: " + tab.getPosition());
     }
 
     @Override
     public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 
     }
 
     @Override
     public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 
     }
 }
