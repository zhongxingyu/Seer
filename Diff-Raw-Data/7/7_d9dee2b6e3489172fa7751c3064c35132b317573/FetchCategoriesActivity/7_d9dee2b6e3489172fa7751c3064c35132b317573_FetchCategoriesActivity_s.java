 /**
  * This activity is used to fetch all important Notices.
  */
 package com.wbs.ginos;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
  
 public class FetchCategoriesActivity extends ListActivity {
     private ProgressDialog pDialog;
     // Creating JSON Parser object
     JSONParser jParser = new JSONParser();
     ArrayList<HashMap<String, String>> NoticesList;
     private static String url_all_categories = "http://ginos.getfreehosting.co.uk/app/fetchcategory.php";
     // JSON Node names
     private static final String TAG_SUCCESS = "success";
     private static final String TAG_CATEGORIES = "categories";
     private static final String TAG_PID = "categoryID";
     private static final String TAG_NAME = "categoryName";
     // Notices JSONArray
     JSONArray allcategories = null;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.all_categories);
         // Hashmap for ListView
         NoticesList = new ArrayList<HashMap<String, String>>();
         // Loading notices in Background Thread
         new LoadAllCategories().execute();
         // Get listview
         ListView lv = getListView();
         // on seleting single item
         lv.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
                 // getting values from selected ListItem
             	String categoryid = ((TextView) view.findViewById(R.id.catid)).getText().toString();
                 // Starting new intent
             	int intCategoryID=Integer.parseInt(categoryid);
                 Intent in = new Intent(getApplicationContext(),FetchSelCatActivity.class);
                 // sending pid to next activity
                 Log.i("Category ID is:",categoryid);
                 in.putExtra("pathid", categoryid);
                 in.putExtra("categoryid", intCategoryID);
                 // starting new activity and expecting some response back
                 startActivityForResult(in, 100);
             }
         });
 }
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         // if result code 100
         if (resultCode == 100) {
             Intent intent = getIntent();
             finish();
             startActivity(intent);
         }
  
     }
  
     /**
      * Background Async Task to Load by making HTTP Request
      * */
     class LoadAllCategories extends AsyncTask<String, String, String> {
  
         /**
          * Before starting background thread Show Progress Dialog
          * */
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             pDialog = new ProgressDialog(FetchCategoriesActivity.this);
             pDialog.setMessage("Fetching Categories. Please wait...");
             pDialog.setIndeterminate(false);
             pDialog.setCancelable(false);
             pDialog.show();
         }
  
         /**
          * getting JSON data from url
          * */
         @Override
 		protected String doInBackground(String... args) {
             // Building Parameters
             List<NameValuePair> params = new ArrayList<NameValuePair>();
             // getting JSON string from URL
             JSONObject json = jParser.makeHttpRequest(url_all_categories, "GET", params);
  
             // Check your log cat for JSON reponse
             Log.d("All Categories: ", json.toString());
  
             try {
                 // Checking for SUCCESS TAG
                 int success = json.getInt(TAG_SUCCESS);
                 if (success == 1) {
                 	allcategories = json.getJSONArray(TAG_CATEGORIES);
                     for (int i = 0; i < allcategories.length(); i++) {
                         JSONObject c = allcategories.getJSONObject(i);
                         // Storing each json item in variable
                         String id = c.getString(TAG_PID);
                         String name = c.getString(TAG_NAME);
                         // creating new HashMap
                         HashMap<String, String> map = new HashMap<String, String>();
                         // adding each child node to HashMap key => value
                         map.put(TAG_PID, id);
                         map.put(TAG_NAME, name);
                         // adding HashList to ArrayList
                         NoticesList.add(map);
                     }
                 } else {
                     // nothing found exception to be implemented.
                 }
             } catch (JSONException e) {
                 e.printStackTrace();
             }
  
             return null;
         }
  
         /**
          * After completing background task Dismiss the progress dialog
          * **/
         @Override
 		protected void onPostExecute(String file_url) {
             pDialog.dismiss();
             // updating UI from Background Thread
             runOnUiThread(new Runnable() {
                 @Override
 				public void run() {
                     /**
                      * Updating parsed JSON data into ListView
                      * */
                     ListAdapter adapter = new SimpleAdapter(
                     		FetchCategoriesActivity.this, NoticesList,
                             R.layout.list_item_categories, new String[] { TAG_PID,
                                     TAG_NAME },
                             new int[] { R.id.catid, R.id.catname});
                     // updating listview
                     setListAdapter(adapter);
                 }
             });
         }
     }
 }
