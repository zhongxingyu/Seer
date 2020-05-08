 package org.blanco.lacuenta;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class ContactDeveloperActivity extends Activity
 	implements View.OnClickListener{
 	
 	public static final String CONTACT_DEVELOPER_INTENT_NAME = "org.blanco.lacuenta.CONTACT_DEVELOPER";
 	
 	/**
 	 * @see android.app.Activity#onCreate(Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.contact_developer);
 		init();
 	}
 	
 	public void init(){
 		btnAccept = (Button) findViewById(R.id.contact_developer_activity_btn_ok);
 		btnCancel = (Button) findViewById(R.id.contact_developer_act_btn_cancel);
 		btnCancel.setOnClickListener(this);
 		btnAccept.setOnClickListener(this);
 	}
 	
 	public void sendMail(){
 		final EditText msg = (EditText) findViewById(R.id.contact_developer_act_edt_message);
 		if (msg.getText().length() > 0){
 			
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.contact_developer_act_btn_cancel:
 			finish();
 			break;
 		case R.id.contact_developer_activity_btn_ok:
 			sendMail();
 			break;
 		}
 	}
 	
 	Button btnAccept = null;
 	Button btnCancel = null;
 
 	
 }
