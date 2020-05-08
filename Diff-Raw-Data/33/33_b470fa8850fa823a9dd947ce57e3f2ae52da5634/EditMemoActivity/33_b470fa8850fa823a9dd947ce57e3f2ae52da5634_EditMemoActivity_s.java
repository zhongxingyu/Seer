 package com.vladimirdaniyan.android.memo;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 
 import com.azazeleleven.android.memo.DaoMaster;
 import com.azazeleleven.android.memo.DaoMaster.DevOpenHelper;
 import com.azazeleleven.android.memo.DaoSession;
 import com.azazeleleven.android.memo.Note;
 import com.azazeleleven.android.memo.NoteDao;
 
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import de.keyboardsurfer.android.widget.crouton.Style;
 
 public class EditMemoActivity extends Activity implements OnClickListener,
 		OnTimeSetListener {
 
 	TimePicker myTimePicker;
 	TimePickerDialog timePickerDialog;
 
 	private EditText editText;
 	private String memoText;
 	private String alarmTime;
 
 	private DaoMaster daoMaster;
 	private DaoSession daoSession;
 	private NoteDao memoDao;
 	private SQLiteDatabase db;
 	private Long mRowId;
 
 	private Calendar newCal;
 	private Note memo;
 	private View divider;
 	private LinearLayout llTimerButtonHost;
 
 	private int mCurSpinnerPos = 0;
 
 	private Date currentTime;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_memo);
		
 		currentTime = Calendar.getInstance().getTime();
 
 		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "memo-db",
 				null);
 		db = helper.getWritableDatabase();
 		daoMaster = new DaoMaster(db);
 		daoSession = daoMaster.newSession();
 		memoDao = daoSession.getNoteDao();
 
 		editText = (EditText) findViewById(R.id.edit_text_memo);
 		llTimerButtonHost = (LinearLayout) findViewById(R.id.ll_timer_button_host);
 		divider = findViewById(R.id.divider);
 
 		Button cancelTimer = (Button) findViewById(R.id.button_cancel_timer);
 		Button changeTimer = (Button) findViewById(R.id.button_set_timer);
 		changeTimer.setOnClickListener(this);
 		cancelTimer.setOnClickListener(this);
		
 
 		// Get extras from list activity
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			String memoText = extras.getString("memoText");
 			mRowId = extras.getLong("mRowId");
 			alarmTime = extras.getString("alarmTime");
 			currentTime = (Date) extras.get("currentTime");
			// Log.d("LOG_TAG", "the value of currentTime is " + currentTime);
 			if (memoText != null && mRowId != null) {
 				editText.setText(memoText);
 				// mCurSpinnerPos = 0;
 				if (currentTime != null) {
 					mCurSpinnerPos = 1;
 					if (alarmTime != null) {
 						// only show the cancel timer button if an alarm was
 						// previously set
 						mCurSpinnerPos = 2;
 						llTimerButtonHost.setVisibility(View.VISIBLE);
 						divider.setVisibility(View.VISIBLE);
 					}
 				}
 			}
 		}
		
 		final Spinner spinner = (Spinner) findViewById(R.id.spinner1);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 				this, R.array.notification_types,
 				android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);
 		spinner.setSelection(mCurSpinnerPos);
 
 		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				// save the spinner position
 				switch (position) {
 				case 0:
 					mCurSpinnerPos = spinner.getSelectedItemPosition();
 					break;
 				case 1:
 					mCurSpinnerPos = spinner.getSelectedItemPosition();
 					break;
 				case 2:
 					mCurSpinnerPos = spinner.getSelectedItemPosition();
 					if (alarmTime == null) {
 						openTimePickerDialog(false);
 					}
 
 					break;
 				}
 
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parent) {
 				mCurSpinnerPos = 0;
 			}
 		});
 
		
 		// Inflate a "Done/Discard" custom action bar view.
 		LayoutInflater inflater = (LayoutInflater) getActionBar()
 				.getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
 		final View customActionBarView = inflater.inflate(
 				R.layout.actionbar_custom_view_done_discard, null);
 		customActionBarView.findViewById(R.id.actionbar_done)
 				.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						memoText = editText.getText().toString();
 						// notify user if edit text is empty
 						if (memoText.matches("")) {
 							Crouton.makeText(EditMemoActivity.this,
 									R.string.toast_input_required, Style.ALERT,
 									R.id.alternate_view_group).show();
 							return;
 						}
 						// Get the spinner position
 						if (mCurSpinnerPos == 1) {
 							saveMemoText();
 							showNotification();
 						} else if (mCurSpinnerPos == 2) {
							setAlarm(newCal);
							finish();
 						} else {
 							saveMemoText();
 							finish();
 						}
 					}
 				});
 		customActionBarView.findViewById(R.id.actionbar_discard)
 				.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						finish();
 					}
 				});
 
 		// Show the custom action bar view and hide the normal Home icon and
 		// title.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
 				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
 						| ActionBar.DISPLAY_SHOW_TITLE);
 		actionBar.setCustomView(customActionBarView,
 				new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
 						ViewGroup.LayoutParams.MATCH_PARENT));
 

 		// // Get Note to Self intent
 		String msg = getIntent().getStringExtra("android.intent.extra.TEXT");
 		if (msg == null) {
 			return;
 		} else {
 			mCurSpinnerPos = 1;
 			memoText = msg;
			currentTime = Calendar.getInstance().getTime();
 			saveMemoText();
 			showNotification();
 			;
 		}
		
 
 	}
 
 	private void openTimePickerDialog(boolean is24HourView) {
 		Calendar calendar = Calendar.getInstance();
 
 		timePickerDialog = new TimePickerDialog(this, (OnTimeSetListener) this,
 				calendar.get(Calendar.HOUR_OF_DAY),
 				calendar.get(Calendar.MINUTE), is24HourView);
 		timePickerDialog.show();
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.button_cancel_timer:
 			memoText = editText.getText().toString();
 			cancelAlarm();
 			finish();
 			break;
 		case R.id.button_set_timer:
 			openTimePickerDialog(false);
 			break;
 		}
 
 	}
 
 	// save the text that was entered
 	private void saveMemoText() {
 		if (mCurSpinnerPos == 0) {
 			alarmTime = null;
 			currentTime = null;
 		} else if (mCurSpinnerPos == 1) {
 			alarmTime = null;
 		}
 
 		memo = new Note(mRowId, memoText, alarmTime, currentTime);
 		memo.setComment(alarmTime);
 		memo.setDate(currentTime);
 		memo.setId(mRowId);
 		memoDao.insertOrReplace(memo);
 		db.close();
 	}
 
 	// show the notification if applicable
 	private void showNotification() {
 
 		NotificationCompat.Builder builder = new NotificationCompat.Builder(
 				this).setSmallIcon(R.drawable.ic_stat_memo)
 				.setContentTitle("Memo").setContentText(memoText);
 
 		NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
 		bigTextStyle.setBigContentTitle("Memo").bigText(memoText);
 		builder.setStyle(bigTextStyle);
 
 		// create intent for edit action when notification is clicked
 		Intent resultIntent = new Intent(this, ListMemoActivity.class);
 
 		// artificial back stack for started activity
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 
 		// Intent to start Activity at the top of the stack
 		stackBuilder.addNextIntent(resultIntent);
 		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 		builder.setContentIntent(resultPendingIntent);
 
 		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		nm.notify((int) currentTime.getTime(), builder.build());
 
 		finish();
 
 	}
 
 	@Override
 	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 		// Get time right now
 		newCal = Calendar.getInstance();
 		newCal.setTimeInMillis(System.currentTimeMillis());
 
 		int hourNow = newCal.get(Calendar.HOUR_OF_DAY);
 		int minuteNow = newCal.get(Calendar.MINUTE);
 
 		// advance alarm by one day if behind current time
 		if (hourOfDay < hourNow || hourOfDay == hourNow && minute == minuteNow) {
 			newCal.add(Calendar.DAY_OF_YEAR, 1);
 		}
 		newCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
 		newCal.set(Calendar.MINUTE, minute);
 		newCal.set(Calendar.SECOND, 0);
 		newCal.set(Calendar.MILLISECOND, 0);
 
 		Crouton.makeText(this,
 				FormatAlarmText.formatText(this, newCal.getTimeInMillis()),
 				Style.INFO, R.id.alternate_view_group).show();
 
 	}
 
 	// set the timer for the memo and pass the alarm time for the listview
 	private void setAlarm(Calendar targetCal) {
 		alarmTime = "Alarm was set for : "
 				+ java.text.DateFormat.getTimeInstance(
 						java.text.DateFormat.SHORT).format(targetCal.getTime());
 
 		// use the current time as a unique requestCode for the pending intent
		currentTime = Calendar.getInstance().getTime();
 
 		Intent alarmIntent = new Intent(getBaseContext(), TimerReceiver.class);
 		alarmIntent.putExtra("memoText", memoText);
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(
 				getBaseContext(), (int) currentTime.getTime(), alarmIntent, 0);
 
 		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(),
 				pendingIntent);
 
 		saveMemoText();
 
		db.close();

 	}
 
 	// clear the timer and its text in the main listview
 	private void cancelAlarm() {
 
 		Intent alarmIntent = new Intent(getBaseContext(), TimerReceiver.class);
 		PendingIntent pendingIntent = PendingIntent.getBroadcast(
 				getBaseContext(), (int) currentTime.getTime(), alarmIntent, 0);
 		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		alarmManager.cancel(pendingIntent);
 
 		// set the alarm time and date to null since they aren't needed anymore
 		alarmTime = null;
 		Date currentTime = null;
 		memo = new Note(mRowId, memoText, alarmTime, currentTime);
 		memo.setComment(alarmTime);
 		memo.setDate(currentTime);
 		memoDao.insertOrReplace(memo);
 
 		db.close();
 	}
 
 	@Override
 	protected void onDestroy() {
 		Crouton.cancelAllCroutons();
 		super.onDestroy();
 	}
 
 }
