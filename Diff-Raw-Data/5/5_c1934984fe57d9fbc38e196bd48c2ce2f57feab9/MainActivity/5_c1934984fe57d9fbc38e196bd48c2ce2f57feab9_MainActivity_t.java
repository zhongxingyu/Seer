 package se.orourke.wetfags;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class MainActivity extends Activity implements OnCheckedChangeListener, OnEditorActionListener {
 	private class MyPagerAdapter extends PagerAdapter {
 
 		@Override
 		public int getCount() {
 			return 3;
 		}
 
 		@Override
 		public boolean isViewFromObject(View arg0, Object arg1) {
 			return arg0 == ((View)arg1);
 		}
 
 		@Override
 		public void destroyItem(ViewGroup container, int position, Object object) {
 			((ViewPager) container).removeView((View) object);
 		}
 
 		@Override
 		public Object instantiateItem(ViewGroup container, int position) {
             LayoutInflater inflater = (LayoutInflater) container.getContext()
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             int resId = 0;
             switch (position) {
 	            case 0:
 	            	resId = R.layout.five_as_layout;
 	                break;
 	            case 1:
 	                resId = R.layout.wetfags_layout;
 	            	break;
 	            case 2:
 	                resId = R.layout.parameters_layout;
 	                break;
             }
             View view = inflater.inflate(resId, null);
             ((ViewPager) container).addView(view, 0);
             return view;
         }
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			return getResources().getStringArray(R.array.tabs)[position];
 		}
 		
 	}
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		MyPagerAdapter adapter = new MyPagerAdapter();
 	    ViewPager myPager = (ViewPager) findViewById(R.id.resultspanelpager);
 	    myPager.setAdapter(adapter);
 	    myPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				super.onPageSelected(position);
 				updateValues();
 			}
 	    });
 	    myPager.setCurrentItem(1);
 	    
 		RadioGroup monthYearKgGroup = (RadioGroup) findViewById(R.id.monthsYearsKgRadioGroup);
 		monthYearKgGroup.setOnCheckedChangeListener(this);
 		EditText weightAgeEditText = (EditText) findViewById(R.id.weightAgeEditText);
 		weightAgeEditText.setOnEditorActionListener(this);
 	}
 
 	/** Updates the displayed values */
 	private void updateValues() {
 		if (findViewById(R.id.weight) == null)
 			return;
 		
 		EditText weightAgeEditText = (EditText) findViewById(R.id.weightAgeEditText);
 		int value = -1;
 		try {
 			value = Integer.parseInt(weightAgeEditText.getText().toString());
 		} catch (NumberFormatException e) {
 			// ignore it, the default value is already invalid
 		}
 		
 		RadioGroup monthYearKgGroup = (RadioGroup) findViewById(R.id.monthsYearsKgRadioGroup);
 		int weight = -1;
 		double age = -1.0;
 		TitleDetailValueView w_formula = (TitleDetailValueView) findViewById(R.id.weight);
 		switch (monthYearKgGroup.getCheckedRadioButtonId()) {
 			case R.id.monthsRadio:
 				if ((value >= 1) && (value <= 12)) {
 					weight = (value/2) + 4;
 					age = value/12.0;
 					w_formula.setDetail(R.string.w_lt_1_formula);
 				}
 				break;
 				
 			case R.id.yearsRadio:
 				if ((value >= 1) && (value <= 5)) {
 					weight = (value*2) + 8;
 					age = (double)value;
 					w_formula.setDetail(R.string.w_1_5_formula);
 				}
 				else if ((value >= 6) && (value <= 12)) {
 					weight = (value*3) + 7;
 					age = (double)value;
 					w_formula.setDetail(R.string.w_6_12_formula);
 				}
 				break;
 			
 			case R.id.kgRadio:
 				if (value >= 1)
 					weight = value;
 				break;
 		}
 		
 		ViewPager myPager = (ViewPager) findViewById(R.id.resultspanelpager);
 		switch (myPager.getCurrentItem())
 		{
 			case 0:
				update5AsValues(weight, age);
 				break;
 			
 			case 1:
				updateWetfagsValues(weight, age);
 				break;
 			
 			case 2:
 				updateParametersValues(weight, age);
 				break;
 		}
 	}
 	
 	private void updateWetfagsValues(int weight, double age) {
 		if (findViewById(R.id.wetfags_rel_layout) == null)
 			return;
 		
 		if (weight == -1) {
 			setTDVText(R.id.weight, R.string.unknown_val);
 			setTDVText(R.id.energy, R.string.unknown_val);
 			setTDVText(R.id.tube, R.string.unknown_val);
 			setTDVText(R.id.fluids, R.string.unknown_val);
 			setTDVText(R.id.adrenaline, R.string.unknown_val);
 			setTDVText(R.id.stesolid, R.string.unknown_val);
 			setTDVText(R.id.stesolid_pr, R.string.unknown_val);
 		} else {
 			setTDVText(R.id.weight, String.format(getString(R.string.w_format), weight));
 			
 			int energy = 4*weight;
 			setTDVText(R.id.energy, String.format(getString(R.string.e_format), energy));
 			
 			if (age < 0.0)
 				setTDVText(R.id.tube, R.string.unknown_val);
 			else
 			{
 				double tube = (age/4.0) + 4.0;
 				setTDVText(R.id.tube, String.format(getString(R.string.t_format), Math.round( tube * 2.0 ) / 2.0));
 			}
 			
 			int fluids_low = 10*weight;
 			int fluids_high = 20*weight;
 			setTDVText(R.id.fluids, String.format(getString(R.string.f_format), fluids_low, fluids_high));
 			
 			double adrenaline_ug = 0.010*weight; // mg
 			double adrenaline_ml = adrenaline_ug / 0.1; // 0.1 mg/ml
 			setTDVText(R.id.adrenaline, String.format(getString(R.string.a_format), adrenaline_ml));
 			
 			int glucose = 2*weight;
 			setTDVText(R.id.glucose, String.format(getString(R.string.g_format), glucose));
 			
 			double stesolid_iv = 0.25*weight;
 			double stesolid_pr = 0.5*weight;
 			setTDVText(R.id.stesolid, String.format(getString(R.string.s_iv_format), stesolid_iv));
 			setTDVText(R.id.stesolid_pr, String.format(getString(R.string.s_pr_format), stesolid_pr));
 		}
 	}
 	
 	private void update5AsValues(int weight, double age)
 	{
 		if (findViewById(R.id.five_as_rel_layout) == null)
 			return;
 
 		double albuterol = 2.5;
 		if (age < 0.0)
 			setTDVText(R.id.a2, R.string.unknown_val);
 		else
 		{
 			if (age >= 5.0) albuterol = 5.0;
 			setTDVText(R.id.a2, String.format(getString(R.string.a2_format), albuterol));
 		}
 		
 		if (weight < 0)
 		{
 			setTDVText(R.id.a4, R.string.unknown_val);
 			setTDVText(R.id.a5, R.string.unknown_val);
 		}
 		else
 		{
 			double atropine = 0.020 * weight;
 			setTDVText(R.id.a4, String.format(getString(R.string.a4_format), atropine));
 			
 			int amiodarone = 5 * weight;
 			setTDVText(R.id.a5, String.format(getString(R.string.a5_format), amiodarone));
 		}
 }
 	
 	private void updateParametersValues(int weight, double age)
 	{
 		if (findViewById(R.id.parameters_rel_layout) == null)
 			return;
 		
 		if (age < 0.0)
 		{
 			setTDVText(R.id.breathing, R.string.unknown_val);
 			setTDVText(R.id.heart, R.string.unknown_val);
 			setTDVText(R.id.pressure, R.string.unknown_val);
 		}
 		else
 		{
 			int age_index = 3;
 			if (age < 1)
 				age_index = 0;
 			else if (age < 6)
 				age_index = 1;
 			else if (age < 12)
 				age_index = 2;
 			setTDVText(R.id.breathing, getResources().getStringArray(R.array.breathing_values)[age_index]);
 			setTDVText(R.id.heart, getResources().getStringArray(R.array.heart_values)[age_index]);
 			setTDVText(R.id.pressure, getResources().getStringArray(R.array.pressure_values)[age_index]);
 		}
 	}
 
 	private void setTDVText(int tdv_id, int string_id)
 	{
 		TitleDetailValueView v = (TitleDetailValueView) findViewById(tdv_id);
 		v.setValue(string_id);
 	}
 	
 	private void setTDVText(int tdv_id, String s)
 	{
 		TitleDetailValueView v = (TitleDetailValueView) findViewById(tdv_id);
 		v.setValue(s);
 	}
 	
 	@Override
 	public void onCheckedChanged(RadioGroup group, int checkedId) {
 		updateValues();
 		hideKeyboard();
 	}
 
 	@Override
 	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		updateValues();
 		hideKeyboard();
 		return true;
 	}
 
 	private void hideKeyboard()
 	{
 		View v = findViewById(R.id.weightAgeEditText);
 		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 	}
 }
