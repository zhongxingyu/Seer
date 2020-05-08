 package com.onelightstudio.velibnroses;
 
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentActivity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient;
 import com.google.android.gms.location.LocationClient;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.LatLngBounds;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 import com.onelightstudio.velibnroses.model.Station;
 import com.onelightstudio.velibnroses.model.StationMarker;
 import com.onelightstudio.velibnroses.ws.WSDefaultHandler;
 import com.onelightstudio.velibnroses.ws.WSRequest;
 import com.onelightstudio.velibnroses.ws.WSSilentHandler;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 
 public class MainActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, View.OnClickListener {
 
     // INNER CLASSES AND PRIVATE MEMBERS
 
     class GetAddressTask extends AsyncTask<Location, Void, String> {
 
         private AutoCompleteTextView field;
         private ImageButton locationButton;
         private ProgressBar locationProgress;
         private Location location;
 
         public GetAddressTask(int fieldId) {
             switch (fieldId) {
                 case FIELD_DEPARTURE:
                     field = departureField;
                     locationButton = departureLocationButton;
                     locationProgress = departureLocationProgress;
                     break;
                 case FIELD_ARRIVAL:
                     field = arrivalField;
                     locationButton = arrivalLocationButton;
                     locationProgress = arrivalLocationProgress;
                     break;
             }
         }
 
         @Override
         protected String doInBackground(Location... params) {
             location = params[0];
 
             WSRequest request = new WSRequest(MainActivity.this, Constants.GOOGLE_API_GEOCODE_URL);
             request.withParam(Constants.GOOGLE_API_LATLNG, location.getLatitude() + "," + location.getLongitude());
             request.withParam(Constants.GOOGLE_API_SENSOR, "true");
             request.handleWith(new WSSilentHandler() {
                 @Override
                 public void onResult(Context context, JSONObject result) {
                     locationButton.setVisibility(View.VISIBLE);
                     locationProgress.setVisibility(View.GONE);
 
                     JSONArray addresses = (JSONArray) result.opt("results");
                     if (addresses.length() > 0) {
                         JSONObject address = (JSONObject) addresses.opt(0);
                         field.setText(address.opt("formatted_address").toString());
                     } else {
                         Toast.makeText(MainActivity.this, R.string.address_not_found, Toast.LENGTH_LONG).show();
                     }
                 }
             });
             request.call();
 
             return "";
         }
 
         @Override
         protected void onPostExecute(String result) {
             if (result == null) {
                 locationButton.setVisibility(View.VISIBLE);
                 locationProgress.setVisibility(View.GONE);
                 Toast.makeText(MainActivity.this, R.string.address_not_found, Toast.LENGTH_LONG).show();
             }
         }
 
         @Override
         protected void onPreExecute() {
             locationButton.setVisibility(View.GONE);
             locationProgress.setVisibility(View.VISIBLE);
             field.setAdapter((ArrayAdapter<String>) null);
         }
     }
 
     private static final int FIELD_DEPARTURE = 0;
     private static final int FIELD_ARRIVAL = 1;
     private final static String FORCE_CAMERA_POSITION = "ForceCameraPosition";
 
     private GoogleMap map;
     private boolean forceCameraPosition;
     private LocationClient locationClient;
     private ArrayList<Station> stations;
     private boolean loadingStations;
     private Handler timer;
     private Runnable timeRunnable;
     private Long pausedTime;
     private HashMap<Marker, LatLngBounds> clusterBounds;
     private boolean searchMode;
 
     private int loadStationCount = 0;
     private View searchView;
     private View mapView;
     private boolean searchViewVisible;
     private AutoCompleteTextView departureField;
     private ImageButton departureLocationButton;
     private ProgressBar departureLocationProgress;
     private EditText departureBikesField;
     private AutoCompleteTextView arrivalField;
     private ImageButton arrivalLocationButton;
     private ProgressBar arrivalLocationProgress;
     private EditText arrivalStandsField;
     private LatLng departureLocation;
     private LatLng arrivalLocation;
     private ArrayList<Station> searchMapDepartureStations;
     private ArrayList<Station> searchMapArrivalStations;
     private Station searchMapDepartureStation;
     private Station searchMapArrivalStation;
     private boolean searchMapMarkersAdded;
     private Polyline searchMapPolyline;
     private MenuItem actionSearchMenuItem;
     private MenuItem actionClearSearchMenuItem;
     private Marker departureMarker;
     private Marker arrivalMarker;
     private AsyncTask displayStationTask;
     private ArrayList<Marker> defaultMapStations;
 
     // ACTIVITY LIFECYCLE
 
     @Override
     public void onAttachedToWindow() {
         super.onAttachedToWindow();
         setUpMapIfNeeded();
     }
 
     @Override
     protected void onCreate(Bundle pSavedInstanceState) {
         super.onCreate(pSavedInstanceState);
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         setContentView(R.layout.activity_main);
         getActionBar().setDisplayShowTitleEnabled(false);
 
         stations = new ArrayList<Station>();
 
         //Start timer
         timeRunnable = new Runnable() {
 
             @Override
             public void run() {
                 Log.d("Refresh Map Tick");
                 if (pausedTime == null) {
                     loadStations();
                     timer.postDelayed(this, Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES);
                 }
             }
         };
         timer = new Handler();
         timer.post(timeRunnable);
 
         //Init view and elements
         searchView = findViewById(R.id.search_view);
         mapView = findViewById(R.id.map_view);
         departureField = (AutoCompleteTextView) findViewById(R.id.departure_field);
         departureLocationButton = (ImageButton) findViewById(R.id.departure_mylocation_button);
         departureLocationProgress = (ProgressBar) findViewById(R.id.departure_mylocation_progress);
         departureBikesField = (EditText) findViewById(R.id.departure_bikes);
         arrivalField = (AutoCompleteTextView) findViewById(R.id.arrival_field);
         arrivalLocationButton = (ImageButton) findViewById(R.id.arrival_mylocation_button);
         arrivalLocationProgress = (ProgressBar) findViewById(R.id.arrival_mylocation_progress);
         arrivalStandsField = (EditText) findViewById(R.id.arrival_stands);
 
         departureField.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 //Nothing
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 //Nothing
             }
 
             @Override
             public void afterTextChanged(Editable editable) {
                 if (departureField.getAdapter() == null) {
                     departureField.setAdapter(new AddressAdapter(MainActivity.this, R.layout.list_item));
                 }
             }
         });
 
         departureField.setOnKeyListener(new View.OnKeyListener() {
 
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 // If the event is a key-down event on the "enter" button
                 if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                         (keyCode == KeyEvent.KEYCODE_ENTER)) {
                     // Perform action on Enter key press
                     departureField.clearFocus();
                     arrivalField.requestFocus();
                     return true;
                 }
                 return false;
             }
         });
 
 
         departureBikesField.setOnKeyListener(new View.OnKeyListener() {
 
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 // If the event is a key-down event on the "enter" button
                 if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                         (keyCode == KeyEvent.KEYCODE_ENTER)) {
                     // Perform action on Enter key press
                     departureBikesField.clearFocus();
                     arrivalStandsField.requestFocus();
                     return true;
                 }
                 return false;
             }
         });
 
         arrivalField.setOnKeyListener(new View.OnKeyListener() {
 
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 // If the event is a key-down event on the "enter" button
                 if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                         (keyCode == KeyEvent.KEYCODE_ENTER)) {
                     // Perform action on Enter key press
                     startSearch();
                     return true;
                 }
                 return false;
             }
         });
 
         arrivalStandsField.setOnKeyListener(new View.OnKeyListener() {
 
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 // If the event is a key-down event on the "enter" button
                 if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                         (keyCode == KeyEvent.KEYCODE_ENTER)) {
                     // Perform action on Enter key press
                     startSearch();
                     return true;
                 }
                 return false;
             }
         });
 
         arrivalField.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 //Nothing
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 //Nothing
             }
 
             @Override
             public void afterTextChanged(Editable editable) {
                 if (arrivalField.getAdapter() == null) {
                     arrivalField.setAdapter(new AddressAdapter(MainActivity.this, R.layout.list_item));
                 }
             }
         });
 
         departureBikesField.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 //Nothing
             }
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                 //Nothing
             }
 
             @Override
             public void afterTextChanged(Editable editable) {
                 arrivalStandsField.setText(editable.toString());
             }
         });
 
         //Default map markers
         defaultMapStations = new ArrayList<Marker>();
 
         //Station request
         loadingStations = false;
 
         if (pSavedInstanceState != null) {
             forceCameraPosition = pSavedInstanceState.getBoolean(FORCE_CAMERA_POSITION);
         } else {
             forceCameraPosition = true;
         }
 
         locationClient = new LocationClient(this, this, this);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         if (!locationClient.isConnected()) {
             locationClient.connect();
         }
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         pausedTime = System.currentTimeMillis();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (pausedTime != null) {
 
             if ((System.currentTimeMillis() - pausedTime) > Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES) {
                 //Too much time has passed, a refresh is needed
                 loadStations();
                 timer.postDelayed(timeRunnable, Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES);
             } else {
                 timer.postDelayed(timeRunnable, Constants.MAP_TIMER_REFRESH_IN_MILLISECONDES - (System.currentTimeMillis() - pausedTime));
             }
 
             pausedTime = null;
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle pOutState) {
         super.onSaveInstanceState(pOutState);
         pOutState.putBoolean(FORCE_CAMERA_POSITION, false);
     }
 
     // LISTENER CALLBACKS
 
     @Override
     public void onConnected(Bundle bundle) {
        if (forceCameraPosition == true) {
             Location userLocation = locationClient.getLastLocation();
            if (userLocation != null && map != null) {
                 map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), Constants.MAP_DEFAULT_USER_ZOOM), Constants.MAP_ANIMATE_TIME, null);
             } else {
                 map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Constants.TLS_LAT, Constants.TLS_LNG), Constants.MAP_DEFAULT_USER_ZOOM), Constants.MAP_ANIMATE_TIME, null);
                 Toast.makeText(this, R.string.location_not_shared, Toast.LENGTH_LONG).show();
             }
         }
     }
 
     @Override
     public void onConnectionFailed(ConnectionResult connectionResult) {
         //Nothing
     }
 
     @Override
     public void onDisconnected() {
         //Nothing
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.map, menu);
         this.actionSearchMenuItem = menu.findItem(R.id.action_search);
         this.actionClearSearchMenuItem = menu.findItem(R.id.action_clear_search);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_search:
                 toggleSearchViewVisible();
                 return true;
             case R.id.action_info:
                 startActivity(new Intent(this, InfoActivity.class));
                 return true;
             case R.id.action_clear_search:
                 clearSearch();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onClick(View view) {
         switch (view.getId()) {
             case R.id.departure_mylocation_button:
                 fillAddressFieldWithCurrentLocation(FIELD_DEPARTURE);
                 break;
             case R.id.arrival_mylocation_button:
                 fillAddressFieldWithCurrentLocation(FIELD_ARRIVAL);
                 break;
             case R.id.search_button:
                 startSearch();
                 break;
             case R.id.hide_search_view_button:
                 hideSearchForm();
                 break;
         }
     }
 
     @Override
     public void onBackPressed() {
         if (searchViewVisible) {
             hideSearchForm();
         } else {
             super.onBackPressed();
         }
     }
 
     // PRIVATE METHODS
 
     private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (map == null) {
             map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
             // Check if we were successful in obtaining the map.
             if (map != null) {
                 map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                     @Override
                     public void onCameraChange(CameraPosition cameraPosition) {
                         displayStations();
                     }
                 });
                 map.setMyLocationEnabled(true);
                 if (forceCameraPosition) {
                     map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(Constants.MAP_DEFAULT_LAT), Constants.MAP_DEFAULT_LNG), Constants.MAP_DEFAULT_ZOOM));
                 }
                 map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                     @Override
                     public boolean onMarkerClick(Marker marker) {
                         if (marker.equals(departureMarker) || marker.equals(arrivalMarker)) {
                             return false;
                         }
                         if (searchMode) {
                             for (Station station : searchMapDepartureStations) {
                                 if (station.searchMarker.equals(marker)) {
                                     searchMapDepartureStation = station;
                                     break;
                                 }
                             }
                             for (Station station : searchMapArrivalStations) {
                                 if (station.searchMarker.equals(marker)) {
                                     searchMapArrivalStation = station;
                                     break;
                                 }
                             }
                             displaySearchResult();
                             return true;
                         } else {
                             LatLngBounds bounds = clusterBounds.get(marker);
                             if (bounds != null) {
                                 map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, getResources().getDimensionPixelSize(R.dimen.padding_zoom_cluster)));
                                 return true;
                             }
                         }
 
                         return false;
                     }
                 });
             } else {
                 //Tell the user to check its google play services
                 Toast.makeText(this, R.string.error_google_play_service, Toast.LENGTH_LONG).show();
             }
         }
     }
 
     private void loadStations() {
 
         loadStationCount++;
 
         Log.d("loadStations " + loadingStations + " retry: " + loadStationCount);
         if (!loadingStations) {
             loadingStations = true;
             Log.d("Call stations WS");
             WSRequest request = new WSRequest(this, Constants.JCD_URL);
             request.withParam(Constants.JCD_API_KEY, ((App) getApplication()).getApiKey(Constants.JCD_APP_API_KEY));
 
             boolean tmp = true;
             synchronized (stations) {
                 tmp = !stations.isEmpty();
             }
 
             final boolean executeInBackground = tmp;
 
             if (executeInBackground) {
                 request.handleWith(new WSSilentHandler() {
                     @Override
                     public void onResult(Context context, JSONObject result) {
                         loadStationCount = 0;
                         parseJSONResult(result, executeInBackground);
                     }
 
                     @Override
                     public void onException(Context context, Exception e) {
                         super.onException(context, e);
                         loadingStations = false;
                         if (loadStationCount < 3) {
                             loadStations();
                         } else {
                             loadStationCount = 0;
                         }
                     }
 
                     @Override
                     public void onError(Context context, int errorCode) {
                         super.onError(context, errorCode);
                         loadingStations = false;
                         if (loadStationCount < 3) {
                             loadStations();
                         } else {
                             loadStationCount = 0;
                         }
                     }
                 });
             } else {
                 request.handleWith(new WSDefaultHandler() {
                     @Override
                     public void onResult(Context context, JSONObject result) {
                         loadStationCount = 0;
                         parseJSONResult(result, executeInBackground);
                     }
 
                     @Override
                     public void onException(Context context, Exception e) {
                         loadingStations = false;
                         if (loadStationCount < 3) {
                             loadStations();
                         } else {
                             if (!Util.isOnline(MainActivity.this)) {
                                 Toast.makeText(MainActivity.this, R.string.internet_not_available, Toast.LENGTH_LONG).show();
                             } else {
                                 Toast.makeText(MainActivity.this, R.string.ws_stations_not_availabel, Toast.LENGTH_LONG).show();
                             }
                             loadStationCount = 0;
                         }
                     }
 
                     @Override
                     public void onError(Context context, int errorCode) {
                         loadingStations = false;
                         if (loadStationCount < 3) {
                             loadStations();
                         } else {
                             if (!Util.isOnline(MainActivity.this)) {
                                 Toast.makeText(MainActivity.this, R.string.internet_not_available, Toast.LENGTH_LONG).show();
                             } else {
                                 Toast.makeText(MainActivity.this, R.string.ws_stations_not_availabel, Toast.LENGTH_LONG).show();
                             }
                             loadStationCount = 0;
                         }
                     }
                 });
             }
             request.call();
         }
     }
 
     private void parseJSONResult(final JSONObject result, final boolean inBackground) {
         new AsyncTask<Void, Void, Void>() {
             @Override
             protected synchronized Void doInBackground(Void... voids) {
                 if (result != null) {
                     JSONArray stationsJSON = (JSONArray) result.opt("list");
                     Log.i("Stations received : " + stationsJSON.length() + " stations");
                     synchronized (stations) {
                         stations.clear();
                         for (int i = 0; i < stationsJSON.length(); i++) {
                             Station station = new Station(stationsJSON.optJSONObject(i));
                             stations.add(station);
                         }
                     }
                 }
                 return null;
             }
 
             @Override
             protected void onPreExecute() {
                 if (!inBackground) {
                     setProgressBarIndeterminateVisibility(true);
                 }
             }
 
             @Override
             protected void onPostExecute(Void aVoid) {
                 if (!inBackground) {
                     setProgressBarIndeterminateVisibility(false);
                 }
                 displayStations();
                 loadingStations = false;
             }
 
             @Override
             protected void onCancelled() {
                 loadingStations = false;
             }
 
             @Override
             protected void onCancelled(Void aVoid) {
                 loadingStations = false;
             }
 
 
         }.execute();
     }
 
     private void displayStations() {
         if (!searchMode) {
 
             if (displayStationTask != null && displayStationTask.getStatus() != AsyncTask.Status.FINISHED) {
                 Log.d("Cancel previous displayStationTask");
                 displayStationTask.cancel(true);
             }
 
             displayStationTask = new AsyncTask<Void, Void, HashMap<MarkerOptions, LatLngBounds>>() {
 
                 private LatLngBounds bounds;
                 private float zoomLevel;
                 private float maxZoomLevel;
 
                 @Override
                 protected void onPreExecute() {
                     bounds = map.getProjection().getVisibleRegion().latLngBounds;
                     zoomLevel = map.getCameraPosition().zoom;
                     maxZoomLevel = map.getMaxZoomLevel();
                 }
 
                 @Override
                 protected HashMap<MarkerOptions, LatLngBounds> doInBackground(Void... voids) {
                     // keep the stations in the viewport only
                     ArrayList<Station> stationsInViewport = new ArrayList<Station>();
                     synchronized (stations) {
                         for (Station station : stations) {
                             if (bounds.contains(station.latLng)) {
                                 stationsInViewport.add(station);
                             }
                         }
                     }
                     // create the markers, clustering if needed
                     clusterBounds = new HashMap<Marker, LatLngBounds>();
                     HashMap<MarkerOptions, LatLngBounds> markers = new HashMap<MarkerOptions, LatLngBounds>();
                     ArrayList<Station> unprocessedStations = (ArrayList<Station>) stationsInViewport.clone();
                     for (Station station : stationsInViewport) {
                         if (unprocessedStations.contains(station)) {
                             unprocessedStations.remove(station);
                             LatLngBounds.Builder clusterBoundsBuilder = new LatLngBounds.Builder().include(station.latLng);
                             int n = 1;
                             for (Iterator<Station> otherIt = unprocessedStations.iterator(); otherIt.hasNext(); ) {
                                 Station otherStation = otherIt.next();
                                 // http://gis.stackexchange.com/questions/7430/google-maps-zoom-level-ratio
                                 int maxDistance = (int) (Math.pow(2, maxZoomLevel - zoomLevel) * 2.5);
                                 if (getDistanceInMeters(station.latLng, otherStation.latLng) < maxDistance) {
                                     clusterBoundsBuilder.include(otherStation.latLng);
                                     otherIt.remove();
                                     n++;
                                 }
                             }
                             if (n > 1) {
                                 LatLngBounds bounds = clusterBoundsBuilder.build();
                                 markers.put(StationMarker.createCluster(bounds), bounds);
                             } else {
                                 markers.put(StationMarker.createMarker(MainActivity.this, station), null);
                             }
                         }
                     }
                     return markers;
                 }
 
                 protected void onPostExecute(HashMap<MarkerOptions, LatLngBounds> markers) {
                     ArrayList<Marker> tmpAddedMarkers = new ArrayList<Marker>();
 
                     for (Map.Entry<MarkerOptions, LatLngBounds> markerEntry : markers.entrySet()) {
                         Marker marker = map.addMarker(markerEntry.getKey());
                         tmpAddedMarkers.add(marker);
 
                         if (markerEntry.getValue() != null) {
                             clusterBounds.put(marker, markerEntry.getValue());
                         }
                     }
 
                     for (Marker stationMarker : defaultMapStations) {
                         stationMarker.remove();
                     }
 
                     defaultMapStations = tmpAddedMarkers;
                 }
             }.execute();
         } else {
 
             Log.d("Update the displayed markers");
 
             synchronized (stations) {
                 for (Station updatedStation : stations) {
                     for (Station station : searchMapDepartureStations) {
                         if (station.lng == updatedStation.lng && station.lat == updatedStation.lat) {
                             // If there is not enough bikes, do not display it anymore
                             if (updatedStation.availableBikes < Integer.valueOf(departureBikesField.getText().toString())) {
                                 findAndDisplaySearchStations();
                                 return;
                             } else {
                                 searchMapDepartureStations.set(searchMapDepartureStations.indexOf(station), updatedStation);
                                 break;
                             }
                         }
                     }
                     for (Station station : searchMapArrivalStations) {
                         if (station.lng == updatedStation.lng && station.lat == updatedStation.lat) {
                             // If there is not enough stands, do not display it anymore
                             if (updatedStation.availableBikeStands < Integer.valueOf(arrivalStandsField.getText().toString())) {
                                 findAndDisplaySearchStations();
                                 return;
                             } else {
                                 searchMapArrivalStations.set(searchMapArrivalStations.indexOf(station), updatedStation);
                                 break;
                             }
 
                         }
                     }
                 }
             }
             clearMap();
 
             searchMapMarkersAdded = false;
 
             for (Station station : searchMapDepartureStations) {
                 if (!searchMapMarkersAdded) {
                     station.searchMarker = map.addMarker(StationMarker.createMarker(MainActivity.this, station));
                 }
             }
             for (Station station : searchMapArrivalStations) {
                 if (!searchMapMarkersAdded) {
                     station.searchMarker = map.addMarker(StationMarker.createMarker(MainActivity.this, station));
                 }
             }
 
             if (!searchMapMarkersAdded) {
                 departureMarker = map.addMarker(new MarkerOptions().position(departureLocation).title(getString(R.string.departure)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_departure)));
                 arrivalMarker = map.addMarker(new MarkerOptions().position(arrivalLocation).title(getString(R.string.arrival)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_arrival)));
             }
 
             if (searchMapPolyline != null) {
                 PolylineOptions options = new PolylineOptions().addAll(searchMapPolyline.getPoints()).width(getResources().getDimensionPixelSize(R.dimen.polyline_width)).color(getResources().getColor(R.color.green)).geodesic(true);
                 if (searchMapPolyline != null) {
                     searchMapPolyline.remove();
                 }
                 searchMapPolyline = map.addPolyline(options);
             }
 
 
             searchMapMarkersAdded = true;
 
         }
     }
 
     private void clearMap() {
         if (map != null) {
             map.clear();
             defaultMapStations.clear();
         }
     }
 
     private void toggleSearchViewVisible() {
         if (!searchViewVisible) {
             searchViewVisible = true;
             searchView.setVisibility(View.VISIBLE);
             this.actionSearchMenuItem.setVisible(false);
             this.actionClearSearchMenuItem.setVisible(false);
             mapView.animate().translationY(searchView.getHeight());
         } else {
             hideSearchForm();
         }
     }
 
     private void clearSearch() {
         searchMode = false;
         actionClearSearchMenuItem.setVisible(false);
         clearMap();
         displayStations();
     }
 
     private void hideSearchForm() {
         searchViewVisible = false;
         this.actionSearchMenuItem.setVisible(true);
 
         if (searchMode) {
             this.actionClearSearchMenuItem.setVisible(true);
         }
 
         mapView.animate().translationY(0);
         InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(FragmentActivity.INPUT_METHOD_SERVICE);
         inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
     }
 
     private void fillAddressFieldWithCurrentLocation(int field) {
         Location userLocation = locationClient.getLastLocation();
         if (userLocation == null) {
             Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show();
         } else {
             new GetAddressTask(field).execute(userLocation);
         }
     }
 
     private void startSearch() {
 
         boolean makeSearch = false;
 
         synchronized (stations) {
             if (departureField.getText().toString().trim().length() == 0) {
                 Toast.makeText(this, R.string.departure_unavailable, Toast.LENGTH_LONG).show();
             } else if (arrivalField.getText().toString().trim().length() == 0) {
                 Toast.makeText(this, R.string.arrival_unavailable, Toast.LENGTH_LONG).show();
             } else if (stations.isEmpty()) {
                 Toast.makeText(this, R.string.stations_not_available, Toast.LENGTH_LONG).show();
             } else {
                 makeSearch = true;
             }
         }
 
         if (makeSearch) {
             departureLocation = null;
             arrivalLocation = null;
             searchMapDepartureStation = null;
             searchMapArrivalStation = null;
             searchMapDepartureStations = null;
             searchMapArrivalStations = null;
             searchMapMarkersAdded = false;
 
             //Close keyboard
             InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
             inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 
             searchStationsNearAddress(departureField.getText().toString().trim(), FIELD_DEPARTURE);
             searchStationsNearAddress(arrivalField.getText().toString().trim(), FIELD_ARRIVAL);
         }
     }
 
     /**
      * Find the stations near departure and arrival locations, and display them
      */
     private void findAndDisplaySearchStations() {
         searchMapDepartureStations = searchStationsNearLocation(departureLocation, arrivalLocation, Integer.valueOf(departureBikesField.getText().toString()), FIELD_DEPARTURE);
         searchMapArrivalStations = searchStationsNearLocation(departureLocation, arrivalLocation, Integer.valueOf(arrivalStandsField.getText().toString()), FIELD_ARRIVAL);
         if (map != null && searchMapDepartureStations.size() > 0 && searchMapArrivalStations.size() > 0) {
 
             searchMode = true;
             actionClearSearchMenuItem.setVisible(true);
 
             clearMap();
 
             searchMapDepartureStation = searchMapDepartureStations.get(0);
             searchMapArrivalStation = searchMapArrivalStations.get(0);
 
             displaySearchResult();
 
 
         } else {
             Toast.makeText(MainActivity.this, R.string.path_impossible, Toast.LENGTH_LONG).show();
         }
     }
 
     private void searchStationsNearAddress(String address, final int fieldId) {
         WSRequest request = new WSRequest(MainActivity.this, Constants.GOOGLE_API_GEOCODE_URL);
         request.withParam(Constants.GOOGLE_API_ADDRESS, address);
         request.withParam(Constants.GOOGLE_API_SENSOR, "true");
         request.handleWith(new WSDefaultHandler() {
             @Override
             public void onResult(Context context, JSONObject result) {
                 JSONArray addressLatLng = (JSONArray) result.opt("results");
                 if (addressLatLng != null && addressLatLng.length() > 0) {
                     JSONObject geometry = addressLatLng.optJSONObject(0).optJSONObject("geometry");
                     if (geometry != null) {
                         JSONObject location = geometry.optJSONObject("location");
                         if (location != null) {
                             double lat = location.optDouble(Constants.GOOGLE_LAT_KEY);
                             double lng = location.optDouble(Constants.GOOGLE_LNG_KEY);
                             if (fieldId == FIELD_DEPARTURE) {
                                 departureLocation = new LatLng(lat, lng);
                             }
                             if (fieldId == FIELD_ARRIVAL) {
                                 arrivalLocation = new LatLng(lat, lng);
                             }
                             if (departureLocation != null && arrivalLocation != null) {
                                 findAndDisplaySearchStations();
                             }
                         } else {
                             if (fieldId == FIELD_DEPARTURE) {
                                 Toast.makeText(MainActivity.this, R.string.arrival_unavailable, Toast.LENGTH_LONG).show();
                             }
                             if (fieldId == FIELD_ARRIVAL) {
                                 Toast.makeText(MainActivity.this, R.string.arrival_unavailable, Toast.LENGTH_LONG).show();
                             }
                         }
                     }
                 }
             }
         });
         request.call();
     }
 
     private ArrayList<Station> searchStationsNearLocation(LatLng startLocation, LatLng finishLocation, int bikesNumber, int fieldId) {
         int matchingStationNumber = 0;
         LatLng location;
         ArrayList<Station> matchingStations = new ArrayList<Station>();
 
         if (fieldId == FIELD_DEPARTURE) {
             location = startLocation;
         } else {
             location = finishLocation;
         }
 
         long radiusDist = getDistanceInMeters(startLocation, finishLocation) / 2;
         if (radiusDist > Constants.STATION_SEARCH_MAX_RADIUS_IN_METERS) {
             radiusDist = Constants.STATION_SEARCH_MAX_RADIUS_IN_METERS;
         }
         if (radiusDist < Constants.STATION_SEARCH_MIN_RADIUS_IN_METERS) {
             radiusDist = Constants.STATION_SEARCH_MIN_RADIUS_IN_METERS;
         }
 
         Log.e(getDistanceInMeters(startLocation, finishLocation) + "");
         Log.e(radiusDist + "");
 
         Map<Station, Long> distanceStations = new HashMap<Station, Long>();
         // find all stations distance for a radius
         synchronized (stations) {
             for (Station station : stations) {
                 if (!Double.isNaN(station.lat) && !Double.isNaN(station.lng)) {
                     Long distance = Long.valueOf(MainActivity.this.getDistanceInMeters(location, station.latLng));
                     if (distance.longValue() <= radiusDist) {
                         if (fieldId == FIELD_DEPARTURE) {
                             if (station.availableBikes >= bikesNumber) {
                                 distanceStations.put(station, distance);
                             }
                         } else {
                             if (station.availableBikeStands >= bikesNumber) {
                                 distanceStations.put(station, distance);
                             }
                         }
                     }
                 }
             }
         }
 
         // sort station by distance and get the first SEARCH_RESULT_MAX_STATIONS_NUMBER stations
         distanceStations = Util.sortMapByValues(distanceStations);
         for (Map.Entry<Station, Long> entry : distanceStations.entrySet()) {
             if (matchingStationNumber < Constants.SEARCH_RESULT_MAX_STATIONS_NUMBER) {
                 if (!matchingStations.contains(entry.getKey())) {
                     matchingStations.add(entry.getKey());
                     matchingStationNumber++;
                 }
             } else {
                 // station max number is reached for this location
                 break;
             }
         }
 
         return matchingStations;
     }
 
     private void displaySearchResult() {
         WSRequest request = new WSRequest(MainActivity.this, Constants.GOOGLE_API_DIRECTIONS_URL);
         request.withParam(Constants.GOOGLE_API_ORIGIN, searchMapDepartureStation.lat + "," + searchMapDepartureStation.lng);
         request.withParam(Constants.GOOGLE_API_DESTINATION, searchMapArrivalStation.lat + "," + searchMapArrivalStation.lng);
         request.withParam(Constants.GOOGLE_API_MODE_KEY, Constants.GOOGLE_API_MODE_VALUE);
         request.withParam(Constants.GOOGLE_API_SENSOR, "true");
         request.handleWith(new WSDefaultHandler() {
             @Override
             public void onResult(Context context, JSONObject result) {
                 if ("OK".equals(result.optString("status"))) {
                     JSONArray routeArray = result.optJSONArray("routes");
                     JSONObject routes = routeArray.optJSONObject(0);
                     JSONObject overviewPolylines = routes.optJSONObject("overview_polyline");
                     String encodedString = overviewPolylines.optString("points");
                     List<LatLng> list = Util.decodePoly(encodedString);
                     // add the location of the departure and arrival stations
                     list.add(0, searchMapDepartureStation.latLng);
                     list.add(searchMapArrivalStation.latLng);
 
                     PolylineOptions options = new PolylineOptions().addAll(list).width(getResources().getDimensionPixelSize(R.dimen.polyline_width)).color(getResources().getColor(R.color.green)).geodesic(true);
                     if (searchMapPolyline != null) {
                         searchMapPolyline.remove();
                     }
                     searchMapPolyline = map.addPolyline(options);
                     //Close form
                     hideSearchForm();
 
                     //Show markers for the first time and set the bounds
                     LatLngBounds.Builder bld = new LatLngBounds.Builder();
                     for (Station station : searchMapDepartureStations) {
                         if (!searchMapMarkersAdded) {
                             station.searchMarker = map.addMarker(StationMarker.createMarker(MainActivity.this, station));
                         }
                         bld.include(station.latLng);
                     }
                     for (Station station : searchMapArrivalStations) {
                         if (!searchMapMarkersAdded) {
                             station.searchMarker = map.addMarker(StationMarker.createMarker(MainActivity.this, station));
                         }
                         bld.include(station.latLng);
                     }
 
                     if (!searchMapMarkersAdded) {
                         departureMarker = map.addMarker(new MarkerOptions().position(departureLocation).title(getString(R.string.departure)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_departure)));
                         arrivalMarker = map.addMarker(new MarkerOptions().position(arrivalLocation).title(getString(R.string.arrival)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_arrival)));
                     }
                     searchMapMarkersAdded = true;
 
                     //Move camera to show path and stations
                     bld.include(departureLocation);
                     bld.include(arrivalLocation);
                     LatLngBounds bounds = bld.build();
                     map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, getResources().getDimensionPixelSize(R.dimen.padding_zoom_search_result)));
 
                 } else {
                     Toast.makeText(MainActivity.this, R.string.ws_google_search_route_fail, Toast.LENGTH_LONG).show();
                 }
             }
         });
         request.call();
     }
 
     private long getDistanceInMeters(LatLng point1, LatLng point2) {
         double lat1 = point1.latitude;
         double lng1 = point1.longitude;
         double lat2 = point2.latitude;
         double lng2 = point2.longitude;
         double dLat = Math.toRadians(lat2 - lat1);
         double dLng = Math.toRadians(lng2 - lng1);
         double radLat1 = Math.toRadians(lat1);
         double radLat2 = Math.toRadians(lat2);
 
         double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(radLat1) * Math.cos(radLat2);
         double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
         return (long) (Constants.EARTH_RADIUS * c);
     }
 
     public LocationClient getLocationClient() {
         return locationClient;
     }
 
 }
