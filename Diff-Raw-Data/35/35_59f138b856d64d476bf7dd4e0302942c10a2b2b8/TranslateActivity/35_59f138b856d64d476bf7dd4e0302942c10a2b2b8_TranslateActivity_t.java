 package ru.ifmo.rain.evdokimov;
 
import java.util.ArrayList;

 import android.app.Activity;
import android.content.Intent;
 import android.os.Bundle;
 import android.view.Window;
import android.widget.TextView;
 
 public class TranslateActivity extends Activity {
 	
	TextView tv;
 	
	private void update() {
		Intent intent = getIntent();
		String translate = intent.getExtras().getString("translate");
		tv.setText(translate);
		ArrayList<String> urls = intent.getExtras().getStringArrayList("picturesUrl");
	}
 	
 	@Override
 	public void onCreate(Bundle state) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(state);
 		setContentView(R.layout.translate);
		tv = (TextView)findViewById(R.id.textViewTranslate);
		update();
 	}
 }
