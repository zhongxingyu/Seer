 package uw.changecapstone.tweakthetweet;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class TestStringBuilderDisasterList extends CustomWindow {
 
 	private ListView disaster_list;
 	private String tweet, custom_disaster_tag;
 	public final static String LAT = "uw.changecapstone.tweakthetweet.latitude";
 	public final static String LONG = "uw.changecapstone.tweakthetweet.longitude";
 	public final static String CITY_LAT = "uw.changecapstone.tweakthetweet.latitude";
 	public final static String CITY_LONG = "uw.changecapstone.tweakthetweet.longitude";
 	static double latitude ;
 	static double longitude;
 	static double city_lat ;
 	static double city_long;
 	private TextView char_count;
 	private EditText crnt_tweet;
 	private ImageButton proceed_custom_disaster_tag;
 	private Map<String, Map<String, Double>> testMap;
 	
 	private final TextWatcher createNewDisasterTag = new TextWatcher() {
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 		}
 	
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 			
 			//Handle tag creation and display in footer box
 			custom_disaster_tag = s.toString();
 			
 			//Add "#" character to hash tag if the user did not already enter it
 			if(!custom_disaster_tag.contains("#")){
 				custom_disaster_tag = "#" + custom_disaster_tag;
 			}
 			crnt_tweet.setText(custom_disaster_tag);
 			
 			//Handle character count display
 			int crntLength = 140 - custom_disaster_tag.length();
 			if(crntLength < 0){
 				char_count.setTextColor(Color.RED);
 			}else{
 				char_count.setTextColor(Color.BLACK);
 			}
 			
 			if(crntLength != 1){
 				char_count.setText(String.valueOf(crntLength) + " characters left");
 			}else{
 				char_count.setText(String.valueOf(crntLength) + " character left");
 			}
 		}
 	
 		@Override
 		public void afterTextChanged(Editable arg0) {
 		}
 	
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//added by mussie. changed the string to double because intent was not used.
 		Intent intent = getIntent();
 		latitude = ((Double)intent.getDoubleExtra(TestStringBuilder.LAT, 0.0));
 		longitude = ((Double)intent.getDoubleExtra(TestStringBuilder.LONG, 0.0));
 		System.out.println("test: "+latitude+" "+longitude);
 		
 		city_lat = ((Double)intent.getDoubleExtra(TestStringBuilder.CITY_LAT, 0.0));
 		city_long = ((Double)intent.getDoubleExtra(TestStringBuilder.CITY_LONG, 0.0));
 		setContentView(R.layout.activity_test_string_builder_disaster_list);
 		this.title.setText("#disaster");
 		
 		// TODO: We get coordinates of location from Google Maps
 		// We compare it with all the parameter coordinates of events
 		// We build a list of events that the coordinates fall within
 		
 		
 		disaster_list = (ListView) findViewById(R.id.list);
 		disaster_list.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View v, int position,
 					long id) {
 				
 				tweet = (String) adapter.getItemAtPosition(position);
 				if(TestStringBuilder.isGpsUsed)
 					nextViewLocation(v);
 				else
 					nextNonGPSLocation(v);
 			}
 		
 		});
 		
 		
 		//Add footer for entering your own hashtag and displaying tweet
 		View footerView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listview_footer, null, false);
 		disaster_list.addFooterView(footerView);
 				
 		//Set the enter your own label to match the disaster page
 		TextView footerLabel = (TextView) findViewById(R.id.enter_your_own_text);
 		footerLabel.setText("or enter your own #disaster");
 		
 		//Set the hint in the text box to match the disaster page
 		EditText stateTextBox = (EditText) findViewById(R.id.custom_text_box);
 		stateTextBox.setHint("i.e. #northhurricane");
 		
 		//Set up disaster text box
 		stateTextBox.addTextChangedListener(createNewDisasterTag);
 				
 		//Set up char count
 		char_count = (TextView) findViewById(R.id.footer_character_count);
 		char_count.setText("140 characters left");
 		
 		//Set up tweet text box
 		crnt_tweet = (EditText) findViewById(R.id.tweet_display);
 		
 		//Set up "next" button for custom hash tag
 		proceed_custom_disaster_tag = (ImageButton) findViewById(R.id.proceed_with_custom);
 		proceed_custom_disaster_tag.setOnClickListener(new OnClickListener() {
 
 		    @Override
 		    public void onClick(View v) {
 				if(custom_disaster_tag != null){
 					tweet = custom_disaster_tag;
 					if(TestStringBuilder.isGpsUsed)
 						nextViewLocation(v);
 					else
 						nextNonGPSLocation(v);
 				}else{
 					Toast.makeText(getApplicationContext(), "Please enter a custom disaster tag", Toast.LENGTH_SHORT).show();
 					
 				}
 		    }
 		});
 		
 		updateHashTagData();
 		
 		//Create adapter
 //		ListAdapter adapter = createAdapter();
 //		disaster_list.setAdapter(adapter);
 		
 		
 		// Create adapter
 		//disaster_list.setAdapter(createAdapter(coord_x, coord_y));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.test_string_builder_disaster_list,
 				menu);
 		return true;
 	}
 	
 	private void updateHashTagData(){
 		testMap = new HashMap<String, Map<String, Double>>();
 		
 		new AsyncTask<String, Void, String>() {
 
 			@Override
 			protected String doInBackground(String... params) {
 				String result = "";
 	        	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 	        	nameValuePairs.add(new BasicNameValuePair("id","message"));
 	    		InputStream is = null;
 	    		// http get
 	    		try{
 			        HttpClient httpclient = new DefaultHttpClient();
			        HttpGet httpget = new HttpGet("http://homes.cs.washington.edu/~yaluen/main.php?id=*");
 			        HttpResponse response = (HttpResponse) httpclient.execute(httpget);
 			        HttpEntity entity = ((org.apache.http.HttpResponse) response).getEntity();
 			        is = entity.getContent();
 	    		} catch(Exception e) {
 			        Log.e("log_tag", "Error in http connection "+e.toString());
 	    		}
 	    		//convert response to string
 	    		try {
 			        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
 			        StringBuilder sb = new StringBuilder();
 			        String line = null;
 			        while ((line = reader.readLine()) != null) {
 		                sb.append(line + "\n");
 			        }
 			        is.close();
 			 
 			        result=sb.toString();
 			        Log.i("output", result);
 	    		} catch (Exception e) {
 			        Log.e("log_tag", "Error converting result "+e.toString());
 	    		}
 	    		 
 	    		String returnResult = "";
 	    		
 	    		//parse json data
 	    		try {
 			        JSONArray jArray = new JSONArray(result);
 			        for(int i=0;i<jArray.length();i++){
 		                JSONObject json_data = jArray.getJSONObject(i);
 		                testMap.put(json_data.getString("event_id"), new HashMap<String, Double>());
 		                testMap.get(json_data.getString("event_id")).put("lat_top_right", Double.parseDouble(json_data.getString("latitude_top_right")));
 		        		testMap.get(json_data.getString("event_id")).put("lon_top_right", Double.parseDouble(json_data.getString("longitude_top_right")));
 		        		testMap.get(json_data.getString("event_id")).put("lat_bot_left", Double.parseDouble(json_data.getString("latitude_bottom_left")));
		        		testMap.get(json_data.getString("event_id")).put("lon_bot_left", Double.parseDouble(json_data.getString("longitude_bottom_left")));
 		        		Log.i(json_data.getString("event_id"), json_data.getString("latitude_top_right") + "," + json_data.getString("longitude_top_right")
		        				+ "   " + json_data.getString("latitude_bottom_left") + "," + json_data.getString("longitude_bottom_left"));
 			        }
 			        
 	    		} catch(JSONException e) {
 			        Log.e("log_tag", "Error parsing data "+e.toString());
 	    		}
 	    		
 	    		System.out.println(returnResult);
 	    		return returnResult;
 	    	}
 
 	    	@Override
 	        protected void onPostExecute(String result) {
 	    		ListAdapter adapter = createAdapter();
 	    		disaster_list.setAdapter(adapter);
 	        }
 			
 		}.execute("");
 	}
 	
 	
 	protected ListAdapter createAdapter()
     {
 //		Map<String, Map<String, Integer>> testMap = new HashMap<String, Map<String, Integer>>();
 //		testMap.put("#TestEvent1", new HashMap<String, Integer>());
 //		testMap.get("#TestEvent1").put("max_x", 10);
 //		testMap.get("#TestEvent1").put("max_y", 10);
 //		testMap.get("#TestEvent1").put("min_x", 0);
 //		testMap.get("#TestEvent1").put("min_y", 0);
 //		testMap.put("#TestEvent2", new HashMap<String, Integer>());
 //		testMap.get("#TestEvent2").put("max_x", 20);
 //		testMap.get("#TestEvent2").put("max_y", 20);
 //		testMap.get("#TestEvent2").put("min_x", 10);
 //		testMap.get("#TestEvent2").put("min_y", 10);
 //		testMap.put("#TestEvent3", new HashMap<String, Integer>());
 //		testMap.get("#TestEvent3").put("max_x", 5);
 //		testMap.get("#TestEvent3").put("max_y", 5);
 //		testMap.get("#TestEvent3").put("min_x", 0);
 //		testMap.get("#TestEvent3").put("min_y", 0);
 
 		
 		List<String> testData = new ArrayList<String>();
 		for(String event_tag : testMap.keySet()) {
			Log.i("looking at", event_tag);
			Log.i("city_lat", Double.toString(city_lat));
			Log.i("city_long", Double.toString(city_long));
			Log.i("lat_top_right", Double.toString(testMap.get(event_tag).get("lat_top_right")));
			Log.i("lon_top_right", Double.toString(testMap.get(event_tag).get("lon_top_right")));
			Log.i("lat_bot_left", Double.toString(testMap.get(event_tag).get("lat_bot_left")));
			Log.i("lon_bot_left", Double.toString(testMap.get(event_tag).get("lon_bot_left")));
			if (city_lat <= testMap.get(event_tag).get("lat_top_right") && city_lat >= testMap.get(event_tag).get("lat_bot_left") &&
					city_long <= testMap.get(event_tag).get("lon_top_right") && city_long >= testMap.get(event_tag).get("lon_bot_left")){
 				testData.add(event_tag);
				Log.i("added", event_tag);
 			}
 		}
 
 		return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testData);
     }
 	
 	public void nextViewLocation(View view){
 		//TODO: the intent is not used to get the LAT/LONG
 		Intent i = new Intent(this, LocationWithGPS.class);
 		i.putExtra("tweet", tweet);
 		i.putExtra("disaster", tweet);
 		i.putExtra(LAT, latitude);
 		i.putExtra(LONG, longitude);
 		System.out.println("going out of disaster list: "+latitude+" "+longitude);
 		startActivity(i);
 	}
 	public void nextNonGPSLocation(View view){
 		//TODO: the intent is not used to get the LAT/LONG
 		System.out.println("inside non-gps");
 		Intent i = new Intent(this, PreviousLocationActivity.class);
 		i.putExtra("tweet", tweet);
 		i.putExtra("disaster", tweet);
 		i.putExtra(CITY_LAT, city_lat);
 		i.putExtra(CITY_LONG, city_long);
 		System.out.println("going out of disaster list: "+city_lat+" "+city_lat);
 		startActivity(i);
 	}
 }
