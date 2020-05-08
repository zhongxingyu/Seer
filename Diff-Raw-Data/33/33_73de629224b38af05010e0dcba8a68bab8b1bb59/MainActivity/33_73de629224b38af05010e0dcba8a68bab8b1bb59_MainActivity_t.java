 /* 
  * MainActivity represents the first tab, where users can scan a qr code,
  * select the amount of time they want to park, and park it.  Also while
  * parked, they are able to refill time and unpark themselves.  
  * 
  * */
 
 package com.test;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.ActivityGroup;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Vibrator;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 import com.objects.Global;
 import com.objects.ParkInstanceObject;
 import com.objects.RateObject;
 import com.objects.RateResponse;
 import com.objects.SavedInfo;
 import com.objects.ServerCalls;
 import com.objects.ThrowDialog;
 import com.quietlycoding.android.picker.NumberPicker;
 import com.quietlycoding.android.picker.NumberPickerDialog;
 
 /**
  * connection ready display, ping server.  
  * robust communication model.  
  * 
  * new timer, crashes on timer end.  Also, two vars in saved pref holding endtime (endTime and TIMER).  use one.  
  * erase unnecessary instance variables such as remainSeconds.  
  * 
  * by saving all info, you can resume properly.  on create, use string starttime to find out how much time is left, calculate it
  * and then use it to initiate timer.  service should still be running, to resume app and auto-unpark and stuff.  have service conduct autorefill,
  * not the timer.  do so by editing a saved string for time, a service can edit it.  
  * 
  * edittext with tags, layout similar to citrix meeting app.  you can edit the layout of the actual edittext widget.	
  * 
  * save all info to be displayed, such as email etc, and load to prevent app from looking bad if resume doesn't work.  true "re-load"
  * refill should edit usertable and history table, and then edit app.  unparking then parking is lazy programming.  
  * edit add_history.php to check if users are currently parked, if true then update else insert.
  * sqlite db for parking history
  * internet detection
  * different user  
  * pop-up tutorial
  * write find car w/ pop-up for location, find parking (nearby? in general? how to do?)
  * create managerial app
  * time zone? how is it delt with on phone?  ON PARK, SAVE PRIVATE STARTTIME DATE OBJ.
  * 
  * compatibility!!!! works on emulators but not on phone.
  * app doesn't work on 2.3, the app always quits and services dont stop.
  * OR it isn't quitting, but the resume is just making a new instance of same app each time.  possible?
  * 
  * expense handling, create database of company charge tokens, for specific user email.  
  *    if the user's expense handlingn is flagged, check db for how much company has paid.  
  *    
  * refill complete dialog cancels after a while
  * settings should have option for warning time, save info on back, the time is read and passed when starting service
  * 
  * on park, grab lat/lon and compare with what we get from server for spot, combine and store for later.
  * park only if detect internet  on the parq button, check if internet is active.  
  * 
  * DO NOT RELY ON GOOD CONNECTION.  model should be - ping server, respond okay, make change on app, confirm change on server.
  * also must consider broken connections, so app must re-send refill requests that did not go through
  * 
  * perfect information display (sizing, borders, etc)
  * INSIDE NUMBER PICKER DISPLAY should show hrs:minutes ???
  * 
  * "Share Parq" option, pulls up qr code to scan.  
  * 
  * flash light when dark
  * 
  * BOOT LOAD SERVICE RESUME
  * 
  * TimeLeftDisplay = analog timer, digital countdown (setting gives choice) 
  * Add server calls for rates.
  * 
  * INCORPORATING NUMBERPICKER:  import class, start activity for view, setContentView of dialog
  * 
  * SEcurity in authenticating with server? encrypt, salt, and ssl all communication.
  * 
  * look into city's expenses, number of parks, gauge the server costs, lay out finance to potential
  *    partners
  * */
 
 public class MainActivity extends ActivityGroup implements LocationListener {
 
 	/*Textual Display objects declared here*/
 	private TextView rate;
 	private TextView userDisplay;
 	private TextView locDisplay;
 	private TextView timeDisplay;
 	private EditText hours;
 	private EditText minutes;
 	private TextView price;
 	private TextView increment;
 	private TextView lotDesc;
 	private TextView spot;
 
 	/*Buttons declared here*/
 	private EditText spotNum;
 	private Button submitButton;
 	private Button scanButton;
 	private Button unparqButton;
 	private Button refillButton;
 	private Button cancelPark;
 	private Button parkButton;
 	private Button gpsButton;
 	private Button pickerIncButton;
 	private Button pickerDecButton;
 
 	/*ints used by calculations*/
 	private int warnTime = 30; //in seconds
 	private int remainSeconds; //in seconds
 	private int minimalIncrement;//in minutes
 	private int maxTime; //in minutes
 	private int totalTimeParked = 0; //in minutes
 	private int parkCost =0;
 
 	/*various Objects used declared here*/
 	private static RateObject mySpot;
 	private static RateResponse myRateResponse;
 	private CountDownTimer timer;
 	public static ViewFlipper vf;
 	private AlertDialog alert;
 	private static MediaPlayer mediaPlayer;
 	private NumberPicker parkTimePicker;
 	private Date globalEndTime;
 	/*final variables*/
 	public static final String SAVED_INFO = "ParqMeInfo";
 
 	private LocationManager locationManager;
 	private Location lastLocation;
 	private static final float LOCATION_ACCURACY = 20f;
 	private boolean goodLocation = false;
 	private Runnable test;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		//check for internet on create, do it a lot later too.
 		//use flipper view
 		setContentView(R.layout.flipper);
 
 		//Create refill dialog which has special components
 		alert = makeRefillDialog();
 
 		//hook elements
 		rate = (TextView) findViewById(R.id.rate);
 		userDisplay = (TextView) findViewById(R.id.welcomeuser);
 		locDisplay = (TextView) findViewById(R.id.location);
 		timeDisplay = (TextView) findViewById(R.id.timeleftdisplay);
 
 		price = (TextView) findViewById(R.id.total_price);
 		increment = (TextView) findViewById(R.id.increment);
 		lotDesc = (TextView) findViewById(R.id.lot_description);
 		spot = (TextView) findViewById(R.id.spot);
 		vf = (ViewFlipper) findViewById(R.id.flipper);
 
 		//		parkTimePicker = (NumberPicker) findViewById(R.id.parktimepicker);
 		mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alarm);
 		final SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 
 		/*	ON CREATE
 		 * 	grab string from TIMER
 		 * 	parse it to a date
 		 * 	compare with current time
 		 * 	
 		 *	if time left, 
 		 *		showNext() x2, initiate timer
 		 *	if else
 		 *		set TIMER = "none"
 		 * 
 		 * */
 
 		try {
 			Date timerEnd = Global.sdf.parse(check.getString("TIMER", ""));
 			Date now = new Date();
 			int seconds = (int)(timerEnd.getTime() - now.getTime())/1000;
 			if(seconds>0){
 				vf.showNext();
 				vf.showNext();
 				timer = initiateTimer(timerEnd, vf);
 				timer.start();
 			}else{
 				SharedPreferences.Editor edit = check.edit();
 				edit.putString("TIMER", "none");
 				edit.commit();
 			}
 		} catch (ParseException e) {
 			//bad time in timer, thus no timer was active.  do nothing.  
 		}
 
 		//if parkstate returned from login was true, then go to the refill view.  
 		if(check.getBoolean("parkState", false)){
 			vf.showNext();
 		}
 
 		final OnFocusChangeListener timeListener = new OnFocusChangeListener() {
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
				updateDisplay(getParkMins());
 			}
 		};
 
 		hours = (EditText) findViewById(R.id.hours);
 		hours.setInputType(InputType.TYPE_NULL);
 		hours.setOnFocusChangeListener(timeListener);
 
 		minutes = (EditText) findViewById(R.id.mins);
 		minutes.setInputType(InputType.TYPE_NULL);
 		minutes.setOnFocusChangeListener(timeListener);
 
 		//initialize buttons and set actions
 		submitButton = (Button) findViewById(R.id.submitButton);
 		submitButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				final String contents = spotNum.getText().toString();
 				// contents contains string "parqme.com/p/c36/p123456" or w/e...
 				SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 
 				final double lat;
 				final double lon;
 				if (!goodLocation) {
 					// TODO: Show a loading dialog and don't do the rest of this stuff yet
 					lat = 0f;
 					lon = 0f;
 				} else {
 					lat = lastLocation.getLatitude();
 					lon = lastLocation.getLongitude();
 				}
 				myRateResponse = ServerCalls.getRateGps(contents, lat, lon, check);
 				mySpot = myRateResponse.getRateObject();
 				// if we get the object successfully
 				if (myRateResponse.getResp().equals("OK")) {
 					vf.showNext();
 					// prepare time picker for this spot
 					//					parkTimePicker.setRange(mySpot.getMinIncrement(),
 					//							mySpot.getMaxTime());
 					//					parkTimePicker.setMinInc(mySpot.getMinIncrement());
 
 					// initialize all variables to match spot
 					minimalIncrement = mySpot.getMinIncrement();
 					maxTime = mySpot.getMaxTime();
 
 					remainSeconds = getParkMins() * 60;
 
					rate.setText(formatCents(mySpot.getDefaultRate()) + " per " + minimalIncrement + " minutes");
 					lotDesc.setText(mySpot.getDescription());
 					spot.setText("Spot #" + contents);
 					if (mySpot.getMinIncrement() != 0) {
 						increment.setText(mySpot.getMinIncrement() + " minute increments");
 					}
 					// store some used info
 					SharedPreferences.Editor editor = check.edit();
 					editor.putString("code", contents);
 					editor.putFloat("lat", (float) mySpot.getLat());
 					editor.putFloat("lon", (float) mySpot.getLon());
 					editor.commit();
 					updateDisplay(minimalIncrement);
 				} else {
 					ThrowDialog.show(MainActivity.this,
 							ThrowDialog.RESULT_ERROR);
 				}
 			}
 		});
 		spotNum = (EditText) findViewById(R.id.spot_num);
 		spotNum.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable s) {
 				submitButton.setEnabled(s.length() > 0);
 			}
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 		});
 		scanButton = (Button) findViewById(R.id.scanButton);
 		scanButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if(!SavedInfo.isLoggedIn(MainActivity.this)){
 					//if not logged in, alert user via dialog
 					ThrowDialog.show(MainActivity.this, ThrowDialog.MUST_LOGIN);
 				}else{
 
 					//else start scan intent
 					IntentIntegrator.initiateScan(MainActivity.this);
 				}
 			}});
 
 
 		parkButton = (Button) findViewById(R.id.parkbutton);
 		parkButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//call park, which ONLY updates server and gives us response code
 				int parkResult = park();
 				//if that response code is good,
 				if (parkResult==1){
 					totalTimeParked+=getParkMins();
 					parkCost = getCostInCents(getParkMins());
 					if(totalTimeParked == maxTime){
 						ThrowDialog.show(MainActivity.this, ThrowDialog.MAX_TIME);
 					}
 					vf.showNext();
 
 					//TODO: DELETE ME FOR TESTING
 					remainSeconds = 60;
 					//END TESTING CODE
 
 					//create and start countdown display
 					try {
 						timer = initiateTimer(Global.sdf.parse(check.getString("TIMER", "")), vf);
 					} catch (ParseException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					timer.start();
 					//start timer background service
 					startService(new Intent(MainActivity.this, Background.class).putExtra("time", remainSeconds));
 
 					SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 					SharedPreferences.Editor editor = check.edit();
 					/*mark the user as currently parked*/
 					editor.putBoolean("parkState", true);
 					editor.commit();
 
 					//display where user is parked and who they are
 					userDisplay.setText("Welcome " + check.getString("fname", "")); 
 					locDisplay.setText("You are parked at\n" + mySpot.getDescription()+"\nAt spot # " + mySpot.getSpot());
 
 
 				}else{
 					ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
 				}
 			}});
 
 
 		unparqButton = (Button) findViewById(R.id.unparqbutton);
 		unparqButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 				builder.setMessage("Are you sure you want to unpark?")
 				.setCancelable(false)
 				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						//if they selected yes, call unpark()
 						int unparkResult= unpark();
 						if(unparkResult==1){
 							//reset ints
 							totalTimeParked=0;
 							remainSeconds=0;
 							minimalIncrement=0;
 							maxTime=0;
 							parkCost=0;
 
 							//change view back to initial one
 							vf.showPrevious();
 							vf.showPrevious();
 							//and set user as not currently parked
 							SharedPreferences.Editor editor = check.edit();
 							editor.putBoolean("parkState", false);
 							editor.putString("TIMER", "");
 							editor.commit();
 						}else{
 							Toast.makeText(MainActivity.this, unparkResult, Toast.LENGTH_LONG);
 						}
 					}
 				})
 				.setNegativeButton("No", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}});
 
 
 		refillButton = (Button) findViewById(R.id.refillbutton);
 		refillButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if(totalTimeParked<maxTime){
 					NumberPickerDialog y = new NumberPickerDialog(MainActivity.this, 0,0);
 					y.setRange(mySpot.getMinIncrement(), mySpot.getMaxTime()-totalTimeParked);
 					y.setMinInc(mySpot.getMinIncrement());
 					y.setOnNumberSetListener(mRefillListener);
 					y.show();
 				}else{
 					ThrowDialog.show(MainActivity.this, ThrowDialog.MAX_TIME);
 				}
 			}});
 
 		cancelPark = (Button) findViewById(R.id.cancel);
 		cancelPark.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				myRateResponse=null;
 				mySpot=null;
 				remainSeconds = 0;
 				totalTimeParked = 0;
 				//return to previous view
 				vf.showPrevious();
 			}});
 
 		/*these buttons appear on the first park, not refills.*/
 		pickerIncButton = (Button) findViewById(R.id.plus);
 		pickerIncButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				plusTime();
 			}
 		});
 		pickerDecButton = (Button) findViewById(R.id.minus);
 		pickerDecButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				minusTime();
 			}
 		});
 
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		startGettingLocation();
 	}
 
 	private int getParkMins() {
 		int h, m;
 		try {
 			h = Integer.valueOf(hours.getText().toString());
 		} catch (NumberFormatException e) {
 			h = 0;
 		}
 		try {
 			m = Integer.valueOf(minutes.getText().toString());
 		} catch (NumberFormatException e) {
 			m = 0;
 		}
 		int time = h*60+m;
 		time -= time % mySpot.getMinIncrement();
 		return time;
 	}
 
 	private void plusTime() {
 		if(hours.hasFocus()){
 			//update the parking minutes and seconds
 			remainSeconds+=60*60;
 //			updateDisplay(Math.min(maxTime, getParkMins()+60));
 			updateDisplay(getParkMins()+60);
 		} else {
 			remainSeconds+=minimalIncrement*60;
 //			updateDisplay(Math.min(maxTime, getParkMins()+minimalIncrement));
 			updateDisplay(getParkMins()+minimalIncrement);
 		}
 	}
 
 	private void minusTime() {
 		if(hours.hasFocus()){
 			//update the parking minutes and seconds
 			remainSeconds-=60*60;
 			updateDisplay(Math.max(minimalIncrement, getParkMins()-60));
 		} else {
 			remainSeconds-=minimalIncrement*60;
 			updateDisplay(Math.max(minimalIncrement, getParkMins()-minimalIncrement));
 		}
 	}
 
 
 	private static boolean isLocationProviderAvailable(LocationManager locationManager, String provider) {
 		return locationManager.getProvider(provider) != null && locationManager.isProviderEnabled(provider);
 	}
 
 	private void startGettingLocation() {
 		if (isLocationProviderAvailable(locationManager, LocationManager.GPS_PROVIDER)) {
 			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
 		} else if (isLocationProviderAvailable(locationManager, LocationManager.NETWORK_PROVIDER)) {
 			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
 		} else {
 			ThrowDialog.show(this, ThrowDialog.NO_LOCATION);
 		}
 	}
 
 	private void stopGettingLocation() {
 		locationManager.removeUpdates(this);
 	}
 
 	private AlertDialog makeRefillDialog(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 		builder.setMessage("You are almost out of time!")
 		.setCancelable(false)
 		.setPositiveButton("Refill", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				if(totalTimeParked<maxTime)
 					refillMe(1);
 				//refillMe(minimalIncrement);
 				else
 					ThrowDialog.show(MainActivity.this, ThrowDialog.MAX_TIME);
 				//mediaPlayer.stop();
 			}
 		})
 		.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 				//mediaPlayer.stop();
 			}
 		});
 		return builder.create();
 	}
 
 	/**
 	 * unpark and park are methods that handle ending/initializing
 	 * the background service, as well as sending queries to the server. 
 	 * So basically, changes that happen behind the app.*/
 	private int unpark(){
 		//grab the qr code and user email from saved info
 		SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 		String qrcode = check.getString("code", "badcode");
 		String email = check.getString("email", "bademail");
 		//use them to unpark the user serverside
 
 		if (ServerCalls.unPark(qrcode, email)==1){
 			try{
 				//stop the countdown timer and service
 				timer.cancel();
 				stopService(new Intent(MainActivity.this, Background.class));
 				//return happy
 				return 1;
 			}catch (Exception e){
 				return 0;
 			}
 		}else{
 			return 0;
 		}
 
 	}
 	private int park(){
 		//		//grab qr code content from saved info.  
 		//		SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 		//		String contents = check.getString("code", "");
 		//
 		//		String email = check.getString("email", "bademail");
 		//		//calculate when parking ends
 		//		globalEndTime = new Date();
 		//		String starttime = Global.sdf.format(globalEndTime);
 		//		String parkDate = Global.sdfDate.format(globalEndTime);		
 		//		globalEndTime.setSeconds(globalEndTime.getSeconds()+remainSeconds);
 		//		String endtime = Global.sdf.format(globalEndTime);
 
 		final SharedPreferences prefs = getSharedPreferences(SAVED_INFO, 0);
 
 		final ParkInstanceObject parkInstance = ServerCalls.park(mySpot.getSpot(), remainSeconds, prefs);
 		if(parkInstance != null){
 			//Toast.makeText(MainActivity.this, ""+ServerCalls.addHistory(parkDate, starttime, endtime, contents, email, parkCost), Toast.LENGTH_SHORT).show();
 			SharedPreferences.Editor edit = prefs.edit();
 			edit.putString("PARKID", String.valueOf(parkInstance.getParkInstanceId()));
 			edit.putString("TIMER", String.valueOf(parkInstance.getEndTime()));
 			edit.commit();
 			//			ServerCalls.addHistory(parkDate, starttime, endtime, contents, email, parkCost);
 			//return a happy response
 			return 1;
 		}else{
 			ThrowDialog.show(MainActivity.this, ThrowDialog.UNPARK_ERROR);
 			return 0;
 		}
 	}
 
 
 
 	private void refillMe(int refillMinutes){
 		//if we haven't gone past the total time we're allowed to park
 		if(totalTimeParked+refillMinutes<=maxTime){
 
 			//update the total time parked and remaining time.
 			parkCost = getCostInCents(getParkMins());
 			totalTimeParked+=refillMinutes;
 			remainSeconds+=refillMinutes*60;
 			updateDisplay(getParkMins()+refillMinutes);
 
 			//TODO:  DELETE ME TESTING CODE
 			//remainSeconds+=60;
 			//END TESTING CODE
 
 			//stop current timer, start new timer with current time + selectedNumber.
 			try{
 
 				SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 				Date oldEnd = Global.sdf.parse(check.getString("TIMER", ""));
 				oldEnd.setMinutes(oldEnd.getMinutes() + refillMinutes);
 				//calculate new endtime and initiate timer from it.  
 				timer.cancel();
 				timer = initiateTimer(oldEnd, vf);
 				stopService(new Intent(MainActivity.this, Background.class));
 				int testing = unpark();
 				int testing2 = park();
 				timer.start();
 				startService(new Intent(MainActivity.this, Background.class).putExtra("time", remainSeconds));
 				if(totalTimeParked==maxTime){
 					ThrowDialog.show(MainActivity.this, ThrowDialog.MAX_TIME);
 				}else{
 					ThrowDialog.show(MainActivity.this, ThrowDialog.REFILL_DONE);
 				}
 			}catch(Exception e){
 				ThrowDialog.show(MainActivity.this, ThrowDialog.UNPARK_ERROR);
 			}
 		}
 	}
 
 	private NumberPickerDialog.OnNumberSetListener mRefillListener =
 			new NumberPickerDialog.OnNumberSetListener() {
 		@Override
 		public void onNumberSet(int selectedNumber) {
 			if(selectedNumber>=minimalIncrement)
 				//TODO testing delete me
 				refillMe(1);
 			//refillMe(selectedNumber);
 		}
 	};
 
 
 	//once we scan the qr code
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 		if (scanResult != null) {
 			//call server using the qr code, to get a resulting spot's info.
 			String contents = scanResult.getContents();
 			if (contents != null) {
 				//contents contains string "parqme.com/p/c36/p123456" or w/e...
 				SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 				myRateResponse = ServerCalls.getRateQr(contents, check);
 				mySpot = myRateResponse.getRateObject();
 				//if we get the object successfully
 				if(myRateResponse.getRateObject().equals("OK")){
 					vf.showNext();
 					//prepare time picker for this spot
 					parkTimePicker.setRange(mySpot.getMinIncrement(), mySpot.getMaxTime());
 					parkTimePicker.setMinInc(mySpot.getMinIncrement());
 
 					//initialize all variables to match spot
 					minimalIncrement=mySpot.getMinIncrement();
 					maxTime = mySpot.getMaxTime();
 
 					//TODO delete me testing
 					updateDisplay(1);
 					remainSeconds=getParkMins()*60;
 
 					//store some used info
 					SharedPreferences.Editor editor = check.edit();
 					editor.putString("code", contents);
 					editor.putFloat("lat", (float) mySpot.getLat());
 					editor.putFloat("lon", (float) mySpot.getLon());
 					editor.commit();
 				}else{
 					ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
 				}
 			} else {
 				//do nothing if the user doesn't scan and just cancels.
 				//call server using the qr code, to get a resulting spot's info.
 				contents = "3";
 				SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 				//contents contains string "parqme.com/p/c36/p123456" or w/e...
 				myRateResponse = ServerCalls.getRateQr(contents, check);
 				mySpot = myRateResponse.getRateObject();
 				//if we get the object successfully
 				if(myRateResponse.getRateObject().equals("OK")){
 					vf.showNext();
 					//prepare time picker for this spot
 					parkTimePicker.setRange(mySpot.getMinIncrement(), mySpot.getMaxTime());
 					parkTimePicker.setMinInc(mySpot.getMinIncrement());
 					//initialize all variables to match spot
 					minimalIncrement=mySpot.getMinIncrement();
 					maxTime = mySpot.getMaxTime();
 
 					updateDisplay(minimalIncrement);
 					remainSeconds=minimalIncrement*60;
 
 					//store some used info
 					SharedPreferences.Editor editor = check.edit();
 					editor.putString("code", contents);
 					editor.putFloat("lat", (float) mySpot.getLat());
 					editor.putFloat("lon", (float) mySpot.getLon());
 					editor.commit();
 				}else{
 					ThrowDialog.show(MainActivity.this, ThrowDialog.RESULT_ERROR);
 				}
 			}
 		}
 	}
 
 	/* THIS TIMER is only used for in-app visuals.  The actual server updating and such are
 	 * done via user button clicks, and the background service we run.  */
 	public CountDownTimer initiateTimer(Date endTime, final ViewFlipper myvf){
 		//creates the countdown timer
 		Date now = new Date();
 		int countDownSeconds = (int)(endTime.getTime() - now.getTime())/1000;
 		return new CountDownTimer(countDownSeconds*1000, 1000){
 			//on each 1 second tick, 
 			@Override
 			public void onTick(long arg0) {
 
 				int seconds = (int)arg0/1000;
 				//if the time is what our warning-time is set to
 				if(seconds==warnTime&&SavedInfo.autoRefill(MainActivity.this)==false){
 					//alert the user
 					alert.show();
 				}
 				//update remain seconds and timer.
 				remainSeconds = seconds;
 				timeDisplay.setText(formatMe(seconds));
 			}
 			//on last tick,
 			//TODO calling alert.cancel() or throwdialog when app isn't in forefront may cause crash?
 			@Override
 			public void onFinish() {
 				//timeDisplay.setText("0:00:00");
 				SharedPreferences check = getSharedPreferences(SAVED_INFO,0);
 				//if autorefill is on, refill the user minimalIncrement
 				if(check.getBoolean("autoRefill", false)){
 					alert.cancel();
 					refillMe(1);
 				}else{
 					SavedInfo.eraseTimer(MainActivity.this);
 					//else we cancel the running out of tie dialog
 					alert.cancel();
 					//and restore view
 					myvf.showPrevious();
 					myvf.showPrevious();
 					ThrowDialog.show(MainActivity.this, ThrowDialog.TIME_OUT);
 				}
 			}
 
 
 		};
 
 	}
 	public static void warnMe(Context activity){
 		SharedPreferences check = activity.getSharedPreferences(SAVED_INFO,0);
 		//if time warning is enabled
 		if(check.getBoolean("warningEnable", false)){
 			//vibrate if settings say so
 			if(check.getBoolean("vibrateEnable", false)){
 				((Vibrator)activity.getSystemService(VIBRATOR_SERVICE)).vibrate(1000);
 			}
 			if(check.getBoolean("ringEnable", false)){
 				mediaPlayer.start();
 			}
 		}
 	}
 
 
 	//converts cents to "$ x.xx"
	public String formatCents(int m) {
		final String dollars = String.valueOf(m / 100);
		String cents = String.valueOf(m % 100);
		if (m % 100 < 10) {
			cents = '0' + cents;
 		}
		return '$'+dollars+'.'+cents;
 	}
 	//converts seconds to "H:mm:ss"
 	public static String formatMe(int seconds){
 		SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
 		try {
 			Date x = sdf.parse("00:00:00");
 			x.setSeconds(seconds);
 			return (sdf.format(x));
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		return "BADBADBAD";
 	}
 	//TODO:  THIN METHOD -- will become robust later.  must support varying units and different parking minutes and all day parking etc.
 	private static int getCostInCents(int mins) {
 		//number of minutes parked, divided by the minimum increment (aka unit of time), multiplied by price per unit in cents
 		return (mins/(mySpot.getMinIncrement())) * mySpot.getDefaultRate();
 	}
 	private void updateDisplay(int time) {
 		hours.setText(String.valueOf(time/60));
 		minutes.setText(String.valueOf(time%60));
		price.setText(formatCents(getCostInCents(time)));
 	}
 
 	@Override
 	public void onLocationChanged(Location location) {
 		if (location != null && (lastLocation == null || location.getAccuracy() < lastLocation.getAccuracy())) {
 			lastLocation = location;
 			if (location.getAccuracy() <= LOCATION_ACCURACY) {
 				stopGettingLocation();
 				goodLocation = true;
 			}
 		}
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 	}
 }
