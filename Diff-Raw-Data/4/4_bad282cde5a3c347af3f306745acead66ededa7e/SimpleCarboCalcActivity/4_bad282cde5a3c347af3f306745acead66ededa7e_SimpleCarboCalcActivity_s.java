 package com.dnsalias.sanja.simplecarbocalc;
 
 import java.text.DecimalFormat;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.RadioButton;
 
 /**
  * 
  * @author Oleksander "Sanja" Byelkin
  *
  * @brief Simple Carbohydrates calculator: calculate 1 of 3 parameters by other 2 (% of Carbohydrates, Total
  * 		  weight of the product, wiegth of carbohydrates in it in units (1 unit = 1, 10 or 12gr)).
  * 
  * @license GPL V2
  * 
  */
 
 public class SimpleCarboCalcActivity extends Activity {
 	/**
 	 * Name to save preferences of the application (application state)
 	 */
 	public static final String PREFS_NAME = "MyState";
 
 	/**
 	 * Constants to identify saved state
 	 */
 	public static final String STATE_UNIT= "Unit";
 	public static final String STATE_SEQ0= "Sequence0";
 	public static final String STATE_SEQ1= "Sequence1";
 	public static final String STATE_SEQ2= "Sequence1";
 	public static final String STATE_PROC= "Proc";
 	public static final String STATE_TOTAL= "Total";
 	public static final String STATE_CARB= "Carb";
 	
 	/**
 	 * Constant for Activity request code
 	 */
 	private static final int ACTIVITY_SETUP= 1;
 	
 	/**
 	 * Menu constants
 	 */
 	private static final int MENU_SETUP = Menu.FIRST + 1;
     private static final int MENU_ABOUT = MENU_SETUP + 1;
 	
 	/**
 	 * Constants to identify configure options
 	 */
 	public static final String CONFIG_UNIT= "unit_conf";
 	
 
 	/**
 	 * Constants of the 3 calculated parameters
 	 */
 	public static final int N_PROC= 0;  // % of Carbohydrates
 	public static final int N_TOTAL= 1; // Total weight of the product
 	public static final int N_CARB= 2;  // wiegth of carbohydrates
 	
 	public static final int UNIT_FACTOR[]= {1, 10, 12};
 	
 	private int mUnitSetup;
 	private boolean mIsSetupProcess= false;
 	
 	/**
 	 * Text fields
 	 */
 	private EditText mText[]= new EditText[3];
 	/**
 	 * Radio buttons which shows which parametr will be calculated
 	 */
 	private RadioButton mRadioButton[]= new RadioButton[3];
 	/**
 	 * Sequence of the parameters in which they was touched
 	 * (first (index 0) parameter is in focus, last (index 2) parameter is calculating parameter
 	 */
 	private int mSequence[]= {N_PROC, N_TOTAL, N_CARB};
 
 	/**
 	 * Finds index of the View object in the given array
 	 * @param v      - View object to find 
 	 * @param array  - array of View objects where to search
 	 * @return index of the found element or -1
 	 */
 	private int getElementIndex(View v, View[] array)
 	{
 		for(int i=0; i < array.length; i++)
 			if (v == array[i])
 				return i;
 		return -1;
 	}
 	
 	/**
 	 * Listener of focus changing for text fields to detect which parameter should be calculated
 	 */
 	private OnFocusChangeListener mTextListener = new OnFocusChangeListener() {
 		public void onFocusChange(View v, boolean hasFocus) {
 			int i= getElementIndex(v, mText);
 			if (hasFocus && !mIsSetupProcess)
 			{
 				setFocusTo(i);
 			}
 	    }
 	};
 	
 	/**
 	 * Listener of checked state changes for radio buttons for direct pointing of calculated value
 	 */
 	private OnCheckedChangeListener mRadioListener = new OnCheckedChangeListener() {
 		public void onCheckedChanged(CompoundButton v, boolean isChecked) {
 			int i= getElementIndex(v, mRadioButton);
 			
 			if (isChecked && !mIsSetupProcess)
 				setCalculatorTo(i);
 	    }
 	};
 	
 	/**
 	 * Checks that given value is positive otherwise turns it to Double.NaN
 	 * @param val  - value to check
 	 * @return val or Double.NaN if val was negative
 	 */
 	private Double checkNegative(Double val)
 	{
 		if (val.isNaN() || val < 0)
 			val= Double.NaN;
 		return val;
 	}
 	
 	/**
 	 * Fetches numeric value from the given text field and check it
 	 * @param txt - text field to fetch value from
 	 * @return Double - positive numeric value of the field or Double.NaN in case of error
 	 */
 	private Double getPositiveDoubleValue(EditText txt)
 	{
 		Double val;
 		try {
 			val= new Double(txt.getText().toString());
 			val= checkNegative(val);
 		}
 		catch (NumberFormatException ex)
 		{
 			val= Double.NaN;
 		}
 		return val;
 	}
 	
 	/**
 	 * Fetches and checks percent from its field
 	 * @return Double - value of the percent field or Double.NaN in case of error
 	 */
 	private Double getProcent()
 	{
 		Double val= getPositiveDoubleValue(mText[N_PROC]);
 		val= checkNegative(val);
 		if (val.isNaN() || val > 100.00)
 		    val= Double.NaN;
 		else
 			val/= 100;
 		return val;
 	}
 	
 	/**
 	 * Shows value inthe fields that indicates inability to calculate it
 	 * @param txt The field where to show error
 	 */
 	private void setToError(EditText txt)
 	{
 		if (txt.getText().toString().compareTo("#") != 0)
 			txt.setText("#");
 	}
 	
 	/**
 	 * Watcher of text fields changes which recalculate last touched field by other two
 	 */
 	private TextWatcher mTextWatcher= new TextWatcher() {
 	   public void afterTextChanged(Editable s)
 	   {
 		   Double total;
 		   Double carb;
 		   Double proc;
 		   DecimalFormat twoDigitsFormat = new DecimalFormat("#.##");
 		   if (s == mText[mSequence[2]].getText() || mIsSetupProcess)
 			   return; // Avoid infinite loop or setup problems
 		   switch (mSequence[2])
 		   {
 		   case N_PROC:
 			   total= getPositiveDoubleValue(mText[N_TOTAL]);
 			   carb= getPositiveDoubleValue(mText[N_CARB]);
 			   if (carb < 0.001 || total.isNaN() || carb.isNaN())
 				   setToError(mText[N_PROC]);
 			   else
 			   {
 				   carb*= UNIT_FACTOR[mUnitSetup];
 				   mText[N_PROC].setText(twoDigitsFormat.format(new Double(carb * 100 / total)));
 			   }
 			   break;
 		   case N_TOTAL:
 			   proc= getProcent();
 			   carb= getPositiveDoubleValue(mText[N_CARB]);
 			   if (proc < 0.00001 || proc.isNaN() || carb.isNaN())
 				   setToError(mText[N_TOTAL]);
 			   else
 			   {
 				   carb*= UNIT_FACTOR[mUnitSetup];
 				   mText[N_TOTAL].setText(twoDigitsFormat.format(new Double(carb/proc)));
 			   }
 			   break;
 		   case N_CARB:
 			   proc= getProcent();
 			   total= getPositiveDoubleValue(mText[N_TOTAL]);
 			   if (proc.isNaN() || total.isNaN())
 				   setToError(mText[N_CARB]);
 			   else
 				   mText[N_CARB].setText(twoDigitsFormat.format(new Double(total*proc/UNIT_FACTOR[mUnitSetup])));
 			   break;
 		   }
 	
 	   }
 	   public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 	   public void onTextChanged(CharSequence s, int start, int before, int count) {}
 	};
 	
 	/**
 	 * Checks and sets all Radio buttons according to current state of mSequence
 	 * @return
 	 */
 	private boolean setRadio()
 	{
 		boolean changed= false;
 		for(int i= 0; i < mRadioButton.length; i++)
 			if (mRadioButton[i].isChecked() != (i == mSequence[2]))
 			{
 				mRadioButton[i].setChecked(i == mSequence[2]);
 				changed= true;
 			}
 		return changed;
 	}
 	
 	/**
 	 * Moves Focus to the given field
 	 * @param focus - number of the field (pProcN, pTotalN, pCarbN)
 	 * @return true if there was changes
 	 */
 	private boolean setFocusTo(int focus)
 	{
 		if (mSequence[0] != focus)
 		{
 			if (mSequence[1] != focus)
 				mSequence[2]= mSequence[1];
 			mSequence[1]= mSequence[0];
 			mSequence[0]= focus;
 			if (setRadio() && !mText[mSequence[0]].isFocused())
 				mText[mSequence[0]].requestFocus();
 			return true;
 	    }
 		return false;
 	}
 	
 	/**
 	 * Moves calculating pointer to the given field
 	 * @param calc - number of the field (pProcN, pTotalN, pCarbN)
 	 * @return true if there was changes
 	 */
 	private boolean setCalculatorTo(int calc)
 	{
 		if (mSequence[2] != calc)
 		{
 			if (mSequence[1] != calc)
 				mSequence[0]= mSequence[1];
 			mSequence[1]= mSequence[2];
 			mSequence[2]= calc;
 		
 			if (setRadio() && !mText[mSequence[0]].isFocused())
 				mText[mSequence[0]].requestFocus();
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Assigns correct carbohydrates unit text
 	 */
 	void setCarbUnitsName()
 	{
 		Resources res= getResources();
 		CharSequence names[]= res.getTextArray(R.array.UnitName);
 		mRadioButton[N_CARB].setText(names[mUnitSetup]);
 	}
 	
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         /*
          * Find our fields
          */
         mText[N_PROC]= (EditText)findViewById(R.id.editTextProc);
         mText[N_TOTAL]= (EditText)findViewById(R.id.editTextTotal);
         mText[N_CARB]= (EditText)findViewById(R.id.editTextCarb);
         mRadioButton[N_PROC]= (RadioButton) findViewById(R.id.radioButtonProc);
         mRadioButton[N_TOTAL]= (RadioButton) findViewById(R.id.radioButtonTotal);
         mRadioButton[N_CARB]= (RadioButton) findViewById(R.id.radioButtonCarb);
         
         /*
          * Restore state of the application
          */
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         mSequence[0] = settings.getInt(STATE_SEQ0, 0);
         mSequence[1] = settings.getInt(STATE_SEQ1, 1);
         mSequence[2] = settings.getInt(STATE_SEQ2, 2);
         if (mSequence[0] < 0 || mSequence[1] < 0 || mSequence[2] < 0 ||
         		mSequence[0] > 2 || mSequence[1] > 2 || mSequence[2] > 2 ||
         		mSequence[0] == mSequence[1] || mSequence[0] == mSequence[2] ||
         		mSequence[1] == mSequence[2])
         {
         	for (int i= 0; i < 3; i++)
         		mSequence[i]= i;
         }
         mText[N_PROC].setText(settings.getString(STATE_PROC, "12"));
         mText[N_TOTAL].setText(settings.getString(STATE_TOTAL, "100"));
         mText[N_CARB].setText(settings.getString(STATE_CARB, "1"));
         mUnitSetup= settings.getInt(STATE_UNIT, 2);
         
 		/*
 		 * Set listeners
 		 */
         for(int i= 0; i < mText.length; i++)
         {
         	mText[i].setOnFocusChangeListener(mTextListener);
         	mText[i].addTextChangedListener(mTextWatcher);
         	mRadioButton[i].setOnCheckedChangeListener(mRadioListener);
         }
         /*
          * Set initial state of focus and ratio buttons
          */
         setCarbUnitsName();
         setRadio();
         mText[mSequence[0]].requestFocus();
     }
 
     /**
      * Process results of setup
      */
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
         if (intent != null)
         {
         	Bundle bundle= intent.getExtras();
         	if (requestCode == ACTIVITY_SETUP && resultCode == RESULT_OK)
         	{
         		int old_unit= UNIT_FACTOR[mUnitSetup];
         		int new_unit_idx= bundle.getInt(CONFIG_UNIT, -1);
         		int new_unit= UNIT_FACTOR[new_unit_idx];
         		if (new_unit != -1)
         		{
         			Double total= getPositiveDoubleValue(mText[N_CARB]);
         			mUnitSetup= new_unit_idx;
         			if (!total.isNaN())
         			{
         				DecimalFormat twoDigitsFormat = new DecimalFormat("#.##");
         				mIsSetupProcess= true;
         				mText[N_CARB].setText(twoDigitsFormat.format(new Double((total*old_unit)/new_unit)));
         				mIsSetupProcess= false;
         			}
         			saveAppState(); // Save new unit (and everything else)
         			setCarbUnitsName();
         		}
         	}
         }
     }
     
     /*
      * Store state of the application
      */  
     private void saveAppState()
     {
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         SharedPreferences.Editor editor = settings.edit();
         editor.putInt(STATE_SEQ0, mSequence[0]);
         editor.putInt(STATE_SEQ0, mSequence[1]);
         editor.putInt(STATE_SEQ0, mSequence[2]);
         editor.putString(STATE_PROC, mText[N_PROC].getText().toString());
         editor.putString(STATE_TOTAL, mText[N_TOTAL].getText().toString());
         editor.putString(STATE_CARB, mText[N_CARB].getText().toString());
         editor.putInt(STATE_UNIT, mUnitSetup);
         editor.commit(); 	
     }
     
     /**
      * Save state on stopping the application and everything else
      */
     protected void onStop(){
         super.onStop();
 
         saveAppState();
      }
     
     /**
      * Define menu
      */
     @Override
     public boolean onCreateOptionsMenu (Menu menu)
     {
     	menu.add(0, MENU_SETUP, 0, R.string.MenuSetup);
     	menu.add(0, MENU_ABOUT, 0, R.string.MenuAbout);
     	return true;
     }
     
     /**
      * Menu actions
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	Intent intent;
         switch (item.getItemId()) {
             case MENU_SETUP:
             	intent = new Intent(this, SimpleCarboCalcSetup.class);
             	intent.putExtra(CONFIG_UNIT, mUnitSetup);
             	startActivityForResult(intent, ACTIVITY_SETUP);
                 return true;
             case MENU_ABOUT:
             	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setMessage(R.string.About)
             	       .setCancelable(true)
             	       .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
             	           public void onClick(DialogInterface dialog, int id) {
             	                dialog.cancel();
             	           }
             	       });
             	       ;
             	AlertDialog alert = builder.create();
             	alert.show();
            
                 return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 }
