 /**
  * 	 Scharing - Allows you to set a ring, vibrate and silence shedule for your android device.
  *    Copyright (C) 2009  Wilby C. Jackson Jr.
  *
  *    This program is free software; you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation; either version 2 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License along
  *    with this program; if not, write to the Free Software Foundation, Inc.,
  *    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *    
  *    Contact: jacksw02-at-gmail-dot-com
  */
 
 package net.wcjj.scharing;
 
 import java.io.IOException;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 public class DailyScheduleListActivity extends ListActivity {
 
 	public static int WEEK_DAY;
 	private DailyScheduleAdapter mAdapter;
 	private final String TAG = "Scharing_DailyScheduleListActivity";
 
 	@Override
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.lv_container);
 
 		this.setTitle(Utilities.DAYS_OF_WEEK_TEXT[WEEK_DAY]);
 		
 //		mSa = new SimpleAdapter(this, Service.getRingSchedule()
 //				.toSimpleAdapterMap(DailyScheduleListActivity.WEEK_DAY),
 //				R.layout.daily_schedule_list_row, new String[] {
 //						Schedule.SCHEDULE_DOW, Schedule.SCHEDULED_TIME,
 //						Schedule.RINGER_MODE }, new int[] { R.id.txtId,
 //						R.id.txtTime, R.id.txtRingMode });
 		Schedule schedule = Service.getRingSchedule();
 		mAdapter = new DailyScheduleAdapter(this, schedule.getDay(WEEK_DAY));
 		setListAdapter(mAdapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.daily_schedule_lv_menu, menu);
 		return true;
 	}
 
 	public void mBtnDelete_Click() {
 		Schedule ringSchedule = Service.getRingSchedule();
 
 		final int CHECKBOX_INDEX = 3;
 		final int TIME_TEXTBOX_INDEX = 1;
 		final int NBR_VIEWS_IN_ROW = 4;
 
 		LinearLayout row = null;
 		Time time = null;
 		CheckBox cb = null;
 		ListView lv = getListView();
 		int rowCount = lv.getCount();
 		int childCount = -1;
 		String strTime;
 		String[] hourMins;
 		// iterate over the views in the ListView
 		// and remove items from the schedule that
 		// have been checked by the user
 		// this also removes the selected times from Services
 		// schedule object
 		for (int i = 0; i < rowCount; i++) {
 			row = (LinearLayout) lv.getChildAt(i);
 			if (row != null) {
 				childCount = row.getChildCount();
 				if (childCount == NBR_VIEWS_IN_ROW) {
 					cb = (CheckBox) row.getChildAt(CHECKBOX_INDEX);
 					if (cb.isChecked()) {
 						strTime = ((TextView) row.getChildAt(TIME_TEXTBOX_INDEX))
 								.getText().toString();
 						hourMins = strTime.split(":");
 						time = Utilities.normalizeToScharingTime(
 								Integer.parseInt(hourMins[0]),
 								Integer.parseInt(hourMins[1])
 			              );
 						ringSchedule.delRingSchedule(WEEK_DAY, 
								time.toMillis(true));						
						mAdapter.notifyDataSetChanged();						
						//row.setVisibility(View.GONE);
 					}
 				}
 			}
 		}
 
 		// Save the changes to disk
 		try {
 			ringSchedule.saveSchedule(this);
 		} catch (IOException e) {
 			Log.e(TAG, Log.getStackTraceString(e));
 		}
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.mBtnDelete:
 			mBtnDelete_Click();
 			return true;
 		}
 		return false;
 	}
 
 }
