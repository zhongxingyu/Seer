 package com.kanbandroid.activity;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import com.kanbandroid.R;
import com.octo.android.rest.client.ContentActivity;
 
 import javax.security.auth.login.LoginException;
 
public class LoginActivity extends ContentActivity {
     private EditText etUsername;
     private EditText etPassword;
     private TextView tvLoginError;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         this.tvLoginError = (TextView) findViewById(R.id.tv_login_error);
 
         this.etUsername = (EditText) findViewById(R.id.et_login_username);
         this.etPassword = (EditText) findViewById(R.id.et_login_password);
 
         Button btnLogin = (Button) findViewById(R.id.bt_login_connexion);
         btnLogin.setOnClickListener(new View.OnClickListener() {

             public void onClick(View view) {
                 boolean hasLogin;
                 try {
                     hasLogin = login(etUsername.getText().toString(), etPassword.getText().toString());
                     if (hasLogin) {
                         navigateToProjectsScreen();
                     }
                 } catch (LoginException e) {
                     showErrorMessage(e.getMessage());
                 }
             }
         });
     }
 
     private void showErrorMessage(String message) {
         this.tvLoginError.setText(message);
         this.tvLoginError.setVisibility(View.VISIBLE);
     }
 
     private void navigateToProjectsScreen() {
 
     }
 
     private boolean login(String username, String password) throws LoginException {
         throw new LoginException("");
     }
 }
