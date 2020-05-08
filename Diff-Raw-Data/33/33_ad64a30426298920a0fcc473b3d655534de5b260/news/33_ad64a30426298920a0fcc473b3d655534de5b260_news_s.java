 package com.messedagliavr.messeapp;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.annotation.SuppressLint;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.ParseException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.Spanned;
 import android.text.format.DateFormat;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 @SuppressWarnings("unused")
 @SuppressLint("SimpleDateFormat")
 public class news extends ListActivity {
 
 	public static final String TITLE = "title";
 	public static final String DESC = "description";
 	public String[] titolim;
 	public String[] descrizionim;
 	public Boolean canceled = true;
 	ProgressDialog mDialog;
 	public Boolean unknhost = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		mDialog = new ProgressDialog(news.this);
 		super.onCreate(savedInstanceState);
 		new connection().execute();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.list_item, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			Database databaseHelper = new Database(getBaseContext());
 			SQLiteDatabase db = databaseHelper.getWritableDatabase();
 			ContentValues nowdb = new ContentValues();
 			nowdb.put("newsdate", "2012-02-20 15:00:00");
 			long samerow = db.update("lstchk", nowdb, null, null);
 			db.close();
 			new connection().execute();
 			break;
 		}
 		return true;
 	}
 
 	@SuppressLint("SimpleDateFormat")
 	private Long getTimeDiff(String time, String curTime) throws ParseException {
 		Date curDate = null;
 		Date oldDate = null;
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		try {
 			curDate = (Date) formatter.parse(curTime);
 			oldDate = (Date) formatter.parse(time);
 		} catch (java.text.ParseException e) {
 			e.printStackTrace();
 		}
 		long oldMillis = oldDate.getTime();
 		long curMillis = curDate.getTime();
 		long diff = curMillis - oldMillis;
 		return diff;
 	}
 
 	@Override
 	public void onBackPressed() {
 		Intent main = new Intent(this, MainActivity.class);
 		main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(main);
 	}
 
 	public class connection extends
 			AsyncTask<Void, Void, HashMap<String, ArrayList<Spanned>>> {
 		protected void onCancelled() {
 			Intent main = new Intent(news.this, MainActivity.class);
 			main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(main);
 			Toast.makeText(news.this, R.string.cancelednews, Toast.LENGTH_LONG)
 					.show();
 		}
 
 		public void onCanceled() {
 			mDialog.dismiss();
 		}
 
 		public void onPreExecute() {
 			mDialog = ProgressDialog.show(news.this, "Scaricando",
 					"Sto scaricando le news", true, true,
 					new DialogInterface.OnCancelListener() {
 						public void onCancel(DialogInterface dialog) {
 							connection.this.cancel(true);
 						}
 					});
 			mDialog.show();
 		}
 
 		public HashMap<String, ArrayList<Spanned>> doInBackground(
 				Void... params) {
 			Database databaseHelper = new Database(getBaseContext());
			SQLiteDatabase db = databaseHelper.getWritableDatabase();
 			HashMap<String, ArrayList<Spanned>> temhashmap = new HashMap<String, ArrayList<Spanned>>();
 			ArrayList<Spanned> titoli = new ArrayList<Spanned>();
 			ArrayList<Spanned> descrizioni = new ArrayList<Spanned>();
 			// All static variables
 			final String URL = "http://www.messedaglia.it/index.php?option=com_ninjarsssyndicator&feed_id=1&format=raw";
 			// XML node keys
 			final String ITEM = "item"; // parent node
 			final String TITLE = "title";
 			final String DESC = "description";
 			Element e = null;
 			ArrayList<HashMap<String, Spanned>> menuItems = new ArrayList<HashMap<String, Spanned>>();
 			String[] outdated = { "newsdate", "calendardate" };
 			Calendar c = Calendar.getInstance();
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			String now = df.format(c.getTime());
 			Cursor date = db.query("lstchk", // The table to query
 					outdated, // The columns to return
 					null, // The columns for the WHERE clause
 					null, // The values for the WHERE clause
 					null, // don't group the rows
 					null, // don't filter by row groups
 					null // The sort order
 					);
 			date.moveToFirst();
 			String past = date.getString(date.getColumnIndex("newsdate"));
 			long l = getTimeDiff(past, now);
 			if (l / 10800000 >= 3) {
 				XMLParser parser = new XMLParser();
 				String xml = parser.getXmlFromUrl(URL);
 				if (xml == "UnknownHostException") {
 					unknhost = true;
 					db.close();
 					date.close();
 					return temhashmap;
 				} else {
 					Document doc = parser.getDomElement(xml);
 					NodeList nl = doc.getElementsByTagName(ITEM);
 					ContentValues values = new ContentValues();
 					for (int i = 0; i < nl.getLength(); i++) {
 						HashMap<String, Spanned> map = new HashMap<String, Spanned>();
 						e = (Element) nl.item(i);
 						values.put("id", i);
 						values.put(TITLE, parser.getValue(e, TITLE));
 						values.put(DESC, parser.getValue(e, DESC));
 						map.put(TITLE, Html.fromHtml(parser.getValue(e, TITLE)));
 						map.put(DESC, Html.fromHtml(parser.getValue(e, DESC)));
 
 						titoli.add(Html.fromHtml(parser.getValue(e, TITLE)));
 						descrizioni
 								.add(Html.fromHtml(parser.getValue(e, DESC)));
 						// adding HashList to ArrayList
 						menuItems.add(map);
 						long newRowId = db.insert("news", null, values);
 
 					}
 					ContentValues nowdb = new ContentValues();
 					nowdb.put("newsdate", now);
 					long samerow = db.update("lstchk", nowdb, null, null);
 					temhashmap.put("titoli", titoli);
 					temhashmap.put("descrizioni", descrizioni);
 					return temhashmap;
 
 				}
 			} else {
 				String[] clmndata = { "title", "description" };
 				String sortOrder = "id";
 
				Cursor data = db.query("news", // The table to query
 						clmndata, // The columns to return
 						null, // The columns for the WHERE clause
 						null, // The values for the WHERE clause
 						null, // don't group the rows
 						null, // don't filter by row groups
 						sortOrder // The sort order
 						);
 
 				for (data.move(0); data.moveToNext(); data.isAfterLast()) {
 					HashMap<String, Spanned> map = new HashMap<String, Spanned>();
 					map.put(TITLE, Html.fromHtml(data.getString(data
 							.getColumnIndex("title"))));
 					map.put(DESC, Html.fromHtml(data.getString(data
 							.getColumnIndex("description"))));
 
 					titoli.add(Html.fromHtml(data.getString(data
 							.getColumnIndex("title"))));
 					descrizioni.add(Html.fromHtml(data.getString(data
 							.getColumnIndex("description"))));
 					// adding HashList to ArrayList
 					menuItems.add(map);
 
 				}
 				data.close();
 				db.close();
 				temhashmap.put("titoli", titoli);
 				temhashmap.put("descrizioni", descrizioni);
 				return temhashmap;
 
 			}
 
 		}
 
 		public void onPostExecute(HashMap<String, ArrayList<Spanned>> resultmap) {
 			if (unknhost == true) {
 				mDialog.dismiss();
 				Toast.makeText(news.this, R.string.connerr,
 						Toast.LENGTH_LONG).show();
 				Intent main = new Intent(news.this, MainActivity.class);
 				main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(main);
 			} else {
				System.out.println("No if onpost");
 				if (resultmap.size() > 0) {
 					final ArrayList<Spanned> titoli = resultmap.get("titoli");
 					final ArrayList<Spanned> descrizioni = resultmap
 							.get("descrizioni");
 
 					ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(
 							news.this, android.R.layout.simple_list_item_1,
 							titoli);
 					setContentView(R.layout.list_item);
 					ListView listView = (ListView) news.this
 							.findViewById(android.R.id.list);
 					listView.setAdapter(adapter);
 					listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 						public void onItemClick(AdapterView<?> parentView,
 								View childView, int position, long id) {
 							Intent intent = new Intent(news.this,
 									ListItemSelectedNews.class);
 							intent.putExtra(TITLE,
 									Html.toHtml(titoli.get(position)));
 							intent.putExtra(DESC,
 									Html.toHtml(descrizioni.get(position)));
 							startActivity(intent);
 						}
 					});
 				}
 				mDialog.dismiss();
 			}
 		}
 	}
 }
