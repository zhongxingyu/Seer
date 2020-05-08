 package com.c1.charityworkout;
 
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Window;
 
 public class SplashScreen extends Activity {
 
 	String prefLocale;
 	Locale locale;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setLocale();
 		setContentView(R.layout.splash_screen);
 				Thread waiting = new Thread() {
 					public void run() {
 						try {
 							sleep(5000);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						} finally {
 							Intent start = new Intent(SplashScreen.this, MainActivity.class);
 							startActivity(start);
 						}
 					}
 				};
 				waiting.start();		
 	}
 	
 	private void setLocale() {
 		SharedPreferences getLocale = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		Configuration config = getBaseContext().getResources()
 				.getConfiguration();
 		prefLocale = getLocale.getString("prefLang", "en");
 		locale = new Locale(prefLocale);
 		Locale.setDefault(locale);
 		config.locale = locale;
 		getBaseContext().getResources().updateConfiguration(config,
 				getBaseContext().getResources().getDisplayMetrics());
 
 	}
 
 }
