 
 package qszhu.parse.login;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.parse.LogInCallback;
 import com.parse.ParseException;
 import com.parse.ParseUser;
 import com.parse.SignUpCallback;
 
 public class LoginActivity extends Activity {
 
     private static String TAG = LoginActivity.class.getCanonicalName();
     private EditText mLoginUsername, mLoginPassword;
     private EditText mSignUpUsername, mSignUpPassword, mSignUpEmail;
     private View mLoginFormView, mSignUpFormView, mStatusView;
     private TextView mStatusMessage;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
 
         // UI entries
         mLoginFormView = findViewById(R.id.login_form);
         mLoginUsername = (EditText) findViewById(R.id.login_username);
         mLoginPassword = (EditText) findViewById(R.id.login_password);
 
         mSignUpFormView = findViewById(R.id.sign_up_form);
         mSignUpUsername = (EditText) findViewById(R.id.sign_up_username);
         mSignUpPassword = (EditText) findViewById(R.id.sign_up_password);
         mSignUpEmail = (EditText) findViewById(R.id.sign_up_email);
 
         mStatusView = findViewById(R.id.login_status);
         mStatusMessage = (TextView) findViewById(R.id.login_status_message);
 
         // Events
         mLoginPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
             @Override
             public boolean onEditorAction(TextView v, int actionId,
                     KeyEvent event) {
                 if (actionId == R.id.action_login
                         || actionId == EditorInfo.IME_NULL) {
                     login();
                     return true;
                 }
                 return false;
             }
         });
         mSignUpEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
             @Override
             public boolean onEditorAction(TextView v, int actionId,
                     KeyEvent event) {
                 if (actionId == R.id.action_sign_up
                         || actionId == EditorInfo.IME_NULL) {
                     signUp();
                     return true;
                 }
                 return false;
             }
         });
         findViewById(R.id.log_in_button).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 login();
             }
         });
         findViewById(R.id.show_sign_up_button).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 switchView(mLoginFormView, mSignUpFormView);
             }
         });
         findViewById(R.id.sign_up_button).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 signUp();
             }
         });
     }
 
    @Override
    public void onBackPressed() {
        if (mSignUpFormView.getVisibility() == View.VISIBLE) {
            switchView(mSignUpFormView, mLoginFormView);
            return;
        }
        super.onBackPressed();
    }

     private void signUp() {
         final String username = String.valueOf(mSignUpUsername.getText());
         final String password = String.valueOf(mSignUpPassword.getText());
         final String email = String.valueOf(mSignUpEmail.getText());
 
         ParseUser user = new ParseUser();
         user.setUsername(username);
         user.setPassword(password);
         user.setEmail(email);
 
         mStatusMessage.setText(R.string.login_progress_signing_up);
         switchView(mSignUpFormView, mStatusView);
 
         user.signUpInBackground(new SignUpCallback() {
             @Override
             public void done(ParseException e) {
                 if (e != null) {
                     switchView(mStatusView, mSignUpFormView);
 
                     Log.d(TAG, e.getMessage());
                     showErrorDialog(R.string.error, R.string.sign_up_error);
                     return;
                 }
                 login(username, password);
             }
         });
     }
 
     private void login() {
         final String username = String.valueOf(mLoginUsername.getText());
         final String password = String.valueOf(mLoginPassword.getText());
         login(username, password);
     }
 
     private void login(String username, String password) {
         mStatusMessage.setText(R.string.login_progress_logging_in);
         switchView(mLoginFormView, mStatusView);
 
         ParseUser.logInInBackground(username, password, new LogInCallback() {
             @Override
             public void done(ParseUser parseUser, ParseException e) {
                 switchView(mStatusView, mLoginFormView);
 
                 if (e != null) {
                     Log.d(TAG, e.getMessage());
                     showErrorDialog(R.string.error, R.string.login_error);
                     return;
                 }
                 finish();
             }
         });
     }
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
     private void switchView(final View viewToHide, final View viewToShow) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
             int shortAnimTime = getResources().getInteger(
                     android.R.integer.config_shortAnimTime);
 
             viewToShow.setVisibility(View.VISIBLE);
             viewToShow.animate()
                     .setDuration(shortAnimTime)
                     .alpha(1)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationEnd(Animator animation) {
                             viewToShow.setVisibility(View.VISIBLE);
                         }
                     });
 
             viewToHide.setVisibility(View.VISIBLE);
             viewToHide.animate()
                     .setDuration(shortAnimTime)
                     .alpha(0)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationEnd(Animator animation) {
                             viewToHide.setVisibility(View.GONE);
                         }
                     });
         } else {
             viewToShow.setVisibility(View.VISIBLE);
             viewToHide.setVisibility(View.GONE);
         }
     }
 
     private void showErrorDialog(int title, int message) {
         new AlertDialog.Builder(this)
                 .setTitle(title)
                 .setMessage(message)
                 .setIcon(android.R.drawable.ic_dialog_alert)
                 .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog,
                             int which) {
                         dialog.dismiss();
                     }
                 })
                 .show();
     }
 
 }
