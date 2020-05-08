 package com.catalyst.android.birdapp;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.text.method.ScrollingMovementMethod;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.catalyst.android.birdapp.database.DatabaseHandler;
 import com.catalyst.android.birdapp.GPS_Utility.GPSUtility;
 import com.catalyst.android.birdapp.utilities.AlertDialogFragment;
 import com.catalyst.android.birdapp.utilities.FormValidationUtilities;
 import com.catalyst.android.birdapp.utilities.OnDialogDoneListener;
 import com.catalyst.android.birdapp.utilities.Utilities;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
 public class BirdFormActivity extends Activity implements OnDialogDoneListener {
 
 	private static final int FIVE_MINUTES = 300000;
 
 	public static final String LOGTAG = "DialogFrag";
 
 	public Vibrator vibrator;
 
 	private Spinner categorySpinner;
 	private Spinner activitySpinner;
 	private GPSUtility gpsUtility;
 	private TextView latitudeEditText, longitudeEditText;
 
 	private EditText commonNameEditText;
 	private EditText scientificNameEditText;
 	private EditText notesEditText;
 	private TextView dateTextView;
 	private TextView timeEditText;
 	private Button coordinateRefreshButton;
 	private Timer coordinateRefreshTimer;
 	private String sep;
 	private String blk;
 	private String or;
 	private int numOfMissingFields;
 	private StringBuilder sb = new StringBuilder();
 	private String missFields;
 	private Utilities util = new Utilities();
 	private List<String> userDefinedFields = new ArrayList<String>();
 	private List<String> missingFieldTitles = new ArrayList<String>();
 	private FormValidationUtilities fvd = new FormValidationUtilities();
 
 	long coordinateTimerStart;
 	long coordinateTimerCurrent;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.getWindow().setSoftInputMode(
 				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		setContentView(R.layout.activity_bird_form);
 		// Checks to see if the device has google play services.
 		GooglePlayServicesUtil
 				.isGooglePlayServicesAvailable(getApplicationContext());
 		// Sets up the GPS Utility class
 		gpsUtility = new GPSUtility(this);
 		categorySpinner = (Spinner) findViewById(R.id.category_drop_down);
 		activitySpinner = (Spinner) findViewById(R.id.bird_acivity_dropdown);
 		notesEditText = (EditText) findViewById(R.id.notes_edit_text);
 		notesEditText.setMovementMethod(ScrollingMovementMethod.getInstance());
 		displayDateAndTime();
 
 		// Grabs the fields needed for gps autofill
 		intializeGPSfields();
 		commonNameEditText = (EditText) findViewById(R.id.common_name_edit_text);
 		scientificNameEditText = (EditText) findViewById(R.id.scientific_name_edit_text);
 		dateTextView = (TextView) findViewById(R.id.date_time_edit_text);
 		timeEditText = (TextView) findViewById(R.id.hour_edit_text);
 		commonNameEditText = (EditText) findViewById(R.id.common_name_edit_text);
 		scientificNameEditText = (EditText) findViewById(R.id.scientific_name_edit_text);
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		gpsUtility.removeFormLocationUpdates();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		displayDateAndTime();
 		fillActivitySpinner();
 		fillCategorySpinner();
 		gpsUtility.setFormLocationListener();
 	}
 
 	/**
 	 * Fills the activity spinner with values from the DB
 	 */
 	private void fillActivitySpinner() {
 		DatabaseHandler dbHandler = DatabaseHandler.getInstance(this);
 		ArrayList<String> activitiesFromDB = dbHandler.getAllActivities();
 		ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinner_item,
 				R.id.spinnertextview, activitiesFromDB);
 		activitySpinner.setAdapter(adapter);
 	}
 
 	/**
 	 * Fills the category spinner with values from the DB
 	 */
 	private void fillCategorySpinner() {
 		DatabaseHandler dbHandler = DatabaseHandler.getInstance(this);
 		ArrayList<String> categoriesFromDB = dbHandler.getAllCategories();
 		ArrayAdapter adapter = new ArrayAdapter(this, R.layout.spinner_item,
 				R.id.spinnertextview, categoriesFromDB);
 		categorySpinner.setAdapter(adapter);
 	}
 
 	/**
 	 * Initializes the GPS coordinates fields and sets the oncheck changed
 	 * listener
 	 */
 	private void intializeGPSfields() {
 		// Grabs the edit texts fields from the page so that they can be edited
 		latitudeEditText = (TextView) findViewById(R.id.latitude_edit_text);
 		longitudeEditText = (TextView) findViewById(R.id.longitude_edit_text);
 
 		// Sets ups the coordinate refresh button and timer
 		coordinateRefreshButton = (Button) findViewById(R.id.refresh_button);
 		coordinateRefreshButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				refreshCoordinateTimer();
 
 			}
 
 		});
 		// Sets up the time that will be used to change the color of the
 		// coordinate refresh button from green to red and back
 		coordinateRefreshTimer = new Timer();
 		// Checks to see if the GPS is on
 		gpsUtility.checkForGPS();
 
 		autoFillCoordinatesSubmitForm();
 
 		gpsUtility.setFormLocationListener();
 
 		// Sets the coordinate timer numbers so that they can later be used to
 		// keep the user from continually pressing the coordinate
 		// refresh button and creating a new timer each time
 		coordinateTimerStart = 0;
 		coordinateTimerCurrent = 0;
 
 	}
 
 	/**
 	 * Refreshes the coordinate timer
 	 */
 	private void refreshCoordinateTimer() {
 		coordinateTimerCurrent = System.currentTimeMillis();
 
 		setCoordinateButtonToGreen();
 
 		// Auto fills the coordinate boxes
 		autoFillCoordinatesSubmitForm();
 
 		// Checks to see if it has been 5 minutes since the button was last
 		// clicked
 		if (coordinateTimerCurrent >= (coordinateTimerStart + FIVE_MINUTES)
 				|| coordinateTimerStart == 0) {
 			// sets the start time for the timer
 			coordinateTimerStart = System.currentTimeMillis();
 
 			// Sets the timer
 			coordinateRefreshTimer.schedule(new TimerTask() {
 
 				@Override
 				public void run() {
 					// Has to run on UI thread
 					runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							setCoordinateButtonToRed();
 						}
 
 					});
 				}
 				// Sets the duration before activating the thread.
 			}, FIVE_MINUTES);
 		}
 	}
 
 	/**
 	 * Sets the coordinate button to red and changes the text
 	 */
 	private void setCoordinateButtonToGreen() {
 		coordinateRefreshButton.setBackgroundColor(Color.GREEN);
 		coordinateRefreshButton
 				.setText(getString(R.string.coordinates_up_to_date));
 
 	}
 
 	/**
 	 * Sets the coordinate button to red and changes the text
 	 */
 	private void setCoordinateButtonToRed() {
 		coordinateRefreshButton.setBackgroundColor(Color.RED);
 		coordinateRefreshButton
 				.setText(getString(R.string.coordinateOutOfDate));
 
 	}
 
 	/**
 	 * Autofills the coordinates and sets the location listener
 	 */
 	private void autoFillCoordinatesSubmitForm() {
 
 		Location location = gpsUtility.getCurrentLocation();
 		// Auto fills the form
 		if (location != null) {
 			latitudeEditText.setText(Double.toString(location.getLatitude()));
 			longitudeEditText.setText(Double.toString(location.getLongitude()));
 		} else {
 			coordinateRefreshButton.setBackgroundColor(Color.RED);
 			coordinateRefreshButton.setText(getString(R.string.coordinates_not_available));
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.bird_form, menu);
 		return true;
 	}
 
 	/**
 	 * Takes the user to the google map
 	 */
 	public void getMap(MenuItem item) {
 		// Takes the user to the Map Activity
 		Intent intent = new Intent(getApplication(), MapActivity.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * Auto populates the date and time fields of the form
 	 * 
 	 * @author mhowell
 	 */
 	private void displayDateAndTime() {
 		TextView date = (TextView) findViewById(R.id.date_time_edit_text);
 		TextView time = (TextView) findViewById(R.id.hour_edit_text);
 		date.setText(util.formatDate(util.currentMillis()));
 		time.setText(util.formatTime(util.currentMillis()));
 	}
 
 	public void submitBirdSighting() {
 		int errors = 0;
 		BirdSighting birdSighting = new BirdSighting();
 		FormValidationUtilities fvd = new FormValidationUtilities();
 		String commonNameField = commonNameEditText.getText().toString();
 		// If user has provided input validate for proper content
 		if (!commonNameField.isEmpty()
 				&& !fvd.isFieldValueFormattedAlphaOnly(commonNameField)) {
 			commonNameEditText
 					.setError(getString(R.string.bird_name_alpha_error));
 			errors++;
 		}
 		String scientificNameField = scientificNameEditText.getText()
 				.toString();
 		// If user has provided input validate for proper content
 		if (!scientificNameField.isEmpty()
 				&& !fvd.isFieldValueFormattedAlphaOnly(scientificNameField)) {
 			scientificNameEditText
 					.setError(getString(R.string.scientific_name_alpha_error));
 			errors++;
 		}
 		if (errors == 0) {
 			String longitudeField = longitudeEditText.getText().toString();
 			String latitudeField = latitudeEditText.getText().toString();
 			String notesField = notesEditText.getText().toString();
 			String categoryField = categorySpinner.getSelectedItem().toString();
 			String activityField = activitySpinner.getSelectedItem().toString();
 			String dateField = dateTextView.getText().toString();
 			String timeField = timeEditText.getText().toString();
 
 			// create Date object from date/time fields
 			String dateTimeString = dateField + " " + timeField;
 			Utilities util = new Utilities();
 			Date dateTime = util.getDateObject(dateTimeString);
 
 			// Set values in BirdSighting object
 			birdSighting.setCommonName(commonNameField);
 			birdSighting.setScientificName(scientificNameField);
 			birdSighting.setNotes(notesField);
 			birdSighting.setActivity(activityField);
 			birdSighting.setCategory(categoryField);
 			birdSighting.setDateTime(dateTime);
 
 			// Check formatting, set field to null if wrong format
 			try {
 				birdSighting.setLatitude(Double.parseDouble(latitudeField));
 			} catch (NumberFormatException e) {
 				birdSighting.setLatitude(null);
 			}
 			try {
 				birdSighting.setLongitude(Double.parseDouble(longitudeField));
 			} catch (NumberFormatException e) {
 				birdSighting.setLongitude(null);
 			}
 			DatabaseHandler dbHandler = DatabaseHandler.getInstance(this);
 			dbHandler.insertBirdSighting(birdSighting);
 			Toast.makeText(this, getString(R.string.sightingAddedBlankName),
 					Toast.LENGTH_SHORT).show();
 			vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 			vibrator.vibrate(1000);
 			refreshActivity();
 		}
 	}
 
 	/**
 	 * pulls up AddNewActivity view
 	 */
 	public void getAddNewActivity(MenuItem menuItem) {
 		Intent intent = new Intent(getApplication(), AddNewActivity.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * Refreshes the current activity
 	 * 
 	 * @author mhowell
 	 */
 	private void refreshActivity() {
 		Intent i = getIntent();
 		finish();
 		startActivity(i);
 	}
 
 	/**
 	 * Called when user taps 'Submit' button. Checks for missing user defined
 	 * fields and alerts user if blank
 	 * 
 	 * @author mhowell
 	 * 
 	 * @param view
 	 */
 	public void checkFieldsAndNotifyUserOfBlanksBeforeSubmission(View view) {
 		userDefinedFields.clear();
 		missingFieldTitles.clear();
 		userDefinedFields.add(commonNameEditText.getText().toString());
 		userDefinedFields.add(scientificNameEditText.getText().toString());
 		userDefinedFields.add(notesEditText.getText().toString());
 		missingFieldTitles = fvd.validateBirdFormFields(userDefinedFields);
 		if (missingFieldTitles.size() > 0) {
 			submitAlertDialog(missingFieldTitles);
 		} else {
 			submitBirdSighting();
 		}
 	}
 
 	/**
 	 * Launches the confirmation dialog, provides applicable string relating to
 	 * fields where no user input has been supplied
 	 * 
 	 * @author mhowell
 	 * 
 	 * @param missingFieldsTitles
 	 */
 	public void submitAlertDialog(List<String> missingFieldTitles) {
 		sep = ", ";
 		blk = " ";
 		or = getString(R.string.or);
 		sb.setLength(0);
 		numOfMissingFields = missingFieldTitles.size();
 		switch (numOfMissingFields) {
 		case 1:
 			sb.append(blk).append(missingFieldTitles.get(0));
 			break;
 		case 2:
			sb.append(blk).append(missingFieldTitles.get(0)).append(or)
 					.append(missingFieldTitles.get(1));
 			break;
 		case 3:
 			sb.append(blk).append(missingFieldTitles.get(0)).append(sep)
					.append(missingFieldTitles.get(1)).append(sep).append(or)
 					.append(missingFieldTitles.get(2));
 			break;
 		default:
 			sb.append(getString(R.string.invalid));
 			break;
 		}
 		missFields = sb.toString();
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		AlertDialogFragment adf = AlertDialogFragment
 				.newInstance(getString(R.string.emptyFieldsWarning)
 						+ missFields + "?");
 		adf.show(ft, missFields);
 	}
 
 	/**
 	 * Listens for what the user chose and initiates callback method if the
 	 * response was positive.
 	 * 
 	 * @author mhowell
 	 */
 	public void onDialogDone(String tag, boolean cancelled, CharSequence message) {
 		if (!cancelled) {
 			submitBirdSighting();
 		}
 	}
 
 	public void openCamera(MenuItem menuItem) {
 		Intent intent = new Intent(getApplication(), CameraActivity.class);
 		startActivity(intent);
 	}
 	public void getCameraSettings(MenuItem menuItem){
 		Intent intent = new Intent(getApplication(), CameraSettingsActivity.class);
 		startActivity(intent);
 	}
 }
