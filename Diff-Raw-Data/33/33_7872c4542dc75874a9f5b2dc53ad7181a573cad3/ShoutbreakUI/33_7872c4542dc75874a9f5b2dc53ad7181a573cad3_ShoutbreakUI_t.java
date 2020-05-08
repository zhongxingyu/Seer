 package com.shoutbreak;
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.shoutbreak.service.CellDensity;
 import com.shoutbreak.service.ErrorManager;
 import com.shoutbreak.service.User;
 import com.shoutbreak.ui.CustomMapView;
 import com.shoutbreak.ui.UserLocationOverlay;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class ShoutbreakUI extends MapActivity {
 	// TODO: bundle saved instance... restore state from that
 	
 	protected ShoutbreakUI _ui;
 	protected Context _context;
 	protected User _user;
 	protected Intent _serviceIntent;
 	protected IShoutbreakService _service;
 	protected ShoutbreakServiceConnection _serviceConn;	
 	
 	// keyboard
 	protected InputMethodManager _inputMM;
 	
 	// UI controls
 	protected Button _cShoutsButton;
 	protected Button _cInboxButton;
 	protected Button _cSettingsButton;
 	protected Button _cOnButton;
 	protected Button _cOffButton;
 	protected Button _cShoutButton;
 	protected CustomMapView _cMapView;
 	protected EditText _cShoutText;
 	protected TextView _cStatusText;
 	protected ListView _cInboxListView;
 	protected LinearLayout _cRow1;
 	protected LinearLayout _cRow2;
 	protected RelativeLayout _cRow3;
 	protected RelativeLayout _cRow4;
 	protected RelativeLayout _cRow6;
 	protected RelativeLayout _cNoticeBox;
 	protected RelativeLayout _cNoticeBoxInbox;
 	protected TextView _cNoticeText;
 	protected TextView _cNoticeTextInbox;
 	protected Animation _animNoticeExpand;
 	protected Animation _animNoticeShowText;
 	
 	protected UserLocationOverlay _userLocationOverlay;
 	protected MapController _mapController;
 	
 	protected NotificationManager _notificationManager;
 	
 	///////////////////////////////////////////////////////////////////////////
 	// LIFECYCLE METHODS
 	///////////////////////////////////////////////////////////////////////////
 	
 	/** Called when the activity is first created. */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		_ui = this;
 		_context = getApplicationContext();
 		_serviceIntent = new Intent();
 		_serviceIntent.setClassName("com.shoutbreak", "com.shoutbreak.ShoutbreakService");
 		_inputMM = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		
 		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 		DisplayMetrics metrics = new DisplayMetrics();
 		display.getMetrics(metrics);
 		int h = metrics.heightPixels;
 		int w = metrics.widthPixels;
 		int h16 = (int)h/16;
 		int h8 = 2 * h16;
 		int tabWidth = (int)w / 3;
 		
 		_animNoticeExpand = AnimationUtils.loadAnimation(_context, R.anim.notice_expand);
 		_animNoticeShowText = AnimationUtils.loadAnimation(_context, R.anim.notice_show_text);
 		_cShoutsButton = (Button) findViewById(R.id.btnShouts);
 		_cInboxButton = (Button) findViewById(R.id.btnInbox);
 		_cSettingsButton = (Button) findViewById(R.id.btnSettings);
 		_cOnButton = (Button) findViewById(R.id.btnOn);
 		_cOffButton = (Button) findViewById(R.id.btnOff);
 		_cShoutButton = (Button) findViewById(R.id.btnShout);
 		_cMapView = (CustomMapView)findViewById(R.id.cmvMap);
 		_cShoutText = (EditText) findViewById(R.id.etShoutText);
 		_cStatusText = (TextView) findViewById(R.id.tvStatus);		
 		_cNoticeBox = (RelativeLayout) findViewById(R.id.rlNotice);
 		_cNoticeText = (TextView) findViewById(R.id.tvNotice);
 		_cNoticeBoxInbox = (RelativeLayout) findViewById(R.id.r6Notice);
 		_cNoticeTextInbox = (TextView) findViewById(R.id.tvNoticeInbox);
 		_cInboxListView = (ListView)findViewById(R.id.lvInbox);
 		
 		_cRow1 = (LinearLayout) findViewById(R.id.llRow1);
 		_cRow2 = (LinearLayout) findViewById(R.id.llRow2);
 		_cRow3 = (RelativeLayout) findViewById(R.id.rlRow3);
 		_cRow4 = (RelativeLayout) findViewById(R.id.rlRow4);
 		_cRow6 = (RelativeLayout) findViewById(R.id.llRow6);
 		
 		_cOnButton.setHeight(h16);
 		_cOffButton.setHeight(h16);
 		_cShoutText.setHeight(h8);
 		_cShoutButton.setHeight(h8);
 		_cShoutsButton.setWidth(tabWidth);
 		_cInboxButton.setWidth(tabWidth);
 		_cSettingsButton.setWidth(tabWidth);
 		_cShoutsButton.setHeight(h8);
 		_cInboxButton.setHeight(h8);
 		_cSettingsButton.setHeight(h8);
 		
 		_mapController = _cMapView.getController();
 		
 		_notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		// Setup User
 		ShoutbreakApplication app = (ShoutbreakApplication)this.getApplication();
		//_user = app.getUser();
		//if (_user == null) {
 			_user = app.createUser(_context);
 			_user.initializeInbox(this, _cInboxListView);
		//}
		
		//Log.e("USER", "USER: " + _user.getAuth());
 		
 		_cShoutsButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				
 				_cRow1.setVisibility(View.VISIBLE);
 				_cRow2.setVisibility(View.VISIBLE);
 				_cRow3.setVisibility(View.VISIBLE);
 				_cRow4.setVisibility(View.VISIBLE);
 				_cRow6.setVisibility(View.GONE);
 			}
 		});
 		
 		_cInboxButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				
 				_cRow1.setVisibility(View.GONE);
 				_cRow2.setVisibility(View.GONE);
 				_cRow3.setVisibility(View.GONE);
 				_cRow4.setVisibility(View.GONE);
 				_cRow6.setVisibility(View.VISIBLE);
 				
 			}
 		});
 		
 		_cSettingsButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 
 				giveNotice("TEST Test Tdada!");
 //				_cRow1.setVisibility(View.GONE);
 //				_cRow2.setVisibility(View.GONE);
 //				_cRow3.setVisibility(View.GONE);
 //				_cRow4.setVisibility(View.GONE);
 //				_cRow6.setVisibility(View.GONE);
 
 			}
 		});
 		
 		_cShoutButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				String text = _cShoutText.getText().toString().trim();
 				try {
 					_service.shout(text);
 				} catch (RemoteException ex) {
 					ErrorManager.manage(ex);
 				}
 				hideKeyboard();
 			}
 		});
 
 		_cOnButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				turnServiceOn();
 			}
 		});
 
 		_cOffButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				turnServiceOff();
 			}
 		});
 		
 		initMap();
 
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		_userLocationOverlay.enableMyLocation();
 		
 		_user.getInbox().refresh();
 		
 		// this should go last.. let ui render
 		linkToService();
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		_userLocationOverlay.disableMyLocation();
 	}
 	
 	
 	@Override
 	protected void onStop() {
 		super.onStop();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		releaseService();
 		Log.d(getClass().getSimpleName(), "onDestroy");
 	}
 	
 	///////////////////////////////////////////////////////////////////////////
 	// END LIFECYCLE METHODS
 	///////////////////////////////////////////////////////////////////////////
 	
 	@Override
 	protected boolean isRouteDisplayed() {
 		// TODO Auto-generated method stub
 		return false;
 	}	
 	
 	public User getUser() {
 		return _user;
 	}
 	
 	public void tempNotify(String alert, String title, String message) {
 		Intent intent = new Intent(this, ShoutbreakUI.class);
 	    Notification notification = new Notification(R.drawable.icon, alert, System.currentTimeMillis());
 	    notification.setLatestEventInfo(this, title, message,
 	    		PendingIntent.getActivity(this.getBaseContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
 	    _notificationManager.notify(Vars.APP_NOTIFICATION_ID, notification);
 	}
 	
 	public void giveNotice(String noticeText) {
 		// map notice
 		_cNoticeText.setText(noticeText);
 		_cNoticeBox.startAnimation(_animNoticeExpand);
 		_cNoticeText.setTextColor(Color.WHITE);
 		_cNoticeText.startAnimation(_animNoticeShowText);
 		// inbox notice
 		_cNoticeTextInbox.setText(noticeText);
 		_cNoticeBoxInbox.startAnimation(_animNoticeExpand);
 		_cNoticeTextInbox.setTextColor(Color.WHITE);
 		_cNoticeTextInbox.startAnimation(_animNoticeShowText);
 	}
 	
 	public IShoutbreakService getService() {
 		return _service;
 	}
 	
 	public void hideKeyboard() {
 		_inputMM.hideSoftInputFromWindow(_cShoutText.getWindowToken(), 0);
 	}
 	
 	protected void initMap() {
 
 		_userLocationOverlay = new UserLocationOverlay(this, _cMapView);
 		_mapController.setZoom(Vars.DEFAULT_ZOOM_LEVEL);
 		_cMapView.setClickable(true);
 		_cMapView.setEnabled(true);
 		_cMapView.setUI(this);
 		_cMapView.setUserLocationOverlay(_userLocationOverlay);
 		_cMapView.getOverlays().add(_userLocationOverlay);
 		
 		_userLocationOverlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				_mapController.animateTo(_userLocationOverlay.getMyLocation());
 			}
 		});
 		
 	} 
 
 	protected void linkToService() {
 		if (_serviceConn == null) {
 			startService(_serviceIntent);
 			_serviceConn = new ShoutbreakServiceConnection();
 			bindService(_serviceIntent, _serviceConn, Context.BIND_AUTO_CREATE);
 			Log.d(getClass().getSimpleName(), "linkToService");
 		}
 	}
 
 	protected void releaseService() {
 		if (_serviceConn != null) {
 			unbindService(_serviceConn);
 			_serviceConn = null;
 			Log.d(getClass().getSimpleName(), "releaseService");
 		} else {
 			// cannot unbind - serivce not bound
 		}
 	}
 
 	protected void turnServiceOn() {
 		startService(_serviceIntent);
 		User.setBooleanPreference(_context, Vars.PREF_APP_ON_OFF_STATUS, true);
 		Log.d(getClass().getSimpleName(), "turnServiceOn");
 	}
 
 	protected void turnServiceOff() {
 		_serviceIntent.setClassName("com.shoutbreak", "com.shoutbreak.ShoutbreakService");
 		stopService(_serviceIntent);
 		User.setBooleanPreference(_context, Vars.PREF_APP_ON_OFF_STATUS, false);
 		Log.d(getClass().getSimpleName(), "turnServiceOff");
 	}
 
 	/**
 	 * This implementation is used to receive callbacks from the remote service.
 	 */
 	protected IShoutbreakServiceCallback _shoutbreakServiceCallback = new IShoutbreakServiceCallback.Stub() {
 		/**
 		 * This is called by the remote service regularly to tell us about new
 		 * values. Note that IPC calls are dispatched through a thread pool
 		 * running in each process, so the code executing here will NOT be
 		 * running in our main thread like most other things -- so, to update
 		 * the UI, we need to use a Handler to hop over there.
 		 */		
 		public void serviceEventComplete(int serviceEventCode) {
 			switch (serviceEventCode) {
 				case Vars.SEC_SHOUT_SENT: {
 					giveNotice("shout successful");
 					_cShoutText.setText("");
 					break;
 				}
 				case Vars.SEC_RECEIVE_SHOUTS: {
 					CellDensity cellDensity = _user.getCellDensity();
 					_userLocationOverlay.setPopulationDensity(cellDensity.density);
 					int newShouts = _user.getShoutsJustReceived();
 					if (newShouts > 0) {
 						String notice = "just heard " + newShouts + " new Shout";
 						if (newShouts > 1) {
 							notice += "s"; // plural is dumb
 						}
 						giveNotice(notice);
 					}
 				}
 				case Vars.SEC_VOTE_COMPLETED: {
 					_user.getInbox().refresh();
 				}
 			}
 		}
 	};
 
 	class ShoutbreakServiceConnection implements ServiceConnection {
 
 		public void onServiceConnected(ComponentName className, IBinder boundService) {
 			
 			_service = IShoutbreakService.Stub.asInterface((IBinder) boundService);
 
 			// We want to monitor the service for as long as we are
 			// connected to it.
 			try {
 				_service.registerCallback(_shoutbreakServiceCallback);
 			} catch (RemoteException e) {
 				// In this case the service has crashed before we could even
 				// do anything with it; we can count on soon being
 				// disconnected (and then reconnected if it can be restarted)
 				// so there is no need to do anything here.
 			}
 			Log.d(getClass().getSimpleName(), "onServiceConnected()");
 
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			_service = null;
 			Log.d(getClass().getSimpleName(), "onServiceDisconnected");
 		}
 
 	}
 
 }
