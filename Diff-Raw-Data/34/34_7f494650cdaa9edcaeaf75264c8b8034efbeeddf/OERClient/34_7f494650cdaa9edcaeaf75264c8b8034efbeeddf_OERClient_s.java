 package org.openexchangerates.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 import org.openexchangerates.client.RestClient.ErrorHandler;
 import org.openexchangerates.client.RestClient.HttpGETCache;
 import org.openexchangerates.client.RestClient.Response;
 import org.openexchangerates.client.RestClient.ResponseDeserializer;
 import org.openexchangerates.client.RestClient.URLBuilder;
 
 /**
  * openexchangerates.org (OER) client for Java.  Deserializes OER JSON messages into native Java types.
  * 
  * Depends on org.touge RestClient (a wrapper for HTTPUrlConnection) and org.json JSON library.
  * 
  * @author kgilmer
  *
  */
 public final class OERClient {
 	
 	/**
 	 * Date format used in composing openexchangerate.org URLs 
 	 */
 	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 	/**
 	 * Default URL for OER service.
 	 */
 	public static final String DEFAULT_OER_URL = "http://openexchangerates.org";
 	
 	/**
 	 * This API_KEY value is used only if the non-api specifying constructor is used.
 	 */
 	public static final String DEFAULT_API_KEY = "CHANGE_ME_TO_YOUR_API_KEY";
 	
 	/**
 	 * Default HTTP connection timeout.
 	 */
 	public static final int DEFAULT_CONNECT_TIMEOUT = 6000;
 	/**
 	 * Default HTTP read timeout.
 	 */
 	public static final int DEFAULT_READ_TIMEOUT = 10000;
 	
 	private final RestClient restClient;
	private final URLBuilder baseURL;
	private final URLBuilder currenciesURL;
	private final URLBuilder latestURL;
 	
 	/**
 	 * {@link OERClient} constructor with default HTTP client and no caching or debugging.  Throw any errors back to the caller.
 	 */
 	public OERClient() {
 		this(null, DEFAULT_OER_URL, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, null, RestClient.THROW_ALL_ERRORS);
 	}
 	
 	/**
 	 * {@link OERClient} constructor with defaults for everything except for cache.
 	 * @param cache
 	 */
 	public OERClient(HttpGETCache cache) {
 		this(cache, DEFAULT_OER_URL, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, null, RestClient.THROW_ALL_ERRORS);
 	}
 		
 	/**
 	 * {@link OERClient} constructor with all configuration capabilities exposed to the client.
 	 * 
 	 * @param cache cache impl or null for no caching
 	 * @param oerUrl base URL to use to connect to OER service
 	 * @param connectTimeout connection timeout
 	 * @param readTimeout stream read timeout
 	 * @param debugWriter PrintWriter to send request/response messages to.  If null no debug output will be generated.
 	 * @param errorHandler Determine how connection and deserialization errors are handled.
 	 * @param apiKey API_KEY provisioned at openexchangerates.org
 	 */
 	public OERClient(HttpGETCache cache, String oerUrl, int connectTimeout, int readTimeout, PrintWriter debugWriter, ErrorHandler errorHandler, String apiKey) {
 		this.restClient = new RestClient();
 		this.baseURL = restClient.buildURL(oerUrl).addParameter("API_KEY", apiKey);
 		this.currenciesURL = baseURL.copy("currencies.json");
 		this.latestURL = baseURL.copy("latest.json");
 		this.restClient.setCache(cache);
 		
 		if (debugWriter != null)
 			this.restClient.setDebugWriter(debugWriter);
 		
 		// By default set timeouts on connect and response.
 		restClient.addConnectionInitializer(
 				new RestClient.TimeoutConnectionInitializer(DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT));
 		
 		// By default throw all errors (Connection, Parsing) back to caller.
 		restClient.setErrorHandler(errorHandler);
 	}
 	
 	/**
 	 * @param cache
 	 * @param oerUrl
 	 * @param connectTimeout
 	 * @param readTimeout
 	 * @param debugWriter
 	 * @param errorHandler
 	 */
 	public OERClient(HttpGETCache cache, String oerUrl, int connectTimeout, int readTimeout, PrintWriter debugWriter, ErrorHandler errorHandler) {
 		this(cache, oerUrl, connectTimeout, readTimeout, debugWriter, errorHandler, DEFAULT_API_KEY);
 	}
 
 	/**
 	 * Get the latest currency rates.
 	 * 
 	 * @return Map of latest currency rates.  Key is currency, value is rate as double.
 	 * @throws IOException on I/O error
 	 * @throws JSONException on JSON parsing error
 	 */
 	public Map<String, Double> getLatestRates() throws JSONException, IOException {
 		Map<String, Double> rateMap = new HashMap<String, Double>();
 		
 		JSONObject json = callServer(latestURL.toString());
 		JSONObject rates = json.getJSONObject("rates");			
 
 		for (Iterator<String> i = rates.keys(); i.hasNext();) {
 			String key = i.next();
 			rateMap.put(key, rates.getDouble(key));
 		}					
 		
 		return rateMap;
 	}
 	
 	/**
 	 * Peform currency conversion calculation.
 	 * 
 	 * @param baseCurrency
 	 * @param quoteCurrency
 	 * @return rebased currency.
 	 */
 	public static double rebaseCurrency(double baseCurrency, double quoteCurrency) {
 		return baseCurrency * (1 / quoteCurrency);
 	}
 
 	/**
 	 * Get currency rates for a specified date.  Only M/D/Y is handled.
 	 * 
 	 * @param date Date to query rates for.
 	 * @return Map<String, Double> of rates for given date, or throws IOException on 404.
 	 * @throws IOException on I/O error
 	 * @throws JSONException on JSON parsing error
 	 */
 	public Map<String, Double> getRates(Date date) throws IOException, JSONException {
 		Map<String, Double> rateMap = new HashMap<String, Double>();
 				
 		String dateUrl = dateFormat.format(date) + ".json";		
 		URLBuilder url = baseURL.copy("historical").append(dateUrl);
 
 		JSONObject json = callServer(url.toString());
 		JSONObject rates = json.getJSONObject("rates");		
 
 		for (Iterator<String> i = rates.keys(); i.hasNext();) {
 			String key = i.next();
 			rateMap.put(key, rates.getDouble(key));
 		}
 	
 		return rateMap;		
 	}
 
 	/**
 	 * Get the latest currencies from openexchangerates.org.
 	 * 
 	 * @return Map of defined currencies with key as name and value as description.
 	 * 
 	 * @throws IOException on I/O error
 	 * @throws JSONException on JSON parsing error
 	 */
 	public Map<String, String> getCurrencies() throws IOException, JSONException {
 		Map<String, String> currencies = new HashMap<String, String>();
 		
 		JSONObject json = callServer(currenciesURL.toString());
 
 		for (Iterator<String> i = json.keys(); i.hasNext();) {
 			String key = i.next();
 			currencies.put(key, json.getString(key));
 		}
 	
 		return currencies;
 	}
 
 	/**
 	 * Get the timestamp of the latest update from openexchangerates.org.
 	 * 
 	 * @return date of latest update
 	 * @throws IOException on I/O error
 	 * @throws JSONException on JSON parsing error
 	 */
 	public Date getTimestamp() throws IOException, JSONException {
 		JSONObject json = callServer(latestURL.toString());	
 		
 		long timestamp = json.getLong("timestamp") * 1000l;
 				
 		return new Date(timestamp);		
 	}
 	
 	/**
 	 * Get JSON from server or returned cached copy if configured and available.
 	 * 
 	 * 
 	 * @param url
 	 * @return
 	 * @throws IOException
 	 */
 	private synchronized JSONObject callServer(String url) throws IOException {		
 		Response<JSONObject> response = restClient.callGet(url, new JSONObjectDeserializer());
 		JSONObject content = response.getContent();
 				
 		return content;
 	}
 
 	/**
 	 * Deserializer for JSON data using org.json class.  Used to configure the Touge rest client to return JSON objects from the HTTP message.
 	 *
 	 */
 	private static class JSONObjectDeserializer implements ResponseDeserializer<JSONObject> {
 		@Override
 		public JSONObject deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {
 			if (input.available() == 0) 
 				throw new IOException("Server returned no data.");
 			
 			String message = new String(RestClient.readStream(input));
 
 			if (message == null || message.length() == 0) 
 				throw new IOException("Server returned no data.");
 			
 			Object json;
 
 			try {
 				json = new JSONTokener(message).nextValue();
 
 				if (json instanceof JSONObject)
 					return (JSONObject) json;
 
 			} catch (JSONException e) {
 				throw new IOException(e.getMessage());
 			}
 
 			throw new IOException("Server did not return JSON object: " + message);
 		}
 	}
 }
