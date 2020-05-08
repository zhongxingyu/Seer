 package stetson.CTF;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.http.client.methods.HttpPost;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.os.PowerManager;
 
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.Overlay;
 import com.google.android.maps.OverlayItem;
 
 public class GameCTF extends MapActivity {
 	
 	// Constant: How often should we wait between game update cycles?
 	public static final int GAME_UPDATE_DELAY = 2500;
 	
 	// Constants: Where should we center to on the next game update cycle?
 	public static final int CENTER_NONE = -1;
 	public static final int CENTER_ORIGIN = 0;
 	public static final int CENTER_SELF = 1;
 	public static final int CENTER_RED = 2;
 	public static final int CENTER_BLUE = -3;
 	
 	// Constant: What is the minimum accuracy (in meters) we should expect ?
 	public static final int MIN_ACCURACY = 40;
 	
 	// Data members
 	private MapView mapView;
 	private Handler gameHandler = new Handler();
 	private static final String TAG = "GameCTF";
 	
 	MapController mapController;
 	GameCTFOverlays itemizedoverlay;
 	OverlayItem overlayitem;
 	List<Overlay> mapOverlays;
 	
 	boolean isRunning = false;
 	int isCentering = CENTER_NONE;
 	
 	private Drawable drawable_unknown;
 	private Drawable drawable_red_owner;
 	private Drawable drawable_blue_owner;
 	private Drawable drawable_red_flag;
 	private Drawable drawable_blue_flag;
 	private Drawable drawable_red_player;
 	private Drawable drawable_blue_player;
 	Boundaries bounds;
 	
 	//Dim Wake Lock
 	private PowerManager.WakeLock ctfWakeLock;
 	
 	
 	/**
 	 * Called when the activity is first created.
 	 * Default behavior is NULL. Nothing is happening yet!
 	 * @param saved instance state
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		
 		Log.i(TAG, "Starting map activity...");
 		isRunning = true;
 		isCentering = CENTER_ORIGIN;
 		
 		// Restore a saved instance of the application
 		super.onCreate(savedInstanceState);
 		
 		// Make sure the user is actually in a game
 		if(CurrentUser.getGameId().equals("")) {
 			this.stopGame();
 			return;
 		}
 		
 		// Move back to the game selection panel
 		setContentView(R.layout.game);
 		
 		// Make sure gps is running at the right speed
 		CurrentUser.userLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), StetsonCTF.GPS_UPDATE_FREQUENCY_GAME);
 		
  		// Turns on built-in zoom controls
 		mapView = (MapView) findViewById(R.id.mapView);
 		mapController = mapView.getController();
 		mapView.setBuiltInZoomControls(true);
 		
 		// Setting up the overlay marker images
 		drawable_unknown = this.getResources().getDrawable(R.drawable.star);
 		drawable_unknown.setBounds(0, 0, drawable_unknown.getIntrinsicWidth(), drawable_unknown.getIntrinsicHeight());
 		
 		drawable_red_owner = this.getResources().getDrawable(R.drawable.person_red_owner);
 		drawable_red_owner.setBounds(0, 0, drawable_red_owner.getIntrinsicWidth(), drawable_red_owner.getIntrinsicHeight());
 		
 		drawable_blue_owner = this.getResources().getDrawable(R.drawable.person_blue_owner);
 		drawable_blue_owner.setBounds(0, 0, drawable_blue_owner.getIntrinsicWidth(), drawable_blue_owner.getIntrinsicHeight());
 		
 		drawable_red_flag = this.getResources().getDrawable(R.drawable.red_flag);
 		int redW = drawable_red_flag.getIntrinsicWidth();
 		int redH = drawable_red_flag.getIntrinsicHeight();
 		drawable_red_flag.setBounds(-redW / 2, -redH, redH / 2, 0);
 		
 		drawable_blue_flag = this.getResources().getDrawable(R.drawable.blue_flag);
 		int blueW = drawable_blue_flag.getIntrinsicWidth();
 		int blueH = drawable_blue_flag.getIntrinsicHeight();
 		drawable_blue_flag.setBounds(-blueW / 2, -blueH, blueH / 2, 0);
 		
 		drawable_red_player = this.getResources().getDrawable(R.drawable.person_red);
 		drawable_red_player .setBounds(0, 0, drawable_red_player.getIntrinsicWidth(), drawable_red_player.getIntrinsicHeight());
 		
 		drawable_blue_player = this.getResources().getDrawable(R.drawable.person_blue);
 		drawable_blue_player.setBounds(0, 0, drawable_blue_player.getIntrinsicWidth(), drawable_blue_player.getIntrinsicHeight());
 		
 		mapOverlays = mapView.getOverlays();
         itemizedoverlay = new GameCTFOverlays(drawable_unknown);
 				
 		// Clear game info
 		TextView text;
 		text = (TextView) findViewById(R.id.gameInfo_red);
 		text.setText(getString(R.string.game_info_loading));
 		text = (TextView) findViewById(R.id.gameInfo_blue);
 		text.setText("");
 		text = (TextView) findViewById(R.id.gameInfo_connection);
 		text.setText("");
 		bounds = new Boundaries();
 		
 		// Setup the wake lock
 		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 		ctfWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
 		
 		// Setup menu button listeners
 		buildMenuListeners();
 		
 		// Start game processor
 		gameHandler.postDelayed(gameProcess, GAME_UPDATE_DELAY);
 
 	}
 	
 	/**
 	 * When the activity is ended, we need to clear the users game and location.
 	 */
 	public void onDestroy() {
 		
 		// No more game, stop running
 		isRunning = false;
 		
 		super.onDestroy();
 		
 		Log.i(TAG, "Stopping Map Activity");
 		CurrentUser.setGameId("");
 		CurrentUser.setLocation(-1, -1);
 		CurrentUser.setAccuracy(-1);
 	}
 	
 	/**
 	 * The activity has regained focus. Acquire a wake lock.
 	 */
 	public void onResume() {
 	    super.onResume();
 	    ctfWakeLock.acquire();
 	}
 	
 	/**
 	 * The activity has lost focus. Release the wake lock.
 	 */
 	public void onPause(){
 	   super.onPause();
 	   ctfWakeLock.release();
 	}
 	
 	/**
 	 * Handles action when the Menu button is pressed.
 	 * Toggles the graphical menu.
 	 */
 	public boolean onCreateOptionsMenu(Menu x) {
 		
 		// Toggle the visibility of the menu
 		LinearLayout menu = (LinearLayout) this.findViewById(R.id.gameMenu);
 		if(menu.getVisibility() == LinearLayout.VISIBLE) {
 			menu.setVisibility(LinearLayout.GONE);
 		} else {
 			menu.setVisibility(LinearLayout.VISIBLE);
 		}
 		
 		// True means the native menu was launched successfully, so we must return false!
 		return false;
 	}
 	
 	
 
 	/**
 	 * Removes the user from the game and stops contact with the server.
 	 */
 	public void stopGame() {
 		isRunning = false;
 		this.finish();
 	}
 	
 	/**
 	 * Handles incoming menu clicks.
 	 */
 	private OnClickListener onMenuClick = new OnClickListener() {
 		public void onClick(View v) {
 			switch(v.getId()) {
 				
 				case R.id.menu_self:
 					isCentering = CENTER_SELF;
 					break;
 					
 				case R.id.menu_red_flag:
 					isCentering = CENTER_RED;
 					break;
 					
 				case R.id.menu_blue_flag:
 					isCentering = CENTER_BLUE;
 					break;
 					
 				case R.id.menu_scores:
 					// display score board
 					break;
 					
 				case R.id.menu_quit:
 					stopGame();
 					break;
 					
 			}
 		}
 	};
 	
 	/**
 	 * Adds an onClick listener to each of the menu items.
 	 */
 	private void buildMenuListeners() {
 		
 		findViewById(R.id.menu_self).setOnClickListener(onMenuClick);
 		findViewById(R.id.menu_red_flag).setOnClickListener(onMenuClick);
 		findViewById(R.id.menu_blue_flag).setOnClickListener(onMenuClick);
 		findViewById(R.id.menu_scores).setOnClickListener(onMenuClick);
 		findViewById(R.id.menu_quit).setOnClickListener(onMenuClick);
 
 	}
 	
 	/**
 	 * Returns false (required by MapActivity)
 	 * @return false
 	 */
 	protected boolean isRouteDisplayed() {
 		return false;
 	}	
 	
 	/**
 	 * Game processor. Runs the GameProcess task every GAME_UPDATE_DELAY (ms).
 	 */
 	private final Runnable gameProcess = new Runnable() {
 	    public void run() {
 
 	    	// Is the game still in progress?
 	    	if(isRunning) {
 	    		
 	    		// Only call for another execution if the previous one is finished (we don't want to overload on data)
 	    		Log.i(TAG, "Processor Status: " + new TaskGameProcess().getStatus());
 	    		new TaskGameProcess().execute();
 	    		
 	    		// Call for another execution later
 	    		gameHandler.postDelayed(this, GAME_UPDATE_DELAY);
 	    		
 	    	}
 	    }
 	    
 	};
 	
 	/**
      * The AsyncTask used for processing game updates.
      * (Generics: Params, Progress, Result)
      */
 	private class TaskGameProcess extends AsyncTask<Void, Void, JSONObject> {
 		
 		/**
 		 * Run as the work on another thread.
 		 * Sends a location update and grabs data from the server.
 		 */
 		protected JSONObject doInBackground(Void... params) {
 			
 			Log.i(TAG, "Grabbing game data...");
			
			/*
			 * Mark hasn't fixed this yet... Once he fixes the /location/ function, please update this to:
			 * HttpPost req = new HttpPost(StetsonCTF.SERVER_URL + "/location/");
			 */
 			String gameUrl = CurrentUser.getGameId().replaceAll(" ", "%20");
 			HttpPost req = new HttpPost(StetsonCTF.SERVER_URL + "/game/" + gameUrl);
 			CurrentUser.buildHttpParams(req, CurrentUser.UPDATE_PARAMS);
 			String data = Connections.sendFlatRequest(req);
 			try {
 				JSONObject jObject = new JSONObject(data);
 				return jObject;
 			} catch (JSONException e) {
 				Log.e(TAG, "Error parsing JSON.", e);
 			}
 			
 			// If we get here, we had problems with json.
 			return null;
 			
 		}
 		
 		/**
 		 * Run after execution on the UI thread.
 		 * Processes the game object retrieved from the worker thread.
 		 * If game is NULL then the game will be stopped and the activity terminated.
 		 */
 		protected void onPostExecute(final JSONObject gameObject) {
 			Log.i(TAG, "Processing game data.");
 			// Clear all overlays
 			mapOverlays.clear();
 			itemizedoverlay.clear();
 			
 			// Stop game if the game is null
 			if(gameObject == null) {
 				stopGame();
 				return;
 			}
 			
 			// For catching unforeseen errors
 			try {
 
 				// Run the processing functions...
 				processCentering(gameObject);
 				processPlayers(gameObject);
 				processGame(gameObject);
 				
 			    // Add map overlays
 				mapOverlays.add(bounds);
 			    mapOverlays.add(itemizedoverlay);
 			    mapView.invalidate();
 			    
 			} catch (Exception e) {
 				Log.e(TAG, "Critical Error!", e);
 			}
 			
 		}
 		
 		/**
 	     * If there is a request to center around the origin, do it.
 	     * @param jObject
 	     */
 	    private void processCentering(JSONObject jObject) {
 	    	
 	    	// Do we even have a request to center?
 	    	if(isCentering != CENTER_NONE) {
 	    		
 	    		Log.i(TAG, "Centering = " + isCentering);
 	    		
 	    		
 	    		int lati = 0; 
 	    		int loni = 0;
 	    		JSONObject subObject;
 	    		
 				try {
 					
 					if (isCentering == CENTER_SELF) {
 				    	lati = (int) (1E6 * CurrentUser.getLatitude());
 				    	loni = (int) (1E6 * CurrentUser.getLongitude());
 					} else {
 						
 						if(isCentering == CENTER_ORIGIN) {
 							subObject = jObject.getJSONObject("origin");
 						} else if (isCentering == CENTER_RED) {
 							subObject = jObject.getJSONObject("red_flag");
 						} else if (isCentering == CENTER_BLUE) {
 							subObject = jObject.getJSONObject("blue_flag");
 						} else {
 							// nothing to center on
 							isCentering = CENTER_NONE;
 							return;
 						}
 						
 				    	lati = (int) (1E6 * Double.parseDouble(subObject.getString("latitude")));
 				    	loni = (int) (1E6 * Double.parseDouble(subObject.getString("longitude")));
 					}
 					
 					Log.i(TAG, "Centering @ LAT " + lati + ", LONG " + loni);
 			    	GeoPoint origin = new GeoPoint(lati, loni);
 			    	mapView.getController().animateTo(origin);
 			    	
 			    	
 				} catch (JSONException e) {
 					Log.e(TAG, "Error centering on target!", e);
 				}
 				
 				isCentering = CENTER_NONE;
 	    	}
 	    	
 	    }
 	    
 	    /**
 	     * Handles other players.
 	     * @param jSubObj containing all players.
 	     */
 	    private void processPlayers(JSONObject jObject) {
 			try {
 	
 				// Loop through all players
 				JSONObject jSubObj = jObject.getJSONObject("players");
 				
 				
 				JSONObject player;
 				String playerKey;
 				
 				Iterator plrIterator = jSubObj.keys();
 			    while(plrIterator.hasNext()) {
 			    	
 			    	playerKey = (String) plrIterator .next();
 			    	player = jSubObj.getJSONObject(playerKey);
 			    	
 			    	/*
 			    	 * We should be using the following line below:
 			    	 * if(player.has("team") && !player.has("observer_mode")) {
 			    	 * 
 			    	 * But due to unhandled logic on the server, observer_mode should be
 			    	 * ignored for the time being. I've made a request for the server
 			    	 * to send a boolean to let us know that the game is active or in progress.
 			    	 * Currently, the server is attempt to rapidly assign teams and its causing
 			    	 * a bunch of problems with observer mode and teams.
 			    	 * 
 			    	 */
 			    	if(player.has("team")) {
 
 				    	int lati = (int) (1E6 * Double.parseDouble(player.getString("latitude")));
 				    	int loni = (int) (1E6 * Double.parseDouble(player.getString("longitude")));
 	
 						Log.i(TAG, "Adding player: " + player.getString("name") + " with  KEY=" + playerKey + " @ LAT " + player.getString("latitude") + ", LONG " + player.getString("longitude"));
 						GeoPoint marker = new GeoPoint(lati, loni);
 						OverlayItem overlayitem = new OverlayItem(marker, player.getString("name"), player.getString("name"));
 						
 						boolean hasFlag = player.getBoolean("has_flag");
 						boolean isCurrentPlayer = playerKey.equals(CurrentUser.getUID());
 						String team = player.getString("team");
 						
 						// Red team member has blue flag
 						if(team.equals("red") && hasFlag) {
 							overlayitem.setMarker(drawable_blue_flag);	
 							
 						// Blue team member has red flag
 						} else if(team.equals("blue") && hasFlag) {
 							overlayitem.setMarker(drawable_red_flag);	
 							
 						// Just a red player
 						} else if(team.equals("red")) {
 							
 							if(isCurrentPlayer) {
 								overlayitem.setMarker(drawable_red_owner);
 							} else {
 								overlayitem.setMarker(drawable_red_player);
 							}
 							
 						// Just a blue player
 						} else if(team.equals("blue")) {
 							
 							if(isCurrentPlayer) {
 								overlayitem.setMarker(drawable_blue_owner);
 							} else {
 								overlayitem.setMarker(drawable_blue_player);
 							}
 							
 						}
 						
 						// Done? Lets add it :D
 						itemizedoverlay.addOverlay(overlayitem);
 					}
 
 			    }
 
 			} catch (JSONException e) {
 				Log.e(TAG, "Error in gameProcess().processPlayers()", e);
 			}
 			
 	    }
 		    
 	    /**
 	     * Handles game data, such as flags and bounds.
 	     * @param jSubObj containing the entire game json object.
 	     */
 	    private void processGame(JSONObject jSubObj) {
 			try {
 				
 				JSONObject game = jSubObj;
 				
 				// Display game info bar details
 				TextView text;
 				text = (TextView) findViewById(R.id.gameInfo_red);
 				text.setText(getString(R.string.game_info_red) + game.getString("red_score"));
 				text = (TextView) findViewById(R.id.gameInfo_blue);
 				text.setText(getString(R.string.game_info_blue) + game.getString("blue_score"));
 				text = (TextView) findViewById(R.id.gameInfo_connection);
 				text.setText(getString(R.string.game_info_accuracy) + CurrentUser.getAccuracy());
 				
 				// Adding red flag
 				JSONObject red_flag = game.getJSONObject("red_flag");
 				int lat = (int) (1E6 * Double.parseDouble(red_flag.getString("latitude")));
 		    	int lon = (int) (1E6 * Double.parseDouble(red_flag.getString("longitude")));
 
 				GeoPoint red_marker = new GeoPoint(lat, lon);
 				OverlayItem red_overlayitem = new OverlayItem(red_marker, "red_flag", "red_flag");
 				red_overlayitem.setMarker(drawable_red_flag);
 				itemizedoverlay.addOverlay(red_overlayitem);
 				
 				Log.i(TAG, "Adding red_flag: " + red_flag.getString("latitude") + red_flag.getString("longitude"));
 				
 				// Adding blue flag
 				JSONObject blue_flag = game.getJSONObject("blue_flag");
 				lat = (int) (1E6 * Double.parseDouble(blue_flag.getString("latitude")));
 		    	lon = (int) (1E6 * Double.parseDouble(blue_flag.getString("longitude")));
 
 				GeoPoint blue_marker = new GeoPoint(lat, lon);
 				OverlayItem blue_overlayitem = new OverlayItem(blue_marker, "blue_flag", "blue_flag");
 				blue_overlayitem.setMarker(drawable_blue_flag);
 				itemizedoverlay.addOverlay(blue_overlayitem);
 				
 				Log.i(TAG, "Adding blue_flag: " + red_flag.getString("latitude") + red_flag.getString("longitude"));
 				
 				
 				// Adding boundaries
 				
 				// Get Red boundaries
 				JSONObject redBounds = game.getJSONObject("red_bounds");
 				JSONObject redTopLeft = redBounds.getJSONObject("top_left");
 				lat = (int) (1E6 * Double.parseDouble(redTopLeft.getString("latitude")));
 		    	lon = (int) (1E6 * Double.parseDouble(redTopLeft.getString("longitude")));
 		    	GeoPoint redTopLeftBoundary = new GeoPoint(lat, lon);
 		    	JSONObject redBottomRight = redBounds.getJSONObject("bottom_right");
 				lat = (int) (1E6 * Double.parseDouble(redBottomRight.getString("latitude")));
 		    	lon = (int) (1E6 * Double.parseDouble(redBottomRight.getString("longitude")));
 		    	GeoPoint redBottomRightBoundary = new GeoPoint(lat, lon);
 		    	bounds.setRedBounds(redTopLeftBoundary, redBottomRightBoundary);
 		    	
 		    	// Get blue  boundaries
 		    	JSONObject blueBounds = game.getJSONObject("blue_bounds");
 				JSONObject blueTopLeft = blueBounds.getJSONObject("top_left");
 				lat = (int) (1E6 * Double.parseDouble(blueTopLeft.getString("latitude")));
 		    	lon = (int) (1E6 * Double.parseDouble(blueTopLeft.getString("longitude")));
 		    	GeoPoint blueTopLeftBoundary = new GeoPoint(lat, lon);
 		    	JSONObject blueBottomRight = blueBounds.getJSONObject("bottom_right");
 				lat = (int) (1E6 * Double.parseDouble(blueBottomRight.getString("latitude")));
 		    	lon = (int) (1E6 * Double.parseDouble(blueBottomRight.getString("longitude")));
 		    	GeoPoint blueBottomRightBoundary = new GeoPoint(lat, lon);
 		    	bounds.setBlueBounds(blueTopLeftBoundary, blueBottomRightBoundary);
 		    	
 				
 			    
 			} catch (JSONException e) {
 				Log.e(TAG, "Error in gameProcess().processGame()", e);
 			}
 	    }
 	}
 }
