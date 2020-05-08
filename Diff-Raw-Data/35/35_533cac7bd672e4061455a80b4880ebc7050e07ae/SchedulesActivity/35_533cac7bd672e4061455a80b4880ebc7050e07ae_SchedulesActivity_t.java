 package sak.todo.gui.schedules;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Random;
 
 import sak.todo.database.Task;
 import sak.todo.gui.R;
 import sak.todo.gui.agenda.TasksListActivity;
 import taskero.learner.Preference_Learner;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.googlecode.android.widgets.DateSlider.DateSlider;
 import com.googlecode.android.widgets.DateSlider.DateTimeSlider;
 import com.learner.svm.SVMAdapter;
 
 public class SchedulesActivity extends Activity implements TabListener, OnItemClickListener{
 	ArrayList<ArrayList<Task>> assignments;
 	TasksListAdapter[] adapters;
 	
 	ListView tasksList;
 	
 	DateTimeSlider dateTimeSlider; //  using single instance of dateTimeSlider instead of creating new ones.
 	Calendar tmpCalendar; // use single instance of the calendar instead of creating new ones.
 	
 	// index of the currently selected schedule
 	int currentScheduleIndex = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// requesting action bar feature
 		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
 		
 		// setup action bar for tabs
 	    ActionBar actionBar = getActionBar();
 	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 	    actionBar.setDisplayShowTitleEnabled(false);
 	    actionBar.setHomeButtonEnabled(false);
 	    actionBar.setDisplayShowTitleEnabled(false);
 	    actionBar.setDisplayShowHomeEnabled(false);
 	    actionBar.setStackedBackgroundDrawable(new ColorDrawable(R.color.tabsColor));
 	    
 	    // getting assignments to be displayed
 		Bundle b = getIntent().getExtras();
 		Object assignmentsObject;
 		if(b != null && ((assignmentsObject = b.get("assignments")) != null))
 			assignments = (ArrayList<ArrayList<Task>>) assignmentsObject;
 		
 		
		
 //		buildRedundantAssignments();
 		
 		setContentView(R.layout.schedules_layout);
 		tasksList = (ListView)findViewById(R.id.tasksList);
 		
 		if(assignments == null){
 			// something went wrong and this activity should be ended
 			Toast.makeText(this, "Assignmets are null", Toast.LENGTH_LONG).show();
 			finish();
 		}else {
 			int numOfAssignments = assignments.size();
 			adapters = new TasksListAdapter[numOfAssignments];
 			
 			if (numOfAssignments == 0) {
 				Toast.makeText(this, "No schdules are available", Toast.LENGTH_LONG).show();
 				finish();
 			}else{
 				// adding tab for each assignment
 				for (int i = 0; i < numOfAssignments; i++) {
 					Tab tab = actionBar.newTab().setText(String.format("Plan#%d", i)).setTabListener(this);
 					actionBar.addTab(tab);
 				}
 			}
 		}
 		
 		// Give a hint about how to manually assign due dates
 		Toast t = Toast.makeText(this, "Click on any task to manually assign due date.", Toast.LENGTH_LONG);
 		t.show();
 		
 		// preparing date time slider dialog
 		tmpCalendar = Calendar.getInstance();
 		dateTimeSlider = new DateTimeSlider(this, mDateTimeSetListener, tmpCalendar);
 	}
 	
 	private void loadAssignment(int assignmentIndex){
 		// lazy load for adapters and caching them
 		if(adapters[assignmentIndex] == null){
 			
 			adapters[assignmentIndex] = new TasksListAdapter(this, R.layout.list_tasks,
 					R.id.task_body, assignments.get(assignmentIndex)); 
 		}
 		
 		// updating index of the current schedule
 		currentScheduleIndex = assignmentIndex;
 		
 		// setting the new adapter
 		tasksList.setAdapter(adapters[assignmentIndex]);
 		tasksList.setOnItemClickListener(this);
 	}
 	
 	private void buildRedundantAssignments(){
 		assignments = new ArrayList<ArrayList<Task>>();
 
 		Task[] tasks = new Task[5];
 		for (int j = 0; j < 5; j++) {
 			Task t = new Task();
 			t.body = "task #" + j;
 			t.duedate =  new Date();
 			t.estimate = 1.5f;
 			t.priority = j * 2;
 			t.id = j;
 			tasks[j] = t;
 		}
 		
 		long shift = 1000 * 60 * 60 * 3;
 		int[] indexes = {0, 1, 2, 3, 4};
 		for (int i = 0; i < 4; i++) {
 			ArrayList<Task> s = new ArrayList<Task>();
 			shuffleArray(indexes);
 			
 			for (int j = 0; j < 5; j++) {
 				Task t = null;
 				try {
 					t = tasks[indexes[j]].clone();
 				} catch (CloneNotSupportedException e) {
 					e.printStackTrace();
 				}
 				
 				t.duedate = new Date(t.duedate.getTime() + (shift * j));
 				
 				s.add(t);
 			}
 			
 			assignments.add(s);
 		}
 	}
 	
 	static Random rnd = new Random();
 	// Implementing Fisher–Yates shuffle
 	static void shuffleArray(int[] ar){
 		for (int i = ar.length - 1; i >= 0; i--){
 			int index = rnd.nextInt(i + 1);
 			
 			// Simple swap
 			int a = ar[index];
 			ar[index] = ar[i];
 			ar[i] = a;
 		}
 	}
 	
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 	}
 
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		// call loadAssingment to load the assignment pointed by this tab
 		loadAssignment(tab.getPosition());
 	}
 
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
 		TasksListAdapter adapter = (TasksListAdapter) arg0.getAdapter();
 		
 		// set the task and its view that will be updated after the dialog returns
 		currentlyUpdatedTask = adapter.getItem(arg2);
 //		if (!currentlyUpdatedTask.schedulledNow) {
 //			Toast.makeText(this, "Task Already Schedulled", Toast.LENGTH_SHORT).show();
 //			return;
 //		}
 		currentlyUpdatedTextView = (TextView)view.findViewById(R.id.task_date);
 		
 		// initialize date time slider with the the time of that task
 		tmpCalendar.setTime(currentlyUpdatedTask.duedate);
 		// DEFECT: calendar is not updated :(
 		dateTimeSlider.updateCalendar(tmpCalendar);
 		
 		// display the date time slider
 		dateTimeSlider = new DateTimeSlider(this, mDateTimeSetListener, tmpCalendar);
 		dateTimeSlider.show();
 	}
 	
 	private Task currentlyUpdatedTask;
 	private TextView currentlyUpdatedTextView;
 	
 	private final DateSlider.OnDateSetListener mDateTimeSetListener = new DateSlider.OnDateSetListener() {
 		public void onDateSet(DateSlider view, Calendar selectedDate) {
 			// update the dateText view with the corresponding date
 			final int minute = (selectedDate.get(Calendar.MINUTE) / DateTimeSlider.MINUTEINTERVAL)
 					* DateTimeSlider.MINUTEINTERVAL;
 			
 			// avoid fractions of minutes and seconds
 			selectedDate.set(Calendar.MINUTE, minute);
 			selectedDate.set(Calendar.SECOND, 0);
 
 			currentlyUpdatedTask.duedate = selectedDate.getTime();
 			currentlyUpdatedTextView.setText(Task.getFormatedDate(selectedDate));
 			
 			currentlyUpdatedTextView.setTextColor(Color.RED);
 		}
 	};
 	
 	/**
 	 * This is function is called when the user hits 'Accept' button.
 	 * It updates the due date of tasks in current schedule which the user has accepted.
 	 * */
 	public void acceptSchedule(View view){
 		ArrayList<Task> schedule = assignments.get(currentScheduleIndex);
 		
 		Iterator<Task> it = schedule.iterator();
 		while (it.hasNext()) {
 			Task task = (Task) it.next();
 			// saving the task will save it due date too
 			task.save();
 		}
 		
 		//= start learning process to with current session information
 		// setting selected assignment on top of all assignments
 		ArrayList<Task> temp = assignments.get(0);
 		ArrayList<Task> temp2 = assignments.get(currentScheduleIndex);
 		assignments.set(0, temp2);
 		assignments.set(currentScheduleIndex, temp);
 		
 		try {
 			SVMAdapter svm = new SVMAdapter(this);
 			svm.init();
 			
 			Preference_Learner learner = Preference_Learner.getInstance(null, svm, incAndGetQueryID());
 			learner.learn(assignments);
 			
 			Toast.makeText(this, "Learning process DONE!", Toast.LENGTH_LONG).show();
 		} catch (Exception e) {
 			Toast.makeText(this, "Learning process Failed", Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 		}
 		
 		// end this activity and back to the main activity
 		Intent i = new Intent(this, TasksListActivity.class);
 		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // setting this flag will cause intermediate activities to finish
 		startActivity(i);
 		finish();
 	}
 	
 	private int incAndGetQueryID(){
 		SharedPreferences pref = getSharedPreferences("LEARNER", Context.MODE_PRIVATE);
 		int queryID = pref.getInt("QUERY_ID", 0);
 		pref.edit().putInt("QUERY_ID", queryID + 1).commit();
 		return queryID + 1;
 	}
 }
