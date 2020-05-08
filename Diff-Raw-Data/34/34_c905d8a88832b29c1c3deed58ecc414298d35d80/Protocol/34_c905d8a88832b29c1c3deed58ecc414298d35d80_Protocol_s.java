 package com.mutableconst.protocol;
 
 import java.util.HashMap;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Singleton class for encoding requests/responses to and from JSON
  * 
  * Basic commands go <COMMAND>:<DATA> For example:
  * { "TYPE":"TEXT_MESSAGE_TYPE" 
  *   "NAME":"Nicholas"
  *   "PHONE":"1-800-999-9999"
  *   "MESSAGE":"Slenderman is in the woods"
  * }
  * 
  * { "TYPE":"CONTACT_REQUEST_TYPE"
  *   "CONTACTS":"Casey Foster|1-262-555-8989,Nicholas|1-994-555-2121" 
  * }
  * 
  */
 
 public class Protocol
 {
 	private static Protocol protocol = null;
 
 	//JSON Keys
	private final String TYPE = "TYPE";
	private final String PHONE = "PHONE";
	private final String NAME = "NAME";
	private final String MESSAGE = "MESSAGE";
	private final String CONTACTS = "CONTACTS";
 
 	//Header Types
	private final String TEXT_MESSAGE_TYPE = "TEXT_MESSAGE_TYPE";
	private final String CONTACT_REQUEST_TYPE = "CONTACT_REQUEST_TYPE";
 
 	private Protocol()
 	{}
 
 	public static Protocol getProtocol()
 	{
 		if (protocol == null) {
 			protocol = new Protocol();
 		}
 		return protocol;
 	}
 
 	public String encodeSendTextMessage(String phone, String message)
 	{
 		JSONObject json = new JSONObject();
 		//Put  name in here
 		json.put(PHONE, phone);
 		json.put(MESSAGE, message);
 		json.put(TYPE, TEXT_MESSAGE_TYPE);
 		return json.toString();
 	}
 
 	public String encodeContactRequest()
 	{
 		return new JSONObject().put(TYPE, CONTACT_REQUEST_TYPE).toString();
 	}
 
 	public HashMap<String, String> decodeRawRequest(String jsonString)
 	{
 		JSONObject json = new JSONObject(jsonString);
 		String requestHeader = json.getString(TYPE);
 		//System.out.println("Decoding Raw Object: " + requestHeader);
 		switch (requestHeader) {
 			case TEXT_MESSAGE_TYPE:
 				return decodeReceiveTextMessage(json);
 			case CONTACT_REQUEST_TYPE:
 				return decodeContactRequest(json);
 		}
 		return null;
 	}
 
 	private HashMap<String, String> decodeReceiveTextMessage(JSONObject json)
 	{
 		try {
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put(MESSAGE, json.getString(MESSAGE));
 			//map.put(NAME, json.getString(NAME));
 			map.put(PHONE, json.getString(PHONE));
 			return map;
 		} catch (JSONException e) {
			System.out.println("Unable to decode receive text message. Probably a misformatted JSON string");
 			e.printStackTrace();
			return null;
 		}

 	}
 
 	private HashMap<String, String> decodeContactRequest(JSONObject json)
 	{
 		try {
 			System.out.println(json.getString(CONTACTS));
 		} catch(Exception e) {
			
 		}
 		return null;
 	}
 
 }
