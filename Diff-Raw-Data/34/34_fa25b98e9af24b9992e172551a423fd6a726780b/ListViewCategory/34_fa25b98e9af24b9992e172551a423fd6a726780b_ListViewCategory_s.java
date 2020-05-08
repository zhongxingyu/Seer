 package com.example.mobileindia;
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
import android.widget.ArrayAdapter;
 
 public class ListViewCategory extends ListActivity {
     //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    static ArrayList<ArrayList<String>> listItems=new ArrayList<ArrayList<String>>();
     
     //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
     Item_Adapter adapter0;
   
     int clickCounter=0;
 
     @SuppressLint("NewApi")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
     	Log.v("LIST", "LIST : create    APPCLASS");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_list_view_category);
 
         adapter0 = new Item_Adapter(this, R.layout.list, listItems);
         setListAdapter(adapter0);
         
         // Make sure we're running on Honeycomb or higher to use ActionBar APIs
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             // Show the Up button in the action bar.
             getActionBar().setDisplayHomeAsUpEnabled(true);
         }
     }
 
     //METHOD WHICH WILL HANDLE DYNAMIC INSERTION
     public void addItems(View v) {
     	//Log.v("LIST", "LIST : ADD    APPCLASS");
     	ArrayList<String> temp = new ArrayList<String>();
         temp.add("Test "+clickCounter++);
         temp.add("This is a test post");
         temp.add("Jeff Walsh");
         //listItems.add(temp);
        
         Intent intent = new Intent(this, Add_Post.class);
 		startActivity(intent);
         adapter0.notifyDataSetChanged();
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             NavUtils.navigateUpFromSameTask(this);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
 	public void onPause(){
 		super.onPause();
 		Log.v("LIST", "PAUSED LIST APPCLASS");
 
 	}
 	
 	@Override
 	public void onResume(){
 		Log.v("List", "RESUMED List APPCLASS");
 		super.onResume();
 		//adapter0.notifyDataSetChanged();
 		
 	}
 	
 	@Override
 	public void onStop(){
 		super.onStop();
 		Log.v("LIST", "Stopped LIST APPCLASS");
 	}
 
 
 
 
 
 
 
 
 }
 
