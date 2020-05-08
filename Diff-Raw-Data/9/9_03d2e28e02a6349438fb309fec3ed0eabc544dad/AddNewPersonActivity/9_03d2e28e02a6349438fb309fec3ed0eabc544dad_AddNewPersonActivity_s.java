 package org.SdkYoungHeads.DoIKnowYou;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class AddNewPersonActivity extends Activity {
 
 	protected ListView groups;
 	
 	ArrayList<String> listItems=new ArrayList<String>();
 	protected ArrayAdapter<String> adapter;
 	
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.addnewperson);
 		
 		
 		/*
		 * listener pro tlatko zpt
 		 */
 		 Button next = (Button) findViewById(R.id.back);
 	        next.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View view) {
 	                Intent myIntent = new Intent(view.getContext(), ListOfGroupsActivity.class);
 	                startActivityForResult(myIntent, 0);
 	            }
 
 	        });
 	        
 		GroupContainer gc = ((Application)getApplication()).getDatabase();
 		
 		groups = (ListView) this.findViewById(R.id.list_of_groups);
 		
 		adapter=new ArrayAdapter<String>(this,
 			    android.R.layout.simple_list_item_1,
 			    listItems);
 		
 		groups.setAdapter(adapter);
 		
 		for (Group g: gc.getGroups()) {
 			listItems.add(g.getName());
 		}
 		//listItems.add("ALL");
 		//listItems.add("WORK");
 		//listItems.add("SCHOOL");
 		
 	}
 
 	
 	
 }
