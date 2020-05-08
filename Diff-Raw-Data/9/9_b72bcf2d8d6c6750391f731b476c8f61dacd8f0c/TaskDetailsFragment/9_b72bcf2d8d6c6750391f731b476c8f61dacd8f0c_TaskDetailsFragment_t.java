 package com.abbyy.cloudocr.fragments;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.abbyy.cloudocr.R;
 import com.abbyy.cloudocr.Task;
 import com.abbyy.cloudocr.TaskDetailsActivity;
 import com.abbyy.cloudocr.TasksManagerService;
 import com.abbyy.cloudocr.database.TasksContract;
 
 /**
  * This fragment shows the information with the download to the user. When the
  * information about the file is present and the task is finished, it allows for
  * download of the result.
  * 
  * @author Denis Lapuente
  * 
  */
 public class TaskDetailsFragment extends Fragment implements
 		LoaderCallbacks<Cursor> {
 	// Loader code for the task information. Useful if the service automatically
 	// updates the information
 	private static final int LOADER_TASK_INFO = 0;
 
 	private String mTaskId;
 	private Task mTask;
 
 	private Button mDownloadButton;
 
 	/**
 	 * Public constructor required for creation of a fragment through the
 	 * fragment manager
 	 */
 	public TaskDetailsFragment() {
 	}
 
 	/**
 	 * Called when the view for the fragment is first created. If the container
 	 * is null, it is being created outside the available views to the user so
 	 * we don't need to do anything else.
 	 * 
 	 * Returns the view of the fragment.
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		if (container == null) {
 			return null;
 		}
 		return inflater.inflate(R.layout.task_details_fragment, container,
 				false);
 	}
 
 	/**
 	 * Called after onCreateView. Performs the initialization of the fragment.
 	 */
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		loadTaskData();
 	}
 
 	/**
 	 * Convenience method for loading the data of the task. If the ID is not we
 	 * first set it. Then get the information from the database.
 	 */
 	private void loadTaskData() {
 		if (mTaskId == null) {
 			mTaskId = getArguments().getString(
 					TaskDetailsActivity.EXTRA_TASK_ID);
 		}
 		getActivity().getSupportLoaderManager().restartLoader(LOADER_TASK_INFO,
 				null, this);
 	}
 
 	// Implementation of the cursor callbacks for the loader call
 
 	/**
 	 * Initialization of the cursor that will provide the information to the
 	 * user.
 	 */
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		Loader<Cursor> loader = null;
 		Uri uri = TasksContract.CONTENT_TASKS;
 		String selection = TasksContract.TasksTable.TASK_ID + "=?";
 		String[] selectionArgs = { mTaskId };
 		switch (id) {
 		case LOADER_TASK_INFO:
 			loader = new CursorLoader(getActivity(), uri, null, selection,
 					selectionArgs, null);
 			break;
 		}
 		return loader;
 	}
 
 	/**
 	 * When the loading is over, we populate a task with the cursor and then
 	 * show the information on screen.
 	 */
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		mTask = new Task(cursor);
 		populateScreen();
 	}
 
 	/**
 	 * Called when the cursor is disposed. As nothing here is actively using the
 	 * cursor we can just omit this.
 	 */
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 	}
 
 	/**
 	 * Convenience method for populating the screen.
 	 */
 	private void populateScreen() {
 		mDownloadButton = (Button) getActivity().findViewById(
 				R.id.task_details_download);
 		// When this is null, the view is not being shown and thus we don't need
 		// any further processing.
 		if (mDownloadButton == null) {
 			return;
 		}
 		TextView taskIdView = (TextView) getActivity().findViewById(
 				R.id.task_details_task_id);
 		TextView descriptionView = (TextView) getActivity().findViewById(
 				R.id.task_details_description);
 		TextView statusView = (TextView) getActivity().findViewById(
 				R.id.task_details_status);
 		TextView registrationTimeView = (TextView) getActivity().findViewById(
 				R.id.task_details_registration_time);
 		TextView statusChangeTimeView = (TextView) getActivity().findViewById(
 				R.id.task_details_status_change_time);
 		TextView filesCountView = (TextView) getActivity().findViewById(
 				R.id.task_details_files_count);
 		TextView creditsView = (TextView) getActivity().findViewById(
 				R.id.task_details_credits);
 		TextView estimatedProcessingTimeView = (TextView) getActivity()
 				.findViewById(R.id.task_details_estimated_processing_time);
 		TextView errorMessageView = (TextView) getActivity().findViewById(
 				R.id.task_details_error);
 
 		taskIdView.setText(getActivity().getString(R.string.details_task_id)
 				+ mTaskId);
 		descriptionView.setText(getActivity().getString(
 				R.string.details_description)
				+ ((mTask.mDescription == null) ? "-" : mTask.mDescription));
 		statusView.setText(getActivity().getString(R.string.details_status)
 				+ mTask.mStatus);
 		registrationTimeView.setText(getActivity().getString(
 				R.string.details_registration_time)
 				+ mTask.mRegistrationDate);
 		filesCountView.setText(getActivity().getString(
 				R.string.details_files_count)
 				+ mTask.mFilesCount);
 		creditsView.setText(getActivity().getString(R.string.details_credits)
 				+ mTask.mCredits);
 		estimatedProcessingTimeView.setText(getActivity().getString(
 				R.string.details_estimated_processing_time)
 				+ mTask.mEstimatedProcessingTime);
 		statusChangeTimeView.setText(getActivity().getString(
 				R.string.details_status_change_time)
 				+ mTask.mStatusChangeDate);
 
 		// If the error message is not empty we show it. If it is empty we make
 		// it disappear
 		if (!TextUtils.isEmpty(mTask.mError)) {
 			errorMessageView.setVisibility(View.VISIBLE);
 			errorMessageView.setText(getActivity().getString(
 					R.string.details_error)
 					+ mTask.mError);
 		} else {
 			errorMessageView.setVisibility(View.GONE);
 		}
 
 		// If we do have a result URL and it was uploaded from the device, it
 		// allows for download. If not, we hide the button.
 		if (!TextUtils.isEmpty(mTask.mResultUrl)) {
 
 			mDownloadButton.setVisibility(View.VISIBLE);
 			mDownloadButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					makeDownload();
 				}
 			});
 		} else {
 			mDownloadButton.setVisibility(View.GONE);
 		}
 
 	}
 
 	/**
 	 * Convenience method for downloading the file. It just calls the service
 	 * with the correct parameters
 	 */
 	private void makeDownload() {
 		Intent service = new Intent(getActivity(), TasksManagerService.class);
 		service.putExtra(TasksManagerService.EXTRA_ACTION,
 				TasksManagerService.ACTION_DOWNLOAD_RESULT);
 		service.putExtra(TasksManagerService.EXTRA_TASK_ID, mTask.mTaskId);
 		service.putExtra(TasksManagerService.EXTRA_URL, mTask.mResultUrl);
 		getActivity().startService(service);
 	}
 }
