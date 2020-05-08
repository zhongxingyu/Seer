 package com.vendsy.bartsy.venue;
 
 import java.io.FileNotFoundException;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ActionBar;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources.NotFoundException;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.vendsy.bartsy.venue.dialog.LoginDialog;
 import com.vendsy.bartsy.venue.dialog.LoginDialog.LoginDialogListener;
 import com.vendsy.bartsy.venue.model.Venue;
 import com.vendsy.bartsy.venue.utils.Constants;
 import com.vendsy.bartsy.venue.utils.Utilities;
 import com.vendsy.bartsy.venue.utils.WebServices;
 
 public class VenueProfileActivity extends Activity implements OnClickListener,  LoginDialogListener{
 
 	private static final String TAG = "VenueRegistrationActivity";
 	
 	// Pointers
 	BartsyApplication mApp;
 	
 	// Form elements
 	private EditText locuId, wifiName, wifiPassword,orderTimeOut;
 	private Handler handler = new Handler();
 	
 	// Progress dialog
 	private ProgressDialog progressDialog;
 	private static final int SELECT_PHOTO = 1000;
 
 	private ImageView venueImage;
 
 	private EditText managerUsernameEditText;
 	private EditText managerPasswordEditText;
 	private EditText confirmPasswordEditText;
 
 	private LinearLayout hoursLayout;
 
 	private Venue venue;
 	
 	// Used to check the validity of this activity's input
 	private static final int ACTIVITY_INPUT_VALID	= 0;
 	private static final int ACTIVITY_INPUT_INVALID = 1;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.venue_profile);
 		
 		// Setup Action bar
 		ActionBar actionBar = getActionBar();
 		// Enable the custom view to add login button
 		actionBar.setDisplayShowCustomEnabled(true);
 		// Inflate the view and set as custom view for action bar
 		LayoutInflater inflator = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View customView = inflator.inflate(R.layout.login_actionbar, null);
 		// Set login button click listener
 		Button login = (Button) customView.findViewById(R.id.loginButton);
 		login.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				new LoginDialog(VenueProfileActivity.this).show();
 			}
 		});
 		
