 package com.michaelnovakjr.numberpicker;
 
 import lorian.graph.android.R;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.preference.DialogPreference;
 import android.preference.PreferenceManager;
 import android.util.AttributeSet;
 import android.view.View;
 
 
 public class NumberPickerPreference extends DialogPreference {
     private NumberPicker mPicker;
     private int mStartRange;
     private int mEndRange;
     private int mCurrentVal;
 
     public NumberPickerPreference(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
 
         if (attrs == null) {
             return;
         }
         
         TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.numberpicker);
         mStartRange = arr.getInteger(R.styleable.numberpicker_startRange, Integer.MIN_VALUE);
         mEndRange = arr.getInteger(R.styleable.numberpicker_endRange, Integer.MAX_VALUE);
        mCurrentVal = PreferenceManager.getDefaultSharedPreferences(context).getInt(getKey(), 0);
         //arr.getInteger(R.styleable.numberpicker_defaultValue, 0);
        
         arr.recycle();
         
         this.setSummary((CharSequence) String.valueOf(mCurrentVal)); 
         setDialogLayoutResource(R.layout.pref_number_picker);
     }
 
     public NumberPickerPreference(Context context, AttributeSet attrs) {
         this(context, attrs, android.R.attr.dialogPreferenceStyle);
     }
 
     public NumberPickerPreference(Context context) {
         this(context, null);
     }
 
     @Override
     protected void onBindDialogView(View view) {
         super.onBindDialogView(view);
         mPicker = (NumberPicker) view.findViewById(R.id.pref_num_picker);
         mPicker.setRange(mStartRange, mEndRange);
         mPicker.setCurrent(mCurrentVal);
     }
 
     @Override
     protected void onDialogClosed(boolean positiveResult) {
         super.onDialogClosed(positiveResult);
 
         final int origValue = getValue();
         final int curValue = mPicker.getCurrent();
 
         if (positiveResult && (curValue != origValue)) {
             if (callChangeListener(curValue)) {
                 saveValue(curValue);
             }
         }
     }
 
     public void setRange(int start, int end) {
         mPicker.setRange(start, end);
     }
 
     private boolean saveValue(int val) {
     	this.mCurrentVal = val;
     	this.setSummary((CharSequence) String.valueOf(mCurrentVal));    
         return persistInt(val);
     }
 
     private int getValue() {
     	return mCurrentVal;
         //return getSharedPreferences().getInt(getKey(), mCurrentVal);
  
 
     }
   
     public void notifyValueChanged(int newValue)
     {
     	saveValue(newValue);
     }
     
 	
     
 }
