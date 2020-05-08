 /**
  *  Kitchen Clock
  *  Copyright (C) 2012 Alexander Pastukhov
  *  
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  
  */
 package com.op.kclock.dialogs;
 
 import java.util.Calendar;
 
 import kankan.wheel.widget.WheelView;
 import kankan.wheel.widget.adapters.NumericWheelAdapter;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.format.DateFormat;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.op.kclock.MainActivity;
 import com.op.kclock.R;
 import com.op.kclock.model.AlarmClock;
 import com.op.kclock.ui.INumberPicker;
 import com.op.kclock.ui.NumberPicker;
 
 public class TimePickDialog extends Dialog {
 
 	private static final int MAX_HOUR = 23;
 	private static final int SCROLL_SPEED = 50;
 	private static final int SECONDS_IN_HOUR = 3600;
 	private static final int MAX_MINUTE_OR_SECOND = 59;
 	private int dialogWidth = 250;
 	private AlarmClock alarm = null;
 	private OnMyDialogResult mDialogResult; // the callback
 
 	private SharedPreferences mPrefs;
 
 	private INumberPicker hours;
 	private INumberPicker mins;
 	private INumberPicker secs;
 
 	private TextView timerName;
 
 	/**
 	 * Constructor with setup context.
 	 */
 	public TimePickDialog(Context context) {
 		super(context);
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		//isDialogShowed = true;
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.picktime);
 
 		LayoutInflater inflater = (LayoutInflater) getContext()
 				.getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);
 		LinearLayout parentL = (LinearLayout) findViewById(R.id.timepick);
 
 		LinearLayout pickView = null;
 
 		mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
 		String pickerType = mPrefs.getString(
 				getContext().getString(
 						R.string.pref_pickstyle_key), "wheel");
 						
   
  		if (pickerType.equals("wheel")) { 
 			pickView = (LinearLayout) inflater.inflate(R.layout.picktime_wheel,
 					null); 			
  		} else {
 			pickView = (LinearLayout) inflater.inflate(R.layout.picktime_stand,
 					null); 			
  		}
 		parentL.addView(pickView, 0, new LinearLayout.LayoutParams(
  				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 		
 		//init dialogs
  		if (pickerType.equals("wheel")) { 
  			setupWheel(); 	
  		} else {
  			setupNumeric();
 		}
 
 
 		timerName = (TextView) findViewById(R.id.timepicker_input_name);
 		if (!mPrefs.getBoolean(
 				getContext().getString(
 						R.string.pref_shownames_key), false)){
 			LinearLayout timerNameLt = (LinearLayout) findViewById(R.id.label_lt);
 			timerNameLt.setVisibility(View.GONE);				
 		}
 
 		if (alarm != null && alarm.getTime() > 0) {
 			hours.setCurrentItem((int) alarm.getHour());
 			mins.setCurrentItem((int) alarm.getMin());
 			secs.setCurrentItem((int) alarm.getSec());
 			timerName.setText(alarm.getName());
 		}
 
 		Button buttonCancel = (Button) findViewById(R.id.cancelsettimer);
 		buttonCancel.setOnClickListener(cancelHandler);
 		Button settimerbtn = (Button) findViewById(R.id.settimerbtn);
 		settimerbtn.setOnClickListener(saveHandler);
 	}
 
 	View.OnClickListener saveHandler = new View.OnClickListener() {
 		public void onClick(View v) {
 			if (!validateTime()) {
 				Toast.makeText(getContext(),
 						getContext().getText(R.string.toast_time_invalid),
 						Toast.LENGTH_SHORT).show();
 				return;
 			}
 
 			if (alarm == null) {
 				alarm = new AlarmClock(getContext());
 				alarm.setState(AlarmClock.TimerState.RUNNING);
 			}
 			alarm.setName(timerName.getText().toString());
 
 			long seconds = hours.getCurrentItem() * SECONDS_IN_HOUR
 					+ mins.getCurrentItem() * 60 + secs.getCurrentItem();
 			alarm.setTime(seconds);
 			
 
			
			mDialogResult.finish(alarm);
 			//isDialogShowed = false;
 			dismiss();
 		}
 
 		private boolean validateTime() {
 			return !(hours.getCurrentItem() == 0 && mins.getCurrentItem() == 0 && secs
 					.getCurrentItem() == 0);
 		}
 	};
 
 		public void setupWheel() {
 				hours = (WheelView) findViewById(R.id.hour);
 				if (mPrefs.getBoolean(
 						getContext().getString(R.string.pref_showhr_key), true)) {
 					((WheelView) hours).setViewAdapter(new NumericWheelAdapter(this
 							.getContext(), 0, MAX_HOUR));
 				} else {
 					hours.setVisibility(View.GONE);
 					View hourslbl = (View) findViewById(R.id.hourslbl);
 					hourslbl.setVisibility(View.GONE);
 				}
 		
 				mins = (WheelView) findViewById(R.id.mins);
 				if (mPrefs.getBoolean(
 						getContext().getString(R.string.pref_cyclicmins_key), true)) {
 					((WheelView) mins).setViewAdapter(new NumericWheelAdapter(this
 							.getContext(), 0, MAX_MINUTE_OR_SECOND, "%02d"));
 					((WheelView) mins).setCyclic(true);
 				} else {
 					((WheelView) mins).setViewAdapter(new NumericWheelAdapter(this
 							.getContext(), 0, 120, "%02d"));
 					((WheelView) mins).setCyclic(false);
 				}
 		
 				secs = (WheelView) findViewById(R.id.secs);
 		
 				if (mPrefs.getBoolean(
 						getContext().getString(R.string.pref_showsec_key), true)) {
 					((WheelView) secs).setViewAdapter(new NumericWheelAdapter(this
 							.getContext(), 0, MAX_MINUTE_OR_SECOND, "%02d"));
 					((WheelView) secs).setCyclic(true);
 				} else {
 					secs.setVisibility(View.GONE);
 					View secslbl = (View) findViewById(R.id.secslbl);
 					secslbl.setVisibility(View.GONE);
 				}
 		
 				TextView minslbl =  (TextView) findViewById(R.id.minslbl);
 				TextView secslbl =  (TextView) findViewById(R.id.secslbl);
 				TextView hrlbl =  (TextView) findViewById(R.id.hourslbl);
 				if (mPrefs.getBoolean(
 						getContext().getString(R.string.pref_simmetricpick_key),
 						true)) {
 					ViewGroup.LayoutParams lp = (LayoutParams) hours
 							.getLayoutParams();
 					lp.width = dialogWidth/3;
 						// lp.height = 10;
 					hours.setLayoutParams(lp);
 					mins.setLayoutParams(lp);
 					secs.setLayoutParams(lp);
 					ViewGroup.LayoutParams lp2 = (LayoutParams) minslbl
 							.getLayoutParams();
 					lp2.width = (dialogWidth/3);
 					minslbl.setLayoutParams(lp2);
 					secslbl.setLayoutParams(lp2);
 					hrlbl.setLayoutParams(lp2);
 				} else {
 					ViewGroup.LayoutParams lp = (LayoutParams) minslbl
 							.getLayoutParams();
 					lp.width = 120;
 					minslbl.setLayoutParams(lp);
 					lp = (LayoutParams) secslbl.getLayoutParams();
 					lp.width = 50;
 					secslbl.setLayoutParams(lp);
 					lp = (LayoutParams) hrlbl.getLayoutParams();
 					lp.width = 70;
 					hrlbl.setLayoutParams(lp);					
 				}
 		
 			}
 		
 			public void setupNumeric() {
 				hours = (NumberPicker) findViewById(R.id.hour);
 				mins = (NumberPicker) findViewById(R.id.mins);
 				secs = (NumberPicker) findViewById(R.id.secs);
 		
 				((NumberPicker) hours)
 						.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
 				((NumberPicker) mins)
 						.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
 				((NumberPicker) secs)
 						.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
 		
 				((NumberPicker) hours).setRange(0, MAX_HOUR);
 				((NumberPicker) mins).setRange(0, MAX_MINUTE_OR_SECOND);
 				((NumberPicker) secs).setRange(0, MAX_MINUTE_OR_SECOND);
 		
 				((NumberPicker) hours).setSpeed(SCROLL_SPEED);
 				((NumberPicker) mins).setSpeed(SCROLL_SPEED);
 				((NumberPicker) secs).setSpeed(SCROLL_SPEED);
 		
 			}
 		 
 		
 	public static String formatTime(final Calendar c) {
 		String M24 = "HH:mm:ss";
 		final String format = M24;
 		return (String) DateFormat.format(format, c);
 	}
 
 	View.OnClickListener cancelHandler = new View.OnClickListener() {
 		public void onClick(View v) {
 			//isDialogShowed = false;
 			dismiss();
 		}
 	};
 
 	public void setDialogResult(Object dialogResult) {
 		mDialogResult = (OnMyDialogResult) dialogResult;
 	}
 
 	public interface OnMyDialogResult {
 		void finish(AlarmClock result);
 	}
 
 	public void setAlarm(AlarmClock _alarm) {
 
 		alarm = _alarm;
 	}
 
 	@Override
 	public void cancel() {
 		//isDialogShowed = false;
 		super.cancel();
 	}
 	
 
 	
 }
