 package com.team03.fitsup.ui;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.team03.fitsup.R;
 import com.team03.fitsup.data.AttributeTable;
 import com.team03.fitsup.data.DatabaseAdapter;
 import com.team03.fitsup.data.ExerciseTable;
 import com.team03.fitsup.data.RecordTable;
 import com.team03.fitsup.data.WorkoutRoutineExerciseTable;
 
 //Needs a lot of thinking and work
 public class RecordView extends SherlockActivity {
 
 	private static final String TAG = "RecordView";
 	private static final boolean DEBUG = true;
 	private static final int ACTIVITY_DELETE = 0;
 	private static final int ACTIVITY_CREATE = 1;
 
 	private static final int DELETE_ID = Menu.FIRST;
 
 	private DatabaseAdapter mDbAdapter;
 	private TextView mDateText;
 	private TextView mValueText;
 	private TextView mHrText;
 	private TextView mMinText;
 	private TextView mSecText;
 	private TextView mSetText;
 	private TextView mRepText;
 	private TextView mWeightText;
 	private TextView mNameText;
 	// private Long mRowId;
 	private Long wreRowId;
 	private Long eRowId;
 	private String date;
 	private String name;
 	private Boolean empty = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (DEBUG)
 			Log.v(TAG, "+++ ON CREATE +++");
 
 		mDbAdapter = new DatabaseAdapter(getApplicationContext());
 		mDbAdapter.open();
 
 		// suggested by alex
 		getSupportActionBar().setDisplayUseLogoEnabled(false);
 		getSupportActionBar().setDisplayShowHomeEnabled(true);
 		getSupportActionBar().setDisplayShowTitleEnabled(true);
 
 		date = (savedInstanceState == null) ? null
 				: (String) savedInstanceState
 						.getSerializable(RecordTable.COLUMN_DATE);
 		if (date == null) {
 			Bundle extras = getIntent().getExtras();
 			date = extras != null ? extras.getString(RecordTable.COLUMN_DATE)
 					: null;
 		}
 
 		name = (savedInstanceState == null) ? null
 				: (String) savedInstanceState
 						.getSerializable(ExerciseTable.COLUMN_NAME);
 		if (name == null) {
 			Bundle extras = getIntent().getExtras();
 			name = extras != null ? extras.getString(ExerciseTable.COLUMN_NAME)
 					: null;
 		}
 
 		wreRowId = (savedInstanceState == null) ? null
 				: (Long) savedInstanceState
 						.getSerializable(RecordTable.COLUMN_WRKT_RTNE_E_ID);
 		if (wreRowId == null) {
 			Bundle extras = getIntent().getExtras();
 			wreRowId = extras != null ? extras
 					.getLong(RecordTable.COLUMN_WRKT_RTNE_E_ID) : null;
 		}
 		Log.v(TAG, "" + wreRowId);
 
 		eRowId = (savedInstanceState == null) ? null
 				: (Long) savedInstanceState
 						.getSerializable(ExerciseTable.COLUMN_ID);
 		if (eRowId == null) {
 			Bundle extras = getIntent().getExtras();
 			eRowId = extras != null ? extras.getLong(ExerciseTable.COLUMN_ID)
 					: null;
 		}
 		Log.v(TAG, "" + eRowId);
 
 		Cursor records = mDbAdapter.fetchAllRecordAttrByDate(date, wreRowId);
 		startManagingCursor(records);
 		if (records != null) {
 			records.moveToFirst();
 		}
 		if (records.getCount() > 0) {
 			Log.v(TAG, "there are more than zero records");
 		} else {
 			Log.v(TAG, "zero records");
 		}
 
 		switch (eRowId.intValue()) {
 		case 1:
 		case 2:
 		case 3:
 			if (records.getCount() > 0) {
 				setContentView(R.layout.cardio_view);
 				mNameText = (TextView) findViewById(R.id.exercise_name);
 				mDateText = (TextView) findViewById(R.id.date);
 				mValueText = (TextView) findViewById(R.id.value);
 				mHrText = (TextView) findViewById(R.id.hr);
 				mMinText = (TextView) findViewById(R.id.min);
 				mSecText = (TextView) findViewById(R.id.sec);
 			} else {
 				setContentView(R.layout.no_records);
 				empty = true;
 			}
 
 			break;
 		case 4:
 		case 5:
 		case 6:
 			if (records.getCount() > 0) {
 				setContentView(R.layout.strength);
 				mNameText = (TextView) findViewById(R.id.exercise_name);
 				mDateText = (TextView) findViewById(R.id.date);
 				mSetText = (TextView) findViewById(R.id.value);
 				mRepText = (TextView) findViewById(R.id.value2);
 				mWeightText = (TextView) findViewById(R.id.value3);
 			} else {
 				setContentView(R.layout.no_records);
 				empty = true;
 
 			}
 
 			break;
 		case 7:
 			if (records.getCount() > 0) {
 				setContentView(R.layout.jumping_jacks);
 				mNameText = (TextView) findViewById(R.id.exercise_name);
 				mDateText = (TextView) findViewById(R.id.date);
 				mSetText = (TextView) findViewById(R.id.value);
 				mRepText = (TextView) findViewById(R.id.value2);
 			} else {
 				setContentView(R.layout.no_records);
 				empty = true;
 
 			}
 
 			break;
 		case 8:
 			Log.v(TAG, "in 8");
 			if (records.getCount() > 0) {
 				setContentView(R.layout.stretch);
 				mNameText = (TextView) findViewById(R.id.exercise_name);
 				mDateText = (TextView) findViewById(R.id.date);
 				mMinText = (TextView) findViewById(R.id.min);
 				mSecText = (TextView) findViewById(R.id.sec);
 			} else {
 				setContentView(R.layout.no_records);
 				empty = true;
 
 			}
 			break;
 		case 9:
 			if (records.getCount() > 0) {
 				setContentView(R.layout.jumprope);
 				mNameText = (TextView) findViewById(R.id.exercise_name);
 				mDateText = (TextView) findViewById(R.id.date);
 				mMinText = (TextView) findViewById(R.id.min);
 				mSecText = (TextView) findViewById(R.id.sec);
 				mSetText = (TextView) findViewById(R.id.value);
 				mRepText = (TextView) findViewById(R.id.value2);
 			} else {
 				setContentView(R.layout.no_records);
 				empty = true;
 
 			}
 			break;
 
 		}
 		populateFields();
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// suggested by alex.
 		// changed options menu to XML
 		if (empty) {
 			getSupportMenuInflater().inflate(R.menu.record_view_options_menu, menu);
 		} else {
 		
 			getSupportMenuInflater().inflate(R.menu.record_delete_options_menu, menu);
 		}
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.delete_record:
 			mDbAdapter.deleteRecord(date, wreRowId);
 			finish();
 			return true;
 		case R.id.add_record:
 			createRecord();
 			return true;
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void createRecord() {
 		Cursor exercise_id = mDbAdapter.fetchExercisebyWRE(wreRowId);
 		long e_id = exercise_id
 				.getLong(exercise_id
 						.getColumnIndexOrThrow(WorkoutRoutineExerciseTable.COLUMN_EXERCISE_ID));
 		Intent i = new Intent(this, ExerciseRecordEdit.class);
 		i.putExtra(RecordTable.COLUMN_WRKT_RTNE_E_ID, wreRowId);
 		i.putExtra(ExerciseTable.COLUMN_ID, e_id);
 		exercise_id.close();
 		startActivityForResult(i, ACTIVITY_CREATE);
 	}
 
 	/*@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// suggested by alex.
 		// changed options menu to XML
 		switch (item.getItemId()) {
 		case R.id.add_workout:
 			createWorkout();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}*/
 
 	private void populateFields() {
 		Cursor records = mDbAdapter.fetchAllRecordAttrByDate(date, wreRowId);
 		startManagingCursor(records);
 		if (records != null) {
 			records.moveToFirst();
 		}
 		switch (eRowId.intValue()) {
 		case 1:
 		case 2:
 		case 3:
 			if (records.getCount() > 0) {
 				while (records.isAfterLast() == false) {
 					long rowId = records.getLong(records
 							.getColumnIndexOrThrow(RecordTable.COLUMN_ID));
 					Cursor record = mDbAdapter.fetchAttributeOfRecord(rowId);
 					String attribute = record.getString(record
 							.getColumnIndexOrThrow(AttributeTable.COLUMN_NAME));
 					if (attribute.equals("Time")) {
 						// fix this later
 						double value = records
 								.getDouble(records
 										.getColumnIndexOrThrow(RecordTable.COLUMN_VALUE));
 						if (DEBUG)
 							Log.v(TAG, "" + value);
 						int hour = (int) Math.floor(value / 60);
 						int min = (int) Math.floor(((value / 60) - hour) * 60);
 
 						int sec = (int) Math
 								.round(((((value / 60) - hour) * 60) - min) * 60);
 						mHrText.setText(String.valueOf(hour));
 						mMinText.setText(String.valueOf(min));
 						mSecText.setText(String.valueOf(sec));
 						record.close();
 
 					} else {
 						mValueText
 								.setText(records.getString(records
 										.getColumnIndexOrThrow(RecordTable.COLUMN_VALUE)));
 						record.close();
 					}
 					records.moveToNext();
 				}
 				mDateText.setText(date);
 				mNameText.setText(name);
 			}
 			break;
 		case 4:
 		case 5:
 		case 6:
 			if (records.getCount() > 0) {
 				while (records.isAfterLast() == false) {
 					long rowId1 = records.getLong(records
 							.getColumnIndexOrThrow(RecordTable.COLUMN_ID));
 					Cursor record1 = mDbAdapter.fetchAttributeOfRecord(rowId1);
 					String attribute1 = record1.getString(record1
 							.getColumnIndexOrThrow(AttributeTable.COLUMN_NAME));
 					int value1 = records.getInt(records
 							.getColumnIndexOrThrow(RecordTable.COLUMN_VALUE));
 					if (attribute1.equals("Set")) {
 						mSetText.setText(String.valueOf(value1));
 						record1.close();
 
 					} else if (attribute1.equals("Reps")) {
 						mRepText.setText(String.valueOf(value1));
 						record1.close();
 					} else {
 						mWeightText.setText(String.valueOf(value1));
 						record1.close();
 					}
 					records.moveToNext();
 				}
 				mDateText.setText(date);
 				mNameText.setText(name);
 
 			}
 			break;
 		case 7:
 			if (records.getCount() > 0) {
 				while (records.isAfterLast() == false) {
 					long rowId3 = records.getLong(records
 							.getColumnIndexOrThrow(RecordTable.COLUMN_ID));
 					Cursor record3 = mDbAdapter.fetchAttributeOfRecord(rowId3);
 					String attribute3 = record3.getString(record3
 							.getColumnIndexOrThrow(AttributeTable.COLUMN_NAME));
 					int value3 = records.getInt(records
 							.getColumnIndexOrThrow(RecordTable.COLUMN_VALUE));
 					if (attribute3.equals("Set")) {
 						mSetText.setText(String.valueOf(value3));
 						record3.close();
 
 					} else if (attribute3.equals("Reps")) {
 						mRepText.setText(String.valueOf(value3));
 						record3.close();
 					}
 					records.moveToNext();
 				}
 				mDateText.setText(date);
 				mNameText.setText(name);
 
 			}
 			break;
 		case 8:
 			if (records.getCount() > 0) {
 				Log.v(TAG, "in 888888");
 				long rowId4 = records.getLong(records
 						.getColumnIndexOrThrow(RecordTable.COLUMN_ID));
 				Cursor record4 = mDbAdapter.fetchAttributeOfRecord(rowId4);
 
 				double value4 = records.getDouble(records
 						.getColumnIndexOrThrow(RecordTable.COLUMN_VALUE));
 
 				int min4 = (int) Math.floor(value4);
 				int sec4 = (int) Math.round((value4 - min4) * 60);
 				mMinText.setText(String.valueOf(min4));
 				mSecText.setText(String.valueOf(sec4));
 				record4.close();
 				mDateText.setText(date);
 				mNameText.setText(name);
 			}
 			break;
 		case 9:
 			if (records.getCount() > 0) {
 				while (records.isAfterLast() == false) {
 					long rowId2 = records.getLong(records
 							.getColumnIndexOrThrow(RecordTable.COLUMN_ID));
 					Cursor record2 = mDbAdapter.fetchAttributeOfRecord(rowId2);
 					String attribute2 = record2.getString(record2
 							.getColumnIndexOrThrow(AttributeTable.COLUMN_NAME));
 
 					if (attribute2.equals("Time")) {
 						// fix this later
 						double value2 = records
 								.getDouble(records
 										.getColumnIndexOrThrow(RecordTable.COLUMN_VALUE));
 
 						int min2 = (int) Math.floor(value2);
 						Log.v(TAG, "Time");
 						int sec2 = (int) Math.round((value2 - min2) * 60);
 						mMinText.setText(String.valueOf(min2));
 						mSecText.setText(String.valueOf(sec2));
 						record2.close();
 
 					} else if (attribute2.equals("Set")) {
 						Log.v(TAG, "Set");
 
 						int value2 = records
 								.getInt(records
 										.getColumnIndexOrThrow(RecordTable.COLUMN_VALUE));
 						mSetText.setText(String.valueOf(value2));
 						record2.close();
 
 					} else if (attribute2.equals("Reps")) {
 						Log.v(TAG, "Reps");
 
 						int value2 = records
 								.getInt(records
 										.getColumnIndexOrThrow(RecordTable.COLUMN_VALUE));
 						mRepText.setText(String.valueOf(value2));
 						record2.close();
 					}
 					records.moveToNext();
 				}
 				mDateText.setText(date);
 				mNameText.setText(name);
 			}
 			break;
 		}
 
 	}
 
 }
