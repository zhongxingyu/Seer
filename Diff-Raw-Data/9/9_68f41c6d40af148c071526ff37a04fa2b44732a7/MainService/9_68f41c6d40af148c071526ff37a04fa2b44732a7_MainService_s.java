 package com.risotto.service;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 
 import com.risotto.controller.StatusBarNotification;
 import com.risotto.controller.StatusBarNotificationManager;
 import com.risotto.model.Drug;
 import com.risotto.model.Patient;
 import com.risotto.model.Prescription;
 import com.risotto.model.Prescription.ScheduledDay;
 import com.risotto.model.Schedule;
 import com.risotto.storage.StorageProvider;
 
 public class MainService extends Service {
 	
 	// A log tag used for debugging.
 	public static final String LOG_TAG = "RISOTTO_SERVICE";
 	
 	// The intent used to start this service.
 	public static final String ACTION_START_SERVICE = "com.risotto.service.START_SERVICE";
 	// The intent used to schedule an alarm.
 	public static final String ACTION_ALARM_TRIGGER = "com.risotto.service.ALARM_TRIGGER";
 	// The intent used to schedule an alarm.
 	public static final String ACTION_ALARM_SCHEDULE = "com.risotto.service.ALARM_SCHEDULE";
 	// The intent used when the schedules need to be recalculated.
 	public static final String ACTION_ALARM_RESCHEDULE = "com.risotto.service.ALARM_RESCHEDULE";
 
 	// The schedule id placed in the alarm pending intent
 	private static final String PENDING_INTENT_SCHEDULE_ID = "SCHEDULE_ID";
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		Log.d(LOG_TAG, "onCreate has been called...");
 		super.onCreate();
 	}
 
 	@Override
 	public void onDestroy() {
 		Log.d(LOG_TAG, "onDestroy has been called...");
 		super.onDestroy();
 	}
 
 	@Override
 	public void onLowMemory() {
 		Log.d(LOG_TAG, "onLowMemory has been called...");
 		super.onLowMemory();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		// Handle this command by passing it to the following method.
 		handleCommand(intent);
 		
 		return Service.START_STICKY;
 	}
 	
 	private void scheduleNewPrescription(int prescriptionId) {
 		Log.d(LOG_TAG, "Scheduling a new prescription in the schedule table...");
 		Log.d(LOG_TAG, "Scheduling prescription id: " + prescriptionId);
 		
 		Uri prescriptionUri = ContentUris.withAppendedId(StorageProvider.PrescriptionColumns.CONTENT_URI, prescriptionId);
 		
 		String[] prescriptionProjection = { 
 				StorageProvider.PrescriptionColumns._ID,
 				StorageProvider.PrescriptionColumns.PRESCRIPTION_PATIENT,
 				StorageProvider.PrescriptionColumns.PRESCRIPTION_DRUG,
 				StorageProvider.PrescriptionColumns.PRESCRIPTION_DOSE_TYPE,
 				StorageProvider.PrescriptionColumns.PRESCRIPTION_DAY_SUNDAY,
 				StorageProvider.PrescriptionColumns.PRESCRIPTION_DAY_MONDAY,
 				StorageProvider.PrescriptionColumns.PRESCRIPTION_DAY_TUESDAY,
 				StorageProvider.PrescriptionColumns.PRESCRIPTION_DAY_WEDNESDAY,
 				StorageProvider.PrescriptionColumns.PRESCRIPTION_DAY_THURSDAY,
 				StorageProvider.PrescriptionColumns.PRESCRIPTION_DAY_FRIDAY,
 				StorageProvider.PrescriptionColumns.PRESCRIPTION_DAY_SATURDAY
 				};
 		
 		Cursor prescriptionCursor = this.getApplicationContext().getContentResolver().query(prescriptionUri, prescriptionProjection, null, null, null);
 		
 		prescriptionCursor.moveToFirst();
 		
 		Prescription prescription = Prescription.fromCursor(prescriptionCursor, this.getApplicationContext());
 		
 		Log.d(LOG_TAG, "Prescription dose type: " + prescription.getDoseType());
 		
 		switch (prescription.getDoseType()) {
 			case Prescription.DOSE_TYPE_EVERY_DAY:
 			case Prescription.DOSE_TYPE_EVERY_HOUR:
 				
 				Log.d(LOG_TAG, "Dose type is every day or every hour...");
 				
 				// Pick any day and schedule an alarm for each time with an interval of 24 hours
 				Enumeration<String> sundayTimes = prescription.getScheduledTimes(Calendar.SUNDAY);
 				
 				while(sundayTimes.hasMoreElements()) {
 					// Get the next scheduled time
 					String scheduledTime = sundayTimes.nextElement();
 					
 					Log.d(LOG_TAG, "The current time to schedule: " + scheduledTime);
 					
 					// Parse out the hours and minutes from the times
 					String hourValue = scheduledTime.split(":")[0];
 					String minuteValue = scheduledTime.split(":")[1];
 					
 					// Get an instance of the current date/time
 					Calendar firstDate = Calendar.getInstance();
 					
 					// Set the hour/minute in todays date
 					firstDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourValue));
 					firstDate.set(Calendar.MINUTE, Integer.parseInt(minuteValue));
 					firstDate.set(Calendar.SECOND, Schedule.ZERO);
 					
 					Log.d(LOG_TAG, "Current date/time: " + new Date(System.currentTimeMillis()).toString());
 					Log.d(LOG_TAG, "The 'first time' of this schedule: " + new Date(firstDate.getTimeInMillis()).toString() );
 					
 					// See if we have already passed the first scheduled time...
 					if ( firstDate.getTimeInMillis() < System.currentTimeMillis() ) {
 						// If we have, schedule it for 24 hours from now
 						firstDate.add(Calendar.HOUR, Schedule.TWENTY_FOUR_HOURS_IN_HOURS);
 						
 						Log.d(LOG_TAG, "Scheduling this prescription on the next date: " + new Date(firstDate.getTimeInMillis()).toString() );
 						
 					} else {
 						// Else, schedule it for today.
 						
 						Log.d(LOG_TAG, "Scheduling the prescription for today at this calendar: " + new Date(firstDate.getTimeInMillis()).toString() );
 						
 					}
 					
 					// TODO: See if there is some way that we can calculate the count remaining
 					
 					// Store the schedule!
 					Schedule newSchedule = new Schedule(prescription.get_id(), firstDate.getTimeInMillis(), Schedule.TWENTY_FOUR_HOURS_IN_MS );
 					Uri scheduleUri = this.getApplicationContext().getContentResolver().insert(StorageProvider.ScheduleColumns.CONTENT_URI, newSchedule.toContentValues());
 					
 					// Create a pending intent which will be sent when the alarm is done.
 					Intent scheduleIntent = this.buildPrescriptionIntent(Integer.parseInt(scheduleUri.getPathSegments().get(1)));
 					
 					// Get an instance of the AlarmManager
 					AlarmManager am = (AlarmManager)this.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 					
 					PendingIntent pendingIntent = PendingIntent.getService(this, 0, scheduleIntent, PendingIntent.FLAG_ONE_SHOT);
 					
 					// Have the AlarmManager schedule the alarm.
 					
 					Log.d(LOG_TAG, "Setting an alarm for: " + new Date(firstDate.getTimeInMillis()).toString() );
 					Log.d(LOG_TAG, "Repeating every 24 hours.");
 					
 					Log.d(LOG_TAG, "Current time in ms: " + System.currentTimeMillis());
 					Log.d(LOG_TAG, "Scheduled for: " + firstDate.getTimeInMillis());
 					Log.d(LOG_TAG, "Difference of: " + (firstDate.getTimeInMillis() - System.currentTimeMillis()) );
 					
 					am.set(AlarmManager.RTC_WAKEUP, firstDate.getTimeInMillis(), pendingIntent);
 					
 				}
 				
 				break;
 			case Prescription.DOSE_TYPE_EVERY_DAY_OF_WEEK:
 			case Prescription.DOSE_TYPE_EVERY_HOUR_DAY_OF_WEEK:
 				
 				Log.d(LOG_TAG, "Dose type is every day of week or every hour day of week...");
 				
 				// Get all the days that are scheduled.
 				
 				Enumeration<ScheduledDay> scheduledDays = prescription.getAllScheduledTimeVectors();
 				
 				// For each of those days...
 				while( scheduledDays.hasMoreElements() ) {
 					
 					// ...get the day.
 					ScheduledDay currentDay = scheduledDays.nextElement();
 					
 					// ...get the times.
 					Enumeration<String> dayTimes = currentDay.getTimes().elements();
 					
 					// For each of the times...
 					while(dayTimes.hasMoreElements()) {
 						// ...schedule an alarm with an interval of 7 days.
 						// Get the next scheduled time
 						String scheduledTime = dayTimes.nextElement();
 						
 						Log.d(LOG_TAG, "The current time to schedule: " + scheduledTime);
 						
 						// Parse out the hours and minutes from the times
 						String hourValue = scheduledTime.split(":")[0];
 						String minuteValue = scheduledTime.split(":")[1];
 						
 						// Get an instance of the current date/time
 						Calendar firstDate = Calendar.getInstance();
 						
 						// Set the hour/minute in todays date
 						firstDate.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourValue));
 						firstDate.set(Calendar.MINUTE, Integer.parseInt(minuteValue));
 						firstDate.set(Calendar.SECOND, Schedule.ZERO);
 						firstDate.set(Calendar.DAY_OF_WEEK, currentDay.getDay());
 						
 						Log.d(LOG_TAG, "Current date/time: " + new Date(System.currentTimeMillis()).toString());
 						Log.d(LOG_TAG, "The 'first time' of this schedule: " + new Date(firstDate.getTimeInMillis()).toString() );
 						
 						// See if we have already passed the first scheduled time...
 						if ( firstDate.getTimeInMillis() < System.currentTimeMillis() ) {
 							// If we have, schedule it for 7 days from now
 							firstDate.add(Calendar.DATE, Schedule.SEVEN_DAYS_IN_DAYS);
 							
 							Log.d(LOG_TAG, "Scheduling this prescription on the next date: " + new Date(firstDate.getTimeInMillis()).toString() );
 							
 						} else {
 							// Else, schedule it for today.
 							
 							Log.d(LOG_TAG, "Scheduling the prescription for today at this calendar: " + new Date(firstDate.getTimeInMillis()).toString() );
 							
 						}
 						
 						// TODO: See if there is some way that we can calculate the count remaining
 						
 						// Store the schedule!
 						Schedule newSchedule = new Schedule(prescription.get_id(), firstDate.getTimeInMillis(), Schedule.SEVEN_DAYS_IN_MS );
 						Uri scheduleUri = this.getApplicationContext().getContentResolver().insert(StorageProvider.ScheduleColumns.CONTENT_URI, newSchedule.toContentValues());
 						
 						// Create the intent that will be sent when the alarm triggers
 						Intent scheduleIntent = this.buildPrescriptionIntent(Integer.parseInt(scheduleUri.getPathSegments().get(1)));
 						
 						// Get an instance of the AlarmManager
 						AlarmManager am = (AlarmManager)this.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 						
 						// Create a pending intent which will be sent when the alarm is done.
 						PendingIntent pendingIntent = PendingIntent.getService(this, 0, scheduleIntent, PendingIntent.FLAG_ONE_SHOT);
 						
 						// Have the AlarmManager schedule the alarm.
 						
 						Log.d(LOG_TAG, "Setting an alarm for: " + new Date(firstDate.getTimeInMillis()).toString() );
 						Log.d(LOG_TAG, "Repeating every 7 days.");
 						
 						am.set(AlarmManager.RTC_WAKEUP, firstDate.getTimeInMillis(), pendingIntent);
 						
 					}			
 				}
 				break;
 			
 			default:
 				Log.e(LOG_TAG, "Not sure how we ended up here without a dose type...");
 		}
 		
 	}
 	
 	@SuppressWarnings("unused")
 	private void scheduleGivenDay() {
 		// TODO: Fill in the logic that was copy/pasted in the scheduleNewPrescription() method.
 	}
 	
 	private Intent buildPrescriptionIntent(int scheduleId) {
 		
 		Log.d(LOG_TAG, "Building intent for schedule entry " + scheduleId );
 		
 		// Create an intent that will launch when the trigger is done.
 		Intent newIntent = new Intent(ACTION_ALARM_TRIGGER);
 		
 		newIntent.setAction(ACTION_ALARM_TRIGGER + ":" + scheduleId);
 		
 		newIntent.putExtra(MainService.PENDING_INTENT_SCHEDULE_ID, scheduleId);
 		newIntent.setClass(this, MainService.class);
 		
 		return newIntent;
 	}
 	
 	private boolean isActionAlarmTriggerIntent(String actionString) {
 		if ( (actionString.split(":")[0]).equals(ACTION_ALARM_TRIGGER) ) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	private boolean isScheduleChanged() {
 		Log.d(LOG_TAG, "The schedule has changed for this prescription.");
 		// TODO: implement this function to compare a given prescription to the items stored in the schedule table
 		return false;
 	}
 	
 	@SuppressWarnings("unused")
 	private void updateScheduledPrescription() {
 		Log.d(LOG_TAG, "Updating the schedule information for the prescription...");
 		// TODO: implement this function instead of simply removing the items from the schedule table
 	}
 	
 	/**
 	 * This method will do the following:
 	 *  - Search for all prescriptions that need to be scheduled.
 	 *  - Check to see if the prescription schedule has changed since last run.
 	 *  - Update and reschedule any changed prescriptions.
 	 *  - Schedule any new prescriptions that have been added to the database.
 	 *  
 	 *  This method will also run when the prescription content uri has been changed 
 	 *  to check for any of the above cases. If this method becomes too processor or 
 	 *  memory intensive, additional optimizations will have to be done.
 	 *  
 	 */
 	private void schedulePrescriptions() {
 		/* 1. Get a cursor to all of the prescriptions that have been 'scheduled' */
 		
 		// Create a where clause to only get the prescriptions that are scheduled
 		String prescriptionWhereClause = StorageProvider.PrescriptionColumns.PRESCRIPTION_SCHEDULED + "=" + "'" + Prescription.SCHEDULED + "'";
 		
 		// We only care about the prescription id fields for this query.
 		String[] prescriptionProjection = { StorageProvider.PrescriptionColumns._ID };
 		
 		Log.d(LOG_TAG, "Getting a cursor on the scheduled prescriptions...");
 		
 		// Get all of the prescriptions that are scheduled
 		Cursor prescriptionCursor = this.getApplicationContext().getContentResolver().query(StorageProvider.PrescriptionColumns.CONTENT_URI, prescriptionProjection, prescriptionWhereClause, null, null);
 		
 		if ( prescriptionCursor == null || !prescriptionCursor.moveToFirst()) {
 			// There are no prescriptions to schedule, we are done here.
 			Log.i(LOG_TAG, "No prescriptions to schedule!");
 		} else {
 			
 			/* 2. Run the list of prescription ids against the foreign key prescription ids that have been placed in the schedule table */
 			
 			Log.d(LOG_TAG, "Getting ready to look for prescription ids in the schedule table...");
 			
 			do {
 				// Get the prescription id
 				int prescriptionId = prescriptionCursor.getInt(prescriptionCursor.getColumnIndex(StorageProvider.PrescriptionColumns._ID));
 				
 				Log.d(LOG_TAG,"Looking for prescription ID: " + prescriptionId );
 				
 				// The where clause for the schedule query
 				String scheduleWhereClause = StorageProvider.ScheduleColumns.SCHEDULES_PRESCRIPTION + "=" + "'" + prescriptionId + "'";
 				
 				Cursor specificScheduleCursor = this.getApplicationContext().getContentResolver().query(StorageProvider.ScheduleColumns.CONTENT_URI, null, scheduleWhereClause, null, null);
 				
 				if ( specificScheduleCursor != null && specificScheduleCursor.moveToFirst()) {
 					// There are entries for this prescription in the schedule table.
 					Log.d(LOG_TAG, "There are entries in the schedule table for prescription ID: " + prescriptionId );
 					
 					/* 3. Remove any schedules for this prescription. */
 					int countRemoved = this.getApplicationContext().getContentResolver().delete(StorageProvider.ScheduleColumns.CONTENT_URI, scheduleWhereClause, null);
 					
 					Log.d(LOG_TAG, "Removed " + countRemoved + " rows from the schedule table..." );	
 				}
 				
 				/* 4. Schedule the prescriptions */
 				// There are no entries for this prescription in the schedule table.
 				Log.d(LOG_TAG, "There are no entries for prescription ID: " + prescriptionId + " in the schedule table.");
 				// Create a new schedule for this prescription.
 				this.scheduleNewPrescription(prescriptionId);
 				
 				// Close the specific query for the schedules.
 				specificScheduleCursor.close();
 				
 			} while (prescriptionCursor.moveToNext());
 			
 		}
 		
 		// Close the query for all the prescription ids
 		prescriptionCursor.close();
 
 		/* 5. Listen for any changes that may occur to the prescription URI table */
 		this.getApplicationContext().getContentResolver().registerContentObserver(StorageProvider.DrugColumns.CONTENT_URI, true, new PrescriptionContentObserver(new Handler()));
 	}
 	
 	private void displayAlarmNotification(Intent intent) {
 		// Get an instance of the SBNM
 		StatusBarNotificationManager sbnm = new StatusBarNotificationManager(this.getApplicationContext());
 		
 		// Get the schedule id from the intent.
 		int scheduleId = intent.getIntExtra(PENDING_INTENT_SCHEDULE_ID, -1);
 		
 		if( scheduleId == -1) {
 			// Something didn't get bundled correctly, how do we handle this case?
 			return;
 		}
 		
 		// Get the schedule that triggered
 		Uri scheduleUri = ContentUris.withAppendedId(StorageProvider.ScheduleColumns.CONTENT_URI, scheduleId);
 		String[] scheduleProjection = { StorageProvider.ScheduleColumns._ID, StorageProvider.ScheduleColumns.SCHEDULES_PRESCRIPTION };
 		Cursor scheduleCursor = this.getApplicationContext().getContentResolver().query(scheduleUri, scheduleProjection, null, null, null);
 		scheduleCursor.moveToFirst();
 		
 		// Get the prescription associated with this alarm
 		Uri prescriptionUri = ContentUris.withAppendedId(StorageProvider.PrescriptionColumns.CONTENT_URI, scheduleCursor.getInt(scheduleCursor.getColumnIndex(StorageProvider.ScheduleColumns.SCHEDULES_PRESCRIPTION)));
 		Cursor prescriptionCursor = this.getApplicationContext().getContentResolver().query(prescriptionUri, null, null, null, null);
 		prescriptionCursor.moveToFirst();
 		
 		Log.d(LOG_TAG, "Notification for prescription " + scheduleCursor.getInt(scheduleCursor.getColumnIndex(StorageProvider.ScheduleColumns.SCHEDULES_PRESCRIPTION)) + " in schedule " + scheduleId);
 		
 		// Parse that cursor
 		Prescription prescription = Prescription.fromCursor(prescriptionCursor, this.getApplicationContext());
 		
 		// Create and display the notification
 		String topText = prescription.getDrug().getBrandName();
 		String titleText = "Please take X " + prescription.getDrug().getBrandName();
 		String bodyText = "Body text.";
 		
 		StatusBarNotification not = new StatusBarNotification(this, prescription, topText, titleText, bodyText);
 		
 		// Get the index of this notification
 		int x = sbnm.add(not);
 		try {
 			// Send the message to the NM to display
 			sbnm.sendMessage(x);
 		}
 		catch(Exception e) {
			Log.d(LOG_TAG, "THERE WAS AN EXCEPTION!!");
 		}
 		
 		// Close the cursors
 		scheduleCursor.close();
 		prescriptionCursor.close();
 	}
 	
 	/**
 	 * Handles the given intent by performing a class specific action.
 	 * @param intent the intent to handle
 	 */
 	private void handleCommand(Intent intent) {
 		Log.d(LOG_TAG, "handleCommand has been called...");
 		Log.d(LOG_TAG, "Intent Info: " + intent.getAction());
 		
 		// Get the string action from the intent object.
 		String intentAction = intent.getAction();
 		
 		if ( this.isActionAlarmTriggerIntent(intentAction) ) {
 			// An alarm has completed!
 			displayAlarmNotification(intent);
 		} else if ( intentAction.equals(ACTION_START_SERVICE) || intentAction.equals(ACTION_ALARM_SCHEDULE) || intent.getAction().equals(ACTION_ALARM_RESCHEDULE) ) {
 			// Schedule alarms...
 			this.schedulePrescriptions();
 		} else {
 			// How did we get this action? What is this!?
 			Log.d(LOG_TAG, "handleCommand() - Unknown intent: " + intentAction);
 		}			
 	}
 	
 	class PrescriptionContentObserver extends ContentObserver {
 
 		public PrescriptionContentObserver(Handler handler) {
 			super(handler);	
 		}
 
 		@Override
 		public void onChange(boolean selfChange) {
 			Log.d(LOG_TAG, "ScheduleContentObserver onChange() called...");
 			Log.d(LOG_TAG, "Updating schedules.");
 			MainService.this.schedulePrescriptions();
 		}
 	}
 }
