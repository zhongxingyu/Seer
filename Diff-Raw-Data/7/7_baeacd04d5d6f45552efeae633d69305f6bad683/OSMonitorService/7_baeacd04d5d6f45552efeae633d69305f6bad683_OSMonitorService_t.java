 package com.eolwral.osmonitor;
 
 import java.text.DecimalFormat;
 
 import com.eolwral.osmonitor.preferences.Preferences;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.widget.RemoteViews;
 
 public class OSMonitorService extends Service
 {
 	private static final int NOTIFYID = 20091231;
 	private static int battLevel = 0;  // percentage, or -1 for unknown
 	private static int temperature = 0;
 	private static int useColor = 0;
 	private static boolean useCelsius = true;
     private NotificationManager serviceNM = null;
 	private Notification serviceNotify = null;
 
 	private boolean TimeUpdate = false;
 	private int UpdateInterval = 2;
 	
 	private static JNIInterface JNILibrary = JNIInterface.getInstance();;
 
 	private static OSMonitorService single = null;
 
 	public static OSMonitorService getInstance()
 	{
 		if(single != null)
 			return single;
 		return null;
 	}
 	
 	public class OSMonitorBinder extends Binder 
 	{
 		OSMonitorService getService()
 		{
 			return OSMonitorService.this;
 		}
 	}
 	
 	private final IBinder mBinder = new OSMonitorBinder();
 
 	private static DecimalFormat MemoryFormat = new DecimalFormat(",000");
 	
 	private static int cpuLoad = 0;
 	
 	private Handler mHandler = new Handler();
 	private Runnable mRefresh = new Runnable() 
 	{
 		@Override  
             public void run() {
 
             	cpuLoad = JNILibrary.GetCPUUsageValue();
             	int color = useColor;
             	
             	//Invert the colour if we are on the LP core.
             	if (JNILibrary.GetTegra3IsTegra3())
     		    {
     		    	if (JNILibrary.GetTegra3ActiveCpuGroup() != null)
     		    	{
     		    		if (JNILibrary.GetTegra3IsLowPowerGroupActive())
     		    		{
     		    			color = color != 0 ? 0 : 1;
     		    		}
     		    	}
     		    }
             	
 				if(cpuLoad < 20)
 					serviceNotify.iconLevel = 1+color*100;
 				else if(cpuLoad < 40)
 					serviceNotify.iconLevel = 2+color*100;
 				else if(cpuLoad < 60)
 					serviceNotify.iconLevel = 3+color*100;
 				else if(cpuLoad < 80)
 					serviceNotify.iconLevel = 4+color*100;
 				else if(cpuLoad < 100)
 					serviceNotify.iconLevel = 5+color*100;
 				else 
 					serviceNotify.iconLevel = 6+color*100;
 				
 				serviceNotify.contentView.setTextViewText(R.id.StatusBarCPU, cpuLoad+"%");
 				serviceNotify.contentView.setTextViewText(R.id.StatusBarMEM, MemoryFormat.format(JNILibrary.GetMemBuffer()+JNILibrary.GetMemCached()+JNILibrary.GetMemFree())+ "K");
 				serviceNotify.contentView.setTextViewText(R.id.StatusBarBAT, battLevel+"%");
 						
 				if(useCelsius)
 					serviceNotify.contentView.setTextViewText(R.id.StatusBarBATTemp, temperature/10+"°C");
 				else
 					serviceNotify.contentView.setTextViewText(R.id.StatusBarBATTemp, ((int)temperature/10*9/5+32)+"°F");			
 
 				try
 				{
 					serviceNM.notify(NOTIFYID, serviceNotify);
 				} catch(Exception e) {}
 
 				mHandler.postDelayed(mRefresh, UpdateInterval * 1000);
             }
     };
 
     @Override
     public void onCreate() {
     	serviceNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
     	InitNotification();
     	Notify();
 
     	single = this;
     }
     
     @Override
     public void onDestroy() {
     	Disable();
     }
 
     public void Notify()
     {
     	Enable();
     }
     
 	
     private void Enable()
     {
     	if(!mRegistered)
     	{
     		IntentFilter filterScreenON = new IntentFilter(Intent.ACTION_SCREEN_ON);
     		registerReceiver(mReceiver, filterScreenON);
 
     		IntentFilter filterScreenOFF = new IntentFilter(Intent.ACTION_SCREEN_OFF);
     		registerReceiver(mReceiver, filterScreenOFF);
     		
     		mRegistered = true;
     	}
     	
 		// load settings
 		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
 
 		try {
 			UpdateInterval = Integer.parseInt(settings.getString(Preferences.PREF_UPDATE, "2"));
 		} catch(Exception e) {}		
 		
 		if(settings.getBoolean(Preferences.PREF_CPUUSAGE, false))
 		{
 			if(TimeUpdate == false)
 			{
 				JNILibrary.doCPUUpdate(1);
 				mHandler.postDelayed(mRefresh, UpdateInterval * 1000);
 				TimeUpdate = true;
 			}
 		}
 		else
 		{
 			if(TimeUpdate == true)
 			{
 	    		JNILibrary.doCPUUpdate(0);
 	    		mHandler.removeCallbacks(mRefresh);
 	    		TimeUpdate = false;
 			}
 			serviceNotify.iconLevel = 0;
 			serviceNM.notify(NOTIFYID, serviceNotify);
 		}
 		
 		useCelsius = settings.getBoolean(Preferences.PREF_TEMPERATURE, true);
 		useColor =  Integer.parseInt(settings.getString(Preferences.PREF_STATUSBARCOLOR, "0"));
 		
 		startBatteryMonitor();
     }
     
     private void Disable()
     {
     	serviceNM.cancel(NOTIFYID);
     	
     	if(TimeUpdate)
     	{
     		JNILibrary.doCPUUpdate(0);
     		mHandler.removeCallbacks(mRefresh);
     		TimeUpdate = false;
     	}
 
     	if(mRegistered)
     	{
     		unregisterReceiver(mReceiver);
     		mRegistered = false;
     	}
     	
     	stopBatteryMonitor();
     }
     
     private void startBatteryMonitor()
     {
     	IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
     	registerReceiver(battReceiver, battFilter);		        		
     }
     
     private void stopBatteryMonitor()
     {
     	unregisterReceiver(battReceiver);
     }
 
 	private static BroadcastReceiver battReceiver = new BroadcastReceiver() 
 	{
 		public void onReceive(Context context, Intent intent) {
 			
 			int rawlevel = intent.getIntExtra("level", -1);
 			int scale = intent.getIntExtra("scale", -1);
 			
 			temperature = intent.getIntExtra("temperature", -1);
 
 			if (rawlevel >= 0 && scale > 0) {
 				battLevel = (rawlevel * 100) / scale;
 			}
 		}
 	};
 
     private boolean mRegistered = false;
     private BroadcastReceiver mReceiver = new BroadcastReceiver() 
     {
     	public void onReceive(Context context, Intent intent) {
     		if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
     		{
     	    	if(TimeUpdate)
     	    	{
     	    		JNILibrary.doCPUUpdate(0);
     	    		mHandler.removeCallbacks(mRefresh);
     	    		TimeUpdate = false;
     	    	}
     		}
     		else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
     		{
     			// load settings
     			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
 
     			if(settings.getBoolean(Preferences.PREF_CPUUSAGE, false))
     			{
     				if(TimeUpdate == false)
     				{
     					JNILibrary.doCPUUpdate(1);
     					mHandler.postDelayed(mRefresh, UpdateInterval * 1000);
     					TimeUpdate = true;
     				}
     			}
     		}
     	}
     }; 
      
     @Override
     public IBinder onBind(Intent intent) {
             return mBinder;
     }
 	
     private void InitNotification() 
     {
 	    int thisIcon = R.anim.statusicon;        		// icon from resources
 	    
 	    CharSequence tickerText = getResources().getString(R.string.bar_title);
 	    serviceNotify = new Notification(thisIcon, tickerText, 0);
 	    serviceNotify.flags |= Notification.FLAG_NO_CLEAR|Notification.FLAG_ONGOING_EVENT|Notification.FLAG_ONLY_ALERT_ONCE;
 
 	    RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notificationlayout);
     	serviceNotify.contentView = contentView;
 
     	Intent notificationIntent = new Intent(this, OSMonitor.class);
     	notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
     	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
     	serviceNotify.contentIntent = contentIntent;
 
     	serviceNM.notify(NOTIFYID, serviceNotify);
     }
 
 }
