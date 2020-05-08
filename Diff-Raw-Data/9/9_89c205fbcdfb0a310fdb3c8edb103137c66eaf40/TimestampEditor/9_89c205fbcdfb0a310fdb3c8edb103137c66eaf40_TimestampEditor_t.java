 package ch.almana.android.stechkarte.view;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.TimePicker;
import android.widget.Toast;
 import android.widget.TimePicker.OnTimeChangedListener;
 import ch.almana.android.stechkarte.R;
 import ch.almana.android.stechkarte.log.Logger;
 import ch.almana.android.stechkarte.model.Timestamp;
 import ch.almana.android.stechkarte.model.TimestampAccess;
 import ch.almana.android.stechkarte.provider.db.DB.Timestamps;
 
 public class TimestampEditor extends Activity implements OnTimeChangedListener {
 
 	private static final int DIA_DATE_SELECT = 0;
 	protected Timestamp timestamp;
 	protected Timestamp origTimestamp;
 	private Button dateField;
 	private TimePicker timeField;
 	private Button inOutField;
 	protected TextView timeDisplay;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.timestamp_editor);
 		Intent intent = getIntent();
 		String action = intent.getAction();
 		if (savedInstanceState != null) {
 			Log.w(Logger.LOG_TAG,
 					"Reading timestamp information from savedInstanceState");
 			if (timestamp != null) {
 				timestamp.readFromBundle(savedInstanceState);
 			} else {
 				timestamp = new Timestamp(savedInstanceState);
 			}
 		} else if (Intent.ACTION_INSERT.equals(action)) {
 			long millies = System.currentTimeMillis();
 			if (intent.hasExtra(Timestamps.NAME_TIMESTAMP)
 					&& intent.getExtras().getLong(Timestamps.NAME_TIMESTAMP) > 0l) {
 				millies = intent.getExtras().getLong(Timestamps.NAME_TIMESTAMP);
 			}
 			int type = getIntent().getIntExtra(Timestamps.NAME_TIMESTAMP_TYPE,
 					0);
 			timestamp = new Timestamp(millies, type);
 
 		} else if (Intent.ACTION_EDIT.equals(action)) {
 			Cursor c = managedQuery(intent.getData(),
 					Timestamps.DEFAULT_PROJECTION, null, null, null);
 			if (c.moveToFirst()) {
 				timestamp = new Timestamp(c);
 			}
 			c.close();
 		}
 		origTimestamp = new Timestamp(timestamp);
 		dateField = (Button) findViewById(R.id.ButtonDate);
 		timeField = (TimePicker) findViewById(R.id.TimePicker01);
 		inOutField = (Button) findViewById(R.id.ButtonInOut);
 		timeDisplay = (TextView) findViewById(R.id.TextViewTime);
 		inOutField.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				timestamp.setTimestampType(Timestamp
 						.invertTimestampType(getTimestamp()));
 				updateDisplayFields();
 			}
 		});
 		dateField.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showDialog(DIA_DATE_SELECT);
 
 			}
 		});
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIA_DATE_SELECT:
 			OnDateSetListener callBack = new OnDateSetListener() {
 				@Override
 				public void onDateSet(DatePicker view, int year,
 						int monthOfYear, int dayOfMonth) {
 					timestamp.setYear(year);
 					timestamp.setMonth(monthOfYear);
 					timestamp.setDay(dayOfMonth);
 					updateDisplayFields();
 				}
 			};
 			return new DatePickerDialog(this, callBack, getTimestamp()
 					.getYear(), getTimestamp().getMonth(), getTimestamp()
 					.getDay());
 
 		default:
 			return super.onCreateDialog(id);
 		}
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		timeField.setIs24HourView(true);
 		timeField.setCurrentHour(timestamp.getHour());
 		timeField.setCurrentMinute(timestamp.getMinute());
 		updateDisplayFields();
 
 		timeField.setOnTimeChangedListener(this);
 
 	}
 
 	@Override
 	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
 		getTimestamp().setHour(hourOfDay);
 		getTimestamp().setMinute(minute);
 		updateDisplayFields();
 	};
 
 	protected Timestamp getTimestamp() {
 		return timestamp;
 	}
 
 	private void updateDisplayFields() {
 		inOutField.setText(Timestamp.getTimestampTypeAsString(this, timestamp
 				.getTimestampType()));
 		timeDisplay.setText(timestamp.toString());
 		dateField.setText(timestamp.formatTimeDateOnly());
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
		try {
 		String action = getIntent().getAction();
 		if (Intent.ACTION_INSERT.equals(action)) {
 			TimestampAccess access = TimestampAccess.getInstance();
 			access.insert(timestamp);
 		} else if (Intent.ACTION_EDIT.equals(action)) {
 			if (origTimestamp.equals(timestamp)) {
 				return;
 			}
 			TimestampAccess access = TimestampAccess.getInstance();
 			access.update(timestamp);
 		}
		} catch (Exception e) {
			Log.w(Logger.LOG_TAG, "Cannot insert or update", e);
			Toast.makeText(this, getString(R.string.cannotSaveTS),
					Toast.LENGTH_LONG);
		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		timestamp.saveToBundle(outState);
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		if (timestamp != null) {
 			timestamp.readFromBundle(savedInstanceState);
 		} else {
 			timestamp = new Timestamp(savedInstanceState);
 		}
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 
 }
