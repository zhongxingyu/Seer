 package stetson.CTF;
 
 import java.util.ArrayList;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class StetsonCTF extends Activity {
 	
 	// Constants: To be used across entire application
 	public static final String TAG = "StetsonCTF";
 	public static final String SERVER_URL = "http://ctf.no.de";
 	
 	// Constants: GPS Update Frequency
 	public static final int GPS_UPDATE_FREQUENCY_GAME = 3000;
 	public static final int GPS_UPDATE_FREQUENCY_INTRO = 10000;
 	public static final int GPS_UPDATE_FREQUENCY_BACKGROUND = 60000;
 	
 	// Constants: GPS Update Threshold
 	public static final int GPS_UPDATE_DISTANCE_GAME = 0;
 	public static final int GPS_UPDATE_DISTANCE_INTRO = 1;
 	public static final int GPS_UPDATE_DISTANCE_BACKGROUND = 10;
 	public static boolean firstStart = true;
 	// Data Members
 
 	/**
 	 * Called when the activity is first created.
 	 * @param saved instance state
 	 */
 	public void onCreate(Bundle savedInstanceState) {
 		
 		Log.i(TAG, "Starting activity...");
 		
 		// Restore a saved instance of the application
 		super.onCreate(savedInstanceState);
 		
 		// Move back to the game selection panel
 		setContentView(R.layout.intro);
 		// Build listeners
 		buildListeners();
 		
 		Log.i(TAG, "Activity ready!");
 	}
 	
 	/**
 	 * Start GPS and rebuild games list.
 	 */
 	public void onResume() {
 		
 		super.onResume();
 		
 		// Start GPS
 		CurrentUser.userLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), GPS_UPDATE_FREQUENCY_INTRO);
 		
 		// Build a new games list
 		buildGamesList();
 	
 	}
 	
 	/**
 	 * Slow down GPS updates a lot when the application is in the background.
 	 */
 	public void onPause() {
 
 		super.onPause();
 		
 		// Stop GPS
 		CurrentUser.stopLocation((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
 		
 	}
 	
 	/**
 	 * Connects the view components to listeners
 	 */
 	private void buildListeners() {
 		
 		Log.i(TAG, "Prepare listeners.");
 		
 		// Create a new game
 		final Button newGameButton = (Button) findViewById(R.id.newgame_button);
 		newGameButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				
 				// Do nothing if we have no loction (still loading)
 				if(!CurrentUser.hasLocation()) {
 					return;
 				}
 				
 				// Get name
 				EditText et = (EditText) findViewById(R.id.name_text);
 				CurrentUser.setName(et.getText().toString());
 				
 				// Create the new game :)
 				joinGame(CurrentUser.getName(), "");
 				
 			}
 		});
 		
 		// Join a game
 		final Button joinGameButton = (Button) findViewById(R.id.joingame_button);
 		joinGameButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				
 				// Do nothing if we have no loction (still loading)
 				if(!CurrentUser.hasLocation()) {
 					return;
 				}
 				
 				RadioGroup gamesGroup = (RadioGroup) findViewById(R.id.games_list_group);
 				String game = "";
 
 				int selected = gamesGroup.getCheckedRadioButtonId();
 				
 				// Join the specified game
 				if(selected > -1) {
 					RadioButton rb = (RadioButton) findViewById(selected);
 					game = (String) rb.getText();
 					CurrentUser.setGameId(game);
 					
 					// Get name
 					EditText et = (EditText) findViewById(R.id.name_text);
 					CurrentUser.setName(et.getText().toString());
 					
 					// Create the new game :)
 					joinGame(CurrentUser.getName(), CurrentUser.getGameId());
 					
 				// No game selected, notify user
 				} else {
 					Toast.makeText(view.getContext(), R.string.no_game_selected, Toast.LENGTH_SHORT).show();
 				}
 
 			}
 		});
 		
 	}
 	
 	/**
 	 * Retrieves and displays a new games list
 	 */
 	private void buildGamesList() {
 		
 		Log.i(TAG, "(UI) Building games list.");
 		new TaskGenerateGamesList().execute();
 		
 	}
 
 	/**
 	 * Joins or creates a new game. If game is empty, then a new game will be created.
 	 * @param name
 	 * @param game
 	 */
     protected void joinGame(String name, String game) {
     	Log.i(TAG, "(UI) joinGame(" + name + ", " + game + ")");
     	new TaskJoinGame().execute(name, game);
     }
     
     /**
      * Sets the user's name and generates a new UID.
      * @param name
      */
     protected void updateUser(String name) {
     	
     	// New name
     	CurrentUser.setName(name);
     	
 		// Generate a new uid
 		String uid = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
 		while(uid.contains("x")) 
 		uid = uid.replaceFirst("x", Long.toHexString(Math.round(Math.random() * 16.0)));
 		uid = uid.toUpperCase();
 		CurrentUser.setUID(uid);
     }
     
     
     /**
      * The AsyncTask used for generating a new list of games.
      * (Generics: Params, Progress, Result)
      */
     private class TaskGenerateGamesList extends AsyncTask<Void, String, ArrayList<String>> {
 		
     	protected static final long GPS_CHECK_PAUSE = 500;
 		private Context mContext = StetsonCTF.this;
 		private RadioGroup gamesGroup;
 		
 		/**
 		 * Run before execution on the UI thread.
 		 * Remove all children from group view and post a loading message.
 		 */
 		protected void onPreExecute() {
 			
 			if(StetsonCTF.firstStart)
 			{
 				Intent titleScreen = new Intent(mContext, Title.class);
 				startActivity(titleScreen);
 				StetsonCTF.firstStart = false;
 			}
 			gamesGroup = (RadioGroup) findViewById(R.id.games_list_group);
 			gamesGroup.removeAllViews();
 			
 			
 		}
 		
 		/**
 		 * Runs every time publicProgress() is called.
 		 * Clears the gamesGroup view and adds a message with the progress text.
 		 */
 	     protected void onProgressUpdate(String... progress) {
 	    	 gamesGroup.removeAllViews();
 	    	 TextView text = new TextView(mContext);
 	    	 text.setText(progress[0]);
 	    	 gamesGroup.addView(text);
 	     }
 	     
 		/**
 		 * Run after execution on the UI thread.
 		 * Clears the gameGroup view and adds a list of games to it.
 		 */
 		protected void onPostExecute(final ArrayList<String> response) {
 			gamesGroup.removeAllViews();
 			
 			// Something bad happened, unknown error :o
 			if(response == null) {
 		    	 TextView text = new TextView(mContext);
 		    	 text.setText(R.string.no_games_error);
 		    	 gamesGroup.addView(text);
 		    	 
 		    // We have no games :(
 			} else if(response.isEmpty()) {
 		    	 TextView text = new TextView(mContext);
 		    	 text.setText(R.string.no_games);
 		    	 gamesGroup.addView(text);
 		    	 
 		    // We have some games! Add them to the list :)
 			} else {
 				for(int i = 0; i < response.size(); i++) {
 					RadioButton rb;
 					Log.i(TAG, "Adding game to view (" + response.get(i) + ")");
 					rb = new RadioButton(mContext);
 					rb.setText(response.get(i));
 					gamesGroup.addView(rb);
 				}
 			}
 			
 		}
 		
 		/**
 		 * Run as the work on another thread.
 		 */
 		protected ArrayList<String> doInBackground(Void... params) {
 			
 			ArrayList<String> gamesList = new ArrayList<String>();
 			
 			publishProgress(mContext.getString(R.string.loading_location));
 			
 			// We might still be waiting for a location...
 			while(!CurrentUser.hasLocation()) {
 				try {
 					Thread.sleep(GPS_CHECK_PAUSE);
 				} catch (InterruptedException e) {
 					Log.e(TAG, "Can't sleep :(", e);
 				}
 			}
 			
 			publishProgress(mContext.getString(R.string.loading_games));
 			
 			// Sweet, we have a location, lets grab a list of games
 			HttpGet req = new HttpGet(SERVER_URL + "/game/?" + CurrentUser.buildQueryParams());
 			String data = Connections.sendRequest(req);
 			try {
 				
 				JSONObject games = new JSONObject(data);
 				if (games.has("games")) {
 					
 					// Add all the games to a list
 					JSONArray list = games.getJSONArray("games");
 					for(int n = 0; n < list.length(); n++) {
 						gamesList.add(list.getString(n));
 					}
 					
 					// Ok, that's all, return the games list
 					return gamesList;
 					
 				} else {
 					Log.e(TAG, "Unexpected Server Response: " + data);
 				}
 			} catch (JSONException e) {
 				Log.e(TAG, "Error parsing JSON.", e);
 			}
 			
 			// Everything fell through, meaning bad stuff happened, check LogCat :(
 			return null;
 		}
     }
     
     /**
      * The AsyncTask used for creating and joining a game.
      * (Generics: Params, Progress, Result)
      */
 	private class TaskJoinGame extends AsyncTask<String, Void, String> {
 		
 		private final static String GOOD_RESPONSE = "OK";
 		private Context mContext = StetsonCTF.this;
 		private ProgressDialog dialog;
 		
 		/**
 		 * Run before execution on the UI thread.
 		 */
 		protected void onPreExecute() {
 			dialog = ProgressDialog.show(mContext, "Joining Game", "Please wait...", true);
 		}
 		
 		/**
 		 * Run after execution on the UI thread.
 		 * If the response string is equal to GOOD_RESPONSE then the game activity
 		 * is started. If not, a toast showing the error message is sent.
 		 */
 		protected void onPostExecute(final String response) {
 			
 			dialog.hide();
 			
 			if(response.equals(GOOD_RESPONSE)) {
 			    Intent i = new Intent(mContext, GameCTF.class);
 			    startActivity(i);
 			} else {
 				Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
 			}
 			
 		}
 		
 		/**
 		 * Run as the work on another thread.
 		 */
 		protected String doInBackground(final String... params) {
 			
 			// More friendly parameters :)
 			String name = params[0];
 			String game = params[1];
 			Log.i(TAG, "(WORKER) joinGame(" + name + ", " + game + ")");
 			
 			// Build a UID
 			updateUser(name);
 			
 			// If a game name wasn't given, then we need to make a game.
 			if(game.equals("")) {
 				
 				CurrentUser.setGameId(CurrentUser.getName());
 				HttpPost hp = new HttpPost(SERVER_URL + "/game/");
 				CurrentUser.buildHttpParams(hp, CurrentUser.CREATE_PARAMS);
 				String data = Connections.sendRequest(hp);
 
 				try {
 					JSONObject response = new JSONObject(data);
 					Log.i(TAG, "(WORKER) create game response: " + data);
 					if(response.has("response") && !response.getString("response").equals(GOOD_RESPONSE)) {
 						return "Unexpected server response #1";
 					} else if (response.has("error")) {
 						return "Server Error: " + response.get("error");
 					}
 				} catch (JSONException e) {
 					Log.e(TAG, "Error parsing JSON.", e);
 					return "Unexpected server response #2";
 				}
 				
 			}
 			
 			// If a game was created, then it was a success at this point! Now we must join the game.
 			String gameUrl = CurrentUser.getGameId().replaceAll(" ", "%20");
 			HttpPost hp = new HttpPost(SERVER_URL + "/game/" + gameUrl);
 			CurrentUser.buildHttpParams(hp, CurrentUser.JOIN_PARAMS);
 			String data = Connections.sendRequest(hp);
 			
 			try {
 				JSONObject jsonGame = new JSONObject(data);
 				if(jsonGame.has("error")) {
 					return "Server Error: " + jsonGame.get("error");
 				}
 			} catch (JSONException e) {
 				Log.e(TAG, "Error parsing JSON.", e);
 				return "Unexpected server response #3";
 			}
 
 		    return GOOD_RESPONSE;
 		}
 		
 
 	     
 	}
 	
 }
 
 
 
