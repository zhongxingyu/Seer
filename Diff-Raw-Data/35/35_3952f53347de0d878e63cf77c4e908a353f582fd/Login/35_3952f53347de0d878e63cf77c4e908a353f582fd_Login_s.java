 package at.roadrunner.android.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import at.roadrunner.android.R;
 
 public class Login extends Activity {
 	@SuppressWarnings("unused")
 	private static final String TAG = "Login";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		
 		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		String[] containers = new String[] {"LKW1", "LKW2", "LKW3", "PKW1"};
 		ArrayAdapter<String> containerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, containers);
 		containerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		
 		final Spinner cbxTransportation = (Spinner) findViewById(R.id.login_container);
 		cbxTransportation.setAdapter(containerAdapter);
 		cbxTransportation.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				prefs.edit().putString("transportation", cbxTransportation.getSelectedItem().toString()).commit();
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
				
 			}
 		});
 	}
 	
 	public void onLoginClick(View view) {
 		EditText txtUsername = (EditText) findViewById(R.id.login_username);
 		EditText txtPassword = (EditText) findViewById(R.id.login_password);
 		
 		// TODO: Authenticate through server -> Wolfy FIX THIS MAN!
 		
 		startActivity(new Intent(this, Roadrunner.class));
 		
 	}
 }
