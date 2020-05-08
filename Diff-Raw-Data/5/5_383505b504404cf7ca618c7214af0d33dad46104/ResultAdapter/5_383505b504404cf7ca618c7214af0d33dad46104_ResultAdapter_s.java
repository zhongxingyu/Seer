 package com.example.grubber;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class ResultAdapter extends ArrayAdapter<ResultContent>{
 	private ArrayList<ResultContent> resultsList;
 	
 	public ResultAdapter(Context context, ArrayList<ResultContent> resultsList) {
 		super (context, R.layout.result_list_item, resultsList);
 		this.resultsList = resultsList;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View row = convertView;
 		// if null, inflate/render it
 		if (row == null) {
 			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			row = inflater.inflate(R.layout.result_list_item, null);
 		}
 		
 		ResultContent res = resultsList.get(position);
 		
 		TextView tname = (TextView) row.findViewById(R.id.result_name);
 		TextView taddress = (TextView) row.findViewById(R.id.result_address);
 		TextView tdistance = (TextView) row.findViewById(R.id.result_distance);
 		
 		if (tname != null)
 		{
			String n = rest.getName();
 			if(n.length() > 30)
 				tname.setText(n.substring(0, 30));
 			else
 				tname.setText(n);
 		}
 		
 		if (taddress != null)
 			 taddress.setText(res.getAddress());
 		
 		if (tdistance != null) {
 			 if (res.getDistance().contains("-"))
 				 tdistance.setText("");
 			 else
				 tdistance.setText(res.getDistance().subString(0,4) + " miles");
 		}
 		
 		return row;
 	}
 }
