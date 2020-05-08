 package com.squattingsasquatches.lucidity;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public class CourseMenuStudent extends Activity {
 	
 	/* DBs */
 	private RemoteDBAdapter remoteDB;
 	private LocalDBAdapter localDB;
 	
 	/* UI */
 	private ProgressDialog loading;
 	private ListView coursesListView;
 	
 	/* Misc */
 	private Intent nextActivity;
 	private ArrayList<Course> userCourses;
 	private int userId;
 	private Context ctx;
 	
 	InternalReceiver getCourses;
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (remoteDB != null)
 			remoteDB.unregisterReceiver("user.courses.view");
 		if (localDB != null)
 			localDB.close();
 	}
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.generic_list);
         
        boolean updateCourses = getIntent().getBooleanExtra("com.squattingsasquatches.updateCourses", false);
         
         ctx = this;
         userCourses = new ArrayList<Course>();
         coursesListView = (ListView) findViewById(R.id.ListContainer);
         loading = new ProgressDialog(this);
         localDB = new LocalDBAdapter(this).open();
         remoteDB = new RemoteDBAdapter(this);
         userId = getIntent().getIntExtra("com.squattingsasquatches.userId", -1);
         
         // Receivers
         getCourses = new InternalReceiver(){
 			public void update( JSONArray data ){
 				CourseMenuStudent.this.updateCourses( data );
 			}
 		};
 		getCourses.addParam("user_id", userId);
 		
 		
 		remoteDB.addReceiver("user.courses.view", getCourses);
 		
         loading.setTitle("Please wait");
         loading.setMessage("Loading your saved courses... ");
         loading.setCancelable(false);
         loading.show();
         
         if (updateCourses) {
         	remoteDB.execute("user.courses.view");
         } else {
         	updateCourses(localDB.getCourses());
         }
 	}
 	
 	public void attachCourseOnClickListener() {
 		userCourses.add(new Course(-1, "Add a Course"));
 		coursesListView.setAdapter(new ListAdapter<Course>(this, userCourses));
 		coursesListView.setOnItemClickListener(listViewHandler);
 		loading.dismiss();
 	}
 	
 	public void updateCourses( JSONArray data )
 	{
 		userCourses.clear();
 		
 		int resultLength = data.length();
 		
 		for (int i = 0; i < resultLength; ++i) {
 			try {
 				JSONObject course = data.getJSONObject(i);
 				CourseMenuStudent.this.userCourses.add(new Course(course.getInt(LocalDBAdapter.KEY_ID),
 										course.getInt(LocalDBAdapter.KEY_COURSE_NUMBER),
 										course.getString(LocalDBAdapter.KEY_NAME),
 										new Date(course.getString(LocalDBAdapter.KEY_START_DATE)),
 										new Date(course.getString(LocalDBAdapter.KEY_END_DATE)),
 										new Subject(course.getInt(LocalDBAdapter.KEY_SUBJECT_ID)),
 										new University(course.getInt(LocalDBAdapter.KEY_UNI_ID))));
 			} catch (JSONException e) {
 				Log.d("getCoursesCallback", "JSON error");
 			}
 		}
 		
 		attachCourseOnClickListener();
 	}
 	private final OnItemClickListener listViewHandler = new OnItemClickListener() {
 		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 			Object o = coursesListView.getItemAtPosition(position);
 			Course course = (Course) o;
 			
 			switch (course.getId()) {
 				case -1:
 					// Add a Course
 					// Start SubjectsActivity
 					nextActivity = new Intent(ctx, SubjectsActivity.class);
 					nextActivity.putExtra("com.squattingsasquatches.userId", userId);
 					startActivity(nextActivity);
 					break;
 				default:
 					// load selected course and start Course activity
 					break;
 			}
 		}
 	};
 	
 	/* NOT NEEDED FOR THIS ACTIVITY ANYMORE BUT COULD STILL USE IT TO VALIDATE UNIVERSITIES IN SPLASH
 	class ValidateStarter implements OnFocusChangeListener {
 		
         public void onFocusChange(View v, boolean hasFocus) {
             if (!hasFocus) {
                 ((AutoCompleteTextView) v).performValidation();
             }
         }
         
     };
 	
 	class ACValidator implements AutoCompleteTextView.Validator {
 
 		public CharSequence fixText(CharSequence invalidText) {
 			return "";
 		}
 
 		public boolean isValid(CharSequence text) {
 			String courseTitle = text.toString();
 			Iterator<Course> it = availableCourses.iterator();
 			
 			// Check that the user hasn't already entered this course
 			int count = 0;
 			
 			for (int i = 0; i < 3; ++i) {
 	    		if (courseTitle.equals(acNewCourses.get(i).getText().toString()))
 	    			++count;
 	    		
 	    		if (count > 1) return false; // 1 for the text just entered
 			}
 			
 			// Check entered course against valid courses for the user's university	
 			while (it.hasNext()) {
 				if (((Course) it.next()).equals(courseTitle.toString()))
 					return true;
 			}
 			
 			return false; // Course doesn't exist
 		}
 		
 	};*/
 }
