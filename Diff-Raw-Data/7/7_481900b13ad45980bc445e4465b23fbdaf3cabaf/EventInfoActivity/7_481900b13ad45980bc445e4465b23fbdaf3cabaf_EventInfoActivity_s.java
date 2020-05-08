 /*******************************************************************
  * Copyright (c) 2010 Rohit Kumbhar
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  *******************************************************************/
 package net.yama.android.views.activity;
 
 import java.io.File;
 import java.util.Date;
 
 import net.yama.android.R;
 import net.yama.android.managers.DataManager;
 import net.yama.android.managers.config.ConfigurationManager;
 import net.yama.android.managers.connection.ApplicationException;
 import net.yama.android.response.Event;
 import net.yama.android.response.Rsvp;
 import net.yama.android.response.Rsvp.RsvpResponse;
 import net.yama.android.util.Constants;
 import net.yama.android.util.Helper;
 import net.yama.android.views.contentfactory.EventInfoContentFactory;
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TabHost;
 import android.widget.Toast;
 
 /**
  * @author Rohit Kumbhar
  */
 public class EventInfoActivity extends TabActivity {
 
 	private File tempImage;
 	private Intent cameraIntent;
 	private String eventId;
 	private Handler handler = new Handler();
 
 	/**
 	 * Calendar dialog handler
 	 */
 	private Runnable showDialogAndLaunchPrefs = new Runnable() {
 		
 		private AlertDialog dialog;
 
 		public void run() {
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder((EventInfoActivity.this));
 			builder.setMessage(R.string.noCalSel);
 			builder.setCancelable(true);
 			builder.setPositiveButton("OK", new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 					Intent prefsIntent = new Intent(EventInfoActivity.this,RendezvousPreferences.class);
 					startActivityForResult(prefsIntent, Constants.START_PREFS_FOR_CAL);
 				}
 			});
 			
 			builder.setNegativeButton("Cancel", new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 					Toast.makeText(EventInfoActivity.this, "Could not set a reminder", Toast.LENGTH_SHORT).show();
 				}
 			});
 			
 			dialog = builder.show();
 			
 		}
 	};
 	
 	
 	private Event event;
 	private Rsvp rsvp;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Intent i = getIntent();
 		eventId = i.getExtras().getString(Constants.EVENT_ID_KEY);
 		try {
 			event = DataManager.getEvent(eventId);
 			rsvp = DataManager.getMyRsvp(eventId);
 		} catch (ApplicationException e) {
 			// will occur later
 		}
 		setContentView(R.layout.dashboard);
 		EventInfoContentFactory contentFactory = new EventInfoContentFactory(EventInfoActivity.this,eventId);
 		TabHost mTabHost = getTabHost();
 		mTabHost.addTab(mTabHost.newTabSpec(Constants.MEETUP_INFO_TAB_ID).setIndicator("Info").setContent(contentFactory));
 	    mTabHost.addTab(mTabHost.newTabSpec(Constants.MEETUP_RSVP_TAB_ID).setIndicator("RSVP").setContent(contentFactory));
 	    mTabHost.addTab(mTabHost.newTabSpec(Constants.MEETUP_TALK_TAB_ID).setIndicator("Talk").setContent(contentFactory));
 	    mTabHost.addTab(mTabHost.newTabSpec(Constants.MEETUP_ALL_RSVP_ID).setIndicator("Responses").setContent(contentFactory));
 	    mTabHost.setCurrentTab(0);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, Constants.TAKE_A_PICTURE, 0, R.string.takePhoto).setIcon(android.R.drawable.ic_menu_camera);
 		Date now = new Date();
 		
 		if(Helper.haveCalendars(this) &&  now.before(event.getEventTime()))
 			menu.add(0, Constants.ADD_TO_CALENDAR, 0, R.string.addToCalendar).setIcon(android.R.drawable.ic_menu_month);
 		
		if(now.after(event.getEventTime()) && (rsvp.getResponse().equals(RsvpResponse.YES)) || rsvp.getResponse().equals(RsvpResponse.MAYBE))
 			menu.add(0, Constants.RATE_EVENT, 0, R.string.rateEvent).setIcon(android.R.drawable.ic_menu_edit);
 		
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		
 		switch (item.getItemId()) {
 		 case Constants.TAKE_A_PICTURE:
 		    	startCamera();
 		        return true;
 		 case Constants.ADD_TO_CALENDAR:
 			 	addEventToCalendar();
 			 	break;
 		 case Constants.RATE_EVENT:
 			 	launchRatingDialog();
 			 	break;
 		 }
 		
 		
 		return false;
 	}
 
 	private void launchRatingDialog() {
 		Intent i = new Intent(this,EventRatingActivity.class);
 		i.putExtra(Constants.EVENT_ID_KEY, event);
 		startActivity(i);
 	}
 
 	private void addEventToCalendar() {
 		
 		String calendarId = ConfigurationManager.instance.getReminderCalendarId();
 		
 		if(calendarId == null){
 			// No calendar selected. Open the preferences page
 			handler.post(showDialogAndLaunchPrefs);
 			return;
 		}
 		
 		try {
 			// Add to calendar.
 			Event event = DataManager.getEvent(eventId);
 			ContentValues calEntry = new ContentValues();
 			calEntry.put("calendar_id", calendarId);
 			calEntry.put("title", event.getName());
 			calEntry.put("description", event.getDescription());
 			calEntry.put("eventLocation", event.getVenue().toString());
 			calEntry.put("dtstart", event.getEventTime().getTime());
 			calEntry.put("visibility", 0);
 			calEntry.put("transparency", 0);
 			calEntry.put("hasAlarm", 1);
 			
 			Uri eventsUri = Uri.parse("content://calendar/events");
 			Uri calendars_2_2_events = Uri.parse("content://com.android.calendar/events");
 			
 			Uri url = null;
 			try {
 				url = getContentResolver().insert(eventsUri, calEntry);
 			} catch (Exception e) {
 				// Trying for 2.2
 				url = getContentResolver().insert(calendars_2_2_events, calEntry);
 			}
 			
 			if(url != null)
 				Toast.makeText(EventInfoActivity.this, getText(R.string.savedInCal), Toast.LENGTH_SHORT).show();
 			
 		} catch (Exception e) {
 			Log.e(EventInfoActivity.class.getName(), e.getMessage());
 			Helper.logCrashReport(e, this);
 			Toast.makeText(EventInfoActivity.this, getText(R.string.calSaveFailed), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private void startCamera() {
 
 		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
 			// they don't have an SDCard, give them an error message and quit
 			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("SD Card could not be found. Cannot start camera.").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				public void onClick(final DialogInterface dialog, final int id) {
 					finish();
 				}
 			});
 			final AlertDialog alert = builder.create();
 			alert.show();
 		} else {
 			tempImage = new File(Helper.getTempStorageDirectory(), Constants.TEMP_IMAGE);
 			cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempImage));
 			startActivityForResult(cameraIntent, Constants.CAMERA_INTENT_ID);
 		}
 
 	}
 		
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		
 		super.onActivityResult(requestCode, resultCode, intent);
 		if (requestCode == Constants.CAMERA_INTENT_ID && resultCode == RESULT_CANCELED) {
 			Toast.makeText(this,R.string.cancelPhoto,Toast.LENGTH_SHORT).show();
 			return;
 		}
 		switch (requestCode) {		
 			case Constants.CAMERA_INTENT_ID: 
 				Intent uploadPhoto = new Intent(this, UploadPhotoActivity.class);
 				uploadPhoto.putExtra(Constants.TEMP_IMAGE_FILE_PATH,tempImage.getAbsolutePath());
 				uploadPhoto.putExtra(Constants.EVENT_ID_KEY, eventId);
 				startActivity(uploadPhoto);
 				break;
 			case Constants.START_PREFS_FOR_CAL:
 				addEventToCalendar();
 				break;
 		}
 	}
 }
