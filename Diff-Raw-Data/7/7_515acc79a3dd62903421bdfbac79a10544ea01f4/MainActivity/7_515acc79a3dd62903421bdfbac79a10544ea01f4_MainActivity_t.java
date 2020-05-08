 package com.dannis.findit;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.dannis.lib.webStuff;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import android.view.View.OnClickListener;
 
 public class MainActivity extends Activity {
 
 	Context _context;
 	LinearLayout _appLayout;
 	searchForm _search;
 	placesDisplay _places;
 	favDisplay _favorites;
 	boolean connected = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		_context = this;
 		_appLayout = new LinearLayout(this);
 		
 		//CREATING THE SEARCH BAR & BUTTON
 		 _search = new searchForm(_context, "Enter a Place...","find it!");
 		
 		//ADDING SEARCH HANDLER
 		Button searchButton = _search.getButton();
 		
 		//GRABBING TEXT WHEN USER TYPES
 		searchButton.setOnClickListener(new OnClickListener() {
 	
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Log.i("USER TYPED:",_search.getField().getText().toString());
 				getPlace(_search.getField().getText().toString());
 			}
 		});
 		
 		//DETECT NETWORK CONNECTION
 		connected = webStuff.getConnectionStatus(_context);
 		if(connected){
 			Log.i("NETWORK CONNECTION", webStuff.getConnectionType(_context));
 		}
 		
 		//ADD PLACES DISPLAY
 		_places = new placesDisplay(_context);
 		
 		//ADDING FAVORITES DISPLAY
 		_favorites = new favDisplay(_context);
 		
 		//ADDING VIEWS TO THE MAIN LAYOUT
 		_appLayout.addView(_search);
 		_appLayout.addView(_places);
 		_appLayout.addView(_favorites);
 
 		
 		_appLayout.setOrientation(LinearLayout.VERTICAL);
 		
 		setContentView(_appLayout);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	
 /* THIS IS WHAT THE YQL STRING WILL RETURN   
  * 
  * "id": "43283209",
     "xmlns": "urn:yahoo:lcl",
     "Title": "Power Play Sports",
     "Address": "1007 State Route 28, #E",
     "City": "Milford",
     "State": "OH",
     "Phone": "(513) 831-7200",
     "Latitude": "39.192851",
     "Longitude": "-84.2447",
     "Rating": {
      "AverageRating": "NaN",
      "TotalRatings": "0",
      "TotalReviews": "0",
      "LastReviewDate": null,
      "LastReviewIntro": null
     },
     "Distance": "8.24",
     "Url": "http://local.yahoo.com/info-43283209-power-play-sports-milford",
     "ClickUrl": "http://local.yahoo.com/info-43283209-power-play-sports-milford",
     "MapUrl": "http://local.yahoo.com/info-43283209-power-play-sports-milford?viewtype=map",
     "BusinessUrl": null,
     "BusinessClickUrl": null,
     "Categories": {
      "Category": {
       "id": "96925976",
       "content": "Sporting Goods"*/
     	  
     	  
 	private void getPlace(String place){
 		Log.i("Click", place);
 		//This Link gets you to the base link to the api service
 		String baseURl = "http://query.yahooapis.com/v1/public/yql";
 		String yql = "select * from local.search where zip='45103' and query='"+ place + "'";
 		String qs;
 		try{
 			qs =URLEncoder.encode(yql,"UTF-8");
 		}catch (Exception e){
 			Log.e("Bad URL","ENCODING PROBLEM");
 			qs="";
 		}
 		URL finalURL;
 		try{
 			finalURL = new URL(baseURl +"?q="+ qs + "&format=json&diagnostics=true&env=store://datatables.org/alltableswithkeys");
 			QuoteRequest qr=new QuoteRequest();
 			qr.execute(finalURL);
 		}catch (MalformedURLException e){
 			Log.e("BAD URL","MALFORMED URL");
 			finalURL =null;
 		}
 	}
 
 	private class QuoteRequest extends AsyncTask<URL, Void, String>{
 		@Override
 		protected String doInBackground(URL... urls){
 			String response ="";
 			for(URL url: urls){
 				response = webStuff.getURLStringResponse(url);
 			}
 			return response;
 		}
 		@Override
 		protected void onPostExecute(String result){
 			Log.i("URL RESPONSE", result);
 			try{
 				JSONObject json = new JSONObject(result);
				JSONObject results=json.getJSONObject("query").getJSONObject("results");
			/*	if(results.getString("Result")==null){
 					Toast toast= Toast.makeText(_context,"Invalid Entry", Toast.LENGTH_SHORT);
 					toast.show();
 				}else{
 					Toast toast= Toast.makeText(_context,"Valid Entry" + results.getString("place"), Toast.LENGTH_SHORT);
 					toast.show();
				}*/
 				Log.i("JSON RESULTS",results.toString());
 			}catch (JSONException e){
 				Log.e("JSON","JSON OBJECT EXCEPTION");
 			}
 			
 			
 		}
 	}
 }
