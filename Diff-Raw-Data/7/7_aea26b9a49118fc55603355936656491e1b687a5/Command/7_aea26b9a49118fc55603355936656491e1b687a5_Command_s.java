 /**
  * 
  */
 package com.theisleoffavalon.mcmanager_mobile;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.simple.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.util.Log;
 
 /**
  * This class represents a command that the server API is exposing
  * 
  * @author Jacob Henkel
  */
 @SuppressLint("DefaultLocale")
 public class Command {
 
 	/**
 	 * Name of the command
 	 */
 	private String					name;
 	/**
 	 * The arguments to the command
 	 */
 	private Map<String, ArgType>	arguments;
 
 	/**
 	 * Enum that defines the Type of an argument
 	 * 
 	 * @author henkelj
 	 */
 	public enum ArgType {
 		STRING, INT, PLAYER;
 
 		/**
 		 * Converts a string into an instance of the enuum
 		 * 
 		 * @param sArgType
 		 *            The string to convert
 		 * @return The appropriate argument
 		 */
 		@SuppressLint("DefaultLocale")
 		public static ArgType getArgTypeFromString(String sArgType) {
 			String arg = sArgType.toLowerCase();
 			if (arg.equals("string")) {
 				return STRING;
 			} else if (arg.equals("int")) {
 				return INT;
 			} else if (arg.equals("player")) {
 				return PLAYER;
 			} else {
 				return STRING;
 			}
 		}
 	}
 
 	/**
 	 * Default ctor
 	 * 
 	 * @param name
 	 *            Name of the Command
 	 * @param arguments
 	 *            The arguments
 	 */
 	public Command(String name, Map<String, ArgType> arguments) {
 		this.name = name;
 		this.arguments = new HashMap<String, ArgType>();
 		this.arguments.putAll(arguments);
 
 	}
 
 	/**
 	 * A JSON RPC version of this command
 	 * 
 	 * @param params
 	 *            The parameters to be executed
 	 * @return A JSONObject with the JSON RPC values set
 	 */
 	@SuppressWarnings("unchecked")
 	public JSONObject createJSONObject(Map<String, Object> params) {
 		JSONObject json = new JSONObject();
 		JSONObject parameters = new JSONObject();
 		for (Object key : params.keySet()) {
 			if (checkParamType((String) key, params.get(key))) {
 				parameters.put(key, params.get(key));
 			} else {
 				throw new IllegalArgumentException("Bad parameter types on "
 						+ key);
 			}
 		}
 		UUID id = UUID.randomUUID();
		
 		json.put("jsonrpc", JSONRpcValues.JSON_RPC_VERSION);
 		json.put("id", id.toString());
 		json.put("method", this.name);
 		json.put("params", parameters);
		
 		Log.d("Command", "Created Command JSON of " + json.toJSONString());
 		return json;
 	}
 
 	/**
 	 * Checks to make sure the value is of the correct type
 	 * 
 	 * @param key
 	 *            The argument name
 	 * @param value
 	 *            The value to be passed in for the argument
 	 * @return True if the value is of the correct type
 	 */
 	private boolean checkParamType(String key, Object value) {
 		boolean ret = false;
 		ArgType expected = this.arguments.get(key);
 		switch (expected) {
 			case STRING:
 			case PLAYER:
 				ret = value instanceof String;
 				break;
 			case INT:
 				ret = value instanceof Integer;
 				break;
 			default:
 				break;
 		}
 		return ret;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return this.name;
 	}
 
 	/**
 	 * @param name
 	 *            the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the arguments
 	 */
 	public Map<String, ArgType> getArguments() {
 		Map<String, ArgType> map = new HashMap<String, ArgType>();
 		map.putAll(this.arguments);
 		return map;
 	}
 
 	/**
 	 * @param arguments
 	 *            the arguments to set
 	 */
 	public void setArguments(Map<String, ArgType> arguments) {
 		this.arguments.putAll(arguments);
 	}
 
 }
