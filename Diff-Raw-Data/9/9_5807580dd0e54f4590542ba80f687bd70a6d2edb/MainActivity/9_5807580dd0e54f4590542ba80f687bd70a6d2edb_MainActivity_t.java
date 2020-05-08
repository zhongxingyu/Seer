 package com.figura4;
 
 import java.util.*;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.Spinner;
 
 public class MainActivity extends ListActivity implements OnClickListener, OnItemSelectedListener {
 	private ExpenseLog log;
 	private ExpenseLogFactory logFactory;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         //Gets current month/year
     	Calendar calendar = Calendar.getInstance();
     	int currentMonth = calendar.get(Calendar.MONTH);
     	int currentYear = calendar.get(Calendar.YEAR);
         
     	//Gets Expense log
         logFactory = new SQLiteExpenseLogFactory();
         log = logFactory.createLog(this, currentYear, currentMonth, -1, -1);
         
         //Initializes views
         initList();
         initSpinners(currentMonth, currentYear);
         
         //Sets button click listener
         View newExpenseButton = findViewById(R.id.new_expense_button);
         newExpenseButton.setOnClickListener(this);
     }
     
     /** Initializes listview */
     private void initList() {
    	// initializes list view    
         ListAdapter adapter = new ExpensesAdapter(this, log.getExpenses());
         setListAdapter(adapter);
     }
     
     /** initializes spinners */
     private void initSpinners(int currentMonth, int currentYear) {
    	
         //Initializes months spinner content
         Spinner monthsSpinner = (Spinner) findViewById(R.id.months_spinner);
         ArrayAdapter<CharSequence> monthsArrayAdapter = ArrayAdapter.createFromResource(this, R.array.months_array, android.R.layout.simple_spinner_item);
         monthsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         monthsSpinner.setAdapter(monthsArrayAdapter);
         monthsSpinner.setSelection(currentMonth); //sets selection to current month
         monthsSpinner.setOnItemSelectedListener(this);
         
         //Initializes years spinner content
         Spinner yearsSpinner = (Spinner) findViewById(R.id.years_spinner);
         ArrayAdapter<CharSequence> yearsArrayAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
         //Fills adapter with last 3 years
         for (int i = currentYear-2; i<=currentYear; i++){
         	yearsArrayAdapter.add(Integer.toString(i));
         }
         yearsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         yearsSpinner.setAdapter(yearsArrayAdapter);
         yearsSpinner.setSelection(2); //Sets selection to current year
         yearsSpinner.setOnItemSelectedListener(this);
         
     }
     
     /** Click events handler */
     public void onClick(View v){
     	switch (v.getId()) {
     	case R.id.new_expense_button:
     		Intent i = new Intent(this, NewExpenseActivity.class);
     		startActivity(i);
     		break;
     	}
     }
     
     /** Spinner selection event handler */
     public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
         // An item was selected. You can retrieve the selected item using
         // parent.getItemAtPosition(pos)
     	
     	Spinner monthSpinner = (Spinner) findViewById(R.id.months_spinner);
     	Spinner yearSpinner = (Spinner) findViewById(R.id.years_spinner);
     	int year = 0;
     	int month = 0;
     	
     	switch (view.getId()) {
     	case R.id.months_spinner:
     		year = Integer.parseInt(yearSpinner.getSelectedItem().toString());
     		month = pos;
     		break;
     		
     	case R.id.years_spinner:
     		year = Integer.parseInt(yearSpinner.getAdapter().getItem(pos).toString());
     		month = Integer.parseInt(monthSpinner.getSelectedItem().toString());
     		break;
     	}
     	
     	log = logFactory.createLog(this, month, year, -1, -1);
     }
 
     /** Spinner empty selection event handler */
     public void onNothingSelected(AdapterView<?> parent) {
         // Do nothing
     }
 }
