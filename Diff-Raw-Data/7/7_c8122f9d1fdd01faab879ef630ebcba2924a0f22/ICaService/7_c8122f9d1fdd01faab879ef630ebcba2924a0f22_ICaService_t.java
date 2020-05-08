 package net.kuwalab.android.icareader;
 
 import android.app.Service;
 import android.appwidget.AppWidgetManager;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.widget.RemoteViews;
 
 public class ICaService extends Service {
 	public static final String ACTION = "net.kuwalab.android.icareader.UPDATE";
 	protected static final String PREFERENCES_NAME = "ICA_DATA";
 	protected static final String PREFERENCES_CONF_DATE = "conf_date";
 	protected static final String PREFERENCES_REST_MONEY = "rest_money";
 
 	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
 
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(ACTION);
 		registerReceiver(receiver, filter);
 
 		refresh(getApplicationContext());

		return START_STICKY;
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	private static void refresh(Context context) {
 		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME,
 				Context.MODE_PRIVATE);
 
 		AppWidgetManager awm = AppWidgetManager.getInstance(context);
 		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
 				R.layout.widget_main);
 		remoteViews.setTextViewText(R.id.confDate,
 				pref.getString(PREFERENCES_CONF_DATE, "未確認"));
 		remoteViews.setTextViewText(R.id.widetRest,
 				pref.getString(PREFERENCES_REST_MONEY, "￥--,---"));
 		awm.updateAppWidget(new ComponentName(context, ICaWidget.class),
 				remoteViews);
 	}
 
 	private static BroadcastReceiver receiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			refresh(context);
 		}
 	};
 }
