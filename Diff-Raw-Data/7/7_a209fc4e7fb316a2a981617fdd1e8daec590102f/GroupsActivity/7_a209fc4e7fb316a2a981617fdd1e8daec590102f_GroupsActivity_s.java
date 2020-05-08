 package uw.cse403.minion;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.TreeMap;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 
 public class GroupsActivity extends Activity {
 	private static final String GROUPNAME = "groupname";
 	private static final String GAME_MASTER = "gm";
 	
 	private static final String PHP_ADDRESS = "http://homes.cs.washington.edu/~elefse/getGroups.php";
 	private String username;
 	
 	// Declare the UI components
 	private ListView groupsListView;
 
 	// Change this array's name and contents to be the character information
 	// received from the database
 	private static ArrayList<HashMap<String, String>> testArray;
 
 	// Adapter for connecting the array above to the UI view
 	private SimpleAdapter adapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_groups);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		username = SaveSharedPreference.getPersistentUserName(GroupsActivity.this);
 		
 		GetGroupsTask task = new GetGroupsTask(this);
 		task.execute(username);
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.groups, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		GetGroupsTask task = new GetGroupsTask(this);
 		task.execute(username);
 	}
 	
 	public void gotoCreateGroup(View view) {
 		Intent intent = new Intent(this, GroupCreateActivity.class);
 		startActivity(intent);
 	}
 	
 	public void gotoPendingInvites(View view) {
 		Intent intent = new Intent(this, ViewInvitesActivity.class);
 		startActivity(intent);
 	}
 
 	private class GetGroupsTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {
 		private Context context;
 		
 		private GetGroupsTask(Context context) {
 			this.context = context;
 		}
 		
 	    /**
 	     * Makes the HTTP request and returns the result as a String.
 	     */
 	    protected ArrayList<HashMap<String, String>> doInBackground(String... args) {
 	        //the data to send
 	        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
 	        postParameters.add(new BasicNameValuePair("un", username));
 	        
 	        // Hashmap for ListView
 	        ArrayList<HashMap<String, String>> groupsArray = new ArrayList<HashMap<String, String>>();
 	        //ArrayList<String> groupsArray = new ArrayList<String>();
 			String result = null;
 	        
 	        //http post
 			String res;
 	        try{
 	        	result = CustomHttpClient.executeHttpPost(PHP_ADDRESS, postParameters);
 	        	res = result.toString();   
 	        	//res = res.replaceAll("\\s+", "");
 	        	JSONObject results  = new JSONObject(res);
 	        	JSONArray groups = results.getJSONArray("items");
 	        	// looping through groups
 	            for(int i = 0; i < groups.length(); i++){
 	                JSONObject c = groups.getJSONObject(i);
 	                 
 	                // Storing each json item in variable
 	                String groupName = c.getString("groupname");
 	                String gm = c.getString("gm");
 	                
 	                // creating new HashMap
 	                HashMap<String, String> map = new HashMap<String, String>();
 	                map.put(GROUPNAME, groupName);
 	                map.put(GAME_MASTER, gm);
 	                // adding HashList to ArrayList
 	                groupsArray.add(map);
 	            }
 	        } catch (Exception e) {
 	        	res = e.toString();
 	        	//Log.i("ERROR", res);
 	        }
 	        return groupsArray;
 	    }
 	 
 	    /**
 	     * Parses the String result and directs to the correct Activity
 	     */
 	    protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
 	    	testArray = result;
 	    	
 		    // Initialize the UI components
 	        groupsListView = (ListView) findViewById(R.id.groupsListView);
 
 		    //int[] toViews = {android.R.id.text1}; // The TextView in activity_characters
 
 	        // Create an empty adapter we will use to display the loaded data.
 	        // We pass null for the cursor, then update it in onLoadFinished()
 	        adapter = new SimpleAdapter(context, testArray,
 	        		android.R.layout.simple_list_item_2, new String[] { GROUPNAME, GAME_MASTER }, new int[] {
 	        			android.R.id.text1, android.R.id.text2 });
 	        groupsListView.setAdapter(adapter);
 	        
 	        groupsListView.setOnItemClickListener(new OnItemClickListener() {
 
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 					// When clicked, show a toast with the TextView text
 		            //Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
 					Intent intent = new Intent(context, ViewGroupActivity.class);
 					String groupname = ((TextView) view).getText().toString();
 					intent.putExtra(GROUPNAME, groupname);
 					startActivity(intent);
 				}
 	          });
 	    }
 	 
 	}
 	
 }
