 package com.advsofteng.app1;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 
 // handles user set POI data
 public class GetPOIActivity extends Activity { 
 	
 	////// Member variables /////////
 	// CheckBoxes that user selects to get data on from server
 	private CheckBox checkBox1, checkBox2, checkBox3, checkBox4;
 	private SeekBar seekBarRadius; // seekBar that user sets the radius of POIs nearby.
 	private TextView tvRadius; // displays the radius data
 	private Integer intRadius; // user set radius
 	private int intMaxRadius = 1000; // maximum radius in metres
 	private Button btnGetPOIdata; // - sends all the data to the server and returns to main screen...
 	private Resources resources1; // - used to access app wide resources.
 	private SharedPreferences prefs;
 	
 	// flag used in saving and loading app wide prefs.
 	//ref: http://sites.google.com/site/jalcomputing/home/mac-osx-android-programming-tutorial/saving-instance-state
 	private Boolean isSavedInstanceState= false; 
 	
 	// string holding the server address
 	private static  String ENDPOINT =null; 
 	private static final String tagPOI = "GetPOIActivity"; // used for logging
  
 	
 	
 	// Called at the start of the full lifetime. 
 	@Override 
 	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		// Initialize activity.
 	
		setContentView(R.layout.get_poi);

 	// initialise class member data
 	resources1 = getResources();
 	prefs = getApplicationContext().getSharedPreferences(AdvSoftEngApp1Activity.TAG, Context.MODE_PRIVATE);
 	
 	// set server address.
 	ENDPOINT = getResources().getString(R.string.serverAddress);
 
 	
 	//
 	if(null != savedInstanceState){ // get saved state from bundle after a soft kill
 	    try {
 	    	InitialiseCheckBoxsAndBtns(savedInstanceState);
 	        Log.i(tagPOI,"RestoredState.");
 	    }
 	    catch(Exception e){
 	        Log.i(tagPOI,"FailedToRestoreState",e);
 	    }
 	}
 	else { // get saved state from preferences on first pass after a hard kill
 	    	InitialiseCheckBoxsAndBtns(null);
 	       Log.i(tagPOI,"gettingPrefs");
 	}
 	//
 	//////////
 	
 	// initialise button to respond to clicks with correct action
 	btnGetPOIdata = (Button) findViewById(R.id.getPOIDataButton);
 	btnGetPOIdata.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
     	  
   
    			try {
 
    	   			
    				if(null != prefs.getString("time", null)) // if we actually get a latitude value back.. then 
    				{												 //  the GPS is working proceed...
 
    					/* Android ID is calculated according to code from
    					 * http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
    					 */
    						
    					String androidId = Secure.getString(getApplicationContext().getContentResolver(),Secure.ANDROID_ID);
    					
    					String myUrl = ENDPOINT + "/" + androidId + "/" + String.valueOf(prefs.getString("latitude", "")) 
 							+ "/" + String.valueOf(prefs.getString("longitude", ""))
 							+ "/" + intRadius.toString();
 
    					/* If the user's checked any checkbox, add a parameter to the URL listing whichever
    					 * checkboxes they've chosen - so they can choose particular classes of POI
    					 */
    					
    					if (checkBox1.isChecked() || checkBox2.isChecked() || checkBox3.isChecked() || checkBox4.isChecked()) {
    						myUrl = myUrl + "?t=";
    						if (checkBox1.isChecked()) myUrl = myUrl + "1,";
    						if (checkBox2.isChecked()) myUrl = myUrl + "2,";
    						if (checkBox3.isChecked()) myUrl = myUrl + "3,";
    						if (checkBox4.isChecked()) myUrl = myUrl + "4,";
    						myUrl = myUrl.substring(0, myUrl.length()-1);
    					}
    					
   					HttpGet get = new HttpGet(myUrl);   					
 
 	   					Log.d("GetPoiActivity", "get="+get.getURI());
 
   					// end of testing block to delete...
 
    					/* Create a new HTTPClient to do our POST for us */
    					HttpClient client = new DefaultHttpClient();
    					HttpResponse response = client.execute(get);
    					
    					//get text out of body of response....
    					String responseBody = HttpHelper.getResponseBody(response);
    					
    					// get the Poi objects out of the string....
    					// ref: http://benjii.me/2010/04/deserializing-json-in-android-using-gson/
    					
    					JSONArray j; 
    					// object deserialises json objects
    					Gson gson = new Gson();
    					
    					AdvSoftEngApp1Activity.poiArray.clear(); // get rid of any old Pois from last request...
 
    					try
    					{
    	   					Log.d("GetPoiActivity", "json="+responseBody);
    	   				    JsonParser parser = new JsonParser();
    	   				    JsonArray array = parser.parse(responseBody).getAsJsonArray();
    	   				    
    	   				    int iCounter = 0;
    	   				    for(JsonElement counter : array)
    	   				    {	
    	   				    	// run through the JsonArray converting each entry into an actual Poi object in the Poi ArrayList
    	   				    	AdvSoftEngApp1Activity.poiArray.add( gson.fromJson(array.get(iCounter), Poi.class));
    	   				    	
    	   				    	//TODO: delete this block when finished testing....
    	   				    	Log.i(tagPOI, String.valueOf(iCounter) + " - - - - - - - - - - -");
    	   				    	Log.i(tagPOI, "Current POI name = " + AdvSoftEngApp1Activity.poiArray.get(iCounter).getName());
    	   				    	Log.i(tagPOI, "Current POI Latitude = " + String.valueOf(AdvSoftEngApp1Activity.poiArray.get(iCounter).getLatitude()));
    	   				    	Log.i(tagPOI, "Current POI Longitude = " + String.valueOf(AdvSoftEngApp1Activity.poiArray.get(iCounter).getLongitude()));
    	   				    	Log.i(tagPOI, "Current POI ID = " + String.valueOf(AdvSoftEngApp1Activity.poiArray.get(iCounter).getId()));
    	   				    	//
    	   				    	
    	   				    	iCounter++;
    	   				    }
    	   				    
    					}
    					catch(Exception e)
    					{
    						e.printStackTrace();
    						Log.i(tagPOI, "Error in parsing Json data from sever, " + e.getMessage());
    					}
    					
    				} 
    				else { 
    					// we have NOT got GPS data.....
    					Toast toast=Toast.makeText(getApplicationContext(), resources1.getString(R.string.no_gps_error), 2000);
    					toast.setGravity(Gravity.TOP, -30, 50);
    					toast.show();
    				}
    				
    			} catch (Exception e) {
    				Log.i(tagPOI, "get to getPOI failed, " + e.getMessage());
    				
    			}
    			
        finish(); // return to previous screen...
    			
        } } );
 
 	}
 	
 	// create and instantiate and override implemented seekbar methods...
 	private SeekBar.OnSeekBarChangeListener seekBarListen = new SeekBar.OnSeekBarChangeListener() {
 		
 		public void onStopTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		public void onStartTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
 			
 			// update member variable and textview with new radius data...
 			intRadius = progress;
 			tvRadius = (TextView) findViewById(R.id.tvRadius);
 			tvRadius.setText(resources1.getText((R.string.tvRadiusText)) + Integer.toString(intRadius) + " m");
 			
 		}
 	};
 	
 	// Called after onCreate has finished, use to restore UI state 
 	@Override 
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 
 		super.onRestoreInstanceState(savedInstanceState); 
 		// Restore UI state from the savedInstanceState.
 		// This bundle has also been passed to onCreate.
 
 		if(savedInstanceState != null){
 
 			intRadius = savedInstanceState.getInt((String)resources1.getText(R.string.tvRadiusText), 0);
 		}
 
 	}
 	
 	 
 	private void InitialiseCheckBoxsAndBtns(Bundle lastInstanceState)
 	{
 		
 		// CheckBox setup...
 		checkBox1 = (CheckBox) findViewById(R.id.CheckBox1);
 		checkBox2 = (CheckBox) findViewById(R.id.CheckBox2);
 		checkBox3 = (CheckBox) findViewById(R.id.CheckBox3);
 		checkBox4 = (CheckBox) findViewById(R.id.CheckBox4);
 
 		// set text....
 		checkBox1.setText(resources1.getText(R.string.poi_checkbox1));
 		checkBox2.setText(resources1.getText(R.string.poi_checkbox2));
 		checkBox3.setText(resources1.getText(R.string.poi_checkbox3));
 		checkBox4.setText(resources1.getText(R.string.poi_checkbox4));
 	
 		/////////////////
 		//seekBar and corresponding label set up
 		seekBarRadius = (SeekBar) findViewById(R.id.seekBarRadius);
 		seekBarRadius.setOnSeekBarChangeListener(seekBarListen);
 		seekBarRadius.setMax(intMaxRadius);
 
 		
 		///////////////////////////////////////////////////////
 		// everything below is called when this activity is paused and then resumed,
 		// rather than when starting the activity from scratch
 		boolean booleanFlag = false; // used as a temp boolean value holder below
 		if(null == lastInstanceState)
 		{
 			//get checkbox1's last state from saved prefs....
 			booleanFlag = prefs.getBoolean((String)resources1.getText((R.string.poi_checkbox1)), booleanFlag);
 			checkBox1.setChecked(booleanFlag);
 			
 			//get checkbox2's last state from saved prefs....
 			booleanFlag = prefs.getBoolean((String)resources1.getText((R.string.poi_checkbox2)), booleanFlag);
 			checkBox2.setChecked(booleanFlag);
 			
 			//get checkbox3's last state from saved prefs....
 			booleanFlag = prefs.getBoolean((String)resources1.getText((R.string.poi_checkbox3)), booleanFlag);
 			checkBox3.setChecked(booleanFlag);
 			
 			//get checkbox4's last state from saved prefs....
 			booleanFlag = prefs.getBoolean((String)resources1.getText((R.string.poi_checkbox4)), booleanFlag);
 			checkBox4.setChecked(booleanFlag);
 			
 			// get radius value...
 			intRadius = prefs.getInt((String)resources1.getText(R.string.tvRadiusText), 0);
 		}
 		else //If lastInstanceState != null, then we are to use preferences to load data...
 		{
 			if(null != lastInstanceState)
 				intRadius = lastInstanceState.getInt((String)resources1.getText(R.string.tvRadiusText), 0);		
 		}
 		
 		seekBarRadius.setProgress(intRadius);
 
 		//radius label
 		tvRadius = (TextView) findViewById(R.id.tvRadius);
 		tvRadius.setText(resources1.getText((R.string.tvRadiusText)) + Integer.toString(intRadius) + " m");
 		
 
 		
 	}
 
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		
 		isSavedInstanceState= true;
 		
 		savedInstanceState.putBoolean((String)resources1.getText((R.string.poi_checkbox1)), checkBox1.isChecked());
 		savedInstanceState.putBoolean((String)resources1.getText((R.string.poi_checkbox2)), checkBox2.isChecked());
 		savedInstanceState.putBoolean((String)resources1.getText((R.string.poi_checkbox3)), checkBox3.isChecked());
 		savedInstanceState.putBoolean((String)resources1.getText((R.string.poi_checkbox4)), checkBox4.isChecked());
 		
 		savedInstanceState.putInt((String)resources1.getText(R.string.tvRadiusText), intRadius);
 		
 		super.onSaveInstanceState(savedInstanceState);
 
 	}
 
     protected void onStart() {
        // Log.i(tagPOI,"onStart");
         
         InitialiseCheckBoxsAndBtns(null);
         super.onStart();
     }
     protected void onResume() {
        // Log.i(tagPOI,"onResume");
     	isSavedInstanceState= false;
         super.onResume();
     }
     protected void onStop() {
     	
     	if (!isSavedInstanceState){ // this is a HARD KILL, write to prefs
       
             SharedPreferences.Editor editor = prefs.edit();
             
             editor.putBoolean((String)resources1.getText(R.string.poi_checkbox1), checkBox1.isChecked());
             editor.putBoolean((String)resources1.getText(R.string.poi_checkbox2), checkBox2.isChecked());
             editor.putBoolean((String)resources1.getText(R.string.poi_checkbox3), checkBox3.isChecked());
             editor.putBoolean((String)resources1.getText(R.string.poi_checkbox4), checkBox4.isChecked());
             editor.putInt((String)resources1.getText(R.string.tvRadiusText), intRadius);
             
             editor.commit();
            // Log.i(tagPOI,"savedPrefs");
             
         }
         super.onStop();
     }
 
 }
