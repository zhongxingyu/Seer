 package jp.mulodo.demoapput;
 
 import jp.mulodo.demoapput.controller.UserController;
 import jp.mulodo.demoapput.database.DataBase;
 import jp.mulodo.demoapput.error.ErrorBase;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class LoginActivity extends FragmentActivity implements OnClickListener{
 
 	private Button btn_Login; 
 	private EditText ed_UserName,ed_Password;
 	private TextView tv_Error;
 	
 	@Override
 	protected void onCreate(Bundle arg0) {
 		// TODO Auto-generated method stub
 		setContentView(R.layout.activity_login);
 		super.onCreate(arg0);
 		initView();
 		initEvent();
 		DataBase dataBase = new DataBase();
 		dataBase.initData();
 	}
 	
 	/**
 	 * Init View 
 	 */
 	public void initView()
 	{
 		btn_Login = (Button)findViewById(R.id.btnLogin);
 		ed_Password = (EditText)findViewById(R.id.edtPassword);
 		ed_UserName = (EditText)findViewById(R.id.edtUsername);
 		tv_Error = (TextView)findViewById(R.id.tv_error);
 	}
 	
 	/**
 	 * Init Event
 	 */
 	public void initEvent()
 	{
 		btn_Login.setOnClickListener(this);
 	}
 	
 
	/**
	 * Login
	 */
 	public void login()
 	{
 		String name = ed_UserName.getText().toString();
 		String pass = ed_Password.getText().toString();
 		try {
 			if(UserController.login(name, pass))
 				finish();
 		} catch (ErrorBase e) {
 			tv_Error.setText(e.ErrorException().getErrorMessage());
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		if (v == btn_Login)
 		{
 			login();
 		}
 	}
 
 	
 }
