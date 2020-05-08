 package ru.ifmo.rain.evdokimov;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Window;
 
 public class TranslateActivity extends Activity {
 	
 	
 	
 	@Override
 	public void onCreate(Bundle state) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(state);
 		setContentView(R.layout.translate);
 	}
 }
