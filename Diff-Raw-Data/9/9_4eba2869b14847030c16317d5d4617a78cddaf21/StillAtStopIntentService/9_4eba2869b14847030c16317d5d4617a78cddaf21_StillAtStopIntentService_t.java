 package ch.unige.tpgcrowd.google.activity;
 
 import android.app.IntentService;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.widget.RemoteViews;
 import ch.unige.tpgcrowd.R;
 import ch.unige.tpgcrowd.google.geofence.GeofenceHandler;
 import ch.unige.tpgcrowd.google.geofence.StopGeofence;
 import ch.unige.tpgcrowd.google.geofence.StopGeofenceStore;
 import ch.unige.tpgcrowd.google.geofence.StopTransitionsIntentService;
 import ch.unige.tpgcrowd.ui.StopNotificationView;
 import ch.unige.tpgcrowd.util.ColorStore;
 
 import com.google.android.gms.location.ActivityRecognitionResult;
 import com.google.android.gms.location.DetectedActivity;
 import com.google.android.gms.location.Geofence;
 
 public class StillAtStopIntentService extends IntentService {
 	private static final String NAME = "StillAtStopIntentService";
 	public static final int TPG_STOP_NOTIFICATION = 1234;
 	
 	// The SharedPreferences object in which activity recognition timestamp is stored
 	private final SharedPreferences mPrefs;
 	// The name of the SharedPreferences
 	private static final String SHARED_PREFERENCES =
 			"SharedPreferences";
 	
 	//Max delay - 10 minutes
 	private static final long MAX_ACTIVITY_RECOGNITION_DELAY = 10 * 60 * 1000;
 	
 	public static PendingIntent getAtStopStill(final Context context) {
 		final Intent intent = new Intent(
 				context, StillAtStopIntentService.class);
 		return PendingIntent.getService(context, 0, intent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 	}
 
 	public StillAtStopIntentService() {
 		
 		super(NAME);
		mPrefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
 	}
 
 	@Override
 	protected void onHandleIntent(final Intent intent) {
 		// If the incoming intent contains an update
 		if (ActivityRecognitionResult.hasResult(intent)) {
 			
 			//Get activity recognition start time from SharedPrefs
 			Long startTime = mPrefs.getLong(ActivityRecognitionHandler.ACTIVITY_RECOGNITION_START_TIME, -1);
 			if (System.currentTimeMillis() - startTime > MAX_ACTIVITY_RECOGNITION_DELAY) {
 				//If elapsed time > max delay, stop the activity recognition service
 				ActivityRecognitionHandler.stopActivityRecognition(getApplicationContext(), getAtStopStill(getApplicationContext()));
 			}
 			
 			// Get the update
 			final ActivityRecognitionResult result =
 					ActivityRecognitionResult.extractResult(intent);
 			// Get the most probable activity
 			final DetectedActivity mostProbableActivity =
 					result.getMostProbableActivity();
 			/*
 			 * Get the probability that this activity is the
 			 * the user's actual activity
 			 */
 			final int confidence = mostProbableActivity.getConfidence();
 			/*
 			 * Get an integer describing the type of activity
 			 */
 			final int activityType = mostProbableActivity.getType();
 			//String activityName = getNameFromType(activityType);
 			/*
 			 * At this point, you have retrieved all the information
 			 * for the current update. You can display this
 			 * information to the user in a notification, or
 			 * send it to an Activity or Service in a broadcast
 			 * Intent.
 			 */
 
 			if (confidence > 50) {
 				if (activityType == DetectedActivity.STILL) {
 					Log.i(NAME, "Detected still with confidence" + confidence + ", start stop dialog...");
 
 					ActivityRecognitionHandler.stopActivityRecognition(getApplicationContext(), 
 							getAtStopStill(getApplicationContext()));
 
 					final StopGeofence geofence = StopGeofenceStore.getGeofence(getApplicationContext(), StopGeofence.STOP_GEOFENCE_ID);
 
 					final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
 
 					//Small view
 					RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notification_small_at_stop);
 					rv.setInt(R.id.lineIcon, "setBackgroundColor", ColorStore.getColor(getApplicationContext(), geofence.getLineCode()));
 
 					rv.setTextViewText(R.id.lineIcon,geofence.getLineCode());
 					rv.setTextViewText(R.id.textDirection,geofence.getDestinationName());
 
 					notificationBuilder.setContent(rv);
 
 					notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
 
 					/* Creates an explicit intent for an Activity in your app */
 					Intent resultIntent = new Intent(getApplicationContext(), StopNotificationView.class);
 					resultIntent.putExtra("STOP_GEOFENCE_KEY", geofence);
 
 					resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 					/* Adds the Intent that starts the Activity to the top of the stack */
 					PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 
 					notificationBuilder.setContentIntent(resultPendingIntent);
 
 					//Add buttons
 					//				Intent infoIntent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
 					//				infoIntent.setAction("ACTION_SHOW_INFO");
 					//				PendingIntent piInfo = PendingIntent.getService(getActivity().getApplicationContext(), 0, infoIntent, 0);
 					//
 					//				Intent wrongStopIntent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
 					//				wrongStopIntent.setAction("ACTION_CHANGE_STOP");
 					//				PendingIntent piWrongStop = PendingIntent.getService(getActivity().getApplicationContext(), 0, wrongStopIntent, 0);
 					//				
 					//				notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Next departure : 3min").setBigContentTitle(sg.getLineCode()));
 					//				notificationBuilder.addAction(android.R.drawable.ic_menu_info_details,getString(R.string.more_info), piInfo);
 					//				notificationBuilder.addAction(android.R.drawable.ic_delete,getString(R.string.wrong_stop), piWrongStop);
 
 					final NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
 					notificationManager.notify(TPG_STOP_NOTIFICATION, notificationBuilder.build());
 
 
 
 
 					geofence.setTransitionType(Geofence.GEOFENCE_TRANSITION_EXIT);
 					StopGeofenceStore.setGeofence(getApplicationContext(), StopGeofence.STOP_GEOFENCE_ID, geofence);
 					GeofenceHandler.addGeofences(getApplicationContext(), new String[] {StopGeofence.STOP_GEOFENCE_ID}, 
 							StopTransitionsIntentService.getTransitionPendingIntent(getApplicationContext()));
 				}
 				else {
 					Log.i(NAME, "Detected other activity...");
 				}
 			}
 			else {
 				Log.i(NAME, "Not enough confidence...");
 			}
 
 		}
 		else {
 			Log.i(NAME, "No result for current activity..");
 		}
 	}
 }
