 package com.eddierangel.greatscott;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 public class TipCalculator extends Activity {
 	
 	private static final String BILL_TOTAL = "BILL_TOTAL";
 	private static final String CUSTOM_PERCENT = "CUSTOM_PERCENT";
 	
 	private double currentBillTotal;
 	private int currentCustomPercent;
 	private EditText tipTenEditText;
 	private EditText totalTenEditText;
 	private EditText tipFifteenEditText;
 	private EditText totalFifteenEditText;
 	private EditText tipTwentyEditText;
 	private EditText totalTwentyEditText;
 	private TextView customTipTextView;
 	private EditText tipCustomEditText;
 	private EditText totalCustomEditText;
 	private EditText billEditText;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		if(savedInstanceState == null)
 		{
 			currentBillTotal = 0.0;
 			currentCustomPercent = 18;
 		}
 		else
 		{
 			currentBillTotal = savedInstanceState.getDouble(BILL_TOTAL);
 			currentCustomPercent = savedInstanceState.getInt(CUSTOM_PERCENT);
 		}
 		
 		tipTenEditText = (EditText) findViewById(R.id.tipTenEditText);
 		totalTenEditText = (EditText) findViewById(R.id.totalTenEditText);
 		tipFifteenEditText = (EditText) findViewById(R.id.tipFifteenEditText);
 		totalFifteenEditText = (EditText) findViewById(R.id.totalFifteenEditText);
 		
 		tipTwentyEditText = (EditText) findViewById(R.id.tipTwentyEditText);
 		totalTwentyEditText = (EditText) findViewById(R.id.totalTwentyEditText);
 		
 		customTipTextView = (TextView) findViewById(R.id.customTipTextView);
 		
 		tipCustomEditText = (EditText) findViewById(R.id.tipCustomEditText);
 		totalCustomEditText = (EditText) findViewById(R.id.totalCustomEditText);
 		
 		billEditText = (EditText) findViewById(R.id.billEditText);
 		billEditText.addTextChangedListener(billEditTextWatcher);
 		
 		SeekBar customSeekBar = (SeekBar) findViewById(R.id.customSeekBar);				
 		customSeekBar.setOnSeekBarChangeListener(customSeekBarChangeListener);		
 			
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 		outState.putDouble(BILL_TOTAL, currentBillTotal);
 		outState.putInt(CUSTOM_PERCENT, currentCustomPercent);
 	}
 	
 	private void updateStandard()
 	{
 		double tenPercentTip = currentBillTotal * 0.1;
		double tenPercentTotal = currentBillTotal + tenPercentTip;
 		
 		tipTenEditText.setText(String.format("%.02f", tenPercentTip));		
 		totalTenEditText.setText(String.format("%.02f", tenPercentTotal));
 		
 		double fifteenPercentTip = currentBillTotal * 0.15;
		double fifteenPercentTotal = currentBillTotal + fifteenPercentTip;
 		
 		tipFifteenEditText.setText(String.format("%.02f", fifteenPercentTip));
 		totalFifteenEditText.setText(String.format("%.02f",fifteenPercentTotal));
 		
 		double twentyPercentTip = currentBillTotal * 0.20;
		double twentyPercentTotal = currentBillTotal + twentyPercentTip;
 		
 		tipTwentyEditText.setText(String.format("%.02f", twentyPercentTip));
 		totalTwentyEditText.setText(String.format("%.02f",twentyPercentTotal));		
 		
 	}
 	
 	private void updateCustom()
 	{
 		customTipTextView.setText(currentCustomPercent + "%");
 		double customTipAmount = currentBillTotal * currentCustomPercent * 0.01;		
 		double customTotalAmount = currentBillTotal + customTipAmount;
 		
 		tipCustomEditText.setText(String.format("%.02f", customTipAmount));
 		totalCustomEditText.setText(String.format("%.02f", customTotalAmount));
 		
 	}
 	
 	//TODO
 	private OnSeekBarChangeListener customSeekBarChangeListener = new OnSeekBarChangeListener()
 	{
 		@Override
 		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromuser)
 		{
 			currentCustomPercent = seekBar.getProgress();
 			updateCustom();
 			
 		}
 		
 		@Override
 		public void onStartTrackingTouch(SeekBar seekBar)
 		{
 			//
 		}
 		
 		@Override
 		public void onStopTrackingTouch(SeekBar seekBar)
 		{
 			//
 		}
 	};
 	
 	private TextWatcher billEditTextWatcher = new TextWatcher()
 	{
 
 		@Override
 		public void afterTextChanged(Editable s) 
 		{
 			// TODO Auto-generated method stub			
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after)
 		{
 			// TODO Auto-generated method stub			
 		}
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before, int count)
 		{
 			// TODO Auto-generated method stub
 			try
 			{
 				currentBillTotal = Double.parseDouble(s.toString());				
 			}
 			catch(NumberFormatException e)
 			{
 				currentBillTotal = 0.0;
 			}
 			
 			updateStandard();
 			updateCustom();
 			
 		}
 		
 	};
 	
 	public void billEditTextOnClick(View view)
 	{
 		billEditText.setText("");
 	}
 
 	
 
 	
 	
 
 }
