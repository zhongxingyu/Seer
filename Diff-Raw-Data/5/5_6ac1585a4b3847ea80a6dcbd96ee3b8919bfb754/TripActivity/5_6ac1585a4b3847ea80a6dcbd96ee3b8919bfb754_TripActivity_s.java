 package com.example.transitlogger;
 
 import java.util.Date;
 import java.util.List;
 
 import com.example.transitlogger.model.Distance;
 import com.example.transitlogger.model.Place;
 import com.example.transitlogger.model.Trip;
 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher;
 
 public class TripActivity extends Activity {
 	private Trip trip;
 	
 	public static TripDB tripDB;
 	
 	public enum State {
 		WAITING_FOR_LOCATION,
 		STARTED,
 		ENDED,
 		AFTER_ENDED;
 	}
 	
 	State state;
 	
 	private TextView distanceText;
 	private TextView labelDistance;
 	private LocationManager locationManager;
 	
 	private AutoCompleteTextView startPlaceText;
 	private AutoCompleteTextView endPlaceText;
 	
 	private Button okStartPlaceButton;
 	private Button okEndPlaceButton;
 	
     private Location currentBestLocation = null;
 
 	private LocationListener locationListener;
 
 	
 	public LocationManager getLocationManager() {
 		return locationManager;
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_trip);
 		
 		//Intent intent = getIntent();
 		// TODO: accept specific trip IDs
 
 		distanceText = (TextView) findViewById(R.id.distanceText);
 		labelDistance = (TextView) findViewById(R.id.labelDistance);
 		labelDistance.setVisibility(View.GONE);
 		distanceText.setVisibility(View.GONE);
 
 	    tripDB = new TripDB(this);
 	    tripDB.open();
 
 		// Create a new trip.
 		trip = new Trip();
 
 		startPlaceText = (AutoCompleteTextView) findViewById(R.id.autoCompleteStartPlace);
 		endPlaceText = (AutoCompleteTextView) findViewById(R.id.autoCompleteEndPlace);
 		
 		setupLocationProvider();
 		
 		updatePlacesAutocomplete();
 		
 		// Hide the start place add button
 		okStartPlaceButton = (Button) findViewById(R.id.okStartPlaceButton);
 		okStartPlaceButton.setVisibility(View.INVISIBLE);
 		
 		// Hide the start place autocomplete
 		startPlaceText.setVisibility(View.INVISIBLE);
 
 		// Hide the end place add button
 		okEndPlaceButton = (Button) findViewById(R.id.okEndPlaceButton);
 		okEndPlaceButton.setVisibility(View.INVISIBLE);
 		
 		// Hide the end place autocomplete
 		endPlaceText.setVisibility(View.INVISIBLE);
 
 		setState(State.WAITING_FOR_LOCATION);
 	}
 	
 	public void setState(State state) {
 		this.state = state;
 		
 		switch (state) {
 		case WAITING_FOR_LOCATION:
 			setStatus("Waiting for GPS...");
 			break;
 		case STARTED:
 			// Show the distance information
 			labelDistance.setVisibility(View.VISIBLE);
 			distanceText.setVisibility(View.VISIBLE);
 			break;
 		case ENDED:
 			break;
 		case AFTER_ENDED:
 			break;
 		}
 	}
 	
 	public void startTrip() {
		trip.setFromDate(new Date());
 		setState(State.STARTED);
 	}
 	
 	public void endTrip() {
		trip.setToDate(new Date());
 		
 		// Save trip to database
 		long id = tripDB.addTrip(trip);
 
 		showLongMessage("Trip saved to DB with id: " + id);
 		
 		setState(State.ENDED);
 	}
 	
 	protected void updatePlacesAutocomplete() {
 		List<Place> places = tripDB.getAllPlaces();
 		ArrayAdapter<Place> adapter = new ArrayAdapter<Place>(this,
 				android.R.layout.simple_list_item_1, places);
 		startPlaceText.setAdapter(adapter);
 		endPlaceText.setAdapter(adapter);
 	}
 
 
 	public void shutdownLocationProvider() {
 		// Acquire a reference to the system Location Manager
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		locationManager.removeUpdates(locationListener);
 	}
 	
 	public void setupLocationProvider() {
 		// Acquire a reference to the system Location Manager
 		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 
 		// Define a listener that responds to location updates
 		locationListener = new LocationListener() {
 			public void onLocationChanged(Location location) {
 				// Called when a new location is found by the network location provider.
 				// Only update the location if it's better
 		    	if (location != null && isBetterLocation(location, currentBestLocation)) {
 			    	updateLocation(location);
 			    	currentBestLocation = location;
 		    	}
 		    }
 
 		    public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 		    public void onProviderEnabled(String provider) {}
 
 		    public void onProviderDisabled(String provider) {}
 		    
 		    private static final int TWO_MINUTES = 1000 * 60 * 2;
 
 		    /** Determines whether one Location reading is better than the current Location fix
 		      * @param location  The new Location that you want to evaluate
 		      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
 		      */
 		    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
 		        if (currentBestLocation == null) {
 		            // A new location is always better than no location
 		            return true;
 		        }
 
 		        // Check whether the new location fix is newer or older
 		        long timeDelta = location.getTime() - currentBestLocation.getTime();
 		        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
 		        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
 		        boolean isNewer = timeDelta > 0;
 
 		        // If it's been more than two minutes since the current location, use the new location
 		        // because the user has likely moved
 		        if (isSignificantlyNewer) {
 		            return true;
 		        // If the new location is more than two minutes older, it must be worse
 		        } else if (isSignificantlyOlder) {
 		            return false;
 		        }
 
 		        // Check whether the new location fix is more or less accurate
 		        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
 		        boolean isLessAccurate = accuracyDelta > 0;
 		        boolean isMoreAccurate = accuracyDelta < 0;
 		        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
 
 		        // Check if the old and new location are from the same provider
 		        boolean isFromSameProvider = isSameProvider(location.getProvider(),
 		                currentBestLocation.getProvider());
 
 		        // Determine location quality using a combination of timeliness and accuracy
 		        if (isMoreAccurate) {
 		            return true;
 		        } else if (isNewer && !isLessAccurate) {
 		            return true;
 		        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
 		            return true;
 		        }
 		        return false;
 		    }
 
 		    /** Checks whether two providers are the same */
 		    private boolean isSameProvider(String provider1, String provider2) {
 		        if (provider1 == null) {
 		          return provider2 == null;
 		        }
 		        return provider1.equals(provider2);
 		    }
 		  };
 
 		final float minDistance = 5.0f; // meters
 		  
 		// Register the listener with the Location Manager to receive location updates
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, minDistance, locationListener);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.trip, menu);
 		return true;
 	}
 	
 	public void onEndTrip(View view) {
 		shutdownLocationProvider();
 		
 		if (state == State.STARTED) {
 			showLongMessage("Trip ended! Distance: " + trip.getDistance().getKilometers() + " km");
 	
 			// Auto-select the start place if we can
 			Place nearestPlace = findNearestPlace(currentBestLocation);
 			if (nearestPlace != null) {
 				// Choose it in our place box
 				endPlaceText.setText(nearestPlace.getName());
 				
 				// Set the trip's end place.
 				trip.setEndPlace(nearestPlace);
 				
 				// End the trip!
 				endTrip();
 				
 				switchEndPlaceView();
 				
 				setState(State.AFTER_ENDED);
 			} else {
 				// If we can't, prompt the user for it, before ending the trip
 				showLongMessage("Please enter a name for this location.");
 				
 				// Show the end place OK button
 				okEndPlaceButton.setVisibility(View.VISIBLE);
 				
 				// Show the end place text field
 				endPlaceText.setVisibility(View.VISIBLE);
 				
 				if (AppStatus.isOnline(this)) {
 					// Try to get the address through reverse geocode of the coordinates.
 					GeocodingHelper.getFromLocation(currentBestLocation.getLatitude(), currentBestLocation.getLongitude(), 1, new GeocoderHandler());
 				}
 			}
 		}
 		
 		// TODO: Allow the distance to be editable now.
 		// TODO: Possibly, let them discard the trip somehow, rather than saving it
 		
 		// Go back if we haven't started yet, or if we've completely ended the trip.
 		if (state == State.WAITING_FOR_LOCATION || state == State.AFTER_ENDED) {
 			// Go back to main screen
 		    Intent intent = new Intent(this, MainActivity.class);
 		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		    startActivity(intent);
 		}
 	}
 	
 
 	public void onOKStartPlace(View view) {
 		String name = startPlaceText.getText().toString();
 		
 		// Check if we have a GPS location yet.
 		if (currentBestLocation == null) {
 			showLongMessage("Please wait until GPS location is acquired before adding a new place.");
 			return;
 		}
 		
 		// Check that this place name has not already been taken.
 		Place place = tripDB.getPlaceByName(name);
 		if (place != null) {
 			showLongMessage("Place already exists with that name. Please choose another name.");
 			return;
 		}
 		
 		// Create the new place and add it to the DB
 		Place newPlace = new Place();
 		newPlace.setName(name);
 		newPlace.setLat(currentBestLocation.getLatitude());
 		newPlace.setLon(currentBestLocation.getLongitude());
 		tripDB.addPlace(newPlace);
 		
 		// Hide the button
 		okStartPlaceButton.setVisibility(View.INVISIBLE);
 
 		// Set the trip's start place.
 		trip.setStartPlace(newPlace);
 		
 		// Update the UI
 		switchStartPlaceView();
 		
 		showLongMessage("Added place successfully. " + newPlace.toString());
 	    
 		// Start the trip!
 		startTrip();
 		
 		// Update autocomplete
 		updatePlacesAutocomplete();
 	}
 	
 	public void onOKEndPlace(View view) {
 		String name = endPlaceText.getText().toString();
 		
 		// Check that this place name has not already been taken.
 		Place place = tripDB.getPlaceByName(name);
 		if (place != null) {
 			showLongMessage("Place already exists with that name. Please choose another name.");
 			return;
 		}
 		
 		// Create the new place and add it to the DB
 		Place newPlace = new Place();
 		newPlace.setName(name);
 		newPlace.setLat(currentBestLocation.getLatitude());
 		newPlace.setLon(currentBestLocation.getLongitude());
 		tripDB.addPlace(newPlace);
 		
 		// Hide the button
 		okEndPlaceButton.setVisibility(View.INVISIBLE);
 
 		// Set the trip's end place.
 		trip.setEndPlace(newPlace);
 		
 		// Update the UI
 		switchEndPlaceView();
 		
 		showLongMessage("Added place successfully. " + newPlace.toString());
 	    
 		// End the trip!
 		endTrip();
 
 		setState(State.AFTER_ENDED);
 		
 		// Update autocomplete
 		updatePlacesAutocomplete();
 	}
 	
 	private class GeocoderHandler extends Handler {
 	    @Override
 	    public void handleMessage(Message message) {
 	        String result;
 	        switch (message.what) {
 	        case 1:
 	            Bundle bundle = message.getData();
 	            result = bundle.getString("address");
 
 	            if (state == State.WAITING_FOR_LOCATION) {
 	            	if (startPlaceText.getText().toString().trim().length() == 0) {
 	            		startPlaceText.setText(result);
 	            	}
 	            } else if (state == State.ENDED) {
 	            	if (endPlaceText.getText().toString().trim().length() == 0) {
 	            		endPlaceText.setText(result);
 	            	}
 	            }
 	            break;
 	        default:
 	            result = "Couldn't get address.";
 	        }
 	    }   
 	}
 	
 	public void onLookupAddress(View view) {
 		if (currentBestLocation != null) {
 			// reverse geocode
 			if (AppStatus.isOnline(this)) {
 				// Try to get the address through reverse geocode of the coordinates.
 				GeocodingHelper.getFromLocation(currentBestLocation.getLatitude(), currentBestLocation.getLongitude(), 1, new GeocoderHandler());
 			}
 		}
 	}
 	
 	public void updateLocation(Location location) {
 		if (currentBestLocation == null) {
 			currentBestLocation = location;
 			
 			// Auto-select the start place if we can
 			Place nearestPlace = findNearestPlace(currentBestLocation);
 			if (nearestPlace != null) {
 				// Choose it in our place box
 				startPlaceText.setText(nearestPlace.getName());
 				
 				// Set the trip's start place.
 				trip.setStartPlace(nearestPlace);
 				
 				// Begin the trip once we've got our first location information
 				startTrip();
 				
 				switchStartPlaceView();
 			} else {
 				// If we can't, prompt the user for it, before beginning the trip
 				showLongMessage("Please enter a name for this location.");
 				
 				// Show the start place OK button
 				okStartPlaceButton.setVisibility(View.VISIBLE);
 				
 				// Show the start place text field
 				startPlaceText.setVisibility(View.VISIBLE);
 				
 				if (AppStatus.isOnline(this)) {
 					// Try to get the address through reverse geocode of the coordinates.
 					GeocodingHelper.getFromLocation(currentBestLocation.getLatitude(), currentBestLocation.getLongitude(), 1, new GeocoderHandler());
 				}
 			}
 		}
 		
 		setStatus(currentBestLocation.toString());
 		
 		if (state == State.STARTED) {
 			// Get distance from current to new location in kilometers.
 			double dist = currentBestLocation.distanceTo(location) / 1000.0;
 			
 			Log.d(getClass().getName(), String.format("curLocation: %s\nnewLocation: %s", currentBestLocation, location));
 			
 			// Update trip distance.
 			Distance distance = trip.getDistance();
 			distance.setKilometers(distance.getKilometers() + dist);
 			distanceText.setText(String.format("%.2f km", distance.getKilometers()));
 		}
 	}
 	
 	private void switchStartPlaceView() {
 		// Switch the startPlace autocomplete with a plain textview,
 		// so the user can't change it anymore.
 		startPlaceText.setVisibility(View.INVISIBLE);
 	    ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.viewSwitcherStartPlace);
 	    switcher.showNext(); //or switcher.showPrevious();
 	    TextView startPlaceTextView = (TextView) switcher.findViewById(R.id.startPlaceTextView);
 	    startPlaceTextView.setText("Starting place: " + trip.getStartPlace().getName());
 //	    startPlaceText.setEnabled(false); // TODO: disable keyboard?
 	}
 	
 	private void switchEndPlaceView() {
 		// Switch the startPlace autocomplete with a plain textview,
 		// so the user can't change it anymore.
 		endPlaceText.setVisibility(View.INVISIBLE);
 	    ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.viewSwitcherEndPlace);
 	    switcher.showNext(); //or switcher.showPrevious();
 	    TextView endPlaceTextView = (TextView) switcher.findViewById(R.id.endPlaceTextView);
 	    endPlaceTextView.setText("Ending place: " + trip.getEndPlace().getName());
 //	    startPlaceText.setEnabled(false); // TODO: disable keyboard?
 	}
 
 	public Place findNearestPlace(Location location) {
 		Place place = tripDB.getNearestPlace(location.getLatitude(), location.getLongitude());
 		return place;
 	}
 
 	public void showMessage(String message, int duration) {
 		Context context = getApplicationContext();
 		CharSequence text = (CharSequence) message;
 
 		Toast toast = Toast.makeText(context, text, duration);
 		toast.show();
 	}
 	
 	public void showShortMessage(String message) {
 		showMessage(message, Toast.LENGTH_SHORT);
 	}
 	
 	public void showLongMessage(String message) {
 		showMessage(message, Toast.LENGTH_LONG);
 	}
 	
 	public void setStatus(String status) {
 		TextView statusText = (TextView) findViewById(R.id.statusText);
 		statusText.setText(status);
 	}
 }
