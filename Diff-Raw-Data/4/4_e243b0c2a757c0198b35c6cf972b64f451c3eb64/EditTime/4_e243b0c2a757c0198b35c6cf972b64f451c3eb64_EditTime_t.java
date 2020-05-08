 package com.kopysoft.chronos.subActivites;
 
 /**
  * 			Copyright (C) 2011 by Ethan Hall
  * 
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  * 	in the Software without restriction, including without limitation the rights
  * 	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * 	copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *  
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *  
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  *  
  */
 
 import java.util.GregorianCalendar;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 
 import com.kopysoft.chronos.R;
 import com.kopysoft.chronos.content.Chronos;
 import com.kopysoft.chronos.enums.Defines;
 
public class EditTime extends Activity  {
 
 	private static final String TAG = Defines.TAG + " - ET";
 	Cursor mCursor = null;
 	Chronos chronoSaver = null;
 	long id;
 	GregorianCalendar cal = null;
 	int position = 0;
 	private static final boolean DEBUG_PRINT = Defines.DEBUG_PRINT;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edittime);
 		if(DEBUG_PRINT) Log.d(TAG, "Creating EditTime");
 
 		id = getIntent().getExtras().getLong("id");
 		long time = getIntent().getExtras().getLong("time");
 		int actionReason = getIntent().getExtras().getInt("actionReason");
 		position = getIntent().getExtras().getInt("position", -1);
 
 		int[] timeSet = new int[2];
 		cal = new GregorianCalendar();
 		cal.setTimeInMillis(time);
 		timeSet[0] = cal.get(GregorianCalendar.HOUR_OF_DAY);
 		timeSet[1] = cal.get(GregorianCalendar.MINUTE);
 
 		if(DEBUG_PRINT) Log.d(TAG, "Input:" + time + "\tHour: " + timeSet[0] + "\tMin: " + timeSet[1]);
 
 		TimePicker timePick = (TimePicker) findViewById(R.id.TimePicker01);
 		timePick.setCurrentHour(timeSet[0]);
 		timePick.setCurrentMinute(timeSet[1]);
 		timePick.setIs24HourView(DateFormat.is24HourFormat(getApplicationContext()));
 
 		Spinner spinner = (Spinner) findViewById(R.id.spinnerType);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 				this, R.array.TimeTitles, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);
 		spinner.setSelection(actionReason);
 	}
 	
 	public void callBack(View v){
 		
 		TimePicker timePick = (TimePicker) findViewById(R.id.TimePicker01);
		timePick.clearFocus();
 		Spinner spinner = (Spinner) findViewById(R.id.spinnerType);
 		
 		int hour = timePick.getCurrentHour();
 		int min = timePick.getCurrentMinute();
 		cal.set(GregorianCalendar.HOUR_OF_DAY, hour);
 		cal.set(GregorianCalendar.MINUTE, min);
 		cal.set(GregorianCalendar.SECOND, 0);
 		long time = cal.getTimeInMillis();
 		//if(Defines.DEBUG_PRINT) Log.d(TAG, "Return Time:" + cal.getTimeInMillis() + "\tHour: " + hour + "\tMin: " + min);
 		if(DEBUG_PRINT) Log.d(TAG, "Return Time:" + cal.getTimeInMillis() + "\tHour: " + hour + "\tMin: " + min);
 		
 		int actionReason = 0;
 		if(spinner.getSelectedItemPosition() != Spinner.INVALID_POSITION)
 			actionReason = (spinner.getSelectedItemPosition());
 		
 		if(DEBUG_PRINT) Log.d(TAG, "ID: " + id);
 		
 		if(v.getId() == R.id.OkButton){ 
 			Intent returnIntent = new Intent();
 			returnIntent.putExtra("id", id);
 			returnIntent.putExtra("time", time);
 			returnIntent.putExtra("actionReason", actionReason);
 			returnIntent.putExtra("position", position);
 			setResult(Activity.RESULT_OK, returnIntent);
 			finish();
 			
 		} else if ( v.getId() == R.id.CancelButton){
 			Intent returnIntent = new Intent();
 			returnIntent.putExtra("id", id);
 			setResult(Activity.RESULT_CANCELED, returnIntent);
 			finish();
 		}
 	}
 
 }
