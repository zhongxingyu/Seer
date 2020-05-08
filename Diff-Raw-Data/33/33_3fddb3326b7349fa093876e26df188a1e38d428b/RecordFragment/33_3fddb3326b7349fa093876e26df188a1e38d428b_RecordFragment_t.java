 package com.suicune.notasupna;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.AsyncTaskLoader;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 
 import com.suicune.notasupna.database.GradesContract;
 import com.suicune.notasupna.helpers.ConnectLoader;
 import com.suicune.notasupna.helpers.GradesParserLoader;
 
 /**
  * 
  * @author Denis Lapuente
  * 
  */
 @SuppressLint("NewApi")
 public class RecordFragment extends ListFragment {
 
 	private static final int LOADER_COURSE = 1;
 	private static final int LOADER_CONNECTION = 2;
 	private static final int LOADER_PARSER = 3;
 
 	private static final int PROGRESS_SIGN_IN = 1;
 	private static final int PROGRESS_PARSE = 2;
 	private static final int PROGRESS_LOAD = 3;
 
 	private static final int ACTIVITY_PREFERENCES = 1;
 	private static final int ACTIVITY_DETAILS = 2;
 
 	private boolean isLandscape;
 	private boolean isHoneyComb;
 	private boolean isSmallScreen;
 
 	private int mCursorPosition = 0;
 	private int mCurrentCourse = 0;
 
 	private Record mCurrentRecord = null;
 	private Student mStudent = null;
 	private Subject mCurrentSubject = null;
 
 	private View mRecordView;
 	private View mRecordStatusView;
 	private View mDetailsHintView;
 	private View mDetailsView;
 	private View mRecordHeaderView;
 	private TextView mRecordStatusMessageView;
 
 	private SharedPreferences prefs;
	
	private static RecordFragment mRecordFragment;
	
	public static RecordFragment getInstance(){
		if(mRecordFragment == null){
			mRecordFragment = new RecordFragment();
		}
		return mRecordFragment;
	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			isHoneyComb = true;
 		} else {
 			isHoneyComb = false;
 		}
 
 		if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			isLandscape = true;
 		} else {
 			isLandscape = false;
 		}
 
 		checkScreenSize();
 
 		setViews();
 		restoreParameters();
 		loadData();
 		if(isLandscape){
 			mDetailsHintView.setVisibility(View.VISIBLE);
 			mDetailsView.setVisibility(View.GONE);
 		}
 
 		super.onActivityCreated(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.record_fragment, container);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		if (!mCurrentSubject.equals(mCurrentRecord.mSubjectsList.get(position))) {
 			setCursorPosition(position);
 			showDetails(mCurrentSubject);
 		}
 		super.onListItemClick(l, v, position, id);
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case ACTIVITY_DETAILS:
 			if (resultCode == Activity.RESULT_OK) {
 
 			}
 			break;
 		case ACTIVITY_PREFERENCES:
 			if (resultCode == Activity.RESULT_OK) {
 
 			}
 			break;
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.action_bar_record, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_record_refresh:
 			getActivity().getSupportLoaderManager().initLoader(
 					LOADER_CONNECTION, null, new AsyncHelper());
 			break;
 		case R.id.action_record_preferences:
 			Intent intent = new Intent(getActivity(), PreferencesActivity.class);
 			startActivityForResult(intent, ACTIVITY_PREFERENCES);
 			break;
 		case R.id.action_record_close_session:
 			getActivity().finish();
 			break;
 		}
 		return true;
 	}
 
 	private void showDetails(Subject subject) {
 		mCurrentSubject = subject;
 		if (isLandscape) {
 			DetailsFragment details = DetailsFragment
 					.newInstance(mCursorPosition);
 			if (details != null) {
 				details.setSubject(mCurrentSubject);
 				
 				FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
 				transaction.replace(R.id.record_details, details);
 				transaction.commit();
 			}
 		} else {
 			Intent intent = new Intent(getActivity(), DetailsActivity.class);
 			intent.putExtra(DetailsActivity.EXTRA_SUBJECT, subject);
 			startActivity(intent);
 		}
 	}
 
 	@SuppressLint("NewApi")
 	private void checkScreenSize() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			isSmallScreen = getResources().getConfiguration()
 					.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE) ? false
 					: true;
 		} else {
 			isSmallScreen = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < 3;
 		}
 	}
 
 	private void setViews() {
 		setHasOptionsMenu(true);
 
 		mRecordView = getActivity().findViewById(R.id.record_view);
 		mRecordStatusView = getActivity().findViewById(R.id.record_status);
 		mRecordStatusMessageView = (TextView) getActivity().findViewById(
 				R.id.record_status_message);
 
 		mDetailsView = getActivity().findViewById(R.id.record_details);
 		mDetailsHintView = getActivity().findViewById(R.id.record_details_hint);
 		mRecordHeaderView = getActivity()
 				.findViewById(R.id.record_header_block);
 
 	}
 
 	private void restoreParameters() {
 		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
 		mCurrentCourse = prefs
 				.getInt(PreferencesActivity.LAST_COURSE_VIEWED, 0);
 		mCursorPosition = prefs.getInt(PreferencesActivity.LAST_SUBJECT_VIEWED,
 				0);
 	}
 
 	private void setActionBar() {
 		if (isHoneyComb) {
 			ActionBar actionBar = getActivity().getActionBar();
 			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 			actionBar.setSelectedNavigationItem(mCurrentCourse);
 			actionBar.setListNavigationCallbacks(setSpinnerAdapter(),
 					setNavigationCallbacks());
 		}
 	}
 
 	private void loadData() {
 		getActivity().getSupportLoaderManager().initLoader(LOADER_COURSE, null,
 				new CursorLoaderHelper());
 	}
 
 	private void showData() {
 		if (isLandscape && isSmallScreen) {
 			mRecordHeaderView.setVisibility(View.GONE);
 		} else {
 			mRecordHeaderView.setVisibility(View.VISIBLE);
 			showStudentInfo();
 			showCourseInfo();
 		}
 		showRecord();
 		if (isLandscape) {
 			showDetails(mCurrentSubject);
 		}
 	}
 
 	private void showStudentInfo() {
 		TextView studentNameView = (TextView) getActivity().findViewById(
 				R.id.student_name);
 		TextView studentNifView = (TextView) getActivity().findViewById(
 				R.id.student_nif);
 		TextView studentNiaView = (TextView) getActivity().findViewById(
 				R.id.student_nia);
 
 		studentNameView.setText(mStudent.mStudentFullName);
 		studentNifView.setText(getString(R.string.header_nif)
 				+ mStudent.mStudentNif);
 		studentNiaView.setText(getString(R.string.header_nia)
 				+ mStudent.mStudentNia);
 	}
 
 	private void showCourseInfo() {
 		TextView courseNameView = (TextView) getActivity().findViewById(
 				R.id.record_course);
 		TextView courseCenterView = (TextView) getActivity().findViewById(
 				R.id.record_center);
 		TextView courseStudiesView = (TextView) getActivity().findViewById(
 				R.id.record_studies);
 
 		courseNameView.setText(mCurrentRecord.mCourseName);
 		courseCenterView.setText(mCurrentRecord.mCourseCenter);
 		courseStudiesView.setText(mCurrentRecord.mCourseStudies);
 	}
 
 	private void showRecord() {
 		String[] from = { GradesContract.SubjectsTable.COL_SU_NAME,
 				GradesContract.SubjectsTable.COL_SU_CREDITS,
 				GradesContract.GradesTable.COL_GR_CODE };
 		int[] to = { R.id.record_item_name, R.id.record_item_credits,
 				R.id.record_item_grade };
 		SimpleAdapter adapter = new SimpleAdapter(getActivity(),
 				prepareSubjectsList(), R.layout.record_list_item, from, to);
 		setListAdapter(adapter);
 	}
 
 	private ArrayList<HashMap<String, String>> prepareSubjectsList() {
 		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
 		ArrayList<String> subjectsList = new ArrayList<String>();
 		for (int i = 0; i < mCurrentRecord.mSubjectsCount; i++) {
 			Subject subject = mCurrentRecord.mSubjectsList.get(i);
 			if (!subjectsList.contains(subject.mSubjectName)) {
 				subjectsList.add(subject.mSubjectName);
 				HashMap<String, String> item = new HashMap<String, String>();
 				item.put(GradesContract.SubjectsTable.COL_SU_NAME,
 						subject.mSubjectName);
 				item.put(GradesContract.SubjectsTable.COL_SU_CREDITS,
 						getString(R.string.subject_credits)
 								+ subject.mSubjectCredits);
 				item.put(GradesContract.GradesTable.COL_GR_CODE,
 						subject.getLastGrade().mGradeLetter);
 				result.add(item);
 			}
 		}
 		return result;
 	}
 
 	private void setCursorPosition(int position) {
 		mCurrentSubject = mCurrentRecord.mSubjectsList.get(position);
 		mCursorPosition = position;
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putInt(PreferencesActivity.LAST_SUBJECT_VIEWED, position);
 		editor.commit();
 	}
 
 	private void setSpinnerPosition(int position) {
 		mCurrentRecord = mStudent.mRecordList.get(position);
 		mCurrentSubject = null;
 		mCursorPosition = 0;
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putInt(PreferencesActivity.LAST_SUBJECT_VIEWED, 0);
 		editor.putInt(PreferencesActivity.LAST_COURSE_VIEWED, position);
 		editor.commit();
 	}
 
 	private SpinnerAdapter setSpinnerAdapter() {
 		String[] from = { GradesContract.CoursesTable.COL_CO_NAME };
 
 		int[] to = { android.R.id.text1 };
 
 		SpinnerAdapter adapter = new SimpleAdapter(getActivity(),
 				prepareSpinnerData(),
 				android.R.layout.simple_spinner_dropdown_item, from, to);
 		return adapter;
 	}
 
 	private ArrayList<HashMap<String, String>> prepareSpinnerData() {
 		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
 		for (int i = 0; i < mStudent.mRecordCount; i++) {
 			HashMap<String, String> item = new HashMap<String, String>();
 			item.put(GradesContract.CoursesTable.COL_CO_NAME,
 					mStudent.mRecordList.get(i).mCourseName);
 			result.add(item);
 		}
 		return result;
 	}
 
 	private OnNavigationListener setNavigationCallbacks() {
 		OnNavigationListener listener = new OnNavigationListener() {
 
 			@Override
 			public boolean onNavigationItemSelected(int itemPosition,
 					long itemId) {
 				setSpinnerPosition(itemPosition);
 				showData();
 				return true;
 			}
 		};
 		return listener;
 	}
 
 	private void showProgress(final boolean show, int who) {
 		String statusMessage = "";
 		switch (who) {
 		case PROGRESS_LOAD:
 			statusMessage = getString(R.string.loading_data);
 			break;
 		case PROGRESS_PARSE:
 			statusMessage = getString(R.string.dialog_parse);
 			break;
 		case PROGRESS_SIGN_IN:
 			statusMessage = getString(R.string.dialog_connect);
 			break;
 		}
 
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mRecordStatusView.setVisibility(View.VISIBLE);
 			mRecordStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mRecordStatusView.setVisibility(show ? View.VISIBLE
 									: View.GONE);
 						}
 					});
 
 			mRecordView.setVisibility(View.VISIBLE);
 			mRecordView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mRecordView.setVisibility(show ? View.GONE
 									: View.VISIBLE);
 						}
 					});
 		} else {
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			if (show) {
 				mRecordView.setVisibility(View.GONE);
 				mRecordStatusMessageView.setText(statusMessage);
 				mRecordStatusView.setVisibility(View.VISIBLE);
 			} else {
 				mRecordView.setVisibility(View.VISIBLE);
 				mRecordStatusView.setVisibility(View.GONE);
 			}
 		}
 	}
 
 	private class AsyncHelper implements LoaderCallbacks<String> {
 
 		@Override
 		public Loader<String> onCreateLoader(int id, Bundle args) {
 			AsyncTaskLoader<String> loader = null;
 			switch (id) {
 			case LOADER_CONNECTION:
 				showProgress(true, PROGRESS_SIGN_IN);
 				loader = new ConnectLoader(getActivity(), args);
 				break;
 			case LOADER_PARSER:
 				showProgress(true, PROGRESS_PARSE);
 				loader = new GradesParserLoader(getActivity(), args);
 				break;
 			}
 			return loader;
 		}
 
 		@Override
 		public void onLoadFinished(Loader<String> loader, String result) {
 			switch (loader.getId()) {
 			case LOADER_CONNECTION:
 				showProgress(false, PROGRESS_SIGN_IN);
 				Bundle args = new Bundle();
 				args.putString(RecordActivity.EXTRA_DOWNLOADED_DATA, result);
 				getActivity().getSupportLoaderManager().initLoader(
 						LOADER_PARSER, args, this);
 				break;
 			case LOADER_PARSER:
 				loadData();
 				showData();
 				showProgress(false, PROGRESS_PARSE);
 				break;
 			}
 		}
 
 		@Override
 		public void onLoaderReset(Loader<String> loader) {
 			loader.reset();
 		}
 	}
 
 	private class CursorLoaderHelper implements LoaderCallbacks<Cursor> {
 
 		@Override
 		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 			showProgress(true, PROGRESS_LOAD);
 			CursorLoader loader = null;
 			switch (id) {
 			case LOADER_COURSE:
 				Uri uri = GradesContract.CONTENT_NAME_ALL;
 				String selection = GradesContract.CoursesTable.COL_CO_LANGUAGE
 						+ "=?";
 				String[] selectionArgs = { PreferencesActivity
 						.getRecordLanguage(getActivity()) };
 				loader = new CursorLoader(getActivity(), uri, null, selection,
 						selectionArgs, null);
 				break;
 			}
 			return loader;
 		}
 
 		@Override
 		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 			switch (loader.getId()) {
 			case LOADER_COURSE:
 				mStudent = new Student(cursor);
 				setActionBar();
 				showData();
 				break;
 			}
 			showProgress(false, PROGRESS_LOAD);
 		}
 
 		@Override
 		public void onLoaderReset(Loader<Cursor> loader) {
 			loader.reset();
 		}
 	}
 }
