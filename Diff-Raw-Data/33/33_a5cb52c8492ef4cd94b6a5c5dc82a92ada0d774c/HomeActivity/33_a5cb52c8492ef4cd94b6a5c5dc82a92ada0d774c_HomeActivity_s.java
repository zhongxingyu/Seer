 package com.feedme.activity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import com.feedme.R;
 
 public class HomeActivity extends Activity {
 
     private static String TAG = "Feed-Me";
 
     /**
      * Called when the activity is first created.
      * @param savedInstanceState If the activity is being re-initialized after
      * previously being shut down then this Bundle contains the data it most 
      * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.i(TAG, "onCreate");
         setContentView(R.layout.main);
 
         // button listener for add child screen button
         Button addChildScreen = (Button)findViewById(R.id.addChildScreen);
         addChildScreen.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 startActivity(new Intent(HomeActivity.this,
                         AddChildActivity.class));
             }
         });
 
         // button listener for add entry screen button
         Button addEntryScreen = (Button)findViewById(R.id.addEntryScreen);
         addEntryScreen.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 startActivity(new Intent(HomeActivity.this,
                         AddEntryActivity.class));
             }
         });
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.family:
                 startActivity(new Intent(HomeActivity.this,
                         FamilyHomeActivity.class));
                 break;
             case R.id.home:
                startActivity(new Intent(HomeActivity.this,
                        HomeActivity.class));
                 break;
         }
         return true;
     }
 
 
     @Override
     public void onBackPressed() {
         new AlertDialog.Builder(this)
                 .setTitle("Exit?")
                 .setMessage("Are you sure you want to exit?")
                 .setNegativeButton(android.R.string.no, null)
                 .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
 
                     public void onClick(DialogInterface arg0, int arg1) {
                         HomeActivity.super.onBackPressed();
                     }
                 }).create().show();
     }
 }
