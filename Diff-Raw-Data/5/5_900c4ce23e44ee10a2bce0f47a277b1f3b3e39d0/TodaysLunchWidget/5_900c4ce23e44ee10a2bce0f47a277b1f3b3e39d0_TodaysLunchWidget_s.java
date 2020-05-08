 package jp.oxiden.todayslunch;
 
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class TodaysLunchWidget extends AppWidgetProvider {
 	private final String TAG = "TodaysLunch";
 
 	/*
 	 * 一番はじめのWidget設置時のみ呼ばれる
 	 */
 	@Override
 	public void onEnabled(Context context) {
 		Log.d(TAG, "onEnabled----------------------------------");
 		super.onEnabled(context);
 	}
 
 	/*
 	 * Widget設置のたび呼ばれる
 	 */
 	@Override
 	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
 		Log.d(TAG, "onUpdate----------------------------------");
 		super.onUpdate(context, appWidgetManager, appWidgetIds);
 
 		// サービスの起動
 		Intent intent = new Intent(context, RefreshMenuService.class);
 		context.startService(intent);
 		Log.d(TAG, "service started.==========================");
 	}
 
 	/*
 	 * Widget削除のたび呼ばれる
 	 */
 	@Override
 	public void onDeleted(Context context, int[] appWidgetIds) {
 		Log.d(TAG, "onDeleted----------------------------------");
 		super.onDeleted(context, appWidgetIds);
 	}
 
 	/*
 	 * 一番さいごのWidget削除時のみ呼ばれる
 	 */
 	@Override
 	public void onDisabled(Context context) {
 		Log.d(TAG, "onDisabled----------------------------------");
 		super.onDisabled(context);
 	}
 
 	/*
 	 * 上記コールバック関数の直後、及びupdatePeriodMillis設定時間ごと(※0以外)呼ばれる
 	 */
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Log.d(TAG, "onReceive====================================");
 		super.onReceive(context, intent);
 	}
 }
