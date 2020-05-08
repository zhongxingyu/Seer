 /*
  * Copyright (C) 2011 Arnaud Bos <arnaud.tlse@gmail.com>
  * 
  * This file is part of Luscinia.
  * 
  * Luscinia is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Luscinia is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Luscinia.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.brookes.arnaudbos.luscinia.views;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import org.ektorp.ViewQuery;
 
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectExtra;
 import roboguice.inject.InjectResource;
 import roboguice.inject.InjectView;
 import uk.ac.brookes.arnaudbos.luscinia.LusciniaApplication;
 import uk.ac.brookes.arnaudbos.luscinia.R;
 import uk.ac.brookes.arnaudbos.luscinia.data.Document;
 import uk.ac.brookes.arnaudbos.luscinia.data.Record;
 import uk.ac.brookes.arnaudbos.luscinia.data.TransRecord;
 import uk.ac.brookes.arnaudbos.luscinia.listeners.TransListener;
 import uk.ac.brookes.arnaudbos.luscinia.utils.Log;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 import com.google.inject.Inject;
 
 /**
  * Activity displaying TRANS document
  * @author arnaudbos
  */
 public class TransActivity extends RoboActivity
 {
 	public static final int DIALOG_EMPTY_FIELD = 101;
 	public static final int DIALOG_TIME_ELAPSED = 102;
 	public static final int DIALOG_CREATE_RECORD_ERROR = 103;
 	public static final int DIALOG_UPDATE_RECORD_ERROR = 104;
 	public static final int DIALOG_LOAD_ERROR = 105;
 
 	final Handler uiThreadCallback = new Handler();
     private ProgressDialog mProgressDialog;
     private TransRecord newRecord;
     private List<TransRecord> records = new ArrayList<TransRecord>();
 
 	@Inject private TransListener listener;
 
 	@InjectExtra("document") private Document document;
 
 	@InjectView(R.id.table_view) private TableLayout tableView;
 	@InjectView(R.id.focus_edit) private EditText focusEdit;
 	@InjectView(R.id.data_edit) private EditText dataEdit;
 	@InjectView(R.id.actions_edit) private EditText actionsEdit;
 	@InjectView(R.id.results_edit) private EditText resultsEdit;
 	@InjectView(R.id.button_validate) private Button validateButton;
 
 	@InjectResource(R.string.dialog_empty_field_error_title) private String dialogEmptyFieldErrorTitle;
 	@InjectResource(R.string.dialog_empty_field_error_message) private String dialogEmptyFieldErrorMessage;
 	@InjectResource(R.string.dialog_time_elapsed_error_title) private String dialogTimeElapsedErrorTitle;
 	@InjectResource(R.string.dialog_time_elapsed_error_message) private String dialogTimeElapsedErrorMessage;
 	@InjectResource(R.string.ok) private String ok;
 	@InjectResource(R.string.save) private String save;
 	@InjectResource(R.string.update) private String update;
 	@InjectResource(R.string.cancel) private String cancel;
 	@InjectResource(R.string.create_loading) private String createLoading;
 	@InjectResource(R.string.create_error_title) private String createErrorTitle;
 	@InjectResource(R.string.create_folder_error_message) private String createRecordErrorMessage;
 	@InjectResource(R.string.save_loading) private String saveLoading;
 	@InjectResource(R.string.save_error_title) private String saveErrorTitle;
 	@InjectResource(R.string.save_error_message) private String saveErrorMessage;
 	@InjectResource(R.string.records_loading) private String recordsLoading;
 	@InjectResource(R.string.load_error_title) private String loadErrorTitle;
 	@InjectResource(R.string.load_error_message) private String loadErrorMessage;
 
 	private TableRow selectedRow;
 	private TableLayout dialogTableView;
 	private ScrollView dialogScrollView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		Log.d("TransActivity.onCreate");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.trans);
 		listener.setContext(this);
 
 		validateButton.setOnClickListener(listener);
 	}
 	
 	@Override
 	protected void onStart()
 	{
     	Log.d("TransActivity.onStart");
 		super.onStart();
 		
 		// Load the records from the database
 		loadRecords();
 	}
 
 	/**
 	 * Retrieve the records of the document from the database
 	 */
 	private void loadRecords()
 	{
 		Log.d("TransActivity.loadRecords");
 		// Launch an indeterminate ProgressBar in the UI while retrieving the records list in a new thread
     	mProgressDialog = ProgressDialog.show(this, "", recordsLoading, true);
 
    	int count = tableView.getChildCount()-3;
		for(int i = count ; i>0 ; i--)
		{
			tableView.removeViewAt(1);
		}

     	// Create a Runnable that will be executed if the query succeeds
     	final Runnable threadCallBackSuceeded = new Runnable()
     	{
     		public void run()
     		{
     			Log.d("Query executed successfully");
     			// Hide the ProgressBar and render the records table
     			mProgressDialog.dismiss();
     			renderTableView();
     		}
     	};
 
     	// Create a Runnable that will be executed if the query fails
     	final Runnable threadCallBackFailed = new Runnable()
     	{
     		public void run()
     		{
     			Log.d("View query failed");
     			// Hide the ProgressBar and open an Alert with an error message
     			mProgressDialog.dismiss();
     			showDialog(DIALOG_LOAD_ERROR);
     		}
     	};
 
     	// Create the separate thread that will retrieve the patients thanks to a view query and start it
 		new Thread()
 		{
 			@Override public void run()
 			{
 				try
 				{
 	    			Log.d("Query "+Record.VIEW_ALL_RECORDS+" view");
 					// Execute the view query and retrieve the records
 	    			records = new ArrayList<TransRecord>();
 					records.addAll(LusciniaApplication.getDB().queryView(new ViewQuery().designDocId("_design/views").viewName(Record.VIEW_ALL_RECORDS), TransRecord.class));
 					uiThreadCallback.post(threadCallBackSuceeded);
 				}
 				catch (Exception e)
 				{
 					Log.e("Execute view query "+Record.VIEW_ALL_RECORDS+" failed", e);
 					uiThreadCallback.post(threadCallBackFailed);
 				}
 			}
 		}.start();
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id)
 	{
 		Log.d("TransActivity.onCreateDialog id="+id);
 		switch (id)
 		{
 			case DIALOG_EMPTY_FIELD:
 				Log.d("Display DIALOG_EMPTY_FIELD Alert");
 				return new AlertDialog.Builder(this)
 				.setTitle(dialogEmptyFieldErrorTitle)
 				.setMessage(dialogEmptyFieldErrorMessage)
 				.setNegativeButton(ok, null)
 				.create();
 			case DIALOG_TIME_ELAPSED:
 				Log.d("Display DIALOG_TIME_ELAPSED Alert");
 				return new AlertDialog.Builder(this)
 				.setTitle(dialogTimeElapsedErrorTitle)
 				.setMessage(dialogTimeElapsedErrorMessage)
 				.setNegativeButton(ok, null)
 				.create();
 			case DIALOG_CREATE_RECORD_ERROR:
 				Log.d("Display DIALOG_CREATE_RECORD_ERROR Alert");
 				return new AlertDialog.Builder(this)
 					.setTitle(createErrorTitle)
 					.setMessage(createRecordErrorMessage)
 					.setPositiveButton(ok, null)
 					.create();
 			case DIALOG_UPDATE_RECORD_ERROR:
 				Log.d("Display DIALOG_UPDATE_RECORD_ERROR Alert");
 				return new AlertDialog.Builder(this)
 					.setTitle(saveErrorTitle)
 					.setMessage(saveErrorMessage)
 					.setPositiveButton(ok, null)
 					.create();
 			case DIALOG_LOAD_ERROR:
 				Log.d("Display DIALOG_LOAD_ERROR Alert");
 				return new AlertDialog.Builder(this)
 					.setTitle(loadErrorTitle)
 					.setMessage(loadErrorMessage)
 					.setPositiveButton(ok, null)
 					.create();
 		}
 		return null;
 	}
 	
 	public void showRowUpdateDialog()
 	{
 		Log.d("TransActivity.showRowUpdateDialog");
 		// Inflate a new trans item and fill it with the content from the selected row's record
 		TransRecord record = (TransRecord) selectedRow.getTag();
 
 		dialogScrollView = (ScrollView)LayoutInflater.from(this).inflate(R.layout.trans_update_item, null);
 		dialogTableView = (TableLayout)dialogScrollView.findViewById(R.id.table_view);
 		((EditText)dialogTableView.findViewById(R.id.focus_edit)).setText(record.getFocus());
 		((EditText)dialogTableView.findViewById(R.id.data_edit)).setText(record.getData());
 		((EditText)dialogTableView.findViewById(R.id.actions_edit)).setText(record.getActions());
 		((EditText)dialogTableView.findViewById(R.id.results_edit)).setText(record.getResults());
 
 		// Display the dialog with the trans item view
 		new AlertDialog.Builder(this)
 			.setTitle(update)
 			.setView(dialogScrollView)
 			.setPositiveButton(save, listener)
 			.setNegativeButton(cancel, listener)
 			.create().show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		Log.d("TransActivity.onCreateOptionsMenu");
 		//   	MenuItem item = menu.add("Transmission Menu");
 		//	   	item.setOnMenuItemClickListener(new OnMenuItemClickListener()
 		//		{
 		//			@Override
 		//			public boolean onMenuItemClick(MenuItem item)
 		//			{
 		//				// Do something.
 		//				Toast.makeText(TransActivity.this, "Trans Menu 1", Toast.LENGTH_SHORT).show();
 		//				return true;
 		//			}
 		//		});
 
 		return true;
 	}
 
 	/**
 	 * Save a new record into the database
 	 */
 	public void createRecord(final String focus, final String data, final String actions, final String results)
 	{
 		Log.d("TransActivity.createRecord");
 		// Launch an indeterminate ProgressBar in the UI while creating the records in a new thread
     	mProgressDialog = ProgressDialog.show(this, "", createLoading, true);
 
     	// Create a Runnable that will be executed if the creation succeeds
     	final Runnable recordCreationSucceeded = new Runnable()
     	{
     		public void run()
     		{
     			Log.d("Record created successfully");
     			// Hide the ProgressBar and render the new record
     			mProgressDialog.dismiss();
     			renderNewRecord(newRecord);
     		}
     	};
 
     	// Create a Runnable that will be executed if the creation fails
     	final Runnable recordCreationFailed = new Runnable()
     	{
     		public void run()
     		{
     			Log.d("Create record failed");
     			// Hide the ProgressBar and open an Alert with an error message
     			newRecord = null;
     			mProgressDialog.dismiss();
     			showDialog(DIALOG_CREATE_RECORD_ERROR);
     		}
     	};
 
     	// Create the separate thread that will create the record and start it
 		new Thread()
 		{
 			@Override public void run()
 			{
 				try
 				{
 	    			Log.d("Create the record");
 	    			newRecord = new TransRecord();
 	    			newRecord.setId(UUID.randomUUID().toString());
 					LusciniaApplication.getDB().create(fillRecord(newRecord, focus, data, actions, results));
 
 					uiThreadCallback.post(recordCreationSucceeded);
 				}
 				catch (Exception e)
 				{
 					Log.e("Create record failed", e);
 					uiThreadCallback.post(recordCreationFailed);
 				}
 			}
 		}.start();
 	}
 	
 	/**
 	 * Return a the record filled with document Id (from activity), focus, data, actions and results fields
 	 * @return a Record object
 	 */
 	private Record fillRecord(TransRecord record, String focus, String data, String actions, String results)
 	{
 		Log.d("TransActivity.fillRecord");
 		record.setDocumentId(document.getId());
 		record.setFocus(focus);
 		record.setData(data);
 		record.setActions(actions);
 		record.setResults(results);
 		
 		return record;
 	}
 
 	/**
 	 * Update the selected record
 	 */
 	public void updateRecord(final String focus, final String data, final String actions, final String results)
 	{
 		Log.d("TransActivity.updateRecord");
 		// Launch an indeterminate ProgressBar in the UI while updating the records in a new thread
     	mProgressDialog = ProgressDialog.show(this, "", saveLoading, true);
 
     	// Create a Runnable that will be executed if the update succeeds
     	final Runnable recordCreationSucceeded = new Runnable()
     	{
     		public void run()
     		{
     			Log.d("Record updated successfully");
     			// Hide the ProgressBar and re-render the selected row
     			mProgressDialog.dismiss();
     			renderRecord(selectedRow, (TransRecord) selectedRow.getTag());
     			selectedRow = null;
     		}
     	};
 
     	// Create a Runnable that will be executed if the update fails
     	final Runnable recordCreationFailed = new Runnable()
     	{
     		public void run()
     		{
     			Log.d("Update record failed");
     			// Hide the ProgressBar and open an Alert with an error message
     			selectedRow = null;
     			mProgressDialog.dismiss();
     			showDialog(DIALOG_UPDATE_RECORD_ERROR);
     		}
     	};
 
     	// Create the separate thread that will update the record and start it
 		new Thread()
 		{
 			@Override public void run()
 			{
 				try
 				{
 	    			Log.d("Update the record");
 	    			TransRecord oldRecord = (TransRecord) selectedRow.getTag();
 	    			TransRecord record = new TransRecord(oldRecord.getId(), oldRecord.getRevision(), oldRecord.getDate());
 					LusciniaApplication.getDB().update(fillRecord(record, focus, data, actions, results));
 					selectedRow.setTag(record);
 
 					uiThreadCallback.post(recordCreationSucceeded);
 				}
 				catch (Exception e)
 				{
 					Log.e("Create record failed", e);
 					uiThreadCallback.post(recordCreationFailed);
 				}
 			}
 		}.start();
 	}
 
 	/**
 	 * Render the tavle view with the list of records of this document
 	 */
 	private void renderTableView()
 	{
 		Log.d("TransActivity.renderTableView");
 		for(TransRecord currentRecord : records)
 		{
 			renderNewRecord(currentRecord);
 		}
 	}
 
 	/**
 	 * Render the given record into a new row and add it to the table
 	 */
 	private void renderNewRecord(TransRecord newRecord)
 	{
 		Log.d("TransActivity.renderNewRecord");
 		// Inflate a new tableRow
 		TableRow newRow = (TableRow) LayoutInflater.from(this).inflate(R.layout.trans_item, null);
 		// Set the new record as tag for this row
 		newRow.setTag(newRecord);
 		newRow.setOnLongClickListener(listener);
 
 		// Fill in the trans item row with the fields from the new record
 		renderRecord(newRow, newRecord);
 		
 		// Insert the new TableRow as last TableLayout row before the EditTexts and validation Button
 		tableView.addView(newRow, tableView.getChildCount()-2);
 
 		// Reset the content of the EditTexts
 		resetEditTexts();
 		if(getCurrentFocus()!=null)
 		{
 			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
 		}
 	}
 	
 	/**
 	 * Render the given record into the given row
 	 * @param row The row in which the record will be rendered
 	 * @param record The record to render
 	 */
 	private void renderRecord(TableRow row, TransRecord record)
 	{
 		Log.d("TransActivity.renderRecord");
 		// Fill in the trans item row with the fields from the new record
 		TextView dateView = (TextView) row.findViewById(R.id.date_view);
 		SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy\nHH:mm:ss");
 		dateView.setText(s.format(record.getDate()));
 		TextView focusView = (TextView) row.findViewById(R.id.focus_view);
 		focusView.setText(record.getFocus());
 		TextView dataView = (TextView) row.findViewById(R.id.data_view);
 		dataView.setText(record.getData());
 		TextView actionsView = (TextView) row.findViewById(R.id.actions_view);
 		actionsView.setText(record.getActions());
 		TextView resultsView = (TextView) row.findViewById(R.id.results_view);
 		resultsView.setText(record.getResults());
 	}
 
 	/**
 	 * @return the tableView
 	 */
 	public TableLayout getTableView()
 	{
 		Log.d("TransActivity.getTableView");
 		return tableView;
 	}
 
 	/**
 	 * @return the focusEdit
 	 */
 	public String getFocus()
 	{
 		Log.d("TransActivity.getFocus");
 		return focusEdit.getText().toString();
 	}
 
 	/**
 	 * @return the dataEdit
 	 */
 	public String getData()
 	{
 		Log.d("TransActivity.getData");
 		return dataEdit.getText().toString();
 	}
 
 	/**
 	 * @return the actionsEdit
 	 */
 	public String getActions()
 	{
 		Log.d("TransActivity.getActions");
 		return actionsEdit.getText().toString();
 	}
 
 	/**
 	 * @return the resultsEdit
 	 */
 	public String getResults()
 	{
 		Log.d("TransActivity.getResults");
 		return resultsEdit.getText().toString();
 	}
 
 	/**
 	 * Reset the EditTexts of the activity view
 	 */
 	public void resetEditTexts()
 	{
 		Log.d("TransActivity.resetEditTexts");
 		focusEdit.setText("");
 		dataEdit.setText("");
 		actionsEdit.setText("");
 		resultsEdit.setText("");
 	}
 
 	/**
 	 * @return the dialogTableView
 	 */
 	public TableLayout getDialogTableView()
 	{
 		Log.d("TransActivity.getDialogTableView");
 		return dialogTableView;
 	}
 
 	/**
 	 * @param the selectedRow
 	 */
 	public void setSelectedRow(TableRow selectedRow)
 	{
 		Log.d("TransActivity.setSelectedRow");
 		this.selectedRow = selectedRow;
 	}
 
 	/**
 	 * @return the selectedRow
 	 */
 	public TableRow getSelectedRow()
 	{
 		Log.d("TransActivity.getSelectedRow");
 		return selectedRow;
 	}
 }
