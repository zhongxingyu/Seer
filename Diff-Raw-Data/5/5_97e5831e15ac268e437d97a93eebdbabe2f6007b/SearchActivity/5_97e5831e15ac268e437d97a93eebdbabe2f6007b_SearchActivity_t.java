 package com.HuskySoft.metrobike.ui;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.Html;
 import android.text.TextWatcher;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.HuskySoft.metrobike.R;
 import com.HuskySoft.metrobike.backend.DirectionsRequest;
 import com.HuskySoft.metrobike.backend.DirectionsStatus;
 import com.HuskySoft.metrobike.backend.TravelMode;
 import com.HuskySoft.metrobike.ui.utility.History;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 
 /**
  * An activity that receives and handles users' requests for routes.
  * 
  * @author Shuo Wang, Sam Wilson
  */
 public class SearchActivity extends Activity implements
         GooglePlayServicesClient.ConnectionCallbacks,
         GooglePlayServicesClient.OnConnectionFailedListener {
 
     /**
      * The tag of this class.
      */
     private static final String TAG = "SearchActivity";
 
     /**
      * A Fragment that contains a date picker to let user select a date of
      * departure/arrival.
      */
     public static class DatePickerFragment extends DialogFragment implements
             DatePickerDialog.OnDateSetListener {
 
         @Override
         public final Dialog onCreateDialog(final Bundle savedInstanceState) {
             // Use the current date as the default date in the picker
             final Calendar calendar = Calendar.getInstance();
             int year = calendar.get(Calendar.YEAR);
             int month = calendar.get(Calendar.MONTH);
             int day = calendar.get(Calendar.DAY_OF_MONTH);
 
             // Create a new instance of DatePickerDialog and return it
             DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, year, month, day);
             // Set the name of the dialog box
             dpd.setTitle(R.string.dialogbox_date);
             Log.v(TAG, "Done creating the calendar dialog");
             return dpd;
         }
 
         /**
          * Update the date EditText widget after an user picks a date.
          * 
          * @param view
          *            The DatePicker view whose date is set by user
          * @param year
          *            The year of the date picked
          * @param month
          *            The month of the date picked (1-12 as Jan. to Dec.)
          * @param day
          *            The day of month of the date picked
          */
         public final void onDateSet(final DatePicker view, final int year, final int month,
                 final int day) {
             EditText dateEditText = (EditText) getActivity().findViewById(R.id.editTextDate);
             // Update the date EditText widget
             dateEditText.setText(com.HuskySoft.metrobike.ui.utility.Utility
                     .convertAndroidSystemDateToFormatedDateString(year, month, day));
 
             Locale currLocale = getResources().getConfiguration().locale;
 
             // Date format in China
             if (currLocale.getLanguage().equals("zh")) {
                 dateEditText.setText(com.HuskySoft.metrobike.ui.utility.Utility
                         .convertAndroidSystemDateToFormatedDateStringChineseConvention(year, month,
                                 day));
             } else { // Date format in US
                 dateEditText.setText(com.HuskySoft.metrobike.ui.utility.Utility
                         .convertAndroidSystemDateToFormatedDateString(year, month, day));
             }
             Log.v(TAG, "Done on data set");
         }
     }
 
     /**
      * A Fragment that contains a time picker to let user select a time (hh:mm)
      * of departure/arrival.
      */
     public static class TimePickerFragment extends DialogFragment implements
             TimePickerDialog.OnTimeSetListener {
 
         @Override
         public final Dialog onCreateDialog(final Bundle savedInstanceState) {
             // Use the current time as the default values for the picker
             final Calendar calendar = Calendar.getInstance();
             int hour = calendar.get(Calendar.HOUR_OF_DAY);
             int minute = calendar.get(Calendar.MINUTE);
 
             // Create a new instance of TimePickerDialog and return it
             TimePickerDialog tpd = new TimePickerDialog(getActivity(), this, hour, minute, true);
             // Set the name of the dialog box
             tpd.setTitle(R.string.dialogbox_time);
             Log.v(TAG, "Done on creating the calender Time dialog");
             return tpd;
         }
 
         /**
          * Update the date EditText widget after an user picks a date.
          * 
          * @param view
          *            The TimePicker view whose date is set by user
          * @param hourOfDay
          *            The hour of a day of the time picked
          * @param minute
          *            The minute of the time picked
          */
         public final void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
             EditText timeEditText = (EditText) getActivity().findViewById(R.id.editTextTime);
             // Update the time EditText widget
             timeEditText.setText(com.HuskySoft.metrobike.ui.utility.Utility
                     .convertAndroidSystemTimeToFormatedTimeString(hourOfDay, minute));
         }
     }
 
     /**
      * An inner class that generates a request for routes and allows the backend
      * request to run on its own thread.
      * 
      * @author dutchscout, Shuo Wang (modification)
      */
     private class DirThread implements Runnable {
 
         /**
          * Fourth digit in a date/time.
          */
         private static final int DIGIT_FOURTH = 3;
 
         /**
          * Sixth digit in a date/time.
          */
         private static final int DIGIT_FIFTH = 4;
 
         /**
          * Fifth digit in a date/time.
          */
         private static final int DIGIT_SIXTH = 5;
 
         /**
          * Seventh digit in a date/time.
          */
         private static final int DIGIT_SEVENTH = 6;
 
         /**
          * Eighth digit in a date/time.
          */
         private static final int DIGIT_EIGHTH = 7;
 
         /**
          * Ninth digit in a date/time.
          */
         private static final int DIGIT_NINTH = 8;
 
         /**
          * Eleventh digit in a date/time.
          */
         private static final int DIGIT_ELEVENTH = 10;
 
         /**
          * 1 second = 1000 milliseconds.
          */
         private static final int SEC_TO_MILLISEC = 1000;
 
         /**
          * 1 mile = 1609.34 meters.
          */
         private static final double MILE_TO_METER = 1609.34;
 
         /**
          * Time to sleep after we get directions data, so user can still cancel.
          */
         private static final long MIN_QUERY_TIME = 500;
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void run() {
             // Generate a direction request
             DirectionsRequest dReq = new DirectionsRequest();
             // Set up addresses for direction request
             String currLocationLatLagString = "";
             if (!fromAutoCompleteTextView.isEnabled() || !toAutoCompleteTextView.isEnabled()) {
                 // defensive programming, avoid null pointer exception and leak
                 // window
                 if (locationClient != null && locationClient.isConnected()
                         && locationClient.getLastLocation() != null) {
                     currLocationLatLagString += locationClient.getLastLocation().getLatitude()
                             + ", " + locationClient.getLastLocation().getLongitude();
                 } else {
                     // Must call runOnUiThread if want to display a Toast or a
                     // Dialog within a thread
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             showErrorDialog(SearchActivity.this.getResources().getString(
                                     R.string.error_gps_not_available));
                         }
                     });
                     if (pd != null) {
                         pd.dismiss();
                     }
                     return;
                 }
             }
             String from = "";
             String to = "";
             if (fromAutoCompleteTextView.isEnabled()) {
                 from = fromAutoCompleteTextView.getText().toString();
             } else {
                 from = currLocationLatLagString;
             }
             if (toAutoCompleteTextView.isEnabled()) {
                 to = toAutoCompleteTextView.getText().toString();
             } else {
                 to = currLocationLatLagString;
             }
             Log.d(TAG, "src address: " + from + " dest address: " + to);
             dReq.setStartAddress(from).setEndAddress(to);
 
             // Set up travel mode for direction request
             if (bicycleOnlyCheckBox.isChecked()) {
                 tm = TravelMode.BICYCLING;
             } else {
                 tm = TravelMode.MIXED;
             }
             dReq.setTravelMode(tm);
             Log.d(TAG, "Done setting the travel mode: " + tm);
             long timeToSend = timeDirectionRequest();
 
             // Set up time mode
             if (arriveAtButton.isChecked()) {
                 dReq.setArrivalTime(timeToSend);
             } else {
                 dReq.setDepartureTime(timeToSend);
             }
 
             // Set up biking distance
             // Note: Use round instead of floor or ceil converting miles into
             // meters
             if (minBikingDistanceEditText.getText().length() != 0) {
                 dReq.setMinDistanceToBikeInMeters(Math.round(Integer
                         .parseInt(minBikingDistanceEditText.getText().toString()) * MILE_TO_METER));
             }
 
             if (maxBikingDistanceEditText.getText().length() != 0) {
                 dReq.setMaxDistanceToBikeInMeters(Math.round(Integer
                         .parseInt(maxBikingDistanceEditText.getText().toString()) * MILE_TO_METER));
             }
 
             // Set up number of buses
             if (!bicycleOnlyCheckBox.isChecked()) {
                 if (minNumBusesEditText.getText().length() != 0) {
                     dReq.setMinNumberBusTransfers(Integer.parseInt(minNumBusesEditText.getText()
                             .toString()));
                 }
 
                 if (maxNumBusesEditText.getText().length() != 0) {
                     dReq.setMaxNumberBusTransfers(Integer.parseInt(maxNumBusesEditText.getText()
                             .toString()));
                 }
             }
 
             // Do Request
             DirectionsStatus retVal = dReq.doRequest();
             Log.d(TAG, "Finish the do request");
 
             try {
                 Thread.sleep(MIN_QUERY_TIME);
             } catch (InterruptedException e) {
                 if (retVal != DirectionsStatus.USER_CANCELLED_REQUEST) {
                     Log.w(TAG, "backend didn't catch thread interrupt.");
                 }
                 retVal = DirectionsStatus.USER_CANCELLED_REQUEST;
             }
             
             // Bottom line for cancellation
             if (cancelled) {
                 retVal = DirectionsStatus.USER_CANCELLED_REQUEST;
                 cancelled = false;
             }
             
             // If a cancellation happens to the direction request
             // display an AlertDialog to let user to (re)start
             // a new request
             if (retVal == DirectionsStatus.USER_CANCELLED_REQUEST) {
                 Log.d(TAG, "Request has been successfully canceled");
 
                 runOnUiThread(new Runnable() {
                     public void run() {
                         pdCancel.dismiss();
                         AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                        builder.setMessage(R.string.message_request_cancelled);
                         builder.setTitle(R.string.dialog_title_success);
                         builder.setCancelable(false);
                         builder.setNeutralButton(R.string.button_ok,
                                 new DialogInterface.OnClickListener() {
 
                                     @Override
                                     public void onClick(final DialogInterface dialog,
                                             final int which) {
                                         // cancel this dialog
                                         dialog.cancel();
                                     }
                                 });
                         // Create the AlertDialog object and return it
                         builder.create().show();
                     }
                 });
                 return;
             }
 
             // If an error happens to the direction request
             // display an AlertDialog to let user to (re)start
             // a new request
             if (retVal.isError()) {
                 Log.d(TAG, "Error on do request");
                 final String errorMessage = retVal.getMessage();
                 // Must call runOnUiThread if want to display a Toast or a
                 // Dialog within a thread
                 runOnUiThread(new Runnable() {
                     public void run() {
                         showErrorDialog(errorMessage);
                     }
                 });
 
                 // Closes the searching dialog and stay in the search activity
                 pd.dismiss();
                 return;
             }
 
             Log.d(TAG, "Do request success!");
             if (historyItem.addToHistory(currLocationLatLagString, from)) {
                 saveHistoryFile(from);
             }
             if (historyItem.addToHistory(currLocationLatLagString, to)) {
                 saveHistoryFile(to);
             }
 
             // send the result to ResultsActivity
             Intent intent = new Intent(SearchActivity.this, ResultsActivity.class);
             intent.putExtra("List of Routes", (Serializable) dReq.getSolutions());
             intent.putExtra("Current Route Index", 0);
             startActivity(intent);
 
             // Closes the searching dialog
             pd.dismiss();
             Log.v(TAG, "Done searching, go to result activity");
         }
 
         /**
          * Show the error dialog to user.
          * 
          * @param message
          *            the error message that will show in the dialog.
          */
         private void showErrorDialog(final String message) {
             AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
             builder.setMessage(message);
             builder.setTitle(Html.fromHtml("<font color='red'>"
                     + SearchActivity.this.getResources().getString(R.string.dialog_title_error)
                     + "</font>"));
             // we can set the onClickListener parameter as null
             builder.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener() {
 
                 @Override
                 public void onClick(final DialogInterface dialog, final int which) {
                     // cancel this dialog
                     dialog.cancel();
                 }
             });
             // Create the AlertDialog object and return it
             builder.create().show();
         }
 
         /**
          * Setup the time direction request.
          * 
          * @return the time in ms.
          */
         private long timeDirectionRequest() {
             // Set up time for direction request
             int month, dayOfMonth, year, hourOfDay, minute, second = 0;
             Time time = new Time();
             if (leaveNowButton.isChecked()) {
                 hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                 minute = calendar.get(Calendar.MINUTE);
                 month = calendar.get(Calendar.MONTH);
                 dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                 year = calendar.get(Calendar.YEAR);
             } else {
                 String dateString = dateEditText.getText().toString();
                 String timeString = timeEditText.getText().toString();
 
                 // System uses month from 0 to 11 to represent January to
                 // December
 
                 // Chinese date format
                 if (currLocale.getLanguage().equals("zh")) {
                     year = Integer.parseInt(dateString.substring(0, DIGIT_FIFTH));
                     month = Integer.parseInt(dateString.substring(DIGIT_SIXTH, DIGIT_EIGHTH)) - 1;
                     dayOfMonth = Integer
                             .parseInt(dateString.substring(DIGIT_NINTH, DIGIT_ELEVENTH));
 
                 } else { // US date format
                     month = Integer.parseInt(dateString.substring(0, 2)) - 1;
                     dayOfMonth = Integer.parseInt(dateString.substring(DIGIT_FOURTH, DIGIT_SIXTH));
                     year = Integer.parseInt(dateString.substring(DIGIT_SEVENTH, DIGIT_ELEVENTH));
                 }
                 hourOfDay = Integer.parseInt(timeString.substring(0, 2));
                 minute = Integer.parseInt(timeString.substring(DIGIT_FOURTH, DIGIT_SIXTH));
             }
 
             time.set(second, minute, hourOfDay, dayOfMonth, month, year);
             long timeToSend = time.toMillis(false) / SEC_TO_MILLISEC;
             return timeToSend;
         }
     }
 
     /**
      * The calendar visible within this SearchActivity as a source of time Note:
      * Calendar subclass instance is set to the current date and time in the
      * default Timezone, could be changed as users live in different timezones.
      */
     private final Calendar calendar = Calendar.getInstance();
 
     /**
      * "Leave Now" radio button.
      */
     private RadioButton leaveNowButton;
 
     /**
      * "Depart At" radio button.
      */
     private RadioButton departAtButton;
 
     /**
      * "Arrive At" radio button.
      */
     private RadioButton arriveAtButton;
 
     /**
      * "Start from" AutoCompleteTextView for starting address.
      */
     private AutoCompleteTextView fromAutoCompleteTextView;
 
     /**
      * "To" AutoCompleteTextView for destination address.
      */
     private AutoCompleteTextView toAutoCompleteTextView;
 
     /**
      * Delete button for user to clear text in fromAutoCompleteTextView.
      */
     private ImageButton fromClearButton;
 
     /**
      * Delete button for user to clear text in toAutoCompleteTextView.
      */
     private ImageButton toClearButton;
 
     /**
      * Reverse button for user to switch starting and destination address.
      */
     private ImageButton reverseButton;
 
     /**
      * EditText for user to pick a date.
      */
     private EditText dateEditText;
 
     /**
      * EditText for user to pick a time.
      */
     private EditText timeEditText;
 
     /**
      * "Find" to start searching.
      */
     private Button findButton;
 
     /**
      * Keeps an array of history entries.
      */
     private History historyItem;
 
     /**
      * A CheckBox for user to select BICYCLE ONLY MODE.
      */
     private CheckBox bicycleOnlyCheckBox;
 
     /**
      * Keeps selected travelMode.
      */
     private TravelMode tm;
 
     /**
      * A progress dialog indicating the searching status of this activity.
      */
     private ProgressDialog pd;
 
     /**
      * Location Client to get user's current location.
      */
     private LocationClient locationClient;
 
     /**
      * Current Location (From) button for current location.
      */
     private ImageButton fromCurrLocationButton;
 
     /**
      * Current Location (To) button for current location.
      */
     private ImageButton toCurrLocationButton;
 
     /**
      * TextView for informing user to choose number of buses.
      */
     private TextView numBusesTextView;
 
     /**
      * EditText for user to set minimum number of buses.
      */
     private EditText minNumBusesEditText;
 
     /**
      * EditText for user to set maximum number of buses.
      */
     private EditText maxNumBusesEditText;
 
     /**
      * EditText for user to set minimum biking distance.
      */
     private EditText minBikingDistanceEditText;
 
     /**
      * EditText for user to set maximum biking distance.
      */
     private EditText maxBikingDistanceEditText;
 
     /**
      * A progress dialog indicating the canceling status of this activity.
      */
     private ProgressDialog pdCancel;
     
     /**
      * A boolean indicating the cancellation by user.
      */
     private boolean cancelled;
 
     /**
      * Thread requesting for new routes.
      */
     private Thread dirThread;
 
     /**
      * User's Current locale.
      */
     private Locale currLocale;
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_search);
 
         establishViewsAndOtherNecessaryComponents();
 
         setListeners();
         setHistorySection();
         Log.v(TAG, "Done on create");
     }
 
     /**
      * Connect location client (for power saving). {@inheritDoc}
      * 
      * @see android.app.Activity#onStart(android.os.Bundle)
      */
     @Override
     protected final void onStart() {
         super.onStart();
         locationClient.connect();
         Log.v(TAG, "Done on start");
     }
 
     /**
      * Refresh the history list.
      */
     @Override
     protected final void onResume() {
         setInitialText();
         setHistorySection();
         super.onResume();
         Log.v(TAG, "Done on Resume");
     }
 
     /**
      * Disconnect location client (for power saving). {@inheritDoc}
      * 
      * @see android.app.Activity#onStop(android.os.Bundle)
      */
     @Override
     protected final void onStop() {
         locationClient.disconnect();
         super.onStop();
         Log.v(TAG, "Done on Stop");
     }
 
     /**
      * Show the menu bar when the setting button is clicked.
      * 
      * @param menu
      *            The options menu in which you place your items.
      * @return true if the menu to be displayed.
      * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
      */
     @Override
     public final boolean onCreateOptionsMenu(final Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_search, menu);
         return true;
     }
 
     /**
      * This method will be called when user click buttons in the setting menu.
      * 
      * @param item
      *            the menu item that user will click
      * @return true if user select an item
      */
     @Override
     public final boolean onOptionsItemSelected(final MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             // app icon in action bar clicked; go home
             Intent homeIntent = new Intent(this, MainActivity.class);
             homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(homeIntent);
             return true;
         case R.id.action_settings:
             // user click the setting button, start the settings activity
             Intent settingsIntent = new Intent(this, SettingsActivity.class);
             settingsIntent.putExtra("parent", "Search");
             startActivity(settingsIntent);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Find and establish all UI components.
      */
     private void establishViewsAndOtherNecessaryComponents() {
         // Action Bar setup
         this.getActionBar().setDisplayHomeAsUpEnabled(true);
 
         // Get locale
         currLocale = getResources().getConfiguration().locale;
 
         // Address setup
         fromAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.editTextStartFrom);
         toAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.editTextTo);
         reverseButton = (ImageButton) findViewById(R.id.imageButtonReverse);
         fromClearButton = (ImageButton) findViewById(R.id.imageButtonClearFrom);
         toClearButton = (ImageButton) findViewById(R.id.imageButtonClearTo);
 
         // Location setup
         locationClient = new LocationClient(this, this, this);
         fromCurrLocationButton = (ImageButton) findViewById(R.id.imageButtonCurrentLocationFrom);
         toCurrLocationButton = (ImageButton) findViewById(R.id.imageButtonCurrentLocationTo);
 
         // Basic options setup
         leaveNowButton = (RadioButton) findViewById(R.id.radioButtonLeaveNow);
         departAtButton = (RadioButton) findViewById(R.id.radioButtonDepartAt);
         arriveAtButton = (RadioButton) findViewById(R.id.radioButtonArriveAt);
 
         dateEditText = (EditText) findViewById(R.id.editTextDate);
         timeEditText = (EditText) findViewById(R.id.editTextTime);
 
         tm = TravelMode.MIXED;
         bicycleOnlyCheckBox = (CheckBox) findViewById(R.id.checkboxBicycleOnly);
 
         // Find button setup
         findButton = (Button) findViewById(R.id.buttonFind);
 
         // Advanced options setup
         numBusesTextView = (TextView) findViewById(R.id.textViewNumBuses);
         minBikingDistanceEditText = (EditText) findViewById(R.id.editTextMinBikingDistance);
         maxBikingDistanceEditText = (EditText) findViewById(R.id.editTextMaxBikingDistance);
         minNumBusesEditText = (EditText) findViewById(R.id.editTextMinNumBuses);
         maxNumBusesEditText = (EditText) findViewById(R.id.editTextMaxNumBuses);
     }
 
     /**
      * Attach all listeners to corresponding UI widgets.
      */
     private void setListeners() {
         setAddressBoxListeners();
         setAddressEditListeners();
         setLocationListeners();
         setBasicOptionsListners();
         setAdvancedOptionsListners();
     }
 
     /**
      * Attach the two address boxes listeners to corresponding UI widgets.
      */
     private void setAddressBoxListeners() {
         fromAutoCompleteTextView
                 .setOnFocusChangeListener(new FromToAutoCompleteTextViewOnFocusChangeListener());
 
         // Determine whether to show clear button for fromAutoCompleteTextView
         // when it is being edited
         fromAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
             public void afterTextChanged(final Editable s) {
                 if (!fromAutoCompleteTextView.hasFocus()
                         || fromAutoCompleteTextView.getText().toString().isEmpty()) {
                     fromClearButton.setVisibility(View.INVISIBLE);
                 } else {
                     fromClearButton.setVisibility(View.VISIBLE);
                 }
             }
 
             public void beforeTextChanged(final CharSequence s, final int start, final int count,
                     final int after) {
                 // Do nothing
             }
 
             public void onTextChanged(final CharSequence s, final int start, final int before,
                     final int count) {
                 // Do nothing
             }
         });
 
         // Handle Event when user press "Next" on Keyboard:
         // Jump from fromAutoCompleteTextView to toAutoCompleteTextView
         fromAutoCompleteTextView.setOnEditorActionListener(new OnEditorActionListener() {
             @Override
             public boolean onEditorAction(final TextView v, final int actionId,
                     final KeyEvent event) {
                 boolean handled = false;
                 if (actionId == EditorInfo.IME_ACTION_NEXT) {
                     toAutoCompleteTextView.requestFocus();
                     handled = true;
                 }
                 return handled;
             }
         });
 
         // Determine whether to show clear button for toAutoCompleteTextView
         // when it is focused
         toAutoCompleteTextView
                 .setOnFocusChangeListener(new FromToAutoCompleteTextViewOnFocusChangeListener());
 
         // Determine whether to show clear button for fromAutoCompleteTextView
         // when it is being edited
         toAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
             public void afterTextChanged(final Editable s) {
                 if (!toAutoCompleteTextView.hasFocus()
                         || toAutoCompleteTextView.getText().toString().isEmpty()) {
                     toClearButton.setVisibility(View.INVISIBLE);
                 } else {
                     toClearButton.setVisibility(View.VISIBLE);
                 }
             }
 
             public void beforeTextChanged(final CharSequence s, final int start, final int count,
                     final int after) {
                 // Do nothing
             }
 
             public void onTextChanged(final CharSequence s, final int start, final int before,
                     final int count) {
                 // Do nothing
             }
         });
     }
 
     /**
      * FromToAutoCompleteTextViewOnFocusChangeListener serves for
      * fromAutoCompleteTextView and toAutoCompleteTextView to determine the
      * visibility of the clear buttons.
      */
     private class FromToAutoCompleteTextViewOnFocusChangeListener implements OnFocusChangeListener {
         @Override
         public void onFocusChange(final View v, final boolean hasFocus) {
             ImageButton targetClearButton;
             if (v.getId() == R.id.editTextStartFrom) {
                 targetClearButton = fromClearButton;
             } else {
                 targetClearButton = toClearButton;
             }
 
             if (hasFocus && !((AutoCompleteTextView) v).getText().toString().isEmpty()) {
                 targetClearButton.setVisibility(View.VISIBLE);
             } else {
                 targetClearButton.setVisibility(View.INVISIBLE);
             }
         }
 
     }
 
     /**
      * Attach address editing listeners to corresponding UI widgets.
      */
     private void setAddressEditListeners() {
         reverseButton.setOnClickListener(new OnClickListener() {
 
             /**
              * @see android.view.View.OnClickListener#onClick(android.view.View)
              */
             public void onClick(final View v) {
                 boolean fromACTVisEnabled = fromAutoCompleteTextView.isEnabled();
                 boolean toACTVisEnabled = toAutoCompleteTextView.isEnabled();
                 String fromACTVOriginalText = fromAutoCompleteTextView.getText().toString();
                 String toACTVOriginalText = toAutoCompleteTextView.getText().toString();
 
                 // process according to from
                 reverseProcessingHelper(toAutoCompleteTextView, toCurrLocationButton,
                         toClearButton, fromACTVOriginalText, fromACTVisEnabled);
 
                 // process according to to
                 reverseProcessingHelper(fromAutoCompleteTextView, fromCurrLocationButton,
                         fromClearButton, toACTVOriginalText, toACTVisEnabled);
             }
 
             /**
              * Helper method to finish reverse process.
              */
             private void reverseProcessingHelper(
                     final AutoCompleteTextView targetAutoCompleteTextView,
                     final ImageButton targetCurrLocationButton,
                     final ImageButton targetClearButton, final String originalACTVText,
                     final boolean originalACTVisEnabled) {
 
                 if (!originalACTVisEnabled) {
                     targetAutoCompleteTextView.clearComposingText();
                     targetAutoCompleteTextView.setEnabled(false);
                     targetAutoCompleteTextView.setText(originalACTVText);
                     targetAutoCompleteTextView.setTextColor(SearchActivity.this.getResources()
                             .getColor(R.color.cyan));
                     targetAutoCompleteTextView.setTypeface(null, Typeface.ITALIC);
                     targetAutoCompleteTextView.dismissDropDown();
                     targetCurrLocationButton.setImageResource(R.drawable.current_location_cancel);
                     targetClearButton.setVisibility(View.INVISIBLE);
                 } else {
                     targetAutoCompleteTextView.setText("");
                     targetAutoCompleteTextView.setTextColor(Color.BLACK);
                     targetAutoCompleteTextView.setTypeface(null, Typeface.NORMAL);
                     targetCurrLocationButton.setImageResource(R.drawable.current_location_select);
                     targetAutoCompleteTextView.setText(originalACTVText);
                     targetAutoCompleteTextView.setEnabled(true);
                 }
             }
 
         });
 
         fromClearButton.setOnClickListener(new FromToClearButtonOnClickListner());
         toClearButton.setOnClickListener(new FromToClearButtonOnClickListner());
 
     }
 
     /**
      * FromToClearButtonOnClickListner serves for from fromClearButton and
      * toClearButton to clear text.
      */
     private class FromToClearButtonOnClickListner implements OnClickListener {
         @Override
         public void onClick(final View v) {
             AutoCompleteTextView targetAutoCompleteTextView;
             if (v.getId() == R.id.imageButtonClearFrom) {
                 targetAutoCompleteTextView = fromAutoCompleteTextView;
             } else {
                 targetAutoCompleteTextView = toAutoCompleteTextView;
             }
             targetAutoCompleteTextView.clearComposingText();
             targetAutoCompleteTextView.setText("");
         }
 
     }
 
     /**
      * Attach location-related listeners to corresponding UI widgets.
      */
     private void setLocationListeners() {
         fromCurrLocationButton.setOnClickListener(new OnClickListener() {
             private boolean currentLocationSelected = false;
 
             public void onClick(final View v) {
                 if (currentLocationSelected) {
                     fromAutoCompleteTextView.setText("");
                     fromAutoCompleteTextView.setTextColor(Color.BLACK);
                     fromAutoCompleteTextView.setTypeface(null, Typeface.NORMAL);
                     fromCurrLocationButton.setImageResource(R.drawable.current_location_select);
                     fromAutoCompleteTextView.setEnabled(true);
                     fromAutoCompleteTextView.requestFocus();
                     currentLocationSelected = false;
                 } else {
                     fromAutoCompleteTextView.clearComposingText();
                     fromAutoCompleteTextView.setEnabled(false);
                     fromAutoCompleteTextView.setText(R.string.edittext_curr_location);
                     fromAutoCompleteTextView.setTextColor(SearchActivity.this.getResources()
                             .getColor(R.color.cyan));
                     fromAutoCompleteTextView.setTypeface(null, Typeface.ITALIC);
                     fromCurrLocationButton.setImageResource(R.drawable.current_location_cancel);
                     fromClearButton.setVisibility(View.INVISIBLE);
                     currentLocationSelected = true;
                 }
 
             }
         });
 
         toCurrLocationButton.setOnClickListener(new OnClickListener() {
             private boolean currentLocationSelected = false;
 
             public void onClick(final View v) {
                 if (currentLocationSelected) {
                     toAutoCompleteTextView.setText("");
                     toAutoCompleteTextView.setTextColor(Color.BLACK);
                     toAutoCompleteTextView.setTypeface(null, Typeface.NORMAL);
                     toCurrLocationButton.setImageResource(R.drawable.current_location_select);
                     toAutoCompleteTextView.setEnabled(true);
                     toAutoCompleteTextView.requestFocus();
                     currentLocationSelected = false;
                 } else {
                     toAutoCompleteTextView.clearComposingText();
                     toAutoCompleteTextView.setEnabled(false);
                     toAutoCompleteTextView.setText(R.string.edittext_curr_location);
                     toAutoCompleteTextView.setTextColor(SearchActivity.this.getResources()
                             .getColor(R.color.cyan));
                     toAutoCompleteTextView.setTypeface(null, Typeface.ITALIC);
                     toCurrLocationButton.setImageResource(R.drawable.current_location_cancel);
                     toClearButton.setVisibility(View.INVISIBLE);
                     currentLocationSelected = true;
                 }
             }
         });
     }
 
     /**
      * Attach basic options listeners to corresponding UI widgets.
      */
     private void setBasicOptionsListners() {
         leaveNowButton.setOnClickListener(new OnClickListener() {
             public void onClick(final View v) {
                 dateEditText.setEnabled(false);
                 timeEditText.setEnabled(false);
             }
         });
 
         departAtButton.setOnClickListener(new OnClickListener() {
             public void onClick(final View v) {
                 dateEditText.setEnabled(true);
                 timeEditText.setEnabled(true);
             }
         });
 
         arriveAtButton.setOnClickListener(new OnClickListener() {
             public void onClick(final View v) {
                 dateEditText.setEnabled(true);
                 timeEditText.setEnabled(true);
             }
         });
 
         dateEditText.setOnClickListener(new OnClickListener() {
             public void onClick(final View v) {
                 DialogFragment dpf = new DatePickerFragment();
                 dpf.show(getFragmentManager(), "datePicker");
             }
         });
 
         // Prohibit user from directly typing a date (instead, users should pick
         // a date)
         dateEditText.setKeyListener(null);
 
         timeEditText.setOnClickListener(new OnClickListener() {
             public void onClick(final View v) {
                 DialogFragment tpf = new TimePickerFragment();
                 tpf.show(getFragmentManager(), "timePicker");
             }
         });
 
         // Prohibit user from directly typing a time (instead, users should pick
         // a time)
         timeEditText.setKeyListener(null);
         findButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(final View v) {
                 cancelled = false;
                 dirThread = new Thread(new DirThread());
                 pd = new ProgressDialog(SearchActivity.this);
                 pd.setTitle(R.string.dialog_title_searching);
                 pd.setMessage(SearchActivity.this.getResources().getString(
                         R.string.message_searching_for_routes));
                 // This is to enforce user to click "cancel" button to cancel
                 // instead of clicking anywhere else
                 pd.setCancelable(false);
                 pd.setButton(DialogInterface.BUTTON_NEUTRAL, SearchActivity.this.getResources()
                         .getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(final DialogInterface dialog, final int which) {
                         // Cancel the request
                         Log.d(TAG, "Interrupting dirThread: " + dirThread.getId());
                         cancelled = true;
                         dirThread.interrupt();
                         pdCancel = new ProgressDialog(SearchActivity.this);
                        pdCancel.setTitle(R.string.dialog_title_cancelling);
                         pdCancel.setMessage(SearchActivity.this.getResources().getString(
                                 R.string.message_cancelling));
                         // This is to enforce user to click "cancel"
                         // button to cancel
                         // instead of clicking anywhere else
                         pdCancel.setCancelable(false);
                         pdCancel.show();
                     }
 
                 });
 
                 pd.show();
 
                 Log.d(TAG, "start dirThread: " + dirThread.getId());
                 dirThread.start();
             }
         });
 
         bicycleOnlyCheckBox.setOnClickListener(new OnClickListener() {
             private boolean isCheckedBefore = false;
 
             public void onClick(final View v) {
                 if (isCheckedBefore) {
                     numBusesTextView.setVisibility(View.VISIBLE);
                     minNumBusesEditText.setVisibility(View.VISIBLE);
                     maxNumBusesEditText.setVisibility(View.VISIBLE);
                     isCheckedBefore = false;
                 } else {
                     numBusesTextView.setVisibility(View.INVISIBLE);
                     minNumBusesEditText.setVisibility(View.INVISIBLE);
                     maxNumBusesEditText.setVisibility(View.INVISIBLE);
                     isCheckedBefore = true;
                 }
             }
         });
     }
 
     /**
      * Attach advanced options listeners to corresponding UI widgets.
      */
     private void setAdvancedOptionsListners() {
         minBikingDistanceEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
             @Override
             public void onFocusChange(final View v, final boolean hasFocus) {
                 if (!hasFocus && minBikingDistanceEditText.getText().length() != 0) {
                     // Format numbers
                     int formattedNumber = Integer.parseInt(minBikingDistanceEditText.getText()
                             .toString());
                     minBikingDistanceEditText.setText("" + formattedNumber);
                 }
             }
 
         });
 
         maxBikingDistanceEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
             @Override
             public void onFocusChange(final View v, final boolean hasFocus) {
                 if (!hasFocus && maxBikingDistanceEditText.getText().length() != 0) {
                     // Format numbers
                     int formattedNumber = Integer.parseInt(maxBikingDistanceEditText.getText()
                             .toString());
                     maxBikingDistanceEditText.setText("" + formattedNumber);
                 }
             }
         });
 
         minNumBusesEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
             @Override
             public void onFocusChange(final View v, final boolean hasFocus) {
                 if (!hasFocus && minNumBusesEditText.getText().length() != 0) {
                     // Format numbers
                     int formattedNumber = Integer
                             .parseInt(minNumBusesEditText.getText().toString());
                     minNumBusesEditText.setText("" + formattedNumber);
                 }
             }
         });
 
         maxNumBusesEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
             @Override
             public void onFocusChange(final View v, final boolean hasFocus) {
                 if (!hasFocus && maxNumBusesEditText.getText().length() != 0) {
                     // Format numbers
                     int formattedNumber = Integer
                             .parseInt(maxNumBusesEditText.getText().toString());
                     maxNumBusesEditText.setText("" + formattedNumber);
                 }
             }
 
         });
 
     }
 
     /**
      * Sets up default text to be displayed on the UI of this activity.
      */
     private void setInitialText() {
         // Set initial date and time
 
         // Date format in China
         if (currLocale.getLanguage().equals("zh")) {
             dateEditText.setText(com.HuskySoft.metrobike.ui.utility.Utility
                     .convertAndroidSystemDateToFormatedDateStringChineseConvention(
                             calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                             calendar.get(Calendar.DAY_OF_MONTH)));
         } else { // Date format in US
             dateEditText.setText(com.HuskySoft.metrobike.ui.utility.Utility
                     .convertAndroidSystemDateToFormatedDateString(calendar.get(Calendar.YEAR),
                             calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)));
         }
 
         timeEditText.setText(com.HuskySoft.metrobike.ui.utility.Utility
                 .convertAndroidSystemTimeToFormatedTimeString(calendar.get(Calendar.HOUR_OF_DAY),
                         calendar.get(Calendar.MINUTE)));
 
     }
 
     /**
      * Fill in the history section.
      */
     private void setHistorySection() {
         historyItem = History.getInstance();
         String[] f = historyItem.getHistory().toArray(new String[0]);
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                 android.R.layout.select_dialog_item, f);
         fromAutoCompleteTextView.setAdapter(adapter);
         toAutoCompleteTextView.setAdapter(adapter);
     }
 
     /**
      * After onStart calls locationClient to connect, this is triggered if there
      * is a problem connecting the Google Play Location Services.
      * 
      * @param cr
      *            the connection result
      */
     @Override
     public final void onConnectionFailed(final ConnectionResult cr) {
         Toast.makeText(this,
                 SearchActivity.this.getResources().getString(R.string.error_gms_connection_fail),
                 Toast.LENGTH_SHORT).show();
     }
 
     /**
      * After onStart calls locationClient to connect, this is triggered if
      * connection to Google Play Location Services is successfully established.
      * 
      * @param bd
      *            the bundle passed in by Google Play Location Services
      */
     @Override
     public final void onConnected(final Bundle bd) {
         // Nothing happens
     }
 
     /**
      * After onStart calls locationClient to disconnect, this is triggered if
      * connection to Google Play Location Services is successfully disconnected.
      */
     @Override
     public final void onDisconnected() {
         // Nothing happens
     }
 
     /**
      * Write all history addresses into file.
      * 
      * @param address
      *            the address that will add the file.
      */
     private void saveHistoryFile(final String address) {
         FileOutputStream fos = null;
         try {
             // append the address into exist file.
             fos = openFileOutput(History.FILENAME, Context.MODE_APPEND);
             History.writeOneAddressToFile(fos, address);
         } catch (FileNotFoundException e) {
             Log.i(TAG, "Cannot create history file");
         } finally {
             History.closeFileStream(null, fos);
         }
     }
 }
