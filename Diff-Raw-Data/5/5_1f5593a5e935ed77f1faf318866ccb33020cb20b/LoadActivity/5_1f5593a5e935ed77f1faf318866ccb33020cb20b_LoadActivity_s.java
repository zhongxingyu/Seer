 package com.tracme.localize;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.tracme.R;
 import com.tracme.data.*;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
import android.widget.Button;
 import android.widget.Toast;
 
 /**
  * This activity handles loading the correct location for localization.
  * The user is prompted to select their location from a list of locations currently accounted for.
  * Once the user has selected their current location, the location is passed to the main activity,
  * which will proceed to load the location data for localizing.
  * 
  * @author Kwaku Farkye
  *
  */
 public class LoadActivity extends Activity {
 
 	public static final String LOAD_MESSAGE = "com.tracme.localize.LoadActivity.Message";
 	public static final String AP_FILE = "com.tracme.localize.LoadActivity.AP_FILE";
 	public static final String LOCALIZE_FILE = "com.tracme.localize.LoadActivity.Localize_File";
 	
 	/** Drop-down list used for selecting location/building */
 	private Spinner locationSpinner;
 	
 	/** Drop-down list used for selecting floor in the building */
 	private Spinner floorSpinner;
 	
 	/** Name of the localization file we need to load */
 	private String locFileName = null;
 	
 	/** Name of the access point file we need to load */
 	private String apFileName = null;
 	
 	/** List of buildings to load from */
 	private List<BuildingData> buildings;
 	
 	/** ID of the building that has been selected */
 	private int buildingID;
 	
 	/** ID of the floor that has been selected */
 	private int floorID;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	
 	    setContentView(R.layout.activity_load);
	    goButton = (Button)findViewById(R.id.button1);
	    
 	    locationSpinner = (Spinner)findViewById(R.id.locationSpinner);
 	    
 	    floorSpinner = (Spinner)findViewById(R.id.floorSpinner);
 	    
 	    floorSpinner.setVisibility(View.INVISIBLE);
 	    
 	    addLocationSpinnerListener();
 	    addFloorSpinnerListener();
 	    populateList();
 	}
 	
 	/**
 	 * Prepares to and loads the main activity
 	 * 
 	 * @param v The View that was clicked
 	 */
 	public void loadMainActivity(View v)
 	{
 		if ( apFileName != null && locFileName != null)
 		{
 			// Make a new intent for the Main Activity and pass it
 			// the access point file name and localization model name
 			Intent intent = new Intent(this, MainActivity.class);
 			intent.putExtra(LOCALIZE_FILE, locFileName);
 			intent.putExtra(AP_FILE, apFileName);
 			startActivity(intent);
 		}
 		else
 		{
 			Toast.makeText(this, "Please select your location", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	/**
 	 * Method for populating a list of buildings and floors for use with the spinners.
 	 * This method is for testing/prototyping purposes ONLY.
 	 */
 	public void populateList()
 	{
 		buildings = new ArrayList<BuildingData>();
 		
 		BuildingData campusCenter = new BuildingData("Campus Center");
 		FloorData floor1 = new FloorData("First Floor", "Campus Center", "cc1_76_nexus.txt", "apcc1_76_nexus.txt", 1);
 		FloorData floor1_20 = new FloorData("Floor1 20", "Campus Center", "cc1_76_cluster20.txt", "apcc1_76_cluster20.txt", 0);
 		FloorData floor1_40 = new FloorData("Floor1 40", "Campus Center", "cc1_76_cluster40.txt", "apcc1_76_cluster40.txt", 1);
 		FloorData floor1_60 = new FloorData("Floor1 60", "Campus Center", "cc1_76_cluster60.txt", "apcc1_76_cluster60.txt", 2);
 		FloorData floor1_80 = new FloorData("Floor1 80", "Campus Center", "cc1_76_cluster80.txt", "apcc1_76_cluster80.txt", 3);
 		FloorData floor1_138 = new FloorData("Floor1 138", "Campus Center", "cc1_76_nexus.txt", "apcc1_76_nexus.txt", 4);
 		campusCenter.addFloor(floor1);
 		campusCenter.addFloor(floor1_20);
 		campusCenter.addFloor(floor1_40);
 		campusCenter.addFloor(floor1_60);
 		campusCenter.addFloor(floor1_80);
 		campusCenter.addFloor(floor1_138);
 		//campusCenter.addFloor(upperlevel);
 		
 		BuildingData wheatleyHall = new BuildingData("Wheatley Hall");
 		FloorData floor1_81 = new FloorData("81 Points", "Campus Center", "cc1_81_nexus.txt", "apcc1_76_nexus.txt", 1);
 		wheatleyHall.addFloor(floor1_81);
 		
 		buildings.add(campusCenter);
 		buildings.add(wheatleyHall);
 		
 		List<String> buildingList = new ArrayList<String>();
 		for (int i = 0; i < buildings.size(); i++)
 		{
 			buildingList.add(buildings.get(i).getBuildingName());
 		}
 		
 		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, buildingList);
 		
 		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		locationSpinner.setAdapter(dataAdapter);
 	}
 	
 	/**
 	 * Add a listener to the location spinner so we can get a building selection and
 	 * display the floors in the floor spinner based on the floors registered with the selected
 	 * building
 	 * 
 	 */
 	public void addLocationSpinnerListener()
 	{
 		// Setup the location spinner listener
 		locationSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
 		{
 			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id)
 			{
 				//String buildName;
 				//buildName = parent.getItemAtPosition(pos).toString();
 				
 				buildingID = pos;
 				
 				// Populate the floors spinner with the floors for this building
 				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(LoadActivity.this,
 						android.R.layout.simple_spinner_item, buildings.get(pos).getFloorNames());
 				
 				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 				floorSpinner.setVisibility(View.VISIBLE);
 				floorSpinner.setAdapter(dataAdapter);
 				
 			}
 			
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0)
 			{
 				//floorSpinner.setVisiblity(View.INVISIBLE);
 			}
 		});	
 	}
 	
 	/**
 	 * Method to add a listener to the floor spinner. The floor spinner is
 	 * enabled once a building has been selected. Once a floor for that building is
 	 * selected the access point file and localization model file are given
 	 * and the go button can be pressed to proceed to the Main Activity
 	 */
 	public void addFloorSpinnerListener()
 	{
 		// Setup the floor spinner listener
 		floorSpinner.setOnItemSelectedListener(new OnItemSelectedListener() 
 		{
 			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id)
 			{
 				floorID = pos;
 				
 				FloorData floor = buildings.get(buildingID).
 						getFloors().get(floorID);
 				
 				locFileName = floor.getFileName();
 				apFileName = floor.getAPFileName();
 				Toast.makeText(LoadActivity.this, "LocFile: " + locFileName
 						+ " AP name: "  + apFileName, Toast.LENGTH_LONG).show();
 			}
 			
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0)
 			{
 				
 			}
 		});
 	}
 }
