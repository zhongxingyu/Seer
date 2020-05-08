 package com.dunksoftware.seminoletix;
 
 import java.util.concurrent.ExecutionException;
 import android.content.DialogInterface;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class LoginActivity extends Activity {
 
 	public static final String PREFS_NAME = "TixSettingsFile";
 	public static final String USER_NAME = "username";
 	public static final String ERROR_STRING = "error";
 
 	private EditText editUsername,
 	editPassword;
 
 	private Button mRegisterBtn,
 	mLoginBtn;
 
 	private String mUserResponse,
 	mPassResponse;
 
 	private UserControl mUserControl;
 	private UserControl.Login Login;
 
 	public static final int NO_CONNECTION_DIALOG = 80;
 	public static boolean remembered = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if( remembered ) {
 			// go to next page
 			startActivity(new Intent(this, ListActivity.class));
 		}
 		else {
 			// display the login screen and proceed
 			setContentView(R.layout.activity_login);
 
 			mUserControl = new UserControl();
 
 			// link widgets to variables 
 			editUsername = (EditText)findViewById(R.id.UI_EditFSUID);
 			editPassword = (EditText)findViewById(R.id.UI_EditFSUPass);
 
 			mRegisterBtn = (Button)findViewById(R.id.UI_registerBtn);
 			mLoginBtn = (Button)findViewById(R.id.UI_signinBtn);
 
 			// set anonymous on_click listeners for registration and login buttons
 			mLoginBtn.setOnClickListener(
 					new View.OnClickListener() {
 
 						// email, cardNum, password, remember_me
 						@SuppressWarnings("deprecation")
 						@Override
 						public void onClick(View v) {
 
 							mUserResponse = editUsername.getText().toString();
 							mPassResponse = editPassword.getText().toString();
 
 							if( ((CheckBox)findViewById(R.id.UI_CheckRememberMe)).isChecked()) {
 								Login = mUserControl.new Login(mUserResponse, mPassResponse, true);
 
 								ShowMessage("You will be remembered.", Toast.LENGTH_SHORT);
 
 								remembered = true;
 							}
 
 							if( !Online() ) 
 								showDialog(NO_CONNECTION_DIALOG);
 
 							else {
 								Login = mUserControl.new Login(mUserResponse, mPassResponse, false);
 								Login.execute();
 
 								try {
 									JSONObject JSONresponse = new JSONObject(Login.get());
 
 									// Send the user back to the login page.
 									if( JSONresponse.getString("success").equals("true")) {
 
 										// force the virtual keyboard off the screen
 										InputMethodManager imm = (InputMethodManager)getSystemService(
 												Context.INPUT_METHOD_SERVICE);
 										imm.hideSoftInputFromWindow(editPassword.getWindowToken(), 0);
 
 										startActivity(new Intent(getApplicationContext(), 
 												ListActivity.class));
 
 										//ShowMessage(JSONresponse.toString(), Toast.LENGTH_LONG);
 
 										/* Close the current activity, ensuring that this
 										 *  SAME page cannot be reached via Back button, 
 										 *  once a user has successfully registered. 
 										 *  (Basically takes this page out of the "page history" )
 										 */
 
 										//finish();
 									}
									// If the user is already logged in, just go back to the list.
									else if( JSONresponse.getString("success").equals("false") && 
												JSONresponse.getString("message").equals("Already logged in.")) {
										startActivity(new Intent(getApplicationContext(), ListActivity.class));
									}
 									/* if server returns false on registration, clear the CardNumber
 									 * and PIN field
 									 */
 									else {
 										// Print out a success message to the user's UI
 										ShowMessage( JSONresponse.getString("message"), Toast.LENGTH_LONG);
 
 										editUsername.getText().clear();
 										editPassword.getText().clear();
 									}
 								} catch (InterruptedException e) {
 									e.printStackTrace();
 								} catch (ExecutionException e) {
 									e.printStackTrace();
 								} catch (JSONException e) {
 									e.printStackTrace();
 								}
 							}
 						}
 					});
 
 			// Event handler for the Register button
 			mRegisterBtn.setOnClickListener(new View.OnClickListener() {
 
 				@SuppressWarnings("deprecation")
 				@Override
 				public void onClick(View arg0) {
 
 					if( !Online() ) 
 						showDialog(NO_CONNECTION_DIALOG);
 
 					else {
 						Intent nextActivityIntent = 
 								new Intent(getApplicationContext(), RegisterActivity.class);
 
 						startActivity(nextActivityIntent);
 					}
 				}
 			});
 		}
 	} // End of onCreate function
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_login, menu);
 		return true;
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected Dialog onCreateDialog(int id) {
 
 		AlertDialog.Builder builder;
 
 		switch( id ) {
 
 		case NO_CONNECTION_DIALOG: {
 			builder = new AlertDialog.
 					Builder(this);
 
 			builder.setCancelable(false).setTitle("Connection Error").
 			setMessage(R.string.Error_NoConnection).setNeutralButton("Close", 
 					new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					removeDialog(NO_CONNECTION_DIALOG);
 					finish();
 				}
 			});
 
 			builder.create().show();
 			break;
 		}
 		}
 		return super.onCreateDialog(id);
 	}
 
 	void ShowMessage(String message, int length) {
 		Toast.makeText(getApplicationContext(), message, length).show();
 	}
 
 	private boolean Online() {
 		ConnectivityManager connectivityManager 
 		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
 	}
 }
