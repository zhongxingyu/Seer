 package com.uiproject.meetingplanner;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.uiproject.meetingplanner.database.MeetingPlannerDatabaseHelper;
 import com.uiproject.meetingplanner.database.MeetingPlannerDatabaseManager;
 
 public class DisplayMeeting extends Activity {
 
 	public static final String PREFERENCE_FILENAME = "MeetAppPrefs";
 	public static final String TAG = "DisplayMeeting";
 	TextView title, desc, date, time, tracktime, attendees, location, status;
 	ImageView trackbutton, callbutton, editbutton, alarmbutton, camerabutton, acceptbutton, declinebutton;
 	MeetingInstance meeting;
 	private MeetingPlannerDatabaseManager db;
 	private int uid, statusid, mid;
 	
 	Calendar calendar = Calendar.getInstance();
 	private PendingIntent pendingIntent;
 	
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Log.d(TAG, TAG + "onCreate ");
         setContentView(R.layout.displaymeeting);
         title = (TextView) findViewById(R.id.title);
         desc = (TextView) findViewById(R.id.desc);
         date = (TextView) findViewById(R.id.date);
         time = (TextView) findViewById(R.id.time);
         tracktime = (TextView) findViewById(R.id.tracktime);
         attendees = (TextView) findViewById(R.id.attendees);
         location = (TextView) findViewById(R.id.location);
         status = (TextView) findViewById(R.id.status);
         trackbutton = (ImageView) findViewById(R.id.displaytrackbutton);
         callbutton = (ImageView) findViewById(R.id.displaycallbutton);
         editbutton = (ImageView) findViewById(R.id.displayeditbutton);
         alarmbutton = (ImageView) findViewById(R.id.displayalarmbutton);
         camerabutton = (ImageView) findViewById(R.id.displaycamerabutton);
         acceptbutton = (ImageView) findViewById(R.id.displayacceptbutton);
         declinebutton = (ImageView) findViewById(R.id.displaydeclinebutton);
         
         camerabutton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				takePhoto(v);
 			}
 		});
         alarmbutton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent myIntent = new Intent(DisplayMeeting.this, MyAlarmService.class);
 				myIntent.putExtra("mid", mid);
 
 				pendingIntent = PendingIntent.getService(DisplayMeeting.this, 0, myIntent, 0);
 
 				AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
 
 				calendar = Calendar.getInstance();
 
 				calendar.setTimeInMillis(System.currentTimeMillis());
 
 				calendar.add(Calendar.SECOND, 5);
 
 				alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
 
 				Toast.makeText(DisplayMeeting.this, "Alarm Set", Toast.LENGTH_LONG).show();
 			}
 		});
 
     	SharedPreferences settings = getSharedPreferences(PREFERENCE_FILENAME, MODE_PRIVATE); 
     	uid = settings.getInt("uid", -1);
 
         mid = getIntent().getIntExtra("mid", -1);
         statusid = getIntent().getIntExtra("status", -1);
         Log.v("DisplayMeeting", "status id: " + statusid);
 	    db = new MeetingPlannerDatabaseManager(this, MeetingPlannerDatabaseHelper.DATABASE_VERSION);
 
     }
 	
 	public void onStart(){
 		super.onStart();
 		
 		Log.d(TAG, TAG + "onStart ");
 		
 	    db.open();
 	    meeting = db.getMeeting(mid);
 	   	    
     	String mtitle = meeting.getMeetingTitle();
     	String mdesc = meeting.getMeetingDescription();
     	String maddr = meeting.getMeetingAddress();
     	String mdate = meeting.getMeetingDate();
     	String mtime = meeting.getMeetingStartTime() + "-" + meeting.getMeetingEndTime();
     	String mtracktime = meeting.getMeetingTrackTime() + " minutes";
     	ArrayList<UserInstance> users = db.getMeetingUsersArray(meeting.getMeetingID());
 	    db.close();
     	
     	String mnames = "";
     	boolean added = false;
     	for (UserInstance u : users){
     		if (added){
     			mnames += ", ";
     		}
    		if (u.getUserAttendingStatus() == MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_ATTENDINGSTRING){
	    		mnames = mnames + u.getUserFirstName() + " " + u.getUserLastName();
	    		added = true;
    		}
     	}
     	
     	// Set the view
     	title.setText(mtitle);
     	desc.setText(mdesc);
     	date.setText(mdate);
     	time.setText(mtime);
     	tracktime.setText(mtracktime);
     	location.setText(maddr);
     	attendees.setText(mnames);
     	
     	if (statusid == MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_ATTENDING){
     		status.setText("Attending");
     		attendingButtons();
     	}else if(statusid == MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_DECLINING){
     		status.setText("Declined");
     		declineButtons();
     	}else{
     		status.setText("Pending");
     		pendingButtons();
     	}
 		
     	
 	}
 	
 	public void attendingButtons(){
 		status.setText("Attending"); 
     	if (meeting.getMeetingInitiatorID() != uid){
     		editbutton.setVisibility(View.GONE);
     		camerabutton.setVisibility(View.GONE);
     		callbutton.setVisibility(View.VISIBLE);
     		declinebutton.setVisibility(View.VISIBLE);
     	}else{
     		callbutton.setVisibility(View.GONE);
     		declinebutton.setVisibility(View.GONE);
     		editbutton.setVisibility(View.VISIBLE);
     		camerabutton.setVisibility(View.VISIBLE);
     	}
     	
     	
     	Calendar cal = new GregorianCalendar();
         int currenth = cal.get(Calendar.HOUR_OF_DAY);
         int currentm = cal.get(Calendar.MINUTE);
     	String start = meeting.getMeetingStartTime();
 		int starth = Integer.parseInt(start.substring(0, start.indexOf(':')));
 		int startm = Integer.parseInt(start.substring(start.indexOf(':') + 1));
     	int tracktime = meeting.getMeetingTrackTime();
     	int minutes_before = ((currenth - starth) * 60) + (currentm - startm);
     	if (minutes_before > tracktime){
 	    	trackbutton.setVisibility(View.GONE);
     	}else{
 	    	trackbutton.setVisibility(View.VISIBLE);    		
     	}
     	acceptbutton.setVisibility(View.GONE);
     	alarmbutton.setVisibility(View.VISIBLE);  
 	}
 	
 	public void declineButtons(){
 		editbutton.setVisibility(View.GONE);
 		camerabutton.setVisibility(View.GONE);
 		callbutton.setVisibility(View.GONE);
 		declinebutton.setVisibility(View.GONE);
     	trackbutton.setVisibility(View.GONE);  
     	alarmbutton.setVisibility(View.GONE);  
     	acceptbutton.setVisibility(View.VISIBLE);  
 		
 	}
 	
 	public void pendingButtons(){
 		editbutton.setVisibility(View.GONE);
 		camerabutton.setVisibility(View.GONE);
 		callbutton.setVisibility(View.GONE);
     	trackbutton.setVisibility(View.GONE);   
     	alarmbutton.setVisibility(View.GONE); 
     	acceptbutton.setVisibility(View.VISIBLE);  	
     	declinebutton.setVisibility(View.VISIBLE);  		
 	}
 	
 
 	public void back(View Button){
 		onBackPressed();
 	}
 	
 	
     @Override
     public void onBackPressed(){
     	finish();
     	
     }
     
     
     public void setalarm(View button){
     	//TODO set alarm code here
         Toast.makeText(getBaseContext(), "Setting alarm", Toast.LENGTH_SHORT).show();
     	
     
     }
     
     public void takepic(View button){
     	//TODO taking picture code here
         Toast.makeText(getBaseContext(), "taking a picture", Toast.LENGTH_SHORT).show();
     }
 
     public void call(View button){
     	//TODO
         Toast.makeText(getBaseContext(), "calling initiator", Toast.LENGTH_SHORT).show();
     }
 
     public void edit(View Button){
 		Intent intent = new Intent(DisplayMeeting.this, EditMeeting.class);
 		intent.putExtra("mid", meeting.getMeetingID());
 		startActivity(intent);
   
     }
     public void track(View Button){
 		Intent intent = new Intent(DisplayMeeting.this, TrackerMap.class);
 		intent.putExtra("mid", meeting.getMeetingID());
 		startActivity(intent);
   
     }
     
     public void accept(View Button){
     	attendingButtons();
     	status.setText("Attending");
         Communicator.acceptMeeting(uid, meeting.getMeetingID());
         db.open();
         db.updateMeetingUser(meeting.getMeetingID(), uid, MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_ATTENDING, "0");
         db.close();
         Toast.makeText(this, "meeting accepted", Toast.LENGTH_SHORT).show();
     }    
     
     public void decline(View Button){
     	declineButtons();
     	status.setText("Declined");
         Communicator.declineMeeting(uid, meeting.getMeetingID());
         db.open();
         db.updateMeetingUser(meeting.getMeetingID(), uid, MeetingPlannerDatabaseHelper.ATTENDINGSTATUS_DECLINING, "0");
         db.close();
         Toast.makeText(this, "meeting declined", Toast.LENGTH_SHORT).show();
     }
    
 
 	 // menu 
 	    @Override
 	    public boolean onCreateOptionsMenu(Menu menu) {
 	        MenuInflater inflater = getMenuInflater();
 	        inflater.inflate(R.menu.logouthelpmenu, menu);
 	        return true;
 	    }
 	    
 	    @Override
 	    public boolean onOptionsItemSelected(MenuItem item) {
 	        switch (item.getItemId()) {
 	            case R.id.logout:{
 	            	logout();
 	            	break;
 	            }
 		        case R.id.help:{
 					Intent intent = new Intent(DisplayMeeting.this, HelpPage.class);
 					startActivity(intent);
 		        	break;
 		        }
 	        }
 	        return true;
 	    }
 	    
 	    private void logout(){
           this.setResult(R.string.logout);
           this.finish();
 	    }
 	    
 	    private Uri imageUri;
 	    private static final int TAKE_PICTURE = 9;
 
 	    public void takePhoto(View view) {
 	        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");  
 	        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
 	        intent.putExtra(MediaStore.EXTRA_OUTPUT,
 	                Uri.fromFile(photo));
 	        imageUri = Uri.fromFile(photo);
 	        startActivityForResult(intent, TAKE_PICTURE);
 	    }
 	    
 	    @Override
 	    public void onActivityResult(int requestCode, int resultCode, Intent data) {
 	        super.onActivityResult(requestCode, resultCode, data);
 	        switch (requestCode) {
 	        case TAKE_PICTURE:
 	            if (resultCode == Activity.RESULT_OK) {
 	                Uri selectedImage = imageUri;
 	                getContentResolver().notifyChange(selectedImage, null);
 	                ImageView imageView = (ImageView) findViewById(R.id.meetingpicture);
 	                ContentResolver cr = getContentResolver();
 	                Bitmap bitmap;
 	                try {
 	                     bitmap = android.provider.MediaStore.Images.Media
 	                     .getBitmap(cr, selectedImage);
 
 	                    imageView.setImageBitmap(bitmap);
 	                    imageView.setVisibility(View.VISIBLE);
 	                    Toast.makeText(this, selectedImage.toString(),
 	                            Toast.LENGTH_LONG).show();
 	                } catch (Exception e) {
 	                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
 	                            .show();
 	                    Log.e("Camera", e.toString());
 	                }
 	            }
 	        }
 	    }
 }
