 package no.group09.utils;
 
 /*
  * Licensed to UbiCollab.org under one or more contributor
  * license agreements.  See the NOTICE file distributed 
  * with this work for additional information regarding
  * copyright ownership. UbiCollab.org licenses this file
  * to you under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import no.group09.ucsoftwarestore.R;
 import no.group09.database.Save;
 import no.group09.database.entity.App;
 import no.group09.database.entity.BinaryFile;
 import no.group09.database.entity.Developer;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 /**
  * The informational view of an app in the shop.
  */
 public class AppView extends Activity {
 
 	private static final String TAG = "AppView";
 	/** The progress bar shown to the user when programming a device */
 	private ProgressDialog progressBar;
 	/** The handler that handles updates to the progress bar window */
 	private ProgressbarHandler progressHandler;
 	private Save save;
 	private Context ctxt;
 	/** 
 	 * The current service holding bluetooth connection and handling updates from
 	 * the programmer 
 	 */
 	private BtArduinoService service;
 	private AlertDialog installDialog;
 	private byte[] byteArray;
 	private AlertDialog.Builder responseDialog;
 	private Activity activityRef;
 	private TextView appName, appDeveloper, appDescription, requirements;
 	private RatingBar rating;
 	private ImageView thumb_image;
 	private App app;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//Set the xml layout
 		setContentView(R.layout.app_view);	
 
 		ctxt = getBaseContext();
 		activityRef = this;
 
 		//Fetch the application ID from the intent
 		int appID = getIntent().getExtras().getInt("app");
 
 		//Get the database
 		save = new Save(getBaseContext());
 
 		responseDialog = new AlertDialog.Builder(this);
 
 		//Fetch the application from the database
 		app = save.getAppByID(appID);
 		Developer developer = save.getDeveloperByID(app.getDeveloperID());
 		BinaryFile binaryfile = save.getBinaryFileByAppID(appID);
 
 		byteArray = prepareHexfile(binaryfile.getBinaryFileAsString());
 
 		//Get the objects from xml
 		appName = (TextView) findViewById(R.id.app_view_app_name);
 		appDeveloper = (TextView) findViewById(R.id.app_view_developer);
 		rating = (RatingBar) findViewById(R.id.ratingBarIndicator);
 		appDescription = (TextView) findViewById(R.id.app_view_description);
 		thumb_image = (ImageView) findViewById(R.id.app_profile_pic);
 		requirements = (TextView) findViewById(R.id.requiredHardwareText);
 		
 
 		//Set the information on the UI that we fetched from the database-objects
 		appName.setText(app.getName());
 		appDeveloper.setText(developer.getName());
 		rating.setRating(app.getRating());
 		appDescription.setText(app.getDescription());
 
 		initializeElements();
 
 		Button installButton = (Button) findViewById(R.id.button1);
 		installButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				installClicked(v);
 			}
 		});
 	}
 	
 	/**
 	 * Method for illustration purposes. This method initializes each element
 	 * with an image when the app view of the selected app is entered. 
 	 */
 	public void initializeElements() {
 		
 		if(app.getCategory().equals("Games")){
 			if (appName.getText().equals("Super Mario Tune")) {
 				thumb_image.setImageResource(R.drawable.supermario);
 				requirements.setText(R.string.supermario);
 				return;
 			}
 			thumb_image.setImageResource(R.drawable.games);
 			requirements.setText(R.string.no_requirements);
 		}
 		else if(app.getCategory().equals("Medical")){
 			thumb_image.setImageResource(R.drawable.medical);
 			requirements.setText(R.string.no_requirements);
 		}
 
 		else if(app.getCategory().equals("Tools")){
 			if (appName.getText().equals("Thermometer Celsius") || 
 					appName.getText().equals("Thermometer Fahrenheit")) {
 				thumb_image.setImageResource(R.drawable.thermometer);
 				requirements.setText(R.string.celcius);
 				return;
 			}
 			thumb_image.setImageResource(R.drawable.tools);
 			requirements.setText(R.string.no_requirements);
 		}
 
 		else if(app.getCategory().equals("Media")){
 			if (appName.getText().equals("Flashing LEDs sequential") || 
 					appName.getText().equals("Flashing LEDs alternative")) {
 				thumb_image.setImageResource(R.drawable.led);
 				requirements.setText(R.string.sequential);
 				return;
 			}
 			thumb_image.setImageResource(R.drawable.media);
 			requirements.setText(R.string.no_requirements);
 		} 
 	}
 
 	/**
 	 * Prepares the chosen hex data for programming.
 	 * 
 	 * @param hexData the hex data that is to be converted to binary file
 	 * @return byte array containing the hex data
 	 */
 	public byte[] prepareHexfile(String hexData) {
 		//Convert string to byte array
 		byte[] binaryFile = new byte[hexData.length() / 2];
 		for(int i=0; i < hexData.length(); i+=2)
 		{
 			binaryFile[i/2] = Integer.decode("0x" + hexData.substring(i, i + 2)).byteValue();
 		}
 		return binaryFile;
 	}
 
 	/**
 	 * Method for handling the click of the install button
 	 *  
 	 * @param view the current view
 	 */
 	public void installClicked(View view){
 
 		//Creates an alertdialog builder
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		//Get the current service
 		service = BtArduinoService.getBtService();
 		
 		if(!Devices.isConnected()){
 			//If no device connected, create popup with that message
 			builder.setMessage("Cannot install app, no device connected")
 			.setPositiveButton("Cancel",new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 
 				}
 				//Take the user directly to the device list to connect to a device
 			}).setNegativeButton("Choose a device",new DialogInterface.OnClickListener(){
 				@Override
 				public void onClick(DialogInterface dialog, int which){
 					startActivity(new Intent(ctxt, Devices.class));
 				}
 			}).show();
 		}
 		else{
 			builder.setMessage("Press install to install this app to " + 
 					getDeviceName() + ".\n\n"+ "Do not turn off Bluetooth or " +
 					"move away from the device.")
 					.setPositiveButton("Install", new DialogInterface.OnClickListener(){
 						@Override
 						public void onClick(DialogInterface dialog, int which){
 							if (service != null) {
 								//Hide the confirmation box
 								installDialog.dismiss();
 								//Initialize the progress bar
 								progressBar = new ProgressDialog(activityRef);
 								progressHandler = new ProgressbarHandler();
 								progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 								progressBar.setCancelable(false);
 								progressBar.setTitle("Installing app to " + getDeviceName());
 								progressBar.setMessage("Working");
 								progressBar.setProgress(0);
 								progressBar.setMax(100);
 								setProgressBarVisibility(true);
 								progressBar.show();
 								//Start programming
 								service.sendData(byteArray, progressHandler);
 							}
 							//No active service
 							else {
 								Log.d(TAG, "Service was null!");
 							}
 						}
 					}).setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
 						@Override
 						public void onClick(DialogInterface dialog, int which){
 
 						}
 					}).show();
 		}
 		installDialog = builder.create();
 	}
 
 	/**
 	 * Method fetches the name of the connected device from the preferences file
 	 * @return String containing the name of the connected device. "no device"
 	 * if no device is stored in preferences.
 	 */
 	private String getDeviceName() {
 		if(Devices.isConnected()){
 			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 			return pref.getString("connected_device_name", "null");
 		}
 		return "no device";
 	}
 
 	/**
 	 * Runnable showin an information box to the user
 	 */
 	class ShowInformationBox implements Runnable {
 
 		/**
 		 * Boolean indicating if the information box is shown because of an 
 		 * error during programming or not. 
 		 */
 		boolean error = false;
 
 		public ShowInformationBox (boolean error) {
 			this.error = error;
 		}
 
 		@Override
 		public void run() {
 			//The information box is shown because of an error occurance during programming
 			if (error) {
 				responseDialog.setMessage("An error was encountered while trying to program your " +
 						"device. Please try to install again.").setPositiveButton
 						("OK", new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog, int which) {
 								//								resultDialog.dismiss();
 							}
 						});
 			}
 			//The programming was successful
 			else {
 				responseDialog.setMessage("You have successfully programmed your" +
 						" device!").setPositiveButton
 				("OK", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {}
 				});
 			}
 			responseDialog.create();
 			responseDialog.show();
 		}
 	}
 	
 	/**
 	 * Handler class handling the progress bar during programming of a device.
 	 */
 	@SuppressLint("HandlerLeak")
 	class ProgressbarHandler extends Handler {
 		
 		public static final int DISMISS_ERROR = 0;
 		public static final int DISMISS_SUCCESS = 1;
 		public static final int DO_NOT_DISMISS = 2;
 
 		@Override
 		public void handleMessage(Message message) {
 			super.handleMessage(message);
 
 			//Set the message for the progress bar
 			String text;
 			if (message.obj instanceof String) {
 				text = (String) message.obj;
 			} else {
 				text = "Unknown";
 			}
 			progressBar.setMessage(text);
 
 			//The progress bar should be updated and dismissed
 			if (message.what == DISMISS_ERROR || message.what == DISMISS_SUCCESS) {
 				//hide it the progress bar
 				progressBar.dismiss();
 				//Make the thread sleep for a short interval to show the message
 				//set above
 				try { Thread.sleep(1500);
 				} catch (Exception e) {}
 
 				if (message.what == DISMISS_ERROR) runOnUiThread(new ShowInformationBox(true));
 				else if (message.what == DISMISS_SUCCESS) runOnUiThread(new ShowInformationBox(false));
 			}
 			//The progress bar should not be dismissed, only updated
 			else if (message.what == DO_NOT_DISMISS) {
 				int progress = message.arg2;
 				//Update progress
 				progressBar.setProgress(progress);
 			}
 
 		}
 	}
 
 	/**
 	 * Creates options menu
 	 */
 	@Override
 	@SuppressLint("NewApi")
 	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.device_menu, menu);
 		return true;
 	}
 
 	/**
 	 * Returns true as long as item corresponds with a proper options action.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		//Start the preferences class
 		case R.id.settings:
 			//Create an intent to start the preferences activity
 			Intent myIntent = new Intent(getApplicationContext(), Preferences.class);
 			this.startActivity(myIntent);
 			return true;
 
 		default : return false;
 		}
 	}
 }
