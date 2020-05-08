 package com.vorsk.crossfitr;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class StopwatchActivity extends Activity {
 	private static String TAG = "StopwatchActivity";
 	// View elements in stopwatch.xml
 	private TextView m_elapsedTime;
 	private Button m_start;
 	private Button m_pause;
 	private Button m_reset;
 	private Stopwatch m_stopwatch = new Stopwatch();
 
 	
 	// Timer to update the elapsedTime display
     private final long mFrequency = 100;    // milliseconds
     private final int TICK_WHAT = 2; 
 	private Handler mHandler = new Handler() {
         public void handleMessage(Message m) {
         	updateElapsedTime();
         	sendMessageDelayed(Message.obtain(this, TICK_WHAT), mFrequency);
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.stopwatch);
 
 
         //startService(new Intent(this, StopwatchService.class));
         //bindStopwatchService();
         
         m_elapsedTime = (TextView)findViewById(R.id.ElapsedTime);
         
         m_start = (Button)findViewById(R.id.StartButton);
         m_pause = (Button)findViewById(R.id.PauseButton);
         m_reset = (Button)findViewById(R.id.ResetButton);
         
         mHandler.sendMessageDelayed(Message.obtain(mHandler, TICK_WHAT), mFrequency);
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
     }
    
     private void showPauseButton() {
     	Log.d(TAG, "showPauseLapButtons");
     	
     	m_start.setVisibility(View.GONE);
     	m_reset.setVisibility(View.GONE);
     	m_pause.setVisibility(View.VISIBLE);
     }
     
     private void showStartResetButtons() {
     	Log.d(TAG, "showStartResetButtons");
 
     	m_start.setVisibility(View.VISIBLE);
     	m_reset.setVisibility(View.VISIBLE);
     	m_pause.setVisibility(View.GONE);
     }
     
     public void onStartClicked(View v) {
     	Log.d(TAG, "start button clicked");
     	start();
     	
     	showPauseButton();
     }
     
     public void onPauseClicked(View v) {
     	Log.d(TAG, "pause button clicked");
     	pause();
     	
     	showStartResetButtons();
     }
     
     public void onResetClicked(View v) {
     	Log.d(TAG, "reset button clicked");
     	reset();
     }
     
     public void updateElapsedTime() {
    		m_elapsedTime.setText(getFormattedElapsedTime());
     }
     
 	private String formatElapsedTime(long now) {
 		long hours=0, minutes=0, seconds=0, tenths=0;
 		StringBuilder sb = new StringBuilder();
 
 		if (now < 1000) {
			tenths = now / 100;
 		} else if (now < 60000) {
 			seconds = now / 1000;
 			now -= seconds * 1000;
			tenths = now / 100;
 		} else if (now < 3600000) {
 			hours = now / 3600000;
 			now -= hours * 3600000;
 			minutes = now / 60000;
 			now -= minutes * 60000;
 			seconds = now / 1000;
 			now -= seconds * 1000;
			tenths = now / 100;
 		}
 		
 		if (hours > 0) {
 			sb.append(hours).append(":")
 				.append(formatDigits(minutes)).append(":")
 				.append(formatDigits(seconds)).append(".")
 				.append(tenths);
 		} else {
 			sb.append(formatDigits(minutes)).append(":")
 			.append(formatDigits(seconds)).append(".")
 			.append(tenths);
 		}
 		
 		return sb.toString();
 	}
 		
 	private String formatDigits(long num) {
 		return (num < 10) ? "0" + num : new Long(num).toString();
 	}
 	
 	public String getFormattedElapsedTime() {
 		return formatElapsedTime(getElapsedTime());
 	}
 	
 	public long getElapsedTime() {
 		if(m_stopwatch.isRunning() == true){
 			return m_stopwatch.getElapsedTime();
 		}else{
 			return 0;
 		}
 		
 	}
 	
 	public void start() {
 		Log.d(TAG, "start");
 		m_stopwatch.start();
 	}
 
 	public void pause() {
 		Log.d(TAG, "pause");
 		m_stopwatch.pause();
 	}
 
 
 	public void reset() {
 		Log.d(TAG, "reset");
 		m_stopwatch.reset();
 	}
     
 }
