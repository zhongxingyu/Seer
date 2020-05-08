 package se.mah.kd330a.project;
 
 import java.io.FileOutputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 import se.mah.kd330a.project.adladok.model.Course;
 import se.mah.kd330a.project.adladok.model.Me;
 import se.mah.kd330a.project.framework.MainActivity;
 import se.mah.kd330a.project.framework.SplashActivity;
 import se.mah.kd330a.project.home.data.DOMParser;
 import se.mah.kd330a.project.home.data.RSSFeed;
 import se.mah.kd330a.project.schedule.data.KronoxCalendar;
 import se.mah.kd330a.project.schedule.data.KronoxCourse;
 import se.mah.kd330a.project.schedule.data.KronoxJSON;
 import se.mah.kd330a.project.schedule.data.KronoxReader;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Toast;
 import android.widget.EditText;
 
 public class StartActivity extends Activity implements Observer
 {
 	private final String TAG = "StartActivity";
 	private final String USER_FILE = "shared.preferences";
 	private final String RSSNEWSFEEDURL = "http://www.mah.se/Nyheter/RSS/News/";
 
 	private SharedPreferences sharedPref;
 	private String username;
 	private String password;
 	private EditText editTextUsername;
 	private EditText editTextPassword;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_start);
 
 		((View) findViewById(R.id.progressBar1)).setVisibility(View.GONE);
 		
 		Me.observable.addObserver(this);
 
 		sharedPref = getSharedPreferences(USER_FILE, Context.MODE_PRIVATE);
 		username = sharedPref.getString("user_id", "");
 		password = sharedPref.getString("user_password", "");
 		editTextUsername = (EditText) findViewById(R.id.editText1);
 		editTextPassword = (EditText) findViewById(R.id.editText2);
 		editTextUsername.setText(username);
 		editTextPassword.setText(password);
 	}
 
 	public void forgetButtonClicked(View v)
 	{
 		SharedPreferences.Editor editor = sharedPref.edit();
 		editor.putString("user_id", "");
 		editor.putString("user_password", "");
 		editor.commit();
 		Toast.makeText(this, "You've been forgotten.", Toast.LENGTH_SHORT).show();
 		finish();
 	}
 
 	public void loginButtonClicked(View v)
 	{
 		username = editTextUsername.getText().toString();
 		password = editTextPassword.getText().toString();
 
 		SharedPreferences.Editor editor = sharedPref.edit();
 		editor.putString("user_id", username);
 		editor.putString("user_password", password);
 		editor.commit();
 
 		/* 
 		 * Reset the Me "object"
 		 */
 		Me.setDispayName("");
 		Me.setEmail("");
 		Me.setFirstName("");
 		Me.setIsStudent(false);
 		Me.setIsStaff(false);
 		Me.setLastName("");
 		Me.clearCourses();
 		Me.setUserID(username);
 		Me.setPassword(password);
 		Me.updateMe();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.start, menu);
 		return true;
 	}
 
 	/*
 	 * Called by "Me" after login button is clicked 
 	 */
 	@Override
 	public void update(Observable observable, Object data)
 	{
 		if (Me.getFirstName().isEmpty())
 		{
 			Toast.makeText(this, "Can't log you in", Toast.LENGTH_LONG).show();
 			return;
 		}
 		Log.i(TAG, "update(): Got callback from Me");
 		
 		//Me.observable.deleteObserver(this);
 
 		BackgroundDownloadTask downloads = new BackgroundDownloadTask(this);
 		downloads.execute();
 	}
 
 	/**
 	 * When all tasks have completed we can go on to the next activity
 	 */
 	public void tasksCompleted()
 	{
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 		finish();
 	}
 
 	private class BackgroundDownloadTask extends AsyncTask<Void, Void, Void>
 	{
 		private StartActivity appContext;
 
 		public BackgroundDownloadTask(StartActivity activity)
 		{
 			appContext = activity;
 		}
 
 		@Override
 		protected Void doInBackground(Void... arg0)
 		{
			((View) findViewById(R.id.progressBar1)).setVisibility(View.VISIBLE);
 			
 			try
 			{
 				/*
 				 * Save a rss feed for god knows what reason
 				 */
 				DOMParser myParser = new DOMParser();
 				RSSFeed feed = myParser.parseXml(RSSNEWSFEEDURL);
 				FileOutputStream fout = openFileOutput("filename", Context.MODE_PRIVATE);
 				ObjectOutputStream out = new ObjectOutputStream(fout);
 				out.writeObject(feed);
 				out.close();
 				fout.close();
 			}
 			catch (Exception e)
 			{
 				Log.e(TAG, e.toString());
 			}
 
 			if (!Me.getCourses().isEmpty())
 			{
 				ArrayList<KronoxCourse> courses = new ArrayList<KronoxCourse>();
 
 				for (Course c : Me.getCourses())
 				{
 					String courseId = c.getKronoxCalendarCode().substring(2);
 					courses.add(new KronoxCourse(courseId));
 				}
 
 				KronoxCourse[] courses_array = new KronoxCourse[courses.size()];
 				courses.toArray(courses_array);
 
 				try
 				{
 					KronoxCourse course = KronoxJSON.getCourse(courses_array[0].getFullCode());
 					if (course != null)
 					{
 						SharedPreferences sp = getSharedPreferences("courseName", Context.MODE_PRIVATE);
 						SharedPreferences.Editor editor = sp.edit();
 						editor.putString(course.getFullCode(), course.getName());
 						editor.commit();
 						Log.i(TAG, String.format("Course: %s, %s", course.getFullCode(), course.getName()));
 						
 						try
 						{
 							Log.i(TAG, "Kronox: Creating calendar");
 							KronoxCalendar.createCalendar(KronoxReader.getFile(getApplicationContext()));
 						}
 						catch (Exception e)
 						{
 							Log.i(TAG, "Kronox: Downloading schedule, then creating calendar");
 							try
 							{
 								KronoxReader.update(getApplicationContext(), courses_array);
 								KronoxCalendar.createCalendar(KronoxReader.getFile(getApplicationContext()));
 							}
 							catch (Exception f)
 							{
 								Log.e(TAG, f.toString());
 							}
 						}
 					}
 				}
 				catch (Exception e)
 				{
 					Log.e(TAG, e.toString());
 				}
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void v)
 		{
			((View) findViewById(R.id.progressBar1)).setVisibility(View.GONE);
 			appContext.tasksCompleted();
 		}
 
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		super.onDestroy();
 		Log.i(TAG, "finish(): destroying now");
 
 	}
 }
