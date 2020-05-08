 package com.rdft.foreveralone.glue.debug;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 
 import com.rdft.foreveralone.R;
 import com.rdft.foreveralone.glue.CommHandler;
 import com.rdft.foreveralone.glue.FaHttpClient;
 import com.rdft.foreveralone.glue.auth.LoginTask;
 import com.rdft.foreveralone.glue.auth.LoginTask.ILoginReceiver;
 import com.rdft.foreveralone.glue.models.Course;
 import com.rdft.foreveralone.glue.models.DatastoreEntity;
 import com.rdft.foreveralone.glue.models.Schedule;
 import com.rdft.foreveralone.glue.models.Section;
 import com.rdft.foreveralone.glue.models.University;
 import com.rdft.foreveralone.glue.models.UserProfile;
 
 public class GlueLayerDebugActivity extends Activity implements ILoginReceiver {
 	String TAG = "GlueDebug";
 	CommHandler comm;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.glue_debug);
 
 		Toast t = Toast.makeText(this, "Server address is "
 				+ DebugConfig.address, Toast.LENGTH_LONG);
 		t.show();
 	}
 
 	public void showConnectionError() {
 		Toast toast = Toast.makeText(this,
 				"Unable to connect - try again later", Toast.LENGTH_SHORT);
 		toast.show();
 	}
 
 	public void onYeahButtonClick(View v) {
 		DebugConfig.logInfo(TAG, "YEAH!");
 		if (comm == null) {
 			Toast t = Toast.makeText(this, "Not yet logged in",
 					Toast.LENGTH_SHORT);
 			t.show();
 			return;
 		}
 
 		try {
 			Schedule schedule = comm.getCurrentSchedule();
 			if (schedule == null) {
 				DebugConfig.logInfo(TAG,
 						"You do not have a current schedule set");
 				Toast.makeText(this, "You do not have a current schedule set",
 						Toast.LENGTH_SHORT).show();
 				return;
 			}
 			Course[] courses = comm.getCourses();
 			if (courses.length == 0) {
 				Toast.makeText(this, "Add a course first", Toast.LENGTH_SHORT)
 						.show();
 				return;
 			}
 
 			Section[] sections = comm.getSections();
 			schedule.addClass(sections[0]);
 			comm.update(schedule);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void onChangeServerButtonClick(View v) {
 
 	}
 
 	public void onLoginButtonClick(View v) {
 		LoginTask task = new LoginTask(this);
		task.execute(null);
 	}
 
 	@Override
 	public void onLoginComplete(FaHttpClient client) {
 		if (client != null) {
 			this.comm = new CommHandler((FaHttpClient) client);
 			Toast.makeText(this, "You are now logged in", Toast.LENGTH_SHORT)
 					.show();
 		} else {
 			Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void onCurSchedButtonClick(View v) {
 		if (comm == null) {
 			Toast t = Toast.makeText(this, "Not yet logged in",
 					Toast.LENGTH_SHORT);
 			t.show();
 			return;
 		}
 
 		Schedule schedule;
 		try {
 			schedule = comm.getCurrentSchedule();
 			if (schedule != null) {
 				Toast.makeText(this,
 						"Got schedule with " + schedule.classes.size()
 								+ " classes", Toast.LENGTH_LONG);
 			} else {
 				Toast.makeText(this, "You do not have a current schedule set.",
 						Toast.LENGTH_LONG).show();
 			}
 		} catch (JSONException e) {
 			DebugConfig.logError(TAG, "Failed to parse current schedule JSON");
 			e.printStackTrace();
 		}
 	}
 
 	public void onCreateCourseButtonClick(View v) {
 		if (comm == null) {
 			Toast t = Toast.makeText(this, "Not yet logged in",
 					Toast.LENGTH_SHORT);
 			t.show();
 			return;
 		}
 
 		Course course = new Course();
 		try {
 			comm.update(course);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void onFullTestButtonClick(View v) {
 		if (comm == null) {
 			Toast t = Toast.makeText(this, "Not yet logged in",
 					Toast.LENGTH_SHORT);
 			t.show();
 			return;
 		}
 
 		University uni = new University();
 		uni.name = "Hello World University";
 		DatastoreEntity entity = uni;
		String key;
 		try {
			key = comm.update(entity);
 			DebugConfig.logInfo(TAG, "Entity key after update: " + entity.getEntityKey());
 			UserProfile profile = comm.getProfile();
 			if (profile == null) {
 				DebugConfig.logError(TAG, "Failed to retrieve profile!");
 				return;
 			}
 			profile.university = uni;
 			comm.update(profile);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 }
