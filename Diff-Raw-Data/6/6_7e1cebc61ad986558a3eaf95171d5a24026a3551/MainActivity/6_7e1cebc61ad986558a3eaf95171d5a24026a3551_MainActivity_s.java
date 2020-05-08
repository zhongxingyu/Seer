 package edu.cmu.paac.activity;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import edu.cmu.paac.R;
 import edu.cmu.paac.activity.SearchResultActivity.ListItemClicked;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class MainActivity extends Activity implements OnClickListener {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 
 //		startActivity(new Intent(this, PrivacyActivity.class));
 		Intent intent = new Intent(this, PrivacyActivity.class);
 		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(intent);
 //		finish();
 		
 		ScrollView view = (ScrollView)findViewById(R.id.scrollView);
 		view.setVerticalScrollBarEnabled(false);
 		view.setHorizontalScrollBarEnabled(false);
 		
 		
 		ListItemClicked listItemClicked;
 		listItemClicked = new ListItemClicked();
 		
 		ArrayAdapter<String> listAdapter;
 		// Create and populate a List of planet names.  
 	    String[] planets = new String[] { "Forbes Ave at Hamburg Bldg", 
 	    								"Craig St at Forbes" };    
 	    ArrayList<String> planetList = new ArrayList<String>();  
 	    planetList.addAll( Arrays.asList(planets) );  
 	      
 	    // Create ArrayAdapter using the planet list.  
 	    listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, planetList);  
 	      
 	    // Add more planets. If you passed a String[] instead of a List<String>   
 	    // into the ArrayAdapter constructor, you must not add more items.   
 	    // Otherwise an exception will occur.  
 	    listAdapter.add( "Forbes Ave at Morewood Ave FS (Carnegie Mellon)" );  
 	      
 		
 		ListView list =(ListView)findViewById(R.id.search_result_list);
 		list.setAdapter(listAdapter);
 		list.setOnItemClickListener(listItemClicked);
 		
 		findViewById(R.id.login_button).setOnClickListener(this);
 		findViewById(R.id.nav_nearby).setOnClickListener(this);
 		findViewById(R.id.nav_destination).setOnClickListener(this);
 		findViewById(R.id.nav_bus_number).setOnClickListener(this);
 		findViewById(R.id.nav_favorite).setOnClickListener(this);
 		
 		
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 
 		switch (v.getId()) {
 		case R.id.login_button:
 			startActivity(new Intent(this, LoginActivity.class));
 //			finish(); 
 			break;
 			
 		case R.id.nav_nearby:
			startActivity(new Intent(this, MainActivity.class));
			
			finish();
 			break;
 			
 		case R.id.nav_destination:
 			startActivity(new Intent(this, HardChoiceActivity.class));
 			
 //			finish();
 			break;
 			
 		case R.id.nav_bus_number:
 			startActivity(new Intent(this, BusSearchActivity.class));
 			
 //			finish();
 			break;
 			
 		case R.id.nav_favorite:
 			startActivity(new Intent(this, MyFavoriteActivity.class));
 			
 //			finish();
 			break;
 			
 		default:
 			break;
 		}
 	}
 	
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	
 	class ListItemClicked implements OnItemClickListener
     {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             // TODO Auto-generated method stub
         	
         	startActivity(new Intent(MainActivity.this, RouteDetailsActivity.class));
         	
         }       
     }
 	
 	
 }
