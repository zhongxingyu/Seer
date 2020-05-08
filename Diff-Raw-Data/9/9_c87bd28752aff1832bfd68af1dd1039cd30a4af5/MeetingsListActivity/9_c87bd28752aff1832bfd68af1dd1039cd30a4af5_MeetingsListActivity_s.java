 package com.madp.meetme;
 
 import java.util.List;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.madp.meetme.common.entities.Meeting;
 import com.madp.meetme.webapi.WebService;
 import com.madp.utils.Logger;
 import com.madp.utils.MeetingsListAdapter;
 import com.madp.utils.SerializerHelper;
 import com.madp.utils.Statics;
 
 //TODO: add logs for debugging
 //TODO: refreshList() should be done on a separate thread and should display progress
 //TODO: implement refresh meeting list button
 //TODO: Delete meetings should take place in MeetingInfo activity
 /**
  * Display meetings where user is participant or creator. *
  * 
  * @author Niklas and Peter initial version without server integration
  * @author Saulius 2012-02-19 integrated WebService and removed some obsolete stuff, added to fetch only user's meetings
  * 
  */
 public class MeetingsListActivity extends ListActivity {	
 
 	private final String TAG = "MeetingListActivity";
 	private List<Meeting> meetings ;
 
 	private MeetingsListAdapter listAdapter;
 	private ImageButton newMeetingButton, exitButton;
 	private static final int CREATE_NEW_MEETING_RESULT = 891030;
 	private static final int CLICK_ON_MEETING_LIST_ELEMENT_RESULT = 860604;
 
 	private WebService ws;
 	
 	private Context mContext;	
 	private SharedPreferences prefs;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.meetinglist);
 		mContext = this;
 		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		ws = new WebService(new Logger());
 		
 		newMeetingButton = (ImageButton) findViewById(R.id.new_meeting_button);
 		exitButton = (ImageButton) findViewById(R.id.logout);
 
 		/*
 		 * We should have user name and user email in the properties.		 * 
 		 */
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());		
 		if (prefs.getString(Statics.USERNAME, null) == null){
 			showUserNameDialog(); // user will enter his name here	
 		} else {
 			Log.d(TAG, "User name:"+prefs.getString(Statics.USERNAME, null));
 		}
 		
 		if (prefs.getString(Statics.USEREMAIL, null) == null){
 			// get default account email
 			final SharedPreferences.Editor prefsEditor = prefs.edit();	
 			Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
 			if (accounts.length > 0) {		
 				Account account = accounts[0];
 				Log.d(TAG, "Found account type:"+account.type+" name:"+account.name+" saving...");		
 				prefsEditor.putString(Statics.USEREMAIL, account.name);
 				prefsEditor.commit();
 			} else {
 				Toast.makeText(this, "Could not get users email, using noemail@default.com", Toast.LENGTH_SHORT).show();
 				Log.e(TAG, "Could not get users email, using noemail@default.com");
 				prefsEditor.putString(Statics.USEREMAIL, "noemail@default.com");
 				prefsEditor.commit();
 			}
 		} else {
 			Log.d(TAG, "User email:"+prefs.getString(Statics.USEREMAIL, null));	
 		}		
 		
 		if(meetings == null)
 			refreshList();
 		if (meetings != null) {
 			listAdapter = new MeetingsListAdapter(this, R.layout.meetingrow, meetings);
 			setListAdapter(listAdapter);
 		}
 
 		/* Implement listers for buttons and a Item in the list */
 		exitButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), MeetMeBackgroundActivity.class);
 				startActivity(intent);
 				//finish();
 			}
 		});
 
 		/* Click on a listitem */
 		this.getListView().setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 				
 				Intent intent = new Intent(arg1.getContext(), MeetingInfoActivity.class);
 				Bundle b=new Bundle();
 				b.putByteArray("meeting", SerializerHelper.serializeObject(meetings.get(position)));			
 				intent.putExtras(b);
 				startActivityForResult(intent, CLICK_ON_MEETING_LIST_ELEMENT_RESULT);
 			
 				
 			}
 		});
 
 		/* Create a new Meeting by going to the Activity */
 		newMeetingButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), NewMeetingActivity.class);
 				startActivityForResult(intent, CREATE_NEW_MEETING_RESULT);
 			}
 		});
 	}
 
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d(TAG, "Result code received:"+resultCode);
 		// TODO: should be two situations: new meeting was created or not, update only if created
 		refreshList();
 		if (requestCode == CREATE_NEW_MEETING_RESULT) {
 			if (resultCode == RESULT_OK) { 
 				Bundle b = data.getExtras();
 				if(b != null){
 					Meeting s = (Meeting) SerializerHelper.deserializeObject(b.getByteArray("meeting"));
					meetings.add(s);
					listAdapter = new MeetingsListAdapter(this, R.layout.meetingrow, meetings);
					setListAdapter(listAdapter);
					listAdapter.setItems(meetings);
 					listAdapter.notifyDataSetChanged();
 				}
 			}
 		} else if (requestCode == CLICK_ON_MEETING_LIST_ELEMENT_RESULT) {
 			// Meeting deletion and update should be performed in MeetingInfo
 		}
 	}
 
 	/**
 	 * Fetch meetings from server
 	 */
 	private void refreshList() {
 		Log.d(TAG, "refreshList");
 		List<Meeting> meetingsTmp = ws.getUserMeetings(prefs.getString(Statics.USEREMAIL, ""), 0, 100);
 		if (meetingsTmp != null) {
 			meetings = meetingsTmp;
 			if (listAdapter != null) {
 				listAdapter.setItems(meetings);
 				listAdapter.notifyDataSetChanged();
 			}
 		}
 		
 	}
 	
 	private void showUserNameDialog(){
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());		
 		if (prefs.getString(Statics.USERNAME, null) == null){
 			final SharedPreferences.Editor prefsEditor = prefs.edit();		
 				
 			//Create dialog where user will enter his name			
             LayoutInflater factory = LayoutInflater.from(this);
             final View textEntryView = factory.inflate(R.layout.dialog_user_name_entry, null);            
             AlertDialog userNameDialog = new AlertDialog.Builder(this)                
                 .setTitle("Please enter your name")
                 .setView(textEntryView)
                 .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                     @Override
 					public void onClick(DialogInterface dialog, int whichButton) {         
                     	EditText editText = (EditText) textEntryView.findViewById(R.id.name);
                     	String name = editText.getText().toString();
                     	if (name.length() > 3){
                     		prefsEditor.putString(Statics.USERNAME, name);
                     		prefsEditor.commit();
                     	} else {
                     		Toast.makeText(mContext, "Name should be > 3 characters", Toast.LENGTH_SHORT).show();
                     		showUserNameDialog();
                     	}
                     }
                 })  
                 .setCancelable(false)               
                 .create();
            userNameDialog.show();
 		}
 	}
 }
