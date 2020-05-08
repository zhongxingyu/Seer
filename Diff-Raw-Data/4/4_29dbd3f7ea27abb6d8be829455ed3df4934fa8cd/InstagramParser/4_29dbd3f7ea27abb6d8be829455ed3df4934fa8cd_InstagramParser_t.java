 package external;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import geometry.Geometry;
 import geometry.Point;
 
 import models.*;
 
 
 
 
 public class InstagramParser {
 	
 	
 	
 	
 	
 	
 	public static Feature getInstaByMediaId(String id) 
 	{
 		String url = "https://api.instagram.com/v1/media/"+id+"?client_id=a80dd450be84452a91527609a4eae97b";
 		String file = isRequestSuccessful(url);
 		if (file != null) {
 			
 			ObjectMapper mapper = new ObjectMapper();
 			JsonNode actualObj;
 			try {
 				actualObj = mapper.readTree(file);
 				if (actualObj.findPath("meta").get("code").toString().equalsIgnoreCase("200")) {
 					Feature geoJSON = instaToGeoJson(actualObj.findValue("data"));
 					return geoJSON;
 				}
 				else {
 					return new Feature();
 				}
 			} catch (JsonProcessingException e) {
 				e.printStackTrace();
 				return new Feature();
 			} catch (IOException e) {
 				e.printStackTrace();
 				return new Feature();
 			}
 			// add validation if the response type is not 200 then skip the rest process
 			
 		};
 
 		return new Feature();
 			
 	}
 	
 	
 	
 	
 	public static List<Feature> searchRecentInstaFeatures(double lng, double lat) throws Exception
 	{
 		long unixTime = System.currentTimeMillis() / 1000L;
 		
		String url = "https://api.instagram.com/v1/media/search?" +
				//"max_timestamp="+unixTime+
				"client_id=a80dd450be84452a91527609a4eae97b"
 				+"&lat="+lat+"&lng="+lng;
 		
 		/*
 
 				HashMap<String, String> params = new HashMap<String, String>();
 		params.put(Param.LAT, String.valueOf(center[0]));
 		params.put(Param.LNG, String.valueOf(center[1]));
 		params.put(Param.DIST, String.valueOf(radius_outer_circle_of_reactangle));
 		params.put(Param.CLIENTID, "a80dd450be84452a91527609a4eae97b");
 		 * */
 		
 		String file = isRequestSuccessful(url);
 		if (file != null) {
 			ObjectMapper mapper = new ObjectMapper();
 			JsonNode actualObj = mapper.readTree(file);
 			if (actualObj.findPath("meta").get("code").toString()
 					.equalsIgnoreCase("200")) 
 			{
 				List<Feature> geoJSON = onResponseReceived(actualObj);
 				return geoJSON;
 
 			} else {
 				return new ArrayList<Feature>();
 			}
 			
 		}
 	
 		return new ArrayList<Feature>();
 
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public static List<Feature> searchInstaPOIsByBBox(double lng1, double lat1,double lng2, double lat2) 
 	{
 		String describeService = "https://api.instagram.com/v1/media/search";
 
 		String url;
 		//String file;
 
 		try {
 			url = buildRequest(describeService, lng1, lat1, lng2, lat2);
 
 			try {
 					String file = isRequestSuccessful(url);
 				if (file != null) {
 					ObjectMapper mapper = new ObjectMapper();
 					JsonNode actualObj;
 					try {
 						actualObj = mapper.readTree(file);
 						if (actualObj.findPath("meta").get("code").toString()
 								.equalsIgnoreCase("200")) {
 							List<Feature> geoJSON = onResponseReceived(actualObj);
 							return geoJSON;
 
 						} else {
 							return new ArrayList<Feature>();
 						}
 
 					} catch (JsonProcessingException e) {
 						//
 						e.printStackTrace();
 					} catch (IOException e) {
 						//  Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 				}
 
 
 			} catch (Exception e) {
 				//  Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} catch (UnsupportedEncodingException e) {
 			//  Auto-generated catch block
 			e.printStackTrace();
 			
 		}
 
 		return new ArrayList<Feature>();
 	}
 		
 		//: need to optimize for speed
 //		return async(
 //			      WS.url(url).get().map(
 //			        new Function<WS.Response, Result>() {
 //			          public Result apply(WS.Response response) {
 //			            return ok(response.asJson());
 //			          }
 //			        }
 //			      )
 //			    ); 
 		
 		//return redirect(url);
 		//return ok(url.toString());
 		//List<Feature> geoJSON = new Arr
 		
 		// : add validation if the response type is not 200 then skip the
 		// rest process
 		
 
 		
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public static List<Feature> onResponseReceived(JsonNode json)
 	{
 	
 		JsonNode insta_feeds = json.findValue("data");
 		List<Feature>features = new ArrayList<Feature>();
 		
 		
 		for (int i = 0; i < insta_feeds.size(); i++) 
 		{
 			JsonNode childNode = insta_feeds.get(i);
 			Feature feature = instaToGeoJson(childNode);
 			
 			features.add(feature);
 			
 		}
 		
 		
 		return features;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public static List<Feature> searchInstaPOIsByTag(String tag) throws Exception
 	{
 		
 		String url = "https://api.instagram.com/v1/tags/"+tag+"/media/recent?client_id=a80dd450be84452a91527609a4eae97b";
 		
 		String file = isRequestSuccessful(url);
 		if (file != null) {
 			ObjectMapper mapper = new ObjectMapper();
 			JsonNode actualObj = mapper.readTree(file);
 			if (actualObj.findPath("meta").get("code").toString()
 					.equalsIgnoreCase("200")) 
 			{
 				List<Feature> geoJSON = onResponseReceived(actualObj);
 				return geoJSON;
 
 			} else {
 				return new ArrayList<Feature>();
 			}
 			
 		}
 	
 		return new ArrayList<Feature>();
 
 	}
 	
 	
 	
 	
 	
 	
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 
 	/**
 	 * @param jsonNode
 	 * @return
 	 */
 	private static Feature instaToGeoJson(JsonNode jsonNode) 
 	{
 		//String type  = jsonNode.findValue("type").asText();
 		
 		String id  = jsonNode.get("id").asText();
 		JsonNode location = jsonNode.findValue("location");
 		
 		
 		Geometry geometry;
 		//check if location node is not null
 		if (!location.isNull()) {
 			double latitude = location.findValue("latitude").asDouble();
 			double longitude = location.findValue("longitude").asDouble();
 			 geometry = new Point(longitude, latitude);
 		}
 		else {
 			geometry = new Point();
 		}
 		
 		Feature feature = new Feature(geometry);
 		feature.id = id;
 		
 		HashMap<String, Object> properties = new HashMap<String, Object>();
 		
 		
 		properties.put("descr_url", Constants.SERVER_NAME_T+"/instagram/"+id);
 		properties.put("icon_url", Constants.SERVER_NAME_T+"/assets/img/"+"instagram.png");
 
 		
 		properties.put("created_time", jsonNode.get("created_time").asLong());
 		properties.put("source_type", "Instagram");
 		
 		//save url to both standard and instagram image
 		//JsonNode image = jsonNode.findPath("images");
 		//properties.put("images", image);
 		
 		JsonNode image = jsonNode.findPath("standard_resolution");
 		String standard_resolution = image.get("url").asText();
 		properties.put("standard_resolution", standard_resolution);
 
 		
 		
 		
 		JsonNode caption = jsonNode.findPath("caption");
 		properties.put("description", caption.findValue("text"));
 		
 		if (!(caption.isNull())) {
 			String description = caption.findValue("text").asText();
 			
 			//Formulate the label of the POI, using first sentence in the description
 			String delims = "[.,?!]+";
 			String[] tokens = description.split(delims);
 			String name = tokens[0];
 			properties.put("name", name);
 			
 		}
 		else {
 			properties.put("name", "");
 		}
 		
 		
 		JsonNode tags = jsonNode.findValue("tags");
 		properties.put("tags", tags);
 		
 		JsonNode user = jsonNode.findValue("user");
 		properties.put("user", user);
 		
 		feature.setProperties(properties);
 		
 		//Save instagram features to local db
 		/*if (Feature.find().byId(id) == null) {
 			feature.insert();
 		}*/
 		
 		
 		return feature;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 	public static String buildRequest(String describeService, double lng1,
 			double lat1, double lng2, double lat2) throws UnsupportedEncodingException 
 	{
 		
 		double center[] = new double[2];
 		center[0] = (lat1 + lat2) / 2;
 		center[1] = (lng1 + lng2) / 2;
 		
 		double radius_outer_circle_of_reactangle = radius(lat1,lng1,lat2,lng2);
 		
 		HashMap<String, String> params = new HashMap<String, String>();
 		params.put(Param.LAT, String.valueOf(center[0]));
 		params.put(Param.LNG, String.valueOf(center[1]));
 		params.put(Param.DIST, String.valueOf(radius_outer_circle_of_reactangle));
 		params.put(Param.CLIENTID, "a80dd450be84452a91527609a4eae97b");
 
 		
 		// construct URL
 		StringBuffer paramsBuffer = new StringBuffer();
 		if (params.keySet().size() > 0) {
 			boolean isFirstParam = true;
 			for (Iterator<String> keys = params.keySet().iterator(); keys.hasNext();) {
 				String key = (String) keys.next();
 				if (isFirstParam) {
 					paramsBuffer.append("?" + key);
 					isFirstParam = false;
 				} else {
 					paramsBuffer.append("&" + key);
 				}
 				paramsBuffer.append("="
 						+ URLEncoder.encode(
 								(String) params.get(key),
 								"UTF-8"));
 			}
 		}
 		
 		StringBuffer url =   new StringBuffer();
 		url.append(describeService);
 		url.append(paramsBuffer);
 		
 		
 		return url.toString();
 	}
 	
 	
 	
 	
 	
 	
 
 	
 	
 	
 	
 	
 	/**
 	 * Calculates the area of the extent
 	 * 
 	 * @return The radius of outer circle of BoundingBox
 	 */
 	public static double radius(double lat1, double lng1, double lat2, double lng2) {
 		
 		double earthRadius = 6371; // km
 		double distant = Math.acos(Math.sin(lat1)*Math.sin(lat2) + 
 		                  Math.cos(lat1)*Math.cos(lat2) *
 		                  Math.cos(lng2-lng1)) * earthRadius;
 		
 		return distant;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	/**
 	 * Calculates the width of the extent
 	 * 
 	 * @return The width of the extent
 	 */
 	public double getWidth(double maxX, double minX) {
 		return Math.abs(maxX - minX);
 	}
 
 	
 	
 	/**
 	 * Calculates the height of the extent
 	 * 
 	 * @return The height of the extent
 	 */
 	public  double getHeight(double maxY, double minY) {
 		return Math.abs(maxY - minY);
 	}
 	
 	
 	
 	
 
 	
 	/**
 	 * Calculates the area of the extent
 	 * 
 	 * @return The area
 	 */
 	public double area(double maxX, double minX,double maxY, double minY) {
 		return getWidth(maxX,minX) * getHeight(maxY,minY);
 	}
 	
 	
 	
 	
 	
 	
 
 	/**
 	 * Calls
 	 * {@link Downloader #downloadFromUrl(String, es.prodevelop.gvsig.mini.utiles.Cancellable)}
 	 * 
 	 * @param url
 	 *            The url to request to
 	 * @return The data downloaded
 	 * @throws Exception
 	 */
 	public static String isRequestSuccessful(String url)  {
 		// hacer peticion en segundo plano
 		Downloader d = new Downloader();
 		System.out.println(url);
 		try {
 			d.downloadFromUrl(url);
 			
 			return new String(d.getData());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 			return new String(d.getData());
 		}
 		
 	}
 	
 	
 	
 	
 }
