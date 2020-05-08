 package com.Client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 
 import com.assurance.R;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListAdapter;
import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
 
 public class Search_plate extends ListActivity {
 
 	// Progress Dialog
 	private ProgressDialog pDialog;
 
 	// Creating JSON Parser object
 	JSONParser jParser = new JSONParser();
 
 	ArrayList<HashMap<String, String>> productsList;
 
 	// url to get all products list
 	private static String url_all_products = "http://smb215.hostyd.com/android_connect/search.php";
 
 
 	// JSON Node names
 	private static final String TAG_SUCCESS = "success";
 	private static final String TAG_PRODUCT = "client";
 	private static final String TAG_PID = "pid";
 	private static final String TAG_NAME = "name";
 	private static final String TAG_ADRESS = "adress";
 	private static final String TAG_PLATE = "plate";
 	private static final String TAG_CAR = "car";
 	private static final String TAG_FEES = "fees";
 	private static final String TAG_PHONE = "phone";
 
 	// products JSONArray
 	JSONArray client = null;
 
 	EditText inputSearch;
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.search);
 		
 		// Edit Text
 				inputSearch = (EditText) findViewById(R.id.inputSearch);
 				
 	   // Create button
 		Button btnSearch = (Button) findViewById(R.id.Search);		
 	   Button btnedit = (Button) findViewById(R.id.edit);	
 		
 
 		// Hashmap for ListView
 		productsList = new ArrayList<HashMap<String, String>>();
 	
 
 		// button click event
 				btnSearch.setOnClickListener(new View.OnClickListener() {
 
 					@Override
 					public void onClick(View view) {
 						// creating new product in background thread
 						new SearchPlate().execute();
 					}
 				});
 			
 		
 				
 				
 		//Get listview;
 	  	//ListView lv = getListView();
 
 				btnedit.setOnClickListener(new View.OnClickListener()  {
 
 					@Override
 					public void onClick(View view) {
 						// getting values from selected Item
 						String pid = ((TextView)findViewById(R.id.pid)).getText()
 								.toString();
 
 						// Starting new intent
 						Intent in = new Intent(getApplicationContext(),
 								Update.class);
 						// sending pid to next activity
 						in.putExtra(TAG_PID, pid);
 						
 						// starting new activity and expecting some response back
 						startActivityForResult(in, 100);
 					}
 				});
 
 			}
 
 			// Response from Edit Product Activity
 			@Override
 			protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 				super.onActivityResult(requestCode, resultCode, data);
 				// if result code 100
 				if (resultCode == 100) {
 					// if result code 100 is received 
 					// means user edited/deleted product
 					// reload this screen again
 					Intent intent = getIntent();
 					finish();
 					startActivity(intent);
 				}
 
 			}
 
 
 	/**
 	 * Background Async Task to Load all product by making HTTP Request
 	 * */
 	class SearchPlate extends AsyncTask<String, String, String> {
 
 		/**
 		 * Before starting background thread Show Progress Dialog
 		 * */
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			pDialog = new ProgressDialog(Search_plate.this);
 			pDialog.setMessage("Searching Plate. Please wait...");
 			pDialog.setIndeterminate(false);
 			pDialog.setCancelable(false);
 			pDialog.show();
 		}
 
 		/**
 		 * getting Searched clients from url
 		 * */
 		protected String doInBackground(String... args) {
 			String search = inputSearch.getText().toString();
 
 
 			// Building Parameters
 			List<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("search", search));
 		
 			JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);
 			
 			// Check your log cat for JSON reponse
 			Log.d("All Products: ", json.toString());
 
 			try {
 				// Checking for SUCCESS TAG
 				int success = json.getInt(TAG_SUCCESS);
 
 				if (success == 1) {
 					// products found
 					// Getting Array of Products
 					client = json.getJSONArray(TAG_PRODUCT);
 
 					// looping through All Products
 					for (int i = 0; i < client.length(); i++) {
 						JSONObject c = client.getJSONObject(i);
 
 						// Storing each json item in variable
 						String id = c.getString(TAG_PID);
 						String name = c.getString(TAG_NAME);
 						String adress = c.getString(TAG_ADRESS);
 						String phone = c.getString(TAG_PHONE);
 						String plate = c.getString(TAG_PLATE);
 						String car = c.getString(TAG_CAR);
 						String fees = c.getString(TAG_FEES);
 						
 
 						// creating new HashMap
 						HashMap<String, String> map = new HashMap<String, String>();
 
 						// adding each child node to HashMap key => value
 						map.put(TAG_PID, id);
 						map.put(TAG_NAME, name);
 						map.put(TAG_ADRESS, adress);
 						map.put(TAG_PHONE, phone);
 						map.put(TAG_PLATE, plate);
 						map.put(TAG_CAR, car);
 						map.put(TAG_FEES, fees);
 						
 
 						// adding HashList to ArrayList
 						productsList.add(map);
 					}
 				} else {
 					// no products found
 					// Launch Add New product Activity
 					Intent i = new Intent(getApplicationContext(),
 							Add.class);
 					// Closing all previous activities
 					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					startActivity(i);
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 			return null;
 		}
 
 		/**
 		 * After completing background task Dismiss the progress dialog
 		 * **/
 		protected void onPostExecute(String file_url) {
 			// dismiss the dialog after getting all products
 			pDialog.dismiss();
 			// updating UI from Background Thread
 			runOnUiThread(new Runnable() {
 				public void run() {
 					/**
 					 * Updating parsed JSON data into ListView
 					 * */
 					ListAdapter adapter = new SimpleAdapter(
 							Search_plate.this, productsList,
 							R.layout.search_details, new String[] { TAG_PID,
 									TAG_NAME,TAG_ADRESS,TAG_PHONE,TAG_PLATE,TAG_CAR,TAG_FEES},
 							new int[] { R.id.pid, R.id.name,R.id.adress,R.id.phone,R.id.plate,R.id.car,R.id.fees });
 					// updating listview
 					setListAdapter(adapter);
 				}
 			});
 
 	
 		}
 
 	}
 }
