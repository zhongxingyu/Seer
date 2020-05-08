 package com.aragaer.reminder;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.annotation.SuppressLint;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Resources;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.util.Pair;
 import android.widget.RemoteViews;
 
 public class ReminderService extends Service {
 	private static final int intent_flags = Intent.FLAG_ACTIVITY_NEW_TASK
 			| Intent.FLAG_ACTIVITY_CLEAR_TOP
 			| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
 
 	public static final String settings_changed = "com.aragaer.reminder.SETTINGS_CHANGE";
 	final Map<Long, Bitmap> cached_bitmaps = new HashMap<Long, Bitmap>();
 	Bitmap list_bmp, new_bmp;
 
 	public IBinder onBind(Intent i) {
 		return null;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		handleCommand(intent);
 		return START_STICKY;
 	}
 
 	private void handleCommand(Intent command) {
 		getContentResolver().registerContentObserver(ReminderProvider.content_uri, false, observer);
 		registerReceiver(catcher, new IntentFilter(catcher_action));
 		new NotificationBuilderTask().execute(this);
 	}
 
 	private static final String PKG_NAME = ReminderService.class.getPackage().getName();
 
 	public static int n_glyphs(int display_size, int glyph_size) {
 		int num = display_size / glyph_size;
 		if (num > 7) // Hardcoded value, yo!
 			num = 7;
 		return num - 2;
 	}
 
 	List<Pair<Bitmap, Intent>> list = new ArrayList<Pair<Bitmap, Intent>>();
 	private Notification buildNotification(Context ctx) {
 		Resources r = ctx.getResources();
 		int height = r.getDimensionPixelSize(R.dimen.notification_height);
 		int margin = r.getDimensionPixelSize(R.dimen.notification_glyph_margin);
 		int size = height - 2 * margin;
 
 		int max = n_glyphs(r.getDisplayMetrics().widthPixels, height);
 
 		list.clear();
 		Cursor cursor = ctx.getContentResolver().query(
 				ReminderProvider.content_uri, null, null, null, null);
 		ReminderItem item = null;
		if (cursor.moveToFirst()) do {
 			item = ReminderProvider.getItem(cursor, item);
 			Bitmap image = cached_bitmaps.get(item._id);
 			if (image == null) {
 				image = Bitmaps.memo_bmp(ctx, item, size);
 				cached_bitmaps.put(item._id, image);
 			}
 			list.add(Pair.create(image,
 					new Intent(ctx, ReminderViewActivity.class)
 							.putExtra("reminder_id", item._id)));
		} while (cursor.moveToNext() && --max > 0);
 		int n_sym = list.size();
 		int lost = cursor.getCount() - n_sym;
 		cursor.close();
 
 		if (list_bmp == null)
 			list_bmp = Bitmaps.list_bmp(ctx, lost);
 		if (new_bmp == null)
 			new_bmp = Bitmaps.add_new_bmp(ctx);
 
 		Pair<Bitmap, Intent> list_btn = Pair.create(list_bmp,
 				new Intent(ctx, ReminderListActivity.class).addFlags(intent_flags));
 		Pair<Bitmap, Intent> new_btn = Pair.create(new_bmp,
 				new Intent(ctx, ReminderCreateActivity.class).addFlags(intent_flags));
 		list.add(list_btn);
 		list.add(new_btn);
 
 		RemoteViews rv = new RemoteViews(PKG_NAME, R.layout.notification);
 		rv.removeAllViews(R.id.wrap);
 		rv.removeAllViews(R.id.wrap2);
 		for (int i = 0; i < list.size(); i++) {
 			final Pair<Bitmap, Intent> g2i = list.get(i);
 			final RemoteViews image = new RemoteViews(PKG_NAME, R.layout.image);
 			image.setOnClickPendingIntent(R.id.image, PendingIntent.getBroadcast(ctx, i,
 					new Intent(catcher_action).putExtra("what", i), 0));
 			image.setImageViewBitmap(R.id.image, g2i.first);
 			if (i < n_sym)
 				rv.addView(R.id.wrap, image);
 			else
 				rv.addView(R.id.wrap2, image);
 		}
 
 		Notification n = new Notification.Builder(ctx).setSmallIcon(
 				R.drawable.notify, n_sym).setContent(rv).getNotification();
 		n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR
 				| Notification.FLAG_ONLY_ALERT_ONCE;
 
 		return n;
 	}
 
 	private final class NotificationBuilderTask extends AsyncTask<Context, Void, Notification> {
 		@Override
 		protected Notification doInBackground(Context... params) {
 			return buildNotification(params[0]);
 		}
 
 		protected void onPostExecute(Notification n) {
 			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, n);
 		}
 	}
 
 	public static final String catcher_action = "com.aragaer.reminder.CATCH_ACTION";
 	private static final String collapse_method = Build.VERSION.SDK_INT > 16 ? "collapsePanels" : "collapse";
 	private final BroadcastReceiver catcher = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			int position = intent.getIntExtra("what", 99);
 			Intent i = list == null || position >= list.size()
 					? new Intent(context, ReminderListActivity.class)
 					: list.get(position).second;
 
 			try {
 				Object obj = context.getSystemService("statusbar");
 				Class.forName("android.app.StatusBarManager")
 						.getMethod(collapse_method, new Class[0])
 						.invoke(obj, (Object[]) null);
 			} catch (Exception e) {
 				Log.e("STATUSBAR", "Failed to collapse status panel: "+e);
 				// do nothing, it's OK
 			}
 			context.startActivity(i.addFlags(intent_flags));
 		}
 	};
 
 	ContentObserver observer = new ContentObserver(new Handler()) {
 		@SuppressLint("NewApi")
 		public void onChange(boolean selfChange) {
 			this.onChange(selfChange, null);
 		}
 
 		@SuppressLint("Override")
 		public void onChange(boolean selfChange, Uri uri) {
 			new NotificationBuilderTask().execute(getApplicationContext());
 		}
 	};
 
 	public void onDestroy() {
 		getContentResolver().unregisterContentObserver(observer);
 	}
 }
