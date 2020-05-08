 package edu.fiu.cs.seniorproject.client;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
import java.util.Calendar;
 
 import edu.fiu.cs.seniorproject.config.AppConfig;
 import edu.fiu.cs.seniorproject.utils.Logger;
 
 import android.os.Bundle;
 
 public class MBVCAClient extends RestClient{
 
 	private static final String BASE_API = "http://www.miamibeachapi.com/api/index.php/";
 	
 	public MBVCAClient(String appId)
 	{
 		super(appId);
 	}
 	
 	public String getEventList() {
 		Bundle params = getBundle();
 		
 		// read today events by default
		Calendar calendar = Calendar.getInstance();
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.HOUR_OF_DAY);
		
		long unixtimestamp = calendar.getTimeInMillis() / 1000L;
 		String query = String.format("{\"start_time\":{\"$gt\":%d,\"$lt\":%d},\"datatable_category_id\":{\"$exists\":true},\"calendar_id\":1}", unixtimestamp, unixtimestamp + (24 * 60 * 60 ) );
 		Logger.Debug("Query string in MBVCA = " + query);
 		params.putString("qry", query);
 		params.putString("srt", "{\"start_time\":1}" );
 		
 		String response = null;
 		try {
 			return openUrl( BASE_API + "search/solodev_view", GET, params );
 		} catch (MalformedURLException e) {
 			Logger.Warning("Malformed exception getting MBVCA events " + e.getMessage() );
 		} catch (IOException e) {
 			Logger.Warning("IO exception getting MBVCA events " + e.getMessage() );
 		}
 		return response;
 	}
 	/**
 	 * 
 	 * @param reference
 	 * @param eventId
 	 * @return String
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	public String getEvent(String reference, String eventId) throws MalformedURLException, IOException
 	{
 		/*
 		 * 	http://www.miamibeachapi.com/api/index.php/search/solodev_view?qry={start_time:{$gt:CURRENTTIME},datatable_category_id:{$exists:true},%20calendar_id:1}
 		 * 
 		 * 	https://maps.googleapis.com/maps/api/place/event/details/format?sensor=true_or_false
   		 *	&key=api_key
   		 *	&reference=CnRkAAAAGnBVNFDeQoOQHzgdOpOqJNV7K9-etc
   		 *	&event_id=weafgerg1234235
 		 */
 		Bundle params = this.getBundle();
 		
 		if(reference != null)
 			params.putString("reference", reference);
 		if(eventId != null)
 			params.putString("event_id", eventId);
 		
 		String result = null;
 		String url = "http://www.miamibeachapi.com/api/index.php/search/solodev_view?qry={start_time:{$gt:CURRENTTIME},datatable_category_id:{$exists:true},%20calendar_id:1}"; //"https://maps.googleapis.com/maps/api/place/event/details/json";
 		String method = "GET";
 
 		try
 		{
 			result = this.openUrl(url, method, params);
 		
 		}catch(MalformedURLException e)
 		{
 			Logger.Error("MBVCAClient getEvents MalformedURLException");
 		}catch(IOException e)
 		{
 			Logger.Error("MBVCAClient getEvents IOException");
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param longitude
 	 * @param latitude
 	 * @param radius
 	 * @param types
 	 * @param name
 	 * @return String
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	public String getPlaces(String longitude, String latitude, String radius, String types, String name) throws MalformedURLException, IOException
 	{
 	   /*
 	    * https://maps.googleapis.com/maps/api/place/search/json?
 		* location=-33.8670522,151.1957362&radius=500&types=food&name=harbour&
 		* sensor=false&key=AddYourOwnKeyHere
 		*/
 
 
 		Bundle params = this.getBundle();
 		
 		if(longitude != null)
 			params.putString("longitude", longitude);
 		if(latitude != null)
 			params.putString("latitude", latitude);
 		if(radius != null)
 			params.putString("radius",radius);
 		if(types != null)
 			params.putString("types",types);
 		if(name != null)
 			params.putString("name",name);
 		
 		params.putString("sensor","false");
 		
 		String result = null;
 		String url = "http://www.miamibeachapi.com/api/index.php/search/solodev_view?qry={start_time:{$gt:CURRENTTIME},datatable_category_id:{$exists:true},%20calendar_id:1}";
 		String method = "GET";
 		
 		try
 		{
 			result = this.openUrl(url, method, params);
 		
 		}catch(MalformedURLException e)
 		{
 			Logger.Error("MBVCAClient getPlaces MalformedURLException");
 		}catch(IOException e)
 		{
 			Logger.Error("MBVCAClient getPlaces IOException");
 		}
 		
 		return result;
 	}
 	
 	public Bundle getBundle()
 	{
 		Bundle bundle = new Bundle();
 		bundle.putString("token", AppConfig.MBVCA_TOKEN);
 		bundle.putString("token_secret", AppConfig.MBVCA_TOKEN_SECRET);
 		return bundle;
 	}
 	
 }
