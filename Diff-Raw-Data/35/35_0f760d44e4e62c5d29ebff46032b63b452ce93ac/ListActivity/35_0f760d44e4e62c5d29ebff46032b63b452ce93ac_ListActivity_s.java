 package com.baggers.bagboy;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 public class ListActivity extends Activity{
	final Spinner spinner1 = (Spinner) findViewById(R.id.categorySpinner);
	final Spinner spinner2 = (Spinner) findViewById(R.id.itemSpinner);
    public void onCreate(Bundle savedInstanceState) {
     	
         super.onCreate(savedInstanceState);
         
         // set default screen to login GUI
         setContentView(R.layout.activity_list);
         
         // Sets up action bar back button
         ActionBar actionBar = this.getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         
         // Checkbox functionality
         CheckBox coldCheck = (CheckBox) findViewById(R.id.coldCheck);
         
         coldCheck.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				
 			}
         	
 		});
         
         Button routeButton = (Button) findViewById(R.id.routeButton);
         
         routeButton.setOnClickListener(new View.OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				Intent i = new Intent(ListActivity.this, RouteActivity.class);
             	startActivity(i); 
 			}
         	
         });
         
         
         //sets up all the drop downs with values that are set in the activities from home 
         //http://developer.android.com/guide/topics/ui/controls/spinner.html
         Spinner spinner = (Spinner) findViewById(R.id.storeSpinner);
         ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, ListManager.loadStores());
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
         
         
         ArrayAdapter<String> adapter1 = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, ListManager.loadCategories());
         adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner1.setAdapter(adapter1);
         ListManager.currCategory = spinner1.getSelectedItem().toString();
         
         
         spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
             	ListManager.currCategory = spinner1.getSelectedItem().toString();
             	refreshSpinner2();
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> parentView) {
                 ListManager.currCategory = "";
             }

        });
        
 
         //TODO: make spinners update content based on other spinners
         
         // Populate item list view
         final ListView itemList = (ListView) findViewById(R.id.itemList);
         ArrayAdapter<String> listAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, ListManager.loadItemsFromList());
        itemList.setAdapter(listAdapter);
       
         
         Button addButton = (Button) findViewById(R.id.addToListButton);
         addButton.setOnClickListener(new View.OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				ListManager.addToList(spinner2.getSelectedItem().toString());
 				itemList.invalidateViews();
 			}
         	
         });
     }
 
     /*
      * Sets which activity the title bar back button goes to
      */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent myIntent = new Intent(getApplicationContext(), HomeActivity.class);
 	    startActivityForResult(myIntent, 0);
 	    return true;
 	}
 	
	public void refreshSpinner2() {
        
         ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item, ListManager.loadItemsFromCategory(ListManager.currCategory));
         adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
        
	}
    
 }
