 package com.quanturium.androcloud2.activities;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.cloudapp.api.CloudApp;
 import com.cloudapp.api.CloudAppException;
 import com.cloudapp.api.model.CloudAppAccount;
 import com.cloudapp.impl.CloudAppImpl;
 import com.quanturium.androcloud2.R;
 import com.quanturium.androcloud2.tools.Prefs;
 
 public class LoginActivity extends Activity implements OnClickListener
 {
 	EditText						emailText;
 	EditText						passwordText;
 	Button							loginButton;
 	private final static Handler	handler				= new Handler();
 	private ProgressDialog			loginRunningDialog	= null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		emailText = (EditText) findViewById(R.id.loginEmail);
 		passwordText = (EditText) findViewById(R.id.loginPassword);
 		loginButton = (Button) findViewById(R.id.loginBouton);
 		loginButton.setOnClickListener(this);
 	}
 
 	@Override
 	protected void onDestroy()
 	{
 		if (loginRunningDialog != null)
 			loginRunningDialog.dismiss();
 
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case android.R.id.home:
 				Intent intent = new Intent(this, SplashActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				intent.putExtra("animated", false);
 				startActivity(intent);
 				return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void loginRunningDialog()
 	{
 		ProgressDialog dialog = new ProgressDialog(this);
 		dialog.setMessage("Authentication");
 		dialog.setCancelable(false);
 
 		loginRunningDialog = dialog;
 		loginRunningDialog.show();
 	}
 
 	@Override
 	public void onClick(View v)
 	{
 		final String email = emailText.getText().toString().trim();
 		final String password = passwordText.getText().toString().trim();
 
 		if (email.equals("") || password.equals(""))
 		{
 			Toast.makeText(this, "All the fields need to be filled", Toast.LENGTH_SHORT).show();
 		}
 		else
 		{
 			loginRunningDialog();
 
 			new Thread(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					CloudApp api = new CloudAppImpl(email, password);
 
 					try
 					{
 						final CloudAppAccount account = api.getAccountDetails();
 
 						if (account != null && account.getEmail().equals(email)) // all is OK
 						{
 							handler.post(new Runnable()
 							{
 
 								@Override
 								public void run()
 								{
 									loginRunningDialog.cancel();
 
 									int hash = (email + password).hashCode();
 
 									Prefs.getPreferences(LoginActivity.this).edit().putString(Prefs.USER_INFOS, account.getJson().toString()).commit();
 									Prefs.getPreferences(LoginActivity.this).edit().putString(Prefs.EMAIL, email).commit();
 									Prefs.getPreferences(LoginActivity.this).edit().putString(Prefs.PASSWORD, password).commit();
 									Prefs.getPreferences(LoginActivity.this).edit().putInt(Prefs.HASH, hash).commit();
 									Prefs.getPreferences(LoginActivity.this).edit().putBoolean(Prefs.LOGGED_IN, true).commit();
 
 									Intent returnIntent = new Intent();
 									setResult(RESULT_OK, returnIntent);
 									finish();
 								}
 							});
 						}
 					} catch (CloudAppException e)
 					{
 						handler.post(new Runnable()
 						{
 							@Override
 							public void run()
 							{
 								loginRunningDialog.cancel();
 								
 								Toast.makeText(LoginActivity.this, "Login failed. Check your connection and credentials", Toast.LENGTH_SHORT).show();
 							}
 						});
 
 						e.printStackTrace();
 					}
 				}
 			}).start();
 		}
 	}
 }
