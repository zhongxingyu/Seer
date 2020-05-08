 package ca.ryerson.scs.rus;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import ca.ryerson.scs.rus.util.HttpRequestAdapter;
 import ca.ryerson.scs.rus.util.URLResource;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LoginActivity extends Activity implements OnClickListener {
 	// Flag for debug/log messages
 	public static final boolean DEBUG = true;
 
 	// Tag for debugging
 	public static final String TAG = "RyeSocial";
 
 	private Context context;
 	
 	private final String MENU_STRING = "ca.ryerson.scs.rus.MENU";
 	private final String REGISTER_STRING = "ca.ryerson.scs.rus.REGISTER";
 
 	private TextView btnSubmit, btnRegister;
 	private EditText evUsername, evPassword;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.login);
 		
 		context=this;
 
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
 			JSONObject json = new JSONObject();
 			try {
 				json.put("username", evUsername.getText().toString());
 				json.put("password", evPassword.getText().toString());
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			HttpRequestAdapter.httpRequest(this, URLResource.LOGIN,json,new LoginHandler());
 			
 			// TODO: Create URL using inputs and make an HTTTP Request for
 			// authentication
			startActivity(new Intent(MENU_STRING));
 		} else if (v == btnRegister) {
 			startActivity(new Intent(REGISTER_STRING));
 		}
 
 	}
 	
 	private class LoginHandler implements HttpRequestAdapter.ResponseHandler {
 		@Override
 		public void postResponse(JSONObject response) {
			
 		}
 
 		@Override
 		public void postTimeout() {
 			Toast.makeText(context, "Connection timed out", Toast.LENGTH_LONG).show();
 		}
 	}
 
 }
