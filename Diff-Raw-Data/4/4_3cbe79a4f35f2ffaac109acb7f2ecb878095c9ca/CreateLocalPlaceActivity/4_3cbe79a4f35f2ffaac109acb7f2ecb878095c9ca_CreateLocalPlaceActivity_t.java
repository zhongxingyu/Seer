 package fr.insalyon.pyp.gui.events;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.LinearLayout;
import android.widget.ScrollView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import fr.insalyon.pyp.R;
 import fr.insalyon.pyp.gui.common.BaseActivity;
 import fr.insalyon.pyp.gui.common.IntentHelper;
 import fr.insalyon.pyp.gui.common.popup.Popups;
 import fr.insalyon.pyp.network.ServerConnection;
 import fr.insalyon.pyp.tools.AppTools;
 import fr.insalyon.pyp.tools.Constants;
 import fr.insalyon.pyp.tools.PYPContext;
 import fr.insalyon.pyp.tools.TerminalInfo;
 
 public class CreateLocalPlaceActivity extends BaseActivity {
 			private LinearLayout abstractView;
 			private ScrollView mainView;
 			private TextView windowTitle;
 			
 			private TextView LocalPlaceName;
 			private TextView AddressLocalPlace;
 			private TextView DescriptionLocalPlace;
 			private Spinner TypeLocalPlace;
 			
 			private Button NextStepBtn;
 			private String event_id;
 			private String place_id;
 			
 			@Override
 			public void onCreate(Bundle savedInstanceState) {
 				super.onCreate(savedInstanceState, Constants.CREAT_LOCAL_PLACE_CONST);
 				AppTools.info("on create CreateEvent");
 				initGraphicalInterface();
 			}
 
 			private void initGraphicalInterface() {
 				// set layouts
 				event_id = IntentHelper.getActiveIntentParam(String[].class)[0];
 				LayoutInflater mInflater = LayoutInflater.from(this);
 				abstractView = (LinearLayout) findViewById(R.id.abstractLinearLayout);
				mainView = (ScrollView) mInflater.inflate(R.layout.create_local_place_activity,
 						null);
 				abstractView.addView(mainView);
 				
 				windowTitle = (TextView) findViewById(R.id.pageTitle);
 				windowTitle.setText(R.string.CreateLocalPlace);
 				
 				hideHeader(false);
 				
 				LocalPlaceName = (TextView) findViewById(R.id.LocalPlaceName);
 				AddressLocalPlace = (TextView) findViewById(R.id.AddressLocalPlace);
 
 				// Get the address from the current location
 				new GetCurrentAddressTask().execute();
 
 				DescriptionLocalPlace = (TextView) findViewById(R.id.DescriptionLocalPlace);
 				TypeLocalPlace = (Spinner) findViewById(R.id.TypeLocalPlace);
 				// Create an ArrayAdapter using the string array and a default spinner
 				// layout
 				ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 						this, R.array.place_type_array, android.R.layout.simple_spinner_item);
 				// Specify the layout to use when the list of choices appears
 				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 				// Apply the adapter to the spinner
 				TypeLocalPlace.setAdapter(adapter);
 				
 				NextStepBtn = (Button) findViewById(R.id.NextStepBtn);
 				
 				NextStepBtn.setOnClickListener( new OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						if( !"".equals(LocalPlaceName.getText().toString()) &&
 							!"".equals(DescriptionLocalPlace.getText().toString())	)
 						new CreateLocalPlaceTask().execute();
 					}
 				});
 				
 			}
 			
 			@Override
 			public void onActivityResult(int requestCode, int resultCode, Intent data) {
 				AppTools.error("Autocompleating...");
 			    if (resultCode == Activity.RESULT_OK) {
 			    	AppTools.error("Executing get request");
 			    	String[] row_values = data.getExtras().getStringArray(Constants.PARAMNAME);
 			    	event_id = row_values[0];
 			    	place_id = row_values[1];
 			    	new GetLocalPlaceTask().execute();
 			    }
 			}
 
 
 			@Override
 			public void onResume() {
 				super.onResume();
 				//check if logged in
 				checkLoggedIn();
 			}
 			
 			public void networkError(String error) {
 				if (error.equals("Incomplete data")) {
 					Popups.showPopup(Constants.IncompleatData);
 				}
 				if (error.equals("Object does not exists")) {
 					AppTools.log("You should never see this!", Level.WARNING);
 				}
 			}
 
 			private class CreateLocalPlaceTask extends AsyncTask<Void, Void, Void> {
 
 				ProgressDialog mProgressDialog;
 				JSONObject res;
 
 				@Override
 				protected void onPostExecute(Void result) {
 					mProgressDialog.dismiss();
 					if (res != null) {
 						try {
 							if (res.has("error")) {
 								// Error
 								String error;
 								error = res.getString("error");
 								CreateLocalPlaceActivity.this.networkError(error);
 							}
 
 							else {
 								// OK
 								String id = res.getString("id");
 								String[] params = new String[1];
 								params[0] = id;
 								place_id = id;
 								new SelectPlaceTask().execute((Object[])params);
 							}
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 				@Override
 				protected void onPreExecute() {
 					mProgressDialog = ProgressDialog.show(CreateLocalPlaceActivity.this,
 							getString(R.string.app_name), getString(R.string.loading));
 				}
 
 				@Override
 				protected Void doInBackground(Void... params) {
 					// Send request to server for login
 
 					ServerConnection srvCon = ServerConnection.GetServerConnection();
 					List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 					
 					parameters.add(new BasicNameValuePair("name", LocalPlaceName
 							.getText().toString()));
 					parameters.add(new BasicNameValuePair("type", TypeLocalPlace
 							.getSelectedItem().toString()));
 					parameters.add(new BasicNameValuePair("description", DescriptionLocalPlace
 							.getText().toString()));
 					parameters.add(new BasicNameValuePair("auth_token", PYPContext.getContext().getSharedPreferences(AppTools.PREFS_NAME, 0).getString("auth_token", "")));
 					if(place_id!=null){
 						parameters.add(new BasicNameValuePair("id", place_id));
 					}
 					Location l = TerminalInfo.getPosition();
 					String latitude = String.valueOf(l.getLatitude());
 					String longitude = String.valueOf(l.getLongitude());
 					parameters.add(new BasicNameValuePair("latitude", latitude));
 					parameters.add(new BasicNameValuePair("longitude", longitude));
 					
 					try {
 						res = srvCon.connect(ServerConnection.ADD_LOCAL_PLACE, parameters);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					return null;
 				}
 			}
 
 			
 			private class GetCurrentAddressTask extends AsyncTask<Void, Void, Void> {
 
 				ProgressDialog mProgressDialog;
 				JSONObject res;
 
 				@Override
 				protected void onPostExecute(Void result) {
 					mProgressDialog.dismiss();
 					if (res != null) {
 						try {
 							if (res.has("error")) {
 								// Error
 								String error;
 								error = res.getString("error");
 								CreateLocalPlaceActivity.this.networkError(error);
 							}
 
 							else {
 								// OK
 								String address = res.getString("address");
 								AddressLocalPlace.setText(address);
 							}
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 				@Override
 				protected void onPreExecute() {
 					mProgressDialog = ProgressDialog.show(CreateLocalPlaceActivity.this,
 							getString(R.string.app_name), getString(R.string.loading));
 				}
 
 				@Override
 				protected Void doInBackground(Void... params) {
 					// Send request to server for login
 					ServerConnection srvCon = ServerConnection.GetServerConnection();
 					List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 					Location l = TerminalInfo.getPosition();
 					String latitude = String.valueOf(l.getLatitude());
 					String longitude =  String.valueOf(l.getLongitude());
 					parameters.add(new BasicNameValuePair("latitude", latitude));
 					parameters.add(new BasicNameValuePair("longitude", longitude));
 					parameters.add(new BasicNameValuePair("auth_token", PYPContext.getContext().getSharedPreferences(AppTools.PREFS_NAME, 0).getString("auth_token", "")));
 
 					try {
 						res = srvCon.connect(ServerConnection.GET_CURRENT_ADDRESS, parameters);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					return null;
 				}
 			}
 				
 				
 				private class SelectPlaceTask extends AsyncTask<Object, Void, Void> {
 
 					ProgressDialog mProgressDialog;
 					JSONObject res;
 
 					@Override
 					protected void onPostExecute(Void result) {
 						mProgressDialog.dismiss();
 						if (res != null) {
 							try {
 								if (res.has("error")) {
 									// Error
 									String error;
 									error = res.getString("error");
 									CreateLocalPlaceActivity.this.networkError(error);
 								}
 
 								else {
 									// OK
 									// Get id of the object
 									String[] params = new String[2];
 									params[0] = event_id;
 									params[1] = place_id;
 									Intent i =  new Intent(CreateLocalPlaceActivity.this, IntrestActivity.class);
 									i.putExtra(Constants.PARAMNAME,params);
 									startActivityForResult(i,1);
 
 								}
 							} catch (JSONException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 
 					@Override
 					protected void onPreExecute() {
 						mProgressDialog = ProgressDialog.show(CreateLocalPlaceActivity.this,
 								getString(R.string.app_name), getString(R.string.loading));
 					}
 
 					@Override
 					protected Void doInBackground(Object... params) {
 						// Send request to server for login
 
 						ServerConnection srvCon = ServerConnection.GetServerConnection();
 						List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 						// Get the token & the username						
 						parameters.add(new BasicNameValuePair("event_id", event_id));
 						parameters.add(new BasicNameValuePair("place_id", (String) params[0]));
 						parameters.add(new BasicNameValuePair("is_local", "True"));
 						parameters.add(new BasicNameValuePair("auth_token", PYPContext.getContext().getSharedPreferences(AppTools.PREFS_NAME, 0).getString("auth_token", "")));
 						
 						
 						try {
 							res = srvCon.connect(ServerConnection.SAVE_EVENT_PLACE, parameters);
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 						return null;
 					}
 				}
 				
 			
 			
 			private class GetLocalPlaceTask extends AsyncTask<Object, Void, Void> {
 
 				ProgressDialog mProgressDialog;
 				JSONObject res;
 
 				@Override
 				protected void onPostExecute(Void result) {
 					mProgressDialog.dismiss();
 					if (res != null) {
 						try {
 							if (res.has("error")) {
 								// Error
 								String error;
 								error = res.getString("error");
 								CreateLocalPlaceActivity.this.networkError(error);
 							}
 
 							else {
 								// OK
 								// Get id of the object
 								LocalPlaceName.setText(res.getString("name"));
 								// TODO: verify this
 								ArrayAdapter myAdap = (ArrayAdapter) TypeLocalPlace.getAdapter();
 								int spinnerPosition = myAdap.getPosition(res.getString("type"));
 								TypeLocalPlace.setSelection(spinnerPosition);
 								DescriptionLocalPlace.setText(res.getString("description"));
 								AddressLocalPlace.setText(res.getString("address"));
 							}
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 				@Override
 				protected void onPreExecute() {
 					mProgressDialog = ProgressDialog.show(CreateLocalPlaceActivity.this,
 							getString(R.string.app_name), getString(R.string.loading));
 				}
 
 				@Override
 				protected Void doInBackground(Object... params) {
 					// Send request to server for login
 
 					ServerConnection srvCon = ServerConnection.GetServerConnection();
 					List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 					// Get the token & the id				
 					parameters.add(new BasicNameValuePair("id",place_id));
 					parameters.add(new BasicNameValuePair("auth_token", PYPContext.getContext().getSharedPreferences(AppTools.PREFS_NAME, 0).getString("auth_token", "")));
 					
 					
 					try {
 						res = srvCon.connect(ServerConnection.GET_LOCAL_PLACE, parameters);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					return null;
 				}
 			}
 			
 			
 }
