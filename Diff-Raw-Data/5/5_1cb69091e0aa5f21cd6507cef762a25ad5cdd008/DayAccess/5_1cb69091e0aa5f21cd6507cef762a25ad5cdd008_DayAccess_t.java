 package ch.almana.android.stechkarte.model;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import android.app.ProgressDialog;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 import ch.almana.android.stechkarte.log.Logger;
 import ch.almana.android.stechkarte.provider.IAccess;
 import ch.almana.android.stechkarte.provider.db.DB;
 import ch.almana.android.stechkarte.provider.db.DB.Days;
 import ch.almana.android.stechkarte.provider.db.DB.Timestamps;
import ch.almana.android.stechkarte.view.appwidget.StechkarteAppwidget.UpdateAppWidgetService;
 
 public class DayAccess implements IAccess {
 	private static final String LOG_TAG = Logger.LOG_TAG;
 	
 	private static SimpleDateFormat dayRefDateFormat = new SimpleDateFormat("yyyyMMdd");
 	
 	private final Context context;
 	
 	private static DayAccess instance;
 	public static final float HOURS_IN_MILLIES = 1000f * 60f * 60f;
 	
 	public static void initInstance(Context context) {
 		instance = new DayAccess(context);
 	}
 	
 	public static DayAccess getInstance() {
 		return instance;
 	}
 	
 	public DayAccess(Context context) {
 		super();
 		this.context = context;
 	}
 	
 	public Context getContext() {
 		return context;
 	}
 	
 	@Override
 	public int delete(Uri uri, String selection, String[] selectionArgs) {
 		int count = getContext().getContentResolver().delete(uri, selection, selectionArgs);
 		getContext().getContentResolver().notifyChange(uri, null);
 		return count;
 	}
 	
 	public int deleteTimestamps(Day day) {
 		Cursor c = null;
 		int delRows = 0;
 		try {
 			c = day.getTimestamps();
 			while (c.moveToNext()) {
 				// delete timestamp
 				delRows += TimestampAccess.getInstance().delete(c);
 			}
 		} finally {
 			if (c != null) {
 				c.close();
 			}
			context.startService(new Intent(context, UpdateAppWidgetService.class));
 		}
 		return delRows;
 	}
 	
 	@Override
 	public Uri insert(Uri uri, ContentValues initialValues) {
 		Uri ret = getContext().getContentResolver().insert(uri, initialValues);
 		getContext().getContentResolver().notifyChange(ret, null);
 		return ret;
 	}
 	
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
 		return getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
 	}
 	
 	@Override
 	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
 		int count = getContext().getContentResolver().update(uri, values, selection, selectionArgs);
 		getContext().getContentResolver().notifyChange(uri, null);
 		return count;
 	}
 	
 	public void insert(Day day) {
 		Uri uri = insert(DB.Days.CONTENT_URI, day.getValues());
 		long id = ContentUris.parseId(uri);
 		if (id > 0) {
 			day.setId(id);
 		}
 	}
 	
 	public boolean hasDayRef(long dayref) {
 		Cursor c = null;
 		try {
 			c = query(Days.CONTENT_URI, DB.Days.PROJECTTION_DAYREF, DB.Days.NAME_DAYREF + "=" + dayref, null,
 					DB.Days.DEFAULT_SORTORDER);
 			return c.moveToFirst();
 		} finally {
 			if (c != null) {
 				c.close();
 				c = null;
 			}
 			
 		}
 	}
 	
 	public Cursor query(String selection) {
 		return query(selection, Days.DEFAULT_SORTORDER);
 	}
 	
 	public Cursor query(String selection, String sortOrder) {
 		return query(DB.Days.CONTENT_URI, DB.Days.DEFAULT_PROJECTION, selection, null, sortOrder);
 	}
 	
 	public Day getOrCreateDay(long dayref) {
 		Day d;
 		Cursor c = null;
 		try {
 			c = query(Days.NAME_DAYREF + "=" + dayref);
 			if (c.moveToFirst()) {
 				d = new Day(c);
 			} else {
 				d = new Day(dayref);
 			}
 		} finally {
 			if (c != null) {
 				c.close();
 			}
 		}
 		return d;
 	}
 	
 	public void insertOrUpdate(Day day) {
 		if (day.getId() > -1) {
 			update(day);
 		} else {
 			insert(day);
 		}
 	}
 	
 	public void update(Day day) {
 		update(Days.CONTENT_URI, day.getValues(), DB.NAME_ID + "=" + day.getId(), null);
 	}
 	
 	/**
 	 * @param timestamp
 	 *            Timestamp to recalculate or null to work on all days
 	 * @param progressDialog
 	 */
 	public void recalculateDayFromTimestamp(Timestamp timestamp, ProgressDialog progressDialog) {
 		String selection = null;
 		String dayDeleteSelection = DB.Days.NAME_FIXED + "=0";
 		if (timestamp != null) {
 			selection = DB.NAME_ID + "=" + timestamp.getId();
 			dayDeleteSelection = dayDeleteSelection + " and " + selection;
 		}
 		// delete all days
 		// dayAccess.delete(Days.CONTENT_URI, dayDeleteSelection, null);
 		TimestampAccess timestampAccess = TimestampAccess.getInstance();
 		Cursor c = timestampAccess.query(selection, Timestamps.REVERSE_SORTORDER);
 		SortedSet<Long> dayRefs = new TreeSet<Long>();
 		int i = 0;
 		progressDialog.setMax(c.getCount() * 2);
 		while (c.moveToNext()) {
 			progressDialog.setProgress(i++);
 			Timestamp ts = new Timestamp(c);
 			long dayref = ts.getDayRef();
 			Day curDay = getOrCreateDay(dayref);
 			if (curDay.isFixed()) {
 				continue;
 			}
 			if (dayRefs.add(dayref)) {
 				Log.i(LOG_TAG, "Added day " + dayref + " for recalculation");
 			}
 			ts.setDayRef(dayref);
 			timestampAccess.update(DB.Timestamps.CONTENT_URI, ts.getValues(), DB.NAME_ID + "=" + ts.getId(), null);
 		}
 		c.close();
 		Iterator<Long> iterator = dayRefs.iterator();
 		// i = 0;
 		// progressDialog.setProgress(0);
 		while (iterator.hasNext()) {
 			progressDialog.setProgress(i++);
 			Long dayRef = iterator.next();
 			recalculate(context, dayRef);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param currentDay
 	 * @return the day before or null if none exists
 	 */
 	private Day getDayBefore(Day currentDay) {
 		Day d = null;
 		Cursor c = query(Days.NAME_DAYREF + "<" + currentDay.getDayRef(), Days.DEFAULT_SORTORDER);
 		if (c.moveToFirst()) {
 			d = new Day(c);
 		}
 		c.close();
 		return d;
 	}
 	
 	public void recalculate(Context context, long dayRef) {
 		if (dayRef < 1) {
 			return;
 		}
 		Day day = getOrCreateDay(dayRef);
 		recalculate(context, day);
 	}
 	
 	public void recalculate(Context context, Day day) {
 		// if (dayRef < 1) {
 		// return;
 		// }
 		// Day day = getOrCreateDay(dayRef);
 		long dayRef = day.getDayRef();
 		Day previousDay = getDayBefore(day);
 		if (previousDay == null) {
 			previousDay = new Day(0);
 		}
 		Log.i(LOG_TAG, "Recalculating " + dayRef + " with prev day " + previousDay.getDayRef());
 		long worked = 0;
 		// calculate for timestamps
 		Cursor c = day.getTimestamps();
 		boolean error = false;
 		while (c.moveToNext()) {
 			// what a timestamp is in an other day?
 			Timestamp t1 = new Timestamp(c);
 			if (t1.getTimestampType() == Timestamp.TYPE_IN) {
 				if (c.moveToNext()) {
 					Timestamp t2 = new Timestamp(c);
 					long diff = (t2.getTimestamp() - t1.getTimestamp());
 					worked = worked + diff;
 					Log.i(LOG_TAG, "Worked " + diff / HOURS_IN_MILLIES + " form " + t1.toString() + " to "
 							+ t2.toString());
 				} else {
 					error = true;
 				}
 			} else {
 				error = true;
 			}
 		}
 		if (!c.isClosed()) {
 			c.close();
 		}
 		day.setError(error);
 		// if (day.getHoursTarget() == 0f) {
 		// day.setHoursTarget(getHoursTargetDefault());
 		// }
 		day.setHoursTarget(day.getHoursTarget() - day.getHolyday() * day.getHoursTarget());
 		float hoursWorked = worked / HOURS_IN_MILLIES;
 		float overtime = hoursWorked - day.getHoursTarget();
 		Log.i(LOG_TAG, "Total hours worked: " + hoursWorked + " yields overtime: " + overtime);
 		day.setHoursWorked(hoursWorked);
 		if (!day.isFixed()) {
 			day.setOvertime(previousDay.getOvertime() + overtime);
 			day.setHolydayLeft(previousDay.getHolydayLeft() - day.getHolyday());
 		}
 		Log.w(Logger.LOG_TAG, "Recalculated " + dayRef);
 		insertOrUpdate(day);
 	}
 	
 	public static long dayRefFromTimestamp(long timestamp) {
 		String timeString = dayRefDateFormat.format(new Date(timestamp));
 		return Long.parseLong(timeString);
 	}
 	
 	public static long getNextFreeDayref(long timestamp) {
 		long dayref = dayRefFromTimestamp(timestamp);
 		while (exists(dayref)) {
 			dayref++;
 		}
 		return dayref;
 	}
 	
 	private static boolean exists(long dayref) {
 		Cursor c = null;
 		try {
 			c = getInstance().query(Days.NAME_DAYREF + "=" + dayref);
 			return c.moveToFirst();
 		} finally {
 			if (c != null) {
 				c.close();
 			}
 		}
 	}
 	
 }
