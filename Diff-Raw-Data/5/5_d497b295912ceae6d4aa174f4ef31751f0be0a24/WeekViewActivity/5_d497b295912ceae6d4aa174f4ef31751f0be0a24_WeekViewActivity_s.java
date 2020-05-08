 /*
  * Copyright (C) 2011 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.morphoss.acal.weekview;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Parcelable;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 
 import com.morphoss.acal.Constants;
 import com.morphoss.acal.R;
 import com.morphoss.acal.acaltime.AcalDateRange;
 import com.morphoss.acal.acaltime.AcalDateTime;
 import com.morphoss.acal.activity.EventEdit;
 import com.morphoss.acal.activity.MonthView;
 import com.morphoss.acal.dataservice.CalendarDataService;
 import com.morphoss.acal.dataservice.DataRequest;
 import com.morphoss.acal.dataservice.DataRequestCallBack;
 import com.morphoss.acal.davacal.AcalEvent;
 import com.morphoss.acal.widget.NumberPickerDialog;
 import com.morphoss.acal.widget.NumberSelectedListener;
 
 /**
  * This is the activity behind WeekView. It catches all UI Events and user interaction.
  *
  * Valid user input is passed on to the WeekViewLayout, responsible for drawing all the components in this activity.
  *  
  * 
  * @author Morphoss Ltd
  * @license GPL v3 or later
  */
 public class WeekViewActivity extends Activity implements OnGestureListener, OnTouchListener, NumberSelectedListener, OnClickListener {
 	/* Fields relating to buttons */
 	public static final int TODAY = 0;
 	public static final int MONTH = 2;
 	public static final int ADD = 3;
 
 	public static final String TAG = "aCal YearView";
 	
 	private WeekViewHeader 	header;
 	private WeekViewSideBar sidebar;
 	private WeekViewDays	days;
 
 	private SharedPreferences prefs = null; 
 
 	//Text Size - if you change this value, please change the values
 	//in week_view_styles.xml to be the same.
 	private static final int TEXT_SIZE = 12;	//SP
 	
 	//Magic Numbers / Configurable values 
 	public static final int MINIMUM_DAY_EVENT_HEIGHT = 20;
 	public static final float[] DASHED_LINE_PARAMS = new float[] {5,5};
 	
 	public static final int EVENT_BORDER = 2;		//hard coded
 	
 	//Preference controlled values
 	public static int DAY_WIDTH = 100;
 	public static int HALF_HOUR_HEIGHT = 20;
 	public static int FIRST_DAY_OF_WEEK = 1;
 	public static boolean TIME_24_HOUR = false;
 	public static int HEADER_ITEM_HEIGHT = 20;  //1 row
 	public static int START_HOUR = 9;
 	public static int START_MINUTE = 0;
 	public static int END_HOUR = 17;
 	public static int END_MINUTE = 0;
 
 	
 	
 	//Image cache
 	private WeekViewImageCache imageCache;
 	
 	//Dialogs
 	private static final int DATE_PICKER = 0;
 	
 	/* Fields Relating to Gesture Detection */
 	private GestureDetector gestureDetector;
 	private AcalDateTime selectedDate = new AcalDateTime();
 	
 	/* Fields relating to calendar data */
 	private DataRequest dataRequest = null;
 	private boolean isBound = false;
 	
 	//Fields relating to scrolling
 	private float scrollx = 0;
 	private float scrolly = 0;
 	
 	
 	/**
 	 * Set up buttons, UI listeners and views.
 	 * @param savedInstanceState Contains the day of the week that we start with
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		gestureDetector = new GestureDetector(this);
 		this.selectedDate = this.getIntent().getExtras().getParcelable("StartDay");
 		
 		if ( selectedDate == null ) {
 			selectedDate = new AcalDateTime();
 		}
 		selectedDate.applyLocalTimeZone();
 		selectedDate.setDaySecond(0);
 
 		this.setContentView(R.layout.week_view);
 		header 	= (WeekViewHeader) 	this.findViewById(R.id.week_view_header);
 		sidebar = (WeekViewSideBar) this.findViewById(R.id.week_view_sidebar);
 		days 	= (WeekViewDays) 	this.findViewById(R.id.week_view_days);
 		
 		// Set up buttons
 		this.setupButton(R.id.year_today_button, TODAY);
 		this.setupButton(R.id.year_month_button, MONTH);
 		this.setupButton(R.id.year_add_button, ADD);
 		
 		loadPrefs();
 		
 		
 		scrolly+=(WeekViewActivity.START_HOUR)*(WeekViewActivity.HALF_HOUR_HEIGHT*2);
 	}
 	
 	private void loadPrefs() {
 		//Load Prefs
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		TIME_24_HOUR = prefs.getBoolean(this.getString(R.string.prefTwelveTwentyfour), false);
 		try {
 			FIRST_DAY_OF_WEEK = Integer.parseInt(prefs.getString(getString(R.string.firstDayOfWeek), "0"));
 			if ( FIRST_DAY_OF_WEEK < AcalDateTime.MONDAY || FIRST_DAY_OF_WEEK > AcalDateTime.SUNDAY ) throw new Exception();
 		}
 		catch( Exception e ) {
 			FIRST_DAY_OF_WEEK = AcalDateTime.MONDAY; 
 		}
 		
 		float SPscaler = this.getResources().getDisplayMetrics().scaledDensity;	//used for scaling our values to SP
 		float DPscaler = this.getResources().getDisplayMetrics().density;	//used for scaling our values to SP
 		
		int lph = Integer.parseInt(prefs.getString(getString(R.string.prefWeekViewLinesPerHour), "1"));
 		if (lph <= 0) lph = 1;
 		if (lph >= 20) lph = 20;
 		HALF_HOUR_HEIGHT = (int)(((lph*TEXT_SIZE)/2)*SPscaler);
 		
		int cpw = Integer.parseInt(prefs.getString(getString(R.string.prefWeekViewDayWidth), "10"));
 		if (cpw <= 0) lph = 10;
 		if (cpw >= 1000) lph = 1000;
 		
 		DAY_WIDTH = (int)(cpw*DPscaler);
 		
 		HEADER_ITEM_HEIGHT = (int)(TEXT_SIZE*SPscaler);
 		
 		try {
 			String startDay =  prefs.getString(getString(R.string.prefWorkdayStart), "9:00");
 			String endDay =  prefs.getString(getString(R.string.prefWorkdayFinish), "17:00");
 			int idx = startDay.indexOf(':');
 			START_HOUR = Integer.parseInt(startDay.substring(0,idx));
 			START_MINUTE = Integer.parseInt(startDay.substring(idx+1));
 			idx = endDay.indexOf(':');
 			END_HOUR = Integer.parseInt(endDay.substring(0,idx));
 			END_MINUTE = Integer.parseInt(endDay.substring(idx+1));
 		} catch (Exception e) {
 			if (Constants.LOG_DEBUG) {
 				Log.d(TAG,"Error parsing Work Day Prferences: "+e);
 			}
 		}
 		
 		//image cache may bow be invalid
 		imageCache = new WeekViewImageCache(this,DAY_WIDTH,HALF_HOUR_HEIGHT);
 	}
 	
 	//force all displays to update
 	public void refresh() {
 		header.invalidate();
 		days.invalidate();	
 		sidebar.invalidate();
 	}
 	
 	public float getScrollY() {
 		return this.scrolly;
 	}
 	public float getScrollX() {
 		return this.scrollx;
 	}
 	
 	public float getSideVerticalOffset() {
 		return days.getHeaderHeight();
 	}
 	
 	public void move(float dx, float dy) {
 		this.scrolly+=dy;
 		if (scrolly < 0) scrolly = 0;
 		if (scrolly > (HALF_HOUR_HEIGHT*49-days.getHeight())) scrolly = WeekViewActivity.HALF_HOUR_HEIGHT*49-days.getHeight();
 		this.scrollx-=dx;
 		while (this.scrollx >= DAY_WIDTH) {
 			decrementCurrentDate();
 			this.scrollx-=DAY_WIDTH;
 		} 
 		while (this.scrollx <= 0-DAY_WIDTH) {
 			incrementCurrentDate();
 			this.scrollx+=DAY_WIDTH;
 		}
 		refresh();
 	}
 	
 	
 	public WeekViewImageCache getImageCache() {
 		return this.imageCache;
 	}
 	
 	public AcalDateTime getCurrentDate() {
 		return this.selectedDate;
 	}
 	
 	public void incrementCurrentDate() {
 		this.selectedDate.addDays(1);
 	}
 	
 	public void decrementCurrentDate() {
 		this.selectedDate.addDays(-1);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public ArrayList<AcalEvent> getEventsForDays(AcalDateRange range) {
 		if (isBound && dataRequest != null) {
 			try {
 				return (ArrayList<AcalEvent>) this.dataRequest.getEventsForDays(range);
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 			}
 		}
 		return new ArrayList<AcalEvent>();
 	}
 	
 	/** Connect to CDS - needed to get event information for views. */
 	private void connectToService() {
 		try {
 			if (this.isBound)
 				return;
 			Intent intent = new Intent(this, CalendarDataService.class);
 			Bundle b = new Bundle();
 			b.putInt(CalendarDataService.BIND_KEY,
 					CalendarDataService.BIND_DATA_REQUEST);
 			intent.putExtras(b);
 			this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
 		} catch (Exception e) {
 			Log.e(TAG, "Error connecting to service: " + e.getMessage());
 		}
 	}
 	
 	
 	@Override 
 	public void onPause() {
 		super.onPause();
 		if (isBound) {
 			try {
 				if (dataRequest != null) {
 					dataRequest.flushCache();
 					dataRequest.unregisterCallback(mCallback);
 				}
 				this.unbindService(mConnection);
 				this.isBound = false;
 				dataRequest = null;
 			} catch (RemoteException re) {
 
 			}
 		}
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();		
 		connectToService();
 		imageCache = new WeekViewImageCache(this,DAY_WIDTH,HALF_HOUR_HEIGHT);
 		loadPrefs();
 		refresh();
 	}
 	
 	/**
 	 * <p>
 	 * Called when user has selected 'Settings' from menu. Starts Settings
 	 * Activity.
 	 * </p>
 	 */
 	private void settings() {
 		Intent settingsIntent = new Intent();
 		settingsIntent.setClassName("com.morphoss.acal",
 				"com.morphoss.acal.activity.Settings");
 		this.startActivity(settingsIntent);
 	}
 	
 	/**
 	 * Methods for managing event structure
 	 */
 	@SuppressWarnings("unchecked")
 	public ArrayList<AcalEvent> getEventsForDay(AcalDateTime day) {
 		if (dataRequest == null) return new ArrayList<AcalEvent>();
 		try {
 			return (ArrayList<AcalEvent>) dataRequest.getEventsForDay(day);
 		} catch (RemoteException e) {
 			if (Constants.LOG_DEBUG) Log.d(TAG,"Remote Exception accessing eventcache: "+e);
 			return new ArrayList<AcalEvent>();
 		}
 	}
 
 	public int getNumberEventsForDay(AcalDateTime day) {
 		if (dataRequest == null) return 0;
 		try {
 			return dataRequest.getNumberEventsForDay(day);
 		} catch (RemoteException e) {
 			if (Constants.LOG_DEBUG) Log.d(TAG,"Remote Exception accessing eventcache: "+e);
 			return 0;
 		}
 	}
 
 	public AcalEvent getNthEventForDay(AcalDateTime day, int n) {
 		if (dataRequest == null) return null;
 		try {
 			return dataRequest.getNthEventForDay(day, n);
 		} catch (RemoteException e) {
 			if (Constants.LOG_DEBUG) Log.d(TAG,"Remote Exception accessing eventcache: "+e);
 			return null;
 		}
 	}
 	/**
 	 * <p>
 	 * Responsible for handling the menu button push.
 	 * </p>
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_options_menu, menu);
 		return true;
 	}
 	/**
 	 * <p>
 	 * Called when the user selects an option from the options menu. Determines
 	 * what (if any) Activity should start.
 	 * </p>
 	 * 
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.settingsMenuItem:
 			settings();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	/************************************************************************
 	 * Service Connection management *
 	 ************************************************************************/
 
 	private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the service object we can use to
 			// interact with the service. We are communicating with our
 			// service through an IDL interface, so get a client-side
 			// representation of that from the raw service object.
 			dataRequest = DataRequest.Stub.asInterface(service);
 			try {
 				dataRequest.registerCallback(mCallback);
 				
 			} catch (RemoteException re) {
 
 			}
 			isBound = true;
 			refresh();
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			dataRequest = null;
 			isBound = false;
 		}
 	};
 
 	/**
 	 * This implementation is used to receive callbacks from the remote service.
 	 */
 	private DataRequestCallBack mCallback = new DataRequestCallBack.Stub() {
 		/**
 		 * This is called by the remote service regularly to tell us about new
 		 * values. Note that IPC calls are dispatched through a thread pool
 		 * running in each process, so the code executing here will NOT be
 		 * running in our main thread like most other things -- so, to update
 		 * the UI, we need to use a Handler to hop over there.
 		 */
 		public void statusChanged(int type, boolean value) {
 			mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, type,
 					(value ? 1 : 0)));
 		}
 	};
 
 	private static final int BUMP_MSG = 1;
 
 	private Handler mHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			refresh();
 		}
 
 	};
 	/**
 	 * <p>
 	 * Helper method for setting up buttons
 	 * </p>
 	 */
 	private void setupButton(int id, int val) {
 		Button today = (Button) this.findViewById(id);
 		if (today == null) {
 			Log.e(TAG, "Cannot find button '" + id + "' by ID, to set value '"
 					+ val + "'");
 			Log.i(TAG, Log.getStackTraceString(new Exception()));
 		} else {
 			today.setOnClickListener(this);
 			today.setTag(val);
 		}
 	}
 	
 	private void dateChanged() {
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		return gestureDetector.onTouchEvent(event);
 	}
 	@Override
 	public boolean onTouch(View view, MotionEvent touch) {
 		return this.gestureDetector.onTouchEvent(touch);
 	}
 	
 	@Override
 	public boolean onDown(MotionEvent arg0) {
 		return false;
 	}
 	@Override
 	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
 			float arg3) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	@Override
 	public void onLongPress(MotionEvent arg0) {
 		// TODO Auto-generated method stub
 		//showDialog(DATE_PICKER);
 	}
 	@Override
 	public boolean onScroll(MotionEvent start, MotionEvent current, float dx, float dy) {
 		if (Math.abs(dx)>Math.abs(dy)) move(dx,0);
 		else move(0,dy);
 		return true;
 	}
 	@Override
 	public void onShowPress(MotionEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public boolean onSingleTapUp(MotionEvent arg0) {
 		return false;
 	}
 	
 	
 	@Override 
 	public void onNumberSelected(int number) {
 		selectedDate = new AcalDateTime(number,1,1,0,0,0,null);
 		this.dateChanged();
 	}
 	
 	protected Dialog onCreateDialog(int id) {
 		switch(id) {
 			case DATE_PICKER:
 			NumberPickerDialog dialog = new NumberPickerDialog(this,this,selectedDate.getYear(),1582,3999);
 			return dialog;
 		}
 		return null;
 		
 	}
 	/**
 	 * <p>
 	 * Handles button Clicks
 	 * </p>
 	 */
 	@Override
 	public void onClick(View clickedView) {
 		int button = (int) ((Integer) clickedView.getTag());
 		Bundle bundle = new Bundle();
 		switch (button) {
 		case TODAY:
 			this.selectedDate.setEpoch(new AcalDateTime().getEpoch());
 			this.scrollx=0;
 			scrolly=(WeekViewActivity.START_HOUR)*(WeekViewActivity.HALF_HOUR_HEIGHT*2);
 			this.selectedDate.setDaySecond(0);
 			this.refresh();
 			break;
 		case ADD:
 			bundle.putParcelable("DATE", this.selectedDate);
 			Intent eventEditIntent = new Intent(this, EventEdit.class);
 			eventEditIntent.putExtras(bundle);
 			this.startActivity(eventEditIntent);
 			break;
 		case MONTH:
 			if ( prefs.getBoolean(getString(R.string.prefDefaultView), false) ) {
 				Intent startIntent = null;
 				startIntent = new Intent(this, MonthView.class);
 				startIntent.putExtras(bundle);
 				this.startActivity(startIntent);
 			}
 			else {
 				Intent res = new Intent();
 				res.putExtra("selectedDate", (Parcelable) selectedDate);
 				this.setResult(RESULT_OK, res);
 				this.finish();
 			}
 			break;
 		default:
 			Log.w(TAG, "Unrecognised button was pushed in MonthView.");
 		}
 	}
 }
