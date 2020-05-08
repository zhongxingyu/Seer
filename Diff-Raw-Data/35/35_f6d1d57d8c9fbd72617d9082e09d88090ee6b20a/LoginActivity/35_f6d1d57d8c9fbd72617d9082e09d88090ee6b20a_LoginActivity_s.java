 package com.reddit.worddit;
 
 import java.net.MalformedURLException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.CheckBox;
 import android.widget.EditText;
 
 import com.reddit.worddit.api.APICall;
 import com.reddit.worddit.api.APICallback;
 import com.reddit.worddit.api.Session;
 
 
 public class LoginActivity extends Activity implements APICallback {
 	/** Debug tag */
 	public static final String TAG = "WordditHome";
 	
 	/** Convenient matter to change the default URL to use */
 	private String URL = Session.API_URL;
 	
 	/** Session object to use */
 	private Session mSession;
 	
 	/** Could we get a handle on the Window's progress bar */
 	private boolean mWindowIndeterminate = false;
 	
 	/** Called when the activity is first created. */
 	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 		
 		mWindowIndeterminate = requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.activity_login);
 		setup();
 	}
 	
 	/** Called when we create a dialog using showDialog(int) */
 	public Dialog onCreateDialog(int id) {
 		Resources r = getResources();
 		
 		// Catch if we're making a progress dialog
 		// TODO: This could cause a problem if we use showDialog for String resource '1'
 		if (id == DIALOG_WAIT) {
 			ProgressDialog dlg = new ProgressDialog(this);
 			dlg.setTitle(R.string.app_name);
 			dlg.setMessage(r.getString(R.string.msg_communicating));
 			return dlg;
 		}
 
 		// Revert to the default behavior, which is treat 'id' as a String resource
 		// and show a dialog displaying the corresponding message.
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		String msg = "(null)";
 		
 		try { msg = r.getString(id); }
 		catch(Exception e) { msg = String.format(r.getString(R.string.msg_not_found), id); }
 		
 		return builder.setMessage(msg)
 			.setTitle(R.string.app_name)
 			.setCancelable(true)
 			.setNeutralButton(R.string.label_ok, null)
 			.create();
 	}
 	
	/*
 	protected void onSaveInstanceState(Bundle icicle) {
 		icicle.putInt(CONFIRM_STATE,
 				findViewById(R.id.login_input_confirmpassword).getVisibility());
 	}
 	
	protected void onRestoreInstanceState(Bundle icicle) {
		int foo = icicle.getInt(CONFIRM_STATE);
		View v = findViewById(R.id.login_input_confirmpassword);
		
		if(v != null) {
			v.setVisibility(foo);
		}
	}*/
 	
 	private void setup() {
 		// Force confirm password field to reflect default state of checkbox
 		doNewChecked(null);
 		
 		// Setup listeners
 		setupListeners();
 		
 		// Load up a session object
 		setupSession();
 	}
 	
 	/** Attempts to load the session */
 	private void setupSession() {
 		try {
 			mSession = Session.makeSession(URL);
 		} catch (MalformedURLException e) {
 			showDialog(R.string.msg_setup_error);
 		}
 	}
 	
 	private void setupListeners() {
 		findViewById(R.id.login_button_login)
 			.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				doButtonClick(v);
 			}
 		});
 		
 		findViewById(R.id.login_check_new)
 			.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				doNewChecked(v);
 			}
 		});
 	}
 	
 	/** This checks the state of the check box and displays the "confirm" field if needed. */
 	public void doNewChecked(View v) {
 		CheckBox check = (CheckBox) findViewById(R.id.login_check_new);
 		EditText confirm = (EditText) findViewById(R.id.login_input_confirmpassword);
 		
 		if(check.isChecked()) {
 			confirm.setVisibility(View.VISIBLE);
 		} else {
 			confirm.setVisibility(View.GONE);
 		}
 	}
 	
 	/** This method is called if the "Login" button is clicked */
 	public void doButtonClick(View v) {
 		CheckBox check = (CheckBox) findViewById(R.id.login_check_new);
 		if(check.isChecked()) {
 			doCreate(v);
 		}
 		else {
 			doLogin(v);
 		}
 	}
 	
 	/** Helper method to easily pull the email and password from the TextViews */
 	public void doLogin(View v) {
 		doLogin(getEmailField(), getPasswordField());
 	}
 	
 	/** Helper method to get email, password, and confirm fields from TextViews */
 	public void doCreate(View v) {
 		String email = getEmailField(), password = getPasswordField(), confirm = getConfirmField();
 		doCreate(email,password,confirm);
 	}
 	
 	/** Manipulates UI widgets if we should be in a loading state */
 	protected void setLoading(boolean state) {
 		// Do this if the device supports having a progress indicator in the
 		// activity's toolbar
 		if(mWindowIndeterminate == true) {
 			this.setProgressBarIndeterminate(true);
 			this.setProgressBarIndeterminateVisibility(state);
 		}
 		
 		// Otherwise, just show a boring progress dialog.
 		else if(state == true) {
 			showDialog(DIALOG_WAIT);
 		}
 		else if(state == false) {
 			removeDialog(DIALOG_WAIT);
 		}
 	}
 	
 	protected void doLogin(String email, String password) {
 		boolean fail = false;
 		int msg = 0;
 		
 		if(email.length() == 0 || password.length() == 0) {
 			msg = R.string.msg_required_fields_missing;
 			fail = true;
 		}
 		
 		if(fail) {
 			showDialog(msg);
 		}
 		else {
 			APICall task = new APICall(this, mSession);
 			task.login(email, password);
 			setLoading(true);
 		}
 	}
 	
 
 	protected void doCreate(String email, String password, String confirm) {
 		boolean fail = false;
 		int msg = 0;
 		
 		if(email.length() == 0 || password.length() == 0 || confirm.length() == 0) {
 			msg = R.string.msg_required_fields_missing;
 			fail = true;
 		}
 		else if(password.equals(confirm) == false) {
 			msg = R.string.msg_confirm_password;
 			fail = true;
 		}
 		
 		if(fail) {
 			showDialog(msg);
 		}
 		else {
 			new APICall(this, mSession).createAccount(email, password);
 		}
 	}
 	
 	protected String getEmailField() {
 		EditText emailField = (EditText) this.findViewById(R.id.login_input_email);
 		return emailField.getText().toString();
 	}
 	
 	protected String getPasswordField() {
 		EditText passwordField = (EditText) this.findViewById(R.id.login_input_password);
 		return passwordField.getText().toString();
 	}
 	
 	protected String getConfirmField() {
 		EditText confirmField = (EditText) this.findViewById(R.id.login_input_confirmpassword);
 		return confirmField.getText().toString();
 	}
 
 	
 	@Override
 	public void onCallComplete(boolean success, APICall task) {
 		setLoading(false);
 		Session s = task.getSession();
 		
 		if(success && s.isAuthenticated()) {
 			Intent i = new Intent(this, HomeActivity.class);
 			i.putExtra(Constants.EXTRA_SESSION, mSession);
 			startActivity(i);
 		}
 		else {
 			showDialog(task.getMessage());
 		}
 	}
 	
 	/** Constant to represent the ProgressDialog */
 	public static final int
 		DIALOG_WAIT = 1;
 	
 	public static final String
 		CONFIRM_STATE = "confirm-state";
 
 }
