 package com.adaptc.mws.plugins;
 
 import net.sf.json.JSON;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONNull;
 import net.sf.json.JSONObject;
 import org.springframework.mock.web.MockHttpServletResponse;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This class is used in the {@link IMoabRestService} to represent a response from MWS.
  * It contains fields for the response, data, and metadata methods about the response.
  * @author bsaville
  */
 public class MoabRestResponse {
 	private final MockHttpServletResponse response;
 	private final JSON data;
 	private final boolean success;
 
 	/**
 	 * Constructs a new response from MWS.
 	 * @param response The HTTP servlet response
 	 * @param data The data as a Map or JSON, will be converted correctly in either case
 	 * @param success Whether or not the response is a success
 	 *
 	 * @throws IllegalArgumentException If the data param is not a Map or JSON object
 	 */
 	public MoabRestResponse(MockHttpServletResponse response, Object data, boolean success) {
 		this.response = response;
 		// Properly convert if JSON or not
 		if (data==null || data instanceof JSON)
 			this.data = (JSON)data;
 		else if (data instanceof Map) {
 			JSONObject json = new JSONObject();
 			json.putAll((Map)data);
 			this.data = json;
 		} else
 			throw new IllegalArgumentException("Argument [data] is not a valid type of Map or JSON");
 		this.success = success;
 	}
 	
 	/**
 	 * The parsed JSON body data from the response.
 	 * @return The parsed JSON body
 	 */
 	public JSON getData() {
 		return data;
 	}
 	
 	/**
 	 * The actual HTTP response object.  This may be used to return any pertinent information from
 	 * the response including headers and content type information.
 	 * @return The HTTP response
 	 */
 	public MockHttpServletResponse getResponse() {
 		return response;
 	}
 
 	/**
 	 * The status code of the HTTP response (i.e. 200, 404, 400) in a convenience method.
 	 * Equivalent to calling getResponse().getStatus().
 	 * @return The HTTP response code
 	 */
 	public int getStatus() {
 		return response.getStatus();
 	}
 	
 	/**
 	 * Returns if the request was successful or not.
 	 * @return True if a 2xx or 3xx HTTP code was encountered or false otherwise
 	 */
 	public boolean isSuccess() {
 		return success;
 	}
 	
 	/**
 	 * Returns true if an error was encountered during the request.
 	 * @return True if the HTTP code is 400 or greater, false otherwise
 	 */
 	public boolean hasError() {
 		return !success;
 	}
 
 	/**
 	 * Returns data converted to an actual Map or List (or null) instead of a {@link JSON} instance.
 	 * @return The equivalent of {@link #getData()} converted to a simple Map or List
 	 */
 	public Object getConvertedData() {
 		return convert(data);
 	}
 
 	/**
 	 * Utility method for converting {@link #data} from a {@link JSON} instance.
 	 * @param json
 	 * @return
 	 */
 	private Object convert(JSON json) {
		if (json instanceof JSONObject)
			return convert((JSONObject)json);
		else if (json instanceof JSONArray)
			return convert((JSONArray)json);
 		else
 			return null;
 	}
 
 	/**
 	 * Recursive utility method for converting {@link #data} from a {@link JSON} instance.
 	 * @param jsonObject
 	 * @return
 	 */
 	private Map convert(JSONObject jsonObject) {
 		Map obj = new HashMap();
 		for (Object k : jsonObject.keySet()) {
 			Object v = jsonObject.get(k);
 			if (v instanceof JSON)
 				obj.put(k, convert((JSON)v));
 			else
 				obj.put(k, v);
 		}
 		return obj;
 	}
 
 	/**
 	 * Recursive utility method for converting {@link #data} from a {@link JSON} instance.
 	 * @param jsonArray
 	 * @return
 	 */
 	private List convert(JSONArray jsonArray) {
 		List list = new ArrayList();
 		for(Object v : jsonArray) {
 			if (v instanceof JSON)
 				list.add(convert((JSON)v));
 			else
 				list.add(v);
 		}
 		return list;
 	}
 
 	/**
 	 * Recursive utility method for converting {@link #data} from a {@link JSON} instance.
 	 * @param jsonNull
 	 * @return
 	 */
 	private Object convert(JSONNull jsonNull) {
 		return null;
 	}
 }
