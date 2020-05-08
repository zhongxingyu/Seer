 package net.ecoarttech.ihplus;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import net.ecoarttech.ihplus.gps.CurrentLocationOverlay;
 import net.ecoarttech.ihplus.gps.DirectionPathOverlay;
 import net.ecoarttech.ihplus.gps.EndVistaOverlay;
 import net.ecoarttech.ihplus.gps.SingleVistaOverlay;
 import net.ecoarttech.ihplus.model.ActionType;
 import net.ecoarttech.ihplus.model.Hike;
 import net.ecoarttech.ihplus.model.ScenicVista;
 import net.ecoarttech.ihplus.network.NetworkConstants;
 import net.ecoarttech.ihplus.util.Constants;
 import net.ecoarttech.ihplus.util.PhotoProvider;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.ContactsContract;
 import android.provider.MediaStore;
 import android.telephony.SmsManager;
 import android.text.Html;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 
 public class IHMapActivity extends MapActivity {
 	private static final String TAG = "IH+ - IHMapView";
 	public static final String BUNDLE_START = "start";
 	public static final String BUNDLE_END = "end";
 	public static final String PROXIMITY_INTENT = "net.ecoarttech.ihplus.ProximityIntent";
 	public final static int TAKE_PHOTO_ACTIVITY = 0;
 	protected Context mContext;
 	protected MapView mMapView;
 	protected MapController mMapController;
 	protected GeoPoint geoPoint;
 	protected CurrentLocationOverlay mCurrentLocationOverlay;
 	protected boolean mRouteShown = false;
 	protected Hike mHike = new Hike();
 	protected LocationManager mLocMgr;
 	protected static ScenicVista mPhotoVista;
 	protected Uri mPhotoUri;
 	protected ProgressDialog mDialog;
 	protected HashMap<String, String> mContacts = new HashMap<String, String>();
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.map);
 		this.mContext = this;
 		// setup map view & view elements
 		mMapView = (MapView) findViewById(R.id.map_view);
 		mMapView.setSatellite(false);
 
 		mLocMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		mCurrentLocationOverlay = new CurrentLocationOverlay(this, mMapView);
 		mCurrentLocationOverlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				mMapView.getController().animateTo(mCurrentLocationOverlay.getMyLocation());
 			}
 		});
 		mMapView.getOverlays().add(mCurrentLocationOverlay);
 
 		// populate user's contact list for any text actions (to prevent db lookups every time this kind of action takes
 		// place)
 		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
 		String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
 				ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE };
 
 		Cursor cur = getContentResolver().query(uri, projection, null, null, null);
 
 		int indexName = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
 		int indexNumber = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
 		int indexType = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
 
 		if (cur.moveToFirst()) {
 			do {
 				String name = cur.getString(indexName);
 				String number = cur.getString(indexNumber);
 				if (number != null) {
 					int type = cur.getInt(indexType);
 					String phoneType;
 					switch (type) {
 					case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE: {
 						phoneType = " (Mobile)";
 						break;
 					}
 					case ContactsContract.CommonDataKinds.Phone.TYPE_HOME: {
 						phoneType = " (Home)";
 						break;
 					}
 					case ContactsContract.CommonDataKinds.Phone.TYPE_WORK: {
 						phoneType = " (Work)";
 						break;
 					}
 					default: {
 						phoneType = " (Other)";
 						break;
 					}
 					}
 					mContacts.put(name + phoneType, number);
 				}
 				// Do work...
 			} while (cur.moveToNext());
 		}
 		cur.close();
 	}
 
 	/*
 	 * Return back to Hike Activity from taking a photo. Mark vista as complete if the activity succeeded.
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		Log.d(TAG, "result code: " + resultCode);
 		if (requestCode == TAKE_PHOTO_ACTIVITY) {
 			if (resultCode == Activity.RESULT_OK)
 				mPhotoVista.setPhotoUri(mPhotoUri);
 			if (mPhotoVista.isComplete()) {
 				// allow user to move on to the next vista
 				markVistaAsCompleted(mPhotoVista);
 			}
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.d(TAG, "onResume");
 		mCurrentLocationOverlay.enableMyLocation();
 		// enable all vista locationListeners
 		if (!mHike.isComplete()) {
 			Log.d(TAG, "hike isn't done yet! Let's re-enable the vista listeners");
 			for (ScenicVista vista : mHike.getVistas()) {
 				Log.d(TAG, "we have a vista!");
 				vista.reenableIntent(mContext, mLocMgr);
 			}
 		}
 	}
 
 	protected void onPause() {
 		super.onPause();
 		mCurrentLocationOverlay.disableMyLocation();
 		for (ScenicVista vista : mHike.getVistas()) {
 			vista.pauseIntent(mContext, mLocMgr);
 		}
 	};
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		for (ScenicVista vista : mHike.getVistas()) {
 			vista.cancelIntent();
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (mHike.isPartiallyComplete()) {
 			showHikeWarning();
 		} else {
 			super.onBackPressed();
 		}
 	}
 
 	protected void drawPath(String[] pairs) {
 		String[] lngLat = pairs[0].split(",");
 		// STARTING POINT
 		double startLat = Double.parseDouble(lngLat[1]);
 		double startLng = Double.parseDouble(lngLat[0]);
 		GeoPoint startGP = new GeoPoint((int) (startLat * 1E6), (int) (startLng * 1E6));
 
 		mHike.addPoint(startGP);
 		mHike.setStartPoints(startLat, startLng);
 		mMapController = mMapView.getController();
 		geoPoint = startGP;
 		mMapController.setCenter(geoPoint);
 		mMapController.setZoom(16);
 		mMapView.getOverlays().add(new DirectionPathOverlay(startGP, startGP));
 
 		// NAVIGATE THE PATH
 		GeoPoint gp1;
 		GeoPoint gp2 = startGP;
 		for (int i = 1; i < pairs.length; i++) {
 			lngLat = pairs[i].split(",");
 			gp1 = gp2;
 			// watch out! For GeoPoint, first:latitude, second:longitude
 			gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[1]) * 1E6), (int) (Double.parseDouble(lngLat[0]) * 1E6));
 			mMapView.getOverlays().add(new DirectionPathOverlay(gp1, gp2));
 			Log.d("xxx", "pair:" + pairs[i]);
 			mHike.addPoint(gp2);
 		}
 
 		// END POINT
 		mMapView.getOverlays().add(new DirectionPathOverlay(gp2, gp2));
 
 		mMapView.getController().animateTo(startGP);
 		mMapView.setBuiltInZoomControls(true);
 		mMapView.displayZoomControls(true);
 		mRouteShown = true;
 	}
 
 	@Override
 	protected boolean isRouteDisplayed() {
 		return mRouteShown;
 	}
 
 	protected void drawVistas() {
 		for (ScenicVista vista : mHike.getVistas()) {
 			if (vista.isEndVista())
 				mMapView.getOverlays().add(new EndVistaOverlay(mContext, vista));
 			else
 				mMapView.getOverlays().add(new SingleVistaOverlay(mContext, vista));
 		}
 		// remove and re-add current location over lay (so it will display on top of the vistas)
 		mMapView.getOverlays().remove(mCurrentLocationOverlay);
 		mMapView.getOverlays().add(mCurrentLocationOverlay);
 	}
 
 	private static final int VISTA_ENTERED = 1;
 
 	protected void enableVistaProximityAlerts() {
 		for (ScenicVista vista : mHike.getVistas()) {
 			// setup proximity alert
 			Intent intent = new Intent(PROXIMITY_INTENT + vista.hashCode());
 			Log.d(TAG, "intent action: " + intent.getAction());
 			PendingIntent pi = PendingIntent.getBroadcast(mContext, VISTA_ENTERED, intent, 0);
 
			mLocMgr.addProximityAlert(vista.getLat(), vista.getLong(), 15, -1, pi);
 			Log.d(TAG, "added alert for: " + vista.getLat() + " long: " + vista.getLong());
 
 			// set up pending intent recievers
 			IntentFilter filter = new IntentFilter(PROXIMITY_INTENT + vista.hashCode());
 			VistaEnteredReceiver br = new VistaEnteredReceiver();
 			registerReceiver(br, filter);
 			vista.setPendingIntentAndReceiver(pi, br);
 		}
 	}
 
 	public void onSearchClick(View v) {
 		Intent i = new Intent(this, SearchActivity.class);
 		startActivity(i);
 	}
 
 	public void onHikesClick(View v) {
 		showHikeWarning();
 	}
 
 	private void showHikeWarning() {
 		if (mHike.isOriginal()) {
 			// (warn that current hike will be lost)
 			new AlertDialog.Builder(this).setTitle("create a new hike").setMessage(
 					"are you sure you would like to create a new hike?\nthe current hike will be lost.")
 					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							Intent i = new Intent(IHMapActivity.this, CreateHikeActivity.class);
 							startActivity(i);
 							dialog.dismiss();
 						}
 					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							dialog.dismiss();
 						}
 					}).show();
 		} else { // just start create hike activity
 			Intent i = new Intent(this, CreateHikeActivity.class);
 			startActivity(i);
 		}
 	}
 
 	public class VistaEnteredReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Log.d(TAG, "IH broadcast, test: " + intent.getAction());
 			Bundle extras = intent.getExtras();
 			String index = intent.getAction().split(PROXIMITY_INTENT)[1];
 			Integer i = Integer.valueOf(index);
 			boolean entering = extras.getBoolean(LocationManager.KEY_PROXIMITY_ENTERING);
 			String enter = entering ? "Entering" : "Leaving";
 			Log.i(TAG, enter + " a scenic vista.");
 			final ScenicVista enteredVista = mHike.getVistaByHashCode(i);
 			if (entering) {
 				// display hike & vista info at top of screen
 				findViewById(R.id.hike_layout).setVisibility(View.VISIBLE);
 				findViewById(R.id.vista_layout).setVisibility(View.VISIBLE);
 				TextView vistaInfo = (TextView) findViewById(R.id.vista_info);
 				vistaInfo.setText(Html.fromHtml(enteredVista.getAction()));
 				// Util.setBoldFont(mContext, findViewById(R.id.hike_name), findViewById(R.id.vista_label), vistaInfo,
 				// vistaCont);
 				// set click listeners
 				View vistaCont = findViewById(R.id.vista_cont);
 				setActionClickListeners(enteredVista, vistaCont);
 			} else {
 				// TODO - if vista task is not completed, tsk tsk! ??
 				if (enteredVista.isComplete()){
 					// if vista is complete, get rid of vista info bar
 					findViewById(R.id.hike_layout).setVisibility(View.GONE);
 					findViewById(R.id.vista_layout).setVisibility(View.GONE);
 				}
 			}
 		}
 	}
 
 	private void setActionClickListeners(final ScenicVista vista, View... views) {
 		for (View view : views) {
 			view.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					// depending on action type
 					// open note dialog
 					if (vista.getActionType() == ActionType.NOTE) {
 						final View alertContent = getLayoutInflater().inflate(R.layout.note_dialog, null);
 						new AlertDialog.Builder(mContext).setTitle("make a field note").setIcon(0)
 								.setView(alertContent).setPositiveButton("Done", new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface d, int which) {
 										EditText noteInput = (EditText) alertContent.findViewById(R.id.vista_note);
 										vista.setNote(noteInput.getText().toString());
 										if (vista.isComplete()) {
 											// allow user to move on to the next vista
 											markVistaAsCompleted(vista);
 										}
 									}
 								}).create().show();
 					} else if (vista.getActionType() == ActionType.MEDITATE) {
 						markVistaAsCompleted(vista);
 
 					} else if (vista.getActionType() == ActionType.PHOTO) {
 						// open camera intent
 						startCameraIntent(vista);
 					} else if (vista.getActionType() == ActionType.TEXT) {
 						final View alertContent = getLayoutInflater().inflate(R.layout.text_dialog, null);
 						// populate contacts dropdown
 						final Spinner contactSpinner = (Spinner) alertContent.findViewById(R.id.vista_contact);
 						final EditText confirmView = (EditText) alertContent.findViewById(R.id.confirm_contact);
 						final Button editConfirm = (Button) alertContent.findViewById(R.id.edit_confirm);
 						ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mContext,
 								android.R.layout.simple_spinner_item, new ArrayList<String>(mContacts.keySet()));
 						arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 						contactSpinner.setAdapter(arrayAdapter);
 						// set on selection listener to display selected number
 						contactSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 							@Override
 							public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 								confirmView.setText(mContacts.get(contactSpinner.getSelectedItem()));
 							}
 
 							@Override
 							public void onNothingSelected(AdapterView<?> parent) {}
 						});
 						// set listener for confirm button
 						editConfirm.setOnClickListener(new OnClickListener() {
 							
 							@Override
 							public void onClick(View v) {
 								confirmView.setEnabled(true);
 							}
 						});
 						new AlertDialog.Builder(mContext).setTitle("send a field note").setIcon(0)
 								.setView(alertContent).setPositiveButton("Send", new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface d, int which) {
 										EditText noteInput = (EditText) alertContent.findViewById(R.id.vista_note);
 										vista.setNote(noteInput.getText().toString());
 										String number = confirmView.getText().toString();//mContacts.get(contactSpinner.getSelectedItem());
 										Log.d(TAG, "selected number to send to: " + number);
 										if (number != null){
 											PendingIntent pi = PendingIntent.getActivity(mContext, 0, null, 0);
 											SmsManager sms = SmsManager.getDefault();
 											sms.sendTextMessage(number, null,
 													noteInput.getText().toString(), pi, null);
 										}
 										if (vista.isComplete()) {
 											// allow user to move on to the next vista
 											markVistaAsCompleted(vista);
 										}
 									}
 								}).create().show();
 					}
 				}
 			});
 		}
 	}
 
 	private void startCameraIntent(ScenicVista vista) {
 		// check that sdcard is available
 		String state = Environment.getExternalStorageState();
 		if (!Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			Toast.makeText(this, "make sure SD Card is available before taking photos.", Toast.LENGTH_LONG).show();
 			return;
 		}
 		// save the vista for which we are saving the photo
 		mPhotoVista = vista;
 
 		// create an intent for accessing the camera
 		Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 
 		ContentValues cameraValues = new ContentValues();
 		cameraValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
 		cameraValues.put(MediaStore.Images.Media.TITLE, vista.getPhotoTitle());
 		cameraValues.put(MediaStore.Images.Media.DESCRIPTION, mContext.getResources().getString(R.string.app_name));
 		Uri uri = mContext.getContentResolver().insert(Uri.parse("content://" + PhotoProvider.AUTHORITY), cameraValues);
 
 		pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
 		mPhotoUri = uri;
 		// take the picture by invoking the camera activity
 		startActivityForResult(pictureIntent, TAKE_PHOTO_ACTIVITY);
 	}
 
 	private void markVistaAsCompleted(ScenicVista vista) {
 		// remove vista info views
 		findViewById(R.id.hike_layout).setVisibility(View.GONE);
 		findViewById(R.id.vista_layout).setVisibility(View.GONE);
 		// remove pending intent
 		vista.pauseIntent(mContext, mLocMgr);
 		vista.cancelIntent();
 		// save vista info to db
 		vista.save(this, mHike.hashCode());
 		// check if the hike is complete
 		if (mHike.isComplete()) {
 			completeHike();
 		}
 	}
 
 	private void completeHike() {
 		// inflate input dialog
 		final View dialogView = getLayoutInflater().inflate(
 				mHike.isOriginal() ? R.layout.complete_hike_dialog : R.layout.complete_walk_hike_dialog, null);
 		new AlertDialog.Builder(this).setTitle("hike completed").setView(dialogView).setPositiveButton("share",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						// get user inputs
 						EditText username = (EditText) dialogView.findViewById(R.id.hike_username);
 						EditText hikeName = (EditText) dialogView.findViewById(R.id.hike_name);
 						EditText hikeDesc = (EditText) dialogView.findViewById(R.id.hike_desc);
 						mHike.setUsername(username.getText().toString());
 						if (mHike.isOriginal()) {
 							mHike.setName(hikeName.getText().toString());
 							mHike.setDescription(hikeDesc.getText().toString());
 						}
 						mHike.upload(mContext, mUploadHandler);
 					}
 				}).create().show();
 	}
 
 	private Handler mUploadHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			if (msg.what == NetworkConstants.FAILURE) {
 				// retry
 				showRetryDialog();
 			} else { // success
 				Toast.makeText(mContext, "hike was uploaded successfully", Toast.LENGTH_LONG).show();
 				// start 'Share Screen' and finish this one
 				Intent i = new Intent(mContext, ShareHikeActivity.class);
 				i.putExtra(Constants.BUNDLE_HIKE, mHike);
 				startActivity(i);
 				finish();
 			}
 		}
 	};
 
 	private void showRetryDialog() {
 		new AlertDialog.Builder(this).setTitle("oops").setMessage(
 				"something went wrong during the upload.\nwould you like to try again?").setPositiveButton("Re-try",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						mHike.upload(mContext, mUploadHandler);
 					}
 				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 			}
 		}).create().show();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 	}
 }
