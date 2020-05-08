 package com.goodhearted.smokebegone;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class SettingsActivity extends Activity {
 
 	MenuHandler handler;
 	EditText CPP, CPD, PPP;
 	Button save;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_settings);
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		new MenuHandler(this);
 
 		CPP = (EditText) findViewById(R.id.etWNumPerPack);
 		CPD = (EditText) findViewById(R.id.etWSPD);
		PPP = (EditText) findViewById(R.id.etWPrice);
 
 		CPP.setText(""
 				+ (PreferenceProvider.readInteger(this,
 						PreferenceProvider.keyCPP, -1)));
 		CPD.setText(""
 				+ (PreferenceProvider.readInteger(this,
 						PreferenceProvider.keyCPD, -1)));
 		PPP.setText(""
 				+ (PreferenceProvider.readFloat(this,
 						PreferenceProvider.keyPPP, -1.0f)));
 
 		save = (Button) findViewById(R.id.btWSave);
 		save.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				saveSettings();
 
 			}
 
 		});
 	}
 
 	private void saveSettings() {
 		PreferenceProvider.writeInteger(this, PreferenceProvider.keyCPP,
 				Integer.parseInt(CPP.getText().toString()));
 		PreferenceProvider.writeFloat(this, PreferenceProvider.keyPPP,
 				Float.parseFloat(PPP.getText().toString()));
 		PreferenceProvider.writeInteger(this, PreferenceProvider.keyCPD,
 				Integer.parseInt(CPD.getText().toString()));
 
 		// display alert!
 
 		Toast toast = Toast.makeText(this, "De instellingen zijn opgeslagen!", Toast.LENGTH_LONG);
 		toast.show();
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		SlideHolder x = (SlideHolder) findViewById(R.id.bla);
 		x.toggle();
 		return true;
 
 	}
 	
 	/**
 	 * Closes menu (if open) on back button click
 	 */
 	@Override
 	public void onBackPressed() {
 		SlideHolder slideholder = (SlideHolder) findViewById(R.id.bla);
 		if (slideholder.isOpened())
 			slideholder.close();
 		else
 			super.onBackPressed();
 	}
 }
