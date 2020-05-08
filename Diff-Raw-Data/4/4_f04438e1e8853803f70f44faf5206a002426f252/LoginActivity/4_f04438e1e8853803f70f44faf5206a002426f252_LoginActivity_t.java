 package com.ladinc.showrss.activitys;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.CookieSyncManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.ladinc.showrss.LoginTask;
 import com.ladinc.showrss.utilities.LoadingDialog;
 import com.ladinc.showrss.utilities.Utilities;
 import com.ladinc.showrss.R;
 
 public class LoginActivity extends Activity implements OnClickListener {
 
 	// Declarations for threading
 	private String username;
 	private String password;
 	EditText uNameEdit;
 	EditText passEdit;
 	LoadingDialog loadingDialog;
 	
 	private CheckBox rememberPassword;
 	private Button loginButton;
 	private Button registerButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// TODO: cookie manager is not working yet
 		CookieSyncManager.createInstance(this);
 
 		setContentView(R.layout.login);
 
 		this.setupViews();
 		this.setupListeners();
		
		this.loadSavedSettings();
 	}
 
 	private void loadSavedSettings() {
 		
 		SharedPreferences settings = this.getPreferences(Context.MODE_PRIVATE);
 		
 		this.username = settings.getString("username", ""); 
 		
 		this.password = settings.getString("password", "");
 		
 		//If the user has already set a password set the checkbox.
 		if (!this.password.equalsIgnoreCase(""))
 		{
 			this.rememberPassword.setChecked(true);
 		}
 		
 		this.uNameEdit.setText(this.username);
 		this.passEdit.setText(this.password);
 		
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		// TODO: cookie manager is not working yet
 		CookieSyncManager.getInstance().stopSync();
 
 	}
 
 	private void setupViews() {
 		loginButton = (Button) this.findViewById(R.id.loginButton);
 		registerButton = (Button) this.findViewById(R.id.registerButton);
 		rememberPassword = (CheckBox) this.findViewById(R.id.rememberPasswordCheckbox);
 		
 		uNameEdit = (EditText) this.findViewById(R.id.username);
 		passEdit = (EditText) this.findViewById(R.id.password);
 		loadingDialog = new LoadingDialog(this, getString(R.string.logging_in_));
 	}
 
 	private void setupListeners() {
 		loginButton.setOnClickListener(this);
 		registerButton.setOnClickListener(this);
 		rememberPassword.setOnClickListener(this);
 	}
 
 	private void displayToast(String text) {
 
 		// Creates and displays a toast
 		Toast.makeText(this, getString(R.string.error_) + text, Toast.LENGTH_SHORT).show();
 	}
 
 	/**
 	 * This method
 	 * 
 	 */
 	public void changeToMenu() {
 		Log.d("LoginActivity", "Changing to Menu");
 
 		CookieSyncManager.getInstance().sync();
 
 		Intent myIntent = new Intent(this, MenuActivity.class);
 		try {
 			startActivity(myIntent);
 		} finally {
 			finish();
 		}
 	}
 	
 	public void changeToRegister() {
 		Log.d("LoginActivity", "Changing to Register");
 
 		CookieSyncManager.getInstance().sync();
 
 		Intent myIntent = new Intent(this, RegisterActivity.class);
 		try {
 			startActivity(myIntent);
 		} finally {
 			finish();
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 
 		if (Utilities.isOnline(this)) 
 		{
 			switch (v.getId()) 
 			{
 				case R.id.loginButton:
 					// extract user name
 					this.username = uNameEdit.getText().toString();
 					this.password = passEdit.getText().toString();
 	
 					LoginTask login = new LoginTask(this.username, this.password);
 					storeLoginCreds();
 					new LoginToRss().execute(login);
 					break;
 				case R.id.registerButton:
 					changeToRegister();
 					break;
 				case R.id.rememberPasswordCheckbox:
 					displayPasswordWarning();
 					break;
 
 			}
 		} 
 		else {
 			displayToast(getString(R.string.no_internet_connection));
 		}
 
 	}
 
 	private void displayPasswordWarning() {
 		
 		//If we got here the checkbox has been clicked
 		if (this.rememberPassword.isChecked())
 		{
 			Intent intent = new Intent(this, DisplayErrorActivity.class);
 			ArrayList<String> errors = new ArrayList<String>();
 			errors.add(getString(R.string.password_string));
 	    	intent.putStringArrayListExtra("errorList", errors);
 	    	intent.putExtra("title", "ShowRss Mobile Message");
 	    	
 	    	startActivityForResult(intent, 2);
 		}
 		
 	}
 
 	private void storeLoginCreds() {
 		Editor settings = this.getPreferences(Context.MODE_PRIVATE).edit();
 		
 		settings.putString("username", this.username);
 		
 		if (this.rememberPassword.isChecked())
 		{
 			settings.putString("password", this.password);
 		}
 		else
 		{
 			settings.putString("password", "");
 		}
 		
 		settings.commit();
 		
 	}
 
 	class LoginToRss extends AsyncTask<LoginTask, Integer, String> {
 		@Override
 		protected void onPreExecute() {
 			loadingDialog.showLoadingDialog();
 		}
 
 		@Override
 		protected String doInBackground(LoginTask... login) {
 
 			LoginTask newLogin = login[0];
 
 			try {
 				return newLogin.attemptLogin();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return "Error";
 
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			// Check if there was an error
 			if (result == null) {
 				loadingDialog.hideLoadingDialog();
 				changeToMenu();
 
 			} else {
 				loadingDialog.hideLoadingDialog();
 				displayToast(result);
 			}
 
 		}
 
 	}
 
 }
