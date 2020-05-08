 package com.rogoapp;
 
 import android.accounts.Account;
 import android.accounts.AccountAuthenticatorActivity;
 import android.accounts.AccountManager;
 import android.content.Context;
 //import android.content.ContentResolver;
 import android.content.Intent;
 //import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class RogoAuthenticatorActivity extends AccountAuthenticatorActivity {
 	public static final String PARAM_AUTHTOKEN_TYPE = "auth.token";  
     public static final String PARAM_CREATE = "create";  
   
     public static final int REQ_CODE_CREATE = 1;  
     public static final int REQ_CODE_UPDATE = 2;  
   
     public static final String EXTRA_REQUEST_CODE = "req.code";  
   
     public static final int RESP_CODE_SUCCESS = 0;  
     public static final int RESP_CODE_ERROR = 1;  
     public static final int RESP_CODE_CANCEL = 2; 
     
     public static boolean createToken;
   
     @Override  
     protected void onCreate(Bundle icicle) {  
         // recreates from saved state -- icicle is the same as savedInstanceState  
         super.onCreate(icicle);  
         //TODO allow view to be created for sign in and authorization forms
         //creates a view from the login.xml file
         this.setContentView(R.layout.login);
     }  
     
     private boolean getToken(){
     	return createToken;
     }
     private void setToken(boolean Token){
     	createToken = Token;
     }
   
     public void onCancelClick(View v) {  
   
         this.finish();  
   
     }  
     public void openRegisterScreen(View v){
         final Context context = this;
         Intent intent = new Intent(context, RegisterActivity.class);
         startActivity(intent);
     }
     public void onRememberMe(View V){
    	Button btnUserName = (Button) this.findViewById(R.id.btnLogin);
    	
     	if(getToken()){
     		this.setToken(false);
     	}
     	else this.setToken(true);
     	
     	if(getToken()){
     		btnUserName.setText("Retain Login");
     	}
    	else btnUserName.setText("Login"); 
     }
   
     public void onSaveClick(View v) {  
         TextView tvUsername;  
         TextView tvPassword;  
           
         String username;  
         String password;  
          
         boolean hasErrors = false;  
   
         tvUsername = (TextView) this.findViewById(R.id.auth_txt_username);  
         tvPassword = (TextView) this.findViewById(R.id.auth_txt_pswd);   
   
         tvUsername.setBackgroundColor(Color.WHITE);  
         tvPassword.setBackgroundColor(Color.WHITE);  
   
         username = tvUsername.getText().toString();  
         password = tvPassword.getText().toString();  
   
         if (username.length() < 3) {  
             hasErrors = true;  
             tvUsername.setBackgroundColor(Color.MAGENTA);  
         }  
         if (password.length() < 3) {  
             hasErrors = true;  
             tvPassword.setBackgroundColor(Color.MAGENTA);  
         }  
         if (hasErrors) {  
             return;  
         }  
           
         // Now that we have done some simple "client side" validation it  
         // is time to check with the server  
   
         //TODO ... perform some network activity here  
   
         // finished  
   
         String accountType = this.getIntent().getStringExtra(PARAM_AUTHTOKEN_TYPE);  
         if (accountType == null) {  
             accountType = AccountAuthenticator.ACCOUNT_TYPE;  
         }  
   
         AccountManager accMgr = AccountManager.get(this);  
   
         if (hasErrors) {  
   
             // handle errors  
   
         } else {  
   
             // This is the magic that adds the account to the Android Account Manager  
             final Account account = new Account(username, accountType);  
             accMgr.addAccountExplicitly(account, password, null);  
   
             // Now we tell our caller, could be the Android Account Manager or even our own application  
             // that the process was successful  
   
             final Intent intent = new Intent();  
             intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);  
             intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
             if(getToken()){
             	
             	intent.putExtra(AccountManager.KEY_AUTHTOKEN, accountType);
             	
             }
             
             this.setAccountAuthenticatorResult(intent.getExtras());  
             this.setResult(RESULT_OK, intent);  
             this.finish();  
   
         }  
     }
 }
