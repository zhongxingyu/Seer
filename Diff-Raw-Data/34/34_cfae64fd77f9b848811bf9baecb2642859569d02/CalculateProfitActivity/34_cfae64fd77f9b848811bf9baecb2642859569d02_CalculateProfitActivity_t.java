 package edu.upenn.cis350.project;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
public class CalculateProfitActivity extends FragmentActivity {
	
	public static class ErrorDialog extends DialogFragment {
		String msg;
		public ErrorDialog(String msg) {
			this.msg = msg;
		}
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage(msg);
	        return builder.create();
	    }
	}
	
	
 	Bundle data;
 	int errors = 0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		data = getIntent().getExtras();
 		setContentView(R.layout.activity_calculate_profit);
 		TextView revenuevalue = (TextView)findViewById(R.id.revenueValue);
 		revenuevalue.setText("$"+data.getDouble("total_cash"));
 		
 		TextView originalCash = (TextView)findViewById(R.id.originalCash);
 		originalCash.setText("Originally, you had $" + data.getDouble("cashbox_starting") + "\n in the cash box");
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_calculate_profit, menu);
 		return true;
 	}
 
 	private double getDoubleFromEditText(int id) {
 		EditText et = (EditText) findViewById(id);
 		return Double.valueOf(et.getText().toString());
 	}
 
 	public void highlightEditTextById(int id, boolean on) {
 		EditText et = (EditText) findViewById(id);
 		et.setBackgroundColor(on ? Color.YELLOW : Color.WHITE);
 	}
 	
 	public void checkCalculations(View v) {
 		DataBaser db = DataBaser.getInstance();
 
 		// Get all the doubles from the user input
 		try {
 			highlightEditTextById(R.id.totalCosts,false);
 			highlightEditTextById(R.id.profit,false);
 			highlightEditTextById(R.id.endingCashbox,false);
 			double revenue = data.getDouble("total_cash");
 			double fruitCosts = getDoubleFromEditText(R.id.fruitStandCost);
 			double smoothieCosts = getDoubleFromEditText(R.id.smoothieCost);
 			double otherCosts = getDoubleFromEditText(R.id.otherCosts);
 			double totalComputed = getDoubleFromEditText(R.id.totalCosts);
 			double donations = getDoubleFromEditText(R.id.donationsAmount);
 			double profitComputed = getDoubleFromEditText(R.id.profit);
 			double endingCashbox = getDoubleFromEditText(R.id.endingCashbox);
 	
 			String[] errorMsgs = {
 					"Looks like there were some differences in your numbers!\n", "", "",
 					"", "Try again\n" };
 	
 			// Only prevent them from moving on the first time.
 			boolean errors = false;
 			double totalReal = fruitCosts + smoothieCosts + otherCosts;
 			if (Math.abs(totalReal - totalComputed) > .05) {
 				errorMsgs[1] = "Total costs should be equal to fruit costs + smoothie costs + other costs.\n";
 				errors = true;
 				highlightEditTextById(R.id.totalCosts,true);
 			}
 			double profitReal = revenue + donations - totalReal;
 			if (Math.abs(profitReal - profitComputed) > .05) {
 				errorMsgs[2] = "Profit should equal revenue + donations - total costs\n";
 				errors = true;
 				highlightEditTextById(R.id.profit,true);
 			}
 			double realEndingCash = revenue + donations
 					+ db.getStartingCashboxAmount();
 			if (Math.abs(endingCashbox - realEndingCash) > .50) {
 				errorMsgs[3] = "Ending cash should equal starting cash + revenue + donations\n"
 						+ "Are you sure you counted all of the cash?\n";
 				errors = true;
 				highlightEditTextById(R.id.endingCashbox,true);
 			}
 			if(errors && this.errors < 3) {
 				this.errors++;
 				if(this.errors == 3) {
 					checkCalculations(null);
 					return;
 				}
 				StringBuffer toastText = new StringBuffer();
 				for(int i=0; i< errorMsgs.length;i++) {
 					toastText.append(errorMsgs[i]);
 				}
 				
				ErrorDialog errs = new ErrorDialog(toastText.toString());
				errs.show(getSupportFragmentManager(), "Error");
 				return;
 			}
 			db.addInfo("revenue", String.valueOf(revenue));
 			db.addInfo("total_costs", String.valueOf(totalReal));
 			db.addInfo("donations", String.valueOf(donations));
 			db.addInfo("profit", String.valueOf(profitReal));
 			db.addInfo("ending_cashbox_amount", String.valueOf(endingCashbox));
 			//saves the data to Parse
 			for(String s: data.keySet()){
 				db.addInfo(s, data.get(s).toString());
 			}
 			DataBaser.getInstance().databaseItThoroughly();
 			db.databaseItThoroughly();
 			Intent i = new Intent(this, FinishedPepActivity.class);
 			i.putExtra("profitable", profitReal > 0);
 			this.startActivity(i);
 		} catch (Exception e) {
 			String toastText = "Looks like you left a box blank, everything has to be filled in before submitting.";
 			Toast.makeText(getApplicationContext(), toastText.toString(), Toast.LENGTH_LONG).show();
 		}
 	}
 
 }
