 /**
  * Just fetching the items related to the selected category
  */
 package com.wbs.ginos;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
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
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
  
 public class FetchSelCatActivity extends ListActivity {
  
 
     private ProgressDialog pDialog;
     JSONParser jParser = new JSONParser();
     ArrayList<HashMap<String, String>> selquesList;
   
     Intent intent;
     String getcatID;
     TextView errorcode;
     private String url_all_selcats = "http://ginos.getfreehosting.co.uk/app/fetchselcategory.php";
  
     private static final String TAG_SUCCESS = "success";
     private static final String TAG_SELITEMS = "menu";
     private static final String TAG_newPID = "itemID";
     private static final String TAG_NAME = "itemname";
     private static final String TAG_INFO = "iteminfo";
     
     JSONArray selques = null;
  
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.all_items);
         
         selquesList = new ArrayList<HashMap<String, String>>();
   		//Intent selectedintent = getIntent();
         //String prevcatid = selectedintent.getExtras().getString("pathid");
         new LoadAllSelques().execute();
         ListView lv = getListView();
         
        Button gohome = (Button)findViewById(R.id.gohome);        
         		gohome.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         Intent i = new Intent(getApplicationContext(), MainActivity.class);
                         i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                         startActivity(i);
                     }
                 });
         
         Button ansbtn =(Button)findViewById(R.id.cartbtn);
         ansbtn.setOnClickListener(new Button.OnClickListener(){
         	@Override
         	 public void onClick(View v) {
         	Intent i = new Intent(getApplicationContext(), ShoppingCartActivity.class);
         	startActivity(i);
         	}});
         	lv.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view,
                     int position, long id) {
             	String singleitemid = ((TextView) view.findViewById(R.id.itemid)).getText()
                         .toString();
             	intent = getIntent();
                 getcatID = intent.getExtras().getString("pathid");
                 int intValueofcatid = intent.getIntExtra("categoryid", 0);
                 Log.i("Int Value of CatID: ", "" +intValueofcatid);
                 Intent myIntent = new Intent(FetchSelCatActivity.this, SingleItemActivity.class);
                 myIntent.putExtra("TAG_SINGLEITEMID", singleitemid);
                 myIntent.putExtra("TAG_SINGLEITEMCAT", getcatID);
                 myIntent.putExtra("int_value", intValueofcatid);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
      		   FetchSelCatActivity.this.startActivity(myIntent);      
             }
         });
     }
 
     class LoadAllSelques extends AsyncTask<String, String, String> {
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             pDialog = new ProgressDialog(FetchSelCatActivity.this);
             pDialog.setMessage("Loading items. Please wait...");
             pDialog.setIndeterminate(false);
             pDialog.setCancelable(false);
             pDialog.show();
         }
 
         @Override
 		protected String doInBackground(String... args) {
             // Building Parameters
             List<NameValuePair> params = new ArrayList<NameValuePair>();
             // getting JSON string from URL
             intent = getIntent();
             getcatID = intent.getExtras().getString("pathid");
             params.add(new BasicNameValuePair("catID",getcatID));  //<<<< add here
             JSONObject json = jParser.makeHttpRequest(url_all_selcats, "GET", params);
 
             Log.d("All Items: ", json.toString());
  
             try {
                 // Checking for SUCCESS TAG
                 int success = json.getInt(TAG_SUCCESS);
                 if (success == 1) {
                 	selques = json.getJSONArray(TAG_SELITEMS);
                     for (int i = 0; i < selques.length(); i++) {
                         JSONObject c = selques.getJSONObject(i);
                         String id = c.getString(TAG_newPID);
                         String name = c.getString(TAG_NAME);
                         String info = c.getString(TAG_INFO);
                         
                       
                         // creating new HashMap
                         HashMap<String, String> map = new HashMap<String, String>();
                         // adding each child node to HashMap key => value
                         map.put(TAG_newPID, id);
                         map.put(TAG_NAME, name);
                         map.put(TAG_INFO, info);
                         
                         // adding HashList to ArrayList
                         selquesList.add(map);
                     }
                 } else {
                    // nothing was found.
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
                     		FetchSelCatActivity.this, selquesList,
                             R.layout.list_selected_items, new String[] { TAG_newPID,
                                     TAG_NAME, TAG_INFO },
                             new int[] { R.id.itemid, R.id.itemname, R.id.iteminfo});
                     // updating listview
                    
                     setListAdapter(adapter);
                 }
             });
         }
     }
 }
