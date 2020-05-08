 package com.example.mapswithfragments;
 
 import java.text.DecimalFormat;
 
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.internal.widget.IcsAdapterView;
 import com.actionbarsherlock.internal.widget.IcsAdapterView.OnItemSelectedListener;
 
 public class BACFrag extends SherlockFragment {
 	public static final String TAG = "bacfrag";
 
 	public String str = null;
 	private View result;
 
 	static BACFrag newInstance() {
 		BACFrag frag = new BACFrag();
 
 		return (frag);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflate, ViewGroup container,
 			Bundle savedInstanceState) {
 		setRetainInstance(true);
 		setHasOptionsMenu(true);
 		result = inflate.inflate(R.layout.bac_layout, container, false);
 		View calculate = result.findViewById(R.id.calculate);
 		calculate.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				clickCalculate(v);
 			}
 			
 		});
 		
 		Spinner sp = (Spinner) result.findViewById(R.id.spinner1);
 
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 				getActivity(), R.array.items,
 				android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		sp.setAdapter(adapter);
 		sp.setOnItemSelectedListener(new function());
 		return result;
 
 	}
 
 	public class function implements OnItemSelectedListener,
 			android.widget.AdapterView.OnItemSelectedListener {
 
 		public void onItemSelected(AdapterView<?> parent, View arg1, int pos,
 				long id) {
 			// TODO Auto-generated method stub
 			str = parent.getItemAtPosition(pos).toString();
 
 		}
 
 		public void onNothingSelected(AdapterView<?> arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onItemSelected(IcsAdapterView<?> parent, View view,
 				int position, long id) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void onNothingSelected(IcsAdapterView<?> parent) {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 
 	public void clickCalculate(View v) {
 
 		RadioButton m = (RadioButton) result.findViewById(R.id.male);
 		RadioButton f = (RadioButton) result.findViewById(R.id.female);
 		Button results = (Button) result.findViewById(R.id.calculate);
 		String resultTxt = null;
 		String bla = null;
 		double BAC = 0;
 		int weight = 0;
 		double drinksConsumed = 0;
 		int hoursConsumed = 0;
 		int x = 0;
 		int par = 0;
 
 		EditText et = (EditText) result.findViewById(R.id.drinksConsumed);
 		bla = et.getText().toString();
 
 		if (bla.matches("")) {
 
 			x = 4;
 		}
 
 		else {
 			drinksConsumed = Integer.parseInt(et.getText().toString());
 		}
 
 		et = (EditText) result.findViewById(R.id.hoursDrank);
 		bla = et.getText().toString();
 
 		if (bla.matches("")) {
 
 			x = 3;
 		}
 
 		else {
 
 			hoursConsumed = Integer.parseInt(et.getText().toString());
 		}
 
 		et = (EditText) result.findViewById(R.id.weight);
 
 		bla = et.getText().toString();
 
 		if (bla.matches("")) {
 
 			x = 2;
 		}
 
 		else {
 
 			weight = Integer.parseInt(et.getText().toString());
 		}
 
 		if (weight < 88) {
 
 			x = 2;
 		}
 
 		if (m.isChecked() == false && f.isChecked() == false) {
 
 			x = 1;
 		}
 
 		switch (x) {
 
 		case 1:
 			AlertDialog.Builder alertBox = new AlertDialog.Builder(
 					getActivity());
 			alertBox.setMessage("Select a gender.");
 			alertBox.show();
 			par = 1;
 			break;
 		case 2:
 			alertBox = new AlertDialog.Builder(getActivity());
 			alertBox.setMessage("Put a reasonable weight.");
 			alertBox.show();
 			par = 1;
 			break;
 		case 3:
 			alertBox = new AlertDialog.Builder(getActivity());
 			alertBox.setMessage("Put a quantity of hours drinking.");
 			alertBox.show();
 			par = 1;
 			break;
 		case 4:
 			alertBox = new AlertDialog.Builder(getActivity());
 			alertBox.setMessage("Put a number of drinks consumed.");
 			alertBox.show();
 			par = 1;
 			break;
 		}
 
 		if (par != 1) {
 
 			if (m.isChecked() && str.equals("Beer 12 oz")) {
 
 				BAC = calculateBAC(.73, .60, weight, drinksConsumed,
 						hoursConsumed);
 				resultTxt = converterDoubleString(BAC);
 				results.setText("BAC: " + resultTxt + "%");
 
 			}
 
 			else if (m.isChecked() && str.equals("Beer 16 oz")) {
 
 				BAC = calculateBAC(.73, .80, weight, drinksConsumed,
 						hoursConsumed);
 				resultTxt = converterDoubleString(BAC);
 				results.setText("BAC: " + resultTxt + "%");
 
 			}
 
 			else if (m.isChecked() && str.equals("Beer 24 oz")) {
 
 				BAC = calculateBAC(.73, 1.2, weight, drinksConsumed,
 						hoursConsumed);
 				resultTxt = converterDoubleString(BAC);
 				results.setText("BAC: " + resultTxt + "%");
 
 			}
 
 			else if (m.isChecked() && str.equals("Glass of Wine")) {
 
 				BAC = calculateBAC(.73, 0.6, weight, drinksConsumed,
 						hoursConsumed);
 				resultTxt = converterDoubleString(BAC);
 				results.setText("BAC: " + resultTxt + "%");
 
 			}
 
			else if (m.isChecked() && str.equals("Shot with Vodka")) {
 
 				BAC = calculateBAC(.73, 0.5, weight, drinksConsumed,
 						hoursConsumed);
 				resultTxt = converterDoubleString(BAC);
 				results.setText("BAC: " + resultTxt + "%");
 
 			}
 
 			if (f.isChecked() && str.equals("Beer 12 oz")) {
 
 				BAC = calculateBAC(.66, .60, weight, drinksConsumed,
 						hoursConsumed);
 				resultTxt = converterDoubleString(BAC);
 				results.setText("BAC: " + resultTxt + "%");
 
 			}
 
 			else if (f.isChecked() && str.equals("Beer 16 oz")) {
 
 				BAC = calculateBAC(.66, .80, weight, drinksConsumed,
 						hoursConsumed);
 				resultTxt = converterDoubleString(BAC);
 				results.setText("BAC: " + resultTxt + "%");
 
 			}
 
 			else if (f.isChecked() && str.equals("Beer 24 oz")) {
 
 				BAC = calculateBAC(.66, 1.2, weight, drinksConsumed,
 						hoursConsumed);
 				resultTxt = converterDoubleString(BAC);
 				results.setText("BAC: " + resultTxt + "%");
 
 			}
 
 			else if (f.isChecked() && str.equals("Glass of Wine")) {
 
 				BAC = calculateBAC(.66, 0.6, weight, drinksConsumed,
 						hoursConsumed);
 				resultTxt = converterDoubleString(BAC);
 				results.setText("BAC: " + resultTxt + "%");
 
 			}
 
			else if (f.isChecked() && str.equals("Shot with Vodka")) {
 
 				BAC = calculateBAC(.66, 0.5, weight, drinksConsumed,
 						hoursConsumed);
 				resultTxt = converterDoubleString(BAC);
 				results.setText("BAC: " + resultTxt + "%");
 
 			}
 
 		}
 	}
 
 	public static String converterDoubleString(double BAC) {
 
 		DecimalFormat fmt = new DecimalFormat("0.00");
 		String Bac = fmt.format(BAC);
 		return Bac;
 	}
 
 	public double calculateBAC(double rVal, double ounces, int weight,
 			double numberDrinks, int hours) {
 
 		double BAC = (((numberDrinks * ounces * 5.14) / (weight * rVal)) - (.015 * (double) hours));
 		if (BAC > 0) {
 
 			return (BAC);
 		} else {
 			return 0;
 		}
 
 	}
 
 }
