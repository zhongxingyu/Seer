 package de.da_sense.moses.client.com;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
 import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;
 import de.da_sense.moses.client.util.Log;
 
 /**
  * Main GCM receiver and message handler
  * @author Wladimir Schmidt
  *
  */
 public class C2DMReceiver extends BroadcastReceiver {
 
 	private static final String GCM_PUSH_MESSAGTYPE_USERSTUDY = "USERSTUDY";
 	private static final String GCM_PUSH_MESSAGTYPE_UPDATE = "UPDATE";
 	private static final String GCM_PUSH_MESSAGTYPE_QUEST = "QUEST"; 
 	public static final String EXTRAFIELD_USERSTUDY_NOTIFICATION = "UserStudyNotification";
 	private static final String GCM_MESSAGETYPE_FIELD = "MESSAGE";
 	private static final String GCM_USERSTUDY_APKID_FIELD = "APKID";
 	private static final String GCM_UPDATE_APKID_FIELD = "APKID";
 	private static final String GCM_QUEST_APKID_FIELD = "APKID";
 	public static final String EXTRAFIELD_C2DM_ID = "c2dmId";
 
 	private static final String LOG_TAG = C2DMReceiver.class.getName();
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
 			handleNotifications(context, intent);
 		}
 	}
 
 	private static void handleNotifications(Context context, Intent intent) {
 		try {
 			JSONObject message = new JSONObject(intent.getExtras().getString("message"));
 			Log.v(LOG_TAG, "handleNotifications() message="+message);
 			String messagetype = message.getString(GCM_MESSAGETYPE_FIELD);
 			if (messagetype.equals(GCM_PUSH_MESSAGTYPE_USERSTUDY)) {
 				String apkidString = message.getString(GCM_USERSTUDY_APKID_FIELD);
 				Log.i(LOG_TAG, "User study notification received!! APK ID = "+ apkidString);
 				UserstudyNotificationManager.userStudyNotificationArrived(context, apkidString);
 			} else if (messagetype.equals(GCM_PUSH_MESSAGTYPE_UPDATE)) {
 				String apkidString = message.getString(GCM_UPDATE_APKID_FIELD);
 				Log.i(LOG_TAG, "update notification received!! APK ID = "
 						+ apkidString);
 				Log.i(LOG_TAG, "update incoming: " + apkidString);
 				InstalledExternalApplicationsManager.updateArrived(apkidString);
 			} else
 			if (messagetype.equals(GCM_PUSH_MESSAGTYPE_QUEST)) {
 				String apkidString = message.getString(GCM_QUEST_APKID_FIELD);
 				Log.i(LOG_TAG, "update incoming: " + apkidString);
 				if(InstalledExternalApplicationsManager.getInstance().getAppForId(apkidString) != null){
 					// show a notification to user only if the app is still installed
 					// the data on the server may be inconsistent
					UserstudyNotificationManager.questionnaireNotificationArrived(context, apkidString);
 					InstalledExternalApplicationsManager.getInstance().getAppForId(apkidString).setEndDateReached(true);
 				}
 				 
 
 			} else {
 				Log.w("MoSeS.C2DM", "Unhandled C2DM Message from type: "
 						+ messagetype);
 			}
 		} catch (JSONException e) {
 			Log.e(LOG_TAG, e.getMessage());
 		}
 
 	}
 }
