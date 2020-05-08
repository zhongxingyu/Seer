 package com.example.web_app;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class SendMessageActivity extends Activity implements RequestHandler {
 	
 	public static final String PREFS_NAME = "UserPrefs";
 	private static final String PREFF_CURR_USER = "currUser";
 	SharedPreferences pref;
 	String currUser;
 	Spinner sendSpinner;
 	EditText messageField;
	int reqID = 0;
 	String[] friendIDs;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_send_message);
 		
 		pref = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
 		currUser = pref.getString(PREFF_CURR_USER, null);
 		
 		sendSpinner = (Spinner) findViewById(R.id.sendSpinner);
 		messageField = (EditText) findViewById(R.id.messageText);
 		
 		ServerRequest servReq = new ServerRequest(this);
 		reqID = 0;
 		servReq.fetchIDs(currUser);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.send_message, menu);
 		return true;
 	}
 	
 	public void sendMessage(View view) {
 		String message = messageField.getText().toString();
 		ServerRequest servReq = new ServerRequest(this);
 		servReq.sendMessage(currUser, "88888888"/*sendSpinner.getSelectedItem().toString()*/, message);
 	}
 
 	@Override
 	public void doOnRequestComplete(String s) {
 		if(reqID == 0 ) {
 			friendIDs = s.split(":");
 			
 			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, friendIDs);
 			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			sendSpinner.setAdapter(adapter);
 			
			
 			
 		} else if(reqID == 1) {
 			int duration = Toast.LENGTH_SHORT;
 			String result;
 			Class<?> next;
 			if(s.equals("OK")) {
 				result = "Message Sent";
 				next = (MessagingActivity.class);
 				Toast toast = Toast.makeText(this, result, duration);
 				toast.show();
 				Intent intent = new Intent(this, next);
 				startActivity(intent);
 			} else {
 				result = s;
 				Toast toast = Toast.makeText(this, result, duration);
 				toast.show();
 			}
 		}
 		
 		
 		
 		
 	}
 
 }
