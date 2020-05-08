 package com.messedagliavr.messeapp;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 @SuppressLint("DefaultLocale")
 public class timetable extends Activity implements
 		AdapterView.OnItemSelectedListener {
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.orarimenu, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.palestre:
 			Toast.makeText(timetable.this, R.string.notavailable,
 					Toast.LENGTH_LONG).show();
 			break;
 		}
 		return true;
 	}
 
 	@Override
 	public void onBackPressed() {
 		Intent main = new Intent(this, MainActivity.class);
 		main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(main);
 	}
 
 	String[] items = { "Scegli una classe", "1A", "1B", "1C", "1D", "1E", "1F",
			"1G", "1H", "1I", "1L", "1M", "1N", "2A", "2B", "2C", "2D", "2E", "2F", "2G",
			"2H", "2I", "2L", "3A", "3B", "3C", "3D", "3E", "3F", "3G", "4A", "4B", "4C",
            "4D", "4E", "4F", "4G", "4H", "4I", "4L", "4M", "4N", "4O", "5A", "5B", "5C",
            "5D", "5E", "5F", "5G", "5H", "5I", "5L", "5M", "5N", "5O" };
 	String fname = null;
 
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		setContentView(R.layout.timetable);
 		Database databaseHelper = new Database(getBaseContext());
 		SQLiteDatabase db = databaseHelper.getWritableDatabase();
 		String[] columns = { "fname" };
 		Cursor classe = db.query("class", // The table to query
 				columns, // The columns to return
 				null, // The columns for the WHERE clause
 				null, // The values for the WHERE clause
 				null, // don't group the rows
 				null, // don't filter by row groups
 				null // The sort order
 				);
 		classe.moveToFirst();
 		fname = classe.getString(classe.getColumnIndex("fname"));
 		classe.close();
 		db.close();
 		if (fname.matches("novalue") == false) {
 			items[0] = "Predefinito: " + fname.toUpperCase();
 		}
 		Spinner spin = (Spinner) findViewById(R.id.spinner);
 		spin.setOnItemSelectedListener(this);
 
 		ArrayAdapter<?> aa = new ArrayAdapter<Object>(this,
 				android.R.layout.simple_spinner_item, items);
 
 		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spin.setAdapter(aa);
 	}
 
 	@SuppressLint({ "DefaultLocale", "SetJavaScriptEnabled" })
 	public void onItemSelected(AdapterView<?> parent, View v, int position,
 			long id) {
 		WebView descrizioneview = (WebView) findViewById(R.id.imageorario);
 		if (position == 0) {
 			if (fname.matches("novalue") == false) {
 				descrizioneview.getSettings().setJavaScriptEnabled(true);
 				descrizioneview.getSettings().setLoadWithOverviewMode(true);
 				descrizioneview.getSettings().setUseWideViewPort(true);
 				descrizioneview
 						.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
 				descrizioneview.setScrollbarFadingEnabled(true);
 				descrizioneview.getSettings().setBuiltInZoomControls(true);
 				descrizioneview.loadUrl("file:///android_res/drawable/o"
 						+ fname + ".png");
 			} else {
 				descrizioneview.loadData("", "text/html", "UTF-8");
 			}
 		} else {
 			Database databaseHelper = new Database(getBaseContext());
 			SQLiteDatabase db = databaseHelper.getWritableDatabase();
 			ContentValues values = new ContentValues();
 			values.put("fname", items[position].toLowerCase());
 			@SuppressWarnings("unused")
 			long samerow = db.update("class", values, null, null);
 			descrizioneview.getSettings().setJavaScriptEnabled(true);
 			descrizioneview.getSettings().setLoadWithOverviewMode(true);
 			descrizioneview.getSettings().setUseWideViewPort(true);
 			descrizioneview
 					.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
 			descrizioneview.setScrollbarFadingEnabled(true);
 			descrizioneview.getSettings().setBuiltInZoomControls(true);
 			descrizioneview.loadUrl("file:///android_res/drawable/o"
 					+ items[position].toLowerCase() + ".png");
 			db.close();
 		}
 	}
 
 	@SuppressWarnings("unused")
 	public void onNothingSelected(AdapterView<?> parent) {
 		WebView descrizioneview = (WebView) findViewById(R.id.imageorario);
 		Database databaseHelper = new Database(getBaseContext());
 		SQLiteDatabase db = databaseHelper.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put("fname", "novalue");
 		long samerow = db.update("class", values, null, null);
 		descrizioneview.loadData("", "text/html", "UTF-8");
 	}
 }// class
 
