 package info.vanderkooy.ucheck;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
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
 	private ProgressDialog dialog;
 
 	// private Button newData;
 
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
 
 		// newData.setVisibility(8);
 
 		usr = prefs.getUsername();
 		if (usr == null)
 			username.setHint(getString(R.string.studentNumberHint));
 		else
 			username.setText(usr);
 		password.setHint(getString(R.string.passHint));
 		password.setTextSize(13);
 		storePass.setChecked(prefs.getStorePass());
 
 		loginButton.setText(getString(R.string.login));
 		loginButton.setOnClickListener(loginListener);
 		infoButton.setOnClickListener(infoButtonListener);
 	}
 
 	private OnClickListener loginListener = new OnClickListener() {
 		public void onClick(View v) {
 			String usernameString = username.getText().toString();
 
 			if (usernameString.length() >= 1
 					&& !usernameString.substring(0, 1).equals("s"))
 				usernameString = "s" + usernameString;
 
 			dialog = ProgressDialog.show(Login.this, "",
 					getString(R.string.login_action), true);
 
 			final String finalUsernameString = usernameString;
 			Thread thread = new Thread(new Runnable() {
 				public void run() {
 					if(handler.isNetworkAvailable()) {
 						int returned = 0;
 						final boolean success = (finalUsernameString.length() < 7 || finalUsernameString
 								.length() > 9) ? false : ((returned = handler
 								.getKey(finalUsernameString, password.getText()
 										.toString())) == 1 ? true : false);
 						final int finalReturned = returned;
 	
 						runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								if (success) {
 									prefs.setStorePass(storePass.isChecked());
 									if(!usr.equals(finalUsernameString))
 										prefs.forceNewData();
								    try {
										dialog.hide();
								        dialog.dismiss();
								        dialog = null;
								    } catch (Exception e) {
								        // nothing
								    }
 									finish();
 								} else {
 									Toast toast;
 									if (finalReturned == 0) {
 										toast = Toast.makeText(
 												getApplicationContext(),
 												getString(R.string.userError), Toast.LENGTH_LONG);
 									} else {
 										toast = Toast
 												.makeText(
 														getApplicationContext(),
 														getString(R.string.verificationError),
 														Toast.LENGTH_LONG);
 									}
 									toast.show();
 									dialog.hide();
 									dialog.dismiss();
 								}
 							}
 						});
 					} else {
 						runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								if (dialog.isShowing()) {
 									dialog.hide();
 									dialog.dismiss();
 								}
 								handler.noNetworkToast();
 							}
 						});
 					}
 				}
 			});
 			thread.start();
 
 		}
 	};
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
 	    	prefs.setKillApp();
 	    	int pid = android.os.Process.myPid();
 	        android.os.Process.killProcess(pid);
 	    }
 
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	private OnClickListener infoButtonListener = new OnClickListener() {
 		public void onClick(View v) {
 			prefs.setGoingToInfo(true);
 			Intent infoIntent = new Intent().setClass(Login.this, Info.class);
 			Login.this.startActivity(infoIntent);
 		}
 	};
 
 }
