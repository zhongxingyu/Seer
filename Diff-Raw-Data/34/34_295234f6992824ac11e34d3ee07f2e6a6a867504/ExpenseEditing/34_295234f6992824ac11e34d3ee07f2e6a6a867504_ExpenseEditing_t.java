 package com.dankronstal.dailyexpenses;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.DatePicker.OnDateChangedListener;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.dankronstal.dailyexpenses.data.Expense;
 import com.dankronstal.dailyexpenses.data.ExpenseContentProvider;
 
 public class ExpenseEditing extends Activity {
 	
 	protected TableLayout tblExpenses;
 	protected TextView txtNoExpenses;
 	protected DatePicker datePickerEdit;
 	protected LinearLayout layoutUpdate;
 	protected TextView txtEditId ;
 	protected Spinner spinnerEditType;
 	protected TextView txtEditAmount;
 	protected Button btnSaveEdit;
 			/**
 	 * @see android.app.Activity#onCreate(Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edit);
 		
 		tblExpenses = (TableLayout) findViewById(R.id.tblExpenses);
 		txtNoExpenses = (TextView) findViewById(R.id.txtNoExpenses);
 		datePickerEdit = (DatePicker) findViewById(R.id.datePickerEdit);
 		layoutUpdate = (LinearLayout) findViewById(R.id.layoutUpdate);
 		txtEditId = (TextView)findViewById(R.id.txtEditID);
 		spinnerEditType = (Spinner) findViewById(R.id.spinnerEditType);
 		txtEditAmount = (TextView) findViewById(R.id.txtEditAmount);
 		btnSaveEdit = (Button) findViewById(R.id.btnSaveEdit);		
 		
 		Bundle extras = getIntent().getExtras();		
 		String dateFromMain = extras.getString("dateFromMain");
 		Date dateToEdit = DateHelper.StringToDate(dateFromMain);
 		//TODO: get this date picker to use the date from the previous screen.... for some reason it's faulting.
 		try{
 			datePickerEdit.init(dateToEdit.getYear()+1900, dateToEdit.getMonth(), dateToEdit.getDate(), lDateEditChanged);
 		}catch(Exception e){
 			Log.getStackTraceString(e);
 			datePickerEdit.init(datePickerEdit.getYear(), datePickerEdit.getMonth(), datePickerEdit.getDayOfMonth(), lDateEditChanged);
 		}
 		btnSaveEdit.setOnClickListener(lSaveEditClicked);
 		
 		bindExpenseTable();
 	}
 
 	/**
 	 * 
 	 */
 	private void bindExpenseTable() {
 		tblExpenses.removeAllViews();
 		ArrayList<Expense> expenses = getExpenseList();
 		if(expenses.size() > 0)
 		{
 			txtNoExpenses.setVisibility(View.GONE);
 			//following from http://stackoverflow.com/questions/5183968/how-to-add-rows-dynamically-into-table-layout
 			for(Expense e : expenses){				
 			    try {
 			    	// Inflate row "template" and fill out the fields.
 					TableRow row = (TableRow)LayoutInflater.from(ExpenseEditing.this).inflate(R.layout.edit_row, null);
 					((TextView)row.findViewById(R.id.txtEditID)).setText(Integer.toString(e.getId()));
 					((TextView)row.findViewById(R.id.txtEditExpenseType)).setText(e.getCategory());
 					((TextView)row.findViewById(R.id.txtEditExpenseAmount)).setText(ExpenseContentProvider.MONEY_FORMAT.format(e.getAmount()));//TODO: fix magic string
 					((TextView)row.findViewById(R.id.txtEditCurrency)).setText(e.getCurrency());
 					((TextView)row.findViewById(R.id.txtEditExchange)).setText(Double.toString(e.getExchange()));
 					((TextView)row.findViewById(R.id.txtEditDate)).setText(e.getDateIncurred());
 					row.setOnClickListener(lEditRowClicked);
 					tblExpenses.addView(row);
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 			tblExpenses.requestLayout();     // Not sure if this is needed.
 		}else{
 			txtNoExpenses.setVisibility(View.VISIBLE);
 		}
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	private ArrayList<Expense> getExpenseList() {
 		ArrayList<Expense> expenses = new ArrayList<Expense>();   
         try {
 			String[] projection = new String[]{
 					ExpenseContentProvider.ID,
 					ExpenseContentProvider.AMOUNT,
 					ExpenseContentProvider.CATEGORY,
 					ExpenseContentProvider.CURRENCY,
 					ExpenseContentProvider.DATEINCURRED,
 					ExpenseContentProvider.EXCHANGE}; 
 			String selection = new String(ExpenseContentProvider.DATEINCURRED + " like '" + DateHelper.makeShortDate(datePickerEdit) + "%'");
 			Cursor results = managedQuery(ExpenseContentProvider.CONTENT_URI, projection, selection, null, null);
 			if(results != null && results.getCount() > 0){
 				results.moveToFirst();
 				do{
 					expenses.add(new Expense(
 							results.getInt(0),
 							results.getDouble(1),
 							results.getString(2),
 							results.getString(3),							
 							results.getString(4),
 							results.getDouble(5)));
 				}while(results.moveToNext());
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         return expenses;
 	}
 	
 /**
 	 * bindSpinnerItems:
 	 * Queries the database to find the list of unique ExpenseType values for presentation in the list.
 	 * @param category 
 	 */
 	private void bindExpenseTypeSpinner(CharSequence category) {
 		ArrayList<CharSequence> listOfTypes = new ArrayList<CharSequence>();        
         try {			
 			String[] projection = new String[]{ExpenseContentProvider.CATEGORY};
 			String selection = "1=1) GROUP BY (" + ExpenseContentProvider.CATEGORY; //sloppy sql injection hack to get distinct categories 
 			Cursor results = getContentResolver().query(ExpenseContentProvider.CONTENT_URI, projection, selection, null, null);
 			if(results != null && results.getCount() > 0){
 				results.moveToFirst();
 				do{
 					listOfTypes.add((CharSequence)results.getString(0));
 				}while(results.moveToNext());
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
         ArrayAdapter<CharSequence> typesArrayAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, listOfTypes);
         spinnerEditType.setAdapter(typesArrayAdapter);
         
         int spinnerPosition = typesArrayAdapter.getPosition(category);
         spinnerEditType.setSelection(spinnerPosition);//set the default according to value
 	}
 	
     private OnDateChangedListener lDateEditChanged = new OnDateChangedListener() {
  		@Override
 		public void onDateChanged(DatePicker picker, int year, int month, int dayOfMonth) {
  			bindExpenseTable();
  			layoutUpdate.setVisibility(View.GONE);
 		}
     };
     
     private OnClickListener lEditRowClicked = new OnClickListener() {		
 		@Override
 		public void onClick(View v) {
 			txtEditId = (TextView) v.findViewById(R.id.txtEditID);
 			TextView vAmount = (TextView) v.findViewById(R.id.txtEditExpenseAmount);
 			txtEditAmount.setText(vAmount.getText().toString().replace("$", "").trim());
 			bindExpenseTypeSpinner(((TextView) v.findViewById(R.id.txtEditExpenseType)).getText());
 			layoutUpdate.setVisibility(View.VISIBLE);			
 		}
 	};
 	
 	private OnClickListener lSaveEditClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			ContentValues values = new ContentValues();
 			values.put(ExpenseContentProvider.AMOUNT, Double.parseDouble(txtEditAmount.getText().toString()));
 			values.put(ExpenseContentProvider.CATEGORY, spinnerEditType.getSelectedItem().toString());
 			String where = ExpenseContentProvider.ID + " = ?";
 			String[] args = new String[]{txtEditId.getText().toString()};			
 			try{
 				if(Double.parseDouble(values.get(ExpenseContentProvider.AMOUNT).toString()) > 0)
 					getContentResolver().update(ExpenseContentProvider.CONTENT_URI, values, where, args);
 				else
 					getContentResolver().delete(ExpenseContentProvider.CONTENT_URI, where, args);				
 			}catch(Exception e){
 				Log.getStackTraceString(e);
 			}			
 			layoutUpdate.setVisibility(View.GONE);
 			bindExpenseTable();
			
 		}
 	};
 }
