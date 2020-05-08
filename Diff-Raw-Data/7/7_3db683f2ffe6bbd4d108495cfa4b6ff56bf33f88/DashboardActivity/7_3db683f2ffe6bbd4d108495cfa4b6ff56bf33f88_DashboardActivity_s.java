 /*******************************************************************************
  * Copyright (c) 2011 Andreas Storlien and Anders Kristiansen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Andreas Storlien and Anders Kristiansen - initial API and implementation
  ******************************************************************************/
 package com.bjorsond.android.timeline;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.jivesoftware.smack.util.collections.ResettableIterator;
 
 import android.accounts.Account;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.bjorsond.android.timeline.database.DatabaseHelper;
 import com.bjorsond.android.timeline.database.TimelineDatabaseHelper;
 import com.bjorsond.android.timeline.database.UserGroupDatabaseHelper;
 import com.bjorsond.android.timeline.database.contentmanagers.ContentAdder;
 import com.bjorsond.android.timeline.database.contentmanagers.ContentLoader;
 import com.bjorsond.android.timeline.database.contentmanagers.UserGroupManager;
 import com.bjorsond.android.timeline.dialogs.NewTimelineDialog;
 import com.bjorsond.android.timeline.dialogs.TimelineBrowserDialog;
 import com.bjorsond.android.timeline.models.Experience;
 import com.bjorsond.android.timeline.models.Experiences;
 import com.bjorsond.android.timeline.models.User;
 import com.bjorsond.android.timeline.reflectionspace.ReflectionSpaceHandler;
 import com.bjorsond.android.timeline.sync.GoogleAppEngineHandler;
 import com.bjorsond.android.timeline.sync.UserAndGroupServiceHandler;
 import com.bjorsond.android.timeline.utilities.Constants;
 import com.bjorsond.android.timeline.utilities.MyLocation;
 import com.bjorsond.android.timeline.utilities.Utilities;
 import com.bjorsond.android.timeline.R;
 import com.swarmconnect.Swarm;
 import com.swarmconnect.SwarmAchievement;
 import com.swarmconnect.SwarmActivity;
 import com.swarmconnect.SwarmLeaderboard;
 
 
 /**
  * The starting activity for the application.
  * The application is mainly a collection of button listeners for the different menu buttons.
  * 
  * Implemented buttons:
  * -New timeline
  * -My private timelines({@link Experience} with shared=false)
  * -My groups
  * -My shared timelines({@link Experience} with shared=true)
  * -My tags
  * -Synchronize(collects all shared experiences and sends to a server, 
  * as well as downloading all of the users shared experiences from the server)
  * 
  * @author andekr
  *
  */
 public class DashboardActivity extends SwarmActivity implements ProgressDialogActivity {
 
 	private ImageButton newTimeLineButton;
 	private ImageButton browseMyTimelinesButton;
 	private ImageButton browseSharedTimelinesButton;
 	private ImageButton tagsButton;
 	private ImageButton myGroupsButton;
 	private ImageButton syncronizeButton;
 	private ImageButton spacesButton;
 	private ImageButton achievementsButton;
 	private ImageButton friendsButton;
 	private ImageButton messagesButton;
 	private ImageButton leaderboardButton;
 	private ImageButton profileButton;
 	private TextView lastSyncedTextView;
 	private Intent timelineIntent;
 	private Intent myGroupsIntent;
 	private Intent tagsIntent;
 	private Intent spacesIntent;
 	private Intent swarmIntent, profileIntent;
 	private Intent achievementsIntent;
 	private ContentAdder contentAdder;
 	private ContentLoader contentLoader;
 	private Account creator;
 	private User user;
 	Runnable syncThread, addGroupToServerThread, checkUserRunnable;
 	private long lastSynced=0;
 	boolean registered=false;
 	private UserGroupDatabaseHelper userGroupDatabaseHelper;
 	private UserGroupManager uGManager;
 	private ProgressDialog progressDialog;
 	private TimelineDatabaseHelper timelineDatabaseHelper;
 	
 	// ADDING COUNTERS FOR ACHIEVEMENT UNLOCKING
 	private static int noteCounter, audioCounter, videoCounter, pictureCounter, moodCounter, reflectionCounter = 0;
 	public static final String PREFS_NAME = "MyPreferencesFile";
 	// VARIABLES FOR POINT SYSTEM
 	private static int pointsCounter = 0;
 	// VARIABLE FOR CONSECUTIVE REFLECTION NOTE BONUS
 	private static Calendar lastReflectionDate = null;
 	// CONSEQUTIVE REF NOTE BONUS
 	private static int consRefNotes = 0;
 	// int[] TO SAVE BONUS POINTS "SCHEDULE"
 	private static int[] bonusPoints = new int[] {0, 0, 0, 0, 0};
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dashboard);
 		MyLocation.getInstance(this);//Starts the LocationManager right away so a location will be available as soon as possible
 		
 		// Restoring preferences - Achievement counters
 		restorePreferences();
 		
 		//Activating swarm, and ID'ing our swarm application
 		Swarm.init(this, 4651, "6ef1c4f59752007d40bd3d8828f789f2");
 		Swarm.setActive(this);
 		
 		creator = Utilities.getUserAccount(this);
 		user = new User(creator.name);
 		
 		//Initializes the content managers
 		setupHelpers();
 		
 		//uGManager.addUserToUserDatabase(user);
 		
 		setupIntents();
 
 		progressDialog = new ProgressDialog(this);
 		
 		//Check if user is registered
 //		checkUserRunnable = new Runnable() {
 //			public void run() {
 //				checkIfUserIsRegisteredOnServer();
 //			}
 //		};
 		
 		//Checks for Internet connection
 //		if(Utilities.isConnectedToInternet(this)){
 //			Thread checkUserThread = new Thread(checkUserRunnable, "checkUserThread");
 //			checkUserThread.start();
 //			progressDialog = ProgressDialog.show(DashboardActivity.this,    
 //		              "", "", true);
 //		}else{
 //			Toast.makeText(this, R.string.No_connection_toast, Toast.LENGTH_LONG).show();
 //		}
 //		
 //		try {
 //			lastSynced = getLastSynced();
 //		} catch (Exception e) {
 //			Log.e(this.getClass().getSimpleName(), getString(R.string.Could_not_sync));
 //		}
 		
 		setupViews();
 		
 		/*
 		 * If the application is started with a SEND- or share Intent, change the Intent to add to a timeline
 		 * This is for cases where the users clicks "share" in another application and selects "Timeline" as application
 		 * to share with
 		 */
 		if (getIntent().getAction().equals(Intent.ACTION_SEND)
 				|| getIntent().getAction().equals("share")) {
 			timelineIntent = getIntent();
 			timelineIntent.setAction(Constants.INTENT_ACTION_ADD_TO_TIMELINE);
 			timelineIntent.setClass(this, TimelineActivity.class); //Changes the class to start
 			browseAllTimelines(Constants.SHARED_ALL);
 		}
 		
 		//Prepares the sync thread
 // 		syncThread = new Runnable() {
 //			public void run() {
 //				syncTimelines();
 //			}
 //		};
 	}
 
 	//TODO Here is checkReflectionDate()
 	/**
 	 * This method checks when/if lastReflectionDate makes it possible to get consecutive bonus.
 	 * It returns an integer, which specifies how many days until the consecutive bonus is lost. 
 	 * -1 means that consecutive bonus is impossible at this time.
 	 */
 	public static int checkReflectionDate() {
 		Calendar c = Calendar.getInstance();
 		c.setTimeInMillis(System.currentTimeMillis());
 //		if (
 //			(lastReflectionDate.get(Calendar.WEEK_OF_YEAR) == c.get(Calendar.WEEK_OF_YEAR)) && //NULL POINTER P DENNE LINJA med feilmelding ResultInfo{who=null, request=10, result=-1, data=intent}
 //			(lastReflectionDate.get(Calendar.DAY_OF_WEEK) == (c.get(Calendar.DAY_OF_WEEK)-1)) &&
 //			(c.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY || 
 //				c.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY || c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
 //			) return 0;
 //	
 //		else if (
 //				(lastReflectionDate.get(Calendar.WEEK_OF_YEAR) == (c.get(Calendar.WEEK_OF_YEAR)-1)) && 
 //				(lastReflectionDate.get(Calendar.DAY_OF_WEEK) == (c.get(Calendar.DAY_OF_WEEK)+4)) &&
 //				(c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) &&
 //				(lastReflectionDate.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)			
 //				) return 0;
 //			 
 //		else if (
 //				(lastReflectionDate.get(Calendar.WEEK_OF_YEAR) == c.get(Calendar.WEEK_OF_YEAR)) && 
 //				(lastReflectionDate.get(Calendar.DAY_OF_WEEK) == (c.get(Calendar.DAY_OF_WEEK))) &&
 //				((c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) && (lastReflectionDate.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) ||
 //						(c.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) && (lastReflectionDate.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) ||
 //						(c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) && (lastReflectionDate.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) ||
 //						(c.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) && (lastReflectionDate.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY))
 //				) return 1;
 //		
 //		
 //		else if (
 //				(lastReflectionDate.get(Calendar.WEEK_OF_YEAR) == c.get(Calendar.WEEK_OF_YEAR)) && 
 //				(lastReflectionDate.get(Calendar.DAY_OF_WEEK) == (c.get(Calendar.DAY_OF_WEEK))) &&
 //				((c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) && (lastReflectionDate.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY))
 //				) return 3;
 		if (true) return 0;
 		
 		
 			 
 //		if ((c.get(Calendar.DAY_OF_WEEK) == (Calendar.FRIDAY))) return 3;
 //		else if ((c.get(Calendar.DAY_OF_WEEK) == (Calendar.SATURDAY))) return 2;
 //		else if ((c.get(Calendar.DAY_OF_WEEK) == (Calendar.SUNDAY)) || 
 //				(c.get(Calendar.DAY_OF_WEEK) == (Calendar.MONDAY)) || 
 //				(c.get(Calendar.DAY_OF_WEEK) == (Calendar.TUESDAY)) || 
 //				(c.get(Calendar.DAY_OF_WEEK) == (Calendar.WEDNESDAY)) || 
 //				(c.get(Calendar.DAY_OF_WEEK) == (Calendar.THURSDAY))
 //				) return 1;
 		else return -1;
 	}	
 	
 	/*
 	 *  It returns an integer, which specifies if it is in the weekend, the next work day, or "out of bounds" for consecutive bonus.
 	 * 0 = out of bounds
 	 * 1 = weekend
 	 * 2 = next work day
 	 */
 //				Calendar c = Calendar.getInstance();
 //		if ((c.get(Calendar.DAY_OF_WEEK) == (Calendar.SATURDAY))||(c.get(Calendar.DAY_OF_WEEK) == (Calendar.SUNDAY))){
 //			return 1;
 //		}
 //		else if (
 //				(c.get(Calendar.DAY_OF_WEEK) == (Calendar.MONDAY))&&
 //				(lastReflectionDate.get(Calendar.DAY_OF_MONTH) == (c.get(Calendar.DAY_OF_MONTH)-3))&&
 //				(lastReflectionDate.get(Calendar.MONTH)) == (c.get(Calendar.MONTH))
 //				){
 //			return 2;
 //		}
 //		else if (
 //				(lastReflectionDate.get(Calendar.DAY_OF_MONTH) == (c.get(Calendar.DAY_OF_MONTH)-1))&&
 //				(lastReflectionDate.get(Calendar.MONTH)) == (c.get(Calendar.MONTH))
 //				){
 //			return 2;
 //		}
 //		else return 0;
 //	}
 	
 //	/**
 //	 * This method sets the notification to pop up
 //	 */
 //	public void createScheduledNotification(int days){
 //	
 //		int hour = -1;
 //		
 //		// Get new calendar object and set the date to now	
 //		Calendar calendar = Calendar.getInstance();
 //		calendar.setTimeInMillis(System.currentTimeMillis());
 //		
 //		//setting current day, hour, minute
 //		hour = calendar.get(Calendar.HOUR_OF_DAY);
 //		
 //		// Setting time for alarm/notification
 //		calendar.add(Calendar.DAY_OF_MONTH, days);
 //		calendar.set(Calendar.MINUTE, 0);
 //		calendar.set(Calendar.SECOND, 0);
 //		if (hour < 12 && hour != -1) calendar.set(Calendar.HOUR_OF_DAY, 12);
 //		else if (hour < 14 && 12 >= hour && hour != -1) calendar.set(Calendar.HOUR_OF_DAY, 14);
 //		else if (hour < 16 && 14 >= hour && hour != -1) calendar.set(Calendar.HOUR_OF_DAY, 16);
 //		else if (hour < 18 && 16 >= hour && hour != -1) calendar.set(Calendar.HOUR_OF_DAY, 18);
 //		else calendar.add(Calendar.MINUTE, 3);
 //	
 //		// Retrieve alarm manager from the system
 //		AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(getBaseContext().ALARM_SERVICE);
 //	
 //		// Every scheduled intent needs a different ID, else it is just executed once
 //		int id = (int) System.currentTimeMillis();
 //	 
 //		// Prepare the intent which should be launched at the date
 //		Intent intent = new Intent(this, TimeAlarm.class);
 //	
 //		// Prepare the pending intent
 //		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 //	
 //		// Register the alert in the system. You have the option to define if the device has to wake up on the alert or not
 //		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
 //	 }
 
 	
 	/**
 	 * Level calculation method
 	 * Returns an array with level and remaining points, and points needed for current level.
 	 * slot 0 = level
 	 * slot 1 = points
 	 * slot 2 = current level points needed
 	 */
 	/*
 	* METHOD FOR LEVEL CALCULATION --- START
 	*/
 	//TODO Here is getLevelAndPoints()
 	public static int[] getLevelAndPoints(){
 		int temp = pointsCounter;
 		int level = 0;
 		int levelPoints = 100;
 		int[] array = new int[] {0, 0, 0};
 		for (int i = 0; i < 7; i++) {
 			if ((temp - Constants.LEVEL[i]) > 0) {
 				temp = temp - Constants.LEVEL[i];
 				level++;
 				levelPoints = Constants.LEVEL[i+1];
 			}
 		}
 		array[0] = level;  		//GIVES THE LEVEL OF THE USER
 		array[1] = temp;		//GIVES THE REMAINING POINTS OF THE USER
 		array[2] = levelPoints;	//GIVES THE REMAINING POINTS OF THE USER
 		return array;
 	}
 	
 	
 	/*
 	* METHOD FOR LEVEL CALCULATION --- END
 	*/
 	
 	//TODO GETTERS AND SETTERS
 	/*
 	* COUNTER GETTERS, SETTERS AND ADDERS -- START
 	*/
 	//    NOTE
 	public static void addNoteCounter() {
 		noteCounter++;
 	}
 	
 	public void setNoteCounter(int number) {
 		noteCounter = number;
 	}
 	
 	public static int getNoteCounter() {
 		return noteCounter;
 	}
 	
 	public static void addReflectionCounter() {
 		reflectionCounter++;
 	}
 	
 	public void setReflectionCounter(int number) {
 		reflectionCounter = number;
 	}
 	
 	public static int getReflectionCounter() {
 		return reflectionCounter;
 	}
 	//  AUDIO
 	public static void addAudioCounter() {
 		audioCounter++;
 	}
 	
 	public void setAudioCounter(int number) {
 		audioCounter = number;
 	}
 	
 	public static int getAudioCounter() {
 		return audioCounter;
 	}
 	//  VIDEO
 	public static void addVideoCounter() {
 		videoCounter++;
 	}
 	
 	public void setVideoCounter(int number) {
 		videoCounter = number;
 	}
 	
 	public static int getVideoCounter() {
 		return videoCounter;
 	}
 	//  PICTURE
 	public static void addPictureCounter() {
 		pictureCounter++;
 	}
 	
 	public void setPictureCounter(int number) {
 		pictureCounter = number;
 	}
 	
 	public static int getPictureCounter() {
 		return pictureCounter;
 	}
 	
 	//  MOOD
 	public static void addMoodCounter() {
 		moodCounter++;
 	}
 	
 	public void setMoodCounter(int number) {
 		moodCounter = number;
 	}
 	
 	public static int getMoodCounter() {
 		return moodCounter;
 	}
 	//  POINTS
 	public static void addPointsCounter(int number) {
 		pointsCounter += number;
 		SwarmLeaderboard.submitScore(Constants.leaderboardID, pointsCounter);
 	}
 	
 	public void setPointsCounter(int number) {
 		pointsCounter = number;
 	}
 	
 	public static int getPointsCounter() {
 		return pointsCounter;
 	}
 	//  CONSEQUTIVE REF NOTE POINTS
 	public static void addConsRefNoteCounter() {
 		consRefNotes++;
 	}
 	
 	public static void setConsRefNoteCounter(int number) {
 		consRefNotes += number;
 	}
 	
 	public static void resetConsRefNoteCounter() {
 		consRefNotes = 0;
 	}
 	
 	public static int getConsRefNoteCounter() {
 		return consRefNotes;
 	}
 	//  LAST REF NOTE
 	public static void setLastRefDate(Calendar c) {
 		lastReflectionDate = c;
 	}
 	
 	public static Calendar getLastRefDate() {
 		return lastReflectionDate;
 	}
 	/*
 	* COUNTER GETTERS AND SETTERS  --  END
 	*/
 	
 	
 	/**
 	 * method that gets, sets and returns bonus points for 
 	 * notes, pictures, audio, video and mood 
 	 */
 	public static int getAndSetBonusPoints (int type){
 		int temp = -1;
 		for (int i = 0; i < bonusPoints.length; i++) {
 			if (bonusPoints[i] == type) temp = i;
 		}
 		if (temp == -1) {
 			simplePushRight(type);
 			return 0;
 		}
 		else if(temp != 0 && temp != -1) {
 			return pushRight(type);
 		}
 		
 		else return 0;
 	}
 		
 	private static int pushRight(int type){
 		int temp = -1; 
 		for (int i = 0; i < bonusPoints.length; i++) {
 			if (bonusPoints[i] == type) temp = i;
 		}
 		
 		for (int i = temp; i > 0; i--) {
 			bonusPoints[i] = bonusPoints[i-1];
 		}
 		bonusPoints[0] = type;
 		
 		return temp;
 	}
 	
 	private static void simplePushRight(int type){
 		for (int i = 3; i >= 0; i--) {
 			if (bonusPoints[i] != 0) bonusPoints[i+1] = bonusPoints[i];
 		}
 		bonusPoints[0] = type;
 	}
 	
 	/**
 	 * 
 	 * Checks if user is registered on the Timeline server.
 	 * If not, the user is registered, using the e-mail address from the primary
 	 * Google Account on the device.
 	 * 
 	 */
 	private void checkIfUserIsRegisteredOnServer() {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				progressDialog.setMessage(getString(R.string.Checking_user));
 			}
 		});
 		registered = GoogleAppEngineHandler.IsUserRegistered(user.getUserName());
 		//Register user if not registered
 		if(!registered){
 			GoogleAppEngineHandler.addUserToServer(user);
 			runOnUiThread(new Runnable() {
 				public void run() {
 					progressDialog.setMessage(getString(R.string.Not_registered));
 				}
 			});
 		}else{
 			runOnUiThread(new Runnable() {
 				public void run() {
 					Toast.makeText(DashboardActivity.this, R.string.Already_registered_toast, Toast.LENGTH_LONG).show();
 				}
 			});
 		}
 		progressDialog.dismiss();
 	}
 	
 	private void setLastSyncedTextView() {
 //		if(lastSynced!=0){
 //			String lastSyncedFormattedString = DateFormat.format
 //   		 ("dd MMMM yyyy "+DateFormat.HOUR_OF_DAY+":mm:ss", new Date(lastSynced)).toString();
 //			lastSyncedTextView.setText(getResources().getString(R.string.Last_synced_label).toString()+" "+lastSyncedFormattedString);
 //		}else{
 //			lastSyncedTextView.setText(getResources().getString(R.string.Last_synced_label).toString()+" Never");
 //		}
 	}
 
 	/**
 	 * Adds the new timeline to the database containing all the timelines.
 	 * 
 	 * 
 	 * @param experience The experience to add to database
 	 */
 	private void addNewTimelineToTimelineDatabase(Experience experience) {
 		contentAdder.addExperienceToTimelineContentProvider(experience);
 		TimelineBrowserDialog dialog = new TimelineBrowserDialog(this,
 				timelineIntent, 1); //TODO timeline achi : 1 was shared from method below
 	}
 
 
 	/**
 	 * Opens a dialog based on an integer constant defined in {@link Constants}.
 	 * 
 	 */
 	private void browseAllTimelines(int shared) {
 		TimelineBrowserDialog dialog = new TimelineBrowserDialog(this,
 				timelineIntent, shared);
 
 		if (dialog.getNumberOfTimelinesSaved() != 0) {
 			dialog.show();
 			SwarmAchievement.unlock(10983);
 		} else {
 			switch (shared) {
 			case Constants.SHARED_TRUE:
 				Toast.makeText(this,
 						R.string.No_shared_timelines_toast,
 						Toast.LENGTH_LONG).show();
 				break;
 			case Constants.SHARED_FALSE:
 				Toast.makeText(this,
 						R.string.No_private_timelines_toast,
 						Toast.LENGTH_LONG).show();
 				break;
 			default:
 				Toast.makeText(this,
 						R.string.No_timelines_toast,
 						Toast.LENGTH_SHORT).show();
 				break;
 			}
 
 		}
 	}
 	
 	/**
 	 * Synchronize shared timelines with database.
 	 * First all local objects are persisted on server, then all content for this user on the server is downloaded.
 	 * 
 	 * TODO: This should be refactored into a new class, and be optimized to help poor run time.
 	 * 
 	 */
 //	private void syncTimelines() {
 //		UserAndGroupServiceHandler ugHandler =  new UserAndGroupServiceHandler(this, this);
 //		ugHandler.downloadUsersAndGroups();
 //		ArrayList<Experience> sharedExperiences = contentLoader.LoadAllSharedExperiencesFromDatabase();
 //		for (Experience experience : sharedExperiences) {
 //			new DatabaseHelper(this, experience.getTitle()+".db");
 //			DatabaseHelper.getCurrentTimelineDatabase().beginTransaction();
 //			experience.setEvents(contentLoader.LoadAllEventsFromDatabase());
 //			DatabaseHelper.getCurrentTimelineDatabase().setTransactionSuccessful();
 //			DatabaseHelper.getCurrentTimelineDatabase().endTransaction();
 //			DatabaseHelper.getCurrentTimelineDatabase().close();
 //		}
 //		
 //		Experiences experiences = new Experiences(sharedExperiences);
 //		GoogleAppEngineHandler.persistTimelineObject(experiences);
 //		
 //		Experiences exps = GoogleAppEngineHandler.getAllSharedExperiences(user);
 //		if(exps!=null){
 //			for (Experience e : exps.getExperiences()) {
 //				e.setSharingGroupObject(uGManager.getGroupFromDatabase(e.getSharingGroup()));
 //				contentAdder.addExperienceToTimelineContentProvider(e);
 //				addNewTimelineToTimelineDatabase(e);
 //
 //			}
 //		}
 //		runOnUiThread(confirmSync);
 //	}
 	
 	/**
 	 * Stores the time of the last sync in a local file.
 	 * 
 	 * @param lastSyncedInMillis Time of last sync in milliseconds
 	 */
 	private void storeLastSynced(long lastSyncedInMillis){
 		String FILENAME = "lastSynced";
 		String lastSynced = String.valueOf(lastSyncedInMillis);
 		this.lastSynced = lastSyncedInMillis;
 //		Commented out for test in Italy
 //		setLastSyncedTextView();
 		
 		try {
 			FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
 //			fos.write(lastSynced.getBytes());
 			fos.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Gets the time of last sync.
 	 * 
 	 * 
 	 * Used in setting last synced TextView.
 	 * Other possible usages is to use this as input when syncing, so that only new content is synced.
 	 * 
 	 * 
 	 * @return Time of last sync in milliseconds
 	 */
 //	private long getLastSynced(){
 //		final String FILENAME = "lastSynced";
 //		int ch;
 //	    StringBuffer strContent = new StringBuffer("");
 //		
 //		try {
 //			FileInputStream fis = openFileInput(FILENAME);
 //			while( (ch = fis.read()) != -1)
 //		        strContent.append((char)ch);
 //			fis.close();
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //		return Long.valueOf(strContent.toString());
 //	}
 	
 	/**
 	 * Thread to notify user that timelines have been synced. Intended run on UI thread.
 	 * 
 	 */
     private Runnable confirmSync = new Runnable() {
         public void run(){
         	try {
         		storeLastSynced(new Date().getTime());
         		progressDialog.dismiss();
         		Toast.makeText(DashboardActivity.this, R.string.Synced_toast, Toast.LENGTH_SHORT).show();
 			} catch (Exception e) {
 			}
         }
       };
     
       private static boolean wasLastWorkDay(){
     	  Calendar c = Calendar.getInstance();
     	  
     	  if ( lastReflectionDate.get(Calendar.DAY_OF_WEEK) == c.get(Calendar.DAY_OF_WEEK)-1 &&
     			 (lastReflectionDate.get(Calendar.DAY_OF_WEEK) ==  Calendar.SUNDAY) ||
     			 (lastReflectionDate.get(Calendar.DAY_OF_WEEK) ==  Calendar.MONDAY) ||
     			 (lastReflectionDate.get(Calendar.DAY_OF_WEEK) ==  Calendar.TUESDAY) ||
     			 (lastReflectionDate.get(Calendar.DAY_OF_WEEK) ==  Calendar.WEDNESDAY) ||
     			 (lastReflectionDate.get(Calendar.DAY_OF_WEEK) ==  Calendar.THURSDAY)
     			  ) return true;
     	  
     	  else if (lastReflectionDate.get(Calendar.DAY_OF_WEEK) == c.get(Calendar.DAY_OF_WEEK)-3 &&
     			  (lastReflectionDate.get(Calendar.DAY_OF_WEEK) ==  Calendar.FRIDAY)
     			  ) return true;
     	  
     	  else if (lastReflectionDate.get(Calendar.DAY_OF_WEEK) == c.get(Calendar.DAY_OF_WEEK)-2 &&
     			  (lastReflectionDate.get(Calendar.DAY_OF_WEEK) ==  Calendar.SATURDAY)
     			  ) return true;
     	  
     	  else return false;
       }
       
       public static int checkAndSetRefNotePoints(){
     	  if (wasLastWorkDay()) {
     		  int i = Constants.ReflectionPoints+(getConsRefNoteCounter()*10);
     		  if (i == 0) {
     			  addPointsCounter(Constants.ReflectionPoints);
         		  addConsRefNoteCounter();
         		  return Constants.ReflectionPoints;
     		  }
     		  else {
     			  addPointsCounter(i);
     			  addConsRefNoteCounter();
     			  return i;
     		  }
     	  }
     	  else {
     		  addPointsCounter(Constants.ReflectionPoints);
     		  resetConsRefNoteCounter();
     		  return Constants.ReflectionPoints;
     	  }
       }
       
      //TODO restorePreferences()
       
      private void restorePreferences() {
     	// Restore preferences
          SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
          setNoteCounter(settings.getInt("noteCount", 0));
          setAudioCounter(settings.getInt("audioCount", 0));
          setVideoCounter(settings.getInt("videoCount", 0));
          setPictureCounter(settings.getInt("pictureCount", 0));
          setMoodCounter(settings.getInt("moodCount", 0));
          setReflectionCounter(settings.getInt("reflectionCount", 0));  //added for reflection note
          setPointsCounter(settings.getInt("pointsCount", 0));  //added for point system
          
          setConsRefNoteCounter(settings.getInt("consRef", 0));
          
          Calendar c = new GregorianCalendar();
          c.setTimeInMillis(settings.getLong("refDate", 0));
          setLastRefDate(c);
          
          bonusPoints[0] = settings.getInt("spot0", 0);
          bonusPoints[1] = settings.getInt("spot1", 0);
          bonusPoints[2] = settings.getInt("spot2", 0);
          bonusPoints[3] = settings.getInt("spot3", 0);
          bonusPoints[4] = settings.getInt("spot4", 0);
          
      }
       
           
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		closeDatabaseHelpers();
 	}
 	
 	@Override
 	public void finish() {
 		super.finish();
 		closeDatabaseHelpers();
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		Swarm.setInactive(this);
 		closeDatabaseHelpers();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Swarm.setActive(this);
 		setupDatabaseHelpers();
 	}
 	
 	@Override
 	protected void onStop() {
 		closeDatabaseHelpers();
 		super.onStop();
 		
 		//TODO shared pref putting
 		// We need an Editor object to make preference changes.
 	    // All objects are from android.context.Context
 	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 	    SharedPreferences.Editor editor = settings.edit();
 	    editor.putInt("noteCount", noteCounter);
 	    editor.putInt("reflectionCount", reflectionCounter);
 	    editor.putInt("videoCount", videoCounter);
 	    editor.putInt("audioCount", audioCounter);
 	    editor.putInt("pictureCount", pictureCounter);
 	    editor.putInt("moodCount", moodCounter);
 	    editor.putInt("reflectionCount", reflectionCounter);  //added to have reflection note counter
 	    editor.putInt("pointsCount", pointsCounter);  //added for points system
 	    
 	    editor.putInt("consRef", consRefNotes);
 
 	    editor.putLong("refDate", lastReflectionDate.getTimeInMillis());
 	    
 	    //bonus points for adding "more seldom" used input
 	    editor.putInt("spot0", bonusPoints[0]);
 	    editor.putInt("spot1", bonusPoints[1]);
 	    editor.putInt("spot2", bonusPoints[2]);
 	    editor.putInt("spot3", bonusPoints[3]);
 	    editor.putInt("spot4", bonusPoints[4]);
 	    // Commit the edits!
 	    editor.commit();
 	}
 	
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		timelineIntent = new Intent(this, TimelineActivity.class);
 		timelineIntent.setAction("NEW");
 	}
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.dashboard_menu, menu);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.SEND_EMAIL_WITH_CONTENT:
 			Runnable sendEmailRunnable = new Runnable() {
 				public void run() {
 					GoogleAppEngineHandler.sendEmailWithActivity(user);
 				}
 			};
 			Thread sendEmailThread = new Thread(sendEmailRunnable);
 			sendEmailThread.start();
 			return true;
 			
 			
		//GAMIFICATION: Swarm menu
 		case R.id.TUTORIAL:
	        startActivity(swarmIntent);
 			
 		default:
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	//TODO oneOfEach()
 	public static boolean onOfEach(){
 		if(noteCounter > 0 &&
 				audioCounter > 0 &&
 				pictureCounter > 0 &&
 				reflectionCounter > 0 && 
 				videoCounter > 0 && 
 				moodCounter > 0
 				){
 			return true;
 		}
 		else return false;
 	}
 	
 	//LISTENERS 
 	
 	private OnClickListener newTimeLineListener = new OnClickListener() {
 		public void onClick(View v) {
 				if(Utilities.isConnectedToInternet(getApplicationContext())) {
 					UserAndGroupServiceHandler ugHandler =  new UserAndGroupServiceHandler(DashboardActivity.this, DashboardActivity.this);
 					ugHandler.startDownloadUsersAndGroups();
 				}
 				else {
 					Toast.makeText(getApplicationContext(), R.string.Offline_toast, Toast.LENGTH_SHORT).show();
 					NewTimelineDialog newTimelineDialog = new NewTimelineDialog(DashboardActivity.this, null,timelineIntent);
 					newTimelineDialog.show();
 				}
 			
 		}
 	};
 
 	private OnClickListener browseTimeLineListener = new OnClickListener() {
 
 		public void onClick(View v) {
 			browseAllTimelines(Constants.SHARED_FALSE);
 		}
 	};
 	
 	private OnClickListener browseSharedTimeLinesListener = new OnClickListener() {
 
 		public void onClick(View v) {
 			browseAllTimelines(Constants.SHARED_TRUE);		
 		}
 
 	};
 	
 	private OnClickListener openMyGroupsListener = new OnClickListener() {
 		public void onClick(View v) {
 			if(Utilities.isConnectedToInternet(getApplicationContext())) {
 				startActivity(myGroupsIntent);
 				closeDatabaseHelpers();
 			}
 			else {
 				Toast.makeText(getApplicationContext(), R.string.Online_functionality_toast, Toast.LENGTH_SHORT).show();
 			}
 		}
 	};
 	
 	private OnClickListener tagsListener = new OnClickListener() {
 		public void onClick(View v) {
 			startActivity(tagsIntent);
 		}
 	};
 
 	
 	private OnClickListener syncListener = new OnClickListener() {
 		public void onClick(View v) {
 			if(Utilities.isConnectedToInternet(getApplicationContext())) {
 				progressDialog = ProgressDialog.show(DashboardActivity.this,    
 			              "", "", true);
 				progressDialog.setMessage(getString(R.string.Synchronizing));
 				Thread shareThread = new Thread(syncThread, "shareThread");
 				shareThread.start();
 			}
 			else {
 				Toast.makeText(getApplicationContext(), R.string.Online_functionality_toast, Toast.LENGTH_SHORT).show();
 			}
 		}
 	};
 	
 	
 	private OnClickListener viewProfileListener = new OnClickListener() {
 		public void onClick(View v) {
 			startActivity(profileIntent);
 		}
 	};
 	
 	private OnClickListener messagesListener = new OnClickListener() {
 		public void onClick(View v) {
 			Swarm.show(5);
 		}
 	};
 	
 	private OnClickListener achievementsListener = new OnClickListener() {
 		public void onClick(View v) {
 			SwarmAchievement.unlock(10839);
 			startActivity(achievementsIntent);
 //			Swarm.showAchievements();
 		}
 	};
 	
 	private OnClickListener leaderboardListener = new OnClickListener() {
 		public void onClick(View v) {
 			SwarmLeaderboard.showLeaderboard(Constants.leaderboardID);
 		}
 	};
 	
 	private OnClickListener friendsListener = new OnClickListener() {
 		public void onClick(View v) {
 			Swarm.show(3);
 		}
 	};
 	
 	private OnClickListener spacesListener = new OnClickListener() {
 		public void onClick(View v) {
 			startActivity(spacesIntent);
 		}
 	};
 	
 	
 	//SETUP HELPERS
 	
 	private void setupIntents() {
 		myGroupsIntent = new Intent(this, MyGroupsActivity.class);
 		myGroupsIntent.putExtra("ACCOUNT", creator);
 		
 		timelineIntent = new Intent(this, TimelineActivity.class);
 		timelineIntent.setAction(Constants.INTENT_ACTION_NEW_TIMELINE); //Default Intent action for TimelineActivity is to create/open a timeline.
 		tagsIntent = new Intent(this, MyTagsActivity.class);
 		profileIntent = new Intent(this, ProfileActivity.class);
 		spacesIntent = new Intent(this, ReflectionSpaceUserActivity.class);
 		achievementsIntent = new Intent(this, AchievementsScreen.class);
 		
 		swarmIntent = new Intent(this, MySwarmActivity.class);
 	}
 
 	private void setupHelpers() {
 		setupDatabaseHelpers();
 		contentAdder = new ContentAdder(getApplicationContext());
 		contentLoader = new ContentLoader(getApplicationContext());
 		uGManager = new UserGroupManager(getApplicationContext());
 	}
 
 	public void callBack() {
 		NewTimelineDialog newTimelineDialog = new NewTimelineDialog(DashboardActivity.this, null, timelineIntent);
 		newTimelineDialog.show();
 	}
 	
 	private void setupDatabaseHelpers() {
 		userGroupDatabaseHelper = new UserGroupDatabaseHelper(this, Constants.USER_GROUP_DATABASE_NAME);
 		timelineDatabaseHelper = new TimelineDatabaseHelper(this, Constants.ALL_TIMELINES_DATABASE_NAME);
 	}
 	
 	private void closeDatabaseHelpers() {
 		userGroupDatabaseHelper.close();
 		timelineDatabaseHelper.close();
 	}
 	
 	/**
 	 * Sets up the views by getting the views from layout XML and attaching listeners to buttons. 
 	 * 
 	 */
 	private void setupViews() {
 		
 		newTimeLineButton = (ImageButton) findViewById(R.id.dash_new_timeline);
 		newTimeLineButton.setOnClickListener(newTimeLineListener);
 		browseMyTimelinesButton = (ImageButton) findViewById(R.id.dash_my_timelines);
 		browseMyTimelinesButton.setOnClickListener(browseTimeLineListener);
 //		browseSharedTimelinesButton = (ImageButton) findViewById(R.id.dash_shared_timelines);
 //		browseSharedTimelinesButton.setOnClickListener(browseSharedTimeLinesListener);
 //		syncronizeButton = (ImageButton)findViewById(R.id.dash_sync);
 //		syncronizeButton.setOnClickListener(syncListener);
 //		myGroupsButton = (ImageButton) findViewById(R.id.dash_my_groups);
 //		myGroupsButton.setOnClickListener(openMyGroupsListener);
 		tagsButton = (ImageButton) findViewById(R.id.dash_my_tags);
 		tagsButton.setOnClickListener(tagsListener);
 		friendsButton = (ImageButton) findViewById(R.id.friends);
 		friendsButton.setOnClickListener(friendsListener);
 		messagesButton = (ImageButton) findViewById(R.id.messages);
 		messagesButton.setOnClickListener(messagesListener);
 		profileButton = (ImageButton) findViewById(R.id.profile);
 		profileButton.setOnClickListener(viewProfileListener);
 		achievementsButton = (ImageButton)findViewById(R.id.achivements);
 		achievementsButton.setOnClickListener(achievementsListener);
 		leaderboardButton = (ImageButton)findViewById(R.id.leaderboard);
 		leaderboardButton.setOnClickListener(leaderboardListener);
 		spacesButton = (ImageButton)findViewById(R.id.spaces_login_button);
 		spacesButton.setOnClickListener(spacesListener);
 //		lastSyncedTextView = (TextView)findViewById(R.id.DashLastSyncedTextView);
 //		Commented out due to test in Italy
 //		setLastSyncedTextView();	
 	}
 	
 	@Override
 	public void onBackPressed() {
 		this.finish();
 	}
 }
