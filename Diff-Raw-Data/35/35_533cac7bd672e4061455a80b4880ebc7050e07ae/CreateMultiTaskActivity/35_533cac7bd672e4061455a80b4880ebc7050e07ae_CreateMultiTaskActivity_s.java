 package sak.todo.gui;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedList;
 
 import sak.todo.database.Task;
 import sak.todo.database.TasksIterator;
 import sak.todo.gui.schedules.SchedulesActivity;
 import taskero.learner.Preference_Learner;
 import GA.ScheduleTasks;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.FragmentTransaction;
 import android.content.ClipData;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Point;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.DragEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.DragShadowBuilder;
 import android.view.View.OnClickListener;
 import android.view.View.OnDragListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.view.Window;
 import android.view.animation.AnimationSet;
 import android.view.animation.LinearInterpolator;
 import android.view.animation.TranslateAnimation;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.googlecode.android.widgets.DateSlider.DateSlider;
 import com.googlecode.android.widgets.DateSlider.DateTimeSlider;
 import com.learner.svm.SVMAdapter;
 
 import cr.Constraint;
 import cr.Interval;
 import cr.Reasoner;
 import cr.STPNotConnsistentException;
 
 public class CreateMultiTaskActivity extends Activity implements TabListener {
 	int i = 1;
 	LinearLayout listofAddedTasks;
 	LinearLayout listofConstraintBefore;
 	LinearLayout listofConstraintAfter;
 	LinkedList<View> checkConstraints;
 	ArrayList<Task> addedTasks;
 	ArrayList<PointConstraint> Constraints;
 	boolean listofAddedTaskAppears;
 	public static final String PREFS_NAME = "MyPrefsFile";
 	protected static final boolean GA_ENABLED = true;
 	// ArrayList<Constraint> constraints;
 	public SharedPreferences sharedPrefs;
 	private int screenWidth;
 	private int screenHeight;
 	static int priority;
 	class PointConstraint{
 		Task t1;
 		Task t2;
 		public PointConstraint(Task t1,Task t2) {
 			this.t1=t1;
 			this.t2=t2;
 		}
 	}
 	@Override
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		if(listofAddedTaskAppears){
 			
 			animateBack();
 			
 		}
 		else super.onBackPressed();
 	}
 	boolean before=false;
 	private void animateBefore(boolean before){
 		this.before=before;
 		Log.d("debug", "anim");
 		TranslateAnimation anim=new TranslateAnimation(100,0,0,0);
 		
 		anim.setDuration(500);
 		AnimationSet logoAnimSet = new AnimationSet(true);
 		logoAnimSet.setInterpolator(new LinearInterpolator());
 		logoAnimSet.addAnimation(anim);
 		logoAnimSet.setFillAfter(true);
 		findViewById(R.id.ScrollViewParent).setAlpha(0.4f);
 		estimateView.setEnabled(false);
 		prioritylow.setClickable(false);
 		prioritymedium.setClickable(false);
 		priorityhigh.setClickable(false);
 		bodyView.setEnabled(false);
 		deadLineView.setClickable(false);
 		dueDateView.setClickable(false);
 		CreateMultiTaskActivity.this.findViewById(R.id.SaveAll).setVisibility(View.GONE);
 		CreateMultiTaskActivity.this.findViewById(R.id.AddTask).setVisibility(View.GONE);
 		CreateMultiTaskActivity.this.findViewById(R.id.AddTask).setEnabled(false);
 		
 		findViewById(R.id.ScrollViewListofAddedTasks).startAnimation(logoAnimSet);
 		findViewById(R.id.ScrollViewListofAddedTasks).setVisibility(View.VISIBLE);
 		findViewById(R.id.ScrollViewListofAddedTasks).setClickable(true);
 		
 		listofAddedTaskAppears=true;
 	}
 	private void animateBack() {
 		// TODO Auto-generated method stub
 		Log.d("debug", "here animate back");
 		TranslateAnimation anim=new TranslateAnimation(0,200,0,0);
 		
 		anim.setDuration(500);
 		AnimationSet logoAnimSet = new AnimationSet(true);
 		logoAnimSet.setInterpolator(new LinearInterpolator());
 		logoAnimSet.addAnimation(anim);
 		logoAnimSet.setFillAfter(true);
 		findViewById(R.id.ScrollViewParent).setAlpha(1f);
 		bodyView.setEnabled(true);
 		
 		estimateView.setEnabled(true);
 		
 		priorityhigh.setClickable(true);
 		prioritylow.setClickable(true);
 		prioritymedium.setClickable(true);
 		deadLineView.setClickable(true);
 		dueDateView.setClickable(true);
 		CreateMultiTaskActivity.this.findViewById(R.id.SaveAll).setVisibility(View.VISIBLE);
 		CreateMultiTaskActivity.this.findViewById(R.id.AddTask).setVisibility(View.VISIBLE);
 		CreateMultiTaskActivity.this.findViewById(R.id.AddTask).setEnabled(true);
 		findViewById(R.id.ScrollViewListofAddedTasks).setVisibility(View.GONE);
 		findViewById(R.id.ScrollViewListofAddedTasks).setClickable(false);
 		CreateMultiTaskActivity.this.findViewById(R.id.SaveAll).bringToFront();
 		
 		
 		findViewById(R.id.ScrollViewListofAddedTasks).startAnimation(logoAnimSet);
 		View v = findViewById(R.id.ScrollViewListofAddedTasks);
 //		((RelativeLayout)findViewById(R.id.RelativeLayout1)).removeView(v);
 //		((RelativeLayout)findViewById(R.id.RelativeLayout1)).addView(v);
 		listofAddedTaskAppears = false;
 		
 	}
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
 		screenWidth = metrics.widthPixels;
 		screenHeight = metrics.heightPixels;
 		
 		// requesting action bar feature
 		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
 		
 		setContentView(R.layout.create_multi_task);
 		// setup action bar for tabs
 	    ActionBar actionBar = getActionBar();
 	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 	    actionBar.setDisplayShowTitleEnabled(false);
 	    actionBar.setHomeButtonEnabled(false);
 	    actionBar.setDisplayShowTitleEnabled(false);
 	    actionBar.setDisplayShowHomeEnabled(false);
 	    actionBar.setStackedBackgroundDrawable(new ColorDrawable(R.color.tabsColor));
 		
 		
 		Tab tab1= actionBar.newTab().setText("Quick Add").setTabListener(this);
 		Tab tab2= actionBar.newTab().setText("Add Task").setTabListener(this);
 		Tab tab3= actionBar.newTab().setText("Add Meeting").setTabListener(this);
 		actionBar.addTab(tab1);
 		actionBar.addTab(tab2);
 		actionBar.addTab(tab3);
 		
 		
 		
 		bodyView = (EditText) this.findViewById(R.id.RelativeLayout1).findViewById(R.id.ScrollViewParent).findViewById(R.id.LinearLayoutTaskcrud).findViewById(R.id.taskBody);
 		priorityhigh = (Button) this.findViewById(R.id.ScrollViewParent).findViewById(R.id.rate_high);
 		prioritylow = (Button) this.findViewById(R.id.ScrollViewParent).findViewById(R.id.rate_low);
 		prioritymedium = (Button) this.findViewById(R.id.ScrollViewParent).findViewById(R.id.rate_medium);
 		estimateView = (EditText) this.findViewById(R.id.ScrollViewParent).findViewById(R.id.editText1);
 		deadLineView = (Button) this.findViewById(R.id.ScrollViewParent).findViewById(R.id.deadline);
 		dueDateView = (Button) this.findViewById(R.id.ScrollViewParent).findViewById(R.id.duedate);
 
 		sharedPrefs = getSharedPreferences(PREFS_NAME, 0);
 		Constraints=new ArrayList<PointConstraint>();
 		checkConstraints=new LinkedList<View>();
 		if (!sharedPrefs.contains("numberOfRuns"))
 			sharedPrefs.edit().putInt("numberOfRuns", 0).commit();
 
 		listofAddedTasks = ((LinearLayout)findViewById(R.id.RelativeLayout1).findViewById(R.id.ScrollViewListofAddedTasks).findViewById(R.id.ListofAddedTasks));
 		listofConstraintBefore = ((LinearLayout) findViewById(R.id.ConstraintLayoutBefore));
 		listofConstraintAfter = ((LinearLayout) findViewById(R.id.ConstraintLayoutAfter));
 		
 		priority=0;
 		priorityhigh.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				priority=3;
 				((TextView)findViewById(R.id.Rate_Statue)).setText("Urgent!");
 			}
 		});
 		prioritylow.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				priority=1;
 				((TextView)findViewById(R.id.Rate_Statue)).setText("low");
 			}
 		});
 		prioritymedium.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				priority=2;
 				((TextView)findViewById(R.id.Rate_Statue)).setText("medium");
 			}
 		});
 		
 		findViewById(R.id.ScrollViewListofAddedTasks).setOnTouchListener(new OnTouchListener() {
 			
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				Log.d("debug", "he is aliveeeeee");
 				return false;
 			}
 		});
 		findViewById(R.id.ScrollViewListofAddedTasks).setVisibility(View.GONE);
 		findViewById(R.id.ScrollViewListofAddedTasks).setClickable(false);
 		findViewById(R.id.ScrollViewParent).setOnTouchListener(new OnTouchListener() {
 			
 			public boolean onTouch(View v, MotionEvent event) {
 				// TODO Auto-generated method stub
 				
 				
 				return false;
 			}
 		});
 		findViewById(R.id.ConstraintLayoutAfter).findViewById(R.id.ButtonAddTaskAfter).setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				animateBefore(false);
 			}
 
 		});
 		findViewById(R.id.ConstraintLayoutBefore).findViewById(R.id.ButtonAddTaskBefore).setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				animateBefore(true);
 			}
 		});
 		
 
 //		findViewById(R.id.ScrollViewParent).setOnTouchListener(new OnTouchListener() {
 //			
 //			public boolean onTouch(View v, MotionEvent event) {
 //				// TODO Auto-generated method stub
 //				if(listofAddedTaskAppears)listofAddedTasks.performClick();
 //				return false;
 //			}
 //		
 //		});
 //		
 //		listofAddedTasks.setOnClickListener(new OnClickListener() {
 //			
 //			public void onClick(View v) {
 //				// TODO Auto-generated method stub
 //				Log.d("debug", "listof added");
 //				v.performClick();
 //			}
 //		});
 		
 		
 		final View activityRootView = findViewById(R.id.RelativeLayout1);
 		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
 		    public void onGlobalLayout() {
 		        int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
 		        if (heightDiff > 150) { // if more than 100 pixels, its probably a keyboard...
 				if(CreateMultiTaskActivity.this.findViewById(R.id.SaveAll)==null)return;
 		        	CreateMultiTaskActivity.this.findViewById(R.id.SaveAll).setVisibility(View.INVISIBLE);
 					CreateMultiTaskActivity.this.findViewById(R.id.AddTask).setVisibility(View.INVISIBLE);
 		        }
 		        else
 		        {
 				if(CreateMultiTaskActivity.this.findViewById(R.id.SaveAll)==null)return;
 		        	CreateMultiTaskActivity.this.findViewById(R.id.SaveAll).setVisibility(View.VISIBLE);
 					CreateMultiTaskActivity.this.findViewById(R.id.AddTask).setVisibility(View.VISIBLE);
 		        }
 		     }
 		});
 		
 
 		addedTasks = new ArrayList<Task>();
 		addedTasks.add(Task.NULLTASK);
 		parentTask = new Task();
 		
 		findViewById(R.id.SaveAll).setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Log.d("debug", "Save alll ........... ");
 
 				ArrayList<ArrayList<Task>> assignments = null;
 				// GA 
 				if(GA_ENABLED){
 					Task[] tasks=new Task[addedTasks.size()-1];
 					for (int i = 1; i < addedTasks.size(); i++) {
 						tasks[i-1]=addedTasks.get(i);
 					}
 					Point[] constraints=new Point[Constraints.size()];
 					ArrayList<Point> points=new ArrayList<Point>();
 					for (int i = 0; i < Constraints.size(); i++) {
 						Task t1=Constraints.get(i).t1;
 						Task t2=Constraints.get(i).t2;
 						int index1=-1,index2=-1;
 						Point p=new Point(-1,-1);
 						for (int j = 0; j < tasks.length; j++) {
 							if(t1.body.equals(tasks[j].body)){
 								index1=j;
 								p.x=j;
 								if(p.y!=-1){
 									points.add(p);
 									break;
 								}
 							}
 							if(t2.body.equals(tasks[j].body)){
 								index2=j;
 								p.y=j;
 								if(p.x!=-1){
 									points.add(p);
 									break;
 								}
 								
 								
 							}
 						}
 						constraints[i]=new Point(index1, index2);
 					}
 					ScheduleTasks s=new ScheduleTasks(tasks, constraints);
 					assignments=s.getAssignments();
 					
 					ArrayList<ArrayList<Task>> deletedAssignments=new ArrayList<ArrayList<Task>>();
 					
 					for (int i = 0; i < points.size(); i++) {
 						Point p=points.get(i);
 						for (int j = 0; j < assignments.size(); j++) {
 							
 						
 							ArrayList<Task >assign1=assignments.get(j);
 							if(assign1.get(p.x).duedate.getTime()>assign1.get(p.y).duedate.getTime()){
 								deletedAssignments.add(assign1);
 							}
 						}
 					}
 					for (int i = 0; i < assignments.size(); i++) {
 						
 						for (int j = i+1; j < assignments.size(); j++) {
 							ArrayList<Task >assign1=assignments.get(i);
 							ArrayList<Task >assign2=assignments.get(j);
 							
 							boolean deleted=true;
 							for (int k = 0; k < assign1.size(); k++) {
 								
 								
 								
 								long min=(long) ((double)((assign1.get(k).duedate.getTime()/15)+1)*15);
 								assign1.get(k).duedate.setTime(min);
 								
 								min=(long) ((double)((assign2.get(k).duedate.getTime()/15)+1)*15);
 								assign2.get(k).duedate.setTime(min);
 								
 								
 								if(Math.abs(assign1.get(k).duedate.getTime()-assign2.get(k).duedate.getTime())>0.15*60*60){// half an hour
 									deleted=false;
 									break;
 								}
 							}
 							if(deleted){
 								deletedAssignments.add(assign2);
 							}
 						}
 					}
 					for (int i = 0; i < deletedAssignments.size(); i++) {
 						assignments.remove(deletedAssignments.get(i));
 					}
 					for (int i = 0; i < assignments.size(); i++) {
 						for (int j = 0; j < assignments.get(i).size(); j++) {
 							assignments.get(i).get(j).schedulledNow=true;
 						}
 					}
 					TasksIterator itr = Task.getScheduledTasks(new Date(System.currentTimeMillis()), new Date(Reasoner.INFINITY));
 					Task task = itr.nextTask();
 					ArrayList<Task> preset = new ArrayList<Task>();
 					while (task != null) {
 						preset.add(task);
 	 					task = itr.nextTask();
 					}
 					for (int i = 0; i < assignments.size(); i++) {
 						for (int p = 0; p < preset.size(); p++) {
 							assignments.get(i).add(preset.get(p));
 						}
 						Collections.sort(assignments.get(i));
 					}
 					
 				}
 				else{
 				// Sherif -> Reasoner
 					TasksIterator itr = Task.getScheduledTasks(new Date(System.currentTimeMillis()), new Date(Reasoner.INFINITY));
 					Task task = itr.nextTask();
 					ArrayList<Task> preset = new ArrayList<Task>();
 					while (task != null) {
 						preset.add(task);
 	 					task = itr.nextTask();
 					}
 					Reasoner reasoner = Reasoner.instance();
 					reasoner.setPresetTasks(preset);
 					
 						try {
 							assignments = reasoner.schedule(addedTasks);
 						} catch (STPNotConnsistentException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						}
 
 						SVMAdapter svmAdapter;
 						try {
 							Log.d("assignments length", assignments.size() + "");
 							svmAdapter = new SVMAdapter(getApplicationContext());
 
 							int numOfRuns = sharedPrefs.getInt("numberOfRuns", 0);
 							Preference_Learner pl = Preference_Learner.getInstance(assignments, svmAdapter, numOfRuns);
 							try {
 								svmAdapter.init();
 								assignments = pl.rank(pl.setcalenderFeatVector(assignments));
 
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 
 						} catch (NameNotFoundException e) {
 							e.printStackTrace();
 						}
 					
 
 				}
 				
 
 				if(GA_ENABLED){
 				
 				
 				
 				// amr-> Ranker
 				SVMAdapter svmAdapter;
 				try {
 					
 					if(assignments.size()>1){
 						Log.d("debug", assignments.size() + "");
 						svmAdapter = new SVMAdapter(getApplicationContext());
 
 						int numOfRuns = sharedPrefs.getInt("numberOfRuns", 0);
 						Preference_Learner pl = Preference_Learner.getInstance(assignments, svmAdapter, numOfRuns);
 						try {
 							svmAdapter.init();
 							assignments = pl.rank(pl.setcalenderFeatVector(assignments));
 							Log.d("debug", assignments.size() + "");
 						} catch (IOException e) {
 							Log.d("debug", "IOException "+e.getLocalizedMessage());
 							e.printStackTrace();
 					}
 					}
 
 				} catch (NameNotFoundException e) {
 					Log.d("debug", "Name not found"+e.getLocalizedMessage());
 					e.printStackTrace();
 				}
 				Log.d("debug", "here");
 				}
 				// output
 				for (int i = 0; i < assignments.size(); i++) {
 					Log.d("debug", "assignment: " + i);
 					ArrayList<Task> assignment = assignments.get(i);
 					for (int j = 0; j < assignment.size(); j++) {
 						Task t = assignment.get(j);
 						Log.d("debug", "Task: " + t.getStartDate());
 					}
 					Log.d("debug", "-----------------------");
 				}
 				
 				Intent intent = new Intent(CreateMultiTaskActivity.this, SchedulesActivity.class);
				for (int i = 0; i < assignments.size(); i++) {
					for (int j = 0; j < assignments.get(i).size(); j++) {
						Log.d("debug", "asss: "+assignments.get(i).get(j).schedulledNow);
						
					}
				}
 				intent.putExtra("assignments", assignments);
 
 				startActivity(intent);
 			}
 		});
 		findViewById(R.id.AddTask).setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 
 				TextView task = (TextView) LayoutInflater.from(
 						CreateMultiTaskActivity.this).inflate(
 						R.drawable.task, null);
 
 				// task.setText("Task"+(i++));
 
 				Log.d("debug", task.toString());
 				//
 				String body = bodyView.getText().toString();
 				task.setText(body);
 				int priority = CreateMultiTaskActivity.priority;
 				String _estimate = estimateView.getText().toString();
 
 				if (body.isEmpty()) {
 					Toast.makeText(CreateMultiTaskActivity.this,
 							"Please write a body for task!", Toast.LENGTH_LONG)
 							.show();
 					return;
 				}
 
 				if (_estimate.isEmpty()) {
 					Toast.makeText(
 							CreateMultiTaskActivity.this,
 							"Please choose estimate duration of task in hours!",
 							Toast.LENGTH_LONG).show();
 					return;
 				}
 				if (priority == 0) {
 					Toast.makeText(CreateMultiTaskActivity.this,
 							"Please indicate priority to task!",
 							Toast.LENGTH_LONG).show();
 					return;
 				}
 
 				if (parentTask.deadline == null) {
 					Toast.makeText(CreateMultiTaskActivity.this,
 							"Please select task deadline!", Toast.LENGTH_LONG)
 							.show();
 					return;
 				}
 				
 				
 				checkConstraints.clear();
 				parentTask.estimate = Float.parseFloat(_estimate);
 				parentTask.body = body;
 				parentTask.priority = priority;
 				Task.NULLTASK.addAfter(
 						parentTask,
 						new Interval(
 								System.currentTimeMillis(),
 								(long) (parentTask.deadline.getTime() - parentTask.estimate * 60 * 60 * 1000)));
 				parentTask.save();
 				listofAddedTasks.addView(task);
 				listofAddedTasks.addView(new TextView(
 						CreateMultiTaskActivity.this));
 				addedTasks.add(parentTask);
 				
 				
 				
 				parentTask = new Task();
 				bodyView.setText("");
 				priority=0;
 				((TextView)findViewById(R.id.Rate_Statue)).setText("");
 				estimateView.setText("");
 				for (int i = 1; i < listofConstraintAfter.getChildCount() - 1; i++) {
 					listofConstraintAfter.removeViewAt(i + 1);
 				}
 
 				for (int i = 1; i < listofConstraintBefore.getChildCount() - 1; i++) {
 					listofConstraintBefore.removeViewAt(i + 1);
 				}
 				dateText.setText("Select date and time");
 				parentTask = new Task();
 				Log.d("debug", "task details " + parentTask.body + " , "
 						+ parentTask.priority + " , " + parentTask.estimate
 						+ " , " + parentTask.deadline);
 				Toast.makeText(CreateMultiTaskActivity.this,
 						"Saved successfuly.", Toast.LENGTH_SHORT).show();
 
 				task.bringToFront();
 				
 				task.setOnClickListener(new OnClickListener() {
 					
 					public void onClick(final View v) {
 						// TODO Auto-generated method stub
 
 						ClipData data = ClipData.newPlainText("", "");
 						Log.d("debug", "hereeee");
 						if(!checkConstraints.contains(v)){
 							checkConstraints.add(v);
 						}
 						else{
 							Toast.makeText(CreateMultiTaskActivity.this, "Error! Constraints violation ..", Toast.LENGTH_SHORT).show();
 							return;
 						}
 						DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
 								v);
 //						v.startDrag(data, shadowBuilder, v, 0);
 						// v.setVisibility(View.INVISIBLE);
 //						v.setBackgroundResource(R.drawable.shape_clicked);
 						animateBack();
 						View view = v;
 						final LinearLayout layoutBefore= (LinearLayout) LayoutInflater.from(
 								CreateMultiTaskActivity.this).inflate(
 								R.drawable.constraint, null);
 						final LinearLayout layoutAfter= (LinearLayout) LayoutInflater.from(
 								CreateMultiTaskActivity.this).inflate(
 								R.drawable.constraint, null);
 						
 						
 //						TextView task=new TextView(CreateMultiTaskActivity.this);
 						
 						final TextView t = new TextView(CreateMultiTaskActivity.this);
 						t.setText("  ");
 						TextView task=null;
 						if(before){
 						layoutBefore.findViewById(R.id.delete).setOnClickListener(new OnClickListener() {
 							
 							public void onClick(View v1) {
 								// TODO Auto-generated method stub
 								checkConstraints.remove(v);
 								
 //									((LinearLayout)findViewById(R.id.ConstraintLayoutBefore)).removeView(layout);
 								((LinearLayout)findViewById(R.id.ConstraintLayoutBefore)).removeView(layoutBefore);
 								
 							}
 						});
 						task=((TextView)layoutBefore.findViewById(R.id.task));
 						task.setText(((TextView) view).getText());
 						
 						task.setTextSize(18);
 						task.setPadding(0, 0, 30, 0);
 						
						layoutBefore.findViewById(R.id.delete).setPadding(50, 0, 0, 0);
 						layoutBefore.findViewById(R.id.delete).setBackgroundResource(R.drawable.cancel);
 
 						view.setVisibility(View.VISIBLE);
 						layoutBefore.findViewById(R.id.delete).setOnClickListener(new OnClickListener() {
 							
 							public void onClick(View v1) {
 								// TODO Auto-generated method stub
 								checkConstraints.remove(v);
 								
 								((LinearLayout)findViewById(R.id.ConstraintLayoutBefore)).removeView(t);
 								((LinearLayout)findViewById(R.id.ConstraintLayoutBefore)).removeView(layoutBefore);
 								
 							}
 						});
 						}
 						else{
 							layoutAfter.findViewById(R.id.delete).setOnClickListener(new OnClickListener() {
 								
 								public void onClick(View v1) {
 									// TODO Auto-generated method stub
 									checkConstraints.remove(v);
 									
 //										((LinearLayout)findViewById(R.id.ConstraintLayoutBefore)).removeView(layout);
 									((LinearLayout)findViewById(R.id.ConstraintLayoutAfter)).removeView(layoutAfter);
 									
 								}
 							});
 							task=((TextView)layoutAfter.findViewById(R.id.task));
 							task.setText(((TextView) view).getText());
 							
 							task.setTextSize(18);
 //							layoutAfter.findViewById(R.id.textview).setPadding(30, 0, 0, 0);
 							
 							task.setPadding(0, 0, 30, 0);
 							layoutAfter.findViewById(R.id.delete).setBackgroundResource(R.drawable.cancel);
 
 							view.setVisibility(View.VISIBLE);
 							layoutAfter.findViewById(R.id.delete).setOnClickListener(new OnClickListener() {
 								
 								public void onClick(View v1) {
 									// TODO Auto-generated method stub
 									checkConstraints.remove(v);
 									
 									((LinearLayout)findViewById(R.id.ConstraintLayoutAfter)).removeView(t);
 									((LinearLayout)findViewById(R.id.ConstraintLayoutAfter)).removeView(layoutAfter);
 									
 								}
 							});
 						}
 
 						
 						if(before){
 							((LinearLayout)findViewById(R.id.ConstraintLayoutBefore)).addView(t);
 							((LinearLayout)findViewById(R.id.ConstraintLayoutBefore)).addView(layoutBefore);
 							
 						}
 						else{
 							((LinearLayout)findViewById(R.id.ConstraintLayoutAfter)).addView(t);
 							((LinearLayout)findViewById(R.id.ConstraintLayoutAfter)).addView(layoutAfter);
 							
 						}
 						// constraint creation:
 
 						Task t1 = null;
 						for (int i = 1; i < addedTasks.size(); i++) {
 							if (addedTasks.get(i).body.equals(task.getText() + "")) {
 								t1 = addedTasks.get(i);
 								Log.d("debug", "Constraint on drag task body: "
 										+ t1.body);
 							}
 						}
 
 						if (before) {
 							Constraints.add(new PointConstraint(t1, parentTask));
 							parentTask.addAfter(t1, new Interval(0, Reasoner.INFINITY));
 						} else {
 							Constraints.add(new PointConstraint(parentTask, t1));
 							parentTask.addBefore(t1, new Interval(0, Reasoner.INFINITY));
 						}
 						
 
 					}
 				});
 					
 					
 				
 					
 			
 //				task.setOnDragListener(new OnDragListener() {
 //
 //					public boolean onDrag(View v, DragEvent event) {
 //						// TODO Auto-generated method stub
 //
 //						int action = event.getAction();
 //						switch (event.getAction()) {
 //						case DragEvent.ACTION_DRAG_STARTED:
 //							// Do nothing
 //							break;
 //						case DragEvent.ACTION_DRAG_ENTERED:
 //
 //							// v.setBackgroundResource(R.drawable.shape_clicked);
 //							break;
 //						case DragEvent.ACTION_DRAG_EXITED:
 //							// v.setBackgroundResource(R.drawable.shape_clicked);
 //
 //							break;
 //						case DragEvent.ACTION_DROP:
 //							// Dropped, reassign View to ViewGroup
 //							//
 //							v.setBackgroundResource(R.drawable.shape);
 //							// ((TableLayout)findViewById(R.id.ListofAddedTasks)).removeView(task);
 //							// ((TableLayout)findViewById(R.id.ListofAddedTasks)).addView(task);
 //
 //							break;
 //						case DragEvent.ACTION_DRAG_ENDED:
 //							v.setBackgroundResource(R.drawable.shape);
 //						default:
 //							break;
 //						}
 //						return true;
 //					}
 //				});
 			}
 		});
 	}
 
 	class constraintDragListner implements OnDragListener {
 
 		boolean before = false;
 
 		public constraintDragListner(boolean b) {
 			// TODO Auto-generated constructor stub
 			before = b;
 		}
 
 		public boolean onDrag(View v, DragEvent event) {
 			// TODO Auto-generated method stub
 			int action = event.getAction();
 			switch (event.getAction()) {
 			case DragEvent.ACTION_DRAG_STARTED:
 				// Do nothing
 				break;
 			case DragEvent.ACTION_DRAG_ENTERED:
 				v.setBackgroundResource(R.drawable.shape);
 				break;
 			case DragEvent.ACTION_DRAG_EXITED:
 				v.setBackgroundResource(R.drawable.shape_clicked);
 				break;
 			case DragEvent.ACTION_DROP:
 				// Dropped, reassign View to ViewGroup
 				// ((LinearLayout)v.getParent()).addView(child)
 				View view = (View) event.getLocalState();
 				TextView task = (TextView) LayoutInflater.from(
 						CreateMultiTaskActivity.this).inflate(
 						R.drawable.task, null);
 				;
 				task.setText(((TextView) view).getText());
 
 				LinearLayout container = (LinearLayout) v.getParent();
 				TextView t = new TextView(CreateMultiTaskActivity.this);
 				t.setText("  ");
 				container.addView(t);
 				container.addView(task);
 
 				view.setVisibility(View.VISIBLE);
 
 				// constraint creation:
 
 				Task t1 = null;
 				for (int i = 1; i < addedTasks.size(); i++) {
 					if (addedTasks.get(i).body.equals(task.getText() + "")) {
 						t1 = addedTasks.get(i);
 						Log.d("debug", "Constraint on drag task body: "
 								+ t1.body);
 					}
 				}
 
 				if (before) {
 					parentTask.addAfter(t1, new Interval(0, Reasoner.INFINITY));
 				} else {
 					parentTask
 							.addBefore(t1, new Interval(0, Reasoner.INFINITY));
 				}
 
 				break;
 			case DragEvent.ACTION_DRAG_ENDED:
 				v.setBackgroundResource(R.drawable.shape_clicked);
 			default:
 				break;
 			}
 			return true;
 		}
 
 	}
 
 	class taskTouchListner implements OnTouchListener {
 
 		public boolean onTouch(View v, MotionEvent event) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 	}
 
 	class taskDragListner implements OnDragListener {
 
 		public boolean onDrag(View v, DragEvent event) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 	}
 
 	private Task parentTask;
 	private EditText bodyView;
 	private Button prioritylow,priorityhigh,prioritymedium;
 	private EditText estimateView;
 	private Button deadLineView;
 	private Button dueDateView;
 	private Button dateText;
 
 	private final DateSlider.OnDateSetListener mDateTimeSetListener = new DateSlider.OnDateSetListener() {
 		public void onDateSet(DateSlider view, Calendar selectedDate) {
 			// update the dateText view with the corresponding date
 			final int minute = (selectedDate.get(Calendar.MINUTE) / DateTimeSlider.MINUTEINTERVAL)
 					* DateTimeSlider.MINUTEINTERVAL;
 			dateText.setText(String.format("%te. %tB %tY  %tH:%02d",
 					selectedDate, selectedDate, selectedDate, selectedDate,
 					minute));
 
 			selectedDate.set(Calendar.MINUTE, minute);
 			selectedDate.set(Calendar.SECOND, 0);
 			// updating task object's dates
 			if (dateText.getId() == R.id.deadline)
 				parentTask.deadline = selectedDate.getTime();
 			else
 				parentTask.duedate = selectedDate.getTime();
 
 		}
 	};
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		// this method is called after invoking 'showDialog' for the first time
 		// here we initiate the corresponding DateSlideSelector and return the
 		// dialog to its caller
 
 		// get todays date and the time
 		if (id == DateSlider.DATETIMESELECTOR_ID) {
 			final Calendar c = Calendar.getInstance();
 			return new DateTimeSlider(this, mDateTimeSetListener, c);
 		}
 		return null;
 	}
 
 	public void showDateDialog(View view) {
 		dateText = (Button) view;
 		showDialog(DateSlider.DATETIMESELECTOR_ID);
 	}
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
