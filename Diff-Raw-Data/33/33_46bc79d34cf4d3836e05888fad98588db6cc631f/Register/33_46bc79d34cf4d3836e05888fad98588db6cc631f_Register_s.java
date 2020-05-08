 package coupling.app;
 
 import java.io.IOException;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.gcm.GoogleCloudMessaging;
 import com.nit.coupling.R;
 
 import coupling.app.com.API;
 import coupling.app.com.Constants;
 import coupling.app.com.ITask;
 import coupling.app.com.Request;
 import coupling.app.com.Response;
 import coupling.app.com.User;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class Register extends Activity{
 
 	static final String TAG = Register.class.getName();
 
 	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
 
 	private String SENDER_ID = "69286130837";
 
 	GoogleCloudMessaging gcm;
 	AtomicInteger msgId = new AtomicInteger();
 	String regid;
 
 	Context context;
 
 	EditText etPnumber;
 	EditText etFirstName;
 	EditText etLastName;
 
 	private User owner;
 
 	ITask tasker;
 	Thread thread;
	Activity activity;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		activity = this;
 		//If user not register with server & google
 		if(getPrefs().getBoolean(Constants.IS_REGISTERED, false)){
 			App.loadOwner(getPrefs());
 			startActivity(new Intent(this, Main.class));
 		} else {
 			insertItemsSQLThread();
 			
 			setContentView(R.layout.settings);
 
 			context = getApplicationContext();
 
 			etPnumber = (EditText) findViewById(R.id.etPnumber);
 			etFirstName = (EditText) findViewById(R.id.etFirstName);
 			etLastName = (EditText) findViewById(R.id.etLastName);
 
 			Button btnRegister = (Button) findViewById(R.id.register);
 
 			btnRegister.setOnClickListener(registerListener());
 
 			if (!checkPlayServices())
 				Log.i(TAG, "No valid Google Play Services APK found.");
 
 			owner = App.getOwner();
 			getDeviceNumber();
 
 			tasker = new ITask() {			
 
 				@Override
 				public void onTaskComplete(Request request, Response response) {
 				}
 
 			};
 		}
 	}
 
 	public void getDeviceNumber(){
 		TelephonyManager tManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
 		String pNumber = tManager.getLine1Number();
 		if (pNumber != null && pNumber.length() > 0){
 			Utils.shopToast("Suggested Device Phone Number Added");
 			
 			etPnumber.setText(pNumber);
 
 		} else {
 			Utils.shopToast("Device Phone Number Not Found");
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		checkPlayServices();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	private void insertItemsSQLThread(){
 
 		thread = new Thread(){
 
 			@Override
 			public void run() {
 				GroceryList gl = GroceryList.getInstance();
				gl.initGroceryListItems(activity);
				Log.w("ILAN DEBUG", "RUN");
 				super.run();
 			}
 
 		};
 		thread.start();
 	}
 
 	private OnClickListener registerListener(){
 		return new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {	
 				if (checkPlayServices()) {
 					owner.setEmail(etPnumber.getText().toString());
 					owner.setFirstname(etFirstName.getText().toString());
 					owner.setLastname(etLastName.getText().toString());
 					if(owner.getEmail().isEmpty())
 						Utils.shopToast("Please fill email");
 					else if(owner.getFirstname().isEmpty())
 						Utils.shopToast("Please fill firstname");
 					else if(owner.getLastname().isEmpty())
 						Utils.shopToast("Please fill lastname");
 					else{
 						SharedPreferences prefs =  getSharedPreferences(Register.class.getSimpleName(),
 								Context.MODE_PRIVATE);
 						SharedPreferences.Editor editor = prefs.edit();
 						editor.putString(Constants.EMAIL, owner.getEmail());
 						editor.putString(Constants.FIRSTNAME, owner.getFirstname());
 						editor.putString(Constants.LASTNAME, owner.getLastname());
 						editor.commit();
 
 						gcm = GoogleCloudMessaging.getInstance(Register.this);
 						regid = getRegistrationId();
 						if (regid == "") {
 							registerInBackground();
 						}else{
 							Utils.shopToast("Error: there is regid already!");
 						}
 					}
 				}
 
 			}
 		};
 	}
 
 	private boolean checkPlayServices() {
 		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
 		if (resultCode != ConnectionResult.SUCCESS) {
 			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
 				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
 						PLAY_SERVICES_RESOLUTION_REQUEST).show();
 			} else {
 				Log.i(TAG, "This device is not supported.");
 				finish();
 			}
 			return false;
 		}
 		return true;
 	}
 
 	private String getRegistrationId() {
 		String registrationId = getPrefs().getString(Constants.REG_ID, "");
 		if (registrationId == "") {
 			Log.i(TAG, "Registration not found.");
 			return "";
 		}
 		// Check if app was updated; if so, it must clear the registration ID
 		// since the existing regID is not guaranteed to work with the new
 		// app version.
 		int registeredVersion = getPrefs().getInt(Constants.APP_VERSION, Integer.MIN_VALUE);
 		int currentVersion = Utils.getAppVersion(context);
 		if (registeredVersion != currentVersion) {
 			Log.i(TAG, "App version changed.");
 			return "";
 		}
 		return registrationId;
 	}
 
 
 	private void registerInBackground() {
 		new AsyncTask<Void, Void, String>() {
 
 			@Override
 			protected void onPreExecute() {
 				setProgressBarIndeterminateVisibility(true);
 			}
 
 			@Override
 			protected String doInBackground(Void... params) {
 				String msg = "";
 				try {
 					if (gcm == null) 
 						gcm = GoogleCloudMessaging.getInstance(context);
 
 					regid = gcm.register(SENDER_ID);
 					storeRegistrationId(context, regid);
 					API.getInstance().registerUser();
 
 					msg = "Device registered";
 				} catch (IOException ex) {
 					msg = "Error :" + ex.getMessage();
 				}
 				return msg;
 			}
 
 			@Override
 			protected void onPostExecute(String msg) {
 				setProgressBarIndeterminateVisibility(false);
 				if(msg.contentEquals("Device registered")){
 					SharedPreferences prefs =  getSharedPreferences(Register.class.getSimpleName(),
 							Context.MODE_PRIVATE);
 					SharedPreferences.Editor editor = prefs.edit();
 					editor.putString(Constants.REG_ID, regid);
 					editor.putBoolean(Constants.IS_REGISTERED, true);
 					editor.commit();
 					startActivity(new Intent(Register.this, Main.class));
 				}
 				Utils.shopToast(msg);
 			}
 		}.execute(null, null, null);
 	}
 
 	/**
 	 * Stores the registration ID and the app versionCode in the application's
 	 * {@code SharedPreferences}.
 	 *
 	 * @param context application's context.
 	 * @param regId registration ID
 	 */
 	private void storeRegistrationId(Context context, String regId) {
 		int appVersion = Utils.getAppVersion(context);
 		Log.i(TAG, "Saving regId on app version " + appVersion);
 		SharedPreferences prefs =  getSharedPreferences(Register.class.getSimpleName(),
 				Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putString(Constants.REG_ID, regId);
 		editor.putInt(Constants.APP_VERSION, appVersion);
 		editor.commit();
 		owner.setRegid(regId);
 	}
 
 	private SharedPreferences getPrefs(){
 		return getSharedPreferences(Register.class.getSimpleName(),
 				Context.MODE_PRIVATE);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu items for use in the action bar
 		//MenuInflater inflater = getMenuInflater();
 		//inflater.inflate(R.menu.shop_list_menu, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/*
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			//NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return false;	
 	}
 	 */
 
 }
