 package com.example.calendar;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
 import android.view.View;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EventDetails extends Activity {
 	DbHandler db;
 	CalendarEvent event;
 	ArrayList<String> arrayContacts;
 	ArrayList<String> arrayFiles;
 	ArrayList<String> arrayApps;
 	ArrayList<String> arrayLinks;
 	ArrayList<String> arrayNotes;
 	
 	public void onCreate(Bundle savedInstanceState) {
 	
 		super.onCreate(savedInstanceState);
 		ExpandableListView contactList;
 		ExpandableListView fileList;
 		ExpandableListView appList;
 		ExpandableListView linkList;
 		ExpandableListView noteList;
 		db = new DbHandler(this);
 		setContentView(R.layout.activity_event_details);
 		Intent data = getIntent();
 		event = db.getEvent(data.getIntExtra("ID", -1));
 	
 		TextView name = (TextView) findViewById(R.id.name);
 		name.setText("Name: " + event.getName());
 		
 		TextView date = (TextView) findViewById(R.id.date);
 		date.setText("Date: " + event.getDate());
 	
 		TextView location = (TextView) findViewById(R.id.location);
 		location.setText("Location: " + event.getLocation());
  
         //contact list
         contactList = (ExpandableListView)findViewById(R.id.contact_list);
 		Parent contactParent = new Parent();
 		ArrayList<Parent> arrayParentsContact = new ArrayList<Parent>();
 		contactParent.setTitle("Contacts");
 		ArrayList<String> arrayContactNames = new ArrayList<String>();
		ArrayList<String> arrayContacts = db.getContacts(event.getID());
		Log.d("EVENTDETAILS", "size of contacts is " + String.valueOf(arrayContacts.size()));
 		for (int i = 0; i<arrayContacts.size(); i++)
 		{
 			String[] projection = {Phone.DISPLAY_NAME };
 			Cursor cursor = getContentResolver().query(Uri.parse(arrayContacts.get(i)),
 					projection, null, null, null);
 			cursor.moveToFirst();
 
 			int column = cursor.getColumnIndex(Phone.DISPLAY_NAME);
 			arrayContactNames.add(cursor.getString(column));
 			Toast.makeText(this, cursor.getString(column), Toast.LENGTH_LONG).show();
 			
 		}
 		contactParent.setArrayChildren(arrayContactNames);
 		arrayParentsContact.add(contactParent);
 		contactList.setAdapter(new ContactCustomAdapter(EventDetails.this,arrayParentsContact,ContactCustomAdapter.CONTACT,arrayContacts));
 
 		//file list
 		fileList = (ExpandableListView)findViewById(R.id.file_list);
 		Parent fileParent = new Parent();
 		ArrayList<Parent> arrayParentsFile = new ArrayList<Parent>();
 		fileParent.setTitle("Files");
 		arrayFiles = db.getFiles(event.getID());
 		fileParent.setArrayChildren(arrayFiles);
 		arrayParentsFile.add(fileParent);
 		fileList.setAdapter(new ContactCustomAdapter(EventDetails.this,arrayParentsFile,ContactCustomAdapter.FILE,null));
 		
 		//apps should go here
 		appList = (ExpandableListView)findViewById(R.id.app_list);
 		Parent appParent = new Parent();
 		ArrayList<Parent> arrayParentsApp = new ArrayList<Parent>();
 		appParent.setTitle("Apps");
 
 		arrayApps = db.getApps(event.getID());
 		appParent.setArrayChildren(arrayApps);
 		arrayParentsApp.add(appParent);
 		appList.setAdapter(new ContactCustomAdapter(EventDetails.this,arrayParentsApp,ContactCustomAdapter.APP,null));
 		// to run app, given the name
 		// final PackageManager pm = getPackageManager();
 		// startActivity(pm.getLaunchIntentForPackage(packageInfo.packageName)));
 
 		// links should go here
 		linkList = (ExpandableListView)findViewById(R.id.link_list);
 		Parent linkParent = new Parent();
 		ArrayList<Parent> arrayParentsLink = new ArrayList<Parent>();
 		linkParent.setTitle("Links");
 		
 		ArrayList<String> arrayLinkNames = new ArrayList<String>();
 		ArrayList<String> arrayLinkURLs = new ArrayList<String>();
 		ArrayList<String> arrayLinks = db.getLinks(event.getID());
 		for (int i = 0; i<arrayLinks.size(); i++)
 		{
 			StringTokenizer stk = new StringTokenizer(arrayLinks.get(i), "\n");
 			arrayLinkNames.add(stk.nextToken());
 			arrayLinkURLs.add(stk.nextToken());
 		}
 		
 		linkParent.setArrayChildren(arrayLinkNames);
 		arrayParentsLink.add(linkParent);
 		linkList.setAdapter(new ContactCustomAdapter(EventDetails.this,arrayParentsLink,ContactCustomAdapter.LINK,arrayLinkURLs));
 		
 		//note list
 		noteList = (ExpandableListView)findViewById(R.id.note_list);
 		Parent noteParent = new Parent();
 		ArrayList<Parent> arrayParentsNote = new ArrayList<Parent>();
 		noteParent.setTitle("Note");
 		ArrayList<String> arrayNotes = db.getNotes(event.getID());
 		noteParent.setArrayChildren(arrayNotes);
 		arrayParentsNote.add(noteParent);
 		noteList.setAdapter(new ContactCustomAdapter(EventDetails.this,arrayParentsNote,ContactCustomAdapter.NOTE,null));
 		//notes.setText("Notes: \n" + event.getNotes());
 		 
 		}
 	
 	public void removeEvent(View view) {
 		for(String link: arrayLinks)
 		{
 			db.deleteLink(event.getID(), link);
 		}
 		for(String note: arrayNotes)
 		{
 			db.deleteNote(event.getID(), note);
 		}
 		for(String file: arrayFiles)
 		{
 			db.deleteFile(event.getID(), file);
 		}
 		for(String app: arrayApps)
 		{
 			db.deleteApp(event.getID(), app);
 		}
 		db.deleteEvent(event);
 		finish();
 		Intent intent = new Intent(this, ViewEvents.class);
 		startActivity(intent);
     }
 	
 	public void editEvent(View view){
 		
 		
 		
 	}
 	
 }
