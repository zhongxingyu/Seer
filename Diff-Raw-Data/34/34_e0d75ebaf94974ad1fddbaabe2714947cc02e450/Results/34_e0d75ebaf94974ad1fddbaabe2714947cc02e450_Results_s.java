 package com.example.grubber;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.google.android.gms.maps.GoogleMap;
 import com.example.grubber.ResultContent;
 import com.example.grubber.ResultAdapter;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 import android.os.AsyncTask;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 
 import android.app.FragmentManager;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.example.grubber.R;
 import com.example.grubber.R.layout;
 import com.example.grubber.R.menu;
 import com.google.android.gms.maps.*;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.support.v4.app.*;
 
 
 
 public class Results extends FragmentActivity {
 	public FragmentManager fragMan;
 	private ListView result_list;
 	private ProgressDialog progDialog; 
 	//private View main_view;
 	private GoogleMap mMap;	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_results);
 		result_list = (ListView) findViewById(R.id.restaurantLV);
 		try {
 			getRestaurant();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		/* create map */
 		/* check we haven't instantiated the map already */
 		if (mMap == null) {
 			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 			
 			if( mMap == null ) {
 				//DialogFragment servicesDialog = new NeedServicesDialogFragment();
 				//servicesDialog.show(getSupportFragmentManager(), "results_services_dialog");
 			} else {
					//do something
 			}	
 		}		
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.results, menu);
 		return true;
 	}
 
 	public void getRestaurant() throws Exception {
 		//start progress bar
 		progDialog = ProgressDialog.show( this, "Process ", "Loading Data...",true,true);
 		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
 
 		nameValuePair.add(new BasicNameValuePair("key", getIntent().getStringExtra("key")));
 		nameValuePair.add(new BasicNameValuePair("min", "0"));
		nameValuePair.add(new BasicNameValuePair("max", "10"));
 		/*Log.d("bug", getIntent().getStringExtra("latitude"));
 		if(getIntent().getStringExtra("latitude") != null)
 		{
 			nameValuePair.add(new BasicNameValuePair("latitude", getIntent().getStringExtra("latitude")));
 		}
 		if(getIntent().getStringExtra("longitude") != null)
 		{
 			nameValuePair.add(new BasicNameValuePair("longitude", getIntent().getStringExtra("longitude")));
 		}*/	
 				
 		// url with the post data
 		HttpPost httpost = new HttpPost("http://cse190.myftp.org:8080/cse190/findRestaurants");
 
 		// sets the post request as the resulting string
 		httpost.setEntity(new UrlEncodedFormEntity(nameValuePair));
 		// Handles what is returned from the page
 		//ResponseHandler responseHandler = new BasicResponseHandler();
 		//Log.d("bugs", "execute request");
 		new GetHttpRequest().execute(httpost);
 
 	}
 
 	
 	public class NeedServicesDialogFragment extends DialogFragment {
 	    
 	    
 	    public Dialog onCreateDialog(Bundle savedInstanceState) {
 	        // Use the Builder class for convenient dialog construction
 	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 	        builder.setMessage(R.string.need_services)
 	               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 	            	   
 	                   public void onClick(DialogInterface dialog, int id) {
 	                	   //return to main screen
 	               	    	Intent intent = new Intent(getBaseContext(), MainActivity.class);
 	               	    	startActivity(intent);   
 	               	    	   
 	                   }
 	               });
 
 	        // Create the AlertDialog object and return it
 	        return builder.create();
 	    }
 	}
 	
 	
 	private class GetHttpRequest extends AsyncTask<HttpPost, Void, HttpResponse> {
 
 		@Override
 		protected HttpResponse doInBackground(HttpPost... params) {
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 			try {
 				HttpResponse resp = httpclient.execute(params[0]);
 				return resp;
 			} catch (Exception e) {
 				Log.d("bugs", "Catch in HTTPGETTER");
 			}
 			return null;
 		}
 
 		protected void onPostExecute(HttpResponse response) {
 			String json = "wrong";
 			try {
 				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
 				json = reader.readLine();
 				//Log.d("bug", json);
 				setView(json);
 			} catch (Exception e) { 
 				Log.d("bugs","reader"); 
 				//add button to refresh
 				
 				runOnUiThread(new Runnable() {
 					public void run() {
 					    Toast.makeText(Results.this, "Failed to get the data", Toast.LENGTH_SHORT).show();
 					}
 				});
 			}
 			//stop progress bar
 			progDialog.dismiss();
 		}
 		
 		protected void setView(String jsonString)
 		{
 	        JsonParser jsonParser = new JsonParser();
 	        JsonObject jo = (JsonObject)jsonParser.parse(jsonString);
 	        JsonArray jarr = (JsonArray) jo.get("result");
 	        
 	        final ArrayList<ResultContent> list_result = new ArrayList<ResultContent>();
 	        for (int i = 0; i < jarr.size(); i++) {
 	        	JsonObject result = (JsonObject) jarr.get(i);
 
 	        	//set for adapter value
 	        	list_result.add(new ResultContent(result.get("rest_id").getAsString(), result.get("name").getAsString(),
 						  result.get("address").getAsString(), result.get("city").getAsString(), result.get("state").getAsString(),
 						  result.get("zip").getAsString(), result.get("longitude").getAsString(), result.get("latitude").getAsString(),
 						  result.get("phone").getAsString(), result.get("website").getAsString(), result.get("distance").getAsString()));
 	        }
 	        
 	        ResultAdapter radapter = new ResultAdapter(Results.this, list_result);
 
 	        //Show the restaurant list to ListView
 	        result_list.setAdapter(radapter);
 	        result_list.setOnItemClickListener(new OnItemClickListener() {
 	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
 	            {//set onClick
 	            	Intent intent = new Intent(Results.this, RestaurantActivity.class);
 	            	ResultContent tmp = list_result.get((int) id);
 	            	intent.putExtra("rest_id", tmp.getId());
 	            	intent.putExtra("name", tmp.getName());
 	            	intent.putExtra("address", tmp.getAddress());
 	            	intent.putExtra("city", tmp.getCity() + ", " + tmp.getState() + ", " + tmp.getZip());
 	            	intent.putExtra("longitude", tmp.getLongitude());
 	            	intent.putExtra("latitude", tmp.getLatitude());
 	            	intent.putExtra("phone", tmp.getPhone());
 	            	intent.putExtra("website", tmp.getWebsite());
 	            	intent.putExtra("distance", tmp.getDistance());
 	        		startActivity(intent);
 	            }
 	        });
 		}
 	}
 	
 	
 }
