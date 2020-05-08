 package com.appchallenge.android;
 
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.http.conn.ConnectTimeoutException;
 
 import com.google.android.gms.maps.CameraUpdate;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
 import com.google.android.gms.maps.LocationSource;
 import com.google.android.gms.maps.SupportMapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationListener;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.appchallenge.android.Event.Type;
 import com.appchallenge.android.TypeFilterDialogFragment.TypeFilterDialogListener;
 
 /**
  * Displays a user's location and surrounding events.
  */
 public class EventViewer extends SherlockFragmentActivity implements LocationListener,
                                                                      LocationSource,
                                                                      OnInfoWindowClickListener,
                                                                      TypeFilterDialogListener {
     /**
      * Object representing the Google Map display of our Events.
      * Note that this may be null if the Google Play services APK is not available.
      */
     private GoogleMap mMap;
 
     /**
      * Listener for the Google Map user location.
      */
     private OnLocationChangedListener mListener;
 
     /**
      * Store the current location and zoom of the user.
      */
     private LatLng currentLocation;
 
     /**
      * Values saving the current location and zoom of the Map.
      * Currently defaulted to Minneapolis, MN.
      */
     private LatLng currentMapLocation = new LatLng(44.9764164, -93.2323474);
     private float currentMapZoom = 15;
 
     /**
      * Array of Events we have downloaded for the user.
      */
     private ArrayList<Event> currentEvents = new ArrayList<Event>();
     
     /**
      * Maps the existing markers to their corresponding Event ID.
      * This allows actions taken on a marker to be traced back to the
      * target Event.
      */
     HashMap<Marker, Integer> eventMarkerMap = new HashMap<Marker, Integer>();
     
     /**
      * Collection of event Types used for filtering events. The user
      * can choose to remove these using the TypeFilterDialogFragment from the
      * action bar.
      */
     ArrayList<Type> filterTypes = new ArrayList<Type>();
     
     /**
      * Provides access to our local sqlite database.
      */
     private LocalDatabase localDB;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_event_viewer);
 
         // Instantiate the list of visible types.
         if (filterTypes.size() == 0) {
         	for (Type type : Type.typeIndices)
         	    filterTypes.add(type);
         }
 
         // We may be reloading due to a configuration change.
         // Load any available information from the previous instance.
         if (savedInstanceState != null) {
             currentMapLocation = savedInstanceState.getParcelable("currentMapLocation");
             currentMapZoom = savedInstanceState.getFloat("currentMapZoom");
 
             // Restore the Event collection and markers.
             this.currentEvents = savedInstanceState.getParcelableArrayList("currentEvents");
             reloadEventMarkers();
 
             // Restore any closed dialogs.
             if (savedInstanceState.getBoolean("noLocationSourceDialogOpen", false))
             	this.showNoLocationSourceDialog();
         }
 
         setUpMapIfNeeded();
 
         if (savedInstanceState != null) {
         	// Feed the map listener it's previous to restore the location indicator.
         	if (savedInstanceState.containsKey("currentUserLocation")) {
         		Location oldLoc = new Location("savedState");
         		this.currentLocation = savedInstanceState.getParcelable("currentUserLocation");
 	            oldLoc.setLatitude(this.currentLocation.latitude);
 	            oldLoc.setLongitude(this.currentLocation.longitude);
 	            oldLoc.setAccuracy(savedInstanceState.getFloat("currentUserLocationAccuracy", 0));
 
                 if (mListener != null)
 	                mListener.onLocationChanged(oldLoc);
         	}
         }
 
         // Grab the latest location information.
         if (savedInstanceState == null)
         	this.updateUserLocation();
         
         String WELCOME_DIALOG = "Welcome";
     	String WELCOME_KEY = "DISPLAY";
         //Displays the Welcome screen until don't show is pressed
         SharedPreferences welcomeIndicator = getBaseContext().getSharedPreferences(WELCOME_DIALOG, 0);
         String indicator = welcomeIndicator.getString(WELCOME_KEY, "");
         if (indicator.length() == 0) {
         	SharedPreferences.Editor editor = welcomeIndicator.edit();
 		    editor.putString(WELCOME_KEY, "no");
 		    editor.commit();
         	DialogFragment welcomeDialog = new WelcomeDialogFragment();
     		welcomeDialog.show(getSupportFragmentManager(), "welcomeDialog");
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         setUpMapIfNeeded();
         mMap.setMyLocationEnabled(true);
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
 
     	// Close our database helper if necessary.
     	if (localDB != null)
             localDB.close();
     }
     
     @Override
     protected void onStop() {
     	// Prevent dialogs from leaking and remaining open.
     	if (noLocationSourceDialog != null && noLocationSourceDialog.isShowing()) {
     		noLocationSourceDialog.cancel();
     		noLocationSourceDialog = null;
     	}
         super.onStop();
     }
 
     /**
      * Receives the result of the event creation wizard.
      */
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	Log.d("EventViewer.onActivityResult", "Received intent back from creation wizard.");
     	if (resultCode != RESULT_OK)
     		return;
     	
     	// Show the user the newly created event.
     	Event event = data.getParcelableExtra("event");
     	if (event == null)
     		return;
     	
     	// Save this event's owner_id to our local storage.
     	if (localDB == null)
     		localDB = new LocalDatabase(this);
     	localDB.takeOwnership(event);
     	
     	// Update the local event cache with this new event to avoid notifications about it.
     	ArrayList<Event> container = new ArrayList<Event>();
     	container.add(event);
     	localDB.updateLocalEventCache(container);
 
     	// Display the new marker
     	this.currentEvents.add(event);
     	Marker m = mMap.addMarker(event.toMarker());
     	eventMarkerMap.put(m, event.getId());
 
     	// Pan and zoom to this new marker.
     	CameraUpdate viewEvent = CameraUpdateFactory.newLatLngZoom(event.getLocation(), 18);
     	mMap.animateCamera(viewEvent);
 
     	// Show the info window for the new event.
     	m.showInfoWindow();
     }
 
     private Menu _menu;
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.activity_event_viewer, menu);
         
         // Keep a reference to the menu for later uses (refresh indicator change).
         this._menu = menu;
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
     	
     	int currentId = item.getItemId();
         if (currentId == R.id.menu_create_event) {
         	//Makes sure null cannot get passed as an location
         	
         	if (this.currentLocation == null) {
         		this.updateUserLocation();
         		//Checks if the have enabled location since event start, if the have let continue
         		if(this.currentLocation !=null) {
         			Intent createEvent = new Intent(EventViewer.this, CreateEvent.class);
         			// Pass the current location to the wizard so the maps appear synced.
         			createEvent.putExtra("location", this.currentLocation);
         			// Launch the wizard for creating a new event.
         			startActivityForResult(createEvent, 0);
         			return true;
         		}
         		
         	    return true;
         	}
 			Intent createEvent = new Intent(EventViewer.this, CreateEvent.class);
 
 			// Pass the current location to the wizard so the maps appear synced.
 			createEvent.putExtra("location", this.currentLocation);
 			// Launch the wizard for creating a new event.
 			startActivityForResult(createEvent, 0);
 			return true;
 		} else if (currentId == R.id.menu_refresh_events) {
 			// Refresh the event listing.
 			if (!isOnline())
 				this.displayConnectivityMessage();
 			else if (currentLocation != null)
 			    new getEventsNearLocationAPICaller().execute(currentLocation);
 			return true;
 		} else if (currentId == R.id.menu_type_filter) {
 			// Show dialog allowing the user to view only certain event types.
 			DialogFragment typeFilterDialog = new TypeFilterDialogFragment();
 			typeFilterDialog.show(getSupportFragmentManager(), "typeFilterDialog");
 			return true;
 		} else if (currentId == R.id.menu_settings) {
 			// Show the settings activity.
 			Intent settings = new Intent(EventViewer.this, Settings.class);
 			startActivity(settings);
 		} else if (currentId == R.id.menu_welcome_dialog) {
 			DialogFragment welcomeDialog = new WelcomeDialogFragment();
     		welcomeDialog.show(getSupportFragmentManager(), "welcomeDialog");
     		return true;
 		} else if (currentId == R.id.edit_my_events) {
 			if (this.currentEvents != null) {
 				Intent eventEdit = new Intent(EventViewer.this, EventEdit.class);
 				eventEdit.putParcelableArrayListExtra("currentEvents", this.currentEvents);
 				startActivity(eventEdit);
 				return true;
 			}  else {
 				Context context = getApplicationContext();
 				Toast.makeText(context, "No Events Found Near You to Edit.", Toast.LENGTH_LONG).show();
 			}
 		}
 
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * Android can arbitrarily reload our Activity, especially during screen rotations,
      * keyboard opening/closing, etc. When this happens, the full onCreate, onResume, etc.
      * sequence takes place. To avoid losing data (our Events in particular), the basic
      * way to preserve state is to override this function and store primitive types in
      * the savedInstanceState Bundle.
      */
     public void onSaveInstanceState(Bundle savedInstanceState) {
     	savedInstanceState.putParcelable("currentMapLocation", mMap.getCameraPosition().target);
     	savedInstanceState.putFloat("currentMapZoom", mMap.getCameraPosition().zoom);
     	if (currentLocation != null) {
     	    savedInstanceState.putParcelable("currentUserLocation", this.currentLocation);
     	    savedInstanceState.putFloat("currentUserLocationAccuracy", mMap.getMyLocation().getAccuracy());
     	}
 
     	// Keep the collection of Events when changing configuration.
     	savedInstanceState.putParcelableArrayList("currentEvents", this.currentEvents);
 
     	// Dialogs get thrown under the bus on configuration changes, so their
         // state must be remembered.
     	if (noLocationSourceDialog != null && noLocationSourceDialog.isShowing())
     		savedInstanceState.putBoolean("noLocationSourceDialogOpen", true);
 
         super.onSaveInstanceState(savedInstanceState);
     }
 
     /**
      * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
      * installed) and the map has not already been instantiated.. This will ensure that we only ever
      * call {@link #setUpMap()} once when {@link #mMap} is not null.
      * <p>
      * If it isn't installed {@link SupportMapFragment} (and
      * {@link com.google.android.gms.maps.MapView
      * MapView}) will show a prompt for the user to install/update the Google Play services APK on
      * their device.
      * <p>
      * A user can return to this Activity after following the prompt and correctly
      * installing/updating/enabling the Google Play services. Since the Activity may not have been
      * completely destroyed during this process (it is likely that it would only be stopped or
      * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
      * {@link #onResume()} to guarantee that it will be called.
      */
     private void setUpMapIfNeeded() {
         // Do a null check to confirm that we have not already instantiated the map.
         if (mMap == null) {
             // Try to obtain the map from the SupportMapFragment.
             mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 
             // Register the LocationSource
             mMap.setLocationSource(this);
 
             // Check if we were successful in obtaining the map.
             if (mMap != null)
                 setUpMap();
         }
     }
 
     /**
      *  This is where we can add markers or lines, add listeners or move the camera.
      *  This should only be called once and when we are sure that the map is not null.
      */
     private void setUpMap() {
     	//mMap.getUiSettings().setZoomControlsEnabled(false);
     	mMap.setMyLocationEnabled(true);
     	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentMapLocation, currentMapZoom));
     	mMap.setOnInfoWindowClickListener(this);
     }
 
     /**
      * Occurs when the user clicks the marker info windows.
      * Shows the extended information activity for the marker.
      * @param marker
      */
 	@Override
 	public void onInfoWindowClick(Marker marker) {
 		// Retrieve the marker event via hash map.
 
 		// It seems like it might be possible to map directly from marker to
 		// Event objects, but there were some reference issues that made
 		// mapping to ID more appealing even despite the increased complexity.
 		int selectedId = eventMarkerMap.get(marker);
 		Event selectedEvent = null;
 		for (Event event : this.currentEvents) {
 			if (event.getId() == selectedId)
 				selectedEvent = event;
 		}
 		
 		if (selectedEvent == null) {
 			Log.e("EventViewer.onInfoWindowClick", "selectedEvent == null");
 			return;
 		}
 		
 		// Pass information about the event to the details activity.
 		Intent eventDetails = new Intent(EventViewer.this, EventDetails.class);
 		eventDetails.putExtra("event", selectedEvent);
 		eventDetails.putExtra("userLocation", this.currentLocation);
 		startActivity(eventDetails);
 	}
 
     /**
      * Adds the current Events tracked in this activity to the map.
      */
     private void reloadEventMarkers() {
     	if (this.currentEvents == null || this.currentEvents.size() == 0)
     		return;
 
     	setUpMapIfNeeded();
     	mMap.clear();
         eventMarkerMap.clear();
         for (Event event : this.currentEvents) {
         	// Only display events that match the currently filtered type list.
         	if (filterTypes.contains(event.getType())) {
     		    Marker m = mMap.addMarker(event.toMarker());
     		    eventMarkerMap.put(m, event.getId());
         	}
     	}
     }
 
     /**
 	 * Interface method for receiving a filter list of event types from
 	 * a TypeFilterDialogFragment.
 	 */
 	public void onTypeFilterDialogOKClick(DialogFragment dialog, ArrayList<Event.Type> selectedTypes) {
 		Log.d("EventViewer.onDialogOKClick", "Received list of selected types: " + selectedTypes.toString());
 		
 		// Replace the previous type filter list with the newly created one.
 		filterTypes = selectedTypes;
 		
 		// Force reloading the markers to perform the filter.
 		this.reloadEventMarkers();
 	}
 	
 	/**
 	 * Interface method for passing the current Type filter to a
 	 * TypeFilterDialogFragment for initializing the checkboxes.
 	 */
 	public ArrayList<Type> receiveCurrentFilterList() {
 		return this.filterTypes;
 	}
 
     private AlertDialog noLocationSourceDialog;
     private void showNoLocationSourceDialog() {
     	if (noLocationSourceDialog != null) {
     		noLocationSourceDialog.cancel();
 		}
     	noLocationSourceDialog = new AlertDialog.Builder(this).create();
     	noLocationSourceDialog.setTitle("Location Source Needed");
     	noLocationSourceDialog.setMessage("Turn on GPS or your cellular connection and try again!");
     	noLocationSourceDialog.setIcon(android.R.drawable.ic_dialog_alert);
     	noLocationSourceDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Enable", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) {
                 startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                 dialog.cancel();
 	        }
 	    });
    	noLocationSourceDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
 	        public void onClick(DialogInterface dialog, int which) {
                 dialog.cancel();
 	        }
 	    });
     	noLocationSourceDialog.show();
     }
     public void updateUserLocation() {
         LocationFinder locationFinder = new LocationFinder();
         boolean sourcesExist = locationFinder.getLocation(this);
         if (!sourcesExist) {
         	// Show a dialog indicating to turn on some location source.
         	showNoLocationSourceDialog();
         }
         locationFinder.getLocation(this);
     }
 
 	@Override
 	public void onLocationChanged(Location location) {
 		// Feed the map listener the location to update the location indicator.
 		if (mListener != null && location != null)
 	        mListener.onLocationChanged(location);
 
 		// Act based on the new location.
         if (location != null) {
         	currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
         	mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
         	if (isOnline())
         	    new getEventsNearLocationAPICaller().execute(currentLocation);
         }
 	}
 
 	// Methods required (besides onLocationChanged()) for LocationListener.
 	public void onProviderDisabled(String provider) {}
 	public void onProviderEnabled(String provider) {}
 	public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 	@Override
 	public void activate(OnLocationChangedListener listener) {
 		mListener = listener;
 	}
 
 	@Override
 	public void deactivate() {
 		mListener = null;
 	}
 
 	/**
 	 * Determines if the device has internet connectivity.
 	 * @return Whether a data connection is available.
 	 */
 	public boolean isOnline() {
         ConnectivityManager connectivityManager =
           (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
 
         return (networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable());
     }
 
 	/**
 	 * Shows a message informing the user that an internet connection is not available.
 	 */
 	public void displayConnectivityMessage() {
         Toast.makeText(getApplicationContext(),
                        "Please connect to the Internet and try again!", 
                        Toast.LENGTH_SHORT).show();
 	}
 
 	/**
 	 * Performs an asynchronous API call to find nearby events.
 	 */
 	private class getEventsNearLocationAPICaller extends AsyncTask<LatLng, Void, ArrayList<Event>> {
 		/**
 		 * Quick access to the refresh button in the actionbar.
 		 */
 		MenuItem refreshItem;
 
 		protected void onPreExecute() {
 			// Establish progress UI changes.
 			if (_menu != null) {
 		        refreshItem = _menu.findItem(R.id.menu_refresh_events);
 		        if (refreshItem != null)
 			        refreshItem.setActionView(R.layout.actionbar_refresh_progress);
 			}
 		}
 
 		protected ArrayList<Event> doInBackground(LatLng... location) {
 			// Perform the network call to retreive nearby events.
 			try {
 				return APICalls.getEventsNearLocation(location[0]);
 			} catch (ConnectTimeoutException cte) {
 				Toast.makeText(getApplicationContext(), "Connection could not be established. Please try again later!", Toast.LENGTH_LONG)
 			         .show();
 				cte.printStackTrace();
 			} catch (SocketTimeoutException ste) {
 				Toast.makeText(getApplicationContext(), "Issue receiving server data. Please try again later!", Toast.LENGTH_LONG)
 		             .show();
 				ste.printStackTrace();
 			}
 			return null;
 		}
 
 		@SuppressWarnings("unchecked")
 		protected void onPostExecute(ArrayList<Event> result) {
 			// Remove progress UI.
 			if (refreshItem != null)
 			    refreshItem.setActionView(null);
 			refreshItem = null;
 
 			// Keep track of these events and populate the map.
 			if (result == null)
 				return;
 			
 			if (result.size() == 0) {
                 Toast.makeText(getApplicationContext(), "No events found near you!", Toast.LENGTH_LONG)
 			         .show();
                 return;
 			}
 
 			currentEvents = (ArrayList<Event>)result.clone();
 			
 			if (localDB == null)
 				localDB = new LocalDatabase(EventViewer.this);
 
 			// Inform the local cache of these new events.
 			localDB.updateLocalEventCache(result);
 			
 			reloadEventMarkers();
 		}
 	}
 }
