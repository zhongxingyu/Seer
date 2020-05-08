 package interdroid.lifediary.sensors;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import interdroid.vdb.content.EntityUriBuilder;
 import interdroid.vdb.content.avro.AvroContentProviderProxy;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.appwidget.AppWidgetManager;
 import android.appwidget.AppWidgetProvider;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.widget.ListAdapter;
 import android.widget.RemoteViews;
 import android.widget.SimpleAdapter;
 
 import interdroid.contextdroid.sensors.AbstractVdbSensor;
 import interdroid.lifediary.R;
 
 /**
  * A widget based mood sensor.
  *
  * @author nick &lt;palmer@cs.vu.nl&gt;
  *
  */
 public class MoodSensor extends AbstractVdbSensor {
 	/**
 	 * Access to logger.
 	 */
 	private static final Logger LOG =
 			LoggerFactory.getLogger(MoodSensor.class);
 
 	public static final long DEFAULT_EXPIRE = 15 * 60 * 1000;
 
 	public static final String MOOD_FIELD = "mood";
 
 	public static final String MOOD_ID_FIELD = "mood_resource";
 
 	/**
 	 * The schema for this sensor.
 	 */
 	public static final String SCHEME = getSchema();
 
 	/**
 	 * The provider for this sensor.
 	 *
 	 * @author nick &lt;palmer@cs.vu.nl&gt;
 	 *
 	 */
 	public static class Provider extends AvroContentProviderProxy {
 
 		/**
 		 * Construct the provider for this sensor.
 		 */
 		public Provider() {
 			super(SCHEME);
 		}
 
 	}
 
 	private static final String NAMESPACE =
 			"interdroid.lifediary.mood";
 
 	private static final String NAME = "mood";
 
 	public static final Uri URI = EntityUriBuilder.nativeUri(NAMESPACE,
 			NAME);
 
 	/**
 	 * @return the schema for this sensor.
 	 */
 	private static String getSchema() {
 		String scheme =
 				"{'type': 'record', 'name': '" + NAME + "', "
 						+ "'namespace': '" + NAMESPACE + "',"
 						+ "\n'fields': ["
 						+ SCHEMA_TIMESTAMP_FIELDS
 						+ "\n{'name': '"
 						+ MOOD_FIELD
 						+ "', 'type': 'string'},"
 						+ "\n{'name': '"
 						+ MOOD_ID_FIELD
 						+ "', 'type': 'int'}"
 						+ "\n]"
 						+ "}";
 		return scheme.replace('\'', '"');
 	}
 
 	public static class WidgetReceiver extends Activity {
 
 		@Override
 		public void onCreate(Bundle b) {
 			super.onCreate(b);
 
 			LOG.debug("onCreate");
 			Context context = this;
 
 			String[] moods =
 					context.getResources().getStringArray(R.array.moods);
 			String unknown_mood = context.getString(R.string.mood_unknown);
 			ContentResolver resolver = context.getContentResolver();
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(context);
 			builder.setAdapter(getListAdapter(context, moods),
 					getOnClickListener(resolver,
 							moods, unknown_mood));
 			builder.setTitle(R.string.title_mood);
 			builder.setOnCancelListener(getOnCancelListener(resolver,
 					unknown_mood));
 			builder.show();
 		}
 
 		private OnCancelListener getOnCancelListener(
 				final ContentResolver resolver,
 				final String unknown) {
 			return new OnCancelListener() {
 
 				@Override
 				public void onCancel(DialogInterface arg0) {
 					putMood(resolver, -1, unknown);
 				}
 			};
 		}
 
 		private void putMood(final ContentResolver resolver,
 				final int moodId, final String mood) {
 			ContentValues values = new ContentValues();
 			values.put(MOOD_FIELD, mood);
 			values.put(MOOD_ID_FIELD, moodId);
 			long now = System.currentTimeMillis();
 			long expire = now + DEFAULT_EXPIRE;
 
 			MoodSensor.putValues(resolver,
					URI, values, now, expire);
 
             WidgetProvider.forceUpdate(this);
 			finish();
 		}
 
 		private OnClickListener getOnClickListener(
 				final ContentResolver resolver,
 				final String[] moods,
 				final String unknown_mood) {
 			return new OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 					if (which < moods.length) {
 						putMood(resolver, which, moods[which]);
 					} else {
 						putMood(resolver, -1, unknown_mood);
 					}
 				}
 
 			};
 		}
 
 		private ListAdapter getListAdapter(final Context context,
 				final String[] moods) {
 			List<Map<String, Object>> data = getMoodList(context,
 					moods);
 			SimpleAdapter adapter = new SimpleAdapter(context,
 					data, R.layout.mood_list_item,
 					new String[] {"r", "m"},
 					new int[] {R.id.mood_image, R.id.mood_label});
 
 			return adapter;
 		}
 
 		private List<Map<String, Object>> getMoodList(Context context,
 				final String[] moods) {
 			List<Map<String, Object>> data =
 					new ArrayList<Map<String, Object>>();
 			for (int i = 0; i < moods.length; i++) {
 				Map<String, Object> map = new HashMap<String, Object>();
 				map.put("m", moods[i]);
 				map.put("r", getMoodDrawableId(i));
 				data.add(map);
 			}
 
 			return data;
 		}
 
 	}
 
 	public static class WidgetProvider extends AppWidgetProvider {
 
 		public void onReceive(Context context, Intent intent) {
 			LOG.debug("Got intent: {}", intent);
 			super.onReceive(context, intent);
 		}
 
 		public final void onUpdate(final Context context,
 				final AppWidgetManager appWidgetManager,
 				final int[] appWidgetIds) {
 			LOG.debug("onUpdate: {}", appWidgetIds);
 			final int N = appWidgetIds.length;
 
 			for (int i=0; i<N; i++) {
 				int appWidgetId = appWidgetIds[i];
 				LOG.debug("Updating widget: {}", appWidgetId);
 
 				// Create an Intent to launch MoodActivity
 				Intent intent = new Intent(context, WidgetReceiver.class);
 				PendingIntent pendingIntent =
 						PendingIntent.getActivity(context, appWidgetId, intent,
 								PendingIntent.FLAG_UPDATE_CURRENT);
 
 				RemoteViews views = getUpdateViews(context);
 
 				views.setOnClickPendingIntent(R.id.mood_button, pendingIntent);
 
 				// Tell the AppWidgetManager to perform an update on widget
 				appWidgetManager.updateAppWidget(appWidgetId, views);
 			}
 		}
 
 		private static RemoteViews getUpdateViews(final Context context) {
 			// Get the layout for the App Widget and attach
 			// an on-click listener to the button
 			RemoteViews views = new RemoteViews(context.getPackageName(),
 					R.layout.mood_widget);
 
 			Cursor moodCursor = null;
 			try {
 				moodCursor = MoodSensor.getValuesCursor(context,
 						URI, new String[] {MOOD_FIELD, MOOD_ID_FIELD},
						System.currentTimeMillis(), 0L);
 				LOG.debug("Got cursor: {} {}",
 						moodCursor.getCount(), moodCursor);
 				views.setImageViewResource(R.id.mood_button,
 						MoodSensor.getLastMoodResource(context, moodCursor));
 				views.setTextViewText(R.id.mood_label,
 						MoodSensor.getLastMoodLabel(context, moodCursor));
 //				views.setInt(R.id.mood_button, "setAlpha", 128);
 			} finally {
 				if (moodCursor != null) {
 					moodCursor.close();
 				}
 			}
 			return views;
 		}
 
 		public static void forceUpdate(Context context) {
 			LOG.debug("forcing update.");
 			Intent intent =
 					new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
 			ComponentName component =
 					new ComponentName(context, WidgetProvider.class.getName());
 			intent.setComponent(component);
 			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
 					AppWidgetManager.getInstance(context)
 						.getAppWidgetIds(component));
 			LOG.debug("Intent: {}", intent);
 			context.sendBroadcast(intent);
 
 		}
 	}
 
 
 	private static CharSequence getLastMoodLabel(Context context,
 			Cursor moodCursor) {
 		String mood = null;
 		if (moodCursor != null) {
 			if (moodCursor.getCount() > 0 && moodCursor.moveToFirst()) {
 				mood = moodCursor.getString(
 						moodCursor.getColumnIndex(MOOD_FIELD));
 			}
 		}
 		if (mood == null) {
 			mood = context.getString(R.string.mood_unknown);
 		}
 		LOG.debug("Returning mood: {}", mood);
 		return mood;
 	}
 
 	private static int getLastMoodResource(Context context, Cursor moodCursor) {
 		int mood = R.drawable.mood_unknown;
 		if (moodCursor != null) {
 			if (moodCursor.getCount() > 0 && moodCursor.moveToFirst()) {
 				int id = moodCursor.getInt(
 						moodCursor.getColumnIndex(MOOD_ID_FIELD));
 				mood = getMoodDrawableId(id);
 			}
 		}
 		LOG.debug("Returning  mood resource: {}", mood);
 		return mood;
 	}
 
 	private static int getMoodDrawableId(final int id) {
 		LOG.debug("Getting drawable for: {}", id);
 		int mood;
 		switch (id) {
 		case 0: // Happy
 			mood = R.drawable.mood_happy;
 			break;
 		case 1: // Normal
 			mood = R.drawable.mood_normal;
 			break;
 		case 2: // Sad
 			mood = R.drawable.mood_sad;
 			break;
 		case 3: // Angry
 			mood = R.drawable.mood_angry;
 			break;
 		case 4: // Afraid
 			mood = R.drawable.mood_afraid;
 			break;
 		case 5: // Love
 			mood = R.drawable.mood_in_love;
 		case 6: // Shocked
 			mood = R.drawable.mood_shocked;
 		default:
 			mood = R.drawable.mood_unknown;
 			break;
 		}
 		return mood;
 	}
 
 	@Override
 	public void initDefaultConfiguration(final Bundle defaults) {
 
 	}
 
 	@Override
 	public String[] getValuePaths() {
 		return new String[] { MOOD_FIELD };
 	}
 
 	@Override
 	public void register(final String id, final String valuePath,
 			final Bundle configuration) {
 		// Nothing to do
 	}
 
 	@Override
 	public void unregister(final String id) {
 		// Nothing to do
 	}
 
 	@Override
 	public final String getScheme() {
 		return SCHEME;
 	}
 
 	@Override
 	public void onConnected() {
 		// Nothing to do
 	}
 
 	@Override
 	public void onDestroySensor() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
