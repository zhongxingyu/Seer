 package org.psywerx.estudent;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class LoginActivity extends Activity {
 
 	private Context mContext;
 	private EditText eUsername;
 	private EditText ePassword;
 	private Button btnConfirm;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.login);
 		mContext = this;
 
 		init();
 	}

 	private void init() {
 		eUsername = (EditText) findViewById(R.id.eUsername);
 		ePassword = (EditText) findViewById(R.id.ePassword);
 		btnConfirm = (Button) findViewById(R.id.btnConfirm);
 		setListeners();
 	}

 	private void setListeners(){
 		eUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 			public void onFocusChange(View v, boolean focus) {
				changedFocus((EditText) v,focus,getString(R.string.edit_text_username),false);
 			}
 		});
 		ePassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 			public void onFocusChange(View v, boolean focus) {
				changedFocus((EditText) v,focus,getString(R.string.edit_text_password),true);
 			}
 		});

 		btnConfirm.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
				if (eUsername.getText().toString().equals(getString(R.string.edit_text_username)) || 
						eUsername.length() == 0 ) {
 					eUsername.setError(getText(R.string.error_required));
 					return;
 				}
				if (ePassword.getText().toString().equals(getString(R.string.edit_text_password)) ||
						ePassword.length() == 0 ) {
 					ePassword.setError(getText(R.string.error_required));
 					return;
 				}
				Toast.makeText(mContext, getText(R.string.error_wrongUP), Toast.LENGTH_SHORT).show();
 			}
 		});

 	}
 
	protected void changedFocus(EditText v, boolean hasFocus, String message, boolean isPassword) {
 		if (hasFocus){
 			if (v.getText().toString().equals(message)){
 				v.setText("");
 				if (isPassword){
 					v.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD | 
 							InputType.TYPE_CLASS_TEXT);
 				}
 				v.setTextColor(getResources().getColor(R.color.black));
 			}
 		}else{
 			if (v.length()==0){
 				v.setText(message);
 				if (isPassword){
 					v.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL | 
 							InputType.TYPE_CLASS_TEXT);
 				}
 				v.setTextColor(getResources().getColor(R.color.gray));
 			}
 		}
 	}
 }
