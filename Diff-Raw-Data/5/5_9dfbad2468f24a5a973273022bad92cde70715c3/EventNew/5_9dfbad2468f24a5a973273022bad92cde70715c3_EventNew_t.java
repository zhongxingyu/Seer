 package com.connectsy.events;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 import com.connectsy.R;
 import com.connectsy.categories.CategoryManager;
 import com.connectsy.categories.CategoryManager.Category;
 import com.connectsy.data.DataManager.DataUpdateListener;
 import com.connectsy.events.EventManager.Event;
 import com.connectsy.events.attendants.AttendantManager;
 import com.connectsy.settings.MainMenu;
import com.connectsy.ActionBarHandler;
 import com.connectsy.users.UserManager;
 import com.connectsy.users.UserManager.User;
 import com.connectsy.utils.DateUtils;
 
 public class EventNew extends Activity implements OnClickListener, 
 		DataUpdateListener, OnKeyListener {
 	private final String TAG = "NewEvent";
 	private ProgressDialog loadingDialog;
     private EventManager eventManager;
     private Category category;
     private ArrayList<User> chosenUsers;
     private JSONObject eventJSON;
 	
     // where we display the selected date and time
     private TextView mDateDisplay;
     private TextView mTimeDisplay;
 	
     static final int TIME_DIALOG_ID = 0;
     static final int DATE_DIALOG_ID = 1;
     static final int SELECT_CATEGORY = 2;
     static final int SELECT_FRIENDS = 3;
     static final int CREATE_EVENT = 4;
     static final int INVITE_USERS = 5;
     
     private int mYear;
     private int mMonth;
     private int mDay;
     private int mHour;
     private int mMinute;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.event_new);
 
        //set up logo clicks
        ActionBarHandler ab = new ActionBarHandler(this);
        
         EditText what = (EditText) findViewById(R.id.events_new_what);
         what.setOnKeyListener(this);
         EditText where = (EditText) findViewById(R.id.events_new_where);
         where.setOnKeyListener(this);
         
         mDateDisplay = (TextView) findViewById(R.id.events_new_date);
         mTimeDisplay = (TextView) findViewById(R.id.events_new_time);
         
         Button dateButton = (Button)findViewById(R.id.events_new_date_change);
         dateButton.setOnClickListener(this);
         Button timeButton = (Button)findViewById(R.id.events_new_time_change);
         timeButton.setOnClickListener(this);
         
         Button everyone = (Button)findViewById(R.id.events_new_who_everyone);
         everyone.setOnClickListener(this);
         Button friends = (Button)findViewById(R.id.events_new_who_friends);
         friends.setOnClickListener(this);
         friends.setSelected(true);
         Button choose = (Button)findViewById(R.id.events_new_who_choose);
         choose.setOnClickListener(this);
         LinearLayout category = (LinearLayout)findViewById(R.id.events_new_cat);
         category.setOnClickListener(this);
         LinearLayout friendSelector = (LinearLayout)findViewById(R.id.events_new_friends_selected);
         friendSelector.setOnClickListener(this);
         
         Button submitButton = (Button)findViewById(R.id.events_new_submit);
         submitButton.setOnClickListener(this);
         
         final Calendar c = Calendar.getInstance();
         mYear = c.get(Calendar.YEAR);
         mMonth = c.get(Calendar.MONTH);
         mDay = c.get(Calendar.DAY_OF_MONTH);
         mHour = c.get(Calendar.HOUR_OF_DAY);
         mMinute = c.get(Calendar.MINUTE);
         
         updateTimeDisplay();
         
         CategoryManager.precacheCategories(this);
     }
 
 	public void onClick(View v) {
 		int id = v.getId();
         if (id == R.id.events_new_time_change){
         	showDialog(TIME_DIALOG_ID);
         }else if (id == R.id.events_new_date_change){
         	showDialog(DATE_DIALOG_ID);
 	    }else if (id == R.id.events_new_who_everyone){
         	setWho("everyone");
 	    }else if (id == R.id.events_new_who_friends){
         	setWho("friends");
 	    }else if (id == R.id.events_new_who_choose){
         	setWho("choose");
 	    }else if (id == R.id.events_new_cat){
         	getCategory();
 	    }else if (id == R.id.events_new_friends_selected){
         	selectFriends();
 	    }else if (id == R.id.events_new_submit){
         	submitData();
 	    }else{
 	    	Log.d("events", "bad view is for button");
 	    }
 	}
 	
 	private void selectFriends(){
 		Log.d(TAG, "selecting friends");
 		Intent i = new Intent(Intent.ACTION_CHOOSER);
 		i.setType("vnd.android.cursor.item/vnd.connectsy.user");
 		ArrayList<User> users = new UserManager(this, this, null).getFriends(false);
 		i.putExtra("com.connectsy.users", User.serializeList(users));
 		startActivityForResult(i, SELECT_FRIENDS);
 	}
 	private void getCategory(){
 		Intent i = new Intent(Intent.ACTION_CHOOSER);
 		i.setType("vnd.android.cursor.item/vnd.connectsy.category");
 		startActivityForResult(i, SELECT_CATEGORY);
 	}
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		try {
 			if (resultCode == RESULT_OK && requestCode == SELECT_CATEGORY){
 					category = new Category(data.getExtras()
 							.getString("com.connectsy.category"));
 				TextView title = (TextView)findViewById(R.id.events_new_category_title);
 				title.setText(category.name);
 			}else if (resultCode == RESULT_OK && requestCode == SELECT_FRIENDS){
 					chosenUsers = User.deserializeList(data.getExtras()
 							.getString("com.connectsy.users"));
 					String usersText = "";
 					for (int i=0;i<chosenUsers.size();i++){
 						usersText += chosenUsers.get(i).username;
 						usersText += " ";
 					}
 					TextView users = (TextView)findViewById(R.id.events_new_friends_selected_text);
 					users.setText(usersText);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case TIME_DIALOG_ID:
                 return new TimePickerDialog(this,
                         mTimeSetListener, mHour, mMinute, false);
             case DATE_DIALOG_ID:
                 return new DatePickerDialog(this,
                         mDateSetListener, mYear, mMonth, mDay);
         }
         return null;
     }
 
     private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener() {
 
                 public void onDateSet(DatePicker view, int year, int monthOfYear,
                         int dayOfMonth) {
                     mYear = year;
                     mMonth = monthOfYear;
                     mDay = dayOfMonth;
                     
                     updateTimeDisplay();
                 }
             };
 
     private TimePickerDialog.OnTimeSetListener mTimeSetListener =
             new TimePickerDialog.OnTimeSetListener() {
 
                 public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                     mHour = hourOfDay;
                     mMinute = minute;
                     
                     updateTimeDisplay();
                 }
             };
             
     private Calendar getCal(){
     	Calendar c = Calendar.getInstance();
     	c.set(mYear, mMonth, mDay, mHour, mMinute);
     	return c;
     }
             
     private void updateTimeDisplay() {
     	Date selected = getCal().getTime();
     	String dateString = DateUtils.formatDate(selected);
     	String timeString = DateUtils.formatTime(selected);
         mTimeDisplay.setText(timeString);
         mDateDisplay.setText(dateString);
     }
     
     private void setEnabled(Button enabled, Button disabled, Button disabled2){
 		enabled.setSelected(true);
 		disabled.setSelected(false);
 		disabled2.setSelected(false);
 	}
     
     private void setWho(String who){
     	Button ev = (Button)findViewById(R.id.events_new_who_everyone);
     	Button fr = (Button)findViewById(R.id.events_new_who_friends);
     	Button ch = (Button)findViewById(R.id.events_new_who_choose);
     	if (who == "everyone"){
 	        findViewById(R.id.events_new_friends_selected).setVisibility(View.GONE);
 	        findViewById(R.id.events_new_cat).setVisibility(View.VISIBLE);
 	        setEnabled(ev, fr, ch);
     	}else if (who == "choose"){
 	        findViewById(R.id.events_new_friends_selected).setVisibility(View.VISIBLE);
 	        findViewById(R.id.events_new_cat).setVisibility(View.GONE);
 	        setEnabled(ch, fr, ev);
     	}else{
 	        findViewById(R.id.events_new_friends_selected).setVisibility(View.GONE);
 	        findViewById(R.id.events_new_cat).setVisibility(View.GONE);
 	        setEnabled(fr, ev, ch);
     	}
 
     	final ScrollView scroll = (ScrollView)findViewById(R.id.events_new_scroller);
     	scroll.post(new Runnable() { 
     	    public void run() { 
     	        scroll.fullScroll(ScrollView.FOCUS_DOWN); 
     	    } 
     	});
     }
 
     private void submitData() {
     	Log.d(TAG, "creating event");
     	
         EditText desc = (EditText) findViewById(R.id.events_new_what);
         EditText where = (EditText) findViewById(R.id.events_new_where);
         String strDesc = desc.getText().toString();
         String strWhere = where.getText().toString();
         Button bcast = (Button)findViewById(R.id.events_new_who_everyone);
         
         SharedPreferences data = getSharedPreferences("consy", 0);
         String username = data.getString("username", "username_fail");
 
         long when = getCal().getTime().getTime();
         
         eventManager = new EventManager(this, this, null, null);
         Event event = eventManager.new Event();
         event.description = strDesc;
         event.where = strWhere;
         event.when = when;
         event.creator = username;
         event.broadcast = bcast.isSelected();
         if (event.broadcast && category != null)
         	event.category = category.name;
         eventManager.createEvent(event, CREATE_EVENT);
         loadingDialog = ProgressDialog.show(this, "", "Posting event...", true);
     }
     	
     public boolean onCreateOptionsMenu(Menu menu) {
         return MainMenu.onCreateOptionsMenu(menu);
 	}
     
     public boolean onOptionsItemSelected(MenuItem item) {
         return MainMenu.onOptionsItemSelected(this, item);
     }
 
 	public void onDataUpdate(int code, String response) {
 		try {
 			if (code == CREATE_EVENT)
 				eventJSON = new JSONObject(response);
 	        Button friends = (Button)findViewById(R.id.events_new_who_friends);
 	        Button choose = (Button)findViewById(R.id.events_new_who_choose);
 			if (code == CREATE_EVENT && (friends.isSelected() || choose.isSelected())){
 				AttendantManager att = new AttendantManager(this, this, eventJSON.getString("id"));
 				if (choose.isSelected())
 					att.bulkInvite(chosenUsers, 0);
 				else
 					att.bulkInvite(null, 0);
 			}else{
 				loadingDialog.dismiss();
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setType("vnd.android.cursor.item/vnd.connectsy.event");
 				i.putExtra("com.connectsy.events.revision", eventJSON.getString("revision"));
 				startActivity(i);
 				this.finish();
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void onRemoteError(int httpStatus, int code) {
 		if (loadingDialog != null) loadingDialog.dismiss();
 	}
 
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		TextView chars;
 		int max;
         if (v.getId() == R.id.events_new_what){
         	chars = (TextView)findViewById(R.id.events_new_chars_what);
         	max = 150;
         }else if (v.getId() == R.id.events_new_where){
         	chars = (TextView)findViewById(R.id.events_new_chars_where);
         	max = 25;
         }else{
         	return false;
         }
     	int curChars = ((EditText)v).getText().length();
         chars.setText(Integer.toString(max-curChars));
 		return false;
 	}
 }
