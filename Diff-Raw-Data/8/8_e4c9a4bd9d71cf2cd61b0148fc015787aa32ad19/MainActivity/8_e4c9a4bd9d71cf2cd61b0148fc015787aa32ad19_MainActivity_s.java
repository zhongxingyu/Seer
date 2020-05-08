 package com.mhaidarhanif.android.kargu;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
  public static final String EXTRA_NUMBER = "com.mhaidarhanif.android.kargu.NUMBER";
   private static Drawable sLogoKargu;
 
   // For complete title purpose. Will be used later
   // String appEditionName = getString(R.string.name_app) + " " + getString(R.string.name_edition);
   TextView mTextView; // Text view in layout
  EditText mNumberInput; // First number input to fill
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     // Avoid leaks by keep potential used image in a static field
     if (sLogoKargu == null) {
       sLogoKargu = this.getResources().getDrawable(R.drawable.ic_launcher);
     }
 
     // Set UI layout for this activity
     // Layout file is defined in res/layout/main_activity.xml
     setContentView(R.layout.activity_main);
 
     // Initialize member TextView
     mTextView = (TextView) findViewById(R.id.about_title);
 
     // Check minimum API level number to use ActionBar API
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
         // Make sure app icon in action bar does not behave as a button
         ActionBar actionBar = getActionBar();
         actionBar.setHomeButtonEnabled(false);
     }
   }
 
   @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
     // Save current input if apply
     savedInstanceState.putInt(EXTRA_NUMBER, mNumberInput);
 
     super.onSaveInstanceState(savedInstanceState);
   }
 
   public void onRestoreInstanceState(Bundle savedInstanceState) {
     super.onRestoreInstanceState(savedInstanceState);
 
     // Restore previous input number if apply
    mNumberInput = savedInstanceState.putInt(EXTRA_NUMBER);
   }
 
   @Override
   public void onDestroy() {
     super.onDestroy();
 
     // Stop method tracing that activity started during onCreate()
     android.os.Debug.stopMethodTracing();
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     // Inflate menu and add item to action bar if present
     getMenuInflater().inflate(R.menu.main, menu);
     return true;
   }
 
   // Called when press calculateNumber
   public void calculateNumber(View view) {
     Intent intent = new Intent(this, DisplayNumberActivity.class);
     EditText editText = (EditText) findViewById(R.id.edit_inputNumber);
     String number = editText.getText().toString();
     intent.putExtra(EXTRA_NUMBER, number);
     startActivity(intent);
   }
 
   public void openDialog(View v) {
     Intent intent = new Intent(MainActivity.this, AboutDialogActivity.class);
     startActivity(intent);
   }
 
 }
