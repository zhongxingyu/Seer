 package com.squattingsasquatches.lucidity;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SectionsActivity extends Activity {
 
 	/* DBs */
 	private RemoteDBAdapter remoteDB;
 	private LocalDBAdapter localDB;
 	
 	/* UI */
 	private ProgressDialog loading;
 	private ListView sectionsListView;
 	
 	/* Misc */
 	private Intent nextActivity;
 	private ArrayList<Section> courseSections;
 	private int courseId;
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (remoteDB != null)
 			remoteDB.unregisterAllReceivers();
 		if (localDB != null)
 			localDB.close();
 	}
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.generic_list);
 		
 		/* DB */
 		remoteDB = new RemoteDBAdapter(this);
 		localDB = new LocalDBAdapter(this).open();
 		
 		/* UI */
 		loading = new ProgressDialog(this);
 		loading.setTitle("Please wait");
         loading.setMessage("Loading available subjects... ");
         loading.setCancelable(false);
         loading.show();
         
         TextView txtHeading = (TextView) findViewById(R.id.txtHeading);
         txtHeading.setText("Choose a Section");
         
         /* Misc */
 		courseSections = new ArrayList<Section>();
 		sectionsListView = (ListView) findViewById(R.id.ListContainer);
 		courseId = getIntent().getIntExtra("com.squattingsasquatches.courseId", -1);
         
         InternalReceiver courseSectionsView = new InternalReceiver(){
 			public void update( JSONArray data ){
 				SectionsActivity.this.loadSectionsCallback( data );
 			}
 		};
 		courseSectionsView.addParam("course_id", courseId);
         
         remoteDB.addReceiver("course.sections.view", courseSectionsView);
         remoteDB.execute("course.sections.view");
 	}
 	
 	public void loadSectionsCallback(JSONArray data) {
 		courseSections.clear();
 		
 		int resultLength = data.length();
 		
 		for (int i = 0; i < resultLength; ++i) {
 			try {
 				JSONObject section = data.getJSONObject(i);
 				String startTimePieces[] = section.getString("start_time").split(":"),
 						endTimePieces[] = section.getString("end_time").split(":");
 				Time startTime = new Time(),
 						endTime = new Time();
 				
 				startTime.set(Integer.valueOf(startTimePieces[2]), Integer.valueOf(startTimePieces[1]), Integer.valueOf(startTimePieces[0]), 0, 0, 0);
 				endTime.set(Integer.valueOf(endTimePieces[2]), Integer.valueOf(endTimePieces[1]), Integer.valueOf(endTimePieces[0]), 0, 0, 0);
 				
 				courseSections.add(new Section(
 										section.getInt("id"),
 										section.getString("section_name"),
 										new Course(section.getInt("course_id"),
 												   section.getInt("course_number"),
 												   new Subject(section.getString("subject_prefix"))),
 										new User(section.getString("professor_name")),
 										section.getString("location"),
 										section.getString("days"),
 										startTime,
 										endTime
 										));
 			} catch (JSONException e) {
 				Log.d("getCoursesCallback", "JSON error");
 			}
 		}
 		
 		sectionsListView.setAdapter(new ExtendedListAdapter<Section>(this, courseSections));
 		sectionsListView.setOnItemClickListener(listViewHandler);
 		loading.dismiss();
 	}
 	
 	public void registerSectionCallback(JSONArray data) {
 		Toast.makeText(getApplicationContext(), "registered for section", Toast.LENGTH_LONG).show();
 	}
 	
 	private final OnItemClickListener listViewHandler = new OnItemClickListener() {
 		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 			Object o = sectionsListView.getItemAtPosition(position);
 			Section section = (Section) o;
 			
 			//register user with section, return to CourseMenuActivity
 			InternalReceiver sectionRegister = new InternalReceiver(){
 				public void update( JSONArray data ){
 					SectionsActivity.this.registerSectionCallback( data );
 				}
 			};
 			sectionRegister.addParam("section_id", section.getId());
 			sectionRegister.addParam("user_id", localDB.getUserId());
 	        
	        remoteDB.addReceiver("user.section.register", sectionRegister);
	        remoteDB.execute("user.section.register");
 			
			nextActivity = new Intent(SectionsActivity.this, CourseMenuStudent.class);
			nextActivity.putExtra("com.squattingsasquatches.userId", localDB.getUserId());
			nextActivity.putExtra("com.squattingsasquatches.updateCourses", true);
			startActivity(nextActivity);
			finish();
 		}
 	};
 
 }
