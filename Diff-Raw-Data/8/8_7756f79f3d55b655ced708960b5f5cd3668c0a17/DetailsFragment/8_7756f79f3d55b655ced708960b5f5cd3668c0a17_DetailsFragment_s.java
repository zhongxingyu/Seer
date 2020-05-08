 package com.suicune.notasupna;
 
 import java.sql.Date;
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.text.format.DateFormat;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ShareActionProvider;
 import android.widget.TextView;
 
 @SuppressLint("NewApi")
 public class DetailsFragment extends Fragment {
 	private final static String ARGUMENT_SHOWN_INDEX = "shown index";
 
 	private final static String DIALOG_CALLS = "dialog calls";
 
 	private CallsDialogFragment mCallsFragment;
 
 	private Subject mSubject = null;
 	private Grade mGrade = null;
 
 	private boolean showCallsAsDialog;
 	
 	ShareActionProvider mShareActionProvider;
 
 	public static DetailsFragment newInstance(int position) {
 		DetailsFragment fragment = new DetailsFragment();
 		Bundle args = new Bundle();
 		args.putLong(ARGUMENT_SHOWN_INDEX, position);
 		fragment.setArguments(args);
 		return fragment;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.details_fragment, container, false);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		showCallsAsDialog = shouldShowAsDialog();
 		boolean isRecord = isRecordActivity();
 		if (isRecord && isPortrait()) {
 			setHasOptionsMenu(false);
 			return;
 		} else {
 			if (mSubject != null) {
 				showSubjectInformation();
 				showGradeInformation();
 				prepareCallsList();
 			}
 		}
 		if(mShareActionProvider != null){
 			mShareActionProvider.setShareIntent(getShareIntent());
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.action_bar_details, menu);
 		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
 			mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_details_share).getActionProvider();
 			if(mSubject != null){
 				mShareActionProvider.setShareIntent(getShareIntent());
 			}
 		}
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_details_calls:
 			showCallsList();
 			break;
 		case R.id.action_details_share:
 			Intent intent = getShareIntent();
 			startActivity(intent);
 			break;
 		case android.R.id.home:
 			getActivity().finish();
 			break;
 		default:
 			return false;
 		}
 		return true;
 	}
 	
 	private Intent getShareIntent() {
 		Intent intent = new Intent(Intent.ACTION_SEND);
 		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_grade_title));
 		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_grade, mSubject.mSubjectName, mSubject.getLastGrade().mGradeNumber,mSubject.getLastGrade().mGradeName));
 		intent.setType("text/plain");
 		return intent;
 	}
 
 	public void setSubject(Subject subject) {
 		setHasOptionsMenu(true);
 
 		mSubject = subject;
 		mGrade = mSubject.mGradesList.get(mSubject.mGradesList.size() - 1);
 		mCallsFragment = new CallsDialogFragment();
 		mCallsFragment.setSubject(mSubject);
 		
 	}
 
 	public long getShownIndex() {
 		return getArguments().getLong(ARGUMENT_SHOWN_INDEX);
 	}
 
 	private void showSubjectInformation() {
 		TextView subjectNameView = (TextView) getActivity().findViewById(
 				R.id.details_subject_name);
 		TextView subjectTypeView = (TextView) getActivity().findViewById(
 				R.id.details_subject_type);
 		TextView subjectCreditsView = (TextView) getActivity().findViewById(
 				R.id.details_subject_credits);
 		TextView subjectCallsView = (TextView) getActivity().findViewById(
 				R.id.details_subject_calls);
 
 		subjectNameView.setText(mSubject.mSubjectName);
 		subjectTypeView.setText(mSubject.mSubjectType);
 		subjectCreditsView.setText(getString(R.string.subject_credits)
 				+ mSubject.mSubjectCredits);
 		subjectCallsView.setText(getString(R.string.subject_total_calls)
 				+ mSubject.mGradesCount);
 	}
 
 	private void showGradeInformation() {
 		TextView gradeView = (TextView) getActivity().findViewById(
 				R.id.details_grade_number);
 		TextView gradeNameView = (TextView) getActivity().findViewById(
 				R.id.details_grade_name);
 		TextView gradeTimeView = (TextView) getActivity().findViewById(
 				R.id.details_grade_time);
 		TextView gradeRevisionTimeView = (TextView) getActivity().findViewById(
 				R.id.details_grade_revision_time);
 		TextView gradeYearView = (TextView) getActivity().findViewById(
 				R.id.details_grade_year);
 		TextView gradeProvisionalView = (TextView) getActivity().findViewById(
 				R.id.details_grade_provisional);
 		TextView gradeCallView = (TextView) getActivity().findViewById(
 				R.id.details_grade_call);
 		TextView gradeCallNumberView = (TextView) getActivity().findViewById(
 				R.id.details_grade_call_number);
 		TextView gradePassedView = (TextView) getActivity().findViewById(
 				R.id.details_grade_passed);
 		TextView gradeTakenView = (TextView) getActivity().findViewById(
 				R.id.details_grade_taken);
 		TextView gradeCallsTakenView = (TextView) getActivity().findViewById(
 				R.id.details_grade_calls_taken);
 
 		String gradeTime = DateFormat.getDateFormat(getActivity()).format(
 				new Date(mGrade.mGradeTime));
 		String gradeRevisionTime = DateFormat.getDateFormat(getActivity())
 				.format(new Date(mGrade.mGradeRevisionTime));
 
 		gradeView.setText("" + mGrade.mGradeNumber);
 		gradeNameView.setText("" + mGrade.mGradeName);
 		gradeTimeView.setText("" + gradeTime);
 		if (gradeRevisionTime.contains("1970")
 				|| gradeRevisionTime.contains("false")) {
 			gradeRevisionTimeView.setText(getString(R.string.no_revision_time));
 		} else {
 			gradeRevisionTimeView.setText("" + gradeRevisionTime);
 		}
 		gradeYearView.setText("" + mGrade.mGradeYear);
 		if (mGrade.mGradeProvisional == null) {
 			gradeProvisionalView.setText(R.string.no);
 		} else {
 			gradeProvisionalView.setText("" + mGrade.mGradeProvisional);
 		}
 		gradeCallView.setText("" + mGrade.mGradeCall);
 		gradeCallNumberView.setText("" + mGrade.mGradeCallNumber);
 		if (mGrade.mGradePassed.equalsIgnoreCase("false")) {
 			gradePassedView.setText(R.string.no);
 		} else {
			gradePassedView.setText("" + mGrade.mGradePassed);
 		}
 		if (mGrade.mGradeTaken.equalsIgnoreCase("false")) {
			gradePassedView.setText(R.string.no);
 		} else {
			gradeTakenView.setText("" + mGrade.mGradeTaken);
 
 		}
 
 		gradeCallsTakenView.setText("" + mSubject.mCallsTakenCount);
 	}
 
 	private void prepareCallsList() {
 		Button showOtherCallsButtonView = (Button) getActivity().findViewById(
 				R.id.details_show_calls);
 
 		if (showCallsAsDialog) {
 			showOtherCallsButtonView.setVisibility(View.VISIBLE);
 			showOtherCallsButtonView.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					showCallsList();
 				}
 			});
 
 		} else {
 			showOtherCallsButtonView.setVisibility(View.GONE);
 			showCallsList();
 		}
 	}
 
 	private void showCallsList() {
 		FragmentManager fragmentManager = getActivity()
 				.getSupportFragmentManager();
 		if (showCallsAsDialog) {
 			mCallsFragment.show(fragmentManager, DIALOG_CALLS);
 		} else {
 			FragmentTransaction transaction = fragmentManager
 					.beginTransaction();
 			transaction
 					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
 			transaction.replace(R.id.record_calls, mCallsFragment)
 					.addToBackStack(null).commit();
 		}
 	}
 
 	@SuppressLint("NewApi")
 	private boolean shouldShowAsDialog() {
 		boolean result;
 
 		if (isPortrait()) {
 			result = true;
 		} else {
 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 				result = getResources().getConfiguration().isLayoutSizeAtLeast(
 						Configuration.SCREENLAYOUT_SIZE_LARGE) ? false : true;
 			} else {
 				result = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE;
 			}
 		}
 		return result;
 	}
 
 	private boolean isPortrait() {
 		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
 	}
 
 	private boolean isRecordActivity() {
 		String result = getActivity().getLocalClassName();
 		return result.contains("RecordActivity");
 	}
 }
