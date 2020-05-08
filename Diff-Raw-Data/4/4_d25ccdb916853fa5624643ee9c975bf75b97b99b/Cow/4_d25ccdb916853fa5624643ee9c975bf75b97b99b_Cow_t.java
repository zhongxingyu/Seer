 package com.ad.cow;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class Cow extends Activity {
 	private ProgressBar mProgress;
 	private SharedPreferences mySharedPreferences;
 	private static String MY_PREFS = "MY_PREFS";
 	
 	private int percent;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         loadPreferences();
         
         mProgress = (ProgressBar) findViewById(R.id.progressBar1);
         mProgress.setProgress(percent);
     }
     
     private void loadPreferences() {
     	int mode = Activity.MODE_PRIVATE;
     	mySharedPreferences = getSharedPreferences(MY_PREFS,mode);
     	percent = mySharedPreferences.getInt("percent",0);
     }
     
     public void feed(View view) {
     	int newPercent = mProgress.getProgress()+10;
     	
     	SharedPreferences.Editor editor = mySharedPreferences.edit();
     	editor.putInt("percent",newPercent);
     	editor.commit();
     	
     	mProgress.setProgress(newPercent);
     	if(newPercent > 50){
     		Toast.makeText(this, "Ваша корова сыта. Приходите когда она проголодается!",
                     Toast.LENGTH_LONG).show();
     		return;
     	}
     }
 }
