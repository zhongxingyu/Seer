 package com.pockwester.forge;
 
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
     private ProgressDialog progressDialog;
 
     private BroadcastReceiver receiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             Bundle bundle = intent.getExtras();
             if (bundle != null) {
                 int resultCode = bundle.getInt(DBSyncService.RESULT);
                 if (resultCode == RESULT_OK) {
                     progressDialog.dismiss();
                 }
             }
         }
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // updates database
 		startService(new Intent(this, DBSyncService.class));
         // creates loading spinner
         progressDialog = new ProgressDialog(this);
         progressDialog.setMessage("Syncing database");
         progressDialog.show();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         registerReceiver(receiver, new IntentFilter(DBSyncService.NOTIFICATION));
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         unregisterReceiver(receiver);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     public void openClassIndex(View v) {
         Intent intent = new Intent(this, CourseIndexActivity.class);
         startActivity(intent);
     }
 
    public void openAvailability(View v) {
         Intent intent = new Intent(this, AvailabilityActivity.class);
         startActivity(intent);
    }
     
 }
