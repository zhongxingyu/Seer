 package com.dankronstal.dailyexpenses;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.ViewManager;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.dankronstal.dailyexpenses.data.Expense;
 import com.dankronstal.dailyexpenses.data.ExpenseContentProvider;
 import com.jjoe64.graphview.BarGraphView;
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.jjoe64.graphview.GraphView.GraphViewSeries;
 
 public class ExpenseReporting extends Activity {
 	
 	private static String ASCENDING = " ASC ";
 	private static String DESCENDING = " DESC ";
 		
 	private String currentlySortedBy = ExpenseContentProvider.DATEINCURRED;
 	private AlertDialog.Builder dialogBuilder;
 	
 	protected String dateSpanFrom = new String();
 	protected String dateSpanTo = new String();
 	
 	protected TextView txtFirstDate;
 	protected TextView txtLastDate;
 	protected TextView txtAvgSpend;
 	protected LinearLayout layoutReport;
 	protected TableLayout tblExpenses;
 	protected MenuItem miReportScope;
 	protected View viewChooseSpan;
 	protected Dialog dialogChooseSpan; 
 	
 	protected ExpenseContentProvider ecp = new ExpenseContentProvider();
 	
 	/**
 	 * @see android.app.Activity#onCreate(Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.report);
 		
 		tblExpenses = (TableLayout) findViewById(R.id.tblExpenses);
 		tblExpenses.removeAllViews();
 		ArrayList<Expense> expenses = getAllExpenses(currentlySortedBy, null, null);
 		bindReportTable(expenses);
 			
 		inflateDialogChooseSpan();
 				
 		dialogBuilder = new AlertDialog.Builder(this);
 		dialogBuilder.setTitle(R.string.lbl_report_choose_span);
		dialogBuilder.setIcon(android.R.drawable.ic_menu_mylocation);
 		dialogBuilder.setView(viewChooseSpan);
 		
 		try{
 			graphViewTest(expenses);
 		}catch(Exception e){
 			Log.getStackTraceString(e);
 		}	
 		
 	}
 
 	/**
 	 * @return
 	 */
 	private void inflateDialogChooseSpan() {
 		LayoutInflater inflater = LayoutInflater.from(this);
 		viewChooseSpan = inflater.inflate(R.layout.report_choose_span, null);
 		
 		EditText from = (EditText)viewChooseSpan.findViewById(R.id.txtReportSpanFrom);
 		from.setInputType(EditorInfo.TYPE_NULL);
 		from.setOnClickListener(lSpanFromClicked);
 		Button fromSave = (Button)viewChooseSpan.findViewById(R.id.btnSpanSaveFrom);
 		fromSave.setOnClickListener(lSpanFromSaveClicked);
 		
 		EditText to = (EditText)viewChooseSpan.findViewById(R.id.txtReportSpanTo);
 		to.setInputType(EditorInfo.TYPE_NULL);
 		to.setOnClickListener(lSpanToClicked);
 		Button toSave = (Button)viewChooseSpan.findViewById(R.id.btnSpanSaveTo);
 		toSave.setOnClickListener(lSpanToSaveClicked);
 		
 		Button applyFilter = (Button)viewChooseSpan.findViewById(R.id.btnReportFilter);
 		applyFilter.setOnClickListener(lUseDateSpanFilterClicked);
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.report_menu, menu);
 	    
 	    MenuItem mi = menu.findItem(R.id.miSpanOf);
 		mi.setIcon(android.R.drawable.ic_menu_view);
 		mi.setOnMenuItemClickListener(lReportBySpanClicked);
 		
 		mi = menu.findItem(R.id.miDayOf);
 		mi.setOnMenuItemClickListener(lReportByDayClicked);
 		
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	/**
 	 * http://www.jjoe64.com/p/graphview-library.html
 	 * @param expenses
 	 */
 	private void graphViewTest(ArrayList<Expense> expenses) {
 		int[] colors = new int[]{ 
 				Color.rgb(200, 50, 00),
 				Color.rgb(90, 250, 00),
 				Color.rgb(90, 250, 250)
 		};
 		//get unique labels
 		ArrayList<String> categories = new ArrayList<String>();
         if(expenses.size() > 0){
         	for(Expense e : expenses)
 			if(!categories.contains(e.getCategory()))
 				categories.add(e.getCategory());
         }
         
         ArrayList<GraphViewSeries> gvs = new ArrayList<GraphViewSeries>();
         for(int i = 0; i < categories.size(); i++){
         	GraphViewData[] gvd = new GraphViewData[expenses.size()];
         	for(int j = 0; j < expenses.size(); j++){
         		if(expenses.get(j).getCategory().equals(categories.get(i))){
         			gvd[j] = new GraphViewData(Double.valueOf(j), expenses.get(j).getAmount());        				
         		}else{
         			gvd[j] = new GraphViewData(Double.valueOf(j), 0.0d);
         		}        			
         	}
         	gvs.add(new GraphViewSeries(categories.get(i), colors[i], gvd));
         }
        		
 		GraphViewData[] data = new GraphViewData[expenses.size()];
 		String[] labels = new String[expenses.size()];
 		for(int i = 0; i < expenses.size(); i++){
 			data[i] = new GraphViewData(Double.valueOf(String.valueOf(i)), expenses.get(i).getAmount());
 			labels[i] = DateHelper.makeShortDate(expenses.get(i).getDateIncurred());
 		}
 		// dataSeries = new GraphViewSeries(data);
 		  
 		GraphView graphView = new BarGraphView(  
 		      this // context  
 		      , "Spend Trend" // heading  
 		);
 	    for(GraphViewSeries series : gvs){
 	    	graphView.addSeries(series); // data 
 	    }
 		//graphView.setHorizontalLabels(labels);  
 		//graphView.setVerticalLabels(new String[]{"More", "Some", "Less"});
 		
 //		graphView.setShowLegend(true);  
 //		graphView.setLegendAlign(LegendAlign.BOTTOM);  
 //		graphView.setLegendWidth(200); 
 		
 		LinearLayout layout = (LinearLayout) findViewById(R.id.layoutReport);
 		layout.addView(graphView);
 	}
 
 	/**
 	 * @param expenses
 	 */
 	private void bindReportTable(ArrayList<Expense> expenses) {
 		tblExpenses.removeAllViews();
 		if(expenses.size() > 0)
 		{
 			//title row:
 			TableRow row = (TableRow)LayoutInflater.from(ExpenseReporting.this).inflate(R.layout.report_row, null);
 			((TextView)row.findViewById(R.id.txtRowID)).setText("hidden_id");
 			((TextView)row.findViewById(R.id.txtRowCurrency)).setText("hidden_currency");
 			((TextView)row.findViewById(R.id.txtRowExchange)).setText("hidden_exchange");
 			((TextView)row.findViewById(R.id.txtRowCategory)).setText(ExpenseContentProvider.CATEGORY);
 			((TextView)row.findViewById(R.id.txtRowCategory)).setOnClickListener(lReportTitleCategoryClicked);
 			((TextView)row.findViewById(R.id.txtRowAmount)).setText(ExpenseContentProvider.AMOUNT);
 			((TextView)row.findViewById(R.id.txtRowAmount)).setOnClickListener(lReportTitleAmountClicked);
 			((TextView)row.findViewById(R.id.txtRowDate)).setText(ExpenseContentProvider.DATEINCURRED);
 			((TextView)row.findViewById(R.id.txtRowDate)).setOnClickListener(lReportTitleDateClicked);
 			tblExpenses.addView(row);			
 			
 			//following from http://stackoverflow.com/questions/5183968/how-to-add-rows-dynamically-into-table-layout
 			for(Expense e : expenses){				
 			    try {
 			    	// Inflate row "template" and fill out the fields.
 					row = (TableRow)LayoutInflater.from(ExpenseReporting.this).inflate(R.layout.report_row, null);
 					((TextView)row.findViewById(R.id.txtRowID)).setText(Integer.toString(e.getId()));
 					((TextView)row.findViewById(R.id.txtRowCategory)).setText(e.getCategory());
 					((TextView)row.findViewById(R.id.txtRowAmount)).setText(ExpenseContentProvider.MONEY_FORMAT.format(e.getAmount()));//TODO: fix magic string
 					((TextView)row.findViewById(R.id.txtRowCurrency)).setText(e.getCurrency());
 					((TextView)row.findViewById(R.id.txtRowExchange)).setText(Double.toString(e.getExchange()));
 					((TextView)row.findViewById(R.id.txtRowDate)).setText(e.getDateIncurred());
 					//row.setOnClickListener(lEditRowClicked);
 					tblExpenses.addView(row);
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 			tblExpenses.requestLayout();     // Not sure if this is needed.
 		}
 	}
 
 	/**
 	 * multi-purpose method for retrieving expense records.
 	 * pass in null dates to get all records.
 	 * pass in a null endDate to get only single specified date.
 	 * otherwise, dates passed will be used.
 	 * @param sortOrder
 	 * @param startDate
 	 * @param endDate
 	 * @return
 	 */
 	private ArrayList<Expense> getAllExpenses(String sortOrder, String startDate, String endDate) {
 		String[] sortBits = currentlySortedBy.split(" ");
 		if(sortBits[0].compareTo(sortOrder) == 0)
 			if(sortBits.length == 1)
 				sortOrder = sortOrder + DESCENDING;
 			else
 				sortOrder += currentlySortedBy.indexOf(DESCENDING) > 0 ? ASCENDING : DESCENDING;
 		currentlySortedBy = sortOrder;
 		String where = null;
 		String[] args = null;
 		if(startDate != null && startDate.length() > 0 && endDate != null && endDate.length() > 0){
 			where = ExpenseContentProvider.DATEINCURRED + " >= date('"+startDate+"') AND " + ExpenseContentProvider.DATEINCURRED + " <= date('"+endDate+"')";
 			//args = new String[] {DateHelper.makeShortDate(startDate), DateHelper.makeShortDate(endDate)};
 		}
 		if(startDate != null && endDate == null){
 			where = ExpenseContentProvider.DATEINCURRED + " like '"+DateHelper.makeShortDate(startDate)+"%'";
 			//args = new String[] {DateHelper.makeShortDate(startDate)};
 		}
 		ArrayList<Expense> expenses = new ArrayList<Expense>();
 		try{
 			Cursor c = getContentResolver().query(ExpenseContentProvider.CONTENT_URI, ExpenseContentProvider.FULL_PROJECTION, where, args, sortOrder);
 			if (c.moveToFirst()) {
 		    	do{
 					expenses.add(new Expense(
 							c.getInt(0),
 							c.getDouble(1),
 							c.getString(2),
 							c.getString(3),							
 							c.getString(4),
 							c.getDouble(5)));
 				}while(c.moveToNext());
 		    }
 			c.close();
 		}catch(Exception e){
 			Log.getStackTraceString(e);
 		}    
 		return expenses;
 	}
 	
 	private OnClickListener lReportTitleCategoryClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {			
 			tblExpenses = (TableLayout) findViewById(R.id.tblExpenses);
 			tblExpenses.removeAllViews();
 			ArrayList<Expense> expenses = getAllExpenses(ExpenseContentProvider.CATEGORY, dateSpanFrom, dateSpanTo);
 			bindReportTable(expenses);
 		}
 	};
 	
 	private OnClickListener lReportTitleAmountClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			tblExpenses = (TableLayout) findViewById(R.id.tblExpenses);
 			tblExpenses.removeAllViews();
 			ArrayList<Expense> expenses = getAllExpenses(ExpenseContentProvider.AMOUNT, dateSpanFrom, dateSpanTo);
 			bindReportTable(expenses);
 		}
 	};
 	
 	private OnClickListener lReportTitleDateClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			tblExpenses = (TableLayout) findViewById(R.id.tblExpenses);
 			tblExpenses.removeAllViews();
 			ArrayList<Expense> expenses = getAllExpenses(ExpenseContentProvider.DATEINCURRED, dateSpanFrom, dateSpanTo);
 			bindReportTable(expenses);
 		}
 	};
 	
 	private OnClickListener lSpanFromClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			((LinearLayout)viewChooseSpan.findViewById(R.id.layoutReportSpan)).setVisibility(View.GONE);
 			((LinearLayout)viewChooseSpan.findViewById(R.id.layoutReportSpanFrom)).setVisibility(View.VISIBLE);
 		}
 	};
 	
 	private OnClickListener lSpanFromSaveClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			((EditText)viewChooseSpan.findViewById(R.id.txtReportSpanFrom))
 				.setText(DateHelper.makeShortDate((DatePicker)viewChooseSpan.findViewById(R.id.dateSpanFrom)));
 			((LinearLayout)viewChooseSpan.findViewById(R.id.layoutReportSpanFrom)).setVisibility(View.GONE);
 			((LinearLayout)viewChooseSpan.findViewById(R.id.layoutReportSpan)).setVisibility(View.VISIBLE);
 		}
 	};
 	
 	private OnClickListener lSpanToClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			((LinearLayout)viewChooseSpan.findViewById(R.id.layoutReportSpan)).setVisibility(View.GONE);
 			((LinearLayout)viewChooseSpan.findViewById(R.id.layoutReportSpanTo)).setVisibility(View.VISIBLE);
 		}
 	};
 	
 	private OnClickListener lSpanToSaveClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			((EditText)viewChooseSpan.findViewById(R.id.txtReportSpanTo))
 				.setText(DateHelper.makeShortDate((DatePicker)viewChooseSpan.findViewById(R.id.dateSpanTo)));
 			((LinearLayout)viewChooseSpan.findViewById(R.id.layoutReportSpanTo)).setVisibility(View.GONE);
 			((LinearLayout)viewChooseSpan.findViewById(R.id.layoutReportSpan)).setVisibility(View.VISIBLE);
 		}
 	};
 	
 	private OnMenuItemClickListener lReportBySpanClicked = new OnMenuItemClickListener(){
 		@Override
 		public boolean onMenuItemClick(MenuItem mi) {
			dialogChooseSpan = dialogBuilder.show();	
 			return false;
 		}		
 	};
 	
 	private OnMenuItemClickListener lReportByDayClicked = new OnMenuItemClickListener(){
 		@Override
 		public boolean onMenuItemClick(MenuItem mi) {
 			ArrayList<Expense> expenses = getAllExpenses(currentlySortedBy, dateSpanFrom, null);
 			bindReportTable(expenses);
 			return false;
 		}		
 	};
 	
	/**
	 * Date span filter will default to using todays date if a date is not specified in either
	 * (or both) date fields before the button is clicked.
	 */
 	private OnClickListener lUseDateSpanFilterClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
			EditText txtFrom = ((EditText)viewChooseSpan.findViewById(R.id.txtReportSpanFrom));
			EditText txtTo = ((EditText)viewChooseSpan.findViewById(R.id.txtReportSpanTo));
			dateSpanFrom = txtFrom.length() > 0 ? txtFrom.getText().toString() : DateHelper.DateToString(new Date());
			dateSpanTo = txtTo.length() > 0 ? txtTo.getText().toString() : DateHelper.DateToString(new Date());
 			ArrayList<Expense> expenses = getAllExpenses(currentlySortedBy, dateSpanFrom, dateSpanTo);
 			bindReportTable(expenses);
 			dialogChooseSpan.dismiss();
			((ViewManager)viewChooseSpan.getParent()).removeView(viewChooseSpan);//remove view from context, in case the dialog is called again
 		}
 	};
 }
