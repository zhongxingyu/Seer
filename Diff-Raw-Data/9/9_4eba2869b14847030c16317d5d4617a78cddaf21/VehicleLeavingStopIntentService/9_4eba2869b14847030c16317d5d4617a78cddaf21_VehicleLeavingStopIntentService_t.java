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
 import ch.unige.tpgcrowd.google.geofence.StopGeofence;
 import ch.unige.tpgcrowd.google.geofence.StopGeofenceStore;
 import ch.unige.tpgcrowd.ui.VehicleNotificationView;
 import ch.unige.tpgcrowd.util.ColorStore;
 
 import com.google.android.gms.location.ActivityRecognitionResult;
 import com.google.android.gms.location.DetectedActivity;
 
 public class VehicleLeavingStopIntentService extends IntentService {
 	private static final String NAME = "VehicleLeavingStopIntentService";
 	public static final int TPG_NOTIFICATION = 1;
 
 	// The SharedPreferences object in which activity recognition timestamp is stored
 	private final SharedPreferences mPrefs;
 	// The name of the SharedPreferences
 	private static final String SHARED_PREFERENCES =
 			"SharedPreferences";
 	
 	//Max delay - 10 minutes
 	private static final long MAX_ACTIVITY_RECOGNITION_DELAY = 10 * 60 * 1000;
 	
 	public static PendingIntent getLeavingStopVehicle(final Context context) {
 		final Intent intent = new Intent(
 				context, VehicleLeavingStopIntentService.class);
 		return PendingIntent.getService(context, 0, intent,
 				PendingIntent.FLAG_UPDATE_CURRENT);
 	}
 
 	public VehicleLeavingStopIntentService() {
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
 				ActivityRecognitionHandler.stopActivityRecognition(getApplicationContext(), 
 						getLeavingStopVehicle(getApplicationContext()));
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
 				if (activityType == DetectedActivity.IN_VEHICLE) {
 					Log.i(NAME, "Detected in vehicle with confidence" + confidence + ", start in bus/tram dialog...");
 					
 					ActivityRecognitionHandler.stopActivityRecognition(getApplicationContext(), 
 							getLeavingStopVehicle(getApplicationContext()));
 					
 					final StopGeofence geofence = StopGeofenceStore.getGeofence(getApplicationContext(), StopGeofence.STOP_GEOFENCE_ID);
 					final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
 					//Small view
 					final RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notification_small_in_vehicle);
 					rv.setInt(R.id.lineIcon, "setBackgroundColor", ColorStore.getColor(getApplicationContext(), geofence.getLineCode()));
 
 					rv.setTextViewText(R.id.lineIcon, geofence.getLineCode());
 					rv.setTextViewText(R.id.textDirection, geofence.getDestinationName());
 
 					notificationBuilder.setContent(rv);
 
 					notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
 					
 					/* Creates an explicit intent for an Activity in your app */
 					final Intent resultIntent = new Intent(getApplicationContext(), VehicleNotificationView.class);
 					resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 					/* Adds the Intent that starts the Activity to the top of the stack */
 					final PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
 
 					notificationBuilder.setContentIntent(resultPendingIntent);
 					
 					final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 					notificationManager.notify(TPG_NOTIFICATION, notificationBuilder.build());
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
