 package com.example.restaurantreviewapplication;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.UUID;
 import java.util.concurrent.ExecutionException;
 
 import android.location.Location;
 import android.os.AsyncTask;
 import android.util.Base64;
 import android.util.Log;
 
 /**
  * 
  * Manage connections to the application server.
  * 
  * @author phil
  * 
  */
 public class Server {
 	private static String serverURL = "http://cop4331.atmdvdusa.com/server/";
 	
 	// BACKUP SERVER
 	//private static String serverURL = "http://71.226.94.202/restaurant/";
 	
 	private static String username = "anonymous";
 	private static String password = "none";
 	private static String error;
 	private static final int BASE64_OPTS = Base64.NO_PADDING | Base64.NO_WRAP
 			| Base64.URL_SAFE;
 	private static boolean isReady = true;
 
 	// //////////////////////////////////////////////////////////////////////////////////////////
 	// Place Stubs / Tests Here.
 	// //////////////////////////////////////////////////////////////////////////////////////////
 
 
 
 	
 	// //////////////////////////////////////////////////////////////////////////////////////////
 
 	/** Delete a message on the server.
 	 * @param theMessage message to be deleted
 	 */
 	public static void deleteMessage(Message theMessage){
 		HashMap<String, String> postData = new HashMap<String, String>();
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("sender", theMessage.getSenderUserId());
 		postData.put("message", theMessage.getTextField());
 		postData.put("action", "deleteMessage");
 		postToServer(postData);
 	}
 	
 	/** Get messages sent to our user.
 	 * @return An ArrayList of messages for the user.
 	 */
 	public static ArrayList<Message> getMessages() {
 		HashMap<String, String> postData = new HashMap<String, String>();
 		ArrayList<Message> mlist = new ArrayList<Message>();
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("action", "getMessages");
 		String res = postToServer(postData);
 		if(noResult(res)) return mlist;
 		String[] messageUnits = res.split(":&:");
 		for(String m : messageUnits) {
 			String[] mparts = m.split("!#!");
 			if(mparts.length != 2) continue;
 			Message n = new Message(mparts[0], username, mparts[1]);
 			mlist.add(n);
 		}
 		return mlist;
 	}
 	
 	/** Get a list of confirmed friends.
 	 * @param friendID I have no idea. 
 	 * @return A list of friends.
 	 */
 	public static ArrayList<Friend> getFriends() {
 		HashMap<String, String> postData = new HashMap<String, String>();
 		ArrayList<Friend> flist = new ArrayList<Friend>();
 
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("action", "getFriends");
 		String res = postToServer(postData);
 		if(noResult(res)) return flist;
 		String[] friendIds = res.split(",");
 		for(String id : friendIds) {
 			Friend f = new Friend();
 			f.setUserId(id);
 			flist.add(f);
 		}
 		return flist;
 	}
 	
 	
 	/** Get a list of unconfirmed friends.
 	 * @return List of unconfirmed friends.
 	 */
 	public static ArrayList<Friend> getUnconfirmedFriends() {
 		HashMap<String, String> postData = new HashMap<String, String>();
 		ArrayList<Friend> flist = new ArrayList<Friend>();
 
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("action", "getUnconfirmedFriends");
 		String res = postToServer(postData);
 		if(noResult(res)) return flist;
 		String[] friendIds = res.split(",");
 		for(String id : friendIds) {
 			Friend f = new Friend();
 			f.setUserId(id);
 			flist.add(f);
 		}
 		return flist;
 	}
 	
 	/** Confirm a friend by ID.
 	 * @param f The friend to confirm (ID)
 	 */
 	public static void confirmFriend(Friend f) {
 		HashMap<String, String> postData = new HashMap<String, String>();
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("action", "confirmFriend");
 		postData.put("friend", f.getUserId());
 		postToServer(postData);
 	}
 	
 	/** Add a new (unconfirmed) friend.
 	 * @param user
 	 * @param newFriend
 	 */
 	public static void addFriend(User user, Friend newFriend) {
 		HashMap<String, String> postData = new HashMap<String, String>();
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("action", "addFriend");
 		postData.put("friend", newFriend.getUserId());
 
 		String res = postToServer(postData);
 		Log.i("Server.addFriend", res);
 		// [MSG: Sent Request] on success
 	}
 
 	/** Delete a friend. 
 	 * @param currentUser
 	 * @param friend
 	 * @return 0 for good, 1 for bad
 	 */
 	public static String deleteFriend(User currentUser, Friend friend) {
 		// 0 for good, 1 for bad
 		HashMap<String, String> postData = new HashMap<String, String>();
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("action", "deleteFriend");
 		postData.put("delete", friend.getUserId());
 		String res = postToServer(postData);
 		Log.i("Server.deleteFriend", res);
 		return res;
 //		if (res.equals("MSG: Friend deleted"))
 //			return 0;
 //		else
 //			return 1;
 	}
 
 	/** Send a message to a friend.
 	 * @param currentUser
 	 * @param friend
 	 * @param message
 	 * @return 0 for good, 1 for bad
 	 */
 	public static String messageFriend(Message theMessage) {
 		// 0 for good, 1 for bad
 		HashMap<String, String> postData = new HashMap<String, String>();
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("action", "messageFriend");
 		postData.put("friend", theMessage.getReceiverUserId());
 		postData.put("message", theMessage.getTextField());
 		String res = postToServer(postData);
 		Log.i("Server.messageFriend", res);
 		return res;
 //		if (res.equals("MSG: Sent"))
 //			return 0;
 //		else
 //			return 1;
 	}
 
 	/** Search for someone by ID.
 	 * 
 	 * @param friendID
 	 *            Username of friend to search for
 	 * @return Friend object if found, otherwise null
 	 */
 	public static Friend findFriend(String friendID) {
 		HashMap<String, String> postData = new HashMap<String, String>();
 		if(friendID.equals(username)) return null;
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("action", "findFriend");
 		postData.put("friend", friendID);
 		try {
 			String res = postToServer(postData);
 			Log.i("Server.findFriend", res);
 			if (res.substring(0, 3).equals("MSG")) {
 				Friend result;
 				result = new Friend();
 				result.setUserId(friendID);
 				return result;
 			} else
 				return null; // not found
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/** Log out (remove stored account info)
 	 * @param currentUser
 	 */
 	public static void LogOut(User currentUser) {
 		// log user out of server
 		Server.username = "anonymous";
 		Server.password = "none";
 	}
 
 	/** Log out (remove stored account info)
 	 * 
 	 */
 	public static void logOut() {
 		LogOut(null);
 	}
 	
 	/** Whether a valid user is logged in.
 	 * 
 	 * @return true if logged in, false if not.
 	 */
 	public static Boolean loggedIn() {
 		return (! username.equals("anonymous"));
 	}
 	/** Get a list of reviews for a single restaurant.
 	 * @param restaurant
 	 * @return List of reviews for this restaurant.
 	 */
 	public static ArrayList<Review> getReviews(Restaurant restaurant) {
 		ArrayList<Review> reviews = new ArrayList<Review>();
 		HashMap<String, String> postData = new HashMap<String, String>();
 		String sresp;
 		String[] srespArray;
 		ArrayList<UUID> uuidresult = new ArrayList<UUID>();
 		Log.i("getReviews", "called");
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("action", "getRestaurantReviews");
 		postData.put("uuid", restaurant.getUuid().toString());
 		sresp = postToServer(postData);
 
 		srespArray = sresp.split(",");
 		Log.i("getReviews", "Response: [" + sresp + "]");
 		Log.i("getReviews", "Response count: " + srespArray.length);
 		if (sresp.length() == 0)
 			return reviews; // no reviews
 		if (Server.checkError(sresp) != null) {
 			Log.e("Server.getReviews", sresp);
 			return reviews;
 		}
 		for (String sr : srespArray) {
 			uuidresult.add(UUID.fromString(sr));
 		}
 		for (UUID u : uuidresult) {
 			Object ob = getObject(u);
 			if (!isError(ob))
 				reviews.add((Review) getObject(u));
 		}
 		return reviews;
 	}
 
 	/** Change the user's password to the new password. 
 	 * @param newPassword the new password.
 	 * @return 0 for good, 1 for bad
 	 */
 	public static int changePassword(String newPassword) {
 		HashMap<String, String> postData = new HashMap<String, String>();
 
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("action", "changePassword");
 		postData.put("newpassword", newPassword);
 
 		String res = postToServer(postData);
 		Log.i("Server.changePassword", res);
 		if (res.equals("MSG: Password changed.")) {
 			password = newPassword;
 			return 0;
 		} else return 1;
 	}
 
 	/** Add a review to a restaurant.
 	 * @param restaurant
 	 *            the Restaurant being reviewed
 	 * @param review
 	 *            The Review.
 	 */
 	public static void addReview(Restaurant restaurant, Review review) {
 		HashMap<String, String> postData = new HashMap<String, String>();
 		String res;
 		Log.i("addReview", "called");
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("visibility", "public");
 		postData.put("action", "putRestaurantReview");
 		postData.put("reviewuuid", review.getUuid().toString());
 		postData.put("restaurantuuid", restaurant.getUuid().toString());
 		postData.put("object", objectToString(review));
 		res = postToServer(postData);
 		Log.i("Server.addReview", res);
 	}
 
 	/** Change the server's URL.
 	 * @param serverURL
 	 *            Set the URL of the server. It is set by default, hardcoded in
 	 *            this class.
 	 */
 
 	public static void setServer(String serverURL) {
 		Log.i("Server.getServer", "Server URL changed to " + serverURL);
 		Server.serverURL = serverURL;
 	}
 
 	/** Set the username.
 	 * 
 	 * @param username
 	 *            The user's username.
 	 */
 	public static void setUsername(String username) {
 		Log.i("Server.setUsername", "Username changed to " + username);
 		Server.username = username;
 	}
 
 	/** Set the password.
 	 * 
 	 * @param password
 	 *            The user's password.
 	 */
 	public static void setPassword(String password) {
 		Log.i("Server.setPassword", "Password changed to " + password);
 		Server.password = password;
 	}
 
 	/** Create an account on the server.
 	 * Create an account on the server. Make sure to specify the
 	 * username/password first with setUsername and setPassword.
 	 * 
 	 * @param email
 	 *            the user's email address
 	 * @return
 	 */
 	public static String createAccount(String email) {
 		HashMap<String, String> postData;
 		postData = new HashMap<String, String>();
 		User user;
 		user = new User();
 		user.setPassword(Server.password);
 		user.setUsername(Server.username);
 		user.getUuid();
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("email", email);
 		postData.put("action", "newUser");
 
 		postData.put("uuid", user.getUuid().toString());
 		postData.put("object", objectToString(user));
 
 		return postToServer(postData);
 	}
 
 	/** Initiate a password reset (web-based)
 	 * Initiate a password reset for the user.
 	 * 
 	 * @return 0 for good, 1 for bad
 	 */
 	public static int resetPassword() {
 		Log.i("Server.resetPassword", "Called");
 		HashMap<String, String> postData;
 		postData = new HashMap<String, String>();
 		postData.put("pwreset", username);
 		String sret = postToServer(postData);
 
 		if (sret.equals("MSG: Mail sent successfully."))
 			return 0;
 		else
 			return 1;
 	}
 
 	/** Search for restaurants near current location.
 	 * This searches restaurants within a five mile radius of the user. It
 	 * should probably be changed to take an argument specifying the maximum
 	 * distance.
 	 * 
 	 * @param location
 	 *            a Location of the user
 	 * @return list of restaurants
 	 */
 	public static ArrayList<Restaurant> getRestaurantsByLocation(
 			Location location) {
 		ArrayList<Restaurant> result = new ArrayList<Restaurant>();
 		ArrayList<UUID> RestaurantUuids;
 		Log.i("getRestaurantsByLocation", "called");
 		// When distance is implemented, replace 1 in the line below.
 		RestaurantUuids = getRestaurantUuids(0, "", location, 5);
 		for (UUID r : RestaurantUuids) {
 			Restaurant toAdd = restaurantFromUuid(r);
 			if (toAdd != null) {
 				result.add(toAdd);
 			} else {
 				Log.e("getRestaurantsByZipKeyword", "Got a null response");
 			}
 		}
 		return result;
 	}
 
 	/** Search by Zip and/or Keyword.
 	 * @param zip
 	 *            0 if not entered, otherwise the zip code.
 	 * @param keyword
 	 *            A keyword/name of a restaurant.
 	 * @return
 	 */
 	public static ArrayList<Restaurant> getRestaurantsByZipKeyword(int zip,
 			String keyword) {
 		ArrayList<Restaurant> result = new ArrayList<Restaurant>();
 		ArrayList<UUID> RestaurantUuids;
 		Log.i("getRestaurantsByZipKeyword", "called");
 		RestaurantUuids = getRestaurantUuids(zip, keyword, null, 0);
 		for (UUID r : RestaurantUuids) {
 			Restaurant toAdd = restaurantFromUuid(r);
 			if (toAdd != null) {
 				result.add(toAdd);
 			} else {
 				Log.e("getRestaurantsByZipKeyword", "Got a null response");
 			}
 		}
 		return result;
 	}
 
 	/** Create a Restaurant object for the given UUID.
 	 * 
 	 * @param uuid
 	 *            the UUID of a Restaurant "object"
 	 * @return a Restaurant.
 	 */
 	private static Restaurant restaurantFromUuid(UUID uuid) {
 		Restaurant ro;
 		HashMap<String, String> vars = new HashMap<String, String>();
 		vars.put("username", Server.username);
 		vars.put("password", Server.password);
 		vars.put("action", "getOneRestaurant");
 		vars.put("uuid", uuid.toString());
 		String result = Server.postToServer(vars);
 		if (result.substring(0, 3).equals("ERR")) {
 			Log.e("Server restaurantFromUUID", result);
 			return null;
 		}
 		/*
 		 * Format: name|Joe's Restaurant%address|123 Fake St
 		 */
 		String[] args = result.split("[*]"); // Array of arguments
 		HashMap<String, String> r = new HashMap<String, String>(); 
 		for (String kvp : args) {
 			String[] kva = kvp.split("[|]");
 			if (kva.length != 2) {
 				Log.e("restaurantFromUuid",
 						"Invalid k/v pair: " + kva.toString());
 				return null;
 			}
 			r.put(kva[0], kva[1]);
 		}
 		if ((r.get("name") == null) || (r.get("address") == null)
 				|| (r.get("phone") == null) || (r.get("lat") == null)
 				|| (r.get("lng") == null)) {
 			Log.e("restaurantFromUuid", "A restaurant is missing data:"
 					+ result);
 			return null;
 		}
 		ro = new Restaurant(r.get("name"), r.get("address"), r.get("phone"),
 				r.get("lat") + "," + r.get("lng"));
 		ro.setUuid(uuid);
 		return ro;
 	}
 
 	/** Get the Restaurant UUID's based on the criteria.
 	 * @param zip
 	 *            The zip to search for, or 0 to ignore.
 	 * @param keyword
 	 *            The keywords, or null to ignore.
 	 * @param location
 	 *            the Location of the restaurant, or null to ignore.
 	 * @param miles
 	 *            The number of miles to search by location.
 	 * @return
 	 */
 	public static ArrayList<UUID> getRestaurantUuids(int zip, String keyword,
 			Location location, int miles) {
 		HashMap<String, String> postData = new HashMap<String, String>();
 		ArrayList<UUID> uuidresult = new ArrayList<UUID>();
 		String sresp;
 		String[] srespArray;
 		Log.i("getRestaurantUuids", "Called");
 		postData.put("username", username);
 		postData.put("password", password);
 		postData.put("action", "getRestaurants");
 
 		if (location != null && miles != 0) {
 			postData.put("longi", Double.toString(location.getLongitude()));
 			postData.put("lati", Double.toString(location.getLatitude()));
 			postData.put("miles", Integer.toString(miles));
 		}
 		if (zip != 0) {
 			postData.put("zip", Integer.toString(zip));
 		}
 		if ((keyword != null) && (!keyword.equals(""))) {
 			postData.put("keywords", keyword);
 		}
 		sresp = postToServer(postData);
 		if (checkError(sresp) != null) {
 			Log.e("Server.getRestaurantUuids", sresp);
 			return uuidresult;
 		}
 		srespArray = sresp.split(",");
 		if (sresp.length() == 0) { // nothing
 			return uuidresult;
 		}
 		for (String sr : srespArray) {
 			uuidresult.add(UUID.fromString(sr));
 		}
 		return uuidresult;
 	}
 
 	/** Check if an object returned is due to an error. 
 	 * @param o
 	 *            An object returned from getObject
 	 * @return True if this object is an error.
 	 */
 	public static boolean isError(Object o) {
 		String str;
 		try {
 			str = (String) o;
 		} catch (ClassCastException e) {
 			return (o == null);
 		}
		return (str == null ||
			   (str.length() > 2 && str.substring(0, 3).equals("ERR")));
 	}
 
 	/** Check if a string response is an error. 
 	 * @param s
 	 *            The response from the server
 	 * @return The error, or null if no error.
 	 */
 	public static String checkError(String s) {
 		if (s.length() >= 3 && s.substring(0, 3).equals("ERR")) {
 			Log.i("Server.checkError", "Found an error.");
 			return s;
 		} else {
 			Log.i("Server.checkError", "Found no error.");
 			return null;
 		}
 	}
 
 	/** Check if a string response is a message. 
 	 * @param s The response from the server
 	 * @return The error, or null if no error.
 	 */
 	public static String checkMsg(String s) {
 		if (s.length() >= 3 && s.substring(0, 3).equals("MSG")) {
 			Log.i("Server.checkError", "Found a message.");
 			return s;
 		} else {
 			Log.i("Server.checkError", "Found a message.");
 			return null;
 		}
 	}
 	
 	/** Check if the server returned a result.
 	 * @param s The response from the server
 	 * @return True if the response was blank, error or a message, false otherwise.
 	 */
 	public static Boolean noResult(String s) {
 		if (s.length() == 0) return true;
 		if (s.length() >= 3) {
 			if(s.substring(0,3).equals("MSG") || 
 					s.substring(0,3).equals("ERR")) return true;
 		}
 		return false;
 	}
 	/** Get an error from the server response object. 
 	 * @param o
 	 *            An object that is an error.
 	 * @return A string containing the error.
 	 */
 
 	public static String getError(Object o) {
 		String str = (String) o;
 		if (o == null)
 			str = "ERR: No object returned.";
 		return str;
 	}
 
 	/** Get the User object for the current username/password.
 	 * 
 	 * @return the User object
 	 */
 	public static User getUser() {
 		HashMap<String, String> vars = new HashMap<String, String>();
 		String sret;
 		User o;
 		vars.put("username", Server.username);
 		vars.put("password", Server.password);
 		vars.put("action", "getUserData");
 		sret = postToServer(vars);
 		if (checkError(sret) != null) {
 			Log.e("Server.getUser", checkError(sret));
 			return null;
 		} else {
 			Log.i("getUser decode", sret);
 			o = (User) stringToObject(sret);
 			Log.i("Server.getUser", "Logged in as " + o.getUsername());
 			return o;
 		}
 	}
 
 	/** Put a Storable object on the server.
 	 * 
 	 * @param o
 	 *            a Storable object.
 	 * @param visibility
 	 *            One of "public", "private", "friends"
 	 * @return The response from the server
 	 */
 	public static String putObject(Storable o, String visibility) {
 		HashMap<String, String> vars = new HashMap<String, String>();
 		vars.put("username", Server.username);
 		vars.put("password", Server.password);
 		vars.put("action", "writeObject");
 		vars.put("visibility", visibility);
 		vars.put("uuid", o.getUuid().toString());
 		vars.put("object", objectToString(o));
 
 		return postToServer(vars);
 	}
 
 	/** Get a Storable object from the server.
 	 * 
 	 * @param uuid
 	 *            The UUID of the object.
 	 * @return an object. If it is an error, the error will be returned as a
 	 *         String object.
 	 */
 	public static Object getObject(UUID uuid) {
 		HashMap<String, String> vars = new HashMap<String, String>();
 		vars.put("username", Server.username);
 		vars.put("password", Server.password);
 		vars.put("action", "readObject");
 		vars.put("uuid", uuid.toString());
 		String result = Server.postToServer(vars);
		if (result.length() > 2 && result.substring(0, 3).equals("ERR")) {
 			Log.e("Server", result);
 			return result;
 		} else
 			return stringToObject(result);
 	}
 
 	/** Send a raw request to the server. 
 	 * 
 	 * Send a POST request to the server and return the response. This can be
 	 * used to do anything, but it's the hard way. It's recommended to add a
 	 * method here instead of using this directly.
 	 * 
 	 * Example:
 	 * 
 	 * HashMap<String, String> vars = new HashMap<String, String>();
 	 * vars.put("username", "bob"); vars.put("password", "secret");
 	 * vars.put("action", "readObject"); vars.put("uuid",
 	 * "bfaf910e-8b4f-11e2-840a-be257188419e"); String result =
 	 * Server.postToServer(vars);
 	 * 
 	 * @param vars
 	 *            A HashMap of POST vars and values.
 	 * @return The response from the server.
 	 */
 	public static String postToServer(HashMap<String, String> vars) {
 		String postData = mapToPost(vars);
 		String res = sendServer(postData);
 
 		return res;
 	}
 
 	/*
 	 * ****************************************************************
 	 * Abandon hope all ye who venture below this line. This is where the
 	 * sausage is made.
 	 * ****************************************************************
 	 */
 
 	/** Wrapper for Android's Base64 conversion. Converts binary to string.
 	 * 
 	 * @param in
 	 *            a byte array
 	 * @return The byte array encoded in URL safe Base64.
 	 */
 	private static String toB64(byte[] in) {
 		return Base64.encodeToString(in, BASE64_OPTS);
 	}
 
 	/** Wrapper for Android's Base64 conversion. Converts string to binary.
 	 * 
 	 * @param in
 	 *            A string encoded in URL safe Base64
 	 * @return the byte array encoded in the string
 	 */
 	private static byte[] fromB64(String in) {
 		return Base64.decode(in, BASE64_OPTS);
 	}
 
 	/** Map a hashmap to a HttpURLConnection string.
 	 * Convert a HashMap of POST vars/vals to a string usable with
 	 * HttpURLConnection.
 	 * 
 	 * @param vars
 	 *            A HashMap<String, String> of post vars/values
 	 * @return A String suitable for HTTP url parameters.
 	 */
 	private static String mapToPost(HashMap<String, String> vars) {
 		Iterator<String> keyIter = vars.keySet().iterator();
 		StringBuilder sb = new StringBuilder();
 		while (keyIter.hasNext()) {
 			String key = keyIter.next();
 			sb.append(key + "=" + vars.get(key));
 			if (keyIter.hasNext()) {
 				sb.append("&");
 			}
 		}
 		Log.i("mapToPost", sb.toString());
 		return sb.toString();
 	}
 
 	/** Convert a string to an object. 
 	 * @param b64in
 	 *            A base-64 encoded string
 	 * @return An object
 	 */
 	private static Storable stringToObject(String b64in) {
 		Storable o = null;
 		byte[] s2 = fromB64(b64in);
 		try {
 			ObjectInputStream is = new ObjectInputStream(
 					new ByteArrayInputStream(s2));
 			o = (Storable) is.readObject();
 			is.close();
 		} catch (Exception e) {
 			Log.e("Server.stringToObject", e.toString());
 			return null;
 		}
 		return o;
 	}
 
 	/** Convert an object to a string. 
 	 * @param ob
 	 *            a Storable object.
 	 * @return A base-64 encoded string that represents the object.
 	 */
 	private static String objectToString(Storable ob) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		try {
 			ObjectOutputStream o = new ObjectOutputStream(b);
 			o.writeObject(ob);
 			o.close();
 		} catch (IOException e) {
 			Log.e("Server.objectToString", e.toString());
 			return "";
 		}
 		return toB64(b.toByteArray());
 	}
 
 	/** Used internally by the asynchronous server task to make the connection.
 	 * @param urlParameters
 	 *            A string containing POST vars/vals.
 	 * @return The response from the server.
 	 */
 	private static String sendServerInternal(String urlParameters) {
 		StringBuilder sb = new StringBuilder();
 		try {
 			URL url = new URL(serverURL);
 			HttpURLConnection connection = (HttpURLConnection) url
 					.openConnection();
 			connection.setDoOutput(true);
 			connection.setDoInput(true);
 			connection.setInstanceFollowRedirects(false);
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("Content-Type",
 					"application/x-www-form-urlencoded");
 			connection.setRequestProperty("charset", "utf-8");
 			connection.setRequestProperty("Content-Length",
 					"" + Integer.toString(urlParameters.getBytes().length));
 			connection.setUseCaches(false);
 
 			DataOutputStream wr = new DataOutputStream(
 					connection.getOutputStream());
 			Log.i("Server.sendServerInternal", "Sent: " + urlParameters);
 			wr.writeBytes(urlParameters);
 			wr.flush();
 			wr.close();
 
 			BufferedReader rd = new BufferedReader(new InputStreamReader(
 					connection.getInputStream()), 8192);
 			String line;
 			while ((line = rd.readLine()) != null) {
 				sb.append(line);
 			}
 			connection.disconnect();
 		} catch (Exception e) {
 			Log.e("Server.sendServerInternal", e.toString());
 			return "ERR: Server not accessible";
 		}
 		Log.i("Server.sendServer", "Got: " + sb.toString());
 		return sb.toString().replaceAll("\\$@.*@\\$", "");
 	}
 
 	/** Send the parameters and obtain response from server (wraps async)
 	 * @param urlParameters
 	 *            The serialized URL parameters to send
 	 * @return The server's response.
 	 */
 	public static String sendServer(String urlParameters) {
 		setReady(false);
 		String r;
 		AsyncTask<String, Void, String> s = new Server.ServerTask();
 		s.execute(urlParameters);
 		try {
 			r = s.get();
 		} catch (InterruptedException e) {
 			Log.e("Server.sendServer", e.toString());
 			return "ERR: Interrupted";
 		} catch (ExecutionException e) {
 			Log.e("Server.sendServer", e.toString());
 			return "ERR: Execution Exception";
 		}
 		if (checkError(r) != null) {
 			error = r;
 			Log.e("sendServer ERR: ", r);
 		}
 		return r;
 	}
 
 	/** The last error message from the server. 
 	 * @return The last error message from the server.
 	 */
 	public static String error() {
 		return Server.error;
 	}
 
 	/** Set the server to be ready or not ready. 
 	 * @param stat
 	 *            True if server is ready, false otherwise.
 	 */
 	private static void setReady(Boolean stat) {
 		isReady = stat;
 	}
 
 	/** Check if server is ready. 
 	 * @return True if server ready, false otherwise.
 	 */
 	public static boolean getReady() {
 		return isReady;
 	}
 
 	/** Asynchronous server task. 
 	 * @author phil Asynchronous server task.
 	 */
 	public static class ServerTask extends AsyncTask<String, Void, String> {
 		@Override
 		protected String doInBackground(String... args) {
 			Server.setReady(false);
 			return Server.sendServerInternal(args[0]);
 		}
 
 		protected void onPostExecute(String result) {
 			Server.setReady(true);
 		}
 	}
 }
