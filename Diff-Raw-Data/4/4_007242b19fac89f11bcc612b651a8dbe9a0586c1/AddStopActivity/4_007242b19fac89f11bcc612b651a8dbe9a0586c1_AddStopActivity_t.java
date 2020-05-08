 package com.karhatsu.suosikkipysakit.ui;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 
 import com.karhatsu.suosikkipysakit.R;
 import com.karhatsu.suosikkipysakit.datasource.LinesRequest;
 import com.karhatsu.suosikkipysakit.datasource.OnHslRequestReady;
 import com.karhatsu.suosikkipysakit.datasource.StopRequest;
 import com.karhatsu.suosikkipysakit.domain.Line;
 import com.karhatsu.suosikkipysakit.domain.Stop;
 
 public class AddStopActivity extends Activity {
 
 	private ProgressDialog progressDialog;
 
 	private StopRequest stopRequest;
 	private StopRequestNotifier stopRequestNotifier = new StopRequestNotifier();
 
 	private LinesRequest linesRequest;
 	private LinesRequestNotifier linesRequestNotifier = new LinesRequestNotifier();
 
 	private SaveStopDialog saveStopDialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_stop);
 		Object retained = getLastNonConfigurationInstance();
 		if (retained instanceof StopRequest) {
 			stopRequest = (StopRequest) retained;
 			stopRequest.setOnHslRequestReady(stopRequestNotifier);
 		} else if (retained instanceof LinesRequest) {
 			linesRequest = (LinesRequest) retained;
 			linesRequest.setOnHslRequestReady(linesRequestNotifier);
 		} else {
 			initializeRequests();
 		}
 	}
 
 	private void initializeRequests() {
 		stopRequest = new StopRequest(stopRequestNotifier);
 		linesRequest = new LinesRequest(linesRequestNotifier);
 	}
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		if (stopRequest != null && stopRequest.isRunning()) {
 			stopRequest.setOnHslRequestReady(null);
 			return stopRequest;
 		} else if (linesRequest != null && linesRequest.isRunning()) {
 			linesRequest.setOnHslRequestReady(null);
 			return linesRequest;
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (saveStopDialog != null) {
 			saveStopDialog.dismiss();
 		}
 	}
 
 	public void searchStop(View button) {
 		if (stopRequest.isRunning()) {
 			return; // prevent double-clicks
 		}
 		String code = getTextFromField(R.id.add_stop_code);
 		if (!Stop.isValidCode(code)) {
 			ToastHelper
 					.showToast(this, R.string.activity_add_stop_invalid_code);
 			return;
 		}
 		showPleaseWait();
 		stopRequest.execute(code);
 	}
 
 	public void searchLine(View button) {
 		if (linesRequest.isRunning()) {
 			return;
 		}
 		String line = getTextFromField(R.id.add_stop_line);
		if (line.trim().equals("")) {
			ToastHelper.showToast(this, R.string.activity_add_stop_empty_line);
			return;
		}
 		showPleaseWait();
 		linesRequest.execute(line);
 	}
 
 	private void showPleaseWait() {
 		progressDialog = new PleaseWaitDialog(this);
 		progressDialog.show();
 	}
 
 	private String getTextFromField(int textFieldId) {
 		return ((EditText) findViewById(textFieldId)).getText().toString();
 	}
 
 	private void hideProgressDialog() {
 		if (progressDialog != null) {
 			progressDialog.dismiss();
 		}
 	}
 
 	private void afterConnectionProblem() {
 		hideProgressDialog();
 		ToastHelper
 				.showToast(AddStopActivity.this, R.string.connection_problem);
 		initializeRequests();
 	}
 
 	private class StopRequestNotifier implements OnHslRequestReady<Stop> {
 		@Override
 		public void notifyAboutResult(Stop stop) {
 			hideProgressDialog();
 			initializeRequests();
 			if (stop != null) {
 				saveStopDialog = new SaveStopDialog(AddStopActivity.this, stop);
 				saveStopDialog.show();
 			} else {
 				ToastHelper.showToast(AddStopActivity.this,
 						R.string.activity_add_stop_stop_not_found);
 			}
 		}
 
 		@Override
 		public void notifyConnectionProblem() {
 			afterConnectionProblem();
 		}
 
 		@Override
 		public Context getContext() {
 			return AddStopActivity.this;
 		}
 	}
 
 	private class LinesRequestNotifier implements
 			OnHslRequestReady<ArrayList<Line>> {
 		@Override
 		public void notifyAboutResult(ArrayList<Line> lines) {
 			hideProgressDialog();
 			initializeRequests();
 			if (lines != null) {
 				Intent intent = new Intent(AddStopActivity.this,
 						LinesActivity.class);
 				intent.putParcelableArrayListExtra(LinesActivity.LINES_LIST,
 						lines);
 				startActivity(intent);
 			} else {
 				ToastHelper.showToast(AddStopActivity.this,
 						R.string.activity_add_stop_line_not_found);
 			}
 		}
 
 		@Override
 		public void notifyConnectionProblem() {
 			afterConnectionProblem();
 		}
 
 		@Override
 		public Context getContext() {
 			return AddStopActivity.this;
 		}
 	}
 }
