 package com.eyebrowssoftware.bptrackerfree;
 
 import java.text.DateFormat;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.ContentUris;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 import com.eyebrowssoftware.bptrackerfree.BPRecords.BPRecord;
 
 public class BPRecordList extends ListActivity implements OnClickListener {
 	
 	private static final String TAG = "BPRecordList";
 
 	private static final String[] PROJECTION = { 
 		BPRecord._ID,
 		BPRecord.SYSTOLIC, 
 		BPRecord.DIASTOLIC, 
 		BPRecord.PULSE,
 		BPRecord.CREATED_DATE,
 		BPRecord.NOTE 
 	};
 
 	// private static final int COLUMN_ID_INDEX = 0;
 	// private static final int COLUMN_SYSTOLIC_INDEX = 1;
 	// private static final int COLUMN_DIASTOLIC_INDEX = 2;
 	// private static final int COLUMN_PULSE_INDEX = 3;
 	private static final int COLUMN_CREATED_AT_INDEX = 4;
 	// private static final int COLUMN_NOTE_INDEX = 5;
 
 	// Menu item ids
 	public static final int MENU_ITEM_DELETE = Menu.FIRST;
 	public static final int MENU_ITEM_EDIT = Menu.FIRST + 1;
 	public static final int MENU_ITEM_SEND = Menu.FIRST + 2;
 	
 	private static final int DELETE_DIALOG_ID = 0;
 
 	private static final String[] VALS = { 
 		BPRecord.CREATED_DATE,
 		BPRecord.CREATED_DATE, 
 		BPRecord.SYSTOLIC, 
 		BPRecord.DIASTOLIC,
 		BPRecord.PULSE,
 		BPRecord.NOTE
 	};
 
 	private static final int[] IDS = { 
 		R.id.date, 
 		R.id.time, 
 		R.id.sys_value,
 		R.id.dia_value, 
 		R.id.pulse_value,
 		R.id.note
 	};
 
	private LinearLayout mEmptyContent;
	
 	private SimpleCursorAdapter mAdapter;
 
 	/** Called when the activity is first created. */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Intent intent = getIntent();
 		if (intent.getData() == null) {
 			intent.setData(BPRecords.CONTENT_URI);
 		}
 		setContentView(R.layout.bp_record_list);
 		
 		setTitle(R.string.title_list);
 		
		mEmptyContent = (LinearLayout) findViewById(R.id.empty_content);
 		mEmptyContent.setOnClickListener(this);
 		
 		View v = this.getLayoutInflater().inflate(R.layout.bp_record_list_header, null);
 
 		ListView lv = getListView();
 		lv.addHeaderView(v, null, true);
 		lv.setOnCreateContextMenuListener(this);
 		
 		Cursor cursor = managedQuery(intent.getData(), PROJECTION, null, null, BPRecord.CREATED_DATE + " DESC");
 		
 		mAdapter = new SimpleCursorAdapter(this, R.layout.bp_record_list_item, cursor, VALS, IDS);
 		mAdapter.setViewBinder(new MyViewBinder());
 		if(cursor == null) {
 			setTitle(getText(R.string.title_error));
 		}
 		setListAdapter(mAdapter);
 	}
 	
 	private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {
 		String val;
 
 		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 			int id = view.getId();
 
 			switch (id) {
 			case R.id.sys_value:
 			case R.id.dia_value:
 			case R.id.pulse_value: // Pulse
 				((TextView) view).setText(String.valueOf(cursor.getInt(columnIndex)));
 				return true;
 			case R.id.date: // Date
 			case R.id.time: // Time -- these use the same cursor column
 				long datetime = cursor.getLong(columnIndex);
 				val = (id == R.id.date) ? BPTrackerFree.getDateString(datetime,
 						DateFormat.SHORT) : BPTrackerFree.getTimeString(datetime,
 						DateFormat.SHORT);
 				((TextView)view).setText(val);
 				return true;
 			case R.id.note:
 				String note = cursor.getString(columnIndex);
 				if(note != null && note.length() > 0) {
 					((TextView)view).setText(note);
 					view.setVisibility(View.VISIBLE);
 				} else {
 					view.setVisibility(View.GONE);
 				}
 				return true;
 			default:
 				return false;
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		menu.add(Menu.NONE, MENU_ITEM_SEND, 0, R.string.menu_send);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case MENU_ITEM_SEND:
 			startActivity(new Intent(Intent.ACTION_SEND, BPRecords.CONTENT_URI, this, BPSend.class));
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
 		
 		AdapterView.AdapterContextMenuInfo info;
 		
 		try {
 			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 		} catch (ClassCastException e) {
 			Log.e(TAG, "bad menuInfo", e);
 			return;
 		}
 		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
 		if (cursor != null && cursor.moveToFirst()) {
 			long datetime = cursor.getLong(COLUMN_CREATED_AT_INDEX);
 			String date = BPTrackerFree.getDateString(datetime, DateFormat.SHORT);
 			String time = BPTrackerFree.getTimeString(datetime, DateFormat.SHORT);
 			String fmt = getString(R.string.datetime_format);
 			menu.setHeaderTitle(String.format(fmt, date, time));
 		}
 		// Add a menu item to edit the record
 		menu.add(Menu.NONE, MENU_ITEM_EDIT, 0, R.string.menu_edit);
 		// Add a menu item to delete the record
 		menu.add(Menu.NONE, MENU_ITEM_DELETE, 1, R.string.menu_delete);
 		// Add a menu item to send the record
 		menu.add(Menu.NONE, MENU_ITEM_SEND, 2, R.string.menu_send);
 	}
 	
 	private Uri mContextMenuUri; // set to the uri of the item with the context menu
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info;
 		try {
 			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 			// Get the Uri of the record we're intending to operate on
 			mContextMenuUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
 			switch (item.getItemId()) {
 			case MENU_ITEM_DELETE:
 				showDialog(DELETE_DIALOG_ID);
 				return true;
 			case MENU_ITEM_EDIT:
 				startActivity(new Intent(Intent.ACTION_EDIT, mContextMenuUri, this, BPRecordEditor.class));
 				return true;
 			case MENU_ITEM_SEND:
 				startActivity(new Intent(Intent.ACTION_SEND, mContextMenuUri, this, BPSend.class));
 				return true;
 			}
 			return false;
 		} catch (ClassCastException e) {
 			Log.e(TAG, "bad menuInfo", e);
 			return false;
 		}
 
 	}
 	
 	private void deleteRecord() {
 		getContentResolver().delete(mContextMenuUri, null, null);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DELETE_DIALOG_ID:
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage(getString(R.string.really_delete))
 				.setCancelable(false)
 				.setPositiveButton(getString(R.string.label_yes), new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						deleteRecord();
 					}
 				})
 				.setNegativeButton(getString(R.string.label_no), new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
 				});
 			return builder.create();
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		if(id < 0) {
 			Uri data = getIntent().getData();
 			startActivity(new Intent(Intent.ACTION_INSERT, data, this, BPRecordEditor.class));
 		} else {
 			Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
 			String action = getIntent().getAction();
 			if (Intent.ACTION_PICK.equals(action)
 					|| Intent.ACTION_GET_CONTENT.equals(action)) {
 				// The caller is waiting for us to return a note selected by
 				// the user. The have clicked on one, so return it now.
 				setResult(RESULT_OK, new Intent().setData(uri));
 			} else {
 				// Launch activity to view/edit the currently selected item
 				startActivity(new Intent(Intent.ACTION_EDIT, uri, this, BPRecordEditor.class));
 			}
 		}
 	}
 
 	public void onClick(View v) {
 		Uri data = getIntent().getData();
 		startActivity(new Intent(Intent.ACTION_INSERT, data, this, BPRecordEditor.class));
 	}
 }