//		// Get the activity input 
 		try {
 			venue = loadInput(mApp);
 		} catch (Exception e) {
 			// Invalid input
 			e.printStackTrace();
 			Log.e(TAG, "Invalid input");
//			finish();
			return;
 		}
 
 		LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 		actionBar.setCustomView(customView, layout);
 		
 		// Set up pointers
 		mApp = (BartsyApplication) getApplication();
 		
 		// Try to get all form elements from the XML
 		venueImage = (ImageView)findViewById(R.id.view_profile_venue_image);
 		
 		// Set image for Venue from preferences if it exists
 		Bitmap bitmap = mApp.loadVenueProfileImage();
 		if (bitmap == null) {
 			// No image, use the default one
 			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
 		}
 		venueImage.setImageBitmap(bitmap);
 		venueImage.setTag(bitmap);
 		
 		locuId = (EditText) findViewById(R.id.locuId);
 		wifiName = (EditText) findViewById(R.id.wifiName);
 		wifiPassword = (EditText) findViewById(R.id.wifiPassword);
 		orderTimeOut = (EditText) findViewById(R.id.orderTimeOut);
 		
 		managerUsernameEditText = (EditText) findViewById(R.id.managerUserNameEditText);
 		managerPasswordEditText = (EditText) findViewById(R.id.managerPasswordEditText);
 		confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
 		
 		orderTimeOut = (EditText) findViewById(R.id.orderTimeOut);
 		
 		// Add click listener for venue image
 		venueImage.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Log.v(TAG, "Clicked on image");
 				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
 				photoPickerIntent.setType("image/*");
 				startActivityForResult(photoPickerIntent, SELECT_PHOTO); 
 			}
 		});
 
 		// Setup listeners
 		findViewById(R.id.view_registration_button_submit).setOnClickListener(this);
 		findViewById(R.id.view_registration_button_cancel).setOnClickListener(this);
 		findViewById(R.id.view_registration_wifi_checkbox).setOnClickListener(this);
 		
 		createOpenAndCloseHoursView();
 		
 		setValues();
 	}
 	
 	/**
 	 * Pre-populate fields if there is a venue object already 
 	 */
 	private void setValues(){
 		if(venue==null){
 			return;
 		}
 		
 //		locuId.setText("");
 		wifiName.setText(venue.getWifiName());
 		wifiPassword.setText(venue.getWifiPassword());
 		
 		orderTimeOut.setText(venue.getCancelOrderTime());
 		
 		((EditText) findViewById(R.id.taxRateEdit)).setText(venue.getTotalTaxRate());
 		
 		((EditText) findViewById(R.id.accountNumberEditText)).setText("");
 		((EditText) findViewById(R.id.managerNameEditText)).setText("");
 		((EditText) findViewById(R.id.managerUserNameEditText)).setText("");
 		((EditText) findViewById(R.id.managerPasswordEditText)).setText("");
 		((EditText) findViewById(R.id.vendsyRepNameEditText)).setText("");
 		((EditText) findViewById(R.id.vendsyRepEmailEditText)).setText("");
 		((EditText) findViewById(R.id.vendsyRepPhoneEditText)).setText("");
 		((EditText) findViewById(R.id.locuSectionEditText)).setText("");
 		((EditText) findViewById(R.id.venueNameEditText)).setText("");
 		((EditText) findViewById(R.id.addressEditText)).setText("");
 		((EditText) findViewById(R.id.phoneEditText)).setText("");
 	}
 	
 	/**
 	 * Sets the input of this activity and makes it valid
 	 */
 	public static final void setInput(BartsyApplication context, Venue venue) {
 		Utilities.savePref(context, R.string.VenueProfileActivity_input_status, ACTIVITY_INPUT_VALID);
 		context.mVenueProfileActivityInput = venue;
 	}
 	
 	private Venue loadInput(BartsyApplication context) throws Exception {
 
 		// Make sure the input is valid
 		if (Utilities.loadPref(this, R.string.VenueProfileActivity_input_status, ACTIVITY_INPUT_VALID) != ACTIVITY_INPUT_VALID) {		
 			Log.e(TAG, "Invalid activity input - exiting...");
 			Utilities.removePref(this, R.string.VenueProfileActivity_input_status);
 			throw new Exception();
 		}
 		
 		return mApp.mVenueProfileActivityInput;
 	}
 	
 	/**
 	 * Create open and closed hours view for all the weeks
 	 */
 	private void createOpenAndCloseHoursView() {
 		
 		hoursLayout = (LinearLayout) findViewById(R.id.hoursLayout);
 		
 		final String[] weeks={"M","T","W","T","F","S","S"}; // week names
 		
 		for(int i=0;i<weeks.length;i++){
 			final View view = getLayoutInflater().inflate(R.layout.hours_item, null);
 			// set title for the row
 			TextView titleView = (TextView) view.findViewById(R.id.rowTitleView);
 			titleView.setText(weeks[i]);
 			
 			hoursLayout.addView(view);
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
 	    super.onActivityResult(requestCode, resultCode, data); 
 
 	    switch(requestCode) { 
 	    case SELECT_PHOTO:
 	        if(resultCode == RESULT_OK){  
 	            Uri selectedImage = data.getData();
 				
 				// Down-sample selected image to make sure we don't get exceptions
 				Bitmap bitmap = null;
 				try {
 	            	bitmap = decodeUri(selectedImage);
 				} catch (FileNotFoundException e) {
 					// Failure - don't change the venue image
 					e.printStackTrace();
 					Log.e(TAG, "Failed to downsample image");
 					return;
 				}
 
 				// Display the image and set the tag to the bitmap, indicating it's a valid profile picture.
 				venueImage.setImageBitmap(bitmap);
 				venueImage.setTag(bitmap);
 				mApp.saveVenueProfileImage(bitmap);
 	        }
 	        break;
 	    }
 	}
 	
 	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
 
         // Decode image size
         BitmapFactory.Options o = new BitmapFactory.Options();
         o.inJustDecodeBounds = true;
         BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);
 
         // The new size we want to scale to
         final int REQUIRED_SIZE = 140;
 
         // Find the correct scale value. It should be the power of 2.
         int width_tmp = o.outWidth, height_tmp = o.outHeight;
         int scale = 1;
         while (true) {
             if (width_tmp / 2 < REQUIRED_SIZE
                || height_tmp / 2 < REQUIRED_SIZE) {
                 break;
             }
             width_tmp /= 2;
             height_tmp /= 2;
             scale *= 2;
         }
 
         // Decode with inSampleSize
         BitmapFactory.Options o2 = new BitmapFactory.Options();
         o2.inSampleSize = scale;
         return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
 
     }
 	
 	/**
 	 * Click listener
 	 */
 	
 	@Override
 	public void onClick(View arg0) {
 
 		switch (arg0.getId()) {
 			
 			case R.id.view_registration_button_submit:
 				Log.d("Bartsy", "Clicked on submit button");
 				if(validateVenueInformation()){
 					// Perform registration - for now assume all will go well
 					processRegistration();
 				}
 				break;
 				
 			case R.id.view_registration_button_cancel:
 				finish();
 				break;
 				
 			case R.id.view_registration_wifi_checkbox:
 				
 				if (((CheckBox) findViewById(R.id.view_registration_wifi_checkbox)).isChecked()) {
 					findViewById(R.id.view_registration_wifi).setVisibility(View.VISIBLE);
 				} else {
 					findViewById(R.id.view_registration_wifi).setVisibility(View.GONE);
 				}
 				break;
 		}
 	}
 	
 	/**
 	 * Check all venue information is valid or not
 	 * 
 	 * @return valid or not
 	 */
 	private boolean validateVenueInformation() {
 		
 		boolean valid = true;
 		
 		if(managerUsernameEditText.getText().toString().trim().equals("")){
 			managerUsernameEditText.setError("Please enter username/email");
 			valid = false;
 			
 		}
 		
 		if(managerPasswordEditText.getText().toString().trim().equals("")){
 			managerPasswordEditText.setError("Please enter password");
 			valid = false;
 			
 		}
 		
 		if(confirmPasswordEditText.getText().toString().trim().equals("")){
 			confirmPasswordEditText.setError("Please retype your password");
 			valid = false;
 			
 		}
 		
 		if(!confirmPasswordEditText.getText().toString().equals(managerPasswordEditText.getText().toString())){
 			confirmPasswordEditText.setError("Password does not match");
 			valid = false;
 			
 		}
 		
 		// Make sure wifi fields are completed if the wifi button is checked
 		if (((CheckBox) findViewById(R.id.view_registration_wifi_checkbox)).isChecked()) {
 			if(((TextView) findViewById(R.id.wifiName)).getText().toString().trim().equals("")) {
 				((TextView) findViewById(R.id.wifiName)).setError("Please enter the wifi name");
 				valid = false;
 			}
 			if(((TextView) findViewById(R.id.wifiPassword)).getText().toString().trim().equals("")) {
 				((TextView) findViewById(R.id.wifiPassword)).setError("Please enter the wifi password");
 				valid = false;
 			}
 		}
 		
 		return valid;
 	}
 	/**
 	 * Try to login with bartsy user name and password  
 	 * 
 	 * @param username
 	 * @param password
 	 */
 	public void proceedBartsyLoginSysCall(final String username, final String password){
 		// Start progress dialog from here
 		progressDialog = Utilities.progressDialog(this, "Loading..");
 		progressDialog.show();
 			
 		// Call web service in the background
 		new Thread() {
 			@Override
 			public void run() {
 				try {
 					JSONObject json = new JSONObject();
 					json.put("venueLogin", username);
 					json.put("venuePassword", password);
 					
 					// Post username and password to the server
 					final String response = WebServices.postRequest(WebServices.URL_BARTSY_LOGIN, json, mApp);
 
 					Log.d("Bartsy", "response :: " + response);
 											
 					// Handler to access UI thread
 					handler.post(new Runnable() {
 
 							@Override
 							public void run() {
 									progressDialog.dismiss();
 									// To check response received from the server or not - Error Handling
 									if (response != null) {
 										try {
 											processVenueResponse(new JSONObject(response));
 										} catch (JSONException e) {
 										}
 									}
 								}
 					});
 
 					} catch (Exception e) {
 						Log.d("Venue Reg", "Exception :: " + e);
 					}
 			}
 		}.start();
 				
 	}
 
 	/**
 	 * Validates input then sends to server and starts main activity
 	 */
 	
 	public void processRegistration() {
 		
 		Log.v(TAG, "processRegistration()");
 
 		int selectedTypeOfAuthentication =  ((RadioGroup) findViewById(R.id.typeOfAuthentication)).getCheckedRadioButtonId();
 
 		// Gets a reference to our "selected" radio button
 		RadioButton typeOfAuthentication = (RadioButton) findViewById(selectedTypeOfAuthentication);
 		SharedPreferences settings = getSharedPreferences(GCMIntentService.REG_ID, 0);
 		String deviceToken = settings.getString("RegId", "");
 
 		System.out.println("sumbit");
 		
 		// To check GCM token received or not
 		if (deviceToken.trim().length() > 0) {
 
 			final JSONObject postData = new JSONObject();
 			try {
 				// Prepare registration information in JSON format to the web service
 				postData.put("locuId", locuId.getText().toString());
 				postData.put("deviceToken", deviceToken);
 				postData.put("wifiName", wifiName.getText().toString());
 				postData.put("wifiPassword", wifiPassword.getText().toString());
 				postData.put("typeOfAuthentication",
 						typeOfAuthentication == null ? ""
 								: typeOfAuthentication.getText().toString());
 				postData.put("deviceType", "0");
 				postData.put("cancelOrderTime",orderTimeOut.getText().toString());
 				postData.put("totalTaxRate", ((EditText) findViewById(R.id.taxRateEdit)).getText().toString());
 				postData.put("routingNumber", ((EditText) findViewById(R.id.routingNumberEditText)).getText().toString());
 				postData.put("accountNumber", ((EditText) findViewById(R.id.accountNumberEditText)).getText().toString());
 				postData.put("managerName", ((EditText) findViewById(R.id.managerNameEditText)).getText().toString());
 				postData.put("venueLogin", ((EditText) findViewById(R.id.managerUserNameEditText)).getText().toString());
 				postData.put("venuePassword", ((EditText) findViewById(R.id.managerPasswordEditText)).getText().toString());
 				postData.put("vendsyRepName", ((EditText) findViewById(R.id.vendsyRepNameEditText)).getText().toString());
 				postData.put("vendsyRepEmail", ((EditText) findViewById(R.id.vendsyRepEmailEditText)).getText().toString());
 				postData.put("vendsyRepPhone", ((EditText) findViewById(R.id.vendsyRepPhoneEditText)).getText().toString());
 				postData.put("locuSection", ((EditText) findViewById(R.id.locuSectionEditText)).getText().toString());
 				postData.put("venueName", ((EditText) findViewById(R.id.venueNameEditText)).getText().toString());
 				postData.put("address", ((EditText) findViewById(R.id.addressEditText)).getText().toString());
 				postData.put("phone", ((EditText) findViewById(R.id.phoneEditText)).getText().toString());
 				
 				// Save printer IP address if present
 				String printerIp = ((EditText) findViewById(R.id.printer_ip_address)).getText().toString();
 				if (Utilities.has(printerIp))
 					Utilities.savePref(this, R.string.config_printer_ip, printerIp);
 				else
 					Utilities.removePref(this, R.string.config_printer_ip);
 				
 				if (((CheckBox) findViewById(R.id.view_registration_wifi_checkbox)).isChecked()){
 					postData.put("wifiPresent", "1");
 					
 					if (((RadioButton) findViewById(R.id.wpaNetWorkRadio)).isChecked()){
 						postData.put("wifiNetworkType", "WPA");
 					}else{
 						postData.put("wifiNetworkType", "WEP");
 					}
 				}
 				else
 					postData.put("wifiPresent", "0");
 				
 				postData.put("open_hours", getHoursDataInJSONArray());
 				
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			
 			// Start progress dialog from here
 			progressDialog = Utilities.progressDialog(this, "Loading..");
 			progressDialog.show();
 			
 			// Call web service in the background
 			new Thread() {
 				@Override
 				public void run() {
 						try {
 						// Post venue details to the server
 						// For now it is hard coded for image - null
 						final String response = WebServices.postVenue(WebServices.URL_SAVE_VENUEDETAILS, postData, (Bitmap)venueImage.getTag(), mApp);
 
 						Log.d("Bartsy", "response :: " + response);
 									
 						// Handler to access UI thread
 						handler.post(new Runnable() {
 
 								@Override
 								public void run() {
 									progressDialog.dismiss();
 									// To check response received from the server or not - Error Handling
 									if (response != null) {
 										try {
 											processVenueResponse(new JSONObject(response));
 										} catch (JSONException e) {
 										}
 									}
 								}
 						});
 
 					} catch (Exception e) {
 					Log.d("Venue Reg", "Exception :: " + e);
 				}
 
 			}
 		}.start();
 		
 		} 
 		// To stop sending details to server if the GCM device token is failed
 		else {
 			WebServices.alertbox("Please try again....",
 					VenueProfileActivity.this);
 		}
 	}
 	private JSONObject getHoursDataInJSONArray() {
 		
 		final String[] weeks={"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"}; // week names
 		
 		// Create JSON Object 
 		JSONObject jsonObject = new JSONObject();
 		// Get the open and closed text values from each row
 		for(int i=0;i<hoursLayout.getChildCount();i++){
 			View view = hoursLayout.getChildAt(i);
 		
 			try {
 				JSONArray hoursArray = new JSONArray();
 				
 				hoursArray.put(((TextView)view.findViewById(R.id.openHourText)).getText() +" - "
 				+ ((TextView)view.findViewById(R.id.closeHourText)).getText());
 				
 				jsonObject.put(weeks[i], hoursArray);
 			} catch (JSONException e) {
 			}
 		}
 		
 		return jsonObject;
 	}
 
 	/**
 	 * To parse venue registration response in JSON format
 	 * 
 	 * @param json
 	 */
 	private void processVenueResponse(JSONObject json) {
 		try {
 			int errorCode = Integer.parseInt(json
 					.getString("errorCode"));
 			String errorMessage = json
 					.getString("errorMessage");
 			String venueName = null, venueId = null;
 			Toast.makeText(getApplicationContext(),
 					errorMessage, Toast.LENGTH_LONG)
 					.show();
 			BartsyApplication app;
 			switch (errorCode) {
 			case 1:
 				// venue already exists - still save
 				// the
 				// profile locally for now
 				// venueName = "Chaya Venice";
 				// venueId = "5a0999dda39f9fe07a44";
 			case 0:
 				// Save the venue id in shared preferences
 				venueId = venueId == null ? json.getString("venueId") : venueId;
 				venueName = venueName == null ? json.getString("venueName") : venueName;
 				// To save venue details in the shared preference
 				Utilities.savePref(this, "RegisteredVenueId", venueId);
 				Utilities.savePref(this, "RegisteredVenueName", venueName);				
 				
 				// Start a new venue
 				mApp.venueProfileID = venueId;
 				mApp.venueProfileName = venueName;
 				mApp.update(Constants.shortUpdateDelay);
 				
 				// Remove progress dialog
 				if(progressDialog!=null){
 					progressDialog.dismiss();
 				}
 				
 				// To navigate main page and try to close this screen
 				Intent intent = new Intent(
 						VenueProfileActivity.this,
 						MainActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent);
 				finish();
 				
 				// Upload ingredients data to server in background
 				if(mApp.isIngredientsSaved){
 					new Thread(){
 						public void run() {
 							mApp.uploadDataToServerInBackground();
 						}
 					}.start();
 				}
 			}
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		} catch (NotFoundException e) {
 			e.printStackTrace();
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public void onDialogPositiveClick(LoginDialog dialog) {
 		proceedBartsyLoginSysCall(dialog.username, dialog.password);
 	}
 
 	@Override
 	public void onDialogNegativeClick(LoginDialog dialog) {
 		dialog.dismiss();
 	}
 	
 }
