 package com.horeca;
import android.app.Activity;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
import android.widget.TextView;
 
public class SignUpActivity extends MyActivity{
 	private Button button = null;
 	private EditText email = null;
 	private EditText name = null;
 	private EditText password = null;
 	private EditText passwordConfirmation = null;
	SQLiteDatabase db = null;
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		MySqliteHelper sqliteHelper = new MySqliteHelper(this);
		db = sqliteHelper.getReadableDatabase();
 		email = (EditText) findViewById(R.id.mail);
 		name = (EditText) findViewById(R.id.name);
 		password = (EditText) findViewById(R.id.mot_de_passe);
 		passwordConfirmation = (EditText) findViewById(R.id.password_confirmation);
 	    setContentView(R.layout.activity_signup);
 	    button = (Button) findViewById(R.id.sign_up_button);
 	    button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View arg0) {
             	String new_name = name.getText().toString();
             	String new_email = email.getText().toString();
             	String new_psw = password.getText().toString();
             	String new_pswC = passwordConfirmation.getText().toString();
             	
         		User.signUp(db, new_email, new_name, new_psw, new_pswC);
        		}
         });
 	}
 }
