 package com.tzachsolomon.spendingtracker;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.SQLException;
 import android.os.Bundle;
 
 import android.text.InputType;
 import android.util.Log;
 
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnDoubleTapListener;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 
 import android.widget.EditText;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ViewEntriesSpent extends Activity implements OnGestureListener,
 		OnDoubleTapListener {
 
 	private final static String TAG = ViewEntriesSpent.class.getSimpleName();
 	public final static int TYPE_MONTH = 0;
 	public final static int TYPE_WEEK = 1;
 	public final static int TYPE_TODAY = 2;
 
 	private GestureDetector m_Detector;
 
 	private final static String XMLFILE = "spendingTracker.xml";
 
 	private TableLayout tlEntries;
 	private TextView textViewSpendingRefrenceDate;
 
 	private SpendingTrackerDbEngine m_SpendingTrackerDbEngine;
 	private int m_Type;
 	private Calendar m_Calendar;
 	private String[][] m_Data;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		//
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.spending_entries);
 
 		initializeVariables();
 
 		try {
 
 			Bundle extras = getIntent().getExtras();
 			m_Type = extras.getInt("TYPE");
 
 			//
 		} catch (SQLException e) {
 			Toast.makeText(this,
 					"Could not populate rows due to " + e.toString(),
 					Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 
 		}
 
 		m_Detector = new GestureDetector(this, this);
 	}
 
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent ev) {
 		//
 		this.m_Detector.onTouchEvent(ev);
 		return super.dispatchTouchEvent(ev);
 	}
 
 	@Override
 	protected void onResume() {
 		//
 
 		super.onResume();
 
 		updateTableLayout();
 	}
 
 	private void updateTableLayout() {
 
 		switch (m_Type) {
 
 		case TYPE_TODAY:
 
 			m_Data = m_SpendingTrackerDbEngine.getSpentDailyEntries(m_Calendar);
 			break;
 
 		case TYPE_WEEK:
 			m_Data = m_SpendingTrackerDbEngine.getSpentThisWeekEnteries(1,
 					m_Calendar);
 			break;
 		case TYPE_MONTH:
 			m_Data = m_SpendingTrackerDbEngine
 					.getSpentThisMonthEnteries(m_Calendar);
 			break;
 
 		default:
 			break;
 		}
 
 		PopulateRows(m_Data);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		//
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.menu.menu_spent, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		//
 		MenuItem menuItemSpentExport = menu.findItem(R.id.menuItemSpentExport);
 		MenuItem menuItemSpentImport = menu.findItem(R.id.menuItemSpentImport);
 		MenuItem menuItemSpentDatabase = menu
 				.findItem(R.id.menuItemSpentDatabase);
 
 		if (m_Type == TYPE_MONTH) {
 			menuItemSpentExport.setVisible(true);
 			menuItemSpentImport.setVisible(true);
 			menuItemSpentDatabase.setVisible(true);
 
 		} else {
 			menuItemSpentExport.setVisible(false);
 			menuItemSpentImport.setVisible(false);
 			menuItemSpentDatabase.setVisible(false);
 		}
 
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		//
 		boolean ret = false;
 		switch (item.getItemId()) {
 
 		case R.id.menuItemSpentStatistics:
 			menuItemSpentStatistics_Clicked();
 			break;
 
 		case R.id.menuItemSpentSortAmount:
 			menuItemSpentSortAmount_Clicked();
 			break;
 
 		case R.id.menuItemSpentSortCategory:
 			menuItemSpentSortCategory_Clicked();
 			break;
 
 		case R.id.menuItemSpentSortDate:
 			menuItemSpentSortDate_Clicked();
 			break;
 
 		case R.id.menuItemSpentSortId:
 			menuItemSpentSortId_Clicked();
 			break;
 
 		case R.id.menuItemSpentDelete:
 			menuItemSpentDelete_Clicked();
 			ret = true;
 			break;
 
 		case R.id.menuItemSpentEdit:
 			menuItemSpentEdit_Clicked();
 			ret = true;
 			break;
 
 		case R.id.menuItemSpentExport:
 			menuItemSpentExport_Clicked();
 			ret = true;
 			break;
 
 		case R.id.menuItemSpentImport:
 			menuItemSpentImport_Clicked();
 			ret = true;
 			break;
 
 		default:
 			ret = super.onOptionsItemSelected(item);
 			break;
 
 		}
 		return ret;
 	}
 
 	private void menuItemSpentStatistics_Clicked() {
 		//
 		int rowIndex = m_Data.length - 1;
 		HashMap<String, Float> stats = new HashMap<String, Float>();
 		HashMap<String, Integer> statsCounter = new HashMap<String, Integer>();
 		
 		StringBuilder stringBuilder = new StringBuilder();
 		
 		Log.i(TAG, "number of rows: " + rowIndex);
 
 		while (rowIndex >= 0) {
 
 			// column 2 in m_Data is the category
 			String key = m_Data[rowIndex][2];
 			if (stats.containsKey(key)) {
 				
 				float value = stats.get(key);
 				Log.i(TAG, "value " + value);
 				value += Float.parseFloat(m_Data[rowIndex][1]);
 				
 				stats.put(key, value);
 				statsCounter.put(key, statsCounter.get(key) + 1);
 
 			} else {
 				stats.put(key,
 						Float.parseFloat(m_Data[rowIndex][1]));
				statsCounter.put(key, 0);
 			}
 
 			rowIndex--;
 			
 
 		}
 		
 		
 		Iterator<String> a = stats.keySet().iterator();
 		
 		while ( a.hasNext() ){
 			String key = (String)a.next();
 			float value = (stats.get(key));
			stringBuilder.append("Category " + key);
 			
 			stringBuilder.append("\n\tTotal Spent: " + value);
 			stringBuilder.append("\n\tAverage Spent: " + value / (float)statsCounter.get(key));
 			stringBuilder.append("\n\t# of entries: " + statsCounter.get(key));
 			
 		}
 		
 		Dialog d = new Dialog(this);
 		d.setTitle(getString(R.string.dialogTitleSpentStatistics));
 		TextView message = new TextView(this);
 		message.setText(stringBuilder.toString());
 		d.setContentView(message);
 		d.show();
 		
 
 	}
 
 	private void menuItemSpentSortCategory_Clicked() {
 		//
 		m_SpendingTrackerDbEngine
 				.setSortBy(SpendingTrackerDbEngine.KEY_CATEGORY);
 		updateTableLayout();
 	}
 
 	private void menuItemSpentSortAmount_Clicked() {
 		//
 		m_SpendingTrackerDbEngine.setSortBy(SpendingTrackerDbEngine.KEY_AMOUNT);
 		updateTableLayout();
 	}
 
 	private void menuItemSpentSortDate_Clicked() {
 		//
 		m_SpendingTrackerDbEngine.setSortBy(SpendingTrackerDbEngine.KEY_DATE);
 		updateTableLayout();
 
 	}
 
 	private void menuItemSpentSortId_Clicked() {
 		//
 		m_SpendingTrackerDbEngine.setSortBy(SpendingTrackerDbEngine.KEY_ROWID);
 		updateTableLayout();
 	}
 
 	private void menuItemSpentImport_Clicked() {
 		//
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 
 		alertDialog
 				.setTitle(getString(R.string.alertDialogImportDatabaseTitle));
 		alertDialog
 				.setMessage(getString(R.string.alertDialogImportDatabaseMessage));
 		alertDialog.setPositiveButton(
 				getString(R.string.alertDialogImportDatabasePositive),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						//
 						importDatabase();
 
 					}
 
 				});
 		alertDialog.setNegativeButton(
 				getString(R.string.alertDialogImportDatabaseNegative),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						//
 
 					}
 				});
 
 		alertDialog.show();
 
 	}
 
 	private void importDatabase() {
 		String result = "";
 
 		try {
 			result = m_SpendingTrackerDbEngine.importFromXMLFile(XMLFILE);
 			updateTableLayout();
 
 		} catch (Exception e) {
 			//
 			result = e.getMessage();
 			e.printStackTrace();
 		}
 
 		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
 	}
 
 	private void exportDatabase() {
 
 		String result = "";
 		try {
 			result = m_SpendingTrackerDbEngine.exportToXMLFile(XMLFILE);
 		} catch (Exception e) {
 			//
 			result = e.getMessage();
 		}
 
 		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
 
 	}
 
 	private void menuItemSpentExport_Clicked() {
 		//
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 
 		alertDialog
 				.setTitle(getString(R.string.alertDialogExportDatabaseTitle));
 
 		alertDialog
 				.setMessage(getString(R.string.alertDialogExportDatabaseMessage));
 
 		alertDialog.setPositiveButton(
 				getString(R.string.alertDialogExportDatabasePositive),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						//
 						exportDatabase();
 
 					}
 
 				});
 		alertDialog.setNegativeButton(
 				getString(R.string.alertDialogExportDatabaseNegative),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						//
 
 					}
 				});
 
 		alertDialog.show();
 
 	}
 
 	private void menuItemSpentDelete_Clicked() {
 		//
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 		final EditText editTextRowId = new EditText(this);
 
 		editTextRowId.setInputType(InputType.TYPE_CLASS_NUMBER);
 		editTextRowId.setHint(getString(R.string.editTextRowIdHint));
 
 		alertDialog.setView(editTextRowId);
 
 		alertDialog
 				.setTitle(getString(R.string.alertDialogViewEntriesSpentTitleDelete));
 
 		alertDialog.setPositiveButton(
 				getString(R.string.alertDialogViewEntriesSpentPositiveDelete),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						//
 						String rowId = editTextRowId.getText().toString();
 
 						m_SpendingTrackerDbEngine
 								.deleteSpentEntryByRowId(rowId);
 
 						Toast.makeText(ViewEntriesSpent.this,
 								getString(R.string.entryDeleted),
 								Toast.LENGTH_SHORT).show();
 
 						updateTableLayout();
 
 					}
 				});
 		alertDialog.setNegativeButton(
 				getString(R.string.alertDialogViewEntriesSpentNegative),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						//
 
 					}
 				});
 
 		alertDialog.show();
 
 	}
 
 	private void menuItemSpentEdit_Clicked() {
 		//
 		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
 		final EditText editTextRowId = new EditText(this);
 
 		editTextRowId.setInputType(InputType.TYPE_CLASS_NUMBER);
 		editTextRowId.setHint(getString(R.string.editTextRowIdHint));
 
 		alertDialog.setView(editTextRowId);
 
 		alertDialog
 				.setTitle(getString(R.string.alertDialogViewEntriesSpentTitleEdit));
 
 		alertDialog.setPositiveButton(
 				getString(R.string.alertDialogViewEntriesSpentPositiveEdit),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						//
 						Intent intent = new Intent(ViewEntriesSpent.this,
 								EditEntrySpent.class);
 
 						intent.putExtra("RowId", editTextRowId.getText()
 								.toString());
 
 						startActivity(intent);
 
 					}
 				});
 		alertDialog.setNegativeButton(
 				getString(R.string.alertDialogViewEntriesSpentNegative),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						//
 
 					}
 				});
 
 		alertDialog.show();
 
 	}
 
 	private void PopulateRows(String[][] i_Data) {
 		int rows = i_Data.length;
 		int i;
 
 		try {
 			int howMuchRowsToRemove = tlEntries.getChildCount() - 1;
 
 			// removing all entries except the headers
 			tlEntries.removeViews(1, howMuchRowsToRemove);
 
 			for (i = 0; i < rows; i++) {
 				AddRowToTable(i_Data[i]);
 
 			}
 
 		} catch (Exception e) {
 			Log.e(TAG, e.getMessage());
 
 		}
 
 	}
 
 	private void AddRowToTable(String[] i_Row) {
 		int columns = i_Row.length;
 		int i;
 		TableRow tr = new TableRow(this);
 		tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 				LayoutParams.WRAP_CONTENT));
 		TextView[] textViews = new TextView[columns];
 		// taken from the todays_entries.xml
 		int[] layout_weight = new int[] { 10, 20, 60, 10 };
 
 		for (i = 0; i < columns; i++) {
 			textViews[i] = new TextView(this);
 			textViews[i].setText(i_Row[i]);
 			textViews[i].setLayoutParams(new LayoutParams(
 					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT,
 					layout_weight[i]));
 
 			tr.addView(textViews[i]);
 		}
 
 		tlEntries.addView(tr);
 
 	}
 
 	private void initializeVariables() {
 		// initialize members
 
 		m_Calendar = Calendar.getInstance();
 		m_Calendar.setTimeInMillis(System.currentTimeMillis());
 
 		tlEntries = (TableLayout) findViewById(R.id.tableLayoutEnteriesSpent);
 		textViewSpendingRefrenceDate = (TextView) findViewById(R.id.textViewSpendingRefrenceDate);
 
 		showRefrenceDate();
 
 		m_SpendingTrackerDbEngine = new SpendingTrackerDbEngine(this);
 		m_SpendingTrackerDbEngine.setSortBy(SpendingTrackerDbEngine.KEY_DATE);
 
 	}
 
 	@Override
 	public boolean onDown(MotionEvent e) {
 		//
 		return true;
 	}
 
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			float velocityY) {
 		//
 		float direction = e1.getX() - e2.getX();
 		float distance = Math.abs(direction);
 
 		Log.d(TAG, "velocityX " + velocityX);
 		Log.d(TAG, "distance " + distance);
 
 		// checking if the user swipe from left to right or right to left
 		if (Math.abs(velocityX) > 100 && distance > 100) {
 
 			if (direction > 0) {
 				// move right
 				screenSlide(1);
 			} else {
 				// move left
 				screenSlide(-1);
 
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Function will add according to m_Type it is currently displaying
 	 * 
 	 * @param i_Add
 	 *            - 1 add 1 day / week / month, -1 subtract 1 day / week / month
 	 */
 	private void screenSlide(int i_Add) {
 		//
 
 		switch (m_Type) {
 		case TYPE_TODAY:
 			m_Calendar.add(Calendar.DAY_OF_YEAR, i_Add);
 			break;
 
 		case TYPE_WEEK:
 
 			m_Calendar.add(Calendar.WEEK_OF_YEAR, i_Add);
 			break;
 
 		case TYPE_MONTH:
 
 			m_Calendar.add(Calendar.MONTH, i_Add);
 
 			break;
 
 		default:
 			break;
 
 		}
 
 		showRefrenceDate();
 		updateTableLayout();
 
 	}
 
 	private void showRefrenceDate() {
 		//
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("Current refrence date is (YYYY/MM/DD): \n");
 		sb.append(m_Calendar.get(Calendar.YEAR));
 		sb.append("/");
 		sb.append(m_Calendar.get(Calendar.MONTH) + 1);
 		sb.append("/");
 		sb.append(m_Calendar.get(Calendar.DAY_OF_MONTH));
 
 		textViewSpendingRefrenceDate.setText(sb.toString());
 
 		// Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onLongPress(MotionEvent e) {
 		//
 
 	}
 
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		//
 		return true;
 	}
 
 	@Override
 	public void onShowPress(MotionEvent e) {
 		//
 
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		//
 		return true;
 	}
 
 	@Override
 	public boolean onDoubleTap(MotionEvent e) {
 		//
 		return false;
 	}
 
 	@Override
 	public boolean onDoubleTapEvent(MotionEvent e) {
 		//
 		return false;
 	}
 
 	@Override
 	public boolean onSingleTapConfirmed(MotionEvent e) {
 		//
 		return false;
 	}
 
 }
