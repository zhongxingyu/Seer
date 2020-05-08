 /**
  * 
  */
 package com.madhackerdesigns.neverbelate.ui;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.content.AsyncQueryHandler;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ViewSwitcher;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 import com.madhackerdesigns.neverbelate.R;
 import com.madhackerdesigns.neverbelate.provider.AlertsContract;
 import com.madhackerdesigns.neverbelate.provider.AlertsHelper;
 import com.madhackerdesigns.neverbelate.service.NeverBeLateService;
 import com.madhackerdesigns.neverbelate.service.ServiceCommander;
 import com.madhackerdesigns.neverbelate.service.WakefulServiceReceiver;
 import com.madhackerdesigns.neverbelate.settings.PreferenceHelper;
 import com.madhackerdesigns.neverbelate.ui.UserLocationOverlay.OnLocationChangedListener;
 import com.madhackerdesigns.neverbelate.util.AdHelper;
 import com.madhackerdesigns.neverbelate.util.BuildMode;
 import com.madhackerdesigns.neverbelate.util.Logger;
 
 /**
  * @author flintinatux
  *
  */
 public class WarningDialog extends MapActivity implements ServiceCommander {
 
 	// private static tokens
 	private static final int ALERT_TOKEN = 1;
 	private static final String LOG_TAG = "NeverBeLateWarning";
 	
 	// static strings for intent stuff
 	private static final String PACKAGE_NAME = "com.madhackerdesigns.neverbelate";
 	private static final Uri APP_DETAILS_URI = Uri.parse("market://details?id=" + PACKAGE_NAME);
 	public static final String EXTRA_URI = PACKAGE_NAME + ".uri";
 	private static final String MAPS_URL = "http://maps.google.com/maps";
 	
 	// static strings for view tags
 	private static final String TAG_ALERT_LIST = "alert_list";
 	private static final String TAG_TRAFFIC_VIEW = "traffic_view";
 	
 	// Maps API keys
 	private static final String MAPS_API_DEBUG = "09UCTg-5fHNGM1co-a60SYy42aJanoXio4xB0oQ";
 	private static final String MAPS_API_PRODUCTION = "09UCTg-5fHNFjWw-FcMUvJPaLH_YTUiHB5gh1Kg";
 	
 	// Dialog id's
 	private static final int DLG_NAV = 0;
 	private static final int DLG_RATE = 1;
 		
 	// fields to hold shared preferences and ad stuff
 	private AdHelper mAdHelper;
 //	private IAdManager mAdManager;
 	private boolean mAdJustShown = false;
 	private PreferenceHelper mPrefs;
 	private static final String PREF_ALERT_STATS = "alert_stats";
 	private static final String KEY_ALERT_COUNT = "alert_stats.alert_count";
 	private static final String KEY_RATED_ALREADY = "alert_stats.rated_already";
 	private static final String KEY_ALERT_STEP = "alert_stats.alert_step";
 	
 	// other fields
 	private AlertQueryHandler mHandler;
 	private Cursor mEventCursor;
 	private ArrayList<EventHolder> mEventHolders = new ArrayList<EventHolder>();
 	private List<String> mDestList = new ArrayList<String>();
 	private boolean mInsistentStopped = false;
 	private MapView mMapView;
 	private ViewSwitcher mSwitcher;
 	private UserLocationOverlay mUserLocationOverlay;
 	
 	// fields for handling locations
 	private ArrayList<OverlayItem> mDest = new ArrayList<OverlayItem>();
 	private OnLocationChangedListener mListener;
 	private GeoPoint mOrig;
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.warning_dialog);
 		setTitle(R.string.advance_warning_title);
 		Logger.d(LOG_TAG, "Title is set.");
 		
 		// Load the preferences and Pontiflex IAdManager
 		Context applicationContext = getApplicationContext();
 		mAdHelper = new AdHelper(applicationContext);
 //		mAdManager = AdManagerFactory.createInstance(getApplication());
 		mPrefs = new PreferenceHelper(applicationContext);
 		
 		// Grab the view switcher, inflate and add the departure window and traffic views
 		ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.view_switcher);
 		View alertListView = View.inflate(this, R.layout.alert_list_view, null);
 		View trafficView = View.inflate(this, R.layout.traffic_view_layout, null);
 		alertListView.setTag(TAG_ALERT_LIST);
 		trafficView.setTag(TAG_TRAFFIC_VIEW);
 		switcher.addView(alertListView);
 		switcher.addView(trafficView);
 		mSwitcher = switcher;
 		Logger.d(LOG_TAG, "ViewSwitcher loaded.");
 		
 		if (mMapView == null) {
 			// Inflate the MapView, and pass correct API key based on debug mode
 			LinearLayout layout = (LinearLayout) findViewById(R.id.mapview);
 			mMapView = new MapView(this, BuildMode.isDebug(applicationContext) ? MAPS_API_DEBUG : MAPS_API_PRODUCTION);
 			layout.addView(mMapView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 			mMapView.setClickable(true);
 		}
 		
 		// Enable the user location to the map (early, to feed the location to the AdMob banner)
 		mUserLocationOverlay = new UserLocationOverlay(this, mMapView);
 		boolean providersEnabled = mUserLocationOverlay.enableMyLocation();
 		if (providersEnabled) { 
 			Logger.d(LOG_TAG, "User location updates enabled."); 
 		}
 		
 		// Adjust the warning text to include early arrival if set
 		final Long earlyArrival = mPrefs.getEarlyArrival() / 60000;
 		if (!earlyArrival.equals(new Long(0))) {
 			final TextView tv_warningText = (TextView) findViewById(R.id.warning_text);
 			final Resources res = getResources();
 			String warningText = res.getString(R.string.warning_text);
 			String onTime = res.getString(R.string.on_time);
 			String minutesEarly = res.getString(R.string.minutes_early);
 			warningText = warningText.replaceFirst(onTime, earlyArrival + " " + minutesEarly);
 			tv_warningText.setText(warningText);
 		}
 		
 		// Load up the list of alerts
 		loadAlertList();
 		
 		// Set the "View Traffic" button action
 		Button trafficButton = (Button) findViewById(R.id.traffic_button);
 		trafficButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				// Switch to the traffic view and setup the MapView
 				Logger.d(LOG_TAG, "'View Traffic' button clicked, switching to traffic view...");
 				stopInsistentAlarm();
 				loadTrafficView();
 			}
 			
 		});
 		Logger.d(LOG_TAG, "Traffic button added.");
 		
 		// Load up an AdMob banner
 		if (! BuildMode.isDebug(applicationContext)) {
 			AdRequest request = new AdRequest();
 			if (providersEnabled) { request.setLocation(mUserLocationOverlay.getLastFix()); }
 			AdView adView = (AdView) findViewById(R.id.ad_view);
 		    adView.loadAd(request);
 		    Logger.d(LOG_TAG, "AdMob banner loaded.");
 		}
 		
 		mUserLocationOverlay.disableMyLocation();
 	}
 	
 	private void loadAlertList() {
 		// Query the AlertProvider for alerts that are fired, but not dismissed
 		ContentResolver cr = getContentResolver();
 		if (mHandler == null) { mHandler = new AlertQueryHandler(cr); }
 		Uri contentUri = AlertsContract.Alerts.CONTENT_URI;
 		String[] projection = AlertsHelper.ALERT_PROJECTION;
 		String selection = 
 			AlertsContract.Alerts.FIRED + "=? AND " + 
 			AlertsContract.Alerts.DISMISSED + "=?";
 		String[] selectionArgs = new String[] { "1", "0" };
 		mHandler.startQuery(ALERT_TOKEN, getApplicationContext(), 
 				contentUri, projection, selection, selectionArgs, null);
 		Logger.d(LOG_TAG, "AlertProvider queried for alerts.");
 	}
 	
 	private class AlertQueryHandler extends AsyncQueryHandler {
 
 		public AlertQueryHandler(ContentResolver cr) {
 			super(cr);
 		}
 
 		/* (non-Javadoc)
 		 * @see android.content.AsyncQueryHandler#onQueryComplete(int, java.lang.Object, android.database.Cursor)
 		 */
 		@SuppressWarnings("deprecation")
 		@Override
 		protected void onQueryComplete(int token, Object context, Cursor cursor) {
 			// Let the activity manage the cursor life-cycle
 			startManagingCursor(cursor);
 			mEventCursor = cursor;
 			Logger.d(LOG_TAG, "Query returned...");
 			
 			// Now fill in the content of the WarningDialog
 			switch (token) {
 			case ALERT_TOKEN:
 				if (cursor.moveToFirst()) {
 					do {
 						// Store away the event information
 						EventHolder eh = new EventHolder();
 						eh.json = cursor.getString(AlertsHelper.PROJ_JSON);
 						eh.title = cursor.getString(AlertsHelper.PROJ_TITLE);
 						eh.location = cursor.getString(AlertsHelper.PROJ_LOCATION);
 						mEventHolders.add(eh);
 					} while (cursor.moveToNext());
 					
 					// Calculate the departure window.  Note that first row in cursor should be
 					// the first upcoming event instance, since it is sorted by begin time, ascending.
 					cursor.moveToFirst();
 					long begin = cursor.getLong(AlertsHelper.PROJ_BEGIN);
 					long duration = cursor.getLong(AlertsHelper.PROJ_DURATION);
 					TextView departureWindow = (TextView) findViewById(R.id.departure_window);
					long departureTime = begin - mPrefs.getEarlyArrival() - duration;
 					long now = new Date().getTime();
 					Resources res = getResources();
 					String unitMinutes = res.getString(R.string.unit_minutes);
 					String departureString;
 					if (departureTime > now) {
 						departureString = "in " + (int)((departureTime - now) / 60000 + 1) 
 							+ " " + unitMinutes + "!";
 					} else {
 						departureString = "NOW!";
 					}
 					departureWindow.setText(departureString);
 					departureWindow.setOnClickListener(new OnClickListener() {
 
 						public void onClick(View v) {
 							// Stop insistent alarm
 							stopInsistentAlarm();
 						}
 						
 					});
 					
 					// Load the copyrights
 					Set<String> copyrights = new HashSet<String>();
 					do {
 						copyrights.add(cursor.getString(AlertsHelper.PROJ_COPYRIGHTS)); 
 					} while (cursor.moveToNext());
 					String copyrightString = TextUtils.join(" | ", copyrights);
 					TextView copyrightText = (TextView) findViewById(R.id.copyright_text);
 					copyrightText.setText(copyrightString);
 					
 					// Attach the cursor to the alert list view
 					cursor.moveToFirst();
 					EventListAdapter adapter = new EventListAdapter((Context) context, 
 							R.layout.event_list_item, cursor, true);
 					ListView lv = (ListView) findViewById(R.id.list_view);
 					lv.setAdapter(adapter);
 					lv.setOnItemClickListener(new OnItemClickListener() {
 
 						public void onItemClick(AdapterView<?> arg0, View arg1,
 								int arg2, long arg3) {
 							// Stop insistent alarm
 							stopInsistentAlarm();
 						}
 						
 					});
 				}
 			}
 			
 			// Set the "Snooze" button label
 			Resources res = getResources();
 			Button snoozeButton = (Button) findViewById(R.id.snooze_button);
 			int count = cursor.getCount();
 			if (count > 1) {
 				snoozeButton.setText(res.getString(R.string.snooze_all_button_text));
 			} else {
 				snoozeButton.setText(res.getString(R.string.snooze_button_text));
 			}
 			
 			// Enable or disable snooze per user preference
			if (mPrefs.getSnoozeDuration().equals(new Long(0))) {
 				snoozeButton.setVisibility(View.GONE);
 				Logger.d(LOG_TAG, "Snooze button disabled.");
 			} else {
 				snoozeButton.setOnClickListener(new OnClickListener() {
 	
 					public void onClick(View v) {
 						// Snooze the alert, and finish the activity
 						snoozeAlert();
 						showRateDialogAndFinish();
 					}
 					
 				});
 				Logger.d(LOG_TAG, "Snooze button added.");
 			}
 			
 			
 			// Set the "Dismiss" button action
 			Button dismissButton = (Button) findViewById(R.id.dismiss_button);
 			if (count > 1) {
 				dismissButton.setText(res.getString(R.string.dismiss_all_button_text));
 			} else {
 				dismissButton.setText(res.getString(R.string.dismiss_button_text));
 			}
 			dismissButton.setOnClickListener(new OnClickListener() {
 
 				/* (non-Javadoc)
 				 * @see android.view.View.OnClickListener#onClick(android.view.View)
 				 */
 				public void onClick(View v) {
 					// For now, to dismiss, cancel notification and finish
 					dismissAlert();
 					showRateDialogAndFinish();
 				}
 				
 			});
 			Logger.d(LOG_TAG, "Dismiss button loaded.");
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	private void snoozeAlert() {
 		// With respect to ads, consider a SNOOZE as a DISMISS
 		mAdHelper.setWarningDismissed(true);
 		
 		// Set a new alarm to notify for this same event instance
 		Logger.d(LOG_TAG, "'Snooze' button clicked.");
 		long now = new Date().getTime();
 		long warnTime = now + mPrefs.getSnoozeDuration();
 		String warnTimeString = NeverBeLateService.FullDateTime(warnTime);
 		Logger.d(LOG_TAG, "Alarm will be set to warn user again at " + warnTimeString);
 		Context context = getApplicationContext();
 		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		Intent intent = new Intent(context, WakefulServiceReceiver.class);
 		intent.putExtra(EXTRA_SERVICE_COMMAND, NOTIFY);
 		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		alarm.set(AlarmManager.RTC_WAKEUP, warnTime, alarmIntent);
 		
 		// Ask the Service to just SNOOZE the event, which just clears the notification
 		intent = new Intent(context, NeverBeLateService.class);
 		intent.putExtra(EXTRA_SERVICE_COMMAND, SNOOZE);
 		startService(intent);
 	}
 	
 	/**
 	 * 
 	 */
 	private void dismissAlert() {
 		// Tell the AdHelper that this warning has been dismissed
 		mAdHelper.setWarningDismissed(true);
 		
 		// Ask the Service to just DISMISS the alert.
 		Context context = getApplicationContext();
 		Intent cancelIntent = new Intent(context, NeverBeLateService.class);
 		cancelIntent.putExtra(EXTRA_SERVICE_COMMAND, DISMISS);
 		startService(cancelIntent);
 	}
 	
 	@SuppressWarnings("deprecation")
 	private void showRateDialogAndFinish() {
 		SharedPreferences alertStats = getSharedPreferences(PREF_ALERT_STATS, Activity.MODE_PRIVATE);
 		if (! alertStats.getBoolean(KEY_RATED_ALREADY, false)) {
 			long count = alertStats.getLong(KEY_ALERT_COUNT, 1);
 			alertStats.edit().putLong(KEY_ALERT_COUNT, ++count).commit();
 			// Show rate dialog after first 4, then every 8 alerts shown
 			int step = alertStats.getInt(KEY_ALERT_STEP, 4);
 			if (count % step == 0) {
 				if (step == 4) { alertStats.edit().putInt(KEY_ALERT_STEP, 8).commit(); }
 				showDialog(DLG_RATE);
 			} else {
 				finish();
 			}
 		} else {
 			finish();
 		}
 	}
 	
 	private void stopInsistentAlarm() {
 		Logger.d(LOG_TAG, "Stopping insistent alarm.");
 		if (mPrefs.isInsistent() && !mInsistentStopped) {
 			mInsistentStopped = true;
 			Context context = getApplicationContext();
 			Intent i = new Intent(context, NeverBeLateService.class);
 			i.putExtra(EXTRA_SERVICE_COMMAND, SILENCE);
 			startService(i);
 		}
 	}
 
 	private void switchToAlertListView() {
 		mSwitcher.showPrevious();
 		Logger.d(LOG_TAG, "Switched to alert list view.");
 	}
 	
 	private void switchToTrafficView() {
 		mSwitcher.showNext();
 		Logger.d(LOG_TAG, "Switched to traffic view.");
 	}
 	
 	/**
 	 * 
 	 */
 	private void loadTrafficView() {
 		// Log a little
 		Logger.d(LOG_TAG, "Loading traffic view.");
 		
 		// Get mapview and add zoom controls
 		MapView mapView = mMapView;
 		if (mapView != null) { Logger.d(LOG_TAG, "MapView loaded."); }
 		mapView.setBuiltInZoomControls(true);		
 		
 		// Turn on the traffic (as early as possible)
 		mapView.setTraffic(true);
 		
 		// Add the Back button action
 		Button backButton = (Button) findViewById(R.id.back_button);
 		backButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				switchToAlertListView(); 
 				mUserLocationOverlay.disableMyLocation();
 				mUserLocationOverlay.disableCompass();
 			}
 			
 		});
 		
 		// Add the Navigate button
 		Button btnNav = (Button) findViewById(R.id.btn_nav);
 		btnNav.setOnClickListener(new OnClickListener() {
 
 			@SuppressWarnings("deprecation")
 			public void onClick(View v) {
 				ArrayList<EventHolder> eventHolders = mEventHolders;
 				if (eventHolders.size() == 1) {
 					// If only one event location, immediately send intent
 					startNavigation(eventHolders.get(0).location);
 				} else if (eventHolders.size() >= 2) {
 					// If two or more locations, ask user to choose first
 					showDialog(DLG_NAV);
 				}
 			}
 			
 		});
 	
 		// Get the UserLocationOverlay to draw both flags and stay updated
 		UserLocationOverlay overlay = mUserLocationOverlay;
 		
 		// Parse the json directions data
 		final Iterator<EventHolder> eventIterator = mEventHolders.iterator();
 		do {
 			try {
 				// Get the next EventHolder
 				EventHolder eh = eventIterator.next();
 				
 				// Get the zoom span and zoom center from the route
 				JSONObject directions = new JSONObject(eh.json);
 				JSONObject route = directions.getJSONArray("routes").getJSONObject(0);
 				
 				// If the origin is null, pull the origin coordinates
 				JSONObject leg = route.getJSONArray("legs").getJSONObject(0);
 				if (mOrig == null) {
 					int latOrigE6 = (int) (1.0E6 * leg.getJSONObject("start_location").getDouble("lat"));
 					int lonOrigE6 = (int) (1.0E6 * leg.getJSONObject("start_location").getDouble("lng"));
 					mOrig = new GeoPoint(latOrigE6, lonOrigE6);
 				}
 
 				// Get the destination coordinates from the leg
 				int latDestE6 = (int) (1.0E6 * leg.getJSONObject("end_location").getDouble("lat")); 
 				int lonDestE6 = (int) (1.0E6 * leg.getJSONObject("end_location").getDouble("lng"));
 				
 				// Create a GeoPoint for the destination and push onto MapOverlay
 				GeoPoint destPoint = new GeoPoint(latDestE6, lonDestE6);
 				mDest.add(new OverlayItem(destPoint, eh.title, eh.location));
 			} catch (JSONException e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			}
 		} while (eventIterator.hasNext());
 		
 		// Create and store new OnLocationChangedListener
 		if (mListener == null) {
 			mListener = new UserLocationOverlay.OnLocationChangedListener() {
 				
 				public void onLocationChanged(Location location) {
 					// If we are ready, then draw the locations
 					if (mDest.size() > 0 && mOrig != null) {
 						drawLocations(location);
 					}
 				}
 			};
 		}
 		
 		// Set the listener and draw the initial locations
 		overlay.setOnLocationChangedListener(mListener);
 		mListener.onLocationChanged(null);
 		overlay.enableMyLocation();
 		overlay.enableCompass();
 		
 //		// Load an interstitial ad if it's time
 //		AdHelper adHelper = mAdHelper;
 //		if (adHelper.isTimeToShowAd()) {
 //			adHelper.setAdShown(true);
 //			mAdJustShown = true;
 ////			mAdManager.showAd();
 //			mAdManager.startMultiOfferActivity();
 //		} else {
 			// Otherwise, switch to the traffic view
 		switchToTrafficView();
 //		}
 		
 	}
 	
 	public void drawLocations(Location current) {
 		final MapView mapView = mMapView;
 		
 		// Use origin point if current is null
 		GeoPoint userLoc;
 		if (current != null) {
 			int lat = (int) (1.0E6 * current.getLatitude());
 			int lon = (int) (1.0E6 * current.getLongitude());
 			userLoc = new GeoPoint(lat, lon);
 		} else {
 			userLoc = mOrig;
 		}
 		
 		// Add the user location to the map bounds
 		MapBounds bounds = new MapBounds();
 		bounds.addPoint(userLoc.getLatitudeE6(), userLoc.getLongitudeE6());
 		
 		// Create an overlay with a blue flag for the user location
 		Drawable blueDrawable = getResources().getDrawable(R.drawable.flag_blue);
 		MapOverlay blueOverlay = new MapOverlay(this, blueDrawable);
 		blueOverlay.addOverlay(new OverlayItem(userLoc, null, null));
 		
 		// Add the blue overlay
 		List<Overlay> mapOverlays = mapView.getOverlays();
 		mapOverlays.clear();
 		mapOverlays.add(blueOverlay);
 		
 		// Always rebuild red overlay to get the bounds correct
 		Drawable redDrawable = getResources().getDrawable(R.drawable.flag_red);
 		MapOverlay redOverlay = new MapOverlay(this, redDrawable);
 		final Iterator<OverlayItem> destIterator = mDest.iterator();
 		do {
 			OverlayItem item = destIterator.next();
 			redOverlay.addOverlay(item);
 			GeoPoint dest = item.getPoint();
 			bounds.addPoint(dest.getLatitudeE6(), dest.getLongitudeE6());
 		} while (destIterator.hasNext());
 		mapOverlays.add(redOverlay); 
 		
 		// Get map controller, animate to zoom center, and set zoom span
 		final MapController mapController = mapView.getController();
 		GeoPoint zoomCenter = new GeoPoint(bounds.getLatCenterE6(), bounds.getLonCenterE6());
 		mapController.animateTo(zoomCenter);
 		mapController.zoomToSpan(bounds.getLatSpanE6(), bounds.getLonSpanE6());
 	}
 	
 	private void startNavigation(String dest) {
 		Uri.Builder b = Uri.parse(MAPS_URL).buildUpon();
 		b.appendQueryParameter("daddr", dest);
 		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, b.build());
 		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
 		startActivity(intent);
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateDialog(int)
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		Context context = getApplicationContext();
 		
 		switch (id) {
 		case DLG_NAV:
 			for (EventHolder eh : mEventHolders) {
 				mDestList.add(eh.location);
 			}
 			builder.setTitle(R.string.dlg_nav_title)
 //				   .setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mDestList), 
 				   .setAdapter(new EventListAdapter(context, R.layout.event_list_item, mEventCursor, true),
 						   new DialogInterface.OnClickListener() {
 							
 						public void onClick(DialogInterface dialog, int which) {
 							// Pull out the selected destination and send nav intent
 							startNavigation(mDestList.get(which));
 						}
 					});
 			break;
 		case DLG_RATE:
 			// Build a "rate my app" dialog
 			builder.setTitle(R.string.dlg_rate_title)
 				   .setMessage(R.string.dlg_rate_msg)
 				   .setPositiveButton(R.string.rate_it, new DialogInterface.OnClickListener() {
 					
 						public void onClick(DialogInterface dialog, int which) {
 							// Send user to app details page
 							SharedPreferences alertStats = getSharedPreferences(PREF_ALERT_STATS, Activity.MODE_PRIVATE);
 							alertStats.edit().putBoolean(KEY_RATED_ALREADY, true).commit();
 							startActivity(new Intent(Intent.ACTION_VIEW, APP_DETAILS_URI));
 							finish();
 						}
 					})
 					.setNegativeButton(R.string.decline_text, new DialogInterface.OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							finish();
 						}
 					});
 			break;
 		}
 		
 		return builder.create();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.google.android.maps.MapActivity#onPause()
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		// Disable the user location
 		if (mUserLocationOverlay != null) { 
 			mUserLocationOverlay.disableMyLocation();
 			mUserLocationOverlay.disableCompass();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.google.android.maps.MapActivity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		// Re-enable the user location
 		if (mUserLocationOverlay != null && 
 				((String) mSwitcher.getCurrentView().getTag()).equals(TAG_TRAFFIC_VIEW)) { 
 			mUserLocationOverlay.enableMyLocation();
 			mUserLocationOverlay.enableCompass();
 		}
 		
 		// Switch to the traffic view if an ad was just shown
 		if (mAdJustShown) {
 			switchToTrafficView();
 			mAdJustShown = false;
 		}
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		// No route will be displayed, since that would cover up the traffic
 		return false;
 	}
 	
 	private class EventHolder {
 		String json;
 		String title;
 		String location;
 	}
 	
 	private class MapBounds {
 		private int mLatMinE6 = 0;
 		private int mLatMaxE6 = 0;
 		private int mLonMinE6 = 0;
 		private int mLonMaxE6 = 0;
 		private static final double RATIO = 1.25;
 		
 		protected MapBounds() {
 			super();
 		}
 		
 		protected void addPoint(int latE6, int lonE6) {
 			// Check if this latitude is the minimum
 			if (mLatMinE6 == 0 || latE6 < mLatMinE6) { mLatMinE6 = latE6; }
 			// Check if this latitude is the maximum
 			if (mLatMaxE6 == 0 || latE6 > mLatMaxE6) { mLatMaxE6 = latE6; }
 			// Check if this longitude is the minimum
 			if (mLonMinE6 == 0 || lonE6 < mLonMinE6) { mLonMinE6 = lonE6; }
 			// Check if this longitude is the maximum
 			if (mLonMaxE6 == 0 || lonE6 > mLonMaxE6) { mLonMaxE6 = lonE6; }
 		}
 		
 		protected int getLatSpanE6() {
 			return (int) Math.abs(RATIO * (mLatMaxE6 - mLatMinE6));
 		}
 		
 		protected int getLonSpanE6() {
 			return (int) Math.abs(RATIO * (mLonMaxE6 - mLonMinE6));
 		}
 		
 		protected int getLatCenterE6() {
 			return (int) (mLatMaxE6 + mLatMinE6)/2;
 		}
 		
 		protected int getLonCenterE6() {
 			return (int) (mLonMaxE6 + mLonMinE6)/2;
 		}
 	}
 }
