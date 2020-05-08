 package com.metat.main;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.http.util.ByteArrayBuffer;
 
 import com.example.metat.R;
 import com.metat.dataaccess.ContactDataAccess;
 import com.metat.dataaccess.GroupsDataAccess;
 import com.metat.models.Group;
 import com.metat.models.MeetupContact;
 import com.metat.models.NavigationSource;
 import com.metat.webservices.ContactWebservices;
 import com.metat.helpers.ConnectionHelper;
 import com.metat.helpers.NoDefaultSpinner;
 import com.metat.helpers.PreferencesHelper;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class AddContactActivity extends Activity implements TextWatcher {
 	private NavigationSource _navigationSource = NavigationSource.AllContacts;
 	
 	private String _sourceGroupId = "";
 	
 	private Group[] _groups;
 	private ArrayAdapter<Group> _meetupGroupsAdapter;
 	private ArrayAdapter<MeetupContact> _meetupGroupContactsAdapter;
 	
 	private static MeetupContact[] _contacts = new MeetupContact[0];	
 	private String _userToken = "";
 	
 	private NoDefaultSpinner _meetupGroupSelect;
 	private AutoCompleteTextView _name;
 	private EditText _email;
 	private EditText _phone;
 	private EditText _notes;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.add_contact);
         
         getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar));
         getActionBar().setDisplayShowHomeEnabled(true);
         getActionBar().setDisplayShowTitleEnabled(false);
 
         Bundle extras = getIntent().getExtras();
 
         if (extras.containsKey("navigationSource"))
         	_navigationSource = (NavigationSource)extras.get("navigationSource");
 
         if (extras.containsKey("groupId"))
         	_sourceGroupId = extras.getString("groupId");
         
         _contacts = new MeetupContact[0];
 
     	GroupsDataAccess groupsDataAccess = new GroupsDataAccess(this);
     	_groups = groupsDataAccess.getAllGroups();
 
         if (_groups.length > 0)
         {
 	    	if (ConnectionHelper.isNetworkAvailable(getBaseContext()))
 	    	{
 		        SharedPreferences settings = getSharedPreferences(PreferencesHelper.MEEUP_PREFS, Context.MODE_PRIVATE);
 		
 		        if (settings.getString(PreferencesHelper.USER_TOKEN, null) != null)
 		        {
 		        	_userToken = settings.getString(PreferencesHelper.USER_TOKEN, "");
 		        }
 		        else
 		        {
 					Intent cancelIntent = new Intent(getBaseContext(), MainActivity.class);
 					startActivity(cancelIntent);	
 		        }
 		        
 		        if (_sourceGroupId.trim().length() > 0)
 		        {
 			    	GetGroupContactsTask getGroupContactsTask = new GetGroupContactsTask(this, _userToken, _sourceGroupId);
 			    	getGroupContactsTask.execute();
 		        }
 		        else
 		        {
 			    	GetGroupContactsTask getGroupContactsTask = new GetGroupContactsTask(this, _userToken, _groups[0].getMeetupId());
 			    	getGroupContactsTask.execute();	
 		        }
 	    	}
 	    	
 	    	int groupSelectinedIndex = 0;
 	    	
 	    	for (int i=0; i<_groups.length; i++)
 	    	{
 	    		if (_groups[i].getMeetupId().equals(_sourceGroupId))
 	    		{
 	    			groupSelectinedIndex=i;
 	    		}
 	    	}
 	    	
 	    	_meetupGroupsAdapter = new ArrayAdapter<Group>(this, R.layout.groups_spinner_style, _groups);
 	    	
 	    	_meetupGroupSelect = (NoDefaultSpinner) findViewById(R.id.meetup_group_select);
 	    	_meetupGroupSelect.setAdapter(_meetupGroupsAdapter);
 	    	_meetupGroupSelect.setSelection(groupSelectinedIndex);
 	    	_meetupGroupSelect.setVisibility(View.VISIBLE);
 	    	_meetupGroupSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
 				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 					_contacts = new MeetupContact[0];
 					_meetupGroupContactsAdapter = new ArrayAdapter<MeetupContact>(getBaseContext(), R.layout.contacts_spinner_style, _contacts);
 					_name.setAdapter(_meetupGroupContactsAdapter);
 					
 					GetGroupContactsTask getGroupContactsTask = new GetGroupContactsTask(getBaseContext(), _userToken, _groups[pos].getMeetupId());
 	    			getGroupContactsTask.execute();
 				}
 
 				public void onNothingSelected(AdapterView<?> parent) {
 				}
 			});
         }
 
     	_name = (AutoCompleteTextView) findViewById(R.id.name);
     	_name.addTextChangedListener(this);
     	_name.setThreshold(2);
 		_meetupGroupContactsAdapter = new ArrayAdapter<MeetupContact>(this, R.layout.contacts_spinner_style, _contacts);
 		_name.setAdapter(_meetupGroupContactsAdapter);
     	
     	_email = (EditText) findViewById(R.id.email);
     	_phone = (EditText) findViewById(R.id.phone);
     	_notes = (EditText) findViewById(R.id.notes);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.add_contact_menu, menu);
         return true;
     }
 
     @Override
     public void onBackPressed() {
 		Intent cancelIntent = new Intent(getBaseContext(), MainActivity.class);
 
 		switch (_navigationSource)
 		{
 		case AllContacts:
 			cancelIntent = new Intent(getBaseContext(), MainActivity.class);
 			cancelIntent.putExtra("selectedTab", MainActivity.TAB_CONTACTS);
 			break;
 		case AllGroups:
 			cancelIntent = new Intent(getBaseContext(), MainActivity.class);
 			cancelIntent.putExtra("selectedTab", MainActivity.TAB_MEETUPS);
 			break;
 		case GroupContacts:
 			if (_sourceGroupId.trim().length() > 0)
 			{
 				cancelIntent = new Intent(getBaseContext(), GroupActivity.class);
 				cancelIntent.putExtra("groupId", _sourceGroupId);
 			}
 			else
 			{
 				cancelIntent = new Intent(getBaseContext(), MainActivity.class);
 				cancelIntent.putExtra("selectedTab", MainActivity.TAB_MEETUPS);
 			}
 			break;
 		}
 		
 		cancelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		
 		getBaseContext().startActivity(cancelIntent);	
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
         	case R.id.cancel:
 				Intent cancelIntent = new Intent(getBaseContext(), MainActivity.class);
 
 				switch (_navigationSource)
 				{
 				case AllContacts:
 					cancelIntent = new Intent(getBaseContext(), MainActivity.class);
 					cancelIntent.putExtra("selectedTab", MainActivity.TAB_CONTACTS);
 					break;
 				case AllGroups:
 					cancelIntent = new Intent(getBaseContext(), MainActivity.class);
 					cancelIntent.putExtra("selectedTab", MainActivity.TAB_MEETUPS);
 					break;
 				case GroupContacts:
 					if (_sourceGroupId.trim().length() > 0)
 					{
 						cancelIntent = new Intent(getBaseContext(), GroupActivity.class);
 						cancelIntent.putExtra("groupId", _sourceGroupId);
 					}
 					else
 					{
 						cancelIntent = new Intent(getBaseContext(), MainActivity.class);
 						cancelIntent.putExtra("selectedTab", MainActivity.TAB_MEETUPS);
 					}
 					break;
 				}
 				
 				cancelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				
 				getBaseContext().startActivity(cancelIntent);	
         		return true;
         	case R.id.save:
         		if (_name.getText().toString().length() > 0)
         		{
 	        		String meetupId = "";
 	        		String name = _name.getText().toString();
 	        		String contactPhotoThumbnailLocation = "";
 	        		
 	        		for (int i=0; i<_contacts.length; i++)
 	        		{
 	        			if ((_contacts[i].getName().trim().toLowerCase()).equals(_name.getText().toString().trim().toLowerCase()))
 	        			{
 		        			meetupId = _contacts[i].getMeetupId();
 		        			name = _contacts[i].getName();
 		        			contactPhotoThumbnailLocation = _contacts[i].getPhotoThumbnail();
 	        				break;
 	        			}
 	        		}
 	        		
 	        		ContactDataAccess contactDataAccess = new ContactDataAccess(this);
 	        		contactDataAccess.Insert(meetupId, new byte[0], name, _email.getText().toString(), _phone.getText().toString(), _notes.getText().toString(), ((Group)_meetupGroupSelect.getSelectedItem()).getMeetupId(), ((Group)_meetupGroupSelect.getSelectedItem()).getName());
 	        		
 	        		if (contactPhotoThumbnailLocation.trim().length() > 0)
 	        		{
 		        		DownloadImageTask downloadImageTask = new DownloadImageTask(this, meetupId, contactPhotoThumbnailLocation);
 		        		downloadImageTask.execute();
 	        		}
 	        		
 					Intent returnIntent = new Intent(getBaseContext(), MainActivity.class);
 
 					switch (_navigationSource)
 					{
 					case AllContacts:
 						returnIntent = new Intent(getBaseContext(), MainActivity.class);
 						returnIntent.putExtra("selectedTab", MainActivity.TAB_CONTACTS);
 						break;
 					case AllGroups:
 						returnIntent = new Intent(getBaseContext(), ViewContactActivity.class);
 						returnIntent.putExtra("selectedTab", MainActivity.TAB_MEETUPS);
 						break;
 					case GroupContacts:
 						returnIntent = new Intent(getBaseContext(), GroupActivity.class);
 						returnIntent.putExtra("groupId", ((Group)_meetupGroupSelect.getSelectedItem()).getMeetupId());
 						break;
 					}
 					
 					returnIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					
 					getBaseContext().startActivity(returnIntent);
 					
 	        		return true;
         		}
         		else
         		{
         			Toast.makeText(this, R.string.you_need_to_provide_a_name, Toast.LENGTH_SHORT).show();
         		}
         }
         
         return false;
     }
     
 	private class GetGroupContactsTask extends AsyncTask<String, String, String>
 	{
 		private Context _context;
 		private String _meetupKey;
 		private String _groupMeetupId;
 		
 		private MeetupContact[] _retrievedContacts = new MeetupContact[0];
 		
 		public GetGroupContactsTask(Context context, String meetupKey, String groupMeetupId)
 		{
 			_context = context;
 			_meetupKey = meetupKey;
 			_groupMeetupId = groupMeetupId;
 			
 			_retrievedContacts = new MeetupContact[0];
 		}
 		
 		@Override
 		protected String doInBackground(String... strings) {
 			_retrievedContacts = ContactWebservices.getAllContacts(_meetupKey, _groupMeetupId);
 	    	
 			return "Complete";
 		}
 		
 		@Override
         protected void onPostExecute(String result) {
 			_contacts = _retrievedContacts;
 
 			_meetupGroupContactsAdapter = new ArrayAdapter<MeetupContact>(_context, R.layout.contacts_spinner_style, _contacts);
 			_name.setAdapter(_meetupGroupContactsAdapter);
 		}
 	}
 
 	private class DownloadImageTask extends AsyncTask<String, String, String>
 	{
 		private Context _context;
 		private String _meetupId;
 		private String _imageLocation;
 		
 		private byte[] _downloadedImage = new byte[0];
 		
 		public DownloadImageTask(Context context, String meetupId, String imageLocation)
 		{
 			_context = context;
 			_meetupId = meetupId;
 			_imageLocation = imageLocation;
 		}
 		
 		@Override
 		protected String doInBackground(String... strings) {
 			URL imageUrl;
 			URLConnection imageConnection;
 			InputStream inputStream;
 			
 			String[] urlParts = _imageLocation.split("/");
 			
 			if (urlParts.length == 0)
 			{
 				return "Aborted";
 			}
 			
 			if (!urlParts[urlParts.length-1].contains("."))
 			{
 				return "Aborted";
 			}
 			
 			try
 			{
 				imageUrl = new URL(_imageLocation);
 			}
 			catch (Exception ex)
 			{
 				return "Aborted";
 			}
 			
 			try
 			{
 				imageConnection = imageUrl.openConnection();
 				inputStream = imageConnection.getInputStream();
 			}
 			catch (IOException ex)
 			{
 				return "Aborted";
 			}
 			
 			BufferedInputStream bis = new BufferedInputStream(inputStream);
 			ByteArrayBuffer baf = new ByteArrayBuffer(50);
 		
 			int current = 0;
 
 			try
 			{
                 while ((current = bis.read()) != -1) 
                 {
                     baf.append((byte) current);
                 }
 			}
 			catch (IOException ex)
 			{
 				return "Aborted";
 			}
 			
 			if (baf.isEmpty())
 			{
 				return "Aborted";
 			}
 			
 			_downloadedImage = baf.toByteArray();
 			
 			try
 			{
 				bis.close();
 			}
 			catch (Exception ex)
 			{
 				bis = null;
 			}
 			
 			try
 			{
 				inputStream.close();
 			}
 			catch (Exception ex)
 			{
 				inputStream = null;
 			}
 			
 			baf.clear();
 			imageConnection = null;
 			baf = null;
 
 			return "Complete";
 		}
 		
 		@Override
         protected void onPostExecute(String result) {
 			ContactDataAccess contactDataAccess = new ContactDataAccess(_context);
 			contactDataAccess.updateImage(_meetupId, _downloadedImage);
 		}
 	}
 
 	@Override
 	public void afterTextChanged(Editable arg0) {
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 	}
 
 	@Override
 	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 	}
 }
