 package com.madp.meetme;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.madp.meetme.common.entities.Meeting;
 import com.madp.meetme.webapi.WebService;
 import com.madp.utils.Logger;
 import com.madp.utils.ParticipantsAdapter;
 import com.madp.utils.SerializerHelper;
 
 /**
  * Activity for displaying Meeting information
  * 
  * @author Niklas and Peter initial version
  * @author Saulius 2012-02-19 integrated WebService, added TODO elements, remove obsolete stuff
  * 
  */
 public class MeetingInfoActivity extends ListActivity {
 	private final String TAG = "MeetingInfoActivity";
 	private ParticipantsAdapter p_adapter;
 	private Context c;
 	private Meeting meeting;
 	private WebService ws;
 	private String getTime, getDate, getAlarm;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.meetinginfo);
 		
 		/* Create important stuff */
 		ws = new WebService(new Logger());
 		TextView infolabel = (TextView) findViewById(R.id.infolabel);
 		TextView timelabel = (TextView) findViewById(R.id.meetingtime);
 		TextView alarmlabel = (TextView) findViewById(R.id.alarmtime);
 		TextView datelabel = (TextView) findViewById(R.id.meetingdate);
 		TextView locationlabel = (TextView) findViewById(R.id.locationview);
 		ImageButton cancelMeeting = (ImageButton) findViewById(R.id.cancelmeeting);
 		final ImageButton updateAndExit = (ImageButton) findViewById(R.id.update);
 		//ImageButton mapbutton	= (ImageButton) findViewById(R.id.iblocation);
 		ImageButton live_map = (ImageButton) findViewById(R.id.ib_live_map);
 		byte [] a=null;
 		
 		
 		Bundle extras = getIntent().getExtras();
 		if(extras != null)
 			 a= extras.getByteArray("meeting");
 		else
 			Log.e("extras","no bundle in the intent");
 		if(a == null)
 			Log.e("getByteArray", "no extras tagged as meeting");
 		else{
 			meeting= (Meeting) SerializerHelper.deserializeObject(a);
 					
 			this.p_adapter = new ParticipantsAdapter(this, R.layout.meetingrow, meeting.getParticipants());
 			getListView().setAdapter(p_adapter);
 			/* Set meeting info */
			
 			getTime = meeting.gettStarting();
 			
 			String[] result = getTime.split(" ");
 			getDate = result[0];
 			getTime = result[1];
 			String[] resultDate = getDate.split("-");
 			String[] resultTime = getTime.split(":");
 			String resultTimeString =resultTime[0]+":"+resultTime[1];
 			String alarmtime = calculateAlarmTime(resultTimeString, 15);
 			
 			infolabel.setText(meeting.getTitle());
 			alarmlabel.setText(alarmtime);
 			timelabel.setText(resultTimeString);
 			datelabel.setText(resultDate[2]+"/"+resultDate[1]+"-"+resultDate[0]); 
 			// TODO: deal with this, date and time is one thing...
 			locationlabel.setText(meeting.getAddress());
 		}
 //		mapbutton.setOnClickListener(new View.OnClickListener() {
 //			
 //			@Override
 //			public void onClick(View v) {
 //				// TODO Auto-generated method stub
 //				Intent myintent = new Intent(v.getContext(),GPSFindLocationOnMap.class);
 //				
 //				Bundle b = new Bundle();
 //				b.putByteArray("meeting", SerializerHelper.serializeObject(meeting));
 //				myintent.putExtras(b);
 //				startActivity(myintent);
 //			}
 //		});
 		
 		
 		
 		cancelMeeting.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
 					
 				builder.setMessage("Are you sure you want to delete this meeting?").setCancelable(false)
 						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								/* Update email list on server to exclude this user */
 								// /TODO: to be implemented in WebService
 								finish();
 							}
 						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 				AlertDialog alert = builder.create();
 				alert.show();
 			}
 		});
 
 		live_map.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Intent myintent = new Intent(arg0.getContext(),GPSMovingObjectsActivity.class);
 				
 				Bundle b = new Bundle();
 				b.putByteArray("meeting", SerializerHelper.serializeObject(meeting));
 				myintent.putExtras(b);
 				startActivity(myintent);
 			}
 		});
 		
 		/* TODO: Add this button in the layout of meeting info!!!*/
 		updateAndExit.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				int i = 0;
 				// TODO: to be implemented in WebService
 				
 				// Stop service when pressing exit
 				updateAndExit.setClickable(false);
 				stopService(new Intent(v.getContext(), BackgroundMeetingManager.class));
 				
 				finish();
 			}
 		});
 	}
 
 	// /TODO: should be fixed to support two arguments: starting date time (e.g '2012-02-02 11:50') and number of
 	// minutes to start alarm before the meeting.
 	public static String calculateAlarmTime(String meetingTime, int minutes){
 		
 		char[] time = meetingTime.toCharArray();
 		String hour = time[0] + "" + time[1];
 		String min = time[3] + ""+ time[4];
 		
 		int timeHour = Integer.valueOf(hour);
 		int timeMin = Integer.valueOf(min);
 		if(timeMin >= minutes){	
 			if(timeHour ==0){
 				if((timeMin-minutes)==0){
 					return "00" + ":" + (timeMin-minutes)+"0";
 				}
 				return "00" + ":" + (timeMin-minutes);	
 			}
 			
 			if((timeMin-minutes)==0){
 				return timeHour + ":" + (timeMin-minutes)+"0";
 			}
 			return timeHour + ":" + (timeMin-minutes);
  		}
 		else{
 			if((timeHour-1 ) == 0){
 				return (timeHour-1) +"0" +":" + (timeMin+(60-minutes));
 			}
 			else if(timeHour == 0){
 				return "23:"+ (timeMin+(60-minutes));
 			}
 			return (timeHour-1) +":" + (timeMin+(60-minutes));
 		}
 	}
 
 	private void refreshMeetingInfo(int id) {
 		Meeting meetingTmp = ws.getMeeting(id);
 		if (meetingTmp != null) {
 			meeting = meetingTmp;
 		}
 	}
 
 	/* Implement listers for buttons and a Item in the list */
 
 	/* Click on a listitem */
 
 }
