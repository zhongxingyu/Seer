 package com.example.getconnected.REST;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpRequest;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.os.AsyncTask;
 
 /**
  * The RESTRequest class simplifies the process of making RESTful requests to the web address of choice.
  */
 public class RESTRequest extends AsyncTask<Void, Void, String>
 {
 	/** The enumeration of available HTTP request methods. */
 	public enum Method { GET, POST, PUT };
 	
 	/** The address an HTTP request will be sent to. */
 	protected String address;
 	
 	/** The method with which the HTTP request is sent. */
 	protected Method method;
 	
 	/** The ID to pass along with the event. This helps recognizing an event fired by a class that needs to handle multiple REST requests. */
 	protected String ID;
 	
 	/** The parameters sent along with the request. */
 	protected List<NameValuePair> parameters;
 	
 	/** The listener classes that need to be notified of a finished request. */
 	protected ArrayList<RESTRequestListener> eventListeners;
 	
 	/**
 	 * Overloads the RESTRequest(String address, String ID) constructor to work without the ID and method variables.
 	 * 
 	 * @param address
 	 */
 	public RESTRequest(String address)
 	{
 		this(address, "");
 	}
 	
 	/**
 	 * Overloads the RESTRequest(String address, Method method, String ID) constructor to work without the ID variable.
 	 * 
 	 * @param address,
 	 * @param ID
 	 */
 	public RESTRequest(String address, String ID)
 	{
 		this(address, Method.GET, ID);
 	}
 	
 	/**
 	 * Constructs a new RESTRequest with the address to issue the request on,
 	 * and the ID that will be sent along with the fired event to be able to recognize your event.
 	 * 
 	 * @param address
 	 * @param method
 	 * @param ID
 	 */
 	public RESTRequest(String address, Method method, String ID)
 	{
 		this.address = address;
 		this.method  = method;
 		this.ID      = ID;
 		
 		parameters = new ArrayList<NameValuePair>();
 		
 		eventListeners = new ArrayList<RESTRequestListener>();
 	}
 	
 //	/**
 //	 * The send method is an asynchronous method. After finishing, this method does not return any data.
 //	 * 
 //	 * When the RESTRequest has finished, a RESTRequestEvent is fired. This event contains the result data
 //	 * of the RESTful request.
 //	 * 
 //	 * @throws RESTRequestException
 //	 */
 //	public void send()
 //	{
 //		String url = address;
 //		
 //		if (!values.isEmpty())
 //		{
 //			url += "?";
 //			
 //			Iterator<HashMap.Entry<String, Object>> iterator = values.entrySet().iterator();
 //			
 //			while (iterator.hasNext())
 //			{
 //				HashMap.Entry<String, Object> entry = (HashMap.Entry<String, Object>) iterator.next();
 //				
 //				url += entry.getKey() + "=" + entry.getValue().toString();
 //				
 //				if (iterator.hasNext())
 //				{
 //					url += "&";
 //				}
 //			}
 //		}
 //		
 //		try
 //		{
 //			new RESTRequestIssuer().execute(new URL(url));
 //		}
 //		catch (MalformedURLException e) { }
 //	}
 	
 	/**
 	 * @return address
 	 */
 	public String getAddress()
 	{
 		return address;
 	}
 
 	/**
 	 * @param address
 	 */
 	public void setAddress(String address)
 	{
 		this.address = address;
 	}
 	
 	/**
 	 * @return method
 	 */
 	public Method getMethod()
 	{
 		return method;
 	}
 	
 	/**
 	 * @param method
 	 */
 	public void setMethod(Method method)
 	{
 		this.method = method;
 	}
 	
 	/**
 	 * @return ID
 	 */
 	public String getID()
 	{
 		return ID;
 	}
 
 	/**
 	 * @param address
 	 */
 	public void setID(String ID)
 	{
 		this.ID = ID;
 	}
 	
 	/**
 	 * @param key
 	 * @param value
 	 */
 	public void putString(String key, String value)
 	{
 		parameters.add(new BasicNameValuePair(key, value));
 	}
 	
 	/**
 	 * @param eventListener
 	 */
 	public void addEventListener(RESTRequestListener eventListener)
 	{
 		this.eventListeners.add(eventListener);
 	}
 
 //	@Override
 //	protected String doInBackground(Void... voids)
 //	{
 //		if (urls.length <= 0)
 //		{
 //			return "";
 //		}
 //		
 //		try
 //		{				
 //			// Open http connection
 //			HttpURLConnection urlConnection = (HttpURLConnection) urls[0].openConnection();
 //			
 //			InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
 //
 //			// Trick to read all data from a stream in one line: https://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
 //			Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
 //			
 //			String result = "";
 //			
 //			while (scanner.hasNext())
 //			{
 //				result += scanner.next();
 //			}
 //			
 //			scanner.close();
 //			
 //			inputStream.close();
 //			
 //			urlConnection.disconnect();
 //
 //			return result;
 //		}
 //		catch (IOException e) { }
 //		
 //		return "";
 //	}
 	
 	@Override
 	protected String doInBackground(Void... voids)
 	{
 //		DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
 
 		HttpRequest httpRequest = null; 
 		
 		// Get the correct request method
 		try
 		{
 			switch (method)
 			{
 				case GET:
 					// Set URL and encode parameters
 					httpRequest = new HttpGet(address + URLEncodedUtils.format(parameters, "utf-8"));
 					break;
 	
 				case POST:
 					httpRequest = new HttpPost(address);
 					
 					// Encode parameters as entity
 					((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(parameters));
 					break;
 					
 				case PUT:
 					httpRequest = new HttpPut(address);
 					
 					// Encode parameters as entity
 					((HttpPut) httpRequest).setEntity(new UrlEncodedFormEntity(parameters));
 					break;
 					
 				default:
 					return "-1";
 			}
 		}
		catch (IllegalArgumentException | UnsupportedEncodingException e)
 		{
 			return "-1";
 		}
 		
		
 		
 		
 //		// Making HTTP request
 //		try
 //		{
 //			// check for request method
 //			if(method == "POST"){
 //				// request method is POST
 //				// defaultHttpClient
 //				DefaultHttpClient httpClient = new DefaultHttpClient();
 //				HttpPost httpPost = new HttpPost(url);
 //				httpPost.setEntity(new UrlEncodedFormEntity(params));
 //
 //				HttpResponse httpResponse = httpClient.execute(httpPost);
 //				HttpEntity httpEntity = httpResponse.getEntity();
 //				is = httpEntity.getContent();
 //
 //			}else if(method == "GET"){
 //				// request method is GET
 //				DefaultHttpClient httpClient = new DefaultHttpClient();
 //				String paramString = URLEncodedUtils.format(params, "utf-8");
 //				url += "?" + paramString;
 //				HttpGet httpGet = new HttpGet(url);
 //
 //				HttpResponse httpResponse = httpClient.execute(httpGet);
 //				HttpEntity httpEntity = httpResponse.getEntity();
 //				is = httpEntity.getContent();
 //			}
 //
 //		} catch (UnsupportedEncodingException e) {
 //			e.printStackTrace();
 //		} catch (ClientProtocolException e) {
 //			e.printStackTrace();
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		}
 
 		return "";
 	}
 	
 	@Override
 	protected void onPostExecute(String result)
 	{
 		for (RESTRequestListener eventListener : eventListeners)
 		{
 			// Create new RESTRequestEvent to be handled by the event listener
 			eventListener.handleRESTRequestEvent(new RESTRequestEvent(this, result, ID));
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	public class RESTRequestException extends Exception { private static final long serialVersionUID = -1259225635751254377L; }
 }
