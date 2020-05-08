 package com.c1.charityworkout;
 
 import com.c1.charityworkout.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.gesture.GestureOverlayView;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Screen3 extends Activity implements OnClickListener,
 		OnTouchListener {
 
 	// Variables for Swipe Gesture
 	float startX, endX;
 	GestureOverlayView main;
 
 	// Variables for Timer & Other stats
 	long currentTime = 0, pauseTime = 0, secondsCalc = 0;
 	static long newTime = 0;
 	int minTimer = 0, donationPerKm;
 	Button bStart, bStop;
 	String seconds = "00", minutes = "00", pauseMessage, stopWarningMsg,
 			stopMessage, timerText, totalDistance, averageSpeed, amountDonated,
 			choice, workout;
 	Thread timer;
 	static Boolean startW = false;
 	TextView timerView, distanceView, speedView, amountView;
 	Bundle data;
 	Boolean threadFinished = true;
 	SharedPreferences getAmount;
 
 	// Variables of banner
 	ImageView imgView;
 	private int banner;
 
 	// Variables of Result
 	Bundle resultSend, getChoice;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.screen_3);
 		rendering();
 	}
 
 	private void rendering() {
 		// TODO Auto-generated method stub
 		getBundle();
 		getPreferences();
 		timerView = (TextView) findViewById(R.id.timerView);
 		distanceView = (TextView) findViewById(R.id.distanceView);
 		amountView = (TextView) findViewById(R.id.amountView);
 		speedView = (TextView) findViewById(R.id.speedView);
 		imgView = (ImageView) findViewById(R.id.imageView2);
 		bStart = (Button) findViewById(R.id.start);
 		bStart.setOnClickListener(this);
 		bStop = (Button) findViewById(R.id.stop);
 		bStop.setOnClickListener(this);
 		Drawable image2 = getResources().getDrawable(banner);
 		imgView.setImageDrawable(image2);
 		main = (GestureOverlayView) findViewById(R.id.gestureOverlayView1);
 		main.setOnTouchListener(this);
 		pauseMessage = getResources().getString(R.string.pauseWorkout);
 		stopWarningMsg = getResources().getString(R.string.stopWarning);
 		stopMessage = getResources().getString(R.string.stopWorkout);
 		startX = 0;
 		endX = 0;
 		timerText = minutes + ":" + seconds;
 	}
 
 	private void getPreferences() {
 		// TODO Auto-generated method stub
 		getAmount = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		if (choice.equals("Running")) {
 			donationPerKm = getAmount.getInt("donRunning", 0);
 		} else if (choice.equals("Cycling")) {
 			donationPerKm = getAmount.getInt("donCycling", 0);
 		}
 	}
 
 	private void getBundle() {
 		// TODO Auto-generated method stub
 		Bundle gotBundle = getIntent().getExtras();
 		choice = gotBundle.getString("choice");
 		if (choice.equals("Running")) {
 			banner = R.drawable.runningbanner;
 			workout = "Running";
 
 		} else if (choice.equals("Cycling")) {
 			banner = R.drawable.cyclingbanner;
 			workout = "Cycling";
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		while (secondsCalc / 60 > 1) {
 			minTimer++;
 		}
 
 	}
 
 	private void startTimer() {
 		timer = new Thread() {
 			public void run() {
 				currentTime = System.currentTimeMillis();
 				while (startW == true) {
 					try {
 						sleep(1000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} finally {
 						threadFinished = false;
 						newTime = ((System.currentTimeMillis() - currentTime) / 1000)
 								+ pauseTime;
 						secondsCalc = newTime - (60 * minTimer);
 						if (secondsCalc / 60 >= 1) {
 							minTimer++;
 							minutes = Integer.toString(minTimer);
 							if (minutes.length() == 1) {
 								minutes = "0" + minutes;
 								secondsCalc = secondsCalc - 60;
 							}
 						}
 						seconds = Long.toString(secondsCalc);
 						if (seconds.length() == 1) {
 							seconds = "0" + seconds;
 						}
 						timerView.post(new Runnable() {
 							public void run() {
 								if (startW == true) {
 									timerText = minutes + ":" + seconds;
 									timerView.setText(timerText);
 								}
 							}
 						});
 						distanceView.post(new Runnable() {
 							public void run() {
 								totalDistance = GoogleMapFragment.totalDistance;
 
 								if (totalDistance != null) {
 									totalDistance = totalDistance.substring(0,
 											totalDistance.indexOf(".") + 3);
 									distanceView
 											.setText(totalDistance + "  km");
 								} else {
 									distanceView.setText("0.00 km");
 								}
 							}
 						});
 						speedView.post(new Runnable() {
 							public void run() {
 								averageSpeed = GoogleMapFragment.averageSpeedString;
 
 								if (averageSpeed != null) {
 									averageSpeed = averageSpeed.substring(0,
 											averageSpeed.indexOf(".") + 2);
 									speedView.setText(averageSpeed + " km/u");
 								} else {
 									speedView.setText("0.0 km/h");
 								}
 							}
 						});
 						amountView.post(new Runnable() {
 							public void run() {
								
								if (averageSpeed != null) {
									
							
 								int amountOfKm = Integer.parseInt(averageSpeed
 										.substring(0, averageSpeed.indexOf(".")));
 								int totalAmount = amountOfKm * donationPerKm;
 								amountDonated = Integer.toString(totalAmount);
								}
 							}
 						});
 						threadFinished = true;
 					}
 				}
 			}
 		};
 		timer.start();
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		// TODO Auto-generated method stub
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 			startX = event.getX();
 			break;
 		case MotionEvent.ACTION_UP:
 			endX = event.getX();
 		}
 
 		if (startX != 0 && endX != 0) {
 			if (endX - startX > 120) {
 
 			}
 			if (endX - startX < -120) {
 
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		switch (arg0.getId()) {
 
 		case R.id.start:
 			if (GoogleMapFragment.gpsReady == true) {
 				if (startW != true) {
 					startW = true;
 					GoogleMapFragment.locTrack = true;
 					startTimer();
 					timerView.setText(timerText);
 					Toast.makeText(this, "Workout started", 1000).show();
 				}
 			} else {
 				Toast.makeText(this, "Please wait for GPS Fix", 2000).show();
 			}
 			break;
 		case R.id.stop:
 			if (startW != false) {
 				startW = false;
 				GoogleMapFragment.locTrack = false;
 				pauseTime = newTime;
 				Toast.makeText(Screen3.this, stopWarningMsg, 2000).show();
 				timerView.setText(timerText + " [" + pauseMessage + "]");
 			} else {
 
 				timerView.setText(timerText + " [" + stopMessage + "]");
 				timerText = "00:00";
 				while (threadFinished != true) {
 					// wait for thread to finish before resetting values.
 				}
 				pauseTime = 0;
 				minTimer = 0;
 				getResult();
 			}
 			break;
 		}
 	}
 
 	private void getResult() {
 		// TODO Auto-generated method stub
 		if (averageSpeed != null && totalDistance != null && timerText != null
 				&& amountDonated != null) {
 			resultSend = new Bundle();
 			resultSend.putString("workout", workout);
 			resultSend.putString("speed", averageSpeed);
 			resultSend.putString("distance", totalDistance);
 			resultSend.putString("timer", timerText);
 			resultSend.putString("amount", amountDonated);
 			Intent resultPage = new Intent(Screen3.this, ResultPage.class);
 			resultPage.putExtras(resultSend);
 			startActivity(resultPage);
 		}
 	}
 
 }
