 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  John Lawson
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at 
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package org.nof1trial.nof1.activities;
 
 import org.nof1trial.nof1.Keys;
 import org.nof1trial.nof1.R;
 import org.nof1trial.nof1.services.FinishedService;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.support.v4.app.TaskStackBuilder;
 import android.support.v4.content.LocalBroadcastManager;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 /**
  * Activity to view the treatment schedule
  * 
  * @author John Lawson
  * 
  */
 public class ScheduleViewer extends SherlockActivity {
 
 	private TextView mText;
 
 	/** Current context */
 	private final Context mContext = this;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.schedule_layout);
 
 		SharedPreferences prefs = getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 		// Only want to show schedule if the trial is finished
 		if (prefs.getBoolean(Keys.SCHED_FINISHED, false)) {
 
 			mText = (TextView) findViewById(R.id.schedule_text);
 
 			if (prefs.contains(Keys.CONFIG_SCHEDULE)) {
 				// Schedule already downloaded
 				mText.setText(prefs.getString(Keys.CONFIG_SCHEDULE, ""));
 
 			} else {
 				mText.setText(R.string.downloading_schedule);
 
 				// Try to download the schedule
 				Intent download = new Intent(mContext, FinishedService.class);
 				download.setAction(Keys.ACTION_DOWNLOAD_SCHEDULE);
 				startService(download);
 
 				// Register listener for response
 				LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
				manager.registerReceiver(new ScheduleReceiver(), new IntentFilter());
 
 			}
 
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// Up/home pressed
 			Intent upIntent = new Intent(this, HomeScreen.class);
 			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
 				TaskStackBuilder.create(this).addNextIntent(upIntent).startActivities();
 				finish();
 			} else {
 				NavUtils.navigateUpTo(this, upIntent);
 			}
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private class ScheduleReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			if (Keys.ACTION_DOWNLOAD_SCHEDULE.equals(intent.getAction())) {
 
 				SharedPreferences prefs = context.getSharedPreferences(Keys.CONFIG_NAME, MODE_PRIVATE);
 
 				// Schedule was downloaded successfully
 				if (mText != null) {
 					String sched = prefs.getString(Keys.CONFIG_SCHEDULE, "Error fetching schedule");
 
 					mText.setText(sched);
 				}
 
 				// Unregister receiver
 				LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
 				manager.unregisterReceiver(this);
 
 			} else if (Keys.ACTION_ERROR.equals(intent.getAction())) {
 				// Some problem getting schedule
 				if (mText != null) {
 					mText.setText(R.string.error_downloading_schedule);
 				}
 
 				// Unregister receiver
 				LocalBroadcastManager manager = LocalBroadcastManager.getInstance(context);
 				manager.unregisterReceiver(this);
 			}
 
 		}
 
 	}
 
 }
