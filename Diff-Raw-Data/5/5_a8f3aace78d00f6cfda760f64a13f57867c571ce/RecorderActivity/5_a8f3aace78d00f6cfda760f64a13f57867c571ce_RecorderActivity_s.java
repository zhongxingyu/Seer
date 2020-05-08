 package com.snitchmedia;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TableLayout;
 import android.widget.ToggleButton;
 
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class RecorderActivity extends Activity {
     private String TAG = "Looper#RecorderActivity";
     private int recordCount = 0;
     private String[] fileNames = {"first_file", "second_file", "third_file", "forth_file"};
     private AudioWrapper[] audioDevices = new AudioWrapper[4];
 
     private boolean startRecording() {
        if(recordCount >= 4) {
            Log.v(TAG, "start recording" + fileNames[recordCount]);
             return false;
         }
         audioDevices[recordCount] = new AudioWrapper(fileNames[recordCount]);
         try {
             audioDevices[recordCount].record();
             return true;
         } catch (IOException e) {
             return false;
         }
     }
 
     private void stopRecording() {
         try {
              Log.v(TAG, "stop recording" + fileNames[recordCount]);
              Log.v(TAG, "stop recording" + recordCount);
              audioDevices[recordCount].stop();
              recordCount++;
         } catch (IOException e) {
 
         }
     }
 
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setContentView(R.layout.studio);
 
         View.OnClickListener recClicker;
         recClicker = new View.OnClickListener() {
             public void onClick(View v) {
                 ToggleButton btn = (ToggleButton)v;
                 if (btn.isChecked()) {
                     Log.v(TAG, "start recording fork");
                     if(!startRecording()) {
                         btn.setChecked(false);
                     }
                 } else {
                     Log.v(TAG, "stop recording fork");
                     addRow(recordCount);
                     stopRecording();
                 }
             }
         };
 
         ToggleButton recordBtn = (ToggleButton)findViewById(R.id.record_btn);
         recordBtn.setOnClickListener(recClicker);
     }
 
     private void addRow(int track) {
         LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         TableLayout container = (TableLayout)findViewById(R.id.container);
         inflater.inflate(R.layout.play_row, container, true);
         ToggleButton playBtn = (ToggleButton)container.findViewWithTag("unused");
         playBtn.setTag(track);
         final ProgressBar seekBar = (ProgressBar)container.findViewWithTag("unused progressbar");
         seekBar.setTag(track);
         playBtn.setOnClickListener(
                 new View.OnClickListener() {
                     public void onClick(View v) {
                         ToggleButton btn = (ToggleButton) v;
                         final int track = ((Number) btn.getTag()).intValue();
                         if (btn.isChecked()) {
                             try {
                                 audioDevices[track].play();
                                 Timer t = new Timer();
                                 TimerTask tt = new TimerTask() {
                                     @Override
                                     public void run() {
                                         float duration = (float)audioDevices[track].getDuration();
                                         float position = (float)audioDevices[track].getCurrentPosition();
                                         int percent = Math.round(position/duration * 100);
                                         Log.v(TAG, "Tick Tack" + percent);
                                         seekBar.setProgress(percent);
                                     }
                                 };
                                 t.schedule(tt, 100, 100);
                             } catch (IOException e) {
                             }
                         } else {
                             audioDevices[track].pause();
                         }
                     }
                 }
         );
     }
 
     @Override
     public void onPause() {
         super.onPause();
         if (audioDevices != null) {
             audioDevices = null;
         }
     }
 }
