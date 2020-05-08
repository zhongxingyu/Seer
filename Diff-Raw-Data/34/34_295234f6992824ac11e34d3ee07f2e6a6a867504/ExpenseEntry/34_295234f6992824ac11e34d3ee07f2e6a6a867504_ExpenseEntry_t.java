 package com.dankronstal.dailyexpenses;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.DatePicker.OnDateChangedListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.dankronstal.dailyexpenses.R;
 import com.dankronstal.dailyexpenses.data.ExpenseContentProvider;
 
 public class ExpenseEntry extends Activity {
 	
 	private ExpenseContentProvider expenseData;
 	protected DatePicker dateToday;
 	protected LinearLayout layoutNewType;
 	protected LinearLayout layoutExpenseType;
 	protected Spinner spinnerExpenseType;
 	protected EditText txtAmount;
 	protected EditText txtAddType;
 	protected TextView lblTodaysSpendValue;	
 	
     /**
      *  Called when the activity is first created. 
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         expenseData = new ExpenseContentProvider();
         
         //wire up interface
         dateToday = (DatePicker) findViewById(R.id.dateToday);
         spinnerExpenseType = (Spinner) findViewById(R.id.spinnerExpenseType);
         layoutNewType = (LinearLayout) findViewById(R.id.layoutNewType);
         layoutExpenseType = (LinearLayout) findViewById(R.id.layoutExpenseType);
         txtAmount = (EditText) findViewById(R.id.txtAmount);
         txtAddType = (EditText) findViewById(R.id.txtAddType);
         lblTodaysSpendValue = (TextView) findViewById(R.id.lblTodaysSpendValue);
         
         Button btnAddType = (Button) findViewById(R.id.btnAddType);
         Button btnSaveNewType = (Button) findViewById(R.id.btnSaveNewType);
         Button btnSave = (Button)findViewById(R.id.btnSave);
         
         btnAddType.setOnClickListener(lAddTypeClicked);
         btnSaveNewType.setOnClickListener(lSaveNewTypeClicked);   
         btnSave.setOnClickListener(lSaveClicked);
         
         
         dateToday.init(dateToday.getYear(), dateToday.getMonth(), dateToday.getDayOfMonth(), lDateTodayChanged);
         
         //set up spinner        
         bindExpenseTypeSpinner(); 
 		
 		//set up todays spend
 		bindTodaysSpend();
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         bindTodaysSpend();
     }
 
 	/**
      * Set up the menu items
      */
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuItem mi = menu.add(R.string.lbl_menu_edit);
 		mi.setIcon(android.R.drawable.ic_menu_agenda);
 		mi.setOnMenuItemClickListener(lMenuEditClicked);
 		
 		mi = menu.add(R.string.lbl_menu_report);
 		mi.setIcon(android.R.drawable.ic_menu_report_image);
 		mi.setOnMenuItemClickListener(lMenuReportsClicked);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/**
 	 * bindSpinnerItems:
 	 * Queries the database to find the list of unique ExpenseType values for presentation in the list.
 	 */
 	private void bindExpenseTypeSpinner() {
 		ArrayList<CharSequence> listOfTypes = new ArrayList<CharSequence>();        
         try {			
 			String[] projection = new String[]{ExpenseContentProvider.CATEGORY};
 			String selection = "1=1) GROUP BY (" + ExpenseContentProvider.CATEGORY; //sloppy sql injection hack to get distinct categories 
 			Cursor c = getContentResolver().query(ExpenseContentProvider.CONTENT_URI, projection, selection, null, null);
 			if(c != null && c.getCount() > 0){
 				c.moveToFirst();
 				do{
 					listOfTypes.add((CharSequence)c.getString(0));
 				}while(c.moveToNext());
 			}
 			c.close();
 		} catch (Exception e) {
 			Log.getStackTraceString(e);
 		}
         ArrayAdapter<CharSequence> typesArrayAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, listOfTypes);
         spinnerExpenseType.setAdapter(typesArrayAdapter);
 	}
 	
 	private void bindTodaysSpend(){
 		double spend = expenseData.getTodaysSpend(getApplicationContext(), getSelectedDate());
 		lblTodaysSpendValue.setText(ExpenseContentProvider.MONEY_FORMAT.format(spend));
 	}
 	
 	protected void bindTodaysSpend(int year, int month, int dayOfMonth) {
 		double spend = expenseData.getTodaysSpend(getApplicationContext(), 
 				DateHelper.makeDateString(year, month, dayOfMonth));
 		lblTodaysSpendValue.setText(ExpenseContentProvider.MONEY_FORMAT.format(spend));
 	}
 	
 	private String getSelectedDate() {
 		return DateHelper.makeDateString(dateToday.getYear(), dateToday.getMonth() + 1, dateToday.getDayOfMonth());
 	}
 
 	/**
 	 * resetInterface:
 	 * set all UI elements to some default state, and refresh the refreshables.
 	 */
 	private void resetInterface(){
 		layoutNewType.setVisibility(View.GONE);
 		layoutExpenseType.setVisibility(View.VISIBLE);
 		txtAmount.setText(new String());
 		txtAddType.setText(new String());
 		bindTodaysSpend();
 		bindExpenseTypeSpinner();
 	}
     
     private OnClickListener lAddTypeClicked = new OnClickListener() {
         public void onClick(View v) {
 			layoutNewType.setVisibility(View.VISIBLE);
 			layoutExpenseType.setVisibility(View.GONE);
         }
     };
     
     private OnClickListener lSaveNewTypeClicked = new OnClickListener() {
         public void onClick(View v) {
         	layoutNewType.setVisibility(View.GONE);
 			layoutExpenseType.setVisibility(View.VISIBLE);
     	}
     };
     
     private OnDateChangedListener lDateTodayChanged = new OnDateChangedListener() {
  		@Override
 		public void onDateChanged(DatePicker picker, int year, int month, int dayOfMonth) {
  			bindTodaysSpend(year, month + 1, dayOfMonth);
 		}
     };
     
     private OnClickListener lSaveClicked = new OnClickListener() {
         public void onClick(View v) { 
         	//TODO: validate that inputs are appropriate
         	ContentValues initialValues = new ContentValues();
         	initialValues.put(ExpenseContentProvider.DATEINCURRED, DateHelper.makeDateString(dateToday.getYear(), dateToday.getMonth()+1, dateToday.getDayOfMonth()));
         	initialValues.put(ExpenseContentProvider.EXCHANGE, 1.00);
         	initialValues.put(ExpenseContentProvider.CURRENCY, "CDN");
         	if(layoutExpenseType.getVisibility() == View.VISIBLE)
         		initialValues.put(ExpenseContentProvider.CATEGORY, spinnerExpenseType.getSelectedItem().toString());
         	else
         		initialValues.put(ExpenseContentProvider.CATEGORY, txtAddType.getText().toString());
         	initialValues.put(ExpenseContentProvider.AMOUNT, txtAmount.getText().toString());
         	getContentResolver().insert(ExpenseContentProvider.CONTENT_URI, initialValues);        	
         	resetInterface();        	
     	}
     };
     
     private OnMenuItemClickListener lMenuEditClicked = new OnMenuItemClickListener(){
 		@Override
 		public boolean onMenuItemClick(MenuItem item) {
 			Intent myIntent = new Intent();
 			myIntent.setClassName("com.dankronstal.dailyexpenses", "com.dankronstal.dailyexpenses.ExpenseEditing");
 			myIntent.putExtra("dateFromMain", getSelectedDate()); // key/value pair, where key needs current package prefix.
 			startActivity(myIntent); 
 			return false;
 		}    	
     };
     
     private OnMenuItemClickListener lMenuReportsClicked = new OnMenuItemClickListener(){
 		@Override
 		public boolean onMenuItemClick(MenuItem item) {
 			Intent myIntent = new Intent();
 			myIntent.setClassName("com.dankronstal.dailyexpenses", "com.dankronstal.dailyexpenses.ExpenseReporting");
 			startActivity(myIntent); 
 			return false;
 		}    	
     };
 
 }
