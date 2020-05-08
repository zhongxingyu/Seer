 package com.horeca;
 
 import android.app.Activity;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class SignInActivity extends MyActivity {
 	private EditText mot_de_passe_txt = null;
 	private TextView password_error = null;
 	private EditText mail_txt = null;
 	private TextView mail_error = null;
 	private Button button = null;
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_signin);
 	    mot_de_passe_txt = (EditText) findViewById(R.id.mot_de_passe);
 	    password_error = (TextView) findViewById(R.id.password_error);
 	    mail_txt = (EditText) findViewById(R.id.mail);
 	    mail_error = (TextView) findViewById(R.id.mail_error);
 	    button = (Button) findViewById(R.id.sign_in_button);
 	    button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View arg0) {
         		// Open the db
         		MySqliteHelper sqliteHelper = new MySqliteHelper(SignInActivity.this);
         		SQLiteDatabase db = sqliteHelper.getReadableDatabase();
         		int err = User.signIn(db, mail_txt.getText().toString(), mot_de_passe_txt.getText().toString());
         		if (err == User.INVALID_EMAIL) {
         			mail_error.setVisibility(View.VISIBLE);
         			password_error.setVisibility(View.GONE);
         		} else if (err == User.INVALID_PASSWORD) {
         			mail_error.setVisibility(View.GONE);
         			password_error.setVisibility(View.VISIBLE);
         		} else {
         			mail_error.setVisibility(View.GONE);
         			password_error.setVisibility(View.GONE);
         			finish();
         		}
             }
         });
 	}
 }
