 package com.michalmazur.orphanedtexts;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract.PhoneLookup;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.app.AlertDialog.Builder;
 
 public class OrphanedTextsActivity extends Activity {
 
 	String uriString;
 	Uri uri;
 	private String output;
 	ArrayList<Orphan> orphans;
 	private static final String PREFS_NAME = "preferences";
 	private static final String DATABASE_LAST_EMPTIED = "database_last_emptied";
 
 	public OrphanedTextsActivity() {
 		super();
 
 		uriString = "content://sms/raw";
 		uri = Uri.parse(uriString);
 		output = "";
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		RawSmsReader reader = new RawSmsReader(this.getApplicationContext());
 //		reader = new RawSmsReader();
 
 		orphans = reader.getOrphans();
 		setContentView(R.layout.main);
 		output = new CsvConverter().convert(orphans);
 		displayOrphanList();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.email:
 			email();
 			return true;
 		case R.id.delete_all:
 			deleteAll();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void displayOrphanList() {
 		ListView lv = (ListView) findViewById(R.id.listView1);
 		List<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
 		for (Orphan o : orphans) {
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("sender", getContactName(o.getAddress()));
 			map.put("datetime", o.getDate().toLocaleString());
 			map.put("message", o.getMessageBody());
 			items.add(map);
 		}
 		Log.d("COUNT", String.valueOf(items.size()));
 		((TextView) findViewById(R.id.count)).setText("Total number of orphaned texts: "
 				+ items.size());
 		((TextView) findViewById(R.id.database_last_emptied)).setText("Database last emptied: "
 				+ readDatabaseLastEmptiedPreference());
 		String[] from = new String[] { "sender", "datetime", "message" };
 		int[] to = new int[] { R.id.sender, R.id.datetime, R.id.message };
 		lv.setAdapter(new SimpleAdapter(this, items, R.layout.lvitem, from, to));
 	}
 
 	public void deleteAll() {
 		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which) {
 				case DialogInterface.BUTTON_POSITIVE:
 					deleteAllRecords();
 					displayOrphanList();
 					break;
 				}
 			}
 		};
 
 		Builder builder = new Builder(this);
 		builder.setMessage("Are you sure you want to delete all orphaned messages?")
 				.setPositiveButton("Yes", dialogClickListener)
 				.setNegativeButton("No", dialogClickListener).show();
 	}
 
 	public void deleteAllRecords() {
 		getContentResolver().delete(uri, null, null);
 		orphans.clear();
 		saveDatabaseLastEmptiedPreference();
 	}
 
 	public void email() {
 		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 		emailIntent.setType("plain/text");
 		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Orphaned Texts");
 		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, output);
 
 		try {
 			startActivity(emailIntent);
 		}
 		catch (ActivityNotFoundException e) {
 			displayAlertDialog("Orphaned texts could not be e-mailed.\nReason: no e-mail app found on device.");
 		}
 	}
 
 	private void displayAlertDialog(String message) {
 		AlertDialog ad = new AlertDialog.Builder(this).create();
 		ad.setMessage(message);
 		ad.setButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		ad.show();
 	}
 
 	public String getContactName(String phoneNumber) {
 		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
 				Uri.encode(phoneNumber));
 		String[] columns = { PhoneLookup.DISPLAY_NAME };
 		String displayName = phoneNumber;
 		Cursor cursor = getContentResolver().query(lookupUri, columns, null, null, null);
 		if (cursor != null) {
 			if (cursor.moveToFirst()) {
 				displayName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
 			}
 			cursor.close();
 		}
 		return displayName;
 	}
 
 	private void saveDatabaseLastEmptiedPreference() {
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putLong(DATABASE_LAST_EMPTIED, new Date().getTime());
 		editor.commit();
 	}
 
 	private String readDatabaseLastEmptiedPreference() {
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		long miliseconds = settings.getLong(DATABASE_LAST_EMPTIED, 0);
		if (miliseconds > 0) {
			return new Date(miliseconds).toLocaleString();
 		} else {
 			return "never";
 		}
 	}
 }
