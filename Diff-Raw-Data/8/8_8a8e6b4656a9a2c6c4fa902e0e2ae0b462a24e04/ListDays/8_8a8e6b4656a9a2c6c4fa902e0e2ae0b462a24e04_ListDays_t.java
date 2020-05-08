 package ch.almana.android.stechkarte.view;
 
 import android.app.ListActivity;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.TextView;
 import ch.almana.android.stechkarte.R;
 import ch.almana.android.stechkarte.log.Logger;
 import ch.almana.android.stechkarte.model.Day;
 import ch.almana.android.stechkarte.provider.db.DB;
 import ch.almana.android.stechkarte.provider.db.DB.Days;
 import ch.almana.android.stechkarte.provider.db.DB.Timestamps;
 import ch.almana.android.stechkarte.utils.DeleteDayDialog;
 import ch.almana.android.stechkarte.utils.DialogCallback;
 import ch.almana.android.stechkarte.utils.Formater;
 import ch.almana.android.stechkarte.utils.RebuildDaysTask;
 
 public class ListDays extends ListActivity implements DialogCallback {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_PROGRESS);
 		setTitle(R.string.daysListTitle);
 
 		Intent intent = getIntent();
 		if (intent.getData() == null) {
 			intent.setData(Days.CONTENT_URI);
 		}
 
 		Cursor cursor = managedQuery(DB.Days.CONTENT_URI, DB.Days.DEFAULT_PROJECTION, null, null, Days.DEFAULT_SORTORDER);
 
 		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.daylist_item, cursor, new String[] { DB.NAME_ID, DB.Days.NAME_DAYREF, DB.Days.NAME_HOURS_WORKED,
 				DB.Days.NAME_OVERTIME, DB.Days.NAME_HOURS_TARGET, DB.Days.NAME_HOLIDAY, DB.Days.NAME_HOLIDAY_LEFT, DB.Days.NAME_FIXED },
 				new int[] { R.id.TextViewDayRef, R.id.TextViewDayRef, R.id.TextViewHoursWorked, R.id.TextViewOvertime, R.id.TextViewHoursTarget, R.id.TextViewHoliday,
 						R.id.TextViewHolidaysLeft, R.id.ImageViewLock });
 
 		adapter.setViewBinder(new ViewBinder() {
 			@Override
 			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 				if (cursor == null) {
 					return false;
 				}
 				if (columnIndex == DB.Days.INDEX_DAYREF) {
 					Day d = new Day(cursor);
 					int color = Color.GREEN;
 					if (d.isError()) {
 						color = Color.RED;
 					}
 					TextView errorView = (TextView) view.findViewById(R.id.TextViewDayRef);
 					errorView.setTextColor(color);
 					// since we do not set the dayref no: return true;
 				} else if (columnIndex == DB.Days.INDEX_OVERTIME) {
 					Day d = new Day(cursor);
 					CharSequence formatHourMinFromHours = Formater.formatHourMinFromHours(d.getOvertime());
 					((TextView) view.findViewById(R.id.TextViewOvertime)).setText(formatHourMinFromHours);
 					TextView tv = (TextView) ((View) view.getParent()).findViewById(R.id.TextViewOvertimeCur);
 					float overtime = d.getHoursWorked() - d.getHoursTarget();
 					tv.setText(Formater.formatHourMinFromHours(overtime));
					tv.setTextColor(Color.LTGRAY);
 					if (overtime > 5) {
 						tv.setTextColor(Color.RED);
 					} else if (overtime > 3) {
 						tv.setTextColor(Color.YELLOW);
 					}
 					return true;
 				} else if (columnIndex == DB.Days.INDEX_HOURS_WORKED) {
 					float hoursWorked = cursor.getFloat(Days.INDEX_HOURS_WORKED);
 					TextView tv = (TextView) view.findViewById(R.id.TextViewHoursWorked);
 					tv.setText(Formater.formatHourMinFromHours(hoursWorked));
					tv.setTextColor(Color.LTGRAY);
					if (hoursWorked > 12) {
 						tv.setTextColor(Color.RED);
					} else if (hoursWorked > 10) {
 						tv.setTextColor(Color.YELLOW);
 					}
 					return true;
 				} else if (columnIndex == DB.Days.INDEX_HOURS_TARGET) {
 					Day d = new Day(cursor);
 					((TextView) view.findViewById(R.id.TextViewHoursTarget)).setText(Formater.formatHourMinFromHours(d.getHoursTarget()));
 					return true;
 				} else if (columnIndex == DB.Days.INDEX_FIXED) {
 					ImageView iv = (ImageView) view.findViewById(R.id.ImageViewLock);
 					if (cursor.getInt(Days.INDEX_FIXED) > 0) {
 						iv.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_lock_lock));
 					} else {
 						iv.setImageBitmap(null);
 					}
 					return true;
 				}
 				return false;
 			}
 		});
 
 		setListAdapter(adapter);
 
 		getListView().setOnCreateContextMenuListener(this);
 		// dia.dismiss();
 	}
 
 	@Override
 	protected void onResume() {
 		RebuildDaysTask.rebuildDaysIfNeeded(this);
 		super.onResume();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.daylist_option, menu);
 		// menu.getItem(0).setEnabled(!RebuildDaysTask.isRebuilding());
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.itemDaylistRebuild:
 			rebuildDays();
 			break;
 		case R.id.itemDaylistInsertDay:
 			startActivity(new Intent(Intent.ACTION_INSERT, Days.CONTENT_URI));
 			break;
 		case R.id.itemDaylistInsertTImestamp:
 			startActivity(new Intent(Intent.ACTION_INSERT, Timestamps.CONTENT_URI));
 			break;
 		default:
 			return super.onOptionsItemSelected(item);
 
 		}
 		return true;
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
 
 		String action = getIntent().getAction();
 		if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
 			setResult(RESULT_OK, new Intent().setData(uri));
 		} else {
 			startActivity(new Intent(Intent.ACTION_EDIT, uri));
 		}
 	}
 
 	private void rebuildDays() {
 		RebuildDaysTask.rebuildDays(this, null);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		getMenuInflater().inflate(R.menu.daylist_context, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		super.onContextItemSelected(item);
 
 		AdapterView.AdapterContextMenuInfo info;
 		try {
 			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 		} catch (ClassCastException e) {
 			Log.e(Logger.LOG_TAG, "bad menuInfo", e);
 			return false;
 		}
 
 		// Uri uri = ContentUris.withAppendedId(Days.CONTENT_URI, info.id);
 		switch (item.getItemId()) {
 		case R.id.itemDeleteDay: {
 
 			DeleteDayDialog alert = new DeleteDayDialog(this, info.id);
 			alert.setTitle("Delete Day...");
 			alert.show();
 			return true;
 		}
 		}
 		return false;
 
 	}
 
 	@Override
 	public void finished(boolean success) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public Context getContext() {
 		return this;
 	}
 
 }
