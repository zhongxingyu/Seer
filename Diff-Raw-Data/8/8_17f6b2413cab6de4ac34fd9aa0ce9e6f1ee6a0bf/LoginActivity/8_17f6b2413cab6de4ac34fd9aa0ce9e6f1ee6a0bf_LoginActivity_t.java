 package ca.ryerson.scs.rus;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import ca.ryerson.scs.rus.adapters.HttpRequestArrayAdapter;
 import ca.ryerson.scs.rus.util.DefaultUser;
 import ca.ryerson.scs.rus.util.IntentRes;
 import ca.ryerson.scs.rus.util.URLResource;
 import ca.ryerson.scs.rus.util.ValidityCheck;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class LoginActivity extends Activity implements LocationListener,
 		OnClickListener {
 	// Flag for debug/log messages
 	public static final boolean DEBUG = true;
 
 	// Tag for debugging
 	public static final String TAG = "RyeSocial";
 
 	private Context context;
 
 	private TextView btnSubmit, btnRegister;
 	private EditText evUsername, evPassword;
 
 	private LocationManager locationManager;
 	private Location locationSend;
 
 	private static final long MIN_TIME = 400;
 	private static final float MIN_DISTANCE = 1000;
 
 	// private static final String JSON_STATUS = "user";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.login);
 
 		context = this;
 
 		btnSubmit = (TextView) findViewById(R.id.BtnSubmit);
 		btnRegister = (TextView) findViewById(R.id.BtnRegister);
 		evUsername = (EditText) findViewById(R.id.EVUsername);
 		evPassword = (EditText) findViewById(R.id.EVPassword);
 
 		btnSubmit.setFocusable(true);
 		btnRegister.setFocusable(true);
 		evUsername.setFocusable(true);
 		evPassword.setFocusable(true);
 
 		btnSubmit.setOnClickListener(this);
 		btnRegister.setOnClickListener(this);
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		locationManager.isProviderEnabled(Context.LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(
 				LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
 
 		// TODO: Implement persistence checking for saved username and password
 		evUsername.setText("Kelvin"); // REMOVE THIS
 		evPassword.setText("Mak");
 	}
 
 	public void onClick(View v) {
 		if (v == btnSubmit) {
 			if (SplashActivity.DEBUG) {
 				Log.i(TAG, "Login Button");
 				Log.i(TAG, evUsername.getText() + " ");
 				Log.i(TAG, evPassword.getText() + " ");
 			}
 
 			if (ValidityCheck.emptyCheck(ValidityCheck
 					.removeWhiteSpace(evUsername.getText().toString()))) {
 
 				final Dialog dialogUser = new Dialog(LoginActivity.this);
 				dialogUser.setContentView(R.layout.dialouge_email);
 				dialogUser.setTitle("Username Error");
 				dialogUser.setCancelable(true);
 
 				// set up text
 				TextView text = (TextView) dialogUser
 						.findViewById(R.id.dia_email);
 				text.setText(R.string.TVErrUsername3);
 
 				// set up button
 				Button button = (Button) dialogUser.findViewById(R.id.Button01);
 				button.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						dialogUser.dismiss();
 					}
 				});
 
 				dialogUser.show();
 			}
 
 			else if (ValidityCheck
 					.emptyCheck((evPassword.getText().toString()))) {
 
 				final Dialog dialog = new Dialog(LoginActivity.this);
 				dialog.setContentView(R.layout.dialouge_email);
 				dialog.setTitle("Password Error");
 				dialog.setCancelable(true);
 
 				// set up text
 				TextView text = (TextView) dialog.findViewById(R.id.dia_email);
 				text.setText(R.string.TVErrPassword2);
 
 				// set up button
 				Button button = (Button) dialog.findViewById(R.id.Button01);
 				button.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						dialog.dismiss();
 						evPassword.setText("");
 					}
 				});
 
 				dialog.show();
 			}
 
 			else {
 				
 				String URLfinal = (ValidityCheck
 						.removeWhiteSpace(URLResource.LOGIN + "?user="
 								+ evUsername.getText().toString()
 								+ "&password="
 								+ evPassword.getText().toString() + "&geoX="
 								+ Double.toString(locationSend.getLatitude()) 
 								//+ Double.toString(43.809319)
 								+ "&geoY="
 								+ Double.toString(locationSend.getLongitude())));
 								//+ Double.toString(-79.268723)));
 
 				System.out.println(URLfinal);
 
 				HttpRequestArrayAdapter.httpRequest(this, URLfinal,
 						new LoginHandler());				
 			}
 
 		} else if (v == btnRegister) {
 			startActivity(new Intent(IntentRes.REGISTER_STRING));
 		}
 
 	}
 
 	private class LoginHandler implements
 			HttpRequestArrayAdapter.ResponseHandler {
 
 		@Override
 		public void postResponse(JSONArray response) {
 
 			try {
 				if (response.length() == 5) {
 
 					JSONObject tempJSON = response.getJSONObject(0);
 					String num_online = tempJSON.getString("num_online_all");
 
 					tempJSON = response.getJSONObject(2);
 					String num_messages = tempJSON.getString("num_messages");
 
					tempJSON = response.getJSONObject(1);
 					String num_friends = tempJSON
							.getString("num_friends");
 
 					tempJSON = response.getJSONObject(4);
 					String email = tempJSON.getString("user_email");
 
 					DefaultUser.setEmail(email);
 
 					Toast.makeText(context, "Successful Login",
 							Toast.LENGTH_LONG).show();
 
 					Intent intent = new Intent(IntentRes.MENU_STRING);
 					intent.putExtra("user", evUsername.getText().toString());
 					intent.putExtra("location", locationSend);
 					intent.putExtra("lookAroundNumber", num_online);
 					intent.putExtra("friendNumber", num_friends);
 					intent.putExtra("messageNumber", num_messages);
 					DefaultUser.setUser(evUsername.getText().toString());
 					DefaultUser.setLatitude(Double.toString(locationSend.getLatitude()));
 					DefaultUser.setLongitude(Double.toString(locationSend.getLongitude()));
					Log.i("LAT",""+Double.toString(locationSend.getLatitude()));
					Log.i("LONG",""+Double.toString(locationSend.getLongitude()));
 					startActivity(intent);
 				}
 
 				else {
 
 					 JSONObject jd0 = response.getJSONObject(0);
 					 String Status = jd0.getString("Status");
 					 Toast.makeText(context, "Login " + Status + "\n" +
 					 "Incorrect username or password",
 					 Toast.LENGTH_LONG).show();
 				}
 			}
 
 			catch (Exception ex) {
 				Log.e("log_tag", "Error getJSONfromURL " + ex.toString());
 				Toast.makeText(context, "Service Currently Unavailable",
 						Toast.LENGTH_LONG).show();
 			}
 
 		}
 
 		@Override
 		public void postTimeout() {
 			// TODO Auto-generated method stub
 		}
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		locationSend = location;
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
