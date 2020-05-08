 package com.lsg.app;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
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
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class VPlan extends Activity {
 	public class VPlanPagerAdapter extends PagerAdapter implements SQLlist, TextWatcher, PagerTitles {
 		private String[] where_conds = new String[4];
 		private String[] where_conds_events = new String[6];
 		private String[] exclude_subjects = new String[4];
 		private String[] titles = new String[3];
 		private final SQLiteDatabase myDB;
 		public Cursor cursor_all;
 		public Cursor cursor_mine;
 		public Cursor cursor_teachers;
 		private VPlan.VertretungAdapter vadapter_all;
 		private VPlan.VertretungAdapter vadapter_mine;
 		private VPlan.VertretungAdapter vadapter_teachers;
 		private String exclude_cond;
 		private String include_cond;
 		private VPlan act;
 		private final Context context;
 		private final SharedPreferences prefs;
 		
 		public VPlanPagerAdapter(VPlan act) {
 			where_conds[0] = "%";
 			where_conds[1] = "%";
 			where_conds[2] = "%";
 			where_conds[3] = "%";
 			where_conds_events[0] = "%";
 			where_conds_events[1] = "%";
 			where_conds_events[2] = "%";
 			where_conds_events[3] = "%";
 			where_conds_events[4] = "%";
 			where_conds_events[5] = "%";
 			titles[0] = "Meine Vertretungen";
 			titles[1] = "Schülervertretungen";
 			titles[2] = "Lehrervertretungen";
 			prefs = PreferenceManager.getDefaultSharedPreferences(act);
 			exclude_subjects[1] = (prefs.getString(Functions.GENDER, "").equals("m")) ? "Sw" : "Sm";
 			if(prefs.getString(Functions.RELIGION, "").equals(Functions.KATHOLISCH)) {
 				exclude_subjects[2] = Functions.EVANGELISCH;
 				exclude_subjects[3] = Functions.ETHIK;
 			} else if(prefs.getString(Functions.RELIGION, "").equals(Functions.EVANGELISCH)) {
 				exclude_subjects[2] = Functions.KATHOLISCH;
 				exclude_subjects[3] = Functions.ETHIK;
 			} else {
 				exclude_subjects[2] = Functions.KATHOLISCH;
 				exclude_subjects[3] = Functions.EVANGELISCH;
 			}
 			context = (Context) act;
 			this.act = act;
 			
 			myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
 			updateCondLists();
 			
 			SQLiteStatement num_rows = myDB.compileStatement("SELECT COUNT(*) FROM " + Functions.DB_VPLAN_TABLE);
 			long count = num_rows.simpleQueryForLong();
 			SQLiteStatement num_rows_2 = myDB.compileStatement("SELECT COUNT(*) FROM " + Functions.DB_VPLAN_TEACHER);
 			long count2 = num_rows_2.simpleQueryForLong();
 			if(count == 0 || count2 == 0)
 				act.updateVP();
 			num_rows.close();
			vadapter_mine = new VPlan.VertretungAdapter(context, cursor_mine,
					prefs.getBoolean(Functions.RIGHTS_TEACHER, false));
 			vadapter_all = new VPlan.VertretungAdapter(context, cursor_all, false);
 			vadapter_teachers = new VPlan.VertretungAdapter(context, cursor_teachers, true);
 			updateCursor();
 			}
 		
 		@Override
 		public int getCount() {
 			return 3;
 			}
 		
 		public String getTitle(int pos) {
 			return titles[pos];
 			}
 		 @Override
 	        public CharSequence getPageTitle (int position) {
 	            return titles[position];
 	        }
 		
 		@Override
 		public Object instantiateItem(View pager, int position) {
 				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				LinearLayout lay = (LinearLayout) inflater.inflate(R.layout.list, null);
 				ListView lv = (ListView) lay.findViewById(android.R.id.list);
 				//set header search bar
 				if(Functions.getSDK() < 11) {
 					View search = inflater.inflate(R.layout.search, null);
 					EditText searchEdit = (EditText) search.findViewById(R.id.search_edit);
 					searchEdit.addTextChangedListener(this);
 					lv.addHeaderView(search);
 					}
 				if(position == 0)
 					lv.setAdapter(vadapter_mine);
 				if(position == 1)
 					lv.setAdapter(vadapter_all);
 				if(position == 2)
 					lv.setAdapter(vadapter_teachers);
 				Functions.styleListView(lv, context);
 				act.registerForContextMenu(lv);
 				lv.setEmptyView(lay.findViewById(R.id.list_view_empty));
 				if(position == 0)
 					((TextView) lay.findViewById(R.id.list_view_empty)).setText(R.string.vplan_mine_empty);
 				if(position == 1)
 					((TextView) lay.findViewById(R.id.list_view_empty)).setText(R.string.vplan_empty);
 				if(position == 2)
 					((TextView) lay.findViewById(R.id.list_view_empty)).setText(R.string.vplan_empty);
 				((ViewPager)pager).addView(lay, 0);
 				return lay;
 				}
 		
 
 		public void updateCondLists() {
 			exclude_cond = new String();
 			Cursor exclude = myDB.query(Functions.EXCLUDE_TABLE, new String[] {Functions.DB_FACH, Functions.DB_NEEDS_SYNC},
 					null, null, null, null, null);
 			exclude.moveToFirst();
 			int i = 0;
 			while(i < exclude.getCount()) {
 				String fach = exclude.getString(exclude.getColumnIndex(Functions.DB_FACH));
 				exclude_cond += " AND " + Functions.DB_FACH + " != '" + fach + "' ";
 				exclude.moveToNext();
 				i++;
 			}
 			exclude.close();
 			include_cond = new String();
 			Cursor include = myDB.query(Functions.INCLUDE_TABLE, new String[] {Functions.DB_FACH, Functions.DB_NEEDS_SYNC},
 					null, null, null, null, null);
 			include.moveToFirst();
 			i = 0;
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 			String connector = "";
 			while(i < include.getCount()) {
 				String fach = include.getString(include.getColumnIndex(Functions.DB_FACH));
 				include_cond += connector + Functions.DB_FACH + " LIKE '%" + fach + "%' ";
 				connector = " OR ";
 				include.moveToNext();
 				i++;
 			}
 			include.close();
 			if(include_cond.length() == 0)
 				include_cond = " 0 ";
 			if(prefs.getBoolean("showonlywhitelist", false))
 				include_cond = "AND (" + include_cond + " ) OR ( " + include_cond + " )";
 			else
 				include_cond = " OR ( " + include_cond + " )";
 		}
 		public void updateCursor() {
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 			String klasse = prefs.getString("class", "");
 			where_conds[0] =  "%" + klasse + "%";
 			String first = "( " + Functions.DB_KLASSE + " LIKE ? ";
 			String sec = "";
 			if(prefs.getBoolean("showwithoutclass", true))
 				sec = "OR " + Functions.DB_KLASSE + " LIKE 'null'";
 			sec += " OR " + Functions.DB_KLASSE + " LIKE 'infotext') AND ( " + Functions.DB_KLASSE
 					+ " LIKE ? OR " + Functions.DB_FACH + " LIKE ? OR " + Functions.DB_LEHRER + " LIKE ? )";
 			String mine_cond = first + include_cond +  sec + exclude_cond;
 			String all_cond = first + sec;
			if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)) {
				cursor_mine = myDB.query(Functions.DB_VPLAN_TEACHER,
						new String[] { Functions.DB_ROWID, Functions.DB_KLASSE,
								Functions.DB_TYPE, Functions.DB_STUNDE,
								Functions.DB_LEHRER, Functions.DB_FACH,
								Functions.DB_VERTRETUNGSTEXT,
								Functions.DB_VERTRETER, Functions.DB_ROOM,
								Functions.DB_CLASS_LEVEL, Functions.DB_DATE,
								Functions.DB_LENGTH },
						Functions.DB_RAW_VERTRETER + "=? OR "
								+ Functions.DB_RAW_LEHRER + "=?", new String[] {
								prefs.getString(Functions.TEACHER_SHORT, ""),
								prefs.getString(Functions.TEACHER_SHORT, "") },
						null, null, null);
			} else {
				cursor_mine = myDB.query(Functions.DB_VPLAN_TABLE,
						new String[] { Functions.DB_ROWID, Functions.DB_KLASSE,
								Functions.DB_TYPE, Functions.DB_STUNDE,
								Functions.DB_LEHRER, Functions.DB_FACH,
								Functions.DB_VERTRETUNGSTEXT,
								Functions.DB_VERTRETER, Functions.DB_ROOM,
								Functions.DB_CLASS_LEVEL, Functions.DB_DATE,
								Functions.DB_LENGTH }, mine_cond, where_conds,
						null, null, null);
			}
 			where_conds[0] = "%";
 			cursor_all = myDB.query(Functions.DB_VPLAN_TABLE, new String [] {Functions.DB_ROWID, Functions.DB_KLASSE, Functions.DB_TYPE, Functions.DB_STUNDE,
 					Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_VERTRETUNGSTEXT, Functions.DB_VERTRETER, Functions.DB_ROOM,
 					Functions.DB_CLASS_LEVEL, Functions.DB_DATE, Functions.DB_LENGTH}, all_cond, where_conds, null, null, null);
 			cursor_teachers = myDB.query(Functions.DB_VPLAN_TEACHER, new String [] {Functions.DB_ROWID, Functions.DB_KLASSE, Functions.DB_TYPE, Functions.DB_STUNDE,
 					Functions.DB_LEHRER, Functions.DB_FACH, Functions.DB_VERTRETUNGSTEXT, Functions.DB_VERTRETER, Functions.DB_ROOM,
 					Functions.DB_CLASS_LEVEL, Functions.DB_DATE, Functions.DB_LENGTH}, all_cond, where_conds, null, null, null);;
 			vadapter_mine.changeCursor(cursor_mine);
 			vadapter_all.changeCursor(cursor_all);
 			vadapter_teachers.changeCursor(cursor_teachers);
 		}
 		public void updateWhereCond(String searchText) {
 			where_conds[1] = "%" + searchText + "%";
 			where_conds[2] = "%" + searchText + "%";
 			where_conds[3] = "%" + searchText + "%";
 			
 			where_conds_events[0] = "%" + searchText + "%";
 			where_conds_events[1] = "%" + searchText + "%";
 			where_conds_events[2] = "%" + searchText + "%";
 			where_conds_events[3] = "%" + searchText + "%";
 			where_conds_events[4] = "%" + searchText + "%";
 			where_conds_events[5] = "%" + searchText + "%";
 			updateCursor();
 		}
 		public void updateList() {
 			updateCondLists();
 			updateCursor();
 		}
 		public void afterTextChanged (Editable s) { }
 		public void beforeTextChanged (CharSequence s, int start, int count, int after) { }
 		public void onTextChanged (CharSequence s, int start, int before, int count) {
 			String search = s + "";
 			updateWhereCond(search);
 		}
 		public void closeCursorsDB() {
 			cursor_mine.close();
 			cursor_all.close();
 			myDB.close();
 		}
 		@Override
 		public void destroyItem( View pager, int position, Object view ) {
 			((ViewPager)pager).removeView((View) view);
 			}
 		
 		@Override
 		public boolean isViewFromObject( View view, Object object ) {
 			return view.equals( object );
 			}
 		
 		@Override
 		public void finishUpdate( View view ) {}
 		
 		@Override
 		public void restoreState( Parcelable p, ClassLoader c ) {}
 		
 		@Override
 		public Parcelable saveState() {
 			return null;
 			}
 		@Override
 		public void startUpdate( View view ) {}
 	}
 	public class VertretungAdapter extends CursorAdapter {
 		private boolean teacher;
 		class Standard {
 			public LinearLayout standard;
 			public TextView date;
 			public TextView klasse;
 			public TextView title;
 			public TextView type;
 			public TextView when;
 			public TextView vtext;
 			public TextView bottom;
 			public WebView webv;
 			}
 		public VertretungAdapter(Context context, Cursor c, boolean teacher) {
 			super(context, c, false);
 			this.teacher = teacher;
 			}
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			LayoutInflater inflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View rowView;
 			if(teacher)
 				rowView = inflater.inflate(R.layout.standard, null, true);
 			else
 				rowView = inflater.inflate(R.layout.standard, null, true);
 			Standard holder = new Standard();
 			holder.standard = (LinearLayout) rowView.findViewById(R.id.standard_rellayout);
 			holder.date = (TextView) rowView.findViewById(R.id.vertretung_date);
 			holder.klasse = (TextView) rowView.findViewById(R.id.vertretung_class);
 			holder.title = (TextView) rowView.findViewById(R.id.vertretung_title);
 			holder.type = (TextView) rowView.findViewById(R.id.vertretung_type);
 			holder.when = (TextView) rowView.findViewById(R.id.vertretung_when);
 			holder.vtext = (TextView) rowView.findViewById(R.id.vertretung_text);
 			holder.bottom = (TextView) rowView.findViewById(R.id.vertretung_bottom);
 			holder.webv = (WebView) rowView.findViewById(R.id.standard_webview);
 			if(Functions.getSDK() < 11)
 				holder.klasse.setBackgroundResource(R.layout.divider_gradient);
 			rowView.setTag(holder);
 			return rowView;
 			}
 		@Override
 		public void bindView(View view, Context context, Cursor cursor) {
 			Standard holder = (Standard) view.getTag();
 			
 			String olddate  = "";
 			String oldclass = "";
 			int position = cursor.getPosition();
 			if(position > 0) {
 				cursor.moveToPosition(position-1);
 				olddate  = cursor.getString(cursor.getColumnIndex(Functions.DB_DATE));
 				oldclass = cursor.getString(cursor.getColumnIndex(Functions.DB_CLASS_LEVEL));
 				cursor.moveToPosition(position);
 				}
 			
 			String date = cursor.getString(cursor.getColumnIndex(Functions.DB_DATE));
 			if(date.equals(olddate))
 				holder.date.setVisibility(View.GONE);
 			else {
 				holder.date.setVisibility(View.VISIBLE);
 				holder.date.setText(date);
 				oldclass = "";
 				}
 			
 			String klassenstufe = cursor.getString(cursor.getColumnIndex(Functions.DB_CLASS_LEVEL));
 			String klasse = cursor.getString(cursor.getColumnIndex(Functions.DB_KLASSE));
 			if(!klasse.equals("infotext")) {
 				//hide
 				holder.webv.setVisibility(View.GONE);
 				//show needed views
 				holder.title.setVisibility(View.VISIBLE);
 				holder.type.setVisibility(View.VISIBLE);
 				holder.when.setVisibility(View.VISIBLE);
 				holder.bottom.setVisibility(View.VISIBLE);
 				
 				if(klassenstufe.equals(oldclass))
 					holder.klasse.setVisibility(View.GONE);
 				else {
 					holder.klasse.setVisibility(View.VISIBLE);
 					}
 				if(Integer.valueOf(klassenstufe) < 14)
 					holder.klasse.setText(klassenstufe + ". " + context.getString(R.string.classes));
 				else
 					holder.klasse.setText(context.getString(R.string.no_classes));
 				if(klasse.equals("null")) {
 					klasse = context.getString(R.string.no_class);
 					}
 				
 				String fach = cursor.getString(cursor.getColumnIndex(Functions.DB_FACH));
 				holder.title.setText(klasse + " (" + fach + ")");
 				String type = cursor.getString(cursor.getColumnIndex(Functions.DB_TYPE));
 				holder.type.setText(type);
 				Integer lesson;
 				try {
 				lesson = Integer.valueOf(cursor.getString(cursor.getColumnIndex(Functions.DB_STUNDE)));
 				} catch(Exception e) {
 					//old db style, do act!!!
 					lesson = 0;
 					VPlan.this.updateVP();
 				}
 				String when = lesson.toString();
 				int i = 0;
 				int length = cursor.getInt(cursor.getColumnIndex(Functions.DB_LENGTH));
 				while(i < length) {
 					lesson++;
 					when += ", " + lesson.toString();
 					i++;
 					}
 				when += ".";
 				holder.when.setText(when + context.getString(R.string.hour));
 				String vtext = cursor.getString(cursor.getColumnIndex(Functions.DB_VERTRETUNGSTEXT));
 				if(vtext.equals("null"))
 					holder.vtext.setVisibility(View.GONE);
 				else {
 					holder.vtext.setVisibility(View.VISIBLE);
 					holder.vtext.setText("[" + vtext + "]");
 					}
 				String lehrer    = cursor.getString(cursor.getColumnIndex(Functions.DB_LEHRER));
 				if(cursor.getString(cursor.getColumnIndex(Functions.DB_TYPE)).equals("Entfall")){
 					holder.bottom.setText(context.getString(R.string.at) + " " + lehrer);
 					} else {
 						String vertreter = cursor.getString(cursor.getColumnIndex(Functions.DB_VERTRETER));
 						String raum = cursor.getString(cursor.getColumnIndex(Functions.DB_ROOM));
 						String raumInsert = "";
 						if(!raum.equals("null"))
 							raumInsert = '\n' + context.getString(R.string.room) + " " + raum;
 						holder.bottom.setText(lehrer + " → " + vertreter + raumInsert);
 						}
 				} else {
 					holder.klasse.setText(context.getString(R.string.info));
 					//hide views not needed
 					holder.title.setVisibility(View.GONE);
 					holder.type.setVisibility(View.GONE);
 					holder.when.setVisibility(View.GONE);
 					holder.bottom.setVisibility(View.GONE);
 					holder.vtext.setVisibility(View.GONE);
 					
 					//unhide needed views that could be hidden
 					holder.klasse.setVisibility(View.VISIBLE);
 					holder.webv.setVisibility(View.VISIBLE);
 					String info = cursor.getString(cursor.getColumnIndex(Functions.DB_VERTRETUNGSTEXT));
 					holder.webv.loadData(info, "text/html", null);
 					//holder.vtext.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(Functions.DB_VERTRETUNGSTEXT))));
 					}
 			}
 		}
 	public static class VPlanUpdater {
 		Context context;
 		VPlanUpdater(Context c) {
 			context = c;
 		}
 		public String[] update() {
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 			String add = "";
 			try {
 				add = "&" + URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("vplan_date", ""), "UTF-8")
 						+ "&" + URLEncoder.encode("time", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("vplan_time", ""), "UTF-8");
 				} catch(UnsupportedEncodingException e) { Log.d("encoding", e.getMessage()); }
 			String get = Functions.getData(Functions.VP_URL, context, true, add);
 			if(!get.equals("networkerror") && !get.equals("loginerror") && !get.equals("noact")) {
 				try {
 					JSONArray jArray = new JSONArray(get);
 					int i = 0;
 					SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
 					myDB.delete(Functions.DB_VPLAN_TABLE, null, null); //clear vertretungen
 					ContentValues vals = new ContentValues();
 					vals.put(Functions.DB_VERTRETUNG, "false");
 					myDB.update(Functions.DB_TIME_TABLE, vals, null, null);
 					vals.put(Functions.DB_VERTRETUNG, "true");
 					while(i < jArray.length() - 1) {
 						JSONObject jObject = jArray.getJSONObject(i);
 						ContentValues values = new ContentValues();
 						values.put(Functions.DB_CLASS_LEVEL, jObject.getString("klassenstufe"));
 						values.put(Functions.DB_KLASSE, jObject.getString("klasse"));
 						values.put(Functions.DB_STUNDE, jObject.getString("stunde"));
 						values.put(Functions.DB_VERTRETER, jObject.getString("vertreter"));
 						values.put(Functions.DB_RAW_VERTRETER, jObject.getString("rawvertreter"));
 						values.put(Functions.DB_LEHRER, jObject.getString("lehrer"));
 						values.put(Functions.DB_RAW_LEHRER, jObject.getString("rawlehrer"));
 						values.put(Functions.DB_ROOM, jObject.getString("raum"));
 						values.put(Functions.DB_TYPE, jObject.getString("art"));
 						values.put(Functions.DB_VERTRETUNGSTEXT, jObject.getString("vertretungstext"));
 						values.put(Functions.DB_FACH, jObject.getString("fach"));
 						values.put(Functions.DB_RAW_FACH, jObject.getString("rawfach"));
 						values.put(Functions.DB_DATE, jObject.getString("date"));
 						values.put(Functions.DB_LENGTH, jObject.getInt("length"));
 						myDB.insert(Functions.DB_VPLAN_TABLE, null, values);
 						try {
 							Date vdate = new SimpleDateFormat("dd.MM.yyyy")
 									.parse(jObject.getString("date"));
 							Calendar cal = new GregorianCalendar(Integer.valueOf(
 									jObject.getString("date").split("\\.")[2]),
 									Integer.valueOf(jObject.getString("date")
 											.split("\\.")[1]), Integer.valueOf(
 											jObject.getString("date")
 													.split("\\.")[0]));
 							myDB.update(Functions.DB_TIME_TABLE, vals,
 									Functions.DB_DAY + "=? AND "
 											+ Functions.DB_HOUR + "=? AND "
 											+ Functions.DB_CLASS + " LIKE ? AND "
 											+ Functions.DB_FACH + "=?",
 									new String[] {
 											Integer.valueOf(
 													cal.get(Calendar.DAY_OF_WEEK)-3-cal.getFirstDayOfWeek())
 													.toString(),
 											Integer.valueOf(jObject.getInt("stunde")-1).toString(),
 											"%" + jObject.getString("klasse")
 													+ "%" , jObject.getString("fach")});
 							Log.d("day", Integer.valueOf(cal.get(Calendar.DAY_OF_WEEK)).toString());
 							Log.d("date", cal.toString());
 						} catch (ParseException e) {
 						      e.printStackTrace();
 						    }
 						
 						i++;
 						}
 					myDB.close();
 					JSONObject jObject            = jArray.getJSONObject(i);
 					String date                   = jObject.getString("date");
 					String time                   = jObject.getString("time");
 					SharedPreferences.Editor edit = prefs.edit();
 					edit.putString("vplan_date", date);
 					edit.putString("vplan_time", time);
 					edit.commit();
 					} catch(JSONException e) {
 						Log.d("jsonerror", e.getMessage());
 						return new String[] {"json", context.getString(R.string.jsonerror)};
 					}
 				}
 			else if(get.equals("noact"))
 				return new String[] {"noact", context.getString(R.string.noact)};
 			else if(get.equals("loginerror"))
 				return new String[] {"loginerror", context.getString(R.string.loginerror)};
 			else if(get.equals("networkerror"))
 				return new String[] {"networkerror", context.getString(R.string.networkerror)};
 			else
 				return new String[] {"unknownerror", context.getString(R.string.unknownerror)};
 			return new String[] {"success", " "};
 			}
 		public String[] updateTeachers() {
 				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 				String add = "";
 				try {
 					add = "&" + URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("vplan_teacher_date", ""), "UTF-8")
 							+ "&" + URLEncoder.encode("time", "UTF-8") + "=" + URLEncoder.encode(prefs.getString("vplan_teacher_time", ""), "UTF-8")
 							+ "&" + URLEncoder.encode("type", "UTF-8") + "=" + URLEncoder.encode("teachers", "UTF-8");
 					} catch(UnsupportedEncodingException e) { Log.d("encoding", e.getMessage()); }
 				String get = Functions.getData(Functions.VP_URL, context, true, add);
 				Log.d("get", get);
 				if(!get.equals("networkerror") && !get.equals("loginerror") && !get.equals("noact") && !get.equals("rights")) {
 					try {
 						JSONArray jArray = new JSONArray(get);
 						int i = 0;
 						SQLiteDatabase myDB = context.openOrCreateDatabase(Functions.DB_NAME, Context.MODE_PRIVATE, null);
 						myDB.delete(Functions.DB_VPLAN_TEACHER, null, null); //clear vertretungen
 						while(i < jArray.length() - 1) {
 							JSONObject jObject = jArray.getJSONObject(i);
 							ContentValues values = new ContentValues();
 							values.put(Functions.DB_CLASS_LEVEL, jObject.getString("klassenstufe"));
 							values.put(Functions.DB_KLASSE, jObject.getString("klasse"));
 							values.put(Functions.DB_STUNDE, jObject.getString("stunde"));
 							values.put(Functions.DB_VERTRETER, jObject.getString("vertreter"));
 							values.put(Functions.DB_RAW_VERTRETER, jObject.getString("rawvertreter"));
 							values.put(Functions.DB_LEHRER, jObject.getString("lehrer"));
 							values.put(Functions.DB_RAW_LEHRER, jObject.getString("rawlehrer"));
 							values.put(Functions.DB_ROOM, jObject.getString("raum"));
 							values.put(Functions.DB_TYPE, jObject.getString("art"));
 							values.put(Functions.DB_VERTRETUNGSTEXT, jObject.getString("vertretungstext"));
 							values.put(Functions.DB_FACH, jObject.getString("fach"));
 							values.put(Functions.DB_RAW_FACH, jObject.getString("rawfach"));
 							values.put(Functions.DB_DATE, jObject.getString("date"));
 							values.put(Functions.DB_LENGTH, jObject.getInt("length"));
 							myDB.insert(Functions.DB_VPLAN_TEACHER, null, values);
 							i++;
 							}
 						myDB.close();
 						JSONObject jObject            = jArray.getJSONObject(i);
 						String date                   = jObject.getString("date");
 						String time                   = jObject.getString("time");
 						SharedPreferences.Editor edit = prefs.edit();
 						edit.putString("vplan_teacher_date", date);
 						edit.putString("vplan_teacher_time", time);
 						edit.commit();
 						} catch(JSONException e) {
 							Log.d("jsonerror", e.getMessage());
 							return new String[] {"json", context.getString(R.string.jsonerror)};
 						}
 					}
 				else if(get.equals("noact"))
 					return new String[] {"noact", context.getString(R.string.noact)};
 				else if(get.equals("loginerror"))
 					return new String[] {"loginerror", context.getString(R.string.loginerror)};
 				else if(get.equals("networkerror"))
 					return new String[] {"networkerror", context.getString(R.string.networkerror)};
 				else
 					return new String[] {"unknownerror", context.getString(R.string.unknownerror)};
 				return new String[] {"success", " "};
 				}
 		}
 	public class VPlanUpdateTask extends AsyncTask<Void, Void, String[]> {
 		@TargetApi(11)
 		protected void onPreExecute() {
 			super.onPreExecute();
 			loading = ProgressDialog.show(VPlan.this, "", getString(R.string.loading_vplan));
 			Functions.lockRotation(VPlan.this);
 		}
 		@Override
 		protected String[] doInBackground(Void... params) {
 			VPlanUpdater vpup = new VPlanUpdater(VPlan.this);
 			vpup.updateTeachers();
 			return vpup.update();
 		}
 		protected void onPostExecute(String[] res) {
 			loading.cancel();
 			Log.d("ready", "finished");
 			if(!res[0].equals("success"))
 				Toast.makeText(VPlan.this, res[1], Toast.LENGTH_LONG).show();
 			if(res[0].equals("loginerror")) {
 				Intent intent;
 				if(Functions.getSDK() >= 11)
 					intent = new Intent(VPlan.this, SettingsAdvanced.class);
 				else
 					intent = new Intent(VPlan.this, Settings.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 				VPlan.this.startActivity(intent);
 			}
 			else {
 				try {
 					adapter.updateCursor();
 				} catch(Exception e) {}
 			}
 			Functions.unlockRotation(VPlan.this);
 		}
 	}
 	private VPlanPagerAdapter adapter;
 	private ExtendedViewPager pager;
 	private SharedPreferences prefs;
 	private ProgressDialog loading;
 	private SlideMenu slidemenu;
 	private Menu menu;
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Functions.setTheme(false, true, this);
         getWindow().setBackgroundDrawableResource(R.layout.background);
 		setContentView(R.layout.viewpager);
 	    adapter = new VPlanPagerAdapter(this);
 	    pager = (ExtendedViewPager)findViewById(R.id.viewpager);
 	    //pager.setOnPageChangeListener(this);
 	    pager.setAdapter(adapter);
 	    prefs = PreferenceManager.getDefaultSharedPreferences(this);
 	    slidemenu = new SlideMenu(this, VPlan.class);
 	    slidemenu.checkEnabled();
 	    }
 	public boolean onCreateOptionsMenu(Menu menu) {
 		this.menu = menu;
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.vplan, menu);
 	    if(Functions.getSDK() >= 11) {
 	    	AdvancedWrapper ahelp = new AdvancedWrapper();
 	    	ahelp.searchBar(menu, adapter);
 	    }
 	    else
 	    	menu.removeItem(R.id.search);
 	    return true;
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    case R.id.settings:
 	    	Intent settings;
 	    	if(Functions.getSDK() >= 11)
 	    		settings = new Intent(this, SettingsAdvanced.class);
 	    	else
 	    		settings = new Intent(this, Settings.class);
 	    	startActivity(settings);
 	        return true;
 	    case R.id.refresh:
 	    	updateVP();
 	    	return true;
 	    case R.id.subjects:
 	    	Toast.makeText(this, getString(R.string.subjectlist_info), Toast.LENGTH_LONG).show();
             Intent subjects = new Intent(this, SubjectList.class);
             startActivity(subjects);
 	    	return true;
 	    case R.id.info:
 	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	    	builder.setMessage(getString(R.string.number_all) + " " + Integer.valueOf(adapter.cursor_all.getCount()).toString() + "\n"
 	    			+ getString(R.string.number_mine) + " " + Integer.valueOf(adapter.cursor_mine.getCount()).toString() + "\n"
 	    			+ getString(R.string.actdate) + prefs.getString("vplan_date", "") + " / " + prefs.getString("vplan_time", ""))
 	    	       .setCancelable(true)
 	    	       .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
 	    	           public void onClick(DialogInterface dialog, int id) {
 	    	                dialog.cancel();
 	    	           }
 	    	       });
 	    	AlertDialog alert = builder.create();
 	    	alert.show();
 	    	return true;
         case android.R.id.home:
             slidemenu.show();
             return true;
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		Functions.createContextMenu(menu, v, menuInfo, this, Functions.DB_VPLAN_TABLE);
 	}
 	@Override
 	public boolean onContextItemSelected(final MenuItem item) {
 		return Functions.contextMenuSelect(item, this, adapter, Functions.DB_VPLAN_TABLE);
 	}
 	public void updateVP() {
 	    VPlanUpdateTask vpup = new VPlanUpdateTask();
 	    vpup.execute();
 	}
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		adapter.closeCursorsDB();
 	}
 }
