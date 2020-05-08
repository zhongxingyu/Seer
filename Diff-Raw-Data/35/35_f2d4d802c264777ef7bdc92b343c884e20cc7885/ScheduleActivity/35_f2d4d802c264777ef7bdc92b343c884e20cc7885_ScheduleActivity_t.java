 package com.dev.campus.schedule;
 
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.dev.campus.R;
 import com.dev.campus.SettingsActivity;
 import com.dev.campus.util.FilterDialog;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 
 public class ScheduleActivity extends ListActivity implements OnItemClickListener {
 
 	private ActionBar mActionBar;
 	private Context mContext;
 	private FilterDialog mFilterDialog;
 	private ScheduleAdapter mScheduleAdapter;
 	private ScheduleGroup mScheduleGroup;
 	private List<ScheduleGroup> mListScheduleGroup;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mFilterDialog = new FilterDialog(this);
 		mActionBar = getActionBar();
 		mActionBar.setDisplayHomeAsUpEnabled(true);
 		mContext = this;
 		mScheduleAdapter = new ScheduleAdapter(this, new ArrayList<ScheduleGroup>());
 
 		ListView listview = getListView();
 		View header = (View) getLayoutInflater().inflate(R.layout.schedule_list_header, listview, false);
 		listview.addHeaderView(header, null, false);
 		listview.setAdapter(mScheduleAdapter);
 		listview.setOnItemClickListener(this);
 
 		Spinner schedule_spinner = (Spinner) findViewById(R.id.schedule_spinner);
 		schedule_spinner.setOnItemSelectedListener(new spinnerOnItemSelectedListener());
 		//schedule_spinner.performClick();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.with_actionbar, menu);
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		mScheduleGroup = (ScheduleGroup) parent.getItemAtPosition(position);
 		Log.d("LogTag", mScheduleGroup.getGroup());
 		Log.d("LogTag", mScheduleGroup.getUrl());
 		//TODO Export selected schedule
 		new ParseScheduleTask().execute();
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			startActivity(new Intent(ScheduleActivity.this, SettingsActivity.class));
 			return true;
 		case R.id.menu_filters:
 			mFilterDialog.showDialog();
 			return true;
 		case android.R.id.home:
 			finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
	public void reloadContent() {
 		mScheduleAdapter.clear();
 		mScheduleAdapter.addAll(mListScheduleGroup);
 		mScheduleAdapter.notifyDataSetChanged();
 	}
 
 	private class ListGroupTask extends AsyncTask<String, Void, Void> {
 
 		@Override
 		protected Void doInBackground(String... urls) {
 			if (urls.length > 0) {
 				try {
 					mListScheduleGroup = new ScheduleParser().parseFeed(urls[0]);
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
			reloadContent();
 		}
 	}
 
 	private class ParseScheduleTask extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... args) {
 			try {
 				new ScheduleParser().parseSchedule(mContext, mScheduleGroup.getUrl());
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 
 		}
 	}
 
 	private class spinnerOnItemSelectedListener implements OnItemSelectedListener {
 
 		@Override
 		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 			String url = "";
 			if (pos == 1) { // Licence Semestre 1
 				url = "http://www.disvu.u-bordeaux1.fr/et/edt_etudiants2/Licence/Semestre1/finder.xml";
 			}
 			else if (pos == 2) { // Licence Semestre 2
 				url = "http://www.disvu.u-bordeaux1.fr/et/edt_etudiants2/Licence/Semestre2/finder.xml";
 			}
 			else if (pos == 3) { // Master Semestre 1
 				url = "http://www.disvu.u-bordeaux1.fr/et/edt_etudiants2/Master/Semestre1/finder.xml";
 			}
 			else if (pos == 4) { // Master Semestre 2
 				url = "http://www.disvu.u-bordeaux1.fr/et/edt_etudiants2/Master/Semestre2/finder.xml";
 			}
 			else {
 				return;
 			}
 			new ListGroupTask().execute(url);
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> parent) {
 
 		}
 	}
 }
