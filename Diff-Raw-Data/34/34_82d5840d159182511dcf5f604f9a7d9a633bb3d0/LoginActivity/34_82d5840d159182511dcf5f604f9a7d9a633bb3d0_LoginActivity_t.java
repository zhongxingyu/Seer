 package ca.ryerson.scs.rus;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.EditText;
import android.widget.TextView;
 
 public class LoginActivity extends Activity implements OnClickListener {
 	// Flag for debug/log messages
 	public static final boolean DEBUG = true;
 
 	// Tag for debugging
 	public static final String TAG = "RyeSocial";
 
 	private final String MENU_STRING = "ca.ryerson.scs.rus.MENU";
 	private final String REGISTER_STRING = "ca.ryerson.scs.rus.REGISTER";
 
	private TextView btnSubmit, btnRegister;
 
 	private EditText evUsername, evPassword;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.login);
 
		btnSubmit = (TextView) findViewById(R.id.BtnSubmit);
		btnRegister = (TextView) findViewById(R.id.BtnRegister);
 		evUsername = (EditText) findViewById(R.id.EVUsername);
 		evPassword = (EditText) findViewById(R.id.EVPassword);
 
 		btnSubmit.setFocusable(true);
 		btnRegister.setFocusable(true);
 		evUsername.setFocusable(true);
 		evPassword.setFocusable(true);
 
 		btnSubmit.setOnClickListener(this);
 		btnRegister.setOnClickListener(this);
 
 		// TODO: Implement persistence checking for saved username and password
 	}
 
 	public void onClick(View v) {
 		if (v == btnSubmit) {
 			if (SplashActivity.DEBUG) {
 				Log.i(TAG, "Login Button");
 				Log.i(TAG, evUsername.getText() + " ");
 				Log.i(TAG, evPassword.getText() + " ");
 			}
 
 			// TODO: Create URL using inputs and make an HTTTP Request for
 			// authentication
 			startActivity(new Intent(MENU_STRING));
 		} else if (v == btnRegister) {
 			startActivity(new Intent(REGISTER_STRING));
 		}
 
 	}
 }
