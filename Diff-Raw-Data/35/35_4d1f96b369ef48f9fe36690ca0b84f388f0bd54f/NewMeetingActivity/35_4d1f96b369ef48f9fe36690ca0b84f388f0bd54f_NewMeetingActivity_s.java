 package com.madp.meetme;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.PendingIntent;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.BaseColumns;
 import android.provider.ContactsContract;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 import com.madp.meetme.common.entities.Meeting;
 import com.madp.meetme.common.entities.User;
 import com.madp.meetme.webapi.WebService;
 import com.madp.utils.Logger;
 import com.madp.utils.ParticipantsAdapter;
 import com.madp.utils.SerializerHelper;
 import com.madp.utils.Statics;
 
 //TODO: we should probably have one activity for creating new meeting, updating it and displaying it's info
 /**
  * Activity for creating new Meeting
  * 
  * @author Niklas and Peter initial version
  * @author Saulius 2012-02-19 integrated WebService, added TODO elements, remove obsolete stuff
  * 
  */
 public class NewMeetingActivity extends ListActivity {
 	private static final String TAG = "NewMeetingActivity";
	private EditText nameOfMeeting, nameOfPlace;
	private String newMeetingName, newMeetingPlace, newMeetingTime, newMeetingDate, smin;
 	private ImageButton createMeeting, meetingTime, addParticipants, meetingDate;
 	private TextView infolabel;
 	private ParticipantsAdapter p_adapter;
 	private static final int TIME_DIALOG_ID = 0;
 	private static final int DATE_DIALOG_ID = 1;
	private int hour, min, year, month, day, meetingId;
 	private long timeLeftToMeetingInMillisec;
	private static final int CONTACT_PICKER_RESULT = 12345;
	private ListView lv;
 	private Meeting meeting;
 	private WebService ws;
 	private AlarmManager am;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.newmeeting);
 
 		ws = new WebService(new Logger());
 		meeting = new Meeting();
 		nameOfMeeting = (EditText) findViewById(R.id.meetingName);
 		nameOfPlace = (EditText) findViewById(R.id.meetingPlace);
 		createMeeting = (ImageButton) findViewById(R.id.createMeeting);
 		meetingTime = (ImageButton) findViewById(R.id.meetingTime);
 		meetingDate = (ImageButton) findViewById(R.id.meetingDate);
 		addParticipants = (ImageButton) findViewById(R.id.addParticipants);
 		infolabel = (TextView) findViewById(R.id.infolabel);
 
 		final Calendar c = Calendar.getInstance();
 
 		hour = c.get(Calendar.HOUR_OF_DAY);
 		min = c.get(Calendar.MINUTE);
 		year = c.get(Calendar.YEAR);
 		month = c.get(Calendar.MONTH);
 		day = c.get(Calendar.DAY_OF_MONTH);
 
 		this.p_adapter = new ParticipantsAdapter(this, R.layout.meetingrow, meeting.getParticipants());
 		this.getListView().setAdapter(p_adapter);
 		infolabel.setText("New Meeting");
 
 		/* Add participants */
 		addParticipants.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
 				startActivityForResult(intent, CONTACT_PICKER_RESULT);
 			}
 		});
 
 		/* If all data is inserted create meeting ! */
 		createMeeting.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				/**
 				 * Currently you must send all the fields
 				 */
 				meeting.setTitle(nameOfMeeting.getText().toString());
 				meeting.setAddress(nameOfPlace.getText().toString());
 				meeting.setDuration(60); // TODO: to be implemented
 				meeting.setMonitoring(20); // TODO: to be implemented
 				meeting.settStarting(year + "-" + (month+1) + "-" + day + " " + hour + ":" + min);
 
 				meeting.setOwner(new User(0, "", "demo@gmail.com"));
 				
 				// Set owner
 				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 				User owner = new User(0, prefs.getString(Statics.USERNAME, null), prefs.getString(Statics.USEREMAIL,
 						null));
 				meeting.setOwner(owner);				
 
 
 				Intent intent = new Intent(view.getContext(), MeetingsListActivity.class);
 				Bundle s = new Bundle();
 				s.putByteArray("meeting", SerializerHelper.serializeObject(meeting));
 				intent.putExtras(s);
 				setResult(RESULT_OK, intent);
 				
 				/* Post the meeting*/
                String meetingId = ws.postMeeting(meeting);
                char [] meetingIdArray = meetingId.toCharArray();
                char [] idNumber = new char[meetingIdArray.length - 11];
                int j=0;
                for(int i = 11; i<meetingIdArray.length;i++){
                    idNumber[j] = meetingIdArray[i];
                    j++;
                }         
                String result = "";
                for(int i = 0; i<idNumber.length; i++){
               	 result += idNumber[i];
                 }
                int id = Integer.parseInt(result);
                 timeLeftToMeetingInMillisec = TimeToMeetingInLong(year, month, day, hour, min);
                 setOneTimeAlarm(timeLeftToMeetingInMillisec, id);
 				finish();
 			}
 		});
 
 		// Buttonlisteners for the date and time buttons
 		meetingTime.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(TIME_DIALOG_ID);
 			}
 		});
 
 		meetingDate.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(DATE_DIALOG_ID);
 			}
 		});
 	}
 
 	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
 		@Override
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			hour = hourOfDay;
 			min = minute;
 			/* LAYOUTPROBLEM?*/
 		}
 	};
 	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
 
 		@Override
 		public void onDateSet(DatePicker view, int thisYear, int monthOfYear, int dayOfMonth) {
 			year = thisYear;
 			month = monthOfYear;
 			day = dayOfMonth;
 
 		}
 	};
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case TIME_DIALOG_ID:
 			return new TimePickerDialog(this, mTimeSetListener, hour, min, true);
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this, mDateSetListener, year, month, day);
 		}
 		return null;
 	}
 	
 	/* Turn clock 15 min back or smth */
 	private long TimeToMeetingInLong(int newYear, int newMonth, int newDay, int newHour, int newMin){
 
         Date d1 = new GregorianCalendar(newYear, newMonth, newDay, newHour, newMin).getTime();
         Date today = new Date();
         System.out.println(d1.getTime() - today.getTime());
         System.out.println(d1.getTime() + " " + today.getTime());
         return (d1.getTime() - today.getTime());
     }
 
     public void setOneTimeAlarm(long timeLeftToMeetingInMillisec, int id) {
         am = (AlarmManager) getSystemService(Context.ALARM_SERVICE); 
         Intent meetingIntent = new Intent(this, MeetingAlarmManager.class);
         meetingIntent.putExtra("meetingid", id);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                 meetingIntent, PendingIntent.FLAG_ONE_SHOT);
 
         Calendar cal = Calendar.getInstance();
         am.set(AlarmManager.RTC_WAKEUP,
                 (System.currentTimeMillis() + timeLeftToMeetingInMillisec), pendingIntent);
     }
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK) {
 			if (requestCode == CONTACT_PICKER_RESULT) {
 				final User user = new User(-1, null, null);
 				List<String> emails = new ArrayList<String>();
 				Uri contactData = data.getData();
 				Cursor userCursor = managedQuery(contactData, null, null, null, null);
 				if (userCursor.moveToFirst()) {
 					// Get contact id
 					String contactId = userCursor.getString(userCursor.getColumnIndex(BaseColumns._ID));
 					// Get user name
 					user.setName(userCursor.getString(userCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
 					// Get contact emails
 					Cursor emailsCursor = getContentResolver().query(
 							ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
 							ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null);
 					while (emailsCursor.moveToNext()) {
 						String e = emailsCursor.getString(emailsCursor
 								.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
 						if (e.length() > 0) {
 							emails.add(e);
 						}
 					}
 					emailsCursor.close();
 					userCursor.close();
 					Log.d(TAG, "Emails found:" + emails);
 				}
 
 				// Set email
 				if (emails.size() == 1) { // only one email
 					Log.d(TAG, "One email found");
 					user.setEmail(emails.get(0));
 				} else if (emails.size() > 1) { // multiple email per contact
 					Log.d(TAG, "More then one email found");
 					final CharSequence[] items = emails.toArray(new CharSequence[emails.size()]);
 					AlertDialog.Builder builder = new AlertDialog.Builder(this);
 					builder.setTitle("Choose email to use");
 					builder.setItems(items, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int item) {
 							user.setEmail((String) items[item]);
 							p_adapter.notifyDataSetChanged();
 						}
 					});
 					AlertDialog alert = builder.create();
 					alert.show();
 				} else { // no emails set for the contact
 					Log.d(TAG, "No emails found");
 					Toast.makeText(this, "This contact has no emails set", Toast.LENGTH_SHORT).show();
 					return;
 				}
 				meeting.getParticipants().add(user);
 				p_adapter.notifyDataSetChanged();
 			}
 		}
 	}
 }
