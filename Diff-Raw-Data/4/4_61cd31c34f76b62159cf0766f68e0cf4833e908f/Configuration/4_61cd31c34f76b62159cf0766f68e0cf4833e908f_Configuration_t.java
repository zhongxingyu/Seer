 package com.modelmetrics;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.simple.parser.JSONParser;
 
 public class Configuration {
 
 	private String _accessToken;
 	private String _accessUrl;
 	private String _endpoint;
 	private String _orgId;
 	private ArrayList<String> _images;
 	
 	public Configuration() {
 		reset();
 	}
 	
 	public ArrayList<String> getImages() {
 		if(_images.isEmpty() && _accessToken != null && _accessUrl != null) {
 			try {
 				Map<Object, Object> json = getSalesforceJSON("SELECT Id, Name from Document where Name like '%Vehicle%' limit 20");
 		        if(json.containsKey("totalSize")) {
 		        	Long size = (Long)json.get("totalSize");
 		        	if(size != null && size > 0) {
 		        		ArrayList<Map<Object, Object>> records = (ArrayList<Map<Object, Object>>)json.get("records");
 		        		for(Map<Object, Object> record : records) {
 		        			String id = record.get("Id").toString();
 		        			_images.add(_accessUrl + "/servlet/servlet.ImageServer?id=" + id + "&oid=" + getOrgId());
 		        		}
 		        	}
 		        }
 			} catch (Exception ex) {
 				ex.printStackTrace(System.err);
 			}
 		}
 		return _images;
 	}
 	
 	public String getAccessToken() {
 		return _accessToken;
 	}
 	
 	public String getAccessUrl() {
 		return _accessUrl;
 	}
 	
 	public String getEndpoint() {
 		return _endpoint;
 	}
 
 	public void setAccessInformation(String token, String url, String endpoint) {
 		_accessToken = token;
 		_accessUrl = url;
 		_endpoint = endpoint;
 	}
 	
 	public String getOrgId() {
 		if(_orgId == null && _endpoint != null) {
 			try {
 				Map<Object, Object> json = getSalesforceJSON("select Id from Organization limit 1");
 		        if(json.containsKey("totalSize")) {
 		        	Long size = (Long)json.get("totalSize");
 		        	if(size != null && size > 0) {
 		        		ArrayList<Object> records = (ArrayList<Object>)json.get("records");
 		        		Map<Object, Object> record = (Map<Object, Object>)records.get(0);
 		        		_orgId = record.get("Id").toString();
 		        	}
 		        }
 			} catch(Exception ex) {
 				_orgId = null;
				ex.printStackTrace(System.out);
 			}
 		}
 		return _orgId;
 	}
 	
 	public void reset() {
 		_images = new ArrayList<String>();
 		_accessToken = null;
 		_accessUrl = null;
 		_endpoint = null;
 		_orgId = null;
 	}
 	
 	private Map<Object, Object> getSalesforceJSON(String queryString) throws Exception {
 		String queryUrl = _endpoint + "query/?q=" + java.net.URLEncoder.encode(queryString, "ISO-8859-1");
         HttpURLConnection connection = (HttpURLConnection)new URL(queryUrl).openConnection();
         connection.setRequestMethod("GET");
        System.out.println("Access Token = " + _accessToken);
         connection.addRequestProperty("Authorization","OAuth " + _accessToken);
         connection.addRequestProperty("X-PrettyPrint", "1");
         connection.connect();
         BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         String inputLine;
         String data = "";
         while ((inputLine = in.readLine()) != null) {
         	data += inputLine;
         }
 
         in.close();
         connection.disconnect();
         return (Map<Object, Object>)new JSONParser().parse(data);
 	}
 }
