 package ca.ryerson.scs.rus.messenger;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import ca.ryerson.scs.rus.MenuActivity;
 import ca.ryerson.scs.rus.R;
 import ca.ryerson.scs.rus.SplashActivity;
 import ca.ryerson.scs.rus.adapters.HttpRequestArrayAdapter;
 import ca.ryerson.scs.rus.adapters.MessageListAdapter;
 import ca.ryerson.scs.rus.messenger.objects.Message;
 import ca.ryerson.scs.rus.util.DefaultUser;
 import ca.ryerson.scs.rus.util.IntentRes;
 import ca.ryerson.scs.rus.util.ProcessList;
 import ca.ryerson.scs.rus.util.URLResource;
 import ca.ryerson.scs.rus.util.ValidityCheck;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class MessageActivity extends Activity implements OnClickListener {
 	private ImageButton btnMapView, btnHome, btnMsg, btnPref, btnFriend, btnNewMail;
 
 	MessageListAdapter mla;
 	Context context;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.messages);
 
 		context = this;
 		
 		btnMapView = (ImageButton) findViewById(R.id.IBLook);
 		btnHome = (ImageButton) findViewById(R.id.IBHome);
 		btnMsg = (ImageButton) findViewById(R.id.IBMsg);
 		btnPref = (ImageButton) findViewById(R.id.IBPref);
 		btnFriend = (ImageButton) findViewById(R.id.IBFriend);
//		btnNewMail = (ImageButton) findViewById(R.id.IBNewMail);
 
 		btnMapView.setFocusable(true);
 		btnHome.setFocusable(true);
 		btnMsg.setFocusable(true);
 		btnPref.setFocusable(true);
 		btnFriend.setFocusable(true);
//		btnNewMail.setFocusable(true);
 
 		btnMapView.setOnClickListener(this);
 		btnHome.setOnClickListener(this);
 		btnMsg.setOnClickListener(this);
 		btnPref.setOnClickListener(this);
 		btnFriend.setOnClickListener(this);
//		btnNewMail.setOnClickListener(this);
 
 		
 		String URLfinal = ValidityCheck.removeWhiteSpace(URLResource.MESSAGES+"?user="+DefaultUser.getUser());
 		Log.i("THIS IS THE URL", ""+URLfinal);
 		HttpRequestArrayAdapter.httpRequest(this, URLfinal,
 				new MessageHandler());
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == btnHome) {
 			if (SplashActivity.DEBUG) {
 				if (SplashActivity.DEBUG)Log.i(MenuActivity.TAG, "Home button");
 			}
 			// TODO: Make it go back to the main page while finishing all other activities
 			
 		}else if (v == btnMapView) {
 			if (SplashActivity.DEBUG) {
 				if (SplashActivity.DEBUG)Log.i(MenuActivity.TAG, "Map View Button");
 			}
 			Intent newIntent = new Intent(IntentRes.SOCIALITE_MAP_STRING);
 			newIntent.putExtra("username", DefaultUser.getUser());
 			finish();
 			startActivity(newIntent);
 		
 		}else if (v == btnMsg) {
 			if (SplashActivity.DEBUG){
 				if (SplashActivity.DEBUG)Log.i(MenuActivity.TAG, "Message Button");
 			}
 			Intent newIntent = new Intent(IntentRes.MESSAGE_STRING);
 			newIntent.putExtra("username", DefaultUser.getUser());
 			finish();
 			startActivity(newIntent);
 		
 		}else if (v == btnFriend) {
 			if (SplashActivity.DEBUG){
 				if (SplashActivity.DEBUG)Log.i(MenuActivity.TAG, "Friend Button");
 			}
 			Intent newIntent = new Intent(IntentRes.FRIEND_STRING);
 			newIntent.putExtra("username", DefaultUser.getUser());
 			finish();
 			startActivity(newIntent);
 			
 			
 		}else if (v == btnPref) {
 			if (SplashActivity.DEBUG){
 				if (SplashActivity.DEBUG)Log.i(MenuActivity.TAG, "Preference Button");
 				}
 			Intent newIntent = new Intent(IntentRes.PREFERENCE_STRING);
 			newIntent.putExtra("username", DefaultUser.getUser());
 			finish();
 			startActivity(newIntent);
 			}
 		else if (v == btnNewMail) {
 			if (SplashActivity.DEBUG){
 				if (SplashActivity.DEBUG)Log.i(MenuActivity.TAG, "New Mail Button");
 				}
 			Intent newIntent = new Intent(IntentRes.NEW_MESSAGE_STRING);
 			newIntent.putExtra("username", DefaultUser.getUser());
 			finish();
 			startActivity(newIntent);
 			}
 		
 	}
 	
 	private class MessageHandler implements HttpRequestArrayAdapter.ResponseHandler {
 
 		@Override
 		public void postResponse(JSONArray response) {
 			final ArrayList<Message> messageList = ProcessList.processMessages(response);
 			mla = new MessageListAdapter(context, 0, messageList);
 			ListView messageListView = (ListView) findViewById(R.id.list);
 			messageListView.setAdapter(mla);
 			messageListView.setOnItemClickListener(new OnItemClickListener() {
 
 				public void onItemClick(AdapterView<?> av, View v, int position, long id) {
 					
 					/*
 					Intent intent = new Intent(IntentRes.PROFILE_STRING);
 					intent.putExtra("username", messageList.get(position).getUsername());
 					startActivity(intent);
 					*/
 				}
 			});
 			
 		}
 
 		@Override
 		public void postTimeout() {
 			// TODO Auto-generated method stub		
 		}
 		
 	}
 }
