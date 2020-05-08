 package com.photon.connecttodoor.activity;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.json.JSONException;
 
 import android.annotation.SuppressLint;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.text.Spanned;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 import com.photon.connecttodoor.R;
 import com.photon.connecttodoor.controller.AttendanceListService;
 import com.photon.connecttodoor.datamodel.AttendanceModel;
 import com.photon.connecttodoor.uiadapter.ListGeneratedAttendanceListArrayAdapter;
 import com.photon.connecttodoor.utils.ApplicationConstant;
 
 
 @SuppressLint("NewApi")
 public class AttendanceListActivity extends MainActivity {
 	private EditText startFromDateTxt;
 	private EditText untilFromDateTxt;
 	private ImageView startFromDateImage;
 	private ImageView untilFromDateImage;
 	private Button searchButton;
 	private EditText inputCategory;
 	private Button backButton;
 	private Button signOutButton;
 	private LinearLayout headerList;
 	private boolean isSetStartDateText = true;
 	private ListView attendanceListReport;
 	String responseAttendanceList;
 	String selectCategory;
 	String searchParameters;
 	String searchValues;
 	String categoryName;
 	private String category[] = {"",ApplicationConstant.CAT_DATE,ApplicationConstant.CAT_NAME,ApplicationConstant.CAT_PROJECT_ID,ApplicationConstant.CAT_EMPLOYEE_ID};
 	private static final String USERNAME = "username";
 	private static final String PROJECT_ID = "projectID";
 	private static final String EMPLOYEE_ID = "employeeID";
 	public static final int EMPTY = 0;
 	Spinner spinnerCategory;
 	char[] acceptedNumberOrChar;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_attendancelist);
 		startFromDateTxt = (EditText)findViewById(R.id.startFromDateTxt);
 		untilFromDateTxt = (EditText)findViewById(R.id.untilFromDateTxt);
 		startFromDateImage = (ImageView)findViewById(R.id.startFromDateImage);
 		untilFromDateImage = (ImageView)findViewById(R.id.untilFromDateImage);
 		inputCategory = (EditText)findViewById(R.id.inputCategory);
 		attendanceListReport = (ListView)findViewById(R.id.table_report_attendancelist);
 		searchButton = (Button)findViewById(R.id.searchButton);
 		spinnerCategory = (Spinner)findViewById(R.id.spinnerCategory);
 		backButton = (Button)findViewById(R.id.back_buttonattlist);
 		signOutButton = (Button)findViewById(R.id.signout_buttonattlist);
 		headerList = (LinearLayout)findViewById(R.id.headerList);
 
 		createDropdownCategory(this, category, spinnerCategory);
 		attendanceListReport.setOverScrollMode(View.OVER_SCROLL_NEVER);
 		this.actionButton();
		acceptedNumberOrChar = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 
				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
 				' ','0','1','2','3','4','5','6','7','8','9'};
 	}
 
 	private void actionButton(){
 		/**
 		 * onClick start date
 		 */
 		startFromDateImage.setOnClickListener(new OnClickListener() {
 
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onClick(View v) {
 				isSetStartDateText = true;
 				getCurrentDate();
 				showDialog(DATE_DIALOG_ID);
 			}
 		});
 		/**
 		 * onClick end date
 		 */
 		untilFromDateImage.setOnClickListener(new OnClickListener() {
 
 			@SuppressWarnings("deprecation")
 			@Override
 			public void onClick(View v) {
 				isSetStartDateText = false;
 				getUntilCurrentDate();
 				showDialog(DATE_UNTIL_DIALOG_ID);
 			}
 		});
 		/**
 		 * onClick search employee by category and date
 		 */
 		searchButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				int inputCategoryLength = inputCategory.getText().length();
 
 				/**check internet connection before request for attendance list */
 				if(connectionAvailable()){
 					if(inputCategory.isShown()){
 						if(inputCategoryLength == EMPTY && startFromDateTxt.getText().length() == EMPTY && untilFromDateTxt.getText().length() == EMPTY){
 							alertMessage(ApplicationConstant.ERR_FILL_THE_BLANK,AttendanceListActivity.this);
 
 						}
 						else if(inputCategoryLength == EMPTY  && ((startFromDateTxt.getText().length() == EMPTY && untilFromDateTxt.getText().length() != EMPTY )
 								|| (startFromDateTxt.getText().length() != EMPTY && untilFromDateTxt.getText().length() == EMPTY ))){
 							alertMessage(ApplicationConstant.ERR_FILL_THE_BLANK,AttendanceListActivity.this);
 						}
 						else if(startFromDateTxt.getText().length() == EMPTY && ((inputCategoryLength == EMPTY && untilFromDateTxt.getText().length() != EMPTY)
 								|| (inputCategoryLength != EMPTY && untilFromDateTxt.getText().length() == EMPTY))){
 							alertMessage(ApplicationConstant.ERR_FILL_START_DATE,AttendanceListActivity.this);
 						}
 						else if(untilFromDateTxt.getText().length() == EMPTY && ((inputCategoryLength == EMPTY && startFromDateTxt.getText().length() != EMPTY)
 								|| (inputCategoryLength != EMPTY && startFromDateTxt.getText().length() == EMPTY))){
 							alertMessage(ApplicationConstant.ERR_FILL_UNTIL_DATE,AttendanceListActivity.this);
 						}
 						else if(inputCategoryLength == EMPTY  && startFromDateTxt.getText().length() != EMPTY  && untilFromDateTxt.getText().length() != EMPTY){
 							alertMessage(ApplicationConstant.ERR_INPUT_BLANK,AttendanceListActivity.this);
 						}
 						else if(inputCategoryLength != EMPTY && startFromDateTxt.getText().length() == EMPTY && untilFromDateTxt.getText().length() != EMPTY){
 							alertMessage(ApplicationConstant.ERR_FILL_START_DATE,AttendanceListActivity.this);
 						}
 						else if(inputCategoryLength != EMPTY && startFromDateTxt.getText().length() != EMPTY && untilFromDateTxt.getText().length() == EMPTY ){
 							alertMessage(ApplicationConstant.ERR_FILL_UNTIL_DATE,AttendanceListActivity.this);
 						}
 						else{
 
 							if(inputCategoryLength != EMPTY && startFromDateTxt.getText().length() != EMPTY && untilFromDateTxt.getText().length() != EMPTY){
 								onLoadResult();
 							}
 							else if(inputCategoryLength!= EMPTY){
 								alertMessage(ApplicationConstant.ERR_FILL_THE_BLANK,AttendanceListActivity.this);
 							}
 							else if(startFromDateTxt.getText().length() != EMPTY && untilFromDateTxt.getText().length() != EMPTY){
 								onLoadResult();
 							}
 							else{
 								alertMessage(ApplicationConstant.ERROR,AttendanceListActivity.this);
 							}
 						}
 
 					}else{
 						if(startFromDateTxt.getText().length() == EMPTY || untilFromDateTxt.getText().length() == EMPTY ){
 							alertMessage(ApplicationConstant.ERR_FILL_DATE,AttendanceListActivity.this);
 						}else{
 							if(startFromDateTxt.getText().length() != EMPTY && untilFromDateTxt.getText().length() != EMPTY){
 								onLoadResult();
 							}else{
 								alertMessage(ApplicationConstant.ERROR,AttendanceListActivity.this);
 							}
 						}
 					}
 				}else{
 					alertMessage(ApplicationConstant.NO_INTERNET_CONNECTION, AttendanceListActivity.this);
 				}
 
 			}
 		});
 		/**
 		 * onClick back button
 		 */
 		backButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goToWelcomeScreen();
 			}
 		});
 		/**
 		 * onClick sign out button
 		 */
 		signOutButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				/**check internet connection before sign out application */
 				if(connectionAvailable()){
 					LoginActivity.onClickLogout();
 					goToLogin();
 				}else{
 					alertMessage(ApplicationConstant.NO_INTERNET_CONNECTION, AttendanceListActivity.this);
 				}
 			}
 		});
 		/**
 		 * onClick drop down category
 		 */
 		spinnerCategory.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3){
 				selectCategory = spinnerCategory.getSelectedItem().toString();
 				try {
 					if(selectCategory.equals(ApplicationConstant.CAT_NAME)){
 						setValeuForSelectedName();
 					}else if(selectCategory.equals(ApplicationConstant.CAT_PROJECT_ID)){
 						setValeuForSelectedProjectId();
 					}else if(selectCategory.equals(ApplicationConstant.CAT_EMPLOYEE_ID)){
 						setValeuForSelectedEmployeeId();
 					}else if(selectCategory.equals(ApplicationConstant.CAT_DATE)){
 						setValeuForSelectedDate();
 					}else{
 						searchParameters = "";
 						inputCategory.setVisibility(View.GONE);	
 						setCharacter(inputCategory, acceptedNumberOrChar);
 					}
 				}catch(NumberFormatException nfe) {
 					System.out.println("Could not parse " + nfe);
 				} 
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
 		});
 	}
 	/**
 	 * Load result attendance list
 	 */
 	private void onLoadResult(){
 		boolean isDateValidated = validateDateSection();
 		if(isDateValidated){
 			if( !searchParameters.equals("")){
 				new CallServiceAttendanceListTask().execute();
 				headerList.setVisibility(View.VISIBLE);
 			}else{
 				alertMessage("Please Choose Category ", AttendanceListActivity.this);
 			}
 		}else{
 			alertMessage(ApplicationConstant.ERR_LAST_DATE_LESS_THAN_START_DATE,AttendanceListActivity.this);
 		}
 
 	}
 	/**
 	 * set data and ui for date
 	 */
 	private void setValeuForSelectedDate(){
 		inputCategory.setVisibility(View.GONE);	
 		inputCategory.setText("");
 		searchParameters = selectCategory;
 		inputCategory.getText().toString();
 		setCharacter(inputCategory, acceptedNumberOrChar);
 	}
 	/**
 	 * set data and ui for name
 	 */
 	private void setValeuForSelectedName(){
 		inputCategory.setVisibility(View.VISIBLE);
 		inputCategory.setText("");
 		searchParameters = USERNAME;
 		inputCategory.getText().toString();
 		categoryName = ApplicationConstant.CAT_NAME;
 		char[] acceptedChars = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 
 				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
 		' '};
 		setCharacter(inputCategory,acceptedChars);
 	}
 	/**
 	 * set data and ui for project id
 	 */
 	private void setValeuForSelectedProjectId(){
 		inputCategory.setVisibility(View.VISIBLE);
 		inputCategory.setText("");
 		searchParameters = PROJECT_ID;
 		inputCategory.getText().toString();
 		char[] numberOnly = new char[]{
 				'0','1','2','3','4','5','6','7','8','9'
 		};
 		categoryName = ApplicationConstant.CAT_PROJECT_ID;
 		setCharacter(inputCategory, numberOnly);
 	}
 	/**
 	 * set data and ui for employee id
 	 */
 	private void setValeuForSelectedEmployeeId(){
 		inputCategory.setVisibility(View.VISIBLE);
 		inputCategory.setText("");
 		searchParameters = EMPLOYEE_ID;
 		inputCategory.getText().toString();
 		categoryName = ApplicationConstant.CAT_EMPLOYEE_ID;
 		setCharacter(inputCategory, acceptedNumberOrChar);
 	}
 	private void setCharacter(EditText input,final char[] value){
 		InputFilter[] filters = new InputFilter[1];
 		filters[0] = new InputFilter(){
 			@Override
 			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
 				if (end > start) {					
 					for (int index = start; index < end; index++) {                                         
 						if (!new String(value).contains(String.valueOf(source.charAt(index)))) { 
 
 							return ""; 
 						}    
 					}
 				}
 				return null;
 			}
 
 		};
 		input.setFilters(filters);
 	}
 	/**
 	 * launch attendance menu
 	 */
 	private void goToWelcomeScreen(){
 		Intent intentWelcomeScreen = new Intent(AttendanceListActivity.this, WelcomeScreenActivity.class);
 		startActivity(intentWelcomeScreen);
 		finish();
 	}
 	/**
 	 * launch login page
 	 */
 	private void goToLogin(){
 		Intent intentLoginPage = new Intent(AttendanceListActivity.this, LoginActivity.class);
 		startActivity(intentLoginPage);
 		finish();
 	}
 
 	@Override
 	public void onBackPressed() {
 		Intent intentWelcomeScreen = new Intent(AttendanceListActivity.this, WelcomeScreenActivity.class);
 		startActivity(intentWelcomeScreen);
 		finish();
 	}
 	/**
 	 * Validate if startDate is less than endDate in Date section
 	 * @return isValidated as boolean
 	 */
 	@SuppressLint("SimpleDateFormat")
 	private boolean validateDateSection(){
 		String startDateString = startFromDateTxt.getText().toString().replace(STRIP, SLASH);
 		String endDateString = untilFromDateTxt.getText().toString().replace(STRIP, SLASH);;
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ApplicationConstant.DATE_PATTERN);
 
 		boolean isValidated = false;
 
 		try {
 			Date startDate = simpleDateFormat.parse(startDateString);
 			Date endDate = simpleDateFormat.parse(endDateString);
 			if(startDate.getTime() < endDate.getTime()){
 				isValidated = true;
 			}
 
 			return isValidated;
 
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	/** Callback received when the user "picks" a date in the dialog */
 	private DatePickerDialog.OnDateSetListener pDateSetListener =
 			new DatePickerDialog.OnDateSetListener() {
 
 		@Override
 		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 			pYear = year;
 			pMonth = monthOfYear;
 			pDay = dayOfMonth;
 
 			updateDisplay();
 		}
 	};
 
 	private DatePickerDialog.OnDateSetListener untilDateSetListener =
 			new DatePickerDialog.OnDateSetListener() {
 
 		@Override
 		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 			untilYear = year;
 			untilMonth = monthOfYear;
 			untilDay = dayOfMonth;
 
 			updateDisplay();
 		}
 	};
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this, pDateSetListener, pYear, pMonth, pDay);
 		case DATE_UNTIL_DIALOG_ID:
 			return new DatePickerDialog(this, untilDateSetListener, untilYear, untilMonth, untilDay);
 		}
 
 		return null;
 	}
 
 	/** Updates the date in the TextView */
 	private void updateDisplay() {
 
 		if(isSetStartDateText){
 			startFromDateTxt.setText(getDateEditText());
 		}else{
 			untilFromDateTxt.setText(getUntilDateEditText());
 		}
 	}
 	/**
 	 * request attendance list service
 	 *
 	 */
 	private class CallServiceAttendanceListTask extends AsyncTask<Void, Void, String> {
 
 		private ProgressDialog dialog;
 
 		protected void onPreExecute() {
 			this.dialog = ProgressDialog.show(AttendanceListActivity.this,
 					"List Process", "Please Wait...", true);
 		}
 
 		@Override
 		protected String doInBackground(Void... params) {
 			// TODO Auto-generated method stub
 			String searchingValue = inputCategory.getText().toString();;
 			String searchParameter = searchParameters;
 			String startDateParam = untilFromDateTxt.getText().toString();
 			String endDateParam = startFromDateTxt.getText().toString();
 			AttendanceListService attendanceListService = new AttendanceListService();
 			String response = attendanceListService.handleRequestAttendanceList(searchParameter, searchingValue,startDateParam,endDateParam);
 			return response;
 		}
 
 		protected void onPostExecute(String result) {
 			if(result != null){
 				AttendanceModel attendance = new AttendanceModel(result);
 				try {
 					attendance.parseSource();
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				if(attendance.getAttendanceListModels().size() != 0){
 					ListGeneratedAttendanceListArrayAdapter tableReport = new ListGeneratedAttendanceListArrayAdapter(AttendanceListActivity.this, attendance.getAttendanceListModels());
 					attendanceListReport.setAdapter(tableReport);
 					tableReport.notifyDataSetChanged();
 				}else{
 					alertMessage("Incorrect "+ categoryName, AttendanceListActivity.this);
 				}
 				
 			}
 			this.dialog.dismiss();
 		}
 	}
 
 }
