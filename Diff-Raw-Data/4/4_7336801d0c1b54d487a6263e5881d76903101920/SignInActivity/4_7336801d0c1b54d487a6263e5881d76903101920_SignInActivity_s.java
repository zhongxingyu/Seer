 package com.haligali.PowerHangmanClient.Menus;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.*;
 import com.haligali.PowerHangmanClient.Networking.LoginTask;
 import com.haligali.PowerHangmanClient.Networking.MakeAccountTask;
 import com.haligali.PowerHangmanClient.Networking.OnLoginResponse;
 import com.haligali.PowerHangmanClient.Networking.ServerResponseCodes.LogInCode;
 import com.haligali.PowerHangmanClient.PowerHangmanApp;
 import com.haligali.PowerHangmanClient.R;
 import junit.framework.Assert;
 import org.json.JSONObject;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Paul
  * Date: 10/8/13
  * Time: 7:00 PM
  * To change this template use File | Settings | File Templates.
  */
 public class SignInActivity extends Activity implements OnLoginResponse
 {
     // TODO make is so we don't lose our AsyncTasks when the screen rotates, try headless fragments
     private EditText _usernameEntry;
     private EditText _passwordEntry;
     private EditText _confirmPassword;
     private Button _signInButton;
     private CheckBox _needAccountCheckBox;
 
    private SharedPreferences _playerPrefs = this.getSharedPreferences(PowerHangmanApp.PLAYER_PREFERENCES, Context.MODE_PRIVATE);
 
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         // If the player ID is not stored as an empty string, then we are already signed in and can
         // skip to the main menu.
         if (_playerPrefs.getString(PowerHangmanApp.PLAYER_ID_EKEY, "").compareTo("") != 0)
         {
             GoToMainMenu();
         }
 
         setContentView(R.layout.sign_in_layout);
 
         // Grab needed on screen View handles for later use.
         _usernameEntry = (EditText) findViewById(R.id.etx_UserNameEntry);
         _passwordEntry = (EditText) findViewById(R.id.etx_PasswordEntry);
         _confirmPassword = (EditText) findViewById(R.id.etx_PasswordConfirm);
         _signInButton = (Button) findViewById(R.id.btn_SignAction);
         _needAccountCheckBox = (CheckBox) findViewById(R.id.chb_MakeAccount);
 
         // Set the logic for when someone selects or deselects "I need an account"
         _needAccountCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
         {
             @Override
             public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
             {
                 _confirmPassword.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                 _signInButton.setText(isChecked ? R.string.SI_MakeAccountButtonLabel : R.string.SI_SignInButtonLabel);
             }
         });
     }
 
     public void onLogInClick(final View v)
     {
         if (_needAccountCheckBox.isChecked())
         {
             // Making a new account
             final String password = _passwordEntry.getText().toString();
             final String confPassword = _confirmPassword.getText().toString();
 
             if (password.compareTo(confPassword) == 0)
             {
                 // Make sure the two passwords are the same...
                 new MakeAccountTask().execute(_usernameEntry.getText().toString(), _passwordEntry.getText().toString());
                 LockDownScreen(true);
             }
             else
             {
                 // If the passwords are not the same..
                 final Toast toast = Toast.makeText(getApplicationContext(), R.string.SI_NoMatchPasswordsToast, Toast.LENGTH_SHORT);
                 toast.show();
 
                 // Clear the passwords for re-entry
                 _passwordEntry.setText("");
                 _confirmPassword.setText("");
             }
         }
         else
         {
             // We're just logging in
             new LoginTask().execute(_usernameEntry.getText().toString(), _passwordEntry.getText().toString());
             LockDownScreen(true);
         }
     }
 
     @Override
     public void ReceiveLoginResponse(final LogInCode code, final String playerID)
     {
         switch (code)
         {
             case LOG_IN_SUCCESSFUL:
             case ACCOUNT_CREATE_SUCCESSFUL:
                 final SharedPreferences.Editor newPlayerInfo = _playerPrefs.edit();
                 newPlayerInfo.putString(PowerHangmanApp.PLAYER_ID_EKEY, playerID);
                 newPlayerInfo.putString(PowerHangmanApp.PLAYER_USERNAME_EKEY, _usernameEntry.getText().toString());
                 newPlayerInfo.commit();
 
                 GoToMainMenu();
                 break;
 
             case LOG_IN_DENIED:
                 final Toast loginToast = Toast.makeText(getApplicationContext(), R.string.SI_LogInFailedToast, Toast.LENGTH_SHORT);
                 loginToast.show();
 
                 // Re-enable screen so user can try again.
                 LockDownScreen(false);
                 break;
 
             case ACCOUNT_CREATE_DENIED:
                 final Toast actCreateToast = Toast.makeText(getApplicationContext(), R.string.SI_UserNameInvalidToast, Toast.LENGTH_SHORT);
                 actCreateToast.show();
 
                 // Re-enable screen so user can try again.
                 LockDownScreen(false);
                 break;
 
             case SERVER_TIMEOUT:
                 // Re-enable screen so user can try again.
                 LockDownScreen(false);
                 break;
 
             default:
                 Assert.fail("Received invalided LogInCode");
                 break;
         }
     }
 
     private void LockDownScreen(final boolean enable)
     {
         final boolean enableViews = !enable;
 
         _usernameEntry.setEnabled(enableViews);
         _passwordEntry.setEnabled(enableViews);
         _confirmPassword.setEnabled(enableViews);
         _signInButton.setEnabled(enableViews);
         _needAccountCheckBox.setEnabled(enableViews);
 
         if (enable)
         {
             // If we're enabling the views, clear them of any old user input
             _usernameEntry.setText("");
             _passwordEntry.setText("");
             _confirmPassword.setText("");
         }
     }
 
     private void GoToMainMenu()
     {
         final Intent MainMenuIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
         startActivity(MainMenuIntent);
         finish();
     }
 }
