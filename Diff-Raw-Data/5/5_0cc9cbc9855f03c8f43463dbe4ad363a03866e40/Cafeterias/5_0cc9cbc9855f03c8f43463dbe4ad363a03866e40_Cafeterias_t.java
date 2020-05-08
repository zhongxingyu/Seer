 package de.tum.in.tumcampus;
 
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.SimpleCursorAdapter;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 import de.tum.in.tumcampus.models.CafeteriaMenuManager;
 
 public class Cafeterias extends Activity implements OnItemClickListener {
 
 	SQLiteDatabase db;
 
 	String date;
 	String dateStr;
 	String mensaId;
 	String mensaName;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
 			setContentView(R.layout.cafeterias_horizontal);
 		} else {
 			setContentView(R.layout.cafeterias);
 		}
 
 		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
 		sd.open();
 
 		db = SQLiteDatabase.openDatabase(this.getDatabasePath("database.db")
 				.toString(), null, SQLiteDatabase.OPEN_READONLY);
 
 		Cursor c = db.rawQuery(
				"SELECT DISTINCT strftime('%d.%m.%Y', date) as date_de, date as _id "
 						+ "FROM cafeterias_menus WHERE "
						+ "date >= date() ORDER BY date", null);
 
 		ListAdapter adapter = new SimpleCursorAdapter(this,
 				android.R.layout.simple_list_item_1, c, c.getColumnNames(),
 				new int[] { android.R.id.text1 });
 
 		ListView lv = (ListView) findViewById(R.id.listView);
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(this);
 
 		Cursor c2 = db.rawQuery("SELECT name, address, id as _id "
 				+ "FROM cafeterias "
 				+ "ORDER BY address like '%Garching%' desc, name", null);
 
 		adapter = new SimpleCursorAdapter(this,
 				android.R.layout.two_line_list_item, c2, c2.getColumnNames(),
 				new int[] { android.R.id.text1, android.R.id.text2 });
 
 		ListView lv2 = (ListView) findViewById(R.id.listView2);
 		lv2.setAdapter(adapter);
 		lv2.setOnItemClickListener(this);
 
 		// TODO destroy db, cursor
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
 
 		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
 		if (sd.isOpened()) {
 			sd.animateClose();
 		}
 
 		if (av.getId() == R.id.listView) {
 			ListView lv = (ListView) findViewById(R.id.listView);
 			Cursor c = (Cursor) lv.getAdapter().getItem(position);
 			date = c.getString(c.getColumnIndex("_id"));
 			dateStr = c.getString(c.getColumnIndex("date"));
 		}
 
 		if (av.getId() == R.id.listView2) {
 			ListView lv2 = (ListView) findViewById(R.id.listView2);
 			Cursor c = (Cursor) lv2.getAdapter().getItem(position);
 			mensaId = c.getString(c.getColumnIndex("_id"));
 			mensaName = c.getString(c.getColumnIndex("name"));
 		}
 
 		if (mensaId != null && date != null) {
 			TextView tv = (TextView) findViewById(R.id.cafeteriaText);
 			tv.setText(mensaName + ": " + dateStr);
 
 			CafeteriaMenuManager cmm = new CafeteriaMenuManager(this,
 					"database.db");
 			List<HashMap<String, String>> list = cmm.getTypeNameFromDb(mensaId,
 					date);
 			cmm.close();
 
 			String[] from = new String[] {};
 			if (list.size() > 0) {
 				from = list.get(0).keySet().toArray(new String[] {});
 			}
 			int[] to = { android.R.id.text1, android.R.id.text2 };
 
 			ListView lv3 = (ListView) findViewById(R.id.listView3);
 
 			SimpleAdapter adapter = new SimpleAdapter(this, list,
 					android.R.layout.two_line_list_item, from, to) {
 				public boolean areAllItemsEnabled() {
 					return false;
 				}
 
 				public boolean isEnabled(int position) {
 					return false;
 				}
 			};
 			lv3.setAdapter(adapter);
 		}
 	}
 }
