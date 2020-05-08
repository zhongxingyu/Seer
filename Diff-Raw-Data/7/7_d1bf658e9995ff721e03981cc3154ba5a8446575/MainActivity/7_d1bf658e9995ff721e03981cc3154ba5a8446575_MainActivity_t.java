 package com.example.mobilehitchhiker;
 
 import java.util.Calendar;
 import java.util.List;
 
 import android.location.Address;
 import android.location.Geocoder;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.text.Editable;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.app.DatePickerDialog;
 
 import android.view.Menu;
 import android.view.View.OnClickListener;
 
 @TargetApi(11)
 public class MainActivity extends Activity {
 
 	private static final String CLASSTAG = MainActivity.class.getSimpleName();
 	private static final int MENU_CREATE_TRIP = Menu.FIRST;
 	private static final int MENU_FIND_TRIP = Menu.FIRST + 1;
 	private static final int MAX_ADDRESSES = 3;
 	private EditText startLocation;
 	private EditText endLocation;
 	private Button buttonShow;
 	private Button buttonFind;
 	private CheckBox startCheckBox;
 	private MobileHitchhikerApplication.Trip trip;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 				.permitAll().build();
 		StrictMode.setThreadPolicy(policy);
 
 		super.onCreate(savedInstanceState);
 
 		Log.v(Config.LOGTAG, " " + MainActivity.CLASSTAG + " onCreate");
 
 		setContentView(R.layout.main);
 
 		startCheckBox = (CheckBox) findViewById(R.id.checkBoxStart);
 
 		buttonShow = (Button) findViewById(R.id.button_show);
 		buttonFind = (Button) findViewById(R.id.button_find);
 
 		startLocation = (EditText) findViewById(R.id.start_location);
 		endLocation = (EditText) findViewById(R.id.end_location);
 
 		buttonShow.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				MobileHitchhikerApplication application = (MobileHitchhikerApplication) getApplication();
 				application.setAim(MobileHitchhikerApplication.TO_CREATE);
 				Log.v(Config.LOGTAG, " " + MainActivity.CLASSTAG
 						+ " CreateTrip button clicked");
 				Log.v(Config.LOGTAG, " " + MainActivity.CLASSTAG + " Aim is : "
 						+ application.getAim());
 				handleShowMap(application.getAim());
 			}
 		});
 
 		buttonFind.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				MobileHitchhikerApplication application = (MobileHitchhikerApplication) getApplication();
 				application.setAim(MobileHitchhikerApplication.TO_FIND);
 				Log.v(Config.LOGTAG, " " + MainActivity.CLASSTAG
 						+ " FindTrip button clicked");
 				handleShowMap(application.getAim());
 			}
 		});
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.v(Config.LOGTAG, " " + MainActivity.CLASSTAG + " onResume");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, MainActivity.MENU_CREATE_TRIP, 0, R.string.menu_create_trip)
 				.setIcon(android.R.drawable.ic_menu_more);
 		menu.add(0, MainActivity.MENU_FIND_TRIP, 1, R.string.menu_find_trip)
 				.setIcon(android.R.drawable.ic_menu_more);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
 		MobileHitchhikerApplication application = (MobileHitchhikerApplication) getApplication();
		if (item.getItemId() == MENU_CREATE_TRIP) {
 			application.setAim(MobileHitchhikerApplication.TO_CREATE);
 			Log.v(Config.LOGTAG, " " + MainActivity.CLASSTAG
 					+ " CreateTrip menu item clicked");
 			handleShowMap(application.getAim());
 			return true;
		} else if (item.getItemId() == MENU_FIND_TRIP) {
 			application.setAim(MobileHitchhikerApplication.TO_FIND);
 			Log.v(Config.LOGTAG, " " + MainActivity.CLASSTAG
 					+ " FindTrip menu item clicked");
 			handleShowMap(application.getAim());
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	public void showDatePickerDialog(View v) {
 		DialogFragment newFragment = new DatePickerFragment();
 		newFragment.show(getFragmentManager(), "datePicker");
 	}
 
 	private void handleShowMap(int aim) {
 
 		MobileHitchhikerApplication application = (MobileHitchhikerApplication) getApplication();
 		Log.v(Config.LOGTAG, " " + MainActivity.CLASSTAG + " handleShowMap");
 		Log.v(Config.LOGTAG, " " + MainActivity.CLASSTAG + " Aim is : "
 				+ application.getAim());
 
 		if (!startCheckBox.isChecked()
 				&& (startLocation.getText() == null || startLocation.getText()
 						.toString().equals(""))) {
 			new AlertDialog.Builder(this)
 					.setTitle(R.string.alert_label)
 					.setMessage(R.string.start_location_not_supplied_message)
 					.setPositiveButton(
 							"Continue",
 							new android.content.DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									// TODO Auto-generated method stub
 
 								}
 							}).show();
 			return;
 		} else if ((endLocation.getText() == null)
 				|| endLocation.getText().toString().equals("")) {
 			new AlertDialog.Builder(this)
 					.setTitle(R.string.alert_label)
 					.setMessage(R.string.end_location_not_supplied_message)
 					.setPositiveButton(
 							"Continue",
 							new android.content.DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									// TODO Auto-generated method stub
 								}
 							}).show();
 			return;
 		}
 
 		String startLocationString = startLocation.getText().toString();
 		String endLocationString = endLocation.getText().toString();
 		try {
 			if (startCheckBox.isChecked()) {
 				LocationService gps = new LocationService(MainActivity.this);
 
 				if (gps.isGPSEnabled()) {
 					double latitude = gps.getLatitude();
 					double longitude = gps.getLongitude();
 
 					trip = application.new Trip(getAddressFromLocation(
 							latitude, longitude),
 							getAddressFromString(endLocationString));
 				} else {
 					gps.showSettingsAlert();
 				}
 
 			} else {
 				// FIXME pop up
 				trip = application.new Trip(
 						getAddressFromString(startLocationString),
 						getAddressFromString(endLocationString),
 						startLocationString, endLocationString);
 			}
 
 			application.setTrip(trip);
 
 			if (application.getAim() == MobileHitchhikerApplication.TO_CREATE) {
 				AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 				alert.setCancelable(false);
 				alert.setTitle("Please insert contact defails");
 				alert.setMessage("Email");
 
 				final EditText input = new EditText(this);
 				alert.setView(input);
 
 				alert.setPositiveButton("Ok",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								Editable value = input.getText();
 								MobileHitchhikerApplication application = (MobileHitchhikerApplication) getApplication();
 								application.setContact(value.toString());
 								application.addTripToTripList(trip);
 								Intent intent = new Intent(
 										Config.INTENT_ACTION_SHOW_TRIP);
 								startActivity(intent);
 								dialog.dismiss();
 							}
 						});
 
 				alert.show();
 
 			} else {
 				MobileHitchhikerApplication.Trip foundTrip;
 				try {
 					foundTrip = application.findBestTrip(trip);
 
 					application.setFoundTrip(foundTrip);
 					Intent intent = new Intent(Config.INTENT_ACTION_SHOW_TRIP);
 					startActivity(intent);
 				} catch (TripNotFoundException e) {
 					new AlertDialog.Builder(this)
 							.setTitle(R.string.alert_label)
 							.setMessage("Trip not found!")
 							.setPositiveButton(
 									"Continue",
 									new android.content.DialogInterface.OnClickListener() {
 										@Override
 										public void onClick(
 												DialogInterface dialog,
 												int which) {
 											// TODO Auto-generated method stub
 										}
 									}).show();
 				}
 			}
 
 		} catch (InvalidAddressException e) {
 			Log.d(Config.LOGTAG, "Popup"); // FIXME
 			new AlertDialog.Builder(this)
 					.setTitle(R.string.alert_label)
 					.setMessage(R.string.location_not_resolved_message)
 					.setPositiveButton(
 							"Continue",
 							new android.content.DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									// TODO Auto-generated method stub
 								}
 							}).show();
 			return;
 		}
 	}
 
 	public Address getAddressFromString(String str)
 			throws InvalidAddressException {
 		Geocoder coder = new Geocoder(this);
 		List<Address> addressList;
 		Address address = null;
 
 		try {
 			addressList = coder.getFromLocationName(str, MAX_ADDRESSES);
 			if (addressList == null) {
 				Log.d(Config.LOGTAG,
 						"############Address not correct #########");
 			}
 			address = addressList.get(0);
 
 			Log.v(Config.LOGTAG, "lat=" + address.getLatitude() + "&long="
 					+ address.getLongitude());
 		} catch (Exception e) {
 			Log.d(Config.LOGTAG, "MY_ERROR : ############Address Not Found");
 			throw new InvalidAddressException(str);
 			// FIXME: pop up "address not found" and go back
 		}
 
 		return address;
 	}
 
 	public Address getAddressFromLocation(double latitude, double longitude) {
 		Geocoder coder = new Geocoder(this);
 		List<Address> addressList;
 		Address address = null;
 
 		try {
 			addressList = coder.getFromLocation(latitude, longitude,
 					MAX_ADDRESSES);
 			if (addressList == null) {
 				Log.d(Config.LOGTAG,
 						"############Address not correct #########");
 			}
 			address = addressList.get(0);
 
 			Log.v(Config.LOGTAG, "lat=" + address.getLatitude() + "&long="
 					+ address.getLongitude());
 		} catch (Exception e) {
 			Log.d(Config.LOGTAG, "MY_ERROR : ############Address Not Found");
 		}
 
 		return address;
 	}
 
 	class DatePickerFragment extends DialogFragment implements
 			DatePickerDialog.OnDateSetListener {
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			final Calendar c = Calendar.getInstance();
 			int year = c.get(Calendar.YEAR);
 			int month = c.get(Calendar.MONTH);
 			int day = c.get(Calendar.DAY_OF_MONTH);
 
 			return new DatePickerDialog(getActivity(), this, year, month, day);
 		}
 
 		public void onDateSet(DatePicker view, int year, int month, int day) {
 			MobileHitchhikerApplication application = (MobileHitchhikerApplication) getApplication();
 			Calendar c = Calendar.getInstance();
 			c.set(year, month, day);
 			application.setStartDate(c);
 		}
 
 	}
 
 }
 
 class InvalidAddressException extends Exception {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	void InvaidAddressException() {
 	}
 
 	InvalidAddressException(String strMessage) {
 		super(strMessage);
 	}
 }
