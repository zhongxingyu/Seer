 package com.ijg.darklight.sdk.web;
 
 import java.util.HashMap;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonObject;
 import com.ijg.darklight.sdk.web.api.APIEndpoint;
 import com.ijg.darklight.sdk.web.api.APIRequest;
 import com.ijg.darklight.sdk.web.api.DarklightWebAPI;
 
 public class DarklightWebSDK {
 	String APIProtocol;
 	String APIServer;
 	
 	int APISessionId;
 	String APISessionKey;
 	
 	private DarklightWebAPI webAPI;
 	
 	JsonObject lastRequestResponse;
 	
 	/**
 	 * Empty constructor, requires the APIProtocol, APIServer, and APISessionId to be set manually,
 	 * and then to initiate the API call createAPI()
 	 */
 	public DarklightWebSDK() {}
 	
 	/**
 	 * 
 	 * @param APIProtocol Protocol the API server uses, usually http
 	 * @param APIServer Web address of the API server
 	 * @param APISessionId ID of the API session
 	 */
 	public DarklightWebSDK(String APIProtocol, String APIServer, int APISessionId) {
 		this.APIProtocol = APIProtocol;
 		this.APIServer = APIServer;
 		this.APISessionId = APISessionId;
 		if (!createAPI()) {
 			System.err.println("[DarklightWebSDK] error creating API");
 		}
 	}
 	
 	/**
 	 * Utility to turn a HashMap into a json string
 	 * @param map HashMap to be converted
 	 * @return Json string generated from the HashMap
 	 */
 	public String toJsonString(HashMap<?, ?> map) {
 		Gson gson = new GsonBuilder().setPrettyPrinting().create();
 		return gson.toJson(map);
 	}
 	
 	/**
 	 * Send an authorization request to the server
 	 * @param name The name to authorize
 	 * @return True if the request returned with a 200 status code
 	 */
 	public boolean auth(String name) {
 		HashMap<String, String> parameters = new HashMap<String, String>();
 		parameters.put("id", String.valueOf(APISessionId));
 		parameters.put("name", name);
 		
 		APIRequest authRequest = webAPI().individualSessionRequest(APIEndpoint.AUTH, parameters);
 		authRequest.send();
 		
 		lastRequestResponse = authRequest.getResponse();
 		
		int statusCode = authRequest.get("status").getAsInt();
 		if (statusCode == 200 || statusCode == 201) {
			APISessionKey = authRequest.get("sessionkey").getAsString();
 			return true;
 		}
 		switch (statusCode) {
 			case 500:
 				System.err
 						.println("[DarklightWebSDK] Server returned status code 500 to an auth request, indicating a database error");
 				break;
 			case 404:
 				System.err
 						.println("[DarklightWebSDK] Server returned status code 404 to an auth request, indicating an invalid session ID");
 				break;
 		}
 		return false;
 	}
 	
 	/**
 	 * Send a score update to the server
 	 * @param score The updated score
 	 * @param desc HashMap of fixed issues
 	 * @return True if request returns with a 200 status code
 	 */
 	public boolean update(int score, HashMap<String, String> issues) {
 		HashMap<String, String> parameters = new HashMap<String, String>();
 		parameters.put("sessionkey", APISessionKey);
 		parameters.put("score", "" + score);
 		parameters.put("issues", toJsonString(issues));
 		
 		APIRequest updateRequest = webAPI().individualSessionRequest(APIEndpoint.UPDATE, parameters);
 		updateRequest.send();
 		
 		lastRequestResponse = updateRequest.getResponse();
 		
		int statusCode = updateRequest.get("status").getAsInt();
 		if (statusCode == 200 || statusCode == 201) {
 			return true;
 		}
 		switch (statusCode) {
 			case 404:
 				System.err
 						.println("[DarklightWebSDK] Server returned status code 404 to an update request, indicating an invalid session key");
 		}
 		return false;
 	}
 	
 	/**
 	 * Check if the API is ready to be created
 	 * @return True if the APIProtocol, APIServer, and APISessionId have been set
 	 */
 	private boolean APIReady() {
 		if (APIProtocol != null && APIServer != null && APISessionId > 0) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Create a DarklightWebAPI instance
 	 * @return True if the API was instantiated
 	 */
 	public boolean createAPI() {
 		if (APIReady()) {
 			webAPI = new DarklightWebAPI(APIProtocol, APIServer);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Get the API instance
 	 * @return An instance of DarklightWebAPI
 	 */
 	public DarklightWebAPI webAPI() {
 		if (webAPI == null) {
 			createAPI();
 		}
 		return webAPI;
 	}
 
 	/**
 	 * Get the protocol used by the API server
 	 * @return The protocol used by the API server
 	 */
 	public String getAPIProtocol() {
 		return APIProtocol;
 	}
 
 	/**
 	 * Change the protocol used by the API
 	 * @param aPIProtocol The protocol used by the specified API server
 	 */
 	public void setAPIProtocol(String aPIProtocol) {
 		APIProtocol = aPIProtocol;
 	}
 
 	/**
 	 * Get the web address of the API server
 	 * @return The web address of the API server
 	 */
 	public String getAPIServer() {
 		return APIServer;
 	}
 
 	/**
 	 * Set the web address of the API server
 	 * @param aPIServer The web address of the API server
 	 */
 	public void setAPIServer(String aPIServer) {
 		APIServer = aPIServer;
 	}
 
 	/**
 	 * Get the ID of the API session
 	 * @return The ID of the API session
 	 */
 	public int getAPISessionId() {
 		return APISessionId;
 	}
 
 	/**
 	 * Change the ID of the API session
 	 * @param aPISessionId The desired ID of the API session
 	 */
 	public void setAPISessionId(int aPISessionId) {
 		APISessionId = aPISessionId;
 	}
 }
