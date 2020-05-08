 package com.andresjruiz.gradetracker;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 
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
     	
     }
 }
