 package com.teamsexy.helloTabs;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class GroupsActivity extends FragmentActivity {
 	
 	/* Group database updates */
 	private FelixDbAdapter groupDbHelper;
 	private ListView groupsview;
 
	private static final String[] groupSamples={"Informatics Dept", "Jedi Knights", 
		"Dumbledore's Army"};
 	
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Initialize database helper
         groupDbHelper = new FelixDbAdapter(this);
         groupDbHelper.open();
         
         // Initialize Groups
         groupsview = new ListView(this);
         groupsview.setOnItemClickListener(new OnItemClickListener () 
         {
         	public void onItemClick(AdapterView<?> parent, View v, int position, long id)
         	{
         		launchGroupEditActivity();
         	}
         });
         
         // Fetch and display groups data
        getAllGroups();
         setContentView(groupsview);
     }
     
     /**
      * getAllSpotsData
      * 
      * Retrieve data for all spots from db adapter with a cursor. 
      */
     public void getAllGroups() {    	
     	Cursor groupsCursor = groupDbHelper.getAllGroupEntries();
     	List<String> groupNames = new ArrayList<String>();
     	groupNames.add("Create a Group");
     	
     	// If query returns group, display them in Groups Tab
     	// Might want to add ordering query so that most recent
     	// spots display first...
     	if (groupsCursor.getCount() > 0) {
     		groupsCursor.moveToFirst();
     		while (!groupsCursor.isAfterLast()) {
     			groupNames.add(groupsCursor.getString(1));
     			groupsCursor.moveToLast();
     		}
     		
     	}
     	
     	// Close cursor
     	groupsCursor.close();
     	
     	// Temporary - Ad`d Sample Groups to List
     	//for (String groupname : groupSamples) {
     		//groupNames.add(groupname);
     	//}
     	
     	groupsview.setAdapter(new ArrayAdapter<String>(this, 
 				android.R.layout.simple_list_item_1, groupNames));
     }
     
     /**
      * launchGroupEditActivity
      */
     public void launchGroupEditActivity ()
     {
     	Intent i = new Intent(this, GroupEditActivity.class);
     	startActivity(i);
     }
 }
