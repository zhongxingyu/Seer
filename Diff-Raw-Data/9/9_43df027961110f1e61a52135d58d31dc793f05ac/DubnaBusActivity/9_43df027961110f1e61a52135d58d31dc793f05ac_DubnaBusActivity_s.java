 package ru.ratadubna.dubnabus;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 public class DubnaBusActivity extends SherlockFragmentActivity implements
 		OnMarkerClickListener, OnCameraChangeListener,
 		OnInfoWindowClickListener {
 	private GoogleMap mMap;
 	private ModelFragment model = null;
 	private BusStopObserverDialogFragment dialog = null;
 	static final String MODEL = "model", DIALOG = "dialog";
 	private final String trainsDMurl = "http://m.rasp.yandex.ru/search?toName=&fromName=&search_type=suburban&fromId=c215",
 			trainsMDurl = "http://m.rasp.yandex.ru/search?toName=&toId=c215&fromName=&search_type=suburban",
 			busesDMurl = "http://m.rasp.yandex.ru/search?toName=&fromName=&search_type=bus&fromId=c215",
 			busesMDurl = "http://m.rasp.yandex.ru/search?toName=&toId=c215&fromName=&search_type=bus",
 			taxiurl = "http://www.dubna.ru/143";
 	public static boolean reloadOverlays = false;
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		setUpMapIfNeeded();
 		model.startLoadingBusLocations();
 	}
 
 	@Override
 	public void onPause() {
 		model.stopLoadingBusLocations();
 		super.onPause();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTheme(R.style.Theme_Sherlock_Light);
 		if ((model = (ModelFragment) getSupportFragmentManager()
 				.findFragmentByTag(MODEL)) == null) {
 			model = new ModelFragment();
 			getSupportFragmentManager().beginTransaction().add(model, MODEL)
 					.commit();
 		}
 		ModelFragment.setCtxt(this);
 		setContentView(R.layout.main);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		new MenuInflater(this).inflate(R.menu.main, menu);
 		return (super.onCreateOptionsMenu(menu));
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.route_refresh:
 			model.stopLoadingBusLocations();
 			Bus.clearList();
 			DubnaBusActivity.reloadOverlays = true;
 			setUpMapIfNeeded();
 			return (true);
 		case R.id.route_selection:
 			Intent i = new Intent(this, MenuActivity.class);
 			startActivity(i);
 			return (true);
 		case R.id.taxi:
 			model.loadTaxiPage(taxiurl);
 			return (true);
 		case R.id.trainsDM:
 			i = new Intent(this, SimpleContentActivity.class);
 			i.putExtra(SimpleContentActivity.EXTRA_DATA, trainsDMurl);
 			startActivity(i);
 			return (true);
 		case R.id.trainsMD:
 			i = new Intent(this, SimpleContentActivity.class);
 			i.putExtra(SimpleContentActivity.EXTRA_DATA, trainsMDurl);
 			startActivity(i);
 			return (true);
 		case R.id.busesDM:
 			i = new Intent(this, SimpleContentActivity.class);
 			i.putExtra(SimpleContentActivity.EXTRA_DATA, busesDMurl);
 			startActivity(i);
 			return (true);
 		case R.id.busesMD:
 			i = new Intent(this, SimpleContentActivity.class);
 			i.putExtra(SimpleContentActivity.EXTRA_DATA, busesMDurl);
 			startActivity(i);
 			return (true);
 		}
 		return (super.onOptionsItemSelected(item));
 	}
 
 	private void setUpMapIfNeeded() {
 		if (mMap == null) {
 			SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.map);
 			mapFrag.setRetainInstance(true);
 			mMap = mapFrag.getMap();
 			if (mMap == null) {
 				if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != 0)
 					Toast.makeText(this, getString(R.string.playProblems),
 							Toast.LENGTH_LONG).show();
 				else
 					Toast.makeText(this, getString(R.string.mapProblems),
 							Toast.LENGTH_LONG).show();
 			} else {
 				if (!model.isMapLoaded()) {
 					reloadOverlays = true; // to redraw buses in case of
 											// activity
 											// reopen
 					// (buses instances are in memory, mMap is
 					// null)
 					mMap.setInfoWindowAdapter(new SnippetAdapter(
 							getLayoutInflater()));
 					mMap.setOnMarkerClickListener(this);
 					mMap.setOnCameraChangeListener(this);
 					mMap.setOnInfoWindowClickListener(this);
 					// mMap.setMyLocationEnabled(true); until Google doesn't fix
 					// the
 					// bug
 					// http://stackoverflow.com/questions/13756261/how-to-get-the-current-lcoation-in-google-maps-android-api-v2
 					model.loadMapRoutes();
 				}
 			}
 		} else if (reloadOverlays) {
 			mMap.clear();
 			model.loadMapRoutes();
 		}
 	}
 
 	@Override
 	public boolean onMarkerClick(Marker marker) {
 		if (marker.getTitle().matches("(\\d{1,3})")) {
 			setBusSnippet(marker);
 		} else {
 			model.processMarker(marker);
 		}
 		return false;
 	}
 
 	void setBusSnippet(Marker marker) {
 		Bus bus = Bus.getBusByMarker(marker);
 		marker.setSnippet("<img src=\"file:///android_res/drawable/"
 				+ bus.getPic() + "\">" + "<br />"
 				+ String.valueOf(bus.getSpeed()) + getString(R.string.kmph)
 				+ "<br />" + String.valueOf(bus.getTime()));
 		marker.showInfoWindow();
 	}
 
 	@Override
 	public void onInfoWindowClick(Marker marker) {
 		if (!marker.getTitle().matches("(\\d{1,3})")
 				&& ((model.lastBusSchedule != null) && !model.lastBusSchedule
 						.isEmpty())) {
 			if ((dialog = (BusStopObserverDialogFragment) getSupportFragmentManager()
 					.findFragmentByTag(DIALOG)) == null) {
 				dialog = BusStopObserverDialogFragment.newInstance(marker
 						.getTitle());
 				((DubnaBusActivity) ModelFragment.getCtxt())
 						.getSupportFragmentManager().beginTransaction()
 						.add(dialog, DIALOG).commit();
 			}
 		}
 	}
 
 	ModelFragment getModel() {
 		return model;
 	}
 
 	void addRoute(PolylineOptions mapRoute) {
 		mMap.addPolyline(mapRoute);
 	}
 
 	Marker addMarker(MarkerOptions maropt) {
 		return mMap.addMarker(maropt);
 	}
 
 	void addBuses() {
 		for (Bus bus : Bus.getList()) {
 			if (bus.isActive() && !reloadOverlays) {
 				bus.updateOverlay();
 				bus.updateMarker();
 			} else {
 				bus.setOverlay(mMap.addGroundOverlay(bus
 						.getGroundOverlayOptions()));
 				Marker marker = mMap.addMarker(bus.getMarkerOptions());
				marker.setVisible(false);
 				bus.setMarker(marker);
 			}
 		}
 		reloadOverlays = false;
 		model.continueLoadingBusLocations();
 	}
 
 	@Override
 	public void onCameraChange(CameraPosition position) {
 		Bus.redrawOnZoomChange(mMap.getProjection());
 	}
 
 }
