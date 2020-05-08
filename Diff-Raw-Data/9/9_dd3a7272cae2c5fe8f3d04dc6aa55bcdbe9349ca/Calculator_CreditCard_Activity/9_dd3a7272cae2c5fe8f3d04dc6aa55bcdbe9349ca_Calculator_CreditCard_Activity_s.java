 package com.jeannius.tallycap.Calculator;
 
 import java.text.NumberFormat;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.jeannius.tallycap.R;
 import com.jeannius.tallycap.TrackedActivity;
 import com.jeannius.tallycap.validators.NumberValidator;
 
 public class Calculator_CreditCard_Activity extends TrackedActivity {
 	
 	private EditText currentBalanceEditText, aprEditText, creditLimitEditText, annualFeeEditText, monthlyFeeEditText, overTheLimitFeeEditText;
 	private Spinner annualFeeMonthlySpinner;
 	private ArrayAdapter<CharSequence> yearAdapter;
 	private Button whatif, calculate;
 	private TextView resultTextView;
 	NumberValidator numval = new NumberValidator();
 	private String currentBalanceValid, aprValid, creditLimitValid, annualFeeValid, monthlyFeeValid, overTheLimitFeeValid;
 	private Double currentBalanceNumber, aprNumber, creditLimitNumber, annualFeeNumber, monthlyFeeNumber, OverTheLimitFeeNumber, firstMinPay;
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.calculator_credit_card);
 
 		creditCardSetup();
 	}
 	
 	/**
 	 * *************************************
 	 * This is the original set up
 	 * ************************************
 	 */
 	private void creditCardSetup(){
 		currentBalanceEditText = (EditText) findViewById(R.id.creditCardCurrentBalanceEditText);
 		aprEditText = (EditText) findViewById(R.id.creditCardAprEditText);
 		creditLimitEditText = (EditText) findViewById(R.id.creditCardCreditLimitEditText);
 		annualFeeEditText = (EditText) findViewById(R.id.creditCardAnnualFeeEditText);
 		monthlyFeeEditText = (EditText) findViewById(R.id.creditCardMonthlyFeeEditText);
 		overTheLimitFeeEditText = (EditText) findViewById(R.id.creditCardOverTheLimitFeeEditText);
 		annualFeeMonthlySpinner = (Spinner) findViewById(R.id.creditCardAnnualFeeMonthSpinner);
 		whatif = (Button) findViewById(R.id.creditCardWhatIfButton);
 		calculate = (Button) findViewById(R.id.creditCardCalculateButton);
 		resultTextView = (TextView) findViewById(R.id.creditCardResultTextView);
 		resultTextView.setText("");
 		
 		yearAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.credit_month, R.layout.my_spin);
 		yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
 		annualFeeMonthlySpinner.setAdapter(yearAdapter);
 		whatif.setEnabled(false);
 		calculate.setOnClickListener(calculation);
 		whatif.setOnClickListener(grapher);
 		
 		//final ArrayList<String> mylist = new ArrayList<String>(Arrays.asList( CalculatorLogic.monthArray));
 		
 		
 	}
 	
 	private OnClickListener calculation = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			currentBalanceValid = numval.validate(currentBalanceEditText, 1.0, true, 1000000.0, false);
 			aprValid = numval.validate(aprEditText, 0.1, true, 10000.0,false);
 			if(currentBalanceEditText.getText().length()>0)
 				creditLimitValid = numval.validate(creditLimitEditText, Double.valueOf(currentBalanceEditText.getText().toString()), true, 1000000.0, false);
 			else creditLimitValid = numval.validate(creditLimitEditText, 1.0, true, 1000000.0,false);
			annualFeeValid = numval.validate(annualFeeEditText, 0.0, false, 1000000.0,false);
 			monthlyFeeValid = numval.validate(monthlyFeeEditText, 0.0, false, 1000000.0,false);
 			overTheLimitFeeValid = numval.validate(overTheLimitFeeEditText, 0.0, false, 1000000.0,false);
 			String sh ="";
 			
 			//current balance
 			if(!currentBalanceValid.equals("Good")){
 				currentBalanceEditText.setBackgroundResource(R.drawable.fight);
 				sh += "Current Balance "+ currentBalanceValid+"\n";
 			}
 			else currentBalanceEditText.setBackgroundResource(R.drawable.edit_text);
 			
 			//apr
 			if(!aprValid.equals("Good")){
 				aprEditText.setBackgroundResource(R.drawable.fight);
 				sh += "APR "+ aprValid+"\n";
 			}
 			else aprEditText.setBackgroundResource(R.drawable.edit_text);
 			
 			//credit limit
 			if(!creditLimitValid.equals("Good")){
 				creditLimitEditText.setBackgroundResource(R.drawable.fight);
 				sh += "Credit Limit "+ creditLimitValid+"\n";
 			}
 			else creditLimitEditText.setBackgroundResource(R.drawable.edit_text);
 			
 			//annual fee
 			if(!annualFeeValid.equals("Good")){
 				annualFeeEditText.setBackgroundResource(R.drawable.fight);
 				sh += "Annual Fee "+annualFeeValid+"\n";
 			}
 			else annualFeeEditText.setBackgroundResource(R.drawable.edit_text);
 			
 			//monthly fee
 			if(!monthlyFeeValid.equals("Good")){
 				monthlyFeeEditText.setBackgroundResource(R.drawable.fight);
 				sh += "Monthly Fee "+ monthlyFeeValid+"\n";
 			}
 			else monthlyFeeEditText.setBackgroundResource(R.drawable.edit_text);
 			
 			//over the limit fee
 			if(!overTheLimitFeeValid.equals("Good")){
 				overTheLimitFeeEditText.setBackgroundResource(R.drawable.fight);
 				sh += "Over The Limit Fee "+ overTheLimitFeeValid+"\n";
 			}
 			else overTheLimitFeeEditText.setBackgroundResource(R.drawable.edit_text);		
 			
 			if(sh.length()>0){
 				globalTracker.trackEvent("ui_interaction", "bad_calc", "Calculator_CreditCard_Activity", 0);
 				Toast.makeText(getApplicationContext(), sh,Toast.LENGTH_LONG).show();
 			}
 			else compute();
 		}
 	};
 
 	private void compute(){
 		currentBalanceNumber =Double.valueOf( currentBalanceEditText.getText().toString());
 		
 		aprNumber =Double.valueOf( aprEditText.getText().toString());
 		
 		if(creditLimitEditText.getText().toString().length()>0)creditLimitNumber =Double.valueOf( creditLimitEditText.getText().toString());
 		else creditLimitNumber=0.0;
 		
 		if(annualFeeEditText.getText().toString().length()>0) annualFeeNumber =Double.valueOf( annualFeeEditText.getText().toString());
 		else annualFeeNumber =0.0;
 		
 		if(monthlyFeeEditText.getText().toString().length()>0)monthlyFeeNumber =Double.valueOf( monthlyFeeEditText.getText().toString());
 		else monthlyFeeNumber=0.0;
 		
 		if(overTheLimitFeeEditText.getText().toString().length()>0)OverTheLimitFeeNumber =Double.valueOf( overTheLimitFeeEditText.getText().toString());
 		else OverTheLimitFeeNumber=0.0;
 		
 		globalTracker.trackEvent("ui_interaction", "calculate", "Calculator_CreditCard_Activity", 0);
 		new backgroundCreditCardCalculator().execute();
 		
 	}
 	
 	/**
 	 * 
 	 * The onclick listener for the whatif button
 	 * 
 	 */
 	
 	private OnClickListener grapher = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			Intent myI = new Intent(getApplicationContext(), Calculator_CreditCard_Grapher.class);
 			myI.putExtra("currentBalance", currentBalanceNumber);
 			myI.putExtra("apr", aprNumber);
 			myI.putExtra("creditLimit",creditLimitNumber);
 			myI.putExtra("annualFee", annualFeeNumber);
 			myI.putExtra("monthlyFee", monthlyFeeNumber);
 			myI.putExtra("overTheLimitFee", OverTheLimitFeeNumber);
 			myI.putExtra("monthOfAnnualFee", annualFeeMonthlySpinner.getSelectedItem().toString());
 			myI.putExtra("minPay", firstMinPay);
 			startActivity(myI);
 		}
 	};
 
 /**
  * ***************************************************
  * **************************************************
  * ************************************************
  * This is the Asynctask
  * ***********************************************
  * **************************************************
  * ***************************************************
  */
 
 class backgroundCreditCardCalculator extends AsyncTask<Void, Void, Double>{
 
 	@Override
 	protected Double doInBackground(Void... params) {
 		CalculatorLogic mylogic = new CalculatorLogic();
 		double red=mylogic.creditCardMinimumPaymentCalculator(currentBalanceNumber, aprNumber, monthlyFeeNumber, OverTheLimitFeeNumber, creditLimitNumber);
 		return red;
 	}
 	
 	@Override
 	protected void onPostExecute(Double result) {		
 		super.onPostExecute(result);
 		NumberFormat  nf = NumberFormat.getCurrencyInstance();
 		nf.setGroupingUsed(true);
 		firstMinPay = result;
 		resultTextView.setText("Your first minimum payment is: "+nf.format(result));
 		whatif.setEnabled(true);
 	}
 }
 
 
 }
 
 
 
 
 
