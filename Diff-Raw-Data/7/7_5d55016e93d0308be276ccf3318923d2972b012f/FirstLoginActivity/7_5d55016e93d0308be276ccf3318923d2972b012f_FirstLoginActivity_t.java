 package com.DGSD.SecretDiary.Activity.Phone;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.DGSD.SecretDiary.Activity.BaseActivity;
 import com.DGSD.SecretDiary.Activity.EntryListChoice;
 import com.DGSD.SecretDiary.Encryption;
 import com.DGSD.SecretDiary.R;
 import com.DGSD.SecretDiary.SecretDiaryApplication;
 import com.DGSD.SecretDiary.Utils;
 import greendroid.widget.ActionBar;
 import greendroid.widget.ActionBar.Type;
 import greendroid.widget.ActionBarItem;
 import greendroid.widget.NormalActionBarItem;
 
 public class FirstLoginActivity extends BaseActivity {
 
 	public static final int ACTION_INFO = 0;
 	
 	public static final int ACTION_SUBMIT = 1;
 	
 	private static final String KEY_LAST_ALERT_TITLE = "alert_title";
 
 	private static final String KEY_LAST_ALERT_MESSAGE = "alert_message";
 	
 	private ActionBar mActionBar;
 	
 	private EditText mPasswordField;
 
 	private EditText mPasswordConfirmField;
 	
 	private EditText mPasswordHint;
 	
 	private TextView mPwStrength;
 	
 	private AlertDialog currentDialog;
 
 	private String mLastAlertTitle;
 
 	private String mLastAlertMessage;
 
 	private SharedPreferences mPrefs;
 	
 	private SecretDiaryApplication mApplication;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setActionBarContentView(R.layout.first_login);
 		
 		//Get Application preferences
 		mPrefs = getSharedPreferences(SecretDiaryApplication.KEY_MY_PREFERENCES, 0);
 		mApplication = (SecretDiaryApplication) getApplication();
 		
 		//Set up Action bar
 		mActionBar = getGdActionBar();
 		mActionBar.setTitle("Secret Diary Setup");
 		mActionBar.setType(Type.Empty);
 		
 		addActionBarItem(ActionBarItem.Type.Info, ACTION_INFO);
 		addActionBarItem(mActionBar
                 .newActionBarItem(NormalActionBarItem.class)
                 .setDrawable(R.drawable.ic_menu_forward), ACTION_SUBMIT);
 		
 		//Get handles to activity views.
 		mPasswordField = (EditText) findViewById(R.id.password);
 
 		mPasswordConfirmField = (EditText) findViewById(R.id.password_confirm);
 		
 		mPasswordHint = (EditText) findViewById(R.id.password_hint);
 
 		mPwStrength = (TextView) findViewById(R.id.password_strength);
 		
 		//Setup handlers
 		mPasswordField.addTextChangedListener(new PasswordStrengthWatcher(mPwStrength));
 		
 		//Restore any previously showing dialogs
 		restoreDialog(savedInstanceState);
 	}
 
 	@Override
 	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
 		switch(item.getItemId()) {
 			case ACTION_INFO:
 				showDialog("Info Pressed", "You pressed the info button");
 				break;
 			case ACTION_SUBMIT:
 				final String password = mPasswordField.getText().toString();
 
 				final String password_confirm = mPasswordConfirmField.getText().toString();
 				
 				final String password_hint = mPasswordHint.getText().toString();
 
 				if(password == null || password.length() == 0 || 
 						password_confirm == null || password_confirm.length() == 0) {
 					showDialog("Error", 
 							"Please fill out both password fields");
 					break;
 				}
 
 				if(!password.equals(password_confirm)) {
 					showDialog("Error", 
 							"Passwords do not match. Please confirm your password");
 					break;
 				}
 				
 				try{
 					mApplication.setPassword(password);
 
 					if(password_hint != null && password_hint.length() > 0) {
 						mApplication.setPasswordHint(password_hint);
 						
 						mPrefs.edit().putString(SecretDiaryApplication.KEY_PASSWORD_HINT, password_hint).commit();
 					}
 					
 					mPrefs.edit().putString(SecretDiaryApplication.KEY_ENCRYPTION_TEST, 
 							getEncryptionKey(password)).commit();
 
 					mPrefs.edit().putBoolean(SecretDiaryApplication.KEY_HAS_LOGGED_IN_BEFORE, true).commit();

                    Intent intent = new Intent(FirstLoginActivity.this, EntryListChoice.class);
                    intent.putExtra(Utils.EXTRA.INTERNAL, true);

					startActivity(intent);
 					finish();
 				} catch(Exception e) {
 					Toast.makeText(FirstLoginActivity.this, 
 							"Error creating login. Please try again", 
 							Toast.LENGTH_LONG).show();
 
 					e.printStackTrace();
 				}
 				
 				break;
 			default:
 				return super.onHandleActionBarItemClick(item, position);
 		}
 		return true;
 	}
 	
 	@Override
 	public void onStop() {
 		if(currentDialog != null) {
 			currentDialog.dismiss();
 			currentDialog = null;
 		}
 
 		super.onStop();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if(currentDialog != null && currentDialog.isShowing()){
 			outState.putString(KEY_LAST_ALERT_TITLE, mLastAlertTitle);
 			outState.putString(KEY_LAST_ALERT_MESSAGE, mLastAlertMessage);
 		}
 	}
 
 	private void restoreDialog(Bundle bundle) {
 		if(bundle != null) {
 			mLastAlertTitle = bundle.getString(KEY_LAST_ALERT_TITLE);
 
 			mLastAlertMessage = bundle.getString(KEY_LAST_ALERT_MESSAGE);
 
 			if(mLastAlertTitle != null && mLastAlertMessage != null) {
 				showDialog(mLastAlertTitle, mLastAlertMessage);
 			}
 		}
 	}
 	
 	private String getEncryptionKey(String password) throws Exception {
 		return Encryption.encrypt(password, 
 				getResources().getString(R.string.password_passage));
 	}
 	
 	private void showDialog(String title, String message) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		builder.setTitle(title);
 		builder.setMessage(message);
 
 		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.dismiss();
 			}
 		});
 
 		currentDialog = builder.create();
 
 		mLastAlertTitle = title;
 		
 		mLastAlertMessage = message;
 		
 		currentDialog.show();
 	}
 	
 	/**
 	 * Class to show an indication of password strength as the user types
 	 * @author Daniel Grech
 	 */
 	private static class PasswordStrengthWatcher implements TextWatcher {
 		TextView mView;
 
 		public PasswordStrengthWatcher(TextView tv) {
 			mView = tv;
 		}
 
 		@Override
 		public void afterTextChanged(Editable s) {
 			if(s == null || s.length() == 0) {
 				mView.setVisibility(View.INVISIBLE);
 			} else {
 				mView.setVisibility(View.VISIBLE);
 
 				int textColor = Color.BLUE;
 				String text = null;
 				switch(Utils.getPasswordRating(s.toString())) {
 					case Utils.Password.WEAK:
 						textColor = Color.RED;
 						text = "Weak Password";
 						break;
 
 					case Utils.Password.OK:
 						textColor = Color.BLUE;
 						text = "OK Password";
 						break;
 
 					case Utils.Password.STRONG:
 						textColor = Color.GREEN;
 						text = "Strong Password";
 						break;
 				}
 
 				mView.setText(text);
 				mView.setTextColor(textColor);
 
 			}
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 
 		}
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 
 		}
 	}
 
 }
