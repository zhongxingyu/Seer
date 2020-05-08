 /* 
 Copyright 2012 Javran Cheng
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 package org.evswork.whatsdonetoday;
 
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.content.Context;
 import android.preference.Preference;
 import android.util.AttributeSet;
 import android.widget.DatePicker;
 
 public class DatePickerPreference extends Preference {
 	public int year = 2012;
 	public int month = 1;
 	public int day = 1;
 
 	public DatePickerPreference(Context context, AttributeSet attrs,
 			int defStyle) {
 		super(context, attrs, defStyle);
 		initPreference();
 	}
 
 	public DatePickerPreference(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initPreference();
 	}
 
 
 	private void initPreference() {
 		setPersistent(false);
 		setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				DatePickerDialog dialog = new DatePickerDialog(getContext(), new OnDateSetListener() {
 					
 					@Override
 					public void onDateSet(DatePicker view, int year, int monthOfYear,
 							int dayOfMonth) {
 						DatePickerPreference.this.year = year;
						DatePickerPreference.this.month = monthOfYear;
 						DatePickerPreference.this.day = dayOfMonth;
 						updateUI();
 					}
				}, year, month, day);
 				dialog.show();
 				return true;
 			}
 		});
 		updateUI();
 	}
 	
 	public void updateUI() {
 		setSummary(buildDateString(year, month, day));
 	}
 
 	
 
 	public static String buildDateString(int year, int month, int day) {
 		return String.format("%04d-%02d-%02d", year, month, day);
 	}
 
 }
