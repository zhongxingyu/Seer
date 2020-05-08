 package com.andresjruiz.gradetracker;
 
 import android.app.Activity;
 import android.app.ListActivity;
import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
 import android.view.Window;
import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
 
 
 public class ClassActivity extends ListActivity {
 	
 	private StorageInterface storage;
 	private String ClassName;
 	private String[] GradeCategories;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	
     	Bundle info = this.getIntent().getExtras();
     	
     	ClassName = info.getString("ClassName");
     	storage = (StorageInterface) info.get("StorageInterface");
     	
     	//Remove title
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         //Fill in with the layout
         setContentView(R.layout.class_overview);
         
         TextView className = (TextView)findViewById(R.id.class_overview_class_name);
         className.setText(ClassName);
         
         //Get the grade categories for this class
         GradeCategories = storage.getClassCategories(ClassName);
         
         setListAdapter(new ArrayAdapter<String>(this, R.layout.main_list_item, R.id.class_name, GradeCategories));

        //Create the listener for the selection of a class
        this.getListView().setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			        int position, long id) {
				Intent showClassInfo = new Intent();
				Toast.makeText(getApplicationContext(), ((TextView) view.findViewById(R.id.class_name)).getText(),
				          Toast.LENGTH_SHORT).show();

			}
        	
        });
     	
     }
 }
