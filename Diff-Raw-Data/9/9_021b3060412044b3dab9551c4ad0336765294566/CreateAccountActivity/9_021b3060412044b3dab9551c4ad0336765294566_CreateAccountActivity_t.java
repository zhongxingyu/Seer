 package edu.myhorseshow;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class CreateAccountActivity extends Activity implements OnClickListener
 {
 	private static final String TAG = "CreateAccountActivity";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_create_account);
 		
 		setOnClickListeners();
 	}
 	
 	private void setOnClickListeners()
     {
     	Button createAccountButton = (Button) findViewById(R.id.create_account_submit_button);
     	
     	createAccountButton.setOnClickListener(this);
     }
 
 	public void onClick(View clickedView) 
 	{
 		switch(clickedView.getId())
     	{
     	case R.id.create_account_submit_button:
     		submitCreateInfo();
     		break;
     	default:
     		Log.w(TAG, "Firing unimplemented button event.");
     	}
 	}
 	
 	private void submitCreateInfo()
     {
     	EditText emailField = (EditText) findViewById(R.id.create_account_email_edit_text);
     	EditText passwordField = (EditText) findViewById(R.id.create_account_password_edit_text);
     	EditText firstNameField = (EditText) findViewById(R.id.create_account_first_name_edit_text);
     	EditText lastNameField = (EditText) findViewById(R.id.create_account_last_name_edit_text);
    	EditText confirmPasswordField = (EditText) findViewById(R.id.create_account_confirmpass_edit_text);
     	
     	
     	String emailAddress = emailField.getText().toString();
     	String password = passwordField.getText().toString();
     	String firstName = firstNameField.getText().toString();
     	String lastName = lastNameField.getText().toString();
    	String userName = confirmPasswordField.getText().toString();
     	
     	/*AsyncTask<String, Integer, User> fetcher = new AsyncTask<String, Integer, User>()
     	{
     		@Override
 			protected User doInBackground(String... emailPassword)
     		{
     			if (emailPassword.length != 2)
     				return null;
     			
     			String email = emailPassword[0];
     			String password = emailPassword[1];
     			
     			String url = new UrlBuilder(Constants.SERVER_DOMAIN)
     					.setScriptChained("enter.php")
     					
     					.addArg(Constants.EMAIL_ADDR_PARAM, email)
     					.addArg(Constants.PASSWORD_PARAM, password)
     					.toString();
 		    	
 		    	return Utility.getJsonObject(url, User.class);
     		}
     		
     		@Override
 			protected void onPostExecute(User user)
     		{
     			mLoadingDialog.dismiss();
     			processUserLogin(user);
     			clearForm();
     		}
     	};*/
    }
 }
