 package com.wanderphone.minesweep;
 
 import java.util.UUID;
 
 //import com.minesweep.R;
 import com.wanderphone.minesweep.xmlparse.HttpClientConnector;
 import com.wanderphone.minesweep.xmlparse.RegisterReturnMessage;
 import com.wanderphone.minesweep.xmlparse.RegisterReturnMessageParse;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.PorterDuff;
 
 public class RegisterActivity extends Activity {
 
 	private EditText usernameEditText;
 	private Button ok;
 	TextView prompt;
 	String username;
 	String registerUrl;
 	String uniqueId;
 	public void onCreate(Bundle savedInstanceState) {
 		/*取得电话的唯一标示符phone_id*/
 		// 全屏显示
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
         final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);    
         final String tmDevice, tmSerial, androidId;    
         tmDevice = "" + tm.getDeviceId();    
         tmSerial = "" + tm.getSimSerialNumber();    
         androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);    
         UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());    
         uniqueId = deviceUuid.toString();
         
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.register);
 		
 		usernameEditText = (EditText)findViewById(R.id.usernameEditText);
 		ok = (Button)findViewById(R.id.ok);
 		ok.getBackground().setColorFilter(0xFFFF7F00, PorterDuff.Mode.MULTIPLY);
 		
 		ok.setOnClickListener(new Button.OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				username = usernameEditText.getText().toString();
 				if(username.equals("")||username == null)
 				{
 					new AlertDialog.Builder(RegisterActivity.this)
 					.setTitle(R.string.dialog_title)
 					.setMessage(R.string.username_prompt)
 					.setPositiveButton
 					(
 							R.string.bt_sure,
 							new DialogInterface.OnClickListener() 
 							{	
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									// TODO Auto-generated method stub
 									
 								}
 							}
 					).show();
 				}
 				else
 				{
 					registerUrl = getResources().getString(R.string.websit) + "?phone_id=" + uniqueId
						+ "&which_use=2&username=" + username;
 					
 					String registerReturnString = HttpClientConnector.getStringByUrl(registerUrl);
 					RegisterReturnMessage registerReturnMessage = RegisterReturnMessageParse
 																	.parse(registerReturnString);
 					
 					if(registerReturnMessage!=null)
 					{
 						if(registerReturnMessage.getIsSuccess().equals("yes"))
 						{
 							new AlertDialog.Builder(RegisterActivity.this)
 							.setTitle(R.string.prompt)
 							.setMessage(R.string.register_success)
 							.setPositiveButton
 							(
 									R.string.bt_sure,
 									new DialogInterface.OnClickListener() 
 									{	
 										@Override
 										public void onClick(DialogInterface dialog, int which) {
 											// TODO Auto-generated method stub
 											Intent intent = new Intent();
 											intent.setClass(RegisterActivity.this, MainActivity.class);
 											
 											startActivity(intent);
 											RegisterActivity.this.finish();
 										}
 									}
 							).show();
 						}
 						else
 						{
 							
 						}
 					}
 				}
 			}
 		});
 	}
 }
