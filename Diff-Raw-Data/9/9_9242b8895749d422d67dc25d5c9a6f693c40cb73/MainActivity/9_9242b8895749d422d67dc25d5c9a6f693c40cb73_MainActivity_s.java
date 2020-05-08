 package com.chess.saldo;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.*;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import com.bcseime.android.chess.saldo2.R;
 import com.chess.saldo.service.entities.Saldo;
 import com.chess.saldo.service.entities.SaldoType;
 
 public class MainActivity extends Activity {
 
     private UpdateCompleteBroadcastReceiver receiver;
 
     private SettingsManager settings;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.settings = new SettingsManager(getApplicationContext());
         setContentView(R.layout.main_layout);
         if (!settings.isUserCredentialsSet()) {
             showPreferenceActivity();
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         IntentFilter filter = new IntentFilter(UpdateService.UPDATE_COMPLETE_ACTION);
         receiver = new UpdateCompleteBroadcastReceiver();
         registerReceiver(receiver, filter);
         updateUI();
     }
 
     @Override
     public void onPause() {
         unregisterReceiver(receiver);
         super.onPause();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuInflater menuInflater = getMenuInflater();
         menuInflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     protected void onStart() {
         super.onStart();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch (item.getItemId()) {
             case R.id.settings_item:
                 showPreferenceActivity();
                 return true;
 
             case R.id.update_item:
                 Intent intent = new Intent(this, UpdateService.class);
                 intent.putExtra(UpdateService.SHOW_TOAST, true);
                 startService(intent);
                 return true;
 
         }
         return false;
     }
 
     private void showPreferenceActivity() {
         Intent intent = new Intent(this, SettingsActivity.class);
         startActivity(intent);
     }
 
     private void updateUI() {
         runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 Saldo saldo = settings.getSaldo();
                 TextView cashTextView = (TextView) findViewById(R.id.cashValue);
                 cashTextView.setText(saldo.strMoneyUsed);
                updateSaldoItem(R.id.main_panel_data, R.id.dataValue, R.id.dataProgress, (int) saldo.dataLeft, (int) saldo.dataTotal, SaldoType.DATA.widgetName);
                 updateSaldoItem(R.id.main_panel_minutes, R.id.minutesValue, R.id.minutesProgress, saldo.minutesLeft, saldo.minutesTotal, SaldoType.MINUTES.widgetName);
                 updateSaldoItem(R.id.main_panel_mms, R.id.mmsValue, R.id.mmsProgress, saldo.mmsLeft, saldo.mmsTotal, SaldoType.MMS.widgetName);
                 updateSaldoItem(R.id.main_panel_sms, R.id.smsValue, R.id.smsProgress, saldo.smsLeft, saldo.smsTotal, SaldoType.SMS.widgetName);
             }
         });
     }
 
     private void updateSaldoItem(int panelId, int textViewId, int progressBarId, int progressValue, int progressMax, String unit) {
         ViewGroup panel = (ViewGroup) findViewById(panelId);
 
         if (progressValue == -1 || progressMax == -1) {
             panel.setVisibility(View.GONE);
         } else {
             TextView textView = (TextView) findViewById(textViewId);
             ProgressBar progressBar = (ProgressBar) findViewById(progressBarId);
             panel.setVisibility(View.VISIBLE);
             progressBar.setMax(progressMax);
             progressBar.setProgress(progressValue);
             textView.setText(String.format("%d av %d %s", progressValue, progressMax, unit));
         }
     }
 
     private class UpdateCompleteBroadcastReceiver extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             Log.d("CHESS_SALDO", "Received saldo update broadcast.");
             updateUI();
         }
     }
 }
