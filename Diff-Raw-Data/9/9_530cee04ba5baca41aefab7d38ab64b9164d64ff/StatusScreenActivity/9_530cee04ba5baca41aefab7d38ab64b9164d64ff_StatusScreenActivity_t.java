 package com.example.friendzyapp;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.example.friendzyapp.HttpRequests.PostStatusRequest;
 import com.example.friendzyapp.HttpRequests.GetEventsRequest;
 import com.example.friendzyapp.StatusAndMeetUpListAdapter.*;
 import android.location.Location;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.text.TextUtils;
 import android.util.Log;
 
 public class StatusScreenActivity extends ListContainingActivity {
     static final String TAG = "StatusScreen";
     private static final String PREF_NOT_FIRST_RUN = "not_first_run_status";
     public static final Global.Entry<String, String> DUMMY_ENTRY = 
             new Global.Entry<String, String>("0", "Post something!");
 
     private String userId;
     
     private List<Entry<String,String>> friendStatuses;
     private List<MeetUp> meetUps;
     private List<StatusListable> statusAndMeetUps;
     
     private ListView statusAndMeetUpListView;
     public Boolean waitingOnAsyncTask;
     public String newStatus;
     private String loginRequestParams;
     
     public class GetEventsResponse {
         public List<MeetUp> data;
         // Object will either be a String or List<String>
         
         public GetEventsResponse() {
         	data = new ArrayList<MeetUp>();
         }
     }
     
     public class MeetUp {
     	public String[] statuses;
     	public String match_age;
     	public String[] attendees;
     	public String longitude;
     	public String location_name;
     	public String latitude;
     	
     	public MeetUp() {
     		statuses = new String[2];
     		attendees = new String[2];
     		match_age = "";
     		longitude = "";
     		location_name = "";
     		latitude = "";
         }
     }
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState, "FRIEND_INFO");
 		setContentView(R.layout.activity_status_screen);
 		
 		waitingOnAsyncTask = false;
 		
         Bundle extras = getIntent().getExtras();
         if (!extras.containsKey("FRIEND_INFO")) {
             Log.wtf(TAG, "no FRIEND_INFO");
         }
         
         loginRequestParams = extras.getString("LOGIN_REQUEST_PARAMS");
         userId = extras.getString("USER_ID");
 
         statusAndMeetUpListView = (ListView) findViewById(R.id.friend_status_list);
         meetUps = new ArrayList<StatusScreenActivity.MeetUp>();
         
         updateFriendsList(serverResponse.data.entrySet());
         
         doFacebookCallback();
 
         // Show the welcome toast if this is the first time we have been run.
         globals.showFirstTimeDialog(this, PREF_NOT_FIRST_RUN, R.string.status_intro_text);
 
         globals.userId = userId;
         
         runGetEvents();
         
     }
 	
 	// TODO: remove this silly method call
 	
 	private void updateFriendsList(Set<Entry<String, String>> data) {
         if (data.isEmpty()) {
             // TODO: Improve appearance
             friendStatuses = new ArrayList<Entry<String, String>>();
             friendStatuses.add(DUMMY_ENTRY);
         } else {
     		// The List and Adapter are now of type List<Entry<String, String>> (Map entrySets return Entry<K, V>).
             friendStatuses = new ArrayList<Entry<String, String>>(data);
         }
         
         updateAdapter();
 	}
 	
 	private void updateMeetUpsList(List<MeetUp> data) {
         meetUps = new ArrayList<MeetUp>();
         meetUps.addAll(data);
         Log.d(TAG, "updateMeetUpsList(): "+meetUps);
         Log.d(TAG, "size=" + Integer.toString(data.size()));
         updateAdapter();
 	}
 	
 	private void updateAdapter() {
 	    Log.d(TAG, "begin updateAdapter");
 	    
 	    if (statusAndMeetUps == null)
 	        statusAndMeetUps = new ArrayList<StatusListable>();
 	    statusAndMeetUps.clear();
         
         for (Entry<String, String> friend : friendStatuses)
             statusAndMeetUps.add(new FriendStatusListable(this, friend, userId));
         for (MeetUp meetUp : meetUps)
             statusAndMeetUps.add(new MeetUpStatusListable(this, meetUp, userId));
         
         Log.d(TAG, "statusAndMeetUps="+ statusAndMeetUps);
         
         if (listAdapter != null) {
         	runOnUiThread(new Runnable() { public void run() { listAdapter.notifyDataSetChanged(); } });
         	
         } else {
         	listAdapter = new StatusAndMeetUpListAdapter(this, this, R.id.friend_status_list, statusAndMeetUps);
             statusAndMeetUpListView.setAdapter(listAdapter);
         }
 	}
 	
 	public void relogin() {
 		// just reuse the params from the first time
 	    Log.d(TAG, "relogin requested");
 	    ProgressDialog progress = ProgressDialog.show(this, null, getString(R.string.loading));
 		new LoginUpdateAsyncTask(this, progress).execute(loginRequestParams);
 	}
 	
 	public void runGetEvents() {
 		Location location = globals.getLocation();
 	    
 	    Map<String, String> loc = new HashMap<String, String>();
 		loc.put("latitude", Double.toString(location.getLatitude()));
 		loc.put("longitude", Double.toString(location.getLongitude()));
 	    
 	    String params = globals.gson.toJson(new GetEventsRequest(userId, loc));
 	    GetEventsAsyncTask getevents = new GetEventsAsyncTask();
 	    getevents.execute(params);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getSupportMenuInflater().inflate(R.menu.status_screen, menu);
 		return true;
 	}
 	
 	// TODO: Refactor into something non-shitty
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch(item.getItemId())
 	    {
 	    case R.id.action_subscriptions:
 	    	Intent intent = new Intent(this, SubListActivity.class);
 	        //Intent intent = new Intent(this, SubscriptionListActivity.class);
 	        intent.putExtras(getIntent());
 	        startActivity(intent);
 	        return true;
 	    case R.id.action_refresh:
 	    	relogin();
 	    	runGetEvents();
 	        return true;
 	    }
 	    return super.onOptionsItemSelected(item);
 	}
 	
 	public void onPostStatus(View view) {	    
 		if (!waitingOnAsyncTask) {
 		    Log.d(TAG, "attempting to post status");
 		    
 		    EditText statusView = (EditText) findViewById(R.id.status_entry);
             newStatus = statusView.getText().toString();
 		    
 		    Log.d(TAG, "new status=" + newStatus);
 		    
 	        if (TextUtils.isEmpty(newStatus)) {
 	            Log.d(TAG, "status was empty, alerting user");
 	            statusView.setError(getString(R.string.error_empty_field));
 	            statusView.requestFocus();
 	            return;
 	        }
 	        
 	        // If there were no entries before, remove the dummy entry.
             if (friendStatuses.size() == 1 && friendStatuses.get(0).equals(DUMMY_ENTRY)) {
                 Log.d(TAG, "removing dummy entry");
                 friendStatuses.clear();
             }
 	        
 	        // Delete current status if already on list
            // Can't delete while modifying.
            for (Iterator<Entry<String, String>> iterator = friendStatuses.iterator(); iterator.hasNext();) {
                Entry<String, String> entry = iterator.next();
 	            if (entry.getKey().equals(userId)) {
 	                Log.d(TAG, "deleting old self status");
	                iterator.remove();
 	            }
            }
 
 	        // Add our new status.
 	        friendStatuses.add(new Global.Entry<String, String>(userId, newStatus)); // displays it to our list only!!
 	        
 	        // Trigger name update. We're on the UI thread
 	        updateAdapter();
 	           
 	        Log.d(TAG, "friendStatuses=" + friendStatuses);
 		    
 		    String params = globals.gson.toJson(new PostStatusRequest(userId, newStatus));
 		    
 		    ProgressDialog progress = ProgressDialog.show(this, null, getString(R.string.loading));
 		    new PostStatusAsyncTask(this, progress).execute(params);
 		    
 		    waitingOnAsyncTask = true;
 		    // TODO: start spinning loading bar thing
 		} else {
 		    Log.d(TAG, "Didn't try to post status because we already have a task executing");
 		}
 	    
 	}
 	
 	public class GetEventsAsyncTask extends InternetConnectionAsyncTask {
         private static final String resource = "get_events";
 
         public GetEventsAsyncTask() {
             super(resource);
             Log.d(TAG, "init GetEventsAsyncTask");
         }
         
         protected void onPostExecute(final String respString) {
             if (respString == null) {
                 Log.e(TAG, "reader is null, did download fail?");
                 return;
             }
             Log.d(TAG, "GetEventsAsyncTask: onPostExecute: serverResponse:" + respString);
             //parseGetEventsResponse(respString);
             
             GetEventsResponse response = globals.gson.fromJson(respString, GetEventsResponse.class);
             
             Log.d(TAG, " ---- "+response);
             if (response.data.isEmpty()) {
             	Log.d(TAG, "No events nearby, leaving list empty");
             } else {
             	Log.d(TAG, "events exist!!!!!!");
             	for (MeetUp meetUp : response.data) {
             		Log.d(TAG, "    "+meetUp.location_name);
             		
             	}
             	updateMeetUpsList(response.data);
 
        		}
         }
     }
 
     public class LoginUpdateAsyncTask extends InternetConnectionAsyncTask {
 	    private static final String resource = "login";
 	    private Activity activity;
 	    private ProgressDialog progress;
 	
 	    public LoginUpdateAsyncTask(Activity activity, ProgressDialog progress) {
 	        super(resource);
             this.activity = activity;
             this.progress = progress;
 	        Log.d(TAG, "init LoginUpdateAsyncTask");
 	    }
 	
 	    protected void onPostExecute(final String respString) {
 	        if (respString == null) {
 	            Log.e(TAG, "reader is null, did download fail?");
 	            return;
 	        }
 	        Log.d(TAG, "LoginUpdateAsyncTask: onPostExecute; " + respString);
 	        
 	        activity.runOnUiThread(new Runnable() { public void run() { progress.dismiss(); }});
 	        // Use serverResponse so doFacebookCallback will work
 	        // Not strictly necessary now that void doFacebookCallback(Iterable<String>) exists, but whatever.
 	        serverResponse = globals.gson.fromJson(respString, GenericDataResponse.class);
 	        updateFriendsList(serverResponse.data.entrySet());
 	        doFacebookCallback();
 	    }
 	}
 	
     public class PostStatusAsyncTask extends InternetConnectionAsyncTask {
         private static final String resource = "set_status";
         private Activity activity;
         private ProgressDialog progress;
         
         public PostStatusAsyncTask(Activity activity, ProgressDialog progress) {
             super(resource);
             this.activity = activity;
             this.progress = progress;
             Log.d(TAG, "init SetStatusAsyncTask");
         }
         protected void onPostExecute(final String respString) {
             if (respString == null) {
                 Log.e(TAG, "reader is null, did download fail?");
                 return;
             }
             
             Log.d(TAG, "PostStatusAsyncTask: onPostExecute: serverResponse:" + respString);
             
             // First time I implemented this there were ConcurrentModificationExceptions in friendStatus.
             // Don't ask me why.
             activity.runOnUiThread(new Runnable() { public void run() { progress.dismiss(); }});
             parsePostStatusResponse(respString);
         }
     }    
     private void parsePostStatusResponse(String respString) {
 		GenericDataResponse response = globals.gson.fromJson(respString, GenericDataResponse.class);
 		
 		if (response.data.isEmpty()) {
 			Log.d(TAG, "Data from response is null or empty!  Time to launch Toast!");
 			Toast.makeText(this, R.string.match_no_matches, Toast.LENGTH_SHORT).show();
 		} else {
 			Log.d(TAG, "launching MatchActivity");
 	        Intent intent = new Intent(this, MatchActivity.class);
 	        intent.putExtra("SERVER_RESPONSE", respString);
 	        intent.putExtra("POSTED_STATUS", newStatus);
 	        intent.putExtra("USER_ID", userId);
 	        intent.putExtra("FROM_NOTIFICATION", false);
 	        
 	        startActivity(intent);
 		}
 		
 		waitingOnAsyncTask = false;
     }
 }
