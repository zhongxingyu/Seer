 package info.vanderkooy.ucheck;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class Login extends Activity {
 
 	private APIHandler handler;
 	private Preferences prefs;
 	private EditText username;
 	private EditText password;
 	private CheckBox storePass;
 	private String usr;
 	private Button infoButton;
 	private Button loginButton;
//	private Button newData;
 	
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.account);
 
 		handler = new APIHandler(getApplicationContext());
 		prefs = new Preferences(getApplicationContext());
 		
 		infoButton = (Button) findViewById(R.id.info);
 		loginButton = (Button) findViewById(R.id.login);
 		username = (EditText) findViewById(R.id.username);
 		password = (EditText) findViewById(R.id.password);
 		storePass = (CheckBox) findViewById(R.id.remember);
 		
//		newData.setVisibility(8);
 		
 		usr = prefs.getUsername();
 		if(usr == null)
 			username.setHint(getString(R.string.studentNumberHint));
 		else
 			username.setText(usr);
 		password.setHint(getString(R.string.passHint));
 		password.setTextSize(13);
 		storePass.setChecked(prefs.getStorePass());
 		
 		loginButton.setText(getString(R.string.logout));
 		loginButton.setOnClickListener(loginListener);
 		infoButton.setOnClickListener(infoButtonListener);
 	}
 	
 	private OnClickListener loginListener = new OnClickListener() {
 		public void onClick(View v) {
 			String usernameString = username.getText().toString();
 			if(usernameString.length() >= 1 && !usernameString.substring(0, 1).equals("s"))
 				usernameString = "s" + usernameString;
 			int returned = 0;
 			boolean success = (usernameString.length() < 7 || usernameString.length() > 9) ? false : ((returned = handler.getKey(usernameString, password.getText().toString())) == 1 ? true : false);
 			if(success) {
 				prefs.setStorePass(storePass.isChecked());
 				if(!usr.equals(usernameString))
 					prefs.forceNewData();
 				finish();
 			} else {
 				Toast toast;
 				if(returned == 0) {
 					toast = Toast.makeText(getApplicationContext(), getString(R.string.userError), 3);
 				} else {
 					toast = Toast.makeText(getApplicationContext(), getString(R.string.verificationError), 10);
 				}
 				toast.show();
 			}
 		}
 	};
 	
 	private OnClickListener infoButtonListener = new OnClickListener() {
 		public void onClick(View v) {
 			prefs.setGoingToInfo(true);
 			Intent infoIntent = new Intent().setClass(Login.this,
 					Info.class);
 			Login.this.startActivity(infoIntent);
 		}
 	};
 
 }
