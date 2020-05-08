 package com.squattingsasquatches.lucidity;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class CourseDescriptionActivity extends Activity {
 	
 	/* DBs */
 	private RemoteDBAdapter remoteDB;
 	private LocalDBAdapter localDB;
 	
 	/* UI */
 	private ProgressDialog loading;
 	
 	/* Misc */
 	private Intent nextActivity;
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
         setContentView(R.layout.course_description);
         
         /* DB */
         remoteDB = new RemoteDBAdapter(this);
         localDB = new LocalDBAdapter(this).open();
         
         /* UI */
         loading = new ProgressDialog(this);
         loading.setTitle("Please wait");
         loading.setMessage("Loading course info... ");
         loading.setCancelable(false);
         loading.show();
         
         courseId = getIntent().getIntExtra("com.squattingsasquatches.courseId", -1);
         
         InternalReceiver courseDescription = new InternalReceiver(){
 			public void update( JSONArray data ){
 				CourseDescriptionActivity.this.displayCourseDescription( data );
 			}
 		};
 		courseDescription.addParam("course_id", courseId);
 		
 		remoteDB.addReceiver("user.course.description.view", courseDescription);
         remoteDB.execute("user.course.description.view");
         
         loading.show();
 	}
 	
 	public void displayCourseDescription(JSONArray data) {
 		TextView txtCourseId = (TextView) findViewById(R.id.txtCourseId),
 				 txtCourseName = (TextView) findViewById(R.id.txtCourseName),
 				 txtCourseDescription = (TextView) findViewById(R.id.txtCourseDescription);
 		Button btnViewSections = (Button) findViewById(R.id.btnViewSections);
 		int resultLength = data.length();
 		JSONObject course;
 		
 		if (resultLength == 1) {
 			try {
 				course = data.getJSONObject(0);
 				txtCourseId.setText(course.getString("subject_prefix") + " " + course.getString("course_number"));
 				txtCourseName.setText(course.getString("course_name"));
 				txtCourseDescription.setText(course.getString("course_description"));
 			} catch (JSONException e) {
 				Log.d("displayCourseDescription", "Error parsing JSON array: " + e.getMessage());
 			}
 		} else {
 			
 		}
 		
 		btnViewSections.setOnClickListener(viewSectionsListener);
 		
 		loading.dismiss();
 		
 	}
 	
 	private OnClickListener viewSectionsListener = new OnClickListener() {
 
 		public void onClick(View v) {
 			nextActivity = new Intent(CourseDescriptionActivity.this, SectionsActivity.class);
 			nextActivity.putExtra("com.squattingsasquatches.courseId", courseId);
			nextActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(nextActivity);
 		}
 		
 	};
 	
 }
