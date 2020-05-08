 package com.app.afridge;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class inside extends Activity {
 
 	private static final int ADD_ITEM = 1;
 	Model model;
 	ArrayList<String> item = new ArrayList<String>();
 	ArrayList<String> item_to_modify = new ArrayList<String>();
 	ArrayList<String> history_item = new ArrayList<String>();
 	ArrayList<String> tempArray = new ArrayList<String>();
 	DatabaseHelper myDb;
 	int temp = 0;
 	ImageAdapter ia;
 	SharedPreferences prefs;
 	private static final int MENU_CHECK = Menu.FIRST;
 	private static final int MENU_SETTINGS = MENU_CHECK + 1;
 	private static final int MENU_LIST = MENU_SETTINGS + 1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.inside);
 
 		prefs = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 
 		int warning_days = Integer.parseInt(prefs
 				.getString("PREF_WARNING", "3"));
 		model = new Model(this, warning_days);
 		model.check_exp_date(true, true);
 
 		ia = new ImageAdapter(this);
 
 		GridView gridview = (GridView) findViewById(R.id.gridview);
 		gridview.setAdapter(ia);
 
 		gridview.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				handleItem(position);
 				temp = position;
 			}
 		});
 
 		myDb = new DatabaseHelper(this);
 		{
 			try {
 				myDb.createDataBase();
 			} catch (IOException ioe) {
 				throw new Error("Unable to create database");
 			}
 			try {
 				myDb.openDataBase();
 			} catch (SQLException sqle) {
 				throw sqle;
 			}
 			myDb.getReadableDatabase();
 		}
 
 	}
 
 	@Override
 	protected void onDestroy() {
 		myDb.close();
 		super.onDestroy();
 	}
	
	@Override
	protected void onPause() {
		myDb.close();
		super.onPause();
	}
 
 	@Override
 	protected void onResume() {
 		myDb = new DatabaseHelper(this);
 		{
 			try {
 				myDb.createDataBase();
 			} catch (IOException ioe) {
 				throw new Error("Unable to create database");
 			}
 			try {
 				myDb.openDataBase();
 			} catch (SQLException sqle) {
 				throw sqle;
 			}
 			myDb.getReadableDatabase();
 		}
 		super.onResume();
 	}
 
 	public void handleItem(int position) {
 		Cursor c = myDb.getItem(Integer.toString(position));
 		if (c.moveToFirst()) {
 			if (c.getString(6).equalsIgnoreCase("true"))
 				if_empty_dialog(position);
 			else if (c.getString(6).equalsIgnoreCase("false")
 					|| c.getString(6).equalsIgnoreCase("warning")
 					|| c.getString(6).equalsIgnoreCase("expired")) {
 				not_empty_dialog(position, c);
 			}
 		}
 
 	}
 
 	public void not_empty_dialog(int position, final Cursor c) {
 
 		final Dialog dialog = new Dialog(this);
 
 		dialog.setContentView(R.layout.show_item_dialog);
 		dialog.setTitle("aFridge");
 
 		TextView text = (TextView) dialog.findViewById(R.id.text);
 		text.setText("Item description:");
 		
 		TextView textState = (TextView) dialog.findViewById(R.id.textState);
 		String state = c.getString(6);
 		if(state.equalsIgnoreCase("false"))
 			textState.setText("State: OK");
 		else if(state.equalsIgnoreCase("warning"))
 			textState.setText("State: Soon to expire!");
 		else if(state.equalsIgnoreCase("expired"))
 			textState.setText("State: Expired!");
 		
 		ImageView image = (ImageView) dialog.findViewById(R.id.image);
 		image.setImageResource(ia.getResourcePic(position));
 
 		TextView name = (TextView) dialog.findViewById(R.id.text_name);
 		if (!c.getString(1).equalsIgnoreCase(""))
 			name.setText("- " + c.getString(1));
 		else
 			name.setText("- name: none");
 
 		TextView type = (TextView) dialog.findViewById(R.id.text_type);
 		type.setText("- " + c.getString(2));
 
 		TextView quant = (TextView) dialog.findViewById(R.id.text_quant);
 		if (!c.getString(3).equalsIgnoreCase(""))
 			quant.setText("- " + c.getString(3));
 		else
 			quant.setText("- quantity: unknown");
 		TextView qtype = (TextView) dialog.findViewById(R.id.text_qtype);
 		qtype.setText(c.getString(4));
 
 		TextView details = (TextView) dialog.findViewById(R.id.text_details);
 		if (!c.getString(5).equalsIgnoreCase(""))
 			details.setText("- " + c.getString(5));
 		else
 			details.setText("- details: none");
 
 		TextView exp = (TextView) dialog.findViewById(R.id.text_exp);
 		if (!c.getString(7).equalsIgnoreCase(""))
 			exp.setText("exp. date: " + c.getString(7));
 		else
 			exp.setText("exp. date: unknown or none");
 
 		Button change = (Button) dialog.findViewById(R.id.change);
 		change.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				item_to_modify.clear();
 				item_to_modify.add(c.getString(1));
 				item_to_modify.add(c.getString(2));
 				item_to_modify.add(c.getString(3));
 				item_to_modify.add(c.getString(4));
 				item_to_modify.add(c.getString(5));
 				item_to_modify.add(c.getString(7));
 				addItem(true);
 				dialog.dismiss();
 			}
 		});
 
 		Button delete = (Button) dialog.findViewById(R.id.delete);
 		delete.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				item.clear();
 				item.add(0, Integer.toString(temp));// id of the item...
 				item.add(1, "");
 				item.add(2, "empty");
 				item.add(3, "");
 				item.add(4, "");
 				item.add(5, "");
 				item.add(6, "true");
 				item.add(7, "");
 
 				Cursor c = myDb.getItem(Integer.toString(temp));
 				history_item.clear();
 
 				int next_id;
 				Cursor c2 = myDb.getHistory();
 				if (c2.getCount() == 0) {
 					next_id = 0;
 				} else {
 					c2.moveToLast();
 					next_id = c2.getInt(0) + 1;
 				}
 
 				Calendar cal = Calendar.getInstance();
 
 				int mYear = cal.get(Calendar.YEAR);
 				int mMonth = cal.get(Calendar.MONTH) + 1;
 				int mDay = cal.get(Calendar.DAY_OF_MONTH);
 				int mHour = cal.get(Calendar.HOUR_OF_DAY);
 				int mMinute = cal.get(Calendar.MINUTE);
 
 				history_item.add(mHour + ":" + mMinute + " " + mDay + "-"
 						+ mMonth + "-" + mYear);
 				history_item.add("position:" + Integer.toString(temp));
 
 				history_item.add(c.getString(1));
 				history_item.add(c.getString(2));
 				history_item.add(c.getString(3));
 				history_item.add(c.getString(4));
 				history_item.add(c.getString(5));
 				history_item.add(c.getString(7));
 				history_item.add("Deleted");
 
 				boolean addToHistory = myDb.addToHistory(Integer
 						.toString(next_id), history_item);
 
 				boolean modify = myDb.modifyItem(item);
 				ia.refresh();
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 
 	}
 
 	public void if_empty_dialog(int position) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Do you want to add an item?").setIcon(
 				R.drawable.icon).setTitle(R.string.app_name).setCancelable(
 				false).setPositiveButton("Yes",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						addItem(false);
 					}
 				});
 		builder.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	public void addItem(boolean modify) {
 		if (modify) {
 			Intent intentAddItem = new Intent(this, temp.class);
 			intentAddItem.putStringArrayListExtra("item_to_modify",
 					item_to_modify);
 			startActivityForResult(intentAddItem, ADD_ITEM);
 
 		} else {
 			Intent intentAddItem = new Intent(this, temp.class);
 			startActivityForResult(intentAddItem, ADD_ITEM);
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == ADD_ITEM)
 			if (resultCode == RESULT_OK) {
 				Log.d("xxx", "Vlezeno e vo if-ot");
 				item.clear();
 				item.add(0, Integer.toString(temp));
 				item.addAll(data.getStringArrayListExtra("Item"));
 				
 				boolean modify = myDb.modifyItem(item);
 				model.check_exp_date(true, true);
 				ia.refresh();
 
 				// Here we add the new/modified item to the history table...
 				history_item.clear();
 				int next_id;
 				Cursor c = myDb.getHistory();
 				if (c.getCount() == 0) {
 					next_id = 0;
 				} else {
 					c.moveToLast();
 					next_id = c.getInt(0) + 1;
 				}
 
 				Calendar cal = Calendar.getInstance();
 
 				int mYear = cal.get(Calendar.YEAR);
 				int mMonth = cal.get(Calendar.MONTH) + 1;
 				int mDay = cal.get(Calendar.DAY_OF_MONTH);
 				int mHour = cal.get(Calendar.HOUR_OF_DAY);
 				int mMinute = cal.get(Calendar.MINUTE);
 
 				history_item.add(mHour + ":" + mMinute + " " + mDay + "-"
 						+ mMonth + "-" + mYear);
 				history_item.add("position:" + Integer.toString(temp));
 
 				tempArray.clear();
 				tempArray.addAll(data.getStringArrayListExtra("Item"));
 
 				history_item.add(tempArray.get(0));
 				history_item.add(tempArray.get(1));
 				history_item.add(tempArray.get(2));
 				history_item.add(tempArray.get(3));
 				history_item.add(tempArray.get(4));
 				history_item.add(tempArray.get(6));
 
 				if (data.getBooleanExtra("isModified", false)) {
 					history_item.add("Modified");
 				} else
 					history_item.add("Added");
 				boolean addToHistory = myDb.addToHistory(Integer
 						.toString(next_id), history_item);
 			}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu pMenu) {
 		pMenu.add(0, MENU_CHECK, Menu.NONE, "Check expiration dates!").setIcon(
 				android.R.drawable.ic_menu_manage);
 		pMenu.add(0, MENU_SETTINGS, Menu.NONE, "Settings").setIcon(
 				android.R.drawable.ic_menu_preferences);
 		pMenu.add(0, MENU_LIST, Menu.NONE, "View List...").setIcon(
 				android.R.drawable.ic_menu_sort_alphabetically);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_CHECK:
 			model.check_exp_date(true, false);
 			return true;
 
 		case MENU_SETTINGS:
 			Intent settingsActivity = new Intent(getBaseContext(), prefs.class);
 			startActivity(settingsActivity);
 			return true;
 
 		case MENU_LIST:
 			model.show_list();
 			return true;
 		default:
 			return true;
 		}
 	}
 }
