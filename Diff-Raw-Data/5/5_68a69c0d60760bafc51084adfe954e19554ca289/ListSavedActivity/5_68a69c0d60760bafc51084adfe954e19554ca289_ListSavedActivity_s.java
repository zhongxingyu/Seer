 package com.seawolfsanctuary.keepingtracks;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ExpandableListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.seawolfsanctuary.keepingtracks.database.Journey;
 
 public class ListSavedActivity extends ExpandableListActivity {
 
 	SharedPreferences settings;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.context_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.add_new:
 			Intent intent = new Intent(this, AddActivity.class);
 			ListSavedActivity.this.finish();
 			startActivity(intent);
 			return true;
 		case R.id.import_csv:
 			final Context context = this;
 			new AlertDialog.Builder(context)
 					.setTitle(getString(R.string.list_saved_import_title))
 					.setMessage(
 							getString(R.string.list_saved_import_text,
 									Helpers.exportDirectoryPath + "/routes.csv"))
 					.setPositiveButton(
 							getString(R.string.list_saved_import_positive),
 							new OnClickListener() {
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									ProgressDialog progressDialog = new ProgressDialog(
 											context);
 									progressDialog
 											.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 									progressDialog
 											.setTitle(getString(R.string.list_saved_import_progress_title));
 									progressDialog
 											.setMessage(getString(
 													R.string.list_saved_import_progress_text,
 													Helpers.exportDirectoryPath
 															+ "/routes.csv"));
 									progressDialog.setCancelable(false);
 									new ImportTask(progressDialog)
 											.execute(Helpers.exportDirectoryPath
 													+ "/routes.csv");
 								}
 							})
 					.setNegativeButton(
 							getString(R.string.list_saved_import_positive),
 							new OnClickListener() {
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									// ignore
 								}
 							}).show();
 			return true;
 		case R.id.export_csv:
 			ProgressDialog progressDialog = new ProgressDialog(this);
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			progressDialog
 					.setTitle(getString(R.string.list_saved_export_progress_title));
 			progressDialog.setMessage(getString(
 					R.string.list_saved_export_progress_text,
 					Helpers.exportDirectoryPath + "/routes.csv"));
 			progressDialog.setCancelable(false);
 			new ExportTask(progressDialog).execute();
 			return true;
 		default:
 			return true;
 		}
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list_saved_activity);
 		settings = getSharedPreferences(UserPrefsActivity.APP_PREFS,
 				MODE_PRIVATE);
 		setListAdapter(new ListSavedAdapter());
 		registerForContextMenu(getExpandableListView());
 
 		ExpandableListView lv = getExpandableListView();
 		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parentView,
 					View childView, final int positionInParentList,
 					final long positionWithinChildList) {
 
 				boolean isParentSelected = (positionWithinChildList >= 0);
 				if (isParentSelected) {
 					// by some fluke we have a parent
 					new AlertDialog.Builder(parentView.getContext())
 							.setTitle(R.string.list_saved_question_title)
 							.setMessage(R.string.list_saved_question_text)
 							.setPositiveButton(
 									R.string.list_saved_question_edit,
 									new OnClickListener() {
 										public void onClick(
 												DialogInterface arg0, int arg1) {
 											editEntry(ExpandableListView
 													.getPackedPositionGroup(positionWithinChildList));
 											// save will create
 											// ListSavedActivity
 										}
 									})
 							.setNeutralButton(
 									R.string.list_saved_question_copy,
 									new OnClickListener() {
 										public void onClick(
 												DialogInterface arg0, int arg1) {
 											copyEntry(ExpandableListView
 													.getPackedPositionGroup(positionWithinChildList));
 											// save will create
 											// ListSavedActivity
 										}
 									})
 							.setNegativeButton(
 									R.string.list_saved_question_delete,
 									new OnClickListener() {
 										public void onClick(
 												DialogInterface arg0, int arg1) {
 											deleteEntry(ExpandableListView
 													.getPackedPositionGroup(positionWithinChildList));
 
 											Intent intent = new Intent(
 													getApplicationContext(),
 													ListSavedActivity.class);
 											ListSavedActivity.this.finish();
 											startActivity(intent);
 										}
 									}).show();
 
 					return true;
 				}
 				return false;
 			}
 		});
 
 		checkForLegacy(this.getExpandableListView());
 	}
 
 	private class ListSavedAdapter extends BaseExpandableListAdapter {
 
 		ArrayList<String[]> data = loadSavedEntries(true);
 		ArrayList<String> names = new ArrayList<String>(getNames(data));
 
 		private String[] presentedNames = Helpers
 				.arrayListToArray(getNames(data));
 		private String[][] presentedData = Helpers
 				.multiArrayListToArray(getData(data));
 
 		private ArrayList<String> getNames(ArrayList<String[]> data) {
 			ArrayList<String> names = new ArrayList<String>();
 			for (int i = 0; i < data.size(); i++) {
 				String[] entry = data.get(i);
 				names.add(getString(
 						R.string.list_saved_entry_name,
 						Helpers.trimCodeFromStation(entry[0], getBaseContext()),
 						Helpers.trimCodeFromStation(entry[6], getBaseContext()),
						Helpers.leftPad(entry[3], 2),
 						Helpers.leftPad(entry[2], 2),
						Helpers.leftPad(entry[1], 4)));
 			}
 			return names;
 		}
 
 		private ArrayList<ArrayList<String>> getData(ArrayList<String[]> entries) {
 			ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
 
 			for (int i = 0; i < entries.size(); i++) {
 				String[] entry = entries.get(i);
 				ArrayList<String> split = new ArrayList<String>();
 
 				split.add(getString(R.string.list_saved_entry_from, Helpers
 						.nameAndCodeFromStation(entry[0], getBaseContext()),
 						Helpers.leftPad(entry[3], 2), Helpers.leftPad(entry[2],
 								2), Helpers.leftPad(entry[1], 2),
 
 						Helpers.leftPad(entry[4], 2), Helpers.leftPad(entry[5],
 								2)));
 
 				split.add(getString(R.string.list_saved_entry_to, Helpers
 						.nameAndCodeFromStation(entry[6], getBaseContext()),
 						Helpers.leftPad(entry[9], 2), Helpers.leftPad(entry[8],
 								2), Helpers.leftPad(entry[7], 2),
 
 						Helpers.leftPad(entry[10], 2), Helpers.leftPad(
 								entry[11], 2)));
 
 				if (settings.getBoolean("AdvancedJourneys", false) == true) {
 					if (entry[12].length() > 0) {
 						split.add(getString(R.string.list_saved_entry_with,
 								entry[12]));
 					}
 
 					if (entry[13].length() > 0) {
 						split.add(getString(R.string.list_saved_entry_as,
 								entry[13]));
 					}
 				}
 
 				data.add(split);
 			}
 
 			return data;
 		}
 
 		public Object getChild(int groupPosition, int childPosition) {
 			return presentedData[groupPosition][childPosition];
 		}
 
 		public long getChildId(int groupPosition, int childPosition) {
 			return childPosition;
 		}
 
 		public int getChildrenCount(int groupPosition) {
 			return presentedData[groupPosition].length;
 		}
 
 		public TextView getGenericView() {
 			// Layout parameters for the ExpandableListView
 			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
 					ViewGroup.LayoutParams.FILL_PARENT, 64);
 
 			TextView textView = new TextView(ListSavedActivity.this);
 			textView.setLayoutParams(lp);
 			// Centre the text vertically
 			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
 			// Set the text starting position
 			textView.setPadding(36, 0, 0, 0);
 			return textView;
 		}
 
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) {
 			TextView textView = getGenericView();
 			textView.setText(getChild(groupPosition, childPosition).toString());
 			return textView;
 		}
 
 		public Object getGroup(int groupPosition) {
 			return presentedNames[groupPosition];
 		}
 
 		public int getGroupCount() {
 			return presentedNames.length;
 		}
 
 		public long getGroupId(int groupPosition) {
 			return groupPosition;
 		}
 
 		public View getGroupView(int groupPosition, boolean isExpanded,
 				View convertView, ViewGroup parent) {
 			TextView textView = getGenericView();
 			textView.setText(getGroup(groupPosition).toString());
 			return textView;
 		}
 
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			return true;
 		}
 
 		public boolean hasStableIds() {
 			return true;
 		}
 
 	}
 
 	public ArrayList<String[]> loadSavedEntries(boolean showToast) {
 
 		ArrayList<String[]> allJourneys = new ArrayList<String[]>();
 
 		Journey db_journeys = new Journey(this);
 		db_journeys.open();
 		Cursor c = db_journeys.getAllJourneysReverse();
 		if (c.moveToFirst()) {
 			do {
 				try {
 					String[] entry = new String[15];
 
 					boolean useForStats = true;
 					if (c.getString(15) == "0") {
 						useForStats = false;
 					}
 
 					entry[0] = c.getString(1);
 					entry[1] = "" + c.getInt(2);
 					entry[2] = "" + c.getInt(3);
 					entry[3] = "" + c.getInt(4);
 					entry[4] = "" + c.getInt(5);
 					entry[5] = "" + c.getInt(6);
 					entry[6] = c.getString(7);
 					entry[7] = "" + c.getInt(8);
 					entry[8] = "" + c.getInt(9);
 					entry[9] = "" + c.getInt(10);
 					entry[10] = "" + c.getInt(11);
 					entry[11] = "" + c.getInt(12);
 					entry[12] = c.getString(13);
 					entry[13] = c.getString(14);
 					entry[14] = "" + useForStats;
 					allJourneys.add(entry);
 				} catch (Exception e) {
 					System.err.println("Unable to parse entry: "
 							+ e.getMessage());
 				}
 			} while (c.moveToNext());
 		}
 		db_journeys.close();
 
 		if (showToast) {
 			String msg;
 			if (allJourneys.size() == 1) {
 				msg = getString(R.string.list_saved_loaded_single);
 			} else {
 				msg = getString(R.string.list_saved_loaded_multiple,
 						allJourneys.size());
 			}
 
 			System.out.println(msg);
 			Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
 		}
 		return allJourneys;
 	}
 
 	public boolean editEntry(int position) {
 		boolean success = false;
 		Bundle entry = fetchJourneyToBundle(this, position, true);
 		entry.putBoolean("editing", true);
 
 		try {
 			System.out.println(getString(R.string.list_saved_answer_editing,
 					entry.getInt("id")));
 			Intent intent = new Intent(this, AddActivity.class);
 			ListSavedActivity.this.finish();
 			intent.putExtras(entry);
 			startActivity(intent);
 
 		} catch (Exception e) {
 			Toast.makeText(getBaseContext(), "Error: " + e.getMessage(),
 					Toast.LENGTH_LONG).show();
 		}
 
 		return success;
 	}
 
 	public boolean copyEntry(int position) {
 		boolean success = false;
 		Bundle entry = fetchJourneyToBundle(this, position, false);
 		entry.putBoolean("copying", true);
 
 		try {
 			System.out.println(getString(R.string.list_saved_answer_copying,
 					position));
 			Intent intent = new Intent(this, AddActivity.class);
 			ListSavedActivity.this.finish();
 			intent.putExtras(entry);
 			startActivity(intent);
 
 		} catch (Exception e) {
 			Toast.makeText(getBaseContext(), "Error: " + e.getMessage(),
 					Toast.LENGTH_LONG).show();
 		}
 
 		return success;
 	}
 
 	private Bundle fetchJourneyToBundle(Activity activity, int position,
 			boolean includeId) {
 		Bundle entry = new Bundle();
 		int id = -1;
 
 		Journey db_journeys = new Journey(activity);
 		db_journeys.open();
 
 		// Fetch all journeys so we can find out the id to edit
 		Cursor c = db_journeys.getAllJourneysReverse();
 		if (c.moveToFirst()) {
 			c.moveToPosition(position);
 			id = c.getInt(0);
 			System.out.println("Fetching row #" + id + "...");
 			Cursor journey = db_journeys.getJourney(id);
 
 			if (includeId) {
 				entry.putInt("id", journey.getInt(0));
 			}
 
 			entry.putString("from_stn", journey.getString(1));
 			entry.putInt("from_date_day", journey.getInt(2));
 			entry.putInt("from_date_month", journey.getInt(3));
 			entry.putInt("from_date_year", journey.getInt(4));
 			entry.putInt("from_time_hour", journey.getInt(5));
 			entry.putInt("from_time_minute", journey.getInt(6));
 
 			entry.putString("to_stn", journey.getString(7));
 			entry.putInt("to_date_day", journey.getInt(8));
 			entry.putInt("to_date_month", journey.getInt(9));
 			entry.putInt("to_date_year", journey.getInt(10));
 			entry.putInt("to_time_hour", journey.getInt(11));
 			entry.putInt("to_time_minute", journey.getInt(12));
 
 			if (journey.getString(13).length() < 1) {
 				entry.putBoolean("detail_class_checked", false);
 			}
 			if (journey.getString(14).length() < 1) {
 				entry.putBoolean("detail_headcode_checked", false);
 			}
 
 			boolean useForStats = true;
 			if (journey.getInt(15) == 0) {
 				useForStats = false;
 			}
 			entry.putString("detail_class", journey.getString(13));
 			entry.putString("detail_headcode", journey.getString(14));
 			entry.putBoolean("detail_use_for_stats", useForStats);
 		}
 		db_journeys.close();
 
 		return entry;
 	}
 
 	public boolean deleteEntry(int position) {
 		boolean success = false;
 		int id = -1;
 
 		try {
 			Journey db_journeys = new Journey(this);
 			db_journeys.open();
 
 			// Fetch all journeys so we can find out the id to delete
 			Cursor c = db_journeys.getAllJourneysReverse();
 			if (c.moveToFirst()) {
 				c.moveToPosition(position);
 				id = c.getInt(0);
 				System.out.println("Deleting row #" + id + "...");
 				success = db_journeys.deleteJourney(id);
 			}
 			db_journeys.close();
 			success = true;
 
 			Toast.makeText(getBaseContext(), R.string.list_saved_answer_delete,
 					Toast.LENGTH_SHORT).show();
 		} catch (Exception e) {
 			Toast.makeText(getBaseContext(), "Error: " + e.getMessage(),
 					Toast.LENGTH_LONG).show();
 		}
 
 		return success;
 	}
 
 	private void checkForLegacy(final View view) {
 		// do we have a CSV file on the SD card?
 		if (new File(Helpers.dataDirectoryPath + "/routes.csv").exists()) {
 			new AlertDialog.Builder(view.getContext())
 					.setTitle(getString(R.string.list_saved_autoimport_title))
 					.setMessage(getString(R.string.list_saved_autoimport_text))
 					.setPositiveButton(
 							getString(R.string.list_saved_autoimport_positive),
 							new OnClickListener() {
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									ProgressDialog progressDialog = new ProgressDialog(
 											view.getContext());
 									progressDialog
 											.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 									progressDialog
 											.setTitle(getString(R.string.list_saved_autoimport_progress_title));
 									progressDialog
 											.setMessage(getString(R.string.list_saved_autoimport_progress_text));
 									progressDialog.setCancelable(false);
 									new ImportTask(progressDialog)
 											.execute(Helpers.dataDirectoryPath
 													+ "/routes.csv");
 								}
 							})
 					.setNegativeButton(
 							getString(R.string.list_saved_autoimport_negative),
 							new OnClickListener() {
 								public void onClick(DialogInterface arg0,
 										int arg1) {
 									// ignore
 								}
 							}).show();
 		}
 	}
 
 	private class ImportTask extends
 			AsyncTask<String, Void, ArrayList<Boolean>> {
 		private ProgressDialog progressDialog;
 
 		public ImportTask(ProgressDialog dialogFromActivity) {
 			progressDialog = dialogFromActivity;
 		}
 
 		public void onPreExecute() {
 			progressDialog.show();
 		}
 
 		protected ArrayList<Boolean> doInBackground(String... args) {
 			return new Journey(progressDialog.getContext())
 					.importFromCSV(args[0]);
 		}
 
 		protected void onPostExecute(ArrayList<Boolean> statuses) {
 			progressDialog.dismiss();
 
 			int successes = 0;
 			int failures = 0;
 			for (Boolean status : statuses) {
 				if (status == true) {
 					successes += 1;
 				} else {
 					failures += 1;
 				}
 			}
 
 			Intent intent = ListSavedActivity.this.getIntent();
 			ListSavedActivity.this.finish();
 			startActivity(intent);
 
 			Toast.makeText(
 					getBaseContext(),
 					getString(R.string.list_saved_import_status, successes,
 							failures), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private class ExportTask extends AsyncTask<Void, Void, Boolean> {
 		private ProgressDialog progressDialog;
 
 		public ExportTask(ProgressDialog dialogFromActivity) {
 			progressDialog = dialogFromActivity;
 		}
 
 		public void onPreExecute() {
 			progressDialog.show();
 		}
 
 		protected Boolean doInBackground(Void... args) {
 			return new Journey(progressDialog.getContext()).exportToCSV();
 		}
 
 		protected void onPostExecute(Boolean status) {
 			progressDialog.dismiss();
 			String msg;
 			if (status == true) {
 				msg = getString(R.string.list_saved_export_success,
 						Helpers.exportDirectoryPath + "/routes.csv");
 			} else {
 				msg = getString(R.string.list_saved_export_failed);
 			}
 
 			Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
 		}
 	}
 }
