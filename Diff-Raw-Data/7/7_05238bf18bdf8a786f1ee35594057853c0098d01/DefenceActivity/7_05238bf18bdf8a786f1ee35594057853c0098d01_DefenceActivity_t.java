 package uq.deco7381.runspyrun.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uq.deco7381.runspyrun.R;
 import uq.deco7381.runspyrun.model.Course;
 import uq.deco7381.runspyrun.model.Equipment;
 import uq.deco7381.runspyrun.model.ListAdapter_defence;
 import uq.deco7381.runspyrun.model.Obstacle;
 import uq.deco7381.runspyrun.model.ParseDAO;
 import android.app.Activity;
 import android.app.ListFragment;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.support.v4.widget.SlidingPaneLayout;
 import android.support.v4.widget.SlidingPaneLayout.PanelSlideListener;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.GoogleMapOptions;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.LatLng;
 import com.parse.ParseException;
 import com.parse.ParseGeoPoint;
 import com.parse.ParseObject;
 import com.parse.ParseUser;
 import com.parse.SaveCallback;
 /**
  * This is an Activity with Defense mode, it inherit from OnMyLocationChangeListener
  * which is going to track user's current location.
  *  
  * There are three state for user to get into this Activity:
  * 1. Create a new course
  * 2. Create new mission from a existing course
  * 3. Existing mission
  * 
  * Functionality:
  * 1. Display user current location
  * 2. Display zone of the course
  * 3. Display obstacle in the zone
  * 
  * @author Jafo
  *
  */
 public class DefenceActivity extends Activity implements OnMyLocationChangeListener {
 	
 	private GoogleMap map;
 	private LocationManager status;
 	private String isFrom; 				// Determine which Activity is user coming from
 	private ArrayList<Equipment> equipments;
 	private Course course;				// Course of this mode
 	private ParseDAO dao;
 	private View mContentView;	// The view contain the whole content.
 	private View mLoadingView;	// The view contain the process animation.
 	private boolean firstLocation;
 	private ListAdapter_defence mAdapter_defence;
 	private SlidingPaneLayout mPaneLayout;
 	private TextView energyTextView;
 	private TextView obstacleTextView;
 	private int maxObstacle;
 	private int currentObstacle;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_defence);
 		Intent intent = getIntent();
 		dao = new ParseDAO();
 		firstLocation = false;
 
 		/*
 		 * Get the Course's center point (where to put data stream) from intent
 		 */
 		double latitude = intent.getDoubleExtra("latitude", 0.0);
 		double longitude = intent.getDoubleExtra("longtitude", 0.0);
 		isFrom = intent.getStringExtra("isFrom");
 		int userEnergy = ParseUser.getCurrentUser().getInt("energyLevel");
 		equipments = dao.getEquipments(ParseUser.getCurrentUser());
 		
 		
 		energyTextView = (TextView)findViewById(R.id.textView4);
 		obstacleTextView = (TextView)findViewById(R.id.textView3);
 		
 		String energyString = String.valueOf(userEnergy);
 		energyTextView.setText(energyString);
 
 		
 		/*
 		 *  Map set up
 		 */
 		status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
 		if (status.isProviderEnabled(LocationManager.GPS_PROVIDER) || status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
 			/*
 			 *  Check the map is exist or not
 			 */
 			if (map == null){
 				map = ((MapFragment) getFragmentManager().findFragmentById(R.id.defence_map)).getMap();
 			}
 			setUpMap();
 		}else{
 			Toast.makeText(this, "Please open the GPS", Toast.LENGTH_LONG).show();
 			startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 		}
 		
 		/*
 		 * Set the course (make it visible on the map)
 		 */
 		if(isFrom.equals("existMission") || isFrom.equals("existCourse")){
 			this.course = dao.getCourseByLoc(latitude, longitude);
 			ArrayList<Obstacle> obstacles  = dao.getObstaclesByCourse(this.course);
 			displayObstacle(obstacles);
 			currentObstacle = obstacles.size();
 		}else{
 			this.course= new Course(latitude,longitude, ParseUser.getCurrentUser(), ParseUser.getCurrentUser().getInt("level"), null, ParseUser.getCurrentUser().getString("organization"));
 			currentObstacle = 0;
 		}
 		displayCourse(this.course);
 		maxObstacle = (int)Math.ceil((double)course.getLevel() / 10) * 4;
 		System.out.println((double)course.getLevel());
 		String obstaclesString = String.valueOf(currentObstacle) + " / " + String.valueOf(maxObstacle);
 		obstacleTextView.setText(obstaclesString);
 		
 		
 		mContentView = findViewById(R.id.content);
 		mLoadingView = findViewById(R.id.loading);
 		mLoadingView.setVisibility(View.GONE);
 		
 		mPaneLayout = (SlidingPaneLayout)findViewById(R.id.content);
 		mPaneLayout.openPane();
 		mPaneLayout.invalidate();
 		mPaneLayout.setPanelSlideListener(new PanelSlideListener() {
 			
 			@Override
 			public void onPanelSlide(View arg0, float arg1) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onPanelOpened(View arg0) {
 				// TODO Auto-generated method stub
 				mPaneLayout.invalidate();
 				arg0.invalidate();
 			}
 			
 			@Override
 			public void onPanelClosed(View arg0) {
 				// TODO Auto-generated method stub
 				mPaneLayout.invalidate();
 				arg0.invalidate();
 			}
 		});
 		/*
 		ArrayListFragment list = new ArrayListFragment();
 		getFragmentManager().beginTransaction().add(R.id.fragment1, list).commit();
 		*/
 		
 		final ListView listview = (ListView) findViewById(R.id.listView1);
 		mAdapter_defence = new ListAdapter_defence(this,equipments,map,mPaneLayout,energyTextView, obstacleTextView, maxObstacle, currentObstacle);
 		listview.setAdapter(mAdapter_defence);
 		
 	}
 
 	/**
 	 * onClick method triggered by "Store"
 	 * 1. Determine which method to execute base on user's state
 	 * 
 	 * @see newCourse()
 	 * @see existMission()
 	 * @see existCourse()
 	 * 
 	 * @param v   
 	 * 
 	 */
 	public void missionDecide(View v){
 		showLoading();
 		
 		final Intent intent = new Intent(this, DashboardActivity.class);
 		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		SaveCallback saveCallback = new SaveCallback() {
 			
 			@Override
 			public void done(ParseException e) {
 				// TODO Auto-generated method stub
 				if(e == null){
 					startActivity(intent);
 				}else{
 					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
 				}
 			}
 		};
 		
 		if(isFrom.equals("newCourse")){
 			newCourse(saveCallback);
 		}else if(isFrom.equals("existMission")){
 			existMission(saveCallback);
 		}else if(isFrom.equals("existCourse")){
 			existCourse(saveCallback);
 		}
 		
 	}
 	/**
 	 * onClick method triggered by "Cancel"
 	 * 
 	 * direct user to previos activity.
 	 * @param v
 	 */
 	public void cancel(View v){
 		finish();
 	}
 	/**
 	 * This function would be called when user come from "New mission" and "Existing mission" which the course is already developed.
 	 * 
 	 * 1. Add the obstacle into Map.
 	 * 
 	 */
 	private void displayObstacle(ArrayList<Obstacle> obstacles){
 		for(Obstacle obstacle: obstacles){
 			map.addMarker(obstacle.getMarkerOptions());
 		}
 	}
 
 	
 	/**
 	 * Set up a Google map.
 	 * Remove the button: zoom
 	 * Remove the button: find my location
 	 * Initiate the zoom of camera to 15
 	 */
 	private void setUpMap(){
 		map.setMyLocationEnabled(true);
 		map.setOnMyLocationChangeListener(this);
 		map.animateCamera(CameraUpdateFactory.zoomTo(15));
 		UiSettings uiSettings = map.getUiSettings();
 		uiSettings.setMyLocationButtonEnabled(false);
 		uiSettings.setZoomControlsEnabled(false);
		
		GoogleMapOptions mapOptions = new GoogleMapOptions();
		mapOptions.zOrderOnTop(true);
		 ((MapFragment) getFragmentManager().findFragmentById(R.id.defence_map)).newInstance(mapOptions);
		
 	}
 	
 	
 	/**
 	 * Set up a course:
 	 * 1. Display the zone
 	 * 2. Set the course object
 	 * 
 	 * @param latLng
 	 */
 	private void displayCourse(Course course){
 		map.addCircle(course.getCourseZone());
 	}
 	/**
 	 * Set up a new obstacle: Guard:
 	 * Base on your current location
 	 * 
 	 * @param v
 	 */
 	/*
 	public void setGuard(View v){
 		if(this.distanceToStream <= 400){
 			int COST = 40; //Cost 40 energy to set a guard in lv 1
 			int userLevel = ParseUser.getCurrentUser().getInt("level");
 			int userSpend = COST * userLevel;
 			if(userSpend > 1000){
 				userSpend  = 1000; // Maximum cost of a guard is 1000 energy.
 			}
 			if(userSpend < this.userEnergy){
 				ParseUser currentUser = ParseUser.getCurrentUser();
 				Guard g1 = new Guard(currentLocation.getLatitude(),currentLocation.getLongitude(),currentLocation.getAltitude(), currentUser, currentUser.getInt("level"),null);
 				map.addMarker(g1.getMarkerOptions());
 				
 				/*
 				 *  Add to the list of new obstacle
 				 */
 	/*
 				newObstaclesOnCourse.add(g1);
 				this.userEnergy -= userSpend;
 				displayEnergy();
 			}else{
 				Toast.makeText(getApplicationContext(), "You don't have enough energy.", Toast.LENGTH_LONG).show();
 			}
 		}else{
 			Toast.makeText(getApplicationContext(), "You can't create obstacle outside of zone.", Toast.LENGTH_LONG).show();
 		}
 
 	}
 	*/
 	
 	@Override
 	public void onMyLocationChange(Location lastKnownLocation) {
 		// TODO Auto-generated method stub
 		
 		/*
 		 *  Getting latitude of the current location
 		 */
         double latitude = lastKnownLocation.getLatitude();
  
         /*
          *  Getting longitude of the current location
          */
         double longitude = lastKnownLocation.getLongitude();
  
         /*
          *  Creating a LatLng object for the current location
          */
         LatLng latLng = new LatLng(latitude, longitude);
 		
         map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
         if(firstLocation == false){
         	map.animateCamera(CameraUpdateFactory.zoomTo(15));
         	firstLocation = true;
         }
         
         double distanceToStream = this.course.getParseGeoPoint().distanceInKilometersTo(new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))*1000;
         mAdapter_defence.setCurrentLocation(lastKnownLocation, distanceToStream);
 		
 		map.setOnCameraChangeListener(null);
 		
 	}
 
 	/**
 	 * This method will called by missionDecide() when user's state is "Create a new course"
 	 * 1. Save new course.
 	 * 2. Save new obstacle
 	 * 3. Save new mission
 	 * 4. Update the number of equipment user have (only available for datasourse in the moment)
 	 * 
 	 * @see missionDecide(View v)
 	 */
 	public void newCourse(SaveCallback saveCallback){
 		
 		List<ParseObject> objectList = new ArrayList<ParseObject>();
 		/*
 		 *  Transfer Course to parse object
 		 */
 		ParseObject courseParseObject = dao.createCourseParseObject(this.course);
 		objectList.add(courseParseObject);
 		
 		/*
 		 * Transfer Obstacle to parse object
 		 */
 		for(Obstacle obstacle: mAdapter_defence.getNewObstaclesOnCourse()){
 			ParseObject obstacleParseObject = dao.createObstacleParseObject(obstacle, courseParseObject);
 			objectList.add(obstacleParseObject);
 		}
 		/*
 		 *  Transfer Mission to parse object
 		 */
 		
 		//dao.insertMissionByNewCourse(ParseUser.getCurrentUser(), courseParseObject);
 		ParseObject missionParseObject = dao.createMissionParseObject(ParseUser.getCurrentUser(), courseParseObject);
 		objectList.add(missionParseObject);
 		
 		/*
 		 *  Update equipment
 		 */
 		objectList.addAll(dao.updateEquipment(this.equipments));
 		objectList.add(dao.updateDatasource(ParseUser.getCurrentUser()));
 		dao.updateEnergyByEnergy(ParseUser.getCurrentUser(), mAdapter_defence.getUserEnergy());
 		/*
 		 * save all all Course,Obstacle,Mission to server.
 		 */
 		dao.insertToServer(objectList,saveCallback);
 
 	}
 	/**
 	 * This method will called by missionDecide() when user's state is "Existing Mission"
 	 * 1. Save Obstacle
 	 * 
 	 * @see missionDecide(View v)
 	 */
 	public void existMission(SaveCallback saveCallback){
 		List<ParseObject> objectList = new ArrayList<ParseObject>();
 		/*
 		 * Transfer Obstacle to parse object
 		 */
 		for(Obstacle obstacle: mAdapter_defence.getNewObstaclesOnCourse()){
 			ParseObject obstacleParseObject = dao.createObstacleParseObject(obstacle, ParseObject.createWithoutData("Course", this.course.getObjectID()));
 			objectList.add(obstacleParseObject);
 		}
 		/*
 		 *  Update equipment
 		 */
 		objectList.addAll(dao.updateEquipment(this.equipments));
 		dao.updateEnergyByEnergy(ParseUser.getCurrentUser(), mAdapter_defence.getUserEnergy());
 		/*
 		 * save all all Course,Obstacle,Mission to server.
 		 */
 		dao.insertToServer(objectList,saveCallback);
 	}
 	
 	/**
 	 * This method will called by missionDecide() when user's state is "New Mission with existing course"
 	 * 1. Save Obstacle
 	 * 2. Save Mission
 	 * @see missionDecide(View v)
 	 */
 	public void existCourse(SaveCallback saveCallback){
 		List<ParseObject> objectList = new ArrayList<ParseObject>();
 		
 		/*
 		 * Transfer Obstacle to parse object
 		 */
 		for(Obstacle obstacle: mAdapter_defence.getNewObstaclesOnCourse()){
 			ParseObject obstacleParseObject = dao.createObstacleParseObject(obstacle, ParseObject.createWithoutData("Course", this.course.getObjectID()));
 			objectList.add(obstacleParseObject);
 		}
 		/*
 		 *  Transfer Mission to parse object
 		 */
 		
 		ParseObject missionParseObject = dao.createMissionParseObject(ParseUser.getCurrentUser(), ParseObject.createWithoutData("Course", this.course.getObjectID()));
 		objectList.add(missionParseObject);
 		/*
 		 *  Update equipment
 		 */
 		objectList.addAll(dao.updateEquipment(this.equipments));
 		dao.updateEnergyByEnergy(ParseUser.getCurrentUser(), mAdapter_defence.getUserEnergy());
 		/*
 		 * save all all Course,Obstacle,Mission to server.
 		 */
 		dao.insertToServer(objectList,saveCallback);
 	}
 
 	/**
 	 * Set up the screen in loading condition
 	 * 1. Set loading progress visible
 	 * 2. Set content invisible
 	 */
 	private void showLoading(){
 		mContentView.setVisibility(View.GONE);
 		mLoadingView.setVisibility(View.VISIBLE);
 		
 		mContentView.invalidate();
 		mLoadingView.invalidate();
 			
 	}
 
 }
