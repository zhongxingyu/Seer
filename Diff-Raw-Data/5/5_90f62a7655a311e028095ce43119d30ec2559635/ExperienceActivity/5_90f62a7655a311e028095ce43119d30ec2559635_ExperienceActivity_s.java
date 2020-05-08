 package com.ad.cow;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class ExperienceActivity extends AbstractActivity {
 	/**
 	 * Необходимые переменные
 	 */
 	private final String MY_PREFS = "MY_PREFS";
 	private SharedPreferences mySharedPreferences;
 	
 	private final float expPerSecond = 0.002777778f;
 	
 	private float exp;
 	private long time;
 	private float newExp;
 	/**
 	 * Старт активности
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.experience);
 
 		loadPreferences();
 	}
 
 	private void loadPreferences() {
 		int mode = Activity.MODE_MULTI_PROCESS;
 		long currentTime = new Date().getTime();
 
 		// Достаем сохраненные данные
 		mySharedPreferences = getSharedPreferences(MY_PREFS,mode);
 		exp  = mySharedPreferences.getFloat("exp", 0.0f);
		time = mySharedPreferences.getLong("time", currentTime);
 		
 		long diff = currentTime - time; 
 		float seconds = diff / 1000;
 		float addExp = seconds * expPerSecond;
 		newExp = exp + addExp; 
 
 		TextView textView = (TextView) findViewById(R.id.textView1);
 		textView.setText("У вас " + newExp + " опыта");
 		
 		TextView textView2 = (TextView) findViewById(R.id.textView2);
 		textView2.setText("Вам добавилось " + addExp+ " опыта");
 		
 		TextView textView3 = (TextView) findViewById(R.id.textView3);
 		textView3.setText("У вас было " + exp + " опыта");
 		
 		int percent = 60; 
 		ProgressBar progressView = (ProgressBar) findViewById(R.id.progressBar1);
 		progressView.setProgress((int)percent);
 	}
 
 	/**
 	 * При завершении экшена сохраняем данные
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 
 		SharedPreferences.Editor editor = mySharedPreferences.edit();
 		editor.putFloat("exp", newExp);
		editor.putLong("time", new Date().getTime());
 		editor.commit();
 	}
 }
