 package edu.fsu.cs.fsu_class_heat;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.StringTokenizer;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.ContextThemeWrapper;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.SeekBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class MainActivity extends Activity {
 
 	Cursor mCursor;
 	GoogleMap map;
 	MarkerOptions mo_lov, mo_hcb,mo_caroth,mo_carr,mo_fish,mo_osb,mo_bel,mo_wel,mo_shor,mo_rova,mo_rovb;
 
 
 	// Integers to keep count of the number of class
 	int HCB = 0;
 	int LOV = 0;
 	int CAR = 0;
 	int FLH = 0;
 	int OSB = 0;
 	int BEL = 0;
 	int HWC = 0;
 	int LSB = 0;
 	int RBA = 0;
 	int RBB = 0;
 	int MCH = 0;
 
 	// building codes
 	static final int lovebc = 116;
 	static final int carothbc = 55;
 	static final int carabc = 113;
 	static final int fishbc = 37;
 	static final int rogbc = 36;
 	static final int belbc = 8;
 	static final int hbcbc = 4009;
 	static final int welbc = 9999;
 	static final int shorbc = 19;
 	static final int rosabc = 23;
 	static final int rosbbc = 52;
 
 	// Day of week constant codes
 	final int MONDAY = 0;
 	final int TUESDAY = 1;
 	final int WEDNESDAY = 2;
 	final int THURSDAY = 3;
 	final int FRIDAY = 4;
 
 	// Semester constant codes
 	final int SPRING = 0;
 	final int SUMMER = 1;
 	final int FALL = 2;
 
 	// user selection global variables
 	int daySelection = MONDAY;
 	int semesterSelection = SPRING;
 	int hourSelection = 0;
 	int minuteSelection = 0;
 	int amPmSelection = 0; // 0 is am, 1 is pm
 
 	// Actionbar spinners
 	private MenuItem spinnerSemester = null;
 	private MenuItem spinnerDay = null;
 	Spinner spinner1;
 	Spinner spinner2;
 
 	// Seekbar
 	SeekBar seekBar;
 	TextView textViewTime;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// set the actionbar
 		getActionBar().setDisplayShowTitleEnabled(false);
 		// end set action bar
 
 		// set up seekbar
 		seekBar = (SeekBar) findViewById(R.id.seekBar1);
 		textViewTime = (TextView) findViewById(R.id.textView1);
 		textViewTime.setText("" + "12" + ":" + "00" + " AM");
 		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
 			public void onStopTrackingTouch(SeekBar bar) {
 				int value = bar.getProgress(); // the value of the seekBar
 												// progress
 
 				if (amPmSelection == 0)
 					Toast.makeText(
 							getApplicationContext(),
 							String.format("%02d", hourSelection) + ":"
 									+ String.format("%02d", minuteSelection)
 									+ " AM", Toast.LENGTH_SHORT).show();
 				else
 					Toast.makeText(
 							getApplicationContext(),
 							String.format("%02d", hourSelection) + ":"
 									+ String.format("%02d", minuteSelection)
 									+ " PM", Toast.LENGTH_SHORT).show();
 
 				// delete toast and run query on time
 			}
 
 			public void onStartTrackingTouch(SeekBar bar) {
 
 			}
 
 			public void onProgressChanged(SeekBar bar, int paramInt,
 					boolean paramBoolean) {
 				if (paramInt == 100)
 					paramInt = 99;// handle the wraparound
 				int numMinutes = (paramInt * (24 * 60)) / 100;
 				int numHours = numMinutes / 60;
 				int amOrPm = numHours / 12;
 				numMinutes = ((numMinutes % 60) / 15) * 15;
 				if (numHours == 0 || numHours == 12)
 					numHours = 12;
 				else
 					numHours = numHours % 12;
 				hourSelection = numHours;
 				minuteSelection = numMinutes;
 				amPmSelection = amOrPm;
 
 				if (amPmSelection == 0) {
 
 					Log.i("current", "numMinutes = " + numMinutes);
 
 					textViewTime.setText(""
 							+ String.format("%02d", hourSelection) + ":"
 							+ String.format("%02d", minuteSelection) + " AM");
 				} else {
 
 					Log.i("current", "set up");
 
 					textViewTime.setText(""
 							+ String.format("%02d", hourSelection) + ":"
 							+ String.format("%02d", minuteSelection) + " PM");
 				}
 
 			}
 		}); // end set up seekbar
 
 		// get the map fragment
 		map = ((MapFragment) getFragmentManager()
 				.findFragmentById(R.id.mapView)).getMap();
 
 		// set the map fragment to FSU
 		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
 		LatLng loc = new LatLng(30.44388, -84.29806);
 		CameraUpdate center = CameraUpdateFactory.newLatLng(loc);
 		CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
 
 		map.moveCamera(center);
 		map.animateCamera(zoom);
 		// end set up map fragment
 
		marker('T', 1000, 1);
 	}// end onCreate
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		MenuInflater mi = getMenuInflater();
 		mi.inflate(R.menu.main, menu);
 
 		// Semester Spinner
 		spinnerSemester = menu.findItem(R.id.menuSem);
 		View view1 = spinnerSemester.getActionView();
 		if (view1 instanceof Spinner) {
 			spinner1 = (Spinner) view1;
 			ArrayAdapter<CharSequence> adapter = ArrayAdapter
 					.createFromResource(new ContextThemeWrapper(this,
 							android.R.style.Theme_Holo), R.array.sem_options,
 							android.R.layout.simple_spinner_item);
 			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			spinner1.setAdapter(adapter);
 
 			spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 				@Override
 				public void onItemSelected(AdapterView<?> arg0, View arg1,
 						int arg2, long arg3) {
 					// TODO Auto-generated method stub
 
 					switch (arg2) {
 					case SPRING:
 						semesterSelection = SPRING;
 						break;
 					case SUMMER:
 						semesterSelection = SUMMER;
 						break;
 					case FALL:
 						semesterSelection = FALL;
 						break;
 					}
 
 				}
 
 				@Override
 				public void onNothingSelected(AdapterView<?> arg0) {
 					// TODO Auto-generated method stub
 				}
 			});
 
 		}// end Semester Spinner
 
 		// Day Spinner
 		spinnerDay = menu.findItem(R.id.menuDay);
 		View view2 = spinnerDay.getActionView();
 		if (view2 instanceof Spinner) {
 			spinner2 = (Spinner) view2;
 			ArrayAdapter<CharSequence> adapter = ArrayAdapter
 					.createFromResource(new ContextThemeWrapper(this,
 							android.R.style.Theme_Holo), R.array.day_options,
 							android.R.layout.simple_spinner_item);
 			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			spinner2.setAdapter(adapter);
 
 			spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 				@Override
 				public void onItemSelected(AdapterView<?> arg0, View arg1,
 						int arg2, long arg3) {
 					// TODO Auto-generated method stub
 
 					switch (arg2) {
 					case MONDAY:
 						daySelection = MONDAY;
 						break;
 					case TUESDAY:
 						daySelection = TUESDAY;
 						break;
 					case WEDNESDAY:
 						daySelection = WEDNESDAY;
 						break;
 					case THURSDAY:
 						daySelection = THURSDAY;
 						break;
 					case FRIDAY:
 						daySelection = FRIDAY;
 						break;
 					}
 
 				}
 
 				@Override
 				public void onNothingSelected(AdapterView<?> arg0) {
 					// TODO Auto-generated method stub
 				}
 			});
 
 		}// end Day Spinner
 		return true;
 	}// end options menu
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menuCurrent:
 			setCurrent();
 			marker('M', 1303, 1);
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public boolean setCurrent() {
 		int time = getTime();
 		char day = getDay();
 		int seekProgress = 0;
 
 		if (day == 'M') {
 			spinner2.setSelection(MONDAY);
 			daySelection = MONDAY;
 		} else if (day == 'T') {
 			spinner2.setSelection(TUESDAY);
 			daySelection = TUESDAY;
 		} else if (day == 'W') {
 			spinner2.setSelection(WEDNESDAY);
 			daySelection = WEDNESDAY;
 		} else if (day == 'R') {
 			spinner2.setSelection(THURSDAY);
 			daySelection = THURSDAY;
 		} else if (day == 'F') {
 			spinner2.setSelection(FRIDAY);
 			daySelection = FRIDAY;
 		}
 
 		Time currentTime = new Time();
 		currentTime.setToNow();
 		int monthInt = currentTime.month + 1;
 		int dateInt = currentTime.monthDay;
 
 		if (monthInt <= 5) {
 			semesterSelection = SPRING;
 			spinner1.setSelection(SPRING);
 		} else if (monthInt == 6 || monthInt == 7
 				|| (monthInt == 8 && dateInt < 15)) {
 			semesterSelection = SUMMER;
 			spinner1.setSelection(SUMMER);
 		} else {
 			semesterSelection = FALL;
 			spinner1.setSelection(FALL);
 		}
 		amPmSelection = time / 1200;
 		hourSelection = time / 100;
 		hourSelection %= 12;
 		if (hourSelection == 0)
 			hourSelection = 12;
 		minuteSelection = ((time % 100) / 15) * 15; // round to nearest 15
 
 		Log.i("current", "time = " + time);
 		Log.i("current", "minuteSelection = " + minuteSelection);
 		Log.i("current",
 				"String.format = " + String.format("%02d", minuteSelection));
 
 			/*
 		 * if ((time % 100 % 15 / 7) > 0) minuteSelection += 15;
 		 * 
 		 * if (minuteSelection == 60) {// correct rounding minuteSelection = 0;
 		 * hourSelection += 1;
 		 * 
 		 * if (hourSelection == 12) { if (amPmSelection == 1) amPmSelection = 0;
 		 * else amPmSelection = 1; } else if (hourSelection == 13) {
 		 * hourSelection = 1; } }// end correct rounding
 		 */
 		
 		seekProgress = (((((hourSelection % 12) + (amPmSelection * 12)) * 60) + minuteSelection + 15) * 100)
 				/ (24 * 60);
 
 		if (amPmSelection == 0) {
 			textViewTime.setText("" + String.format("%02d", hourSelection)
 					+ ":" + String.format("%02d", minuteSelection) + " AM");
 		} else {
 			textViewTime.setText("" + String.format("%02d", hourSelection)
 					+ ":" + String.format("%02d", minuteSelection) + " PM");
 		}
 
 		seekBar.setProgress(seekProgress);
 
 		return true;
 	}
 
 	// returns integer time hhmm
 	public int getTime() {
 		Time now = new Time();
 		now.setToNow();
 		String timestr = now.format("%H%M");
 		int timeint = Integer.valueOf(timestr);
 
 		// or we could Query(timeint);
 
 		return timeint;
 	}
 
 	public char getDay() {
 
 		char daychar = ' ';
 		Time day = new Time();
 
 		day.setToNow();
 
 		int dayint = day.weekDay;
 
 		if (dayint == 1) {
 			daychar = 'M';
 		} else if (dayint == 2) {
 			daychar = 'T';
 		} else if (dayint == 3) {
 			daychar = 'W';
 		} else if (dayint == 4) {
 			daychar = 'R';
 		} else if (dayint == 5) {
 			daychar = 'F';
 		} else {
 			daychar = 'M';
 		}
 
 		return daychar;
 	}
 
 	// Count the number of classes going on at a given time
 	// Call example: Monday 3:00 PM Spring will be count_classes('M', 1500, 0);
 	public void count_classes(char CURRENT_DAY, int CURRENT_TIME,
 			int CURRENT_SEMESTER) {
 
 		// Main loop: iterate through contentprovider and take count of classes
 		mCursor = getContentResolver().query(class_database.CONTENT_URI, null,
 				null, null, null);
 		mCursor.moveToFirst();
 
 		// Reset counts
 		HCB = 0;
 		LOV = 0;
 		CAR = 0;
 		FLH = 0;
 		OSB = 0;
 		BEL = 0;
 		HWC = 0;
 		LSB = 0;
 		RBA = 0;
 		RBB = 0;
 		MCH = 0;
 
 		while (mCursor.isAfterLast() == false) {
 
 			// Get data from database
 			String CLASS_DAYS = mCursor.getString(2).trim();
 			int CLASS_BEGIN = Integer.valueOf(mCursor.getString(3).trim());
 			int CLASS_END = Integer.valueOf(mCursor.getString(4).trim());
 			String getSemester = mCursor.getString(5).trim();
 			int CLASS_SEMESTER = 0;
 
 			if (getSemester.equals("spring"))
 				CLASS_SEMESTER = 0;
 			else if (getSemester.equals("summer"))
 				CLASS_SEMESTER = 1;
 			else if (getSemester.equals("fall"))
 				CLASS_SEMESTER = 2;
 
 			if (mCursor.getString(1).equals("HCB")) {
 				if (CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
 					if (CLASS_BEGIN <= CURRENT_TIME
 							&& CURRENT_TIME <= CLASS_END) {
 						if (CLASS_SEMESTER == CURRENT_SEMESTER) {
 							HCB++;
 						}
 					}
 				}
 			} else if (mCursor.getString(1).equals("LOV")) {
 				if (CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
 					if (CLASS_BEGIN <= CURRENT_TIME
 							&& CURRENT_TIME <= CLASS_END) {
 						if (CLASS_SEMESTER == CURRENT_SEMESTER) {
 							LOV++;
 						}
 					}
 				}
 			} else if (mCursor.getString(1).equals("CAR")) {
 				if (CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
 					if (CLASS_BEGIN <= CURRENT_TIME
 							&& CURRENT_TIME <= CLASS_END) {
 						if (CLASS_SEMESTER == CURRENT_SEMESTER) {
 							CAR++;
 						}
 					}
 				}
 			} else if (mCursor.getString(1).equals("FLH")) {
 				if (CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
 					if (CLASS_BEGIN <= CURRENT_TIME
 							&& CURRENT_TIME <= CLASS_END) {
 						if (CLASS_SEMESTER == CURRENT_SEMESTER) {
 							FLH++;
 						}
 					}
 				}
 			} else if (mCursor.getString(1).equals("OSB")) {
 				if (CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
 					if (CLASS_BEGIN <= CURRENT_TIME
 							&& CURRENT_TIME <= CLASS_END) {
 						if (CLASS_SEMESTER == CURRENT_SEMESTER) {
 							OSB++;
 						}
 					}
 				}
 			} else if (mCursor.getString(1).equals("BEL")) {
 				if (CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
 					if (CLASS_BEGIN <= CURRENT_TIME
 							&& CURRENT_TIME <= CLASS_END) {
 						if (CLASS_SEMESTER == CURRENT_SEMESTER) {
 							BEL++;
 						}
 					}
 				}
 			} else if (mCursor.getString(1).equals("HWC")) {
 				if (CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
 					if (CLASS_BEGIN <= CURRENT_TIME
 							&& CURRENT_TIME <= CLASS_END) {
 						if (CLASS_SEMESTER == CURRENT_SEMESTER) {
 							HWC++;
 						}
 					}
 				}
 			} else if (mCursor.getString(1).equals("LSB")) {
 				if (CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
 					if (CLASS_BEGIN <= CURRENT_TIME
 							&& CURRENT_TIME <= CLASS_END) {
 						if (CLASS_SEMESTER == CURRENT_SEMESTER) {
 							LSB++;
 						}
 					}
 				}
 			} else if (mCursor.getString(1).equals("RBA")) {
 				if (CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
 					if (CLASS_BEGIN <= CURRENT_TIME
 							&& CURRENT_TIME <= CLASS_END) {
 						if (CLASS_SEMESTER == CURRENT_SEMESTER) {
 							RBA++;
 						}
 					}
 				}
 			} else if (mCursor.getString(1).equals("RBB")) {
 				if (CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
 					if (CLASS_BEGIN <= CURRENT_TIME
 							&& CURRENT_TIME <= CLASS_END) {
 						if (CLASS_SEMESTER == CURRENT_SEMESTER) {
 							RBB++;
 						}
 					}
 				}
 			} else if (mCursor.getString(1).equals("MCH")) {
 				if (CLASS_DAYS.contains(Character.toString(CURRENT_DAY))) {
 					if (CLASS_BEGIN <= CURRENT_TIME
 							&& CURRENT_TIME <= CLASS_END) {
 						if (CLASS_SEMESTER == CURRENT_SEMESTER) {
 							MCH++;
 						}
 					}
 				}
 			}
 			mCursor.moveToNext();
 		}
 		mCursor.close();
 	}
 
 	// //place the static final variables up top
 
 	LatLng dotlocation(int buildingcode) {
 
 		// All SW and NE points
 		// Love
 		double lovelathigh = 30.446112;
 		double lovelonhigh = -84.299222;
 		double lovelatlow = 30.446084;
 		double lovelonlow = -84.299877;
 
 		// Carothers
 		// LatLng carothSW = new LatLng(30.445612,-84.300488);
 		double carothlathigh = 30.445612;
 		double carothlonhigh = -84.299555;
 		double carothlatlow = 30.445473;
 		double carothlonlow = -84.300488;
 		// LatLng carothNE = new LatLng(30.445473,-84.299555);
 
 		// Caraway
 		// LatLng caraSW = new LatLng(30.44514,-84.29889);
 		// LatLng caraNE = new LatLng(30.445131,-84.298439);
 		double caralathigh = 30.44514;
 		double caralonhigh = -84.298439;
 		double caralatlow = 30.445131;
 		double caralonlow = -84.29889;
 
 		// Fischer lecture hall
 		// LatLng fishSW = new LatLng(30.444095,-84.300735);
 		// LatLng fishNE = new LatLng(30.444308,-84.300402);
 		double fishlathigh = 30.444308;
 		double fishlonhigh = -84.300402;
 		double fishlatlow = 30.444095;
 		double fishlonlow = -84.300735;
 
 		// Rogers
 		// LatLng rogSW = new LatLng(30.443984,-84.300284);
 		// LatLng rogNE = new LatLng(30.444114,-84.299909);
 		double roglathigh = 30.444114;
 		double roglonhigh = -84.299909;
 		double roglatlow = 30.443984;
 		double roglonlow = -84.300284;
 
 		// Bellamy
 		// LatLng belSW = new LatLng(30.44293,-84.296025);
 		// LatLng belNE = new LatLng(30.443559,-84.29566);
 		double bellathigh = 30.443559;
 		double bellonhigh = -84.29566;
 		double bellatlow = 30.44293;
 		double bellonlow = -84.296025;
 
 		// HCB
 		// LatLng hcbSW = new LatLng(30.443041,-84.297634);
 		// LatLng hcbNE = new LatLng(30.443346,-84.296669);
 		double hcblathigh = 30.443346;
 		double hcblonhigh = -84.296669;
 		double hcblatlow = 30.443041;
 		double hcblonlow = -84.297634;
 
 		// Wellness center
 		// LatLng wellSW = new LatLng(30.441468,-84.299608);
 		// LatLng wellNE = new LatLng(30.441875,-84.298911);
 		double wellathigh = 30.441875;
 		double wellonhigh = -84.298911;
 		double wellatlow = 30.441468;
 		double wellonlow = -84.299608;
 
 		// Shores
 		// LatLng shorSW = new LatLng(30.44096,-84.296154);
 		// LatLng shorNE = new LatLng(30.441376,-84.295853);
 		double shorlathigh = 30.441376;
 		double shorlonhigh = -84.295853;
 		double shorlatlow = 30.44096;
 		double shorlonlow = -84.296154;
 
 		// Rovetta business building A
 		// LatLng rovaSW = new LatLng(30.444336,-84.296336);
 		// LatLng rovaNE = new LatLng(30.444206,-84.295413);
 		double rovalathigh = 30.444336;
 		double rovalonhigh = -84.295413;
 		double rovalatlow = 30.444206;
 		double rovalonlow = -84.296336;
 
 		// Rovetta business building B
 		// LatLng rovbSW = new LatLng(30.443725,-84.295521);
 		// LatLng rovbNE = new LatLng(30.443966,-84.295092);
 		double rovblathigh = 30.443966;
 		double rovblonhigh = -84.295092;
 		double rovblatlow = 30.443725;
 		double rovblonlow = -84.295521;
 
 		switch (buildingcode) {
 		case lovebc:
 			double lovegenlat = (double) (Math.random()
 					* (lovelathigh - lovelatlow) + lovelatlow);
 			double lovegenlon = (double) (Math.random()
 					* (lovelonhigh - lovelonlow) + lovelonlow);
 			Log.d("maps", "Love lat: " + lovegenlat);
 			Log.d("maps", "Love lon: " + lovegenlon);
 			LatLng lovegen = new LatLng(lovegenlat, lovegenlon);
 			return lovegen;
 			// break;
 		case carothbc:
 			double carothgenlat = (double) (Math.random()
 					* (carothlathigh - carothlatlow) + carothlatlow);
 			double carothgenlon = (double) (Math.random()
 					* (carothlonhigh - carothlonlow) + carothlonlow);
 			LatLng carothgen = new LatLng(carothgenlat, carothgenlon);
 			return carothgen;
 			// break;
 		case carabc:
 			double caragenlat = (double) (Math.random()
 					* (caralathigh - caralatlow) + caralatlow);
 			double caragenlon = (double) (Math.random()
 					* (caralonhigh - caralonlow) + caralonlow);
 			LatLng caragen = new LatLng(caragenlat, caragenlon);
 			return caragen;
 			// break;
 		case fishbc:
 			double fishgenlat = (double) (Math.random()
 					* (fishlathigh - fishlatlow) + fishlatlow);
 			double fishgenlon = (double) (Math.random()
 					* (fishlonhigh - fishlonlow) + fishlonlow);
 			LatLng fishgen = new LatLng(fishgenlat, fishgenlon);
 			return fishgen;
 			// break;
 		case rogbc:
 			double roggenlat = (double) (Math.random()
 					* (roglathigh - roglatlow) + roglatlow);
 			double roggenlon = (double) (Math.random()
 					* (roglonhigh - roglonlow) + roglonlow);
 			LatLng roggen = new LatLng(roggenlat, roggenlon);
 			return roggen;
 			// break;
 		case belbc:
 			double belgenlat = (double) (Math.random()
 					* (bellathigh - bellatlow) + bellatlow);
 			double belgenlon = (double) (Math.random()
 					* (bellonhigh - bellonlow) + bellonlow);
 			LatLng belgen = new LatLng(belgenlat, belgenlon);
 			return belgen;
 			// break;
 		case hbcbc:
 			double hcbgenlat = (double) (Math.random()
 					* (hcblathigh - hcblatlow) + hcblatlow);
 			double hcbgenlon = (double) (Math.random()
 					* (hcblonhigh - hcblonlow) + hcblonlow);
 			LatLng hcbgen = new LatLng(hcbgenlat, hcbgenlon);
 			return hcbgen;
 			// break;
 		case welbc:
 			double welgenlat = (double) (Math.random()
 					* (wellathigh - wellatlow) + wellatlow);
 			double welgenlon = (double) (Math.random()
 					* (wellonhigh - wellonlow) + wellonlow);
 			LatLng welgen = new LatLng(welgenlat, welgenlon);
 			return welgen;
 			// break;
 		case shorbc:
 			double shorgenlat = (double) (Math.random()
 					* (shorlathigh - shorlatlow) + shorlatlow);
 			double shorgenlon = (double) (Math.random()
 					* (shorlonhigh - shorlonlow) + shorlonlow);
 			LatLng shorgen = new LatLng(shorgenlat, shorgenlon);
 			return shorgen;
 			// break;
 		case rosabc:
 			double rovagenlat = (double) (Math.random()
 					* (rovalathigh - rovalatlow) + rovalatlow);
 			double rovagenlon = (double) (Math.random()
 					* (rovalonhigh - rovalonlow) + rovalonlow);
 			LatLng rovagen = new LatLng(rovagenlat, rovagenlon);
 			return rovagen;
 			// break;
 		case rosbbc:
 			double rovbgenlat = (double) (Math.random()
 					* (rovblathigh - rovblatlow) + rovblatlow);
 			double rovbgenlon = (double) (Math.random()
 					* (rovblonhigh - rovblonlow) + rovblonlow);
 			LatLng rovbgen = new LatLng(rovbgenlat, rovbgenlon);
 			return rovbgen;
 			// break;
 
 		default:
 			LatLng def = new LatLng(0.0, 0.0);
 			return def;
 			// break;
 		}
 	}
 	
 	public void marker(char day, int time, int semester)
 	{
 
		map.clear();
 		// ***** Database Portion *****
 		mCursor = getContentResolver().query(class_database.CONTENT_URI, null,
 				null, null, null);
 
 		if (mCursor != null) {
 
 			// if contentprovider is empty, load the dataset into content
 			// provider
 			// if contentprovider is not empty, there is no need to import the
 			// dataset
 			if (mCursor.getCount() <= 0) {
 				Uri mNewUri;
 				ContentValues mNewValues = new ContentValues();
 
 				// Retrieve dataset textfile in res/raw/class_dataset.txt
 				InputStream inputStream = this.getResources().openRawResource(
 						R.raw.class_dataset);
 				InputStreamReader inputreader = new InputStreamReader(
 						inputStream);
 				BufferedReader buffreader = new BufferedReader(inputreader);
 
 				String line;
 
 				// Iterate each line of the dataset file
 				// Split each line into tokens and load into the database
 				try {
 					while ((line = buffreader.readLine()) != null) {
 						StringTokenizer line_tokens = new StringTokenizer(line,
 								",");
 
 						mNewValues.put(class_database.COLUMN_BUILDING,
 								line_tokens.nextToken());
 						mNewValues.put(class_database.COLUMN_DAYS,
 								line_tokens.nextToken());
 						mNewValues.put(class_database.COLUMN_BEGIN,
 								line_tokens.nextToken());
 						mNewValues.put(class_database.COLUMN_END,
 								line_tokens.nextToken());
 						mNewValues.put(class_database.COLUMN_SEMESTER,
 								line_tokens.nextToken());
 						mNewUri = getContentResolver().insert(
 								class_database.CONTENT_URI, mNewValues);
 					}
 				} catch (IOException e) {
 
 				}
 
 				// Reinitialize database cursor
 				mCursor = getContentResolver().query(
 						class_database.CONTENT_URI, null, null, null, null);
 			}
 		}
 		mCursor.close();
 
 		// ***** End of Database Portion *****
 
 		LatLng love = new LatLng(30.446056,-84.299587);
 		LatLng hcb = new LatLng(30.443226,-84.297034);
 		LatLng carr = new LatLng(30.445113,-84.298729);
 		LatLng caroth = new LatLng(30.445501,-84.300059);
 		LatLng rova = new LatLng(30.444225,-84.295864);
 		LatLng rovb = new LatLng(30.443762,-84.295349);
 		LatLng bel = new LatLng(30.443032,-84.295864);
 		LatLng shor = new LatLng(30.441006,-84.296025);
 		LatLng osb = new LatLng(30.443975,-84.300145);
 		LatLng fish = new LatLng(30.444086,-84.300628);
 		LatLng well = new LatLng(30.441579,-84.299169);
 
 		// count_classes(getDay(), getTime());
 		count_classes(day, time, semester);
 
 		Log.i("marker", String.valueOf(LOV));
 		Log.i("marker", String.valueOf(HCB));
 		Log.i("marker", String.valueOf(CAR));
 		Log.i("marker", String.valueOf(FLH));
 		Log.i("marker", String.valueOf(OSB));
 		Log.i("marker", String.valueOf(BEL));
 		Log.i("marker", String.valueOf(HWC));
 		Log.i("marker", String.valueOf(LSB));
 		Log.i("marker", String.valueOf(RBA));
 		Log.i("marker", String.valueOf(RBB));
 		Log.i("marker", String.valueOf(MCH));
 
 
 		if (LOV == 0) {
 			mo_lov = new MarkerOptions()
 					.title("Love Building")
 					.position(love)
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_RED))
 					.visible(false).draggable(false);
 		}
 
 		else if (LOV > 0 && LOV <= 10) {
 			mo_lov = new MarkerOptions()
 					.title("Love Building")
 					.position(love)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_blue))
 					.visible(true).draggable(false);
 		}
 
 		else if (LOV > 10 && LOV <= 20) {
 			mo_lov = new MarkerOptions()
 					.title("Love Building")
 					.position(love)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_geen))
 					.visible(true).draggable(false);
 		}
 
 		else if (LOV > 20 && LOV <= 30) {
 			mo_lov = new MarkerOptions()
 					.title("Love Building")
 					.position(love)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_yellow))
 					.visible(true).draggable(false);
 		}
 
 		else if (LOV > 30 && LOV <= 40) {
 			mo_lov = new MarkerOptions()
 					.title("Love Building")
 					.position(love)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(true).draggable(false);
 		}
 
 		else if (LOV > 40) {
 			mo_lov = new MarkerOptions()
 					.title("Love Building")
 					.position(love)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_red))
 					.visible(true).draggable(false);
 		}
 
 		if (HCB == 0) {
 			mo_hcb = new MarkerOptions()
 					.title("HCB")
 					.position(hcb)
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_RED))
 					.visible(false).draggable(false);
 		}
 
 		else if (HCB > 0 && HCB <= 10) {
 			mo_hcb = new MarkerOptions()
 					.title("HCB")
 					.position(hcb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_blue))
 					.visible(true).draggable(false);
 		}
 
 		else if (HCB > 10 && HCB <= 20) {
 			mo_hcb = new MarkerOptions()
 					.title("HCB")
 					.position(hcb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_geen))
 					.visible(true).draggable(false);
 		}
 
 		else if (HCB > 20 && HCB <= 30) {
 			mo_hcb = new MarkerOptions()
 					.title("HCB")
 					.position(hcb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_yellow))
 					.visible(true).draggable(false);
 		}
 		
 		else if (HCB > 30 && HCB <= 40) {
 			mo_hcb = new MarkerOptions()
 					.title("HCB")
 					.position(hcb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(true).draggable(false);
 		}
 
 		else if (HCB > 40) {
 			mo_hcb = new MarkerOptions()
 					.title("HCB")
 					.position(hcb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_red))
 					.visible(true).draggable(false);
 		}
 		
 		if (MCH == 0) {
 			mo_caroth = new MarkerOptions()
 					.title("Corothers")
 					.position(caroth)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(false).draggable(false);
 		}
 
 		else if (MCH > 0 && MCH <= 10) {
 			mo_caroth = new MarkerOptions()
 					.title("Corothers")
 					.position(caroth)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_blue))
 					.visible(true).draggable(false);
 		}
 
 		else if (MCH > 10 && MCH <= 20) {
 			mo_caroth = new MarkerOptions()
 					.title("Corothers")
 					.position(caroth)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_geen))
 					.visible(true).draggable(false);
 		}
 
 		else if (MCH > 20 && MCH <= 30) {
 			mo_caroth = new MarkerOptions()
 					.title("Corothers")
 					.position(caroth)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_yellow))
 					.visible(true).draggable(false);
 		}
 		
 		else if (MCH > 30 && MCH <= 40) {
 			mo_caroth = new MarkerOptions()
 					.title("Corothers")
 					.position(caroth)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(true).draggable(false);
 		}
 		
 		else if (MCH > 40) {
 			mo_caroth = new MarkerOptions()
 					.title("Corothers")
 					.position(caroth)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_red))
 					.visible(true).draggable(false);
 		}
 
 		if (CAR == 0) {
 			mo_carr = new MarkerOptions()
 					.title("Carraway")
 					.position(carr)
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_RED))
 					.visible(false).draggable(false);
 		}
 
 		else if (CAR > 0 && CAR <= 10) {
 			mo_carr = new MarkerOptions()
 					.title("Carraway")
 					.position(carr)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_blue))
 					.visible(true).draggable(false);
 		}
 
 		else if (CAR > 10 && CAR <= 20) {
 			mo_carr = new MarkerOptions()
 					.title("Carraway")
 					.position(carr)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_geen))
 					.visible(true).draggable(false);
 		}
 
 		else if (CAR > 20 && CAR <= 30) {
 			mo_carr = new MarkerOptions()
 					.title("Carraway")
 					.position(carr)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_yellow))
 					.visible(true).draggable(false);
 		}
 		
 		else if (CAR > 30 && CAR <= 40) {
 			mo_carr = new MarkerOptions()
 					.title("Carraway")
 					.position(carr)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(true).draggable(false);
 		}
 		
 		else if (CAR >  40) {
 			mo_carr = new MarkerOptions()
 					.title("Carraway")
 					.position(carr)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_red))
 					.visible(true).draggable(false);
 		}
 
 		if (BEL == 0) {
 			mo_bel = new MarkerOptions()
 					.title("Bellamy")
 					.position(bel)
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_RED))
 					.visible(false).draggable(false);
 		}
 
 		else if (BEL > 0 && BEL <= 10) {
 			mo_bel = new MarkerOptions()
 					.title("Bellamy")
 					.position(bel)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_blue))
 					.visible(true).draggable(false);
 		}
 
 		else if (BEL > 10 && BEL <= 20) {
 			mo_bel = new MarkerOptions()
 					.title("Bellamy")
 					.position(bel)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_geen))
 					.visible(true).draggable(false);
 		}
 
 		else if (BEL > 20 && BEL <= 30) {
 			mo_bel = new MarkerOptions()
 					.title("Bellamy")
 					.position(bel)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_yellow))
 					.visible(true).draggable(false);
 		}
 		
 		else if (BEL > 30 && BEL <= 40) {
 			mo_bel = new MarkerOptions()
 					.title("Bellamy")
 					.position(bel)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(true).draggable(false);
 		}
 		
 		else if (BEL > 40) {
 			mo_bel = new MarkerOptions()
 					.title("Bellamy")
 					.position(bel)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_red))
 					.visible(true).draggable(false);
 		}
 
 		if (RBA == 0) {
 			mo_rova = new MarkerOptions()
 					.title("Rovetta A")
 					.position(rova)
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_RED))
 					.visible(false).draggable(false);
 		}
 
 		else if (RBA > 0 && RBA <= 10) {
 			mo_rova = new MarkerOptions()
 					.title("Rovetta A")
 					.position(rova)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_blue))
 					.visible(true).draggable(false);
 		}
 
 		else if (RBA > 10 && RBA <= 20) {
 			mo_rova = new MarkerOptions()
 					.title("Rovetta A")
 					.position(rova)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_geen))
 					.visible(true).draggable(false);
 		}
 
 		else if (RBA > 20 && RBA <= 30) {
 			mo_rova = new MarkerOptions()
 					.title("Rovetta A")
 					.position(rova)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_yellow))
 					.visible(true).draggable(false);
 		}
 		
 		else if (RBA > 30 && RBA <= 40) {
 			mo_rova = new MarkerOptions()
 					.title("Rovetta A")
 					.position(rova)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(true).draggable(false);
 		}
 		
 		else if (RBA > 40) {
 			mo_rova = new MarkerOptions()
 					.title("Rovetta A")
 					.position(rova)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_red))
 					.visible(true).draggable(false);
 		}
 
 		if (RBB == 0) {
 			mo_rovb = new MarkerOptions()
 					.title("Rovetta B")
 					.position(rovb)
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_RED))
 					.visible(false).draggable(false);
 		}
 
 		else if (RBB > 0 && RBB <= 10) {
 			mo_rovb = new MarkerOptions()
 					.title("Rovetta B")
 					.position(rovb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_blue))
 					.visible(true).draggable(false);
 		}
 
 		else if (RBB > 10 && RBB <= 20) {
 			mo_rovb = new MarkerOptions()
 					.title("Rovetta B")
 					.position(rovb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_geen))
 					.visible(true).draggable(false);
 		}
 
 		else if (RBB > 20 && RBB <= 30) {
 			mo_rovb = new MarkerOptions()
 					.title("Rovetta B")
 					.position(rovb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_yellow))
 					.visible(true).draggable(false);
 		}
 		
 		else if (RBB > 30 && RBB <= 40) {
 			mo_rovb = new MarkerOptions()
 					.title("Rovetta B")
 					.position(rovb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(true).draggable(false);
 		}
 		
 		else if (RBB > 40) {
 			mo_rovb = new MarkerOptions()
 					.title("Rovetta B")
 					.position(rovb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_red))
 					.visible(true).draggable(false);
 		}
 
 		if (FLH == 0) {
 			mo_fish = new MarkerOptions()
 					.title("Fisher")
 					.position(fish)
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_RED))
 					.visible(false).draggable(false);
 		}
 
 		else if (FLH > 0 && FLH <= 10) {
 			mo_fish = new MarkerOptions()
 					.title("Fisher")
 					.position(fish)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_blue))
 					.visible(true).draggable(false);
 		}
 
 		else if (FLH > 10 && RBB <= 20) {
 			mo_fish = new MarkerOptions()
 					.title("Fisher")
 					.position(fish)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_geen))
 					.visible(true).draggable(false);
 		}
 
 		else if (FLH > 20 && FLH <= 30) {
 			mo_fish = new MarkerOptions()
 					.title("Fisher")
 					.position(fish)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_yellow))
 					.visible(true).draggable(false);
 		}
 		
 		else if (FLH > 30 && FLH <= 40) {
 			mo_fish = new MarkerOptions()
 					.title("Fisher")
 					.position(fish)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(true).draggable(false);
 		}
 		
 		else if (FLH > 40) {
 			mo_fish = new MarkerOptions()
 					.title("Fisher")
 					.position(fish)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_red))
 					.visible(true).draggable(false);
 		}
 
 		if (HWC == 0) {
 			mo_wel = new MarkerOptions()
 					.title("Health and Wellness Center")
 					.position(well)
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_RED))
 					.visible(false).draggable(false);
 		}
 
 		else if (HWC > 0 && HWC <= 10) {
 			mo_wel = new MarkerOptions()
 					.title("Health and Wellness Center")
 					.position(well)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_blue))
 					.visible(true).draggable(false);
 		}
 
 		else if (HWC > 10 && HWC <= 20) {
 			mo_wel = new MarkerOptions()
 					.title("Health and Wellness Center")
 					.position(well)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_geen))
 					.visible(true).draggable(false);
 		}
 
 		else if (HWC > 20 && HWC <= 30) {
 			mo_wel = new MarkerOptions()
 					.title("Health and Wellness Center")
 					.position(well)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_yellow))
 					.visible(true).draggable(false);
 		}
 		
 		else if (HWC > 30 && HWC <= 40) {
 			mo_wel = new MarkerOptions()
 					.title("Health and Wellness Center")
 					.position(well)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(true).draggable(false);
 		}
 		
 		else if (HWC > 40) {
 			mo_wel = new MarkerOptions()
 					.title("Health and Wellness Center")
 					.position(well)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_red))
 					.visible(true).draggable(false);
 		}
 
 		if (LSB == 0) {
 			mo_shor = new MarkerOptions()
 					.title("Shores")
 					.position(shor)
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_RED))
 					.visible(false).draggable(false);
 		}
 
 		else if (LSB > 0 && LSB <= 10) {
 			mo_shor = new MarkerOptions()
 					.title("Shores")
 					.position(shor)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_blue))
 					.visible(true).draggable(false);
 		}
 
 		else if (LSB > 10 && LSB <= 20) {
 			mo_shor = new MarkerOptions()
 					.title("Shores")
 					.position(shor)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_geen))
 					.visible(true).draggable(false);
 		}
 
 		else if (LSB > 20 && LSB <= 30) {
 			mo_shor = new MarkerOptions()
 					.title("Shores")
 					.position(shor)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_yellow))
 					.visible(true).draggable(false);
 		}
 		
 		else if (LSB > 30 && LSB <= 40) {
 			mo_shor = new MarkerOptions()
 					.title("Shores")
 					.position(shor)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(true).draggable(false);
 		}
 		
 		else if (LSB > 40) {
 			mo_shor = new MarkerOptions()
 					.title("Shores")
 					.position(shor)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_red))
 					.visible(true).draggable(false);
 		}
 		
 		if (OSB == 0) {
 			mo_osb = new MarkerOptions()
 					.title("OSB")
 					.position(osb)
 					.icon(BitmapDescriptorFactory
 							.defaultMarker(BitmapDescriptorFactory.HUE_RED))
 					.visible(false).draggable(false);
 		}
 
 		else if (OSB > 0 && OSB <= 10) {
 			mo_osb = new MarkerOptions()
 					.title("OSB")
 					.position(osb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_blue))
 					.visible(true).draggable(false);
 		}
 
 		else if (OSB > 10 && OSB <= 20) {
 			mo_osb = new MarkerOptions()
 					.title("OSB")
 					.position(osb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_geen))
 					.visible(true).draggable(false);
 		}
 
 		else if (OSB > 20 && OSB <= 30) {
 			mo_osb = new MarkerOptions()
 					.title("OSB")
 					.position(osb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_yellow))
 					.visible(true).draggable(false);
 		}
 
 		else if (OSB > 30 && OSB <= 40) {
 			mo_osb = new MarkerOptions()
 					.title("OSB")
 					.position(osb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_orange))
 					.visible(true).draggable(false);
 		}
 		
 		else if (OSB > 40) {
 			mo_osb = new MarkerOptions()
 					.title("OSB")
 					.position(osb)
 					.icon(BitmapDescriptorFactory
 							.fromResource(R.drawable.circle_red))
 					.visible(true).draggable(false);
 		}
 		
 		
 		
 		map.addMarker(mo_lov);
 		map.addMarker(mo_hcb);
 		map.addMarker(mo_carr);
 		map.addMarker(mo_caroth);
 		map.addMarker(mo_rova);
 		map.addMarker(mo_rovb);
 		map.addMarker(mo_bel);
 		map.addMarker(mo_wel);
 		map.addMarker(mo_shor);
 		map.addMarker(mo_fish);
 		map.addMarker(mo_osb);
 
 		map.setOnMarkerClickListener(new OnMarkerClickListener() {
 
 			@Override
 			public boolean onMarkerClick(Marker marker) {
 				if (marker.getTitle().toString()
 						.equals(mo_hcb.getTitle().toString())) {
 					marker.setSnippet("In-Use: " + String.valueOf(HCB));
 				}
 
 				if (marker.getTitle().toString()
 						.equals(mo_lov.getTitle().toString())) {
 					marker.setSnippet("In-Use: " + String.valueOf(LOV));
 				}
 				
 				if (marker.getTitle().toString()
 						.equals(mo_caroth.getTitle().toString())) {
 					marker.setSnippet("In-Use: " + String.valueOf(MCH));
 				}
 
 				if (marker.getTitle().toString()
 						.equals(mo_carr.getTitle().toString())) {
 					marker.setSnippet("In-Use: " + String.valueOf(CAR));
 				}
 				
 				if (marker.getTitle().toString()
 						.equals(mo_rova.getTitle().toString())) {
 					marker.setSnippet("In-Use: " + String.valueOf(RBA));
 				}
 
 				if (marker.getTitle().toString()
 						.equals(mo_rovb.getTitle().toString())) {
 					marker.setSnippet("In-Use: " + String.valueOf(RBB));
 				}
 				
 				if (marker.getTitle().toString()
 						.equals(mo_bel.getTitle().toString())) {
 					marker.setSnippet("In-Use: " + String.valueOf(BEL));
 				}
 
 				if (marker.getTitle().toString()
 						.equals(mo_shor.getTitle().toString())) {
 					marker.setSnippet("In-Use: " + String.valueOf(LSB));
 				}
 				
 				if (marker.getTitle().toString()
 						.equals(mo_wel.getTitle().toString())) {
 					marker.setSnippet("In-Use: " + String.valueOf(HWC));
 				}
 
 				if (marker.getTitle().toString()
 						.equals(mo_osb.getTitle().toString())) {
 					marker.setSnippet("In-Use: " + String.valueOf(OSB));
 				}
 				
 				if (marker.getTitle().toString()
 						.equals(mo_fish.getTitle().toString())) {
 					marker.setSnippet("In-Use: " + String.valueOf(FLH));
 				}
 				
 
 				return false;
 			}
 		});
 	}
 	
 }
