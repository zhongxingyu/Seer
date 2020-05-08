 package com.cruzroja.creuroja;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.SearchView;
 import android.widget.SearchView.OnQueryTextListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 public class MainActivity extends FragmentActivity implements
 		LoaderCallbacks<ArrayList<Location>>, OnCheckedChangeListener,
 		OnInfoWindowClickListener {
 	public static final int LOADER_CONNECTION = 1;
 	public static final int LOADER_DIRECTIONS = 2;
 
 	protected static final int MAP_STYLE_NORMAL = 0;
 	protected static final int MAP_STYLE_HYBRID = 1;
 	protected static final int MAP_STYLE_TERRAIN = 2;
 	protected static final int MAP_STYLE_SATELLITE = 3;
 
 	private static final String SHOW_ASAMBLEA = "showAsamblea";
 	private static final String SHOW_BRAVO = "showBravo";
 	private static final String SHOW_CUAP = "showCuap";
 	private static final String SHOW_EMBARCACION = "showEmbarcacion";
 	private static final String SHOW_HOSPITAL = "showHospital";
 	private static final String SHOW_PREVENTIVO = "showPreventivo";
 	private static final String MAP_STYLE = "mapStyle";
 	private static final String IS_FIRST_RUN = "isFirstRun";
 
 	GoogleMap mGoogleMap;
 	ArrayList<Location> mLocationsList;
 	Polyline mPolyline;
 
 	CheckBox mAsambleaCheckBox;
 	CheckBox mBravoCheckBox;
 	CheckBox mCuapCheckBox;
 	CheckBox mEmbarcacionCheckBox;
 	CheckBox mHospitalCheckBox;
 	CheckBox mPreventivoCheckBox;
 
 	View mMarkerPanel;
 
 	boolean isMarkerPanelShowing;
 
 	SharedPreferences prefs;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		handleIntent(getIntent());
 
 		if (prefs.getBoolean(IS_FIRST_RUN, true)) {
 			makeFirstRun();
 		}
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			setActionBar();
 		}
 
 		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
 				.findFragmentById(R.id.map);
 
 		if (savedInstanceState == null) {
 			mapFragment.setRetainInstance(true);
 			downloadData();
 		} else {
 			mGoogleMap = mapFragment.getMap();
 		}
 
 		setMap();
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		setIntent(intent);
 		handleIntent(intent);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			setSearchOptions(menu);
 		}
 		return true;
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (isMarkerPanelShowing) {
 			mMarkerPanel.setVisibility(View.GONE);
 			isMarkerPanelShowing = false;
 		} else {
 			super.onBackPressed();
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_locate:
 			moveToLocation();
 			return true;
 		case R.id.search:
 			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
 				onSearchRequested();
 			}
 			return true;
 		case android.R.id.home:
 		case R.id.menu_show_panel:
 			showMarkerPanel();
 			return true;
 		case R.id.menu_refresh:
 			downloadData();
 			return true;
 		case R.id.menu_show_hybrid:
 			return setMapStyle(MAP_STYLE_HYBRID);
 		case R.id.menu_show_normal:
 			return setMapStyle(MAP_STYLE_NORMAL);
 		case R.id.menu_show_satellite:
 			return setMapStyle(MAP_STYLE_SATELLITE);
 		case R.id.menu_show_terrain:
 			return setMapStyle(MAP_STYLE_TERRAIN);
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void makeFirstRun() {
 		prefs.edit().putBoolean(IS_FIRST_RUN, false).commit();
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setActionBar() {
 		ActionBar actionBar = getActionBar();
 		actionBar.setBackgroundDrawable(new ColorDrawable(Color
 				.parseColor("#CC0000")));
 		actionBar.setDisplayHomeAsUpEnabled(true);
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setSearchOptions(Menu menu) {
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		SearchView searchView = (SearchView) menu.findItem(R.id.search)
 				.getActionView();
 		searchView.setSearchableInfo(searchManager
 				.getSearchableInfo(getComponentName()));
 
 		searchView.setOnQueryTextListener(new QueryListener());
 	}
 
 	private void downloadData() {
 		if (isConnected()) {
 			getSupportLoaderManager().restartLoader(LOADER_CONNECTION, null,
 					this);
 		}
 	}
 
 	private boolean isConnected() {
 		ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 		if (manager.getActiveNetworkInfo() == null) {
 			return false;
 		}
 		return manager.getActiveNetworkInfo().isConnected();
 	}
 
 	private void setMap() {
 		if (mGoogleMap == null) {
 			mGoogleMap = ((SupportMapFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.map)).getMap();
 			if (mGoogleMap != null) {
 				setMapStyle(prefs.getInt(MAP_STYLE, MAP_STYLE_NORMAL));
 				mGoogleMap.setMyLocationEnabled(true);
 				mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
 				mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
 				mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
 				mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
 						new LatLng(41.3958, 2.1739), 12));
 			}
 		}
 
 	}
 
 	private boolean setMapStyle(int mapStyle) {
 		if (mGoogleMap == null) {
 			return false;
 		}
 
 		switch (mapStyle) {
 		case MAP_STYLE_NORMAL:
 			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
 			break;
 		case MAP_STYLE_HYBRID:
 			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 			break;
 		case MAP_STYLE_SATELLITE:
 			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
 			break;
 		case MAP_STYLE_TERRAIN:
 			mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
 			break;
 		default:
 			return false;
 		}
 		prefs.edit().putInt(MAP_STYLE, mapStyle).commit();
 		return true;
 	}
 
 	private void drawMarkers(String filter) {
 		if (mGoogleMap == null || mLocationsList == null) {
 			return;
 		}
 
 		mGoogleMap.clear();
 
 		if (mPolyline != null) {
 			drawLine(mPolyline.getPoints());
 		}
 
 		for (int i = 0; i < mLocationsList.size(); i++) {
 			if (!shouldShowMarker(mLocationsList.get(i), filter)) {
 				continue;
 			}
 
 			MarkerOptions marker = new MarkerOptions().position(mLocationsList
 					.get(i).getPosition());
 			if (mLocationsList.get(i).mIcono != 0) {
 				marker.icon(BitmapDescriptorFactory.fromResource(mLocationsList
 						.get(i).mIcono));
 			}
 			if (mLocationsList.get(i).mContenido.mNombre != null) {
 				marker.title(mLocationsList.get(i).mContenido.mNombre);
 			}
 			if (mLocationsList.get(i).mContenido.mSnippet != null) {
 				marker.snippet(mLocationsList.get(i).mContenido.mSnippet);
 			}
 
 			mGoogleMap.addMarker(marker);
 		}
 		mGoogleMap.setInfoWindowAdapter(new MarkerAdapter());
 		mGoogleMap.setOnInfoWindowClickListener(this);
 	}
 
 	private void drawLine(Collection<LatLng> points) {
 		if (mPolyline != null) {
 			mPolyline.remove();
 		}
 		if (mGoogleMap == null || points == null) {
 			return;
 		}
 		if (points.size() == 0) {
 			Toast.makeText(getApplicationContext(), R.string.limit_reached,
 					Toast.LENGTH_LONG).show();
 		}
 
 		mPolyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(points)
 				.color(Color.parseColor("#3399FF")));
 	}
 
 	private boolean shouldShowMarker(Location location, String filter) {
 		boolean show = true;
 		if (!matchFilter(location, filter)) {
 			return false;
 		}
 		switch (location.mIcono) {
 		case R.drawable.asamblea:
 			show = prefs.getBoolean(SHOW_ASAMBLEA, true);
 			break;
 		case R.drawable.bravo:
 			show = prefs.getBoolean(SHOW_BRAVO, true);
 			break;
 		case R.drawable.cuap:
 			show = prefs.getBoolean(SHOW_CUAP, true);
 			break;
 		case R.drawable.embarcacion:
 			show = prefs.getBoolean(SHOW_EMBARCACION, true);
 			break;
 		case R.drawable.hospital:
 			show = prefs.getBoolean(SHOW_HOSPITAL, true);
 			break;
 		case R.drawable.preventivo:
 			show = prefs.getBoolean(SHOW_PREVENTIVO, true);
 			break;
 		default:
 			return true;
 		}
 		return show;
 	}
 
 	private boolean matchFilter(Location location, String filter) {
 		if (filter != null) {
 			filter = dehyphenize(filter);
 			String nombre = dehyphenize(location.mContenido.mNombre);
 			String lugar = null;
 			if (location.mContenido.mLugar != null) {
 				lugar = dehyphenize(location.mContenido.mLugar);
 			}
 			if (!nombre.contains(filter)) {
 				if (lugar == null || !lugar.contains(filter)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	@SuppressLint("DefaultLocale")
 	private String dehyphenize(String input) {
 		input = input.toLowerCase();
 		return input.replace("à", "a").replace("á", "a").replace("é", "e")
 				.replace("è", "e").replace("í", "i").replace("ì", "i")
 				.replace("ó", "o").replace("ò", "o").replace("ú", "u")
 				.replace("ù", "u");
 	}
 
 	private void handleIntent(Intent intent) {
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			String query = intent.getStringExtra(SearchManager.QUERY);
 			if (!TextUtils.isEmpty(query)) {
 				drawMarkers(query);
 			}
 		}
 	}
 
 	private void moveToLocation() {
 		if (mGoogleMap != null) {
 			if (mGoogleMap.getMyLocation() != null) {
 				mGoogleMap.animateCamera(CameraUpdateFactory
 						.newLatLng(new LatLng(mGoogleMap.getMyLocation()
 								.getLatitude(), mGoogleMap.getMyLocation()
 								.getLongitude())));
 			} else {
 				LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 				if (manager.getProviders(true).size() > 1) {
 					Toast.makeText(this, R.string.locating, Toast.LENGTH_SHORT)
 							.show();
 				} else {
 					locationUnavailable();
 				}
 			}
 		}
 	}
 
 	private void showMarkerPanel() {
 		if (isMarkerPanelShowing) {
 			mMarkerPanel.setVisibility(View.GONE);
 			isMarkerPanelShowing = false;
 		} else {
 			prepareMarkerPanel();
 			isMarkerPanelShowing = true;
 			mMarkerPanel.setVisibility(View.VISIBLE);
 		}
 
 	}
 
 	private void prepareMarkerPanel() {
 		if (mMarkerPanel == null) {
 			mMarkerPanel = findViewById(R.id.marker_panel);
 			mAsambleaCheckBox = (CheckBox) findViewById(R.id.checkbox_asamblea);
 			mBravoCheckBox = (CheckBox) findViewById(R.id.checkbox_bravo);
 			mCuapCheckBox = (CheckBox) findViewById(R.id.checkbox_cuap);
 			mEmbarcacionCheckBox = (CheckBox) findViewById(R.id.checkbox_embarcacion);
 			mHospitalCheckBox = (CheckBox) findViewById(R.id.checkbox_hospital);
 			mPreventivoCheckBox = (CheckBox) findViewById(R.id.checkbox_preventivo);
 
 			mAsambleaCheckBox.setChecked(prefs.getBoolean(SHOW_ASAMBLEA, true));
 			mBravoCheckBox.setChecked(prefs.getBoolean(SHOW_BRAVO, true));
 			mCuapCheckBox.setChecked(prefs.getBoolean(SHOW_CUAP, true));
 			mEmbarcacionCheckBox.setChecked(prefs.getBoolean(SHOW_EMBARCACION,
 					true));
 			mHospitalCheckBox.setChecked(prefs.getBoolean(SHOW_HOSPITAL, true));
 			mPreventivoCheckBox.setChecked(prefs.getBoolean(SHOW_PREVENTIVO,
 					true));
 
 			mAsambleaCheckBox.setOnCheckedChangeListener(this);
 			mBravoCheckBox.setOnCheckedChangeListener(this);
 			mCuapCheckBox.setOnCheckedChangeListener(this);
 			mEmbarcacionCheckBox.setOnCheckedChangeListener(this);
 			mHospitalCheckBox.setOnCheckedChangeListener(this);
 			mPreventivoCheckBox.setOnCheckedChangeListener(this);
 		}
 	}
 
 	private void locationUnavailable() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.location_disabled_title);
 		builder.setMessage(R.string.location_disabled_message);
 		builder.setPositiveButton(R.string.open_location_settings,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialogInterface, int i) {
 						startActivity(new Intent(
 								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 					}
 				});
 		builder.setNegativeButton(R.string.cancel, null);
 		builder.create().show();
 		return;
 	}
 
 	@Override
 	public Loader<ArrayList<Location>> onCreateLoader(int id, Bundle args) {
 		Loader<ArrayList<Location>> loader = null;
 		switch (id) {
 		case LOADER_CONNECTION:
 			loader = new ConnectionLoader(this, args);
 			break;
 		}
 		return loader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<ArrayList<Location>> loader,
 			ArrayList<Location> locations) {
 		switch (loader.getId()) {
 		case LOADER_CONNECTION:
 			if (locations == null) {
 				return;
 			}
 			mLocationsList = locations;
 			drawMarkers(null);
 			break;
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<ArrayList<Location>> loader) {
 	}
 
 	@Override
 	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 		SharedPreferences.Editor editor = prefs.edit();
 		switch (buttonView.getId()) {
 		case R.id.checkbox_asamblea:
 			editor.putBoolean(SHOW_ASAMBLEA, isChecked);
 			break;
 		case R.id.checkbox_bravo:
 			editor.putBoolean(SHOW_BRAVO, isChecked);
 			break;
 		case R.id.checkbox_cuap:
 			editor.putBoolean(SHOW_CUAP, isChecked);
 			break;
 		case R.id.checkbox_embarcacion:
 			editor.putBoolean(SHOW_EMBARCACION, isChecked);
 			break;
 		case R.id.checkbox_hospital:
 			editor.putBoolean(SHOW_HOSPITAL, isChecked);
 			break;
 		case R.id.checkbox_preventivo:
 			editor.putBoolean(SHOW_PREVENTIVO, isChecked);
 			break;
 		}
 		editor.commit();
 		drawMarkers(null);
 	}
 
 	@Override
 	public void onInfoWindowClick(Marker marker) {
 		if (marker.getPosition() == null || mGoogleMap.getMyLocation() == null) {
 			Toast.makeText(getApplicationContext(), R.string.locating,
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		// Intent navigation = new Intent(Intent.ACTION_VIEW,
 		// Uri.parse("http://maps.google.com/maps?saddr="
 		// + mGoogleMap.getMyLocation().getLatitude() + ","
 		// + mGoogleMap.getMyLocation().getLongitude() + "&daddr="
 		// + marker.getPosition().latitude + ","
 		// + marker.getPosition().longitude));
 		// startActivity(navigation);
 		Bundle args = new Bundle();
 		args.putDouble(DirectionsLoader.ARG_ORIGIN_LAT, mGoogleMap
 				.getMyLocation().getLatitude());
 		args.putDouble(DirectionsLoader.ARG_ORIGIN_LNG, mGoogleMap
 				.getMyLocation().getLongitude());
 		args.putDouble(DirectionsLoader.ARG_DESTINATION_LAT,
 				marker.getPosition().latitude);
 		args.putDouble(DirectionsLoader.ARG_DESTINATION_LNG,
 				marker.getPosition().longitude);
 		getSupportLoaderManager().restartLoader(LOADER_DIRECTIONS, args,
 				new DirectionsLoaderHelper());
 	}
 
 	private class MarkerAdapter implements InfoWindowAdapter {
 		@Override
 		public View getInfoWindow(Marker marker) {
 			return null;
 		}
 
 		@Override
 		public View getInfoContents(Marker marker) {
 			View v = getLayoutInflater().inflate(R.layout.map_marker, null);
 
 			((TextView) v.findViewById(R.id.location)).setText(marker
 					.getTitle());
 
 			if (marker.getSnippet() != null) {
 				String address = marker.getSnippet().substring(0,
 						marker.getSnippet().indexOf(Location.MARKER_NEW_LINE));
 				String other = marker.getSnippet().substring(
 						marker.getSnippet().indexOf(Location.MARKER_NEW_LINE)
 								+ Location.MARKER_NEW_LINE.length(),
 						marker.getSnippet().length());
 				((TextView) v.findViewById(R.id.address)).setText(address);
 				((TextView) v.findViewById(R.id.other_information))
 						.setText(other);
 			}
 
 			return v;
 		}
 	}
 
 	private class QueryListener implements OnQueryTextListener {
 		@Override
 		public boolean onQueryTextChange(String newText) {
 			drawMarkers(newText);
 			return false;
 		}
 
 		@Override
 		public boolean onQueryTextSubmit(String query) {
 			drawMarkers(query);
 			return false;
 		}
 	}
 
 	private class DirectionsLoaderHelper implements LoaderCallbacks<String> {
 
 		@Override
 		public Loader<String> onCreateLoader(int id, Bundle args) {
 			if (id == LOADER_DIRECTIONS) {
 				return new DirectionsLoader(getApplicationContext(), args);
 			} else {
 				return null;
 			}
 		}
 
 		@Override
 		public void onLoadFinished(Loader<String> loader, String response) {
 			if (loader.getId() == LOADER_DIRECTIONS) {
 				drawLine(JSONParser.getPoints(response));
 			}
 		}
 
 		@Override
 		public void onLoaderReset(Loader<String> loader) {
 
 		}
 	}
 }
