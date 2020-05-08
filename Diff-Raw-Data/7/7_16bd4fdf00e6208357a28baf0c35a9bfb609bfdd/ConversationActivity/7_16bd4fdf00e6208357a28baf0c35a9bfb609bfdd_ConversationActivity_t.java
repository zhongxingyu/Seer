 package org.research.chatclient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.LinkedList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class ConversationActivity extends BaseActivity implements Constants{
 	
 	private SQLiteDatabase db;
 	private ProgressDialog mProgress;
 	private SharedPreferences mPrefs;
 	private JSONArray mUsers;
 	private Spinner spin;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.conversation);
 		MessagesTable table = new MessagesTable(ConversationActivity.this);
 		db = table.getWritableDatabase();
 		spin = (Spinner)findViewById(R.id.personSpin);
 		String convo = getIntent().getStringExtra(InboxActivity.CONVO_USER);
 	    if(convo != null){
 	    	mPrefs = getSharedPreferences( CreateAccountActivity.PREFS, Context.MODE_PRIVATE );
 			mProgress = new ProgressDialog(ConversationActivity.this);
 		    mProgress.setIndeterminate(true);
 		    mProgress.setCancelable(false);
 		    mProgress.setMessage("Getting Conversation...");
 		    mProgress.show();
 	    	spin.setAdapter(new ArrayAdapter<String>(ConversationActivity.this, android.R.layout.simple_spinner_item, new String[]{convo}));
 	    	spin.setClickable(false);
 	    	((TextView)findViewById(R.id.placeHoldText)).setVisibility(View.GONE);
 	    	loadConvo(convo);
 	    }
 	    else{
 			mPrefs = getSharedPreferences( CreateAccountActivity.PREFS, Context.MODE_PRIVATE );
 			mProgress = new ProgressDialog(ConversationActivity.this);
 		    mProgress.setIndeterminate(true);
 		    mProgress.setCancelable(false);
 		    mProgress.setMessage("Getting Users...");
 		    mProgress.show();
 	    	new GetUsersTask().execute(new HttpGet("http://devimiiphone1.nku.edu/research_chat_client/testphp/get_users.php"));
 	    }
 	}
 	
 	public void sendMessage(View v){
 		
 		EditText messBox = (EditText)findViewById(R.id.messageText);
 		String message = messBox.getText().toString();
 		String sender = mPrefs.getString(CreateAccountActivity.USER, "");
 		String time = "" + System.currentTimeMillis();
 		String recipient = (String)spin.getSelectedItem();
 		Log.d("selected", recipient);
 		if(!message.equals("")){
 			insertMessage(sender, message, time);
 			try{
 	    		HttpPost httppost = new HttpPost("http://devimiiphone1.nku.edu/research_chat_client/testphp/send_message.php");
 	    		LinkedList<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
 	    		
 	    		nameValuePairs.add(new BasicNameValuePair("recipient", recipient));
 	    		nameValuePairs.add(new BasicNameValuePair("sender", sender));
 	    		nameValuePairs.add(new BasicNameValuePair("message", message));
 	    		nameValuePairs.add(new BasicNameValuePair("time", "" + time));
 	    		
 	    		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			    
 			    new SendMessageTask().execute(httppost);
 	    		
 	    	}catch(UnsupportedEncodingException e){
 	    		e.printStackTrace();
 	    	}
 		}
 	}
 	
 	private void loadConvo(String otherPers){
 		Cursor convoCursor = db.rawQuery("Select * from " + MESSAGE_TABLE_NAME + " where " + OTHER_MEMBER + "='" + otherPers + "' order by " + TIMESTAMP + " ASC", null);
     	while(convoCursor.moveToNext()){
     		if(convoCursor != null){
     			LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     			View v = null;
     			if(convoCursor.getString(convoCursor.getColumnIndex(RECIPIENT)).equals(convoCursor.getString(convoCursor.getColumnIndex(OTHER_MEMBER)))){
     				v = inflate.inflate(R.layout.sent_view, null, false);
     				TextView tv = (TextView) v.findViewById(R.id.sentText);
     				tv.setText(convoCursor.getString(convoCursor.getColumnIndex(MESSAGE)));
     				LinearLayout wrapper = (LinearLayout)findViewById(R.id.convoLay);
     				wrapper.addView(v);
     			}
     			if(convoCursor.getString(convoCursor.getColumnIndex(SENDER)).equals(convoCursor.getString(convoCursor.getColumnIndex(OTHER_MEMBER)))){
     				v = inflate.inflate(R.layout.received_view, null, false);
     				TextView tv = (TextView) v.findViewById(R.id.receivedText);
     				tv.setText(convoCursor.getString(convoCursor.getColumnIndex(MESSAGE)));
     				LinearLayout wrapper = (LinearLayout)findViewById(R.id.convoLay);
     				wrapper.addView(v);
     			}
     		}
     	}
     	if(mProgress.isShowing())
     		mProgress.dismiss();
 	}
 	
	@Override
	public void onBackPressed() {
		if(db != null && db.isOpen())
			db.close();
		super.onBackPressed();
	}
	
 	private class GetUsersTask extends AsyncTask<HttpGet, Void, InputStream> {
 	    @Override
 		 protected InputStream doInBackground(HttpGet... post) {
 	    	HttpClient httpclient = new DefaultHttpClient();
 	    	InputStream stream = null;
 	    	HttpResponse response;
 			try {
 				response = httpclient.execute(post[0]);
 				HttpEntity entity = response.getEntity();
 				stream = entity.getContent();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 	        return stream;
 	     }
 
 	     @Override
 	     protected void onPostExecute(InputStream result) {
 	    	 String text = "";
 	    	 
 	    	 try{
 	    		 if( result != null ){
 			    	 BufferedReader br = new BufferedReader(new InputStreamReader(result));
 			    	 String line = br.readLine();
 			    	 
 			    	 while( line != null ){
 			    		 text += line + " ";
 			    		 line = br.readLine();
 			    	 }
 	    		 }
 	    	 }catch (IOException e) {
 				e.printStackTrace();
 			}
 	    	 
 	    	 if(mProgress.isShowing())
 	    		 mProgress.dismiss();
 	    	 try {
 				mUsers = new JSONArray(text);
 				String[] users = new String[mUsers.length()];
 				for(int i = 0; i < mUsers.length(); i++)
 					users[i] = mUsers.getString(i);
 				Spinner personSpinner = (Spinner)findViewById(R.id.personSpin);
 				ArrayAdapter<String> adapter = new ArrayAdapter<String>(ConversationActivity.this, android.R.layout.simple_spinner_item, users);
 				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 				personSpinner.setAdapter(adapter);
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 	     }
 	 }
 }
 
