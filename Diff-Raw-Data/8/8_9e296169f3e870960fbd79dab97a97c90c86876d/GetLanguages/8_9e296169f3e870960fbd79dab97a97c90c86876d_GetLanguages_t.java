 package com.ahmetkizilay.yatlib4j.help;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import net.minidev.json.JSONArray;
 import net.minidev.json.JSONObject;
 import net.minidev.json.JSONValue;
 
 import com.ahmetkizilay.yatlib4j.TwitterRequestWrapper;
 import com.ahmetkizilay.yatlib4j.TwitterResponseWrapper;
 import com.ahmetkizilay.yatlib4j.datatypes.Language;
import com.ahmetkizilay.yatlib4j.exceptions.TwitterIOException;
 import com.ahmetkizilay.yatlib4j.oauthhelper.OAuthHolder;
 import com.ahmetkizilay.yatlib4j.oauthhelper.OAuthUtils;
 
 
 public class GetLanguages {
 	private static final String BASE_URL = "https://api.twitter.com/1.1/help/languages.json";
 	private static final String HTTP_METHOD = "GET";
 	
	public static GetLanguages.Response sendRequest(GetLanguages.Parameters params, OAuthHolder oauthHolder) throws TwitterIOException {
 		Hashtable<String, String> httpParams = params.prepareParams();
 		String oauthHeader = OAuthUtils.generateOAuthHeader(HTTP_METHOD, BASE_URL, httpParams, oauthHolder);
 		
 		TwitterResponseWrapper twitterResponse = TwitterRequestWrapper.sendRequest(HTTP_METHOD, BASE_URL, httpParams, oauthHeader);
		if(!twitterResponse.isSuccess()) {
			throw new TwitterIOException(twitterResponse.getResponseCode(), twitterResponse.getResponse());
		}
 		
 		GetLanguages.Response response = new GetLanguages.Response(twitterResponse.getResponse());
 		
 		return response;
 	}
 	
 	/***
 	 * Currently this method does not need parameters
 	 * @author ahmetkizilay
 	 *
 	 */
 	public static class Parameters {
 		
 		public Hashtable<String, String> prepareParams() {
 			return new Hashtable<String, String>();
 		}
 	}
 	
 	public static class Response {
 		private JSONArray jsonData;
 		private String rawResponse;
 		
 		public Response(String rawResponse) {
 			this.rawResponse = rawResponse;
 			init();
 		}
 		
 		private void init() {			
 			 this.jsonData = (JSONArray) JSONValue.parse(this.rawResponse);
 		}
 		
 		public String getRawResponse() {
 			return this.rawResponse;
 		}
 		
 		public ArrayList<Language> getLanguages() {
 			return getLanguages(false);
 		}
 		
 		public ArrayList<Language> getLanguages(boolean keepData) {
 			ArrayList<Language> languageList = new ArrayList<Language>();
 			
 			for(int i = 0, len = jsonData.size(); i < len; ++i) {
 				languageList.add(Language.fromJSON((JSONObject) jsonData.get(i), keepData));
 			}
 			
 			return languageList;
 		}
 	}
 }
