 package com.photon.connecttodoor.activity;
 
 import org.json.JSONException;
 
 import android.annotation.SuppressLint;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 import com.photon.connecttodoor.R;
 import com.photon.connecttodoor.controller.ReportAttendanceService;
 import com.photon.connecttodoor.datamodel.ReportAttendanceModel;
 import com.photon.connecttodoor.uiadapter.ListGeneratedReportArrayAdapter;
 import com.photon.connecttodoor.utils.ApplicationConstant;
 
 public class ReportActivity extends MainActivity {
 
 	private String category[] = {"","Before Adjustment", "Adjustment", "After Adjustment" };
 	Spinner dropDownCategory;
 	String selectCategory;
	private String attendanceStatus = "before";
 	private ListView attendanceAdminReport;
 	private Button signOutButton ;
 	private Button backButton, searchButton ;
 	private EditText startFromDateTxt;
 	private ImageView startFromDateImage;
 	private static final String BEFORE = "before";
 	private static final String ADJUSTMENT = "adjustment";
 	private static final String AFTER = "after";
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_report);
 		signOutButton = (Button)findViewById(R.id.signout_button);
 		backButton = (Button)findViewById(R.id.back_button);
 		startFromDateImage = (ImageView)findViewById(R.id.dateReportId);
 		startFromDateTxt = (EditText)findViewById(R.id.inputDateReport);
 		searchButton = (Button)findViewById(R.id.search_button);
 		attendanceAdminReport = (ListView) findViewById(R.id.table_report_attendance);	
 		dropDownCategory = (Spinner)findViewById(R.id.spinnerCategory);
 
 		actionButton();
 		createDropdownCategory(this, category, dropDownCategory);
 		attendanceAdminReport.setOverScrollMode(View.OVER_SCROLL_NEVER);
 	}
 
 	private void actionButton(){
 		dropDownCategory.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
 				selectCategory = dropDownCategory.getSelectedItem().toString();
 				try {
 					if(selectCategory.equalsIgnoreCase("Before Adjustment")){
 						attendanceStatus = BEFORE;
 					}else if(selectCategory.equalsIgnoreCase("Adjustment")){
 						attendanceStatus = ADJUSTMENT;
 					}else if(selectCategory.equalsIgnoreCase("After Adjustment")){
 						attendanceStatus = AFTER;
 					}
 				}catch(NumberFormatException nfe) {
 					System.out.println("Could not parse " + nfe);
 				} 
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 		});
 
 		signOutButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				/**check internet connection before sign out application */
 				if(connectionAvailable()){
 					LoginActivity.onClickLogout();
 					goToLoginPage();
 				}else{
 					alertMessage(ApplicationConstant.NO_INTERNET_CONNECTION, ReportActivity.this);
 				}
 
 			}
 		});
 
 		backButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goToAttendancePage();
 
 			}
 		});
 
 		startFromDateImage.setOnClickListener(new OnClickListener() {
 
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onClick(View v) {
 				getCurrentDate();
 				showDialog(DATE_DIALOG_ID);
 			}
 		});
 
 		searchButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String date = startFromDateTxt.getText().toString();
 				String empty = "";
				if(date.equalsIgnoreCase(empty)){
					alertMessage("Please Input Date", ReportActivity.this);
 				}else{
 					/**check internet connection before call report service */
 					if(connectionAvailable()){
 						new CallServiceAttendanceReportTask().execute();
 					}else{
 						alertMessage(ApplicationConstant.NO_INTERNET_CONNECTION, ReportActivity.this);
 					}
 				}
 			}
 		});
 	}
 
 	/** Callback received when the user "picks" a date in the dialog */
 	private DatePickerDialog.OnDateSetListener pDateSetListener =
 			new DatePickerDialog.OnDateSetListener() {
 
 		@Override
 		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 			pYear = year;
 			pMonth = monthOfYear;
 			pDay = dayOfMonth;
 			startFromDateTxt.setText(getDateEditText());
 		}
 	};
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this,
 					pDateSetListener,
 					pYear, pMonth, pDay);
 		}
 
 		return null;
 	}
 
 	private class CallServiceAttendanceReportTask extends AsyncTask<Void, Void, String> {
 
 		private ProgressDialog dialog;
 
 		protected void onPreExecute() {
 			this.dialog = ProgressDialog.show(ReportActivity.this,
 					"List Process", "Please Wait...", true);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			String startDateParam = changeFormatDate(startFromDateTxt.getText().toString());
 			ReportAttendanceService reportAttendanceService = new ReportAttendanceService();
 			String response = reportAttendanceService.handleRequestReportAttendance(attendanceStatus,startDateParam);
 			return response;
 		}
 
 		protected void onPostExecute(String result) {
 			if(result != null){
 				ReportAttendanceModel reportModel = new ReportAttendanceModel(result);
 				try {
 					reportModel.parseSource();
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				ListGeneratedReportArrayAdapter tableReport = new ListGeneratedReportArrayAdapter(ReportActivity.this,reportModel.getReportAttendanceList());
 				attendanceAdminReport.setAdapter(tableReport);
 				tableReport.notifyDataSetChanged();
 			}
 			this.dialog.dismiss();
 		}
 	}
 
 	private void goToAttendancePage(){
 		Intent attendancePage = new Intent(ReportActivity.this,WelcomeScreenActivity.class);
 		startActivity(attendancePage);
 		finish();
 	}
 
 	private void goToLoginPage(){
 		Intent loginPage = new Intent(ReportActivity.this,LoginActivity.class);
 		startActivity(loginPage);
 		finish();
 	}
 	@Override
 	public void onBackPressed() {
 		Intent attendancePage = new Intent(ReportActivity.this,WelcomeScreenActivity.class);
 		startActivity(attendancePage);
 		finish();
 	}
 
 }
