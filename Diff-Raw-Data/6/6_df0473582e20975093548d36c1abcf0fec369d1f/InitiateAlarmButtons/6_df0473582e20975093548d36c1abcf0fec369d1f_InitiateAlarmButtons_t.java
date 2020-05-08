 package noteedit;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.annotation.TargetApi;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.DatePicker;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import com.example.ass2note.R;
 import com.example.ass2note.alarm.DatePickerFragment;
 import com.example.ass2note.alarm.TimePickerFragment;
 import com.example.ass2note.location.ConnectionService;
 
 public class InitiateAlarmButtons implements Parcelable {
 	 private int mData;
 	private NoteEdit noteEdit;
 	private NoteEditLayoutManager layoutManager;
 	public Calendar myCalendar = Calendar.getInstance();
 	private long da = 0;
 	private int timesCalledDate = 1, timesCalledTime = 1;
 	private ToggleButton alarmPosition, alarmTime, showAlarmInfo;
 	Dialog dialog;
 	
 	FragmentManager mangerSupport;
 	InitiateAlarmButtons inn;
 	
 	public InitiateAlarmButtons(Context cont, NoteEditLayoutManager layoutM) {
 		noteEdit = (NoteEdit) cont;
 		da = myCalendar.getTimeInMillis();
 		layoutManager = layoutM;
 
 		initiateAlarmButtons();
 	}
 
 	private void initiateAlarmButtons() {
 		alarmPosition = (ToggleButton) noteEdit
 				.findViewById(R.id.toggleAlarmPosition);
 		alarmTime = (ToggleButton) noteEdit.findViewById(R.id.toggleAlarmTime);
 		showAlarmInfo = (ToggleButton) noteEdit
 				.findViewById(R.id.showAlarmNoteInfo);
 
 		
 		
 		alarmPosition.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				layoutManager.changePositionBtnStatus();
 			}
 		});
 
 		alarmTime.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				layoutManager.changeTimeStatus();
 			}
 		});
 
 		showAlarmInfo.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				layoutManager.showAlarmLayout();
 			}
 		});
 	}
 /*
 	DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
 
 		public void onDateSet(DatePicker view, int year, int monthOfYear,
 				int dayOfMonth) {
 			
 			/*
 			 * The Jelly Bean API has an error and calls DatePicker twice, so we
 			 * need to only call it only once:
 			 */
 /*
 
 			if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.JELLY_BEAN) {
 				timesCalledDate++;
 				if ((timesCalledDate % 2) == 0)
 					setDate(year, monthOfYear, dayOfMonth);
 
 				// All other API's calls DatePicker only once.
 			} else
 				setDate(year, monthOfYear, dayOfMonth);
 		}
 	};
 */
 	public void setDate(int year, int monthOfYear, int dayOfMonth) {
 		myCalendar.set(Calendar.YEAR, year);
 		myCalendar.set(Calendar.MONTH, monthOfYear);
 		myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
 	}
 /*
 	TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			
 			/*
 			 * The Jelly Bean API has an error and calls TimePicker twice, so we
 			 * need to only call it only once:
 			 */
 	/*
 			if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.JELLY_BEAN) {
 				timesCalledTime++;
 				if ((timesCalledTime % 2) == 0)	setTime(hourOfDay, minute);
 			} 
 			// All other API's calls DatePicker only once.
 			else setTime(hourOfDay, minute);
 
 		} // end onTimeSet
 	};
 
 */	public void setTime(int hourOfDay, int minute) {
 		
 		myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
 		myCalendar.set(Calendar.MINUTE, minute);
 		da = myCalendar.getTimeInMillis();
 
 		Date date = new Date();
 		long now = date.getTime();
 		if (da > now){
 			updateTime();
 			
 		}else {
			
			// We are postponing this since its a bug we dont have time to fix and the this point in time.
			// Its just that is shows up all the time when orientating the phone since it wont save the time untile The
			// timepicker has sett the time.
			//Toast.makeText(noteEdit, R.string.no_alarm_has_been_set , Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void initiateAlarmButtonDialog(final FragmentManager manger,  final InitiateAlarmButtons in) {
 		dialog = new Dialog(noteEdit);
 		// Set the dialog title
 		dialog.setTitle(R.string.alarm);
 		// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		dialog.setContentView(R.layout.alert_dialog);
 		
 		dialog.show();
 		
 		ImageButton positionButton = (ImageButton) dialog
 				.findViewById(R.id.positionButton);
 		ImageButton timeButton = (ImageButton) dialog
 				.findViewById(R.id.timeButton);
 		
 		
 			
 		timeButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				mangerSupport = manger;
 				DialogFragment newFragment;
 				inn = in;
 				 newFragment = new TimePickerFragment(in);
 				 newFragment.show( manger , "timePicker");
 				 newFragment = null;
 				 
 				
 				 newFragment = new DatePickerFragment(in);
 				 newFragment.show( manger , "datePicker");
 				
 				 newFragment = null;
 				
 				dialog.dismiss();
 				
 			}
 		});
 
 		positionButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(noteEdit, ConnectionService.class);
 				intent.putExtra("fromActivity", "NoteEdit");
 				noteEdit.startService(intent);
 				
 				dialog.dismiss();
 			}
 		});
 		
 	}
 
 	public long getDa() {
 		return da;
 	}
 
 	private void updateTime() {
 		Intent i = new Intent(
 				"com.example.ass2note.notepad.NoteEdit.connectionReceiver");
 		i.putExtra("fromCaller", "InitiateAlarmButtons");
 		i.putExtra("command", "updateTime");
 		i.putExtra("time", da);
 		noteEdit.sendBroadcast(i);
 	}
 
 public void dimiss() {
 	if(dialog!= null)dialog.dismiss();
 }
 public boolean isDialogShowing(){
 	  if(dialog!=null && dialog.isShowing())return true;
 	  return false;
 	 }
 
 public void newFragment(){
 	DialogFragment newFragment;
 	 newFragment = new TimePickerFragment(inn);
 	 newFragment.show( mangerSupport , "timePicker");
 	 newFragment = null;
 }
 
 public int describeContents() {
 	// TODO Auto-generated method stub
 	return 0;
 }
 
 public void writeToParcel(Parcel out, int flags) {
    
 	out.writeInt(mData);
 }
 }// -- End Class
