 package com.final_proj.flores;
 
 import java.util.LinkedList;
 
 import oauth.signpost.OAuthConsumer;
 
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.final_proj.flores.UserStatusRecords.UserStatusRecord;
 
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
 
 
 public class GetAccess extends FragmentActivity implements LoaderCallbacks<Cursor> {
     public static final String TAG = GetAccess.class.toString();
 
 //    private CheckBox mCB;
 //    private EditText mEditor;
 //    private Button mButton;
 //    private TextView mUser;
 //    private TextView mLast;
 
     private OAuthConsumer mConsumer = null;
 
     private String mToken;
     private String mSecret;
 
     private SharedPreferences mSettings;
 
 
     KeysProvider mKeysProvider = new MyKeysProvider();
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.main);
 
         mConsumer = ((App) getApplication()).getOAuthConsumer();
 
 //        mCB = (CheckBox) this.findViewById(R.id.enable);
 //        mCB.setChecked(false);
 //        mCB.setOnClickListener(new LoginCheckBoxClickedListener());
 //
 //        mEditor = (EditText) this.findViewById(R.id.editor);
 //
 //        mButton = (Button) this.findViewById(R.id.post);
 //        mButton.setOnClickListener(new PostButtonClickListener());
 //
 //        mUser = (TextView) this.findViewById(R.id.user);
 //        mLast = (TextView) this.findViewById(R.id.last);
 
 
         mSettings = PreferenceManager.getDefaultSharedPreferences(this);
 
 
         getSupportLoaderManager().initLoader(App.BLOA_LOADER_ID, null, (LoaderCallbacks<Cursor>) this);
         //getting authorization from user
         //GetAccess.this.startActivity(new Intent(GetAccess.this, OAuthActivity.class));
         startUp();
 }
 
     @Override
     public void onResume() {
         super.onResume();
      // We look for saved user keys
         if(mSettings.contains(App.USER_TOKEN) && mSettings.contains(App.USER_SECRET)) {
             mToken = mSettings.getString(App.USER_TOKEN, null);
             mSecret = mSettings.getString(App.USER_SECRET, null);
             // If we find some we update the consumer with them
             if(!(mToken == null || mSecret == null)) {
                 mConsumer.setTokenWithSecret(mToken, mSecret);
                 (new GetCredentialsTask()).execute();
             }
         }
     }
 
     public void startUp(){
    	if(mSettings.contains(App.USER_TOKEN) && mSettings.contains(App.USER_SECRET)) {
            mToken = mSettings.getString(App.USER_TOKEN, null);
            mSecret = mSettings.getString(App.USER_SECRET, null);
            // If we find some we update the consumer with them
            if(!(mToken == null || mSecret == null)) {
                mConsumer.setTokenWithSecret(mToken, mSecret);
                (new GetCredentialsTask()).execute();
            }
        }
    	else{
    		GetAccess.this.startActivity(new Intent(GetAccess.this, OAuthActivity.class));
    	}
     }
     // These parameters are needed to talk to the messaging service
     public HttpParams getParams() {
         HttpParams params = new BasicHttpParams();
         // set to false to avoid an Expectation Failed: error
         HttpProtocolParams.setUseExpectContinue(params, false);
         return params;
     }
 
     //----------------------------
     // This task is run on every onResume(), to make sure the current credential are valid.
     // This is probably overkill for a non-educational program
     class GetCredentialsTask extends AsyncTask<Void, Void, Boolean> {
 
         DefaultHttpClient mClient = new DefaultHttpClient();
         ProgressDialogFragment mDialog;
 
         @Override
         protected void onPreExecute() {
             mDialog = ProgressDialogFragment.newInstance(R.string.auth_progress_title, R.string.auth_progress_text);
             mDialog.show(getSupportFragmentManager(), "auth");
         }
 
         @Override
         protected Boolean doInBackground(Void... arg0) {
             JSONObject jso = null;
             HttpGet get = new HttpGet(App.VERIFY_URL_STRING);
             try {
                 mConsumer.sign(get);
                 String response = mClient.execute(get, new BasicResponseHandler());
                 jso = new JSONObject(response);
                 makeNewUserStatusRecord(parseVerifyUserJSONObject(jso));
                 return true;
             } catch (Exception e) {
                 // Expected if we don't have the proper credentials saved away
             }
             return false;
         }
 
         // This is in the UI thread, so we can mess with the UI
         @Override
         protected void onPostExecute(Boolean loggedIn) {
             mDialog.dismiss();
 //            mCB.setChecked(loggedIn);
 //            mButton.setEnabled(loggedIn);
 //            mEditor.setEnabled(loggedIn);
             if (loggedIn) {
                 //TimelineSelector ss = new TimelineSelector(App.HOME_TIMELINE_URL_STRING);
            	//clearing out the Timeline and reloading it
            	deleteTimelineRecords();
            	deleteStatusRecord();
             	TimelineSelector ss = new TimelineSelector(App.BEYONCE_LIST_URL_STRING);
             	new GetTimelineTask().execute(ss);
             } else {
                 deleteStatusRecord();
                 deleteTimelineRecords();
             }
         }
     }
 
     private int deleteTimelineRecords() {
         return getContentResolver().delete(UserStatusRecords.CONTENT_URI, App.USER_TIMELINE_QUERY_WHERE, null);
     }
 
     private void deleteStatusRecord() {
         getContentResolver().delete(UserStatusRecords.CONTENT_URI, App.USER_STATUS_QUERY_WHERE, null);
     }
 
     private ContentValues parseVerifyUserJSONObject(JSONObject object) throws Exception {
         ContentValues values = new ContentValues();
         values.put(UserStatusRecord.USER_NAME, object.getString("name"));
         values.put(UserStatusRecord.RECORD_ID, object.getInt("id_str"));
         values.put(UserStatusRecord.USER_CREATED_DATE, object.getString("created_at"));
         JSONObject status = object.getJSONObject("status");
         values.put(UserStatusRecord.USER_TEXT, status.getString("text"));
         return values;
     }
 
     private ContentValues parseTimelineJSONObject(JSONObject object) throws Exception {
         ContentValues values = new ContentValues();
         JSONObject user = object.getJSONObject("user");
         values.put(UserStatusRecord.USER_NAME, user.getString("name"));
         values.put(UserStatusRecord.RECORD_ID, user.getInt("id_str"));
         values.put(UserStatusRecord.USER_CREATED_DATE, object.getString("created_at"));
         values.put(UserStatusRecord.USER_TEXT, object.getString("text"));
         return values;
     }
 
     private void makeNewUserStatusRecord(ContentValues values) {
         // Delete any existing records for user
         getContentResolver().delete(UserStatusRecords.CONTENT_URI, App.USER_STATUS_QUERY_WHERE, null);
         try {
             // Distinguish this as a User Status singleton, regardless of origin
             values.put(UserStatusRecord.LATEST_STATUS, "true");
             getContentResolver().insert(UserStatusRecords.CONTENT_URI, values);
         } catch (Exception e) {
             Log.e(TAG, "Exception adding users status record", e);
         }
     }
 
 //    class PostButtonClickListener implements OnClickListener {
 //        @Override
 //        public void onClick(View v) {
 //            String postString = mEditor.getText().toString();
 //            if (postString.length() == 0) {
 //                Toast.makeText(GetAccess.this, getText(R.string.tweet_empty), Toast.LENGTH_SHORT).show();
 //            } else {
 //                new PostTask().execute(postString);
 //            }
 //        }
 //    }
 //
 //    class LoginCheckBoxClickedListener implements OnClickListener {
 //
 //        @Override
 //        public void onClick(View v) {
 //            if(mCB.isChecked()) {
 //                GetAccess.this.startActivity(new Intent(GetAccess.this, OAuthActivity.class));
 //            } else {
 //                App.saveAuthInformation(mSettings, null, null);
 //                deleteStatusRecord();
 //                deleteTimelineRecords();
 //                mButton.setEnabled(false);
 //                mEditor.setEnabled(false);
 //                mEditor.setText(null);
 //                updateUI(null, null);
 //            }
 //            mCB.setChecked(false); // the oauth callback will set it to the proper state
 //        }
 //    }
     
     
     //----------------------------
     // This task posts a message to your message queue on the service.
     class PostTask extends AsyncTask<String, Void, JSONObject> {
 
         ProgressDialogFragment mDialog;
         DefaultHttpClient mClient = new DefaultHttpClient();
 
 
         @Override
         protected void onPreExecute() {
             mDialog = ProgressDialogFragment.newInstance(R.string.tweet_progress_title, R.string.tweet_progress_text);
             mDialog.show(getSupportFragmentManager(), "auth");
         }
 
         @Override
         protected JSONObject doInBackground(String... params) {
 
             JSONObject jso = null;
             try {
 
                 HttpPost post = new HttpPost(App.STATUSES_URL_STRING);
                 LinkedList<BasicNameValuePair> out = new LinkedList<BasicNameValuePair>();
                 out.add(new BasicNameValuePair("status", params[0]));
                 post.setEntity(new UrlEncodedFormEntity(out, HTTP.UTF_8));
                 post.setParams(getParams());
                 // sign the request to authenticate
                 mConsumer.sign(post);
                 String response = mClient.execute(post, new BasicResponseHandler());
                 jso = new JSONObject(response);
                 makeNewUserStatusRecord(parseTimelineJSONObject(jso));
             } catch (Exception e) {
                 Log.e(TAG, "Post Task Exception", e);
             }
             return jso;
         }
 
         // This is in the UI thread, so we can mess with the UI
         protected void onPostExecute(JSONObject jso) {
             mDialog.dismiss();
 //            GetAccess.this.mEditor.setText(null);
         }
     }
 
 
     class TimelineSelector extends Object {
         public String url; // the url to perform the query from
         // not all these apply to every url - you are responsible
         public Long since_id; // ids newer than this will be fetched
         public Long max_id; // ids older than this will be fetched
         public Integer count; // # of tweets to fetch Max is 200
         public Integer page; // # of page to fetch (with limits)
         public Integer b_l_id; //id of the beyonce list id
 
         public TimelineSelector(String u) {
             url = u;
             max_id = null;
             since_id = null;
             count = null;
             page = null;
         }
 
         public TimelineSelector(String u, Long since, Long max, Integer cnt, Integer pg) {
             url = u;
             max_id = max;
             since_id = since;
             count = cnt;
             page = pg;
         }
     }
 
     class GetTimelineTask extends AsyncTask<TimelineSelector, Void, Void> {
 
         ProgressDialogFragment mDialog;
         DefaultHttpClient mClient = new DefaultHttpClient();
 
         @Override
         protected void onPreExecute() {
             mDialog = ProgressDialogFragment.newInstance(R.string.timeline_progress_title, R.string.timeline_progress_text);
             mDialog.show(GetAccess.this.getSupportFragmentManager(), "auth");
         }
 
         @Override
         protected Void doInBackground(TimelineSelector... params) {
             JSONArray array = null;
             try {
                 Uri sUri = Uri.parse(params[0].url);
                 Uri.Builder builder = sUri.buildUpon();
                 if(params[0].since_id != null) {
                     builder.appendQueryParameter("since_id", String.valueOf(params[0].since_id));
                 } else if (params[0].max_id != null) { // these are mutually exclusive
                     builder.appendQueryParameter("max_id", String.valueOf(params[0].max_id));
                 }
                 if(params[0].count != null) {
                     builder.appendQueryParameter("count", String.valueOf((params[0].count > 200) ? 200 : params[0].count));
                 }
                 if(params[0].page != null) {
                     builder.appendQueryParameter("page", String.valueOf(params[0].page));
                 }
                 builder.appendQueryParameter("list_id", "89430082");
                 HttpGet get = new HttpGet(builder.build().toString());
                 mConsumer.sign(get);
                 String response = mClient.execute(get, new BasicResponseHandler());
                 array = new JSONArray(response);
                 // Delete the existing timeline
                deleteTimelineRecords();
                //^added.
                 ContentValues[] values = new ContentValues[array.length()];
                 for(int i = 0; i < array.length(); ++i) {
                     JSONObject status = array.getJSONObject(i);
                     values[i] = parseTimelineJSONObject(status);
                 }
                 getContentResolver().bulkInsert(UserStatusRecords.CONTENT_URI, values);
             } catch (Exception e) {
                 Log.e(TAG, "Get Timeline Exception", e);
             }
             return null;
         }
 
         // This is in the UI thread, so we can mess with the UI
         @Override
         protected void onPostExecute(Void nada) {
             mDialog.dismiss();
         }
 }
 
     @Override
     public Loader<Cursor> onCreateLoader(int loaderId, Bundle savedValues) {
         // Create a CursorLoader that will take care of creating a cursor for the data
         return new CursorLoader(this, UserStatusRecords.CONTENT_URI,
             App.USER_STATUS_PROJECTION, App.USER_STATUS_QUERY_WHERE,
             null, UserStatusRecord.DEFAULT_SORT_ORDER);
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
         // We got something but it might be empty
         String name = null, last = null;
         if (cursor.moveToFirst()) {
             name = cursor.getString(App.IDX_USER_STATUS_USER_NAME);
             last = cursor.getString(App.IDX_USER_STATUS_USER_TEXT);
         }
         updateUI(name, last);
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         updateUI(null, null);
     }
 
     private void updateUI(String userName, String lastMessage) {
         if (userName != null && lastMessage != null) {
 //            mUser.setText(userName);
 //            mLast.setText(lastMessage);
         } else {
 //            mUser.setText(getString(R.string.userhint));
 //            mLast.setText(getString(R.string.userhint));
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.twitter:
             deleteTimelineRecords();
             //TimelineSelector ss = new TimelineSelector(App.HOME_TIMELINE_URL_STRING);
             TimelineSelector ss = new TimelineSelector(App.BEYONCE_LIST_URL_STRING);
             new GetTimelineTask().execute(ss);
             return true;
         case R.id.logout:
         	 App.saveAuthInformation(mSettings, null, null);
              deleteStatusRecord();
              deleteTimelineRecords();
              updateUI(null, null);
              finish();
         default:
             return false;
         }
     }
 
 }
