 package edu.drexel.cs544.mcmuc.actions;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.drexel.cs544.mcmuc.Certificate;
 import edu.drexel.cs544.mcmuc.JSON;
 
 /**
 * A chat room message action must carry a "from" field, which is a nickname that identifies 
 * the sender, and a "body" field that carries the user's input. Messages can also optionally
 *  contain a "to" value, for which clients can choose to hide from the user messages not 
  *  meant for them. Messages can also be optionally encrypted with the public key certificate 
 *  specified by "key". Using "to" and "key" together allows clients to avoid checking the "key" 
  *  against their own keystore, as they will know the messages was not directed at them.
  *  
  *  The possible JSON formats are:
  *  {'action':'message','from':'<from>','body':'<body>'}
  *  {'action':'message','from':'<from>','body':'<body>','to','<to>'}
  *  {'action':'message','from':'<from>','body':'<body>','key','<key>'}
  *  {'action':'message','from':'<from>','body':'<body>','to','<to>','key','<key>'}
  *  
  */
 public class Message extends Action implements JSON {
 
 	private String from;
 	private String body;
 	private String to;
 	private Boolean hasTo;
 	private Certificate key;
 	private Boolean hasKey;
 	
 	/**
 	 * Initializes the Message class. If either to or key are null, then hasTo() or hasKey() will return
 	 * false, respectively.
 	 * @param from String from
 	 * @param body String body
 	 * @param to String to
 	 * @param key Certificate key
 	 */
 	private void init(String from, String body, String to, Certificate key)
 	{
 		this.from = from;
 		this.body = body;
 		if(to != null)
 		{
 			this.to = to;
 			this.hasTo = true;
 		}
 		else
 			this.hasTo = false;
 		
 		if(key != null)
 		{
 			this.key = key;
 			this.hasKey = true;
 		}
 		else
 			this.hasKey = false;
 	}
 	/**
 	 * Calls init() to create message with a from and body but not to or certificate
 	 * @param from String from
 	 * @param body String body
 	 */
 	public Message(String from, String body)
 	{
 		init(from,body,null,null);
 	}
 	
 	/**
 	 * Calls init() to create message with a from, body, and to but no certificate
 	 * @param from String from
 	 * @param body String body
 	 * @param to String to
 	 */
 	public Message(String from, String body, String to)
 	{
 		init(from,body,to,null);
 	}
 	
 	/**
 	 * Calls init() to create message with a from, body, and certificate but no to
 	 * @param from String from
 	 * @param body String body
 	 * @param key Certificate key
 	 */
 	public Message(String from, String body, Certificate key)
 	{
 		init(from,body,null,key);
 	}
 	
 	/**
 	 * Calls init() to create message with a from, body, to, and certificate
 	 * @param from String from
 	 * @param body String body
 	 * @param to String to
 	 * @param key Certificate key
 	 */
 	public Message(String from, String body, String to, Certificate key)
 	{
 		init(from,body,to,key);
 	}
 	
 	/**
 	 * Deserializes JSON into a Message object. If the JSON has to or key key/value pairs, the object's 
 	 * to or key attributes are filled in and hasTo() or hasKey() return true; otherwise they return false.
 	 * @param json the JSONObject
 	 */
 	public Message(JSONObject json)
 	{
 		super(json,"message");
 		try {		
 			String from = json.getString("from");
 			String body = json.getString("body");
 			
 			String to = null;
 			Certificate key = null;
 			
 			if(json.has("to"))
 				to = json.getString("to");
 			
 			if(json.has("key"))
 				key = new Certificate(json.getJSONObject("key"));
 			
 			init(from,body,to,key);
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Accessor for message's from field
 	 * @return String from
 	 */
 	public String getFrom() {
 		return from;
 	}
 
 	/**
 	 * Accessor for message's body field
 	 * @return String body
 	 */
 	public String getBody() {
 		return body;
 	}
 
 	/**
 	 * Accessor for message's (optional) to field
 	 * @return String to (null if hasTo() returns false)
 	 */
 	public String getTo() {
 		return to;
 	}
 
 	/**
 	 * Determine if message's optional to field is filled
 	 * @return true if message has 'to' address, false otherwise
 	 */
 	public Boolean hasTo() {
 		return hasTo;
 	}
 
 	/**
 	 * Accessor for message's Certificate key field
 	 * @return Certificate key (null if hasKey() returns false)
 	 */
 	public Certificate getKey() {
 		return key;
 	}
 
 	/**
 	 * Determine if message's optional key field is filled
 	 * @return true if message has Certificate key, false otherwise
 	 */
 	public Boolean hasKey() {
 		return hasKey;
 	}
 	
 	/**
 	 * Serializes the Message object to JSON. The to and key key/value pairs are only included if
 	 * the object was created with a to or key attribute.
 	 */
 	@Override
 	public JSONObject toJSON() {
 		JSONObject json = new JSONObject();
 		
 		try {
 			json.put("action", "message");
 			json.put("uid", uid);
 			json.put("from", from);
 			json.put("body", body);
 			if(hasTo)
 				json.put("to", to);
 			if(hasKey)
 				json.put("key", key.toJSON());
 		} catch (JSONException e) {
 
 		}
 		
 		return json;
 	}
 	
 }
