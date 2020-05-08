 package com.example.PrayerTimes;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Vector;
 
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnCancelListener;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public final class cityManager extends Activity implements OnItemClickListener, OnItemLongClickListener, OnCancelListener {
 
 
 	private ListView cityList;
 	private ArrayAdapter<String> cities;
 	Profile newProfile = new Profile();
 	DatabaseHandler db = new DatabaseHandler(this);
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.city_listview);
 
 		// Receive the data passed from MainActivity
 		Bundle params = getIntent().getExtras();
 
 		this.setTitle("Choose a city to load from:");		
 
 		// Construct the cities list
 		Vector<String> items = new Vector<String>();
 		items.add("<Delete City>");
 
 		List<Profile> allCities = db.getAllProfiles();
 		for(int i=0;i<allCities.size();i++){
 			items.add((allCities.get(i)).cityName);
 		}
 
 		cityList = (ListView) findViewById(R.id.list);
 		cityList.setOnItemClickListener(this);
 		cityList.setOnItemLongClickListener(this);
 		cities = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items.toArray(new String[0]));
 		cityList.setAdapter(cities);
 
 		// get the mainProfile from MainActivity
 		newProfile = (Profile)params.getParcelableArrayList("profile").get(0);
 
 	}
 
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		//Get the text of the current list item
 		String text = (((TextView)(view)).getText()).toString();
 
 		// Clicked on <Delete City>
 		if(text.equals("<Delete City>")){
 			Toast.makeText(getApplicationContext(),"To delete a city press and hold on its name.",Toast.LENGTH_LONG).show();
 		}
 		// If clicked on a city name when in load
 		if(!text.equals("<Delete City")){

			Toast.makeText(getApplicationContext(),"You have deleted "+text+" from your cities list.",Toast.LENGTH_LONG).show();
 			// Uppdate the profile from database
 			newProfile = db.getProfile(text);
 
 			// Send it back to MainActivity
 			sendProfileBack(newProfile);
 		}
 
 		// Clicked on anything except <Delete City>
 		if(!text.equals("<Delete City>"))
 			finish();
 	}
 
 
 	@Override
 	public void onCancel(DialogInterface dialog) {
 		this.finish();
 	}
 
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 		String text = (((TextView)(view)).getText()).toString();
 		if(!(text.equals("<Delete City>"))){
 			if(newProfile.cityName.equals(text)){
 				Toast.makeText(getApplicationContext(),"You cannot delete current city, please select another city then delete this.",Toast.LENGTH_LONG).show();
 			}
 			else
 			{
 				db.deleteProfile(""+(((TextView)view).getText()));
 				finish();
 			}
 		}
 
 		return true;
 	}
 
 	void sendProfileBack(Profile profile){
 		// Send the profile back to MainActivity
 		Intent cityManagerIntent = getIntent();
 		cityManagerIntent.putParcelableArrayListExtra("profile", new ArrayList<Profile>(Collections.singletonList(profile)));
 		cityManagerIntent.putExtra("status", "statusOK");
 		setResult(RESULT_OK,cityManagerIntent);
 	}
 }
