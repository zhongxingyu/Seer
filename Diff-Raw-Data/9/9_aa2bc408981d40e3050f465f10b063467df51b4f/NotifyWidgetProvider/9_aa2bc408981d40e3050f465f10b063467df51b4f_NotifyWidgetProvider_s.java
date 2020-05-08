 package net.kaisti.txrxwidget;
 
 import java.util.HashMap;
 
 import net.kaisti.txrxwidget.NotifyInfo.TrafficType;
 import net.kaisti.txrxwidget.NotifyInfo.TrafficChannel;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.IBinder;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 public class NotifyWidgetProvider extends AppWidgetProvider {
 	@Override
 	public void onDeleted(Context context, int[] appWidgetIds) {
 		super.onDeleted(context, appWidgetIds);
 	}

 	@Override
 	public void onDisabled(Context context) {
 		super.onDisabled(context);
 	}
 
 	@Override
 	public void onEnabled(Context context) {
 		super.onEnabled(context);
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		super.onReceive(context, intent);
 	}
 
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 		
 		final int N = appWidgetIds.length;
 
         for (int i=0; i<N; i++) {
             int appWidgetId = appWidgetIds[i];
 
             Intent intent = new Intent(context, NotifyWidgetService.class);
             PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
             
             RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notifywidget);
             views.setOnClickPendingIntent(R.id.txrx, pendingIntent);
             
             appWidgetManager.updateAppWidget(appWidgetId, views);
         }
 	}
 
 	public static class NotifyWidgetService extends Service{
 		protected static final long DELAY = 3000; // notification updates every two seconds
 	    private static int TXRX_ON = R.drawable.txrx_on;  // widget button on
 	    private static int TXRX_OFF = R.drawable.txrx_off; // widget button off
 		private NotificationManager manager = null;
 		
 		@Override
 		public IBinder onBind(Intent intent) {
 			return null;
 		}
 
 		@Override
 		public void onCreate() {
 			super.onCreate();
 		}
 
 		@Override
 		public void onStart(Intent intent, int startId) {
 			super.onStart(intent, startId);
 			manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		}
 
 		@Override
 		public void onDestroy() {
 			super.onDestroy();
 			toggleState(false);
 		}
 
 		private boolean isActive = false;
 		public int onStartCommand(Intent intent, int flags, int startId) {
 			isActive = !isActive; // change between on and off states
 			toggleState(isActive);
 			return super.onStartCommand(intent, flags, startId);
 		}
 
 		private HashMap<TrafficType, NotifyInfo> previousInfo = new HashMap<TrafficType, NotifyInfo>();
 		private TrafficType currentType = TrafficType.RX;
 		
 		private NotifyInfo getNotificationInfo() {
 			currentType = (currentType == TrafficType.RX ? TrafficType.TX : TrafficType.RX);
 			NotifyInfo info = new NotifyInfo(previousInfo.get(currentType), currentType, TrafficChannel.TOTAL);
 			previousInfo.put(currentType, info);
 			return info;
 			
 		}
 
 		private CharSequence previousNotifyText;
 		private void notifyStatus(NotifyInfo info) {
 			CharSequence text = info.getText(DELAY);
 			
 			// skip unchanged notifies
 			if(previousNotifyText != null && previousNotifyText.equals(text)) {
 				manager.cancel(Constants.NOTIFICATION_ID);
 			}
 			else {	
 				PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
 				Notification notification = new Notification(info.getIcon(), text, System.currentTimeMillis());
 				notification.setLatestEventInfo(this, getText(R.string.txrx_notifytext), text, contentIntent);
 
 				manager.notify(Constants.NOTIFICATION_ID, notification);
 			}
 			previousNotifyText = text;
 		}
 
 		private Handler handler = new Handler();
 	    private Runnable runnable = new Runnable() {
 	    	public void run() {
 	    		notifyStatus( getNotificationInfo() );
 		    	handler.postDelayed(this, DELAY);
 	    	}
 	    };
 	    
 	    private void toggleState(boolean state) {
 			handler.removeCallbacks(runnable);
 
 			RemoteViews views = new RemoteViews(getPackageName(), R.layout.notifywidget);
 			if(state == true) {
 				views.setInt(R.id.txrx, "setImageResource", TXRX_ON);
 		        handler.postDelayed(runnable, DELAY);
 		        // Tell the user we stopped.
 		        Toast.makeText(this, getText(R.string.txrx_started), Toast.LENGTH_SHORT).show();	
 			}
 			else {
 				views.setInt(R.id.txrx, "setImageResource", TXRX_OFF);
 		        manager.cancel(Constants.NOTIFICATION_ID);
 		        Toast.makeText(this, getText(R.string.txrx_stopped), Toast.LENGTH_SHORT).show();		
 			}
 
 			ComponentName thisWidget = new ComponentName(this, NotifyWidgetProvider.class);
             AppWidgetManager.getInstance(this).updateAppWidget(thisWidget, views);
 
 		}
 	}
 }
