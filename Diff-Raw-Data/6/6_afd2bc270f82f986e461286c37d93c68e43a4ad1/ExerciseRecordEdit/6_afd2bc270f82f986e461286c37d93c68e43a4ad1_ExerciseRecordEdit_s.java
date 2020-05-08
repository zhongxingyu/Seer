 package com.team03.fitsup.ui;
 
 import java.util.Calendar;
 
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.team03.fitsup.R;
 import com.team03.fitsup.data.DatabaseAdapter;
 import com.team03.fitsup.data.ExerciseTable;
 import com.team03.fitsup.data.RecordTable;
 
 public class ExerciseRecordEdit extends SherlockActivity {
 	private static final String TAG = "ExerciseRecordEdit";
 	private static final boolean DEBUG = true;
 
 	private DatabaseAdapter mDbAdapter;
 	private EditText mHourText;
 	private EditText mMinuteText;
 	private EditText mSecondText;
 	private EditText mValueText;
 	private EditText mSetText;
 	private EditText mRepText;
 	private EditText mWeightText;
 	private Long wreRowId;
 	private Long eRowId;
 	private String date;
 	private Button confirmButton;
 	private Button cancelButton;
 	private Cursor c;
 	private boolean saved = false;
 
 	// for the date picker
 
 	private int mYear;
 	private int mMonth;
 	private int mDay;
 
 	private TextView mDateDisplay;
 	private Button mPickDate;
 
 	static final int DATE_DIALOG_ID = 0;
 	static final int CANCEL_DIALOG_ID = 1;
 
 	// end date picker code
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (DEBUG)
 			Log.v(TAG, "+++ ON CREATE +++");
 
 		mDbAdapter = new DatabaseAdapter(getApplicationContext());
 		mDbAdapter.open();
 		
 		wreRowId = (savedInstanceState == null) ? null
 				: (Long) savedInstanceState
 						.getSerializable(RecordTable.COLUMN_WRKT_RTNE_E_ID);
 		if (wreRowId == null) {
 			Bundle extras = getIntent().getExtras();
 			wreRowId = extras != null ? extras
 					.getLong(RecordTable.COLUMN_WRKT_RTNE_E_ID) : null;
 		}
 
 		eRowId = (savedInstanceState == null) ? null
 				: (Long) savedInstanceState
 						.getSerializable(ExerciseTable.COLUMN_ID);
 		if (eRowId == null) {
 			Bundle extras = getIntent().getExtras();
 			eRowId = extras != null ? extras.getLong(ExerciseTable.COLUMN_ID)
 					: null;
 		}
 		
 		date = (savedInstanceState == null) ? null
 				: (String) savedInstanceState
 						.getSerializable(RecordTable.COLUMN_DATE);
 		if (date == null) {
 			Bundle extras = getIntent().getExtras();
 			date = extras != null ? extras.getString(RecordTable.COLUMN_DATE)
 					: "test";
 		}
 
 		Log.v(TAG, "Exercise id: " + eRowId);
 		switch (eRowId.intValue()) {
 		case 1:
 		case 2:
 		case 3:
 			setContentView(R.layout.records_edit);
 			mHourText = (EditText) findViewById(R.id.hr);
 			mMinuteText = (EditText) findViewById(R.id.min);
 			mSecondText = (EditText) findViewById(R.id.sec);
 			mValueText = (EditText) findViewById(R.id.value);
 			confirmButton = (Button) findViewById(R.id.confirm);
 			cancelButton = (Button) findViewById(R.id.cancel);
 			break;
 		case 4:
 		case 5:
 		case 6:
 			setContentView(R.layout.records_edit2);
 			mSetText = (EditText) findViewById(R.id.value);
 			mRepText = (EditText) findViewById(R.id.value2);
 			mWeightText = (EditText) findViewById(R.id.value3);
 			confirmButton = (Button) findViewById(R.id.confirm);
 			cancelButton = (Button) findViewById(R.id.cancel);
 			break;
 		case 7:
 			setContentView(R.layout.jumping_jacks_edit);
 			mSetText = (EditText) findViewById(R.id.value);
 			mRepText = (EditText) findViewById(R.id.value2);
 			confirmButton = (Button) findViewById(R.id.confirm);
 			cancelButton = (Button) findViewById(R.id.cancel);
 			break;
 		case 8:
 			setContentView(R.layout.stretch_edit);
 			mMinuteText = (EditText) findViewById(R.id.min);
 			mSecondText = (EditText) findViewById(R.id.sec);
 			confirmButton = (Button) findViewById(R.id.confirm);
 			cancelButton = (Button) findViewById(R.id.cancel);
 			break;
 		case 9:
 			setContentView(R.layout.jumprope_edit);
 			mHourText = (EditText) findViewById(R.id.hr);
 			mMinuteText = (EditText) findViewById(R.id.min);
 			mSecondText = (EditText) findViewById(R.id.sec);
 			mSetText = (EditText) findViewById(R.id.value);
 			mRepText = (EditText) findViewById(R.id.value2);
 			confirmButton = (Button) findViewById(R.id.confirm);
 			cancelButton = (Button) findViewById(R.id.cancel);
 			break;
 
 		}
 
 		// populateFields();
 
 		confirmButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View view) {
 				saveState();
 				if (saved) {
 					setResult(RESULT_OK);
 					finish();
 				} else {
 					Toast.makeText(
 
 					getApplicationContext(), "Please fill in fields.",
 							Toast.LENGTH_LONG).show();
 
 				}
 			}
 
 		});
 
 		cancelButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showDialog(CANCEL_DIALOG_ID);
 			}
 		});
 
 		mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
 		mPickDate = (Button) findViewById(R.id.pickDate);
 
 		mPickDate.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showDialog(DATE_DIALOG_ID);
 			}
 		});
 
 		ComponentName activity = getCallingActivity();
 		String act = activity != null ? activity.getClassName(): "other view";
 		if(act.equals("com.team03.fitsup.ui.ExerciseRecordUI"))
 		{
 			String title = getIntent().getStringExtra(ExerciseRecordUI.LAUNCH_RECORD);
 			final Calendar c = Calendar.getInstance();
 			mYear = c.get(Calendar.YEAR);
 			Log.v(TAG, "" + mYear);
 			mMonth = c.get(Calendar.MONTH);
 			Log.v(TAG, "" + mMonth);
 			mDay = c.get(Calendar.DAY_OF_MONTH);
 			Log.v(TAG, "" + mDay);
 		} else
 		{
 			String [] temp = date.split("-");
			for(int i = 0; i <temp.length; i++)
			{
				Log.v(TAG, "for loop: " + temp[i]);
			}
 			mYear = Integer.parseInt(temp[2]);
 			mMonth = Integer.parseInt(temp[0])-1;
			Log.v(TAG, "asdfadsfa "+mMonth);
 			mDay = Integer.parseInt(temp[1]);
 		}
 		
 		
 		
 //		// get the current date
 //		final Calendar c = Calendar.getInstance();
 //		mYear = c.get(Calendar.YEAR);
 //		Log.v(TAG, "" + mYear);
 //		mMonth = c.get(Calendar.MONTH);
 //		Log.v(TAG, "" + mMonth);
 //		mDay = c.get(Calendar.DAY_OF_MONTH);
 //		Log.v(TAG, "" + mDay);
 
 		// display the current date
 		updateDisplay();
 	}
 
 	// date picker helper method
 
 	private void updateDisplay() {
 		this.mDateDisplay.setText(new StringBuilder()
 				// Month is 0 based so add 1
 				.append(mMonth + 1).append("-").append(mDay).append("-")
 				.append(mYear));
 	}
 
 	// date picker helper class
 
 	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
 		public void onDateSet(DatePicker view, int year, int monthOfYear,
 				int dayOfMonth) {
 			mYear = year;
 			mMonth = monthOfYear;
 			mDay = dayOfMonth;
 			updateDisplay();
 		}
 	};
 
 	// date picker helper method
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_DIALOG_ID:
 			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
 					mDay);
 		case CANCEL_DIALOG_ID:
 			return new AlertDialog.Builder(this)
 					.setMessage("Are you sure you want to cancel?")
 					.setPositiveButton("YES",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									finish();
 								}
 							})
 					.setNegativeButton("NO",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 								}
 							}).create();
 
 		}
 		return null;
 
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		if (DEBUG)
 			Log.v(TAG, "ON SAVED INSTANCE STATE");
 
 		saveState();
 
 		// Toast.makeText(
 
 		// getApplicationContext(), "Please enter in the record." ,
 		// Toast.LENGTH_LONG).show();
 
 		// }
 		outState.putSerializable(RecordTable.COLUMN_WRKT_RTNE_E_ID, wreRowId);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (DEBUG)
 			Log.v(TAG, "+ ON PAUSE +");
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Log.v(TAG, "+ ON RESUME +");
 		// populateFields();
 	}
 
 	private void saveState() { // need to write exceptions for formatting errors
 		// switch case statement - later need to get exercise id //create SQL
 		// query or reuse another
 		String date = mDateDisplay.getText().toString();
 		// Cursor cTime = mDbAdapter.fetchAttribute("Time");
 		// long time_id = cTime.getLong(cTime
 		// .getColumnIndexOrThrow(AttributeTable.COLUMN_NAME));
 		// Cursor cDistance = mDbAdapter.fetchAttribute("Distance");
 		// long distance_id = cTime.getLong(cTime
 		// .getColumnIndexOrThrow(AttributeTable.COLUMN_NAME));
 		// Cursor cSets = mDbAdapter.fetchAttribute("Set");
 		// long sets_id = cTime.getLong(cTime
 		// .getColumnIndexOrThrow(AttributeTable.COLUMN_NAME));
 		// Cursor cReps = mDbAdapter.fetchAttribute("Reps");
 		// long reps_id = cTime.getLong(cTime
 		// .getColumnIndexOrThrow(AttributeTable.COLUMN_NAME));
 		// Cursor cWeights = mDbAdapter.fetchAttribute("Weight");
 		// long weight_id = cTime.getLong(cTime
 		// .getColumnIndexOrThrow(AttributeTable.COLUMN_NAME));
 
 		Context context = getApplicationContext();
 		CharSequence text = "Your Record has been saved.";
 		int duration = Toast.LENGTH_SHORT;
 		Toast toast = Toast.makeText(context, text, duration);
 		switch (eRowId.intValue()) {
 		case 1:
 		case 2:
 		case 3: // later need to have individual cases for all
 			String hour = mHourText.getText().toString();
 			String minute = mMinuteText.getText().toString();
 			String second = mSecondText.getText().toString();
 			String value = mValueText.getText().toString(); // distance
 
 			double hr = !TextUtils.isEmpty(hour) ? Double.parseDouble(hour) : 0;
 			double min = !TextUtils.isEmpty(minute) ? Double
 					.parseDouble(minute) : 0;
 			double sec = !TextUtils.isEmpty(second) ? Double
 					.parseDouble(second) : 0;
 
 			double savedTime = (hr == 0.0 && min == 0.0 && sec == 0.0) ? -1
 					: ((hr * 60.0) + min + (sec / 60.0));
 			double val = !TextUtils.isEmpty(value) ? Double.parseDouble(value)
 					: -1;
 			// time
 			if (eRowId.intValue() == 1) {
 				c = mDbAdapter.fetchRecord(date, 1, wreRowId);
 			} else if (eRowId.intValue() == 2) {
 				c = mDbAdapter.fetchRecord(date, 3, wreRowId);
 
 			} else if (eRowId.intValue() == 3) {
 				c = mDbAdapter.fetchRecord(date, 5, wreRowId);
 
 			}
 
 			// Cursor a = mDbAdapter.fetchAttribute("Time");
 			// long a_id =
 			// a.getLong(a.getColumnIndexOrThrow(AttributeTable.COLUMN_ID));
 			// Cursor ea = mDbAdapter.fetchExerciseAttribute(a_id, eRowId);
 			// long ea_id =
 			// ea.getLong(a.getColumnIndexOrThrow(ExerciseAttributeTable.COLUMN_ID));
 			// Cursor d = mDbAdapter.fetchRecord(date, 2, wreRowId); Is this
 			// okay? assuming if one value is filled, other is filled too
 			// if(c!=null && c.getCount()>0 && d!=null && d.getCount()>0)
 			if (c != null && c.getCount() > 0) {
 				if (savedTime != -1 && val != -1) {
 					if (eRowId.intValue() == 1) {
 						mDbAdapter.updateRecord(date, savedTime, 1, wreRowId);
 						mDbAdapter.updateRecord(date, val, 2, wreRowId);
 						saved = true;
 						toast.show();
 
 					} else if (eRowId.intValue() == 2) {
 						mDbAdapter.updateRecord(date, savedTime, 3, wreRowId);
 						mDbAdapter.updateRecord(date, val, 4, wreRowId);
 						saved = true;
 						toast.show();
 
 					} else if (eRowId.intValue() == 3) {
 						mDbAdapter.updateRecord(date, savedTime, 5, wreRowId);
 						mDbAdapter.updateRecord(date, val, 6, wreRowId);
 						saved = true;
 						toast.show();
 
 					}
 				}
 			} else {
 				if (savedTime != -1 && val != -1) {
 
 					if (eRowId.intValue() == 1) {
 						// time
 						mDbAdapter.createRecord(date, savedTime, 1, wreRowId);
 						// distance
 						mDbAdapter.createRecord(date, val, 2, wreRowId);
 						toast.show();
 						saved = true;
 					} else if (eRowId.intValue() == 2) {
 						// time
 						mDbAdapter.createRecord(date, savedTime, 3, wreRowId);
 						// distance
 						mDbAdapter.createRecord(date, val, 4, wreRowId);
 						toast.show();
 						saved = true;
 
 					} else if (eRowId.intValue() == 3) {
 						// time
 						mDbAdapter.createRecord(date, savedTime, 5, wreRowId);
 						// distance
 						mDbAdapter.createRecord(date, val, 6, wreRowId);
 						toast.show();
 						saved = true;
 
 					}
 
 				}
 			}
 			break;
 
 		case 4:
 		case 5:
 		case 6:
 			String setString = mSetText.getText().toString();
 			String repString = mRepText.getText().toString();
 			String weightString = mWeightText.getText().toString();
 			double set = !TextUtils.isEmpty(setString) ? Double
 					.parseDouble(setString) : -1;
 			double rep = !TextUtils.isEmpty(repString) ? Double
 					.parseDouble(repString) : -1;
 			double weight = !TextUtils.isEmpty(weightString) ? Double
 					.parseDouble(weightString) : -1;
 			// create Query that will query for the name of the attribute
 			// and take in ExerciseId to replace in createRecord() method
 
 			if (eRowId.intValue() == 4) {
 				c = mDbAdapter.fetchRecord(date, 7, wreRowId);
 			} else if (eRowId.intValue() == 5) {
 				c = mDbAdapter.fetchRecord(date, 10, wreRowId);
 
 			} else if (eRowId.intValue() == 6) {
 				c = mDbAdapter.fetchRecord(date, 13, wreRowId);
 
 			}
 
 			if (c != null && c.getCount() > 0) {
 				if (set != -1 && rep != -1 && weight != -1) {
 					if (eRowId.intValue() == 4) {
 						mDbAdapter.updateRecord(date, set, 7, wreRowId);
 						mDbAdapter.updateRecord(date, rep, 8, wreRowId);
 						mDbAdapter.updateRecord(date, weight, 9, wreRowId);
 						toast.show();
 						saved = true;
 					} else if (eRowId.intValue() == 5) {
 						mDbAdapter.updateRecord(date, set, 10, wreRowId);
 						mDbAdapter.updateRecord(date, rep, 11, wreRowId);
 						mDbAdapter.updateRecord(date, weight, 12, wreRowId);
 						toast.show();
 						saved = true;
 
 					} else if (eRowId.intValue() == 6) {
 						mDbAdapter.updateRecord(date, set, 13, wreRowId);
 						mDbAdapter.updateRecord(date, rep, 14, wreRowId);
 						mDbAdapter.updateRecord(date, weight, 15, wreRowId);
 						toast.show();
 						saved = true;
 					}
 
 				}
 			} else {
 				if (set != -1 && rep != -1 && weight != -1) {
 					if (eRowId.intValue() == 4) {
 						mDbAdapter.createRecord(date, set, 7, wreRowId);
 						mDbAdapter.createRecord(date, rep, 8, wreRowId);
 						mDbAdapter.createRecord(date, weight, 9, wreRowId);
 						toast.show();
 						saved = true;
 					} else if (eRowId.intValue() == 5) {
 						mDbAdapter.createRecord(date, set, 10, wreRowId);
 						mDbAdapter.createRecord(date, rep, 11, wreRowId);
 						mDbAdapter.createRecord(date, weight, 12, wreRowId);
 						toast.show();
 						saved = true;
 
 					} else if (eRowId.intValue() == 6) {
 						mDbAdapter.createRecord(date, set, 13, wreRowId);
 						mDbAdapter.createRecord(date, rep, 14, wreRowId);
 						mDbAdapter.createRecord(date, weight, 15, wreRowId);
 						toast.show();
 						saved = true;
 
 					}
 
 				}
 			}
 			break;
 		case 7:
 			String setString2 = mSetText.getText().toString();
 			String repString2 = mRepText.getText().toString();
 			double set2 = !TextUtils.isEmpty(setString2) ? Double
 					.parseDouble(setString2) : -1;
 			double rep2 = !TextUtils.isEmpty(repString2) ? Double
 					.parseDouble(repString2) : -1;
 			// create Query that will query for the name of the attribute
 			// and take in ExerciseId to replace in createRecord() method
 
 			c = mDbAdapter.fetchRecord(date, 16, wreRowId);
 
 			if (c != null && c.getCount() > 0) {
 				if (set2 != -1 && rep2 != -1) {
 
 					mDbAdapter.updateRecord(date, set2, 16, wreRowId);
 					mDbAdapter.updateRecord(date, rep2, 17, wreRowId);
 					toast.show();
 					saved = true;
 				}
 
 			} else {
 				if (set2 != -1 && rep2 != -1) {
 
 					mDbAdapter.createRecord(date, set2, 16, wreRowId);
 					mDbAdapter.createRecord(date, rep2, 17, wreRowId);
 					toast.show();
 					saved = true;
 				}
 			}
 			break;
 		case 8:
 			String minute2 = mMinuteText.getText().toString();
 			String second2 = mSecondText.getText().toString();
 			double min2 = !TextUtils.isEmpty(minute2) ? Double
 					.parseDouble(minute2) : 0;
 			double sec2 = !TextUtils.isEmpty(second2) ? Double
 					.parseDouble(second2) : 0;
 
 			double savedTime2 = (min2 == 0.0 && sec2 == 0.0) ? -1 : min2
 					+ (sec2 / 60.0);
 
 			// time
 
 			c = mDbAdapter.fetchRecord(date, 18, wreRowId);
 
 			if (c != null && c.getCount() > 0) {
 				if (savedTime2 != -1) {
 					mDbAdapter.updateRecord(date, savedTime2, 1, wreRowId);
 					toast.show();
 					saved = true;
 				}
 			} else {
 				if (savedTime2 != -1) {
 
 					// time
 					mDbAdapter.createRecord(date, savedTime2, 18, wreRowId);
 					toast.show();
 					saved = true;
 				}
 			}
 			break;
 		case 9:
 			String minute1 = mMinuteText.getText().toString();
 			String second1 = mSecondText.getText().toString();
 			String setString1 = mSetText.getText().toString();
 			String repString1 = mRepText.getText().toString();
 
 			// time
 			double min1 = !TextUtils.isEmpty(minute1) ? Double
 					.parseDouble(minute1) : 0;
 			double sec1 = !TextUtils.isEmpty(second1) ? Double
 					.parseDouble(second1) : 0;
 
 			double savedTime1 = (min1 == 0.0 && sec1 == 0.0) ? -1
 					: (min1 + (sec1 / 60.0));
 
 			// set/rep
 			double set1 = !TextUtils.isEmpty(setString1) ? Double
 					.parseDouble(setString1) : -1;
 			double rep1 = !TextUtils.isEmpty(repString1) ? Double
 					.parseDouble(repString1) : -1;
 
 			c = mDbAdapter.fetchRecord(date, 19, wreRowId);
 			// Cursor a = mDbAdapter.fetchAttribute("Time");
 			// long a_id =
 			// a.getLong(a.getColumnIndexOrThrow(AttributeTable.COLUMN_ID));
 			// Cursor ea = mDbAdapter.fetchExerciseAttribute(a_id, eRowId);
 			// long ea_id =
 			// ea.getLong(a.getColumnIndexOrThrow(ExerciseAttributeTable.COLUMN_ID));
 			// Cursor d = mDbAdapter.fetchRecord(date, 2, wreRowId); Is this
 			// okay? assuming if one value is filled, other is filled too
 			// if(c!=null && c.getCount()>0 && d!=null && d.getCount()>0)
 			if (c != null && c.getCount() > 0) {
 				if (savedTime1 != -1 && set1 != -1 && rep1 != -1) {
 					mDbAdapter.updateRecord(date, savedTime1, 19, wreRowId);
 					mDbAdapter.updateRecord(date, set1, 20, wreRowId);
 					mDbAdapter.updateRecord(date, rep1, 21, wreRowId);
 					toast.show();
 					saved = true;
 				}
 			} else {
 				if (savedTime1 != -1 && set1 != -1 && rep1 != -1) {
 					// time
 					mDbAdapter.createRecord(date, savedTime1, 19, wreRowId);
 					// set
 					mDbAdapter.createRecord(date, set1, 20, wreRowId);
 					// rep
 					mDbAdapter.createRecord(date, rep1, 21, wreRowId);
 					toast.show();
 					saved = true;
 				}
 			}
 			break;
 
 		}
 
 	}
 
 }
