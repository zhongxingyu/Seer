 package com.example.projectv1;
 
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class PasswordPrompt extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_password_prompt);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_password_prompt, menu);
 		return true;
 	}
 	public void buttonClick(View v)
 	{
 		switch(v.getId())
 		{
 		case R.id.sumitPass:
 			
 			TextView pass = (TextView) findViewById(R.id.ETpassword);
 			long password = pass.getText().toString().hashCode();
 			
 //			Intent passPrompt = new Intent("android.intent.action.PREFERENCES");
 //			passPrompt.putExtra("submitted_pass", password);
 //			startActivity(passPrompt);
 
 			Log.v("PasswordPrompt","before startActivity");
 				
 			  Intent i = new Intent(PasswordPrompt.this, Preferences.class);
 	          i.putExtra("submitted_pass", password);
 			  startActivity(i);	     
 		      Log.v("PasswordPrompt","after startActivity");
 			
 			break;
 		}
 	}
 }
