 package com.kinnack.nthings.activity;
 
 import com.kinnack.nthings.R;
 import com.kinnack.nthings.StopWatch;
 import com.kinnack.nthings.helper.RangedIntTextWatcher;
 
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class ManualEntryCounterActivity extends CounterActivity {
     private RangedIntTextWatcher rangedIntTextWatcher;
     @Override
     public void onCreate(Bundle savedInstanceState_) {
         super.onCreate(savedInstanceState_);
         stopWatch = new StopWatch();
         rangedIntTextWatcher = new RangedIntTextWatcher(0, 300);
         EditText manualEntry = (EditText)findViewById(R.id.manualEntry);
         manualEntry.addTextChangedListener(rangedIntTextWatcher);
         manualEntry.setOnEditorActionListener(new OnEditorActionListener() {
             @Override
             public boolean onEditorAction(TextView v_, int actionId_, KeyEvent event_) {
                 View endButton = findViewById(R.id.Done);
                 if (rangedIntTextWatcher.isValid()) {
                     endButton.setEnabled(true);                    
                 } else {
                     endButton.setEnabled(false);  
                 }
                 return false;
             }
         });
     }
     
     @Override
     protected void additionalViewConfiguration(Bundle extras_) {
     }
     
     public void startTimer(View target_) {
         findViewById(R.id.startBtn).setEnabled(false);
         stopWatch.start();
         findViewById(R.id.endBtn).setEnabled(true);
         
     }
     
     public void endTimer(View target_) {
         stopWatch.stop();
         // DONE button only enabled after validation
         findViewById(R.id.endBtn).setEnabled(false);
         findViewById(R.id.manualEntry).setEnabled(true);
         sumTimeBetweenCounts = stopWatch.getElapsedTime();
         if (increment == -1) {
             setResult(RESULT_OK,createIntentWithStats(neededCount));
             finish();
         }
     }
     
     public void done(View target_) {
         EditText manualEntry = (EditText) findViewById(R.id.manualEntry);
         int reps = Integer.parseInt(manualEntry.getText().toString());
         setResult(RESULT_OK, createIntentWithStats(reps));
         finish();
     }
     
     protected int getLayout() {
         return R.layout.manual_entry_with_timer;
     }
     
     protected void showQuitingOptions() {
        findViewById(R.id.Done).setVisibility(View.VISIBLE);
         findViewById(R.id.manualEntry).setVisibility(View.VISIBLE);
        
     }
 }
