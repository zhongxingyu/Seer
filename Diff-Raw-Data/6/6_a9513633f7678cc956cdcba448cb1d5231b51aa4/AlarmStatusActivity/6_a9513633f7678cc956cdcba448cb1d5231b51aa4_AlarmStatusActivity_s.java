 /*
  * Copyright (C) 2011 AlarmApp.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at 
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  */
 
 package org.alarmapp.activities;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 
 import org.alarmapp.AlarmApp;
 import org.alarmapp.Broadcasts;
 import org.alarmapp.R;
 import org.alarmapp.activities.adapters.AlarmedUserAdapter;
 import org.alarmapp.model.Alarm;
 import org.alarmapp.model.AlarmedUser;
 import org.alarmapp.model.classes.AlarmData;
 import org.alarmapp.model.classes.AlarmedUserData;
 import org.alarmapp.util.ActivityUtil;
 import org.alarmapp.util.Ensure;
 import org.alarmapp.util.IntentUtil;
 import org.alarmapp.util.LogEx;
 import org.alarmapp.web.WebException;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class AlarmStatusActivity extends Activity {
 
 	Alarm alarm;
 	ListView lvAlarmedUsers;
 	TextView tvAcceptCount;
 	ImageButton btRefresh;
 	ImageButton btMap;
 	ProgressBar pbLoader;
 
 	private final OnClickListener refreshListener = new OnClickListener() {
 
 		public void onClick(View v) {
 			startFetchAlarmStatus();
 		}
 	};
 
 	private final BroadcastReceiver alarmStatusReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			final AlarmedUser changedUser = AlarmedUserData.create(intent
 					.getExtras());
 
 			if (changedUser.getOperationId().equals(alarm.getOperationId())) {
 				runOnUiThread(new Runnable() {
 
 					public void run() {
 						refreshAlarmStatusView(changedUser);
 					}
 				});
 			}
 		}
 	};
 
 	private class LoadAlarmstatusAsyncTask extends
 			AsyncTask<Alarm, Void, Collection<AlarmedUser>> {
 
 		@Override
 		protected void onPreExecute() {
 			AlarmStatusActivity.this.btRefresh.setVisibility(ImageButton.GONE);
 			AlarmStatusActivity.this.pbLoader
 					.setVisibility(ProgressBar.VISIBLE);
 		}
 
 		@Override
 		protected Collection<AlarmedUser> doInBackground(Alarm... params) {
 			Ensure.valid(params.length == 1);
 
 			Alarm alarm = params[0];
 
 			try {
 				final Collection<AlarmedUser> alarmedUsers = AlarmApp
 						.getAuthWebClient().getAlarmStatus(
 								alarm.getOperationId());
 
 				return alarmedUsers;
 
 			} catch (final WebException e) {
 				LogEx.exception("Failed to load the user list", e);
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Collection<AlarmedUser> result) {
 			super.onPostExecute(result);
 
 			AlarmStatusActivity.this.btRefresh
 					.setVisibility(ImageButton.VISIBLE);
 			AlarmStatusActivity.this.pbLoader.setVisibility(ProgressBar.GONE);
 
 			if (result == null)
 				return;
 
 			displayAlarmStatusView(result);
 
 		}
 
 	}
 
 	private void addUser(AlarmedUser user) {
 
 		this.alarm.updateAlarmedUser(user);
 
 		LogEx.verbose("Number of positions: " + user.getPositions().size());
 
 		LogEx.verbose("Added " + user + ". Hash is " + user.hashCode()
 				+ ". User id is " + user.getId());
 	}
 
 	private void displaySummary() {
 		int accepted = 0;
 
 		for (AlarmedUser u : alarm.getAlarmedUsers()) {
 			if (u.hasAccepted()) {
 				accepted++;
 			}
 		}
 
 		this.tvAcceptCount.setText("akzeptiert: " + accepted);
 	}
 
 	private void displayAlarmStatusView(Collection<AlarmedUser> users) {
 		LogEx.verbose("Display the alarm status for " + users.size()
 				+ " fire fighters");
 
 		for (AlarmedUser u : users)
 			addUser(u);
 
 		alarm.save();
 		LogEx.verbose("Alarmed user collection contains "
 				+ alarm.getAlarmedUsers().size() + " elements");
 
 		fillAlarmStatusView();
 	}
 
 	Comparator<AlarmedUser> comparator = new Comparator<AlarmedUser>() {
 		public int compare(AlarmedUser lhs, AlarmedUser rhs) {
 			return lhs.getFullName().compareTo(rhs.getFullName());
 		}
 	};
 	private OnClickListener mapClickListener = new OnClickListener() {
 
 		public void onClick(View v) {
 			IntentUtil.displayAlarmMapActivity(AlarmStatusActivity.this, alarm);
 
 		}
 	};
 
 	private void fillAlarmStatusView() {
		ArrayList<AlarmedUser> usersList = new ArrayList<AlarmedUser>(
				this.alarm.getAlarmedUsers());
 		Collections.sort(usersList, AlarmedUserData.StatusComparator);
 		AlarmedUserAdapter adapter = new AlarmedUserAdapter(this, usersList);
 
 		this.lvAlarmedUsers.setAdapter(adapter);
 
 		displaySummary();
 	}
 
 	private void refreshAlarmStatusView(AlarmedUser changedUser) {
 		LogEx.verbose("Received Alarm status Update for User " + changedUser);
 
 		addUser(changedUser);
 		alarm.save();
 
 		fillAlarmStatusView();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.alarm_status);
 
 		LogEx.info("Started AlarmStatusActivity");
 
 		this.lvAlarmedUsers = (ListView) findViewById(R.id.lvAlarmedUserList);
 		this.btRefresh = (ImageButton) findViewById(R.id.btRefresh);
 		this.btMap = (ImageButton) findViewById(R.id.btMap);
 		this.tvAcceptCount = (TextView) findViewById(R.id.tvAcceptCount);
 		this.pbLoader = (ProgressBar) findViewById(R.id.pb_loading);
 
 		if (AlarmData.isAlarmDataBundle(savedInstanceState)) {
 
 			LogEx.info("savedInstanceState is AlarmDataBundle");
 
 			alarm = AlarmData.create(savedInstanceState);
 			fillAlarmStatusView();
 
 		} else {
 			Ensure.valid(AlarmData.isAlarmDataBundle(getIntent().getExtras()));
 			alarm = AlarmData.create(getIntent().getExtras());
 			fillAlarmStatusView();
 
 			if (isAlarmStatusUpdateRequired())
 				startFetchAlarmStatus();
 		}
 
 		btRefresh.setOnClickListener(refreshListener);
 		btMap.setOnClickListener(mapClickListener);
 
 		ActivityUtil.makeVisible(this);
 	}
 
 	private boolean isAlarmStatusUpdateRequired() {
 		Calendar tenMinutesBeforNow = Calendar.getInstance();
 		tenMinutesBeforNow.add(Calendar.MINUTE, -10);
 
 		return alarm.getAlarmed().after(tenMinutesBeforNow.getTime());
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		LogEx.info("Saving State");
 		outState.putAll(alarm.getBundle());
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		Broadcasts.registerForAlarmstatusChangedBroadcast(this,
 				alarmStatusReceiver);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		this.unregisterReceiver(alarmStatusReceiver);
 	}
 
 	private void displayError(String message) {
 		ActivityUtil.displayToast(this, message, 15);
 	}
 
 	private void startFetchAlarmStatus() {
 
 		new LoadAlarmstatusAsyncTask().execute(this.alarm);
 	}
 
 	@Override
 	public void onBackPressed() {
 		IntentUtil.displayAlarmActivity(AlarmStatusActivity.this, this.alarm);
 		finish();
 	}
 }
