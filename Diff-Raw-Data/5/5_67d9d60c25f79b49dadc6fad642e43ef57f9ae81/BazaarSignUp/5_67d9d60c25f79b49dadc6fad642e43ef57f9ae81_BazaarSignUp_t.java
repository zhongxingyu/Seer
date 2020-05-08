 /**
  * BazaarSignUp, part of Aptoide
  * Copyright (C) 2011 Duarte Silveira
  * duarte.silveira@caixamagica.pt
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 package pt.caixamagica.aptoide.appsbackup;
 
 import pt.caixamagica.aptoide.appsbackup.data.AIDLAptoideServiceData;
 import pt.caixamagica.aptoide.appsbackup.data.AptoideServiceData;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewListIds;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumServerLoginCreateStatus;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewServerLogin;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.Toast;
 
 
 /**
  * BazaarSignUp, handles Aptoide's Bazaar signup interface (by activity)
  * 
  * @author dsilveira
  *
  */
 public class BazaarSignUp extends Activity {
 	
 	private AIDLAptoideServiceData serviceDataCaller = null;
 
 	private boolean serviceDataIsBound = false;
 
 	private ServiceConnection serviceDataConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the object we can use to
 			// interact with the service.  We are communicating with the
 			// service using AIDL, so here we set the remote service interface.
 			serviceDataCaller = AIDLAptoideServiceData.Stub.asInterface(service);
 			serviceDataIsBound = true;
 			
 			Log.v("Aptoide-SignUp", "Connected to ServiceData");
 			
 			try {
 				serviceDataCaller.callRegisterLoginObserver(serviceDataCallback);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 	        			
 			handleSignUp();
 			
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			// This is called when the connection with the service has been
 			// unexpectedly disconnected -- that is, its process crashed.
 			serviceDataIsBound = false;
 			serviceDataCaller = null;
 			
 			Log.v("Aptoide-SignUp", "Disconnected from ServiceData");
 		}
 	};	
 	
 	private AIDLLogin.Stub serviceDataCallback = new AIDLLogin.Stub() {
 
 		@Override
 		public void repoInserted() throws RemoteException {
 			interfaceTasksHandler.sendEmptyMessage(EnumLoginInterfaceTasks.REPO_INSERTED.ordinal());	
 		}
 	};
 	
 	private Handler interfaceTasksHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
         	EnumLoginInterfaceTasks task = EnumLoginInterfaceTasks.reverseOrdinal(msg.what);
         	switch (task) {
 				case REPO_INSERTED:
 					dialogProgress.dismiss();
 					break;
 					
 				default:
 					break;
 				}
         }
 	};
 	
 	
 	private ProgressDialog dialogProgress;	
 	private boolean success;	
 	private LoginState loginState;
 	
 	private ViewServerLogin serverLogin;
 
 	private boolean afterAction;
 	private ViewListIds actionListIds;
 	private EnumAppsLists actionType;
 //	private InvoqueType invoqueType;
 	
 	public static enum LoginState{
 		WAITING,
 		SUCCESS,
 		BAD_LOGIN,
 		REPO_NOT_FROM_USER
 	}
 	
 //	public static enum InvoqueType{ 
 //		CREDENTIALS_FAILED,
 //		NO_CREDENTIALS_SET,
 //		OVERRIDE_CREDENTIALS
 //	}
 	
 	private EditText username;
 	private EditText password;
 //	private CheckBox showPass;
 	
 	private EditText repository;
 	
 	private RadioButton privt;
 	
 // 	private CheckBox privt;
 // 	private TextView priv_username_id;
 // 	private EditText priv_username;
 // 	private TextView priv_password_id;
 // 	private EditText priv_password;
 //	private CheckBox priv_showPass;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.form_signup);
 
 		Bundle extras = getIntent().getExtras();
		if(extras != null && extras.containsKey("uploads")){
 			afterAction = true;
 			this.actionListIds = (ViewListIds) getIntent().getIntegerArrayListExtra("uploads");
 			this.actionType = EnumAppsLists.BACKUP;
		}else if(extras != null && extras.containsKey("restores")){
 			afterAction = true;			
 			this.actionListIds = (ViewListIds) getIntent().getIntegerArrayListExtra("restores");
 			this.actionType = EnumAppsLists.RESTORE;
 		}else{
 			afterAction = false;
 		}
 		
 //		this.invoqueType = ;	//TODO receive as Intent extra integer encoded
 		loginState = LoginState.WAITING;
 		success = false;
 		
 		if(!serviceDataIsBound){
     		bindService(new Intent(this, AptoideServiceData.class), serviceDataConnection, Context.BIND_AUTO_CREATE);
     	}
 		
 		super.onCreate(savedInstanceState);
 	}
 
 	
 	private void handleSignUp(){
 		
  		username = ((EditText)findViewById(R.id.username));
  		password = ((EditText)findViewById(R.id.password));
  //		showPass = (CheckBox) findViewById(R.id.show_password);
  //		showPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
  //            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
  //                if(isChecked) {
  //                    password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
  //                } else {
  //                    password.setInputType(129);
  ////                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
  //                }
  //            }
  //        });
  		
  		repository = ((EditText)findViewById(R.id.repository));
  		
  		privt = (RadioButton) findViewById(R.id.private_store);
  		
 // 		privt = (CheckBox) findViewById(R.id.privt_store);
 // 		privt.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 // 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 // 				if(isChecked){
 // 					priv_username.setEnabled(true);
 // 					priv_password.setEnabled(true);
 // //					priv_showPass.setEnabled(true);
 // 					priv_username_id.setVisibility(View.VISIBLE);
 // 					priv_username.setVisibility(View.VISIBLE);
 // 					priv_password_id.setVisibility(View.VISIBLE);
 // 					priv_password.setVisibility(View.VISIBLE);
 // //					priv_showPass.setVisibility(View.VISIBLE);
 // 				}else{
 // 					priv_username.setEnabled(false);
 // 					priv_password.setEnabled(false);
 // //					priv_showPass.setEnabled(false);
 // 					priv_username_id.setVisibility(View.GONE);
 // 					priv_username.setVisibility(View.GONE);
 // 					priv_password.setVisibility(View.GONE);
 // 					priv_password_id.setVisibility(View.GONE);
 // //					priv_showPass.setVisibility(View.GONE);
 // 				}
 // 			}
 // 		});
 // 		priv_username_id = (TextView) findViewById(R.id.priv_username_id);
 // 		priv_username = ((EditText)findViewById(R.id.priv_username));
 // 		priv_username.setEnabled(false);
 // 		priv_password_id = (TextView) findViewById(R.id.priv_password_id);
 // 		priv_password = ((EditText)findViewById(R.id.priv_password));
 // 		priv_password.setEnabled(false);
  //		priv_showPass = (CheckBox) findViewById(R.id.priv_show_password);
  //		priv_showPass.setOnCheckedChangeListener(new OnCheckedChangeListener() {
  //            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
  //                if(isChecked) {
  //                	priv_password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
  //                } else {
  //                	priv_password.setInputType(129);
  ////                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD );
  //                }
  //            }
  //        });
  //		priv_showPass.setEnabled(false);
  		
  		
  		((Button)findViewById(R.id.sign_up)).setOnClickListener(new View.OnClickListener(){
  			public void onClick(View arg) {
  				success = false;
  				if(username.getText().toString().trim().equals("")){
  					Toast.makeText(BazaarSignUp.this, BazaarSignUp.this.getString(R.string.no_username), Toast.LENGTH_SHORT).show();
  				}else if(password.getText().toString().trim().equals("")){
  					Toast.makeText(BazaarSignUp.this, BazaarSignUp.this.getString(R.string.no_password), Toast.LENGTH_SHORT).show();
  				}else if(repository.getText().toString().trim().equals("")){
  					Toast.makeText(BazaarSignUp.this, BazaarSignUp.this.getString(R.string.no_repository), Toast.LENGTH_SHORT).show();
  				}
 // 				else if(privt.isChecked() && priv_username.getText().toString().trim().equals("")){
 // 					Toast.makeText(Login.this, Login.this.getString(R.string.no_private_repo_username), Toast.LENGTH_SHORT).show();
 // 				}else if(privt.isChecked() && priv_password.getText().toString().trim().equals("")){
 // 					Toast.makeText(Login.this, Login.this.getString(R.string.no_private_repo_password), Toast.LENGTH_SHORT).show();
 // 				}
  				else{
  					
  					serverLogin =  new ViewServerLogin(username.getText().toString(), password.getText().toString());
  					if(serverLogin.getPasshash() == null){
  						serverLogin =  new ViewServerLogin(username.getText().toString(), password.getText().toString());
  					}
  					serverLogin.setRepoName(repository.getText().toString());
  					if(privt.isChecked()){
 // 						serverLogin.setRepoPrivate(priv_username.getText().toString(), priv_password.getText().toString());
  						serverLogin.setRepoPrivate(serverLogin.getUsername(), serverLogin.getPasshash());
  					}
  					
  //					(new DialogName(Login.this)).show();
  					
  					dialogProgress = ProgressDialog.show(BazaarSignUp.this, BazaarSignUp.this.getString(R.string.new_account), BazaarSignUp.this.getString(R.string.please_wait),true);
  					dialogProgress.setIcon(android.R.drawable.ic_menu_add);
  					dialogProgress.setCancelable(true);
  					dialogProgress.setOnDismissListener(new OnDismissListener(){
  						public void onDismiss(DialogInterface arg0) {
  								if(success){
  									Log.d("Aptoide-Login", "New User Created");Log.d("Aptoide-Login", "Logged in");
  									if(afterAction){
  										switch (actionType) {
 											case BACKUP:
 												Intent upload = new Intent(BazaarSignUp.this, Upload.class);
 												upload.putIntegerArrayListExtra("uploads", actionListIds);
 												startActivity(upload);
 												break;
 												
 											case RESTORE:
 												for (Integer appHashid : actionListIds) {
 													try {
 														serviceDataCaller.callInstallApp(appHashid);
 													} catch (RemoteException e) {
 														e.printStackTrace();
 													}
 												}
 												break;
 	
 											default:
 												break;
 										}
  									}
  									finish();
  								}else{
  									
  								}
  						}
  					});
  					
  					Log.d("Aptoide-Login", "Creating new acocunt with login: "+serverLogin);
  					new CreateAccountTask(BazaarSignUp.this, serverLogin).execute();
  				}
  			}
  		});
  		
  		
  		
 // 		if(invoqueType.equals(InvoqueType.OVERRIDE_CREDENTIALS)){
 // 			ViewServerLogin serverLogin = null;
 // 			try {
 // 				serverLogin = serviceDataCaller.callGetServerLogin();
 // 			} catch (RemoteException e) {
 // 				e.printStackTrace();
 // 			}
 // 			if(serverLogin != null){
 // 				username.setText(serverLogin.getUsername());
 //				repository.setText(serverLogin.getRepoName());
 //// 				if(serverLogin.isRepoPrivate()){
 //// 					privt.setChecked(true);
 //// 					priv_username.setText(serverLogin.getPrivUsername());
 //// 					priv_password.setText(serverLogin.getPrivPassword());
 //// 				}
 // 			}
 // 		}
 		
 	}
 
 	
 //	@Override
 //	public boolean onKeyDown(int keyCode, KeyEvent event) {
 //		if (keyCode == KeyEvent.KEYCODE_BACK && !success) {
 //			Log.d("Aptoide-BazaarSignUp", "back press ignored");
 //			return true;
 //		}
 //		return super.onKeyDown(keyCode, event);
 //	}
 
 
 	@Override
 	public void finish() {
 		if(serviceDataIsBound){
 			unbindService(serviceDataConnection);
 		}
 		super.finish();
 	}
 
 	
 	public class CreateAccountTask extends AsyncTask<Void, Void, EnumServerLoginCreateStatus>{
 		
 		private Context context;
 		private ViewServerLogin serverLogin;
 		
 		public CreateAccountTask(Context context, ViewServerLogin serverLogin) {
 			this.context = context;
 			this.serverLogin = serverLogin;
 		}
 		
 		@Override
 		protected EnumServerLoginCreateStatus doInBackground(Void... args) {
 			try {
 				return EnumServerLoginCreateStatus.reverseOrdinal(serviceDataCaller.callServerLoginCreate(serverLogin));
 			} catch (RemoteException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 		
 		protected void onPostExecute(EnumServerLoginCreateStatus status) {
 			if(status!=null){
 
 				if(status.equals(EnumServerLoginCreateStatus.SUCCESS)){
 					success = true;
 					dialogProgress.setCancelable(false);
 				}else{
 					success = false;
 					Toast.makeText(BazaarSignUp.this, status.toString(BazaarSignUp.this), Toast.LENGTH_SHORT).show();
 				}
 				
 			}else{
 				Toast.makeText(BazaarSignUp.this, getString(R.string.login_create_service_unavailable), Toast.LENGTH_SHORT).show();
 				dialogProgress.dismiss();
 			}
 	    }
 		
 	}
 	
 }
