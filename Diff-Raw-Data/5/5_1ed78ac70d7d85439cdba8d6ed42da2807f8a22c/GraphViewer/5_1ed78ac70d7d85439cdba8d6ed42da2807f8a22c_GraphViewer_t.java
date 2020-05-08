 package com.diabetes.app;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 
 import org.achartengine.chart.XYChart;
 
 public class GraphViewer extends Activity {
 	
	private Spinner dataTypeSpinner;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.xy_chart);
		dataTypeSpinner = (Spinner) findViewById(R.id.graphTypeSpin);
 		
 		final String spinnerItems[] = {"Insulin Levels", "Blood Sugar Levels", "Carbohydrate Levels",};
 		ArrayAdapter itemsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems);
 		dataTypeSpinner.setAdapter(itemsAdapter);
 		dataTypeSpinner.setSelection(0);
 		dataTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			
 			public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
 				String dataType = dataTypeSpinner.getSelectedItem().toString();
 				//refreshGraph();
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {}
 		});
 		
 		Button dateSelection = (Button) findViewById(R.id.openDateSelector);
 		dateSelection.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 					startActivityForResult(new Intent(getApplicationContext(), DateSelection.class), 0);			
 			}
 		});
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		Boolean cont = false;
 		if (requestCode == 1) {
 		     if(resultCode == RESULT_OK) {
 		    	 cont = true;
 		     }
 		     else if (resultCode == RESULT_CANCELED)
 		    	 cont = false;
 		}
 		
 		if (cont) {
 	    	 String fromDate = data.getStringExtra("fromDate");
 	    	 String toDate = data.getStringExtra("toDate");
 	    	 String dataType = dataTypeSpinner.getSelectedItem().toString();
 	    	 refreshGraph(fromDate, toDate, dataType);
 		}
 	}
 
 	private void refreshGraph(String fromDate, String toDate, String dataTypeSelection) {
 		//Parse injection_data.csv and begin adding points to graph when the current line < 
 		
 	}
 }
