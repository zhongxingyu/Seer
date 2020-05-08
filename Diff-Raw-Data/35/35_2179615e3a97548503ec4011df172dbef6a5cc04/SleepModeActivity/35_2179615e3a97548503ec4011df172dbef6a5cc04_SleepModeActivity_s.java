 package com.example.smartalarm;
 
 import java.io.IOException;
 
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 
 public class SleepModeActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);		
 		setContentView(R.layout.activity_sleep_mode);
 		
 		Intent intent = getIntent();
 		
 		//The start time and end time for the alarm window are passed into the activity on its creation
 		int windowStartHours 	= intent.getIntExtra(ConfirmSleepAlarmActivity.START_ALARM_HOURS, 6);
 		int windowStartMinutes 	= intent.getIntExtra(ConfirmSleepAlarmActivity.START_ALARM_MINUTES, 30);
 		int windowEndHours 		= intent.getIntExtra(ConfirmSleepAlarmActivity.FAILSAFE_ALARM_HOURS, 7);
 		int windowEndMinutes 	= intent.getIntExtra(ConfirmSleepAlarmActivity.FAILSAFE_ALARM_HOURS, 30);
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.sleep_mode, menu);
 		return true;
 	}
 	
 	public void awakeButtonClicked(View view) {
 		
 	}
 	public void testAlarmClicked(View view) {
 		
 		
		MediaPlayer mp = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
		if(mp == null)
 		{
			throw new NullPointerException(Settings.System.DEFAULT_ALARM_ALERT_URI.toString());
 		}
		mp.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }

        });   
        mp.start();
 	}
 
 }
