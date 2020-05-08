 package edu.jhu.isi.hms.visavis;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
import android.text.format.DateFormat;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class StdTestAdapter extends ArrayAdapter<StdTest> {
 	TextView test;
 	TextView date;
 	TextView result;
 	public StdTestAdapter(Context context, int textViewResourceId,
 			List<StdTest> objects) {
 		super(context, textViewResourceId, objects);
 		
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = ((Activity) this.getContext()).getLayoutInflater();
 		View row = convertView;
 		if(null == row){
 			row = inflater.inflate(R.layout.std_tests, parent,false);
 			test = (TextView) row.findViewById(R.id.test_name);
 			date = (TextView) row.findViewById(R.id.test_date);
 			result = (TextView) row.findViewById(R.id.test_result);
 		}
 	
 		StdTest t = this.getItem(position);
 		test.setText(t.testname);
 		date.setText(t.testtime.toLocaleString());
 		result.setText(t.isPositive.toString());
 		return row;	
 	}
 }
