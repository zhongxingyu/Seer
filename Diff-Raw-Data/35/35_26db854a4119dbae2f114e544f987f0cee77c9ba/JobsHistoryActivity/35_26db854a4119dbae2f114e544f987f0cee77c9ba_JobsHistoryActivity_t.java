 package com.skybot.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.Toast;
 import com.skybot.activities.delegate.ActionDelegate;
 import com.skybot.adapters.JobsHistoryAdapter;
 import com.skybot.connection.connection.BaseNetworkManager;
 import com.skybot.connection.connection.helper.RequestCreator;
 import com.skybot.connection.connection.helper.RequestHelper;
 import com.skybot.serivce.parser.dataholder.DataHolder;
 import com.skybot.util.Constants;
 import com.skybot.util.Util;
 import com.skybot.util.ViewTracker;
 
 /**
  * Representing and controlling Job History items and logs.
  * 
  * @author gor, armenabrahamyan
  * 
  */
 
 public class JobsHistoryActivity extends ListActivity implements ActionDelegate {
 
 	static final String KEY_TITLE = "id";
 	static final String KEY_RUNNUMBER = "runnumber";
 	static final String KEY_SUITRUNNUMBER = "suitrunnumber";
 
 	private ListView listView;
 	private JobsHistoryAdapter adapter;
 
 	ArrayList<HashMap<String, String>> jobsList = new ArrayList<HashMap<String, String>>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		listView = getListView(); //
 		adapter = new JobsHistoryAdapter(this, jobsList);
 		listView.setAdapter(adapter);
 	}
 
 	private void getJobsHistoryResponse() {
 		Util.showOrHideActivityIndicator(JobsHistoryActivity.this.getParent(),
 				0, "Requesting Job Histories...");
 		RequestCreator creator = new RequestCreator();
 		BaseNetworkManager baseNetworkManager = new BaseNetworkManager();
 		String system_Time = Long.toString(System.currentTimeMillis());
 
 		Map<String, String> job_params = creator
 				.createAppropriateMapRequest(
 						Constants.DATE,
 						system_Time,
 						Constants.RESULTS,
 						"500",
 						Constants.SORT,
 						"id",
 						Constants.DIRECTION,
 						"DESC",
 						Constants.TAG,
 						"",
 						Constants.TAG_MATCH_ANY,
 						"false",
 						Constants.START,
 						String.valueOf(DataHolder.getInstance().JOBS_HISTORIES_MORE_START_INDEX),
 						Constants.LIMIT, String.valueOf(DataHolder
 								.getInstance().JOBS_HISTORIES_MORE_END_INDEX)
 
 				// Additional constants for job history
 				/*
 				 * Constants.DATAFILTERFIELD, "server_initiated_time_utc",
 				 * Constants.DATAFILTERDATACOMPARASION, "eq",
 				 * Constants.DATAFILTERDATATYPE, "dateTime",
 				 * Constants.DATAFILTERVALUE, "2013-02-26T00:00:00",
 				 * Constants.EXCLUDETIMEDINTERVAL, "false", Constants.START,
 				 * "0", Constants.LIMIT, "3"
 				 */
 
 				);
 
 		final RequestHelper reqHelper = new RequestHelper();
 		String urlStringWithParams = reqHelper.constructGetRequestString(
 				job_params, Constants.SERVER_URL,
 				Constants.JOBHISTORY_SERVICE_URL);
 
 		baseNetworkManager.constructConnectionAndHitGET(
 				"Jobs History Recieved", "Jobs History Request Started",
 				urlStringWithParams, this, Constants.JOBSHISTORY_VIEW,
 				Constants.JOBHISTORY_SERVICE_URL);
 	}
 
 	private void getJobsMoreHistoryResponse() {
 		Util.showOrHideActivityIndicator(JobsHistoryActivity.this.getParent(),
 				0, "Requesting Job Histories...");
 		RequestCreator creator = new RequestCreator();
 		BaseNetworkManager baseNetworkManager = new BaseNetworkManager();
 		String system_Time = Long.toString(System.currentTimeMillis());
 
 		Map<String, String> job_params = creator
 				.createAppropriateMapRequest(Constants.DATE, system_Time,
						Constants.RESULTS, "500",
 						Constants.SORT, "id", Constants.DIRECTION, "DESC",
						Constants.TAG, "", Constants.TAG_MATCH_ANY, "false", 
 						Constants.START, String.valueOf(DataHolder
								.getInstance().JOBS_HISTORIES_MORE_START_INDEX),
						Constants.LIMIT, String.valueOf(DataHolder
								.getInstance().JOBS_HISTORIES_MORE_END_INDEX)
 
 				// Additional constants for job history
 				/*
 				 * Constants.DATAFILTERFIELD, "server_initiated_time_utc",
 				 * Constants.DATAFILTERDATACOMPARASION, "eq",
 				 * Constants.DATAFILTERDATATYPE, "dateTime",
 				 * Constants.DATAFILTERVALUE, "2013-02-26T00:00:00",
 				 * Constants.EXCLUDETIMEDINTERVAL, "false", Constants.START,
 				 * "0", Constants.LIMIT, "3"
 				 */
 
 				);
 
 		final RequestHelper reqHelper = new RequestHelper();
 		String urlStringWithParams = reqHelper.constructGetRequestString(
 				job_params, Constants.SERVER_URL,
 				Constants.JOBHISTORY_SERVICE_URL);
 
 		baseNetworkManager.constructConnectionAndHitGET(
 				"Jobs History Recieved", "Jobs History Request Started",
 				urlStringWithParams, this, Constants.JOBSHISTORY_VIEW,
 				Constants.MORE_JOB_HISTORIES);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		ViewTracker.getInstance().setCurrentContext(this);
 
 		if (DataHolder.getInstance().jobHistoriesList.isEmpty()) {
 			getJobsHistoryResponse();
 		}
 
 		// listView = (ListView) findViewById(R.id.listView2);
 
 		listView = getListView();
 		if (jobsList != null) {
 			adapter.data = jobsList;
 			adapter.notifyDataSetChanged();
 		}
 	};
 
 	@Override
 	public void didFinishRequestProcessing() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void didFinishRequestProcessing(
 			ArrayList<HashMap<String, String>> list, String service) {
 		jobsList = list;
 
 		listView = getListView();
 		if (jobsList != null) {
 			Util.showOrHideActivityIndicator(
 					JobsHistoryActivity.this.getParent(), 1,
 					"Requesting Job Histories...");
 			adapter.data = jobsList;
 			adapter.notifyDataSetChanged();
 		}
 	}
 
 	@Override
 	public void didFailRequestProcessing() {
 		Util.showOrHideActivityIndicator(JobsHistoryActivity.this.getParent(),
 				1, "Requesting Job Histories...");
 		Toast.makeText(getApplicationContext(), "Request Failed",
 				Toast.LENGTH_SHORT).show();
 
 	}
 
 	@Override
 	public void didFinishRequestProcessing(
 			ArrayList<HashMap<String, String>> list) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onBackPressed() {
 		showDialog(10);
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case 10:
 			// Create out AlterDialog
 			Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("Do you want to log out?");
 			builder.setCancelable(true);
 			builder.setPositiveButton("Yes", new OkOnClickListener());
 			builder.setNegativeButton("No", new CancelOnClickListener());
 			AlertDialog dialog = builder.create();
 			dialog.show();
 		}
 		return super.onCreateDialog(id);
 	}
 
 	private final class CancelOnClickListener implements
 			DialogInterface.OnClickListener {
 		public void onClick(DialogInterface dialog, int which) {
 
 		}
 	}
 
 	private final class OkOnClickListener implements
 			DialogInterface.OnClickListener {
 		public void onClick(DialogInterface dialog, int which) {
 			JobsActivity.getActivity().signOutRequest();
 			JobsHistoryActivity.this.finish();
 			DataHolder.getInstance().emptyDataSet();
 			Toast.makeText(getApplicationContext(), "Log out",
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 	/****************************** Menu Callbacks ************************************/
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.layout.more_refresh_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.menu_refresh:
 			DataHolder.getInstance().JOBS_HISTORIES_MORE_END_INDEX = 5;
 			DataHolder.getInstance().JOBS_HISTORIES_MORE_START_INDEX = 0;
 			getJobsHistoryResponse();
 			return true;
 		case R.id.menu_more:
 			DataHolder.getInstance().JOBS_HISTORIES_MORE_END_INDEX += DataHolder
 					.getInstance().JOBS_HISTORIES_NEXT_STEP;
 			DataHolder.getInstance().JOBS_HISTORIES_MORE_START_INDEX += DataHolder
 					.getInstance().JOBS_HISTORIES_NEXT_STEP;
 			getJobsMoreHistoryResponse();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
