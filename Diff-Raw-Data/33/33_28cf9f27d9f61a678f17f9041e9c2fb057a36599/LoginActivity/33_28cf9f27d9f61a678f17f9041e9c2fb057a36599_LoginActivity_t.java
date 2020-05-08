 package com.qrsphere.login;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 
 import com.qrsphere.R;
 import com.qrsphere.database.QrcodeDataOperator;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 @SuppressLint("HandlerLeak")
 public class LoginActivity extends Activity {
 	
 	Button btLater;
 	Button btLogin;
 	EditText etName,etAccount, etPassword;
 	boolean isLoginOK = false;
 	CheckBox checkbox;
 	
 	private ProgressDialog pd;
 	Thread thread = null;
 	@SuppressLint("HandlerLeak")
 	private Handler handler = new Handler() {
 	    public void handleMessage(Message msg) {
 	        pd.dismiss();
 	        if (msg.what == 11) {
 				if (isLoginOK){
 					LoginActivity.this.finish();
					startActivity(new Intent("com.qrsphere.MainViewActivity"));
 				}
 				else{
 					Toast.makeText(getBaseContext(), "Account dosen't exist or password is incorrect.", Toast.LENGTH_SHORT).show();
 				}
 	        	
 	        } else if (msg.what == 0) {
 
 	        } else {
 	            showNetworkAlert();
 	        }
 	    }
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.login);
 		
 
 		checkbox = (CheckBox) findViewById(R.id.checkBox1);
 		etName = (EditText) findViewById(R.id.nameEdit);
 		etAccount = (EditText) findViewById(R.id.accountEdit);
 		etPassword= (EditText) findViewById(R.id.passwordEdit);
 		
 		btLater = (Button) this.findViewById(R.id.btn_later);
 		btLater.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				//startActivity(new Intent("com.example.clienttest.login.RegisterActivity"));
 				
 			}
 		});
 		
 
 		
 		
 		btLogin = (Button) this.findViewById(R.id.btn_login);
 		btLogin.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 			try {	
 				String name = etName.getText().toString();
 				String account = etAccount.getText().toString();
 				String password = etPassword.getText().toString();
 				
 				JSONObject json = new JSONObject();  
                 json.put("Name", name);  
                 json.put("Account", account);  
                 json.put("Password", password); 
                 
 //                String result = client.doPost(mServerBaseUrl + SET_DATA,   
 //                        json.toString(), "application/json"); 
                 
 
 				
 			    if (isOnline(LoginActivity.this)) {
 			        pd = ProgressDialog.show(LoginActivity.this, "", "Login to server...", true,
 			                false);
 			        new Thread(new Runnable() {
 
 			            @Override
 			            public void run() {
 			                try {
 			    				String name = etName.getText().toString();
 			    				String account = etAccount.getText().toString();
 			    				String password = etPassword.getText().toString();
 			    				
 			    				JSONObject json = new JSONObject();  
 			                    json.put("Name", name);  
 			                    json.put("Account", account);  
 			                    json.put("Password", password); 
 			    				isLoginOK = LoginProcess.Login(json);
 			    				Log.i("LoginPostResult",isLoginOK+"");
 			    				
 			    				checkLoginTickBox(json);
 			    				
 			    				Thread.sleep(5000);
 			    				
 			    				//strResponse = res;
 
 			                    handler.sendEmptyMessage(11);
 			                } catch (Exception e) {
 			                    System.out.println("In Cache :");
 			                    handler.sendEmptyMessage(1);
 			                }
 			            }
 			        }).start();
 			    } else {
 			        showNetworkAlert();
 			    }
 				
 //				if (res.compareTo("No result!")!=0){
 //					finish();
 //					startActivity(new Intent("com.example.clienttest.MainActivity"));
 //				}
 //				else{
 //					Toast.makeText(getBaseContext(), "Wrong password.", Toast.LENGTH_SHORT).show();
 //				}
 
 //				finishActivity(getParent());
 				//getParent().finish();
 			}
 			catch(Exception e)
 			{
 				Log.i("LoginPost",e.toString());
 			}
 				
 			}
 		});
 	}
 	public static Boolean isOnline(Context context) {
 	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
 	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 	        return true;
 	    }
 	    return false;
 	}
 	public void showNetworkAlert() {
 	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	    builder.setMessage("Please check your internet connection.")
 	            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 	                public void onClick(DialogInterface dialog, int id) {
 	                    dialog.dismiss();
 	                }
 	            });
 
 	    builder.setTitle("Connection Problem");
 	    builder.show();
 
 	}
 
 	protected boolean checkLoginTickBox(JSONObject loginInfo){
 		QrcodeDataOperator qdo = new QrcodeDataOperator(this);
 		String mac = new com.qrsphere.userinfo.CollectPhoneInformation(getApplication()).getMacAddress();
 		if (checkbox.isChecked()){
 			//QrcodeDataOperator qdo = new QrcodeDataOperator(this);
 			
 			qdo.storeUserInfo(mac,loginInfo.toString());
 		}
 		else{
 			try {
 				String name = loginInfo.getString("Name");
 				String account = loginInfo.getString("Account");
 				
 				JSONObject json = new JSONObject();  
                 json.put("Name", name);  
                 json.put("Account", account);
                 json.put("Password", "");
     			//QrcodeDataOperator qdo = new QrcodeDataOperator(this);
     	    	qdo.storeUserInfo(mac,json.toString());
 				
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		return true;
 	}
 
 }
