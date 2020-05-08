 package com.capsule.android;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.EditText;
 
 import com.capsule.android.repositories.LoginRepository;
 import com.capsule.common.Navigator;
 import com.capsule.model.User;
 
 public class LoginActivity extends BaseActivity {
 	
 	private LoginRepository loginRespository = null;
 	private User user = null;
 	
 	private AutoCompleteTextView numberView = null;
 	private EditText passwordView = null; 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		loginRespository = new LoginRepository(this);
 		numberView = (AutoCompleteTextView) findViewById(R.id.editTextNumber);
 		passwordView = (EditText)findViewById(R.id.editTextPassword);
 		
		setContentView(R.layout.activity_login);
 	}
 
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 		
 		//Auto Login first
 		user = loginRespository.loginAsLast();		
 		//If no try get LoginHistory and attach to control
 		if(user == null){
 			AttachLoginHistory();
 		}else{
 			gotoNext();
 		}
 		
 	}
 
 
 	public void gotoRegistActivity(View target)
 	{
 	   myNavigator.swtichTo(Navigator.RegistActivitySEQ);
 	}
 	
 	public void gotoForgetPasswordActivity(View targert)
 	{
 		myNavigator.swtichTo(Navigator.ForgertPasswordSEQ);
 	}
 	
 	public void login(View targert)
 	{
 		//pop a loading dialog and verify by server
 		String number = numberView.getText().toString();
 		String password = passwordView.getText().toString();	
 		user = loginRespository.login(number, password);
 		
 		//if passed then switch to FriendsActivity
 		if(user !=null){
 			gotoNext();
 			return;
 		}else{ //if failed then pop a dialog and clear the password
 			
 			passwordView.setText("");
 		}
 		
 	}
 	
 	private void AttachLoginHistory()
 	{
 		String[] numbers = loginRespository.getHistoryUserNumber();
 		if(numbers == null)
 			return;
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, numbers);
 		numberView.setAdapter(adapter);
 	}
 	
 	private void gotoNext()
 	{
 		myNavigator.swtichTo(Navigator.MainActivitySEQ);
 		this.finish();
 	}
 }
