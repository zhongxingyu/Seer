 package giulio.frasca.silencesched;
 
 import giulio.frasca.lib.TimeFunctions;
 
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.AudioManager;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class BackroundService extends Service {
 
 	private final long UPDATE_TIME=15*1000L;
 	private static final int HELLO_ID = 1;
 	private final String PREF_FILE = "ncsusilencepreffile2";
 
 	
 	
 	private Timer timer;
 	
 	private TimerTask updateTask = new TimerTask() {
 	    @Override
 	    public void run() {
 	    	AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 	    	SharedPreferences settings = getSharedPreferences(PREF_FILE,Context.MODE_PRIVATE);
 	    	Schedule s = new Schedule(settings);
 	    	LinkedList<RingerSettingBlock> list = s.getList();
 	    	Iterator<RingerSettingBlock> i = list.iterator();
 	    	int prevRingerLevel= am.getRingerMode();
 	    	int newRingerLevel= am.getRingerMode();
 	    	while (i.hasNext()){
 	    		RingerSettingBlock block = i.next();
 	    		if (block.isEnabled() && isEnabledToday(block) && isWithinTimeBlock(block)){
 	    			newRingerLevel = block.getRingVal();
 	    		}
 	    	}
 	    	if (newRingerLevel != prevRingerLevel){
 				am.setRingerMode(newRingerLevel);
 				if (newRingerLevel == AudioManager.RINGER_MODE_NORMAL){
 					changeNotification("Silence","Service Running: Normal Level","Normal Level Restored", newRingerLevel);
 				}
 				if (newRingerLevel == AudioManager.RINGER_MODE_VIBRATE){
 					changeNotification("Silence","Service Running: Vibrate","Vibrate Only Mode Enabled", newRingerLevel);
 				}
 				if (newRingerLevel == AudioManager.RINGER_MODE_SILENT){
 					changeNotification("Silence","Service Running: Silent","Silent / No Vibration Enabled", newRingerLevel);
 				}
 				logcatPrint("CHANGING LEVEL");
 			}
 			else{
 				logcatPrint("Ringer Level already the same!");
 			}
 		}
 	    	
 
 		private boolean isWithinTimeBlock(RingerSettingBlock block) {
 			long todayTimestamp = TimeFunctions.getLocalTime();
 			long now = TimeFunctions.timeSinceMidnight(todayTimestamp);
 			long start = block.getStartTime();
 			long end = block.getEndTime();
 			if ((start<now) && (now<end)){
 				return true;
 			}
 			return false;
 		}
 
 		private boolean isEnabledToday(RingerSettingBlock block) {
 			long now = TimeFunctions.getLocalTime();
 			int dayOfWeek = TimeFunctions.getDayofWeekFromTimestamp(now);
 			if (dayOfWeek == Calendar.SUNDAY){
 				return block.isEnabledSunday();
 			}
 			if (dayOfWeek == Calendar.MONDAY){
 				return block.isEnabledMonday();
 			}
 			if (dayOfWeek == Calendar.TUESDAY){
 				return block.isEnabledTuesday();
 			}
 			if (dayOfWeek == Calendar.WEDNESDAY){
 				return block.isEnabledWednesday();
 			}
 			if (dayOfWeek == Calendar.THURSDAY){
 				return block.isEnabledThursday();
 			}
 			if (dayOfWeek == Calendar.FRIDAY){
 				return block.isEnabledFriday();
 			}
 			if (dayOfWeek == Calendar.SATURDAY){
 				return block.isEnabledSaturday();
 			}
 			return false;
 		}
 	  };
 	
 //	public BackroundService(){
 //		this.lastUpdated=System.currentTimeMillis();
 //		toastMessage("hello world");
 //		toastMessage("last Updated:" + lastUpdated);
 //	}
 	
 	@Override
 	public void onCreate(){
 		 super.onCreate();
 		 logcatPrint("Service creating");
 		 String ns = Context.NOTIFICATION_SERVICE;
 	    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
	    	AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 	    	
	    	int icon = R.drawable.redicon;
	    	if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE){ icon = R.drawable.yellowicon; };
	    	if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){ icon = R.drawable.greenicon;}
 	    	CharSequence tickerText = "Service Started";
 	    	long when = System.currentTimeMillis();
 	    	
 	    	Notification notification = new Notification(icon, tickerText, when);
 	    	
 	    	Context context = getApplicationContext();
 	    	CharSequence contentTitle = "Silence";
 	    	CharSequence contentText = "Service Running: ";
 	    	

 	    	if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL){
 	    		contentText = contentText + "Normal Level";
 	    	}
 	    	if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE){
 	    		contentText = contentText + "Vibrate";
 	    	}
 	    	if (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT){
 	    		contentText = contentText + "Silent";
 	    	}
 	    	Intent notificationIntent = new Intent(this, BackroundService.class);
 	    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
 
 	    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 	    	notification.flags |= Notification.FLAG_ONGOING_EVENT;
 	    	notification.flags |= Notification.FLAG_NO_CLEAR;
 	    	
 	    	mNotificationManager.notify(HELLO_ID, notification);
 	    	
 		 
 		 timer = new Timer("TweetCollectorTimer");
 		 timer.schedule(updateTask, 0L , UPDATE_TIME);
 	}
 	
 	public void changeNotification(String titleMessage, String subtitleMessage, String tickerText, int ringlevel){
 		String ns = Context.NOTIFICATION_SERVICE;
     	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
     	mNotificationManager.cancel(HELLO_ID);
     	int icon = R.drawable.redicon;
     	if (ringlevel == AudioManager.RINGER_MODE_VIBRATE){ icon = R.drawable.yellowicon; };
     	if (ringlevel == AudioManager.RINGER_MODE_NORMAL){ icon = R.drawable.greenicon;}
     	long when = System.currentTimeMillis();
     	
     	Notification notification = new Notification(icon, tickerText, when);
     	
     	Context context = getApplicationContext();
     	CharSequence contentTitle = titleMessage;
     	CharSequence contentText = subtitleMessage;
     	Intent notificationIntent = new Intent(this, BackroundService.class);
     	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
 
     	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
     	notification.flags |= Notification.FLAG_ONGOING_EVENT;
     	notification.flags |= Notification.FLAG_NO_CLEAR;
     	
     	mNotificationManager.notify(HELLO_ID, notification);
 	}
 	
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 	    logcatPrint("Service destroying");
 	    
 	    String ns = Context.NOTIFICATION_SERVICE;
 	    NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
 	    mNotificationManager.cancel(HELLO_ID);
 	    
 	    timer.cancel();
 	    timer = null;
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 	
     public void logcatPrint(String message){
     	Log.v("customdebug",message + " | sent from " +this.getClass().getSimpleName());
     }
     /**
      * Prints a temporary 'tooltip' style reminder on the GUI
      * 
      * @param msg - a short message to print
      */
     public void toastMessage(String msg){
     	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
     }
 }
