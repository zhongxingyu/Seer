 package eltoraz.pug.android;
 
 import eltoraz.pug.*;
 
 import java.lang.String;
 import java.util.ArrayList;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 /**
  * The <code>PugNetworkInterface</code> class provides methods to interact with the server.
  * @author Brian Orecchio
  * @version 0.8
  */
 public class PugNetworkInterface {
 	// NOTE: These getGames functions have a bunch of duplicated code, in the future I will add a way to do this filtering in a single get games 
 	//  function by just passing in a map from the filter key to the filter parameter.
 
 	/**
 	 * Get all the Games from the server.
 	 * @return <code>ArrayList</code> containing all the Games the server has in its database
 	 */
 	public static ArrayList<Game> getGames() {
 		ArrayList<Game> games = new ArrayList<Game>();
 
 		String page = "http://pug.myrpi.org/";
 		page += "getfilter.php";
 
 		games = getGamesFromServer(page);
 		return games;
 	}
 
 	/**
 	 * Get all Games within a five-mile radius of the specified location from the server.
 	 * @param lat <code>int</code> Latitude of the game (in microdegrees)
 	 * @param lon <code>int</code> Longitude of the game (in microdegrees)
 	 * @return <code>ArrayList</code> containing the requested Games
 	 */
 	public static ArrayList<Game> getGames(Integer lat, Integer lon) {
 		return getGames(lat, lon, 5);
 	}
 
 	/**
 	 * Get all Games in a location within a specified radius.
 	 * @param lat <code>int</code> Latitude of the game (in microdegrees)
 	 * @param lon <code>int</code> Longitude of the game (in microdegrees)
 	 * @param dist <code>int</code> Maximum distance from the point specified to search (in miles)
 	 * @return <code>ArrayList</code> containing the requested Games
 	 */
 	public static ArrayList<Game> getGames(Integer lat, Integer lon, Integer dist) {
 		ArrayList<Game> games = new ArrayList<Game>();
 
 		String page = "http://pug.myrpi.org/";
 		page += "getfilter.php" + "?lat=" + lat.toString() + "&lon=" + lon.toString() + "&dist=" + dist.toString();
 
 		games = getGamesFromServer(page);
 		return games;
 	}
 
 	/**
 	 * Get all the Games of the specified sport at the specified location.
 	 * @param lat <code>int</code> Latitude of the game (in microdegrees)
 	 * @param lon <code>int</code> Longitude of the game (in microdegrees)
 	 * @param sport <code>String</code> Sport to filter results by
 	 * @return <code>ArrayList</code> containing the requested Games
 	 */
 	public static ArrayList<Game> getGames(Integer lat, Integer lon, String sport) {
 		ArrayList<Game> Games  = new ArrayList<Game>();
 
 		String page = "http://pug.myrpi.org/";
 		page = page + "getfilter.php" + "?lat=" + lat.toString() + "&lon=" + lon.toString() + "&sport=" + sport;
 
 		Games = getGamesFromServer(page);
 		return Games;
 	}
 
 	/**
 	 * Carry out the HTTP request to get all the Games in the server from the filter PHP page with
 	 *  the specified parameters
 	 * @param page <code>String</code> URL of the PHP filter page, with all arguments specified
 	 * @return <code>ArrayList</code> containing the requested Games
 	 */
 	private static ArrayList<Game> getGamesFromServer(String page) {
 		HttpClient httpClient = new DefaultHttpClient();
 		ArrayList<Game> Games = new ArrayList<Game>();
 
 		try {			
 			HttpGet httpGet = new HttpGet (page);
 			HttpResponse response = httpClient.execute(httpGet);
 			HttpEntity entity = response.getEntity();
 			JSONArray jsonArray = new JSONArray(EntityUtils.toString(entity));
 
 			Games = parseGameJSONArray(jsonArray);	
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		return Games;
 	}
 
 	/**
 	 * Parse the given JSON array for the contained Games
 	 * @param jsonArray <code>JSONArray</code> containing containing the Games
 	 * @return <code>ArrayList</code> containing the decoded Games
 	 */
 	private static ArrayList<Game> parseGameJSONArray(JSONArray jsonArray) {
 		Game game;
		ArrayList<Game> games = null;
 		
 		try {
 			for(int i = 0; i < jsonArray.length(); i++) {
				games = new ArrayList<Game>();
 				
 				// Get the JSON object at index i
 				JSONObject gameJson = jsonArray.getJSONObject(i);
 				game = Game.buildGame(gameJson);
 
 				// Add the game to the list
 				games.add(game);
 			}
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 
 		return games;
 	}
 
 	/**
 	 * Send the specified <code>Game</code> object to the server to add to the database
 	 * @param game <code>Game</code> to send to the server
 	 * @return <code>int</code> status of the request
 	 */
 	public static int sendGame(Game game) {
 		HttpClient httpClient = new DefaultHttpClient();
 
 		try{
 			//pack the Game into a JSONObject
 			JSONObject jsonGame = game.JSON();
 
 			//send the JSONObject to the server for processing using some HTTPClient call or something
 			HttpResponse response;
 			HttpPost httpPost = new HttpPost ("http://pug.myrpi.org/creategame.php");
 			StringEntity se = new StringEntity (jsonGame.toString());
 			httpPost.setEntity(se);
 			// For debugging.
 			//System.out.print(se);
 			httpPost.setHeader("Accept", "application/json");
 			httpPost.setHeader("Content-type", "application/json");
 
 			response=httpClient.execute(httpPost);
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		return 0;
 	}
 
 	/**
 	 * Get the <code>Person</code> corresponding to the phone's unique phone ID string.
 	 * @param phoneId <code>String</code> The Android device's unique phone ID
 	 * @return <code>Person</code> corresponding to the ID, if in the database
 	 */
 	public static Person getUser(String phoneId) {
 		HttpClient httpClient = new DefaultHttpClient();
 		Person p = null;
 
 		try {
 			String page = "http://pug.myrpi.org/getuser.php";
 			page += "?phone=" + phoneId;
 
 			HttpGet httpGet= new HttpGet (page);
 			HttpResponse response = httpClient.execute(httpGet);
 			HttpEntity entity = response.getEntity();
 			JSONObject jsonObject=new JSONObject(EntityUtils.toString(entity));
 
 			p = new Person(jsonObject);
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 
 		return p;
 	}
 
 	/**
 	 * send a user id and game id to server signifying that the user has joined the game
 	 * 
 	 * @param userid 
 	 * @param gameid
 	 * @return
 	 */
 	public static void joinGame( int userid, int gameid ) {
 		HttpClient httpClient = new DefaultHttpClient();
 
 		try {
 			String page = "http://pug.myrpi.org/joingame.php";
 			page += "?user=" + userid;
 			page += "&game=" + gameid;
 
 			HttpGet httpGet= new HttpGet (page);
 			HttpResponse response = httpClient.execute(httpGet);
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 
 		return;
 	}
 	
 	/**
 	 * send the <code>Person</code> with updated fields to the server so the user's info can be updated
 	 * @param user
 	 * 
 	 * @return
 	 */
 	public static void editUser( Person user ) {
 		
 		HttpClient httpClient = new DefaultHttpClient();
 
 		try{
 			//pack the Game into a JSONObject
 			JSONObject jsonPerson = user.JSON();
 
 			//send the JSONObject to the server for processing using some HTTPClient call or something
 			HttpResponse response;
 			HttpPost httpPost = new HttpPost ("http://pug.myrpi.org/edituser.php");
 			StringEntity se = new StringEntity (jsonPerson.toString());
 			httpPost.setEntity(se);
 			// For debugging.
 			//System.out.print(se);
 			httpPost.setHeader("Accept", "application/json");
 			httpPost.setHeader("Content-type", "application/json");
 
 			response=httpClient.execute(httpPost);
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		return;
 	}
 	
 
 }
