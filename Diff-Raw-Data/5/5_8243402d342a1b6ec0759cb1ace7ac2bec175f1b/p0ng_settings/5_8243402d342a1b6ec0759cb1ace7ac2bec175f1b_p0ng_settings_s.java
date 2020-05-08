 package com.rampantmonk3y.p0ng;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 public class p0ng_settings extends Activity{
 	public static final String PREFERENCES = "p0ngPrefs";
 	public String userName;
 	public Integer ballCount;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.settings);
         
         restorePreferences();
         initControls();
     }
     
     private void initControls(){
     	SeekBar numBalls = (SeekBar)findViewById(R.id.seekBar1);
     	final TextView numBallsValue = (TextView)findViewById(R.id.ballCountDisplay);
     	numBalls.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
     		@Override
     		public void onProgressChanged(SeekBar _seekBar, int progress, boolean fromUser){
     			ballCount = progress+1;
     			numBallsValue.setText(String.valueOf(ballCount));
     		}
     		
     		@Override
     		public void onStartTrackingTouch(SeekBar _seekBar){
     		}
     		
     		@Override
     		public void onStopTrackingTouch(SeekBar _seekBar){
     		}
     	});
     	
     	Button saveSettings = (Button) findViewById(R.id.saveSettingsButton);
     	saveSettings.setOnClickListener(new View.OnClickListener(){
     		public void onClick(View view){
     			savePreferences();
     		}
     	});
     }
     
     private void restorePreferences(){
     	SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
     	userName = settings.getString("user", "");
     	ballCount = settings.getInt("balls", 1);
     }
     
     private void savePreferences(){
     	TextView name = (TextView) findViewById(R.id.userName);
     	userName = name.getText().toString();
     	
     	SharedPreferences settings = getSharedPreferences(PREFERENCES, 0);
     	SharedPreferences.Editor editor = settings.edit();
     	editor.putString("user", userName);
     	editor.putInt("balls", ballCount);
     	
     	editor.commit();
     }
 }
