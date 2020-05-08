 package com.cinemar.phoneticket.authentication;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.JsonHttpResponseHandler;

 
 public class FilmsClientAPI {
 
 	private static final String BASE_URL = "http://phoneticket-stg.herokuapp.com/api/";
 
 	private AsyncHttpClient client;
 
 	public FilmsClientAPI() {
 		client = new AsyncHttpClient();
 	}
 	
	public void getFilms(JsonHttpResponseHandler responseHandler){			
		client.get(getAbsoluteUrl("movies.json"), responseHandler);		
 	}
 
 	private static String getAbsoluteUrl(String relativeUrl) {
 		return BASE_URL + relativeUrl;
 	}
 }
