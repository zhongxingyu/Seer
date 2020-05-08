 package com.goodhearted.smokebegone;
 
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class WelcomeActivity extends Activity implements OnClickListener {
 
 	Button save;
 	EditText numPack, pricePack, numCigPerDay;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		save = (Button) findViewById(R.id.btWSave);
 		save.setOnClickListener(this);
 
 		numPack = (EditText) findViewById(R.id.etWNumPerPack);
 		pricePack = (EditText) findViewById(R.id.etWPrice);
 		numCigPerDay = (EditText) findViewById(R.id.etWSPD);
 	}
 
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.btWSave:
 			if (saveOptions()) {
 				Intent i = new Intent(this, MainActivity.class);
 				this.startActivity(i);
 			}
 			break;
 		}
 	}
 
 	private boolean saveOptions() {
 		PreferenceProvider.writeInteger(this, PreferenceProvider.keyCPP, Integer.parseInt(numPack.getText().toString()));
 		PreferenceProvider.writeFloat(this, PreferenceProvider.keyPPP, Float.parseFloat(pricePack.getText().toString()));
 		PreferenceProvider.writeInteger(this, PreferenceProvider.keyCPD, Integer.parseInt(numCigPerDay.getText().toString()));
 		PreferenceProvider.writeLong(this, PreferenceProvider.keyQD, (new Date()).getTime());
 		return true;
 	}
 
 	@Override
 	protected void onPause() {
 		//When paused kill with fire!
 		super.onPause();
 		finish();
 	}
 	
 	
 }
