 package com.seawolfsanctuary.tmt;
 
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.view.Gravity;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.TimePicker;
import android.widget.Toast;
 
 public class FromActivity extends Activity {
 	TextView txt_Search, txt_FromSummary;
 	DatePicker dp_Date;
 	TimePicker tp_Time;
 	AutoCompleteTextView actv_Search;
 
 	private String[] read_csv(String filename) {
 		String[] array = {};
 
 		try {
 			InputStream input;
 			input = getAssets().open(filename);
 			int size = input.available();
 			byte[] buffer = new byte[size];
 
 			input.read(buffer);
 			input.close();
 			array = new String(buffer).split("\n");
 
 		} catch (Exception e) {
			String error_msg = "Error reading station list!";
			Toast toast = Toast.makeText(this, error_msg, 50000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
 		}
 
 		return array;
 	}
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tab_from);
 
 		actv_Search = (AutoCompleteTextView) findViewById(R.id.actv_Search);
 
 		// Link array of completions
 		String[] completions = read_csv("stations.lst");
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, completions);
 		actv_Search.setAdapter(adapter);
 		actv_Search.setThreshold(2);
 	}
 
 }
