 package com.zybnet.abc.view;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ScrollView;
 
 import com.zybnet.abc.R;
 import com.zybnet.abc.utils.U;
 
 // This class is not intended to be instantiated in XML
 @SuppressLint("ViewConstructor")
 public class PreferenceView extends ScrollView {
 
 	public PreferenceView(Context context) {
 		super(context);
 		setBackgroundColor(Color.BLACK);
 		View.inflate(context, R.layout.preferences, this);
 		setupCheckboxes();
 	}
 	
 	private OnCheckedChangeListener boolListener = new OnCheckedChangeListener() {
 		
 		@Override
 		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 			BoolPref pref = (BoolPref) buttonView.getTag();
 			pref.set(isChecked);
 		}
 	};
 	
 	private SharedPreferences prefs() {
 		return PreferenceManager.getDefaultSharedPreferences(getContext());
 	}
 	
 	private void setupCheckboxes() {
 		setupCheckboxPreference(R.id.decor_days, U.P_DECORATE_DAYS);
 		setupCheckboxPreference(R.id.decor_ords, U.P_DECORATE_ORDS);
 		
 		for (int i = 1;  i <= 7; i++ ) {
 			int id = getResources().getIdentifier("day_" + i, "id", getContext().getPackageName());
 			setupCheckboxPreference(id, U.P_DAY_PREFIX + i);
			((CheckBox) findViewById(id)).setText(
					U.uppercaseFirstChar(String.format("%tA", U.getLocalizedDayOfTheWeek(i))));
 		}
 	}
 	
 	private class BoolPref {
 		BoolPref(String name) {
 			this.name = name;
 		}
 		
 		String name;
 		
 		boolean get() {
 			return prefs().getBoolean(name, true);
 		}
 		
 		void set(boolean value) {
 			prefs().edit().putBoolean(name, value).commit();
 		}
 	};
 	
 	private void setupCheckboxPreference(int id, String prefKey) {
		CheckBox v = (CheckBox) findViewById(id);
		BoolPref p = new BoolPref(prefKey);
 		v.setChecked(p.get());
 		v.setTag(p);
 		v.setOnCheckedChangeListener(boolListener);
 	}
 
 }
