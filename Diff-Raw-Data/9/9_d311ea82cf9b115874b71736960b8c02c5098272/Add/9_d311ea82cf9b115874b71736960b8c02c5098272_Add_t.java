 package eecs.berkeley.edu.cs294;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.graphics.Color;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TableLayout;
 import android.widget.TimePicker;
 
 public class Add extends Activity {
 	private String title, place, note, tag, group_id, status, priority, timestamp, deadline, to_do_rails_id;
 	private String array_spinner_group[];
 
 	EditText et_title, et_place, et_note;
 	AutoCompleteTextView actv_tag;
 	Spinner s_group, s_priority, s_deadline;
 	Button b_submit_1, b_submit_2, b_priority_low, b_priority_medium, b_priority_high, b_deadline_date, b_deadline_time;
 	RadioButton rb_status_incomplete, rb_status_in_progress, rb_status_complete;
 	TabHost th_add;
 	TableLayout tl_add_1, tl_add_2;
 
 	ArrayAdapter<String> adapter;
 
 	private int mYear, mMonth, mDay, mHour, mMinute;
 	static final int DATE_DIALOG_ID = 0;
 	static final int TIME_DIALOG_ID = 1;
 	DatePickerDialog.OnDateSetListener mDateSetListener;
 	TimePickerDialog.OnTimeSetListener mTimeSetListener;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add);
 		
 		setTitle("Add ToDo");
 
 		th_add = (TabHost) findViewById(R.id.th_add);
 		th_add.setup();
 
 		tl_add_1 = (TableLayout) findViewById(R.id.tl_add_1);
 		tl_add_2 = (TableLayout) findViewById(R.id.tl_add_2);
 
 		TabSpec tab_one = th_add.newTabSpec("tab_one_btn_tab");
 		tab_one.setContent(R.id.tl_add_1);
 		tab_one.setIndicator("Basic", getResources().getDrawable(R.drawable.tab_simple));
 		th_add.addTab(tab_one);
 
 		TabSpec tab_two = th_add.newTabSpec("tab_two_btn_tab");
 		tab_two.setContent(R.id.tl_add_2);
 		tab_two.setIndicator("More", getResources().getDrawable(R.drawable.tab_more));
 		th_add.addTab(tab_two);
 
 		th_add.setCurrentTab(0);
 
 		et_title = (EditText) findViewById(R.id.et_title);
 
 		et_place = (EditText) findViewById(R.id.et_place);
 
 		List<String> groups = ToDo_Replica.dh.select_all_group_name();
 		array_spinner_group = new String[groups.size()];
 		//array_spinner_group[0] = "None";
 
 		for(int i = 0; i < groups.size(); i++)
 			array_spinner_group[i] = groups.get(i);
 
 		s_group = (Spinner) findViewById(R.id.s_group);
 		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array_spinner_group);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		s_group.setAdapter(adapter);
 		s_group.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 				String group_name =  parent.getItemAtPosition(pos).toString();
 				if(group_name.equalsIgnoreCase("None"))
 					group_id = "None";
 				else
 					group_id = group_name;
 			}
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 		});
 
 		rb_status_incomplete = (RadioButton) findViewById(R.id.rb_status_incomplete);
 		rb_status_in_progress = (RadioButton) findViewById(R.id.rb_status_in_progress);
 		rb_status_complete = (RadioButton) findViewById(R.id.rb_status_complete);
 
 		et_note = (EditText) findViewById(R.id.et_note);
 
 		actv_tag = (AutoCompleteTextView) findViewById(R.id.actv_tag);
 
 		b_priority_low = (Button) findViewById(R.id.b_priority_low);
 		b_priority_low.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				priority = "Low";
 
 				b_priority_low.setTextColor(Color.YELLOW);
 				b_priority_medium.setTextColor(Color.BLACK);
 				b_priority_high.setTextColor(Color.BLACK);
 			}
 		});
 
 		b_priority_medium = (Button) findViewById(R.id.b_priority_medium);
 		b_priority_medium.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				priority = "Medium";
 				b_priority_medium.setTextColor(Color.YELLOW);
 				b_priority_low.setTextColor(Color.BLACK);
 				b_priority_high.setTextColor(Color.BLACK);
 			}
 		});
 
 		b_priority_high = (Button) findViewById(R.id.b_priority_high);
 		b_priority_high.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				priority = "High";
 				b_priority_high.setTextColor(Color.YELLOW);
 				b_priority_medium.setTextColor(Color.BLACK);
 				b_priority_low.setTextColor(Color.BLACK);
 			}
 		});
 		priority = "Low";
 		b_priority_low.setTextColor(Color.YELLOW);
 
 		b_deadline_date = (Button) findViewById(R.id.b_deadline_date);
 		b_deadline_time = (Button) findViewById(R.id.b_deadline_time);
 
 		b_deadline_date.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(DATE_DIALOG_ID);
 			}
 		});
 
 		b_deadline_time.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(TIME_DIALOG_ID);
 			}
 		});
 
 		b_submit_1 = (Button) findViewById(R.id.b_submit_1);
 		b_submit_1.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				title = et_title.getText().toString();
 				place = et_place.getText().toString();
 				if(rb_status_incomplete.isChecked())
 					status = "Incomplete";
 				else if(rb_status_in_progress.isChecked())
 					status = "In Progress";
 				else
 					status = "Complete";
 				note = et_note.getText().toString();
 				tag = actv_tag.getText().toString();
 				Time time = new Time();
 				String timestamp = Long.toString(time.normalize(false));
 				deadline = mYear + " " + mMonth + " " + mDay + "," + mHour + " " + mMinute;
 				to_do_rails_id = "";
 
 				ToDo_Replica.dh.insert_to_do(title, place, note, tag, group_id, status, priority, timestamp, deadline, to_do_rails_id);
 				
 				/* Push changes to the remote if applicable */
 				List<String> newEntry = ToDo_Replica.dh.select_to_do("title", title);
				if(group_id.equalsIgnoreCase("None") == false) {
 					ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 					NetworkInfo netInfo = connManager.getActiveNetworkInfo();
 
 					if(netInfo == null) {
 						Log.d("DEBUG", "--------------- No internet connection --------- ");
 					}
 
 					if (netInfo.isConnected()) {
 						ServerConnection.pushRemote(newEntry, 
 								ServerConnection.TODO_SERVER_UPDATE,
 								ServerConnection.CREATE_REQUEST);
 					}
 				}
 
 				setResult(RESULT_OK);
 				finish();
 			}
 		});
 
 		final Calendar c = Calendar.getInstance();
 		mYear = c.get(Calendar.YEAR);
 		mMonth = c.get(Calendar.MONTH);
 		mDay = c.get(Calendar.DAY_OF_MONTH);
 		mHour = c.get(Calendar.HOUR_OF_DAY);
 		mMinute = c.get(Calendar.MINUTE);
 
 		updateDisplay();
 
 		mDateSetListener = new DatePickerDialog.OnDateSetListener() {
 			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 				mYear = year;
 				mMonth = monthOfYear;
 				mDay = dayOfMonth;
 				updateDisplay();
 			}
 		};
 		
 		mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
 			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 				mHour = hourOfDay;
 				mMinute = minute;
 				updateDisplay();
 			}
 		};
 	}
 
 	private void updateDisplay() {
 		b_deadline_date.setText(new StringBuilder()
 		.append(mMonth + 1).append("-")
 		.append(mDay).append("-")
 		.append(mYear).append(" "));
 		b_deadline_time.setText(new StringBuilder()
 		.append(pad(mHour)).append(":")
 		.append(pad(mMinute)));			
 	}
 
 	private static String pad(int c) {
 		if (c >= 10)
 			return String.valueOf(c);
 		else
 			return "0" + String.valueOf(c);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
 		case TIME_DIALOG_ID:
 	        return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, false);
 		}
 		return null;
 	}
 }
