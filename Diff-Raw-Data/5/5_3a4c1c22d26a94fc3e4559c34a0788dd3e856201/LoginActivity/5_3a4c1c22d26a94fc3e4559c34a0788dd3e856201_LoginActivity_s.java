 package com.cloudappstudio.activities;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.cloudappstudio.android.R;
 import com.cloudappstudio.utility.CloudViewEntriesParser;
 
 /**
  * An activity that lets the user log in to their google account
  * @author mrjanek <Jesper Lindberg>
  */
 public class LoginActivity extends SherlockActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(com.cloudappstudio.android.R.layout.login_view);
 		Button logInButton = (Button) findViewById(R.id.logIn_button);
 		
 		logInButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
				//Intent intent = new Intent(getApplicationContext(), WebApplicationsActivity.class);
				//startActivity(intent);
				CloudViewEntriesParser parser = new CloudViewEntriesParser();
 			}
 		});
 	}
 
 }
