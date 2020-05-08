 package tw.ipis.routetaiwan;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
import android.content.Intent;
 import android.content.DialogInterface.OnShowListener;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.location.Address;
 import android.location.Criteria;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.provider.Settings;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.ContextThemeWrapper;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.PopupWindow;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.location.LocationListener;
 import com.google.android.gms.location.LocationRequest;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 public class showmap extends Activity implements 
 GooglePlayServicesClient.ConnectionCallbacks,
 GooglePlayServicesClient.OnConnectionFailedListener,LocationListener {
 	/* Define area */
 	private static final int TEXT_INSTRUCTION = 0x12300001;
 	private static final int BUTTON_PLAN_ROUTE = 0x12300002;
 	private static final int IMAGE_INSTRUCTION = 0x12300003;
 	/* Define area end */
 
 	private static final String TAG = "~~showmap~~";
 	GoogleMap googleMap;
 	private LocationClient locationclient;
 	private LocationRequest locationrequest;
 	private boolean first_read = true;
 	private boolean pos_track = false;
 	private boolean button_exist = false;
 	MarkerOptions opt_start, opt_destination, opt_temp, opt_mypos;
	Marker start, dest, temp, mypos;
 	List<Marker> results;
 	Point p = new Point(0, 0);
 	private static final String projectdir = Environment.getExternalStorageDirectory() + "/.routetaiwan/";
 	EditText etLocation;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.showmap);
 		
 		LocationManager locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 		//1.選擇最佳提供器
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_FINE);
 		criteria.setAltitudeRequired(false);
 		criteria.setBearingRequired(false);
 		criteria.setCostAllowed(true);
 		criteria.setPowerRequirement(Criteria.POWER_LOW);
 		
 		Drawable dr = getResources().getDrawable(R.drawable.pos_notfollow);
 		Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
 		
 		Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 				(int) (32 * getResources().getDisplayMetrics().density), (int) (32 * getResources().getDisplayMetrics().density), true)); 
 		ImageButton trackme = (ImageButton) findViewById(R.id.myMapLocationButton);
 		trackme.setBackgroundDrawable(drawable);		
 		RelativeLayout.LayoutParams imageLayoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		imageLayoutParameters.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		imageLayoutParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		imageLayoutParameters.setMargins(0, 0, (int)(8 * getResources().getDisplayMetrics().density), (int)(8 * getResources().getDisplayMetrics().density));
 		trackme.setLayoutParams(imageLayoutParameters);
 
 		if (locationMgr.getBestProvider(criteria, true) == null ) {
 			AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.ThemeWithCorners));
 			dialog.setTitle(getResources().getString(R.string.no_loc_provider));
 			dialog.setMessage(getResources().getString(R.string.no_loc_provider_msg));
 			dialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 				}
 			});
 			dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					// do nothing
 				}
 			});
 			dialog.show();
 		}
 		results = new ArrayList<Marker>();
 
 		first_read = true;
 		// Getting Google Play availability status
 		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
 		// Showing status
 		if(status != ConnectionResult.SUCCESS){ // Google Play Services are not available
 			int requestCode = 10;
 			Log.d(TAG, "Google play service unavailable");
 			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
 			dialog.show();
 			Toast.makeText(this, "Google play service unavailable", Toast.LENGTH_LONG).show();
 		}else {    // Google Play Services are available
 			Log.d(TAG, "Google play service available");
 			final View cover = findViewById(R.id.mapcover);
 			
 			/* Check if it is the first time to use this app, If yes, show some instruction */
 			File chk_fist_use = new File(Environment.getExternalStorageDirectory() + "/.routetaiwan/.first_showmap2");
 			if(chk_fist_use.exists() == false) {
 
 				try {
 					chk_fist_use.createNewFile();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				
 				cover.setBackgroundColor(Color.argb(0x70, 0xA, 0xA, 0xA));
 				cover.setClickable(true);
 				cover.setFocusable(true);
 				
 				final ImageView image = new ImageView(this);
 				image.setId(IMAGE_INSTRUCTION);
 				image.setImageResource(R.drawable.showmap_instruction);
 				image.setAdjustViewBounds(true);
 				image.setBackgroundColor(Color.TRANSPARENT);
 				
 				TextView instruction = new TextView(this);
 				instruction.setId(TEXT_INSTRUCTION);
 				instruction.setText(getResources().getString(R.string.instruction));
 				instruction.setTextColor(Color.WHITE);
 				instruction.setTextSize(20);
 				instruction.setGravity(Gravity.CENTER);
 
 				Button ok = new Button(this);
 				ok.setText(getResources().getString(R.string.understand));
 				ok.setGravity(Gravity.CENTER);
 				ok.setTextColor(Color.WHITE);
 				
 				RelativeLayout ll = (RelativeLayout)findViewById(R.id.rl_showmap);
 				RelativeLayout.LayoutParams coverLayoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 				cover.setLayoutParams(coverLayoutParameters);
 				
 				imageLayoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 				imageLayoutParameters.addRule(RelativeLayout.CENTER_IN_PARENT);
 				imageLayoutParameters.setMargins(0, 120, 0, 0);
 				image.setLayoutParams(imageLayoutParameters);
 				
 				RelativeLayout.LayoutParams textLayoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 				textLayoutParameters.addRule(RelativeLayout.BELOW, image.getId());
 				textLayoutParameters.addRule(RelativeLayout.CENTER_IN_PARENT, image.getId());
 				instruction.setLayoutParams(textLayoutParameters);
 
 				RelativeLayout.LayoutParams buttonLayoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 				buttonLayoutParameters.addRule(RelativeLayout.BELOW, instruction.getId());
 				buttonLayoutParameters.addRule(RelativeLayout.CENTER_IN_PARENT, instruction.getId());
 				ok.setLayoutParams(buttonLayoutParameters);
 
 				ll.addView(image);
 				ll.addView(instruction);
 				ll.addView(ok);
 
 				ok.setOnClickListener(new OnClickListener(){  
 					public void onClick(View v) {  
 						RelativeLayout ll = (RelativeLayout)findViewById(R.id.rl_showmap);
 						TextView instruction = (TextView) findViewById(TEXT_INSTRUCTION);
 
 						ll.removeView(image);
 						ll.removeView(instruction);
 						ll.removeView(v);
 
 						final Animation animTrans = AnimationUtils.loadAnimation(showmap.this, R.anim.anim_alpha_out);
 						cover.setAnimation(animTrans);
 
 						/* Make the cover fully transparent after 500ms */
 						Handler reset_view = new Handler();
 						reset_view.postDelayed(new Runnable()
 						{
 							public void run()
 							{
 								cover.setBackgroundColor(Color.argb(0x0, 0x0, 0x0, 0x0));
 								cover.setClickable(false);
 								cover.setFocusable(false);
 							}
 						}, 500);
 					}  
 				});
 			}
 
 			opt_start = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.map_start));
 			opt_destination = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.map_destination));
 			opt_temp = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
 			opt_mypos = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.map_mylocation));
 
 			locationclient = new LocationClient(this,this,this);
 			locationclient.connect();
 
 			// Getting reference to the SupportMapFragment of activity_main.xml
 			MapFragment fm = ((MapFragment)getFragmentManager().findFragmentById(R.id.mapv2));
 
 			// Getting GoogleMap object from the fragment
 			googleMap = fm.getMap();
 
 			// Enabling MyLocation Layer of Google Map
			googleMap.setMyLocationEnabled(false);
 
 			googleMap.getUiSettings().setCompassEnabled(true);
 			googleMap.getUiSettings().setZoomControlsEnabled(false);
 			googleMap.getUiSettings().setAllGesturesEnabled(true);
 			
 			CameraPosition camPosition = new CameraPosition.Builder()
 			.target(new LatLng(23.583234, 120.582598))
 			.zoom(7)
 			.build();
 
 			googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
 			
 			googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
 				@Override
 				public void onInfoWindowClick(Marker marker) {
 					opt_temp.position(marker.getPosition());
 					if(temp != null)
 						temp.remove();
 					temp = googleMap.addMarker(opt_temp);
 					showPopup(showmap.this, marker.getPosition(), marker.getTitle());
 				}
 			});
 			
 			etLocation = (EditText) findViewById(R.id.search_map);
 			
 //			etLocation.setOnClickListener(new OnClickListener(){  
 //				public void onClick(View v) {
 //					etLocation.setError(getResources().getString(R.string.info_showmap_edit));
 //				}  
 //			});
 
 			cover.setOnTouchListener(new OnTouchListener() {
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 //					etLocation.setError(null);
 					p.x = (int)event.getX();
 					p.y = (int)event.getY();
 					return false;
 				}
 			});
 

 			googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
 				@Override
 				public void onMapLongClick(LatLng position) {
 					opt_temp.position(position);
 					if(temp != null)
 						temp.remove();
 					temp = googleMap.addMarker(opt_temp);
 					showPopup(showmap.this, position, null);
 				}
 			});
 
 			if(locationclient != null && locationclient.isConnected()){
 				locationrequest = LocationRequest.create();
 				locationrequest.setInterval(100);
 				locationclient.requestLocationUpdates(locationrequest, this);
 				Location last = locationclient.getLastLocation();
 				focus_on_me(last);
 			}
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		locationclient.disconnect();
 		first_read = true;
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult arg0) {
 	}
 
 	@Override
 	public void onConnected(Bundle arg0) {
 		if(locationclient != null && locationclient.isConnected()) {
 			locationrequest = LocationRequest.create();
 			locationrequest.setInterval(100);
 			locationclient.requestLocationUpdates(locationrequest, this);
 			Location last = locationclient.getLastLocation();
 			first_read = true;
 			focus_on_me(last);
 		}
 	}
 
 	@Override
 	public void onDisconnected() {
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if(locationclient!=null)
 			locationclient.disconnect();
 		first_read = true;
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 	}
 
 	LocationListener locationListener1 = new LocationListener(){
 		@Override
 		public void onLocationChanged(Location location) {
 			focus_on_me(location);
 		}
 	};
 
 	public void focus_on_me(Location location) {
 
 		if(location != null){
 			// Getting latitude of the current location
 			double latitude = location.getLatitude();
 
 			// Getting longitude of the current location
 			double longitude = location.getLongitude();
 			
 			Log.i(TAG, "focus!");
 
 			if(first_read) {
 				CameraPosition camPosition = new CameraPosition.Builder()
 				.target(new LatLng(latitude, longitude))
 				.zoom(16)
 				.build();
 
 				googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camPosition));
 				first_read = false;
 			}
 			else if(pos_track) {
 				CameraPosition camPosition = new CameraPosition.Builder()
 				.target(new LatLng(latitude, longitude))
 				.zoom(googleMap.getCameraPosition().zoom)
 				.build();
 
 				googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
 			}
 			opt_mypos.position(new LatLng(latitude, longitude));
			if(mypos != null)
				mypos.remove();
			mypos = googleMap.addMarker(opt_mypos);
 		}
 	}
 
 	@Override
 	public void onLocationChanged(Location arg0) {
 		focus_on_me(arg0);
 	}
 
 	public void create_button() {
 		if(button_exist == false) {
 			Button gotoplan = new Button(this);
 			gotoplan.setId(BUTTON_PLAN_ROUTE);
 			gotoplan.setText(getResources().getString(R.string.goto_planroute));
 			gotoplan.setBackgroundResource(R.drawable.seletor_ltgray);
 			gotoplan.setTextColor(Color.WHITE);
 
 			RelativeLayout ll = (RelativeLayout)findViewById(R.id.rl_showmap);
 			RelativeLayout.LayoutParams buttonLayoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			buttonLayoutParameters.setMargins(0, 0, 0, 0);
 
 			// Add Rule to Layout
 			buttonLayoutParameters.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 
 			// Setting the parameters on the Button
 			gotoplan.setLayoutParams(buttonLayoutParameters); 
 			ll.addView(gotoplan);
 			final Animation animTrans = AnimationUtils.loadAnimation(this, R.anim.anim_translate_y);
 			gotoplan.startAnimation(animTrans);
 
 			button_exist = true;
 			gotoplan.setOnClickListener(new OnClickListener(){  
 				public void onClick(View v) {  
 					Intent route = new Intent(showmap.this, planroute.class);
 					Bundle bundle=new Bundle();
 
 					if(start != null) {
 						bundle.putString("start", new DecimalFormat("###.######").format(start.getPosition().latitude) + "," + new DecimalFormat("###.######").format(start.getPosition().longitude));
 					}
 					if(dest != null) {
 						bundle.putString("end", new DecimalFormat("###.######").format(dest.getPosition().latitude) + "," + new DecimalFormat("###.######").format(dest.getPosition().longitude));
 					}
 
 					route.putExtras(bundle);
 					startActivity(route);
 				}  
 			});
 		}
 	}
 
 	public void remove_button() {
 		if(button_exist == true) {
 			Button gotoplan = (Button) findViewById(BUTTON_PLAN_ROUTE);
 			gotoplan.setOnClickListener(null);
 
 			final Animation animTrans = AnimationUtils.loadAnimation(showmap.this, R.anim.anim_translate_y_out);
 			gotoplan.setAnimation(animTrans);
 
 			/* Make the cover fully transparent after 500ms */
 			Handler reset_view = new Handler();
 			reset_view.postDelayed(new Runnable()
 			{
 				public void run()
 				{
 					Button gotoplan = (Button) findViewById(BUTTON_PLAN_ROUTE);
 					RelativeLayout ll = (RelativeLayout)findViewById(R.id.rl_showmap);
 					ll.removeView(gotoplan);
 					button_exist = false;
 				}
 			}, 500);
 		}
 	}
 	
 	public static String getMD5EncryptedString(String encTarget){
 		MessageDigest mdEnc = null;
 		try {
 			mdEnc = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			System.out.println("Exception while encrypting to md5");
 			e.printStackTrace();
 		} // Encryption algorithm
 		mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
 		String md5 = new BigInteger(1, mdEnc.digest()).toString(16) ;
 		return md5;
 	}
 	
 	public void save2fav(String default_str) {
 		AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.ThemeWithCorners));
 		dialog.setTitle(getResources().getString(R.string.edit_title));
 		final EditText editText = new EditText(this);
 		if(default_str != null) {
 			editText.setText(default_str);
 			editText.selectAll();
 		}
 		editText.setOnFocusChangeListener(new OnFocusChangeListener() {
 
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (hasFocus) {
 					v.setBackgroundColor(Color.WHITE);
 					((EditText) v).setTextColor(Color.BLACK);
 				} 
 			}
 		});
 		
 		dialog.setView(editText);
 		dialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				String fav_description = editText.getText().toString();
 				
 				String fav_point = String.format("save,%s,%s,%s", fav_description.isEmpty() ? 
 						getResources().getString(R.string.fav_points) : fav_description, 
 						new DecimalFormat("###.######").format(temp.getPosition().latitude), new DecimalFormat("###.######").format(temp.getPosition().longitude));
 				String filename = projectdir + getMD5EncryptedString(fav_point) + ".point";
 				
 				File file = new File(filename);
 				if (!file.exists()) {
 					Log.i(TAG, String.format("file:%s content:%s", filename, fav_point));
 					
 					FileWriter writer;
 					try {
 						writer = new FileWriter(filename);
 						writer.write(fav_point);
 						writer.close();
 						Toast.makeText(showmap.this, getResources().getString(R.string.saved_to_fav) , Toast.LENGTH_SHORT).show();
 					} catch (IOException e) {
 						e.printStackTrace();
 						Toast.makeText(showmap.this, getResources().getString(R.string.info_internal_error) , Toast.LENGTH_SHORT).show();
 					}
 				}
 				else {
 					Toast.makeText(showmap.this, getResources().getString(R.string.file_already_existed) , Toast.LENGTH_SHORT).show();
 				}
 				editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
 			}
 		});
 		dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				// do nothing
 				temp.remove();
 				editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
 			}
 		});
 		
 		final AlertDialog diag = dialog.create();
 		
 		diag.setOnShowListener(new OnShowListener()
 		{
 		    @Override
 		    public void onShow(final DialogInterface dialog)
 		    {
 		    	editText.requestFocus();
 		    	((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, 0);
 		    }
 		});
 		
 		editText.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable s) {
 			}
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				if(editText.getText().toString().contains(",")) {
 					editText.setError(getResources().getString(R.string.info_illegal_titlename));
 					diag.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
 				}
 				else if(editText.getText().toString().isEmpty()) {
 					diag.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
 				}
 				else {
 					editText.setError(null);
 					diag.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
 				}
 			}
 		});
 		diag.show();
 	}
 	
 	public void send_sms(String title) {
 		/* Pop-up a dialog to ask for permission */
 		Intent launchpop = new Intent(this, sms_send.class);
 		Bundle bundle=new Bundle();
 		
 		bundle.putString("title", title != null ? title : "");
 		bundle.putString("latlng"
 				, new DecimalFormat("###.######").format(temp.getPosition().latitude) + "," + new DecimalFormat("###.######").format(temp.getPosition().longitude));
 		launchpop.putExtras(bundle);
 
 		startActivity(launchpop);
 	}
 
 	// The method that displays the popup.
 	@SuppressWarnings("deprecation")
 	private void showPopup(final Activity context, final LatLng position, final String default_name) {
 
 		// Inflate the popup_layout.xml
 		LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.menu1);
 		LayoutInflater layoutInflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = layoutInflater.inflate(R.layout.menu_route, viewGroup);
 
 		// Creating the PopupWindow
 		final PopupWindow popup = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
 		
 		// Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
 		int OFFSET_X = 10;
 		int OFFSET_Y = 60;
 
 		// Clear the default translucent background
 		popup.setBackgroundDrawable(new BitmapDrawable());
 		popup.setOutsideTouchable(true);
 		popup.setAnimationStyle(R.style.PopupWindowAnimation);
 
 		// Displaying the popup at the specified location, + offsets.
 		popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
 		
 		Drawable dr = getResources().getDrawable(R.drawable.start);
 		Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
 		
 		Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 				(int) (24 * getResources().getDisplayMetrics().density), (int) (24 * getResources().getDisplayMetrics().density), true)); 
 
 		Button set_start = (Button) layout.findViewById(R.id.add_to_departure);
 		set_start.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
 		set_start.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if(start != null)
 					start.remove();
 				temp.remove();
 				opt_start.position(position);
 				start = googleMap.addMarker(opt_start);
 				popup.dismiss();
 				create_button();
 				v.setOnClickListener(null);
 			}
 		});
 		
 		dr = getResources().getDrawable(R.drawable.destination);
 		bitmap = ((BitmapDrawable) dr).getBitmap();
 		
 		drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 				(int) (24 * getResources().getDisplayMetrics().density), (int) (24 * getResources().getDisplayMetrics().density), true)); 
 
 		Button set_end = (Button) layout.findViewById(R.id.add_to_arrival);
 		set_end.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
 		set_end.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if(dest != null)
 					dest.remove();
 				temp.remove();
 				opt_destination.position(position);
 				dest = googleMap.addMarker(opt_destination);
 				popup.dismiss();
 				create_button();
 				v.setOnClickListener(null);
 			}
 		});
 		
 		dr = getResources().getDrawable(R.drawable.friend);
 		bitmap = ((BitmapDrawable) dr).getBitmap();
 		
 		drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 				(int) (24 * getResources().getDisplayMetrics().density), (int) (24 * getResources().getDisplayMetrics().density), true)); 
 		
 		Button send_friend = (Button) layout.findViewById(R.id.send_friend);
 		send_friend.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
 		send_friend.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				popup.dismiss();
 				send_sms(default_name);
 				v.setOnClickListener(null);
 			}
 		});
 		
 		dr = getResources().getDrawable(R.drawable.favorite_32);
 		bitmap = ((BitmapDrawable) dr).getBitmap();
 		
 		drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 				(int) (24 * getResources().getDisplayMetrics().density), (int) (24 * getResources().getDisplayMetrics().density), true)); 
 		
 		Button save_point = (Button) layout.findViewById(R.id.save);
 		save_point.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
 		save_point.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				save2fav(default_name);
 				popup.dismiss();
 				v.setOnClickListener(null);
 			}
 		});
 		
 		dr = getResources().getDrawable(R.drawable.clean);
 		bitmap = ((BitmapDrawable) dr).getBitmap();
 		
 		drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 				(int) (24 * getResources().getDisplayMetrics().density), (int) (24 * getResources().getDisplayMetrics().density), true)); 
 		
 		// Getting a reference to Close button, and close the popup when clicked.
 		Button clearall = (Button) layout.findViewById(R.id.close);
 		clearall.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
 		clearall.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if(start != null)
 					start.remove();
 				if(dest != null)
 					dest.remove();
 				temp.remove();
 				remove_button();
 				for(int i=0; i<results.size(); i++) {
 					results.get(i).remove();
 				}
 				if(results.size() > 0)
 					results.clear();
 				popup.dismiss();
 				v.setOnClickListener(null);
 			}
 		});
 	}
 	
 	public void track_me(View v) {
 		ImageButton trackme = (ImageButton) findViewById(R.id.myMapLocationButton);
 		if(pos_track == false) {
 			pos_track = true;
 			Drawable dr = getResources().getDrawable(R.drawable.pos_follow);
 			Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
 			
 			Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 					(int) (32 * getResources().getDisplayMetrics().density), (int) (32 * getResources().getDisplayMetrics().density), true)); 
 			trackme.setBackgroundDrawable(drawable);
 			if(locationclient != null && locationclient.isConnected()) {
 				Location last = locationclient.getLastLocation();
 				focus_on_me(last);
 			}
 		}
 		else {
 			pos_track = false;
 			Drawable dr = getResources().getDrawable(R.drawable.pos_notfollow);
 			Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
 			
 			Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 					(int) (32 * getResources().getDisplayMetrics().density), (int) (32 * getResources().getDisplayMetrics().density), true)); 
 			trackme.setBackgroundDrawable(drawable);
 		}
 	}
 
 	public void google_search(View v) {
 		etLocation = (EditText) findViewById(R.id.search_map);
 		etLocation.setError(null);
 
 		// Getting user input location
 		String location = etLocation.getText().toString();
 
 		InputMethodManager imm = (InputMethodManager)getSystemService(
 				Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
 
 		if(location!=null && !location.equals("")){
 			pos_track = false;
 			Drawable dr = getResources().getDrawable(R.drawable.pos_notfollow);
 			Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
 			
 			Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 					(int) (32 * getResources().getDisplayMetrics().density), (int) (32 * getResources().getDisplayMetrics().density), true)); 
 			ImageButton trackme = (ImageButton) findViewById(R.id.myMapLocationButton);
 			trackme.setBackgroundDrawable(drawable);
 			v.setEnabled(false);
 			new Geocoder_get_address_by_name().execute(location);
 		}
 //		else
 //			etLocation.setError(getResources().getString(R.string.info_showmap_edit));
 	}
 	
 	private class Geocoder_get_address_by_name extends AsyncTask<String, Void, List<Address>>{
 
 		@Override
 		protected List<Address> doInBackground(String... locationName) {
 			// Creating an instance of Geocoder class
 			Geocoder geocoder = new Geocoder(getBaseContext());
 			List<Address> addresses = null;
 
 			try {
 				// Getting a maximum of 10 Address that matches the input text
 				addresses = geocoder.getFromLocationName(locationName[0], 20);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}			
 			return addresses;
 		}
 
 
 		@Override
 		protected void onPostExecute(List<Address> addresses) {
 
 			// Clears all the existing markers on the map
 			for(int i=0; i<results.size(); i++) {
 				results.get(i).remove();
 			}
 
 			if(results.size() > 0)
 				results.clear();
 
 
 			if(addresses==null || addresses.size()==0){
 				Toast.makeText(getBaseContext(), getResources().getString(R.string.info_no_result), Toast.LENGTH_SHORT).show();
 			}
 			else {
 				final LatLngBounds.Builder builder = new LatLngBounds.Builder();
 
 				// Adding Markers on Google Map for each matching address
 				for(int i=0;i<addresses.size();i++){
 
 					Address address = (Address) addresses.get(i);
 
 					// Creating an instance of GeoPoint, to display in Google Map
 					LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
 
 					if(pos_not_in_taiwan(latLng))
 						continue;
 
 					MarkerOptions markerOptions = new MarkerOptions();
 					markerOptions.position(latLng);
 					markerOptions.title(address.getFeatureName());
					markerOptions.snippet(address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) 
							: address.getAdminArea() + address.getLocality());
 
 					Marker marker = googleMap.addMarker(markerOptions);
 					if(i == 0)
 						marker.showInfoWindow();
 
 					results.add(marker);
 
 					builder.include(latLng);
 				}
 
 				if(results.size() == 1)
 					googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(results.get(0).getPosition(), 15));
 				else if(results.size() > 1)
 					googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 80));
 				else
 					Toast.makeText(getBaseContext(), getResources().getString(R.string.info_no_result), Toast.LENGTH_SHORT).show();
 
 				ImageButton search_btn = (ImageButton)findViewById(R.id.search_location);
 				search_btn.setEnabled(true);
 			}	
 		}
 	}
 	
 	public boolean pos_not_in_taiwan(LatLng pos) {
 		double lat = pos.latitude;
 		double lon = pos.longitude;
 		
 		if(lon > 122 || lon < 120)
 			return true;
 		else if (lat > 25.5 || lat < 21.9)
 			return true;
 		else
 			return false;
 	}
 }
