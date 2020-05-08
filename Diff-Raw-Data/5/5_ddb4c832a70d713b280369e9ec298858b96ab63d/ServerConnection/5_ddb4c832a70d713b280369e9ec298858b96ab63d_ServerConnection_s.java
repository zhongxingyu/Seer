 package com.boh.flatmate.connection;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import android.util.Log;
 
 import com.google.gson.Gson;
 
 public class ServerConnection {
 
 	private static String auth_token = null;
 	
 	private Gson gson = new Gson();
 	private static String server = "http://whispering-plains-6470.herokuapp.com";
 	public static String FileName = "";
 
 	//*****************************************user stuff*****************************************
 
 	//register a user and create a User object with all the details
 	//note: must have an email and password or this won't work
 	public  String registerUser(User newUser, String password) {
 		String userInfo = null;
 		String jsonResult;
 
 
 		//setup http string for registering user, in same order as API
 		userInfo = newUser.toHTTPString();
 		//userInfo += "&user[password]=" + password;
 
 		jsonResult = post(server + "/users.json", userInfo);
 		
 		return jsonResult;
 
 		//Check JSON string:
 		// System.out.println(jsonResult);
 
 		//class containing all objects returned during registration
 		//can be used to check against newUser, make sure everything is registered correctly
 		//User reg = gson.fromJson(jsonResult, User.class);
 
 		//Check parsed stoof
 		//System.out.println(reg.email);
 	}
 
 	//login and receive auth_token
 	public  String login (String uname, String pword) {
 		String info = "email=" + uname + "&password=" + pword;
 		//System.out.println(info);
 		String jsonResult = putOrPost(server+"/tokens", info, false);
 		if(jsonResult == "failed" || jsonResult == "connection" || jsonResult == "invalid"){
 			return jsonResult;
 		}
 		Login log = gson.fromJson(jsonResult, Login.class);
 
 		auth_token = log.getToken();
 		return auth_token;
 	}
 	
 	public String login (String uname, String pword, String deviceID) {
 		String auth = login(uname, pword);
 		maintainGcmRegistration(deviceID);
 		
 		return auth;
 	}
 	
 	/* Reminds the server that this device is still active, if a long period between the device's
 	*  initial registration and now passes without it firing this, it will be removed from the 
 	*  database of saved device IDs.
 	*  Therefore this must run on every app open.
 	*/
 	public int maintainGcmRegistration(String deviceID){
 			User u = getMe();
 			
 			u.setRegistration_id(deviceID);
 			updateUser(u);
 			return 1;
 	}
 
 	//set authentication code
 	public static void setAuth(String at) {
 		auth_token = at;
 	}
 	
 	// load the authentication code from a file, returns null if failed.
 	public static String loadAuth(String filename){
 		FileName = filename;
 		File logFile = new File(filename);
 		String authCode = null;
 		if (logFile.exists())
 		{
 			BufferedReader input;
 			try {
 				input = new BufferedReader(new FileReader(filename));
 			} catch (FileNotFoundException e1) {
 				return null;
 			}
 			try {
 				String line = null;
 				if (( line = input.readLine()) != null){
 					authCode = line;
 				}
 			}catch (Exception e){
 
 			}
 			try {
 				input.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			setAuth(authCode);
 			// sucessfull login here returns the authcode
 			return authCode;
 			// to test without logging in automatically, set to null:
 			//return null;
 		}
 
 		return null;	
 	}
 
 	//logout by deleting auth_token from server
 	public  void logout() {
 		delete(server+"/tokens/" + auth_token);
 	}
 
 	//delete user by id
 	public  void deleteUser(String id) {
 		delete(server + "/users/" + id);
 	}
 
 	//get list of all users
 	public  User[] getUserList() {
 		System.out.println(server+"/users");
 		return gson.fromJson(get(server + "/users"), User[].class);
 	}
 
 	//get a single user by id
 	public  User getSingleUser(String id) {
 		return gson.fromJson(get(server + "/users/" + id), User.class);
 	}
 
 	//edit user given by user
 	public  void updateUser(User user) {
 		String userInfo = user.toHTTPString();
 		//System.out.println(userInfo);
 		put(server + "/users/" + user.getId(), userInfo);
 	}
 	
 	//edit user given by user
 		public  void updateMe(User user) {
 			String userInfo = user.toHTTPString();
 			//System.out.println(userInfo);
 			put(server + "/users/m", userInfo);
 		}
 
 	//change password, note: not 100% sure this is how to do it
 	public  void changePassword(int id, String password) {
 		put(server + "/users/" + id, "user[password]=" + password);
 	}
 
 	//return me, needs authentication code
 	public User getMe() {
 		return gson.fromJson(get(server + "/users/m"), User.class);
 	}
 
 	//*****************************************flat stuff*****************************************
 
 	//create a new flat
 	public Flat addFlat(Flat flat) {
 		String flatInfo = flat.toHTTPString();
 		return gson.fromJson(post(server + "/flats", flatInfo), Flat.class);
 	}
 	
 	//search for flats
 	public Flat[] searchFlats(String pCode, String nName) {
 		
 		//auth_token = "4sfmMSJroUn3bU9YpAso"; //Only used as the server requires authentication for this task.
 		
 		return gson.fromJson(get(server + "/flats/search/" + pCode), Flat[].class);
 		//return gson.fromJson(get(server + "/flats/search/" + pCode + "/" + nName), Flat[].class);
 	}
 	
 	//get a single flat by id
 	public  Flat getFlat(int flat_id) {
 		return gson.fromJson(get(server + "/flats/" + flat_id), Flat.class);
 	}
 
 	//get whole list of flats
 	public  Flat[] getFlats() {
 		//auth_token = "4sfmMSJroUn3bU9YpAso"; //Only used as the server requires authentication for this task.
 		return gson.fromJson(get(server + "/flats"), Flat[].class);
 	}
 
 	//return my flat, needs authentication code -- note will return users but doesn't currently
 
 	public Flat getMyFlat() {
 		String body = get(server + "/flats/m");
 		Flat f = gson.fromJson(body, Flat.class);
 		if(f == null) {
 			Log.e("FLAT", "Flat gson conversion was null for body: '" + body + "'");
 		}
 		return f;
 	}
 	
 	//*****************************************message stuff*****************************************
 
 	//get all messages for your flat
 	public  Message[] getMyFlatMessages() {
 		return gson.fromJson(get(server + "/flats/m/messages"), Message[].class);
 	}
 
 	//get all message for user with given id
 	public  Message[] getUserMessages(int user_id) {
 		Message[] messages = gson.fromJson(get(server + "/users/" + user_id + "/messages"), Message[].class);
 		for (@SuppressWarnings("unused") Message m : messages) {
 			//put(server + "/users/" + m.g)
 		}
 		return messages;
 	}
 
 	//delete a message with given flat and message ids
 	public  void deleteFlatMessage(int flat_id, int message_id) {
 		delete(server + "/flats/" + flat_id + "/messages/" + message_id);
 	}
 
 	//send message m
 	public  void postMessage(Message m) {
 		if (m.getTo_id() > 0 && m.getFlat_id() > 0) {
 			//HAZ TEMPER TANTRUM
 		} else {
 			post(server + "/users/" + m.getTo_id() + "/messages", m.toHTTPString());
 		}
 	}
 
 	//*****************************************shopping stuff*****************************************
 
 	//add item to flats shopping list given by item
 	public void addItem(ShopItem item) {
 		String itemInfo = item.toHTTPString();
 		@SuppressWarnings("unused")
 		String jsonResult = post(server + "/flats/" + item.getFlatId() + "/shop_items", itemInfo);
 	}
 	
 	//update item with given item
 	public void updateItem(ShopItem item) {
 		String itemInfo = item.toHTTPString();
 		put(server + "/flats/" + item.getFlatId() + "/shop_items/" + item.getId(), itemInfo);
 	}
 	
 	//delete item given by flat id (fid) and item id (iid)
 	public void deleteItem(String fid, String iid) {
 		delete(server + "/flats/" + fid + "/shop_items/" + iid);
 	}
 	
 	//get all shopping items for flat with given id
 	public  ShopItem[] getFlatShoppingList(int flat_id) {
 		//return gson.fromJson(get(server + "/flats/" + flat_id + "/shop_items"), ShopItem[].class);
 		return new ShopItem[0]; //FIXME the above line causes my client to crash - Matt. Not that this is needed anymore. Delete function?
 	}
 
 	//*****************************************connection stuff***************************************** 
 	
 	//put to server
 	private void put(String tu, String up) {
 		System.out.println("Put Command:");
 		if (auth_token != null) tu += "?auth_token=" + auth_token;
 		URL url;
 		System.out.println(tu);
 		System.out.println(up);
 		try {
 			url = new URL(tu);
 			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
 			httpCon.setDoOutput(true);
 			httpCon.setRequestMethod("PUT");
 			OutputStreamWriter out = new OutputStreamWriter(
 			    httpCon.getOutputStream());
 			out.write(up);
 			out.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println();
 	}
 	
 	//post to server
 	private String post(String tu, String up) {
 		return putOrPost(tu, up, false);
 	}
 	
 	//put/post to server
 	//note: true is put, false is post
 	private  String putOrPost(String targetURL, String urlParameters, boolean put) {
 		if (auth_token != null) targetURL += "?auth_token=" + auth_token;
 		System.out.println(urlParameters);
 		URL url;
 		HttpURLConnection connection = null;  
 		try {
 			//Create connection
 			url = new URL(targetURL);
 			connection = (HttpURLConnection)url.openConnection();
 			if (put) connection.setRequestMethod("PUT");
 			else connection.setRequestMethod("POST");
 			connection.setRequestProperty("Content-Type", 
 					"application/x-www-form-urlencoded");
 
 			connection.setRequestProperty("Content-Length", "" + 
 					Integer.toString(urlParameters.getBytes().length));
 			connection.setRequestProperty("Content-Language", "en-US");  
 
 			connection.setUseCaches (false);
 			connection.setDoInput(true);
 			connection.setDoOutput(true);
 
 			//Send request
 			DataOutputStream wr = new DataOutputStream (
 					connection.getOutputStream ());
 			wr.writeBytes(urlParameters);
 			wr.flush ();
 			wr.close ();
 
 			//Get Response	
 			InputStream is = connection.getInputStream();
 			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 			String line;
 			StringBuffer response = new StringBuffer(); 
 			while((line = rd.readLine()) != null) {
 				response.append(line);
 				response.append('\r');
 			}
 			rd.close();
 			return response.toString();
 
 		} catch (Exception e) {
 			System.err.println("Problem with POST request:" + e);
 			if(e.toString().contains("authentication challenge is null")){
 				return "invalid";
 			}else if(e.toString().contains("UnknownHostException")){
 				return "connection";
 			}
 			return "failed";
 		
 		} finally {
 
 			if(connection != null) {
 				connection.disconnect(); 
 			}
 		}
 	}
 
 	//get from server
 	private  String get(String targetURL) {
 		System.out.println("Get Command:");
 		URL url;
 		HttpURLConnection connection = null; 
 		if (auth_token != null) targetURL += "?auth_token=" + auth_token;
 		try {
 			System.out.println(targetURL);
 			//Create connection
 			url = new URL(targetURL);
 			connection = (HttpURLConnection)url.openConnection();
 			connection.setRequestMethod("GET");
 
 			//Get Response	
 			InputStream is = connection.getInputStream();
 			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 			String line;
 			StringBuffer response = new StringBuffer(); 
 			while((line = rd.readLine()) != null) {
 				response.append(line);
 				response.append('\r');
 			}
 			rd.close();
 			System.out.println();
 			return response.toString();
 		} catch (Exception e) {
 			System.err.println("Problem with GET request " + e);
 			return null;
 		} finally {
 			if(connection != null) {
 				connection.disconnect(); 
 			}
 		}
 	}
 
 	//delete from server
 	private  void delete(String targetURL) {
 		System.out.println("Delete Command:");
 		if (auth_token != null) targetURL += "?auth_token=" + auth_token;
 		URL url;
 		System.out.println(targetURL);
 		try {
 			url = new URL(targetURL);
 			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
 			httpCon.setDoOutput(true);
 			httpCon.setRequestProperty(
 					"Content-Type", "application/x-www-form-urlencoded" );
 			httpCon.setRequestMethod("DELETE");
 			httpCon.connect();
 			@SuppressWarnings("unused")
 			int responseCode = httpCon.getResponseCode();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println();
 	}
 
 	public void updateMyFlat(Flat flat) {
 		String flatInfo = flat.toHTTPString();
 		put(server + "/flats/m", flatInfo);
 	}
 
 	public void ignoreMember(int id) {
		post(server + "/flats/m/"+id+"/review", "user[approve]=false" );		
 	}
 
 	public void approveMember(int id) {
		post(server + "/flats/m/"+id+"/review", "user[approve]=true" );
 	}
 }
