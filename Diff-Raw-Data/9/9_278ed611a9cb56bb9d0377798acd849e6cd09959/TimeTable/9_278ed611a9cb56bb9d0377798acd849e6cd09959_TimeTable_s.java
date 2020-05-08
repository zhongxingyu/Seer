 package com.lsg.app;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Calendar;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteStatement;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Messenger;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.CursorAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.lsg.app.interfaces.SelectedCallback;
 import com.lsg.app.lib.SlideMenu;
 import com.lsg.app.lib.TitleCompat;
 import com.lsg.app.lib.TitleCompat.HomeCall;
 import com.lsg.app.lib.TitleCompat.RefreshCall;
 
 public class TimeTable extends Activity implements SelectedCallback, HomeCall, RefreshCall, WorkerService.WorkerClass {
 	public static class TimetableAdapter extends CursorAdapter {
 
 		class TimetableItem {
 			public LinearLayout lay;
 			public TextView break_surveillance;
 			public TextView timetable_day;
 			public TextView timetable_hour;
 			public TextView header;
 			public TextView subtitle;
 			public TextView timetable_room;
 		}
 
 		public TimetableAdapter(Context context, Cursor c) {
 			super(context, c, false);
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View rowView = inflater
 					.inflate(R.layout.timetable_item, null, true);
 			TimetableItem holder = new TimetableItem();
 			holder.lay = (LinearLayout) rowView
 					.findViewById(R.id.timetable_lay);
 			holder.break_surveillance = (TextView) rowView.findViewById(R.id.break_surveillance);
 			holder.timetable_day = (TextView) rowView
 					.findViewById(R.id.timetable_day);
 			holder.timetable_hour = (TextView) rowView
 					.findViewById(R.id.timetable_hour);
 			holder.header = (TextView) rowView
 					.findViewById(R.id.timetable_subject);
 			holder.subtitle = (TextView) rowView
 					.findViewById(R.id.timetable_teacher);
 			holder.timetable_room = (TextView) rowView
 					.findViewById(R.id.timetable_room);
 			if (Functions.getSDK() < 11)
 				holder.timetable_hour
 						.setBackgroundResource(R.layout.divider_gradient);
 			rowView.setTag(holder);
 			return rowView;
 		}
 
 		@Override
 		public void bindView(View view, Context context, Cursor cursor) {
 			TimetableItem holder = (TimetableItem) view.getTag();
 			/*
 			 * int position = cursor.getPosition(); if(position == 0) {
 			 * holder.timetable_day.setVisibility(View.VISIBLE);
 			 * holder.timetable_day
 			 * .setText(context.getResources().getStringArray
 			 * (R.array.days)[cursor
 			 * .getInt(cursor.getColumnIndex(Functions.DB_DAY))]); } else
 			 */
 			holder.timetable_day.setVisibility(View.GONE);
 			int hour = cursor.getInt(cursor.getColumnIndex(Functions.DB_HOUR)) + 1;
 			String when = Integer.valueOf(hour).toString();
 			int i = 1;
 			int length = cursor.getInt(cursor
 					.getColumnIndex(Functions.DB_LENGTH));
 			while (i < length) {
 				when += ", " + Integer.valueOf(hour + i).toString();
 				i++;
 			}
 			if (cursor
 					.getString(cursor.getColumnIndex(Functions.DB_VERTRETUNG)) != null
 					&& cursor.getString(
 							cursor.getColumnIndex(Functions.DB_VERTRETUNG))
 							.equals("true"))
 				holder.lay.setBackgroundResource(R.layout.background_info);
 			else
 				holder.lay.setBackgroundResource(R.layout.background);
 			holder.timetable_hour.setText(when + ". "
 					+ context.getString(R.string.hour));
 			holder.timetable_room
 					.setText(context.getString(R.string.room)
 							+ " "
 							+ cursor.getString(cursor
 									.getColumnIndex(Functions.DB_ROOM)));
 			String subtitle;
 			if (cursor.getColumnIndex(Functions.DB_BREAK_SURVEILLANCE) != -1) {
 				if (!cursor.getString(
 						cursor.getColumnIndex(Functions.DB_BREAK_SURVEILLANCE))
 						.equals("null")) {
 					holder.break_surveillance.setVisibility(View.VISIBLE);
 					holder.break_surveillance.setText(context.getString(R.string.break_surveillance) + " " + cursor.getString(cursor
 							.getColumnIndex(Functions.DB_BREAK_SURVEILLANCE)));
 				} else
 					holder.break_surveillance.setVisibility(View.GONE);
 				subtitle = Functions.DB_CLASS;
 			} else {
 				holder.break_surveillance.setVisibility(View.GONE);
 				subtitle = Functions.DB_LEHRER;
 			}
 			if(cursor.getString(cursor
 					.getColumnIndex(Functions.DB_ROOM)).equals("null"))
 				holder.timetable_room.setVisibility(View.GONE);
 			else
 				holder.timetable_room.setVisibility(View.VISIBLE);
 			holder.header.setText(cursor.getString(cursor
 					.getColumnIndex(Functions.DB_FACH)));
 			holder.subtitle.setText(cursor.getString(cursor
 					.getColumnIndex(subtitle)));
 			if(holder.subtitle.getText().equals("null"))
 				holder.subtitle.setVisibility(View.GONE);
 			else
 				holder.subtitle.setVisibility(View.VISIBLE);
 		}
 	}
 
 	public class TimeTableViewPagerAdapter extends PagerAdapter implements
 			PagerTitles {
 		private String[] exclude_subjects = new String[6];
 		private final SQLiteDatabase myDB;
 		private Cursor[] timetable_cursors = new Cursor[5];
 		private TimetableAdapter[] timetableadapters = new TimetableAdapter[5];
 		private final Context context;
 		private final SharedPreferences prefs;
 		private String[] titles = new String[5];
 		private String klasse;
 		private String teacher;
 		private boolean ownClass;
 
 		public TimeTableViewPagerAdapter(TimeTable act) {
 			prefs = PreferenceManager.getDefaultSharedPreferences(act);
 			if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)) {
 				Log.d("teacher", prefs.getString(Functions.TEACHER_SHORT, ""));
 				setTeacher(prefs.getString(Functions.TEACHER_SHORT, ""));
 			}
 			else
 				setClass("", true);
 			context = (Context) act;
 			titles = getResources().getStringArray(R.array.days);
 
 			myDB = context.openOrCreateDatabase(Functions.DB_NAME,
 					Context.MODE_PRIVATE, null);
 			for(int i = 0; i < 5; i++)
 				timetableadapters[i] =  new TimetableAdapter(context, timetable_cursors[i]);
 			
 			SQLiteStatement num_rows = myDB
 					.compileStatement("SELECT COUNT(*) FROM "
 							+ Functions.DB_TIME_TABLE);
 			long count = num_rows.simpleQueryForLong();
 			if (count == 0)
 				act.updateTimeTable();
 			num_rows.close();
 			updateCursor();
 		}
 
 		@Override
 		public int getCount() {
 			return 5;
 		}
 
 		@Override
 		public String getTitle(int pos) {
 			return titles[pos];
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			return titles[position];
 		}
 
 		@Override
 		public Object instantiateItem(View pager, int position) {
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.list,
 					null);
 			ListView lv = (ListView) lay.findViewById(android.R.id.list);
 			lv.setAdapter(timetableadapters[position]);
 			lv.setOnItemClickListener(new OnItemClickListener() {
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view,
 						int position, long id) {
 					Cursor c = myDB
 							.query(Functions.DB_TIME_TABLE, new String[] {
 									Functions.DB_REMOTE_ID, Functions.DB_VERTRETUNG }, Functions.DB_ROWID
 									+ "=?", new String[] { Long.valueOf(id)
 									.toString() }, null, null, null);
 					c.moveToFirst();
 					if (c.getString(c.getColumnIndex(Functions.DB_VERTRETUNG))
 							.equals("true")) {
 						Cursor d = myDB.query(Functions.DB_VPLAN_TABLE,
 								new String[] { Functions.DB_VERTRETUNGSTEXT,
 										Functions.DB_KLASSE, Functions.DB_FACH,
 										Functions.DB_STUNDE, Functions.DB_TYPE,
 										Functions.DB_LEHRER },
 								Functions.DB_ROWID + "=?", new String[] {c.getString(c.getColumnIndex(Functions.DB_REMOTE_ID))},
 								null, null, null);
 						String vtext = (!(d.getString(d
 								.getColumnIndex(Functions.DB_VERTRETUNGSTEXT)))
 								.equals("null")) ? d.getString(d
 								.getColumnIndex(Functions.DB_VERTRETUNGSTEXT))
 								+ "\n" : "";
 						AlertDialog.Builder builder = new AlertDialog.Builder(
 								context);
 						builder.setTitle(
 								d.getString(d
 										.getColumnIndex(Functions.DB_KLASSE)))
 								.setMessage(
 										d.getString(d
 												.getColumnIndex(Functions.DB_FACH))
 												+ " / "
 												+ d.getString(d
 														.getColumnIndex(Functions.DB_STUNDE))
 												+ ". "
 												+ context
 														.getString(R.string.hour)
 												+ "\n"
 												+ vtext
 												+ d.getString(d
 														.getColumnIndex(Functions.DB_TYPE))
 												+ " "
 												+ context
 														.getString(R.string.at)
 												+ " "
 												+ d.getString(d
 														.getColumnIndex(Functions.DB_LEHRER)))
 								.setCancelable(true)
 								.setNeutralButton(
 										context.getString(R.string.ok),
 										new DialogInterface.OnClickListener() {
 											public void onClick(
 													DialogInterface dialog,
 													int id) {
 												dialog.cancel();
 											}
 										});
 						AlertDialog alert = builder.create();
 						alert.show();
 					}
 				}
 			});
 			lv.setEmptyView(lay.findViewById(R.id.list_view_empty));
 			((TextView) lay.findViewById(R.id.list_view_empty)).setText(R.string.timetable_empty);
 			((ViewPager) pager).addView(lay, 0);
 			return lay;
 		}
 
 		public void updateExclude() {
 			if (ownClass) {
 				exclude_subjects[1] = (prefs.getString(Functions.GENDER, "")
 						.equals("m")) ? "Sw" : "Sm";
 				if (prefs.getString(Functions.RELIGION, "").equals(
 						Functions.KATHOLISCH)) {
 					exclude_subjects[2] = Functions.EVANGELISCH;
 					exclude_subjects[3] = Functions.ETHIK;
 				} else if (prefs.getString(Functions.RELIGION, "").equals(
 						Functions.EVANGELISCH)) {
 					exclude_subjects[2] = Functions.KATHOLISCH;
 					exclude_subjects[3] = Functions.ETHIK;
 				} else {
 					exclude_subjects[2] = Functions.KATHOLISCH;
 					exclude_subjects[3] = Functions.EVANGELISCH;
 				}
 			}
 			else {
 				exclude_subjects[1] = "%";
 				exclude_subjects[2] = "%";
 				exclude_subjects[3] = "%";
 			}
 		}
 
 		public void setClass(String klasse, boolean ownClass) {
 			this.klasse = klasse;
 			this.ownClass = ownClass;
 			if(ownClass)
 				this.klasse = prefs.getString(Functions.FULL_CLASS, "null");
 			updateExclude();
 		}
 		public void setTeacher(String teacher) {
 			this.teacher = teacher;
 			this.klasse = null;
 		}
 
 		public void updateCursor() {
 			if (this.klasse == null) {
 				for (int i = 0; i < getCount(); i++) {
 					timetable_cursors[i] = myDB.query(
 							Functions.DB_TIME_TABLE_TEACHERS, new String[] {
 									Functions.DB_ROWID,
 									Functions.DB_BREAK_SURVEILLANCE, Functions.DB_RAW_FACH, Functions.DB_VERTRETUNG,
 									Functions.DB_FACH, Functions.DB_ROOM,
 									Functions.DB_CLASS, Functions.DB_LENGTH,
 									Functions.DB_HOUR, Functions.DB_DAY },
 							Functions.DB_SHORT + "=? AND " + Functions.DB_DAY + "=?",
 							new String[] { this.teacher, Integer.valueOf(i).toString() }, null, null, null);
 					timetableadapters[i].changeCursor(timetable_cursors[i]);
 				}
 			} else {
 				exclude_subjects[4] = (this.ownClass) ? "1" : "%";
 				if (ownClass)
 					exclude_subjects[5] = "%" + klasse.substring(0, 2) + "%"
 							+ klasse.substring(2, 3) + "%";
 				else
 					exclude_subjects[5] = klasse;
 				String wherecond = Functions.DB_DAY + "=? AND  "
 						+ Functions.DB_RAW_FACH + " != ? AND "
 						+ Functions.DB_RAW_FACH + " != ? AND "
 						+ Functions.DB_RAW_FACH + " != ? AND "
 						+ Functions.DB_DISABLED + " != ? AND "
 						+ Functions.DB_CLASS + " LIKE ?";
 				for (int i = 0; i < getCount(); i++) {
 					exclude_subjects[0] = Integer.valueOf(i).toString();
 					timetable_cursors[i] = myDB.query(Functions.DB_TIME_TABLE,
 							new String[] { Functions.DB_ROWID,
 									Functions.DB_LEHRER, Functions.DB_FACH,
 									Functions.DB_ROOM, Functions.DB_VERTRETUNG,
 									Functions.DB_LENGTH, Functions.DB_HOUR,
 									Functions.DB_DAY, Functions.DB_RAW_FACH },
 							wherecond, exclude_subjects, null, null, null);
 					timetableadapters[i].changeCursor(timetable_cursors[i]);
 				}
 			}
 		}
 
 		public void closeCursorsDB() {
 			for(int i = 0; i < getCount(); i++)
 				timetable_cursors[i].close();
 			myDB.close();
 		}
 
 		@Override
 		public void destroyItem(View pager, int position, Object view) {
 			((ViewPager) pager).removeView((View) view);
 		}
 
 		@Override
 		public boolean isViewFromObject(View view, Object object) {
 			return view.equals(object);
 		}
 
 		@Override
 		public void finishUpdate(View view) {
 		}
 
 		@Override
 		public void restoreState(Parcelable p, ClassLoader c) {
 		}
 
 		@Override
 		public Parcelable saveState() {
 			return null;
 		}
 
 		@Override
 		public void startUpdate(View view) {
 		}
 	}
 
 	public static class TimeTableUpdater {
 		private Context context;
 
 		TimeTableUpdater(Context c) {
 			context = c;
 		}
 
 		public String[] updatePupils() {
 			SharedPreferences prefs = PreferenceManager
 					.getDefaultSharedPreferences(context);
 			String add = "";
 			try {
 				add = "&"
 						+ URLEncoder.encode("date", "UTF-8")
 						+ "="
 						+ URLEncoder.encode(
 								prefs.getString("timetable_date", ""), "UTF-8")
 						+ "&"
 						+ URLEncoder.encode("time", "UTF-8")
 						+ "="
 						+ URLEncoder.encode(
 								prefs.getString("timetable_time", ""), "UTF-8")
 						+ "&"
 						+ URLEncoder.encode("class", "UTF-8")
 						+ "="
 						+ URLEncoder.encode(
 								prefs.getString(Functions.FULL_CLASS, ""),
 								"UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				Log.d("encoding", e.getMessage());
 			}
 			String get = Functions.getData(Functions.TIMETABLE_URL, context,
 					true, add);
 			if (!get.equals("networkerror") && !get.equals("loginerror")
 					&& !get.equals("noact")) {
 				try {
 					JSONArray classes = new JSONArray(get);
 					SQLiteDatabase myDB = context.openOrCreateDatabase(
 							Functions.DB_NAME, Context.MODE_PRIVATE, null);
 					// clear timetable
 					myDB.delete(Functions.DB_TIME_TABLE, null, null);
 					// clear headers
 					myDB.delete(Functions.DB_TIME_TABLE_HEADERS_PUPILS, null, null);
 					for (int i = 0; i < classes.length(); i++) {
 						JSONArray one_class = classes.getJSONArray(i);
 						JSONObject class_info = one_class.getJSONObject(0);
 						String date = class_info.getString("date");
 						String time = class_info.getString("time");
 						String one = class_info.getString("one");
 						String two = class_info.getString("two");
 						String klasse = class_info.getString("klasse");
 						SharedPreferences.Editor edit = prefs.edit();
 						edit.putString("timetable_date", date);
 						edit.putString("timetable_time", time);
 						ContentValues headerval = new ContentValues();
 						headerval.put(Functions.DB_TEACHER, one);
 						headerval.put(Functions.DB_SECOND_TEACHER, two);
 						headerval.put(Functions.DB_KLASSE, klasse);
 						myDB.insert(Functions.DB_TIME_TABLE_HEADERS_PUPILS, null, headerval);
 						edit.commit();
 						for(int ii = 1; ii < one_class.length(); ii++) {
 							JSONObject jObject = one_class.getJSONObject(ii);
 							ContentValues values = new ContentValues();
 							values.put(Functions.DB_LEHRER,
 									jObject.getString("teacher"));
 							values.put(Functions.DB_FACH,
 									jObject.getString("subject"));
 							values.put(Functions.DB_RAW_FACH,
 									jObject.getString("rawsubject"));
 							values.put(Functions.DB_ROOM,
 									jObject.getString("room"));
 							values.put(Functions.DB_LENGTH,
 									jObject.getInt("length"));
 							values.put(Functions.DB_DAY, jObject.getInt("day"));
 							values.put(Functions.DB_HOUR,
 									jObject.getInt("hour"));
 							values.put(Functions.DB_CLASS,
 									jObject.getString("class"));
 							values.put(Functions.DB_RAW_LEHRER,
 									jObject.getString("rawteacher"));
 							Cursor c = myDB
 									.query(Functions.DB_EXCLUDE_TABLE,
 											new String[] { Functions.DB_ROWID },
 											Functions.DB_TEACHER + "=? AND "
 													+ Functions.DB_RAW_FACH
 													+ "=? AND "
 													+ Functions.DB_HOUR
 													+ "=? AND "
 													+ Functions.DB_DAY + "=?",
 											new String[] {
 													values.getAsString(Functions.DB_RAW_LEHRER),
 													values.getAsString(Functions.DB_RAW_FACH),
 													values.getAsString(Functions.DB_HOUR),
 													values.getAsString(Functions.DB_DAY) },
 											null, null, null);
 //							c.moveToFirst();
 							if (c.getCount() > 0) {
 								values.put(Functions.DB_DISABLED, 1);
 							} else
 								values.put(Functions.DB_DISABLED, 2);
 							c.close();
 							myDB.insert(Functions.DB_TIME_TABLE, null, values);
 						}
 					}
 					myDB.close();
 				} catch (JSONException e) {
 					Log.d("json", e.getMessage());
 					return new String[] { "json",
 							context.getString(R.string.jsonerror) };
 				}
 			} else if (get.equals("networkerror")) {
 				return new String[] { "networkerror",
 						context.getString(R.string.networkerror) };
 			} else if (get.equals("loginerror"))
 				return new String[] { "loginerror",
 						context.getString(R.string.loginerror) };
 			return new String[] { "success", "" };
 		}
 		public String[] updateTeachers() {
 			SharedPreferences prefs = PreferenceManager
 					.getDefaultSharedPreferences(context);
 			String add = "";
 			try {
 				add = "&"
 						+ URLEncoder.encode("date", "UTF-8")
 						+ "="
 						+ URLEncoder.encode(
 								prefs.getString("timetable_teachers_date", ""), "UTF-8")
 						+ "&"
 						+ URLEncoder.encode("time", "UTF-8")
 						+ "="
 						+ URLEncoder.encode(
 								prefs.getString("timetable_teachers_time", ""), "UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				Log.d("encoding", e.getMessage());
 			}
 			String get = Functions.getData(Functions.TIMETABLE_TEACHERS_URL, context,
 					true, add);
 			if (!get.equals("networkerror") && !get.equals("loginerror")
 					&& !get.equals("noact")) {
 				try {
 					JSONArray classes = new JSONArray(get);
 					SQLiteDatabase myDB = context.openOrCreateDatabase(
 							Functions.DB_NAME, Context.MODE_PRIVATE, null);
 					// clear timetable
 					myDB.delete(Functions.DB_TIME_TABLE_TEACHERS, null, null);
 					// clear headers
 					myDB.delete(Functions.DB_TIME_TABLE_HEADERS_TEACHERS, null, null);
 					for (int i = 0; i < classes.length(); i++) {
 						JSONArray one_teacher = classes.getJSONArray(i);
 						JSONObject teacher_info = one_teacher.getJSONObject(0);
 						String date = teacher_info.getString("date");
 						String time = teacher_info.getString("time");
 						String name = teacher_info.getString("name");
 						String short_ = teacher_info.getString("short");
 						SharedPreferences.Editor edit = prefs.edit();
 						edit.putString("timetable_teachers_date", date);
 						edit.putString("timetable_teachers_time", time);
 						ContentValues headerval = new ContentValues();
 						headerval.put(Functions.DB_TEACHER, name);
 						headerval.put(Functions.DB_SHORT, short_);
 						myDB.insert(Functions.DB_TIME_TABLE_HEADERS_TEACHERS, null, headerval);
 						edit.commit();
 						for(int ii = 1; ii < one_teacher.length(); ii++) {
 							JSONObject jObject = one_teacher.getJSONObject(ii);
 							//Log.d("json", jObject.toString());
 							ContentValues values = new ContentValues();
 							values.put(Functions.DB_SHORT, short_);
 							values.put(Functions.DB_BREAK_SURVEILLANCE,
 									jObject.getString("pausenaufsicht"));
 							values.put(Functions.DB_RAW_FACH, jObject.getString("rawfach"));
 							values.put(Functions.DB_FACH,
 									jObject.getString("fach"));
 							values.put(Functions.DB_ROOM,
 									jObject.getString("room"));
 							values.put(Functions.DB_CLASS,
 									jObject.getString("class"));
 							values.put(Functions.DB_LENGTH, jObject.getInt("length"));
 							values.put(Functions.DB_DAY, jObject.getInt("day"));
 							values.put(Functions.DB_HOUR,
 									jObject.getInt("hour"));
 							myDB.insert(Functions.DB_TIME_TABLE_TEACHERS, null, values);
 						}
 					}
 					myDB.close();
 				} catch (JSONException e) {
 					Log.d("json", e.getMessage());
 					return new String[] { "json",
 							context.getString(R.string.jsonerror) };
 				}
 			} else if (get.equals("networkerror")) {
 				return new String[] { "networkerror",
 						context.getString(R.string.networkerror) };
 			} else if (get.equals("loginerror"))
 				return new String[] { "loginerror",
 						context.getString(R.string.loginerror) };
 			return new String[] { "success", "" };
 		}
 	}
 
 	public static void blacklistTimeTable(Context context) {
 		Log.d("timetable", "blacklisttimetable");
 		SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME,
 				Context.MODE_PRIVATE, null);
 		Cursor allSubjects = myDB.query(Functions.DB_TIME_TABLE, new String[] { Functions.DB_ROWID,
 				Functions.DB_DAY, Functions.DB_HOUR, Functions.DB_RAW_FACH,
 				Functions.DB_RAW_LEHRER }, null, null, null, null, null);
 		allSubjects.moveToFirst();
 		ContentValues vals = new ContentValues();
 		vals.put(Functions.DB_DISABLED, 2);
 		myDB.update(Functions.DB_TIME_TABLE, vals, null, null);
 		if(allSubjects.getCount() > 0)
 		do {
 			Cursor exclude = myDB.query(
 					Functions.DB_EXCLUDE_TABLE,
 					new String[] { Functions.DB_ROWID },
 					Functions.DB_TEACHER + "=? AND " + Functions.DB_RAW_FACH
  + "=? AND "
 										+ Functions.DB_HOUR + "=? AND "
 										+ Functions.DB_DAY + "=?",
 								new String[] {
 										allSubjects.getString(allSubjects
 												.getColumnIndex(Functions.DB_RAW_LEHRER)),
 										allSubjects.getString(allSubjects
 												.getColumnIndex(Functions.DB_RAW_FACH)),
 										allSubjects.getString(allSubjects
 												.getColumnIndex(Functions.DB_HOUR)),
 										allSubjects.getString(allSubjects
 												.getColumnIndex(Functions.DB_DAY)) },
 								null, null, null);
 				Cursor exclude_oldstyle = myDB
 						.query(Functions.DB_EXCLUDE_TABLE,
 								new String[] { Functions.DB_ROWID },
 								Functions.DB_RAW_FACH + "=? AND "
 										+ Functions.DB_TYPE + "=?",
 								new String[] {
 										allSubjects.getString(allSubjects
 												.getColumnIndex(Functions.DB_RAW_FACH)),
 										"oldstyle" }, null, null, null);
 				if (exclude.getCount() > 0 || exclude_oldstyle.getCount() > 0) {
 					myDB.execSQL(
 							"UPDATE " + Functions.DB_TIME_TABLE + " SET "
 									+ Functions.DB_DISABLED + "=? WHERE "
 									+ Functions.DB_ROWID + "=?",
 							new String[] {
 									"1",
 									allSubjects.getString(allSubjects
 											.getColumnIndex(Functions.DB_ROWID)) });
 				}
 				exclude.close();
 			} while (allSubjects.moveToNext());
 		allSubjects.close();
 		try {
 			myDB.close();
 		} catch (Exception e) {
 
 		}
 	}
 	private ProgressDialog loading;
 	private TimeTableViewPagerAdapter viewpageradap;
 	private ViewPager pager;
 	private SlideMenu slidemenu;
 	private TextView footer;
 	private TitleCompat titlebar;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		titlebar = new TitleCompat(this, true);
 		Functions.setupDB(this);
 		super.onCreate(savedInstanceState);
 		if (!((SharedPreferences) PreferenceManager
 				.getDefaultSharedPreferences(this)).getBoolean(
 				Functions.IS_LOGGED_IN, false)) {
 			if (!((SharedPreferences) PreferenceManager
 					.getDefaultSharedPreferences(this)).getString("username",
 					"null").equals("null")) {
 				Toast.makeText(this,
 						getString(R.string.setup_assistant_opening),
 						Toast.LENGTH_LONG).show();
 				startActivity(new Intent(TimeTable.this, SetupAssistant.class));
 			} else {
 				Toast.makeText(this, getString(R.string.run_setup_assistant),
 						Toast.LENGTH_LONG).show();
 				startActivity(new Intent(TimeTable.this, Events.class));
 			}
 			Functions.init(this);
 			this.finish();
 			return;
 		}
 		setTitle(R.string.timetable);
 		Functions.setTheme(false, true, this);
 		getWindow().setBackgroundDrawableResource(R.layout.background);
 		setContentView(R.layout.viewpager);
 		viewpageradap = new TimeTableViewPagerAdapter(this);
 		pager = (ViewPager) findViewById(R.id.viewpager);
 		pager.setAdapter(viewpageradap);
 		// pager.setOnPageChangeListener(this);
 		pager.setPageMargin(Functions.dpToPx(40, this));
 		pager.setPageMarginDrawable(R.layout.viewpager_margin);
 
 		// get current day
 		Calendar cal = Calendar.getInstance();
 		int day;
 		switch (cal.get(Calendar.DAY_OF_WEEK)) {
 		case Calendar.MONDAY:
 			day = 0;
 			break;
 		case Calendar.TUESDAY:
 			day = 1;
 			break;
 		case Calendar.WEDNESDAY:
 			day = 2;
 			break;
 		case Calendar.THURSDAY:
 			day = 3;
 			break;
 		case Calendar.FRIDAY:
 			day = 4;
 			break;
 		default:
 			day = 0;
 			break;
 		}
 		pager.setCurrentItem(day, true);
 		slidemenu = new SlideMenu(this, TimeTable.class);
 		slidemenu.checkEnabled();
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		footer = (TextView) findViewById(R.id.footer_text);
 		titlebar.init(this);
 		titlebar.addRefresh(this);
 		titlebar.setTitle(getTitle());
 		if ((prefs.getBoolean(Functions.RIGHTS_TEACHER, false) || prefs.getBoolean(Functions.RIGHTS_PUPIL,
 				false))) {
 			titlebar.addSpinnerNavigation( this, (prefs.getBoolean(Functions.RIGHTS_ADMIN, false)) ? R.array.timetable_actions : R.array.timetable_actions_teachers);
 		}
 		//let service check for update
 		Intent intent = new Intent(TimeTable.this, WorkerService.class);
 	    intent.putExtra(WorkerService.WHAT, 100);
 	    startService(intent);
 	}
 	public void showMine() {
 		((TextView) findViewById(R.id.footer_text)).setVisibility(View.GONE);
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)) {
 			viewpageradap.setTeacher(prefs.getString(Functions.TEACHER_SHORT, ""));
 			viewpageradap.updateCursor();
 		} else {
 			viewpageradap.setClass("", true);
 			viewpageradap.updateCursor();
 		}
 	}
 
 	public void showClasses() {
 		footer.setVisibility(View.VISIBLE);
 		final SQLiteDatabase myDB = openOrCreateDatabase(Functions.DB_NAME,
 				Context.MODE_PRIVATE, null);
 		final Cursor c = myDB.query(Functions.DB_TIME_TABLE_HEADERS_PUPILS,
 				new String[] { Functions.DB_ROWID, Functions.DB_KLASSE }, null,
 				null, null, null, null);
 		AlertDialog.Builder builder = new AlertDialog.Builder(TimeTable.this);
 		builder.setTitle(R.string.select_class);
 		builder.setCursor(c, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				c.moveToPosition(item);
 				viewpageradap.setClass(
 						c.getString(c.getColumnIndex(Functions.DB_KLASSE)),
 						false);
 				((TextView) findViewById(R.id.footer_text)).setText(c
 						.getString(c.getColumnIndex(Functions.DB_KLASSE)));
 				viewpageradap.updateCursor();
 				c.close();
 				myDB.close();
 			}
 		}, Functions.DB_KLASSE);
 		builder.setCancelable(false);
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public void showTeachers() {
 		footer.setVisibility(View.VISIBLE);
 		final SQLiteDatabase myDB = openOrCreateDatabase(Functions.DB_NAME,
 				Context.MODE_PRIVATE, null);
 		final Cursor c = myDB.query(Functions.DB_TIME_TABLE_HEADERS_TEACHERS,
 				new String[] { Functions.DB_ROWID, Functions.DB_TEACHER,
 						Functions.DB_SHORT }, null, null, null, null, null);
 		AlertDialog.Builder builder = new AlertDialog.Builder(TimeTable.this);
 		builder.setTitle(R.string.select_teacher);
 		builder.setCursor(c, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 				c.moveToPosition(item);
 				viewpageradap.setTeacher(c.getString(c
 						.getColumnIndex(Functions.DB_SHORT)));
 				((TextView) findViewById(R.id.footer_text)).setText(c
 						.getString(c.getColumnIndex(Functions.DB_TEACHER)));
 				viewpageradap.updateCursor();
 				c.close();
 				myDB.close();
 			}
 		}, Functions.DB_TEACHER);
 		builder.setCancelable(false);
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	private MenuItem refresh;
 	@TargetApi(11)
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.timetable, menu);
 		if(Functions.getSDK() < 11)
 			menu.removeItem(R.id.refresh);
 		else
 			refresh = menu.findItem(R.id.refresh);
 		if(refreshing && Functions.getSDK() >= 11)
 				refresh.setActionView(new ProgressBar(this));
 		else if(refreshing)
 			loading = ProgressDialog.show(this, null, "Lade...");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			onRefreshPress();
 			return true;
 		case android.R.id.home:
 			onHomePress();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	private boolean refreshing = false;
 	private static ServiceHandler hand;
 	@TargetApi(11)
 	public void updateTimeTable() {
 		refreshing = true;
 		final View actionView;
 		if (Functions.getSDK() >= 11) {
 			actionView = refresh.getActionView();
 			refresh.setActionView(new ProgressBar(this));
 		} else {
 			actionView = null;
 			loading = ProgressDialog.show(this, null,
 					"Lade...");
 		}
 		hand = new ServiceHandler(new ServiceHandler.ServiceHandlerCallback() {
 			@Override
 			public void onServiceError() {
 				// TODO Auto-generated method stub
 			}
 
 			@Override
 			public void onFinishedService() {
 				Log.d("service", "finished without error");
 				if (Functions.getSDK() >= 11)
 					refresh.setActionView(actionView);
 				else
 					loading.cancel();
 				refreshing = false;
 			}
 		});
 		Handler handler = hand.getHandler();
 		
 		Intent intent = new Intent(this, WorkerService.class);
 	    // Create a new Messenger for the communication back
 	    Messenger messenger = new Messenger(handler);
 	    intent.putExtra(WorkerService.MESSENGER, messenger);
 	    intent.putExtra(WorkerService.WORKER_CLASS, TimeTable.class.getCanonicalName());
 	    intent.putExtra(WorkerService.WHAT, WorkerService.UPDATE_ALL);
 	    startService(intent);
 	    Log.d("class", TimeTableUpdater.class.getCanonicalName());
 	}
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		try {
 		loading.cancel();
 		} catch(NullPointerException e) {
 			//no dialog
 		}
 		try {
 			viewpageradap.closeCursorsDB();
 		} catch(NullPointerException e) {
 			//viewpager not yet initialized
 		}
 	}
 	private int selPos;
 	private boolean suppressSelect = false;
 	@Override
 	public boolean selected(int position, long id) {
 		selPos = position;
 		switch(position) {
 		case 0:
 			showMine();
 			break;
 		case 1:
 			footer.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					showClasses();
 				}
 			});
 			if (!suppressSelect)
 				showClasses();
 			break;
 		case 2:
 			footer.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					showTeachers();
 				}
 			});
 			if (!suppressSelect)
 				showTeachers();
 			break;
 		default:
 			showMine();
 			break;
 		}
 		if (suppressSelect)
 			suppressSelect = false;
 		return false;
 	}
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 	  super.onSaveInstanceState(savedInstanceState);
 	  savedInstanceState.putInt("navlistselected", selPos);
 	  savedInstanceState.putBoolean("ownclass", viewpageradap.ownClass);
 	  savedInstanceState.putString("selclass", viewpageradap.klasse);
 	  savedInstanceState.putString("selshort", viewpageradap.teacher);
 	  savedInstanceState.putBoolean("refreshing", refreshing);
 	}
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		if (savedInstanceState.getString("selclass") != null) {
 			viewpageradap.setClass(
 					(String) savedInstanceState.getString("selclass"),
 					savedInstanceState.getBoolean("ownclass", true));
 			if(!savedInstanceState.getBoolean("ownclass", true)) {
 				footer.setVisibility(View.VISIBLE);
 				footer.setText(savedInstanceState.getString("selclass"));
 			}
 		}
 		else {
 			viewpageradap.setTeacher(savedInstanceState.getString("selshort"));
 			footer.setVisibility(View.VISIBLE);
 			footer.setText(savedInstanceState.getString("selshort"));
 		}
 		viewpageradap.updateCursor();
 		if(Functions.getSDK() >= 11) {
 			suppressSelect = true;
 			AdvancedWrapper adv = new AdvancedWrapper();
 			adv.setSelectedItem(savedInstanceState.getInt("navlistselected"), this);
 		}
 		Log.d("extras", savedInstanceState.toString());
 		//super.onRestoreInstanceState(savedInstanceState);
 		refreshing = savedInstanceState.getBoolean("refreshing");
 	}
 	@Override
 	public void onHomePress() {
 		slidemenu.show();
 	}
 	@Override
 	public void onRefreshPress() {
 		updateTimeTable();
 	}
 	public void update(int what, Context c) {
 		TimeTableUpdater udp = new TimeTableUpdater(c);
 		switch(what) {
 		case WorkerService.UPDATE_ALL:
 			udp.updatePupils();
 			udp.updateTeachers();
 			break;
 		case WorkerService.UPDATE_PUPILS:
 			udp.updatePupils();
 			break;
 		case WorkerService.UPDATE_TEACHERS:
 			udp.updateTeachers();
 			break;
 		}
 	}
 }
