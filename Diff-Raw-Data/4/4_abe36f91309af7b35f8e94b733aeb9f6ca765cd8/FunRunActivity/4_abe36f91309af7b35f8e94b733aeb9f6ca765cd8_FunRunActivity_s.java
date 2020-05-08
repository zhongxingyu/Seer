 //Copyright (c) 2011 Charles L. Capps
 //Released under MIT License
 
 package xanthanov.droid.funrun;
 
 import xanthanov.droid.gplace.*;
 import xanthanov.droid.xantools.*;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface; 
 import android.content.IntentFilter; 
 
 import android.os.Bundle;
 import android.view.View;
 import android.view.animation.Animation; 
 import android.view.animation.AlphaAnimation;
 import android.view.KeyEvent; 
 import android.widget.TextView;
 import android.widget.Button; 
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.Toast; 
 import android.content.Context; 
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.content.Intent;
 import android.content.SharedPreferences; 
 import android.content.res.Resources; 
 import android.app.PendingIntent;
 import android.text.Html; 
 import android.text.Spanned; 
 import android.media.AudioManager;
 import android.view.MenuInflater; 
 import android.view.Menu; 
 import android.view.MenuItem; 
 
 import android.speech.tts.TextToSpeech; 
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random; 
 import java.util.HashMap; 
 import java.sql.SQLException; 
 
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapView;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MyLocationOverlay; 
 import com.google.android.maps.Overlay; 
 
 /**
 * <h3>Activity for when user is actually running.</h3>
 *
 *<h3>Things this class does:</h3>
 *<ul>
 *<li>Displays route the user runs as they run.</li>
 *<li>Speaks walking directions. User can press &quot;volume up&quot; button to repeat them. (This fact is spoken aloud)</li>
 *<li>Adds a point to this route every PATH_INCREMENT_METERS meters (public static constant, see below).</li>
 *<li>Detects when the user is within ACCEPT_RADIUS_METERS meters of the end of a step for the Google directions.</li>
 *<li>Passes index of completed step to the StepCompleteActivity using Intent &quot;extras&quot;</li>
 *<li>TODO: Tweak accept radius. Need balance between accuracy, and guaranteeing it's actually accepted even if GPS is inaccurate.</li>
 *<li>Also TODO: Make a preferences screen allowing the user to choose the accept radius, path increment</li>
 *<li>Maybe try to integrate wifi, but from my experience it's way too inaccurate for this.</li>
 *</ul>
 *
 *@author Charles L. Capps
 *@version 0.9b
 *@see xanthanov.droid.xantools.DroidTTS
 *
 **/
 
 public class FunRunActivity extends MapActivity
 {
 	//***********VIEW OBJECTS DEFINED IN XML**********************
 	private LinearLayout parentContainer; 
 	private Button centerOnMeButton; 
 	private MapView myMap;
 	private Button zoomInButton;
 	private Button zoomOutButton;
 	private Button zoomToRouteButton;
 	private TextView directionsTextView; 
 	private TextView chosenPlaceTextView; 
 	private TextView copyrightTextView; 
 	private RelativeLayout mapRelLayout; 
 	//*******************OTHER OBJECTS****************************
 	private FunRunApplication funRunApp; 
 	private MyLocationOverlay myLocOverlay;
 	private MapController myMapController; 
 	private LocationListener myGpsListener; 
 	private LocationListener myNetworkListener; 
 	private FunRunOverlay myFunRunOverlay; 
 
 	private GeoPoint lastKnownGeoPoint; 
 	private Location bestLocation; 
 
 	private GoogleDirections runDirections; 
 	private GoogleLeg currentLeg; 
 	private GoogleStep currentStep; 
 	private GooglePlace runPlace;  
 	private DroidLoc droidLoc; 
 
 	private TextToSpeech myTts; 
 	private DroidTTS ttsTools; 
 	private AudioManager audioMan;  
 
 	private Spanned htmlInstructions;
 	private String speakingInstructions;  
 	private boolean firstSpeechCompleted; 
 	private boolean SPEAK_DIRECTIONS; 
 	//*****************CONSTANTS**********************************
 	private final static int DEFAULT_ZOOM = 15; 
 	private final static int DEFAULT_RADIUS_METERS = 1000;
 	public final static int MAX_RADIUS_METERS = 4000; 
 	public final static int MIN_RADIUS_METERS = 50; 
 
 	private float ACCEPT_RADIUS_METERS; 
 	private float PATH_INCREMENT_METERS = 10.0f; 
 
 	private float MIN_DISTANCE_TO_SAVE; 
 
 	public final static String STEP_EXTRA = "step_no";
 	//************************************************************
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.runlayout);

 		//Get the Application object and its global data
 		funRunApp = (FunRunApplication) this.getApplicationContext(); 
 
 		//Get audio manager
 		audioMan = (AudioManager)getSystemService(Context.AUDIO_SERVICE); 
 		//***************GET VIEWS DEFINED IN XML***********************
 		myMap = (MapView) findViewById(R.id.run_myMap); 
 		centerOnMeButton = (Button) findViewById(R.id.run_buttonCenterOnMe); 
 		zoomToRouteButton = (Button) findViewById(R.id.run_buttonZoomToRoute); 
 		zoomInButton = (Button) findViewById(R.id.run_buttonZoomIn); 
 		zoomOutButton = (Button) findViewById(R.id.run_buttonZoomOut); 
 		directionsTextView = (TextView) findViewById(R.id.directionsTextView); 
 		chosenPlaceTextView = (TextView) findViewById(R.id.chosenPlaceTextView); 
 		copyrightTextView = (TextView) findViewById(R.id.copyrightTextView); 
 		parentContainer = (LinearLayout) findViewById(R.id.run_parentContainer); 
 		mapRelLayout = (RelativeLayout) findViewById(R.id.run_relLayout); 
 		//******************DEFINE OTHER OBJECTS**************************
 		droidLoc = new DroidLoc(this); 
 		myLocOverlay = new MyLocationOverlay(this, myMap); 
 		myMapController = myMap.getController(); 
 		runDirections = funRunApp.getRunDirections(); 
 		//Initialize currentStep to the first step in the last leg of the GoogleDirections object
 		//As the runner arrives at destinations, new legs will be added
 		currentLeg = runDirections.lastLeg(); 
 		currentStep = currentLeg.get(0);  
 		runPlace = currentLeg.getLegDestination();
 		//Store current step in the Application object to pass between activities
 		funRunApp.setCurrentStep(currentStep); 
 
 		//Get the HTML directions from the raw string and set the text view
 		htmlInstructions = Html.fromHtml(currentStep.getHtmlInstructions().trim());		
 		Spanned txt = android.text.Html.fromHtml("Running to <b>" + runPlace.getName() + "</b>"); 		
 		chosenPlaceTextView.setText(txt); 
 		updateDirectionsTextView(); 
 
 		String copyrightPlusWarnings = currentLeg.getCopyright() + "<br/>" + currentLeg.getWarnings(); 
 		Spanned copyrightSpanned = android.text.Html.fromHtml(copyrightPlusWarnings); 
 		copyrightTextView.setText(copyrightSpanned); 
 
 		//******************CALL SETUP METHODS****************************
 		setupMap(); 
 		setupCenterOnMeButton(); 
 		setupZoomToRouteButton(); 
 		setupZoomButtons(); 
 		myMap.preLoad(); 
 
 		long theTime = System.currentTimeMillis(); 
 
 		//onCreate() is called when a new leg starts, so set the start time for the leg and the first step
 		currentLeg.setStartTime(theTime); 
 		currentLeg.get(0).setStartTime(theTime); 
 
 		zoomToRoute(); 
 
 		myTts = funRunApp.getTextToSpeech(); 
 		ttsTools = new DroidTTS(); 
 		firstSpeechCompleted = false; 
 
 		myTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
 			@Override
 			public void onUtteranceCompleted(String id) {
 				firstSpeechCompleted = true; 
 			}
 		});
     }
 
 	private void grabPrefs() {
 		Resources res = getResources(); 
 
 		SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this); 
 
 		String default_accept_radius = res.getString(R.string.default_accept_radius); 
 		String default_min_run = res.getString(R.string.default_min_run); 
 		String default_path_segment = res.getString(R.string.default_path_segment); 
 		String default_speak_directions = res.getString(R.string.default_speak_directions); 
 
 		String accept_radius_key = res.getString(R.string.accept_radius_pref); 
 		String min_run_key = res.getString(R.string.min_run_pref); 
 		String path_segment_key = res.getString(R.string.path_segment_pref); 
 		String speak_directions_key = res.getString(R.string.speak_directions_pref); 
 
 		ACCEPT_RADIUS_METERS = Float.parseFloat(prefs.getString(accept_radius_key, default_accept_radius)); 
 		MIN_DISTANCE_TO_SAVE = Float.parseFloat(prefs.getString(min_run_key, default_min_run)); 
 		PATH_INCREMENT_METERS = Float.parseFloat(prefs.getString(path_segment_key, default_path_segment)); 
 		SPEAK_DIRECTIONS = Boolean.parseBoolean(prefs.getString(speak_directions_key, default_speak_directions)); 
 
 	}
 	
 	@Override
 	public boolean isRouteDisplayed() {
 		return true;
 	}
 
 	@Override
 	public boolean onKeyDown( int keycode, KeyEvent e) {		
 	//	super.onKeyDown(keycode, e); 
 
 		if (keycode == KeyEvent.KEYCODE_VOLUME_UP) {
 			if (firstSpeechCompleted && SPEAK_DIRECTIONS && funRunApp.isTtsReady() && (!myTts.isSpeaking())) { //If a TTS isn't already playing, say the directions again. This avoids the annoying-as-hell possibility of spamming the TTS
 				speakDirections(); 	
 			}
 			audioMan.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI); 
 			return true; 
 		}
 		else if (keycode == KeyEvent.KEYCODE_VOLUME_DOWN) {
 			audioMan.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI); 
 			return true; 
 		}
 		else if (keycode == KeyEvent.KEYCODE_BACK) {
 			endActivity(); 
 			return true; 
 		}
 
 		return false; 
 	}
 
 	public void endActivity() {
 		String msg; 
 		double distanceRan = currentLeg.getActualDistanceRan(); 
 		
 		if (distanceRan < MIN_DISTANCE_TO_SAVE) {
 			msg = "Your progress won't be saved. You ran less than " + (int)MIN_DISTANCE_TO_SAVE + " meters.";
 		}
 		else {
 			msg = "Your progress will be saved, since you ran " + new java.text.DecimalFormat("#.##").format(distanceRan) + " meters.";  
 		}
 
 		DroidDialogs.showPopup(this, false, "Choose new place?", 
 								"Stop running to " + runPlace.getName() + "?\n\n" + msg, 
 								"Okay", "No way!", 
 								new DialogInterface.OnClickListener() {
 									@Override
 									public void onClick(DialogInterface dialog, int id) {
 										dialog.dismiss(); 
 										FunRunActivity.this.finish(); 
 									}
 								},	
 								new DialogInterface.OnClickListener() {
 									@Override
 									public void onClick(DialogInterface dialog, int id) {
 										dialog.dismiss(); 
 									}
 								}	
 								); 
 
 	}
 

 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus); 
 
 		if (hasFocus) {
 			myFunRunOverlay.startRunAnimation(); 
 		}
 
 	}
 
 	@Override 
 	protected void onStart() {
 		super.onStart();
 
 		//******************GET PREFERENCES*****************************
 		grabPrefs(); 
 
 		firstSpeechCompleted = false; 
 
 		//See if current step was updated by StepCompleteActivity
 		currentStep = ((FunRunApplication)getApplication()).getCurrentStep(); 
 		if (currentStep != null) {
 			htmlInstructions = Html.fromHtml(currentStep.getHtmlInstructions().trim());		
 			speakingInstructions = ttsTools.expandDirectionsString(htmlInstructions.toString()); 
 			updateDirectionsTextView(); 
 
 			if (SPEAK_DIRECTIONS && funRunApp.isTtsReady()) { //Expand abbreviations so it speaks properly and play it
 				HashMap<String,String> params = new HashMap<String,String> (); 
 				params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FIRST_SPEECH"); 
 				myTts.speak(speakingInstructions, TextToSpeech.QUEUE_FLUSH, null); 
 				myTts.playSilence(300, TextToSpeech.QUEUE_ADD, null); 
 				myTts.speak("Press volume up to hear directions again.", TextToSpeech.QUEUE_ADD, params); 
 			}
 
 			setupLocListener(); //Instantiate new location listeners 
 
 			//Start up compass and location updates
 			myLocOverlay.enableCompass(); 	
 			droidLoc.getLocManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, FunRunApplication.MIN_GPS_UPDATE_TIME_MS, 0, myGpsListener);
 			droidLoc.getLocManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, FunRunApplication.MIN_GPS_UPDATE_TIME_MS, 0, myNetworkListener);
 
 			//Force update of lastKnownGeoPoint
 			bestLocation = droidLoc.getBestLocation(bestLocation); 
 			lastKnownGeoPoint = DroidLoc.locationToGeoPoint(bestLocation); 
 
 			//Update visuals so it doesn't show you in the wrong place
 			myFunRunOverlay.updateCurrentLocation(lastKnownGeoPoint); 
 			myMap.invalidate(); 
 		}
 		else { //current step was null, indicating the user finished running to a place.
 			//Go choose another place	
 			finish(); 
 		}
 	}
 
 	private void speakDirections() {
 		myTts.speak(speakingInstructions, TextToSpeech.QUEUE_FLUSH, null); 
 	}
 	
 	@Override 
 	protected void onStop() {
 		super.onStop(); 
 		droidLoc.getLocManager().removeUpdates(myGpsListener); 
 		droidLoc.getLocManager().removeUpdates(myNetworkListener); 
 		myLocOverlay.disableCompass();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy(); 
 
 		//Remove this leg if the runner didn't go any distance. 
 		if (currentLeg.getActualDistanceRan() < MIN_DISTANCE_TO_SAVE) {
 			runDirections.remove(currentLeg); 
 			(Toast.makeText(this, "You ran too little. Progress not saved.", 5)).show(); 
 			
 		}
 		else {
 			(Toast.makeText(this, currentLeg.getLegPoints() + " points earned!", 5)).show(); 
 			//Here: write directions to DB. If this is too slow, even with a transaction, then will change implementation to instead write as they run, 
 			//and delete if they don't run enough
 			try {
 				funRunApp.getDbWriter().insertLeg(currentLeg); 
 			}
 			catch (SQLException e) {
 				System.err.println("Error writing leg to SQLite DB: "); 
 				e.printStackTrace(); 
 			}
 		}
 	}
 
 	private void updateDirectionsTextView() {
 		directionsTextView.setText(htmlInstructions); 	
 	}
 
 	private void setupLocListener() {
 		myGpsListener = new MyLocListener(); 
 		myNetworkListener = new MyLocListener(); 
 	}
 
 	private void updateLocation(Location l) {
 		if (l==null) {
 			return;
 		}
 
 		bestLocation = droidLoc.compareLocations(bestLocation, l); //Compare new location to previous best, and return the best one
 
 		GeoPoint newGeoPoint = DroidLoc.locationToGeoPoint(bestLocation); 
 
 		lastKnownGeoPoint = newGeoPoint; 
 
 		//Update location and invalidate map to redraw
 		myFunRunOverlay.updateCurrentLocation(lastKnownGeoPoint); 
 		myMap.invalidate(); 
 
 		double[] latLng = null; 
 		if (bestLocation != null) {
 			latLng = new double[] {bestLocation.getLatitude(), bestLocation.getLongitude()}; 
 		}
 
 		//Check if we've finished a step
 		checkForCompleteSteps(); 
 
 		//Just for fun! Animate the title bar.
 		//As a side effect, this indicates whether GPS is working or not (^o ^o)
 		animateTitleBar(); 
 
 		//Add a GeoPoint to the actualPath in the currentLeg, provided the previous point is far enough away from the current point. 
 		//This obviously is intended to prevent 
 		if (latLng != null) {
 			addToActualPath(latLng);
 		} 
 	}
 
 	private void checkForCompleteSteps() {
 		float distance[] = new float[1]; 
 		GoogleStep step = null;
 		double latLng[] = DroidLoc.geoPointToDegrees(lastKnownGeoPoint); 
 		for (int i = currentLeg.getMaxStepCompleted() + 1; i < currentLeg.size(); i++) {
 			step = currentLeg.get(i); 
 			Location.distanceBetween(step.getEndPoint()[0], step.getEndPoint()[1], latLng[0], latLng[1], distance); 
 			if (distance[0] <= ACCEPT_RADIUS_METERS) {
 				currentLeg.setMaxStepCompleted(i); 
 				long time = System.currentTimeMillis(); 
 				currentLeg.setEndTime(time); 
 				currentLeg.get(i).setEndTime(time); 
 				Intent completeStepIntent = new Intent(this, StepCompleteActivity.class); 
 				completeStepIntent.putExtra(STEP_EXTRA, i); 
 				startActivity(completeStepIntent);
 				break;  
 			} 
 		}
 	} 
 
 	private void addToActualPath(double[] latLng) {
 		List<LatLng> actualPath = currentLeg.getActualPath();
 		int size = actualPath.size(); 
 		if (size == 0) {
 			actualPath.add(new LatLng(latLng)); 
 			return; 
 		}
 		LatLng lastPathPoint = actualPath.get(size - 1); 
 		float[] distance = new float[1]; 
 
 		android.location.Location.distanceBetween(lastPathPoint.lat, lastPathPoint.lng, latLng[0], latLng[1], distance);
 
 		if (distance[0] >= PATH_INCREMENT_METERS) {
 			currentLeg.addToActualPath(new LatLng(latLng)); 
 		}
 	}
 
 	private void animateTitleBar() {
 		String txt = (String)(this.getTitle());  
 		int len = txt.length(); 
 		char lastChar = txt.charAt(len - 1); 
 		txt = (String.valueOf(lastChar) + txt).substring(0, len); 
 
 		setTitle(txt); 
 		
 	}
 
 	private void setupMap() {
 		myFunRunOverlay = new FunRunOverlay(myMap, null, true, false, true, mapRelLayout);
 		myFunRunOverlay.updateCurrentDirections(runDirections); 
 		myMap.getOverlays().add(myLocOverlay); 
 		myMap.getOverlays().add(myFunRunOverlay); 
 		
 		myMap.invalidate(); 
 	}
 
 	private void setupCenterOnMeButton() {
 		final MapController mc = myMapController; 
 		final GeoPoint loc = lastKnownGeoPoint; 
 
 		Animation animation = new AlphaAnimation(1.0f, 0.7f);
 		animation.setFillAfter(true);
 		centerOnMeButton.startAnimation(animation);
 
 		centerOnMeButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					centerOnMe(); 
 				}
 			});	
 	}
 
 	private void centerOnMe() {
 		if (lastKnownGeoPoint == null) {
 			return; 
 		}
 		else {
 			myMapController.animateTo(lastKnownGeoPoint); 
 		}
 		
 	}
 
 	private void setupZoomButtons() {
 		Animation animation = new AlphaAnimation(1.0f, 0.7f);
 		animation.setFillAfter(true);
 		zoomInButton.startAnimation(animation);
 		zoomOutButton.startAnimation(animation);
 
 		zoomInButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					myMapController.zoomIn(); 
 				}
 			});	
 		zoomOutButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					myMapController.zoomOut(); 
 				}
 			});	
 	}
 
 	private void setupZoomToRouteButton() {
 
 		Animation animation = new AlphaAnimation(1.0f, 0.7f);
 		animation.setFillAfter(true);
 		zoomToRouteButton.startAnimation(animation);
 
 		zoomToRouteButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					zoomToRoute(); 
 				}
 			});	
 	} 
 
 	private void zoomToRoute() {
 		final GeoPoint neBound = DroidLoc.degreesToGeoPoint(currentLeg.getNeBound()); 
 		final GeoPoint swBound = DroidLoc.degreesToGeoPoint(currentLeg.getSwBound()); 
 		final GeoPoint midPoint = new GeoPoint( (neBound.getLatitudeE6() + swBound.getLatitudeE6())/2, (neBound.getLongitudeE6() + swBound.getLongitudeE6())/2);
 		final int latSpan = Math.abs(neBound.getLatitudeE6() - swBound.getLatitudeE6()); 
 		final int lngSpan = Math.abs(neBound.getLongitudeE6() - swBound.getLongitudeE6()); 
 
 		myMapController.animateTo(midPoint); 
 		myMapController.zoomToSpan(latSpan, lngSpan); 
 	}
 
 	class MyLocListener implements LocationListener {
 
 		@Override
 		public void onLocationChanged(Location l) {
 			FunRunActivity.this.updateLocation(l); 
 		}
 
 		@Override
 		public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 		@Override
 		public void onProviderEnabled(String provider) {}
 
 		@Override
 		public void onProviderDisabled(String provider) {}		
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.funrun_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.menu_preferences:
 			Intent i = new Intent(this, xanthanov.droid.funrun.pref.FunRunPref.class); 
 			startActivity(i); 
 			return true;
 		case R.id.menu_back:
 			endActivity();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 }
 
