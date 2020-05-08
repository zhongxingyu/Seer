 package com.t2.vas.activity;
 
 import java.util.ArrayList;
 
 import com.t2.vas.Global;
 import com.t2.vas.R;
 import com.t2.vas.ReminderService;
 import com.t2.vas.activity.preference.MainPreferenceActivity;
 import com.t2.vas.db.DBAdapter;
 import com.t2.vas.db.tables.Group;
 import com.t2.vas.db.tables.Scale;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class MainActivity extends BaseActivity implements OnClickListener, OnItemSelectedListener {
 	private static final String TAG = MainActivity.class.getName();
 	
 	private static final int FORM_ACTIVITY = 345;
 	private static final int RESULTS_ACTIVITY = 346;
 	private static final int NOTES_ACTIVITY = 347;
 	private static final int REMINDER_ACTIVITY = 348;
 	
 	private static final int REPLAY_INTRO = 235325243;
 	
 	private ArrayAdapter<String> adapter;
 	private ArrayList<Group> groupList;
 	private Group currentGroup;
 	private DBAdapter dbHelper;
 	private Toast needScalesToast;
 
 	private ArrayList<String> groupListString = new ArrayList<String>();
 	private SharedPreferences sharedPrefs;
 
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
         
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         this.setContentView(R.layout.main_activity);
         
         this.findViewById(R.id.formActivityButton).setOnClickListener(this);
         this.findViewById(R.id.resultsActivityButton).setOnClickListener(this);
         this.findViewById(R.id.notesActivityButton).setOnClickListener(this);
         this.findViewById(R.id.reminderPreferenceActivityButton).setOnClickListener(this);
 
         sharedPrefs = this.getSharedPreferences(Global.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
         needScalesToast = Toast.makeText(this, R.string.activity_main_no_group_scales, 3000);
         dbHelper = new DBAdapter(this, Global.Database.name, Global.Database.version);
         dbHelper.open();
         
         currentGroup = ((Group)dbHelper.getTable("group")).newInstance();
         groupList = currentGroup.getGroups();
         
         initAdapterData();
         
         adapter = new ArrayAdapter<String>(
         		this, 
         		android.R.layout.simple_spinner_item, 
         		groupListString
 		);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         
         // Init the spinner.
         Spinner spinner = (Spinner)this.findViewById(R.id.groupSelector);
         spinner.setAdapter(adapter);
         spinner.setOnItemSelectedListener(this);
         
         dbHelper.close();
         
         this.findViewById(R.id.formActivityButton).setEnabled(false);
 		this.findViewById(R.id.resultsActivityButton).setEnabled(false);
 		this.findViewById(R.id.notesActivityButton).setEnabled(false);
         
 		// Select the group to work with.
         if(groupList.size() > 0) {
         	boolean groupSelected = false;
         	long group_id = sharedPrefs.getLong("selected_group_id", groupList.get(0)._id);
         	if(group_id > 0) {
 	        	for(int i = 0; i < groupList.size(); i++) {
 	        		if(groupList.get(i)._id == group_id) {
 	        			((Spinner)this.findViewById(R.id.groupSelector)).setSelection(i);
 	        			groupSelected = true;
 	        			break;
 	        		}
 	        	}
         	}
         	
         	if(!groupSelected) {
         		((Spinner)this.findViewById(R.id.groupSelector)).setSelection(0);
         	}
         }
         
         
         // Stopping any services that may be running and starting them up again.
         Intent serviceIntent = new Intent(this, ReminderService.class);
         this.stopService(serviceIntent);
         this.startService(serviceIntent);
 	}
 	
 	private void initAdapterData() {
 		this.dbHelper.open();
 		groupList = currentGroup.getGroups();
 		
 		// Convert the group list to an array of strings.
         groupListString.clear();
         for(int i = 0; i < groupList.size(); i++) {
         	groupListString.add(groupList.get(i).title);
         }
 		
 		if(adapter != null) {
 			adapter.notifyDataSetChanged();
 		}
 	}
 	
 	private void selectGroupAt(int index) {
 		this.currentGroup = this.groupList.get(index);
 		this.dbHelper.open();
 		
 		ArrayList<Scale> scales = this.currentGroup.getScales();
 		if(scales.size() <= 0) {
 			this.findViewById(R.id.formActivityButton).setEnabled(false);
 			this.findViewById(R.id.resultsActivityButton).setEnabled(false);
 			this.findViewById(R.id.notesActivityButton).setEnabled(false);
 			needScalesToast.show();
 		} else {
 			this.findViewById(R.id.formActivityButton).setEnabled(true);
 			this.findViewById(R.id.resultsActivityButton).setEnabled(true);
 			this.findViewById(R.id.notesActivityButton).setEnabled(true);
 		}
 		
 		this.dbHelper.close();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		initAdapterData();
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		SharedPreferences.Editor ed = sharedPrefs.edit();
 		ed.putLong("selected_group_id", this.currentGroup._id);
 		ed.commit();
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch(requestCode) {
 			case FORM_ACTIVITY:
 				if(resultCode == Activity.RESULT_OK) {
 					this.startActivity(RESULTS_ACTIVITY);
 				}
 				break;
 		}
 	}
 
 	private void startActivity(int id) {
 		Intent intent;
 		switch(id) {
 			case FORM_ACTIVITY:
 				intent = new Intent(this, FormActivity.class);
 				intent.putExtra("group_id", this.currentGroup._id);
 				this.startActivityForResult(intent, FORM_ACTIVITY);
 				break;
 				
 			case RESULTS_ACTIVITY:
 				intent = new Intent(this, ResultsActivity.class);
 				intent.putExtra("group_id", this.currentGroup._id);
 				this.startActivityForResult(intent, RESULTS_ACTIVITY);
 				break;
 				
 			case NOTES_ACTIVITY:
 				intent = new Intent(this, NotesActivity.class);
 				intent.putExtra("group_id", this.currentGroup._id);
 				this.startActivityForResult(intent, NOTES_ACTIVITY);
 				break;
 				
 			case REMINDER_ACTIVITY:
 				intent = new Intent(this, ReminderPreferenceActivity.class);
 				intent.putExtra("group_id", this.currentGroup._id);
 				this.startActivityForResult(intent, NOTES_ACTIVITY);
 				break;
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		
 		switch(v.getId()) {
 			case R.id.formActivityButton:
 				this.startActivity(FORM_ACTIVITY);
 				break;
 				
 			case R.id.resultsActivityButton:
 				this.startActivity(RESULTS_ACTIVITY);
 				break;
 				
 			case R.id.notesActivityButton:
 				this.startActivity(NOTES_ACTIVITY);
 				break;
 				
 			case R.id.reminderPreferenceActivityButton:
 				this.startActivity(REMINDER_ACTIVITY);
 				break;
 		}
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		selectGroupAt(arg2);
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> arg0) {
 		
 	}
 	
 	public int getHelp() {
 		return R.string.activity_main_help;
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		
 		MenuItem item = menu.add(Menu.NONE, REPLAY_INTRO, 0, R.string.activity_main_replay_intro);
 		item.setIcon(android.R.drawable.ic_menu_revert);
 		
 		return true;
 	}
 	
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i;
 		switch(item.getItemId()){
 			case REPLAY_INTRO:
 				i = new Intent(this, SplashScreen.class);
 				this.startActivity(i);
 				this.finish();
 				return true;
 		}
 		
		return super.onContextItemSelected(item);
 	}
 }
