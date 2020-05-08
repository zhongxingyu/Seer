 package com.example.helloandroid;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 public class HelloAndroid extends Activity implements SeekBar.OnSeekBarChangeListener {
    private LinearLayout linearLayout;
    private SeekBar redSeekBar;
    private SeekBar greenSeekBar;
    private SeekBar blueSeekBar;
    private TextView textView;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         this.linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
 
         this.redSeekBar = (SeekBar) findViewById(R.id.redSeekBar);
         this.redSeekBar.setOnSeekBarChangeListener(this);
 
         this.greenSeekBar = (SeekBar) findViewById(R.id.greenSeekBar);
         this.greenSeekBar.setOnSeekBarChangeListener(this);
 
         this.blueSeekBar = (SeekBar) findViewById(R.id.blueSeekBar);
         this.blueSeekBar.setOnSeekBarChangeListener(this);
 
         this.textView = (TextView) findViewById(R.id.textView);
     }
 
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUse) {
         int red = this.redSeekBar.getProgress();
         int green = this.greenSeekBar.getProgress();
         int blue = this.blueSeekBar.getProgress();
         this.linearLayout.setBackgroundColor(Color.rgb(red, green, blue));
         this.textView.setText("(" + red + ", " + green + ", " + blue + ")");
    }
 
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
 
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
 }
